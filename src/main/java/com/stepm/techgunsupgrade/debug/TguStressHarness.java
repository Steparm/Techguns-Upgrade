package com.stepm.techgunsupgrade.debug;

import com.mojang.authlib.GameProfile;
import com.stepm.techgunsupgrade.TechgunsUpgradeMod;
import com.stepm.techgunsupgrade.event.UpgradeEventHandler;
import com.stepm.techgunsupgrade.manager.UpgradeApplicator;
import com.stepm.techgunsupgrade.upgrade.UpgradeData;
import com.stepm.techgunsupgrade.upgrade.effect.JsonUpgradeDefinitionRegistry;
import com.stepm.techgunsupgrade.upgrade.effect.UpgradeDefinition;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import techguns.capabilities.TGExtendedPlayer;
import techguns.entities.projectiles.GenericProjectile;
import techguns.items.guns.GenericGun;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Bounded dedicated-server stress scenario: ten Forge FakePlayers fire real
 * Techguns weapons for up to one minute. Terrain damage is forced off and all
 * harness-owned targets/projectiles are removed at the end.
 */
@Mod.EventBusSubscriber(modid = TechgunsUpgradeMod.MODID)
public final class TguStressHarness {
    private static final int COOLDOWN_TICKS = 200;
    private static final String[] UPGRADE_IDS = {
            "hmg_apocalypse", "rev_judge", "p90_machine_gun", "lmg_firestorm",
            "min_hurricane_death_ultra", "vec_death_storm", "scar_legendary",
            "br_star_destroyer", "bfg_apocalypse", "ad_nuclear_apocalypse"
    };
    private static Run active;
    private static Report lastReport;

    private TguStressHarness() {
    }

    public static synchronized String start(MinecraftServer server, ICommandSender sender,
                                            int seconds) {
        if (active != null) return "Stress test already running: " + active.ticks + "/"
                + (active.durationTicks + COOLDOWN_TICKS) + " ticks";
        WorldServer world = server.getWorld(0);
        if (world == null) return "Dimension 0 not loaded yet";
        int safeSeconds = Math.max(5, Math.min(60, seconds));
        try {
            active = createRun(world, sender, safeSeconds * 20);
            return "Started: 10 shooters, " + safeSeconds
                    + " seconds, real Techguns projectiles; terrain damage disabled";
        } catch (Throwable error) {
            TechgunsUpgradeMod.LOGGER.error("Cannot start TGU stress harness", error);
            active = null;
            return "Failed to start: " + error.getClass().getSimpleName() + " — "
                    + String.valueOf(error.getMessage());
        }
    }

    public static synchronized String stop() {
        if (active == null) return "Stress test not running";
        finish(active, true);
        return "Stress test stopped; temporary entities removed";
    }

    public static synchronized String status() {
        if (active != null) {
            String phase = active.ticks <= active.durationTicks ? "firing" : "cleanup";
            return "Running (" + phase + "): " + active.ticks + "/"
                    + (active.durationTicks + COOLDOWN_TICKS)
                    + " ticks, shot attempts " + active.shotAttempts
                    + ", projectiles " + active.projectilesSpawned
                    + ", impacts " + active.impacts + ", errors " + active.errors;
        }
        if (lastReport == null) return "Stress test has not run this server session";
        return lastReport.summary();
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Run run;
        synchronized (TguStressHarness.class) {
            run = active;
        }
        if (run == null) return;
        try {
            run.ticks++;
            run.peakAliveEntities = Math.max(run.peakAliveEntities,
                    countAliveEntities(run.world));
            if (run.ticks <= run.durationTicks && (run.ticks - 1) % 4 == 0) fireVolley(run);
            if (run.ticks == run.durationTicks) {
                run.firingElapsedNanos = System.nanoTime() - run.startedNanos;
            }
            if (run.ticks >= run.durationTicks + COOLDOWN_TICKS) finish(run, false);
        } catch (Throwable error) {
            run.errors++;
            TechgunsUpgradeMod.LOGGER.error("TGU stress harness tick failed", error);
            finish(run, true);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        Run run = active;
        if (run != null && event.getEntityLiving().getEntityData()
                .getBoolean("tgu_stress_target")) {
            run.impacts++;
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        Run run = active;
        if (run == null || !(event.getEntity() instanceof GenericProjectile)) return;
        Entity owner = UpgradeEventHandler.getShooter((GenericProjectile) event.getEntity());
        for (Shooter shooter : run.shooters) {
            if (owner == shooter.player) {
                event.getEntity().getEntityData().setBoolean("tgu_stress_projectile", true);
                run.projectilesSpawned++;
                return;
            }
        }
    }

    private static Run createRun(WorldServer world, ICommandSender sender, int durationTicks) {
        boolean previousSafe = DebugSettings.isSafeMode();
        boolean previousForced = DebugSettings.isForceRandomEffects();
        DebugSettings.setSafeMode(true);
        DebugSettings.setForceRandomEffects(true);
        Run run = new Run(world, sender, durationTicks, previousSafe, previousForced,
                countAliveEntities(world));
        BlockPos spawn = world.getSpawnPoint();
        for (int i = 0; i < UPGRADE_IDS.length; i++) {
            UpgradeDefinition definition = JsonUpgradeDefinitionRegistry.get(UPGRADE_IDS[i]);
            if (definition == null) throw new IllegalStateException("Missing " + UPGRADE_IDS[i]);
            Item item = Item.REGISTRY.getObject(new ResourceLocation(definition.getWeaponId()));
            if (!(item instanceof GenericGun)) {
                throw new IllegalStateException("Not a Techguns weapon: "
                        + definition.getWeaponId());
            }
            GenericGun gun = (GenericGun) item;
            ItemStack stack = new ItemStack(gun);
            UpgradeData.addUpgrade(stack, definition.getId());
            UpgradeApplicator.applyUpgradesToGun(stack);
            gun.reloadAmmo(stack, Math.max(1, gun.getClipsize()));

            String name = String.format(Locale.ROOT, "TGUStress%02d", i + 1);
            UUID uuid = UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));
            FakePlayer player = FakePlayerFactory.get(world, new GameProfile(uuid, name));
            player.capabilities.isCreativeMode = true;
            player.capabilities.disableDamage = true;
            double x = spawn.getX() + (i - 4.5) * 6.0;
            double z = spawn.getZ() - 12.0;
            double y = surfaceY(world, x, z);
            player.setPosition(x, y, z);
            player.setHeldItem(EnumHand.MAIN_HAND, stack);
            double targetZ = spawn.getZ() - 4.0;
            EntityLiving target = spawnTarget(world, x, surfaceY(world, x, targetZ),
                    targetZ, i);
            run.shooters.add(new Shooter(player, gun, stack, target));
        }
        run.startedNanos = System.nanoTime();
        run.peakAliveEntities = countAliveEntities(world);
        return run;
    }

    private static void fireVolley(Run run) {
        for (int i = 0; i < run.shooters.size(); i++) {
            Shooter shooter = run.shooters.get(i);
            if (shooter.target == null || !shooter.target.isEntityAlive()) {
                double x = shooter.player.posX;
                double z = run.world.getSpawnPoint().getZ() - 4.0;
                shooter.target = spawnTarget(run.world, x, surfaceY(run.world, x, z), z, i);
            }
            lookAt(shooter.player, shooter.target);
            TGExtendedPlayer extended = TGExtendedPlayer.get(shooter.player);
            if (extended == null) throw new IllegalStateException("Techguns capability missing");
            extended.setFireDelay(EnumHand.MAIN_HAND, 0);
            shooter.gun.reloadAmmo(shooter.stack, Math.max(1, shooter.gun.getClipsize()));
            shooter.gun.shootGunPrimary(shooter.stack, run.world, shooter.player,
                    false, EnumHand.MAIN_HAND, shooter.target);
            run.shotAttempts++;
        }
    }

    private static EntityLiving spawnTarget(WorldServer world, double x, double y,
                                            double z, int index) {
        EntityGiantZombie target = new EntityGiantZombie(world);
        target.setPosition(x, y, z);
        target.setNoAI(true);
        target.setSilent(true);
        target.enablePersistence();
        target.setCustomNameTag("TGU Stress Target " + (index + 1));
        target.getEntityData().setBoolean("tgu_stress_target", true);
        target.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(1024.0);
        target.setHealth(1024.0f);
        world.spawnEntity(target);
        return target;
    }

    private static void lookAt(FakePlayer player, Entity target) {
        double dx = target.posX - player.posX;
        double dz = target.posZ - player.posZ;
        double dy = target.posY + target.height * 0.5
                - (player.posY + player.getEyeHeight());
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        player.rotationYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        player.rotationPitch = (float) -Math.toDegrees(Math.atan2(dy, horizontal));
        player.rotationYawHead = player.rotationYaw;
    }

    private static double surfaceY(WorldServer world, double x, double z) {
        return world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).getY() + 1.0;
    }

    private static synchronized void finish(Run run, boolean cancelled) {
        if (active != run) return;
        long elapsedNanos = Math.max(1L, System.nanoTime() - run.startedNanos);
        long measuredFiringNanos = run.firingElapsedNanos > 0L
                ? run.firingElapsedNanos : elapsedNanos;
        int naturalAlive = countAliveEntities(run.world);
        int naturalGrowth = Math.max(0, naturalAlive - run.baselineAliveEntities);
        cleanup(run);
        double elapsedSeconds = measuredFiringNanos / 1_000_000_000.0;
        int measuredTicks = Math.min(run.ticks, run.durationTicks);
        double tps = Math.min(20.0, measuredTicks / elapsedSeconds);
        int remainingStressEntities = countStressEntities(run.world);
        int peakGrowth = Math.max(0, run.peakAliveEntities - run.baselineAliveEntities);
        lastReport = new Report(cancelled, measuredTicks, run.shotAttempts, run.impacts,
                run.projectilesSpawned, run.errors, tps, peakGrowth, naturalGrowth,
                remainingStressEntities);
        DebugSettings.setSafeMode(run.previousSafeMode);
        DebugSettings.setForceRandomEffects(run.previousForcedRandom);
        active = null;
        String summary = lastReport.summary();
        run.sender.sendMessage(new TextComponentString(TextFormatting.GOLD
                + "[TGU] " + summary));
    }

    private static void cleanup(Run run) {
        for (Entity entity : new ArrayList<>(run.world.loadedEntityList)) {
            if (entity.getEntityData().getBoolean("tgu_stress_target")
                    || entity.getEntityData().getBoolean("tgu_stress_projectile")) {
                entity.setDead();
            } else if (entity instanceof GenericProjectile) {
                Entity owner = UpgradeEventHandler.getShooter((GenericProjectile) entity);
                for (Shooter shooter : run.shooters) {
                    if (owner == shooter.player) {
                        entity.setDead();
                        break;
                    }
                }
            }
        }
        for (Shooter shooter : run.shooters) shooter.player.setDead();
    }

    private static int countAliveEntities(WorldServer world) {
        int count = 0;
        for (Entity entity : world.loadedEntityList) if (!entity.isDead) count++;
        return count;
    }

    private static int countStressEntities(WorldServer world) {
        int count = 0;
        for (Entity entity : world.loadedEntityList) {
            if (!entity.isDead && (entity.getEntityData().getBoolean("tgu_stress_target")
                    || entity.getEntityData().getBoolean("tgu_stress_projectile"))) count++;
        }
        return count;
    }

    private static final class Run {
        private final WorldServer world;
        private final ICommandSender sender;
        private final int durationTicks;
        private final boolean previousSafeMode;
        private final boolean previousForcedRandom;
        private final int baselineAliveEntities;
        private final List<Shooter> shooters = new ArrayList<>();
        private long startedNanos;
        private long firingElapsedNanos;
        private int ticks;
        private int shotAttempts;
        private int projectilesSpawned;
        private int impacts;
        private int errors;
        private int peakAliveEntities;

        private Run(WorldServer world, ICommandSender sender, int durationTicks,
                    boolean previousSafeMode, boolean previousForcedRandom,
                    int baselineAliveEntities) {
            this.world = world;
            this.sender = sender;
            this.durationTicks = durationTicks;
            this.previousSafeMode = previousSafeMode;
            this.previousForcedRandom = previousForcedRandom;
            this.baselineAliveEntities = baselineAliveEntities;
        }
    }

    private static final class Shooter {
        private final FakePlayer player;
        private final GenericGun gun;
        private final ItemStack stack;
        private EntityLiving target;

        private Shooter(FakePlayer player, GenericGun gun, ItemStack stack, EntityLiving target) {
            this.player = player;
            this.gun = gun;
            this.stack = stack;
            this.target = target;
        }
    }

    private static final class Report {
        private final boolean cancelled;
        private final int ticks;
        private final int shots;
        private final int impacts;
        private final int projectiles;
        private final int errors;
        private final double tps;
        private final int peakGrowth;
        private final int naturalGrowth;
        private final int finalGrowth;

        private Report(boolean cancelled, int ticks, int shots, int impacts, int projectiles,
                       int errors,
                       double tps, int peakGrowth, int naturalGrowth, int finalGrowth) {
            this.cancelled = cancelled;
            this.ticks = ticks;
            this.shots = shots;
            this.impacts = impacts;
            this.projectiles = projectiles;
            this.errors = errors;
            this.tps = tps;
            this.peakGrowth = peakGrowth;
            this.naturalGrowth = naturalGrowth;
            this.finalGrowth = finalGrowth;
        }

        private String summary() {
            boolean boundedPerShot = peakGrowth <= shots * 2 + 100;
            boolean stoppedGrowing = naturalGrowth <= peakGrowth + 10;
            boolean passed = !cancelled && ticks >= 1200 && tps >= 18.0
                    && impacts > 0 && errors == 0 && boundedPerShot && stoppedGrowing
                    && finalGrowth == 0;
            return (cancelled ? "CANCELLED" : passed ? "PASSED" : "FAILED")
                    + ": TPS=" + String.format(Locale.ROOT, "%.2f", tps)
                    + ", shots=" + shots + ", projectiles=" + projectiles
                    + ", impacts=" + impacts
                    + ", errors=" + errors
                    + ", peak entities=+" + peakGrowth
                    + ", after 10s=+" + naturalGrowth
                    + ", test entities after cleanup=" + finalGrowth;
        }
    }
}
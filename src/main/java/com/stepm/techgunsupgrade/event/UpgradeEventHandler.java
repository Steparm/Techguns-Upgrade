package com.stepm.techgunsupgrade.event;

import com.stepm.techgunsupgrade.TechgunsUpgradeMod;
import com.stepm.techgunsupgrade.apocalypse.ZombieApocalypseManager;
import com.stepm.techgunsupgrade.debug.DebugRandom;
import com.stepm.techgunsupgrade.debug.DebugSettings;
import com.stepm.techgunsupgrade.debug.SafeModeAccess;
import com.stepm.techgunsupgrade.config.TguConfig;
import com.stepm.techgunsupgrade.manager.UpgradeApplicator;
import com.stepm.techgunsupgrade.upgrade.GunStatModifiers;
import com.stepm.techgunsupgrade.upgrade.effect.EffectAction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import techguns.entities.projectiles.EnumBulletFirePos;
import techguns.entities.projectiles.BioGunProjectile;
import techguns.entities.projectiles.FlamethrowerProjectile;
import techguns.entities.projectiles.GenericProjectile;
import techguns.entities.projectiles.GuidedMissileProjectile;
import techguns.TGRadiationSystem;
import techguns.capabilities.TGExtendedPlayer;
import techguns.items.guns.GenericGun;
import techguns.items.guns.GuidedMissileLauncher;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = TechgunsUpgradeMod.MODID)
public class UpgradeEventHandler {

    private static final Random RANDOM = new DebugRandom();
    private static final String PROJECTILE_CONTEXT = "tgu_shot_context";
    private static final String STACK_ID = "stack_id";
    private static final int CLUSTER_CHILD_MAX_TICKS = 8;
    private static final double CLUSTER_CHILD_MAX_DISTANCE_SQ = 16.0;
    private static final ThreadLocal<ShotContext> PROJECTILE_GUN_CONTEXT = new ThreadLocal<>();
    private static int tickCounter = 0;
    private static final UUID COMMON_MOVEMENT_SPEED =
            UUID.fromString("0d973db4-0294-4592-b4b8-14c25fe44452");
    private static final UUID EPIC_TEMPORARY_ATTACK_SPEED =
            UUID.fromString("f1ac9d75-0d1b-4f65-94bc-a2f783641a20");

    private static Field shooterField;
    private static Field projDamageField;
    private static Field projDamageMinField;
    private static Field projDamageDropStartField;
    private static Field projDamageDropEndField;
    private static Field projPenetrationField;
    private static Field projTicksToLiveField;
    private static Field projBlockdamageField;
    private static Field projSilencedField;
    private static Field projRadiusField;

    private static final Set<GenericProjectile> clonedProjectiles = Collections.newSetFromMap(new WeakHashMap<>());

    static {
        try {
            shooterField = GenericProjectile.class.getDeclaredField("shooter");
            shooterField.setAccessible(true);
        } catch (Exception e) {
            TechgunsUpgradeMod.LOGGER.warn("Failed to init shooter field: " + e.getMessage());
        }
        try {
            projDamageField = GenericProjectile.class.getDeclaredField("damage");
            projDamageField.setAccessible(true);
            projDamageMinField = GenericProjectile.class.getDeclaredField("damageMin");
            projDamageMinField.setAccessible(true);
            projDamageDropStartField = GenericProjectile.class.getDeclaredField("damageDropStart");
            projDamageDropStartField.setAccessible(true);
            projDamageDropEndField = GenericProjectile.class.getDeclaredField("damageDropEnd");
            projDamageDropEndField.setAccessible(true);
            projPenetrationField = GenericProjectile.class.getDeclaredField("penetration");
            projPenetrationField.setAccessible(true);
            projTicksToLiveField = GenericProjectile.class.getDeclaredField("ticksToLive");
            projTicksToLiveField.setAccessible(true);
            projBlockdamageField = GenericProjectile.class.getDeclaredField("blockdamage");
            projBlockdamageField.setAccessible(true);
            projSilencedField = GenericProjectile.class.getDeclaredField("silenced");
            projSilencedField.setAccessible(true);
            projRadiusField = GenericProjectile.class.getDeclaredField("radius");
            projRadiusField.setAccessible(true);
        } catch (Exception e) {
            TechgunsUpgradeMod.LOGGER.warn("Failed to init GenericProjectile reflection fields: " + e.getMessage());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerLoggedInEvent event) {
        if (event.player == null || event.player.world.isRemote) return;
        applyBuffsToPlayer(event.player);
    }

    @SubscribeEvent
    public static void onPlayerLoad(PlayerEvent.LoadFromFile event) {
        if (event.getEntityPlayer() == null || event.getEntityPlayer().world.isRemote) return;
        applyBuffsToPlayer(event.getEntityPlayer());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (event.player == null || event.player.world.isRemote) return;

        updateHeldMovementBonus(event.player);
        updateMovementHistory(event.player);
        updateTemporaryAttackSpeed(event.player);
        updateLockOnProgress(event.player);
        updateUltraHeldEffects(event.player);

        tickCounter++;
        if (tickCounter % 20 != 0) return;

        ItemStack gun = event.player.getHeldItemMainhand();
        if (gun.isEmpty() || !(gun.getItem() instanceof GenericGun)) return;

        if (UpgradeApplicator.needsRefresh(gun)) {
            UpgradeApplicator.applyUpgradesToGun(gun);
        }
    }

    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        if (event.getEntityPlayer() == null || event.getEntityPlayer().world.isRemote) return;
        ItemStack stack = event.getItem().getItem();
        if (!stack.isEmpty() && stack.getItem() instanceof GenericGun) {
            if (UpgradeApplicator.needsRefresh(stack)) {
                UpgradeApplicator.applyUpgradesToGun(stack);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() == null || event.getWorld().isRemote) return;

        if (event.getEntity() instanceof net.minecraft.entity.item.EntityItem) {
            net.minecraft.entity.item.EntityItem entityItem = (net.minecraft.entity.item.EntityItem) event.getEntity();
            ItemStack stack = entityItem.getItem();
            if (!stack.isEmpty() && stack.getItem() instanceof GenericGun) {
                if (UpgradeApplicator.needsRefresh(stack)) {
                    UpgradeApplicator.applyUpgradesToGun(stack);
                }
            }
            return;
        }

        if (event.getEntity() instanceof GenericProjectile) {
            handleProjectileSpawn((GenericProjectile) event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        applyStoredDamageOverTimeBonus(event, source);
        if ("tgu_chain_beam".equals(source.damageType)
                || "tgu_mythic_area".equals(source.damageType)
                || "tgu_ultra_zone".equals(source.damageType)
                || "tgu_ultra_execute".equals(source.damageType)
                || "tgu_railgun".equals(source.damageType)) return;
        Entity trueSource = source.getTrueSource();

        if (!(trueSource instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) trueSource;
        ItemStack gun = gunForDamageSource(player, source);
        if (gun.isEmpty() || !(gun.getItem() instanceof GenericGun)) return;

        NBTTagCompound tag = gun.getOrCreateSubCompound("techgunsupgrade");
        if (!tag.getBoolean("buffs_applied")) return;

        EntityLivingBase target = event.getEntityLiving();

        float damage = event.getAmount();
        float modifiedDamage = damage;

        if (!(source.getImmediateSource() instanceof GenericProjectile)) {
            modifiedDamage = getModifiedDamage(gun, damage);
            modifiedDamage *= GunStatModifiers.randomDamageMultiplier(gun, RANDOM);
        }

        modifiedDamage *= GunStatModifiers.conditionalDamageMultiplier(gun, player, target);
        modifiedDamage = applyEpicHitDamage(gun, tag, player, target, source, modifiedDamage);
        modifiedDamage = applyMythicHitDamage(gun, tag, player, target, source, modifiedDamage);
        modifiedDamage = applyUltraHitDamage(gun, tag, player, target, source, modifiedDamage);

        if (tag.getBoolean("techguns_critical")) {
            float chance = tag.getFloat("techguns_critical_chance");
            float mult = tag.getFloat("techguns_critical_multiplier");
            if (DebugSettings.roll(RANDOM, chance)) {
                modifiedDamage *= mult;
            }
        }

        if (modifiedDamage != damage) {
            event.setAmount(modifiedDamage);
        }

        if (target != null && target != player) {
            World world = player.world;
            double x = target.posX, y = target.posY, z = target.posZ;

            if (source.getImmediateSource() instanceof FlamethrowerProjectile) {
                rememberDamageOverTimeBonus(target, "tgu_burn_multiplier", "tgu_burn_until",
                        GunStatModifiers.burnDamageMultiplier(gun), 200);
                rememberConditionalBurnBonus(target, tag);
                float bypass = tag.getFloat("fire_resistance_bypass");
                if (bypass > 0.0f && target.isPotionActive(MobEffects.FIRE_RESISTANCE)) {
                    target.attackEntityFrom(DamageSource.MAGIC, modifiedDamage * bypass);
                }
            }
            if (source.getImmediateSource() instanceof BioGunProjectile) {
                float poisonMultiplier = GunStatModifiers.poisonDamageMultiplier(gun)
                        * conditionalPoisonMultiplier(tag,
                        (GenericProjectile) source.getImmediateSource(), target);
                rememberDamageOverTimeBonus(target, "tgu_poison_multiplier", "tgu_poison_until",
                        poisonMultiplier, 120);
            }

            if (tag.getBoolean("techguns_explosive")) {
                SpecialEffectsHandler.handleExplosive(world, x, y, z, tag.getFloat("techguns_explosive_power"));
            }
            if (tag.getBoolean("techguns_incendiary")) {
                target.setFire(Math.max(1, tag.getInteger("techguns_incendiary_duration") / 20));
            }
            if (tag.getBoolean("techguns_freeze")) {
                SpecialEffectsHandler.handleFreeze(world, x, y, z, tag.getInteger("techguns_freeze_duration"), player);
            }
            if (tag.getBoolean("techguns_lightning")) {
                SpecialEffectsHandler.handleLightning(world, x, y, z, player);
            }
            if (tag.getBoolean("techguns_chain_lightning")) {
                SpecialEffectsHandler.handleChainLightning(world, x, y, z, tag.getInteger("techguns_chain_targets"), player);
            }
            if (tag.getBoolean("techguns_hunter")) {
                SpecialEffectsHandler.handleHunter(world, x, y, z, tag.getFloat("techguns_hunter_multiplier"), player);
            }
            if (tag.getBoolean("techguns_knockback")) {
                applyKnockback(target, player, tag.getFloat("techguns_knockback_strength"));
            }
            float knockbackMultiplier = tag.hasKey("mod_knockback_multiplier")
                    ? tag.getFloat("mod_knockback_multiplier") : 1.0f;
            if (knockbackMultiplier > 1.0f) {
                applyKnockback(target, player, knockbackMultiplier - 1.0f);
            }
            if (tag.getBoolean("techguns_poison")) {
                target.addPotionEffect(new PotionEffect(MobEffects.POISON, tag.getInteger("techguns_poison_duration"), 0));
            }

            int incendiaryDuration = tag.getInteger("incendiary_duration");
            if (incendiaryDuration > 0) {
                target.setFire(Math.max(1, incendiaryDuration / 20));
            }

            float explosionPower = tag.getFloat("explosion_power");
            if (explosionPower > 0.0f) {
                SpecialEffectsHandler.handleExplosive(world, x, y, z, explosionPower);
            }

            float randomExplosionChance = tag.getFloat("random_explosion_chance");
            if (DebugSettings.roll(RANDOM, randomExplosionChance)) {
                SpecialEffectsHandler.handleExplosive(world, x, y, z,
                        tag.getFloat("random_explosion_power"));
            }

            int glowDuration = tag.getInteger("glow_duration");
            if (glowDuration > 0) {
                target.addPotionEffect(new PotionEffect(MobEffects.GLOWING, glowDuration, 0));
            }

            int stunDuration = tag.getInteger("stun_duration");
            if (stunDuration > 0) {
                target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, stunDuration, 4));
                target.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, stunDuration, 2));
            }

            int poisonDurationBonus = tag.getInteger("poison_duration_add");
            if (poisonDurationBonus > 0) {
                target.addPotionEffect(new PotionEffect(MobEffects.POISON,
                        100 + poisonDurationBonus, 3));
            }

            float contagionChance = tag.getFloat("contagion_chance");
            if (DebugSettings.roll(RANDOM, contagionChance)) {
                applyContagion(target, player, 3.0, 100 + poisonDurationBonus);
            }

            int radiationDuration = tag.getInteger("radiation_duration");
            if (radiationDuration > 0 && TGRadiationSystem.radiation_effect != null) {
                target.addPotionEffect(new PotionEffect(TGRadiationSystem.radiation_effect,
                        radiationDuration, 0));
            }

            float pullRange = tag.getFloat("pull_target_range");
            if (pullRange > 0.0f && !(source.getImmediateSource() instanceof GenericProjectile)
                    && player.getDistance(target) <= pullRange) {
                pullTargetTowardPlayer(target, player);
            }

            if (tag.getFloat("quiet_kill_radius") > 0.0f
                    && source.getImmediateSource() instanceof GenericProjectile
                    && modifiedDamage >= target.getHealth()) {
                target.getEntityData().setLong("tgu_quiet_one_shot_until",
                        target.world.getTotalWorldTime() + 2);
            }

            int chainBeamTargets = tag.getInteger("chain_beam_targets");
            if (chainBeamTargets > 0 && source.getImmediateSource() instanceof GenericProjectile) {
                applyChainBeam((GenericProjectile) source.getImmediateSource(), player,
                        target, modifiedDamage, chainBeamTargets);
            }

            if (source.getImmediateSource() instanceof GenericProjectile) {
                GenericProjectile projectile = (GenericProjectile) source.getImmediateSource();
                int randomStun = projectile.getEntityData().getInteger(
                        "tgu_random_damage_stun_duration");
                if (randomStun <= 0) randomStun = tag.getInteger("random_damage_stun_duration");
                if (randomStun > 0 && projectile.getEntityData().getBoolean(
                        "tgu_random_damage_triggered")) {
                    target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, randomStun, 4));
                    target.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE,
                            randomStun, 2));
                }
                if (tag.getFloat("railgun_pierce_range") > 0.0f
                        && !projectile.getEntityData().getBoolean("tgu_railgun_applied")) {
                    projectile.getEntityData().setBoolean("tgu_railgun_applied", true);
                    applyRailgunPierce(projectile, player, target, modifiedDamage,
                            tag.getFloat("railgun_pierce_range"));
                }
            }

            float shockwaveRadius = GunStatModifiers.shockwaveRadius(gun);
            if (shockwaveRadius > 0.0f && !(source.getImmediateSource() instanceof GenericProjectile)) {
                applyShockwave(player, target, shockwaveRadius);
            }
        }
        syncRuntimeState(gun, player);
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player == null) return;
        float multiplier = GunStatModifiers.miningSpeedMultiplier(player.getHeldItemMainhand());
        if (multiplier != 1.0f) event.setNewSpeed(event.getOriginalSpeed() * multiplier);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (ZombieApocalypseManager.isApocalypseActiveServer()) {
            Entity killed = event.getEntity();
            Entity trueSource = event.getSource().getTrueSource();

            boolean isTechgunsMob = killed instanceof techguns.entities.npcs.GenericNPC ||
                                    killed instanceof techguns.entities.npcs.GenericNPCUndead ||
                                    killed instanceof techguns.entities.npcs.ITGSpawnerNPC ||
                                    killed instanceof techguns.entities.npcs.AttackHelicopter ||
                                    killed instanceof techguns.entities.npcs.ZombieSoldier ||
                                    killed instanceof techguns.entities.npcs.Bandit ||
                                    killed instanceof techguns.entities.npcs.ZombieMiner ||
                                    killed instanceof techguns.entities.npcs.ZombieFarmer ||
                                    killed instanceof techguns.entities.npcs.ZombiePoliceman ||
                                    killed instanceof techguns.entities.npcs.ZombiePigmanSoldier ||
                                    killed instanceof techguns.entities.npcs.SkeletonSoldier ||
                                    killed instanceof techguns.entities.npcs.Commando ||
                                    killed instanceof techguns.entities.npcs.ArmySoldier ||
                                    killed instanceof techguns.entities.npcs.StormTrooper ||
                                    killed instanceof techguns.entities.npcs.Outcast ||
                                    killed instanceof techguns.entities.npcs.PsychoSteve ||
                                    killed instanceof techguns.entities.npcs.DictatorDave ||
                                    killed instanceof techguns.entities.npcs.CyberDemon ||
                                    killed instanceof techguns.entities.npcs.SuperMutantBasic ||
                                    killed instanceof techguns.entities.npcs.SuperMutantHeavy ||
                                    killed instanceof techguns.entities.npcs.SuperMutantElite ||
                                    killed instanceof techguns.entities.npcs.Ghastling ||
                                    killed instanceof techguns.entities.npcs.AlienBug;

            if (isTechgunsMob) {
                if (trueSource instanceof EntityPlayer) {
                    ZombieApocalypseManager.onMobDeath(killed.world, (EntityPlayer) trueSource);
                }
            }
        }

        DamageSource source = event.getSource();
        if ("tgu_mythic_area".equals(source.damageType)
                || "tgu_ultra_zone".equals(source.damageType)
                || "tgu_ultra_execute".equals(source.damageType)) return;
        Entity trueSource = source.getTrueSource();
        EntityLivingBase killed = event.getEntityLiving();

        if (trueSource instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) trueSource;
            ItemStack gun = gunForDamageSource(player, source);
            if (!gun.isEmpty()) {
                NBTTagCompound tag = gun.getOrCreateSubCompound("techgunsupgrade");
                ItemStack liveGun = findLiveGun(player, tag.getString(STACK_ID));
                if (liveGun.isEmpty()) liveGun = gun;

                applyMythicStacks(tag);
                applyMythicKillEffects(tag, liveGun, player, killed, source);
                applyUltraKillEffects(tag, gun, player, killed, source);

                float goldDropChance = tag.getFloat("gold_drop_chance");
                if (DebugSettings.roll(RANDOM, goldDropChance)) {
                    killed.entityDropItem(new ItemStack(Items.GOLD_INGOT), 0.5f);
                }

                float fuelRestoreChance = tag.getFloat("fuel_restore_chance");
                if (DebugSettings.roll(RANDOM, fuelRestoreChance)) {
                    restoreAmmo(liveGun, tag.getInteger("fuel_restore_amount"));
                }

                float pyromaniacPower = tag.getFloat("pyromaniac_explosion_power");
                if (pyromaniacPower > 0.0f) {
                    SpecialEffectsHandler.handleExplosive(player.world, killed.posX, killed.posY,
                            killed.posZ, pyromaniacPower);
                    SpecialEffectsHandler.handleIncendiary(player.world, killed.posX, killed.posY,
                            killed.posZ, 100);
                }

                float quietRadius = tag.getFloat("quiet_kill_radius");
                if (quietRadius > 0.0f && killed.getEntityData().getLong("tgu_quiet_one_shot_until")
                        >= killed.world.getTotalWorldTime()) {
                    calmNearbyMobs(killed, quietRadius, player);
                }

                float killSpeed = tag.hasKey("kill_attack_speed_multiplier")
                        ? tag.getFloat("kill_attack_speed_multiplier") : 1.0f;
                int killSpeedDuration = tag.getInteger("kill_attack_speed_duration");
                if (killSpeed > 1.0f && killSpeedDuration > 0
                        && (!tag.getBoolean("kill_attack_speed_continuous")
                        || player.isHandActive())) {
                    applyTemporaryAttackSpeed(player, killSpeed, killSpeedDuration);
                }

                float killHeal = tag.getFloat("kill_heal_amount");
                if (killHeal > 0.0f) {
                    player.heal(killHeal);
                    SpecialEffectsHandler.spawnHeartParticles(player.world,
                            player.posX, player.posY + 1.0, player.posZ);
                }

                float invisibilityChance = tag.getFloat("kill_invisibility_chance");
                int invisibilityDuration = tag.getInteger("kill_invisibility_duration");
                if (invisibilityChance > 0.0f && invisibilityDuration > 0
                        && DebugSettings.roll(RANDOM, invisibilityChance)) {
                    player.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY,
                            invisibilityDuration, 0, false, false));
                }

                if (tag.getBoolean("techguns_gold_drop")) {
                    SpecialEffectsHandler.handleGoldDrop(killed);
                }

                if (tag.getBoolean("techguns_invisibility")) {
                    SpecialEffectsHandler.handleInvisibility(player);
                }

                if (tag.getBoolean("techguns_death_scythe")) {
                    SpecialEffectsHandler.handleDeathScythe(player.world, killed.posX, killed.posY, killed.posZ, player);
                }

                if (tag.getBoolean("techguns_doomsaw")) {
                    SpecialEffectsHandler.handleDoomsaw(player.world, killed.posX, killed.posY, killed.posZ);
                }

                if (tag.getBoolean("techguns_armageddon")) {
                    SpecialEffectsHandler.handleArmageddon(player.world, killed.posX, killed.posY, killed.posZ);
                }

                if (tag.getBoolean("techguns_annihilator")) {
                    SpecialEffectsHandler.handleAnnihilator(player.world, killed.posX, killed.posY, killed.posZ);
                }

                if (tag.getBoolean("techguns_apocalypse")) {
                    SpecialEffectsHandler.handleApocalypse(player.world, killed.posX, killed.posY, killed.posZ);
                }

                if (tag.getBoolean("techguns_hell_duet")) {
                    SpecialEffectsHandler.handleHellDuet(player.world, killed.posX, killed.posY, killed.posZ);
                }

                if (tag.getBoolean("techguns_legendary")) {
                    SpecialEffectsHandler.handleLegendary(player.world, killed.posX, killed.posY, killed.posZ, player);
                }

                if (tag.getBoolean("techguns_stacking")) {
                    applyStackingBuff(liveGun, liveGun.getOrCreateSubCompound("techgunsupgrade"));
                }

                if (tag.getBoolean("techguns_vampire")) {
                    int healAmount = tag.getInteger("techguns_vampire_heal");
                    if (healAmount <= 0) healAmount = 4;
                    player.heal(healAmount);
                    SpecialEffectsHandler.spawnHeartParticles(player.world, player.posX, player.posY + 1, player.posZ);
                }
                syncRuntimeState(gun, player);
            }
        }
    }
       public static void beginProjectileConstruction(ItemStack gun) {
        beginProjectileConstruction(gun, null, null);
    }

    public static void captureTriggerPull(ItemStack gun, long tick, int ammoBefore) {
        if (gun == null || gun.isEmpty()) return;
        NBTTagCompound tag = gun.getOrCreateSubCompound("techgunsupgrade");
        tag.setLong("runtime_trigger_tick", tick);
        tag.setInteger("runtime_trigger_ammo_before", Math.max(0, ammoBefore));
    }

    public static void beginProjectileConstruction(ItemStack gun,
                                                   EntityLivingBase shooter,
                                                   Entity target) {
        if (gun == null || gun.isEmpty()) {
            PROJECTILE_GUN_CONTEXT.remove();
            return;
        }
        NBTTagCompound upgradeTag = gun.getOrCreateSubCompound("techgunsupgrade");
        String stackId = ensureStackId(upgradeTag);
        long shotTick = shooter == null || shooter.world == null
                ? 0L : shooter.world.getTotalWorldTime();
        int currentAmmo = 0;
        int clipSize = 0;
        if (gun.getItem() instanceof GenericGun) {
            GenericGun gunItem = (GenericGun) gun.getItem();
            currentAmmo = gunItem.getCurrentAmmo(gun);
            clipSize = GunStatModifiers.clipSize(gun, gunItem.getClipsize());
        }
        int ammoBefore = upgradeTag.getLong("runtime_trigger_tick") == shotTick
                ? upgradeTag.getInteger("runtime_trigger_ammo_before")
                : Math.min(clipSize, currentAmmo + 1);
        int[] state = registerProjectileShot(upgradeTag, currentAmmo, shotTick);
        int projectileOrdinal = upgradeTag.getLong("runtime_projectile_ordinal_tick") == shotTick
                ? upgradeTag.getInteger("runtime_projectile_ordinal") + 1 : 0;
        upgradeTag.setLong("runtime_projectile_ordinal_tick", shotTick);
        upgradeTag.setInteger("runtime_projectile_ordinal", projectileOrdinal);
        PROJECTILE_GUN_CONTEXT.set(new ShotContext(gun, stackId, state[0], state[1],
                state[2], ammoBefore, currentAmmo, clipSize, shotTick,
                target == null ? -1 : target.getEntityId(), projectileOrdinal));
    }

    public static void endProjectileConstruction() {
        PROJECTILE_GUN_CONTEXT.remove();
    }

    public static int currentTeslaChainBonus() {
        ShotContext context = PROJECTILE_GUN_CONTEXT.get();
        ItemStack gun = context == null ? ItemStack.EMPTY : context.gun;
        if (gun == null || gun.isEmpty() || !gun.hasTagCompound()
                || !gun.getTagCompound().hasKey("techgunsupgrade", 10)) return 0;
        NBTTagCompound tag = gun.getTagCompound().getCompoundTag("techgunsupgrade");
        if (!tag.getBoolean("buffs_applied")) return 0;
        return Math.max(0, tag.getInteger("chain_lightning_targets"));
    }

    public static void initializeProjectileContext(GenericProjectile projectile) {
        if (projectile == null || projectile.getEntityData().hasKey(PROJECTILE_CONTEXT, 10)) return;
        ShotContext context = PROJECTILE_GUN_CONTEXT.get();
        if (context == null) return;

        NBTTagCompound data = projectile.getEntityData();
        applyShotDamageBeforeTrace(projectile, context);
        NBTTagCompound serialized = new NBTTagCompound();
        NBTTagCompound gunNbt = new NBTTagCompound();
        context.gun.copy().writeToNBT(gunNbt);
        serialized.setTag("gun", gunNbt);
        serialized.setString(STACK_ID, context.stackId);
        serialized.setInteger("shot_sequence", context.shotSequence);
        serialized.setInteger("continuous_shots", context.continuousShots);
        serialized.setInteger("magazine_shot", context.magazineShot);
        serialized.setInteger("ammo_before", context.ammoBefore);
        serialized.setInteger("ammo_after", context.ammoAfter);
        serialized.setInteger("clip_size", context.clipSize);
        serialized.setLong("shot_tick", context.shotTick);
        serialized.setInteger("locked_target", context.lockedTargetId);
        data.setTag(PROJECTILE_CONTEXT, serialized);
        data.setInteger("tgu_shot_sequence", context.shotSequence);
        data.setInteger("tgu_continuous_shots", context.continuousShots);
        data.setInteger("tgu_magazine_shot", context.magazineShot);
        data.setInteger("tgu_ammo_before", context.ammoBefore);
        data.setInteger("tgu_ammo_after", context.ammoAfter);
        data.setInteger("tgu_clip_size", context.clipSize);
        data.setLong("tgu_shot_tick", context.shotTick);
        data.setInteger("tgu_locked_target", context.lockedTargetId);
        data.setInteger("tgu_random_damage_stun_duration", context.gun
                .getOrCreateSubCompound("techgunsupgrade")
                .getInteger("runtime_random_damage_stun_duration"));
        applyDeterministicFan(projectile, context);
    }

    public static int additionalProjectilesForShot(ItemStack gun, EntityLivingBase shooter) {
        if (!(shooter instanceof EntityPlayer) || gun == null || gun.isEmpty()
                || !gun.hasTagCompound()
                || !gun.getTagCompound().hasKey("techgunsupgrade", 10)) return 0;
        NBTTagCompound tag = gun.getTagCompound().getCompoundTag("techgunsupgrade");
        if (!tag.getBoolean("buffs_applied")) return 0;

        EntityPlayer player = (EntityPlayer) shooter;
        int extra = 0;
        if (tag.hasKey("random_extra_projectile_specs", 10)) {
            NBTTagCompound specs = tag.getCompoundTag("random_extra_projectile_specs");
            for (String key : specs.getKeySet()) {
                NBTTagCompound spec = specs.getCompoundTag(key);
                int count = Math.max(0, spec.getInteger("count"));
                if (count > 0 && DebugSettings.roll(RANDOM, spec.getFloat("chance"))
                        && consumeAdditionalAmmo(gun,
                        Math.max(0, spec.getInteger("ammo_cost")), player)) {
                    extra += count;
                }
            }
        } else {
            float chance = tag.getFloat("random_extra_projectile_chance");
            int randomCount = Math.max(0, tag.getInteger("random_extra_projectile_count"));
            if (randomCount > 0 && DebugSettings.roll(RANDOM, chance)
                    && consumeAdditionalAmmo(gun,
                    Math.max(0, tag.getInteger("random_extra_ammo_cost")), player)) {
                extra += randomCount;
            }
        }

        int sequence = Math.max(1, tag.getInteger("runtime_shot_sequence"));
        int interval = tag.getInteger("periodic_extra_interval");
        if (interval > 0 && sequence % interval == 0) {
            extra += Math.max(0, tag.getInteger("periodic_extra_count"));
        }

        int legacy = Math.max(0, tag.getInteger("techguns_double_shot_count"));
        if (tag.hasKey("techguns_burst_count")) {
            legacy = Math.max(legacy, Math.max(0, tag.getInteger("techguns_burst_count") - 1));
        }
        return Math.min(32, extra + legacy);
    }

    private static void handleProjectileSpawn(GenericProjectile proj) {
        if (clonedProjectiles.contains(proj)) return;

        Entity shooterEntity = getShooter(proj);
        if (!(shooterEntity instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) shooterEntity;
        initializeProjectileContext(proj);
        ItemStack gun = gunFromProjectile(proj);
        mergeRuntimeStateFromLive(gun, player);
        if (gun.isEmpty()) gun = player.getHeldItemMainhand();
        if (gun.isEmpty() || !(gun.getItem() instanceof GenericGun)) return;

        NBTTagCompound tag = gun.getOrCreateSubCompound("techgunsupgrade");
        if (!tag.getBoolean("buffs_applied")) return;

        GenericGun gunItem = (GenericGun) gun.getItem();
        NBTTagCompound shotContext = proj.getEntityData().getCompoundTag(PROJECTILE_CONTEXT);
        int currentAmmo = shotContext.hasKey("ammo_after")
                ? shotContext.getInteger("ammo_after") : gunItem.getCurrentAmmo(gun);
        int[] shotState = shotContext.hasKey("shot_sequence")
                ? new int[]{shotContext.getInteger("shot_sequence"),
                shotContext.getInteger("continuous_shots"),
                shotContext.getInteger("magazine_shot")}
                : registerProjectileShot(tag, currentAmmo, player.world.getTotalWorldTime());
        int shotSequence = shotState[0];
        int continuousShots = shotState[1];
        long shotTick = shotContext.hasKey("shot_tick")
                ? shotContext.getLong("shot_tick") : player.world.getTotalWorldTime();
        proj.getEntityData().setInteger("tgu_shot_sequence", shotSequence);
        proj.getEntityData().setInteger("tgu_continuous_shots", continuousShots);
        proj.getEntityData().setLong("tgu_shot_tick", shotTick);
        if (proj instanceof GuidedMissileProjectile
                && tag.getBoolean("runtime_instant_lock_pending")) {
            tag.setBoolean("runtime_instant_lock_pending", false);
        }

        if (!proj.getEntityData().getBoolean("tgu_random_damage_preapplied")) {
            float randomDamage = randomDamageForShot(gun, tag, shotTick);
            if (randomDamage != 1.0f) {
                proj.getEntityData().setBoolean("tgu_random_damage_triggered", true);
                try {
                    multiplyProjectileDamage(proj, randomDamage);
                } catch (IllegalAccessException e) {
                    TechgunsUpgradeMod.LOGGER.warn("Failed to apply random projectile damage: "
                            + e.getMessage());
                }
            }
        }

        float selfDamageChance = tag.getFloat("random_self_damage_chance");
        if (selfDamageChance > 0.0f
                && (!tag.hasKey("runtime_self_damage_tick")
                || tag.getLong("runtime_self_damage_tick") != shotTick)) {
            tag.setLong("runtime_self_damage_tick", shotTick);
            if (DebugSettings.roll(RANDOM, selfDamageChance)) {
                float fraction = tag.getFloat("random_self_damage_fraction");
                player.attackEntityFrom(new DamageSource("tgu_chaotic_backlash")
                                .setDamageBypassesArmor(),
                        player.getMaxHealth() * fraction);
            }
        }

        if (tag.getBoolean("runtime_next_shot_pending")
                && (!tag.hasKey("runtime_next_shot_bonus_tick")
                || tag.getLong("runtime_next_shot_bonus_tick") != shotTick)) {
            tag.setLong("runtime_next_shot_bonus_tick", shotTick);
            tag.setBoolean("runtime_next_shot_pending", false);
        }

        float selfExplosionChance = tag.getFloat("self_explosion_chance");
        if (selfExplosionChance > 0.0f
                && (!tag.hasKey("runtime_self_explosion_tick")
                || tag.getLong("runtime_self_explosion_tick") != shotTick)) {
            tag.setLong("runtime_self_explosion_tick", shotTick);
            if (DebugSettings.roll(RANDOM, selfExplosionChance)) {
                SpecialEffectsHandler.handleExplosive(player.world, player.posX,
                        player.posY + 0.5, player.posZ, tag.getFloat("self_explosion_power"));
            }
        }

        try {
            if (tag.getBoolean("silencer") && projSilencedField != null) {
                projSilencedField.setBoolean(proj, true);
                proj.getEntityData().setFloat("tgu_detection_multiplier", 0.0f);
            }
            float detectionMultiplier = GunStatModifiers.detectionRadiusMultiplier(gun);
            if (detectionMultiplier < 0.999f) {
                proj.getEntityData().setFloat("tgu_detection_multiplier", detectionMultiplier);
                if (projSilencedField != null) projSilencedField.setBoolean(proj, true);
            }

            float speedMultiplier = GunStatModifiers.projectileSpeedMultiplier(gun);
            if (speedMultiplier != 1.0f) {
                proj.speed *= speedMultiplier;
                proj.motionX *= speedMultiplier;
                proj.motionY *= speedMultiplier;
                proj.motionZ *= speedMultiplier;
            }

            float radiusMultiplier = GunStatModifiers.explosionRadiusMultiplier(gun);
            if (radiusMultiplier != 1.0f) {
                if (projRadiusField != null) {
                    projRadiusField.setFloat(proj,
                            projRadiusField.getFloat(proj) * radiusMultiplier);
                }
                if (projDamageDropStartField != null) {
                    projDamageDropStartField.setFloat(proj,
                            projDamageDropStartField.getFloat(proj) * radiusMultiplier);
                }
                if (projDamageDropEndField != null) {
                    projDamageDropEndField.setFloat(proj,
                            projDamageDropEndField.getFloat(proj) * radiusMultiplier);
                }
            }

            float beamWidthMultiplier = tag.hasKey("mod_beam_width")
                    ? tag.getFloat("mod_beam_width") : 1.0f;
            if (beamWidthMultiplier != 1.0f && projRadiusField != null) {
                projRadiusField.setFloat(proj,
                        projRadiusField.getFloat(proj) * beamWidthMultiplier);
            }

            int clipSize = GunStatModifiers.clipSize(gun, gunItem.getClipsize());
            boolean shotDamageApplied = proj.getEntityData().getBoolean("tgu_shot_damage_applied");
            if (!shotDamageApplied && tag.hasKey("first_shot_damage") && currentAmmo == clipSize - 1) {
                multiplyProjectileDamage(proj, tag.getFloat("first_shot_damage"));
            }
            if (!shotDamageApplied && tag.hasKey("last_round_damage") && currentAmmo == 0) {
                multiplyProjectileDamage(proj, tag.getFloat("last_round_damage"));
            }

            if (!shotDamageApplied && tag.hasKey("mod_projectile_damage")) {
                multiplyProjectileDamage(proj, tag.getFloat("mod_projectile_damage"));
            }

            if (!shotDamageApplied && tag.getLong("runtime_next_shot_bonus_tick") == shotTick
                    && tag.hasKey("first_kill_next_damage_multiplier")) {
                multiplyProjectileDamage(proj, tag.getFloat("first_kill_next_damage_multiplier"));
            }

            if (!shotDamageApplied && tag.hasKey("periodic_damage_specs", 10)) {
                NBTTagCompound periodicSpecs = tag.getCompoundTag("periodic_damage_specs");
                for (String key : periodicSpecs.getKeySet()) {
                    NBTTagCompound spec = periodicSpecs.getCompoundTag(key);
                    int interval = spec.getInteger("interval");
                    if (interval > 0 && shotSequence % interval == 0) {
                        multiplyProjectileDamage(proj, spec.getFloat("multiplier"));
                    }
                }
            } else if (!shotDamageApplied) {
                int periodicInterval = tag.getInteger("periodic_shot_interval");
                if (periodicInterval > 0 && shotSequence % periodicInterval == 0) {
                    multiplyProjectileDamage(proj, tag.getFloat("periodic_shot_damage"));
                }
            }

            int burstThreshold = tag.getInteger("burst_damage_threshold");
            int burstProgress = tag.getBoolean("burst_damage_until_reload")
                    ? shotSequence : continuousShots;
            if (!shotDamageApplied && burstThreshold > 0 && burstProgress > burstThreshold) {
                multiplyProjectileDamage(proj, tag.getFloat("burst_damage_multiplier"));
            }

            int stationaryTicks = tag.getInteger("stationary_damage_ticks");
            if (!shotDamageApplied && tag.hasKey("stationary_damage_multiplier")
                    && hasBeenStationary(player, stationaryTicks)) {
                multiplyProjectileDamage(proj, tag.getFloat("stationary_damage_multiplier"));
            }

            int scopedTicks = tag.getInteger("scoped_damage_ticks");
            if (!shotDamageApplied && scopedTicks > 0
                    && hasBeenScoped(gun, player.world.getTotalWorldTime(), scopedTicks)) {
                multiplyProjectileDamage(proj, tag.getFloat("scoped_damage_multiplier"));
            }

            boolean charged = proj.getEntityData().getBoolean("tgu_charged_projectile");
            if (charged) {
                if (!proj.getEntityData().getBoolean("tgu_charged_stats_preapplied")) {
                    multiplyProjectileDamage(proj, GunStatModifiers.damage(gun, 1.0f));
                    if (projDamageDropStartField != null) {
                        projDamageDropStartField.setFloat(proj, GunStatModifiers.range(gun,
                                projDamageDropStartField.getFloat(proj)));
                    }
                    if (projDamageDropEndField != null) {
                        projDamageDropEndField.setFloat(proj, GunStatModifiers.range(gun,
                                projDamageDropEndField.getFloat(proj)));
                    }
                    if (projPenetrationField != null) {
                        projPenetrationField.setFloat(proj, GunStatModifiers.penetration(gun,
                                projPenetrationField.getFloat(proj)));
                    }
                }
                if (tag.hasKey("charged_damage_multiplier")) {
                    multiplyProjectileDamage(proj, tag.getFloat("charged_damage_multiplier"));
                }
                int chargeThreshold = tag.getInteger("charged_threshold_ticks");
                if (chargeThreshold > 0 && proj.getEntityData().getFloat("tgu_charge_ticks")
                        > chargeThreshold) {
                    multiplyProjectileDamage(proj, tag.getFloat("charged_threshold_damage"));
                    proj.getEntityData().setFloat("tgu_threshold_explosion_radius",
                            tag.getFloat("charged_threshold_explosion_radius"));
                }
                float nuclearChance = tag.getFloat("charged_nuclear_chance");
                if (nuclearChance > 0.0f
                        && proj.getEntityData().getBoolean("tgu_fully_charged")
                        && DebugSettings.roll(RANDOM, nuclearChance)) {
                    multiplyProjectileDamage(proj, tag.getFloat("charged_nuclear_multiplier"));
                    proj.getEntityData().setFloat("tgu_mythic_nuclear_radius",
                            tag.getFloat("charged_nuclear_radius"));
                }
                float chargedRadius = tag.hasKey("charged_radius_multiplier")
                        ? tag.getFloat("charged_radius_multiplier") : 1.0f;
                if (chargedRadius != 1.0f) {
                    if (projRadiusField != null) {
                        projRadiusField.setFloat(proj, projRadiusField.getFloat(proj) * chargedRadius);
                    }
                    if (projDamageDropStartField != null) {
                        projDamageDropStartField.setFloat(proj,
                                projDamageDropStartField.getFloat(proj) * chargedRadius);
                    }
                    if (projDamageDropEndField != null) {
                        projDamageDropEndField.setFloat(proj,
                                projDamageDropEndField.getFloat(proj) * chargedRadius);
                    }
                }
            }

            float extraRange = tag.getFloat("projectile_range_add");
            if (extraRange > 0.0f && projTicksToLiveField != null) {
                int extraTicks = Math.max(1, (int) Math.ceil(extraRange
                        / Math.max(0.1f, proj.speed)));
                projTicksToLiveField.setInt(proj, projTicksToLiveField.getInt(proj) + extraTicks);
            }

            float seeking = tag.getFloat("heat_seeking_strength");
            if (seeking > 0.0f) {
                proj.getEntityData().setFloat("tgu_heat_seeking_strength", seeking);
            }
            float throughWallHoming = tag.getFloat("through_wall_homing");
            if (throughWallHoming > 0.0f) {
                proj.getEntityData().setFloat("tgu_heat_seeking_strength", throughWallHoming);
                proj.getEntityData().setBoolean("tgu_through_wall_homing", true);
            }
        } catch (Exception e) {
            TechgunsUpgradeMod.LOGGER.warn("Failed to patch projectile stats: " + e.getMessage());
        }

        handleUltraProjectileSpawn(proj, tag, player, shotSequence, shotTick);

        if (tag.getBoolean("smoke_screen")) {
            createSmokeScreen(player);
        }
        syncRuntimeState(gun, player);
    }

    private static void cloneProjectile(GenericProjectile original, EntityPlayer shooter, int count) {
        cloneProjectile(original, shooter, count, false);
    }

    private static void cloneProjectile(GenericProjectile original, EntityPlayer shooter, int count,
                                        boolean cascadeChild) {
        if (projDamageField == null || projDamageMinField == null || projDamageDropStartField == null
                || projDamageDropEndField == null || projPenetrationField == null
                || projTicksToLiveField == null || projBlockdamageField == null) {
            return;
        }

        try {
            World world = original.world;
            float damage = projDamageField.getFloat(original);
            float damageMin = projDamageMinField.getFloat(original);
            float damageDropStart = projDamageDropStartField.getFloat(original);
            float damageDropEnd = projDamageDropEndField.getFloat(original);
            float penetration = projPenetrationField.getFloat(original);
            int ticksToLive = projTicksToLiveField.getInt(original);
            boolean blockdamage = projBlockdamageField.getBoolean(original)
                    && SafeModeAccess.canDamageBlocks(shooter);
            float speed = original.speed;

            for (int i = 0; i < count; i++) {
                GenericProjectile clone = new GenericProjectile(world, shooter, damage, speed, ticksToLive,
                        0.03f, damageDropStart, damageDropEnd, damageMin, penetration, blockdamage,
                        EnumBulletFirePos.values()[0]);
                copyProjectileRuntimeData(original, clone);
                if (cascadeChild) clone.getEntityData().setBoolean("tgu_cascade_child", true);
                clonedProjectiles.add(clone);
                world.spawnEntity(clone);
            }
        } catch (Exception e) {
            TechgunsUpgradeMod.LOGGER.warn("Failed to clone projectile for multi-shot: " + e.getMessage());
        }
    }

    public static void handleRareProjectileImpact(GenericProjectile projectile, RayTraceResult result) {
        if (projectile == null || result == null || projectile.world == null
                || projectile.world.isRemote) return;

        projectile.getEntityData().setBoolean("tgu_impact_resolved", true);

        Vec3d hit = result.hitVec == null
                ? new Vec3d(projectile.posX, projectile.posY, projectile.posZ) : result.hitVec;

        if (projectile.getEntityData().getBoolean("tgu_cluster_child")) {
            detonateClusterChild(projectile, hit);
            return;
        }

        Entity shooterEntity = getShooter(projectile);
        if (!(shooterEntity instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) shooterEntity;
        ItemStack gun = gunFromProjectile(projectile);
        mergeRuntimeStateFromLive(gun, player);
        if (gun.isEmpty()) gun = player.getHeldItemMainhand();
        if (gun.isEmpty() || !(gun.getItem() instanceof GenericGun)) return;

        NBTTagCompound tag = gun.getOrCreateSubCompound("techgunsupgrade");
        if (!tag.getBoolean("buffs_applied")) return;

        handleUltraProjectileImpact(projectile, result, hit, player, tag);

        if (result.typeOfHit == RayTraceResult.Type.BLOCK
                && tag.getFloat("railgun_pierce_range") > 0.0f
                && !projectile.getEntityData().getBoolean("tgu_railgun_applied")) {
            projectile.getEntityData().setBoolean("tgu_railgun_applied", true);
            applyRailgunPierce(projectile, player, null, projectileDamage(projectile),
                    tag.getFloat("railgun_pierce_range"));
        }

        if (result.typeOfHit == RayTraceResult.Type.BLOCK
                && tag.getInteger("hit_streak_required") > 0) {
            tag.setInteger("runtime_hit_streak", 0);
            tag.removeTag("runtime_hit_streak_target");
        }

        long shotTick = projectile.getEntityData().getLong("tgu_shot_tick");
        float impactExplosionRadius = tag.getFloat("impact_explosion_radius");
        if (impactExplosionRadius > 0.0f
                && (!tag.hasKey("runtime_impact_explosion_shot")
                || tag.getLong("runtime_impact_explosion_shot") != shotTick)) {
            tag.setLong("runtime_impact_explosion_shot", shotTick);
            SpecialEffectsHandler.handleExplosive(projectile.world, hit.x, hit.y, hit.z,
                    Math.max(1.0f, impactExplosionRadius / 2.0f));
        }

        float thresholdRadius = projectile.getEntityData().getFloat(
                "tgu_threshold_explosion_radius");
        if (thresholdRadius > 0.0f
                && !projectile.getEntityData().getBoolean("tgu_threshold_exploded")) {
            projectile.getEntityData().setBoolean("tgu_threshold_exploded", true);
            SpecialEffectsHandler.handleExplosive(projectile.world, hit.x, hit.y, hit.z,
                    Math.max(1.0f, thresholdRadius / 2.0f));
        }

        float nuclearRadius = projectile.getEntityData().getFloat("tgu_mythic_nuclear_radius");
        if (nuclearRadius > 0.0f
                && !projectile.getEntityData().getBoolean("tgu_mythic_nuclear_exploded")) {
            projectile.getEntityData().setBoolean("tgu_mythic_nuclear_exploded", true);
            SpecialEffectsHandler.handleExplosive(projectile.world, hit.x, hit.y, hit.z,
                    Math.max(2.0f, nuclearRadius / 2.0f));
        }

        if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
            float ricochetChance = tag.getFloat("ricochet_chance");
            if (DebugSettings.roll(RANDOM, ricochetChance)) {
                EntityLivingBase nearest = findNearestEnemy(projectile.world, hit, player,
                        tag.getFloat("ricochet_search_range"));
                if (nearest != null) {
                    SpecialEffectsHandler.handleExplosive(projectile.world, nearest.posX,
                            nearest.posY + nearest.height * 0.5, nearest.posZ,
                            tag.getFloat("ricochet_explosion_power"));
                }
            }
        }

        if (result.entityHit instanceof EntityLivingBase) {
            handleSplitterImpact(projectile, player, hit, tag);
        }

        float impactShockwave = tag.getFloat("impact_shockwave_strength");
        boolean chargedOnlyShockwave = tag.getBoolean("charged_impact_shockwave");
        if (impactShockwave > 0.0f && (!chargedOnlyShockwave
                || projectile.getEntityData().getBoolean("tgu_charged_projectile"))) {
            applyImpactShockwave(projectile.world, hit, player, impactShockwave);
        }

        int suppressionDuration = tag.getInteger("suppression_duration");
        float suppressionRadius = tag.getFloat("suppression_radius");
        if (suppressionDuration > 0 && suppressionRadius > 0.0f) {
            applyAreaPotion(projectile.world, hit, player, suppressionRadius,
                    MobEffects.SLOWNESS, suppressionDuration, 1);
        }

        int smokeDuration = tag.getInteger("impact_smoke_duration");
        if (smokeDuration > 0) {
            player.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY,
                    smokeDuration, 0, false, false));
            applyAreaPotion(projectile.world, hit, player, 5.0,
                    MobEffects.SLOWNESS, smokeDuration, 1);
            spawnImpactSmoke(projectile.world, hit);
        }

        int blindnessDuration = tag.getInteger("impact_blindness_duration");
        float blindnessRadius = tag.getFloat("impact_blindness_radius");
        if (blindnessDuration > 0 && blindnessRadius > 0.0f) {
            applyAreaPotion(projectile.world, hit, player, blindnessRadius,
                    MobEffects.BLINDNESS, blindnessDuration, 0);
        }

        if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
            float explosionPower = tag.getFloat("explosion_power");
            if (explosionPower > 0.0f) {
                SpecialEffectsHandler.handleExplosive(projectile.world,
                        hit.x, hit.y, hit.z, explosionPower);
            }
            float randomChance = tag.getFloat("random_explosion_chance");
            if (DebugSettings.roll(RANDOM, randomChance)) {
                SpecialEffectsHandler.handleExplosive(projectile.world,
                        hit.x, hit.y, hit.z, tag.getFloat("random_explosion_power"));
            }

            int fireZoneDuration = tag.getInteger("fire_zone_duration");
            if (fireZoneDuration > 0) {
                GroundFireTracker.createFireZone(projectile.world,
                        new BlockPos(hit.x, hit.y, hit.z), 2, fireZoneDuration);
            }
        }

        int clusterCount = tag.getInteger("cluster_count");
        if (clusterCount > 0) {
            spawnClusterProjectiles(projectile, player, hit, clusterCount);
        }
        syncRuntimeState(gun, player);
    }

    public static void handleProjectileMiss(GenericProjectile projectile) {
        if (projectile == null || projectile.world == null || projectile.world.isRemote) return;
        NBTTagCompound data = projectile.getEntityData();
        if (data.getBoolean("tgu_cluster_child")
                && !data.getBoolean("tgu_cluster_detonated")) {
            detonateClusterChild(projectile,
                    new Vec3d(projectile.posX, projectile.posY, projectile.posZ));
            return;
        }
        if (data.getBoolean("tgu_impact_resolved")
                || data.getBoolean("tgu_miss_finalized")) return;
        data.setBoolean("tgu_miss_finalized", true);

        Entity shooterEntity = getShooter(projectile);
        if (!(shooterEntity instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) shooterEntity;
        ItemStack snapshot = gunFromProjectile(projectile);
        if (snapshot.isEmpty() || !(snapshot.getItem() instanceof GenericGun)) return;
        NBTTagCompound snapshotTag = snapshot.getOrCreateSubCompound("techgunsupgrade");
        ItemStack live = findLiveGun(player, snapshotTag.getString(STACK_ID));
        if (live.isEmpty()) live = snapshot;
        NBTTagCompound tag = live.getOrCreateSubCompound("techgunsupgrade");

        tag.setInteger("runtime_hit_streak", 0);
        tag.removeTag("runtime_hit_streak_target");
        tag.setBoolean("runtime_full_mag_invalid", true);
        tag.removeTag("runtime_last_hit_target");
        tag.setInteger("runtime_last_hit_shot", -1);
        tag.removeTag("runtime_poison_last_target");
        tag.setInteger("runtime_poison_last_shot", -1);
    }

    public static int getConfiguredPiercingLimit(GenericProjectile projectile) {
        if (projectile == null || projectile.world == null || projectile.world.isRemote) return 0;
        Entity shooterEntity = getShooter(projectile);
        if (!(shooterEntity instanceof EntityPlayer)) return 0;
        ItemStack gun = gunFromProjectile(projectile);
        if (gun.isEmpty()) gun = ((EntityPlayer) shooterEntity).getHeldItemMainhand();
        if (gun.isEmpty()) return 0;
        NBTTagCompound tag = gun.getOrCreateSubCompound("techgunsupgrade");
        return tag.getBoolean("buffs_applied") ? Math.max(0, tag.getInteger("piercing_count")) : 0;
    }

    public static void updateEpicProjectile(GenericProjectile projectile) {
        if (projectile == null || projectile.world == null || projectile.world.isRemote) return;
        NBTTagCompound entityData = projectile.getEntityData();
        if (entityData.getBoolean("tgu_cluster_child")) {
            double dx = projectile.posX - entityData.getDouble("tgu_cluster_origin_x");
            double dy = projectile.posY - entityData.getDouble("tgu_cluster_origin_y");
            double dz = projectile.posZ - entityData.getDouble("tgu_cluster_origin_z");
            if (clusterChildExpired(projectile.ticksExisted, dx * dx + dy * dy + dz * dz)) {
                entityData.setBoolean("tgu_impact_resolved", true);
                detonateClusterChild(projectile,
                        new Vec3d(projectile.posX, projectile.posY, projectile.posZ));
                projectile.setDead();
                return;
            }
        }
        float secondaryRange = projectile.getEntityData().getFloat("tgu_ultra_secondary_range");
        if (secondaryRange > 0.0f) {
            Entity shooter = getShooter(projectile);
            if (shooter != null && projectile.getDistance(shooter) > secondaryRange) {
                projectile.setDead();
                return;
            }
        }
        int trailDuration = projectile.getEntityData().getInteger("tgu_ultra_fire_trail_duration");
        if (trailDuration > 0 && projectile.ticksExisted % 5 == 0) {
            GroundFireTracker.createFireZone(projectile.world,
                    new BlockPos(projectile.posX, projectile.posY - 0.25, projectile.posZ),
                    Math.max(1, projectile.getEntityData().getInteger(
                            "tgu_ultra_fire_trail_radius")), trailDuration);
        }
        float strength = projectile.getEntityData().getFloat("tgu_heat_seeking_strength");
        if (strength <= 0.0f) return;
        Entity shooter = getShooter(projectile);
        AxisAlignedBB search = projectile.getEntityBoundingBox().grow(12.0);
        EntityLivingBase nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (EntityLivingBase candidate : projectile.world.getEntitiesWithinAABB(EntityLivingBase.class, search)) {
            if (candidate == shooter || !candidate.isEntityAlive()) continue;
            double distance = candidate.getDistanceSq(projectile);
            if (distance < nearestDistance) {
                nearest = candidate;
                nearestDistance = distance;
            }
        }
        if (nearest == null) return;

        double speed = Math.sqrt(projectile.motionX * projectile.motionX
                + projectile.motionY * projectile.motionY + projectile.motionZ * projectile.motionZ);
        if (speed < 0.01) speed = Math.max(0.1, projectile.speed);
        Vec3d direction = new Vec3d(nearest.posX - projectile.posX,
                nearest.posY + nearest.getEyeHeight() * 0.5 - projectile.posY,
                nearest.posZ - projectile.posZ).normalize();
        double blend = Math.min(0.25, strength);
        projectile.motionX = projectile.motionX * (1.0 - blend) + direction.x * speed * blend;
        projectile.motionY = projectile.motionY * (1.0 - blend) + direction.y * speed * blend;
        projectile.motionZ = projectile.motionZ * (1.0 - blend) + direction.z * speed * blend;
        projectile.velocityChanged = true;
    }
        private static void multiplyProjectileDamage(GenericProjectile projectile, float multiplier)
            throws IllegalAccessException {
        if (!Float.isFinite(multiplier) || multiplier <= 0.0f || multiplier == 1.0f) return;
        if (projDamageField != null) {
            projDamageField.setFloat(projectile, projDamageField.getFloat(projectile) * multiplier);
        }
        if (projDamageMinField != null) {
            projDamageMinField.setFloat(projectile,
                    projDamageMinField.getFloat(projectile) * multiplier);
        }
    }

    private static void applyShotDamageBeforeTrace(GenericProjectile projectile,
                                                   ShotContext context) {
        ItemStack gun = context.gun;
        if (gun.isEmpty() || !gun.hasTagCompound()
                || !gun.getTagCompound().hasKey("techgunsupgrade", 10)) return;
        NBTTagCompound tag = gun.getTagCompound().getCompoundTag("techgunsupgrade");
        if (!tag.getBoolean("buffs_applied")) return;
        try {
            if (tag.hasKey("first_shot_damage") && context.magazineShot == 1
                    && context.ammoBefore == context.clipSize) {
                multiplyProjectileDamage(projectile, tag.getFloat("first_shot_damage"));
            }
            if (tag.hasKey("last_round_damage") && context.ammoAfter == 0) {
                multiplyProjectileDamage(projectile, tag.getFloat("last_round_damage"));
            }
            if (tag.hasKey("mod_projectile_damage")) {
                multiplyProjectileDamage(projectile, tag.getFloat("mod_projectile_damage"));
            }
            if (tag.getBoolean("runtime_next_shot_pending")
                    && tag.hasKey("first_kill_next_damage_multiplier")) {
                multiplyProjectileDamage(projectile,
                        tag.getFloat("first_kill_next_damage_multiplier"));
                tag.setBoolean("runtime_next_shot_pending", false);
                tag.setLong("runtime_next_shot_bonus_tick", context.shotTick);
            }
            if (tag.hasKey("periodic_damage_specs", 10)) {
                NBTTagCompound specs = tag.getCompoundTag("periodic_damage_specs");
                for (String key : specs.getKeySet()) {
                    NBTTagCompound spec = specs.getCompoundTag(key);
                    int interval = spec.getInteger("interval");
                    if (interval > 0 && context.shotSequence % interval == 0) {
                        multiplyProjectileDamage(projectile, spec.getFloat("multiplier"));
                    }
                }
            } else {
                int interval = tag.getInteger("periodic_shot_interval");
                if (interval > 0 && context.shotSequence % interval == 0) {
                    multiplyProjectileDamage(projectile, tag.getFloat("periodic_shot_damage"));
                }
            }
            int burstThreshold = tag.getInteger("burst_damage_threshold");
            int burstProgress = tag.getBoolean("burst_damage_until_reload")
                    ? context.magazineShot : context.continuousShots;
            if (burstThreshold > 0 && burstProgress > burstThreshold) {
                multiplyProjectileDamage(projectile, tag.getFloat("burst_damage_multiplier"));
            }
            int stationaryTicks = tag.getInteger("stationary_damage_ticks");
            Entity shooter = getShooter(projectile);
            if (shooter instanceof EntityPlayer && tag.hasKey("stationary_damage_multiplier")
                    && hasBeenStationary((EntityPlayer) shooter, stationaryTicks)) {
                multiplyProjectileDamage(projectile, tag.getFloat("stationary_damage_multiplier"));
            }
            int scopedTicks = tag.getInteger("scoped_damage_ticks");
            if (scopedTicks > 0 && hasBeenScoped(gun, context.shotTick, scopedTicks)) {
                multiplyProjectileDamage(projectile, tag.getFloat("scoped_damage_multiplier"));
                if (tag.getBoolean("scoped_damage_consumes_shot")) {
                    tag.setLong("runtime_scope_start_tick", context.shotTick);
                }
            }
            projectile.getEntityData().setBoolean("tgu_shot_damage_applied", true);
        } catch (IllegalAccessException e) {
            TechgunsUpgradeMod.LOGGER.warn("Failed to apply pre-trace shot damage: "
                    + e.getMessage());
        }
    }

    private static void applyDeterministicFan(GenericProjectile projectile, ShotContext context) {
        NBTTagCompound tag = context.gun.getOrCreateSubCompound("techgunsupgrade");
        int count = tag.getInteger("fan_projectile_count");
        if (count <= 1 || context.projectileOrdinal >= count) return;
        Entity shooter = getShooter(projectile);
        if (!(shooter instanceof EntityLivingBase)) return;
        Vec3d look = ((EntityLivingBase) shooter).getLookVec().normalize();
        double speed = Math.sqrt(projectile.motionX * projectile.motionX
                + projectile.motionY * projectile.motionY + projectile.motionZ * projectile.motionZ);
        if (speed < 0.01) speed = Math.max(0.1, projectile.speed);
        double step = Math.toRadians(6.0);
        double yaw = (context.projectileOrdinal - (count - 1) * 0.5) * step;
        double cos = Math.cos(yaw);
        double sin = Math.sin(yaw);
        double horizontalX = look.x * cos - look.z * sin;
        double horizontalZ = look.x * sin + look.z * cos;
        projectile.motionX = horizontalX * speed;
        projectile.motionY = look.y * speed;
        projectile.motionZ = horizontalZ * speed;
        projectile.velocityChanged = true;
    }

    private static int[] registerProjectileShot(NBTTagCompound tag, int currentAmmo, long now) {
        long lastTick = tag.getLong("runtime_last_shot_tick");
        int sequence = Math.max(0, tag.getInteger("runtime_shot_sequence"));
        int continuous = Math.max(1, tag.getInteger("runtime_continuous_shots"));
        int magazineShot = Math.max(0, tag.getInteger("runtime_magazine_shot"));
        if (lastTick != now) {
            boolean firstTrackedShot = !tag.hasKey("runtime_last_ammo");
            boolean reloaded = !firstTrackedShot && currentAmmo > tag.getInteger("runtime_last_ammo");
            sequence++;
            magazineShot = firstTrackedShot || reloaded ? 1 : magazineShot + 1;
            continuous = firstTrackedShot || reloaded || now - lastTick > 10
                    ? 1 : continuous + 1;
            tag.setInteger("runtime_shot_sequence", sequence);
            tag.setInteger("runtime_continuous_shots", continuous);
            tag.setInteger("runtime_magazine_shot", magazineShot);
            tag.setInteger("runtime_last_ammo", currentAmmo);
            tag.setLong("runtime_last_shot_tick", now);
        }
        return new int[]{Math.max(1, sequence), continuous, Math.max(1, magazineShot)};
    }

    private static float randomDamageForShot(ItemStack gun, NBTTagCompound tag, long now) {
        if (tag.getFloat("random_damage_chance") <= 0.0f
                && !tag.hasKey("random_damage_specs", 10)) return 1.0f;
        if (!tag.hasKey("runtime_random_damage_tick")
                || tag.getLong("runtime_random_damage_tick") != now) {
            tag.setLong("runtime_random_damage_tick", now);
            tag.setFloat("runtime_random_damage_multiplier",
                    GunStatModifiers.randomDamageMultiplier(gun, RANDOM));
        }
        float multiplier = tag.getFloat("runtime_random_damage_multiplier");
        return multiplier > 0.0f ? multiplier : 1.0f;
    }

    public static float projectileRandomDamageMultiplier(ItemStack gun, long now) {
        if (gun == null || gun.isEmpty() || !gun.hasTagCompound()
                || !gun.getTagCompound().hasKey("techgunsupgrade", 10)) return 1.0f;
        NBTTagCompound tag = gun.getTagCompound().getCompoundTag("techgunsupgrade");
        if (!tag.getBoolean("buffs_applied")) return 1.0f;
        return randomDamageForShot(gun, tag, now);
    }

    private static boolean hasBeenStationary(EntityPlayer player, int requiredTicks) {
        boolean moving = player.motionX * player.motionX + player.motionZ * player.motionZ > 0.0004;
        if (moving) return false;
        if (requiredTicks <= 0) return true;
        long lastMove = player.getEntityData().getLong("tgu_last_move_tick");
        return player.world.getTotalWorldTime() - lastMove >= requiredTicks;
    }

    private static boolean hasBeenScoped(ItemStack gun, long now, int requiredTicks) {
        if (!gun.hasTagCompound() || !gun.getTagCompound().hasKey("techgunsupgrade", 10)) return false;
        NBTTagCompound tag = gun.getTagCompound().getCompoundTag("techgunsupgrade");
        return tag.getBoolean("runtime_scoped")
                && now - tag.getLong("runtime_scope_start_tick") >= requiredTicks;
    }

    public static void recordScopeToggle(EntityPlayer player, ItemStack gun) {
        if (player == null || gun.isEmpty() || !(gun.getItem() instanceof GenericGun)) return;
        NBTTagCompound tag = gun.getOrCreateSubCompound("techgunsupgrade");
        if (!tag.getBoolean("buffs_applied")) return;
        boolean scoped = !tag.getBoolean("runtime_scoped");
        tag.setBoolean("runtime_scoped", scoped);
        if (scoped) tag.setLong("runtime_scope_start_tick", player.world.getTotalWorldTime());
    }

    public static void recordScopeShot(EntityPlayer player, ItemStack gun, boolean zooming) {
        if (player == null || gun.isEmpty() || !(gun.getItem() instanceof GenericGun)) return;
        NBTTagCompound tag = gun.getOrCreateSubCompound("techgunsupgrade");
        if (!tag.getBoolean("buffs_applied")) return;
        if (zooming && !tag.getBoolean("runtime_scoped")) {
            tag.setBoolean("runtime_scoped", true);
            tag.setLong("runtime_scope_start_tick", player.world.getTotalWorldTime());
        } else if (!zooming) {
            tag.setBoolean("runtime_scoped", false);
        }
    }

    private static float applyEpicHitDamage(ItemStack gun, NBTTagCompound tag, EntityPlayer player,
                                            EntityLivingBase target, DamageSource source, float amount) {
        if (target == null || !(source.getImmediateSource() instanceof GenericProjectile)) return amount;
        GenericProjectile projectile = (GenericProjectile) source.getImmediateSource();
        int shot = projectile.getEntityData().getInteger("tgu_shot_sequence");
        long now = target.world.getTotalWorldTime();
        String targetId = target.getUniqueID().toString();
        float result = amount;

        if (tag.hasKey("consecutive_target_damage") && shot > 0) {
            int previousShot = tag.getInteger("runtime_last_hit_shot");
            String previousTarget = tag.getString("runtime_last_hit_target");
            long previousTick = tag.getLong("runtime_last_hit_tick");
            if (shot == previousShot + 1 && targetId.equals(previousTarget)
                    && now - previousTick <= 60) {
                result *= positiveMultiplier(tag, "consecutive_target_damage");
            }
            if (shot != previousShot) {
                tag.setInteger("runtime_last_hit_shot", shot);
                tag.setString("runtime_last_hit_target", targetId);
                tag.setLong("runtime_last_hit_tick", now);
            }
        }

        if (tag.hasKey("multi_target_damage") && shot > 0) {
            int trackedShot = tag.getInteger("runtime_multi_target_shot");
            if (trackedShot != shot) {
                tag.setInteger("runtime_multi_target_shot", shot);
                tag.setString("runtime_multi_first_target", targetId);
                tag.setInteger("runtime_multi_first_entity", target.getEntityId());
                tag.setFloat("runtime_multi_first_damage", amount);
                tag.setBoolean("runtime_multi_bonus_applied", false);
            } else if (!targetId.equals(tag.getString("runtime_multi_first_target"))) {
                float multiplier = positiveMultiplier(tag, "multi_target_damage");
                result *= multiplier;
                if (!tag.getBoolean("runtime_multi_bonus_applied")) {
                    Entity first = target.world.getEntityByID(tag.getInteger("runtime_multi_first_entity"));
                    if (first instanceof EntityLivingBase && first.isEntityAlive()) {
                        first.attackEntityFrom(DamageSource.MAGIC,
                                tag.getFloat("runtime_multi_first_damage") * (multiplier - 1.0f));
                    }
                    tag.setBoolean("runtime_multi_bonus_applied", true);
                }
            }
        }

        float rampStep = tag.getFloat("damage_ramp_step");
        int rampInterval = tag.getInteger("damage_ramp_interval");
        if (rampStep > 0.0f && rampInterval > 0) {
            long lastHit = tag.getLong("runtime_ramp_last_hit");
            if (!targetId.equals(tag.getString("runtime_ramp_target")) || now - lastHit > 2) {
                tag.setString("runtime_ramp_target", targetId);
                tag.setLong("runtime_ramp_start", now);
            }
            long heldTicks = Math.max(0, now - tag.getLong("runtime_ramp_start"));
            float ramp = 1.0f + (heldTicks / rampInterval) * rampStep;
            float maximum = tag.getFloat("damage_ramp_max");
            result *= Math.min(maximum > 1.0f ? maximum : 2.0f, ramp);
            tag.setLong("runtime_ramp_last_hit", now);
        }
        return result;
    }

    private static float applyMythicHitDamage(ItemStack gun, NBTTagCompound tag,
                                              EntityPlayer player, EntityLivingBase target,
                                              DamageSource source, float amount) {
        if (target == null) return amount;
        float result = amount;
        Entity immediate = source.getImmediateSource();

        if (!(immediate instanceof GenericProjectile)) {
            float chance = tag.getFloat("melee_area_chance");
            if (DebugSettings.roll(RANDOM, chance)) {
                result *= positiveMultiplier(tag, "melee_area_damage_multiplier");
                applyMythicAreaDamage(target.world,
                        new Vec3d(target.posX, target.posY + target.height * 0.5, target.posZ),
                        player, target, tag.getFloat("melee_area_radius"), result,
                        tag.getInteger("melee_area_fire_duration"), 0.0f);
            }
            return result;
        }

        GenericProjectile projectile = (GenericProjectile) immediate;
        NBTTagCompound projectileData = projectile.getEntityData();
        long shotTick = projectileData.getLong("tgu_shot_tick");
        int shotSequence = projectileData.getInteger("tgu_shot_sequence");
        String targetId = target.getUniqueID().toString();
        long now = target.world.getTotalWorldTime();

        int fullMagazine = projectileData.getInteger("tgu_clip_size");
        int magazineShot = projectileData.getInteger("tgu_magazine_shot");
        int ammoBefore = projectileData.getInteger("tgu_ammo_before");
        int ammoAfter = projectileData.getInteger("tgu_ammo_after");
        if (tag.getInteger("full_mag_required_shots") > 0 && fullMagazine > 0
                && magazineShot > 0
                && tag.getLong("runtime_full_mag_processed_tick") != shotTick) {
            tag.setLong("runtime_full_mag_processed_tick", shotTick);
            if (magazineShot == 1) {
                tag.setString("runtime_full_mag_target", targetId);
                tag.setInteger("runtime_full_mag_hits", 1);
                tag.setInteger("runtime_full_mag_last_shot", 1);
                tag.setBoolean("runtime_full_mag_invalid", ammoBefore != fullMagazine);
            } else {
                int previousHits = tag.getInteger("runtime_full_mag_hits");
                if (tag.getBoolean("runtime_full_mag_invalid")
                        || !targetId.equals(tag.getString("runtime_full_mag_target"))
                        || magazineShot != tag.getInteger("runtime_full_mag_last_shot") + 1) {
                    tag.setBoolean("runtime_full_mag_invalid", true);
                } else {
                    tag.setInteger("runtime_full_mag_hits", previousHits + 1);
                    tag.setInteger("runtime_full_mag_last_shot", magazineShot);
                }
            }
            if (!tag.getBoolean("runtime_full_mag_invalid")
                    && ammoAfter == 0
                    && tag.getInteger("runtime_full_mag_hits") == magazineShot) {
                tag.setLong("runtime_full_mag_bonus_tick", shotTick);
            }
        }
        if (tag.getLong("runtime_full_mag_bonus_tick") == shotTick) {
            result *= positiveMultiplier(tag, "full_mag_damage_multiplier");
        }

        int requiredStreak = tag.getInteger("hit_streak_required");
        if (requiredStreak > 0 && tag.getLong("runtime_hit_streak_processed_tick") != shotTick) {
            tag.setLong("runtime_hit_streak_processed_tick", shotTick);
            if (targetId.equals(tag.getString("runtime_hit_streak_target"))) {
                tag.setInteger("runtime_hit_streak", tag.getInteger("runtime_hit_streak") + 1);
            } else {
                tag.setString("runtime_hit_streak_target", targetId);
                tag.setInteger("runtime_hit_streak", 1);
            }
            if (tag.getInteger("runtime_hit_streak") > requiredStreak) {
                tag.setLong("runtime_hit_streak_bonus_tick", shotTick);
                tag.setInteger("runtime_hit_streak", 0);
                tag.removeTag("runtime_hit_streak_target");
            }
        }
        if (tag.getLong("runtime_hit_streak_bonus_tick") == shotTick) {
            result *= positiveMultiplier(tag, "hit_streak_damage_multiplier");
        }

        if (tag.hasKey("same_shot_pellet_damage")) {
            if (tag.getLong("runtime_pellet_shot_tick") != shotTick
                    || !targetId.equals(tag.getString("runtime_pellet_target"))) {
                tag.setLong("runtime_pellet_shot_tick", shotTick);
                tag.setString("runtime_pellet_target", targetId);
                tag.setInteger("runtime_pellet_hits", 1);
            } else {
                tag.setInteger("runtime_pellet_hits", tag.getInteger("runtime_pellet_hits") + 1);
                result *= positiveMultiplier(tag, "same_shot_pellet_damage");
            }
        }

        if (tag.hasKey("stunned_target_damage")
                && (target.isPotionActive(MobEffects.SLOWNESS)
                || target.isPotionActive(MobEffects.MINING_FATIGUE))) {
            result *= positiveMultiplier(tag, "stunned_target_damage");
        }

        int continuousTicks = tag.getInteger("continuous_explosion_ticks");
        if (continuousTicks > 0) {
            if (!targetId.equals(tag.getString("runtime_continuous_target"))
                    || now - tag.getLong("runtime_continuous_last_tick") > 5) {
                tag.setString("runtime_continuous_target", targetId);
                tag.setLong("runtime_continuous_start_tick", now);
                tag.setBoolean("runtime_continuous_exploded", false);
            }
            tag.setLong("runtime_continuous_last_tick", now);
            if (!tag.getBoolean("runtime_continuous_exploded")
                    && now - tag.getLong("runtime_continuous_start_tick") >= continuousTicks) {
                float multiplier = positiveMultiplier(tag, "continuous_explosion_multiplier");
                result *= multiplier;
                tag.setBoolean("runtime_continuous_exploded", true);
                applyMythicAreaDamage(target.world,
                        new Vec3d(target.posX, target.posY + target.height * 0.5, target.posZ),
                        player, target, tag.getFloat("continuous_explosion_radius"), result,
                        0, 0.4f);
            }
        }

        float headshotMultiplier = tag.getFloat("headshot_damage_multiplier");
        if (headshotMultiplier > 0.0f
                && projectile.posY >= target.posY + target.height * 0.65f) {
            result *= headshotMultiplier;
            applyImpactShockwave(target.world,
                    new Vec3d(target.posX, target.posY + target.height, target.posZ), player,
                    tag.getFloat("headshot_shockwave"));
        }
        return result;
    }

    private static void applyMythicStacks(NBTTagCompound tag) {
        if (!tag.hasKey("mythic_stack_specs", 10)) return;
        NBTTagCompound specs = tag.getCompoundTag("mythic_stack_specs");
        NBTTagCompound progress = tag.getCompoundTag("mythic_stack_progress");
        for (String key : specs.getKeySet()) {
            NBTTagCompound spec = specs.getCompoundTag(key);
            float next = progress.getFloat(key) + spec.getFloat("step");
            progress.setFloat(key, Math.min(spec.getFloat("max"), next));
        }
        tag.setTag("mythic_stack_progress", progress);
    }

    private static void applyMythicKillEffects(NBTTagCompound tag, ItemStack gun,
                                               EntityPlayer player, EntityLivingBase killed,
                                               DamageSource source) {
        Entity immediate = source.getImmediateSource();
        if (tag.hasKey("first_kill_next_damage_multiplier")
                && immediate instanceof GenericProjectile
                && ((GenericProjectile) immediate).getEntityData().getInteger(
                "tgu_shot_sequence") == 1) {
            tag.setBoolean("runtime_next_shot_pending", true);
        }

        int blindness = tag.getInteger("kill_blind_duration");
        if (blindness > 0 && player.getDistance(killed) > tag.getFloat("kill_blind_distance")) {
            applyAreaPotion(killed.world,
                    new Vec3d(killed.posX, killed.posY + killed.height * 0.5, killed.posZ),
                    player, tag.getFloat("kill_blind_radius"), MobEffects.BLINDNESS,
                    blindness, 0);
        }

        float contagion = tag.getFloat("kill_contagion_chance");
        if (DebugSettings.roll(RANDOM, contagion)) {
            applyContagion(killed, player, 3.0, tag.getInteger("kill_contagion_duration"));
        }

        int chainTargets = tag.getInteger("kill_chain_targets");
        if (chainTargets > 0) {
            SpecialEffectsHandler.handleChainLightning(killed.world, killed.posX,
                    killed.posY + killed.height * 0.5, killed.posZ, chainTargets, player, killed);
        }

        float cloudChance = tag.getFloat("kill_cloud_chance");
        if ((!tag.getBoolean("kill_cloud_continuous") || player.isHandActive())
                && DebugSettings.roll(RANDOM, cloudChance)) {
            applyMythicAreaDamage(killed.world,
                    new Vec3d(killed.posX, killed.posY + killed.height * 0.5, killed.posZ),
                    player, killed, tag.getFloat("kill_cloud_radius"),
                    Math.max(4.0f, killed.getMaxHealth() * 0.20f), 0, 0.15f, UpgradeVisualEffects.AreaStyle.POISON);
        }

        float tremorChance = tag.getFloat("kill_tremor_chance");
        if (tremorChance > 0.0f && !(immediate instanceof GenericProjectile)
                && (!tag.getBoolean("kill_tremor_continuous") || player.isHandActive())
                && DebugSettings.roll(RANDOM, tremorChance)) {
            applyMythicAreaDamage(killed.world,
                    new Vec3d(killed.posX, killed.posY, killed.posZ), player, killed,
                    tag.getFloat("kill_tremor_radius"),
                    Math.max(5.0f, killed.getMaxHealth() * 0.25f), 0, 1.2f, UpgradeVisualEffects.AreaStyle.SEISMIC);
        }

        if (tag.getBoolean("kill_instant_lock")
                && immediate instanceof GuidedMissileProjectile
                && ((GenericProjectile) immediate).getEntityData().getInteger(
                "tgu_locked_target") == killed.getEntityId()) {
            tag.setBoolean("runtime_instant_lock_pending", true);
        }
    }

    private static float applyUltraHitDamage(ItemStack gun, NBTTagCompound tag,
                                             EntityPlayer player, EntityLivingBase target,
                                             DamageSource source, float amount) {
        if (target == null) return amount;
        Entity immediate = source.getImmediateSource();
        boolean projectileHit = immediate instanceof GenericProjectile;
        GenericProjectile projectile = projectileHit ? (GenericProjectile) immediate : null;
        if (projectile != null && projectile.getEntityData().getBoolean("tgu_ultra_secondary")) {
            return amount;
        }
        float result = amount;

        if (projectileHit) {
            if (tag.getBoolean("every_shot_lightning")
                    && !projectile.getEntityData().getBoolean("tgu_ultra_lightning")) {
                projectile.getEntityData().setBoolean("tgu_ultra_lightning", true);
                SpecialEffectsHandler.handleLightning(target.world, target.posX,
                        target.posY, target.posZ, player);
            }

            float chainRadius = tag.getFloat("chain_all_radius");
            if (chainRadius > 0.0f
                    && !projectile.getEntityData().getBoolean("tgu_ultra_chain_all")) {
                projectile.getEntityData().setBoolean("tgu_ultra_chain_all", true);
                applyLightningToAll(target.world, target, player, chainRadius,
                        Math.max(4.0f, amount * 0.5f));
            }

            if (rollProjectileChance(projectile, "sniper", tag.getFloat(
                    "sniper_strike_chance"))) {
                result *= positiveMultiplier(tag, "sniper_strike_multiplier");
                target.world.addWeatherEffect(new EntityLightningBolt(target.world,
                        target.posX, target.posY, target.posZ, false));
            }

            if (rollProjectileChance(projectile, "instant_kill",
                    tag.getFloat("instant_kill_chance"))) {
                result = instantKillDamage(player, target, result);
            }

            if (rollProjectileChance(projectile, "sonic", tag.getFloat("sonic_wave_chance"))) {
                float multiplier = positiveMultiplier(tag, "sonic_wave_multiplier");
                result *= multiplier;
                float radius = tag.getFloat("sonic_wave_radius");
                applyMythicAreaDamage(target.world, center(target), player, target, radius,
                        amount * multiplier, 0, 0.6f, UpgradeVisualEffects.AreaStyle.SONIC);
                target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 100, 4));
                target.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 100, 3));
                applyAreaPotion(target.world, center(target), player, radius,
                        MobEffects.SLOWNESS, 100, 4);
            }

            if (rollProjectileChance(projectile, "pulse", tag.getFloat("pulse_wave_chance"))) {
                float multiplier = positiveMultiplier(tag, "pulse_wave_multiplier");
                result *= multiplier;
                float radius = tag.getFloat("pulse_wave_radius");
                applyMythicAreaDamage(target.world, center(target), player, target, radius,
                        amount * multiplier, 0, 2.5f, UpgradeVisualEffects.AreaStyle.PULSE);
                applyKnockback(target, player, 2.5f);
            }

            if (rollProjectileChance(projectile, "emp", tag.getFloat("emp_chance"))) {
                float multiplier = positiveMultiplier(tag, "emp_multiplier");
                result *= multiplier;
                float radius = tag.getFloat("emp_radius");
                applyMythicAreaDamage(target.world, center(target), player, target, radius,
                        amount * multiplier, 0, 0.5f, UpgradeVisualEffects.AreaStyle.EMP);
                target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 4));
                target.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 60, 4));
                applyAreaPotion(target.world, center(target), player, radius,
                        MobEffects.SLOWNESS, 60, 4);
            }

            if (rollProjectileChance(projectile, "light", tag.getFloat(
                    "light_explosion_chance"))) {
                float multiplier = positiveMultiplier(tag, "light_explosion_multiplier");
                result *= multiplier;
                float radius = tag.getFloat("light_explosion_radius");
                applyMythicAreaDamage(target.world, center(target), player, target, radius,
                        amount * multiplier, 0, 0.35f, UpgradeVisualEffects.AreaStyle.LIGHT);
                target.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 100, 0));
                applyAreaPotion(target.world, center(target), player, radius,
                        MobEffects.BLINDNESS, 100, 0);
            }

            int nuclearTicks = tag.getInteger("continuous_nuclear_ticks");
            if (nuclearTicks > 0) {
                long now = target.world.getTotalWorldTime();
                NBTTagCompound targetData = target.getEntityData();
                targetData.setString("tgu_nuclear_owner", player.getUniqueID().toString());
                targetData.setLong("tgu_nuclear_mark_until", now + 20);
                targetData.setFloat("tgu_nuclear_radius",
                        Math.max(4.0f, tag.getFloat("continuous_nuclear_radius")));
                targetData.setFloat("tgu_nuclear_damage", Math.max(amount,
                        amount * positiveMultiplier(tag, "continuous_nuclear_multiplier")));
                String targetId = target.getUniqueID().toString();
                if (!targetId.equals(tag.getString("runtime_ultra_nuclear_target"))
                        || now - tag.getLong("runtime_ultra_nuclear_last") > 5) {
                    tag.setString("runtime_ultra_nuclear_target", targetId);
                    tag.setLong("runtime_ultra_nuclear_start", now);
                    tag.setBoolean("runtime_ultra_nuclear_triggered", false);
                    tag.setBoolean("runtime_ultra_nuclear_warning", false);
                }
                tag.setLong("runtime_ultra_nuclear_last", now);
                long elapsedNuclearTicks = now - tag.getLong("runtime_ultra_nuclear_start");
                if (!tag.getBoolean("runtime_ultra_nuclear_triggered")
                        && !tag.getBoolean("runtime_ultra_nuclear_warning")
                        && elapsedNuclearTicks >= Math.max(0, nuclearTicks - 20)) {
                    tag.setBoolean("runtime_ultra_nuclear_warning", true);
                    NuclearSounds.playWarning(target.world, center(target));
                }
                if (!tag.getBoolean("runtime_ultra_nuclear_triggered")
                        && elapsedNuclearTicks >= nuclearTicks) {
                    float multiplier = positiveMultiplier(tag, "continuous_nuclear_multiplier");
                    result *= multiplier;
                    tag.setBoolean("runtime_ultra_nuclear_triggered", true);
                    createNuclearImpact(target.world, center(target), player,
                            tag.getFloat("continuous_nuclear_radius"), amount * multiplier);
                }
            }
        } else {
            long now = player.world.getTotalWorldTime();
            if (tag.getFloat("melee_vortex_radius") > 0.0f) {
                tag.setLong("runtime_ultra_vortex_last_hit", now);
            }
            if (tag.getFloat("continuous_earthquake_radius") > 0.0f) {
                tag.setLong("runtime_ultra_earthquake_last_hit", now);
            }
            if (tag.getFloat("melee_instant_kill_chance") > 0.0f
                    && DebugSettings.roll(RANDOM, tag.getFloat("melee_instant_kill_chance"))) {
                result = instantKillDamage(player, target, result);
            }
            float seismicRadius = tag.getFloat("seismic_wave_radius");
            if (seismicRadius > 0.0f) {
                float strength = Math.min(3.0f, tag.getFloat("seismic_wave_knockback") / 8.0f);
                applyMythicAreaDamage(target.world, center(target), player, target,
                        seismicRadius, amount * 0.50f, 0, strength, UpgradeVisualEffects.AreaStyle.SEISMIC);
                applyKnockback(target, player, strength);
            }
            float explosionChance = tag.getFloat("random_melee_explosion_chance");
            if (DebugSettings.roll(RANDOM, explosionChance)) {
                float radius = tag.getFloat("random_melee_explosion_radius");
                applyMythicAreaDamage(target.world, center(target), player, target,
                        radius, amount, 0, 1.0f);
                SpecialEffectsHandler.handleExplosive(target.world, target.posX,
                        target.posY, target.posZ, Math.max(2.0f, radius / 2.0f));
            }
        }
        return result;
    }

    private static void applyUltraKillEffects(NBTTagCompound tag, ItemStack gun,
                                              EntityPlayer player, EntityLivingBase killed,
                                              DamageSource source) {
        if (tag.getBoolean("nuclear_on_kill")
                && source.getImmediateSource() instanceof GenericProjectile
                && !killed.getEntityData().getBoolean("tgu_nuclear_death_exploded")) {
            killed.getEntityData().setBoolean("tgu_nuclear_death_exploded", true);
            float radius = Math.max(4.0f, tag.getFloat("continuous_nuclear_radius"));
            float damage = Math.max(20.0f, tag.getFloat("base_damage")
                    * positiveMultiplier(tag, "mod_damage")
                    * positiveMultiplier(tag, "continuous_nuclear_multiplier"));
            createNuclearImpact(killed.world, center(killed), player, radius, damage);
        }

        float executeChance = tag.getFloat("kill_execute_chance");
        if (DebugSettings.roll(RANDOM, executeChance)) {
            float radius = tag.getFloat("kill_execute_radius");
            AxisAlignedBB area = killed.getEntityBoundingBox().grow(radius);
            for (EntityLivingBase target : killed.world.getEntitiesWithinAABB(
                    EntityLivingBase.class, area)) {
                if (target == killed || !canAffect(player, target)
                        || !target.isEntityAlive() || target.getDistance(killed) > radius) continue;
                target.attackEntityFrom(new EntityDamageSource("tgu_ultra_execute", player)
                        .setDamageBypassesArmor(), instantKillDamage(player, target, 0.0f));
            }
        }
        if (tag.getBoolean("kill_lightning")) {
            killed.world.addWeatherEffect(new EntityLightningBolt(killed.world,
                    killed.posX, killed.posY, killed.posZ, false));
        }
        float bloodChance = tag.getFloat("kill_blood_rain_chance");
        if (DebugSettings.roll(RANDOM, bloodChance)) {
            UltraAreaEffectTracker.create(killed.world, center(killed), player, 8.0f,
                    100, 6.0f, 0.0f, null, 0, EnumParticleTypes.BLOCK_CRACK, UpgradeVisualEffects.ZoneStyle.BLOOD_RAIN);
        }
        float earthquakeChance = tag.getFloat("kill_earthquake_chance");
        if (DebugSettings.roll(RANDOM, earthquakeChance)) {
            UltraAreaEffectTracker.create(killed.world, center(killed), player,
                    tag.getFloat("kill_earthquake_radius"), 100, 8.0f, 1.4f,
                    null, 0, EnumParticleTypes.BLOCK_DUST, UpgradeVisualEffects.ZoneStyle.EARTHQUAKE);
        }
        float killExplosion = tag.getFloat("kill_explosion_radius");
        if (killExplosion > 0.0f) {
            SpecialEffectsHandler.handleExplosive(killed.world, killed.posX,
                    killed.posY, killed.posZ, Math.max(2.0f, killExplosion / 2.0f));
        }
    }

    private static void handleUltraProjectileSpawn(GenericProjectile projectile,
                                                    NBTTagCompound tag, EntityPlayer player,
                                                    int shotSequence, long shotTick) {
        if (tag.getInteger("ultra_fire_trail_duration") > 0) {
            projectile.getEntityData().setInteger("tgu_ultra_fire_trail_duration",
                    tag.getInteger("ultra_fire_trail_duration"));
            projectile.getEntityData().setInteger("tgu_ultra_fire_trail_radius",
                    tag.getInteger("ultra_fire_trail_radius"));
        }
        int areaInterval = tag.getInteger("periodic_area_interval");
        if (areaInterval > 0 && shotSequence % areaInterval == 0) {
            projectile.getEntityData().setBoolean("tgu_periodic_area_explosion", true);
        }

        int radialCount = tag.getInteger("radial_burst_count");
        int radialInterval = tag.getInteger("radial_burst_interval");
        float radialChance = tag.getFloat("radial_burst_chance");
        boolean radial = radialCount > 0 && radialInterval > 0
                && shotSequence % radialInterval == 0;
        if (!radial && radialCount > 0 && radialChance > 0.0f
                && tag.getLong("runtime_radial_check_tick") != shotTick) {
            tag.setLong("runtime_radial_check_tick", shotTick);
            radial = DebugSettings.roll(RANDOM, radialChance);
        }
        if (radial && tag.getLong("runtime_radial_burst_tick") != shotTick) {
            tag.setLong("runtime_radial_burst_tick", shotTick);
            spawnRadialProjectiles(projectile, player, radialCount,
                    Math.max(5.0f, tag.getFloat("radial_burst_range")), false);
        }

        int targetedCount = tag.getInteger("targeted_burst_count");
        int targetedInterval = tag.getInteger("targeted_burst_interval");
        float targetedChance = tag.getFloat("targeted_burst_chance");
        boolean targeted = targetedCount > 0 && targetedInterval > 0
                && shotSequence % targetedInterval == 0;
        if (!targeted && targetedCount > 0 && targetedChance > 0.0f
                && tag.getLong("runtime_targeted_check_tick") != shotTick) {
            tag.setLong("runtime_targeted_check_tick", shotTick);
            targeted = DebugSettings.roll(RANDOM, targetedChance);
        }
        if (targeted && tag.getLong("runtime_targeted_burst_tick") != shotTick) {
            tag.setLong("runtime_targeted_burst_tick", shotTick);
            spawnTargetedProjectiles(projectile, player, targetedCount,
                    Math.max(8.0f, tag.getFloat("targeted_burst_range")));
        }
    }

    private static void handleUltraProjectileImpact(GenericProjectile projectile,
                                                     RayTraceResult result, Vec3d hit,
                                                     EntityPlayer player, NBTTagCompound tag) {
        int splitChildren = tag.getInteger("infinite_split_children");
        if (splitChildren > 0 && result.entityHit instanceof EntityLivingBase
                && !projectile.getEntityData().getBoolean("tgu_ultra_split_spawned")) {
            projectile.getEntityData().setBoolean("tgu_ultra_split_spawned", true);
            SplitSwarmTracker.create(projectile.world, hit,
                    new Vec3d(projectile.motionX, projectile.motionY, projectile.motionZ),
                    player, projectileDamage(projectile));
        }
        if (projectile.getEntityData().getBoolean("tgu_ultra_secondary")) {
            if (projectile.getEntityData().getBoolean("tgu_ultra_acid_child")
                    && tag.getFloat("acid_cloud_radius") > 0.0f) {
                UltraAreaEffectTracker.create(projectile.world, hit, player,
                        tag.getFloat("acid_cloud_radius"),
                        Math.max(20, tag.getInteger("acid_cloud_duration")),
                        Math.max(3.0f, projectileDamage(projectile) * 0.20f), 0.0f,
                        MobEffects.POISON, 40, EnumParticleTypes.SLIME, UpgradeVisualEffects.ZoneStyle.ACID_CLOUD);
            }
            return;
        }

        long shotTick = projectile.getEntityData().getLong("tgu_shot_tick");
        float nuclearChance = tag.getFloat("nuclear_impact_chance");
        if (nuclearChance > 0.0f && impactTriggerMatches(tag, EffectAction.NUCLEAR_IMPACT, result)
                && tag.getLong("runtime_nuclear_impact_tick") != shotTick) {
            tag.setLong("runtime_nuclear_impact_tick", shotTick);
            if (DebugSettings.roll(RANDOM, nuclearChance)) {
                createNuclearImpact(projectile.world, hit, player,
                        tag.getFloat("nuclear_impact_radius"), projectileDamage(projectile) * 5.0f);
            }
        }

        int extraExplosions = tag.getInteger("extra_impact_explosions");
        if (!impactTriggerMatches(tag, EffectAction.EXTRA_IMPACT_EXPLOSIONS, result)) {
            extraExplosions = 0;
        }
        if (extraExplosions > 0
                && !projectile.getEntityData().getBoolean("tgu_extra_impact_exploded")) {
            projectile.getEntityData().setBoolean("tgu_extra_impact_exploded", true);
            for (int i = 0; i < Math.min(4, extraExplosions); i++) {
                SpecialEffectsHandler.handleExplosive(projectile.world,
                        hit.x + (RANDOM.nextDouble() - 0.5) * 2.0,
                        hit.y, hit.z + (RANDOM.nextDouble() - 0.5) * 2.0, 3.0f);
            }
        }

        if (tag.getFloat("artillery_chance") > 0.0f
                && impactTriggerMatches(tag, EffectAction.ARTILLERY_STRIKE, result)
                && tag.getLong("runtime_artillery_tick") != shotTick) {
            tag.setLong("runtime_artillery_tick", shotTick);
            if (DebugSettings.roll(RANDOM, tag.getFloat("artillery_chance"))) {
                applyArtillery(projectile.world, hit, tag.getInteger("artillery_count"),
                        tag.getFloat("artillery_radius"));
            }
        }

        if (tag.getFloat("fire_rain_chance") > 0.0f
                && impactTriggerMatches(tag, EffectAction.FIRE_RAIN, result)
                && rollTagChanceForShot(tag, "runtime_fire_rain_tick", shotTick,
                tag.getFloat("fire_rain_chance"))) {
            applyFireRain(projectile.world, hit, player, tag.getInteger("fire_rain_count"),
                    tag.getFloat("fire_rain_radius"));
        }
        if (tag.getFloat("laser_rain_chance") > 0.0f
                && impactTriggerMatches(tag, EffectAction.LASER_RAIN, result)
                && rollTagChanceForShot(tag, "runtime_laser_rain_tick", shotTick,
                tag.getFloat("laser_rain_chance"))) {
            applySkyRain(projectile.world, hit, player, tag.getInteger("laser_rain_count"),
                    tag.getFloat("laser_rain_radius"), projectileDamage(projectile), false);
        }
        if (tag.getFloat("plasma_rain_chance") > 0.0f
                && impactTriggerMatches(tag, EffectAction.PLASMA_RAIN, result)
                && rollTagChanceForShot(tag, "runtime_plasma_rain_tick", shotTick,
                tag.getFloat("plasma_rain_chance"))) {
            applySkyRain(projectile.world, hit, player, tag.getInteger("plasma_rain_count"),
                    tag.getFloat("plasma_rain_radius"), projectileDamage(projectile), true);
        }

        if (projectile.getEntityData().getBoolean("tgu_periodic_area_explosion")
                && impactTriggerMatches(tag, EffectAction.PERIODIC_AREA_EXPLOSION, result)) {
            applyMythicAreaDamage(projectile.world, hit, player,
                    result.entityHit instanceof EntityLivingBase
                            ? (EntityLivingBase) result.entityHit : null,
                    tag.getFloat("periodic_area_radius"), projectileDamage(projectile)
                            * positiveMultiplier(tag, "periodic_area_multiplier"), 0, 0.8f);
            SpecialEffectsHandler.handleExplosive(projectile.world, hit.x, hit.y, hit.z, 4.0f);
        }

        float acidRadius = tag.getFloat("acid_cloud_radius");
        if (acidRadius > 0.0f
                && impactTriggerMatches(tag, EffectAction.ACID_CLOUD, result)
                && projectile.getEntityData().getBoolean("tgu_charged_projectile")) {
            UltraAreaEffectTracker.create(projectile.world, hit, player, acidRadius,
                    Math.max(20, tag.getInteger("acid_cloud_duration")),
                    Math.max(3.0f, projectileDamage(projectile) * 0.20f), 0.0f,
                    MobEffects.POISON, 40, EnumParticleTypes.SLIME, UpgradeVisualEffects.ZoneStyle.ACID_CLOUD);
        }

        float zoneChance = tag.getFloat("timed_zone_chance");
        if (zoneChance > 0.0f
                && impactTriggerMatches(tag, EffectAction.RANDOM_TIMED_ZONE, result)
                && rollTagChanceForShot(tag, "runtime_timed_zone_tick",
                shotTick, zoneChance)) {
            UltraAreaEffectTracker.create(projectile.world, hit, player,
                    tag.getFloat("timed_zone_radius"), tag.getInteger("timed_zone_duration"),
                    Math.max(4.0f, projectileDamage(projectile) * 0.25f), 0.2f,
                    null, 0, EnumParticleTypes.PORTAL, UpgradeVisualEffects.ZoneStyle.INSTABILITY);
        }
    }

    private static Vec3d center(EntityLivingBase entity) {
        return new Vec3d(entity.posX, entity.posY + entity.height * 0.5, entity.posZ);
    }

    private static boolean rollProjectileChance(GenericProjectile projectile, String key,
                                                float chance) {
        if (chance <= 0.0f) return false;
        String checked = "tgu_ultra_checked_" + key;
        if (projectile.getEntityData().getBoolean(checked)) return false;
        projectile.getEntityData().setBoolean(checked, true);
        return DebugSettings.roll(RANDOM, chance);
    }

    private static boolean impactTriggerMatches(NBTTagCompound tag, EffectAction action,
                                                RayTraceResult result) {
        if (!tag.hasKey("block_impact_actions", 10)
                || !tag.getCompoundTag("block_impact_actions").getBoolean(action.name())) {
            return true;
        }
        return result != null && result.typeOfHit == RayTraceResult.Type.BLOCK;
    }

    private static boolean rollTagChanceForShot(NBTTagCompound tag, String key,
                                                long shotTick, float chance) {
        if (chance <= 0.0f || (tag.hasKey(key) && tag.getLong(key) == shotTick)) return false;
        tag.setLong(key, shotTick);
        return DebugSettings.roll(RANDOM, chance);
    }

    private static void applyLightningToAll(World world, EntityLivingBase center,
                                            EntityPlayer player, float radius, float damage) {
        AxisAlignedBB area = center.getEntityBoundingBox().grow(radius);
        for (EntityLivingBase target : world.getEntitiesWithinAABB(EntityLivingBase.class, area)) {
            if (target == center || !canAffect(player, target)
                    || !target.isEntityAlive() || target.getDistance(center) > radius) continue;
            target.attackEntityFrom(new EntityDamageSource("tgu_mythic_area", player)
                    .setMagicDamage(), damage);
            SpecialEffectsHandler.spawnElectricArc(world, center, target);
        }
    }

    private static void createNuclearImpact(World world, Vec3d hit, EntityPlayer player,
                                            float radius, float damage) {
        float safeRadius = Math.max(4.0f, radius);
        NuclearDetonationTracker.schedule(world, hit, player, safeRadius,
                Math.max(20.0f, damage));
    }

    static void detonateNuclearImpact(World world, Vec3d hit, EntityPlayer player,
                                      float radius, float damage) {
        float safeRadius = Math.max(4.0f, radius);
        applyMythicAreaDamage(world, hit, player, null, safeRadius,
                Math.max(20.0f, damage), 100, 2.0f);
        NuclearExplosionTracker.create(world, hit, player, safeRadius);
    }

    private static void applyArtillery(World world, Vec3d hit, int count, float radius) {
        int safeCount = Math.max(0, Math.min(6, count));
        for (int i = 0; i < safeCount; i++) {
            double angle = RANDOM.nextDouble() * Math.PI * 2.0;
            double distance = RANDOM.nextDouble() * Math.max(2.0f, radius);
            Vec3d landing = new Vec3d(hit.x + Math.cos(angle) * distance,
                    hit.y, hit.z + Math.sin(angle) * distance);
            UpgradeVisualEffects.artilleryStrike(world, landing);
            SpecialEffectsHandler.handleExplosive(world, landing.x, landing.y, landing.z, 4.0f);
        }
    }

    private static void applyFireRain(World world, Vec3d hit, EntityPlayer player,
                                      int count, float radius) {
        if (world == null || world.isRemote) return;
        for (int i = 0; i < count; i++) {
            double x = hit.x + (RANDOM.nextDouble() - 0.5) * radius * 2.0;
            double z = hit.z + (RANDOM.nextDouble() - 0.5) * radius * 2.0;
            BlockPos ground = world.getTopSolidOrLiquidBlock(new BlockPos(x, hit.y, z));
            double landingY = Math.max(hit.y - 4.0, Math.min(hit.y + 4.0, ground.getY()));
            UpgradeVisualEffects.fireRainStrike(world, new Vec3d(x, landingY, z));
            AxisAlignedBB impact = new AxisAlignedBB(x - 1.5, landingY - 1.0, z - 1.5,
                    x + 1.5, landingY + 2.5, z + 1.5);
            for (EntityLivingBase target : world.getEntitiesWithinAABB(
                    EntityLivingBase.class, impact)) {
                if (!canAffect(player, target) || !target.isEntityAlive()) continue;
                target.attackEntityFrom(new EntityDamageSource("tgu_fire_rain", player)
                        .setFireDamage(), 8.0f);
                target.setFire(6);
            }
            GroundFireTracker.createFireZone(world, new BlockPos(x, landingY, z),
                    1, 120);
        }
    }

    private static void applySkyRain(World world, Vec3d hit, EntityPlayer player, int count,
                                     float radius, float damage, boolean explosive) {
        List<EntityLivingBase> targets = world.getEntitiesWithinAABB(EntityLivingBase.class,
                new AxisAlignedBB(hit.x - radius, hit.y - radius, hit.z - radius,
                        hit.x + radius, hit.y + radius, hit.z + radius));
        targets.removeIf(target -> !canAffect(player, target) || !target.isEntityAlive());
        for (int i = 0; i < count; i++) {
            EntityLivingBase target = targets.isEmpty() ? null : targets.get(i % targets.size());
            double x = target == null ? hit.x + (RANDOM.nextDouble() - 0.5) * radius * 2.0
                    : target.posX;
            double y = target == null ? hit.y : target.posY;
            double z = target == null ? hit.z + (RANDOM.nextDouble() - 0.5) * radius * 2.0
                    : target.posZ;
            UpgradeVisualEffects.skyStrike(world, new Vec3d(x, y, z), explosive
                    ? UpgradeVisualEffects.BeamStyle.PLASMA
                    : UpgradeVisualEffects.BeamStyle.LASER);
            if (target != null) {
                target.attackEntityFrom(new EntityDamageSource("tgu_mythic_area", player)
                        .setMagicDamage(), Math.max(5.0f, damage * 0.6f));
            }
            if (explosive) {
                SpecialEffectsHandler.handleExplosive(world, x, y, z, 3.0f);
            }
        }
    }

    private static void spawnRadialProjectiles(GenericProjectile original, EntityPlayer shooter,
                                               int count, float range, boolean acidChildren) {
        Vec3d position = new Vec3d(shooter.posX,
                shooter.posY + shooter.getEyeHeight(), shooter.posZ);
        float damage = Math.max(1.0f, projectileDamage(original));
        for (int i = 0; i < count; i++) {
            double angle = Math.PI * 2.0 * i / Math.max(1, count);
            Vec3d direction = new Vec3d(Math.cos(angle), 0.04, Math.sin(angle)).normalize();
            applyComputationalProjectileRay(original.world, original, shooter, position,
                    direction, range, damage, acidChildren);
        }
    }

    private static void spawnTargetedProjectiles(GenericProjectile original, EntityPlayer shooter,
                                                 int count, float range) {
        AxisAlignedBB area = shooter.getEntityBoundingBox().grow(range);
        List<EntityLivingBase> targets = new ArrayList<>();
        for (EntityLivingBase candidate : shooter.world.getEntitiesWithinAABB(
                EntityLivingBase.class, area)) {
            if (canAffect(shooter, candidate) && candidate.isEntityAlive()) targets.add(candidate);
        }
        targets.sort((left, right) -> Double.compare(left.getDistanceSq(shooter),
                right.getDistanceSq(shooter)));
        if (targets.isEmpty()) return;
        Vec3d position = new Vec3d(shooter.posX,
                shooter.posY + shooter.getEyeHeight(), shooter.posZ);
        float damage = Math.max(1.0f, projectileDamage(original));
        for (int i = 0; i < count; i++) {
            EntityLivingBase target = targets.get(i % targets.size());
            Vec3d direction = center(target).subtract(position).normalize();
            applyComputationalProjectileRay(original.world, original, shooter, position,
                    direction, range, damage, false);
        }
    }

    private static void applyComputationalProjectileRay(World world,
                                                        GenericProjectile original,
                                                        EntityPlayer shooter,
                                                        Vec3d start, Vec3d direction,
                                                        float range, float damage,
                                                        boolean poison) {
        Vec3d requestedEnd = start.add(direction.scale(Math.max(1.0f, range)));
        RayTraceResult blockHit = world.rayTraceBlocks(start, requestedEnd,
                false, true, false);
        Vec3d end = blockHit != null && blockHit.hitVec != null
                ? blockHit.hitVec : requestedEnd;
        AxisAlignedBB bounds = new AxisAlignedBB(start.x, start.y, start.z,
                end.x, end.y, end.z).grow(0.45);
        EntityLivingBase closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (EntityLivingBase candidate : world.getEntitiesWithinAABB(
                EntityLivingBase.class, bounds)) {
            if (!canAffect(shooter, candidate) || !candidate.isEntityAlive()) continue;
            RayTraceResult intercept = candidate.getEntityBoundingBox().grow(0.30)
                    .calculateIntercept(start, end);
            if (intercept == null) continue;
            double distance = start.squareDistanceTo(intercept.hitVec);
            if (distance < closestDistance) {
                closest = candidate;
                closestDistance = distance;
                end = intercept.hitVec;
            }
        }
        if (closest != null) {
            closest.attackEntityFrom(new EntityDamageSourceIndirect("tgu_mythic_area",
                    original, shooter), damage);
            if (poison) {
                closest.addPotionEffect(new PotionEffect(MobEffects.POISON, 100, 1));
            }
        }
        UpgradeVisualEffects.beam(world, start, end, poison
                ? UpgradeVisualEffects.BeamStyle.POISON
                : UpgradeVisualEffects.BeamStyle.SHRAPNEL);
        if (closest != null) {
            UpgradeVisualEffects.areaBurst(world, end, 1.3f, poison
                    ? UpgradeVisualEffects.AreaStyle.POISON
                    : UpgradeVisualEffects.AreaStyle.IMPACT);
        }
    }

    private static void spawnSplitProjectiles(GenericProjectile original, EntityPlayer shooter,
                                              Vec3d hit, int count, int generation) {
        Vec3d forward = new Vec3d(original.motionX, original.motionY, original.motionZ);
        if (forward.lengthSquared() < 1.0e-6) forward = shooter.getLookVec();
        forward = forward.normalize();
        for (int i = 0; i < count; i++) {
            double spread = (i - (count - 1) / 2.0) * 0.28;
            Vec3d direction = forward.add(new Vec3d(-forward.z * spread,
                    0.12 + RANDOM.nextDouble() * 0.10, forward.x * spread)).normalize();
            spawnUltraClone(original, shooter, hit.add(direction.scale(0.3)), direction,
                    false, generation, 24.0f);
        }
    }

    private static void handleSplitterImpact(GenericProjectile projectile, EntityPlayer shooter,
                                              Vec3d hit, NBTTagCompound tag) {
        NBTTagCompound data = projectile.getEntityData();
        if (data.getBoolean("tgu_split_spawned")) return;
        int generation = data.getInteger("tgu_split_generation");
        int extra = Math.max(0, tag.getInteger("split_shard_add"));
        int count = 0;
        ItemStack firedGun = gunFromProjectile(projectile);
        boolean splitterWeapon = !firedGun.isEmpty()
                && firedGun.getItem().getRegistryName() != null
                && "techguns:mibgun".equals(firedGun.getItem().getRegistryName().toString());

        int cascadeCap = Math.max(0, tag.getInteger("cascade_shard_count"));
        if (cascadeCap > 0 && generation < 2) {
            long shotTick = data.getLong("tgu_shot_tick");
            if (tag.getLong("runtime_cascade_budget_tick") != shotTick) {
                tag.setLong("runtime_cascade_budget_tick", shotTick);
                tag.setInteger("runtime_cascade_spawned", 0);
            }
            int remaining = cascadeCap - tag.getInteger("runtime_cascade_spawned");
            count = Math.min(Math.max(0, remaining), 2 + extra);
            tag.setInteger("runtime_cascade_spawned",
                    tag.getInteger("runtime_cascade_spawned") + count);
        } else if (generation == 0 && splitterWeapon) {
            count = 2 + RANDOM.nextInt(2) + extra;
        } else if (generation == 1 && tag.getFloat("split_again_chance") > 0.0f
                && DebugSettings.roll(RANDOM, tag.getFloat("split_again_chance"))) {
            count = 2 + extra;
        }
        if (count <= 0) return;
        data.setBoolean("tgu_split_spawned", true);
        UpgradeVisualEffects.areaBurst(projectile.world, hit, 1.6f, UpgradeVisualEffects.AreaStyle.PULSE);
        spawnSplitterProjectiles(projectile, shooter, hit, count, generation + 1);
    }

    private static void spawnSplitterProjectiles(GenericProjectile original,
                                                  EntityPlayer shooter, Vec3d hit,
                                                  int count, int generation) {
        if (projDamageField == null || projDamageMinField == null
                || projDamageDropStartField == null || projDamageDropEndField == null
                || projPenetrationField == null || projBlockdamageField == null) return;
        try {
            Vec3d forward = new Vec3d(original.motionX, original.motionY, original.motionZ);
            if (forward.lengthSquared() < 1.0e-6) forward = shooter.getLookVec();
            forward = forward.normalize();
            float childScale = generation > 1 ? 0.55f : 0.70f;
            for (int i = 0; i < count; i++) {
                double spread = (i - (count - 1) / 2.0) * 0.24;
                Vec3d direction = forward.add(new Vec3d(-forward.z * spread,
                        0.05 + (i % 2) * 0.06, forward.x * spread)).normalize();
                float speed = Math.max(0.45f, original.speed * 0.85f);
                GenericProjectile child = new GenericProjectile(original.world, shooter,
                        projDamageField.getFloat(original) * childScale, speed, 45, 0.03f,
                        projDamageDropStartField.getFloat(original),
                        projDamageDropEndField.getFloat(original),
                        projDamageMinField.getFloat(original) * childScale,
                        projPenetrationField.getFloat(original),
                        projBlockdamageField.getBoolean(original)
                                && SafeModeAccess.canDamageBlocks(shooter),
                        EnumBulletFirePos.CENTER);
                child.setPosition(hit.x + direction.x * 0.3,
                        hit.y + direction.y * 0.3, hit.z + direction.z * 0.3);
                child.setVelocity(direction.x * speed, direction.y * speed, direction.z * speed);
                copyProjectileRuntimeData(original, child);
                child.getEntityData().setBoolean("tgu_split_child", true);
                child.getEntityData().setInteger("tgu_split_generation", generation);
                clonedProjectiles.add(child);
                original.world.spawnEntity(child);
            }
        } catch (Exception e) {
            TechgunsUpgradeMod.LOGGER.warn("Failed to spawn splitter shards: " + e.getMessage());
        }
    }

    private static void spawnUltraClone(GenericProjectile original, EntityPlayer shooter,
                                        Vec3d position, Vec3d direction,
                                        boolean acidChild, int splitGeneration, float maxRange) {
        if (projDamageField == null || projDamageMinField == null
                || projDamageDropStartField == null || projDamageDropEndField == null
                || projPenetrationField == null || projTicksToLiveField == null
                || projBlockdamageField == null) return;
        try {
            float speed = Math.max(0.5f, original.speed);
            GenericProjectile clone = new GenericProjectile(original.world, shooter,
                    projDamageField.getFloat(original), speed,
                    Math.min(80, projTicksToLiveField.getInt(original)), 0.03f,
                    projDamageDropStartField.getFloat(original),
                    projDamageDropEndField.getFloat(original),
                    projDamageMinField.getFloat(original), projPenetrationField.getFloat(original),
                    projBlockdamageField.getBoolean(original)
                            && SafeModeAccess.canDamageBlocks(shooter),
                    EnumBulletFirePos.CENTER);
            clone.setPosition(position.x, position.y, position.z);
            clone.setVelocity(direction.x * speed, direction.y * speed, direction.z * speed);
            clone.getEntityData().setBoolean("tgu_ultra_secondary", true);
            clone.getEntityData().setFloat("tgu_ultra_secondary_range", maxRange);
            if (acidChild) clone.getEntityData().setBoolean("tgu_ultra_acid_child", true);
            if (splitGeneration > 0) {
                clone.getEntityData().setInteger("tgu_ultra_split_generation", splitGeneration);
            }
            clone.getEntityData().setLong("tgu_shot_tick",
                    original.getEntityData().getLong("tgu_shot_tick"));
            clone.getEntityData().setInteger("tgu_shot_sequence",
                    original.getEntityData().getInteger("tgu_shot_sequence"));
            clonedProjectiles.add(clone);
            original.world.spawnEntity(clone);
        } catch (Exception e) {
            TechgunsUpgradeMod.LOGGER.warn("Failed to spawn Ultra-Mythic projectile: "
                    + e.getMessage());
        }
    }

    private static void applyRailgunPierce(GenericProjectile projectile, EntityPlayer player,
                                           EntityLivingBase directTarget, float damage,
                                           float range) {
        Vec3d direction = new Vec3d(projectile.motionX, projectile.motionY,
                projectile.motionZ);
        if (direction.lengthSquared() < 1.0e-6) return;
        direction = direction.normalize();
        Vec3d start = new Vec3d(projectile.posX, projectile.posY, projectile.posZ);
        Vec3d end = start.add(direction.scale(Math.max(1.0f, range)));
        UpgradeVisualEffects.beam(projectile.world, start, end, UpgradeVisualEffects.BeamStyle.LASER);
        AxisAlignedBB area = new AxisAlignedBB(start.x, start.y, start.z,
                end.x, end.y, end.z).grow(2.0);
        for (EntityLivingBase candidate : projectile.world.getEntitiesWithinAABB(
                EntityLivingBase.class, area)) {
            if (candidate == player || candidate == directTarget || !candidate.isEntityAlive()) continue;
            Vec3d offset = new Vec3d(candidate.posX - start.x,
                    candidate.posY + candidate.height * 0.5 - start.y,
                    candidate.posZ - start.z);
            double along = offset.dotProduct(direction);
            if (along <= 0.0 || along > range) continue;
            double perpendicular = Math.sqrt(offset.subtract(direction.scale(along)).lengthSquared());
            if (perpendicular > Math.max(0.8, candidate.width * 0.7 + 0.5)) continue;
            candidate.attackEntityFrom(new EntityDamageSourceIndirect("tgu_railgun",
                    projectile, player).setProjectile(), damage);
        }
    }

    private static float projectileDamage(GenericProjectile projectile) {
        try {
            return projDamageField == null ? 1.0f : projDamageField.getFloat(projectile);
        } catch (IllegalAccessException ignored) {
            return 1.0f;
        }
    }

    private static EntityLivingBase findNearestEnemy(World world, Vec3d center,
                                                     EntityPlayer player, float range) {
        AxisAlignedBB area = new AxisAlignedBB(center.x - range, center.y - range,
                center.z - range, center.x + range, center.y + range, center.z + range);
        EntityLivingBase nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (EntityLivingBase candidate : world.getEntitiesWithinAABB(EntityLivingBase.class, area)) {
            if (candidate == player || !candidate.isEntityAlive()) continue;
            double distance = candidate.getDistanceSq(center.x, center.y, center.z);
            if (distance < nearestDistance) {
                nearest = candidate;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private static void applyMythicAreaDamage(World world, Vec3d center, EntityPlayer shooter,
                                               EntityLivingBase excluded, float radius,
                                               float damage, int fireTicks, float knockback) {
        applyMythicAreaDamage(world, center, shooter, excluded, radius, damage, fireTicks,
                knockback, UpgradeVisualEffects.AreaStyle.IMPACT);
    }

    private static void applyMythicAreaDamage(World world, Vec3d center, EntityPlayer shooter,
                                               EntityLivingBase excluded, float radius,
                                               float damage, int fireTicks, float knockback,
                                               UpgradeVisualEffects.AreaStyle visualStyle) {
        if (radius <= 0.0f || damage <= 0.0f) return;
        AxisAlignedBB area = new AxisAlignedBB(center.x - radius, center.y - radius,
                center.z - radius, center.x + radius, center.y + radius, center.z + radius);
        DamageSource source = new EntityDamageSource("tgu_mythic_area", shooter).setExplosion();
        for (EntityLivingBase nearby : world.getEntitiesWithinAABB(EntityLivingBase.class, area)) {
            if (nearby == excluded || !canAffect(shooter, nearby)
                    || !nearby.isEntityAlive()) continue;
            double distance = nearby.getDistance(center.x, center.y, center.z);
            if (distance > radius) continue;
            float falloff = Math.max(0.25f, 1.0f - (float) distance / radius);
            nearby.attackEntityFrom(source, damage * falloff);
            if (fireTicks > 0) nearby.setFire(Math.max(1, fireTicks / 20));
            if (knockback > 0.0f) {
                double dx = nearby.posX - center.x;
                double dz = nearby.posZ - center.z;
                double length = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
                nearby.addVelocity(dx / length * knockback, Math.min(0.8f, knockback * 0.3f),
                        dz / length * knockback);
                nearby.velocityChanged = true;
            }
        }
        UpgradeVisualEffects.areaBurst(world, center, radius, visualStyle);
    }

    private static float positiveMultiplier(NBTTagCompound tag, String key) {
        float value = tag.getFloat(key);
        return Float.isFinite(value) && value > 0.0f ? value : 1.0f;
    }

    private static void copyProjectileRuntimeData(GenericProjectile source, GenericProjectile target) {
        NBTTagCompound from = source.getEntityData();
        NBTTagCompound to = target.getEntityData();
        to.setInteger("tgu_shot_sequence", from.getInteger("tgu_shot_sequence"));
        to.setInteger("tgu_continuous_shots", from.getInteger("tgu_continuous_shots"));
        to.setInteger("tgu_magazine_shot", from.getInteger("tgu_magazine_shot"));
        to.setInteger("tgu_ammo_before", from.getInteger("tgu_ammo_before"));
        to.setInteger("tgu_ammo_after", from.getInteger("tgu_ammo_after"));
        to.setInteger("tgu_clip_size", from.getInteger("tgu_clip_size"));
        to.setLong("tgu_shot_tick", from.getLong("tgu_shot_tick"));
        if (from.hasKey(PROJECTILE_CONTEXT, 10)) {
            to.setTag(PROJECTILE_CONTEXT, from.getCompoundTag(PROJECTILE_CONTEXT).copy());
        }
        if (from.getBoolean("tgu_random_damage_triggered")) {
            to.setBoolean("tgu_random_damage_triggered", true);
        }
        if (from.hasKey("tgu_heat_seeking_strength")) {
            to.setFloat("tgu_heat_seeking_strength", from.getFloat("tgu_heat_seeking_strength"));
        }
        if (from.getBoolean("tgu_charged_projectile")) {
            to.setBoolean("tgu_charged_projectile", true);
            to.setFloat("tgu_charge_ticks", from.getFloat("tgu_charge_ticks"));
            to.setBoolean("tgu_fully_charged", from.getBoolean("tgu_fully_charged"));
        }
        if (from.hasKey("tgu_threshold_explosion_radius")) {
            to.setFloat("tgu_threshold_explosion_radius",
                    from.getFloat("tgu_threshold_explosion_radius"));
        }
        if (from.hasKey("tgu_mythic_nuclear_radius")) {
            to.setFloat("tgu_mythic_nuclear_radius",
                    from.getFloat("tgu_mythic_nuclear_radius"));
        }
    }

    private static void pullTargetTowardPlayer(EntityLivingBase target, EntityPlayer player) {
        Vec3d pull = new Vec3d(player.posX - target.posX,
                player.posY + player.getEyeHeight() * 0.5 - target.posY,
                player.posZ - target.posZ).normalize();
        target.addVelocity(pull.x * 0.9, Math.max(0.1, pull.y * 0.5), pull.z * 0.9);
        target.velocityChanged = true;
        UpgradeVisualEffects.beam(player.world, center(target),
                new Vec3d(player.posX, player.posY + player.getEyeHeight() * 0.5, player.posZ),
                UpgradeVisualEffects.BeamStyle.ARC);
    }

    private static void applyChainBeam(GenericProjectile projectile, EntityPlayer shooter,
                                       EntityLivingBase directTarget, float damage, int targets) {
        NBTTagCompound data = projectile.getEntityData();
        if (data.getBoolean("tgu_chain_beam_active")) return;
        data.setBoolean("tgu_chain_beam_active", true);
        try {
            EntityLivingBase current = directTarget;
            Set<EntityLivingBase> hit = Collections.newSetFromMap(new java.util.IdentityHashMap<>());
            hit.add(directTarget);
            for (int i = 0; i < targets; i++) {
                EntityLivingBase nearest = null;
                double distance = Double.MAX_VALUE;
                for (EntityLivingBase candidate : current.world.getEntitiesWithinAABB(EntityLivingBase.class,
                        current.getEntityBoundingBox().grow(8.0))) {
                    if (candidate == shooter || hit.contains(candidate) || !candidate.isEntityAlive()) continue;
                    double candidateDistance = candidate.getDistanceSq(current);
                    if (candidateDistance < distance) {
                        nearest = candidate;
                        distance = candidateDistance;
                    }
                }
                if (nearest == null) break;
                hit.add(nearest);
                SpecialEffectsHandler.spawnElectricArc(current.world, current, nearest);
                nearest.attackEntityFrom(new EntityDamageSourceIndirect("tgu_chain_beam",
                        projectile, shooter).setMagicDamage(), damage * 0.50f);
                current = nearest;
            }
        } finally {
            data.setBoolean("tgu_chain_beam_active", false);
        }
    }

    private static void applyImpactShockwave(World world, Vec3d center, EntityPlayer shooter,
                                             float strength) {
        double radius = Math.max(4.0, strength);
        AxisAlignedBB area = new AxisAlignedBB(center.x - radius, center.y - radius,
                center.z - radius, center.x + radius, center.y + radius, center.z + radius);
        for (EntityLivingBase nearby : world.getEntitiesWithinAABB(EntityLivingBase.class, area)) {
            if (!canAffect(shooter, nearby) || !nearby.isEntityAlive()) continue;
            double dx = nearby.posX - center.x;
            double dz = nearby.posZ - center.z;
            displaceByBlocks(nearby, dx, dz, strength);
        }
        UpgradeVisualEffects.areaBurst(world, center, (float) radius,
                UpgradeVisualEffects.AreaStyle.PULSE);
    }

    private static void applyAreaPotion(World world, Vec3d center, EntityPlayer shooter,
                                        double radius, Potion potion, int duration, int amplifier) {
        AxisAlignedBB area = new AxisAlignedBB(center.x - radius, center.y - radius,
                center.z - radius, center.x + radius, center.y + radius, center.z + radius);
        for (EntityLivingBase nearby : world.getEntitiesWithinAABB(EntityLivingBase.class, area)) {
            if (!canAffect(shooter, nearby) || !nearby.isEntityAlive()) continue;
            nearby.addPotionEffect(new PotionEffect(potion, duration, amplifier));
        }
    }

    private static void spawnImpactSmoke(World world, Vec3d center) {
        UpgradeVisualEffects.smokeCloud(world, center, 2.8f);
    }

    private static void calmNearbyMobs(EntityLivingBase killed, float radius, EntityPlayer player) {
        AxisAlignedBB area = killed.getEntityBoundingBox().grow(radius);
        for (EntityLiving mob : killed.world.getEntitiesWithinAABB(EntityLiving.class, area)) {
            if (mob.getAttackTarget() == player) mob.setAttackTarget(null);
            if (mob.getRevengeTarget() == player) mob.setRevengeTarget(null);
        }
    }

    private static void applyTemporaryAttackSpeed(EntityPlayer player, float multiplier, int duration) {
        IAttributeInstance attribute = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED);
        if (attribute == null) return;
        AttributeModifier current = attribute.getModifier(EPIC_TEMPORARY_ATTACK_SPEED);
        if (current != null) attribute.removeModifier(current);
        attribute.applyModifier(new AttributeModifier(EPIC_TEMPORARY_ATTACK_SPEED,
                "Techguns Epic kill attack speed", multiplier - 1.0, 2).setSaved(false));
        player.getEntityData().setLong("tgu_epic_attack_speed_until",
                player.world.getTotalWorldTime() + duration);
    }

    private static boolean consumeAdditionalAmmo(ItemStack gun, int amount, EntityPlayer player) {
        if (amount <= 0 || player.capabilities.isCreativeMode
                || GunStatModifiers.isInfiniteFuel(gun)) return true;
        if (GunStatModifiers.isBottomlessMagazine(gun)) {
            return GunStatModifiers.consumeBottomlessMagazineAmmo(gun, player, amount, false);
        }
        GenericGun gunItem = (GenericGun) gun.getItem();
        int current = gunItem.getCurrentAmmo(gun);
        if (gun.getTagCompound() == null) return false;
        int fromMagazine = Math.min(current, amount);
        int fromInventory = amount - fromMagazine;
        if (fromInventory > 0
                && !GunStatModifiers.consumeInventoryAmmoItems(gun, player, fromInventory)) {
            return false;
        }
        gun.getTagCompound().setShort("ammo", (short) (current - fromMagazine));
        return true;
    }

    private static void createSmokeScreen(EntityPlayer player) {
        player.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, 10, 0, false, false));
        UpgradeVisualEffects.smokeCloud(player.world,
                new Vec3d(player.posX, player.posY + 0.5, player.posZ), 1.5f);
    }

    private static void restoreAmmo(ItemStack gun, int amount) {
        if (amount <= 0 || gun.getTagCompound() == null || !(gun.getItem() instanceof GenericGun)) return;
        GenericGun item = (GenericGun) gun.getItem();
        int current = item.getCurrentAmmo(gun);
        int maximum = GunStatModifiers.clipSize(gun, item.getClipsize());
        gun.getTagCompound().setShort("ammo", (short) Math.min(maximum, current + amount));
    }

    private static void applyContagion(EntityLivingBase directTarget, EntityPlayer shooter,
                                       double radius, int duration) {
        AxisAlignedBB area = directTarget.getEntityBoundingBox().grow(radius);
        UpgradeVisualEffects.areaBurst(directTarget.world, center(directTarget), (float) radius,
                UpgradeVisualEffects.AreaStyle.POISON);
        for (EntityLivingBase nearby : directTarget.world.getEntitiesWithinAABB(EntityLivingBase.class, area)) {
            if (nearby == directTarget || nearby == shooter || !nearby.isEntityAlive()) continue;
            nearby.addPotionEffect(new PotionEffect(MobEffects.POISON, Math.max(20, duration), 1));
            UpgradeVisualEffects.beam(directTarget.world, center(directTarget), center(nearby),
                    UpgradeVisualEffects.BeamStyle.POISON);
        }
    }

    private static void spawnClusterProjectiles(GenericProjectile original, EntityPlayer shooter,
                                                Vec3d hit, int count) {
        if (projDamageField == null || projDamageMinField == null || projDamageDropStartField == null
                || projDamageDropEndField == null || projPenetrationField == null
                || projBlockdamageField == null) return;
        try {
            float damage = projDamageField.getFloat(original) * 0.40f;
            float damageMin = projDamageMinField.getFloat(original) * 0.40f;
            float damageDropStart = projDamageDropStartField.getFloat(original);
            float damageDropEnd = projDamageDropEndField.getFloat(original);
            float penetration = projPenetrationField.getFloat(original);
            int boundedCount = Math.min(3, Math.max(0, count));
            for (int i = 0; i < boundedCount; i++) {
                GenericProjectile child = new GenericProjectile(original.world, shooter, damage,
                        Math.max(0.2f, original.speed * 0.35f), CLUSTER_CHILD_MAX_TICKS, 0.05f,
                        damageDropStart, damageDropEnd, damageMin, penetration,
                        false, EnumBulletFirePos.CENTER);
                child.setPosition(hit.x, hit.y + 0.2, hit.z);
                double angle = 2.0 * Math.PI * i / Math.max(1, boundedCount)
                        + RANDOM.nextDouble() * 0.25;
                child.setVelocity(Math.cos(angle) * 0.22,
                        0.12 + RANDOM.nextDouble() * 0.08, Math.sin(angle) * 0.22);
                child.getEntityData().setBoolean("tgu_cluster_child", true);
                child.getEntityData().setDouble("tgu_cluster_origin_x", hit.x);
                child.getEntityData().setDouble("tgu_cluster_origin_y", hit.y + 0.2);
                child.getEntityData().setDouble("tgu_cluster_origin_z", hit.z);
                clonedProjectiles.add(child);
                original.world.spawnEntity(child);
            }
        } catch (Exception e) {
            TechgunsUpgradeMod.LOGGER.warn("Failed to spawn cluster projectiles: " + e.getMessage());
        }
    }

    static boolean clusterChildExpired(int ticksExisted, double distanceSquared) {
        return ticksExisted >= CLUSTER_CHILD_MAX_TICKS
                || !Double.isFinite(distanceSquared)
                || distanceSquared >= CLUSTER_CHILD_MAX_DISTANCE_SQ;
    }

    private static void detonateClusterChild(GenericProjectile projectile, Vec3d position) {
        if (projectile == null || position == null || projectile.world == null
                || projectile.world.isRemote) return;
        NBTTagCompound data = projectile.getEntityData();
        if (data.getBoolean("tgu_cluster_detonated")) return;
        data.setBoolean("tgu_cluster_detonated", true);
        SpecialEffectsHandler.handleExplosive(projectile.world,
                position.x, position.y, position.z, 1.0f);
        UpgradeVisualEffects.areaBurst(projectile.world, position, 1.5f,
                UpgradeVisualEffects.AreaStyle.IMPACT);
    }

    private static void applyKnockback(EntityLivingBase target, Entity source, float strength) {
        double dx = target.posX - source.posX;
        double dz = target.posZ - source.posZ;
        if (strength >= 2.0f) {
            displaceByBlocks(target, dx, dz, strength);
            return;
        }
        target.knockBack(source, strength, dx, dz);
    }

    private static void displaceByBlocks(EntityLivingBase target, double dx, double dz,
                                         float blocks) {
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length < 0.001 || blocks <= 0.0f) return;
        double nx = dx / length;
        double nz = dz / length;
        double safeDistance = 0.0;
        for (double distance = 0.25; distance <= blocks + 0.001; distance += 0.25) {
            AxisAlignedBB moved = target.getEntityBoundingBox().offset(
                    nx * distance, 0.0, nz * distance);
            if (!target.world.getCollisionBoxes(target, moved).isEmpty()) break;
            safeDistance = distance;
        }
        if (safeDistance > 0.0) {
            target.setPositionAndUpdate(target.posX + nx * safeDistance,
                    target.posY, target.posZ + nz * safeDistance);
            target.addVelocity(nx * 0.12, 0.12, nz * 0.12);
            target.velocityChanged = true;
        }
    }

    public static boolean canAffect(EntityPlayer shooter, EntityLivingBase target) {
        if (shooter == null || target == null || target == shooter) return false;
        if (!(target instanceof EntityPlayer)) return true;
        EntityPlayer other = (EntityPlayer) target;
        return TguConfig.enablePvpEffects && !shooter.isOnSameTeam(other)
                && shooter.canAttackPlayer(other);
    }

    private static float instantKillDamage(EntityPlayer shooter, EntityLivingBase target,
                                           float normalDamage) {
        if (!canAffect(shooter, target)) return normalDamage;
        if (!target.isNonBoss()) {
            return Math.max(normalDamage, target.getMaxHealth() * 0.25f);
        }
        return Math.max(normalDamage, target.getMaxHealth() * 20.0f);
    }

    private static void applyShockwave(EntityPlayer source, EntityLivingBase directTarget, float radius) {
        AxisAlignedBB area = directTarget.getEntityBoundingBox().grow(radius);
        for (EntityLivingBase nearby : directTarget.world.getEntitiesWithinAABB(EntityLivingBase.class, area)) {
            if (nearby == source || nearby == directTarget || !nearby.isEntityAlive()) continue;
            applyKnockback(nearby, directTarget, 0.6f);
        }
        UpgradeVisualEffects.areaBurst(directTarget.world, center(directTarget), radius,
                UpgradeVisualEffects.AreaStyle.PULSE);
    }

    private static void rememberDamageOverTimeBonus(EntityLivingBase target,
                                                     String multiplierKey, String expiryKey,
                                                     float multiplier, int durationTicks) {
        if (multiplier <= 1.0f) return;
        NBTTagCompound data = target.getEntityData();
        data.setFloat(multiplierKey, multiplier);
        data.setLong(expiryKey, target.world.getTotalWorldTime() + durationTicks);
    }

    private static float conditionalPoisonMultiplier(NBTTagCompound tag,
                                                     GenericProjectile projectile,
                                                     EntityLivingBase target) {
        float multiplier = 1.0f;
        int magazineShot = projectile.getEntityData().getInteger("tgu_magazine_shot");
        int ammoBefore = projectile.getEntityData().getInteger("tgu_ammo_before");
        int clipSize = projectile.getEntityData().getInteger("tgu_clip_size");
        if (tag.hasKey("poison_first_shot_multiplier") && clipSize > 0
                && magazineShot == 1 && ammoBefore == clipSize) {
            multiplier *= positiveMultiplier(tag, "poison_first_shot_multiplier");
        }
        if (tag.hasKey("poison_consecutive_multiplier")) {
            int shot = projectile.getEntityData().getInteger("tgu_shot_sequence");
            long now = target.world.getTotalWorldTime();
            String targetId = target.getUniqueID().toString();
            if (shot == tag.getInteger("runtime_poison_last_shot") + 1
                    && targetId.equals(tag.getString("runtime_poison_last_target"))
                    && now - tag.getLong("runtime_poison_last_tick") <= 60) {
                multiplier *= positiveMultiplier(tag, "poison_consecutive_multiplier");
            }
            tag.setInteger("runtime_poison_last_shot", shot);
            tag.setString("runtime_poison_last_target", targetId);
            tag.setLong("runtime_poison_last_tick", now);
        }
        return multiplier;
    }

    private static void rememberConditionalBurnBonus(EntityLivingBase target,
                                                     NBTTagCompound weaponTag) {
        int requiredTicks = weaponTag.getInteger("burn_damage_ticks");
        if (requiredTicks <= 0) return;
        long now = target.world.getTotalWorldTime();
        NBTTagCompound data = target.getEntityData();
        if (!data.hasKey("tgu_conditional_burn_until")
                || data.getLong("tgu_conditional_burn_until") < now) {
            data.setLong("tgu_conditional_burn_start", now);
        }
        data.setLong("tgu_conditional_burn_until", now + 200);
        data.setInteger("tgu_conditional_burn_ticks", requiredTicks);
        data.setFloat("tgu_conditional_burn_multiplier",
                positiveMultiplier(weaponTag, "burn_duration_damage_multiplier"));
    }

    private static void applyStoredDamageOverTimeBonus(LivingHurtEvent event, DamageSource source) {
        if (source.getImmediateSource() instanceof GenericProjectile) return;
        EntityLivingBase target = event.getEntityLiving();
        if (target == null) return;

        NBTTagCompound data = target.getEntityData();
        long now = target.world.getTotalWorldTime();
        String multiplierKey = null;
        String expiryKey = null;
        if (source.isFireDamage()) {
            multiplierKey = "tgu_burn_multiplier";
            expiryKey = "tgu_burn_until";
        } else if (source.isMagicDamage()) {
            multiplierKey = "tgu_poison_multiplier";
            expiryKey = "tgu_poison_until";
        }
        if (multiplierKey == null) return;

        if (data.hasKey(multiplierKey)) {
            if (data.getLong(expiryKey) >= now) {
                float multiplier = data.getFloat(multiplierKey);
                if (multiplier > 1.0f) event.setAmount(event.getAmount() * multiplier);
            } else {
                data.removeTag(multiplierKey);
                data.removeTag(expiryKey);
            }
        }
        if (source.isFireDamage()
                && data.getLong("tgu_conditional_burn_until") >= now
                && now - data.getLong("tgu_conditional_burn_start")
                >= data.getInteger("tgu_conditional_burn_ticks")) {
            float multiplier = data.getFloat("tgu_conditional_burn_multiplier");
            if (multiplier > 1.0f) event.setAmount(event.getAmount() * multiplier);
        }
    }

    private static void updateHeldMovementBonus(EntityPlayer player) {
        IAttributeInstance attribute = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (attribute == null) return;

        ItemStack held = player.getHeldItemMainhand();
        float multiplier = GunStatModifiers.movementSpeedMultiplier(held);
        if (!held.isEmpty() && held.hasTagCompound()
                && held.getTagCompound().hasKey("techgunsupgrade", 10)) {
            NBTTagCompound tag = held.getTagCompound().getCompoundTag("techgunsupgrade");
            if (tag.getBoolean("buffs_applied") && tag.hasKey("runtime_last_shot_tick")
                    && player.world.getTotalWorldTime()
                    - tag.getLong("runtime_last_shot_tick") <= 5) {
                multiplier *= GunStatModifiers.firingMovementSpeedMultiplier(held);
            }
        }
        AttributeModifier current = attribute.getModifier(COMMON_MOVEMENT_SPEED);
        double amount = multiplier - 1.0f;

        if (current != null && Math.abs(current.getAmount() - amount) < 0.000001) return;
        if (current != null) attribute.removeModifier(current);
        if (Math.abs(amount) > 0.000001) {
            attribute.applyModifier(new AttributeModifier(COMMON_MOVEMENT_SPEED,
                    "Techguns Common held movement", amount, 2).setSaved(false));
        }
    }

    private static void updateMovementHistory(EntityPlayer player) {
        if (player.motionX * player.motionX + player.motionZ * player.motionZ > 0.0004) {
            player.getEntityData().setLong("tgu_last_move_tick", player.world.getTotalWorldTime());
        }
    }

    private static void updateTemporaryAttackSpeed(EntityPlayer player) {
        IAttributeInstance attribute = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED);
        if (attribute == null) return;
        AttributeModifier current = attribute.getModifier(EPIC_TEMPORARY_ATTACK_SPEED);
        if (current != null && player.world.getTotalWorldTime()
                > player.getEntityData().getLong("tgu_epic_attack_speed_until")) {
            attribute.removeModifier(current);
        }
    }

    private static void updateLockOnProgress(EntityPlayer player) {
        ItemStack held = player.getHeldItemMainhand();
        if (held.isEmpty() || !(held.getItem() instanceof GuidedMissileLauncher)
                || !player.isHandActive()) return;

        NBTTagCompound tag = held.getOrCreateSubCompound("techgunsupgrade");
        TGExtendedPlayer data = TGExtendedPlayer.get(player);
        if (tag.getBoolean("instant_lock_on")) {
            if (data != null) data.lockOnTicks = Math.max(data.lockOnTicks, 200);
            return;
        }
        if (tag.getBoolean("runtime_instant_lock_pending")) {
            if (data != null) data.lockOnTicks = Math.max(data.lockOnTicks, 200);
            return;
        }

        float multiplier = GunStatModifiers.lockOnTimeMultiplier(held);
        if (multiplier >= 0.999f) return;

        double accumulated = tag.getDouble("lock_on_bonus_accumulator")
                + (1.0 / multiplier - 1.0);
        int extraTicks = (int) Math.floor(accumulated);
        if (extraTicks > 0) {
            if (data != null && data.lockOnTicks > 0) data.lockOnTicks += extraTicks;
            accumulated -= extraTicks;
        }
        tag.setDouble("lock_on_bonus_accumulator", accumulated);
    }

    private static void updateUltraHeldEffects(EntityPlayer player) {
        if (player.world.getTotalWorldTime() % 20 != 0) return;
        ItemStack gun = player.getHeldItemMainhand();
        if (gun.isEmpty() || !(gun.getItem() instanceof GenericGun)
                || !gun.hasTagCompound() || !gun.getTagCompound().hasKey("techgunsupgrade", 10)) {
            return;
        }
        NBTTagCompound tag = gun.getTagCompound().getCompoundTag("techgunsupgrade");
        if (!tag.getBoolean("buffs_applied")) return;
        long now = player.world.getTotalWorldTime();
        float base = tag.hasKey("base_damage") ? tag.getFloat("base_damage") : 8.0f;
        float damage = Math.max(4.0f, GunStatModifiers.meleeDamage(gun, base) * 0.35f);
        float vortex = tag.getFloat("melee_vortex_radius");
        if (vortex > 0.0f && now - tag.getLong("runtime_ultra_vortex_last_hit") <= 6) {
            applyMythicAreaDamage(player.world,
                    new Vec3d(player.posX, player.posY + 0.8, player.posZ),
                    player, null, vortex, damage, 0, 0.25f, UpgradeVisualEffects.AreaStyle.VORTEX);
        }
        float earthquake = tag.getFloat("continuous_earthquake_radius");
        if (earthquake > 0.0f
                && now - tag.getLong("runtime_ultra_earthquake_last_hit") <= 6) {
            applyMythicAreaDamage(player.world,
                    new Vec3d(player.posX, player.posY, player.posZ),
                    player, null, earthquake, damage, 0, 1.0f, UpgradeVisualEffects.AreaStyle.SEISMIC);
        }
    }

    private static float getModifiedDamage(ItemStack gun, float originalDamage) {
        return GunStatModifiers.meleeDamage(gun, originalDamage);
    }

    private static void applyBuffsToPlayer(EntityPlayer player) {
        if (player == null) return;

        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof GenericGun) {
                if (UpgradeApplicator.needsRefresh(stack)) {
                    UpgradeApplicator.applyUpgradesToGun(stack);
                }
            }
        }
    }

    private static void applyStackingBuff(ItemStack gun, NBTTagCompound tag) {
        String type = tag.getString("stacking_type");
        float valuePerKill = tag.getFloat("stacking_value_per_kill");
        int maxStack = tag.getInteger("stacking_max");

        String key = "stack_" + type;
        int current = tag.getInteger(key);

        if (current < maxStack) {
            current += (int) valuePerKill;
            if (current > maxStack) current = maxStack;
            tag.setInteger(key, current);

            float bonus = 1.0f + (float) current / 100.0f;
            if ("FIRE_RATE".equals(type)) {
                tag.setFloat("stack_firerate_bonus", 1.0f / bonus);
            } else if ("ACCURACY".equals(type)) {
                tag.setFloat("stack_accuracy_bonus", 1.0f / bonus);
            } else {
                tag.setFloat("stack_damage_bonus", bonus);
            }
        }
    }

    private static String ensureStackId(NBTTagCompound tag) {
        String id = tag.getString(STACK_ID);
        if (id.isEmpty()) {
            id = UUID.randomUUID().toString();
            tag.setString(STACK_ID, id);
        }
        return id;
    }

    private static ItemStack gunForDamageSource(EntityPlayer player, DamageSource source) {
        Entity immediate = source == null ? null : source.getImmediateSource();
        if (immediate instanceof GenericProjectile) {
            ItemStack snapshot = gunFromProjectile((GenericProjectile) immediate);
            if (!snapshot.isEmpty()) {
                mergeRuntimeStateFromLive(snapshot, player);
                return snapshot;
            }
        }
        return player == null ? ItemStack.EMPTY : player.getHeldItemMainhand();
    }

    public static ItemStack gunFromProjectile(GenericProjectile projectile) {
        if (projectile == null) return ItemStack.EMPTY;
        NBTTagCompound data = projectile.getEntityData();
        if (!data.hasKey(PROJECTILE_CONTEXT, 10)) return ItemStack.EMPTY;
        NBTTagCompound context = data.getCompoundTag(PROJECTILE_CONTEXT);
        if (!context.hasKey("gun", 10)) return ItemStack.EMPTY;
        try {
            return new ItemStack(context.getCompoundTag("gun"));
        } catch (RuntimeException ignored) {
            return ItemStack.EMPTY;
        }
    }

    private static ItemStack findLiveGun(EntityPlayer player, String stackId) {
        if (player == null || stackId == null || stackId.isEmpty()) return ItemStack.EMPTY;
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack candidate = player.inventory.getStackInSlot(i);
            if (matchesStackId(candidate, stackId)) return candidate;
        }
        ItemStack offhand = player.getHeldItemOffhand();
        return matchesStackId(offhand, stackId) ? offhand : ItemStack.EMPTY;
    }

    private static boolean matchesStackId(ItemStack stack, String stackId) {
        return stack != null && !stack.isEmpty() && stack.hasTagCompound()
                && stack.getTagCompound().hasKey("techgunsupgrade", 10)
                && stackId.equals(stack.getTagCompound().getCompoundTag("techgunsupgrade")
                .getString(STACK_ID));
    }

    private static void syncRuntimeState(ItemStack snapshot, EntityPlayer player) {
        if (snapshot == null || snapshot.isEmpty() || !snapshot.hasTagCompound()
                || !snapshot.getTagCompound().hasKey("techgunsupgrade", 10)) return;
        NBTTagCompound source = snapshot.getTagCompound().getCompoundTag("techgunsupgrade");
        ItemStack live = findLiveGun(player, source.getString(STACK_ID));
        if (live.isEmpty()) return;
        NBTTagCompound target = live.getOrCreateSubCompound("techgunsupgrade");
        for (String key : source.getKeySet()) {
            if (key.startsWith("runtime_") || key.startsWith("stack_")
                    || "mythic_stack_progress".equals(key)
                    || "ammo_cost_accumulator".equals(key)
                    || "lock_on_bonus_accumulator".equals(key)) {
                target.setTag(key, source.getTag(key).copy());
            }
        }
    }

    private static void mergeRuntimeStateFromLive(ItemStack snapshot, EntityPlayer player) {
        if (snapshot == null || snapshot.isEmpty() || !snapshot.hasTagCompound()
                || !snapshot.getTagCompound().hasKey("techgunsupgrade", 10)) return;
        NBTTagCompound target = snapshot.getTagCompound().getCompoundTag("techgunsupgrade");
        ItemStack live = findLiveGun(player, target.getString(STACK_ID));
        if (live.isEmpty() || !live.hasTagCompound()
                || !live.getTagCompound().hasKey("techgunsupgrade", 10)) return;
        NBTTagCompound source = live.getTagCompound().getCompoundTag("techgunsupgrade");
        for (String key : source.getKeySet()) {
            if (key.startsWith("runtime_") || key.startsWith("stack_")
                    || "mythic_stack_progress".equals(key)
                    || "ammo_cost_accumulator".equals(key)
                    || "lock_on_bonus_accumulator".equals(key)) {
                target.setTag(key, source.getTag(key).copy());
            }
        }
    }

    private static final class ShotContext {
        private final ItemStack gun;
        private final String stackId;
        private final int shotSequence;
        private final int continuousShots;
        private final int magazineShot;
        private final int ammoBefore;
        private final int ammoAfter;
        private final int clipSize;
        private final long shotTick;
        private final int lockedTargetId;
        private final int projectileOrdinal;

        private ShotContext(ItemStack gun, String stackId, int shotSequence,
                            int continuousShots, int magazineShot, int ammoBefore, int ammoAfter,
                            int clipSize, long shotTick, int lockedTargetId,
                            int projectileOrdinal) {
            this.gun = gun;
            this.stackId = stackId;
            this.shotSequence = shotSequence;
            this.continuousShots = continuousShots;
            this.magazineShot = magazineShot;
            this.ammoBefore = ammoBefore;
            this.ammoAfter = ammoAfter;
            this.clipSize = clipSize;
            this.shotTick = shotTick;
            this.lockedTargetId = lockedTargetId;
            this.projectileOrdinal = projectileOrdinal;
        }
    }

    public static Entity getShooter(GenericProjectile projectile) {
        try {
            if (shooterField != null) {
                return (Entity) shooterField.get(projectile);
            }
        } catch (Exception e) {}
        return null;
    }
}
package com.stepm.techgunsupgrade.event;

import com.stepm.techgunsupgrade.debug.DebugRandom;
import com.stepm.techgunsupgrade.debug.DebugSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class SpecialEffectsHandler {

    private static final Random RANDOM = new DebugRandom();

    public static void handleGoldDrop(EntityLivingBase killed) {
        if (DebugSettings.roll(RANDOM, 0.15f)) {
            killed.entityDropItem(new net.minecraft.item.ItemStack(net.minecraft.init.Items.GOLD_INGOT), 0.5f);
        }
    }

    public static void handleInvisibility(EntityPlayer player) {
        player.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, 100, 0));
    }

    public static void handleDeathScythe(World world, double x, double y, double z, Entity shooter) {
        if (DebugSettings.roll(RANDOM, 0.10f)) {
            for (Entity entity : world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(
                x - 30, y - 30, z - 30,
                x + 30, y + 30, z + 30
            ))) {
                if (entity instanceof EntityLivingBase && entity != shooter) {
                    entity.attackEntityFrom(DamageSource.MAGIC, 100.0f);
                }
            }
            spawnDeathScytheParticles(world, x, y, z);
        }
    }

    public static void handleDoomsaw(World world, double x, double y, double z) {
        if (DebugSettings.roll(RANDOM, 0.15f)) {
            spawnBloodRain(world, x, y, z);
        }
    }

    public static void handleArmageddon(World world, double x, double y, double z) {
        if (DebugSettings.roll(RANDOM, 0.08f)) {
            LightweightExplosion.enqueue(world, x, y, z, 20.0f);
            spawnNuclearMushroom(world, x, y, z);
        }
    }

    public static void handleAnnihilator(World world, double x, double y, double z) {
        if (DebugSettings.roll(RANDOM, 0.15f)) {
            spawnLightningRain(world, x, y, z);
        }
    }

    public static void handleApocalypse(World world, double x, double y, double z) {
        if (DebugSettings.roll(RANDOM, 0.20f)) {
            spawnPlasmaRain(world, x, y, z);
        }
    }

    public static void handleHellDuet(World world, double x, double y, double z) {
        LightweightExplosion.enqueue(world, x, y, z, 8.0f);
        spawnHellFire(world, x, y, z);
    }

    public static void handleLegendary(World world, double x, double y, double z, Entity shooter) {
        if (DebugSettings.roll(RANDOM, 0.20f)) {
            spawnLegendaryEffect(world, x, y, z, shooter);
        }
    }

    public static void handleChainLightning(World world, double x, double y, double z, int maxTargets, Entity shooter) {
        handleChainLightning(world, x, y, z, maxTargets, shooter, null);
    }

    public static void handleChainLightning(World world, double x, double y, double z,
                                            int maxTargets, Entity shooter, Entity directTarget) {
        List<EntityLivingBase> targets = world.getEntitiesWithinAABB(EntityLivingBase.class,
                new AxisAlignedBB(x - 15, y - 5, z - 15, x + 15, y + 5, z + 15));
        Set<Entity> visited = new HashSet<>();
        if (directTarget != null) visited.add(directTarget);
        Entity current = directTarget;
        int count = 0;
        while (count < maxTargets) {
            EntityLivingBase next = null;
            double nearest = Double.MAX_VALUE;
            for (EntityLivingBase candidate : targets) {
                if (candidate == shooter || visited.contains(candidate)
                        || !candidate.isEntityAlive()) continue;
                double distance = current == null
                        ? candidate.getDistanceSq(x, y, z) : candidate.getDistanceSq(current);
                if (distance < nearest) {
                    nearest = distance;
                    next = candidate;
                }
            }
            if (next == null) break;
            if (current != null) spawnElectricArc(world, current, next);
            next.attackEntityFrom(DamageSource.MAGIC, 8.0f);
            visited.add(next);
            current = next;
            count++;
        }
    }

    /** Visual electric arc without creating a weather lightning entity. */
    public static void spawnElectricArc(World world, Entity from, Entity to) {
        if (from == null || to == null) return;
        UpgradeVisualEffects.beam(world,
                new Vec3d(from.posX, from.posY + from.height * 0.5, from.posZ),
                new Vec3d(to.posX, to.posY + to.height * 0.5, to.posZ),
                UpgradeVisualEffects.BeamStyle.ARC);
    }

    public static void handleHunter(World world, double x, double y, double z, float multiplier, Entity shooter) {
        if (multiplier <= 1.0f) multiplier = 1.5f;
        for (Entity entity : world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(
            x - 2, y - 2, z - 2,
            x + 2, y + 2, z + 2
        ))) {
            if (entity instanceof EntityLivingBase && entity != shooter) {
                if (!(entity instanceof EntityPlayer)) {
                    entity.attackEntityFrom(DamageSource.MAGIC, 3.0f * multiplier);
                }
            }
        }
    }

    public static void handleFreeze(World world, double x, double y, double z, int duration, Entity shooter) {
        for (Entity entity : world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(
            x - 3, y - 3, z - 3,
            x + 3, y + 3, z + 3
        ))) {
            if (entity instanceof EntityLivingBase && entity != shooter) {
                ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, duration, 4));
                ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, duration, 2));
            }
        }
        spawnFreezeParticles(world, x, y, z);
    }

    public static void handleLightning(World world, double x, double y, double z, Entity shooter) {
        BlockPos pos = new BlockPos(x, y, z);
        world.addWeatherEffect(new EntityLightningBolt(
            world, pos.getX(), pos.getY(), pos.getZ(), false
        ));
        for (Entity entity : world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(
            x - 5, y - 5, z - 5,
            x + 5, y + 5, z + 5
        ))) {
            if (entity instanceof EntityLivingBase && entity != shooter) {
                entity.attackEntityFrom(DamageSource.LIGHTNING_BOLT, 5.0f);
            }
        }
    }

    public static void handleExplosive(World world, double x, double y, double z, float power) {
        if (power <= 0) power = 2.0f;
        LightweightExplosion.enqueue(world, x, y, z, power);
    }

    public static void handleIncendiary(World world, double x, double y, double z, int duration) {
        if (duration <= 0) duration = 60;
        for (Entity entity : world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(
            x - 3, y - 3, z - 3,
            x + 3, y + 3, z + 3
        ))) {
            if (entity instanceof EntityLivingBase) {
                entity.setFire(duration / 20);
            }
        }
        spawnFireParticles(world, x, y, z);
    }

    public static void spawnHeartParticles(World world, double x, double y, double z) {
        UpgradeVisualEffects.specialBurst(world, new Vec3d(x, y + 0.5, z), 2.0f,
                UpgradeVisualEffects.SpecialStyle.HEAL);
    }

    private static void spawnDeathScytheParticles(World world, double x, double y, double z) {
        UpgradeVisualEffects.specialBurst(world, new Vec3d(x, y, z), 10.0f,
                UpgradeVisualEffects.SpecialStyle.DEATH);
    }

    private static void spawnBloodRain(World world, double x, double y, double z) {
        // Do not use vanilla REDSTONE here: its colour is client-state dependent.
        UpgradeVisualEffects.bloodRainFrame(world, new Vec3d(x, y, z), 5.0f);
        for (Entity entity : world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(
            x - 5, y - 5, z - 5,
            x + 5, y + 5, z + 5
        ))) {
            if (entity instanceof EntityLivingBase) {
                entity.attackEntityFrom(DamageSource.MAGIC, 5.0f);
            }
        }
    }

    private static void spawnNuclearMushroom(World world, double x, double y, double z) {
        UpgradeVisualEffects.specialBurst(world, new Vec3d(x, y, z), 15.0f,
                UpgradeVisualEffects.SpecialStyle.NUCLEAR_MUSHROOM);
    }

    private static void spawnLightningRain(World world, double x, double y, double z) {
        for (int i = 0; i < 10; i++) {
            double lx = x + (RANDOM.nextDouble() - 0.5) * 15;
            double lz = z + (RANDOM.nextDouble() - 0.5) * 15;
            BlockPos pos = new BlockPos(lx, y + 10, lz);
            world.addWeatherEffect(new EntityLightningBolt(
                world, pos.getX(), pos.getY(), pos.getZ(), false
            ));
        }
    }

    private static void spawnPlasmaRain(World world, double x, double y, double z) {
        UpgradeVisualEffects.specialBurst(world, new Vec3d(x, y, z), 10.0f,
                UpgradeVisualEffects.SpecialStyle.PLASMA_STORM);
        LightweightExplosion.enqueue(world, x, y, z, 15.0f);
    }

    private static void spawnHellFire(World world, double x, double y, double z) {
        UpgradeVisualEffects.specialBurst(world, new Vec3d(x, y, z), 5.0f,
                UpgradeVisualEffects.SpecialStyle.FIRE);
        for (Entity entity : world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(
            x - 5, y - 5, z - 5,
            x + 5, y + 5, z + 5
        ))) {
            if (entity instanceof EntityLivingBase) {
                entity.setFire(5);
            }
        }
    }

    private static void spawnLegendaryEffect(World world, double x, double y, double z, Entity shooter) {
        UpgradeVisualEffects.specialBurst(world, new Vec3d(x, y, z), 5.0f,
                UpgradeVisualEffects.SpecialStyle.LEGENDARY);
        for (Entity entity : world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(
            x - 5, y - 3, z - 5,
            x + 5, y + 3, z + 5
        ))) {
            if (entity instanceof EntityLivingBase && entity != shooter) {
                entity.attackEntityFrom(DamageSource.MAGIC, 10.0f);
            }
        }
    }

    private static void spawnFreezeParticles(World world, double x, double y, double z) {
        UpgradeVisualEffects.specialBurst(world, new Vec3d(x, y, z), 3.0f,
                UpgradeVisualEffects.SpecialStyle.FROST);
    }

    private static void spawnFireParticles(World world, double x, double y, double z) {
        UpgradeVisualEffects.specialBurst(world, new Vec3d(x, y, z), 3.0f,
                UpgradeVisualEffects.SpecialStyle.FIRE);
    }
}
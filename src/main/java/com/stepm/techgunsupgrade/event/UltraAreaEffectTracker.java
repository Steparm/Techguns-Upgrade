package com.stepm.techgunsupgrade.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import com.stepm.techgunsupgrade.TechgunsUpgradeMod;
import com.stepm.techgunsupgrade.config.TguConfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/** Server-owned timed damage zones used by Ultra-Mythic effects. */
@Mod.EventBusSubscriber(modid = TechgunsUpgradeMod.MODID)
public final class UltraAreaEffectTracker {
    private static final Map<World, List<Zone>> ACTIVE = new WeakHashMap<>();

    private UltraAreaEffectTracker() {
    }

    public static void create(World world, Vec3d center, EntityPlayer owner, float radius,
                              int durationTicks, float damage, float knockback,
                              Potion potion, int potionDuration, EnumParticleTypes particle) {
        create(world, center, owner, radius, durationTicks, damage, knockback, potion,
                potionDuration, particle, styleForParticle(particle));
    }

    public static void create(World world, Vec3d center, EntityPlayer owner, float radius,
                              int durationTicks, float damage, float knockback,
                              Potion potion, int potionDuration, EnumParticleTypes particle,
                              UpgradeVisualEffects.ZoneStyle style) {
        if (world == null || world.isRemote || center == null || owner == null
                || radius <= 0.0f || durationTicks <= 0) return;
        Zone zone = new Zone(center, owner, radius, world.getTotalWorldTime() + durationTicks,
                Math.max(0.0f, damage), Math.max(0.0f, knockback), potion,
                Math.max(0, potionDuration), particle == null
                ? EnumParticleTypes.SPELL_MOB : particle,
                style == null ? UpgradeVisualEffects.ZoneStyle.GENERIC : style);
        List<Zone> zones = ACTIVE.computeIfAbsent(world, ignored -> new ArrayList<>());
        int limit = Math.max(4, Math.min(64, TguConfig.maxActiveUltraZones));
        if (zones.size() >= limit) zones.remove(0);
        zones.add(zone);
        applyEffects(world, zone);
        spawnVisuals(world, zone);
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world == null || event.world.isRemote) return;
        List<Zone> zones = ACTIVE.get(event.world);
        if (zones == null || zones.isEmpty()) return;
        long now = event.world.getTotalWorldTime();
        Iterator<Zone> iterator = zones.iterator();
        while (iterator.hasNext()) {
            Zone zone = iterator.next();
            if (now > zone.expiresAt || zone.owner == null || zone.owner.isDead) {
                iterator.remove();
                continue;
            }
            if (now % 20 == 0) applyEffects(event.world, zone);
            if (now % 4 == 0) spawnVisuals(event.world, zone);
        }
        if (zones.isEmpty()) ACTIVE.remove(event.world);
    }

    private static void applyEffects(World world, Zone zone) {
        AxisAlignedBB bounds = new AxisAlignedBB(zone.center.x - zone.radius,
                zone.center.y - zone.radius, zone.center.z - zone.radius,
                zone.center.x + zone.radius, zone.center.y + zone.radius,
                zone.center.z + zone.radius);
        for (EntityLivingBase target : world.getEntitiesWithinAABB(EntityLivingBase.class, bounds)) {
            if (!UpgradeEventHandler.canAffect(zone.owner, target) || !target.isEntityAlive()
                    || target.getDistance(zone.center.x, zone.center.y, zone.center.z) > zone.radius) {
                continue;
            }
            if (zone.damage > 0.0f) {
                target.attackEntityFrom(new EntityDamageSource("tgu_ultra_zone", zone.owner)
                        .setMagicDamage(), zone.damage);
            }
            if (zone.potion != null && zone.potionDuration > 0) {
                target.addPotionEffect(new PotionEffect(zone.potion, zone.potionDuration, 1));
            }
            if (zone.knockback > 0.0f) {
                double dx = target.posX - zone.center.x;
                double dz = target.posZ - zone.center.z;
                double length = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
                target.addVelocity(dx / length * zone.knockback,
                        Math.min(0.8, zone.knockback * 0.25), dz / length * zone.knockback);
                target.velocityChanged = true;
            }
        }
    }

    private static void spawnVisuals(World world, Zone zone) {
        if (!(world instanceof WorldServer)) return;
        UpgradeVisualEffects.spawnZoneFrame(world, zone.center, zone.radius, zone.style);
    }

    private static UpgradeVisualEffects.ZoneStyle styleForParticle(EnumParticleTypes particle) {
        if (particle == EnumParticleTypes.REDSTONE) return UpgradeVisualEffects.ZoneStyle.BLOOD_RAIN;
        if (particle == EnumParticleTypes.SPELL_MOB) return UpgradeVisualEffects.ZoneStyle.ACID_CLOUD;
        if (particle == EnumParticleTypes.PORTAL) return UpgradeVisualEffects.ZoneStyle.INSTABILITY;
        if (particle == EnumParticleTypes.EXPLOSION_NORMAL) {
            return UpgradeVisualEffects.ZoneStyle.EARTHQUAKE;
        }
        return UpgradeVisualEffects.ZoneStyle.GENERIC;
    }

    private static final class Zone {
        private final Vec3d center;
        private final EntityPlayer owner;
        private final float radius;
        private final long expiresAt;
        private final float damage;
        private final float knockback;
        private final Potion potion;
        private final int potionDuration;
        private final EnumParticleTypes particle;
        private final UpgradeVisualEffects.ZoneStyle style;

        private Zone(Vec3d center, EntityPlayer owner, float radius, long expiresAt,
                     float damage, float knockback, Potion potion, int potionDuration,
                     EnumParticleTypes particle, UpgradeVisualEffects.ZoneStyle style) {
            this.center = center;
            this.owner = owner;
            this.radius = radius;
            this.expiresAt = expiresAt;
            this.damage = damage;
            this.knockback = knockback;
            this.potion = potion;
            this.potionDuration = potionDuration;
            this.particle = particle;
            this.style = style;
        }
    }
}

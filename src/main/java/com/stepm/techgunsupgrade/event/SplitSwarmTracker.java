package com.stepm.techgunsupgrade.event;

import com.stepm.techgunsupgrade.TechgunsUpgradeMod;
import com.stepm.techgunsupgrade.config.TguConfig;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

/** Five-second computational shard swarm: rays and one particle batch, no child entities. */
@Mod.EventBusSubscriber(modid = TechgunsUpgradeMod.MODID)
public final class SplitSwarmTracker {
    private static final WeakHashMap<World, List<Swarm>> ACTIVE = new WeakHashMap<>();

    private SplitSwarmTracker() {
    }

    public static void create(World world, Vec3d origin, Vec3d forward,
                              EntityPlayer owner, float damage) {
        if (world == null || world.isRemote || owner == null) return;
        List<Swarm> swarms = ACTIVE.computeIfAbsent(world, ignored -> new ArrayList<>());
        int limit = Math.max(1, Math.min(16, TguConfig.maxActiveSplitSwarms));
        if (swarms.size() >= limit) swarms.remove(0);
        swarms.add(new Swarm(
                origin, forward.lengthSquared() < 1.0e-6 ? owner.getLookVec() : forward.normalize(),
                owner, Math.max(1.0f, damage), world.getTotalWorldTime(),
                world.getTotalWorldTime() + 100));
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world == null || event.world.isRemote) return;
        List<Swarm> swarms = ACTIVE.get(event.world);
        if (swarms == null) return;
        long now = event.world.getTotalWorldTime();
        Iterator<Swarm> iterator = swarms.iterator();
        while (iterator.hasNext()) {
            Swarm swarm = iterator.next();
            if (now > swarm.expiresAt || !swarm.owner.isEntityAlive()) {
                iterator.remove();
                continue;
            }
            if ((now - swarm.startedAt) % 10 == 0) tickSwarm(event.world, swarm, now);
        }
        if (swarms.isEmpty()) ACTIVE.remove(event.world);
    }

    private static void tickSwarm(World world, Swarm swarm, long now) {
        int generation = (int) ((now - swarm.startedAt) / 10L);
        int rays = Math.min(64, 2 << Math.min(5, generation));
        double baseAngle = Math.atan2(swarm.forward.z, swarm.forward.x);
        Set<Integer> damaged = new HashSet<>();
        for (int i = 0; i < rays; i++) {
            double angle = baseAngle + Math.PI * 2.0 * i / rays + generation * 0.17;
            Vec3d direction = new Vec3d(Math.cos(angle),
                    ((i + generation) % 3 - 1) * 0.08, Math.sin(angle)).normalize();
            Vec3d end = swarm.origin.add(direction.scale(8.0));
            AxisAlignedBB rayArea = new AxisAlignedBB(swarm.origin.x, swarm.origin.y,
                    swarm.origin.z, end.x, end.y, end.z).grow(0.6);
            EntityLivingBase closest = null;
            double closestDistance = Double.MAX_VALUE;
            for (EntityLivingBase candidate : world.getEntitiesWithinAABB(
                    EntityLivingBase.class, rayArea)) {
                if (!UpgradeEventHandler.canAffect(swarm.owner, candidate)
                        || !candidate.isEntityAlive()) continue;
                double distance = candidate.getDistanceSq(swarm.origin.x,
                        swarm.origin.y, swarm.origin.z);
                if (distance < closestDistance) {
                    closest = candidate;
                    closestDistance = distance;
                }
            }
            if (closest != null && damaged.add(closest.getEntityId())) {
                closest.attackEntityFrom(new EntityDamageSource("tgu_split_swarm", swarm.owner)
                        .setMagicDamage(), swarm.damage * 0.20f);
            }
            // Show a rotating subset as full rays.  Drawing every one of 64 rays
            // would be needlessly expensive and would turn the swarm into a solid fog.
            if (i % Math.max(1, rays / 20) == 0) {
                UpgradeVisualEffects.beam(world, swarm.origin, end,
                        UpgradeVisualEffects.BeamStyle.SHRAPNEL);
            }
        }
    }

    private static final class Swarm {
        private final Vec3d origin;
        private final Vec3d forward;
        private final EntityPlayer owner;
        private final float damage;
        private final long startedAt;
        private final long expiresAt;

        private Swarm(Vec3d origin, Vec3d forward, EntityPlayer owner, float damage,
                      long startedAt, long expiresAt) {
            this.origin = origin;
            this.forward = forward;
            this.owner = owner;
            this.damage = damage;
            this.startedAt = startedAt;
            this.expiresAt = expiresAt;
        }
    }
}

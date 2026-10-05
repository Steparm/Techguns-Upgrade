package com.stepm.techgunsupgrade.event;

import com.stepm.techgunsupgrade.TechgunsUpgradeMod;
import com.stepm.techgunsupgrade.config.TguConfig;
import com.stepm.techgunsupgrade.debug.DebugSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/** Server-owned short charge phase for Nuclear Death Ray detonations. */
@Mod.EventBusSubscriber(modid = TechgunsUpgradeMod.MODID)
public final class NuclearDetonationTracker {
    private static final int MAX_PENDING_PER_WORLD = 16;
    private static final int VISUAL_INTERVAL_TICKS = 2;
    private static final Map<World, List<PendingDetonation>> PENDING = new WeakHashMap<>();

    private NuclearDetonationTracker() {
    }

    public static void schedule(World world, Vec3d center, EntityPlayer owner,
                                float radius, float damage) {
        if (!(world instanceof WorldServer) || world.isRemote || center == null
                || owner == null || !finite(center)) return;
        long now = world.getTotalWorldTime();
        int delay = configuredDelayTicks();
        List<PendingDetonation> detonations = PENDING.computeIfAbsent(
                world, ignored -> new ArrayList<>());

        PendingDetonation duplicate = findDuplicate(detonations, center, owner, now);
        if (duplicate != null) {
            duplicate.radius = Math.max(duplicate.radius, safeRadius(radius));
            duplicate.damage = Math.max(duplicate.damage, Math.max(20.0f, damage));
            return;
        }
        if (detonations.size() >= MAX_PENDING_PER_WORLD) {
            TechgunsUpgradeMod.LOGGER.warn(
                    "Nuclear charge queue is full in dimension {}; dropping the newest detonation to protect TPS",
                    world.provider.getDimension());
            return;
        }

        PendingDetonation pending = new PendingDetonation(center, owner,
                safeRadius(radius), Math.max(20.0f, damage), now, now + delay);
        detonations.add(pending);
        NuclearSounds.playCharge(world, center);
        UpgradeVisualEffects.nuclearCharge(world, center, pending.radius, 0.0f);
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world == null
                || event.world.isRemote) return;
        List<PendingDetonation> detonations = PENDING.get(event.world);
        if (detonations == null || detonations.isEmpty()) return;
        long now = event.world.getTotalWorldTime();
        Iterator<PendingDetonation> iterator = detonations.iterator();
        while (iterator.hasNext()) {
            PendingDetonation pending = iterator.next();
            if (pending.owner == null) {
                iterator.remove();
                continue;
            }
            if (now >= pending.detonateAt) {
                UpgradeVisualEffects.nuclearCharge(event.world, pending.center,
                        pending.radius, 1.0f);
                iterator.remove();
                UpgradeEventHandler.detonateNuclearImpact(event.world, pending.center,
                        pending.owner, pending.radius, pending.damage);
                continue;
            }
            if (now - pending.lastVisualAt >= VISUAL_INTERVAL_TICKS) {
                pending.lastVisualAt = now;
                float progress = (now - pending.startedAt)
                        / (float) Math.max(1L, pending.detonateAt - pending.startedAt);
                UpgradeVisualEffects.nuclearCharge(event.world, pending.center,
                        pending.radius, MathHelper.clamp(progress, 0.0f, 1.0f));
            }
        }
        if (detonations.isEmpty()) PENDING.remove(event.world);
    }

    static int configuredDelayTicks() {
        return MathHelper.clamp(TguConfig.nuclearDetonationDelayTicks, 6, 30);
    }

    private static PendingDetonation findDuplicate(List<PendingDetonation> detonations,
                                                    Vec3d center, EntityPlayer owner,
                                                    long startedAt) {
        for (PendingDetonation pending : detonations) {
            if (pending.owner == owner && pending.startedAt == startedAt
                    && pending.center.squareDistanceTo(center) <= 1.0) {
                return pending;
            }
        }
        return null;
    }

    private static float safeRadius(float radius) {
        return MathHelper.clamp(radius, 4.0f, 30.0f);
    }

    private static boolean finite(Vec3d position) {
        return !Double.isNaN(position.x) && !Double.isInfinite(position.x)
                && !Double.isNaN(position.y) && !Double.isInfinite(position.y)
                && !Double.isNaN(position.z) && !Double.isInfinite(position.z);
    }

    private static final class PendingDetonation {
        private final Vec3d center;
        private final EntityPlayer owner;
        private final long startedAt;
        private final long detonateAt;
        private long lastVisualAt;
        private float radius;
        private float damage;

        private PendingDetonation(Vec3d center, EntityPlayer owner, float radius,
                                  float damage, long startedAt, long detonateAt) {
            this.center = center;
            this.owner = owner;
            this.radius = radius;
            this.damage = damage;
            this.startedAt = startedAt;
            this.detonateAt = detonateAt;
            this.lastVisualAt = startedAt;
        }
    }
}

package com.stepm.techgunsupgrade.event;

import com.stepm.techgunsupgrade.network.NetworkHandler;
import com.stepm.techgunsupgrade.network.PacketVisualEffect;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.NetworkRegistry;

/**
 * Server entry point for every upgrade visual. Gameplay stays authoritative on
 * the server, while one compact packet lets each nearby client draw the rich
 * particle geometry locally.
 */
public final class UpgradeVisualEffects {
    public enum ZoneStyle {
        GENERIC,
        BLOOD_RAIN,
        ACID_CLOUD,
        INSTABILITY,
        EARTHQUAKE
    }

    public enum AreaStyle {
        IMPACT,
        SONIC,
        PULSE,
        EMP,
        LIGHT,
        SEISMIC,
        VORTEX,
        POISON
    }

    public enum BeamStyle {
        ARC,
        LASER,
        PLASMA,
        POISON,
        SHRAPNEL
    }

    public enum SpecialStyle {
        HEAL,
        DEATH,
        FROST,
        FIRE,
        LEGENDARY,
        PLASMA_STORM,
        NUCLEAR_MUSHROOM,
        EXPLOSION
    }

    private UpgradeVisualEffects() {
    }

    public static void spawnZoneFrame(World world, Vec3d center, float radius,
                                      ZoneStyle style) {
        send(world, PacketVisualEffect.ZONE, ordinal(style, ZoneStyle.GENERIC),
                center, center, MathHelper.clamp(radius, 1.0f, 30.0f), 0.0f,
                effectRange(radius, 72.0));
    }

    public static void bloodRainFrame(World world, Vec3d center, float radius) {
        spawnZoneFrame(world, center, radius, ZoneStyle.BLOOD_RAIN);
    }

    public static void areaBurst(World world, Vec3d center, float radius, AreaStyle style) {
        send(world, PacketVisualEffect.AREA, ordinal(style, AreaStyle.IMPACT),
                center, center, MathHelper.clamp(radius, 1.0f, 30.0f), 0.0f,
                effectRange(radius, 80.0));
    }

    public static void beam(World world, Vec3d start, Vec3d end, BeamStyle style) {
        if (start == null || end == null) return;
        Vec3d midpoint = start.add(end).scale(0.5);
        float width = style == BeamStyle.ARC ? 1.35f : 1.0f;
        sendAt(world, PacketVisualEffect.BEAM, ordinal(style, BeamStyle.SHRAPNEL),
                start, end, midpoint, width, 0.0f,
                MathHelper.clamp(start.distanceTo(end) + 72.0, 80.0, 160.0));
    }

    public static void skyStrike(World world, Vec3d landing, BeamStyle style) {
        send(world, PacketVisualEffect.SKY_STRIKE, ordinal(style, BeamStyle.SHRAPNEL),
                landing, landing, 3.0f, 0.0f, 112.0);
    }

    public static void fireRainStrike(World world, Vec3d landing) {
        send(world, PacketVisualEffect.FIRE_RAIN, 0, landing, landing,
                3.0f, 0.0f, 112.0);
    }

    public static void artilleryStrike(World world, Vec3d landing) {
        send(world, PacketVisualEffect.ARTILLERY, 0, landing, landing,
                4.5f, 0.0f, 128.0);
    }

    public static void smokeCloud(World world, Vec3d center, float radius) {
        send(world, PacketVisualEffect.SMOKE, 0, center, center,
                MathHelper.clamp(radius, 1.0f, 30.0f), 0.0f,
                effectRange(radius, 80.0));
    }

    public static void fireZone(World world, Vec3d center, float radius) {
        send(world, PacketVisualEffect.FIRE_ZONE, 0, center, center,
                MathHelper.clamp(radius, 0.8f, 30.0f), 0.0f,
                effectRange(radius, 80.0));
    }

    public static void specialBurst(World world, Vec3d center, float radius,
                                    SpecialStyle style) {
        send(world, PacketVisualEffect.SPECIAL, ordinal(style, SpecialStyle.LEGENDARY),
                center, center, MathHelper.clamp(radius, 0.8f, 30.0f), 0.0f,
                style == SpecialStyle.NUCLEAR_MUSHROOM ? 224.0
                        : effectRange(radius, 88.0));
    }

    /** Draws one frame of the inward nuclear charge animation. */
    public static void nuclearCharge(World world, Vec3d center, float radius,
                                     float progress) {
        send(world, PacketVisualEffect.NUCLEAR_CHARGE, 0, center, center,
                MathHelper.clamp(radius, 4.0f, 30.0f),
                MathHelper.clamp(progress, 0.0f, 1.0f), 224.0);
    }

    /**
     * Emits one stable explosion sound and starts the local multi-stage flash,
     * shockwave and mushroom animation. It runs in the same server tick as the
     * actual damage and crater.
     */
    public static void nuclearBlast(World world, Vec3d center, float radius) {
        WorldServer server = server(world);
        if (server == null || center == null) return;
        float safe = MathHelper.clamp(radius, 4.0f, 30.0f);
        world.playSound(null, new BlockPos(center), SoundEvents.ENTITY_GENERIC_EXPLODE,
                SoundCategory.BLOCKS, 4.0f, 0.55f);
        send(server, PacketVisualEffect.NUCLEAR_BLAST, 0, center, center,
                safe, 0.0f, 256.0);
    }

    private static void send(World world, int type, int style, Vec3d start, Vec3d end,
                             float radius, float progress, double range) {
        sendAt(world, type, style, start, end, start, radius, progress, range);
    }

    private static void sendAt(World world, int type, int style, Vec3d start, Vec3d end,
                               Vec3d target, float radius, float progress, double range) {
        WorldServer server = server(world);
        if (server == null || start == null || end == null || target == null) return;
        if (!finite(start) || !finite(end) || !finite(target)) return;
        int dimension = server.provider.getDimension();
        PacketVisualEffect packet = new PacketVisualEffect(type, style, dimension,
                start.x, start.y, start.z, end.x, end.y, end.z,
                radius, progress, server.rand.nextInt());
        NetworkHandler.INSTANCE.sendToAllAround(packet,
                new NetworkRegistry.TargetPoint(dimension, target.x, target.y, target.z,
                        MathHelper.clamp(range, 32.0, 256.0)));
    }

    private static double effectRange(float radius, double minimum) {
        return MathHelper.clamp(Math.max(minimum, radius + 56.0), 48.0, 160.0);
    }

    private static int ordinal(Enum<?> value, Enum<?> fallback) {
        return value == null ? fallback.ordinal() : value.ordinal();
    }

    private static boolean finite(Vec3d position) {
        return !Double.isNaN(position.x) && !Double.isInfinite(position.x)
                && !Double.isNaN(position.y) && !Double.isInfinite(position.y)
                && !Double.isNaN(position.z) && !Double.isInfinite(position.z);
    }

    private static WorldServer server(World world) {
        return world instanceof WorldServer ? (WorldServer) world : null;
    }
}

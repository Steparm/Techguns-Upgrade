package com.stepm.techgunsupgrade.client;

import com.stepm.techgunsupgrade.TechgunsUpgradeMod;
import com.stepm.techgunsupgrade.config.TguConfig;
import com.stepm.techgunsupgrade.network.PacketVisualEffect;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Renders the elaborate part of upgrade effects locally. The server sends one
 * compact description instead of hundreds of individual particle packets.
 */
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = TechgunsUpgradeMod.MODID, value = Side.CLIENT)
public final class ClientVisualEffectRenderer {
    private static long budgetTick = Long.MIN_VALUE;
    private static WorldClient budgetWorld;
    private static int particlesThisTick;
    private static int particleLimitThisTick = 600;
    private static final List<NuclearAnimation> NUCLEAR_ANIMATIONS = new ArrayList<>();

    private ClientVisualEffectRenderer() {
    }

    public static void render(PacketVisualEffect packet) {
        Minecraft minecraft = Minecraft.getMinecraft();
        WorldClient world = minecraft.world;
        if (world == null || minecraft.player == null || packet == null
                || packet.getDimension() != minecraft.player.dimension
                || packet.getType() < PacketVisualEffect.ZONE
                || packet.getType() > PacketVisualEffect.SPECIAL
                || !finite(packet.getX()) || !finite(packet.getY()) || !finite(packet.getZ())
                || !finite(packet.getEndX()) || !finite(packet.getEndY())
                || !finite(packet.getEndZ()) || !finite(packet.getRadius())
                || !finite(packet.getProgress())) return;
        beginTick(world);

        Random random = new Random(packet.getSeed());
        Vec3d start = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
        Vec3d end = new Vec3d(packet.getEndX(), packet.getEndY(), packet.getEndZ());
        Vec3d focus = packet.getType() == PacketVisualEffect.BEAM
                ? start.add(end).scale(0.5) : start;
        double viewRange = packet.getType() >= PacketVisualEffect.NUCLEAR_CHARGE
                ? 288.0 : 176.0;
        if (minecraft.player.getDistanceSq(focus.x, focus.y, focus.z)
                > viewRange * viewRange) return;
        float radius = MathHelper.clamp(packet.getRadius(), 0.5f, 40.0f);
        switch (packet.getType()) {
            case PacketVisualEffect.ZONE:
                zone(world, random, start, radius, packet.getStyle());
                break;
            case PacketVisualEffect.AREA:
                area(world, random, start, radius, packet.getStyle());
                break;
            case PacketVisualEffect.BEAM:
                beam(world, random, start, end, packet.getStyle(), 3);
                impactFlare(world, random, end, radius, packet.getStyle());
                break;
            case PacketVisualEffect.SKY_STRIKE:
                skyStrike(world, random, start, packet.getStyle());
                break;
            case PacketVisualEffect.FIRE_RAIN:
                fireRain(world, random, start);
                break;
            case PacketVisualEffect.ARTILLERY:
                artillery(world, random, start);
                break;
            case PacketVisualEffect.SMOKE:
                smokeCloud(world, random, start, radius);
                break;
            case PacketVisualEffect.FIRE_ZONE:
                fireZone(world, random, start, radius);
                break;
            case PacketVisualEffect.NUCLEAR_CHARGE:
                nuclearCharge(world, random, start, radius,
                        MathHelper.clamp(packet.getProgress(), 0.0f, 1.0f));
                break;
            case PacketVisualEffect.NUCLEAR_BLAST:
                startNuclearBlast(world, start, radius, packet.getSeed());
                break;
            case PacketVisualEffect.SPECIAL:
                special(world, random, start, radius, packet.getStyle(), packet.getSeed());
                break;
            default:
                break;
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        WorldClient world = Minecraft.getMinecraft().world;
        if (world == null) {
            NUCLEAR_ANIMATIONS.clear();
            return;
        }
        beginTick(world);
        Iterator<NuclearAnimation> iterator = NUCLEAR_ANIMATIONS.iterator();
        while (iterator.hasNext()) {
            NuclearAnimation animation = iterator.next();
            if (animation.world != world || animation.age > 28) {
                iterator.remove();
                continue;
            }
            Random random = new Random(animation.seed + animation.age * 0x6D2B79F5L);
            nuclearBlastFrame(world, random, animation.center,
                    animation.radius, animation.age);
            animation.age++;
        }
    }

    private static void startNuclearBlast(WorldClient world, Vec3d center,
                                          float radius, int seed) {
        if (NUCLEAR_ANIMATIONS.size() >= 8) NUCLEAR_ANIMATIONS.remove(0);
        NUCLEAR_ANIMATIONS.add(new NuclearAnimation(world, center,
                MathHelper.clamp(radius, 4.0f, 30.0f), seed));
    }

    private static void zone(WorldClient world, Random random, Vec3d center,
                             float radius, int style) {
        double phase = world.getTotalWorldTime() * 0.17;
        if (style == 1) {
            int red = Block.getStateId(Blocks.REDSTONE_BLOCK.getDefaultState());
            for (int i = 0; i < 110; i++) {
                Vec3d point = disc(random, center, radius * 0.92, 6.0 + random.nextDouble() * 5.0);
                particle(world, EnumParticleTypes.BLOCK_CRACK, point.x, point.y, point.z,
                        randomVelocity(random, 0.035), -0.55 - random.nextDouble() * 0.55,
                        randomVelocity(random, 0.035), red);
            }
            for (int i = 0; i < 75; i++) {
                Vec3d point = disc(random, center, radius * 0.92, 0.08);
                particle(world, i % 3 == 0 ? EnumParticleTypes.FALLING_DUST
                                : EnumParticleTypes.BLOCK_DUST,
                        point.x, point.y, point.z, randomVelocity(random, 0.12),
                        0.04 + random.nextDouble() * 0.18, randomVelocity(random, 0.12), red);
            }
            ring(world, EnumParticleTypes.BLOCK_DUST, center.add(0.0, 0.12, 0.0),
                    radius, 64, phase, 0.0, red);
            ring(world, EnumParticleTypes.BLOCK_CRACK, center.add(0.0, 0.20, 0.0),
                    radius * 0.58, 42, -phase * 1.45, 0.0, red);
            return;
        }
        if (style == 2) {
            cloud(world, random, center.add(0.0, 0.8, 0.0), radius, 115,
                    EnumParticleTypes.SLIME, EnumParticleTypes.VILLAGER_HAPPY, 0.12);
            helix(world, EnumParticleTypes.SLIME, center, radius * 0.76,
                    2.8, 58, phase, 2, 0.015);
            ring(world, EnumParticleTypes.VILLAGER_HAPPY, center.add(0.0, 0.18, 0.0),
                    radius, 66, phase * 0.5, 0.018);
            return;
        }
        if (style == 3) {
            for (int i = 0; i < 130; i++) {
                Vec3d outside = spherePoint(random, center.add(0.0, 1.2, 0.0),
                        radius * (0.55 + random.nextDouble() * 0.45));
                Vec3d pull = center.add(0.0, 1.2, 0.0).subtract(outside).normalize()
                        .scale(0.08 + random.nextDouble() * 0.13);
                particle(world, EnumParticleTypes.PORTAL, outside.x, outside.y, outside.z,
                        pull.x, pull.y, pull.z);
            }
            helix(world, EnumParticleTypes.END_ROD, center.add(0.0, -0.4, 0.0),
                    radius * 0.78, 4.2, 72, phase, 3, 0.01);
            ring(world, EnumParticleTypes.SPELL_WITCH, center.add(0.0, 0.22, 0.0),
                    radius, 72, -phase, 0.025);
            return;
        }
        if (style == 4) {
            int ground = groundState(world, center);
            for (int i = 0; i < 120; i++) {
                Vec3d point = disc(random, center, radius, 0.12);
                particle(world, i % 5 == 0 ? EnumParticleTypes.EXPLOSION_NORMAL
                                : EnumParticleTypes.BLOCK_DUST,
                        point.x, point.y, point.z, randomVelocity(random, 0.16),
                        0.08 + random.nextDouble() * 0.30, randomVelocity(random, 0.16), ground);
            }
            ring(world, EnumParticleTypes.BLOCK_DUST, center.add(0.0, 0.14, 0.0),
                    radius, 76, phase, 0.06, ground);
            ring(world, EnumParticleTypes.BLOCK_CRACK, center.add(0.0, 0.18, 0.0),
                    radius * 0.62, 52, -phase * 1.3, 0.08, ground);
            return;
        }
        cloud(world, random, center.add(0.0, 0.8, 0.0), radius, 100,
                EnumParticleTypes.SPELL_WITCH, EnumParticleTypes.CRIT_MAGIC, 0.10);
        ring(world, EnumParticleTypes.CRIT_MAGIC, center.add(0.0, 0.14, 0.0),
                radius, 68, phase, 0.025);
        ring(world, EnumParticleTypes.END_ROD, center.add(0.0, 0.22, 0.0),
                radius * 0.55, 42, -phase * 1.4, 0.012);
    }

    private static void area(WorldClient world, Random random, Vec3d center,
                             float radius, int style) {
        double phase = random.nextDouble() * Math.PI * 2.0;
        EnumParticleTypes first = EnumParticleTypes.EXPLOSION_NORMAL;
        EnumParticleTypes second = EnumParticleTypes.CLOUD;
        switch (style) {
            case 1:
                first = EnumParticleTypes.NOTE;
                second = EnumParticleTypes.CLOUD;
                break;
            case 2:
                first = EnumParticleTypes.END_ROD;
                second = EnumParticleTypes.SPELL_WITCH;
                break;
            case 3:
                first = EnumParticleTypes.CRIT_MAGIC;
                second = EnumParticleTypes.END_ROD;
                break;
            case 4:
                first = EnumParticleTypes.FIREWORKS_SPARK;
                second = EnumParticleTypes.TOTEM;
                break;
            case 5:
                first = EnumParticleTypes.BLOCK_DUST;
                second = EnumParticleTypes.EXPLOSION_NORMAL;
                break;
            case 6:
                first = EnumParticleTypes.SWEEP_ATTACK;
                second = EnumParticleTypes.CRIT_MAGIC;
                break;
            case 7:
                first = EnumParticleTypes.SLIME;
                second = EnumParticleTypes.VILLAGER_HAPPY;
                break;
            default:
                break;
        }
        int block = style == 5 ? groundState(world, center) : 0;
        ring(world, first, center.add(0.0, 0.16, 0.0), radius * 0.34,
                42, phase, 0.045, block);
        ring(world, second, center.add(0.0, 0.24, 0.0), radius * 0.67,
                62, -phase * 1.3, 0.055, block);
        ring(world, first, center.add(0.0, 0.32, 0.0), radius,
                86, phase * 1.7, 0.075, block);
        radialBurst(world, random, center.add(0.0, 0.55, 0.0), radius,
                95, first, block);

        if (style == 3) {
            for (int i = 0; i < 10; i++) {
                double angle = Math.PI * 2.0 * i / 10.0 + phase;
                Vec3d end = center.add(Math.cos(angle) * radius,
                        (i & 1) == 0 ? 1.5 : 0.1, Math.sin(angle) * radius);
                beam(world, random, center.add(0.0, 0.5, 0.0), end, 0, 2);
            }
        } else if (style == 6) {
            helix(world, EnumParticleTypes.CRIT_MAGIC, center.add(0.0, -0.7, 0.0),
                    radius * 0.80, 4.5, 70, phase, 3, 0.04);
            helix(world, EnumParticleTypes.SWEEP_ATTACK, center.add(0.0, -0.5, 0.0),
                    radius * 0.50, 3.8, 54, -phase * 1.5, 2, 0.02);
        } else if (style == 4) {
            cloud(world, random, center.add(0.0, 0.8, 0.0), radius * 0.65,
                    90, EnumParticleTypes.TOTEM, EnumParticleTypes.END_ROD, 0.28);
        }
    }

    private static void beam(WorldClient world, Random random, Vec3d start, Vec3d end,
                             int style, int strands) {
        Vec3d delta = end.subtract(start);
        double distance = delta.length();
        if (distance < 0.01) return;
        Vec3d direction = delta.scale(1.0 / distance);
        Vec3d reference = Math.abs(direction.y) > 0.88
                ? new Vec3d(1.0, 0.0, 0.0) : new Vec3d(0.0, 1.0, 0.0);
        Vec3d right = direction.crossProduct(reference).normalize();
        Vec3d up = right.crossProduct(direction).normalize();
        int points = MathHelper.clamp((int) Math.ceil(distance * 4.0), 12, 96);
        EnumParticleTypes primary = beamPrimary(style);
        EnumParticleTypes secondary = beamSecondary(style);
        double phase = random.nextDouble() * Math.PI * 2.0;
        int safeStrands = MathHelper.clamp(strands, 1, 4);
        for (int strand = 0; strand < safeStrands; strand++) {
            double strandPhase = phase + Math.PI * 2.0 * strand / safeStrands;
            for (int i = 0; i <= points; i++) {
                double progress = i / (double) points;
                double wobble = style == 0
                        ? 0.05 + random.nextDouble() * 0.13
                        : 0.035 + Math.sin(progress * Math.PI) * 0.09;
                double angle = progress * Math.PI * 8.0 + strandPhase;
                Vec3d offset = right.scale(Math.cos(angle) * wobble)
                        .add(up.scale(Math.sin(angle) * wobble));
                if (style == 0) {
                    offset = offset.add(right.scale(randomVelocity(random, 0.11)))
                            .add(up.scale(randomVelocity(random, 0.11)));
                }
                Vec3d point = start.add(delta.scale(progress)).add(offset);
                particle(world, primary, point.x, point.y, point.z,
                        direction.x * 0.015, direction.y * 0.015, direction.z * 0.015);
                if (strand == 0 && i % 3 == 0) {
                    particle(world, secondary, point.x, point.y, point.z,
                            randomVelocity(random, 0.018), randomVelocity(random, 0.018),
                            randomVelocity(random, 0.018));
                }
            }
        }
    }

    private static void impactFlare(WorldClient world, Random random, Vec3d center,
                                    float radius, int style) {
        float safe = MathHelper.clamp(radius, 0.8f, 4.0f);
        ring(world, beamPrimary(style), center.add(0.0, 0.12, 0.0), safe,
                42, random.nextDouble() * Math.PI * 2.0, 0.07);
        radialBurst(world, random, center.add(0.0, 0.35, 0.0), safe,
                45, beamSecondary(style), 0);
    }

    private static void skyStrike(WorldClient world, Random random, Vec3d landing, int style) {
        Vec3d top = landing.add(0.0, 24.0, 0.0);
        beam(world, random, top, landing, style, 4);
        for (int i = 0; i < 60; i++) {
            double y = landing.y + random.nextDouble() * 22.0;
            double spread = 0.18 + (y - landing.y) * 0.012;
            particle(world, beamSecondary(style), landing.x + randomVelocity(random, spread),
                    y, landing.z + randomVelocity(random, spread),
                    randomVelocity(random, 0.03), -0.16 - random.nextDouble() * 0.18,
                    randomVelocity(random, 0.03));
        }
        impactFlare(world, random, landing, 2.8f, style);
        ring(world, EnumParticleTypes.EXPLOSION_NORMAL, landing.add(0.0, 0.18, 0.0),
                3.6, 58, random.nextDouble(), 0.10);
    }

    private static void fireRain(WorldClient world, Random random, Vec3d landing) {
        for (int i = 0; i < 150; i++) {
            double progress = random.nextDouble();
            double angle = progress * Math.PI * 10.0 + random.nextDouble();
            double spiral = 0.15 + (1.0 - progress) * 0.55;
            double y = landing.y + 22.0 * progress;
            EnumParticleTypes type = i % 4 == 0
                    ? EnumParticleTypes.SMOKE_NORMAL : EnumParticleTypes.FLAME;
            particle(world, type, landing.x + Math.cos(angle) * spiral, y,
                    landing.z + Math.sin(angle) * spiral,
                    Math.cos(angle) * 0.025, -0.18 - random.nextDouble() * 0.18,
                    Math.sin(angle) * 0.025);
        }
        for (int i = 0; i < 70; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double speed = 0.08 + random.nextDouble() * 0.28;
            particle(world, i % 4 == 0 ? EnumParticleTypes.LAVA : EnumParticleTypes.FLAME,
                    landing.x, landing.y + 0.25, landing.z,
                    Math.cos(angle) * speed, 0.10 + random.nextDouble() * 0.30,
                    Math.sin(angle) * speed);
        }
        ring(world, EnumParticleTypes.FLAME, landing.add(0.0, 0.14, 0.0),
                2.8, 64, random.nextDouble(), 0.08);
    }

    private static void artillery(WorldClient world, Random random, Vec3d landing) {
        Vec3d top = landing.add(0.0, 26.0, 0.0);
        beam(world, random, top, landing, 4, 2);
        for (int i = 0; i < 90; i++) {
            double progress = i / 89.0;
            double angle = progress * Math.PI * 12.0;
            particle(world, i % 3 == 0 ? EnumParticleTypes.SMOKE_LARGE
                            : EnumParticleTypes.SMOKE_NORMAL,
                    landing.x + Math.cos(angle) * 0.35, landing.y + 24.0 * progress,
                    landing.z + Math.sin(angle) * 0.35,
                    randomVelocity(random, 0.025), 0.035, randomVelocity(random, 0.025));
        }
        radialBurst(world, random, landing.add(0.0, 0.55, 0.0), 4.2,
                125, EnumParticleTypes.EXPLOSION_NORMAL, 0);
        cloud(world, random, landing.add(0.0, 0.9, 0.0), 3.4, 95,
                EnumParticleTypes.EXPLOSION_LARGE, EnumParticleTypes.SMOKE_LARGE, 0.18);
        int ground = groundState(world, landing);
        radialBurst(world, random, landing.add(0.0, 0.15, 0.0), 4.6,
                90, EnumParticleTypes.BLOCK_CRACK, ground);
    }

    private static void smokeCloud(WorldClient world, Random random, Vec3d center,
                                   float radius) {
        cloud(world, random, center.add(0.0, 1.0, 0.0), radius, 180,
                EnumParticleTypes.SMOKE_LARGE, EnumParticleTypes.CLOUD, 0.08);
        helix(world, EnumParticleTypes.SMOKE_NORMAL, center.add(0.0, -0.4, 0.0),
                radius * 0.82, radius * 1.4, 80,
                world.getTotalWorldTime() * 0.18, 3, 0.025);
        ring(world, EnumParticleTypes.CLOUD, center.add(0.0, 0.16, 0.0),
                radius, 72, random.nextDouble(), 0.035);
    }

    private static void fireZone(WorldClient world, Random random, Vec3d center,
                                 float radius) {
        double phase = world.getTotalWorldTime() * 0.20;
        ring(world, EnumParticleTypes.FLAME, center.add(0.0, 0.14, 0.0),
                radius, 78, phase, 0.055);
        ring(world, EnumParticleTypes.SMOKE_NORMAL, center.add(0.0, 0.35, 0.0),
                radius * 0.72, 58, -phase * 1.3, 0.025);
        for (int i = 0; i < 125; i++) {
            Vec3d point = disc(random, center, radius * 0.92, 0.12);
            particle(world, i % 7 == 0 ? EnumParticleTypes.LAVA : EnumParticleTypes.FLAME,
                    point.x, point.y, point.z, randomVelocity(random, 0.06),
                    0.08 + random.nextDouble() * 0.30, randomVelocity(random, 0.06));
        }
    }

    private static void special(WorldClient world, Random random, Vec3d center,
                                float radius, int style, int seed) {
        double phase = world.getTotalWorldTime() * 0.23 + random.nextDouble();
        if (style == 0) {
            helix(world, EnumParticleTypes.HEART, center.add(0.0, -0.3, 0.0),
                    radius * 0.55, radius * 1.7, 46, phase, 2, 0.025);
            radialBurst(world, random, center.add(0.0, 0.8, 0.0), radius,
                    65, EnumParticleTypes.TOTEM, 0);
            ring(world, EnumParticleTypes.VILLAGER_HAPPY,
                    center.add(0.0, 0.22, 0.0), radius, 54, phase, 0.025);
            return;
        }
        if (style == 1) {
            cloud(world, random, center.add(0.0, 2.2, 0.0), radius, 150,
                    EnumParticleTypes.SMOKE_LARGE, EnumParticleTypes.SPELL_WITCH, 0.18);
            radialBurst(world, random, center.add(0.0, 1.1, 0.0), radius,
                    120, EnumParticleTypes.PORTAL, 0);
            for (int i = 0; i < 5; i++) {
                ring(world, i % 2 == 0 ? EnumParticleTypes.SWEEP_ATTACK
                                : EnumParticleTypes.CRIT_MAGIC,
                        center.add(0.0, 0.6 + i * 0.65, 0.0),
                        radius * (0.35 + i * 0.13), 58,
                        phase + i * 0.75, 0.06);
            }
            return;
        }
        if (style == 2) {
            for (int i = 0; i < 170; i++) {
                Vec3d point = spherePoint(random, center.add(0.0, 1.0, 0.0),
                        radius * (0.25 + random.nextDouble() * 0.75));
                particle(world, i % 4 == 0 ? EnumParticleTypes.CLOUD
                                : EnumParticleTypes.SNOW_SHOVEL,
                        point.x, point.y, point.z, randomVelocity(random, 0.08),
                        0.025 + random.nextDouble() * 0.08, randomVelocity(random, 0.08));
            }
            verticalRing(world, EnumParticleTypes.END_ROD,
                    center.add(0.0, 1.0, 0.0), radius, 68, phase, true);
            verticalRing(world, EnumParticleTypes.SNOW_SHOVEL,
                    center.add(0.0, 1.0, 0.0), radius * 0.82, 68, -phase, false);
            ring(world, EnumParticleTypes.CLOUD, center.add(0.0, 0.12, 0.0),
                    radius, 72, phase * 0.5, 0.035);
            return;
        }
        if (style == 3) {
            fireZone(world, random, center, radius);
            helix(world, EnumParticleTypes.FLAME, center.add(0.0, -0.4, 0.0),
                    radius * 0.68, radius * 2.2, 68, phase, 3, 0.055);
            radialBurst(world, random, center.add(0.0, 0.55, 0.0), radius,
                    95, EnumParticleTypes.LAVA, 0);
            return;
        }
        if (style == 4) {
            radialBurst(world, random, center.add(0.0, 1.0, 0.0), radius * 1.2,
                    180, EnumParticleTypes.CRIT_MAGIC, 0);
            radialBurst(world, random, center.add(0.0, 1.0, 0.0), radius,
                    125, EnumParticleTypes.TOTEM, 0);
            verticalRing(world, EnumParticleTypes.END_ROD,
                    center.add(0.0, 1.0, 0.0), radius, 82, phase, true);
            verticalRing(world, EnumParticleTypes.FIREWORKS_SPARK,
                    center.add(0.0, 1.0, 0.0), radius, 82, -phase * 1.3, false);
            ring(world, EnumParticleTypes.FIREWORKS_SPARK,
                    center.add(0.0, 0.18, 0.0), radius * 1.25, 96,
                    phase * 1.6, 0.09);
            return;
        }
        if (style == 5) {
            for (int i = 0; i < 6; i++) {
                double angle = Math.PI * 2.0 * i / 6.0 + phase;
                double distance = radius * (0.28 + (i % 3) * 0.25);
                Vec3d landing = center.add(Math.cos(angle) * distance, 0.0,
                        Math.sin(angle) * distance);
                beam(world, random, landing.add(0.0, 20.0, 0.0), landing, 2, 1);
                ring(world, EnumParticleTypes.DRAGON_BREATH,
                        landing.add(0.0, 0.16, 0.0), 1.4 + i * 0.12,
                        38, phase + i, 0.08);
            }
            cloud(world, random, center.add(0.0, 1.0, 0.0), radius, 150,
                    EnumParticleTypes.DRAGON_BREATH, EnumParticleTypes.PORTAL, 0.19);
            return;
        }
        if (style == 6) {
            startNuclearBlast(world, center, Math.max(8.0f, radius), seed);
            return;
        }
        cloud(world, random, center.add(0.0, 0.65, 0.0), radius, 110,
                EnumParticleTypes.EXPLOSION_LARGE, EnumParticleTypes.SMOKE_LARGE, 0.20);
        radialBurst(world, random, center.add(0.0, 0.45, 0.0), radius,
                125, EnumParticleTypes.EXPLOSION_NORMAL, 0);
        ring(world, EnumParticleTypes.CLOUD, center.add(0.0, 0.16, 0.0),
                radius * 1.15, 76, phase, 0.12);
        int ground = groundState(world, center);
        radialBurst(world, random, center.add(0.0, 0.12, 0.0), radius,
                70, EnumParticleTypes.BLOCK_CRACK, ground);
    }

    private static void nuclearCharge(WorldClient world, Random random, Vec3d center,
                                      float radius, float progress) {
        double phase = world.getTotalWorldTime() * (0.35 + progress * 0.30);
        double collapse = Math.max(1.15, radius * (0.92 - progress * 0.72));
        int ringPoints = 76 + (int) (progress * 42.0f);
        ring(world, EnumParticleTypes.END_ROD, center.add(0.0, 0.45, 0.0),
                collapse, ringPoints, phase, -0.04);
        verticalRing(world, EnumParticleTypes.FIREWORKS_SPARK,
                center.add(0.0, 0.8, 0.0), collapse * 0.82, ringPoints,
                -phase * 1.35, true);
        verticalRing(world, EnumParticleTypes.CRIT_MAGIC,
                center.add(0.0, 0.8, 0.0), collapse * 0.68, ringPoints,
                phase * 1.65, false);

        int inward = 90 + (int) (progress * 100.0f);
        Vec3d focus = center.add(0.0, 0.8, 0.0);
        for (int i = 0; i < inward; i++) {
            Vec3d outside = spherePoint(random, focus,
                    collapse * (0.70 + random.nextDouble() * 0.55));
            Vec3d velocity = focus.subtract(outside).normalize()
                    .scale(0.13 + progress * 0.22 + random.nextDouble() * 0.09);
            particle(world, i % 4 == 0 ? EnumParticleTypes.END_ROD
                            : EnumParticleTypes.PORTAL,
                    outside.x, outside.y, outside.z, velocity.x, velocity.y, velocity.z);
        }
        for (int i = 0; i < 35 + (int) (progress * 45.0f); i++) {
            particle(world, i % 3 == 0 ? EnumParticleTypes.FIREWORKS_SPARK
                            : EnumParticleTypes.END_ROD,
                    focus.x + randomVelocity(random, 0.28 + progress * 0.18),
                    focus.y + randomVelocity(random, 0.28 + progress * 0.18),
                    focus.z + randomVelocity(random, 0.28 + progress * 0.18),
                    randomVelocity(random, 0.04), randomVelocity(random, 0.04),
                    randomVelocity(random, 0.04));
        }
    }

    private static void nuclearBlastFrame(WorldClient world, Random random, Vec3d center,
                                          float radius, int age) {
        float safe = MathHelper.clamp(radius, 4.0f, 30.0f);
        double phase = random.nextDouble() * Math.PI * 2.0;
        int height = Math.min(28, Math.max(14, (int) (safe * 1.35f)));
        if (age == 0) {
            for (int i = 0; i < 7; i++) {
                particle(world, EnumParticleTypes.EXPLOSION_HUGE,
                        center.x + randomVelocity(random, safe * 0.12),
                        center.y + 0.8 + random.nextDouble() * 1.8,
                        center.z + randomVelocity(random, safe * 0.12), 0.0, 0.0, 0.0);
            }
            radialBurst(world, random, center.add(0.0, 1.0, 0.0), safe * 0.65,
                    155, EnumParticleTypes.FIREWORKS_SPARK, 0);
            radialBurst(world, random, center.add(0.0, 0.7, 0.0), safe * 0.55,
                    105, EnumParticleTypes.END_ROD, 0);
        }

        if (age <= 8) {
            double waveProgress = (age + 1.0) / 9.0;
            double waveRadius = safe * waveProgress;
            ring(world, age % 3 == 0 ? EnumParticleTypes.FIREWORKS_SPARK
                            : EnumParticleTypes.CLOUD,
                    center.add(0.0, 0.12 + age * 0.045, 0.0), waveRadius,
                    88 + age * 4, phase, 0.15 + age * 0.012);
            ring(world, age % 2 == 0 ? EnumParticleTypes.EXPLOSION_NORMAL
                            : EnumParticleTypes.SMOKE_LARGE,
                    center.add(0.0, 0.22, 0.0), waveRadius * 0.82,
                    64 + age * 3, -phase * 1.3, 0.11 + age * 0.01);

            int ground = groundState(world, center);
            for (int i = 0; i < 42; i++) {
                double angle = random.nextDouble() * Math.PI * 2.0;
                double speed = 0.16 + random.nextDouble() * 0.46;
                particle(world, i % 6 == 0 ? EnumParticleTypes.LAVA
                                : EnumParticleTypes.BLOCK_CRACK,
                        center.x + randomVelocity(random, 1.2), center.y + 0.25,
                        center.z + randomVelocity(random, 1.2),
                        Math.cos(angle) * speed, 0.10 + random.nextDouble() * 0.42,
                        Math.sin(angle) * speed, ground);
            }
        }

        double grownHeight = Math.min(height, 2.0 + age * height / 13.0);
        int stemParticles = age <= 20 ? 46 : 22;
        for (int i = 0; i < stemParticles; i++) {
            double yProgress = random.nextDouble();
            double y = center.y + 1.2 + yProgress * grownHeight;
            double stemRadius = 0.8 + yProgress * safe * 0.23;
            double angle = random.nextDouble() * Math.PI * 2.0;
            double distance = Math.sqrt(random.nextDouble()) * stemRadius;
            EnumParticleTypes type = i % 6 == 0
                    ? EnumParticleTypes.FLAME : EnumParticleTypes.SMOKE_LARGE;
            particle(world, type, center.x + Math.cos(angle) * distance, y,
                    center.z + Math.sin(angle) * distance,
                    Math.cos(angle) * 0.025, 0.06 + random.nextDouble() * 0.08,
                    Math.sin(angle) * 0.025);
        }

        if (age < 7) return;
        double capGrowth = Math.min(1.0, (age - 6.0) / 11.0);
        Vec3d cap = center.add(0.0, Math.min(height, grownHeight), 0.0);
        int capParticles = 48 + (int) (capGrowth * 30.0);
        for (int i = 0; i < capParticles; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double distance = safe * capGrowth
                    * (0.12 + Math.sqrt(random.nextDouble()) * 0.48);
            double crown = Math.sin((distance / Math.max(0.1, safe * 0.70)) * Math.PI)
                    * safe * 0.12;
            EnumParticleTypes type = i % 8 == 0
                    ? EnumParticleTypes.EXPLOSION_LARGE : EnumParticleTypes.SMOKE_LARGE;
            particle(world, type, cap.x + Math.cos(angle) * distance,
                    cap.y + crown + randomVelocity(random, 0.7),
                    cap.z + Math.sin(angle) * distance,
                    Math.cos(angle) * 0.04, 0.035 + random.nextDouble() * 0.06,
                    Math.sin(angle) * 0.04);
        }
        ring(world, age % 3 == 0 ? EnumParticleTypes.FIREWORKS_SPARK
                        : EnumParticleTypes.SMOKE_NORMAL,
                cap, safe * 0.55 * capGrowth, 70, -phase * 1.4, 0.06);
    }

    private static void cloud(WorldClient world, Random random, Vec3d center,
                              double radius, int count, EnumParticleTypes first,
                              EnumParticleTypes second, double speed) {
        for (int i = 0; i < count; i++) {
            Vec3d point = spherePoint(random, center,
                    radius * (0.20 + random.nextDouble() * 0.80));
            particle(world, (i & 3) == 0 ? second : first,
                    point.x, point.y * 0.35 + center.y * 0.65, point.z,
                    randomVelocity(random, speed), randomVelocity(random, speed * 0.45),
                    randomVelocity(random, speed));
        }
    }

    private static void radialBurst(WorldClient world, Random random, Vec3d center,
                                    double radius, int count, EnumParticleTypes type,
                                    int... arguments) {
        for (int i = 0; i < count; i++) {
            Vec3d direction = randomDirection(random);
            double speed = 0.05 + random.nextDouble() * Math.max(0.10, radius * 0.07);
            particle(world, type, center.x + direction.x * random.nextDouble() * 0.4,
                    center.y + direction.y * random.nextDouble() * 0.4,
                    center.z + direction.z * random.nextDouble() * 0.4,
                    direction.x * speed, direction.y * speed, direction.z * speed,
                    arguments);
        }
    }

    private static void ring(WorldClient world, EnumParticleTypes type, Vec3d center,
                             double radius, int points, double phase, double speed,
                             int... arguments) {
        int safePoints = MathHelper.clamp(points, 8, 128);
        for (int i = 0; i < safePoints; i++) {
            double angle = Math.PI * 2.0 * i / safePoints + phase;
            double cosine = Math.cos(angle);
            double sine = Math.sin(angle);
            particle(world, type, center.x + cosine * radius, center.y,
                    center.z + sine * radius, cosine * speed, 0.015, sine * speed,
                    arguments);
        }
    }

    private static void verticalRing(WorldClient world, EnumParticleTypes type,
                                     Vec3d center, double radius, int points,
                                     double phase, boolean xPlane) {
        int safePoints = MathHelper.clamp(points, 8, 128);
        for (int i = 0; i < safePoints; i++) {
            double angle = Math.PI * 2.0 * i / safePoints + phase;
            double a = Math.cos(angle) * radius;
            double b = Math.sin(angle) * radius;
            double x = xPlane ? center.x + a : center.x;
            double z = xPlane ? center.z : center.z + a;
            particle(world, type, x, center.y + b, z, 0.0, 0.0, 0.0);
        }
    }

    private static void helix(WorldClient world, EnumParticleTypes type, Vec3d center,
                              double radius, double height, int points, double phase,
                              int strands, double speed) {
        int safePoints = MathHelper.clamp(points, 12, 100);
        for (int strand = 0; strand < strands; strand++) {
            double strandPhase = phase + Math.PI * 2.0 * strand / strands;
            for (int i = 0; i < safePoints; i++) {
                double progress = i / (double) Math.max(1, safePoints - 1);
                double angle = progress * Math.PI * 5.0 + strandPhase;
                double cosine = Math.cos(angle);
                double sine = Math.sin(angle);
                particle(world, type, center.x + cosine * radius,
                        center.y + progress * height, center.z + sine * radius,
                        cosine * speed, 0.01, sine * speed);
            }
        }
    }

    private static EnumParticleTypes beamPrimary(int style) {
        switch (style) {
            case 1:
                return EnumParticleTypes.END_ROD;
            case 2:
                return EnumParticleTypes.DRAGON_BREATH;
            case 3:
                return EnumParticleTypes.SLIME;
            case 0:
                return EnumParticleTypes.CRIT_MAGIC;
            default:
                return EnumParticleTypes.CRIT;
        }
    }

    private static EnumParticleTypes beamSecondary(int style) {
        switch (style) {
            case 1:
                return EnumParticleTypes.FIREWORKS_SPARK;
            case 2:
                return EnumParticleTypes.PORTAL;
            case 3:
                return EnumParticleTypes.VILLAGER_HAPPY;
            case 0:
                return EnumParticleTypes.END_ROD;
            default:
                return EnumParticleTypes.SMOKE_NORMAL;
        }
    }

    private static Vec3d disc(Random random, Vec3d center, double radius, double yOffset) {
        double angle = random.nextDouble() * Math.PI * 2.0;
        double distance = Math.sqrt(random.nextDouble()) * radius;
        return new Vec3d(center.x + Math.cos(angle) * distance, center.y + yOffset,
                center.z + Math.sin(angle) * distance);
    }

    private static Vec3d spherePoint(Random random, Vec3d center, double radius) {
        return center.add(randomDirection(random).scale(radius));
    }

    private static Vec3d randomDirection(Random random) {
        double y = random.nextDouble() * 2.0 - 1.0;
        double angle = random.nextDouble() * Math.PI * 2.0;
        double horizontal = Math.sqrt(Math.max(0.0, 1.0 - y * y));
        return new Vec3d(Math.cos(angle) * horizontal, y, Math.sin(angle) * horizontal);
    }

    private static double randomVelocity(Random random, double scale) {
        return (random.nextDouble() * 2.0 - 1.0) * scale;
    }

    private static boolean finite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    private static int groundState(WorldClient world, Vec3d center) {
        BlockPos position = new BlockPos(center).down();
        IBlockState state = world.isBlockLoaded(position)
                ? world.getBlockState(position) : Blocks.STONE.getDefaultState();
        if (state.getBlock() == Blocks.AIR) state = Blocks.STONE.getDefaultState();
        return Block.getStateId(state);
    }

    private static void beginTick(WorldClient world) {
        long tick = world.getTotalWorldTime();
        if (world != budgetWorld || tick != budgetTick) {
            budgetWorld = world;
            budgetTick = tick;
            particlesThisTick = 0;
            particleLimitThisTick = configuredParticleLimit();
        }
    }

    private static int configuredParticleLimit() {
        Minecraft minecraft = Minecraft.getMinecraft();
        int configured = MathHelper.clamp(TguConfig.maxClientEffectParticles, 300, 4000);
        if (minecraft.gameSettings == null) return configured;
        if (minecraft.gameSettings.particleSetting == 2) return Math.max(120, configured / 4);
        if (minecraft.gameSettings.particleSetting == 1) return Math.max(240, configured / 2);
        return configured;
    }

    private static void particle(WorldClient world, EnumParticleTypes type,
                                 double x, double y, double z,
                                 double velocityX, double velocityY, double velocityZ,
                                 int... arguments) {
        if (particlesThisTick >= particleLimitThisTick) return;
        particlesThisTick++;
        // Our own dimension/distance/budget checks above replace vanilla's
        // hard 32-block particle cut-off, which would hide distant sky strikes
        // and most of a nuclear mushroom.
        world.spawnParticle(type, true, x, y, z,
                velocityX, velocityY, velocityZ, arguments);
    }

    private static final class NuclearAnimation {
        private final WorldClient world;
        private final Vec3d center;
        private final float radius;
        private final int seed;
        private int age;

        private NuclearAnimation(WorldClient world, Vec3d center, float radius, int seed) {
            this.world = world;
            this.center = center;
            this.radius = radius;
            this.seed = seed;
        }
    }
}

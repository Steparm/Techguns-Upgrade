package com.stepm.techgunsupgrade.event;

import com.stepm.techgunsupgrade.TechgunsUpgradeMod;
import com.stepm.techgunsupgrade.config.TguConfig;
import com.stepm.techgunsupgrade.debug.DebugSettings;
import com.stepm.techgunsupgrade.debug.SafeModeAccess;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;

/** Keeps upgraded flamethrower ground fire alive for its explicit bonus duration. */
@Mod.EventBusSubscriber(modid = TechgunsUpgradeMod.MODID)
public final class GroundFireTracker {
    private static final Map<World, Map<BlockPos, Long>> ACTIVE = new WeakHashMap<>();

    private GroundFireTracker() {
    }

    public static void keepBurning(World world, BlockPos position, int bonusTicks) {
        keepBurning(world, position, bonusTicks, null);
    }

    public static void keepBurning(World world, BlockPos position, int bonusTicks,
                                   Entity owner) {
        if (world == null || world.isRemote || position == null || bonusTicks <= 0) return;
        if (!SafeModeAccess.canDamageBlocks(owner)) return;
        if (world.isAirBlock(position) && Blocks.FIRE.canPlaceBlockAt(world, position)) {
            world.setBlockState(position, Blocks.FIRE.getDefaultState(), 3);
        }
        Map<BlockPos, Long> fires = ACTIVE.computeIfAbsent(world,
                ignored -> new LinkedHashMap<>());
        BlockPos immutable = position.toImmutable();
        int limit = Math.max(64, Math.min(2048, TguConfig.maxTrackedGroundFires));
        if (!fires.containsKey(immutable) && fires.size() >= limit) {
            Iterator<BlockPos> oldest = fires.keySet().iterator();
            if (oldest.hasNext()) {
                oldest.next();
                oldest.remove();
            }
        }
        fires.put(immutable, world.getTotalWorldTime() + bonusTicks);
    }

    public static void createFireZone(World world, BlockPos center, int radius, int durationTicks) {
        createFireZone(world, center, radius, durationTicks, null);
    }

    public static void createFireZone(World world, BlockPos center, int radius,
                                      int durationTicks, Entity owner) {
        if (world == null || world.isRemote || center == null || durationTicks <= 0) return;
        // The damage-safe setting must never hide the visual warning of a fire zone.
        UpgradeVisualEffects.fireZone(world,
                new net.minecraft.util.math.Vec3d(center).add(0.5, 0.0, 0.5),
                Math.max(1.0f, radius + 0.35f));
        if (!SafeModeAccess.canDamageBlocks(owner)) return;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z > radius * radius) continue;
                BlockPos column = center.add(x, 0, z);
                BlockPos fire = world.isAirBlock(column) ? column : column.up();
                if (world.isAirBlock(fire) && Blocks.FIRE.canPlaceBlockAt(world, fire)) {
                    keepBurning(world, fire, durationTicks, owner);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world == null || event.world.isRemote) return;
        if (!DebugSettings.canDamageBlocks()) {
            ACTIVE.remove(event.world);
            return;
        }
        Map<BlockPos, Long> fires = ACTIVE.get(event.world);
        if (fires == null || fires.isEmpty()) return;

        long now = event.world.getTotalWorldTime();
        Iterator<Map.Entry<BlockPos, Long>> iterator = fires.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, Long> fire = iterator.next();
            if (now > fire.getValue()) {
                iterator.remove();
                continue;
            }
            BlockPos position = fire.getKey();
            if (event.world.isAirBlock(position) && Blocks.FIRE.canPlaceBlockAt(event.world, position)) {
                event.world.setBlockState(position, Blocks.FIRE.getDefaultState(), 3);
            } else if (event.world.getBlockState(position).getBlock() != Blocks.FIRE) {
                iterator.remove();
            }
        }
        if (fires.isEmpty()) ACTIVE.remove(event.world);
    }
}

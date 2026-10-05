package com.stepm.techgunsupgrade.event;

import com.stepm.techgunsupgrade.TechgunsUpgradeMod;
import com.stepm.techgunsupgrade.debug.SafeModeAccess;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One-tick, claim-aware nuclear terrain destruction invoked after the short
 * charge phase. Damage, visual blast and crater share that detonation tick. Every block still
 * goes through the normal Forge break event, so claims, bedrock and other
 * unbreakable blocks remain protected.
 */
@Mod.EventBusSubscriber(modid = TechgunsUpgradeMod.MODID)
public final class NuclearExplosionTracker {
    private NuclearExplosionTracker() {
    }

    public static void create(World world, Vec3d center, EntityPlayer owner, float radius) {
        if (!(world instanceof WorldServer) || world.isRemote || center == null || owner == null) return;
        spawnInitialVisual(world, center, radius);
        if (!SafeModeAccess.canDamageBlocks(owner)) return;

        WorldServer server = (WorldServer) world;
        int blockRadius = Math.max(1, Math.min(15, (int) Math.ceil(radius)));
        BlockPos origin = new BlockPos(center);
        Map<Chunk, Integer> changedChunks = new LinkedHashMap<>();
        int radiusSquared = blockRadius * blockRadius;
        for (int x = -blockRadius; x <= blockRadius; x++) {
            for (int y = -blockRadius; y <= blockRadius; y++) {
                for (int z = -blockRadius; z <= blockRadius; z++) {
                    int distanceSquared = x * x + y * y + z * z;
                    if (distanceSquared > radiusSquared) continue;
                    destroyIfAllowed(server, owner, origin.add(x, y, z), changedChunks);
                }
            }
        }
        synchronizeChunks(server, changedChunks);
    }

    private static boolean destroyIfAllowed(WorldServer world, EntityPlayer owner,
                                            BlockPos position,
                                            Map<Chunk, Integer> changedChunks) {
        if (!world.isBlockLoaded(position) || !world.isBlockModifiable(owner, position)) {
            return false;
        }
        IBlockState state = world.getBlockState(position);
        if (state.getBlock() == Blocks.AIR || state.getBlock() == Blocks.BEDROCK
                || state.getBlockHardness(world, position) < 0.0f) return false;

        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, position, state, owner);
        if (MinecraftForge.EVENT_BUS.post(event)) return false;

        Chunk chunk = world.getChunk(position);
        ExtendedBlockStorage storage = chunk.getBlockStorageArray()[position.getY() >> 4];
        if (storage == null) return false;
        if (state.getBlock().hasTileEntity(state)) {
            state.getBlock().breakBlock(world, position, state);
            world.removeTileEntity(position);
        }
        storage.set(position.getX() & 15, position.getY() & 15,
                position.getZ() & 15, Blocks.AIR.getDefaultState());
        chunk.markDirty();
        int section = 1 << (position.getY() >> 4);
        changedChunks.put(chunk, changedChunks.getOrDefault(chunk, 0) | section);
        return true;
    }

    private static void synchronizeChunks(WorldServer world, Map<Chunk, Integer> changedChunks) {
        for (Map.Entry<Chunk, Integer> changed : changedChunks.entrySet()) {
            Chunk chunk = changed.getKey();
            chunk.generateSkylightMap();
            PlayerChunkMapEntry entry = world.getPlayerChunkMap().getEntry(chunk.x, chunk.z);
            if (entry != null) {
                entry.sendPacket(new SPacketChunkData(chunk, changed.getValue()));
            }
        }
    }

    private static void spawnInitialVisual(World world, Vec3d center, float radius) {
        UpgradeVisualEffects.nuclearBlast(world, center, radius);
    }
}
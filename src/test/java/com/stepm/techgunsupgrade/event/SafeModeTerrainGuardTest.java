package com.stepm.techgunsupgrade.event;

import com.stepm.techgunsupgrade.debug.DebugSettings;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SafeModeTerrainGuardTest {
    @AfterEach
    void resetSettings() {
        DebugSettings.reset();
    }

    @Test
    void safeModeClearsOnlyTheServerExplosionBlockList() {
        DebugSettings.setSafeMode(true);
        List<BlockPos> serverBlocks = new ArrayList<>();
        serverBlocks.add(BlockPos.ORIGIN);
        serverBlocks.add(new BlockPos(1, 2, 3));

        SafeModeTerrainGuard.protectTerrain(false, serverBlocks);

        assertTrue(serverBlocks.isEmpty());

        List<BlockPos> clientBlocks = new ArrayList<>(Collections.singletonList(BlockPos.ORIGIN));
        SafeModeTerrainGuard.protectTerrain(true, clientBlocks);
        assertEquals(1, clientBlocks.size());
    }

    @Test
    void normalModeKeepsExplosionBlocks() {
        List<BlockPos> blocks = new ArrayList<>(Collections.singletonList(BlockPos.ORIGIN));

        SafeModeTerrainGuard.protectTerrain(false, blocks);

        assertEquals(1, blocks.size());
    }
}

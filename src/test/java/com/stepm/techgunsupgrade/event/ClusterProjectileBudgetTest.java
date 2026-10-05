package com.stepm.techgunsupgrade.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClusterProjectileBudgetTest {
    @Test
    void expiresClusterChildrenAfterEightTicksOrFourBlocks() {
        assertFalse(UpgradeEventHandler.clusterChildExpired(7, 15.99));
        assertTrue(UpgradeEventHandler.clusterChildExpired(8, 0.0));
        assertTrue(UpgradeEventHandler.clusterChildExpired(1, 16.0));
        assertTrue(UpgradeEventHandler.clusterChildExpired(1, Double.NaN));
    }
}

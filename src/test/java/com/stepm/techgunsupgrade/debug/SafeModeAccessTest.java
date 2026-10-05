package com.stepm.techgunsupgrade.debug;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SafeModeAccessTest {
    @AfterEach
    void resetSettings() {
        DebugSettings.reset();
    }

    @Test
    void operatorSafeModeStillProtectsOwnerlessEffects() {
        DebugSettings.setSafeMode(true);
        assertTrue(SafeModeAccess.isSafeMode(null));
        assertFalse(SafeModeAccess.canDamageBlocks(null));
    }

    @Test
    void ownerlessEffectsRemainDestructiveWithoutEitherSafeMode() {
        DebugSettings.setSafeMode(false);
        assertFalse(SafeModeAccess.isSafeMode(null));
        assertTrue(SafeModeAccess.canDamageBlocks(null));
    }
}

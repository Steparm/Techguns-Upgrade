package com.stepm.techgunsupgrade.debug;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DebugSettingsTest {

    @AfterEach
    void resetSettings() {
        DebugSettings.reset();
    }

    @Test
    void forcedRollOnlyOverridesPositiveChances() {
        DebugSettings.setForceRandomEffects(true);
        Random never = new Random() {
            @Override public float nextFloat() { return 0.99f; }
        };

        assertTrue(DebugSettings.roll(never, 0.01f));
        assertFalse(DebugSettings.roll(never, 0.0f));
    }

    @Test
    void safeModeOnlyDisablesTerrainDamageAndResetRestoresDefaults() {
        DebugSettings.setSafeMode(true);
        DebugSettings.setForceRandomEffects(true);
        DebugSettings.setVerificationRunning(true);
        assertFalse(DebugSettings.canDamageBlocks());
        assertTrue(DebugSettings.canCreateNuclearExplosions());

        DebugSettings.reset();
        assertTrue(DebugSettings.canDamageBlocks());
        assertTrue(DebugSettings.canCreateNuclearExplosions());
        assertFalse(DebugSettings.isForceRandomEffects());
        assertFalse(DebugSettings.isVerificationRunning());
    }
}

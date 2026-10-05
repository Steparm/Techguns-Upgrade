package com.stepm.techgunsupgrade.debug;

import java.util.Random;

/**
 * Volatile, server-lifetime switches used by the in-game test command.
 * Nothing is persisted deliberately: a production server always starts with
 * normal probabilities and normal terrain damage.
 */
public final class DebugSettings {

    private static volatile boolean forceRandomEffects;
    private static volatile boolean safeMode;
    private static volatile boolean verificationRunning;

    private DebugSettings() {
    }

    public static boolean isForceRandomEffects() {
        return forceRandomEffects;
    }

    public static void setForceRandomEffects(boolean enabled) {
        forceRandomEffects = enabled;
    }

    public static boolean isSafeMode() {
        return safeMode;
    }

    public static void setSafeMode(boolean enabled) {
        safeMode = enabled;
    }

    public static boolean canDamageBlocks() {
        return !safeMode;
    }

    /**
     * Kept for source compatibility. Safe mode never cancels the detonation:
     * it only prevents terrain changes through {@link #canDamageBlocks()}.
     */
    @Deprecated
    public static boolean canCreateNuclearExplosions() {
        return true;
    }

    public static boolean isVerificationRunning() {
        return verificationRunning;
    }

    public static void setVerificationRunning(boolean running) {
        verificationRunning = running;
    }

    public static boolean roll(Random random, float chance) {
        if (chance <= 0.0f || random == null) return false;
        return forceRandomEffects || random.nextFloat() < chance;
    }

    public static void reset() {
        forceRandomEffects = false;
        safeMode = false;
        verificationRunning = false;
    }
}

package com.stepm.techgunsupgrade.network;

/** Stable SimpleNetworkWrapper discriminator assignments. */
public final class NetworkProtocol {
    /** Upgrade-table start request, client to server. */
    public static final int START_UPGRADE_C2S = 0;
    /** Rich combat visual, server to client. Kept at its original protocol ID. */
    public static final int VISUAL_EFFECT_S2C = 1;
    /** Upgrade-table reset request, client to server. */
    public static final int RESET_UPGRADES_C2S = 2;

    private NetworkProtocol() {
    }
}

package com.stepm.techgunsupgrade.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class NetworkProtocolTest {
    @Test
    void packetDiscriminatorsStayStableAndUnique() {
        assertEquals(0, NetworkProtocol.START_UPGRADE_C2S);
        assertEquals(1, NetworkProtocol.VISUAL_EFFECT_S2C);
        assertEquals(2, NetworkProtocol.RESET_UPGRADES_C2S);

        assertNotEquals(NetworkProtocol.START_UPGRADE_C2S, NetworkProtocol.VISUAL_EFFECT_S2C);
        assertNotEquals(NetworkProtocol.START_UPGRADE_C2S, NetworkProtocol.RESET_UPGRADES_C2S);
        assertNotEquals(NetworkProtocol.VISUAL_EFFECT_S2C, NetworkProtocol.RESET_UPGRADES_C2S);
    }
}

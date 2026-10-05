package com.stepm.techgunsupgrade.network;

import com.stepm.techgunsupgrade.TechgunsUpgradeMod;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NetworkHandler {

    public static final SimpleNetworkWrapper INSTANCE = 
        NetworkRegistry.INSTANCE.newSimpleChannel(TechgunsUpgradeMod.MODID);

    private static boolean registered;

    public static synchronized void registerMessages() {
        if (registered) return;

        INSTANCE.registerMessage(PacketStartUpgrade.Handler.class, PacketStartUpgrade.class,
                NetworkProtocol.START_UPGRADE_C2S, Side.SERVER);
        INSTANCE.registerMessage(PacketVisualEffect.Handler.class, PacketVisualEffect.class,
                NetworkProtocol.VISUAL_EFFECT_S2C, Side.CLIENT);
        INSTANCE.registerMessage(PacketResetUpgrades.Handler.class, PacketResetUpgrades.class,
                NetworkProtocol.RESET_UPGRADES_C2S, Side.SERVER);
        INSTANCE.registerMessage(PacketSyncApocalypseData.Handler.class, PacketSyncApocalypseData.class,
                10, Side.CLIENT);
        INSTANCE.registerMessage(PacketOpenCardGui.Handler.class, PacketOpenCardGui.class,
                11, Side.CLIENT);
        registered = true;
        TechgunsUpgradeMod.LOGGER.info("Network messages registered (start={}, visual={}, reset={}, sync={}, card={})",
                NetworkProtocol.START_UPGRADE_C2S,
                NetworkProtocol.VISUAL_EFFECT_S2C,
                NetworkProtocol.RESET_UPGRADES_C2S,
                10, 11);
    }

    public static void sendToAll(IMessage packet) {
        INSTANCE.sendToAll(packet);
    }
}
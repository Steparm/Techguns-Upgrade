package com.stepm.techgunsupgrade.proxy;

import com.stepm.techgunsupgrade.network.PacketVisualEffect;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public void handleVisualEffect(PacketVisualEffect message) {
        // ClientProxy renders it; the dedicated server intentionally does nothing.
    }

    public void preInit(FMLPreInitializationEvent event) {
    }

    public void init(FMLInitializationEvent event) {
    }

    public void postInit(FMLPostInitializationEvent event) {
    }
}

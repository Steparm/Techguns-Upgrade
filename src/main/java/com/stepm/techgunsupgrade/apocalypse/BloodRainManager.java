package com.stepm.techgunsupgrade.apocalypse;

import com.stepm.techgunsupgrade.TechgunsUpgradeMod;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = TechgunsUpgradeMod.MODID, value = Side.CLIENT)
@SideOnly(Side.CLIENT)
public class BloodRainManager {

    private static boolean wasActive = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null) return;

        boolean isActive = ZombieApocalypseManager.isApocalypseActive();

        if (isActive != wasActive) {
            wasActive = isActive;

            World world = mc.world;
            if (world != null) {
                if (isActive) {
                    world.getWorldInfo().setRaining(true);
                    world.getWorldInfo().setRainTime(999999);
                } else {
                    world.getWorldInfo().setRaining(false);
                    world.getWorldInfo().setRainTime(0);
                }
            }
        }

        if (isActive && mc.world != null) {
            if (!mc.world.isRaining()) {
                mc.world.getWorldInfo().setRaining(true);
                mc.world.getWorldInfo().setRainTime(999999);
            }
            if (mc.world.getWorldInfo().getRainTime() < 1000) {
                mc.world.getWorldInfo().setRainTime(999999);
            }
        }
    }
}
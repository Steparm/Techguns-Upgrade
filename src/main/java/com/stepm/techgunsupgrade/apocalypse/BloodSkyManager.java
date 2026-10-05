package com.stepm.techgunsupgrade.apocalypse;

import com.stepm.techgunsupgrade.TechgunsUpgradeMod;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = TechgunsUpgradeMod.MODID, value = Side.CLIENT)
@SideOnly(Side.CLIENT)
public class BloodSkyManager {

    private static float currentRedness = 0.0f;

    @SubscribeEvent
    public static void onFogColor(EntityViewRenderEvent.FogColors event) {
        if (!ZombieApocalypseManager.isApocalypseActive()) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null) return;

        int wave = ZombieApocalypseManager.getCurrentWave();
        if (wave <= 0) return;

        float redness = calculateRedness(wave);
        currentRedness = MathHelper.clamp(currentRedness + (redness - currentRedness) * 0.02f, 0.0f, 1.0f);

        float r = 0.5f + currentRedness * 0.5f;
        float g = 0.6f - currentRedness * 0.55f;
        float b = 0.7f - currentRedness * 0.65f;

        r = MathHelper.clamp(r, 0.1f, 0.95f);
        g = MathHelper.clamp(g, 0.0f, 0.6f);
        b = MathHelper.clamp(b, 0.0f, 0.6f);

        event.setRed(r);
        event.setGreen(g);
        event.setBlue(b);
    }

    @SubscribeEvent
    public static void onFogDensity(EntityViewRenderEvent.FogDensity event) {
        if (!ZombieApocalypseManager.isApocalypseActive()) return;

        int wave = ZombieApocalypseManager.getCurrentWave();
        if (wave <= 0) return;

        float density = calculateFogDensity(wave);
        event.setDensity(density);
        event.setCanceled(true);
    }

    private static float calculateRedness(int wave) {
        if (wave <= 5) return 0.1f;
        if (wave <= 10) return 0.2f;
        if (wave <= 15) return 0.35f;
        if (wave <= 20) return 0.5f;
        if (wave <= 30) return 0.65f;
        if (wave <= 40) return 0.75f;
        if (wave <= 50) return 0.85f;
        if (wave <= 75) return 0.92f;
        return 0.98f;
    }

    private static float calculateFogDensity(int wave) {
        float base = 0.01f;
        if (wave <= 5) return base + 0.0f;
        if (wave <= 10) return base + 0.002f;
        if (wave <= 20) return base + 0.005f;
        if (wave <= 30) return base + 0.01f;
        if (wave <= 50) return base + 0.02f;
        if (wave <= 75) return base + 0.035f;
        return base + 0.05f;
    }

    public static void resetSky() {
        currentRedness = 0.0f;
    }
}
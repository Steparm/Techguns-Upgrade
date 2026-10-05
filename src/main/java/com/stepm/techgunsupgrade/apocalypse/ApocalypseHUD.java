package com.stepm.techgunsupgrade.apocalypse;

import com.stepm.techgunsupgrade.TechgunsUpgradeMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = TechgunsUpgradeMod.MODID, value = Side.CLIENT)
@SideOnly(Side.CLIENT)
public class ApocalypseHUD {

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.TEXT) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if (player == null) return;
        if (!ZombieApocalypseManager.isApocalypseActive()) return;

        int currentWave = ZombieApocalypseManager.getCurrentWave();
        int totalKills = ZombieApocalypseManager.getTotalKills();
        int zombiesAlive = ZombieApocalypseManager.getZombiesAlive();

        if (currentWave == 0) return;

        ScaledResolution resolution = new ScaledResolution(mc);
        int screenWidth = resolution.getScaledWidth();
        int screenHeight = resolution.getScaledHeight();

        int centerX = screenWidth / 2;
        int yOffset = 20;

        int barWidth = 220;
        int barHeight = 40;
        int barX = centerX - barWidth / 2;
        int barY = yOffset;

        Gui.drawRect(barX - 5, barY - 5, barX + barWidth + 5, barY + barHeight + 5, 0xCC000000);
        Gui.drawRect(barX - 4, barY - 4, barX + barWidth + 4, barY + barHeight + 4, 0xFF444444);
        Gui.drawRect(barX - 3, barY - 3, barX + barWidth + 3, barY + barHeight + 3, 0x00000000);

        int waveColor;
        if (currentWave <= 5) waveColor = 0xFFFFFF;
        else if (currentWave <= 10) waveColor = 0xFFFF55;
        else if (currentWave <= 15) waveColor = 0xFFAA00;
        else if (currentWave <= 20) waveColor = 0xFF5555;
        else if (currentWave <= 30) waveColor = 0xAA00FF;
        else if (currentWave <= 50) waveColor = 0xFF00AA;
        else waveColor = 0xFF0000;

        boolean isBossWave = (currentWave % 5 == 0) || (currentWave == 50) || (currentWave == 100);

        String prefix = isBossWave ? "BANDIT " : "ZOMBIE ";
        String waveText = prefix + "WAVE " + currentWave;
        String statsText = "Kills: " + totalKills + "  |  Enemies: " + zombiesAlive;

        mc.fontRenderer.drawString(
            waveText,
            centerX - mc.fontRenderer.getStringWidth(waveText) / 2,
            barY + 4,
            isBossWave ? 0xFFAA00 : waveColor,
            true
        );

        mc.fontRenderer.drawString(
            statsText,
            centerX - mc.fontRenderer.getStringWidth(statsText) / 2,
            barY + 18,
            0xCCCCCC,
            true
        );

        int progressBarY = barY + barHeight + 6;
        int progressBarWidth = 200;
        int progressBarX = centerX - progressBarWidth / 2;

        Gui.drawRect(progressBarX, progressBarY, progressBarX + progressBarWidth, progressBarY + 4, 0x66000000);

        float progress = Math.min(1.0f, (float) currentWave / 100.0f);
        int fillWidth = (int) (progressBarWidth * progress);

        int fillColor;
        if (isBossWave) fillColor = 0xFFFFAA00;
        else if (currentWave >= 50) fillColor = 0xFFFF5500;
        else if (currentWave >= 100) fillColor = 0xFFFF0000;
        else {
            int red = (int) (255 * progress);
            int green = (int) (255 * (1 - progress));
            fillColor = (0xFF << 24) | (red << 16) | (green << 8) | 0x00;
        }

        Gui.drawRect(progressBarX, progressBarY, progressBarX + fillWidth, progressBarY + 4, fillColor);

        if (currentWave >= 100) {
            int pulse = (int) (Math.sin(System.currentTimeMillis() / 300.0) * 20 + 30);
            int alpha = Math.min(60, pulse);
            Gui.drawRect(0, 0, screenWidth, screenHeight, (alpha << 24) | 0xFF0000);
        }

        if (isBossWave && currentWave != 50 && currentWave != 100) {
            String bossText = "BANDIT WAVE";
            mc.fontRenderer.drawString(
                bossText,
                centerX - mc.fontRenderer.getStringWidth(bossText) / 2,
                barY - 14,
                0xFFAA00,
                true
            );
        }

        if (currentWave == 50) {
            String text = "HELICOPTER";
            mc.fontRenderer.drawString(
                text,
                centerX - mc.fontRenderer.getStringWidth(text) / 2,
                barY - 14,
                0xFF5500,
                true
            );
        }

        if (currentWave == 100) {
            String text = "TWO HELICOPTERS";
            mc.fontRenderer.drawString(
                text,
                centerX - mc.fontRenderer.getStringWidth(text) / 2,
                barY - 14,
                0xFF0000,
                true
            );
        }
    }
}
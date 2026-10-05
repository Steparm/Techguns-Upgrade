package com.stepm.techgunsupgrade.card;

import com.stepm.techgunsupgrade.card.gui.GuiCardChoice;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;

public class CardRewardHandler {

    public static void openVotingGui(EntityPlayer player, int wave) {
        if (player == null) return;

        if (player.world.isRemote) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                Minecraft.getMinecraft().displayGuiScreen(
                    new GuiCardChoice(player, CardVotingManager.getPlayerCards(player), wave)
                );
            });
        } else {
            player.sendMessage(new TextComponentString("Open the GUI to vote for a card"));
        }
    }
}
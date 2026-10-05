package com.stepm.techgunsupgrade.card.gui;

import com.stepm.techgunsupgrade.card.Card;
import com.stepm.techgunsupgrade.card.CardVotingManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;

import java.io.IOException;
import java.util.List;

public class GuiCardChoice extends GuiScreen {

    private final List<Card> cards;
    private final EntityPlayer player;
    private final int wave;
    private int animTime = 0;
    private boolean voted = false;

    public GuiCardChoice(EntityPlayer player, List<Card> cards, int wave) {
        this.player = player;
        this.cards = cards;
        this.wave = wave;
    }

    @Override
    public void initGui() {
        super.initGui();
        ScaledResolution resolution = new ScaledResolution(mc);
        int centerX = resolution.getScaledWidth() / 2;
        int centerY = resolution.getScaledHeight() / 2;

        int buttonWidth = 160;
        int buttonHeight = 200;
        int spacing = 30;
        int totalWidth = cards.size() * buttonWidth + (cards.size() - 1) * spacing;
        int startX = centerX - totalWidth / 2;

        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            int x = startX + i * (buttonWidth + spacing);
            int y = centerY - buttonHeight / 2;

            CardVoteButton button = new CardVoteButton(
                i,
                x, y,
                buttonWidth, buttonHeight,
                card,
                this
            );
            this.buttonList.add(button);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        animTime++;

        drawDefaultBackground();
        drawRect(0, 0, width, height, 0x88000000);

        ScaledResolution resolution = new ScaledResolution(mc);
        int centerX = resolution.getScaledWidth() / 2;

        float pulse = (float) (Math.sin(animTime / 20.0) * 0.1 + 1.0);
        GlStateManager.pushMatrix();
        GlStateManager.translate(centerX, 30, 0);
        GlStateManager.scale(1.5f * pulse, 1.5f * pulse, 1.5f * pulse);
        String title = "§6CHOOSE A CARD TO VOTE FOR!";
        mc.fontRenderer.drawString(
            title,
            -mc.fontRenderer.getStringWidth(title) / 2,
            0,
            0xFFFFFF,
            true
        );
        GlStateManager.popMatrix();

        String subtitle = "§7The card with the most votes wins";
        mc.fontRenderer.drawString(
            subtitle,
            centerX - mc.fontRenderer.getStringWidth(subtitle) / 2,
            55,
            0xAAAAAA,
            false
        );

        String info = "§7Click a card to vote";
        mc.fontRenderer.drawString(
            info,
            centerX - mc.fontRenderer.getStringWidth(info) / 2,
            height - 30,
            0x888888,
            false
        );

        super.drawScreen(mouseX, mouseY, partialTicks);

        if (voted) {
            drawRect(0, 0, width, height, 0x4400FF00);
            String selectedText = "§aYOU VOTED!";
            mc.fontRenderer.drawString(
                selectedText,
                centerX - mc.fontRenderer.getStringWidth(selectedText) / 2,
                height - 60,
                0x55FF55,
                true
            );
        }
    }

    @Override
    protected void actionPerformed(net.minecraft.client.gui.GuiButton button) throws IOException {
        if (button instanceof CardVoteButton && !voted) {
            CardVoteButton voteButton = (CardVoteButton) button;
            int cardIndex = voteButton.id;

            Card selectedCard = cards.get(cardIndex);

            CardVotingManager.castVote(player, cardIndex);
            voted = true;

            player.sendMessage(new TextComponentString(
                "You voted for: " + selectedCard.getName()
            ));

            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {}
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    mc.displayGuiScreen(null);
                });
            }).start();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if (!voted && !cards.isEmpty()) {
            int randomChoice = (int) (Math.random() * cards.size());
            CardVotingManager.castVote(player, randomChoice);
            player.sendMessage(new TextComponentString(
                "Random vote for: " + cards.get(randomChoice).getName()
            ));
        }
    }
}
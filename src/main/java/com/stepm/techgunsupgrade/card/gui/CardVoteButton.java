package com.stepm.techgunsupgrade.card.gui;

import com.stepm.techgunsupgrade.card.Card;
import com.stepm.techgunsupgrade.card.CardRarity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.translation.I18n;

public class CardVoteButton extends GuiButton {

    private final Card card;
    private final GuiCardChoice parent;
    private int animTime = 0;

    public CardVoteButton(int id, int x, int y, int width, int height, Card card, GuiCardChoice parent) {
        super(id, x, y, width, height, "");
        this.card = card;
        this.parent = parent;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        animTime++;
        boolean hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

        float scale = 1.0f;
        if (hovered) {
            scale = 1.05f + (float)(Math.sin(animTime / 10.0) * 0.02);
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(this.x + this.width / 2, this.y + this.height / 2, 0);
        GlStateManager.scale(scale, scale, 1);
        GlStateManager.translate(-this.width / 2, -this.height / 2, 0);

        int bgColor = card.getRarity().getColor();
        int alpha = 220;

        drawRect(3, 3, this.width + 3, this.height + 3, 0x88000000);

        drawRect(0, 0, this.width, this.height, 0xCC111122);
        drawRect(2, 2, this.width - 2, this.height - 2, (alpha << 24) | (bgColor & 0xFFFFFF));

        drawRect(0, 0, this.width, 3, (0xFF << 24) | (bgColor & 0xFFFFFF));
        drawRect(0, this.height - 3, this.width, this.height, (0xFF << 24) | (bgColor & 0xFFFFFF));
        drawRect(0, 0, 3, this.height, (0xFF << 24) | (bgColor & 0xFFFFFF));
        drawRect(this.width - 3, 0, this.width, this.height, (0xFF << 24) | (bgColor & 0xFFFFFF));

        if (hovered) {
            drawRect(0, 0, this.width, this.height, 0x44FFFFFF);
        }

        String rarityText = "§" + getRarityColor() + card.getRarity().getName();
        mc.fontRenderer.drawString(
            rarityText,
            (this.width - mc.fontRenderer.getStringWidth(rarityText)) / 2,
            10,
            card.getRarity().getColor(),
            true
        );

        String name = card.getName();
        mc.fontRenderer.drawString(
            name,
            (this.width - mc.fontRenderer.getStringWidth(name)) / 2,
            35,
            0xFFFFFF,
            true
        );

        drawRect(this.width / 2 - 40, 52, this.width / 2 + 40, 53, 0x66FFFFFF);

        String description = card.getDescription();
        String[] words = description.split(" ");
        StringBuilder line = new StringBuilder();
        int lineY = 65;
        int maxWidth = this.width - 20;

        for (String word : words) {
            if (mc.fontRenderer.getStringWidth(line + " " + word) > maxWidth) {
                mc.fontRenderer.drawString(
                    line.toString(),
                    (this.width - mc.fontRenderer.getStringWidth(line.toString())) / 2,
                    lineY,
                    0xCCCCCC,
                    false
                );
                line = new StringBuilder(word);
                lineY += 12;
            } else {
                if (line.length() > 0) line.append(" ");
                line.append(word);
            }
        }
        if (line.length() > 0) {
            mc.fontRenderer.drawString(
                line.toString(),
                (this.width - mc.fontRenderer.getStringWidth(line.toString())) / 2,
                lineY,
                0xCCCCCC,
                false
            );
        }

        String valueText = "§6" + (card.isPositive() ? "+" : "") + card.getValue() + "%";
        mc.fontRenderer.drawString(
            valueText,
            (this.width - mc.fontRenderer.getStringWidth(valueText)) / 2,
            this.height - 45,
            card.isPositive() ? 0x55FF55 : 0xFF5555,
            true
        );

        String typeKey = card.isPositive() ? "gui.card.positive" : "gui.card.negative";
        String typeText = (card.isPositive() ? "§a" : "§c")
            + (I18n.canTranslate(typeKey) ? I18n.translateToLocal(typeKey) : typeKey);
        mc.fontRenderer.drawString(
            typeText,
            (this.width - mc.fontRenderer.getStringWidth(typeText)) / 2,
            this.height - 28,
            card.isPositive() ? 0x55FF55 : 0xFF5555,
            false
        );

        String voteKey = "gui.card.vote";
        String voteText = "§6" + (I18n.canTranslate(voteKey) ? I18n.translateToLocal(voteKey) : voteKey);
        mc.fontRenderer.drawString(
            voteText,
            (this.width - mc.fontRenderer.getStringWidth(voteText)) / 2,
            this.height - 15,
            0xFFAA00,
            false
        );

        GlStateManager.popMatrix();
    }

    private String getRarityColor() {
        if (card.getRarity() == CardRarity.COMMON) return "a";
        if (card.getRarity() == CardRarity.UNCOMMON) return "b";
        if (card.getRarity() == CardRarity.EPIC) return "d";
        if (card.getRarity() == CardRarity.LEGENDARY) return "6";
        return "c";
    }
}
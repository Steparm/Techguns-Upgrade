package com.stepm.techgunsupgrade.card;

import net.minecraft.util.text.translation.I18n;

public enum CardRarity {
    COMMON("common", 0x55FF55, 0.50f),
    UNCOMMON("uncommon", 0x55FFFF, 0.25f),
    EPIC("epic", 0xAA55FF, 0.15f),
    LEGENDARY("legendary", 0xFFAA00, 0.08f),
    MYTHIC("mythic", 0xFF5555, 0.02f);

    private final String key;
    private final int color;
    private final float chance;

    CardRarity(String key, int color, float chance) {
        this.key = key;
        this.color = color;
        this.chance = chance;
    }

    public String getName() {
        String translationKey = "card.rarity." + key;
        return I18n.canTranslate(translationKey) ? I18n.translateToLocal(translationKey) : key;
    }

    public int getColor() { return color; }
    public float getChance() { return chance; }

    public static CardRarity getRandomForWave(int wave) {
        float bonus = Math.min(0.3f, wave / 100.0f);
        float roll = (float) Math.random();

        if (roll < 0.02f + bonus * 0.05f) return MYTHIC;
        if (roll < 0.08f + bonus * 0.1f) return LEGENDARY;
        if (roll < 0.15f + bonus * 0.2f) return EPIC;
        if (roll < 0.25f + bonus * 0.3f) return UNCOMMON;
        return COMMON;
    }
}
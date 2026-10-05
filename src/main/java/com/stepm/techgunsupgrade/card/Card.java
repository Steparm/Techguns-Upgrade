package com.stepm.techgunsupgrade.card;

import net.minecraft.util.text.translation.I18n;

public class Card {
    private final String id;
    private final CardRarity rarity;
    private final CardType type;
    private final int value;
    private final boolean isPositive;

    public Card(String id, CardRarity rarity, CardType type, int value) {
        this.id = id;
        this.rarity = rarity;
        this.type = type;
        this.value = value;
        this.isPositive = type.isPositive();
    }

    public String getId() { return id; }
    public CardRarity getRarity() { return rarity; }
    public CardType getType() { return type; }
    public int getValue() { return value; }
    public boolean isPositive() { return isPositive; }

    public String getName() {
        String key = "card.type." + type.name().toLowerCase() + ".name";
        String translated = I18n.canTranslate(key) ? I18n.translateToLocal(key)
                : type.name().toLowerCase();
        return rarity.getName() + " " + translated;
    }

    public String getDescription() {
        String key = "card.type." + type.name().toLowerCase() + ".desc";
        if (!I18n.canTranslate(key)) return type.name().toLowerCase();
        return I18n.translateToLocalFormatted(key, value);
    }

    public static Card createRandom(CardRarity rarity, int wave) {
        boolean isPositive = Math.random() < 0.6;
        CardType type = isPositive ? CardType.getRandomPositive() : CardType.getRandomNegative();

        int baseValue = 5 + (int)(Math.random() * 15);
        int waveBonus = wave / 10;

        float rarityMultiplier = 1.0f;
        if (rarity == CardRarity.UNCOMMON) rarityMultiplier = 1.5f;
        else if (rarity == CardRarity.EPIC) rarityMultiplier = 2.0f;
        else if (rarity == CardRarity.LEGENDARY) rarityMultiplier = 3.0f;
        else if (rarity == CardRarity.MYTHIC) rarityMultiplier = 5.0f;

        int finalValue = (int)((baseValue + waveBonus) * rarityMultiplier);

        return new Card(
            "card_" + System.currentTimeMillis() + "_" + Math.random(),
            rarity,
            type,
            finalValue
        );
    }
}
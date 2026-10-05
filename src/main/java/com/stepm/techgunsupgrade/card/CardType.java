package com.stepm.techgunsupgrade.card;

import net.minecraft.util.text.translation.I18n;

public enum CardType {
    DAMAGE_BONUS("damage_bonus", true),
    HEALTH_BONUS("health_bonus", true),
    REGENERATION("regeneration", true),
    SPEED_BONUS("speed_bonus", true),
    ARMOR_BONUS("armor_bonus", true),
    LIFE_STEAL("life_steal", true),
    CRIT_CHANCE("crit_chance", true),
    FIRE_RATE("fire_rate", true),

    DAMAGE_DEBUFF("damage_debuff", false),
    HEALTH_DEBUFF("health_debuff", false),
    SLOW("slow", false),
    WEAKNESS("weakness", false),
    POISON("poison", false),
    CURSED("cursed", false);

    private final String key;
    private final boolean isPositive;

    CardType(String key, boolean isPositive) {
        this.key = key;
        this.isPositive = isPositive;
    }

    public String getName() {
        String translationKey = "card.type." + key + ".name";
        return I18n.canTranslate(translationKey) ? I18n.translateToLocal(translationKey) : key;
    }

    public String getDescription() {
        String translationKey = "card.type." + key + ".desc";
        return I18n.canTranslate(translationKey) ? I18n.translateToLocal(translationKey) : key;
    }

    public boolean isPositive() { return isPositive; }

    public static CardType getRandomPositive() {
        CardType[] positives = {
            DAMAGE_BONUS, HEALTH_BONUS, REGENERATION, SPEED_BONUS,
            ARMOR_BONUS, LIFE_STEAL, CRIT_CHANCE, FIRE_RATE
        };
        return positives[(int)(Math.random() * positives.length)];
    }

    public static CardType getRandomNegative() {
        CardType[] negatives = {
            DAMAGE_DEBUFF, HEALTH_DEBUFF, SLOW, WEAKNESS, POISON, CURSED
        };
        return negatives[(int)(Math.random() * negatives.length)];
    }
}
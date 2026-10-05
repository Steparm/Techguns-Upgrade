package com.stepm.techgunsupgrade.upgrade;

import net.minecraft.util.text.TextFormatting;

public enum UpgradeRarity {
    COMMON(TextFormatting.WHITE, "common"),
    UNCOMMON(TextFormatting.GREEN, "uncommon"),
    RARE(TextFormatting.BLUE, "rare"),
    EPIC(TextFormatting.DARK_PURPLE, "epic"),
    LEGENDARY(TextFormatting.GOLD, "legendary"),
    MYTHIC(TextFormatting.LIGHT_PURPLE, "mythic"),
    ULTRA_MYTHIC(TextFormatting.RED, "ultra_mythic");

    private final TextFormatting color;
    private final String name;

    UpgradeRarity(TextFormatting color, String name) {
        this.color = color;
        this.name = name;
    }

    public TextFormatting getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public String getLocalizedName() {
        String key = "rarity." + name;
        if (net.minecraft.util.text.translation.I18n.canTranslate(key)) {
            return net.minecraft.util.text.translation.I18n.translateToLocal(key);
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1).replace('_', ' ');
    }
}
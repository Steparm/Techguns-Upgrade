package com.stepm.techgunsupgrade.upgrade;

import com.stepm.techgunsupgrade.config.TguConfig;
import com.stepm.techgunsupgrade.config.UpgradeConfig;
import com.stepm.techgunsupgrade.upgrade.effect.EffectAction;
import com.stepm.techgunsupgrade.upgrade.effect.EffectSpec;
import com.stepm.techgunsupgrade.upgrade.effect.JsonUpgradeDefinitionRegistry;
import com.stepm.techgunsupgrade.upgrade.effect.UpgradeDefinition;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;

import java.lang.reflect.Field;
import java.util.List;

public class UpgradeBuff {
    private final String id;
    private final String displayName;
    private String description;
    private final String translationKey;
    private UpgradeRarity rarity;
    private UpgradeType type;
    private double value;
    private boolean isStacking;
    private int maxStack;
    private boolean configDisabled;

    private UpgradeBuff(Builder builder) {
        this.id = builder.id;
        this.displayName = builder.displayName;
        this.description = builder.description;
        this.translationKey = builder.translationKey;
        this.rarity = builder.rarity;
        this.type = builder.type;
        this.value = builder.value;
        this.isStacking = builder.isStacking;
        this.maxStack = builder.maxStack;
        this.configDisabled = false;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public UpgradeRarity getRarity() { return rarity; }
    public UpgradeType getType() { return type; }
    public double getValue() { return value; }
    public boolean isStacking() { return isStacking; }
    public int getMaxStack() { return maxStack; }
    public boolean isConfigDisabled() { return configDisabled; }

    public String getLocalizedDisplayName() {
        if (configDisabled) {
            return TextFormatting.RED + I18n.translateToLocal("gui.techgunsupgrade.disabled") + " " + displayName;
        }
        String key = "upgrade." + id + ".name";
        if (I18n.canTranslate(key)) {
            return I18n.translateToLocal(key);
        }
        return displayName;
    }

    public String getLocalizedDescription() {
        if (configDisabled) {
            return TextFormatting.RED + I18n.translateToLocal("gui.techgunsupgrade.disabled_desc");
        }

        String key = "upgrade." + id + ".desc";
        if (!I18n.canTranslate(key)) {
            return description;
        }
        String translated = I18n.translateToLocal(key);

        UpgradeDefinition structured = JsonUpgradeDefinitionRegistry.get(id);
        if (structured != null && !structured.getEffects().isEmpty()) {
            List<EffectSpec> effects = structured.getEffects();
            Object[] args = new Object[effects.size()];
            for (int i = 0; i < effects.size(); i++) {
                args[i] = computePercent(effects.get(i));
            }
            try {
                if (isStacking && maxStack > 0) {
                    // If lang has %d for maxStack, append it last
                    Object[] withStack = new Object[args.length + 1];
                    System.arraycopy(args, 0, withStack, 0, args.length);
                    withStack[args.length] = maxStack;
                    return String.format(translated, withStack);
                }
                return String.format(translated, args);
            } catch (Exception e) {
                return translated;
            }
        }

        // Fallback for non-structured upgrades (should never happen).
        int percent = (int) Math.round(value * 100.0 * TguConfig.getTotalMultiplier(type));
        try {
            return String.format(translated, percent);
        } catch (Exception e) {
            return translated;
        }
    }

    private int computePercent(EffectSpec effect) {
        EffectAction action = effect.getAction();
        double value = effect.getValue();
        double mult = TguConfig.getMultiplierForAction(action);

        if (TguConfig.isMultiplicativeBonus(action)) {
            if (TguConfig.isInvertedMultiplier(action)) {
                if (action == EffectAction.FIRE_DELAY_MULTIPLIER
                        || action == EffectAction.CONDITIONAL_FIRE_DELAY_MULTIPLIER
                        || action == EffectAction.MELEE_ATTACK_SPEED_MULTIPLIER
                        || action == EffectAction.CHARGE_TIME_MULTIPLIER
                        || action == EffectAction.SPIN_UP_TIME_MULTIPLIER
                        || action == EffectAction.LOCK_ON_TIME_MULTIPLIER
                        || action == EffectAction.RELOAD_TIME_MULTIPLIER) {
                    // value = 1/(1+bonus). Extract bonus, weaken it, return the percent.
                    double bonus = (1.0 / value) - 1.0;
                    return (int) Math.round(bonus * mult * 100.0);
                }
                // spread/ammo/detection: value = 0.85 → 15%
                double reduction = (1.0 - value);
                return (int) Math.round(reduction * mult * 100.0);
            }
            // DAMAGE/RANGE/RADIUS etc.: value = 1.05 → 5%
            double bonus = value - 1.0;
            return (int) Math.round(bonus * mult * 100.0);
        }

        // Additive (MAGAZINE_ADD, KNOCKBACK_BONUS, durations)
        return (int) Math.round(value * mult);
    }

    public String getLocalizedRarity() {
        String key = "rarity." + rarity.getName().toLowerCase();
        if (I18n.canTranslate(key)) {
            return I18n.translateToLocal(key);
        }
        return rarity.getName();
    }

    public String getLocalizedType() {
        String key = "type." + type.name().toLowerCase();
        if (I18n.canTranslate(key)) {
            return I18n.translateToLocal(key);
        }
        return type.name();
    }

    public String getFullLocalizedTooltip() {
        if (configDisabled) {
            return TextFormatting.RED + I18n.translateToLocal("gui.techgunsupgrade.disabled") + "\n" +
                   TextFormatting.GRAY + I18n.translateToLocal("gui.techgunsupgrade.disabled_desc");
        }
        String rarityColor = rarity.getColor().toString();
        String name = getLocalizedDisplayName();
        String desc = getLocalizedDescription();
        String rarityName = getLocalizedRarity();
        String typeName = getLocalizedType();
        String tooltip = rarityColor + "[" + rarityName + "] " + name + "\n" +
                         TextFormatting.GRAY + desc;
        if (isStacking && maxStack > 0) {
            tooltip += "\n" + TextFormatting.GOLD + I18n.translateToLocal("gui.techgunsupgrade.max_stack") + ": " + maxStack;
        }
        tooltip += "\n" + TextFormatting.DARK_GRAY + "Type: " + typeName;
        return tooltip;
    }

    public void applyConfigOverrides() {
        if (!UpgradeConfig.isUpgradeEnabled(id)) {
            try {
                Field rarityField = UpgradeBuff.class.getDeclaredField("rarity");
                rarityField.setAccessible(true);
                rarityField.set(this, UpgradeRarity.COMMON);
                Field typeField = UpgradeBuff.class.getDeclaredField("type");
                typeField.setAccessible(true);
                typeField.set(this, UpgradeType.DAMAGE);
                Field valueField = UpgradeBuff.class.getDeclaredField("value");
                valueField.setAccessible(true);
                valueField.set(this, 0.0);
                Field maxStackField = UpgradeBuff.class.getDeclaredField("maxStack");
                maxStackField.setAccessible(true);
                maxStackField.set(this, 0);
                Field configDisabledField = UpgradeBuff.class.getDeclaredField("configDisabled");
                configDisabledField.setAccessible(true);
                configDisabledField.set(this, true);
            } catch (Exception ignored) {}
            return;
        }

        this.configDisabled = false;

        UpgradeConfig.OverrideData override = UpgradeConfig.getOverride(id);
        if (override != null) {
            try {
                Field rarityField = UpgradeBuff.class.getDeclaredField("rarity");
                rarityField.setAccessible(true);
                rarityField.set(this, override.rarity);
                Field typeField = UpgradeBuff.class.getDeclaredField("type");
                typeField.setAccessible(true);
                typeField.set(this, override.type);
                Field valueField = UpgradeBuff.class.getDeclaredField("value");
                valueField.setAccessible(true);
                valueField.set(this, override.value);
                Field maxStackField = UpgradeBuff.class.getDeclaredField("maxStack");
                maxStackField.setAccessible(true);
                maxStackField.set(this, override.maxStack);
                if (override.maxStack > 0) {
                    Field isStackingField = UpgradeBuff.class.getDeclaredField("isStacking");
                    isStackingField.setAccessible(true);
                    isStackingField.set(this, true);
                }
            } catch (Exception ignored) {}
        }
    }

    @Override
    public String toString() {
        return "UpgradeBuff{" +
                "id='" + id + '\'' +
                ", displayName='" + displayName + '\'' +
                ", rarity=" + rarity +
                ", type=" + type +
                ", value=" + value +
                ", isStacking=" + isStacking +
                ", maxStack=" + maxStack +
                ", configDisabled=" + configDisabled +
                '}';
    }

    public static class Builder {
        private String id;
        private String displayName;
        private String description = "";
        private String translationKey = "";
        private UpgradeRarity rarity;
        private UpgradeType type;
        private double value = 0;
        private boolean isStacking = false;
        private int maxStack = 0;

        public Builder(String id, String displayName, UpgradeRarity rarity, UpgradeType type) {
            this.id = id;
            this.displayName = displayName;
            this.rarity = rarity;
            this.type = type;
            this.translationKey = "upgrade." + id;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder value(double value) {
            this.value = value;
            return this;
        }

        public Builder stacking(int maxStack) {
            this.isStacking = true;
            this.maxStack = maxStack;
            return this;
        }

        public Builder translationKey(String translationKey) {
            this.translationKey = translationKey;
            return this;
        }

        public UpgradeBuff build() {
            return new UpgradeBuff(this);
        }
    }
}
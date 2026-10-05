package com.stepm.techgunsupgrade.config;

import com.stepm.techgunsupgrade.TechgunsUpgradeMod;
import com.stepm.techgunsupgrade.upgrade.UpgradeRarity;
import com.stepm.techgunsupgrade.upgrade.UpgradeType;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = TechgunsUpgradeMod.MODID)
public class UpgradeConfig {

    @Config(modid = TechgunsUpgradeMod.MODID, name = "techgunsupgrade/upgrades")
    public static class Upgrades {
        @Config.Comment("Upgrade stat overrides. Format: upgrade_id:rarity:type:value:maxStack")
        @Config.Name("upgrade_overrides")
        public static String[] upgradeOverrides = new String[0];

        @Config.Comment("Enable/disable individual upgrades. Format: upgrade_id:true/false")
        @Config.Name("upgrade_enabled")
        public static String[] upgradeEnabled = new String[0];
    }

    private static final Map<String, OverrideData> overrides = new HashMap<>();
    private static final Map<String, Boolean> enabled = new HashMap<>();

    public static void loadConfig() {
        overrides.clear();
        enabled.clear();

        for (String entry : Upgrades.upgradeOverrides) {
            if (entry == null || entry.trim().isEmpty()) continue;
            String[] parts = entry.split(":");
            if (parts.length >= 4) {
                String id = parts[0].trim();
                try {
                    UpgradeRarity rarity = UpgradeRarity.valueOf(parts[1].trim().toUpperCase());
                    UpgradeType type = UpgradeType.valueOf(parts[2].trim().toUpperCase());
                    double value = Double.parseDouble(parts[3].trim());
                    int maxStack = parts.length >= 5 ? Integer.parseInt(parts[4].trim()) : 0;
                    overrides.put(id, new OverrideData(rarity, type, value, maxStack));
                } catch (Exception e) {
                    TechgunsUpgradeMod.LOGGER.warn("Invalid override entry: " + entry);
                }
            }
        }

        for (String entry : Upgrades.upgradeEnabled) {
            if (entry == null || entry.trim().isEmpty()) continue;
            String[] parts = entry.split(":");
            if (parts.length == 2) {
                String id = parts[0].trim();
                boolean value = Boolean.parseBoolean(parts[1].trim());
                enabled.put(id, value);
            }
        }
    }

    public static boolean isUpgradeEnabled(String id) {
        if (enabled.containsKey(id)) {
            return enabled.get(id);
        }
        return true;
    }

    public static OverrideData getOverride(String id) {
        return overrides.get(id);
    }

    public static class OverrideData {
        public final UpgradeRarity rarity;
        public final UpgradeType type;
        public final double value;
        public final int maxStack;

        public OverrideData(UpgradeRarity rarity, UpgradeType type, double value, int maxStack) {
            this.rarity = rarity;
            this.type = type;
            this.value = value;
            this.maxStack = maxStack;
        }
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(TechgunsUpgradeMod.MODID)) {
            ConfigManager.sync(TechgunsUpgradeMod.MODID, Config.Type.INSTANCE);
            loadConfig();
        }
    }
}
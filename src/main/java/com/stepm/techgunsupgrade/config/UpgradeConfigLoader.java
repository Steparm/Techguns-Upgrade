package com.stepm.techgunsupgrade.config;

import com.stepm.techgunsupgrade.upgrade.UpgradeBuff;
import com.stepm.techgunsupgrade.upgrade.WeaponUpgrades;

import java.util.List;
import java.util.Map;

public class UpgradeConfigLoader {

    public static void applyConfigToUpgrades() {
        UpgradeConfig.loadConfig();

        Map<String, List<UpgradeBuff>> allUpgrades = WeaponUpgrades.getAllWeaponUpgrades();

        for (Map.Entry<String, List<UpgradeBuff>> entry : allUpgrades.entrySet()) {
            for (UpgradeBuff buff : entry.getValue()) {
                buff.applyConfigOverrides();
            }
        }
    }
}
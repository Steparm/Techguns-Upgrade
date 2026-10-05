package com.stepm.techgunsupgrade.upgrade;

import com.stepm.techgunsupgrade.TechgunsUpgradeMod;
import com.stepm.techgunsupgrade.upgrade.effect.CommonUpgradeDefinitions;
import com.stepm.techgunsupgrade.upgrade.effect.UncommonUpgradeDefinitions;
import com.stepm.techgunsupgrade.upgrade.effect.RareUpgradeDefinitions;
import com.stepm.techgunsupgrade.upgrade.effect.EpicUpgradeDefinitions;
import com.stepm.techgunsupgrade.upgrade.effect.LegendaryUpgradeDefinitions;
import com.stepm.techgunsupgrade.upgrade.effect.MythicUpgradeDefinitions;
import com.stepm.techgunsupgrade.upgrade.effect.UltraMythicUpgradeDefinitions;
import com.stepm.techgunsupgrade.upgrade.effect.JsonUpgradeDefinitionRegistry;

public class UpgradeRegistry {

    public static void init() {
        WeaponUpgrades.registerAll();
        CommonUpgradeDefinitions.init();
        UncommonUpgradeDefinitions.init();
        RareUpgradeDefinitions.init();
        EpicUpgradeDefinitions.init();
        LegendaryUpgradeDefinitions.init();
        MythicUpgradeDefinitions.init();
        UltraMythicUpgradeDefinitions.init();
        JsonUpgradeDefinitionRegistry.init();
        TechgunsUpgradeMod.LOGGER.info("Registered weapon upgrades; structured Common coverage: "
                + CommonUpgradeDefinitions.getAll().size() + "/" + CommonUpgradeDefinitions.EXPECTED_COUNT
                + "; Uncommon coverage: " + UncommonUpgradeDefinitions.getAll().size()
                + "/" + UncommonUpgradeDefinitions.EXPECTED_COUNT
                + "; Rare coverage: " + RareUpgradeDefinitions.getAll().size()
                + "/" + RareUpgradeDefinitions.EXPECTED_COUNT
                + "; Epic coverage: " + EpicUpgradeDefinitions.getAll().size()
                + "/" + EpicUpgradeDefinitions.EXPECTED_COUNT
                + "; Legendary coverage: " + LegendaryUpgradeDefinitions.getAll().size()
                + "/" + LegendaryUpgradeDefinitions.EXPECTED_COUNT
                + "; Mythic coverage: " + MythicUpgradeDefinitions.getAll().size()
                + "/" + MythicUpgradeDefinitions.EXPECTED_COUNT
                + "; Ultra-Mythic coverage: " + UltraMythicUpgradeDefinitions.getAll().size()
                + "/" + UltraMythicUpgradeDefinitions.EXPECTED_COUNT
                + "; JSON runtime coverage: " + JsonUpgradeDefinitionRegistry.getAll().size()
                + "/839");
    }
}

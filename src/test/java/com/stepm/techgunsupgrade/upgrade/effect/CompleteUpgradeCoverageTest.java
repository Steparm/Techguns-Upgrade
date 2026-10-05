package com.stepm.techgunsupgrade.upgrade.effect;

import com.stepm.techgunsupgrade.upgrade.UpgradeBuff;
import com.stepm.techgunsupgrade.upgrade.UpgradeRarity;
import com.stepm.techgunsupgrade.upgrade.WeaponUpgrades;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CompleteUpgradeCoverageTest {
    @Test
    void all839SourceUpgradesHaveStructuredDefinitions() {
        WeaponUpgrades.registerAll();
        CommonUpgradeDefinitions.init();
        UncommonUpgradeDefinitions.init();
        RareUpgradeDefinitions.init();
        EpicUpgradeDefinitions.init();
        LegendaryUpgradeDefinitions.init();
        MythicUpgradeDefinitions.init();
        UltraMythicUpgradeDefinitions.init();

        int sourceCount = 0;
        int definitionCount = CommonUpgradeDefinitions.getAll().size()
                + UncommonUpgradeDefinitions.getAll().size()
                + RareUpgradeDefinitions.getAll().size()
                + EpicUpgradeDefinitions.getAll().size()
                + LegendaryUpgradeDefinitions.getAll().size()
                + MythicUpgradeDefinitions.getAll().size()
                + UltraMythicUpgradeDefinitions.getAll().size();
        for (java.util.List<UpgradeBuff> buffs : WeaponUpgrades.getAllWeaponUpgrades().values()) {
            for (UpgradeBuff buff : buffs) {
                sourceCount++;
                assertNotNull(find(buff), "Missing structured definition: " + buff.getId());
            }
        }
        assertEquals(40, WeaponUpgrades.getAllWeaponUpgrades().size());
        assertEquals(839, sourceCount);
        assertEquals(839, definitionCount);
    }

    private static UpgradeDefinition find(UpgradeBuff buff) {
        UpgradeRarity rarity = buff.getRarity();
        switch (rarity) {
            case COMMON: return CommonUpgradeDefinitions.get(buff.getId());
            case UNCOMMON: return UncommonUpgradeDefinitions.get(buff.getId());
            case RARE: return RareUpgradeDefinitions.get(buff.getId());
            case EPIC: return EpicUpgradeDefinitions.get(buff.getId());
            case LEGENDARY: return LegendaryUpgradeDefinitions.get(buff.getId());
            case MYTHIC: return MythicUpgradeDefinitions.get(buff.getId());
            case ULTRA_MYTHIC: return UltraMythicUpgradeDefinitions.get(buff.getId());
            default: return null;
        }
    }
}

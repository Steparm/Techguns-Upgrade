package com.stepm.techgunsupgrade.upgrade.effect;

import com.stepm.techgunsupgrade.upgrade.UpgradeBuff;
import com.stepm.techgunsupgrade.upgrade.WeaponUpgrades;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExhaustiveUpgradeCombinationTest {

    @BeforeAll
    static void initialize() {
        WeaponUpgrades.registerAll();
        CommonUpgradeDefinitions.init();
        UncommonUpgradeDefinitions.init();
        RareUpgradeDefinitions.init();
        EpicUpgradeDefinitions.init();
        LegendaryUpgradeDefinitions.init();
        MythicUpgradeDefinitions.init();
        UltraMythicUpgradeDefinitions.init();
    }

    @Test
    void everyCompatiblePairIsOrderIndependent() {
        int pairs = 0;
        List<String> orderDependent = new ArrayList<>();
        for (Map.Entry<String, List<UpgradeBuff>> weapon
                : WeaponUpgrades.getAllWeaponUpgrades().entrySet()) {
            List<UpgradeBuff> buffs = weapon.getValue();
            for (int first = 0; first < buffs.size(); first++) {
                for (int second = first + 1; second < buffs.size(); second++) {
                    pairs++;
                    UpgradeDefinition a = definition(buffs.get(first));
                    UpgradeDefinition b = definition(buffs.get(second));
                    assertNotNull(a);
                    assertNotNull(b);

                    NBTTagCompound forward = new NBTTagCompound();
                    StructuredEffectApplier.apply(forward, a);
                    StructuredEffectApplier.apply(forward, b);
                    NBTTagCompound reverse = new NBTTagCompound();
                    StructuredEffectApplier.apply(reverse, b);
                    StructuredEffectApplier.apply(reverse, a);
                    if (!forward.equals(reverse)) {
                        orderDependent.add(buffs.get(first).getId() + " + "
                                + buffs.get(second).getId() + " => " + forward + " != " + reverse);
                    }
                }
            }
        }
        assertEquals(8380, pairs);
        assertTrue(orderDependent.isEmpty(), String.join("\n", orderDependent));
    }

    private static UpgradeDefinition definition(UpgradeBuff buff) {
        switch (buff.getRarity()) {
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

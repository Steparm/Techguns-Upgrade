package com.stepm.techgunsupgrade.upgrade.effect;

import com.stepm.techgunsupgrade.upgrade.UpgradeBuff;
import com.stepm.techgunsupgrade.upgrade.UpgradeRarity;
import com.stepm.techgunsupgrade.upgrade.WeaponUpgrades;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommonUpgradeDefinitionsTest {
    @BeforeAll
    static void initialize() {
        WeaponUpgrades.registerAll();
        CommonUpgradeDefinitions.init();
    }

    @Test
    void coversEveryCommonUpgradeExactlyOnce() {
        Map<String, UpgradeDefinition> definitions = CommonUpgradeDefinitions.getAll();
        assertEquals(CommonUpgradeDefinitions.EXPECTED_COUNT, definitions.size());

        Set<String> sourceIds = new HashSet<>();
        int weaponCount = 0;
        for (Map.Entry<String, List<UpgradeBuff>> entry
                : WeaponUpgrades.getAllWeaponUpgrades().entrySet()) {
            int commonForWeapon = 0;
            for (UpgradeBuff buff : entry.getValue()) {
                if (buff.getRarity() != UpgradeRarity.COMMON) continue;
                commonForWeapon++;
                assertTrue(sourceIds.add(buff.getId()), "Duplicate source ID: " + buff.getId());
                UpgradeDefinition definition = definitions.get(buff.getId());
                assertNotNull(definition, "Missing definition: " + buff.getId());
                assertEquals(entry.getKey(), definition.getWeaponId());
                assertEquals(buff.getDescription(), definition.getDescription());
                assertFalse(definition.getEffects().isEmpty());
            }
            assertEquals(4, commonForWeapon, "Unexpected Common count for " + entry.getKey());
            weaponCount++;
        }
        assertEquals(40, weaponCount);
        assertEquals(sourceIds, definitions.keySet());
    }

    @Test
    void compoundCommonDescriptionsAreSplitIntoIndependentEffects() {
        assertEquals(2, CommonUpgradeDefinitions.get("hmg_rusty_barrel").getEffects().size());
        assertEquals(2, CommonUpgradeDefinitions.get("db_short_barrel").getEffects().size());
        assertEquals(2, CommonUpgradeDefinitions.get("cs_short_barrel").getEffects().size());
    }

    @Test
    void allDeclaredEffectsCarryTraceableDescriptionFragments() {
        for (UpgradeDefinition definition : CommonUpgradeDefinitions.getAll().values()) {
            for (EffectSpec effect : definition.getEffects()) {
                assertNotNull(effect.getTrigger());
                assertNotNull(effect.getAction());
                assertNotNull(effect.getCondition());
                assertFalse(effect.getSourceFragment().trim().isEmpty());
                assertTrue(Double.isFinite(effect.getValue()));
            }
        }
    }

    @Test
    void everyCommonDefinitionCanBeAppliedWithoutAnUnsupportedAction() {
        for (UpgradeDefinition definition : CommonUpgradeDefinitions.getAll().values()) {
            NBTTagCompound tag = new NBTTagCompound();
            StructuredEffectApplier.apply(tag, definition);
            assertFalse(tag.getKeySet().isEmpty(),
                    "Definition wrote no runtime data: " + definition.getId());
        }
    }

    @Test
    void compoundAndSpecialEffectsWriteExpectedRuntimeValues() {
        NBTTagCompound rusty = new NBTTagCompound();
        StructuredEffectApplier.apply(rusty, CommonUpgradeDefinitions.get("hmg_rusty_barrel"));
        assertEquals(1.10f, rusty.getFloat("mod_damage"), 0.0001f);
        assertEquals(0.05f, rusty.getFloat("common_jam_chance"), 0.0001f);

        NBTTagCompound shortBarrel = new NBTTagCompound();
        StructuredEffectApplier.apply(shortBarrel, CommonUpgradeDefinitions.get("db_short_barrel"));
        assertEquals(0.80f, shortBarrel.getFloat("mod_range"), 0.0001f);
        assertEquals(1.15f, shortBarrel.getFloat("common_close_damage"), 0.0001f);
        assertEquals(5.0f, shortBarrel.getFloat("common_close_damage_distance"), 0.0001f);

        NBTTagCompound drillTank = new NBTTagCompound();
        StructuredEffectApplier.apply(drillTank, CommonUpgradeDefinitions.get("drill_big_tank"));
        assertEquals(1.25f, drillTank.getFloat("mod_clipsize_mult"), 0.0001f);

        NBTTagCompound lockOn = new NBTTagCompound();
        StructuredEffectApplier.apply(lockOn, CommonUpgradeDefinitions.get("lor_scope"));
        assertEquals(0.85f, lockOn.getFloat("mod_lock_on_time"), 0.0001f);
    }
}

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

class RareUpgradeDefinitionsTest {
    @BeforeAll
    static void initialize() {
        WeaponUpgrades.registerAll();
        RareUpgradeDefinitions.init();
    }

    @Test
    void coversEveryRareUpgradeExactlyOnce() {
        Map<String, UpgradeDefinition> definitions = RareUpgradeDefinitions.getAll();
        assertEquals(RareUpgradeDefinitions.EXPECTED_COUNT, definitions.size());
        Set<String> sourceIds = new HashSet<>();
        int weaponCount = 0;
        for (Map.Entry<String, List<UpgradeBuff>> entry : WeaponUpgrades.getAllWeaponUpgrades().entrySet()) {
            int count = 0;
            for (UpgradeBuff buff : entry.getValue()) {
                if (buff.getRarity() != UpgradeRarity.RARE) continue;
                count++;
                assertTrue(sourceIds.add(buff.getId()), "Duplicate source ID: " + buff.getId());
                UpgradeDefinition definition = definitions.get(buff.getId());
                assertNotNull(definition, "Missing definition: " + buff.getId());
                assertEquals(entry.getKey(), definition.getWeaponId());
                assertEquals(buff.getDescription(), definition.getDescription());
            }
            assertEquals(4, count, "Unexpected Rare count for " + entry.getKey());
            weaponCount++;
        }
        assertEquals(40, weaponCount);
        assertEquals(sourceIds, definitions.keySet());
    }

    @Test
    void everyRareDefinitionWritesExecutableData() {
        for (UpgradeDefinition definition : RareUpgradeDefinitions.getAll().values()) {
            NBTTagCompound tag = new NBTTagCompound();
            StructuredEffectApplier.apply(tag, definition);
            assertFalse(tag.getKeySet().isEmpty(), "No runtime data: " + definition.getId());
        }
    }

    @Test
    void representativeComplexEffectsHaveExactValues() {
        NBTTagCompound triple = apply("hmg_triple_shot");
        assertEquals(1.0f, triple.getFloat("random_extra_projectile_chance"), 0.0001f);
        assertEquals(2, triple.getInteger("random_extra_projectile_count"));
        assertEquals(3, triple.getInteger("random_extra_ammo_cost"));
        assertFalse(triple.hasKey("mod_ammo_consumption"));
        assertEquals(3, triple.getCompoundTag("random_extra_projectile_specs")
                .getCompoundTag("hmg_triple_shot").getInteger("ammo_cost"));

        NBTTagCompound tactical = apply("baz_tactical");
        assertEquals(1.2f, tactical.getFloat("mod_damage"), 0.0001f);
        assertEquals(1.2f, tactical.getFloat("mod_explosion_radius"), 0.0001f);

        NBTTagCompound drill = apply("drill_wide");
        assertEquals(5, drill.getInteger("mining_area_size"));
    }

    private static NBTTagCompound apply(String id) {
        NBTTagCompound tag = new NBTTagCompound();
        StructuredEffectApplier.apply(tag, RareUpgradeDefinitions.get(id));
        return tag;
    }
}

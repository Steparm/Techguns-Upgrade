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

class LegendaryUpgradeDefinitionsTest {
    @BeforeAll
    static void initialize() {
        WeaponUpgrades.registerAll();
        LegendaryUpgradeDefinitions.init();
    }

    @Test
    void coversEveryLegendaryUpgradeExactlyOnce() {
        Map<String, UpgradeDefinition> definitions = LegendaryUpgradeDefinitions.getAll();
        assertEquals(119, definitions.size());
        Set<String> sourceIds = new HashSet<>();
        int weaponCount = 0;
        for (Map.Entry<String, List<UpgradeBuff>> entry
                : WeaponUpgrades.getAllWeaponUpgrades().entrySet()) {
            int count = 0;
            for (UpgradeBuff buff : entry.getValue()) {
                if (buff.getRarity() != UpgradeRarity.LEGENDARY) continue;
                count++;
                assertTrue(sourceIds.add(buff.getId()), "Duplicate source ID: " + buff.getId());
                UpgradeDefinition definition = definitions.get(buff.getId());
                assertNotNull(definition, "Missing definition: " + buff.getId());
                assertEquals(entry.getKey(), definition.getWeaponId());
                assertEquals(buff.getDescription(), definition.getDescription());
            }
            int expected = "techguns:handcannon".equals(entry.getKey()) ? 2 : 3;
            assertEquals(expected, count, "Unexpected Legendary count for " + entry.getKey());
            weaponCount++;
        }
        assertEquals(40, weaponCount);
        assertEquals(sourceIds, definitions.keySet());
    }

    @Test
    void everyLegendaryDefinitionWritesExecutableData() {
        for (UpgradeDefinition definition : LegendaryUpgradeDefinitions.getAll().values()) {
            NBTTagCompound tag = new NBTTagCompound();
            StructuredEffectApplier.apply(tag, definition);
            assertFalse(tag.getKeySet().isEmpty(), "No runtime data: " + definition.getId());
        }
    }

    @Test
    void representativeLegendaryEffectsHaveExactValues() {
        NBTTagCompound critical = apply("hmg_critical_miss");
        assertEquals(0.25f, critical.getFloat("random_damage_chance"), 0.0001f);
        assertEquals(3.50f, critical.getFloat("random_damage_multiplier"), 0.0001f);
        assertEquals(0.05f, critical.getFloat("self_explosion_chance"), 0.0001f);
        assertEquals(2.0f, critical.getFloat("self_explosion_power"), 0.0001f);

        NBTTagCompound harvest = apply("gr_gold_harvest");
        assertEquals(6.0f, harvest.getFloat("kill_heal_amount"), 0.0001f);

        NBTTagCompound chainsaw = apply("chain_monster");
        assertEquals(1.50f, chainsaw.getFloat("mod_damage"), 0.0001f);
        assertEquals(1.40f, chainsaw.getFloat("mod_melee_attack_speed"), 0.0001f);
        assertEquals(1.30f, chainsaw.getFloat("mod_ammo_consumption"), 0.0001f);

        NBTTagCompound endless = apply("grf_endless");
        assertEquals(0.25f, endless.getFloat("ammo_save_chance"), 0.0001f);

        NBTTagCompound fist = apply("pf_endless_charge");
        assertTrue(fist.getBoolean("instant_charge"));
    }

    private static NBTTagCompound apply(String id) {
        NBTTagCompound tag = new NBTTagCompound();
        StructuredEffectApplier.apply(tag, LegendaryUpgradeDefinitions.get(id));
        return tag;
    }
}

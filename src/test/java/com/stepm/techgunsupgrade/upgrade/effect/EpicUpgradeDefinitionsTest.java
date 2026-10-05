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

class EpicUpgradeDefinitionsTest {
    @BeforeAll
    static void initialize() {
        WeaponUpgrades.registerAll();
        EpicUpgradeDefinitions.init();
    }

    @Test
    void coversEveryEpicUpgradeExactlyOnce() {
        Map<String, UpgradeDefinition> definitions = EpicUpgradeDefinitions.getAll();
        assertEquals(120, definitions.size());
        Set<String> sourceIds = new HashSet<>();
        int weaponCount = 0;
        for (Map.Entry<String, List<UpgradeBuff>> entry : WeaponUpgrades.getAllWeaponUpgrades().entrySet()) {
            int count = 0;
            for (UpgradeBuff buff : entry.getValue()) {
                if (buff.getRarity() != UpgradeRarity.EPIC) continue;
                count++;
                assertTrue(sourceIds.add(buff.getId()), "Duplicate source ID: " + buff.getId());
                UpgradeDefinition definition = definitions.get(buff.getId());
                assertNotNull(definition, "Missing definition: " + buff.getId());
                assertEquals(entry.getKey(), definition.getWeaponId());
                assertEquals(buff.getDescription(), definition.getDescription());
            }
            assertEquals(3, count, "Unexpected Epic count for " + entry.getKey());
            weaponCount++;
        }
        assertEquals(40, weaponCount);
        assertEquals(sourceIds, definitions.keySet());
    }

    @Test
    void everyEpicDefinitionWritesExecutableData() {
        for (UpgradeDefinition definition : EpicUpgradeDefinitions.getAll().values()) {
            NBTTagCompound tag = new NBTTagCompound();
            StructuredEffectApplier.apply(tag, definition);
            assertFalse(tag.getKeySet().isEmpty(), "No runtime data: " + definition.getId());
        }
    }

    @Test
    void makeshiftShotgunPreservesTotalDamageAndHeavyStrikeSlowsAttack() {
        NBTTagCompound shotgun = new NBTTagCompound();
        StructuredEffectApplier.apply(shotgun, EpicUpgradeDefinitions.get("hmg_shotgun"));
        assertEquals(4, shotgun.getInteger("mod_bulletcount"));
        assertEquals(5, shotgun.getInteger("fan_projectile_count"));
        assertEquals(0.20f, shotgun.getFloat("mod_projectile_damage"), 0.0001f);
        assertEquals(1.0f, 5.0f * shotgun.getFloat("mod_projectile_damage"), 0.0001f);

        NBTTagCompound heavyStrike = new NBTTagCompound();
        StructuredEffectApplier.apply(heavyStrike,
                EpicUpgradeDefinitions.get("chain_heavy_strike"));
        assertEquals(1.50f, heavyStrike.getFloat("mod_melee_damage"), 0.0001f);
        assertEquals(0.85f, heavyStrike.getFloat("mod_melee_attack_speed"), 0.0001f);
    }

    @Test
    void representativeComplexEffectsHaveExactValues() {
        NBTTagCompound hail = apply("lmg_long_burst");
        assertEquals(30, hail.getInteger("burst_damage_threshold"));
        assertEquals(1.25f, hail.getFloat("burst_damage_multiplier"), 0.0001f);
        assertTrue(hail.getBoolean("burst_damage_until_reload"));

        NBTTagCompound nuclear = apply("bfg_nuclear");
        assertEquals(2.5f, nuclear.getFloat("charged_damage_multiplier"), 0.0001f);
        assertEquals(1.5f, nuclear.getFloat("charged_radius_multiplier"), 0.0001f);

        NBTTagCompound ramp = apply("ad_deadly_radiation");
        assertEquals(0.10f, ramp.getFloat("damage_ramp_step"), 0.0001f);
        assertEquals(40, ramp.getInteger("damage_ramp_interval"));
        assertEquals(2.0f, ramp.getFloat("damage_ramp_max"), 0.0001f);
    }

    private static NBTTagCompound apply(String id) {
        NBTTagCompound tag = new NBTTagCompound();
        StructuredEffectApplier.apply(tag, EpicUpgradeDefinitions.get(id));
        return tag;
    }
}

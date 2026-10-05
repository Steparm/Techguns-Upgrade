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

class UltraMythicUpgradeDefinitionsTest {
    @BeforeAll
    static void initialize() {
        WeaponUpgrades.registerAll();
        UltraMythicUpgradeDefinitions.init();
    }

    @Test
    void coversEveryUltraMythicUpgradeExactlyOnce() {
        Map<String, UpgradeDefinition> definitions = UltraMythicUpgradeDefinitions.getAll();
        assertEquals(UltraMythicUpgradeDefinitions.EXPECTED_COUNT, definitions.size());
        Set<String> sourceIds = new HashSet<>();
        int weaponCount = 0;
        for (Map.Entry<String, List<UpgradeBuff>> entry
                : WeaponUpgrades.getAllWeaponUpgrades().entrySet()) {
            int count = 0;
            for (UpgradeBuff buff : entry.getValue()) {
                if (buff.getRarity() != UpgradeRarity.ULTRA_MYTHIC) continue;
                count++;
                assertTrue(sourceIds.add(buff.getId()), "Duplicate source ID: " + buff.getId());
                UpgradeDefinition definition = definitions.get(buff.getId());
                assertNotNull(definition, "Missing definition: " + buff.getId());
                assertEquals(entry.getKey(), definition.getWeaponId());
                assertEquals(buff.getDescription(), definition.getDescription());
            }
            assertEquals(1, count, "Unexpected Ultra-Mythic count for " + entry.getKey());
            weaponCount++;
        }
        assertEquals(40, weaponCount);
        assertEquals(sourceIds, definitions.keySet());
    }

    @Test
    void everyDefinitionWritesExecutableRuntimeData() {
        for (UpgradeDefinition definition : UltraMythicUpgradeDefinitions.getAll().values()) {
            NBTTagCompound tag = new NBTTagCompound();
            StructuredEffectApplier.apply(tag, definition);
            assertFalse(tag.getKeySet().isEmpty(), "No runtime data: " + definition.getId());
            if (!"pf_fist_of_god".equals(definition.getId())) {
                assertTrue(tag.getBoolean("unlimited_magazine"),
                        "Missing declared infinite ammunition: " + definition.getId());
            }
        }
    }

    @Test
    void staticAndPeriodicValuesAreExact() {
        NBTTagCompound apocalypse = apply("hmg_apocalypse");
        assertEquals(3.0f, apocalypse.getFloat("mod_damage"), 0.0001f);
        assertEquals(2.5f, apocalypse.getFloat("explosion_power"), 0.0001f);
        assertTrue(apocalypse.getBoolean("unlimited_magazine"));

        NBTTagCompound duet = apply("db_hell_duet_ultra");
        assertEquals(20, duet.getInteger("mod_bulletcount_override"));
        assertEquals(100, duet.getInteger("incendiary_duration"));

        NBTTagCompound pistol = apply("pist_legendary");
        assertEquals(3, pistol.getInteger("periodic_shot_interval"));
        assertEquals(6.0f, pistol.getFloat("periodic_shot_damage"), 0.0001f);

        NBTTagCompound p90 = apply("p90_machine_gun");
        assertEquals(5, p90.getInteger("targeted_burst_interval"));
        assertEquals(5, p90.getInteger("targeted_burst_count"));
        assertEquals(0.10f, p90.getFloat("radial_burst_chance"), 0.0001f);
        assertEquals(10, p90.getInteger("radial_burst_count"));
    }

    @Test
    void complexAreaAndChargeEffectsAreExact() {
        NBTTagCompound armageddon = apply("baz_armageddon");
        assertEquals(3.0f, armageddon.getFloat("mod_explosion_radius"), 0.0001f);
        assertEquals(0.10f, armageddon.getFloat("nuclear_impact_chance"), 0.0001f);
        assertEquals(30.0f, armageddon.getFloat("nuclear_impact_radius"), 0.0001f);

        NBTTagCompound artillery = apply("gl_artillery");
        assertEquals(1, artillery.getInteger("extra_impact_explosions"));
        assertEquals(3, artillery.getInteger("artillery_count"));
        assertEquals(0.10f, artillery.getFloat("artillery_chance"), 0.0001f);

        NBTTagCompound bio = apply("bio_bioapocalypse");
        assertEquals(1.0f / 3.0f, bio.getFloat("mod_charge_time"), 0.0001f);
        assertEquals(9, bio.getInteger("charged_extra_projectiles"));
        assertEquals(5.0f, bio.getFloat("acid_cloud_radius"), 0.0001f);
        assertEquals(100, bio.getInteger("acid_cloud_duration"));

        NBTTagCompound nuclear = apply("ad_nuclear_apocalypse");
        assertEquals(0.10f, nuclear.getFloat("timed_zone_chance"), 0.0001f);
        assertEquals(200, nuclear.getInteger("timed_zone_duration"));
        assertEquals(60, nuclear.getInteger("continuous_nuclear_ticks"));
        assertEquals(15.0f, nuclear.getFloat("continuous_nuclear_radius"), 0.0001f);
        assertTrue(nuclear.getBoolean("nuclear_on_kill"));

        NBTTagCompound fireRain = apply("hb_armageddon");
        assertEquals(0.15f, fireRain.getFloat("fire_rain_chance"), 0.0001f);
        assertEquals(10, fireRain.getInteger("fire_rain_count"));
        assertEquals(15.0f, fireRain.getFloat("fire_rain_radius"), 0.0001f);
        assertFalse(fireRain.getBoolean("techguns_lightning"));
    }

    private static NBTTagCompound apply(String id) {
        NBTTagCompound tag = new NBTTagCompound();
        StructuredEffectApplier.apply(tag, UltraMythicUpgradeDefinitions.get(id));
        return tag;
    }
}

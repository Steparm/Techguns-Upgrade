package com.stepm.techgunsupgrade.upgrade.effect;

import com.stepm.techgunsupgrade.upgrade.GunStatModifiers;
import com.stepm.techgunsupgrade.upgrade.UpgradeBuff;
import com.stepm.techgunsupgrade.upgrade.UpgradeRarity;
import com.stepm.techgunsupgrade.upgrade.WeaponUpgrades;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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

class MythicUpgradeDefinitionsTest {
    @BeforeAll
    static void initialize() {
        Bootstrap.register();
        WeaponUpgrades.registerAll();
        MythicUpgradeDefinitions.init();
    }

    @Test
    void coversEveryMythicUpgradeExactlyOnce() {
        Map<String, UpgradeDefinition> definitions = MythicUpgradeDefinitions.getAll();
        assertEquals(MythicUpgradeDefinitions.EXPECTED_COUNT, definitions.size());
        Set<String> sourceIds = new HashSet<>();
        int weaponCount = 0;
        for (Map.Entry<String, List<UpgradeBuff>> entry
                : WeaponUpgrades.getAllWeaponUpgrades().entrySet()) {
            int count = 0;
            for (UpgradeBuff buff : entry.getValue()) {
                if (buff.getRarity() != UpgradeRarity.MYTHIC) continue;
                count++;
                assertTrue(sourceIds.add(buff.getId()), "Duplicate source ID: " + buff.getId());
                UpgradeDefinition definition = definitions.get(buff.getId());
                assertNotNull(definition, "Missing definition: " + buff.getId());
                assertEquals(entry.getKey(), definition.getWeaponId());
                assertEquals(buff.getDescription(), definition.getDescription());
            }
            assertEquals(2, count, "Unexpected Mythic count for " + entry.getKey());
            weaponCount++;
        }
        assertEquals(40, weaponCount);
        assertEquals(sourceIds, definitions.keySet());
    }

    @Test
    void everyMythicDefinitionWritesExecutableData() {
        for (UpgradeDefinition definition : MythicUpgradeDefinitions.getAll().values()) {
            NBTTagCompound tag = new NBTTagCompound();
            StructuredEffectApplier.apply(tag, definition);
            assertFalse(tag.getKeySet().isEmpty(), "No runtime data: " + definition.getId());
        }
    }

    @Test
    void representativeConditionalEffectsHaveExactValues() {
        NBTTagCompound chaotic = apply("hmg_chaotic_charge");
        assertEquals(0.25f, chaotic.getFloat("random_damage_chance"), 0.0001f);
        assertEquals(4.0f, chaotic.getFloat("random_damage_multiplier"), 0.0001f);
        assertEquals(0.10f, chaotic.getFloat("random_self_damage_chance"), 0.0001f);
        assertEquals(0.50f, chaotic.getFloat("random_self_damage_fraction"), 0.0001f);

        NBTTagCompound queue = apply("m4_deadly_queue");
        assertEquals(30, queue.getInteger("full_mag_required_shots"));
        assertEquals(4.0f, queue.getFloat("full_mag_damage_multiplier"), 0.0001f);

        NBTTagCompound speedCharge = apply("br_speed_charge");
        assertEquals(3, speedCharge.getInteger("periodic_free_charge_interval"));
        assertEquals(3, speedCharge.getInteger("periodic_shot_interval"));
        assertEquals(1.50f, speedCharge.getFloat("periodic_shot_damage"), 0.0001f);

        NBTTagCompound nuclear = apply("bfg_deadly_charge");
        assertEquals(0.20f, nuclear.getFloat("charged_nuclear_chance"), 0.0001f);
        assertEquals(6.0f, nuclear.getFloat("charged_nuclear_multiplier"), 0.0001f);
        assertEquals(15.0f, nuclear.getFloat("charged_nuclear_radius"), 0.0001f);

        NBTTagCompound headshot = apply("lp_deadly_beam");
        assertEquals(4.0f, headshot.getFloat("headshot_damage_multiplier"), 0.0001f);
        assertEquals(6.0f, headshot.getFloat("headshot_shockwave"), 0.0001f);
    }

    @Test
    void permanentStackProgressIsIndependentAndAffectsRuntimeStats() {
        ItemStack stack = new ItemStack(new Item());
        NBTTagCompound root = new NBTTagCompound();
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("buffs_applied", true);
        StructuredEffectApplier.apply(tag, MythicUpgradeDefinitions.get("bolt_hunter"));
        StructuredEffectApplier.apply(tag, MythicUpgradeDefinitions.get("grp_deadly_harvest"));

        NBTTagCompound progress = new NBTTagCompound();
        progress.setFloat("bolt_hunter_damage", 25.0f);
        progress.setFloat("grp_deadly_harvest_damage", 10.0f);
        progress.setFloat("grp_deadly_harvest_explosion_radius", 5.0f);
        tag.setTag("mythic_stack_progress", progress);
        root.setTag("techgunsupgrade", tag);
        stack.setTagCompound(root);

        assertEquals(135.0f, GunStatModifiers.damage(stack, 100.0f), 0.0001f);
        assertEquals(1.05f, GunStatModifiers.explosionRadiusMultiplier(stack), 0.0001f);

        StructuredEffectApplier.clear(tag);
        assertTrue(tag.hasKey("mythic_stack_progress", 10),
                "Permanent per-upgrade progress must survive modifier rebuilds");
    }

    private static NBTTagCompound apply(String id) {
        NBTTagCompound tag = new NBTTagCompound();
        StructuredEffectApplier.apply(tag, MythicUpgradeDefinitions.get(id));
        return tag;
    }
}

package com.stepm.techgunsupgrade.upgrade.effect;

import com.stepm.techgunsupgrade.upgrade.WeaponUpgrades;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonUpgradeDefinitionRegistryTest {
    @BeforeAll
    static void initialize() {
        WeaponUpgrades.registerAll();
        JsonUpgradeDefinitionRegistry.init();
    }

    @Test
    void loadsExactCatalogCardinalities() {
        assertEquals(839, JsonUpgradeDefinitionRegistry.getAll().size());
        assertEquals(40, JsonUpgradeDefinitionRegistry.getAll().values().stream()
                .map(UpgradeDefinition::getWeaponId).distinct().count());
        assertEquals(382, JsonUpgradeDefinitionRegistry.getAll().values().stream()
                .map(UpgradeDefinition::getDescription).distinct().count());
    }

    @Test
    void everyEffectHasUniqueIdAndExactTooltipSource() {
        Set<String> effectIds = new HashSet<>();
        int effectCount = 0;
        for (UpgradeDefinition definition : JsonUpgradeDefinitionRegistry.getAll().values()) {
            assertFalse(definition.getEffects().isEmpty(), definition.getId());
            String tooltip = definition.getDescription().toLowerCase(Locale.ROOT);
            for (EffectSpec effect : definition.getEffects()) {
                effectCount++;
                assertFalse(effect.getId().trim().isEmpty(), definition.getId());
                assertTrue(effectIds.add(effect.getId()), "Duplicate effect ID: " + effect.getId());
                assertTrue(effect.getProbability() >= 0.0 && effect.getProbability() <= 1.0,
                        effect.getId());
                assertTrue(tooltip.contains(effect.getSourceFragment()
                                .toLowerCase(Locale.ROOT)),
                        effect.getId() + " does not point into its tooltip: "
                                + effect.getSourceFragment());
            }
        }
        assertEquals(1106, effectCount);
    }

    @Test
    void everySemanticTooltipSentenceIsMappedToAnExecutableEffect() {
        for (UpgradeDefinition definition : JsonUpgradeDefinitionRegistry.getAll().values()) {
            String[] sentences = definition.getDescription().split("(?i)(?:\\.\\s+|;\\s+)");
            for (String raw : sentences) {
                String sentence = raw.trim().toLowerCase(Locale.ROOT);
                if (sentence.isEmpty()) continue;
                boolean covered = false;
                for (EffectSpec effect : definition.getEffects()) {
                    String source = effect.getSourceFragment().toLowerCase(Locale.ROOT);
                    if (sentence.contains(source) || source.contains(sentence)) {
                        covered = true;
                        break;
                    }
                }
                assertTrue(covered, definition.getId()
                        + " has no executable effect mapped to tooltip sentence: " + raw);
            }
        }
    }

    @Test
    void runtimeCatalogExercisesEveryDeclaredActionTriggerAndCondition() {
        Set<EffectAction> actions = new HashSet<>();
        Set<EffectTrigger> triggers = new HashSet<>();
        Set<EffectCondition> conditions = new HashSet<>();
        for (UpgradeDefinition definition : JsonUpgradeDefinitionRegistry.getAll().values()) {
            for (EffectSpec effect : definition.getEffects()) {
                actions.add(effect.getAction());
                triggers.add(effect.getTrigger());
                conditions.add(effect.getCondition());
            }
        }
        assertEquals(new HashSet<>(Arrays.asList(EffectAction.values())), actions,
                "EffectAction exists without a JSON-backed upgrade");
        assertEquals(new HashSet<>(Arrays.asList(EffectTrigger.values())), triggers,
                "EffectTrigger exists without a JSON-backed upgrade");
        assertEquals(new HashSet<>(Arrays.asList(EffectCondition.values())), conditions,
                "EffectCondition exists without a JSON-backed upgrade");
    }
}

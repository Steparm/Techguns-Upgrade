package com.stepm.techgunsupgrade.upgrade.effect;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stepm.techgunsupgrade.upgrade.UpgradeBuff;
import com.stepm.techgunsupgrade.upgrade.UpgradeRarity;
import com.stepm.techgunsupgrade.upgrade.WeaponUpgrades;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Runtime source of truth for the 40 weapon JSON definition files. */
public final class JsonUpgradeDefinitionRegistry {
    private static final String ROOT = "assets/techgunsupgrade/upgrades/";
    private static final Map<String, UpgradeDefinition> DEFINITIONS = new LinkedHashMap<>();

    private JsonUpgradeDefinitionRegistry() {
    }

    public static synchronized void init() {
        DEFINITIONS.clear();
        JsonObject index = readJson(ROOT + "index.json");
        JsonArray files = index.getAsJsonArray("files");
        if (files == null || files.size() != 40) {
            throw new IllegalStateException("Expected exactly 40 weapon definition files");
        }
        for (JsonElement file : files) loadWeapon(file.getAsString());
        validate();
    }

    public static UpgradeDefinition get(String id) {
        return DEFINITIONS.get(id);
    }

    public static Map<String, UpgradeDefinition> getAll() {
        return Collections.unmodifiableMap(DEFINITIONS);
    }

    private static void loadWeapon(String file) {
        JsonObject root = readJson(ROOT + file);
        String weaponId = requiredString(root, "weapon");
        JsonArray upgrades = root.getAsJsonArray("upgrades");
        if (upgrades == null) throw new IllegalStateException("Missing upgrades in " + file);
        for (JsonElement element : upgrades) {
            JsonObject json = element.getAsJsonObject();
            String id = requiredString(json, "id");
            UpgradeRarity rarity = UpgradeRarity.valueOf(requiredString(json, "rarity"));
            String description = requiredString(json, "description");
            List<EffectSpec> effects = new ArrayList<>();
            JsonArray jsonEffects = json.getAsJsonArray("effects");
            if (jsonEffects == null || jsonEffects.size() == 0) {
                throw new IllegalStateException("Upgrade has no effects: " + id);
            }
            for (JsonElement effectElement : jsonEffects) {
                JsonObject effect = effectElement.getAsJsonObject();
                EffectSpec.Builder builder = EffectSpec.builder(
                        EffectTrigger.valueOf(requiredString(effect, "trigger")),
                        EffectAction.valueOf(requiredString(effect, "action")),
                        effect.get("value").getAsDouble(),
                        requiredString(effect, "sourceFragment"))
                        .id(requiredString(effect, "id"));
                if (effect.has("probability")) {
                    builder.probability(effect.get("probability").getAsDouble());
                }
                EffectCondition condition = effect.has("condition")
                        ? EffectCondition.valueOf(effect.get("condition").getAsString())
                        : EffectCondition.ALWAYS;
                double conditionValue = effect.has("parameters")
                        && effect.getAsJsonObject("parameters").has("conditionValue")
                        ? effect.getAsJsonObject("parameters").get("conditionValue").getAsDouble()
                        : 0.0;
                builder.condition(condition, conditionValue);
                effects.add(builder.build());
            }
            UpgradeDefinition previous = DEFINITIONS.put(id,
                    new UpgradeDefinition(id, weaponId, rarity, description, effects));
            if (previous != null) throw new IllegalStateException("Duplicate upgrade ID: " + id);
        }
    }

    private static void validate() {
        if (DEFINITIONS.size() != 839) {
            throw new IllegalStateException("Expected 839 JSON upgrades, got " + DEFINITIONS.size());
        }
        for (Map.Entry<String, List<UpgradeBuff>> weapon :
                WeaponUpgrades.getAllWeaponUpgrades().entrySet()) {
            for (UpgradeBuff buff : weapon.getValue()) {
                UpgradeDefinition definition = DEFINITIONS.get(buff.getId());
                if (definition == null) {
                    throw new IllegalStateException("Missing JSON upgrade: " + buff.getId());
                }
                if (!weapon.getKey().equals(definition.getWeaponId())
                        || buff.getRarity() != definition.getRarity()) {
                    throw new IllegalStateException("JSON metadata differs from tooltip: "
                            + buff.getId());
                }
            }
        }
    }

    private static JsonObject readJson(String path) {
        InputStream stream = JsonUpgradeDefinitionRegistry.class.getClassLoader()
                .getResourceAsStream(path);
        if (stream == null) throw new IllegalStateException("Missing upgrade resource: " + path);
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return new JsonParser().parse(reader).getAsJsonObject();
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Cannot read upgrade resource " + path, e);
        }
    }

    private static String requiredString(JsonObject json, String key) {
        if (!json.has(key) || json.get(key).getAsString().trim().isEmpty()) {
            throw new IllegalStateException("Missing string field: " + key);
        }
        return json.get(key).getAsString();
    }
}
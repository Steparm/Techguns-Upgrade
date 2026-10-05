package com.stepm.techgunsupgrade.upgrade.effect;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.stepm.techgunsupgrade.upgrade.UpgradeBuff;
import com.stepm.techgunsupgrade.upgrade.WeaponUpgrades;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Maintenance tool that materializes the reviewed Java catalog as 40 JSON files. */
public final class UpgradeJsonExporter {
    private UpgradeJsonExporter() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) throw new IllegalArgumentException("Output directory is required");
        WeaponUpgrades.registerAll();
        CommonUpgradeDefinitions.init();
        UncommonUpgradeDefinitions.init();
        RareUpgradeDefinitions.init();
        EpicUpgradeDefinitions.init();
        LegendaryUpgradeDefinitions.init();
        MythicUpgradeDefinitions.init();
        UltraMythicUpgradeDefinitions.init();

        Path output = Paths.get(args[0]);
        Files.createDirectories(output);
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        List<String> weapons = new ArrayList<>(WeaponUpgrades.getAllWeaponUpgrades().keySet());
        Collections.sort(weapons);
        JsonArray indexFiles = new JsonArray();
        for (String weaponId : weapons) {
            String fileName = weaponId.substring(weaponId.indexOf(':') + 1) + ".json";
            indexFiles.add(fileName);
            JsonObject root = new JsonObject();
            root.addProperty("schemaVersion", 1);
            root.addProperty("weapon", weaponId);
            JsonArray upgrades = new JsonArray();
            for (UpgradeBuff buff : WeaponUpgrades.getUpgradesForWeapon(weaponId)) {
                UpgradeDefinition definition = definition(buff.getId());
                if (definition == null) throw new IllegalStateException(buff.getId());
                JsonObject upgrade = new JsonObject();
                upgrade.addProperty("id", definition.getId());
                upgrade.addProperty("rarity", definition.getRarity().name());
                upgrade.addProperty("description", buff.getDescription());
                JsonArray effects = new JsonArray();
                int ordinal = 1;
                for (EffectSpec effect : definition.getEffects()) {
                    JsonObject jsonEffect = new JsonObject();
                    jsonEffect.addProperty("id", definition.getId() + "." + ordinal++);
                    jsonEffect.addProperty("trigger", effect.getTrigger().name());
                    jsonEffect.addProperty("action", effect.getAction().name());
                    jsonEffect.addProperty("condition", effect.getCondition().name());
                    jsonEffect.addProperty("value", effect.getValue());
                    jsonEffect.addProperty("probability", effect.getProbability());
                    JsonObject parameters = new JsonObject();
                    parameters.addProperty("conditionValue", effect.getConditionValue());
                    jsonEffect.add("parameters", parameters);
                    jsonEffect.addProperty("sourceFragment",
                            tooltipFragment(buff.getDescription(), effect,
                                    definition.getEffects().size()));
                    effects.add(jsonEffect);
                }
                upgrade.add("effects", effects);
                upgrades.add(upgrade);
            }
            root.add("upgrades", upgrades);
            write(gson, output.resolve(fileName), root);
        }
        JsonObject index = new JsonObject();
        index.addProperty("schemaVersion", 1);
        index.addProperty("weaponCount", weapons.size());
        index.addProperty("upgradeCount", 839);
        index.add("files", indexFiles);
        write(gson, output.resolve("index.json"), index);
    }

    private static void write(Gson gson, Path file, JsonObject json) throws IOException {
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(json, writer);
        }
    }

    private static UpgradeDefinition definition(String id) {
        UpgradeDefinition result = CommonUpgradeDefinitions.get(id);
        if (result == null) result = UncommonUpgradeDefinitions.get(id);
        if (result == null) result = RareUpgradeDefinitions.get(id);
        if (result == null) result = EpicUpgradeDefinitions.get(id);
        if (result == null) result = LegendaryUpgradeDefinitions.get(id);
        if (result == null) result = MythicUpgradeDefinitions.get(id);
        if (result == null) result = UltraMythicUpgradeDefinitions.get(id);
        return result;
    }

    /**
     * Materializes an exact substring of the visible tooltip. Older Java
     * definitions often contained an equivalent paraphrase; JSON must point
     * back to the text the player actually sees.
     */
    private static String tooltipFragment(String description, EffectSpec effect,
                                          int definitionEffectCount) {
        // A single combined action owns the complete sentence(s), including
        // its condition, cap, duration and secondary result.
        if (definitionEffectCount == 1) return description;

        String source = effect.getSourceFragment();
        int exact = description.toLowerCase(Locale.ROOT)
                .indexOf(source.toLowerCase(Locale.ROOT));
        if (exact >= 0) return description.substring(exact, exact + source.length());

        // A comma often joins an activation condition to its result. Splitting
        // there used to associate the 3-second Nuclear Apocalypse effect with
        // the neighboring on-kill sentence. Keep complete semantic sentences.
        String[] clauses = description.split("(?i)(?:\\.\\s+|;\\s+)");
        Set<String> sourceTokens = tokens(source + " "
                + effect.getAction().name().replace('_', ' ') + " "
                + effect.getCondition().name().replace('_', ' ') + " "
                + effect.getTrigger().name().replace('_', ' '));
        Set<String> sourceNumbers = numbers(source);
        String best = description;
        double bestScore = -1.0;
        for (String raw : clauses) {
            String clause = raw.trim();
            if (clause.isEmpty()) continue;
            Set<String> clauseTokens = tokens(clause);
            int overlap = 0;
            for (String token : sourceTokens) if (clauseTokens.contains(token)) overlap++;
            double score = overlap / (double) Math.max(1, sourceTokens.size());
            Set<String> clauseNumbers = numbers(clause);
            for (String number : sourceNumbers) {
                score += clauseNumbers.contains(number) ? 1.5 : -0.75;
            }
            if (score > bestScore) {
                bestScore = score;
                best = clause;
            }
        }
        return best;
    }

    private static Set<String> tokens(String text) {
        Set<String> result = new HashSet<>();
        for (String token : text.toLowerCase(Locale.ROOT).split("[^a-z0-9%]+")) {
            if (token.length() > 1 && !Arrays.asList("to", "of", "the", "a", "for",
                    "in", "on", "with", "every", "chance").contains(token)) {
                result.add(token);
            }
        }
        return result;
    }

    private static Set<String> numbers(String text) {
        Set<String> result = new HashSet<>();
        String normalized = text.toLowerCase(Locale.ROOT)
                .replaceAll("\\bone\\b", "1").replaceAll("\\btwo\\b", "2")
                .replaceAll("\\bthree\\b", "3").replaceAll("\\bfour\\b", "4")
                .replaceAll("\\bfive\\b", "5").replaceAll("\\bsix\\b", "6")
                .replaceAll("\\bseven\\b", "7").replaceAll("\\beight\\b", "8")
                .replaceAll("\\bnine\\b", "9").replaceAll("\\bten\\b", "10")
                .replaceAll("\\bfifteen\\b", "15").replaceAll("\\btwenty\\b", "20");
        Matcher matcher = Pattern.compile("[+-]?\\d+(?:\\.\\d+)?%?").matcher(normalized);
        while (matcher.find()) result.add(matcher.group());
        return result;
    }
}

package com.stepm.techgunsupgrade.debug;

import com.stepm.techgunsupgrade.manager.UpgradeApplicator;
import com.stepm.techgunsupgrade.upgrade.GunStatModifiers;
import com.stepm.techgunsupgrade.upgrade.UpgradeBuff;
import com.stepm.techgunsupgrade.upgrade.UpgradeData;
import com.stepm.techgunsupgrade.upgrade.WeaponUpgrades;
import com.stepm.techgunsupgrade.upgrade.effect.EffectAction;
import com.stepm.techgunsupgrade.upgrade.effect.EffectCondition;
import com.stepm.techgunsupgrade.upgrade.effect.EffectSpec;
import com.stepm.techgunsupgrade.upgrade.effect.EffectTrigger;
import com.stepm.techgunsupgrade.upgrade.effect.JsonUpgradeDefinitionRegistry;
import com.stepm.techgunsupgrade.upgrade.effect.StructuredEffectApplier;
import com.stepm.techgunsupgrade.upgrade.effect.UpgradeDefinition;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import techguns.items.guns.GenericGun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Exhaustive runtime audit used after all item registries are available. */
public final class UpgradeVerifier {

    private UpgradeVerifier() {
    }

    public static Report verifyAll() {
        long started = System.currentTimeMillis();
        List<String> failures = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        Set<String> effectIds = new HashSet<>();
        Set<EffectAction> actions = new HashSet<>();
        Set<EffectTrigger> triggers = new HashSet<>();
        Set<EffectCondition> conditions = new HashSet<>();
        int weaponCount = 0;
        int upgradeCount = 0;
        int effectCount = 0;
        int pairCount = 0;

        DebugSettings.setVerificationRunning(true);
        try {
            for (Map.Entry<String, List<UpgradeBuff>> weapon
                    : WeaponUpgrades.getAllWeaponUpgrades().entrySet()) {
                weaponCount++;
                Item item = Item.REGISTRY.getObject(new ResourceLocation(weapon.getKey()));
                if (!(item instanceof GenericGun)) {
                    failures.add(weapon.getKey() + ": item not found or not a GenericGun");
                }

                List<UpgradeBuff> buffs = weapon.getValue();
                for (UpgradeBuff buff : buffs) {
                    upgradeCount++;
                    if (!ids.add(buff.getId())) failures.add(buff.getId() + ": duplicate ID");
                    UpgradeDefinition definition = definition(buff);
                    if (definition == null) {
                        failures.add(buff.getId() + ": missing executable definition");
                        continue;
                    }
                    if (!weapon.getKey().equals(definition.getWeaponId())) {
                        failures.add(buff.getId() + ": definition bound to "
                                + definition.getWeaponId() + " instead of " + weapon.getKey());
                    }
                    effectCount += definition.getEffects().size();
                    validateDefinition(buff, definition, effectIds, actions, triggers,
                            conditions, failures);
                    if (item instanceof GenericGun) verifySingle(item, buff, failures);
                }

                for (int first = 0; first < buffs.size(); first++) {
                    for (int second = first + 1; second < buffs.size(); second++) {
                        pairCount++;
                        verifyPair(buffs.get(first), buffs.get(second), failures);
                    }
                }
            }
            requireCompleteEnum("actions", EffectAction.values(), actions, failures);
            requireCompleteEnum("triggers", EffectTrigger.values(), triggers, failures);
            requireCompleteEnum("conditions", EffectCondition.values(), conditions, failures);
        } finally {
            DebugSettings.setVerificationRunning(false);
        }

        return new Report(weaponCount, upgradeCount, effectCount, pairCount, failures,
                System.currentTimeMillis() - started);
    }

    private static void verifySingle(Item item, UpgradeBuff buff, List<String> failures) {
        try {
            ItemStack stack = new ItemStack(item);
            UpgradeData.addUpgrade(stack, buff.getId());
            UpgradeApplicator.applyUpgradesToGun(stack);
            NBTTagCompound tag = stack.getOrCreateSubCompound("techgunsupgrade");

            List<String> installed = UpgradeData.getUpgrades(stack);
            if (installed.size() != 1 || !buff.getId().equals(installed.get(0))) {
                failures.add(buff.getId() + ": ID not saved to ItemStack");
            }
            if (!tag.getBoolean("buffs_applied") || !GunStatModifiers.isActive(stack)) {
                failures.add(buff.getId() + ": calculated NBT not active");
            }
            if (UpgradeApplicator.needsRefresh(stack)) {
                failures.add(buff.getId() + ": ItemStack needs refresh right after apply");
            }
            if (!buff.getId().equals(tag.getString("applied_buffs"))) {
                failures.add(buff.getId() + ": applied_buffs does not match installed ID");
            }
            if (!tag.getBoolean("base_saved")) {
                failures.add(buff.getId() + ": base weapon stats not saved");
            }
            validateCalculatedStats(stack, tag, buff.getId(), failures);
            validateFinite(tag, buff.getId(), failures);
        } catch (Throwable error) {
            failures.add(buff.getId() + ": exception " + error.getClass().getSimpleName()
                    + " — " + String.valueOf(error.getMessage()));
        }
    }

    private static void verifyPair(UpgradeBuff first, UpgradeBuff second, List<String> failures) {
        UpgradeDefinition a = definition(first);
        UpgradeDefinition b = definition(second);
        if (a == null || b == null) return;
        String label = first.getId() + " + " + second.getId();
        try {
            NBTTagCompound forward = new NBTTagCompound();
            StructuredEffectApplier.apply(forward, a);
            StructuredEffectApplier.apply(forward, b);
            NBTTagCompound reverse = new NBTTagCompound();
            StructuredEffectApplier.apply(reverse, b);
            StructuredEffectApplier.apply(reverse, a);
            if (!forward.equals(reverse)) {
                failures.add(label + ": result depends on apply order");
            }
            validateFinite(forward, label, failures);
        } catch (Throwable error) {
            failures.add(label + ": exception " + error.getClass().getSimpleName()
                    + " — " + String.valueOf(error.getMessage()));
        }
    }

    private static void validateDefinition(UpgradeBuff buff, UpgradeDefinition definition,
                                           Set<String> effectIds, Set<EffectAction> actions,
                                           Set<EffectTrigger> triggers,
                                           Set<EffectCondition> conditions,
                                           List<String> failures) {
        String label = buff.getId();
        if (!label.equals(definition.getId())) {
            failures.add(label + ": JSON definition ID mismatch");
        }
        if (buff.getRarity() != definition.getRarity()) {
            failures.add(label + ": JSON definition rarity mismatch");
        }
        if (!buff.getDescription().equals(definition.getDescription())) {
            failures.add(label + ": JSON definition description does not match tooltip");
        }
        if (definition.getEffects().isEmpty()) {
            failures.add(label + ": executable effects list is empty");
            return;
        }
        String tooltip = definition.getDescription().toLowerCase(Locale.ROOT);
        for (EffectSpec effect : definition.getEffects()) {
            actions.add(effect.getAction());
            triggers.add(effect.getTrigger());
            conditions.add(effect.getCondition());
            String effectLabel = effect.getId() == null || effect.getId().trim().isEmpty()
                    ? label + "/<no ID>" : effect.getId();
            if (effect.getId() == null || effect.getId().trim().isEmpty()) {
                failures.add(effectLabel + ": effect has no unique ID");
            } else if (!effectIds.add(effect.getId())) {
                failures.add(effectLabel + ": duplicate effect ID");
            }
            if (effect.getTrigger() == null || effect.getAction() == null
                    || effect.getCondition() == null) {
                failures.add(effectLabel + ": missing trigger, condition, or action");
            }
            if (!Double.isFinite(effect.getValue())
                    || !Double.isFinite(effect.getConditionValue())
                    || !Double.isFinite(effect.getProbability())
                    || effect.getProbability() < 0.0 || effect.getProbability() > 1.0) {
                failures.add(effectLabel + ": invalid numeric parameters");
            }
            String source = effect.getSourceFragment();
            if (source == null || source.trim().isEmpty()
                    || !tooltip.contains(source.toLowerCase(Locale.ROOT))) {
                failures.add(effectLabel + ": sourceFragment does not point to tooltip text");
            }
        }
        for (String raw : definition.getDescription().split("(?i)(?:\\.\\s+|;\\s+)")) {
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
            if (!covered) {
                failures.add(label + ": tooltip sentence has no executable effect — " + raw);
            }
        }
    }

    private static <T> void requireCompleteEnum(String label, T[] expected, Set<T> actual,
                                                List<String> failures) {
        Set<T> missing = new HashSet<>(Arrays.asList(expected));
        missing.removeAll(actual);
        if (!missing.isEmpty()) failures.add("Catalog does not use " + label + ": " + missing);
    }

    private static void validateCalculatedStats(ItemStack stack, NBTTagCompound tag,
                                                String id, List<String> failures) {
        checkFinite(id, "damage", GunStatModifiers.damage(stack, tag.getFloat("base_damage")), failures);
        checkFinite(id, "damage_min", GunStatModifiers.damage(stack,
                tag.getFloat("base_damage_min")), failures);
        checkFinite(id, "accuracy", GunStatModifiers.accuracy(stack,
                tag.getFloat("base_accuracy")), failures);
        checkFinite(id, "range_start", GunStatModifiers.range(stack,
                tag.getFloat("base_range_start")), failures);
        checkFinite(id, "range_end", GunStatModifiers.range(stack,
                tag.getFloat("base_range_end")), failures);
        checkFinite(id, "penetration", GunStatModifiers.penetration(stack,
                tag.getFloat("base_penetration")), failures);
        if (GunStatModifiers.fireDelay(stack, tag.getInteger("base_min_firetime")) < 1) {
            failures.add(id + ": fire delay less than one tick");
        }
        if (GunStatModifiers.reloadTime(stack, tag.getInteger("base_reloadtime")) < 1) {
            failures.add(id + ": reload time less than one tick");
        }
        if (GunStatModifiers.clipSize(stack, tag.getInteger("base_clipsize")) < 1) {
            failures.add(id + ": magazine size less than one");
        }
        if (GunStatModifiers.bulletCount(stack, tag.getInteger("base_bulletcount")) < 0) {
            failures.add(id + ": negative projectile count");
        }
    }

    private static void checkFinite(String id, String stat, float value, List<String> failures) {
        if (!Float.isFinite(value)) failures.add(id + ": invalid number in " + stat);
    }

    private static void validateFinite(NBTTagCompound compound, String label,
                                       List<String> failures) {
        validateFiniteTag(compound, label, "techgunsupgrade", failures);
    }

    private static void validateFiniteTag(NBTBase tag, String label, String path,
                                          List<String> failures) {
        if (tag instanceof NBTPrimitive) {
            double value = ((NBTPrimitive) tag).getDouble();
            if (!Double.isFinite(value)) failures.add(label + ": invalid number in " + path);
            return;
        }
        if (tag instanceof NBTTagCompound) {
            NBTTagCompound compound = (NBTTagCompound) tag;
            for (String key : compound.getKeySet()) {
                validateFiniteTag(compound.getTag(key), label, path + "." + key, failures);
            }
            return;
        }
        if (tag instanceof NBTTagList) {
            NBTTagList list = (NBTTagList) tag;
            for (int i = 0; i < list.tagCount(); i++) {
                validateFiniteTag(list.get(i), label, path + "[" + i + "]", failures);
            }
        }
    }

    private static UpgradeDefinition definition(UpgradeBuff buff) {
        return JsonUpgradeDefinitionRegistry.get(buff.getId());
    }

    public static final class Report {
        private final int weaponCount;
        private final int upgradeCount;
        private final int effectCount;
        private final int pairCount;
        private final List<String> failures;
        private final long elapsedMillis;

        private Report(int weaponCount, int upgradeCount, int effectCount, int pairCount,
                       List<String> failures, long elapsedMillis) {
            this.weaponCount = weaponCount;
            this.upgradeCount = upgradeCount;
            this.effectCount = effectCount;
            this.pairCount = pairCount;
            this.failures = Collections.unmodifiableList(new ArrayList<>(failures));
            this.elapsedMillis = elapsedMillis;
        }

        public int getWeaponCount() { return weaponCount; }
        public int getUpgradeCount() { return upgradeCount; }
        public int getEffectCount() { return effectCount; }
        public int getPairCount() { return pairCount; }
        public List<String> getFailures() { return failures; }
        public long getElapsedMillis() { return elapsedMillis; }
        public boolean isSuccessful() { return failures.isEmpty(); }
    }
}
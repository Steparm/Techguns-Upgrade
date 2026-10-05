package com.stepm.techgunsupgrade.manager;

import com.stepm.techgunsupgrade.TechgunsUpgradeMod;
import com.stepm.techgunsupgrade.config.TguConfig;
import com.stepm.techgunsupgrade.config.UpgradeConfig;
import com.stepm.techgunsupgrade.debug.DebugSettings;
import com.stepm.techgunsupgrade.upgrade.UpgradeBuff;
import com.stepm.techgunsupgrade.upgrade.UpgradeData;
import com.stepm.techgunsupgrade.upgrade.UpgradeType;
import com.stepm.techgunsupgrade.upgrade.UpgradeRarity;
import com.stepm.techgunsupgrade.upgrade.WeaponUpgrades;
import com.stepm.techgunsupgrade.upgrade.effect.StructuredEffectApplier;
import com.stepm.techgunsupgrade.upgrade.effect.UpgradeDefinition;
import com.stepm.techgunsupgrade.upgrade.effect.JsonUpgradeDefinitionRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import techguns.items.guns.GenericGun;

import java.lang.reflect.Field;
import java.util.List;

public class UpgradeApplicator {

    private static final int MODIFIER_SCHEMA_VERSION = 13;

    private static Field damageField;
    private static Field damageMinField;
    private static Field clipsizeField;
    private static Field reloadtimeField;
    private static Field minFiretimeField;
    private static Field accuracyField;
    private static Field damageDropStartField;
    private static Field damageDropEndField;
    private static Field penetrationField;
    private static Field bulletcountField;

    static {
        try {
            damageField = getField(GenericGun.class, "damage");
            damageMinField = getField(GenericGun.class, "damageMin");
            clipsizeField = getField(GenericGun.class, "clipsize");
            reloadtimeField = getField(GenericGun.class, "reloadtime");
            minFiretimeField = getField(GenericGun.class, "minFiretime");
            accuracyField = getField(GenericGun.class, "accuracy");
            damageDropStartField = getField(GenericGun.class, "damageDropStart");
            damageDropEndField = getField(GenericGun.class, "damageDropEnd");
            penetrationField = getField(GenericGun.class, "penetration");
            bulletcountField = getField(GenericGun.class, "bulletcount");
        } catch (Exception e) {
            TechgunsUpgradeMod.LOGGER.error("Failed to init reflection: " + e.getMessage());
        }
    }

    private static Field getField(Class<?> clazz, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private static float getFloat(Object instance, Field field) {
        if (field == null) return 0.0f;
        try {
            return field.getFloat(instance);
        } catch (IllegalAccessException e) {
            return 0.0f;
        }
    }

    private static int getInt(Object instance, Field field) {
        if (field == null) return 0;
        try {
            return field.getInt(instance);
        } catch (IllegalAccessException e) {
            return 0;
        }
    }

    public static void saveBaseValuesToNBT(ItemStack gun) {
        if (gun.isEmpty() || !(gun.getItem() instanceof GenericGun)) return;

        NBTTagCompound tag = gun.getOrCreateSubCompound("techgunsupgrade");
        if (tag.hasKey("base_saved")) return;

        Object gunItem = gun.getItem();

        if (damageField != null) tag.setFloat("base_damage", getFloat(gunItem, damageField));
        if (damageMinField != null) tag.setFloat("base_damage_min", getFloat(gunItem, damageMinField));
        if (clipsizeField != null) tag.setInteger("base_clipsize", getInt(gunItem, clipsizeField));
        if (reloadtimeField != null) tag.setInteger("base_reloadtime", getInt(gunItem, reloadtimeField));
        if (minFiretimeField != null) tag.setInteger("base_min_firetime", getInt(gunItem, minFiretimeField));
        if (accuracyField != null) tag.setFloat("base_accuracy", getFloat(gunItem, accuracyField));
        if (damageDropStartField != null) tag.setFloat("base_range_start", getFloat(gunItem, damageDropStartField));
        if (damageDropEndField != null) tag.setFloat("base_range_end", getFloat(gunItem, damageDropEndField));
        if (penetrationField != null) tag.setFloat("base_penetration", getFloat(gunItem, penetrationField));
        if (bulletcountField != null) tag.setInteger("base_bulletcount", getInt(gunItem, bulletcountField));

        tag.setBoolean("base_saved", true);
    }

    public static void applyUpgradesToGun(ItemStack gun) {
        if (gun.isEmpty()) return;
        if (!(gun.getItem() instanceof GenericGun)) return;

        List<String> upgradeIds = UpgradeData.getUpgrades(gun);
        if (upgradeIds.isEmpty()) {
            clearAllModifiers(gun);
            return;
        }

        NBTTagCompound tag = gun.getOrCreateSubCompound("techgunsupgrade");

        String currentBuffs = String.join(",", upgradeIds);
        String savedBuffs = tag.getString("applied_buffs");

        boolean sameBuffs = currentBuffs.equals(savedBuffs);
        if (!sameBuffs || tag.getInteger("modifier_schema") != MODIFIER_SCHEMA_VERSION) {
            float legacyDamage = sameBuffs ? tag.getInteger("stack_DAMAGE") : 0.0f;
            float legacyFireRate = sameBuffs ? tag.getInteger("stack_FIRE_RATE") : 0.0f;
            float legacyAccuracy = sameBuffs ? tag.getInteger("stack_ACCURACY") : 0.0f;
            clearAllModifiers(gun);
            clearRuntimeState(tag);
            if (legacyDamage > 0.0f) {
                tag.setFloat("mythic_legacy_damage_progress", legacyDamage);
            }
            if (legacyFireRate > 0.0f) {
                tag.setFloat("mythic_legacy_fire_rate_progress", legacyFireRate);
            }
            if (legacyAccuracy > 0.0f) {
                tag.setFloat("mythic_legacy_accuracy_progress", legacyAccuracy);
            }
            tag.setString("applied_buffs", currentBuffs);
        }

        if (tag.getBoolean("buffs_applied") && tag.getInteger("modifier_schema") == MODIFIER_SCHEMA_VERSION) {
            return;
        }

        saveBaseValuesToNBT(gun);
        if (tag.getString("stack_id").isEmpty()) {
            tag.setString("stack_id", java.util.UUID.randomUUID().toString());
        }
        clearModifiers(tag);

        for (String id : upgradeIds) {
            if (!UpgradeConfig.isUpgradeEnabled(id)) {
                continue;
            }

            UpgradeBuff buff = findBuffById(id);
            if (buff != null) {
                applyBuffToNBT(gun, buff);
            }
        }

        tag.setBoolean("buffs_applied", true);
        tag.setInteger("modifier_schema", MODIFIER_SCHEMA_VERSION);
    }

    public static boolean needsRefresh(ItemStack gun) {
        if (gun == null || gun.isEmpty() || !(gun.getItem() instanceof GenericGun)) return false;

        List<String> upgradeIds = UpgradeData.getUpgrades(gun);
        if (upgradeIds.isEmpty()) return false;

        NBTTagCompound tag = gun.getOrCreateSubCompound("techgunsupgrade");
        String currentBuffs = String.join(",", upgradeIds);
        return !tag.getBoolean("buffs_applied")
                || tag.getInteger("modifier_schema") != MODIFIER_SCHEMA_VERSION
                || !currentBuffs.equals(tag.getString("applied_buffs"));
    }

    public static void refreshAllUpgrades() {
        for (List<UpgradeBuff> buffs : WeaponUpgrades.getAllWeaponUpgrades().values()) {
            for (UpgradeBuff buff : buffs) {
                buff.applyConfigOverrides();
            }
        }
    }

    private static void applyBuffToNBT(ItemStack gun, UpgradeBuff buff) {
        NBTTagCompound tag = gun.getOrCreateSubCompound("techgunsupgrade");

        UpgradeConfig.OverrideData override = UpgradeConfig.getOverride(buff.getId());

        UpgradeRarity actualRarity = override != null ? override.rarity : buff.getRarity();
        UpgradeType actualType = override != null ? override.type : buff.getType();
        double baseValue = override != null ? override.value : buff.getValue();

        // Multiplier: global × per-type (DAMAGE / FIRE_RATE / 1.0).
        // Only used by the fallback applyEffectToNBT path for non-structured upgrades.
        double multiplier = TguConfig.getTotalMultiplier(actualType);
        double actualValue = baseValue * multiplier;

        int actualMaxStack = override != null ? override.maxStack : buff.getMaxStack();
        String desc = buff.getDescription();
        String id = buff.getId();

        UpgradeDefinition structured = JsonUpgradeDefinitionRegistry.get(id);
        if (structured != null) {
            // Structured upgrades: the config multiplier is applied inside
            // StructuredEffectApplier (scaleValue) to avoid double multiplication.
            StructuredEffectApplier.apply(tag, structured);
            return;
        }

        // Fallback: non-structured upgrade (should never happen — all 839 are in JSON).
        applyEffectToNBT(tag, actualType, actualValue, actualMaxStack, id, desc);
    }

    private static void applyEffectToNBT(NBTTagCompound tag, UpgradeType type, double value, int maxStack, String id, String desc) {
        switch (type) {
            case DAMAGE:
                float dmgMult = 1.0f + (float) value;
                multiplyModifier(tag, "mod_damage", dmgMult);
                multiplyModifier(tag, "mod_damage_min", dmgMult);
                break;

            case FIRE_RATE:
                float rateMult = 1.0f / (1.0f + (float) value);
                multiplyModifier(tag, "mod_min_firetime", rateMult);
                break;

            case ACCURACY:
                float accMult = 1.0f / (1.0f + (float) value);
                multiplyModifier(tag, "mod_accuracy", accMult);
                break;

            case RANGE:
                float rangeMult = 1.0f + (float) value;
                multiplyModifier(tag, "mod_range", rangeMult);
                break;

            case MAGAZINE_SIZE:
                int magAdd = (int) value;
                addIntModifier(tag, "mod_clipsize", magAdd);
                break;

            case RELOAD_SPEED:
                float reloadMult = 1.0f / (1.0f + (float) value);
                multiplyModifier(tag, "mod_reloadtime", reloadMult);
                break;

            case ARMOR_PIERCING:
                float apValue = (float) value;
                addFloatModifier(tag, "mod_penetration", apValue);
                tag.setBoolean("techguns_armor_piercing", true);
                addFloatModifier(tag, "techguns_armor_piercing_amount", apValue);
                break;

            case PIERCING:
                float pierceValue = (float) value * 0.1f;
                addFloatModifier(tag, "mod_penetration", pierceValue);
                tag.setBoolean("techguns_piercing", true);
                tag.setInteger("techguns_piercing_count", (int) value);
                break;

            case EXPLOSIVE:
                tag.setBoolean("techguns_explosive", true);
                tag.setFloat("techguns_explosive_power", (float) value);
                break;

            case INCENDIARY:
                tag.setBoolean("techguns_incendiary", true);
                tag.setInteger("techguns_incendiary_duration", (int) (value * 20));
                break;

            case VAMPIRE:
                tag.setBoolean("techguns_vampire", true);
                tag.setInteger("techguns_vampire_heal", (int) (value * 2));
                break;

            case KNOCKBACK:
                tag.setBoolean("techguns_knockback", true);
                tag.setFloat("techguns_knockback_strength", (float) value);
                break;

            case POISON:
                tag.setBoolean("techguns_poison", true);
                tag.setInteger("techguns_poison_duration", (int) (value * 20));
                break;

            case LIGHTNING:
                tag.setBoolean("techguns_lightning", true);
                break;

            case FREEZE:
                tag.setBoolean("techguns_freeze", true);
                tag.setInteger("techguns_freeze_duration", (int) (value * 20));
                break;

            case CHAIN_LIGHTNING:
                tag.setBoolean("techguns_chain_lightning", true);
                tag.setInteger("techguns_chain_targets", (int) value);
                break;

            case DOUBLE_SHOT:
                tag.setInteger("techguns_double_shot_count", 1);
                break;

            case TRIPLE_SHOT:
                tag.setInteger("techguns_double_shot_count", 2);
                break;

            case BURST_FIRE:
                tag.setInteger("techguns_burst_count", (int) value);
                break;

            case SILENCER:
                tag.setBoolean("silencer", true);
                break;

            case UNLIMITED_AMMO:
            case INFINITE_MAGAZINE:
                tag.setBoolean("unlimited_magazine", true);
                break;

            case HUNTER:
                tag.setBoolean("techguns_hunter", true);
                tag.setFloat("techguns_hunter_multiplier", (float) (1.0 + value));
                break;

            case STACKING_DAMAGE:
                tag.setBoolean("techguns_stacking", true);
                tag.setFloat("stacking_value_per_kill", (float) value);
                tag.setInteger("stacking_max", maxStack);
                tag.setString("stacking_type", "DAMAGE");
                break;

            case STACKING_FIRE_RATE:
                tag.setBoolean("techguns_stacking", true);
                tag.setFloat("stacking_value_per_kill", (float) value);
                tag.setInteger("stacking_max", maxStack);
                tag.setString("stacking_type", "FIRE_RATE");
                break;

            case STACKING_ACCURACY:
                tag.setBoolean("techguns_stacking", true);
                tag.setFloat("stacking_value_per_kill", (float) value);
                tag.setInteger("stacking_max", maxStack);
                tag.setString("stacking_type", "ACCURACY");
                break;

            case STACKING_CRITICAL:
                tag.setBoolean("techguns_critical", true);
                tag.setFloat("techguns_critical_chance", 0.2f);
                tag.setFloat("techguns_critical_multiplier", 1.0f + (float) value / 100.0f);
                break;

            case ULTRA_MYTHIC:
                // Ultra-Mythic is already handled through StructuredEffectApplier
                break;

            default:
                TechgunsUpgradeMod.LOGGER.warn("  Unknown buff type: " + type);
                break;
        }
    }

    public static void clearAllModifiers(ItemStack gun) {
        if (gun.isEmpty()) return;
        NBTTagCompound tag = gun.getOrCreateSubCompound("techgunsupgrade");
        StructuredEffectApplier.clear(tag);

        tag.removeTag("mod_damage");
        tag.removeTag("mod_damage_min");
        tag.removeTag("mod_clipsize");
        tag.removeTag("mod_reloadtime");
        tag.removeTag("mod_min_firetime");
        tag.removeTag("mod_accuracy");
        tag.removeTag("mod_range");
        tag.removeTag("mod_penetration");
        tag.removeTag("mod_bulletcount");
        tag.removeTag("mod_armor_piercing");
        tag.removeTag("mod_piercing");

        tag.removeTag("techguns_explosive");
        tag.removeTag("techguns_incendiary");
        tag.removeTag("techguns_vampire");
        tag.removeTag("techguns_knockback");
        tag.removeTag("techguns_poison");
        tag.removeTag("techguns_lightning");
        tag.removeTag("techguns_freeze");
        tag.removeTag("techguns_chain_lightning");
        tag.removeTag("techguns_hunter");
        tag.removeTag("techguns_critical");
        tag.removeTag("techguns_stacking");
        tag.removeTag("techguns_double_shot_count");
        tag.removeTag("techguns_burst_count");
        tag.removeTag("techguns_piercing");
        tag.removeTag("techguns_piercing_count");
        tag.removeTag("techguns_armor_piercing");
        tag.removeTag("techguns_armor_piercing_amount");
        tag.removeTag("ultra_mythic");
        tag.removeTag("techguns_gold_drop");
        tag.removeTag("techguns_invisibility");
        tag.removeTag("techguns_death_scythe");
        tag.removeTag("techguns_doomsaw");
        tag.removeTag("techguns_armageddon");
        tag.removeTag("techguns_annihilator");
        tag.removeTag("techguns_apocalypse");
        tag.removeTag("techguns_legendary");
        tag.removeTag("techguns_hell_duet");
        tag.removeTag("techguns_hail_bullets");
        tag.removeTag("unlimited_magazine");
        tag.removeTag("silencer");
        tag.removeTag("techguns_critical_chance");
        tag.removeTag("techguns_critical_multiplier");
        tag.removeTag("stack_damage_bonus");
        tag.removeTag("stack_firerate_bonus");
        tag.removeTag("stack_accuracy_bonus");
        tag.removeTag("stack_DAMAGE");
        tag.removeTag("stack_FIRE_RATE");
        tag.removeTag("stack_ACCURACY");

        tag.setBoolean("buffs_applied", false);
        tag.removeTag("applied_buffs");
        tag.removeTag("modifier_schema");
    }

    public static void removeAllUpgrades(ItemStack gun) {
        if (gun == null || gun.isEmpty() || !gun.hasTagCompound()) return;
        NBTTagCompound root = gun.getTagCompound();
        if (root == null || !root.hasKey("techgunsupgrade", 10)) return;

        NBTTagCompound tag = root.getCompoundTag("techgunsupgrade");
        int baseClip = Math.max(0, tag.getInteger("base_clipsize"));
        int ammo = Math.max(0, root.getShort("ammo"));
        if (tag.hasKey("runtime_bottomless_reserve")) {
            ammo = Math.max(ammo, (int) Math.floor(
                    Math.max(0.0D, tag.getDouble("runtime_bottomless_reserve"))));
        }
        if (baseClip > 0) {
            root.setShort("ammo", (short) Math.min(baseClip, ammo));
        }

        clearAllModifiers(gun);
        root.removeTag("techgunsupgrade");
    }

    private static void clearModifiers(NBTTagCompound tag) {
        StructuredEffectApplier.clear(tag);
        tag.removeTag("mod_damage");
        tag.removeTag("mod_damage_min");
        tag.removeTag("mod_clipsize");
        tag.removeTag("mod_reloadtime");
        tag.removeTag("mod_min_firetime");
        tag.removeTag("mod_accuracy");
        tag.removeTag("mod_range");
        tag.removeTag("mod_penetration");
        tag.removeTag("mod_bulletcount");
        tag.removeTag("mod_armor_piercing");
        tag.removeTag("mod_piercing");
    }

    private static void clearRuntimeState(NBTTagCompound tag) {
        for (String key : new java.util.ArrayList<>(tag.getKeySet())) {
            if (key.startsWith("runtime_")) tag.removeTag(key);
        }
        tag.removeTag("ammo_cost_accumulator");
        tag.removeTag("lock_on_bonus_accumulator");
    }

    private static void multiplyModifier(NBTTagCompound tag, String key, float multiplier) {
        float current = tag.hasKey(key) ? tag.getFloat(key) : 1.0f;
        tag.setFloat(key, current * multiplier);
    }

    private static void addFloatModifier(NBTTagCompound tag, String key, float value) {
        tag.setFloat(key, tag.getFloat(key) + value);
    }

    private static void addIntModifier(NBTTagCompound tag, String key, int value) {
        tag.setInteger(key, tag.getInteger(key) + value);
    }

    private static UpgradeBuff findBuffById(String id) {
        for (List<UpgradeBuff> buffs : WeaponUpgrades.getAllWeaponUpgrades().values()) {
            for (UpgradeBuff buff : buffs) {
                if (buff.getId().equals(id)) {
                    return buff;
                }
            }
        }
        return null;
    }
}
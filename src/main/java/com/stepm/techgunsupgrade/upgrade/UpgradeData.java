package com.stepm.techgunsupgrade.upgrade;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.ArrayList;
import java.util.List;

public class UpgradeData {

    private static final String TAG_UPGRADES = "upgrades";
    private static final String TAG_MOD_DAMAGE = "mod_damage";
    private static final String TAG_MOD_FIRE_RATE = "mod_fire_rate";
    private static final String TAG_MOD_ACCURACY = "mod_accuracy";
    private static final String TAG_MOD_RANGE = "mod_range";
    private static final String TAG_MOD_CLIP = "mod_clip";
    private static final String TAG_MOD_RELOAD = "mod_reload";
    private static final String TAG_MOD_PENETRATION = "mod_penetration";
    private static final String TAG_EFFECTS = "effects";
    private static final String TAG_APPLIED = "applied";

    public static List<String> getUpgrades(ItemStack stack) {
        List<String> result = new ArrayList<>();
        if (stack.isEmpty()) return result;

        NBTTagCompound tag = stack.getOrCreateSubCompound("techgunsupgrade");
        if (tag.hasKey(TAG_UPGRADES)) {
            NBTTagList list = tag.getTagList(TAG_UPGRADES, 8);
            for (int i = 0; i < list.tagCount(); i++) {
                result.add(list.getStringTagAt(i));
            }
        }
        return result;
    }

    public static void addUpgrade(ItemStack stack, String upgradeId) {
        if (stack.isEmpty()) return;

        NBTTagCompound tag = stack.getOrCreateSubCompound("techgunsupgrade");
        NBTTagList list = tag.getTagList(TAG_UPGRADES, 8);

        for (int i = 0; i < list.tagCount(); i++) {
            if (list.getStringTagAt(i).equals(upgradeId)) return;
        }

        if (list.tagCount() >= 2) return;

        list.appendTag(new NBTTagString(upgradeId));
        tag.setTag(TAG_UPGRADES, list);
        tag.setBoolean(TAG_APPLIED, false);
    }

    public static boolean isApplied(ItemStack stack) {
        if (stack.isEmpty()) return false;
        NBTTagCompound tag = stack.getOrCreateSubCompound("techgunsupgrade");
        return tag.getBoolean(TAG_APPLIED);
    }

    public static void setApplied(ItemStack stack, boolean applied) {
        if (stack.isEmpty()) return;
        NBTTagCompound tag = stack.getOrCreateSubCompound("techgunsupgrade");
        tag.setBoolean(TAG_APPLIED, applied);
    }

    // Modifiers
    public static void setModifier(ItemStack stack, String key, float value) {
        if (stack.isEmpty()) return;
        NBTTagCompound tag = stack.getOrCreateSubCompound("techgunsupgrade");
        tag.setFloat(key, value);
    }

    public static float getModifier(ItemStack stack, String key, float defaultValue) {
        if (stack.isEmpty()) return defaultValue;
        NBTTagCompound tag = stack.getOrCreateSubCompound("techgunsupgrade");
        return tag.hasKey(key) ? tag.getFloat(key) : defaultValue;
    }

    public static void setModifierInt(ItemStack stack, String key, int value) {
        if (stack.isEmpty()) return;
        NBTTagCompound tag = stack.getOrCreateSubCompound("techgunsupgrade");
        tag.setInteger(key, value);
    }

    public static int getModifierInt(ItemStack stack, String key, int defaultValue) {
        if (stack.isEmpty()) return defaultValue;
        NBTTagCompound tag = stack.getOrCreateSubCompound("techgunsupgrade");
        return tag.hasKey(key) ? tag.getInteger(key) : defaultValue;
    }

    public static void setEffect(ItemStack stack, String effect, boolean value) {
        if (stack.isEmpty()) return;
        NBTTagCompound tag = stack.getOrCreateSubCompound("techgunsupgrade");
        NBTTagCompound effects = tag.getCompoundTag(TAG_EFFECTS);
        effects.setBoolean(effect, value);
        tag.setTag(TAG_EFFECTS, effects);
    }

    public static boolean hasEffect(ItemStack stack, String effect) {
        if (stack.isEmpty()) return false;
        NBTTagCompound tag = stack.getOrCreateSubCompound("techgunsupgrade");
        NBTTagCompound effects = tag.getCompoundTag(TAG_EFFECTS);
        return effects.getBoolean(effect);
    }

    public static void clearModifiers(ItemStack stack) {
        if (stack.isEmpty()) return;
        NBTTagCompound tag = stack.getOrCreateSubCompound("techgunsupgrade");
        tag.removeTag(TAG_MOD_DAMAGE);
        tag.removeTag(TAG_MOD_FIRE_RATE);
        tag.removeTag(TAG_MOD_ACCURACY);
        tag.removeTag(TAG_MOD_RANGE);
        tag.removeTag(TAG_MOD_CLIP);
        tag.removeTag(TAG_MOD_RELOAD);
        tag.removeTag(TAG_MOD_PENETRATION);
        tag.removeTag(TAG_EFFECTS);
        tag.setBoolean(TAG_APPLIED, false);
    }
}
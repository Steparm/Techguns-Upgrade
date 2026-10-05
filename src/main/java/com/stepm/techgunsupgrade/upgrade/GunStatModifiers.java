package com.stepm.techgunsupgrade.upgrade;

import com.stepm.techgunsupgrade.debug.DebugSettings;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import techguns.items.guns.GenericGun;
import techguns.util.InventoryUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Calculates Techguns stats from the upgrade data stored on one weapon stack.
 *
 * <p>The base values belong to the Techguns Item singleton and must never be
 * mutated. Keeping all calculations here makes the same rules available to
 * combat code, ammo handling and the Techguns tooltip.</p>
 */
public final class GunStatModifiers {

    private static final String ROOT_TAG = "techgunsupgrade";
    private static final ThreadLocal<Boolean> CHARGED_AMMO_CONTEXT =
            new ThreadLocal<Boolean>() {
                @Override protected Boolean initialValue() { return Boolean.FALSE; }
            };
    private static final Random AMMO_RANDOM = new Random();

    private GunStatModifiers() {
    }

    public static float damage(ItemStack stack, float baseValue) {
        NBTTagCompound tag = getActiveTag(stack);
        if (tag == null) return baseValue;
        return baseValue * multiplier(tag, "mod_damage") * multiplier(tag, "stack_damage_bonus")
                * (1.0f + mythicStackPercent(tag, "damage") / 100.0f);
    }

    public static int fireDelay(ItemStack stack, int baseValue) {
        return fireDelay(stack, baseValue, null);
    }

    public static int fireDelay(ItemStack stack, int baseValue, EntityLivingBase shooter) {
        NBTTagCompound tag = getActiveTag(stack);
        if (tag == null) return baseValue;
        float value = baseValue * multiplier(tag, "mod_min_firetime")
            * multiplier(tag, "stack_firerate_bonus")
            * multiplier(tag, "mod_spinup_time")
            / (1.0f + mythicStackPercent(tag, "fire_rate") / 100.0f);
        if (shooter != null && shooter.motionX * shooter.motionX + shooter.motionZ * shooter.motionZ
                > 0.0004) {
            value *= multiplier(tag, "moving_fire_delay");
        }
        return Math.max(1, Math.round(value));
    }

    public static int reloadTime(ItemStack stack, int baseValue) {
        NBTTagCompound tag = getActiveTag(stack);
        if (tag == null) return baseValue;
        return Math.max(1, Math.round(baseValue * multiplier(tag, "mod_reloadtime")));
    }

    public static int clipSize(ItemStack stack, int baseValue) {
        NBTTagCompound tag = getActiveTag(stack);
        if (tag == null) return baseValue;
        float scaled = baseValue * multiplier(tag, "mod_clipsize_mult");
        return Math.max(1, Math.round(scaled) + tag.getInteger("mod_clipsize"));
    }

    public static float accuracy(ItemStack stack, float baseValue) {
        return accuracy(stack, baseValue, null, false);
    }

    public static float accuracy(ItemStack stack, float baseValue,
                                 EntityLivingBase shooter, boolean burstWeapon) {
        NBTTagCompound tag = getActiveTag(stack);
        if (tag == null) return baseValue;
        float result = baseValue * multiplier(tag, "mod_accuracy")
                * multiplier(tag, "stack_accuracy_bonus")
                / (1.0f + mythicStackPercent(tag, "accuracy") / 100.0f);
        if (burstWeapon) result *= multiplier(tag, "common_burst_accuracy");
        if (shooter != null && shooter.isSneaking()) {
            result *= multiplier(tag, "common_prone_accuracy");
        }
        return result;
    }

    public static float range(ItemStack stack, float baseValue) {
        NBTTagCompound tag = getActiveTag(stack);
        if (tag == null) return baseValue;
        return baseValue * multiplier(tag, "mod_range");
    }

    public static float penetration(ItemStack stack, float baseValue) {
        NBTTagCompound tag = getActiveTag(stack);
        if (tag == null) return baseValue;

        // Prefer the unified key, but keep old upgraded weapons compatible.
        if (tag.hasKey("mod_penetration")) {
            return baseValue + tag.getFloat("mod_penetration");
        }
        if (tag.hasKey("mod_armor_piercing")) {
            return baseValue + tag.getFloat("mod_armor_piercing");
        }
        if (tag.hasKey("mod_piercing")) {
            return baseValue + tag.getFloat("mod_piercing");
        }
        return baseValue;
    }

    public static int bulletCount(ItemStack stack, int baseValue) {
        NBTTagCompound tag = getActiveTag(stack);
        if (tag == null) return baseValue;
        if (tag.getInteger("mod_bulletcount_override") > 0) {
            return tag.getInteger("mod_bulletcount_override");
        }
        int scaled = Math.round(baseValue * multiplier(tag, "mod_bulletcount_mult"));
        return Math.max(0, scaled + tag.getInteger("mod_bulletcount"));
    }

    /**
     * Charged Techguns weapons read their pellet count through a separate code
     * path. Include Ultra-Mythic charged projectiles there so the weapon's own
     * projectile factory creates the correct entity type and renderer.
     */
    public static int chargedBulletCount(ItemStack stack, int baseValue) {
        NBTTagCompound tag = getActiveTag(stack);
        int result = bulletCount(stack, baseValue);
        if (tag == null) return result;
        return Math.max(0, result + Math.max(0, tag.getInteger("charged_extra_projectiles")));
    }

    public static boolean isActive(ItemStack stack) {
        return getActiveTag(stack) != null;
    }

    public static boolean isUnlimitedAmmo(ItemStack stack) {
        NBTTagCompound tag = getActiveTag(stack);
        return tag != null && tag.getBoolean("unlimited_magazine");
    }

    /**
     * Fuel upgrades are genuinely infinite. Every other "infinite magazine"
     * upgrade only removes reloading: compatible bullets, rockets and energy
     * charges still come out of the player's inventory.
     */
    public static boolean isInfiniteFuel(ItemStack stack) {
        if (!isUnlimitedAmmo(stack) || stack == null || stack.isEmpty()) return false;
        ResourceLocation id = stack.getItem().getRegistryName();
        if (id == null || !"techguns".equals(id.getNamespace())) return false;
        String weapon = id.getPath();
        return "flamethrower".equals(weapon)
                || "chainsaw".equals(weapon)
                || "miningdrill".equals(weapon);
    }

    public static boolean isBottomlessMagazine(ItemStack stack) {
        return isUnlimitedAmmo(stack) && !isInfiniteFuel(stack);
    }

    /**
     * Pays for a shot fired by a bottomless (reload-free) magazine. Magazine
     * ammo is consumed one compatible magazine at a time; loose ammo, rockets
     * and energy/fuel units are consumed one item per internal ammo unit, just
     * like Techguns' own reload routine.
     */
    public static boolean consumeBottomlessMagazineAmmo(ItemStack stack,
                                                         EntityPlayer player,
                                                         int requested) {
        return consumeBottomlessMagazineAmmo(stack, player, requested, true);
    }

    public static boolean consumeBottomlessMagazineAmmo(ItemStack stack,
                                                         EntityPlayer player,
                                                         int requested,
                                                         boolean applyAmmoModifiers) {
        if (requested <= 0 || player == null || player.capabilities.isCreativeMode) return true;
        if (!isBottomlessMagazine(stack) || !(stack.getItem() instanceof GenericGun)) return false;

        int cost = requested;
        if (applyAmmoModifiers) {
            if (shouldSaveAmmo(stack)) return true;
            cost = discountedAmmoCost(stack, requested);
        }
        if (cost <= 0) return true;

        NBTTagCompound upgradeTag = getActiveTag(stack);
        NBTTagCompound root = stack.getTagCompound();
        if (upgradeTag == null || root == null) return false;

        final String reserveKey = "runtime_bottomless_reserve";
        double reserve;
        if (upgradeTag.hasKey(reserveKey)) {
            reserve = Math.max(0.0, upgradeTag.getDouble(reserveKey));
        } else {
            // Preserve rounds that were already loaded when the upgrade was installed.
            reserve = Math.max(0, root.getShort("ammo"));
            // The visible magazine is supplied by getCurrentAmmo; keeping a second
            // real magazine here would duplicate it when the upgrade is replaced.
            root.setShort("ammo", (short) 0);
        }

        GenericGun gun = (GenericGun) stack.getItem();
        double ammoPerInventoryItem = gun.getAmmoCount() <= 1
                ? Math.max(1, clipSize(stack, gun.getClipsize())) : 1.0;
        while (reserve + 1.0e-6 < cost) {
            if (!InventoryUtil.consumeAmmoPlayer(player, gun.getReloadItem(stack))) {
                upgradeTag.setDouble(reserveKey, reserve);
                return false;
            }
            reserve += ammoPerInventoryItem;
        }
        upgradeTag.setDouble(reserveKey, Math.max(0.0, reserve - cost));
        return true;
    }

    public static int bottomlessReserve(ItemStack stack) {
        if (!isBottomlessMagazine(stack)) return 0;
        NBTTagCompound tag = getActiveTag(stack);
        if (tag != null && tag.hasKey("runtime_bottomless_reserve")) {
            return Math.max(0, (int) Math.floor(tag.getDouble("runtime_bottomless_reserve")));
        }
        return stack.hasTagCompound() ? Math.max(0, stack.getTagCompound().getShort("ammo")) : 0;
    }

    /**
     * Atomically consumes individual reload items from every inventory that
     * Techguns normally searches. Used by multi-projectile shots and by
     * expanded single-shot magazines, where one reload item must equal one
     * actual round instead of filling the whole upgraded magazine for free.
     */
    public static boolean consumeInventoryAmmoItems(ItemStack gunStack,
                                                     EntityPlayer player,
                                                     int requested) {
        if (requested <= 0 || player == null || player.capabilities.isCreativeMode) return true;
        if (gunStack == null || gunStack.isEmpty()
                || !(gunStack.getItem() instanceof GenericGun)) return false;

        GenericGun gun = (GenericGun) gunStack.getItem();
        ItemStack[] candidates = gun.getReloadItem(gunStack);
        if (candidates == null || candidates.length == 0) return false;

        List<ItemStack> consumed = new ArrayList<>();
        for (int unit = 0; unit < requested; unit++) {
            ItemStack selected = ItemStack.EMPTY;
            for (ItemStack candidate : candidates) {
                if (candidate != null && !candidate.isEmpty()
                        && InventoryUtil.canConsumeAmmoPlayer(player, candidate)) {
                    selected = candidate.copy();
                    selected.setCount(1);
                    break;
                }
            }
            if (selected.isEmpty() || !InventoryUtil.consumeAmmoPlayer(player, selected)) {
                for (ItemStack rollback : consumed) {
                    InventoryUtil.addAmmoToPlayerInventory(player, rollback.copy());
                }
                return false;
            }
            consumed.add(selected);
        }
        return true;
    }

    public static float conditionalDamageMultiplier(ItemStack stack,
                                                    EntityLivingBase shooter,
                                                    EntityLivingBase target) {
        NBTTagCompound tag = getActiveTag(stack);
        if (tag == null || shooter == null || target == null) return 1.0f;
        float result = 1.0f;
        float targetDistance = shooter.getDistance(target);
        if (tag.hasKey("conditional_damage_specs", 10)) {
            NBTTagCompound specs = tag.getCompoundTag("conditional_damage_specs");
            for (String key : specs.getKeySet()) {
                NBTTagCompound spec = specs.getCompoundTag(key);
                String mode = spec.getString("mode");
                float distance = spec.getFloat("distance");
                if (("close".equals(mode) && targetDistance <= distance)
                        || ("long".equals(mode) && targetDistance > distance)) {
                    float value = spec.getFloat("multiplier");
                    if (Float.isFinite(value) && value > 0.0f) result *= value;
                }
            }
        } else {
            if (tag.hasKey("common_close_damage")) {
                float maxDistance = tag.getFloat("common_close_damage_distance");
                if (targetDistance <= maxDistance) {
                    result *= multiplier(tag, "common_close_damage");
                }
            }
            if (tag.hasKey("long_range_damage")) {
                float minDistance = tag.getFloat("long_range_distance");
                if (targetDistance > minDistance) {
                    result *= multiplier(tag, "long_range_damage");
                }
            }
        }
        float closeStack = mythicStackPercent(tag, "close_damage");
        if (closeStack > 0.0f && targetDistance <= tag.getFloat("mythic_close_damage_distance")) {
            result *= 1.0f + closeStack / 100.0f;
        }
        return result;
    }

    public static float movementSpeedMultiplier(ItemStack stack) {
        NBTTagCompound tag = getActiveTag(stack);
        return tag == null ? 1.0f : multiplier(tag, "mod_movement_speed");
    }

    public static float firingMovementSpeedMultiplier(ItemStack stack) {
        NBTTagCompound tag = getActiveTag(stack);
        return tag == null ? 1.0f : multiplier(tag, "firing_movement_speed");
    }

    public static float meleeDamage(ItemStack stack, float baseValue) {
        NBTTagCompound tag = getActiveTag(stack);
        if (tag == null) return baseValue;
        return damage(stack, baseValue) * multiplier(tag, "mod_melee_damage");
    }

    public static float meleeAttackSpeedMultiplier(ItemStack stack) {
        NBTTagCompound tag = getActiveTag(stack);
        return tag == null ? 1.0f : multiplier(tag, "mod_melee_attack_speed");
    }

    public static float chargeTime(ItemStack stack, float baseValue) {
        NBTTagCompound tag = getActiveTag(stack);
        if (tag == null) return baseValue;
        if (tag.getBoolean("instant_charge")) return 1.0f;
        int freeInterval = tag.getInteger("periodic_free_charge_interval");
        if (freeInterval > 0
                && (tag.getInteger("runtime_shot_sequence") + 1) % freeInterval == 0) {
            return 1.0f;
        }
        return baseValue * multiplier(tag, "mod_charge_time");
    }

    public static int lockOnTime(ItemStack stack, int baseValue) {
        NBTTagCompound tag = getActiveTag(stack);
        if (tag == null) return baseValue;
        return Math.max(1, Math.round(baseValue * multiplier(tag, "mod_lock_on_time")));
    }

    public static float lockOnTimeMultiplier(ItemStack stack) {
        NBTTagCompound tag = getActiveTag(stack);
        return tag == null ? 1.0f : multiplier(tag, "mod_lock_on_time");
    }

    public static float detectionRadiusMultiplier(ItemStack stack) {
        NBTTagCompound tag = getActiveTag(stack);
        if (tag == null) return 1.0f;
        float stackedReduction = mythicStackPercent(tag, "detection") / 100.0f;
        return multiplier(tag, "mod_detection_radius")
                * Math.max(0.05f, 1.0f - stackedReduction);
    }

    public static float soundVolumeMultiplier(ItemStack stack) {
        NBTTagCompound tag = getActiveTag(stack);
        if (tag == null) return 1.0f;
        if (tag.getBoolean("silencer")) return 0.0f;
        return Math.max(0.0f, Math.min(1.0f, detectionRadiusMultiplier(stack)));
    }

    public static float projectileSpeedMultiplier(ItemStack stack) {
        NBTTagCompound tag = getActiveTag(stack);
        return tag == null ? 1.0f : multiplier(tag, "mod_projectile_speed");
    }

    public static float explosionRadiusMultiplier(ItemStack stack) {
        NBTTagCompound tag = getActiveTag(stack);
        return tag == null ? 1.0f : multiplier(tag, "mod_explosion_radius")
                * (1.0f + mythicStackPercent(tag, "explosion_radius") / 100.0f);
    }

    public static float burnDamageMultiplier(ItemStack stack) {
        NBTTagCompound tag = getActiveTag(stack);
        return tag == null ? 1.0f : multiplier(tag, "mod_burn_damage")
                * (1.0f + mythicStackPercent(tag, "burn_damage") / 100.0f);
    }

    public static float poisonDamageMultiplier(ItemStack stack) {
        NBTTagCompound tag = getActiveTag(stack);
        return tag == null ? 1.0f : multiplier(tag, "mod_poison_damage")
                * (1.0f + mythicStackPercent(tag, "poison_damage") / 100.0f);
    }

    public static int groundFireBonusTicks(ItemStack stack) {
        NBTTagCompound tag = getActiveTag(stack);
        return tag == null ? 0 : Math.max(0, tag.getInteger("ground_fire_bonus_ticks"));
    }

    public static float miningSpeedMultiplier(ItemStack stack) {
        NBTTagCompound tag = getActiveTag(stack);
        return tag == null ? 1.0f : multiplier(tag, "mod_mining_speed");
    }

    public static float randomDamageMultiplier(ItemStack stack, Random random) {
        NBTTagCompound tag = getActiveTag(stack);
        if (tag == null || random == null) return 1.0f;
        if (tag.hasKey("random_damage_specs", 10)) {
            // Each upgrade owns an independent roll. When more than one
            // succeeds, their advertised +X% bonuses are added; multiplying
            // x3.5 by x4.0 would turn the valid HMG pair into the unintended
            // x14 spike.
            float bonus = 0.0f;
            int stunDuration = 0;
            NBTTagCompound specs = tag.getCompoundTag("random_damage_specs");
            for (String key : specs.getKeySet()) {
                NBTTagCompound spec = specs.getCompoundTag(key);
                if (DebugSettings.roll(random, spec.getFloat("chance"))) {
                    float value = spec.getFloat("multiplier");
                    if (Float.isFinite(value) && value > 0.0f) {
                        bonus += value - 1.0f;
                    }
                    stunDuration = Math.max(stunDuration, spec.getInteger("stun_duration"));
                }
            }
            tag.setInteger("runtime_random_damage_stun_duration", stunDuration);
            return Math.max(0.0f, 1.0f + bonus);
        }
        float chance = tag.getFloat("random_damage_chance");
        return DebugSettings.roll(random, chance)
                ? multiplier(tag, "random_damage_multiplier") : 1.0f;
    }

    public static float shockwaveRadius(ItemStack stack) {
        NBTTagCompound tag = getActiveTag(stack);
        return tag == null ? 0.0f : Math.max(0.0f, tag.getFloat("shockwave_radius"));
    }

    public static int requiredAmmoForShot(ItemStack stack, int requested) {
        if (requested <= 0) return requested;
        NBTTagCompound tag = getActiveTag(stack);
        if (tag == null) return requested;
        float consumption = multiplier(tag, "mod_ammo_consumption");
        if (consumption <= 1.0f) return requested;
        double accumulated = tag.getDouble("ammo_cost_accumulator") + requested * consumption;
        return Math.max(requested, (int) Math.floor(accumulated + 1.0e-6));
    }

    public static int miningAreaSize(ItemStack stack) {
        NBTTagCompound tag = getActiveTag(stack);
        if (tag == null) return 1;
        int size = tag.getInteger("mining_area_size");
        return size > 1 && size % 2 == 1 ? size : 1;
    }

    public static boolean shouldJam(ItemStack stack, Random random) {
        NBTTagCompound tag = getActiveTag(stack);
        if (tag == null || random == null) return false;
        float chance = tag.getFloat("common_jam_chance") * multiplier(tag, "mod_jam_chance");
        return DebugSettings.roll(random, chance);
    }

    public static boolean shouldSaveAmmo(ItemStack stack) {
        return shouldSaveAmmo(stack, AMMO_RANDOM);
    }

    public static boolean shouldSaveAmmo(ItemStack stack, Random random) {
        NBTTagCompound tag = getActiveTag(stack);
        if (tag == null || random == null) return false;
        int interval = tag.getInteger("periodic_free_ammo_interval");
        if (interval > 0) {
            int count = tag.getInteger("runtime_ammo_use_sequence") + 1;
            tag.setInteger("runtime_ammo_use_sequence", count);
            if (count % interval == 0) return true;
        }
        float chance = tag.getFloat("ammo_save_chance");
        return DebugSettings.roll(random, chance);
    }

    /**
     * Converts a fractional ammo-saving multiplier to an integer cost without
     * turning a 15% reduction into free infinite ammo. The remainder is stored
     * independently on the weapon stack.
     */
    public static int discountedAmmoCost(ItemStack stack, int requested) {
        if (requested <= 0) return requested;
        NBTTagCompound tag = getActiveTag(stack);
        if (tag == null) return requested;

        float consumption = multiplier(tag, "mod_ammo_consumption");
        if (!CHARGED_AMMO_CONTEXT.get()) {
            consumption *= multiplier(tag, "lmb_ammo_consumption");
        }
        if (Math.abs(consumption - 1.0f) < 0.000001f) return requested;
        double accumulated = tag.getDouble("ammo_cost_accumulator") + requested * consumption;
        int cost = Math.max(0, (int) Math.floor(accumulated + 1.0e-6));
        tag.setDouble("ammo_cost_accumulator", accumulated - cost);
        return cost;
    }

    public static void beginChargedAmmoContext() {
        CHARGED_AMMO_CONTEXT.set(Boolean.TRUE);
    }

    public static void endChargedAmmoContext() {
        CHARGED_AMMO_CONTEXT.set(Boolean.FALSE);
    }

    private static NBTTagCompound getActiveTag(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTagCompound()) return null;

        NBTTagCompound root = stack.getTagCompound();
        if (root == null || !root.hasKey(ROOT_TAG, 10)) return null;

        NBTTagCompound tag = root.getCompoundTag(ROOT_TAG);
        return tag.getBoolean("buffs_applied") ? tag : null;
    }

    private static float multiplier(NBTTagCompound tag, String key) {
        if (!tag.hasKey(key)) return 1.0f;
        float value = tag.getFloat(key);
        return Float.isFinite(value) && value > 0.0f ? value : 1.0f;
    }

    private static float mythicStackPercent(NBTTagCompound tag, String stat) {
        if (tag == null || !tag.hasKey("mythic_stack_specs", 10)
                || !tag.hasKey("mythic_stack_progress", 10)) return 0.0f;
        NBTTagCompound specs = tag.getCompoundTag("mythic_stack_specs");
        NBTTagCompound progress = tag.getCompoundTag("mythic_stack_progress");
        float total = 0.0f;
        for (String key : specs.getKeySet()) {
            NBTTagCompound spec = specs.getCompoundTag(key);
            if (stat.equals(spec.getString("stat"))) total += progress.getFloat(key);
        }
        return Math.max(0.0f, total);
    }
}

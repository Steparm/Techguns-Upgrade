package com.stepm.techgunsupgrade.upgrade;

import net.minecraft.init.Bootstrap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GunStatModifiersTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        Bootstrap.register();
    }

    @Test
    void calculatesBaseCommonStatsFromPerStackData() {
        ItemStack stack = upgradedStack();
        NBTTagCompound tag = stack.getTagCompound().getCompoundTag("techgunsupgrade");
        tag.setFloat("mod_damage", 1.20f);
        tag.setFloat("mod_min_firetime", 0.80f);
        tag.setFloat("mod_accuracy", 0.75f);
        tag.setFloat("mod_range", 1.10f);
        tag.setFloat("mod_clipsize_mult", 1.25f);
        tag.setInteger("mod_clipsize", 5);

        assertEquals(120.0f, GunStatModifiers.damage(stack, 100.0f), 0.0001f);
        assertEquals(8, GunStatModifiers.fireDelay(stack, 10));
        assertEquals(3.0f, GunStatModifiers.accuracy(stack, 4.0f), 0.0001f);
        assertEquals(55.0f, GunStatModifiers.range(stack, 50.0f), 0.0001f);
        assertEquals(30, GunStatModifiers.clipSize(stack, 20));
    }

    @Test
    void fractionalAmmoSavingAccumulatesInsteadOfGrantingInfiniteAmmo() {
        ItemStack stack = upgradedStack();
        NBTTagCompound tag = stack.getTagCompound().getCompoundTag("techgunsupgrade");
        tag.setFloat("mod_ammo_consumption", 0.85f);

        int consumed = 0;
        for (int shot = 0; shot < 20; shot++) {
            consumed += GunStatModifiers.discountedAmmoCost(stack, 1);
        }

        assertEquals(17, consumed);
        assertEquals(0.0d, tag.getDouble("ammo_cost_accumulator"), 0.000001d);
    }

    @Test
    void doubleBarrelUsesTwoAmmoAndDoublesProjectileCount() {
        ItemStack stack = upgradedStack();
        NBTTagCompound tag = stack.getTagCompound().getCompoundTag("techgunsupgrade");
        tag.setFloat("mod_ammo_consumption", 2.0f);
        tag.setFloat("mod_bulletcount_mult", 2.0f);

        assertEquals(2, GunStatModifiers.requiredAmmoForShot(stack, 1));
        assertEquals(2, GunStatModifiers.discountedAmmoCost(stack, 1));
        assertEquals(16, GunStatModifiers.bulletCount(stack, 8));
    }

    @Test
    void chargedWeaponsCombineRegularAndChargedProjectileBonuses() {
        ItemStack stack = upgradedStack();
        NBTTagCompound tag = stack.getTagCompound().getCompoundTag("techgunsupgrade");
        tag.setInteger("mod_bulletcount", 2);
        tag.setInteger("charged_extra_projectiles", 9);

        assertEquals(3, GunStatModifiers.bulletCount(stack, 1));
        assertEquals(12, GunStatModifiers.chargedBulletCount(stack, 1));
    }

    @Test
    void exposesProjectileAndToolSpecificMultipliers() {
        ItemStack stack = upgradedStack();
        NBTTagCompound tag = stack.getTagCompound().getCompoundTag("techgunsupgrade");
        tag.setFloat("mod_projectile_speed", 1.4f);
        tag.setFloat("mod_explosion_radius", 1.3f);
        tag.setFloat("mod_mining_speed", 1.5f);
        tag.setInteger("ground_fire_bonus_ticks", 40);

        assertEquals(1.4f, GunStatModifiers.projectileSpeedMultiplier(stack), 0.0001f);
        assertEquals(1.3f, GunStatModifiers.explosionRadiusMultiplier(stack), 0.0001f);
        assertEquals(1.5f, GunStatModifiers.miningSpeedMultiplier(stack), 0.0001f);
        assertEquals(40, GunStatModifiers.groundFireBonusTicks(stack));
    }

    @Test
    void appliesEpicMeleeAndFiringMovementMultipliers() {
        ItemStack stack = upgradedStack();
        NBTTagCompound tag = stack.getTagCompound().getCompoundTag("techgunsupgrade");
        tag.setFloat("mod_damage", 1.5f);
        tag.setFloat("mod_melee_damage", 2.0f);
        tag.setFloat("firing_movement_speed", 1.2f);

        assertEquals(150.0f, GunStatModifiers.damage(stack, 100.0f), 0.0001f);
        assertEquals(300.0f, GunStatModifiers.meleeDamage(stack, 100.0f), 0.0001f);
        assertEquals(1.2f, GunStatModifiers.firingMovementSpeedMultiplier(stack), 0.0001f);
    }

    @Test
    void economyModeOnlyDiscountsLmbAmmo() {
        ItemStack stack = upgradedStack();
        NBTTagCompound tag = stack.getTagCompound().getCompoundTag("techgunsupgrade");
        tag.setFloat("lmb_ammo_consumption", 0.8f);

        int lmbCost = 0;
        for (int i = 0; i < 5; i++) lmbCost += GunStatModifiers.discountedAmmoCost(stack, 1);
        assertEquals(4, lmbCost);

        GunStatModifiers.beginChargedAmmoContext();
        try {
            assertEquals(5, GunStatModifiers.discountedAmmoCost(stack, 5));
        } finally {
            GunStatModifiers.endChargedAmmoContext();
        }
    }

    @Test
    void reliableRifleHalvesConfiguredJamChance() {
        ItemStack stack = upgradedStack();
        NBTTagCompound tag = stack.getTagCompound().getCompoundTag("techgunsupgrade");
        tag.setFloat("common_jam_chance", 1.0f);
        tag.setFloat("mod_jam_chance", 0.5f);

        assertEquals(false, GunStatModifiers.shouldJam(stack, fixedRandom(0.75f)));
        assertEquals(true, GunStatModifiers.shouldJam(stack, fixedRandom(0.25f)));
    }

    @Test
    void legendaryAmmoSavingUsesItsDeclaredProbability() {
        ItemStack stack = upgradedStack();
        NBTTagCompound tag = stack.getTagCompound().getCompoundTag("techgunsupgrade");
        tag.setFloat("ammo_save_chance", 0.20f);

        assertEquals(true, GunStatModifiers.shouldSaveAmmo(stack, fixedRandom(0.10f)));
        assertEquals(false, GunStatModifiers.shouldSaveAmmo(stack, fixedRandom(0.30f)));

        tag.setFloat("random_damage_chance", 0.25f);
        tag.setFloat("random_damage_multiplier", 3.50f);
        assertEquals(3.50f, GunStatModifiers.randomDamageMultiplier(stack,
                fixedRandom(0.10f)), 0.0001f);
        assertEquals(1.0f, GunStatModifiers.randomDamageMultiplier(stack,
                fixedRandom(0.30f)), 0.0001f);
    }

    @Test
    void compatibleHmgChanceEffectsRollIndependentlyWithoutX14Multiplication() {
        ItemStack stack = upgradedStack();
        NBTTagCompound tag = stack.getTagCompound().getCompoundTag("techgunsupgrade");
        NBTTagCompound specs = new NBTTagCompound();
        specs.setTag("hmg_critical_miss", randomDamageSpec(0.25f, 3.5f));
        specs.setTag("hmg_chaotic_charge", randomDamageSpec(0.25f, 4.0f));
        tag.setTag("random_damage_specs", specs);

        CountingRandom bothSucceed = new CountingRandom(0.10f);
        assertEquals(6.5f, GunStatModifiers.randomDamageMultiplier(stack, bothSucceed), 0.0001f);
        assertEquals(2, bothSucceed.calls);

        CountingRandom bothFail = new CountingRandom(0.30f);
        assertEquals(1.0f, GunStatModifiers.randomDamageMultiplier(stack, bothFail), 0.0001f);
        assertEquals(2, bothFail.calls);
    }

    @Test
    void legendaryFuelSurchargeUsesFractionalAccumulatorAndInstantChargeIsMinimal() {
        ItemStack stack = upgradedStack();
        NBTTagCompound tag = stack.getTagCompound().getCompoundTag("techgunsupgrade");
        tag.setFloat("mod_ammo_consumption", 1.30f);
        tag.setBoolean("instant_charge", true);

        int consumed = 0;
        for (int shot = 0; shot < 10; shot++) {
            int required = GunStatModifiers.requiredAmmoForShot(stack, 1);
            int cost = GunStatModifiers.discountedAmmoCost(stack, 1);
            assertEquals(cost, required);
            consumed += cost;
        }
        assertEquals(13, consumed);
        assertEquals(1.0f, GunStatModifiers.chargeTime(stack, 40.0f), 0.0001f);
    }

    @Test
    void unlimitedUpgradeDistinguishesFuelFromBottomlessReserveAmmo() {
        for (String weapon : new String[]{"flamethrower", "chainsaw", "miningdrill"}) {
            ItemStack fuel = upgradedStack("techguns", weapon);
            fuel.getTagCompound().getCompoundTag("techgunsupgrade")
                    .setBoolean("unlimited_magazine", true);
            assertEquals(true, GunStatModifiers.isInfiniteFuel(fuel));
            assertEquals(false, GunStatModifiers.isBottomlessMagazine(fuel));
        }

        ItemStack energy = upgradedStack("techguns", "lasergun");
        energy.getTagCompound().getCompoundTag("techgunsupgrade")
                .setBoolean("unlimited_magazine", true);
        assertEquals(false, GunStatModifiers.isInfiniteFuel(energy));
        assertEquals(true, GunStatModifiers.isBottomlessMagazine(energy));

        ItemStack bullets = upgradedStack("techguns", "ak47");
        bullets.getTagCompound().getCompoundTag("techgunsupgrade")
                .setBoolean("unlimited_magazine", true);
        assertEquals(false, GunStatModifiers.isInfiniteFuel(bullets));
        assertEquals(true, GunStatModifiers.isBottomlessMagazine(bullets));
    }

    private static Random fixedRandom(final float value) {
        return new Random() {
            @Override public float nextFloat() { return value; }
        };
    }

    private static NBTTagCompound randomDamageSpec(float chance, float multiplier) {
        NBTTagCompound result = new NBTTagCompound();
        result.setFloat("chance", chance);
        result.setFloat("multiplier", multiplier);
        return result;
    }

    private static final class CountingRandom extends Random {
        private final float value;
        private int calls;

        private CountingRandom(float value) {
            this.value = value;
        }

        @Override
        public float nextFloat() {
            calls++;
            return value;
        }
    }

    private static ItemStack upgradedStack() {
        return upgradedStack("test", "weapon");
    }

    private static ItemStack upgradedStack(String namespace, String path) {
        Item item = new Item();
        item.setRegistryName(namespace, path);
        ItemStack stack = new ItemStack(item);
        NBTTagCompound root = new NBTTagCompound();
        NBTTagCompound upgrade = new NBTTagCompound();
        upgrade.setBoolean("buffs_applied", true);
        root.setTag("techgunsupgrade", upgrade);
        stack.setTagCompound(root);
        return stack;
    }
}

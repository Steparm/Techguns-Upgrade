package com.stepm.techgunsupgrade.bridge;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

/**
 * Exposes stack-aware values added to GenericGun to subclasses such as
 * GenericGunCharge without shadowing fields declared in the parent class.
 */
public interface GunStatsBridge {
    float tgu$chargedAccuracy(ItemStack stack, EntityLivingBase shooter);
    int tgu$baseBulletCount();
    float tgu$baseDamage();
    float tgu$baseMinimumDamage();
    float tgu$baseRangeStart();
    float tgu$baseRangeEnd();
    float tgu$basePenetration();
}

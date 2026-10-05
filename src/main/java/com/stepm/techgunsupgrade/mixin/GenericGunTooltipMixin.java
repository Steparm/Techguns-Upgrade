package com.stepm.techgunsupgrade.mixin;

import com.stepm.techgunsupgrade.upgrade.GunStatModifiers;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import techguns.items.guns.GenericGun;

@Mixin(value = GenericGun.class, remap = false)
public abstract class GenericGunTooltipMixin {

    @Shadow int minFiretime;
    @Shadow int reloadtime;
    @Shadow int clipsize;
    @Shadow float damage;
    @Shadow float damageMin;
    @Shadow float damageDropStart;
    @Shadow float damageDropEnd;
    @Shadow float penetration;
    @Shadow float accuracy;
    @Shadow int bulletcount;

    @Redirect(
        method = "getAmmoLeftCountTooltip",
        at = @At(value = "FIELD", target = "Ltechguns/items/guns/GenericGun;bulletcount:I"),
        require = 0
    )
    private int tgu$tooltipAmmoCount(GenericGun gun, ItemStack stack) {
        return GunStatModifiers.bulletCount(stack, this.bulletcount);
    }

    @Redirect(
        method = "getTooltipTextDmg",
        at = @At(value = "FIELD", target = "Ltechguns/items/guns/GenericGun;damage:F"),
        require = 0
    )
    private float tgu$tooltipDamage(GenericGun gun, ItemStack stack, boolean expanded) {
        return GunStatModifiers.damage(stack, this.damage);
    }

    @Redirect(
        method = "getTooltipTextDmg",
        at = @At(value = "FIELD", target = "Ltechguns/items/guns/GenericGun;damageMin:F"),
        require = 0
    )
    private float tgu$tooltipMinimumDamage(GenericGun gun, ItemStack stack, boolean expanded) {
        return GunStatModifiers.damage(stack, this.damageMin);
    }

    @Redirect(
        method = "getTooltipTextDps",
        at = @At(value = "FIELD", target = "Ltechguns/items/guns/GenericGun;minFiretime:I"),
        require = 0
    )
    private int tgu$tooltipDpsFireDelay(GenericGun gun) {
        return this.minFiretime;
    }

    @Redirect(
        method = "getTooltipTextDps",
        at = @At(value = "FIELD", target = "Ltechguns/items/guns/GenericGun;damage:F"),
        require = 0
    )
    private float tgu$tooltipDpsDamage(GenericGun gun) {
        return this.damage;
    }

    @Redirect(
        method = "getTooltipTextDps",
        at = @At(value = "FIELD", target = "Ltechguns/items/guns/GenericGun;damageMin:F"),
        require = 0
    )
    private float tgu$tooltipDpsDamageMin(GenericGun gun) {
        return this.damageMin;
    }

    @Redirect(
        method = "getTooltipTextRange",
        at = @At(value = "FIELD", target = "Ltechguns/items/guns/GenericGun;damageDropStart:F"),
        require = 0
    )
    private float tgu$tooltipRangeStart(GenericGun gun, ItemStack stack) {
        return GunStatModifiers.range(stack, this.damageDropStart);
    }

    @Redirect(
        method = "getTooltipTextRange",
        at = @At(value = "FIELD", target = "Ltechguns/items/guns/GenericGun;damageDropEnd:F"),
        require = 0
    )
    private float tgu$tooltipRangeEnd(GenericGun gun, ItemStack stack) {
        return GunStatModifiers.range(stack, this.damageDropEnd);
    }

    @Redirect(
        method = "func_77624_a",
        at = @At(value = "FIELD", target = "Ltechguns/items/guns/GenericGun;clipsize:I"),
        require = 0
    )
    private int tgu$tooltipClipSize(GenericGun gun, ItemStack stack) {
        return GunStatModifiers.clipSize(stack, this.clipsize);
    }

    @Redirect(
        method = "func_77624_a",
        at = @At(value = "FIELD", target = "Ltechguns/items/guns/GenericGun;reloadtime:I"),
        require = 0
    )
    private int tgu$tooltipReloadTime(GenericGun gun, ItemStack stack) {
        return GunStatModifiers.reloadTime(stack, this.reloadtime);
    }

    @Redirect(
        method = "func_77624_a",
        at = @At(value = "FIELD", target = "Ltechguns/items/guns/GenericGun;penetration:F"),
        require = 0
    )
    private float tgu$tooltipPenetration(GenericGun gun, ItemStack stack) {
        return GunStatModifiers.penetration(stack, this.penetration);
    }

    @Redirect(
        method = "func_77624_a",
        at = @At(value = "FIELD", target = "Ltechguns/items/guns/GenericGun;accuracy:F"),
        require = 0
    )
    private float tgu$tooltipAccuracy(GenericGun gun, ItemStack stack) {
        return GunStatModifiers.accuracy(stack, this.accuracy);
    }
}
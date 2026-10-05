package com.stepm.techgunsupgrade.mixin;

import com.stepm.techgunsupgrade.event.UpgradeEventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import techguns.entities.projectiles.GenericProjectile;

/** Applies the stored silencer percentage to Techguns' AI revenge distance. */
@Mixin(value = GenericProjectile.class, remap = false)
public abstract class GenericProjectileAiMixin {
    private static final double TGU_NORMAL_ALERT_DISTANCE = 32.0;

    @Inject(method = "setAIRevengeTarget", at = @At("HEAD"), cancellable = true, require = 1)
    private void tgu$limitAiAlertDistance(EntityLiving target, CallbackInfo ci) {
        GenericProjectile projectile = (GenericProjectile) (Object) this;
        NBTTagCompound data = projectile.getEntityData();
        if (!data.hasKey("tgu_detection_multiplier")) return;

        float multiplier = data.getFloat("tgu_detection_multiplier");
        if (!Float.isFinite(multiplier) || multiplier >= 1.0f) return;
        if (multiplier <= 0.0f) {
            ci.cancel();
            return;
        }
        Entity shooter = UpgradeEventHandler.getShooter(projectile);
        if (shooter != null && target.getDistance(shooter) > TGU_NORMAL_ALERT_DISTANCE * multiplier) {
            ci.cancel();
        }
    }

    @Inject(method = "func_70071_h_", at = @At("HEAD"), require = 1, remap = false)
    private void tgu$updateEpicHoming(CallbackInfo ci) {
        UpgradeEventHandler.updateEpicProjectile((GenericProjectile) (Object) this);
    }

    @Inject(method = "func_70071_h_", at = @At("TAIL"), require = 1, remap = false)
    private void tgu$finalizeMissedShot(CallbackInfo ci) {
        GenericProjectile projectile = (GenericProjectile) (Object) this;
        if (projectile.isDead) UpgradeEventHandler.handleProjectileMiss(projectile);
    }
}

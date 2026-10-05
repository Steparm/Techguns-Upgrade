package com.stepm.techgunsupgrade.mixin;

import com.stepm.techgunsupgrade.event.UpgradeEventHandler;
import net.minecraft.util.math.RayTraceResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import techguns.entities.projectiles.SonicShotgunProjectile;

/** Applies Rare impact effects and the explicit three-target sonic-wave limit. */
@Mixin(value = SonicShotgunProjectile.class, remap = false)
public abstract class SonicShotgunProjectileMixin {
    @Inject(method = "onHit", at = @At("HEAD"), require = 1)
    private void tgu$handleRareSonicImpact(RayTraceResult result, CallbackInfo ci) {
        UpgradeEventHandler.handleRareProjectileImpact(
                (SonicShotgunProjectile) (Object) this, result);
    }

    @Inject(method = "onHit", at = @At("TAIL"), require = 1)
    private void tgu$limitRareSonicPiercing(RayTraceResult result, CallbackInfo ci) {
        SonicShotgunProjectile projectile = (SonicShotgunProjectile) (Object) this;
        int maximumTargets = UpgradeEventHandler.getConfiguredPiercingLimit(projectile);
        if (maximumTargets > 0 && result.entityHit != null && projectile.entitiesHit != null
                && projectile.entitiesHit.size() >= maximumTargets) {
            projectile.setDead();
        }
    }
}

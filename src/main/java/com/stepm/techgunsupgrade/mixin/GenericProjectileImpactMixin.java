package com.stepm.techgunsupgrade.mixin;

import com.stepm.techgunsupgrade.event.UpgradeEventHandler;
import net.minecraft.util.math.RayTraceResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import techguns.entities.projectiles.GenericProjectile;

/** Adds Rare impact mechanics without replacing Techguns' normal hit handling. */
@Mixin(value = GenericProjectile.class, remap = false)
public abstract class GenericProjectileImpactMixin {
    @Inject(method = "onHit", at = @At("HEAD"), require = 1)
    private void tgu$handleRareImpact(RayTraceResult result, CallbackInfo ci) {
        UpgradeEventHandler.handleRareProjectileImpact(
                (GenericProjectile) (Object) this, result);
    }
}

package com.stepm.techgunsupgrade.mixin;

import com.stepm.techgunsupgrade.event.UpgradeEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import techguns.entities.projectiles.AbstractBeamProjectile;
import techguns.entities.projectiles.GenericProjectile;

/** Attaches the shot snapshot before Laser/Tesla perform constructor-time traces. */
@Mixin(value = AbstractBeamProjectile.class, remap = false)
public abstract class AbstractBeamProjectileMixin {
    @Inject(method = "trace", at = @At("HEAD"), require = 1)
    private void tgu$attachContextBeforeTrace(CallbackInfo ci) {
        UpgradeEventHandler.initializeProjectileContext((GenericProjectile) (Object) this);
    }
}

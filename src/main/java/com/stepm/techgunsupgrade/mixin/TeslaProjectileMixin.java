package com.stepm.techgunsupgrade.mixin;

import com.stepm.techgunsupgrade.event.UpgradeEventHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import techguns.entities.projectiles.EnumBulletFirePos;
import techguns.entities.projectiles.TeslaProjectile;

/** Adds Tesla bounce upgrades to Techguns' native chain counter. */
@Mixin(value = TeslaProjectile.class, remap = false)
public abstract class TeslaProjectileMixin {
    @Shadow protected int chainTargets;

    @Inject(
            method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/entity/EntityLivingBase;FFIFFFFFZLtechguns/entities/projectiles/EnumBulletFirePos;I)V",
            at = @At(value = "INVOKE",
                    target = "Ltechguns/entities/projectiles/TeslaProjectile;trace()V",
                    shift = At.Shift.BEFORE),
            require = 1
    )
    private void tgu$extendNativeChain(World world, EntityLivingBase shooter,
                                       float damage, float speed, int ticksToLive,
                                       float spread, float damageDropStart,
                                       float damageDropEnd, float damageMin,
                                       float penetration, boolean blockDamage,
                                       EnumBulletFirePos firePos, int originalTargets,
                                       CallbackInfo ci) {
        this.chainTargets = Math.max(0, this.chainTargets
                + UpgradeEventHandler.currentTeslaChainBonus());
    }
}

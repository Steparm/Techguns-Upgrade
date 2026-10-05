package com.stepm.techgunsupgrade.mixin;

import com.stepm.techgunsupgrade.event.GroundFireTracker;
import com.stepm.techgunsupgrade.event.UpgradeEventHandler;
import com.stepm.techgunsupgrade.upgrade.GunStatModifiers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import techguns.entities.projectiles.GenericProjectile;

/** Adds deterministic extra lifetime to fire placed by the Uncommon thickener. */
@Mixin(targets = "techguns.entities.projectiles.FlamethrowerProjectile", remap = false)
public abstract class FlamethrowerProjectileMixin {
    @Inject(method = "hitBlock", at = @At("TAIL"), require = 1)
    private void tgu$keepGroundFireAlive(RayTraceResult result, CallbackInfo ci) {
        GenericProjectile projectile = (GenericProjectile) (Object) this;
        if (projectile.world == null || projectile.world.isRemote || result == null
                || result.getBlockPos() == null || result.sideHit == null) return;

        Entity shooter = UpgradeEventHandler.getShooter(projectile);
        if (!(shooter instanceof EntityPlayer)) return;
        ItemStack gun = UpgradeEventHandler.gunFromProjectile(projectile);
        if (gun.isEmpty()) gun = ((EntityPlayer) shooter).getHeldItemMainhand();
        int bonusTicks = GunStatModifiers.groundFireBonusTicks(gun);
        BlockPos firePosition = result.getBlockPos().offset(result.sideHit);
        if (bonusTicks > 0) {
            GroundFireTracker.keepBurning(projectile.world, firePosition, bonusTicks, shooter);
        }

        if (gun.hasTagCompound() && gun.getTagCompound().hasKey("techgunsupgrade", 10)) {
            int duration = gun.getTagCompound().getCompoundTag("techgunsupgrade")
                    .getInteger("moving_fire_zone_duration");
            int radius = gun.getTagCompound().getCompoundTag("techgunsupgrade")
                    .getInteger("moving_fire_zone_radius");
            if (duration > 0) {
                GroundFireTracker.createFireZone(projectile.world, firePosition,
                        Math.max(1, radius), duration, shooter);
            }
        }
    }
}

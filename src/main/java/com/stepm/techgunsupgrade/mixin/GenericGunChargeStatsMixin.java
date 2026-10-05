package com.stepm.techgunsupgrade.mixin;

import com.stepm.techgunsupgrade.bridge.GunStatsBridge;
import com.stepm.techgunsupgrade.event.UpgradeEventHandler;
import com.stepm.techgunsupgrade.upgrade.GunStatModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import techguns.entities.projectiles.EnumBulletFirePos;
import techguns.items.guns.GenericGunCharge;

/** Applies per-stack charge-time effects without mutating the shared gun Item. */
@Mixin(value = GenericGunCharge.class, remap = false)
public abstract class GenericGunChargeStatsMixin {
    @Shadow public float fullChargeTime;
    @Shadow public int ammoConsumedOnFullCharge;
    @Unique private static final ThreadLocal<Float> TGU_CHARGE_TICKS =
            new ThreadLocal<Float>() {
                @Override protected Float initialValue() { return 0.0f; }
            };
    @Unique private static final ThreadLocal<Boolean> TGU_FULLY_CHARGED =
            new ThreadLocal<Boolean>() {
                @Override protected Boolean initialValue() { return Boolean.FALSE; }
            };
    @Unique private static final ThreadLocal<Float> TGU_RANDOM_DAMAGE =
            new ThreadLocal<Float>() {
                @Override protected Float initialValue() { return 1.0f; }
            };

    @Inject(method = "func_77615_a", at = @At("HEAD"), cancellable = true, require = 1)
    private void tgu$captureChargedTrigger(ItemStack stack, World world,
                                           EntityLivingBase entity, int timeLeft,
                                           CallbackInfo ci) {
        if (!world.isRemote && entity instanceof EntityPlayer
                && GunStatModifiers.isBottomlessMagazine(stack)) {
            EntityPlayer player = (EntityPlayer) entity;
            float chargeTicks = Math.max(0, stack.getMaxItemUseDuration() - timeLeft);
            float charge = Math.min(1.0f, chargeTicks
                    / Math.max(1.0f, GunStatModifiers.chargeTime(stack, this.fullChargeTime)));
            int cost = (int) Math.ceil(charge * this.ammoConsumedOnFullCharge);
            GunStatModifiers.beginChargedAmmoContext();
            boolean paid;
            try {
                paid = GunStatModifiers.consumeBottomlessMagazineAmmo(stack, player, cost);
            } finally {
                GunStatModifiers.endChargedAmmoContext();
            }
            if (!paid) {
                ci.cancel();
                return;
            }
        }
        if (!world.isRemote) {
            UpgradeEventHandler.captureTriggerPull(stack, world.getTotalWorldTime(),
                    ((GenericGunCharge) (Object) this).getCurrentAmmo(stack));
        }
    }

    @Redirect(
            method = "func_77615_a",
            at = @At(value = "FIELD",
                    target = "Ltechguns/items/guns/GenericGunCharge;fullChargeTime:F"),
            require = 1
    )
    private float tgu$chargeTime(GenericGunCharge gun, ItemStack stack, World world,
                                 EntityLivingBase entity, int timeLeft) {
        return GunStatModifiers.chargeTime(stack, this.fullChargeTime);
    }

    @Redirect(
            method = "func_77615_a",
            at = @At(value = "FIELD",
                    target = "Ltechguns/items/guns/GenericGunCharge;accuracy:F"),
            require = 1
    )
    private float tgu$chargedAccuracy(GenericGunCharge gun, ItemStack stack, World world,
                                      EntityLivingBase entity, int timeLeft) {
        return ((GunStatsBridge) gun).tgu$chargedAccuracy(stack, entity);
    }

    @Inject(method = "consumeAmmoCharge", at = @At("HEAD"), require = 1)
    private void tgu$beginChargedAmmo(ItemStack stack, float charge, boolean creative,
                                      CallbackInfoReturnable<Integer> cir) {
        GunStatModifiers.beginChargedAmmoContext();
    }

    @Inject(method = "consumeAmmoCharge", at = @At("RETURN"), require = 1)
    private void tgu$endChargedAmmo(ItemStack stack, float charge, boolean creative,
                                    CallbackInfoReturnable<Integer> cir) {
        GunStatModifiers.endChargedAmmoContext();
    }

    @Inject(method = "spawnChargedProjectile", at = @At("HEAD"), require = 1)
    private void tgu$captureChargeTicks(World world, EntityLivingBase shooter, ItemStack stack,
                                        float accuracy, float charge, int ammo,
                                        EnumBulletFirePos firePos, CallbackInfo ci) {
        TGU_CHARGE_TICKS.set(Math.max(0.0f, charge * this.fullChargeTime));
        TGU_FULLY_CHARGED.set(charge >= 0.99f);
        TGU_RANDOM_DAMAGE.set(UpgradeEventHandler.projectileRandomDamageMultiplier(
                stack, world.getTotalWorldTime()));
        UpgradeEventHandler.beginProjectileConstruction(stack, shooter, null);
    }

    @Inject(method = "spawnChargedProjectile", at = @At("RETURN"), require = 1)
    private void tgu$clearChargeTicks(World world, EntityLivingBase shooter, ItemStack stack,
                                      float accuracy, float charge, int ammo,
                                      EnumBulletFirePos firePos, CallbackInfo ci) {
        UpgradeEventHandler.endProjectileConstruction();
        TGU_RANDOM_DAMAGE.remove();
        TGU_CHARGE_TICKS.remove();
        TGU_FULLY_CHARGED.remove();
    }

    @Redirect(
            method = "func_77615_a",
            at = @At(value = "FIELD",
                    target = "Ltechguns/items/guns/GenericGunCharge;bulletcount:I"),
            require = 1
    )
    private int tgu$chargedProjectileCount(GenericGunCharge gun, ItemStack stack, World world,
                                            EntityLivingBase shooter, int timeLeft) {
        return GunStatModifiers.chargedBulletCount(stack,
                ((GunStatsBridge) gun).tgu$baseBulletCount());
    }

    @Redirect(
            method = "spawnChargedProjectile",
            at = @At(value = "FIELD",
                    target = "Ltechguns/items/guns/GenericGunCharge;damage:F"),
            require = 1
    )
    private float tgu$chargedDamage(GenericGunCharge gun, World world,
                                    EntityLivingBase shooter, ItemStack stack,
                                    float spread, float charge, int ammo,
                                    EnumBulletFirePos firePos) {
        return GunStatModifiers.damage(stack,
                ((GunStatsBridge) gun).tgu$baseDamage()) * TGU_RANDOM_DAMAGE.get();
    }

    @Redirect(
            method = "spawnChargedProjectile",
            at = @At(value = "FIELD",
                    target = "Ltechguns/items/guns/GenericGunCharge;damageMin:F"),
            require = 1
    )
    private float tgu$chargedMinimumDamage(GenericGunCharge gun, World world,
                                           EntityLivingBase shooter, ItemStack stack,
                                           float spread, float charge, int ammo,
                                           EnumBulletFirePos firePos) {
        return GunStatModifiers.damage(stack,
                ((GunStatsBridge) gun).tgu$baseMinimumDamage()) * TGU_RANDOM_DAMAGE.get();
    }

    @Redirect(
            method = "spawnChargedProjectile",
            at = @At(value = "FIELD",
                    target = "Ltechguns/items/guns/GenericGunCharge;damageDropStart:F"),
            require = 1
    )
    private float tgu$chargedRangeStart(GenericGunCharge gun, World world,
                                        EntityLivingBase shooter, ItemStack stack,
                                        float spread, float charge, int ammo,
                                        EnumBulletFirePos firePos) {
        return GunStatModifiers.range(stack,
                ((GunStatsBridge) gun).tgu$baseRangeStart());
    }

    @Redirect(
            method = "spawnChargedProjectile",
            at = @At(value = "FIELD",
                    target = "Ltechguns/items/guns/GenericGunCharge;damageDropEnd:F"),
            require = 1
    )
    private float tgu$chargedRangeEnd(GenericGunCharge gun, World world,
                                      EntityLivingBase shooter, ItemStack stack,
                                      float spread, float charge, int ammo,
                                      EnumBulletFirePos firePos) {
        return GunStatModifiers.range(stack,
                ((GunStatsBridge) gun).tgu$baseRangeEnd());
    }

    @Redirect(
            method = "spawnChargedProjectile",
            at = @At(value = "FIELD",
                    target = "Ltechguns/items/guns/GenericGunCharge;penetration:F"),
            require = 1
    )
    private float tgu$chargedPenetration(GenericGunCharge gun, World world,
                                         EntityLivingBase shooter, ItemStack stack,
                                         float spread, float charge, int ammo,
                                         EnumBulletFirePos firePos) {
        return GunStatModifiers.penetration(stack,
                ((GunStatsBridge) gun).tgu$basePenetration());
    }

    @Redirect(
            method = "spawnChargedProjectile",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/World;func_72838_d(Lnet/minecraft/entity/Entity;)Z",
                    remap = false),
            require = 1
    )
    private boolean tgu$markChargedProjectile(World world, Entity entity) {
        if (entity instanceof techguns.entities.projectiles.GenericProjectile) {
            UpgradeEventHandler.initializeProjectileContext(
                    (techguns.entities.projectiles.GenericProjectile) entity);
        }
        entity.getEntityData().setBoolean("tgu_charged_projectile", true);
        entity.getEntityData().setBoolean("tgu_charged_stats_preapplied", true);
        entity.getEntityData().setBoolean("tgu_random_damage_preapplied", true);
        if (TGU_RANDOM_DAMAGE.get() != 1.0f) {
            entity.getEntityData().setBoolean("tgu_random_damage_triggered", true);
        }
        entity.getEntityData().setFloat("tgu_charge_ticks", TGU_CHARGE_TICKS.get());
        entity.getEntityData().setBoolean("tgu_fully_charged", TGU_FULLY_CHARGED.get());
        return world.spawnEntity(entity);
    }
}

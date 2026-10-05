package com.stepm.techgunsupgrade.mixin;

import com.stepm.techgunsupgrade.bridge.GunStatsBridge;
import com.stepm.techgunsupgrade.upgrade.GunStatModifiers;
import com.stepm.techgunsupgrade.event.UpgradeEventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ActionResult;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import techguns.entities.projectiles.EnumBulletFirePos;
import techguns.entities.projectiles.GenericProjectile;
import techguns.items.guns.GenericGun;
import techguns.util.InventoryUtil;
import techguns.util.SoundUtil;
import techguns.client.audio.TGSoundCategory;

import java.util.Collections;
import java.util.List;

@Mixin(value = GenericGun.class, remap = false)
public abstract class GenericGunStatsMixin implements GunStatsBridge {

    @Unique private static final ThreadLocal<Float> TGU_RANDOM_DAMAGE =
            new ThreadLocal<Float>() {
                @Override protected Float initialValue() { return 1.0f; }
            };
    @Unique private static final ThreadLocal<Float> TGU_SOUND_VOLUME =
            new ThreadLocal<Float>() {
                @Override protected Float initialValue() { return 1.0f; }
            };

    @Shadow int minFiretime;
    @Shadow int reloadtime;
    @Shadow int clipsize;
    @Shadow float accuracy;
    @Shadow float spread;
    @Shadow float damage;
    @Shadow float damageMin;
    @Shadow float damageDropStart;
    @Shadow float damageDropEnd;
    @Shadow float penetration;
    @Shadow int bulletcount;
    @Shadow boolean shotgun;
    @Shadow boolean burst;
    @Shadow boolean canZoom;
    @Shadow float projectileForwardOffset;
    @Shadow public abstract int getCurrentAmmo(ItemStack stack);
    @Shadow protected abstract void spawnProjectile(World world, EntityLivingBase player,
                                                     ItemStack stack, float spread, float offset,
                                                     float damageBonus, EnumBulletFirePos firePos,
                                                     Entity target);

    @Override
    @Unique
    public float tgu$chargedAccuracy(ItemStack stack, EntityLivingBase shooter) {
        return GunStatModifiers.accuracy(stack, this.accuracy, shooter, this.burst);
    }

    @Override @Unique public int tgu$baseBulletCount() { return this.bulletcount; }
    @Override @Unique public float tgu$baseDamage() { return this.damage; }
    @Override @Unique public float tgu$baseMinimumDamage() { return this.damageMin; }
    @Override @Unique public float tgu$baseRangeStart() { return this.damageDropStart; }
    @Override @Unique public float tgu$baseRangeEnd() { return this.damageDropEnd; }
    @Override @Unique public float tgu$basePenetration() { return this.penetration; }

    @Inject(method = "useAmmo", at = @At("HEAD"), cancellable = true, require = 1)
    private void tgu$keepUnlimitedAmmo(ItemStack stack, int amount,
                                      CallbackInfoReturnable<Integer> cir) {
        if (GunStatModifiers.isUnlimitedAmmo(stack)) {
            cir.setReturnValue(amount);
            return;
        }

        if (GunStatModifiers.shouldSaveAmmo(stack)) {
            cir.setReturnValue(amount);
            return;
        }

        int discountedCost = GunStatModifiers.discountedAmmoCost(stack, amount);
        if (discountedCost != amount) {
            int current = getCurrentAmmo(stack);
            int paid = Math.min(current, discountedCost);
            if (stack.getTagCompound() != null) {
                stack.getTagCompound().setShort("ammo", (short) Math.max(0, current - paid));
            }
            cir.setReturnValue(current >= discountedCost ? amount : current);
        }
    }

    @Inject(method = "shootGunPrimary", at = @At("HEAD"), cancellable = true, require = 1)
    private void tgu$handleCommonJam(ItemStack stack, World world, EntityPlayer player,
                                     boolean zooming, EnumHand hand, Entity target,
                                     CallbackInfo ci) {
        TGU_SOUND_VOLUME.set(GunStatModifiers.soundVolumeMultiplier(stack));
        if (!world.isRemote && player.capabilities.isCreativeMode) {
            UpgradeEventHandler.captureTriggerPull(stack, world.getTotalWorldTime(),
                    getCurrentAmmo(stack));
            UpgradeEventHandler.recordScopeShot(player, stack, zooming);
        }
        int requiredAmmo = GunStatModifiers.requiredAmmoForShot(stack, 1);
        if (!player.capabilities.isCreativeMode && !GunStatModifiers.isUnlimitedAmmo(stack)
                && getCurrentAmmo(stack) < requiredAmmo) {
            TGU_SOUND_VOLUME.remove();
            ci.cancel();
            return;
        }
    }

    @Inject(
            method = "shootGunPrimary",
            at = @At(value = "INVOKE",
                    target = "Ltechguns/items/guns/GenericGun;useAmmo(Lnet/minecraft/item/ItemStack;I)I",
                    shift = At.Shift.BEFORE),
            cancellable = true,
            require = 1
    )
    private void tgu$payAcceptedShot(ItemStack stack, World world, EntityPlayer player,
                                     boolean zooming, EnumHand hand, Entity target,
                                     CallbackInfo ci) {
        if (world.isRemote) return;
        if (GunStatModifiers.shouldJam(stack, world.rand)) {
            TGU_SOUND_VOLUME.remove();
            ci.cancel();
            return;
        }
        if (GunStatModifiers.isBottomlessMagazine(stack)
                && !GunStatModifiers.consumeBottomlessMagazineAmmo(stack, player, 1)) {
            TGU_SOUND_VOLUME.remove();
            ci.cancel();
            return;
        }
        UpgradeEventHandler.captureTriggerPull(stack, world.getTotalWorldTime(),
                getCurrentAmmo(stack));
        UpgradeEventHandler.recordScopeShot(player, stack, zooming);
    }

    @Inject(method = "shootGunPrimary", at = @At("RETURN"), require = 1)
    private void tgu$clearSoundContext(ItemStack stack, World world, EntityPlayer player,
                                       boolean zooming, EnumHand hand, Entity target,
                                       CallbackInfo ci) {
        TGU_SOUND_VOLUME.remove();
    }

    @Redirect(
            method = "tryForcedReload",
            at = @At(value = "INVOKE",
                    target = "Ltechguns/util/InventoryUtil;consumeAmmoPlayer(Lnet/minecraft/entity/player/EntityPlayer;[Lnet/minecraft/item/ItemStack;)Z",
                    ordinal = 0),
            require = 1
    )
    private boolean tgu$consumeExpandedSingleShotReload(EntityPlayer inventoryPlayer,
                                                         ItemStack[] reloadItems,
                                                         ItemStack stack, World world,
                                                         EntityPlayer player, EnumHand hand) {
        int upgradedClip = GunStatModifiers.clipSize(stack, this.clipsize);
        if (this.clipsize == 1 && upgradedClip > 1) {
            int missingRounds = Math.max(1, upgradedClip - Math.max(0, getCurrentAmmo(stack)));
            return GunStatModifiers.consumeInventoryAmmoItems(stack, player, missingRounds);
        }
        return InventoryUtil.consumeAmmoPlayer(inventoryPlayer, reloadItems);
    }

    @Redirect(
            method = "shootGunPrimary",
            at = @At(value = "INVOKE",
                    target = "Ltechguns/util/SoundUtil;playSoundOnEntityGunPosition(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/SoundEvent;FFZZLtechguns/client/audio/TGSoundCategory;)V"),
            require = 3
    )
    private void tgu$applySilencedVolume(World world, Entity entity, SoundEvent sound,
                                         float volume, float pitch, boolean repeat,
                                         boolean distant, TGSoundCategory category) {
        float multiplier = TGU_SOUND_VOLUME.get();
        if (multiplier <= 0.0f) return;
        SoundUtil.playSoundOnEntityGunPosition(world, entity, sound,
                volume * multiplier, pitch, repeat, distant, category);
    }

    @Inject(method = "func_77659_a", at = @At("HEAD"), require = 1)
    private void tgu$recordScopeToggle(World world, EntityPlayer player, EnumHand hand,
                                       CallbackInfoReturnable<ActionResult<ItemStack>> cir) {
        if (!world.isRemote && this.canZoom) {
            UpgradeEventHandler.recordScopeToggle(player, player.getHeldItem(hand));
        }
    }

    @Inject(method = "spawnProjectile", at = @At("HEAD"), require = 1)
    private void tgu$beginProjectileConstruction(World world, EntityLivingBase shooter,
                                                 ItemStack stack, float spread, float offset,
                                                 float damageBonus, EnumBulletFirePos firePos,
                                                 Entity target, CallbackInfo ci) {
        UpgradeEventHandler.beginProjectileConstruction(stack, shooter, target);
        TGU_RANDOM_DAMAGE.set(UpgradeEventHandler.projectileRandomDamageMultiplier(
                stack, world.getTotalWorldTime()));
    }

    @Inject(method = "spawnProjectile", at = @At("RETURN"), require = 1)
    private void tgu$endProjectileConstruction(World world, EntityLivingBase shooter,
                                               ItemStack stack, float spread, float offset,
                                               float damageBonus, EnumBulletFirePos firePos,
                                               Entity target, CallbackInfo ci) {
        UpgradeEventHandler.endProjectileConstruction();
        TGU_RANDOM_DAMAGE.remove();
    }

    @Redirect(
            method = "spawnProjectile",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/World;func_72838_d(Lnet/minecraft/entity/Entity;)Z"),
            require = 1
    )
    private boolean tgu$markPreappliedProjectileDamage(World world, Entity entity) {
        if (entity instanceof GenericProjectile) {
            UpgradeEventHandler.initializeProjectileContext(
                    (GenericProjectile) entity);
        }
        entity.getEntityData().setBoolean("tgu_random_damage_preapplied", true);
        if (TGU_RANDOM_DAMAGE.get() != 1.0f) {
            entity.getEntityData().setBoolean("tgu_random_damage_triggered", true);
        }
        return world.spawnEntity(entity);
    }

    @Inject(method = "shootGun", at = @At("TAIL"), require = 1)
    private void tgu$spawnNativeExtraProjectiles(World world, EntityLivingBase shooter,
                                                 ItemStack stack, float accuracyBonus,
                                                 float damageBonus, int attackType, EnumHand hand,
                                                 EnumBulletFirePos firePos, Entity target,
                                                 CallbackInfo ci) {
        if (world.isRemote) return;
        int extra = UpgradeEventHandler.additionalProjectilesForShot(stack, shooter);
        if (!this.shotgun) {
            extra += Math.max(0, GunStatModifiers.bulletCount(stack, 1) - 1);
        }
        if (extra <= 0) return;

        float projectileSpread = GunStatModifiers.accuracy(stack, this.accuracy,
                shooter, this.burst) * accuracyBonus;
        for (int i = 0; i < extra; i++) {
            spawnProjectile(world, shooter, stack, projectileSpread,
                    this.projectileForwardOffset + i * 0.01f, damageBonus, firePos, target);
        }
    }

    @Inject(method = "getCurrentAmmo", at = @At("HEAD"), cancellable = true, require = 1)
    private void tgu$showUnlimitedAmmoAsFull(ItemStack stack,
                                             CallbackInfoReturnable<Integer> cir) {
        if (GunStatModifiers.isUnlimitedAmmo(stack)) {
            cir.setReturnValue(GunStatModifiers.clipSize(stack, this.clipsize));
        }
    }

    @Inject(method = "getAmmoOnUnload", at = @At("HEAD"), cancellable = true, require = 1)
    private void tgu$doNotDuplicateUnlimitedAmmo(ItemStack stack,
                                                 CallbackInfoReturnable<List<ItemStack>> cir) {
        if (GunStatModifiers.isUnlimitedAmmo(stack)) {
            cir.setReturnValue(Collections.emptyList());
        }
    }

    @Redirect(
        method = "shootGunPrimary",
        at = @At(value = "FIELD", target = "Ltechguns/items/guns/GenericGun;minFiretime:I"),
        require = 1
    )
    private int tgu$fireDelay(GenericGun gun, ItemStack stack, World world, EntityPlayer player,
                              boolean zooming, EnumHand hand, Entity target) {
        return GunStatModifiers.fireDelay(stack, this.minFiretime, player);
    }

    @Redirect(
        method = "shootGun",
        at = @At(value = "FIELD", target = "Ltechguns/items/guns/GenericGun;accuracy:F"),
        require = 1
    )
    private float tgu$accuracy(GenericGun gun, World world, EntityLivingBase player, ItemStack stack,
                               float accuracyBonus, float damageBonus, int type, EnumHand hand,
                               EnumBulletFirePos firePos, Entity target) {
        return GunStatModifiers.accuracy(stack, this.accuracy, player, this.burst);
    }

    @Redirect(
        method = "shootGun",
        at = @At(value = "FIELD", target = "Ltechguns/items/guns/GenericGun;spread:F"),
        require = 1
    )
    private float tgu$spread(GenericGun gun, World world, EntityLivingBase player, ItemStack stack,
                             float accuracyBonus, float damageBonus, int type, EnumHand hand,
                             EnumBulletFirePos firePos, Entity target) {
        return GunStatModifiers.accuracy(stack, this.spread, player, this.burst);
    }

    @Redirect(
        method = "shootGun",
        at = @At(value = "FIELD", target = "Ltechguns/items/guns/GenericGun;bulletcount:I"),
        require = 1
    )
    private int tgu$shotgunBulletCount(GenericGun gun, World world, EntityLivingBase player, ItemStack stack,
                                       float accuracyBonus, float damageBonus, int type, EnumHand hand,
                                       EnumBulletFirePos firePos, Entity target) {
        return GunStatModifiers.bulletCount(stack, this.bulletcount);
    }

    @Redirect(
        method = "spawnProjectile",
        at = @At(value = "FIELD", target = "Ltechguns/items/guns/GenericGun;damage:F"),
        require = 1
    )
    private float tgu$projectileDamage(GenericGun gun, World world, EntityLivingBase player, ItemStack stack,
                                       float spread, float offset, float damageBonus,
                                       EnumBulletFirePos firePos, Entity target) {
        return GunStatModifiers.damage(stack, this.damage) * TGU_RANDOM_DAMAGE.get();
    }

    @Redirect(
        method = "spawnProjectile",
        at = @At(value = "FIELD", target = "Ltechguns/items/guns/GenericGun;damageMin:F"),
        require = 1
    )
    private float tgu$projectileMinimumDamage(GenericGun gun, World world, EntityLivingBase player,
                                              ItemStack stack, float spread, float offset,
                                              float damageBonus, EnumBulletFirePos firePos, Entity target) {
        return GunStatModifiers.damage(stack, this.damageMin) * TGU_RANDOM_DAMAGE.get();
    }

    @Redirect(
        method = "spawnProjectile",
        at = @At(value = "FIELD", target = "Ltechguns/items/guns/GenericGun;damageDropStart:F"),
        require = 1
    )
    private float tgu$projectileRangeStart(GenericGun gun, World world, EntityLivingBase player,
                                           ItemStack stack, float spread, float offset,
                                           float damageBonus, EnumBulletFirePos firePos, Entity target) {
        return GunStatModifiers.range(stack, this.damageDropStart);
    }

    @Redirect(
        method = "spawnProjectile",
        at = @At(value = "FIELD", target = "Ltechguns/items/guns/GenericGun;damageDropEnd:F"),
        require = 1
    )
    private float tgu$projectileRangeEnd(GenericGun gun, World world, EntityLivingBase player,
                                         ItemStack stack, float spread, float offset,
                                         float damageBonus, EnumBulletFirePos firePos, Entity target) {
        return GunStatModifiers.range(stack, this.damageDropEnd);
    }

    @Redirect(
        method = "spawnProjectile",
        at = @At(value = "FIELD", target = "Ltechguns/items/guns/GenericGun;penetration:F"),
        require = 1
    )
    private float tgu$projectilePenetration(GenericGun gun, World world, EntityLivingBase player,
                                            ItemStack stack, float spread, float offset,
                                            float damageBonus, EnumBulletFirePos firePos, Entity target) {
        return GunStatModifiers.penetration(stack, this.penetration);
    }

    @Redirect(
        method = "tryForcedReload",
        at = @At(value = "FIELD", target = "Ltechguns/items/guns/GenericGun;reloadtime:I"),
        require = 1
    )
    private int tgu$reloadTime(GenericGun gun, ItemStack stack, World world,
                               EntityPlayer player, EnumHand hand) {
        return GunStatModifiers.reloadTime(stack, this.reloadtime);
    }

    @Redirect(
        method = "tryForcedReload",
        at = @At(value = "FIELD", target = "Ltechguns/items/guns/GenericGun;minFiretime:I"),
        require = 1
    )
    private int tgu$reloadFireDelay(GenericGun gun, ItemStack stack, World world,
                                    EntityPlayer player, EnumHand hand) {
        return GunStatModifiers.fireDelay(stack, this.minFiretime);
    }

    @Redirect(
        method = "tryForcedReload",
        at = @At(value = "FIELD", target = "Ltechguns/items/guns/GenericGun;clipsize:I"),
        require = 1
    )
    private int tgu$reloadClipSize(GenericGun gun, ItemStack stack, World world,
                                   EntityPlayer player, EnumHand hand) {
        return GunStatModifiers.clipSize(stack, this.clipsize);
    }

    @Redirect(
        method = "isFullyLoaded",
        at = @At(value = "FIELD", target = "Ltechguns/items/guns/GenericGun;clipsize:I"),
        require = 1
    )
    private int tgu$fullClipSize(GenericGun gun, ItemStack stack) {
        return GunStatModifiers.clipSize(stack, this.clipsize);
    }

    @Redirect(
        method = "reloadAmmo(Lnet/minecraft/item/ItemStack;)V",
        at = @At(value = "FIELD", target = "Ltechguns/items/guns/GenericGun;clipsize:I"),
        require = 1
    )
    private int tgu$reloadAmmoClipSize(GenericGun gun, ItemStack stack) {
        return GunStatModifiers.clipSize(stack, this.clipsize);
    }

    @Redirect(
        method = "getPercentAmmoLeft",
        at = @At(value = "FIELD", target = "Ltechguns/items/guns/GenericGun;clipsize:I"),
        require = 1
    )
    private int tgu$ammoBarClipSize(GenericGun gun, ItemStack stack) {
        return GunStatModifiers.clipSize(stack, this.clipsize);
    }
}
package com.stepm.techgunsupgrade.mixin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.stepm.techgunsupgrade.upgrade.GunStatModifiers;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import techguns.items.guns.GenericGunMeleeCharge;

import java.util.UUID;

/** Adds the Common LMB attack-speed effects to Techguns melee weapons. */
@Mixin(value = GenericGunMeleeCharge.class, remap = false)
public abstract class GenericGunMeleeStatsMixin {
    private static final UUID TGU_ATTACK_SPEED =
            UUID.fromString("7a60b4f8-7572-4a74-91b5-acde39d6be31");

    @Inject(method = "getAttributeModifiers", at = @At("RETURN"), cancellable = true, require = 1)
    private void tgu$meleeAttackSpeed(EntityEquipmentSlot slot, ItemStack stack,
                                      CallbackInfoReturnable<Multimap<String, AttributeModifier>> cir) {
        if (slot != EntityEquipmentSlot.MAINHAND) return;

        float multiplier = GunStatModifiers.meleeAttackSpeedMultiplier(stack);
        if (Math.abs(multiplier - 1.0f) < 0.0001f) return;

        Multimap<String, AttributeModifier> result = HashMultimap.create(cir.getReturnValue());
        result.put(SharedMonsterAttributes.ATTACK_SPEED.getName(),
                new AttributeModifier(TGU_ATTACK_SPEED, "Techguns Common attack speed",
                        multiplier - 1.0, 2));
        cir.setReturnValue(result);
    }
}

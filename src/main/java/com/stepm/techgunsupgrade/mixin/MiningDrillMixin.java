package com.stepm.techgunsupgrade.mixin;

import com.stepm.techgunsupgrade.upgrade.GunStatModifiers;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import techguns.items.guns.MiningDrill;

/** Feeds the Rare wide-drill size into Techguns' native area-mining routine. */
@Mixin(value = MiningDrill.class, remap = false)
public abstract class MiningDrillMixin {
    @Inject(method = "getExtraMiningRadius", at = @At("HEAD"), cancellable = true, require = 1)
    private void tgu$rareMiningRadius(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        int areaSize = GunStatModifiers.miningAreaSize(stack);
        if (areaSize > 1) {
            cir.setReturnValue((areaSize - 1) / 2);
        }
    }
}

package com.stepm.techgunsupgrade.mixin;

import com.stepm.techgunsupgrade.TechgunsUpgradeMod;
import com.stepm.techgunsupgrade.apocalypse.ZombieApocalypseManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@SideOnly(Side.CLIENT)
@Mixin(TextureManager.class)
public class MixinRainTexture {

    private static final ResourceLocation VANILLA_RAIN = new ResourceLocation("minecraft", "textures/environment/rain.png");
    private static final ResourceLocation BLOOD_RAIN = new ResourceLocation(TechgunsUpgradeMod.MODID, "textures/environment/blood_rain.png");

    @ModifyVariable(
        method = "bindTexture",
        at = @At("HEAD"),
        argsOnly = true
    )
    private ResourceLocation onBindTexture(ResourceLocation location) {
        if (location != null && location.equals(VANILLA_RAIN) && ZombieApocalypseManager.isApocalypseActive()) {
            return BLOOD_RAIN;
        }
        return location;
    }
}
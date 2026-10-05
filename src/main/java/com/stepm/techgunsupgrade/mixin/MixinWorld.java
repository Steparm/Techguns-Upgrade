package com.stepm.techgunsupgrade.mixin;

import com.stepm.techgunsupgrade.apocalypse.ZombieApocalypseManager;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SideOnly(Side.CLIENT)
@Mixin(World.class)
public class MixinWorld {

    @Shadow
    public WorldInfo getWorldInfo() { return null; }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        World world = (World) (Object) this;
        if (world.isRemote) return;

        boolean isActive = ZombieApocalypseManager.isApocalypseActive();
        WorldInfo info = world.getWorldInfo();

        if (isActive) {
            if (!info.isRaining()) {
                info.setRaining(true);
                info.setRainTime(999999);
            }
            if (info.getRainTime() < 1000) {
                info.setRainTime(999999);
            }
        } else {
            if (info.isRaining()) {
                info.setRaining(false);
                info.setRainTime(0);
            }
        }
    }
}
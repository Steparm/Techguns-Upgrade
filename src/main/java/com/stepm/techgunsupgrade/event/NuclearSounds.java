package com.stepm.techgunsupgrade.event;

import com.stepm.techgunsupgrade.TechgunsUpgradeMod;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Nuclear warning sound based on the public MIT-licensed Oedldoedl Explosives
 * asset and registration pattern. Attribution is in THIRD_PARTY_NOTICES.md.
 */
@Mod.EventBusSubscriber(modid = TechgunsUpgradeMod.MODID)
public final class NuclearSounds {
    private static final ResourceLocation WARNING_ID =
            new ResourceLocation(TechgunsUpgradeMod.MODID, "nuclear_warning");
    public static final SoundEvent NUCLEAR_WARNING =
            new SoundEvent(WARNING_ID).setRegistryName(WARNING_ID);

    private NuclearSounds() {
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().register(NUCLEAR_WARNING);
    }

    public static void playWarning(World world, Vec3d position) {
        if (world == null || world.isRemote || position == null) return;
        world.playSound(null, new BlockPos(position), NUCLEAR_WARNING,
                SoundCategory.BLOCKS, 4.0f, 1.0f);
    }

    /** Short, mixer-safe cue for the final inward charge. */
    public static void playCharge(World world, Vec3d position) {
        if (world == null || world.isRemote || position == null) return;
        world.playSound(null, new BlockPos(position), SoundEvents.BLOCK_END_PORTAL_FRAME_FILL,
                SoundCategory.BLOCKS, 2.0f, 0.68f);
    }
}

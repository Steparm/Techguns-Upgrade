package com.stepm.techgunsupgrade.event;

import com.stepm.techgunsupgrade.TechgunsUpgradeMod;
import com.stepm.techgunsupgrade.debug.SafeModeAccess;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

/**
 * Makes every standard explosion terrain-safe while the operator safe mode is
 * enabled. Only the affected block list is cleared; affected entities remain
 * untouched, so damage, knockback, sound and particles still execute normally.
 */
@Mod.EventBusSubscriber(modid = TechgunsUpgradeMod.MODID)
public final class SafeModeTerrainGuard {
    private SafeModeTerrainGuard() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        EntityLivingBase owner = event.getExplosion().getExplosivePlacedBy();
        protectTerrain(event.getWorld().isRemote, event.getAffectedBlocks(), owner);
    }

    static void protectTerrain(boolean remote, List<BlockPos> affectedBlocks) {
        protectTerrain(remote, affectedBlocks, null);
    }

    static void protectTerrain(boolean remote, List<BlockPos> affectedBlocks,
                               EntityLivingBase owner) {
        if (remote || SafeModeAccess.canDamageBlocks(owner) || affectedBlocks == null) return;
        affectedBlocks.clear();
    }
}

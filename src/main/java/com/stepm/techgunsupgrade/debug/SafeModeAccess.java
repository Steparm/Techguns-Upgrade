package com.stepm.techgunsupgrade.debug;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import techguns.capabilities.TGExtendedPlayer;

/**
 * Resolves the effective terrain-safe state for upgrade effects.
 *
 * <p>Techguns stores the inventory toggle per player, while {@link DebugSettings}
 * exposes the server-wide operator override used by {@code /tgu safe}. Upgrade
 * effects must honour both switches.</p>
 */
public final class SafeModeAccess {
    private SafeModeAccess() {
    }

    public static boolean isSafeMode(Entity owner) {
        if (DebugSettings.isSafeMode()) return true;
        if (!(owner instanceof EntityPlayer)) return false;

        TGExtendedPlayer properties = TGExtendedPlayer.get((EntityPlayer) owner);
        return properties != null && properties.enableSafemode;
    }

    public static boolean canDamageBlocks(Entity owner) {
        return !isSafeMode(owner);
    }
}

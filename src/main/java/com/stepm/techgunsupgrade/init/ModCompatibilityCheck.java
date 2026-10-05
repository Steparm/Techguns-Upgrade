package com.stepm.techgunsupgrade.init;

import com.stepm.techgunsupgrade.TechgunsUpgradeMod;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ModCompatibilityCheck {

    public static void check(FMLPreInitializationEvent event) {
        ModContainer techguns = Loader.instance().getIndexedModList().get("techguns");
        if (techguns == null) {
            TechgunsUpgradeMod.LOGGER.warn("Techguns not found, skipping compatibility check");
            return;
        }

        String version = techguns.getVersion();
        if (!isCompatible(version)) {
            TechgunsUpgradeMod.LOGGER.error("Incompatible Techguns version: " + version);
            throw new RuntimeException("Techguns version mismatch");
        }

        TechgunsUpgradeMod.LOGGER.info("Techguns compatibility verified: " + version);
    }

    private static boolean isCompatible(String version) {
        if (version == null || version.isEmpty()) return false;
        if (version.startsWith("2.2.")) return true;
        if (version.startsWith("2.1.")) return true;
        if (version.startsWith("2.3.")) return true;
        return false;
    }
}
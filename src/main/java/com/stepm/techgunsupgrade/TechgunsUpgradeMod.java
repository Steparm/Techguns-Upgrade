package com.stepm.techgunsupgrade;

import com.stepm.techgunsupgrade.command.CommandApocalypse;
import com.stepm.techgunsupgrade.command.CommandTgu;
import com.stepm.techgunsupgrade.debug.DebugSettings;
import com.stepm.techgunsupgrade.init.ModBlocks;
import com.stepm.techgunsupgrade.init.ModCompatibilityCheck;
import com.stepm.techgunsupgrade.init.ModGuiHandler;
import com.stepm.techgunsupgrade.init.ModItems;
import com.stepm.techgunsupgrade.network.NetworkHandler;
import com.stepm.techgunsupgrade.proxy.CommonProxy;
import com.stepm.techgunsupgrade.upgrade.UpgradeRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
    modid = TechgunsUpgradeMod.MODID,
    name = TechgunsUpgradeMod.NAME,
    version = TechgunsUpgradeMod.VERSION,
    dependencies = "required-after:techguns;required-after:mixinbooter@[11.2,)"
)
public class TechgunsUpgradeMod {
    public static final String MODID = "techgunsupgrade";
    public static final String NAME = "Techguns Upgrade";
    public static final String VERSION = "1.0.0.0";
    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @SidedProxy(clientSide = "com.stepm.techgunsupgrade.proxy.ClientProxy", serverSide = "com.stepm.techgunsupgrade.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.Instance(TechgunsUpgradeMod.MODID)
    public static TechgunsUpgradeMod instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModBlocks.register();
        ModItems.register();
        UpgradeRegistry.init();

        ModCompatibilityCheck.check(event);

        proxy.preInit(event);
        LOGGER.info("Techguns Upgrade Station preInit with Mixins!");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new ModGuiHandler());
        NetworkHandler.registerMessages();

        proxy.init(event);
        LOGGER.info("Techguns Upgrade Station initialized with Mixins!");
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        DebugSettings.reset();
        event.registerServerCommand(new CommandTgu());
        event.registerServerCommand(new CommandApocalypse());
        LOGGER.info("Registered /tgu and /apocalypse commands");
    }
}
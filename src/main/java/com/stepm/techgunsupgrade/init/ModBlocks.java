package com.stepm.techgunsupgrade.init;

import com.stepm.techgunsupgrade.TechgunsUpgradeMod;
import com.stepm.techgunsupgrade.blocks.BlockUpgradeTable;
import com.stepm.techgunsupgrade.tileentity.TileEntityUpgradeTable;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber(modid = TechgunsUpgradeMod.MODID)
public class ModBlocks {

    public static Block upgradeTable;

    public static void register() {
        upgradeTable = new BlockUpgradeTable();
        GameRegistry.registerTileEntity(TileEntityUpgradeTable.class, TechgunsUpgradeMod.MODID + ":upgrade_table");
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(upgradeTable);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        ItemBlock itemBlock = new ItemBlock(upgradeTable);
        itemBlock.setRegistryName(upgradeTable.getRegistryName());
        event.getRegistry().register(itemBlock);
    }

}

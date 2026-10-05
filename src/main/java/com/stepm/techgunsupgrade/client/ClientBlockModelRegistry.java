package com.stepm.techgunsupgrade.client;

import com.stepm.techgunsupgrade.TechgunsUpgradeMod;
import com.stepm.techgunsupgrade.init.ModBlocks;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/** Registers the Upgrade Table ItemBlock after the item registry is complete. */
@Mod.EventBusSubscriber(modid = TechgunsUpgradeMod.MODID, value = Side.CLIENT)
public final class ClientBlockModelRegistry {

    private ClientBlockModelRegistry() {
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        Item item = Item.getItemFromBlock(ModBlocks.upgradeTable);
        ModelLoader.setCustomModelResourceLocation(
                item,
                0,
                new ModelResourceLocation(ModBlocks.upgradeTable.getRegistryName(), "inventory"));
    }
}

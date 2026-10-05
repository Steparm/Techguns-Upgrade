package com.stepm.techgunsupgrade.proxy;

import com.stepm.techgunsupgrade.card.gui.GuiCardChoice;
import com.stepm.techgunsupgrade.client.ClientVisualEffectRenderer;
import com.stepm.techgunsupgrade.client.UpgradeTableRenderer;
import com.stepm.techgunsupgrade.init.ModItems;
import com.stepm.techgunsupgrade.network.PacketVisualEffect;
import com.stepm.techgunsupgrade.tileentity.TileEntityUpgradeTable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void handleVisualEffect(PacketVisualEffect message) {
        Minecraft.getMinecraft().addScheduledTask(
                () -> ClientVisualEffectRenderer.render(message));
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        
        ClientRegistry.bindTileEntitySpecialRenderer(
                TileEntityUpgradeTable.class,
                new UpgradeTableRenderer());
        
        registerItemModels();
    }
    
    private void registerItemModels() {
        registerItemModel(ModItems.IRON_TICKET);
        registerItemModel(ModItems.GOLDEN_TICKET);
        registerItemModel(ModItems.DIAMOND_TICKET);
        registerItemModel(ModItems.NETHERITE_TICKET);
        registerItemModel(ModItems.STAR_TICKET);
        registerItemModel(ModItems.CREATIVE_TICKET);
    }
    
    private void registerItemModel(Item item) {
        ModelLoader.setCustomModelResourceLocation(
                item,
                0,
                new ModelResourceLocation(item.getRegistryName(), "inventory")
        );
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

    public static void openCardChoice(com.stepm.techgunsupgrade.card.Card... cards) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player != null && cards != null && cards.length > 0) {
            mc.displayGuiScreen(new GuiCardChoice(mc.player, java.util.Arrays.asList(cards), 0));
        }
    }
}
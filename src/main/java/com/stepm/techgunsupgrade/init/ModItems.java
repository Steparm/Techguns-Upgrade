package com.stepm.techgunsupgrade.init;

import com.stepm.techgunsupgrade.TechgunsUpgradeMod;
import com.stepm.techgunsupgrade.items.ItemTicket;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = TechgunsUpgradeMod.MODID)
public class ModItems {

    public static Item IRON_TICKET;
    public static Item GOLDEN_TICKET;
    public static Item DIAMOND_TICKET;
    public static Item NETHERITE_TICKET;
    public static Item STAR_TICKET;
    public static Item CREATIVE_TICKET;

    public static void register() {
        IRON_TICKET = new ItemTicket("ticket_iron", 0, "Iron", TextFormatting.GRAY);
        GOLDEN_TICKET = new ItemTicket("ticket_gold", 1, "Gold", TextFormatting.GOLD);
        DIAMOND_TICKET = new ItemTicket("ticket_diamond", 2, "Diamond", TextFormatting.AQUA);
        NETHERITE_TICKET = new ItemTicket("ticket_netherite", 3, "Netherite", TextFormatting.DARK_RED);
        STAR_TICKET = new ItemTicket("ticket_nether_star", 4, "Nether Star", TextFormatting.LIGHT_PURPLE);
        CREATIVE_TICKET = new ItemTicket("ticket_creative", 5, "Creative", TextFormatting.DARK_PURPLE);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
            IRON_TICKET, GOLDEN_TICKET, DIAMOND_TICKET,
            NETHERITE_TICKET, STAR_TICKET, CREATIVE_TICKET
        );
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        registerModel(IRON_TICKET);
        registerModel(GOLDEN_TICKET);
        registerModel(DIAMOND_TICKET);
        registerModel(NETHERITE_TICKET);
        registerModel(STAR_TICKET);
        registerModel(CREATIVE_TICKET);
    }

    @SideOnly(Side.CLIENT)
    private static void registerModel(Item item) {
        ModelLoader.setCustomModelResourceLocation(
            item,
            0,
            new ModelResourceLocation(item.getRegistryName(), "inventory")
        );
    }
}
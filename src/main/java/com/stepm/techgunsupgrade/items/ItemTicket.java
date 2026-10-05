package com.stepm.techgunsupgrade.items;

import com.stepm.techgunsupgrade.TechgunsUpgradeMod;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemTicket extends Item {

    private final int tier;
    private final String ticketName;
    private final TextFormatting color;

    public ItemTicket(String name, int tier, String ticketName, TextFormatting color) {
        setRegistryName(TechgunsUpgradeMod.MODID, name);
        setTranslationKey(TechgunsUpgradeMod.MODID + "." + name);
        setCreativeTab(CreativeTabs.MISC);
        setMaxStackSize(16);
        this.tier = tier;
        this.ticketName = ticketName;
        this.color = color;
    }

    public int getTier() {
        return tier;
    }

    public String getTicketName() {
        return ticketName;
    }

    public TextFormatting getColor() {
        return color;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(color + I18n.format("item.techgunsupgrade.ticket.tooltip_tier", getTicketName()));
        tooltip.add(TextFormatting.GRAY + I18n.format("item.techgunsupgrade.ticket.tooltip_use"));
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }
}
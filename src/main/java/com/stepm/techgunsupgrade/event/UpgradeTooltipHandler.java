package com.stepm.techgunsupgrade.event;

import com.stepm.techgunsupgrade.TechgunsUpgradeMod;
import com.stepm.techgunsupgrade.upgrade.UpgradeBuff;
import com.stepm.techgunsupgrade.upgrade.UpgradeData;
import com.stepm.techgunsupgrade.upgrade.UpgradeRarity;
import com.stepm.techgunsupgrade.upgrade.GunStatModifiers;
import com.stepm.techgunsupgrade.upgrade.WeaponUpgrades;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = TechgunsUpgradeMod.MODID)
public final class UpgradeTooltipHandler {
    private UpgradeTooltipHandler() {
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;
        List<String> upgradeIds = UpgradeData.getUpgrades(stack);
        if (upgradeIds.isEmpty()) return;

        List<String> block = new ArrayList<>();
        block.add("\u00A76" + I18n.format("gui.techgunsupgrade.upgrades") + " (" + upgradeIds.size() + "/2):");
        
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("techgunsupgrade", 10)) {
            NBTTagCompound stats = stack.getTagCompound().getCompoundTag("techgunsupgrade");
            if (stats.hasKey("base_min_firetime")) {
                int delay = GunStatModifiers.fireDelay(stack,
                        stats.getInteger("base_min_firetime"));
                block.add("\u00A77" + I18n.format("gui.techgunsupgrade.fire_interval") + ": \u00A7f" + delay
                        + " " + I18n.format("gui.techgunsupgrade.ticks") + " (" + trim(20.0f / Math.max(1, delay)) + "/s)");
            }
            if (GunStatModifiers.isInfiniteFuel(stack)) {
                block.add("\u00A77" + I18n.format("gui.techgunsupgrade.ammo_mode") + ": \u00A7f" + I18n.format("gui.techgunsupgrade.infinite_fuel"));
            } else if (GunStatModifiers.isBottomlessMagazine(stack)) {
                block.add("\u00A77" + I18n.format("gui.techgunsupgrade.ammo_mode") + ": \u00A7f" + I18n.format("gui.techgunsupgrade.no_reload"));
            }
        }
        
        for (String upgradeId : upgradeIds) {
            UpgradeBuff buff = WeaponUpgrades.getUpgradeById(upgradeId);
            if (buff == null) continue;
            String rarityColor = getRarityColor(buff.getRarity());
            String rarityName = buff.getLocalizedRarity();
            String displayName = buff.getLocalizedDisplayName();
            block.add(rarityColor + "\u2022 " + displayName + " \u00A78[" + rarityName + "]");
            
            String desc = buff.getLocalizedDescription();
            block.addAll(Minecraft.getMinecraft().fontRenderer
                    .listFormattedStringToWidth("\u00A77" + desc, 260));
            
            if (buff.isStacking() && buff.getMaxStack() > 0) {
                block.add("\u00A77" + I18n.format("gui.techgunsupgrade.max_stack") + ": \u00A7f" + buff.getMaxStack());
            }
        }

        boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)
                || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        if (shift) {
            appendRuntime(block, stack);
        } else {
            block.add("\u00A78" + I18n.format("gui.techgunsupgrade.hold_shift"));
        }

        List<String> tooltip = event.getToolTip();
        int insertAt = Math.min(1, tooltip.size());
        tooltip.addAll(insertAt, block);
    }

    private static void appendRuntime(List<String> tooltip, ItemStack stack) {
        if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("techgunsupgrade", 10)) {
            return;
        }
        NBTTagCompound tag = stack.getTagCompound().getCompoundTag("techgunsupgrade");
        tooltip.add("\u00A76" + I18n.format("gui.techgunsupgrade.live_progress") + ":");
        int shot = tag.getInteger("runtime_shot_sequence");
        int magazineShot = tag.getInteger("runtime_magazine_shot");
        if (shot > 0) tooltip.add("\u00A77" + I18n.format("gui.techgunsupgrade.shots") + ": \u00A7f" + shot);
        if (magazineShot > 0) tooltip.add("\u00A77" + I18n.format("gui.techgunsupgrade.current_magazine") + ": \u00A7f" + magazineShot);
        if (GunStatModifiers.isBottomlessMagazine(stack)) {
            tooltip.add("\u00A77" + I18n.format("gui.techgunsupgrade.loaded_reserve") + ": \u00A7f" + GunStatModifiers.bottomlessReserve(stack));
        }

        if (tag.hasKey("periodic_damage_specs", 10)) {
            NBTTagCompound specs = tag.getCompoundTag("periodic_damage_specs");
            for (String id : specs.getKeySet()) {
                int interval = specs.getCompoundTag(id).getInteger("interval");
                if (interval > 0) {
                    int progress = shot % interval;
                    tooltip.add("\u00A77" + I18n.format("gui.techgunsupgrade.nth_shot") + " " + id + ": \u00A7f" + progress
                            + "/" + interval);
                }
            }
        }
        int streakRequired = tag.getInteger("hit_streak_required");
        if (streakRequired > 0) {
            tooltip.add("\u00A77" + I18n.format("gui.techgunsupgrade.hit_streak") + ": \u00A7f" + tag.getInteger("runtime_hit_streak")
                    + "/" + streakRequired);
        }
        if (tag.hasKey("mythic_stack_specs", 10)) {
            NBTTagCompound specs = tag.getCompoundTag("mythic_stack_specs");
            NBTTagCompound progress = tag.getCompoundTag("mythic_stack_progress");
            for (String id : specs.getKeySet()) {
                NBTTagCompound spec = specs.getCompoundTag(id);
                tooltip.add("\u00A77" + spec.getString("stat") + ": \u00A7f+"
                        + trim(progress.getFloat(id)) + "%/" + trim(spec.getFloat("max")) + "%");
            }
        }
        if (Minecraft.getMinecraft().world != null) {
            long now = Minecraft.getMinecraft().world.getTotalWorldTime();
            appendCooldown(tooltip, tag, "runtime_scope_start_tick", I18n.format("gui.techgunsupgrade.scope_charge"), now, false);
            appendCooldown(tooltip, tag, "runtime_ultra_nuclear_start", I18n.format("gui.techgunsupgrade.nuclear_charge"), now, false);
        }
    }

    private static void appendCooldown(List<String> tooltip, NBTTagCompound tag, String key,
                                       String label, long now, boolean until) {
        if (!tag.hasKey(key)) return;
        long ticks = until ? Math.max(0, tag.getLong(key) - now)
                : Math.max(0, now - tag.getLong(key));
        tooltip.add("\u00A77" + label + ": \u00A7f" + trim(ticks / 20.0f) + "s");
    }

    private static String trim(float value) {
        return Math.abs(value - Math.round(value)) < 0.001f
                ? Integer.toString(Math.round(value)) : String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    private static String getRarityColor(UpgradeRarity rarity) {
        if (rarity == null) return "\u00A7f";
        switch (rarity) {
            case COMMON: return "\u00A7a";
            case UNCOMMON: return "\u00A7b";
            case RARE: return "\u00A7d";
            case EPIC: return "\u00A75";
            case LEGENDARY: return "\u00A76";
            case MYTHIC: return "\u00A7c";
            case ULTRA_MYTHIC: return "\u00A7e";
            default: return "\u00A7f";
        }
    }
}
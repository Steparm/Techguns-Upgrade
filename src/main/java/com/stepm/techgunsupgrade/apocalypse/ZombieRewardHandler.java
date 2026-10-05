package com.stepm.techgunsupgrade.apocalypse;

import com.stepm.techgunsupgrade.init.ModItems;
import net.minecraft.item.ItemStack;

import java.util.Random;

public class ZombieRewardHandler {

    private static final Random RANDOM = new Random();

    public static boolean rollForTicket(int wave) {
        float chance = calculateChance(wave);
        return RANDOM.nextFloat() < chance;
    }

    private static float calculateChance(int wave) {
        if (wave <= 5) return 0.02f;
        if (wave <= 10) return 0.03f;
        if (wave <= 15) return 0.04f;
        if (wave <= 20) return 0.05f;
        if (wave <= 30) return 0.06f;
        if (wave <= 50) return 0.08f;
        return 0.10f;
    }

    public static ItemStack getTicketForWave(int wave) {
        float roll = RANDOM.nextFloat();

        if (wave <= 5) {
            if (roll < 0.97f) return new ItemStack(ModItems.IRON_TICKET);
            if (roll < 0.995f) return new ItemStack(ModItems.GOLDEN_TICKET);
            return new ItemStack(ModItems.DIAMOND_TICKET);
        }

        if (wave <= 10) {
            if (roll < 0.75f) return new ItemStack(ModItems.IRON_TICKET);
            if (roll < 0.93f) return new ItemStack(ModItems.GOLDEN_TICKET);
            if (roll < 0.99f) return new ItemStack(ModItems.DIAMOND_TICKET);
            return new ItemStack(ModItems.NETHERITE_TICKET);
        }

        if (wave <= 15) {
            if (roll < 0.55f) return new ItemStack(ModItems.IRON_TICKET);
            if (roll < 0.80f) return new ItemStack(ModItems.GOLDEN_TICKET);
            if (roll < 0.93f) return new ItemStack(ModItems.DIAMOND_TICKET);
            if (roll < 0.99f) return new ItemStack(ModItems.NETHERITE_TICKET);
            return new ItemStack(ModItems.STAR_TICKET);
        }

        if (wave <= 20) {
            if (roll < 0.40f) return new ItemStack(ModItems.IRON_TICKET);
            if (roll < 0.65f) return new ItemStack(ModItems.GOLDEN_TICKET);
            if (roll < 0.83f) return new ItemStack(ModItems.DIAMOND_TICKET);
            if (roll < 0.93f) return new ItemStack(ModItems.NETHERITE_TICKET);
            if (roll < 0.99f) return new ItemStack(ModItems.STAR_TICKET);
            return new ItemStack(ModItems.CREATIVE_TICKET);
        }

        if (wave <= 30) {
            if (roll < 0.30f) return new ItemStack(ModItems.IRON_TICKET);
            if (roll < 0.52f) return new ItemStack(ModItems.GOLDEN_TICKET);
            if (roll < 0.72f) return new ItemStack(ModItems.DIAMOND_TICKET);
            if (roll < 0.87f) return new ItemStack(ModItems.NETHERITE_TICKET);
            if (roll < 0.97f) return new ItemStack(ModItems.STAR_TICKET);
            return new ItemStack(ModItems.CREATIVE_TICKET);
        }

        if (wave <= 50) {
            if (roll < 0.20f) return new ItemStack(ModItems.IRON_TICKET);
            if (roll < 0.38f) return new ItemStack(ModItems.GOLDEN_TICKET);
            if (roll < 0.58f) return new ItemStack(ModItems.DIAMOND_TICKET);
            if (roll < 0.76f) return new ItemStack(ModItems.NETHERITE_TICKET);
            if (roll < 0.91f) return new ItemStack(ModItems.STAR_TICKET);
            return new ItemStack(ModItems.CREATIVE_TICKET);
        }

        if (roll < 0.12f) return new ItemStack(ModItems.IRON_TICKET);
        if (roll < 0.24f) return new ItemStack(ModItems.GOLDEN_TICKET);
        if (roll < 0.40f) return new ItemStack(ModItems.DIAMOND_TICKET);
        if (roll < 0.60f) return new ItemStack(ModItems.NETHERITE_TICKET);
        if (roll < 0.80f) return new ItemStack(ModItems.STAR_TICKET);
        return new ItemStack(ModItems.CREATIVE_TICKET);
    }
}
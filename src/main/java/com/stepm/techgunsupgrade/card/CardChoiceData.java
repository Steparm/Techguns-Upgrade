package com.stepm.techgunsupgrade.card;

import net.minecraft.entity.player.EntityPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CardChoiceData {
    private static final Map<UUID, List<Card>> pendingCards = new HashMap<>();

    public static void setPendingCards(EntityPlayer player, List<Card> cards) {
        pendingCards.put(player.getUniqueID(), cards);
    }

    public static List<Card> getPendingCards(EntityPlayer player) {
        return pendingCards.get(player.getUniqueID());
    }

    public static void clearPendingCards(EntityPlayer player) {
        pendingCards.remove(player.getUniqueID());
    }
}
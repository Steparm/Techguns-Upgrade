package com.stepm.techgunsupgrade.card;

import com.stepm.techgunsupgrade.TechgunsUpgradeMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.*;

public class CardVotingManager {

    private static final Map<UUID, List<Card>> playerCards = new HashMap<>();
    private static final Map<UUID, Integer> playerVotes = new HashMap<>();
    private static final Map<UUID, Card> selectedCards = new HashMap<>();
    private static final Map<String, Integer> voteCounts = new HashMap<>();
    private static Card winningCard = null;
    private static boolean votingComplete = false;
    private static int votingTimeout = 0;
    private static final int VOTING_TIMEOUT_MAX = 600;

    public static void startVoting(EntityPlayer player, int wave) {
        if (player == null) return;

        UUID uuid = player.getUniqueID();
        List<Card> cards = CardManager.generateCardChoices(wave, 3);
        playerCards.put(uuid, cards);
        playerVotes.put(uuid, -1);
        votingComplete = false;
        winningCard = null;
        voteCounts.clear();
        selectedCards.clear();
        votingTimeout = VOTING_TIMEOUT_MAX;
    }

    public static void setPlayerCards(EntityPlayer player, List<Card> cards) {
        if (player != null && cards != null) {
            playerCards.put(player.getUniqueID(), cards);
            playerVotes.put(player.getUniqueID(), -1);
        }
    }

    public static void castVote(EntityPlayer player, int cardIndex) {
        if (player == null) return;

        UUID uuid = player.getUniqueID();
        List<Card> cards = playerCards.get(uuid);

        if (cards == null || cardIndex < 0 || cardIndex >= cards.size()) {
            TechgunsUpgradeMod.LOGGER.warn("Player {} tried to vote for invalid card", player.getName());
            return;
        }

        if (playerVotes.getOrDefault(uuid, -1) != -1) {
            TechgunsUpgradeMod.LOGGER.warn("Player {} already voted", player.getName());
            return;
        }

        playerVotes.put(uuid, cardIndex);
        selectedCards.put(uuid, cards.get(cardIndex));

        checkAllVoted();
    }

    private static void checkAllVoted() {
        boolean allVoted = true;
        for (Map.Entry<UUID, Integer> entry : playerVotes.entrySet()) {
            if (entry.getValue() == -1) {
                allVoted = false;
                break;
            }
        }

        if (allVoted) {
            calculateWinner();
        }
    }

    public static void calculateWinner() {
        voteCounts.clear();

        for (Map.Entry<UUID, Integer> entry : playerVotes.entrySet()) {
            int cardIndex = entry.getValue();
            if (cardIndex != -1) {
                UUID playerUUID = entry.getKey();
                Card selectedCard = selectedCards.get(playerUUID);
                if (selectedCard != null) {
                    String cardId = selectedCard.getId();
                    voteCounts.put(cardId, voteCounts.getOrDefault(cardId, 0) + 1);
                }
            }
        }

        int maxVotes = 0;
        String winningCardId = null;

        for (Map.Entry<String, Integer> entry : voteCounts.entrySet()) {
            if (entry.getValue() > maxVotes) {
                maxVotes = entry.getValue();
                winningCardId = entry.getKey();
            }
        }

        List<String> tiedCards = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : voteCounts.entrySet()) {
            if (entry.getValue() == maxVotes) {
                tiedCards.add(entry.getKey());
            }
        }

        if (tiedCards.size() > 1) {
            winningCardId = tiedCards.get(new Random().nextInt(tiedCards.size()));
        }

        for (Map.Entry<UUID, Card> entry : selectedCards.entrySet()) {
            if (entry.getValue().getId().equals(winningCardId)) {
                winningCard = entry.getValue();
                break;
            }
        }

        if (winningCard != null) {
            applyToAllPlayers(winningCard);
            votingComplete = true;
        }
    }

    private static void applyToAllPlayers(Card card) {
        for (Map.Entry<UUID, Card> entry : selectedCards.entrySet()) {
            EntityPlayer player = getPlayerByUUID(entry.getKey());
            if (player != null) {
                CardManager.applyCardEffect(player, card);
            }
        }
    }

    private static EntityPlayer getPlayerByUUID(UUID uuid) {
        if (FMLCommonHandler.instance().getMinecraftServerInstance() == null) return null;
        for (EntityPlayer player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
            if (player.getUniqueID().equals(uuid)) {
                return player;
            }
        }
        return null;
    }

    public static boolean isVotingComplete() {
        return votingComplete;
    }

    public static Card getWinningCard() {
        return winningCard;
    }

    public static void resetVoting() {
        playerCards.clear();
        playerVotes.clear();
        selectedCards.clear();
        voteCounts.clear();
        winningCard = null;
        votingComplete = false;
        votingTimeout = VOTING_TIMEOUT_MAX;
    }

    public static void updateTimeout() {
        if (!votingComplete && votingTimeout > 0) {
            votingTimeout--;
            if (votingTimeout <= 0) {
                if (selectedCards.isEmpty()) {
                    List<Card> firstCards = playerCards.values().iterator().next();
                    if (firstCards != null && !firstCards.isEmpty()) {
                        winningCard = firstCards.get(new Random().nextInt(firstCards.size()));
                        applyToAllPlayers(winningCard);
                        votingComplete = true;
                    }
                } else {
                    calculateWinner();
                }
            }
        }
    }

    public static List<Card> getPlayerCards(EntityPlayer player) {
        if (player == null) return new ArrayList<>();
        return playerCards.getOrDefault(player.getUniqueID(), new ArrayList<>());
    }

    public static boolean hasVoted(EntityPlayer player) {
        if (player == null) return false;
        return playerVotes.getOrDefault(player.getUniqueID(), -1) != -1;
    }

    public static int getVoteCount(Card card) {
        if (card == null) return 0;
        return voteCounts.getOrDefault(card.getId(), 0);
    }

    public static Map<String, Integer> getAllVotes() {
        return new HashMap<>(voteCounts);
    }

    public static void forceCompleteVoting() {
        if (!votingComplete) {
            calculateWinner();
        }
    }
}
package com.stepm.techgunsupgrade.card;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

import java.util.*;

public class CardManager {

    private static final Map<UUID, List<Card>> playerCards = new HashMap<>();
    private static final Map<UUID, Map<String, Integer>> playerBonuses = new HashMap<>();
    private static final Map<UUID, List<PotionEffect>> appliedEffects = new HashMap<>();

    public static void applyCardEffect(EntityPlayer player, Card card) {
        if (player == null) return;

        UUID uuid = player.getUniqueID();
        Map<String, Integer> bonuses = playerBonuses.getOrDefault(uuid, new HashMap<>());

        List<Card> cards = playerCards.getOrDefault(uuid, new ArrayList<>());
        cards.add(card);
        playerCards.put(uuid, cards);

        String key = card.getType().name();
        int currentValue = bonuses.getOrDefault(key, 0);
        bonuses.put(key, currentValue + card.getValue());
        playerBonuses.put(uuid, bonuses);

        applyBonusToPlayer(player, card.getType(), card.getValue());
    }

    private static void applyBonusToPlayer(EntityPlayer player, CardType type, int value) {
        UUID uuid = player.getUniqueID();
        List<PotionEffect> effects = appliedEffects.getOrDefault(uuid, new ArrayList<>());

        switch (type) {
            case HEALTH_BONUS:
                float healthBoost = player.getMaxHealth() * (value / 100.0f);
                IAttributeInstance maxHealth = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
                if (maxHealth != null) {
                    AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), "CardHealthBoost", healthBoost, 0);
                    maxHealth.applyModifier(modifier);
                }
                player.setHealth(player.getHealth() + healthBoost);
                break;

            case REGENERATION:
                PotionEffect regen = new PotionEffect(MobEffects.REGENERATION, 999999, Math.max(0, value / 10 - 1));
                player.addPotionEffect(regen);
                effects.add(regen);
                break;

            case SPEED_BONUS:
                PotionEffect speed = new PotionEffect(MobEffects.SPEED, 999999, Math.max(0, value / 10 - 1));
                player.addPotionEffect(speed);
                effects.add(speed);
                break;

            case ARMOR_BONUS:
                PotionEffect resistance = new PotionEffect(MobEffects.RESISTANCE, 999999, Math.max(0, value / 15 - 1));
                player.addPotionEffect(resistance);
                effects.add(resistance);
                break;

            case DAMAGE_BONUS:
                PotionEffect strength = new PotionEffect(MobEffects.STRENGTH, 999999, Math.max(0, value / 10 - 1));
                player.addPotionEffect(strength);
                effects.add(strength);
                break;

            case LIFE_STEAL:
                PotionEffect lifeSteal = new PotionEffect(MobEffects.REGENERATION, 999999, Math.max(0, value / 15 - 1));
                player.addPotionEffect(lifeSteal);
                effects.add(lifeSteal);
                break;

            case CRIT_CHANCE:
                PotionEffect crit = new PotionEffect(MobEffects.STRENGTH, 999999, Math.max(0, value / 15 - 1));
                player.addPotionEffect(crit);
                effects.add(crit);
                break;

            case FIRE_RATE:
                PotionEffect fireRate = new PotionEffect(MobEffects.HASTE, 999999, Math.max(0, value / 10 - 1));
                player.addPotionEffect(fireRate);
                effects.add(fireRate);
                break;

            case DAMAGE_DEBUFF:
                PotionEffect weakness = new PotionEffect(MobEffects.WEAKNESS, 999999, Math.max(0, value / 10 - 1));
                player.addPotionEffect(weakness);
                effects.add(weakness);
                break;

            case HEALTH_DEBUFF:
                float healthReduction = player.getMaxHealth() * (value / 100.0f);
                IAttributeInstance maxHealth2 = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
                if (maxHealth2 != null) {
                    AttributeModifier modifier2 = new AttributeModifier(UUID.randomUUID(), "CardHealthDebuff", -healthReduction, 0);
                    maxHealth2.applyModifier(modifier2);
                }
                if (player.getHealth() > player.getMaxHealth()) {
                    player.setHealth(player.getMaxHealth());
                }
                break;

            case SLOW:
                PotionEffect slowness = new PotionEffect(MobEffects.SLOWNESS, 999999, Math.max(0, value / 10 - 1));
                player.addPotionEffect(slowness);
                effects.add(slowness);
                break;

            case WEAKNESS:
                PotionEffect weakness2 = new PotionEffect(MobEffects.WEAKNESS, 999999, Math.max(0, value / 10 - 1));
                player.addPotionEffect(weakness2);
                effects.add(weakness2);
                break;

            case POISON:
                PotionEffect poison = new PotionEffect(MobEffects.POISON, 999999, Math.max(0, value / 15 - 1));
                player.addPotionEffect(poison);
                effects.add(poison);
                break;

            case CURSED:
                PotionEffect curse1 = new PotionEffect(MobEffects.WEAKNESS, 999999, Math.max(0, value / 15 - 1));
                PotionEffect curse2 = new PotionEffect(MobEffects.SLOWNESS, 999999, Math.max(0, value / 15 - 1));
                player.addPotionEffect(curse1);
                player.addPotionEffect(curse2);
                effects.add(curse1);
                effects.add(curse2);
                break;

            default:
                break;
        }

        appliedEffects.put(uuid, effects);
    }

    public static List<Card> getPlayerCards(EntityPlayer player) {
        return playerCards.getOrDefault(player.getUniqueID(), new ArrayList<>());
    }

    public static Map<String, Integer> getPlayerBonuses(EntityPlayer player) {
        return playerBonuses.getOrDefault(player.getUniqueID(), new HashMap<>());
    }

    public static void clearAllCards(EntityPlayer player) {
        if (player == null) return;

        UUID uuid = player.getUniqueID();

        List<PotionEffect> effects = appliedEffects.getOrDefault(uuid, new ArrayList<>());
        for (PotionEffect effect : effects) {
            player.removePotionEffect(effect.getPotion());
        }

        IAttributeInstance maxHealthAttr = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.removeAllModifiers();
        }
        player.setHealth(Math.min(player.getHealth(), player.getMaxHealth()));

        playerCards.remove(uuid);
        playerBonuses.remove(uuid);
        appliedEffects.remove(uuid);
    }

    public static void clearAllPlayers(List<EntityPlayer> players) {
        for (EntityPlayer player : players) {
            clearAllCards(player);
        }
    }

    public static List<Card> generateCardChoices(int wave, int count) {
        List<Card> choices = new ArrayList<>();
        Random random = new Random();

        CardRarity rarity1 = CardRarity.getRandomForWave(wave);
        choices.add(Card.createRandom(rarity1, wave));

        CardRarity rarity2 = CardRarity.getRandomForWave(wave);
        choices.add(Card.createRandom(rarity2, wave));

        if (random.nextFloat() < 0.4) {
            CardType negativeType = CardType.getRandomNegative();
            CardRarity rarity = CardRarity.getRandomForWave(wave);
            int value = 5 + random.nextInt(15) + wave / 10;
            choices.add(new Card(
                "card_" + System.currentTimeMillis() + "_neg",
                rarity,
                negativeType,
                value
            ));
        } else {
            CardRarity rarity3 = CardRarity.getRandomForWave(wave);
            choices.add(Card.createRandom(rarity3, wave));
        }

        Collections.shuffle(choices);
        return choices;
    }
}
package com.stepm.techgunsupgrade.apocalypse;

import com.stepm.techgunsupgrade.TechgunsUpgradeMod;
import com.stepm.techgunsupgrade.card.Card;
import com.stepm.techgunsupgrade.card.CardManager;
import com.stepm.techgunsupgrade.card.CardVotingManager;
import com.stepm.techgunsupgrade.network.NetworkHandler;
import com.stepm.techgunsupgrade.network.PacketOpenCardGui;
import com.stepm.techgunsupgrade.network.PacketSyncApocalypseData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;

@Mod.EventBusSubscriber(modid = TechgunsUpgradeMod.MODID)
public class ZombieApocalypseManager {

    private static boolean isApocalypseActive = false;
    private static int currentWave = 0;
    private static int zombiesAlive = 0;
    private static int totalKills = 0;
    private static int waveCooldown = 0;
    private static int nextWaveDelay = 200;

    private static boolean waitingForVoting = false;
    private static int votingTimeout = 0;
    private static final int VOTING_TIMEOUT_MAX = 600;

    private static boolean clientIsActive = false;
    private static int clientCurrentWave = 0;
    private static int clientTotalKills = 0;
    private static int clientZombiesAlive = 0;

    private static final Map<UUID, Integer> playerKills = new HashMap<>();
    private static final Set<UUID> alivePlayers = new HashSet<>();
    private static final Random RANDOM = new Random();
    private static final Set<String> processedDeaths = new HashSet<>();

    public static void setClientData(boolean isActive, int currentWave, int totalKills, int zombiesAlive) {
        clientIsActive = isActive;
        clientCurrentWave = currentWave;
        clientTotalKills = totalKills;
        clientZombiesAlive = zombiesAlive;
    }

    public static boolean isApocalypseActive() {
        try {
            if (net.minecraft.client.Minecraft.getMinecraft().world != null &&
                net.minecraft.client.Minecraft.getMinecraft().world.isRemote) {
                return clientIsActive;
            }
        } catch (Exception e) {
            return isApocalypseActive;
        }
        return isApocalypseActive;
    }

    public static int getCurrentWave() {
        try {
            if (net.minecraft.client.Minecraft.getMinecraft().world != null &&
                net.minecraft.client.Minecraft.getMinecraft().world.isRemote) {
                return clientCurrentWave;
            }
        } catch (Exception e) {
            return currentWave;
        }
        return currentWave;
    }

    public static int getTotalKills() {
        try {
            if (net.minecraft.client.Minecraft.getMinecraft().world != null &&
                net.minecraft.client.Minecraft.getMinecraft().world.isRemote) {
                return clientTotalKills;
            }
        } catch (Exception e) {
            return totalKills;
        }
        return totalKills;
    }

    public static int getZombiesAlive() {
        try {
            if (net.minecraft.client.Minecraft.getMinecraft().world != null &&
                net.minecraft.client.Minecraft.getMinecraft().world.isRemote) {
                return clientZombiesAlive;
            }
        } catch (Exception e) {
            return zombiesAlive;
        }
        return zombiesAlive;
    }

    public static boolean isApocalypseActiveServer() { return isApocalypseActive; }
    public static int getCurrentWaveServer() { return currentWave; }
    public static int getTotalKillsServer() { return totalKills; }
    public static int getZombiesAliveServer() { return zombiesAlive; }

    public static void onCardSelectionComplete(World world) {
        waitingForVoting = false;
        CardVotingManager.resetVoting();

        if (zombiesAlive <= 0 && waveCooldown > 0) {
            waveCooldown = 0;
        }

        if (zombiesAlive <= 0 && waveCooldown <= 0 && isApocalypseActive) {
            startNextWave(world);
        }

        syncToAllPlayers(world);
    }

    public static boolean canStartApocalypse(World world) {
        if (isApocalypseActive) return false;
        if (world.isRemote) return false;
        return true;
    }

    public static void startApocalypse(World world) {
        if (isApocalypseActive) return;
        if (world.isRemote) return;

        isApocalypseActive = true;
        currentWave = 0;
        totalKills = 0;
        zombiesAlive = 0;
        waveCooldown = 0;
        waitingForVoting = false;
        votingTimeout = 0;
        alivePlayers.clear();
        playerKills.clear();
        processedDeaths.clear();
        CardVotingManager.resetVoting();

        for (EntityPlayer player : world.playerEntities) {
            alivePlayers.add(player.getUniqueID());
            playerKills.put(player.getUniqueID(), 0);
        }

        world.setWorldTime(13000);

        for (EntityPlayer player : world.playerEntities) {
            player.sendMessage(new TextComponentString("§4ZOMBIE APOCALYPSE STARTED!"));
        }

        startNextWave(world);
        syncToAllPlayers(world);
    }

    public static void endApocalypse(World world) {
        if (world == null || world.isRemote) return;
        if (!isApocalypseActive) return;

        for (Entity entity : world.loadedEntityList) {
            if (entity instanceof techguns.entities.npcs.GenericNPC ||
                entity instanceof techguns.entities.npcs.GenericNPCUndead ||
                entity instanceof techguns.entities.npcs.ITGSpawnerNPC ||
                entity instanceof techguns.entities.npcs.AttackHelicopter ||
                entity instanceof techguns.entities.npcs.ZombieSoldier ||
                entity instanceof techguns.entities.npcs.Bandit ||
                entity instanceof techguns.entities.npcs.ZombieMiner ||
                entity instanceof techguns.entities.npcs.ZombieFarmer ||
                entity instanceof techguns.entities.npcs.ZombiePoliceman ||
                entity instanceof techguns.entities.npcs.ZombiePigmanSoldier ||
                entity instanceof techguns.entities.npcs.SkeletonSoldier ||
                entity instanceof techguns.entities.npcs.Commando ||
                entity instanceof techguns.entities.npcs.ArmySoldier ||
                entity instanceof techguns.entities.npcs.StormTrooper ||
                entity instanceof techguns.entities.npcs.Outcast ||
                entity instanceof techguns.entities.npcs.PsychoSteve ||
                entity instanceof techguns.entities.npcs.DictatorDave ||
                entity instanceof techguns.entities.npcs.CyberDemon ||
                entity instanceof techguns.entities.npcs.SuperMutantBasic ||
                entity instanceof techguns.entities.npcs.SuperMutantHeavy ||
                entity instanceof techguns.entities.npcs.SuperMutantElite ||
                entity instanceof techguns.entities.npcs.Ghastling ||
                entity instanceof techguns.entities.npcs.AlienBug) {
                entity.setDead();
            }
        }

        zombiesAlive = 0;
        isApocalypseActive = false;
        waitingForVoting = false;
        votingTimeout = 0;
        world.setWorldTime(0);
        processedDeaths.clear();

        CardManager.clearAllPlayers(world.playerEntities);
        CardVotingManager.resetVoting();

        for (EntityPlayer player : world.playerEntities) {
            int kills = playerKills.getOrDefault(player.getUniqueID(), 0);
            player.sendMessage(new TextComponentString(
                "§6Apocalypse ended! §fYou killed §c" + kills + " §fenemies"
            ));
            player.sendMessage(new TextComponentString(
                "§6Total waves: §f" + currentWave
            ));
        }
        syncToAllPlayers(world);
    }

    private static void syncToAllPlayers(World world) {
        if (world == null || world.isRemote) return;
        PacketSyncApocalypseData packet = new PacketSyncApocalypseData(
            isApocalypseActive,
            currentWave,
            totalKills,
            zombiesAlive
        );
        NetworkHandler.sendToAll(packet);
    }

    private static void startNextWave(World world) {
        currentWave++;
        int zombieCount = calculateZombieCount(currentWave);

        if (currentWave == 50) {
            for (EntityPlayer player : world.playerEntities) {
                player.sendMessage(new TextComponentString("§4WAVE 50: HELICOPTER!"));
            }
            ZombieSpawner.spawnHelicopter(world, 1);
            zombiesAlive = 1;
            syncToAllPlayers(world);
            return;
        }

        if (currentWave == 100) {
            for (EntityPlayer player : world.playerEntities) {
                player.sendMessage(new TextComponentString("§4WAVE 100: TWO HELICOPTERS!"));
            }
            ZombieSpawner.spawnHelicopter(world, 2);
            zombiesAlive = 2;
            syncToAllPlayers(world);
            return;
        }

        for (int i = 0; i < zombieCount; i++) {
            EntityPlayer target = getRandomPlayer(world);
            if (target != null) {
                ZombieSpawner.spawnTechgunsMob(world, target, currentWave);
            }
        }

        zombiesAlive = zombieCount;

        String waveType = (currentWave % 5 == 0) ? "§6BANDIT WAVE" : "§2ZOMBIE WAVE";
        for (EntityPlayer player : world.playerEntities) {
            player.sendMessage(new TextComponentString(
                "§4" + waveType + " §f" + currentWave + "! §c" + zombieCount + " enemies"
            ));
        }
        syncToAllPlayers(world);
    }

    private static int calculateZombieCount(int wave) {
        if (wave % 5 != 0 && wave < 50) {
            if (wave <= 5) return 3 + wave * 1;
            if (wave <= 10) return 8 + (wave - 5) * 2;
            if (wave <= 15) return 18 + (wave - 10) * 2;
            if (wave <= 20) return 28 + (wave - 15) * 3;
            if (wave <= 30) return 43 + (wave - 20) * 4;
            if (wave <= 50) return 83 + (wave - 30) * 5;
            return 183 + (wave - 50) * 6;
        }

        if (wave % 5 == 0 && wave < 50) {
            if (wave <= 10) return 5 + wave / 5 * 1;
            if (wave <= 20) return 7 + (wave / 5 - 2) * 2;
            return 13 + (wave / 5 - 4) * 3;
        }

        if (wave > 50 && wave < 100) {
            return 183 + (wave - 50) * 8;
        }

        return 10;
    }

    public static void onMobDeath(World world, EntityPlayer killer) {
        if (world == null || world.isRemote) return;

        long time = world.getTotalWorldTime();
        String key = (killer != null ? killer.getUniqueID().toString() : "environment") + ":" + (time / 20);
        if (processedDeaths.contains(key)) return;
        processedDeaths.add(key);
        if (processedDeaths.size() > 200) {
            processedDeaths.clear();
        }

        zombiesAlive--;
        if (zombiesAlive < 0) zombiesAlive = 0;
        totalKills++;

        if (killer != null) {
            UUID playerId = killer.getUniqueID();
            playerKills.put(playerId, playerKills.getOrDefault(playerId, 0) + 1);

            if (ZombieRewardHandler.rollForTicket(currentWave)) {
                killer.entityDropItem(ZombieRewardHandler.getTicketForWave(currentWave), 0.0f);
            }
        }

        if (zombiesAlive <= 0) {
            waveCooldown = nextWaveDelay;

            for (EntityPlayer player : world.playerEntities) {
                player.sendMessage(new TextComponentString(
                    "§aWave " + currentWave + " complete!"
                ));
            }

            showCardChoice(world);
        }

        syncToAllPlayers(world);
    }

    private static void showCardChoice(World world) {
        if (world.isRemote) return;

        waitingForVoting = true;
        votingTimeout = VOTING_TIMEOUT_MAX;
        CardVotingManager.resetVoting();

        for (EntityPlayer player : world.playerEntities) {
            if (player instanceof EntityPlayerMP) {
                CardVotingManager.startVoting(player, currentWave);
                List<Card> cards = CardVotingManager.getPlayerCards(player);
                NetworkHandler.INSTANCE.sendTo(new PacketOpenCardGui(currentWave, cards), (EntityPlayerMP) player);
            }
        }
    }

    private static void cleanStuckMobs(World world) {
        if (world == null || world.isRemote) return;

        int removed = 0;
        for (Entity entity : world.loadedEntityList) {
            if (!(entity instanceof EntityLiving)) continue;
            if (!(entity instanceof IMob)) continue;
            if (!entity.isEntityAlive()) continue;

            boolean isTechgunsMob = entity instanceof techguns.entities.npcs.GenericNPC ||
                                    entity instanceof techguns.entities.npcs.GenericNPCUndead ||
                                    entity instanceof techguns.entities.npcs.ITGSpawnerNPC ||
                                    entity instanceof techguns.entities.npcs.AttackHelicopter ||
                                    entity instanceof techguns.entities.npcs.ZombieSoldier ||
                                    entity instanceof techguns.entities.npcs.Bandit ||
                                    entity instanceof techguns.entities.npcs.ZombieMiner ||
                                    entity instanceof techguns.entities.npcs.ZombieFarmer ||
                                    entity instanceof techguns.entities.npcs.ZombiePoliceman ||
                                    entity instanceof techguns.entities.npcs.ZombiePigmanSoldier ||
                                    entity instanceof techguns.entities.npcs.SkeletonSoldier ||
                                    entity instanceof techguns.entities.npcs.Commando ||
                                    entity instanceof techguns.entities.npcs.ArmySoldier ||
                                    entity instanceof techguns.entities.npcs.StormTrooper ||
                                    entity instanceof techguns.entities.npcs.Outcast ||
                                    entity instanceof techguns.entities.npcs.PsychoSteve ||
                                    entity instanceof techguns.entities.npcs.DictatorDave ||
                                    entity instanceof techguns.entities.npcs.CyberDemon ||
                                    entity instanceof techguns.entities.npcs.SuperMutantBasic ||
                                    entity instanceof techguns.entities.npcs.SuperMutantHeavy ||
                                    entity instanceof techguns.entities.npcs.SuperMutantElite ||
                                    entity instanceof techguns.entities.npcs.Ghastling ||
                                    entity instanceof techguns.entities.npcs.AlienBug;

            if (!isTechgunsMob) continue;

            EntityLiving living = (EntityLiving) entity;

            if (living.posY < 0 || living.posY > 300) {
                living.setDead();
                removed++;
                continue;
            }

            if (living.hurtTime > 0) continue;

            if (living.getAttackTarget() == null && living.ticksExisted > 200) {
                boolean hasPlayerNear = false;
                for (EntityPlayer player : world.playerEntities) {
                    if (player.getDistance(living) < 50) {
                        hasPlayerNear = true;
                        break;
                    }
                }
                if (!hasPlayerNear) {
                    living.setDead();
                    removed++;
                }
            }
        }

        if (removed > 0) {
            if (zombiesAlive > 0) {
                zombiesAlive -= removed;
                if (zombiesAlive < 0) zombiesAlive = 0;
            }

            if (zombiesAlive <= 0 && currentWave > 0) {
                waveCooldown = nextWaveDelay;

                for (EntityPlayer player : world.playerEntities) {
                    player.sendMessage(new TextComponentString(
                        "§aWave " + currentWave + " complete!"
                    ));
                }

                showCardChoice(world);
                syncToAllPlayers(world);
            }
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        World world = event.world;
        if (world == null) return;

        if (!world.isRemote && isApocalypseActive) {
            if (world.getTotalWorldTime() % 20 == 0) {
                syncToAllPlayers(world);
            }
        }

        if (world.isRemote) return;
        if (!isApocalypseActive) return;

        if (waitingForVoting) {
            votingTimeout--;
            CardVotingManager.updateTimeout();

            if (votingTimeout <= 0 || CardVotingManager.isVotingComplete()) {
                waitingForVoting = false;
                CardVotingManager.resetVoting();

                waveCooldown = 0;
                if (zombiesAlive <= 0) {
                    startNextWave(world);
                }
                syncToAllPlayers(world);
                return;
            }
            return;
        }

        long time = world.getTotalWorldTime();

        if (time % 20 == 0) {
            if (world.getWorldTime() % 24000 > 13000 || world.getWorldTime() % 24000 < 0) {
                world.setWorldTime(13000);
            }
        }

        if (time % 100 == 0) {
            cleanStuckMobs(world);
        }

        if (zombiesAlive <= 0 && waveCooldown > 0) {
            waveCooldown--;
            if (waveCooldown == 0) {
                startNextWave(world);
            }
        }

        boolean allDead = true;
        for (EntityPlayer player : world.playerEntities) {
            if (player.isEntityAlive() && alivePlayers.contains(player.getUniqueID())) {
                allDead = false;
                break;
            }
        }

        if (allDead && !world.playerEntities.isEmpty()) {
            endApocalypse(world);
        }
    }

    private static EntityPlayer getRandomPlayer(World world) {
        if (world.playerEntities.isEmpty()) return null;
        return world.playerEntities.get(RANDOM.nextInt(world.playerEntities.size()));
    }
}
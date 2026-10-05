package com.stepm.techgunsupgrade.apocalypse;

import com.stepm.techgunsupgrade.TechgunsUpgradeMod;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import techguns.entities.npcs.*;
import techguns.entities.npcs.AttackHelicopter;

import java.util.Random;

public class ZombieSpawner {

    private static final Random RANDOM = new Random();

    private static final Class<? extends EntityLiving>[] ZOMBIE_TYPES = new Class[] {
        ZombieSoldier.class,
        ZombieMiner.class,
        ZombieFarmer.class,
        ZombiePoliceman.class,
        ZombiePigmanSoldier.class,
        SkeletonSoldier.class
    };

    private static final Class<? extends EntityLiving>[] BANDIT_TIER_1 = new Class[] {
        Bandit.class
    };

    private static final Class<? extends EntityLiving>[] BANDIT_TIER_2 = new Class[] {
        ArmySoldier.class,
        Commando.class
    };

    private static final Class<? extends EntityLiving>[] BANDIT_TIER_3 = new Class[] {
        StormTrooper.class,
        Outcast.class
    };

    private static final Class<? extends EntityLiving>[] BANDIT_TIER_4 = new Class[] {
        PsychoSteve.class,
        DictatorDave.class
    };

    private static final Class<? extends EntityLiving>[] ELITE_ZOMBIE_TYPES = new Class[] {
        CyberDemon.class,
        SuperMutantBasic.class,
        SuperMutantHeavy.class
    };

    private static final Class<? extends EntityLiving>[] BOSS_TYPES = new Class[] {
        SuperMutantElite.class
    };

    public static void spawnTechgunsMob(World world, EntityPlayer player, int wave) {
        if (world == null || player == null) return;
        if (world.isRemote) return;

        Class<? extends EntityLiving> mobClass;
        float healthMultiplier = calculateHealthMultiplier(wave);

        if (wave % 5 == 0) {
            mobClass = selectBanditType(wave);
        } else {
            mobClass = selectZombieType(wave);
        }

        if (mobClass == null) {
            spawnVanillaZombie(world, player);
            return;
        }

        try {
            EntityLiving mob = mobClass.getConstructor(World.class).newInstance(world);

            BlockPos spawnPos = findSafeSpawnPosition(world, player);
            if (spawnPos == null) {
                spawnPos = new BlockPos(
                    (int) player.posX + RANDOM.nextInt(20) - 10,
                    (int) player.posY + 1,
                    (int) player.posZ + RANDOM.nextInt(20) - 10
                );
            }

            mob.setPosition(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
            mob.setHealth(mob.getMaxHealth() * healthMultiplier);

            int difficulty = Math.min(10, wave / 2);

            if (mob instanceof GenericNPC) {
                GenericNPC npc = (GenericNPC) mob;
                npc.onSpawnByManager(difficulty);
                npc.setAttackTarget(player);
            } else if (mob instanceof GenericNPCUndead) {
                try {
                    java.lang.reflect.Method method = mob.getClass().getDeclaredMethod("addRandomArmor", int.class);
                    method.setAccessible(true);
                    method.invoke(mob, difficulty);
                } catch (Exception e) {
                    TechgunsUpgradeMod.LOGGER.warn("Failed to set equipment for " + mob.getClass().getSimpleName());
                }
            }

            ((WorldServer) world).spawnEntity(mob);

        } catch (Exception e) {
            TechgunsUpgradeMod.LOGGER.warn("Failed to spawn mob, falling back to vanilla zombie");
            spawnVanillaZombie(world, player);
        }
    }

    private static BlockPos findSafeSpawnPosition(World world, EntityPlayer player) {
        for (int attempt = 0; attempt < 20; attempt++) {
            double angle = RANDOM.nextDouble() * 2 * Math.PI;
            double distance = 15 + RANDOM.nextDouble() * 20;

            int x = (int) (player.posX + Math.cos(angle) * distance);
            int z = (int) (player.posZ + Math.sin(angle) * distance);

            int y = findGroundLevel(world, x, z);

            if (y > 0 && y < 250) {
                BlockPos pos = new BlockPos(x, y, z);
                if (world.isAirBlock(pos) && world.isAirBlock(pos.up()) && world.isAirBlock(pos.up(2))) {
                    return pos;
                }
            }
        }
        return null;
    }

    private static Class<? extends EntityLiving> selectBanditType(int wave) {
        if (wave == 5) {
            return BANDIT_TIER_1[RANDOM.nextInt(BANDIT_TIER_1.length)];
        } else if (wave == 10) {
            return BANDIT_TIER_2[RANDOM.nextInt(BANDIT_TIER_2.length)];
        } else if (wave == 15) {
            return BANDIT_TIER_3[RANDOM.nextInt(BANDIT_TIER_3.length)];
        } else if (wave >= 20) {
            float roll = RANDOM.nextFloat();
            if (roll < 0.25f) return BANDIT_TIER_1[RANDOM.nextInt(BANDIT_TIER_1.length)];
            if (roll < 0.5f) return BANDIT_TIER_2[RANDOM.nextInt(BANDIT_TIER_2.length)];
            if (roll < 0.75f) return BANDIT_TIER_3[RANDOM.nextInt(BANDIT_TIER_3.length)];
            return BANDIT_TIER_4[RANDOM.nextInt(BANDIT_TIER_4.length)];
        }
        return BANDIT_TIER_1[0];
    }

    private static Class<? extends EntityLiving> selectZombieType(int wave) {
        if (wave < 15) {
            return ZOMBIE_TYPES[RANDOM.nextInt(ZOMBIE_TYPES.length)];
        } else if (wave < 25) {
            return RANDOM.nextFloat() < 0.3f ?
                ELITE_ZOMBIE_TYPES[RANDOM.nextInt(ELITE_ZOMBIE_TYPES.length)] :
                ZOMBIE_TYPES[RANDOM.nextInt(ZOMBIE_TYPES.length)];
        } else {
            float roll = RANDOM.nextFloat();
            if (roll < 0.15f) return BOSS_TYPES[RANDOM.nextInt(BOSS_TYPES.length)];
            if (roll < 0.45f) return ELITE_ZOMBIE_TYPES[RANDOM.nextInt(ELITE_ZOMBIE_TYPES.length)];
            return ZOMBIE_TYPES[RANDOM.nextInt(ZOMBIE_TYPES.length)];
        }
    }

    public static void spawnHelicopter(World world, int count) {
        if (world == null || world.isRemote) return;
        if (world.playerEntities.isEmpty()) return;

        try {
            EntityPlayer target = world.playerEntities.get(RANDOM.nextInt(world.playerEntities.size()));
            if (target == null) return;

            for (int i = 0; i < count; i++) {
                AttackHelicopter helicopter = new AttackHelicopter(world);

                double angle = RANDOM.nextDouble() * 2 * Math.PI;
                double distance = 40 + RANDOM.nextDouble() * 30;

                double x = target.posX + Math.cos(angle) * distance;
                double z = target.posZ + Math.sin(angle) * distance;
                double y = 80 + RANDOM.nextInt(20);

                helicopter.setPosition(x, y, z);
                helicopter.setHealth(helicopter.getMaxHealth() * (count == 2 ? 1.5f : 1.0f));
                helicopter.setAttackTarget(target);

                ((WorldServer) world).spawnEntity(helicopter);
            }
        } catch (Exception e) {
            TechgunsUpgradeMod.LOGGER.error("Failed to spawn helicopter", e);
        }
    }

    private static float calculateHealthMultiplier(int wave) {
        if (wave <= 5) return 1.0f;
        if (wave <= 10) return 1.0f + (wave - 5) * 0.1f;
        if (wave <= 15) return 1.5f + (wave - 10) * 0.12f;
        if (wave <= 20) return 2.1f + (wave - 15) * 0.15f;
        if (wave <= 30) return 2.85f + (wave - 20) * 0.1f;
        if (wave <= 50) return 3.85f + (wave - 30) * 0.05f;
        return 4.85f + (wave - 50) * 0.03f;
    }

    private static void spawnVanillaZombie(World world, EntityPlayer player) {
        net.minecraft.entity.monster.EntityZombie zombie = new net.minecraft.entity.monster.EntityZombie(world);
        double x = player.posX + (RANDOM.nextDouble() - 0.5) * 40;
        double z = player.posZ + (RANDOM.nextDouble() - 0.5) * 40;
        double y = getGroundHeight(world, x, z);
        zombie.setPosition(x, y, z);
        ((WorldServer) world).spawnEntity(zombie);
    }

    private static int findGroundLevel(World world, int x, int z) {
        for (int y = 120; y > 5; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (!world.isAirBlock(pos) && world.isAirBlock(pos.up()) && world.isAirBlock(pos.up(2))) {
                return y + 1;
            }
        }
        return -1;
    }

    private static double getGroundHeight(World world, double x, double z) {
        BlockPos pos = new BlockPos((int) x, 120, (int) z);
        while (pos.getY() > 0 && world.isAirBlock(pos)) {
            pos = pos.down();
        }
        return pos.getY() + 1;
    }
}
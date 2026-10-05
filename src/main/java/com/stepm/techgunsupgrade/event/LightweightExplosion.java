package com.stepm.techgunsupgrade.event;

import com.stepm.techgunsupgrade.TechgunsUpgradeMod;
import com.stepm.techgunsupgrade.config.TguConfig;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Bounded secondary explosion simulation. Techguns still owns the primary
 * projectile explosion and terrain damage; upgrade-created follow-up blasts
 * only calculate entities, knockback, sound and a capped particle burst.
 *
 * <p>The separation of an explosion into terrain, entity and presentation
 * stages follows the approach used by the MIT-licensed Oedldoedl Explosives
 * project. This implementation is purpose-built for the upgrade runtime and
 * deliberately avoids Minecraft's expensive affected-block ray march.</p>
 */
@Mod.EventBusSubscriber(modid = TechgunsUpgradeMod.MODID)
public final class LightweightExplosion {
    private static final Map<World, ArrayDeque<Task>> PENDING = new WeakHashMap<>();
    private static final Map<World, Long> LAST_OVERFLOW_WARNING = new WeakHashMap<>();
    private static final int HARD_QUEUE_LIMIT = 256;
    private static final int HARD_ENTITY_LIMIT = 96;

    private LightweightExplosion() {
    }

    public static void enqueue(World world, double x, double y, double z, float power) {
        if (world == null || world.isRemote) return;
        ArrayDeque<Task> queue = PENDING.computeIfAbsent(world, ignored -> new ArrayDeque<>());
        int queueLimit = MathHelper.clamp(TguConfig.maxQueuedSecondaryExplosions,
                8, HARD_QUEUE_LIMIT);
        if (queue.size() >= queueLimit) {
            long now = world.getTotalWorldTime();
            long lastWarning = LAST_OVERFLOW_WARNING.getOrDefault(world, Long.MIN_VALUE);
            if (lastWarning == Long.MIN_VALUE || now - lastWarning >= 100L) {
                LAST_OVERFLOW_WARNING.put(world, now);
                TechgunsUpgradeMod.LOGGER.warn("Secondary explosion queue is full in dimension {}; "
                        + "dropping effects to protect server TPS",
                        world.provider.getDimension());
            }
            return;
        }
        queue.addLast(new Task(x, y, z, MathHelper.clamp(power, 0.5f, 20.0f)));
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world == null || event.world.isRemote) return;
        ArrayDeque<Task> queue = PENDING.get(event.world);
        if (queue == null || queue.isEmpty()) return;

        int budget = MathHelper.clamp(TguConfig.maxSecondaryExplosionsPerTick, 1, 12);
        while (budget-- > 0 && !queue.isEmpty()) {
            execute(event.world, queue.removeFirst());
        }
        if (queue.isEmpty()) PENDING.remove(event.world);
    }

    private static void execute(World world, Task task) {
        float diameter = Math.min(24.0f, task.power * 2.0f);
        AxisAlignedBB bounds = new AxisAlignedBB(task.x - diameter, task.y - diameter,
                task.z - diameter, task.x + diameter, task.y + diameter,
                task.z + diameter);
        List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(null, bounds);
        entities.sort(Comparator.comparingDouble(entity -> entity.getDistanceSq(
                task.x, task.y, task.z)));

        Explosion explosion = new Explosion(world, null, task.x, task.y, task.z,
                task.power, false, false);
        int entityLimit = MathHelper.clamp(TguConfig.maxEntitiesPerSecondaryExplosion,
                8, HARD_ENTITY_LIMIT);
        int checked = 0;
        for (Entity entity : entities) {
            if (checked++ >= entityLimit) break;
            if (entity.isImmuneToExplosions()) continue;
            double normalizedDistance = entity.getDistance(task.x, task.y, task.z) / diameter;
            if (normalizedDistance > 1.0) continue;

            double dx = entity.posX - task.x;
            double dy = entity.posY + entity.getEyeHeight() - task.y;
            double dz = entity.posZ - task.z;
            double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (length < 1.0e-4) length = 1.0e-4;
            dx /= length;
            dy /= length;
            dz /= length;

            // Secondary upgrade blasts are deliberately computational. Calling
            // World#getBlockDensity performs many block rays per entity and was
            // the remaining source of integrated-server stalls when Artillery
            // Strike queued several blasts together.
            double impact = 1.0 - normalizedDistance;
            if (impact <= 0.0) continue;
            float damage = (float) (((impact * impact + impact) * 0.5 * 7.0
                    * diameter) + 1.0);
            entity.attackEntityFrom(DamageSource.causeExplosionDamage(explosion), damage);

            double knockback = impact;
            if (entity instanceof EntityLivingBase) {
                knockback = EnchantmentProtection.getBlastDamageReduction(
                        (EntityLivingBase) entity, impact);
            }
            if (!(entity instanceof EntityPlayer)
                    || !((EntityPlayer) entity).capabilities.isFlying) {
                entity.motionX += dx * knockback;
                entity.motionY += dy * knockback;
                entity.motionZ += dz * knockback;
                entity.velocityChanged = true;
            }
        }

        playSound(world, task);
        spawnParticles(world, task);
    }

    private static void playSound(World world, Task task) {
        BlockPos position = new BlockPos(task.x, task.y, task.z);
        float volume = MathHelper.clamp(1.8f + task.power * 0.55f, 2.0f, 8.0f);
        float pitch = MathHelper.clamp(0.82f - task.power * 0.025f, 0.48f, 0.82f);
        world.playSound(null, position, SoundEvents.ENTITY_GENERIC_EXPLODE,
                SoundCategory.BLOCKS, volume, pitch);
        if (task.power >= 3.5f) {
            world.playSound(null, position, SoundEvents.ENTITY_FIREWORK_LARGE_BLAST,
                    SoundCategory.BLOCKS, volume * 0.65f, pitch * 0.82f);
        }
    }

    private static void spawnParticles(World world, Task task) {
        UpgradeVisualEffects.specialBurst(world,
                new Vec3d(task.x, task.y, task.z),
                MathHelper.clamp(task.power * 0.75f, 1.5f, 12.0f),
                UpgradeVisualEffects.SpecialStyle.EXPLOSION);
    }

    private static final class Task {
        private final double x;
        private final double y;
        private final double z;
        private final float power;

        private Task(double x, double y, double z, float power) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.power = power;
        }
    }
}

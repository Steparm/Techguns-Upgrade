package com.stepm.techgunsupgrade.config;

import com.stepm.techgunsupgrade.TechgunsUpgradeMod;
import com.stepm.techgunsupgrade.upgrade.effect.EffectAction;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = TechgunsUpgradeMod.MODID, name = TechgunsUpgradeMod.MODID)
@Mod.EventBusSubscriber(modid = TechgunsUpgradeMod.MODID)
public final class TguConfig {

    @Config.Comment("Allow upgrade-only damage, control and instant-kill effects against players")
    @Config.Name("enablePvpEffects")
    public static boolean enablePvpEffects = false;

    @Config.Comment("Maximum lightweight upgrade explosions processed in one world tick")
    @Config.Name("maxSecondaryExplosionsPerTick")
    @Config.RangeInt(min = 1, max = 12)
    public static int maxSecondaryExplosionsPerTick = 2;

    @Config.Comment("Maximum queued secondary upgrade explosions per world")
    @Config.Name("maxQueuedSecondaryExplosions")
    @Config.RangeInt(min = 8, max = 256)
    public static int maxQueuedSecondaryExplosions = 64;

    @Config.Comment("Maximum entities checked by one secondary explosion")
    @Config.Name("maxEntitiesPerSecondaryExplosion")
    @Config.RangeInt(min = 8, max = 96)
    public static int maxEntitiesPerSecondaryExplosion = 32;

    @Config.Comment("Maximum simultaneous timed Ultra-Mythic areas per world")
    @Config.Name("maxActiveUltraZones")
    @Config.RangeInt(min = 4, max = 64)
    public static int maxActiveUltraZones = 32;

    @Config.Comment("Maximum simultaneous five-second split swarms per world")
    @Config.Name("maxActiveSplitSwarms")
    @Config.RangeInt(min = 1, max = 16)
    public static int maxActiveSplitSwarms = 8;

    @Config.Comment("Maximum persistent ground-fire positions tracked per world")
    @Config.Name("maxTrackedGroundFires")
    @Config.RangeInt(min = 64, max = 2048)
    public static int maxTrackedGroundFires = 512;

    @Config.Comment("Ticks between a Nuclear Death Ray charge and its real damage/crater")
    @Config.Name("nuclearDetonationDelayTicks")
    @Config.RangeInt(min = 6, max = 30)
    public static int nuclearDetonationDelayTicks = 12;

    @Config.Comment("Global per-tick budget for locally rendered upgrade particles")
    @Config.Name("maxClientEffectParticles")
    @Config.RangeInt(min = 300, max = 4000)
    public static int maxClientEffectParticles = 600;

    @Config.Comment("Global multiplier for ALL buff strengths. 1.0 = default, 0.5 = half, 2.0 = double. Range: 0.1 - 10.0")
    @Config.Name("buffMultiplier")
    @Config.RangeDouble(min = 0.1, max = 10.0)
    public static double buffMultiplier = 0.5;

    @Config.Comment("Additional multiplier ONLY for damage buffs (DAMAGE). 1.0 = default, 0.5 = half. Range: 0.1 - 10.0")
    @Config.Name("damageBuffMultiplier")
    @Config.RangeDouble(min = 0.1, max = 10.0)
    public static double damageBuffMultiplier = 1.0;

    @Config.Comment("Additional multiplier ONLY for fire rate buffs (FIRE_RATE). 1.0 = default, 0.5 = half. Range: 0.1 - 10.0")
    @Config.Name("fireRateBuffMultiplier")
    @Config.RangeDouble(min = 0.1, max = 10.0)
    public static double fireRateBuffMultiplier = 1.0;

    public static double getTypeMultiplier(Object type) {
        if (type == null) return 1.0;
        String name = type.toString();
        if (name.equals("DAMAGE")) return damageBuffMultiplier;
        if (name.equals("FIRE_RATE")) return fireRateBuffMultiplier;
        return 1.0;
    }

    public static double getTotalMultiplier(Object type) {
        return buffMultiplier * getTypeMultiplier(type);
    }

    public static double getMultiplierForAction(EffectAction action) {
        if (action == null) return buffMultiplier;
        switch (action) {
            case DAMAGE_MULTIPLIER:
            case CONDITIONAL_DAMAGE_MULTIPLIER:
            case RANDOM_DAMAGE_MULTIPLIER:
            case PERIODIC_SHOT_DAMAGE_MULTIPLIER:
            case BURST_THRESHOLD_DAMAGE_MULTIPLIER:
            case STATIONARY_DAMAGE_MULTIPLIER:
            case SCOPED_DAMAGE_MULTIPLIER:
            case CONSECUTIVE_TARGET_DAMAGE_MULTIPLIER:
            case MULTI_TARGET_DAMAGE_MULTIPLIER:
            case FULL_MAG_TARGET_DAMAGE:
            case HIT_STREAK_DAMAGE:
            case STUNNED_TARGET_DAMAGE:
            case FIRST_SHOT_KILL_NEXT_DAMAGE:
            case CHARGE_THRESHOLD_EXPLOSIVE_DAMAGE:
            case SAME_SHOT_PELLET_DAMAGE:
            case PROJECTILE_DAMAGE_MULTIPLIER:
            case MELEE_DAMAGE_MULTIPLIER:
            case DAMAGE_RAMP:
            case BURN_DAMAGE_MULTIPLIER:
            case POISON_DAMAGE_MULTIPLIER:
            case CHARGED_DAMAGE_MULTIPLIER:
            case CONTINUOUS_TARGET_EXPLOSION:
            case CONTINUOUS_NUCLEAR_EXPLOSION:
                return buffMultiplier * damageBuffMultiplier;

            case FIRE_DELAY_MULTIPLIER:
            case CONDITIONAL_FIRE_DELAY_MULTIPLIER:
            case MELEE_ATTACK_SPEED_MULTIPLIER:
                return buffMultiplier * fireRateBuffMultiplier;

            default:
                return buffMultiplier;
        }
    }

    public static boolean isMultiplicativeBonus(EffectAction action) {
        if (action == null) return false;
        switch (action) {
            case DAMAGE_MULTIPLIER:
            case CONDITIONAL_DAMAGE_MULTIPLIER:
            case RANDOM_DAMAGE_MULTIPLIER:
            case PERIODIC_SHOT_DAMAGE_MULTIPLIER:
            case BURST_THRESHOLD_DAMAGE_MULTIPLIER:
            case STATIONARY_DAMAGE_MULTIPLIER:
            case SCOPED_DAMAGE_MULTIPLIER:
            case CONSECUTIVE_TARGET_DAMAGE_MULTIPLIER:
            case MULTI_TARGET_DAMAGE_MULTIPLIER:
            case FULL_MAG_TARGET_DAMAGE:
            case HIT_STREAK_DAMAGE:
            case STUNNED_TARGET_DAMAGE:
            case FIRST_SHOT_KILL_NEXT_DAMAGE:
            case CHARGE_THRESHOLD_EXPLOSIVE_DAMAGE:
            case SAME_SHOT_PELLET_DAMAGE:
            case PROJECTILE_DAMAGE_MULTIPLIER:
            case MELEE_DAMAGE_MULTIPLIER:
            case DAMAGE_RAMP:
            case RANGE_MULTIPLIER:
            case EXPLOSION_RADIUS_MULTIPLIER:
            case BEAM_WIDTH_MULTIPLIER:
            case PROJECTILE_SPEED_MULTIPLIER:
            case CHARGED_RADIUS_MULTIPLIER:
            case CHARGED_DAMAGE_MULTIPLIER:
            case KNOCKBACK_MULTIPLIER:
                return true;

            case FIRE_DELAY_MULTIPLIER:
            case CONDITIONAL_FIRE_DELAY_MULTIPLIER:
            case MELEE_ATTACK_SPEED_MULTIPLIER:
            case RELOAD_TIME_MULTIPLIER:
            case CHARGE_TIME_MULTIPLIER:
            case SPIN_UP_TIME_MULTIPLIER:
            case LOCK_ON_TIME_MULTIPLIER:
            case AMMO_CONSUMPTION_MULTIPLIER:
            case LMB_AMMO_CONSUMPTION_MULTIPLIER:
            case DETECTION_RADIUS_MULTIPLIER:
            case SPREAD_MULTIPLIER:
            case CONDITIONAL_SPREAD_MULTIPLIER:
            case JAM_CHANCE_MULTIPLIER:
                return true;

            default:
                return false;
        }
    }

    /** Actions where lower is better (weaken the 1-value deviation). */
    public static boolean isInvertedMultiplier(EffectAction action) {
        if (action == null) return false;
        switch (action) {
            case FIRE_DELAY_MULTIPLIER:
            case CONDITIONAL_FIRE_DELAY_MULTIPLIER:
            case MELEE_ATTACK_SPEED_MULTIPLIER:
            case RELOAD_TIME_MULTIPLIER:
            case CHARGE_TIME_MULTIPLIER:
            case SPIN_UP_TIME_MULTIPLIER:
            case LOCK_ON_TIME_MULTIPLIER:
            case AMMO_CONSUMPTION_MULTIPLIER:
            case LMB_AMMO_CONSUMPTION_MULTIPLIER:
            case DETECTION_RADIUS_MULTIPLIER:
            case SPREAD_MULTIPLIER:
            case CONDITIONAL_SPREAD_MULTIPLIER:
            case JAM_CHANCE_MULTIPLIER:
                return true;
            default:
                return false;
        }
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(TechgunsUpgradeMod.MODID)) {
            ConfigManager.sync(TechgunsUpgradeMod.MODID, Config.Type.INSTANCE);
        }
    }
}
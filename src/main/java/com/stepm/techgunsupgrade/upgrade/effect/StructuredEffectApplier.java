package com.stepm.techgunsupgrade.upgrade.effect;

import com.stepm.techgunsupgrade.config.TguConfig;
import net.minecraft.nbt.NBTTagCompound;

/** Writes structured effects to the per-ItemStack runtime data used by hooks. */
public final class StructuredEffectApplier {
    private static final String[] KEYS = {
            "mod_clipsize_mult", "mod_movement_speed", "mod_melee_attack_speed",
            "mod_ammo_consumption", "mod_charge_time", "mod_spinup_time",
            "mod_lock_on_time", "mod_detection_radius", "common_jam_chance",
            "common_close_damage", "common_close_damage_distance", "conditional_damage_specs",
            "common_burst_accuracy", "common_prone_accuracy", "ammo_cost_accumulator",
            "lock_on_bonus_accumulator", "mod_bulletcount_mult", "mod_projectile_speed",
            "mod_explosion_radius", "mod_burn_damage", "mod_poison_damage",
            "ground_fire_bonus_ticks", "random_damage_chance", "random_damage_multiplier",
            "random_damage_specs", "fan_projectile_count", "poison_first_shot_multiplier",
            "poison_consecutive_multiplier", "scoped_damage_consumes_shot",
            "mod_mining_speed", "shockwave_radius"
            , "moving_fire_delay", "incendiary_duration", "explosion_power",
            "random_explosion_chance", "random_explosion_power", "glow_duration",
            "gold_drop_chance", "random_extra_projectile_chance", "random_extra_projectile_count",
            "random_extra_projectile_specs",
            "random_extra_ammo_cost", "fire_zone_duration", "cluster_count", "stun_duration",
            "split_shard_add", "split_again_chance",
            "poison_duration_add", "contagion_chance", "chain_lightning_targets",
            "piercing_count", "radiation_duration", "mod_beam_width", "fuel_restore_chance",
            "fuel_restore_amount", "mining_area_size", "smoke_screen",
            "first_shot_damage", "last_round_damage", "long_range_damage", "long_range_distance",
            "mod_projectile_damage", "mod_jam_chance", "periodic_shot_interval",
            "periodic_damage_specs",
            "periodic_shot_damage", "firing_movement_speed", "burst_damage_threshold",
            "burst_damage_multiplier", "stationary_damage_ticks", "stationary_damage_multiplier",
            "scoped_damage_ticks", "scoped_damage_multiplier", "quiet_kill_radius",
            "consecutive_target_damage", "multi_target_damage", "fire_resistance_bypass",
            "impact_shockwave_strength", "heat_seeking_strength", "impact_smoke_duration",
            "impact_blindness_duration", "impact_blindness_radius", "suppression_duration",
            "suppression_radius", "projectile_range_add", "pull_target_range",
            "kill_attack_speed_multiplier", "kill_attack_speed_duration", "damage_ramp_step",
            "kill_attack_speed_continuous", "kill_cloud_continuous", "kill_tremor_continuous",
            "damage_ramp_interval", "damage_ramp_max", "chain_beam_targets",
            "charged_damage_multiplier", "charged_radius_multiplier", "lmb_ammo_consumption",
            "pyromaniac_explosion_power", "moving_fire_zone_duration", "moving_fire_zone_radius",
            "mod_melee_damage", "burst_damage_until_reload", "periodic_extra_interval",
            "periodic_extra_count", "charged_impact_shockwave", "kill_heal_amount",
            "kill_invisibility_chance", "kill_invisibility_duration",
            "self_explosion_chance", "self_explosion_power", "ammo_save_chance",
            "unlimited_magazine", "instant_charge", "mod_knockback_multiplier",
            "mythic_stack_specs", "mythic_close_damage_distance",
            "random_self_damage_chance", "random_self_damage_fraction",
            "railgun_pierce_range", "impact_explosion_radius", "full_mag_required_shots",
            "full_mag_damage_multiplier", "kill_blind_distance", "kill_blind_radius",
            "kill_blind_duration", "random_damage_stun_duration", "burn_damage_ticks",
            "burn_duration_damage_multiplier", "first_kill_next_damage_multiplier",
            "kill_contagion_chance", "kill_contagion_duration", "kill_chain_targets",
            "ricochet_chance", "ricochet_explosion_power", "ricochet_search_range",
            "hit_streak_required", "hit_streak_damage_multiplier", "periodic_free_ammo_interval",
            "periodic_free_charge_interval",
            "same_shot_pellet_damage", "stunned_target_damage", "charged_threshold_ticks",
            "charged_threshold_damage", "charged_threshold_explosion_radius",
            "cascade_shard_count", "melee_area_chance", "melee_area_damage_multiplier",
            "melee_area_radius", "melee_area_fire_duration", "kill_cloud_chance",
            "kill_cloud_radius", "continuous_explosion_ticks", "continuous_explosion_multiplier",
            "continuous_explosion_radius", "kill_instant_lock", "kill_tremor_chance",
            "kill_tremor_radius", "charged_nuclear_chance", "charged_nuclear_multiplier",
            "charged_nuclear_radius", "headshot_damage_multiplier", "headshot_shockwave",
            "mod_bulletcount_override", "every_shot_lightning", "radial_burst_count",
            "radial_burst_interval", "radial_burst_chance", "radial_burst_range",
            "targeted_burst_count", "targeted_burst_interval", "targeted_burst_chance",
            "targeted_burst_range", "ultra_fire_trail_duration", "ultra_fire_trail_radius",
            "nuclear_impact_chance", "nuclear_impact_radius", "kill_execute_chance",
            "kill_execute_radius", "extra_impact_explosions", "artillery_chance",
            "artillery_count", "artillery_radius", "fire_rain_chance", "fire_rain_count",
            "fire_rain_radius", "charged_extra_projectiles", "acid_cloud_radius",
            "acid_cloud_duration", "chain_all_radius", "kill_lightning",
            "sniper_strike_chance", "sniper_strike_multiplier", "instant_kill_chance",
            "laser_rain_chance", "laser_rain_count", "laser_rain_radius",
            "plasma_rain_chance", "plasma_rain_count", "plasma_rain_radius",
            "periodic_area_interval", "periodic_area_multiplier", "periodic_area_radius",
            "infinite_split_children", "infinite_split_generations", "timed_zone_chance",
            "timed_zone_radius", "timed_zone_duration", "seismic_wave_radius",
            "seismic_wave_knockback", "kill_earthquake_chance", "kill_earthquake_radius",
            "melee_vortex_radius", "melee_instant_kill_chance", "kill_blood_rain_chance",
            "continuous_nuclear_ticks", "continuous_nuclear_multiplier",
            "continuous_nuclear_radius", "nuclear_on_kill", "emp_chance", "emp_radius", "emp_multiplier",
            "instant_lock_on", "through_wall_homing", "continuous_earthquake_radius",
            "random_melee_explosion_chance", "random_melee_explosion_radius",
            "kill_explosion_radius", "sonic_wave_chance", "sonic_wave_radius",
            "sonic_wave_multiplier", "pulse_wave_chance", "pulse_wave_radius",
            "pulse_wave_multiplier", "light_explosion_chance", "light_explosion_radius",
            "light_explosion_multiplier"
            , "block_impact_actions"
    };

    private StructuredEffectApplier() {
    }

    public static void apply(NBTTagCompound tag, UpgradeDefinition definition) {
        for (EffectSpec effect : definition.getEffects()) {
            apply(tag, effect, definition.getId());
        }
    }

    public static void clear(NBTTagCompound tag) {
        for (String key : KEYS) tag.removeTag(key);
    }

    private static void apply(NBTTagCompound tag, EffectSpec effect, String upgradeId) {
        String fragment = effect.getSourceFragment().toLowerCase(java.util.Locale.ROOT);
        if (effect.getTrigger() == EffectTrigger.BLOCK_IMPACT
                && (fragment.contains("block impact") || fragment.contains("hits a block")
                || fragment.contains("on block"))) {
            NBTTagCompound actions = tag.getCompoundTag("block_impact_actions");
            actions.setBoolean(effect.getAction().name(), true);
            tag.setTag("block_impact_actions", actions);
        }
        float value = scaleValue(effect);
        switch (effect.getAction()) {
            case DAMAGE_MULTIPLIER:
                multiply(tag, "mod_damage", value);
                multiply(tag, "mod_damage_min", value);
                break;
            case FIRE_DELAY_MULTIPLIER:
                multiply(tag, "mod_min_firetime", value);
                break;
            case SPREAD_MULTIPLIER:
                multiply(tag, "mod_accuracy", value);
                break;
            case RANGE_MULTIPLIER:
                multiply(tag, "mod_range", value);
                break;
            case MAGAZINE_ADD:
                tag.setInteger("mod_clipsize", tag.getInteger("mod_clipsize") + (int) Math.round(value));
                break;
            case MAGAZINE_MULTIPLIER:
                multiply(tag, "mod_clipsize_mult", value);
                break;
            case RELOAD_TIME_MULTIPLIER:
                multiply(tag, "mod_reloadtime", value);
                break;
            case MOVEMENT_SPEED_MULTIPLIER:
                multiply(tag, "mod_movement_speed", value);
                break;
            case MELEE_ATTACK_SPEED_MULTIPLIER:
                multiply(tag, "mod_melee_attack_speed", value);
                break;
            case AMMO_CONSUMPTION_MULTIPLIER:
                multiply(tag, "mod_ammo_consumption", value);
                break;
            case CHARGE_TIME_MULTIPLIER:
                multiply(tag, "mod_charge_time", value);
                break;
            case SPIN_UP_TIME_MULTIPLIER:
                multiply(tag, "mod_spinup_time", value);
                break;
            case LOCK_ON_TIME_MULTIPLIER:
                multiply(tag, "mod_lock_on_time", value);
                break;
            case KNOCKBACK_BONUS:
                tag.setBoolean("techguns_knockback", true);
                tag.setFloat("techguns_knockback_strength",
                        tag.getFloat("techguns_knockback_strength") + value);
                break;
            case DETECTION_RADIUS_MULTIPLIER:
                multiply(tag, "mod_detection_radius", value);
                break;
            case JAM_CHANCE:
                tag.setFloat("common_jam_chance",
                        combineChance(tag.getFloat("common_jam_chance"), (float) effect.getProbability()));
                break;
            case CONDITIONAL_DAMAGE_MULTIPLIER:
                if (effect.getCondition() == EffectCondition.TARGET_WITHIN_DISTANCE) {
                    multiply(tag, "common_close_damage", value);
                    tag.setFloat("common_close_damage_distance", Math.max(
                            tag.getFloat("common_close_damage_distance"),
                            (float) effect.getConditionValue()));
                    registerConditionalDamage(tag, upgradeId, "close", value,
                            (float) effect.getConditionValue());
                } else if (effect.getCondition() == EffectCondition.TARGET_BEYOND_DISTANCE) {
                    multiply(tag, "long_range_damage", value);
                    float distance = (float) effect.getConditionValue();
                    float current = tag.getFloat("long_range_distance");
                    tag.setFloat("long_range_distance", current <= 0.0f
                            ? distance : Math.min(current, distance));
                    registerConditionalDamage(tag, upgradeId, "long", value, distance);
                } else if (effect.getCondition() == EffectCondition.FIRST_SHOT_AFTER_RELOAD) {
                    if ("bio_precise_shot".equals(upgradeId)) {
                        multiply(tag, "poison_first_shot_multiplier", value);
                    } else {
                        multiply(tag, "first_shot_damage", value);
                    }
                } else if (effect.getCondition() == EffectCondition.LAST_ROUND) {
                    multiply(tag, "last_round_damage", value);
                }
                break;
            case CONDITIONAL_SPREAD_MULTIPLIER:
                if (effect.getCondition() == EffectCondition.BURST_FIRE) {
                    multiply(tag, "common_burst_accuracy", value);
                } else if (effect.getCondition() == EffectCondition.PRONE) {
                    multiply(tag, "common_prone_accuracy", value);
                }
                break;
            case PROJECTILE_COUNT_ADD:
                if ("sp_extra_shard".equals(upgradeId) || "sp_monster".equals(upgradeId)) {
                    tag.setInteger("split_shard_add", tag.getInteger("split_shard_add")
                            + (int) Math.round(value));
                } else {
                    tag.setInteger("mod_bulletcount",
                            tag.getInteger("mod_bulletcount") + (int) Math.round(value));
                }
                if ("hmg_shotgun".equals(upgradeId)) {
                    tag.setInteger("fan_projectile_count", 1 + (int) Math.round(value));
                }
                break;
            case PROJECTILE_COUNT_MULTIPLIER:
                multiply(tag, "mod_bulletcount_mult", value);
                break;
            case PROJECTILE_SPEED_MULTIPLIER:
                multiply(tag, "mod_projectile_speed", value);
                break;
            case EXPLOSION_RADIUS_MULTIPLIER:
                multiply(tag, "mod_explosion_radius", value);
                break;
            case ARMOR_PENETRATION_ADD:
                tag.setFloat("mod_penetration", tag.getFloat("mod_penetration") + value);
                break;
            case BURN_DAMAGE_MULTIPLIER:
                multiply(tag, "mod_burn_damage", value);
                break;
            case POISON_DAMAGE_MULTIPLIER:
                multiply(tag, "mod_poison_damage", value);
                break;
            case GROUND_FIRE_DURATION_ADD:
                tag.setInteger("ground_fire_bonus_ticks",
                        tag.getInteger("ground_fire_bonus_ticks") + (int) Math.round(value));
                break;
            case RANDOM_DAMAGE_MULTIPLIER:
                registerRandomDamage(tag, upgradeId, (float) effect.getProbability(),
                        value, 0);
                break;
            case MINING_SPEED_MULTIPLIER:
                multiply(tag, "mod_mining_speed", value);
                break;
            case SHOCKWAVE_RADIUS:
                tag.setFloat("shockwave_radius", Math.max(tag.getFloat("shockwave_radius"), value));
                break;
            case CONDITIONAL_FIRE_DELAY_MULTIPLIER:
                if (effect.getCondition() == EffectCondition.MOVING) {
                    multiply(tag, "moving_fire_delay", value);
                }
                break;
            case INCENDIARY_DURATION:
                tag.setInteger("incendiary_duration",
                        Math.max(tag.getInteger("incendiary_duration"), (int) Math.round(value)));
                break;
            case EXPLOSION_POWER:
                tag.setFloat("explosion_power", Math.max(tag.getFloat("explosion_power"), value));
                break;
            case RANDOM_EXPLOSION_POWER:
                tag.setFloat("random_explosion_chance",
                        combineChance(tag.getFloat("random_explosion_chance"),
                                (float) effect.getProbability()));
                tag.setFloat("random_explosion_power",
                        Math.max(tag.getFloat("random_explosion_power"), value));
                break;
            case GLOW_DURATION:
                tag.setInteger("glow_duration", Math.max(tag.getInteger("glow_duration"),
                        (int) Math.round(value)));
                break;
            case GOLD_DROP_CHANCE:
                tag.setFloat("gold_drop_chance", combineChance(tag.getFloat("gold_drop_chance"),
                        (float) effect.getProbability()));
                break;
            case RANDOM_EXTRA_PROJECTILES:
                if ("sp_cascade".equals(upgradeId)) {
                    tag.setFloat("split_again_chance", (float) effect.getProbability());
                    break;
                }
                NBTTagCompound projectileSpecs = tag.getCompoundTag(
                        "random_extra_projectile_specs");
                NBTTagCompound projectileSpec = new NBTTagCompound();
                projectileSpec.setFloat("chance", (float) effect.getProbability());
                projectileSpec.setInteger("count", (int) Math.round(value));
                projectileSpec.setInteger("ammo_cost",
                        (int) Math.round(effect.getConditionValue()));
                projectileSpecs.setTag(upgradeId, projectileSpec);
                tag.setTag("random_extra_projectile_specs", projectileSpecs);
                tag.setFloat("random_extra_projectile_chance",
                        combineChance(tag.getFloat("random_extra_projectile_chance"),
                                (float) effect.getProbability()));
                tag.setInteger("random_extra_projectile_count",
                        Math.max(tag.getInteger("random_extra_projectile_count"),
                                (int) Math.round(value)));
                tag.setInteger("random_extra_ammo_cost",
                        Math.max(tag.getInteger("random_extra_ammo_cost"),
                                (int) Math.round(effect.getConditionValue())));
                break;
            case EXTRA_PROJECTILE_AMMO_COST:
                NBTTagCompound extraAmmoSpecs = tag.getCompoundTag(
                        "random_extra_projectile_specs");
                NBTTagCompound extraAmmoSpec = extraAmmoSpecs.getCompoundTag(upgradeId);
                int extraAmmoCost = Math.max(0, (int) Math.round(value));
                extraAmmoSpec.setInteger("ammo_cost", extraAmmoCost);
                extraAmmoSpecs.setTag(upgradeId, extraAmmoSpec);
                tag.setTag("random_extra_projectile_specs", extraAmmoSpecs);
                tag.setInteger("random_extra_ammo_cost",
                        Math.max(tag.getInteger("random_extra_ammo_cost"), extraAmmoCost));
                break;
            case FIRE_ZONE_DURATION:
                tag.setInteger("fire_zone_duration", Math.max(tag.getInteger("fire_zone_duration"),
                        (int) Math.round(value)));
                break;
            case CLUSTER_COUNT:
                tag.setInteger("cluster_count", Math.max(tag.getInteger("cluster_count"),
                        (int) Math.round(value)));
                break;
            case STUN_DURATION:
                tag.setInteger("stun_duration", Math.max(tag.getInteger("stun_duration"),
                        (int) Math.round(value)));
                break;
            case POISON_DURATION_ADD:
                tag.setInteger("poison_duration_add", tag.getInteger("poison_duration_add")
                        + (int) Math.round(value));
                break;
            case CONTAGION_CHANCE:
                tag.setFloat("contagion_chance", combineChance(tag.getFloat("contagion_chance"),
                        (float) effect.getProbability()));
                break;
            case CHAIN_LIGHTNING_TARGETS:
                tag.setInteger("chain_lightning_targets",
                        tag.getInteger("chain_lightning_targets") + (int) Math.round(value));
                break;
            case PIERCING_COUNT:
                tag.setInteger("piercing_count", Math.max(tag.getInteger("piercing_count"),
                        (int) Math.round(value)));
                break;
            case RADIATION_DURATION:
                tag.setInteger("radiation_duration", Math.max(tag.getInteger("radiation_duration"),
                        (int) Math.round(value)));
                break;
            case BEAM_WIDTH_MULTIPLIER:
                multiply(tag, "mod_beam_width", value);
                break;
            case FUEL_RESTORE_ON_KILL:
                tag.setFloat("fuel_restore_chance", combineChance(tag.getFloat("fuel_restore_chance"),
                        (float) effect.getProbability()));
                tag.setInteger("fuel_restore_amount", Math.max(tag.getInteger("fuel_restore_amount"),
                        (int) Math.round(value)));
                break;
            case MINING_AREA_SIZE:
                tag.setInteger("mining_area_size", Math.max(tag.getInteger("mining_area_size"),
                        (int) Math.round(value)));
                break;
            case SMOKE_SCREEN:
                tag.setBoolean("smoke_screen", true);
                break;
            case PROJECTILE_DAMAGE_MULTIPLIER:
                multiply(tag, "mod_projectile_damage", value);
                break;
            case JAM_CHANCE_MULTIPLIER:
                multiply(tag, "mod_jam_chance", value);
                break;
            case PERIODIC_SHOT_DAMAGE_MULTIPLIER:
                int interval = Math.max(1, (int) Math.round(effect.getConditionValue()));
                int currentInterval = tag.getInteger("periodic_shot_interval");
                tag.setInteger("periodic_shot_interval", currentInterval <= 0
                        ? interval : Math.min(currentInterval, interval));
                multiply(tag, "periodic_shot_damage", value);
                registerPeriodicDamage(tag, upgradeId, interval, value);
                break;
            case FIRING_MOVEMENT_SPEED_MULTIPLIER:
                multiply(tag, "firing_movement_speed", value);
                break;
            case BURST_THRESHOLD_DAMAGE_MULTIPLIER:
                tag.setInteger("burst_damage_threshold", Math.max(1,
                        (int) Math.round(effect.getConditionValue())));
                multiply(tag, "burst_damage_multiplier", value);
                if (effect.getCondition() == EffectCondition.SHOT_SEQUENCE_BEYOND) {
                    tag.setBoolean("burst_damage_until_reload", true);
                }
                break;
            case STATIONARY_DAMAGE_MULTIPLIER:
                tag.setInteger("stationary_damage_ticks", Math.max(0,
                        (int) Math.round(effect.getConditionValue())));
                multiply(tag, "stationary_damage_multiplier", value);
                break;
            case SCOPED_DAMAGE_MULTIPLIER:
                tag.setInteger("scoped_damage_ticks", Math.max(1,
                        (int) Math.round(effect.getConditionValue())));
                multiply(tag, "scoped_damage_multiplier", value);
                if (effect.getTrigger() == EffectTrigger.DAMAGE_CALCULATION) {
                    tag.setBoolean("scoped_damage_consumes_shot", true);
                }
                break;
            case SILENCE_SHOT:
                tag.setBoolean("silencer", true);
                break;
            case QUIET_KILL_RADIUS:
                tag.setFloat("quiet_kill_radius", Math.max(tag.getFloat("quiet_kill_radius"), value));
                break;
            case CONSECUTIVE_TARGET_DAMAGE_MULTIPLIER:
                if ("bio_deadly_poison".equals(upgradeId)) {
                    multiply(tag, "poison_consecutive_multiplier", value);
                } else {
                    multiply(tag, "consecutive_target_damage", value);
                }
                break;
            case MULTI_TARGET_DAMAGE_MULTIPLIER:
                multiply(tag, "multi_target_damage", value);
                break;
            case FIRE_RESISTANCE_BYPASS:
                tag.setFloat("fire_resistance_bypass",
                        Math.max(tag.getFloat("fire_resistance_bypass"), value));
                break;
            case IMPACT_SHOCKWAVE:
                tag.setFloat("impact_shockwave_strength",
                        Math.max(tag.getFloat("impact_shockwave_strength"), value));
                if (effect.getCondition() == EffectCondition.LARGE_PROJECTILE) {
                    tag.setBoolean("charged_impact_shockwave", true);
                }
                break;
            case HEAT_SEEKING:
                tag.setFloat("heat_seeking_strength",
                        Math.max(tag.getFloat("heat_seeking_strength"), value));
                break;
            case IMPACT_SMOKE:
                tag.setInteger("impact_smoke_duration", Math.max(tag.getInteger("impact_smoke_duration"),
                        (int) Math.round(value)));
                break;
            case IMPACT_BLINDNESS:
                tag.setInteger("impact_blindness_duration",
                        Math.max(tag.getInteger("impact_blindness_duration"), (int) Math.round(value)));
                tag.setFloat("impact_blindness_radius", Math.max(tag.getFloat("impact_blindness_radius"),
                        (float) effect.getConditionValue()));
                break;
            case SUPPRESSION:
                tag.setInteger("suppression_duration", Math.max(tag.getInteger("suppression_duration"),
                        (int) Math.round(value)));
                tag.setFloat("suppression_radius", Math.max(tag.getFloat("suppression_radius"),
                        (float) effect.getConditionValue()));
                break;
            case PROJECTILE_RANGE_ADD:
                tag.setFloat("projectile_range_add", tag.getFloat("projectile_range_add") + value);
                break;
            case PULL_TARGET:
                tag.setFloat("pull_target_range", Math.max(tag.getFloat("pull_target_range"), value));
                break;
            case KILL_ATTACK_SPEED_BONUS:
                multiply(tag, "kill_attack_speed_multiplier", value);
                tag.setInteger("kill_attack_speed_duration", Math.max(tag.getInteger("kill_attack_speed_duration"),
                        (int) Math.round(effect.getConditionValue())));
                if (effect.getCondition() == EffectCondition.CONTINUOUS_KILL) {
                    tag.setBoolean("kill_attack_speed_continuous", true);
                }
                break;
            case DAMAGE_RAMP:
                tag.setFloat("damage_ramp_step", Math.max(tag.getFloat("damage_ramp_step"), value));
                tag.setInteger("damage_ramp_interval", Math.max(1,
                        (int) Math.round(effect.getConditionValue())));
                tag.setFloat("damage_ramp_max", 2.0f);
                break;
            case CHAIN_BEAM_TARGETS:
                tag.setInteger("chain_beam_targets", Math.max(tag.getInteger("chain_beam_targets"),
                        (int) Math.round(value)));
                break;
            case CHARGED_DAMAGE_MULTIPLIER:
                multiply(tag, "charged_damage_multiplier", value);
                break;
            case CHARGED_RADIUS_MULTIPLIER:
                multiply(tag, "charged_radius_multiplier", value);
                break;
            case LMB_AMMO_CONSUMPTION_MULTIPLIER:
                multiply(tag, "lmb_ammo_consumption", value);
                break;
            case PYROMANIAC_EXPLOSION:
                tag.setFloat("pyromaniac_explosion_power",
                        Math.max(tag.getFloat("pyromaniac_explosion_power"), value));
                break;
            case MOVING_FIRE_ZONE:
                tag.setInteger("moving_fire_zone_duration",
                        Math.max(tag.getInteger("moving_fire_zone_duration"), (int) Math.round(value)));
                tag.setInteger("moving_fire_zone_radius", Math.max(tag.getInteger("moving_fire_zone_radius"),
                        (int) Math.round(effect.getConditionValue())));
                break;
            case MELEE_DAMAGE_MULTIPLIER:
                multiply(tag, "mod_melee_damage", value);
                break;
            case PERIODIC_EXTRA_PROJECTILES:
                tag.setInteger("periodic_extra_interval", Math.max(1,
                        (int) Math.round(effect.getConditionValue())));
                tag.setInteger("periodic_extra_count", Math.max(tag.getInteger("periodic_extra_count"),
                        (int) Math.round(value)));
                break;
            case HEAL_ON_KILL:
                tag.setFloat("kill_heal_amount", tag.getFloat("kill_heal_amount") + value);
                break;
            case KILL_INVISIBILITY:
                tag.setFloat("kill_invisibility_chance",
                        combineChance(tag.getFloat("kill_invisibility_chance"),
                                (float) effect.getProbability()));
                tag.setInteger("kill_invisibility_duration",
                        Math.max(tag.getInteger("kill_invisibility_duration"),
                                (int) Math.round(value)));
                break;
            case SELF_EXPLOSION:
                tag.setFloat("self_explosion_chance",
                        combineChance(tag.getFloat("self_explosion_chance"),
                                (float) effect.getProbability()));
                tag.setFloat("self_explosion_power",
                        Math.max(tag.getFloat("self_explosion_power"), value));
                break;
            case AMMO_SAVE_CHANCE:
                tag.setFloat("ammo_save_chance", combineChance(tag.getFloat("ammo_save_chance"),
                        (float) effect.getProbability()));
                break;
            case UNLIMITED_AMMO:
                tag.setBoolean("unlimited_magazine", true);
                break;
            case INSTANT_CHARGE:
                tag.setBoolean("instant_charge", true);
                break;
            case KNOCKBACK_MULTIPLIER:
                multiply(tag, "mod_knockback_multiplier", value);
                break;
            case STACK_DAMAGE_ON_KILL:
                registerMythicStack(tag, upgradeId, "damage", value,
                        (float) effect.getConditionValue());
                break;
            case STACK_FIRE_RATE_ON_KILL:
                registerMythicStack(tag, upgradeId, "fire_rate", value,
                        (float) effect.getConditionValue());
                break;
            case STACK_ACCURACY_ON_KILL:
                registerMythicStack(tag, upgradeId, "accuracy", value,
                        (float) effect.getConditionValue());
                break;
            case STACK_DETECTION_ON_KILL:
                registerMythicStack(tag, upgradeId, "detection", value,
                        (float) effect.getConditionValue());
                break;
            case STACK_CLOSE_DAMAGE_ON_KILL:
                registerMythicStack(tag, upgradeId, "close_damage", value,
                        (float) effect.getConditionValue());
                tag.setFloat("mythic_close_damage_distance", 5.0f);
                break;
            case STACK_BURN_DAMAGE_ON_KILL:
                registerMythicStack(tag, upgradeId, "burn_damage", value,
                        (float) effect.getConditionValue());
                break;
            case STACK_POISON_DAMAGE_ON_KILL:
                registerMythicStack(tag, upgradeId, "poison_damage", value,
                        (float) effect.getConditionValue());
                break;
            case STACK_EXPLOSION_RADIUS_ON_KILL:
                registerMythicStack(tag, upgradeId, "explosion_radius", value,
                        (float) effect.getConditionValue());
                break;
            case RANDOM_SELF_DAMAGE:
                tag.setFloat("random_self_damage_chance",
                        combineChance(tag.getFloat("random_self_damage_chance"),
                                (float) effect.getProbability()));
                tag.setFloat("random_self_damage_fraction",
                        Math.max(tag.getFloat("random_self_damage_fraction"), value));
                break;
            case RAILGUN_PIERCE:
                tag.setFloat("railgun_pierce_range",
                        Math.max(tag.getFloat("railgun_pierce_range"), value));
                break;
            case IMPACT_EXPLOSION_RADIUS:
                tag.setFloat("impact_explosion_radius",
                        Math.max(tag.getFloat("impact_explosion_radius"), value));
                break;
            case FULL_MAG_TARGET_DAMAGE:
                tag.setInteger("full_mag_required_shots", Math.max(tag.getInteger(
                        "full_mag_required_shots"), (int) Math.round(effect.getConditionValue())));
                multiply(tag, "full_mag_damage_multiplier", value);
                break;
            case LONG_RANGE_KILL_BLINDNESS:
                tag.setFloat("kill_blind_distance", (float) effect.getConditionValue());
                tag.setFloat("kill_blind_radius", 15.0f);
                tag.setInteger("kill_blind_duration", (int) Math.round(value));
                break;
            case RANDOM_DAMAGE_STUN:
                registerRandomDamage(tag, upgradeId, (float) effect.getProbability(),
                        (float) effect.getConditionValue(), (int) Math.round(value));
                break;
            case BURN_DURATION_DAMAGE:
                tag.setInteger("burn_damage_ticks", (int) Math.round(effect.getConditionValue()));
                multiply(tag, "burn_duration_damage_multiplier", value);
                break;
            case FIRST_SHOT_KILL_NEXT_DAMAGE:
                multiply(tag, "first_kill_next_damage_multiplier", value);
                break;
            case KILL_CONTAGION:
                tag.setFloat("kill_contagion_chance", combineChance(tag.getFloat(
                        "kill_contagion_chance"), (float) effect.getProbability()));
                tag.setInteger("kill_contagion_duration", (int) Math.round(value));
                break;
            case KILL_CHAIN_LIGHTNING:
                tag.setInteger("kill_chain_targets", Math.max(tag.getInteger("kill_chain_targets"),
                        (int) Math.round(value)));
                break;
            case RICOCHET_NEAREST:
                tag.setFloat("ricochet_chance", combineChance(tag.getFloat("ricochet_chance"),
                        (float) effect.getProbability()));
                tag.setFloat("ricochet_explosion_power", value);
                tag.setFloat("ricochet_search_range", (float) effect.getConditionValue());
                break;
            case HIT_STREAK_DAMAGE:
                tag.setInteger("hit_streak_required", (int) Math.round(effect.getConditionValue()));
                multiply(tag, "hit_streak_damage_multiplier", value);
                break;
            case PERIODIC_FREE_CHARGE:
                tag.setInteger("periodic_free_charge_interval",
                        (int) Math.round(effect.getConditionValue()));
                break;
            case SAME_SHOT_PELLET_DAMAGE:
                multiply(tag, "same_shot_pellet_damage", value);
                break;
            case STUNNED_TARGET_DAMAGE:
                multiply(tag, "stunned_target_damage", value);
                break;
            case CHARGE_THRESHOLD_EXPLOSIVE_DAMAGE:
                tag.setInteger("charged_threshold_ticks",
                        (int) Math.round(effect.getConditionValue()));
                multiply(tag, "charged_threshold_damage", value);
                tag.setFloat("charged_threshold_explosion_radius", 3.0f);
                break;
            case CASCADE_ON_HIT:
                tag.setInteger("cascade_shard_count", Math.max(tag.getInteger("cascade_shard_count"),
                        (int) Math.round(value)));
                break;
            case MELEE_AREA_EXPLOSION:
                tag.setFloat("melee_area_chance", combineChance(tag.getFloat("melee_area_chance"),
                        (float) effect.getProbability()));
                multiply(tag, "melee_area_damage_multiplier", value);
                tag.setFloat("melee_area_radius", (float) effect.getConditionValue());
                tag.setInteger("melee_area_fire_duration", 100);
                break;
            case KILL_DAMAGE_CLOUD:
                tag.setFloat("kill_cloud_chance", combineChance(tag.getFloat("kill_cloud_chance"),
                        (float) effect.getProbability()));
                tag.setFloat("kill_cloud_radius", value);
                if (effect.getCondition() == EffectCondition.CONTINUOUS_KILL) {
                    tag.setBoolean("kill_cloud_continuous", true);
                }
                break;
            case CONTINUOUS_TARGET_EXPLOSION:
                tag.setInteger("continuous_explosion_ticks",
                        (int) Math.round(effect.getConditionValue()));
                multiply(tag, "continuous_explosion_multiplier", value);
                tag.setFloat("continuous_explosion_radius", 6.0f);
                break;
            case KILL_INSTANT_LOCK:
                tag.setBoolean("kill_instant_lock", true);
                break;
            case KILL_TREMOR:
                tag.setFloat("kill_tremor_chance", combineChance(tag.getFloat("kill_tremor_chance"),
                        (float) effect.getProbability()));
                tag.setFloat("kill_tremor_radius", value);
                if (effect.getCondition() == EffectCondition.CONTINUOUS_KILL) {
                    tag.setBoolean("kill_tremor_continuous", true);
                }
                break;
            case CHARGED_NUCLEAR_CHANCE:
                tag.setFloat("charged_nuclear_chance", combineChance(tag.getFloat(
                        "charged_nuclear_chance"), (float) effect.getProbability()));
                multiply(tag, "charged_nuclear_multiplier", value);
                tag.setFloat("charged_nuclear_radius", (float) effect.getConditionValue());
                break;
            case HEADSHOT_SHOCKWAVE:
                multiply(tag, "headshot_damage_multiplier", value);
                tag.setFloat("headshot_shockwave", (float) effect.getConditionValue());
                break;
            case PROJECTILE_COUNT_OVERRIDE:
                tag.setInteger("mod_bulletcount_override", Math.max(1, (int) Math.round(value)));
                break;
            case EVERY_SHOT_LIGHTNING:
                tag.setBoolean("every_shot_lightning", true);
                break;
            case RADIAL_PROJECTILE_BURST:
                tag.setInteger("radial_burst_count", Math.max(tag.getInteger("radial_burst_count"),
                        (int) Math.round(value)));
                if (effect.getCondition() == EffectCondition.EVERY_NTH_SHOT) {
                    tag.setInteger("radial_burst_interval", (int) Math.round(effect.getConditionValue()));
                }
                tag.setFloat("radial_burst_chance", combineChance(tag.getFloat(
                        "radial_burst_chance"), (float) effect.getProbability()));
                break;
            case RADIAL_BURST_RANGE:
                tag.setFloat("radial_burst_range", Math.max(tag.getFloat("radial_burst_range"), value));
                break;
            case TARGETED_PROJECTILE_BURST:
                tag.setInteger("targeted_burst_count", Math.max(tag.getInteger("targeted_burst_count"),
                        (int) Math.round(value)));
                if (effect.getCondition() == EffectCondition.EVERY_NTH_SHOT) {
                    tag.setInteger("targeted_burst_interval", (int) Math.round(effect.getConditionValue()));
                }
                tag.setFloat("targeted_burst_chance", combineChance(tag.getFloat(
                        "targeted_burst_chance"), (float) effect.getProbability()));
                break;
            case TARGETED_BURST_RANGE:
                tag.setFloat("targeted_burst_range", Math.max(tag.getFloat("targeted_burst_range"), value));
                break;
            case FIRE_TRAIL:
                tag.setInteger("ultra_fire_trail_duration", (int) Math.round(value));
                tag.setInteger("ultra_fire_trail_radius", (int) Math.round(effect.getConditionValue()));
                break;
            case NUCLEAR_IMPACT:
                tag.setFloat("nuclear_impact_chance", combineChance(tag.getFloat(
                        "nuclear_impact_chance"), (float) effect.getProbability()));
                tag.setFloat("nuclear_impact_radius", value);
                break;
            case KILL_AREA_EXECUTE:
                tag.setFloat("kill_execute_chance", combineChance(tag.getFloat(
                        "kill_execute_chance"), (float) effect.getProbability()));
                tag.setFloat("kill_execute_radius", value);
                break;
            case EXTRA_IMPACT_EXPLOSIONS:
                tag.setInteger("extra_impact_explosions", Math.max(tag.getInteger(
                        "extra_impact_explosions"), (int) Math.round(value)));
                break;
            case ARTILLERY_STRIKE:
                tag.setFloat("artillery_chance", combineChance(tag.getFloat("artillery_chance"),
                        (float) effect.getProbability()));
                tag.setInteger("artillery_count", (int) Math.round(value));
                tag.setFloat("artillery_radius", (float) effect.getConditionValue());
                break;
            case FIRE_RAIN:
                tag.setFloat("fire_rain_chance", combineChance(tag.getFloat("fire_rain_chance"),
                        (float) effect.getProbability()));
                tag.setInteger("fire_rain_count", (int) Math.round(value));
                tag.setFloat("fire_rain_radius", (float) effect.getConditionValue());
                break;
            case CHARGED_EXTRA_PROJECTILES:
                tag.setInteger("charged_extra_projectiles", Math.max(0, (int) Math.round(value)));
                break;
            case ACID_CLOUD:
                tag.setFloat("acid_cloud_radius", value);
                tag.setInteger("acid_cloud_duration", (int) Math.round(effect.getConditionValue()));
                break;
            case CHAIN_ALL_RADIUS:
                tag.setFloat("chain_all_radius", value);
                break;
            case KILL_LIGHTNING:
                tag.setBoolean("kill_lightning", true);
                break;
            case SNIPER_STRIKE:
                tag.setFloat("sniper_strike_chance", combineChance(tag.getFloat(
                        "sniper_strike_chance"), (float) effect.getProbability()));
                tag.setFloat("sniper_strike_multiplier", value);
                break;
            case RANDOM_INSTANT_KILL:
                tag.setFloat("instant_kill_chance", combineChance(tag.getFloat(
                        "instant_kill_chance"), (float) effect.getProbability()));
                break;
            case LASER_RAIN:
                tag.setFloat("laser_rain_chance", combineChance(tag.getFloat("laser_rain_chance"),
                        (float) effect.getProbability()));
                tag.setInteger("laser_rain_count", (int) Math.round(value));
                tag.setFloat("laser_rain_radius", (float) effect.getConditionValue());
                break;
            case PLASMA_RAIN:
                tag.setFloat("plasma_rain_chance", combineChance(tag.getFloat("plasma_rain_chance"),
                        (float) effect.getProbability()));
                tag.setInteger("plasma_rain_count", (int) Math.round(value));
                tag.setFloat("plasma_rain_radius", (float) effect.getConditionValue());
                break;
            case PERIODIC_AREA_EXPLOSION:
                tag.setInteger("periodic_area_interval", (int) Math.round(effect.getConditionValue()));
                tag.setFloat("periodic_area_multiplier", value);
                tag.setFloat("periodic_area_radius", 8.0f);
                break;
            case INFINITE_SPLIT:
                tag.setInteger("infinite_split_children", Math.max(1, (int) Math.round(value)));
                tag.setInteger("infinite_split_generations", Math.max(1,
                        (int) Math.round(effect.getConditionValue())));
                break;
            case RANDOM_TIMED_ZONE:
                tag.setFloat("timed_zone_chance", combineChance(tag.getFloat("timed_zone_chance"),
                        (float) effect.getProbability()));
                tag.setFloat("timed_zone_radius", value);
                tag.setInteger("timed_zone_duration", (int) Math.round(effect.getConditionValue()));
                break;
            case SEISMIC_WAVE:
                tag.setFloat("seismic_wave_radius", value);
                tag.setFloat("seismic_wave_knockback", (float) effect.getConditionValue());
                break;
            case KILL_EARTHQUAKE:
                tag.setFloat("kill_earthquake_chance", combineChance(tag.getFloat(
                        "kill_earthquake_chance"), (float) effect.getProbability()));
                tag.setFloat("kill_earthquake_radius", value);
                break;
            case MELEE_VORTEX:
                tag.setFloat("melee_vortex_radius", value);
                break;
            case MELEE_INSTANT_KILL:
                tag.setFloat("melee_instant_kill_chance", combineChance(tag.getFloat(
                        "melee_instant_kill_chance"), (float) effect.getProbability()));
                break;
            case KILL_BLOOD_RAIN:
                tag.setFloat("kill_blood_rain_chance", combineChance(tag.getFloat(
                        "kill_blood_rain_chance"), (float) effect.getProbability()));
                break;
            case CONTINUOUS_NUCLEAR_EXPLOSION:
                tag.setInteger("continuous_nuclear_ticks", (int) Math.round(effect.getConditionValue()));
                tag.setFloat("continuous_nuclear_multiplier", value);
                tag.setFloat("continuous_nuclear_radius", 15.0f);
                break;
            case NUCLEAR_ON_KILL:
                tag.setBoolean("nuclear_on_kill", true);
                break;
            case RANDOM_EMP:
                tag.setFloat("emp_chance", combineChance(tag.getFloat("emp_chance"),
                        (float) effect.getProbability()));
                tag.setFloat("emp_radius", value);
                tag.setFloat("emp_multiplier", (float) effect.getConditionValue());
                break;
            case INSTANT_LOCK_ON:
                tag.setBoolean("instant_lock_on", true);
                break;
            case THROUGH_WALL_HOMING:
                tag.setFloat("through_wall_homing", value);
                break;
            case CONTINUOUS_EARTHQUAKE:
                tag.setFloat("continuous_earthquake_radius", value);
                break;
            case RANDOM_MELEE_EXPLOSION:
                tag.setFloat("random_melee_explosion_chance", combineChance(tag.getFloat(
                        "random_melee_explosion_chance"), (float) effect.getProbability()));
                tag.setFloat("random_melee_explosion_radius", value);
                break;
            case KILL_EXPLOSION:
                tag.setFloat("kill_explosion_radius", value);
                break;
            case RANDOM_SONIC_WAVE:
                tag.setFloat("sonic_wave_chance", combineChance(tag.getFloat("sonic_wave_chance"),
                        (float) effect.getProbability()));
                tag.setFloat("sonic_wave_radius", value);
                tag.setFloat("sonic_wave_multiplier", (float) effect.getConditionValue());
                break;
            case RANDOM_PULSE_WAVE:
                tag.setFloat("pulse_wave_chance", combineChance(tag.getFloat("pulse_wave_chance"),
                        (float) effect.getProbability()));
                tag.setFloat("pulse_wave_radius", value);
                tag.setFloat("pulse_wave_multiplier", (float) effect.getConditionValue());
                break;
            case RANDOM_LIGHT_EXPLOSION:
                tag.setFloat("light_explosion_chance", combineChance(tag.getFloat(
                        "light_explosion_chance"), (float) effect.getProbability()));
                tag.setFloat("light_explosion_radius", value);
                tag.setFloat("light_explosion_multiplier", (float) effect.getConditionValue());
                break;
            default:
                throw new IllegalStateException("Unsupported structured action: " + effect.getAction());
        }
    }

    /** Applies the config multiplier to the effect value. */
    private static float scaleValue(EffectSpec effect) {
        float value = (float) effect.getValue();
        EffectAction action = effect.getAction();
        double configMult = TguConfig.getMultiplierForAction(action);
        if (configMult == 1.0) return value;

        if (TguConfig.isInvertedMultiplier(action)) {
            // Lower is better: weaken the deviation from 1.0
            if (action == EffectAction.FIRE_DELAY_MULTIPLIER
                    || action == EffectAction.CONDITIONAL_FIRE_DELAY_MULTIPLIER
                    || action == EffectAction.MELEE_ATTACK_SPEED_MULTIPLIER
                    || action == EffectAction.CHARGE_TIME_MULTIPLIER
                    || action == EffectAction.SPIN_UP_TIME_MULTIPLIER
                    || action == EffectAction.LOCK_ON_TIME_MULTIPLIER
                    || action == EffectAction.RELOAD_TIME_MULTIPLIER) {
                // value = 1/(1+bonus). Extract bonus, weaken it, return 1/(1+bonus*mult)
                double bonus = (1.0 / value) - 1.0;
                return (float) (1.0 / (1.0 + bonus * configMult));
            }
            // spread/ammo/detection: value = 0.85 → deviation 0.15 → 0.075 → 0.925
            double deviation = 1.0 - value;
            return (float) (1.0 - deviation * configMult);
        }

        if (TguConfig.isMultiplicativeBonus(action)) {
            // value = 1.05 → bonus 0.05 → 0.025 → value = 1.025
            double bonus = value - 1.0;
            return (float) (1.0 + bonus * configMult);
        }

        // Additive: MAGAZINE_ADD, KNOCKBACK_BONUS, durations, etc.
        return (float) (value * configMult);
    }

    private static void multiply(NBTTagCompound tag, String key, float value) {
        float current = tag.hasKey(key) ? tag.getFloat(key) : 1.0f;
        tag.setFloat(key, current * value);
    }

    private static float combineChance(float current, float added) {
        return 1.0f - (1.0f - current) * (1.0f - added);
    }

    private static void registerMythicStack(NBTTagCompound tag, String upgradeId, String stat,
                                            float step, float maximum) {
        NBTTagCompound specs = tag.getCompoundTag("mythic_stack_specs");
        NBTTagCompound spec = new NBTTagCompound();
        spec.setString("stat", stat);
        spec.setFloat("step", step);
        spec.setFloat("max", maximum);
        String key = upgradeId + "_" + stat;
        specs.setTag(key, spec);
        tag.setTag("mythic_stack_specs", specs);

        NBTTagCompound progress = tag.getCompoundTag("mythic_stack_progress");
        if (!progress.hasKey(key)) {
            String legacyKey = ("fire_rate".equals(stat) ? "mythic_legacy_fire_rate_progress"
                    : ("accuracy".equals(stat) || "detection".equals(stat))
                    ? "mythic_legacy_accuracy_progress" : "mythic_legacy_damage_progress");
            if (tag.hasKey(legacyKey)) {
                progress.setFloat(key, Math.min(maximum, tag.getFloat(legacyKey)));
                tag.removeTag(legacyKey);
            }
        }
        tag.setTag("mythic_stack_progress", progress);
    }

    private static void registerConditionalDamage(NBTTagCompound tag, String upgradeId,
                                                  String mode, float multiplier,
                                                  float distance) {
        NBTTagCompound specs = tag.getCompoundTag("conditional_damage_specs");
        NBTTagCompound spec = new NBTTagCompound();
        spec.setString("mode", mode);
        spec.setFloat("multiplier", multiplier);
        spec.setFloat("distance", distance);
        specs.setTag(upgradeId + "_" + mode, spec);
        tag.setTag("conditional_damage_specs", specs);
    }

    private static void registerPeriodicDamage(NBTTagCompound tag, String upgradeId,
                                               int interval, float multiplier) {
        NBTTagCompound specs = tag.getCompoundTag("periodic_damage_specs");
        NBTTagCompound spec = new NBTTagCompound();
        spec.setInteger("interval", interval);
        spec.setFloat("multiplier", multiplier);
        specs.setTag(upgradeId, spec);
        tag.setTag("periodic_damage_specs", specs);
    }

    private static void registerRandomDamage(NBTTagCompound tag, String upgradeId,
                                             float chance, float multiplier,
                                             int stunDuration) {
        NBTTagCompound specs = tag.getCompoundTag("random_damage_specs");
        NBTTagCompound spec = new NBTTagCompound();
        spec.setFloat("chance", chance);
        spec.setFloat("multiplier", multiplier);
        spec.setInteger("stun_duration", Math.max(0, stunDuration));
        specs.setTag(upgradeId, spec);
        tag.setTag("random_damage_specs", specs);
        tag.setFloat("random_damage_chance",
                combineChance(tag.getFloat("random_damage_chance"), chance));
        multiply(tag, "random_damage_multiplier", multiplier);
    }
}
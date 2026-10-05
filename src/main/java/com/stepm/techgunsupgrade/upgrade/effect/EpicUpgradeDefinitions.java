package com.stepm.techgunsupgrade.upgrade.effect;

import com.stepm.techgunsupgrade.upgrade.UpgradeBuff;
import com.stepm.techgunsupgrade.upgrade.UpgradeRarity;
import com.stepm.techgunsupgrade.upgrade.WeaponUpgrades;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Explicit executable definitions for all 120 Epic upgrades. */
public final class EpicUpgradeDefinitions {
    public static final int EXPECTED_COUNT = 120;
    private static final Map<String, List<EffectSpec>> DECLARED = new LinkedHashMap<>();
    private static final Map<String, UpgradeDefinition> DEFINITIONS = new LinkedHashMap<>();
    private static boolean declared;

    private EpicUpgradeDefinitions() {
    }

    public static synchronized void init() {
        if (!declared) {
            declareAll();
            declared = true;
        }
        DEFINITIONS.clear();
        for (Map.Entry<String, List<EffectSpec>> entry : DECLARED.entrySet()) {
            UpgradeBuff buff = WeaponUpgrades.getUpgradeById(entry.getKey());
            String weaponId = findWeaponId(entry.getKey());
            if (buff == null || weaponId == null) {
                throw new IllegalStateException("Epic definition references unknown upgrade: " + entry.getKey());
            }
            if (buff.getRarity() != UpgradeRarity.EPIC) {
                throw new IllegalStateException("Non-Epic upgrade in Epic registry: " + entry.getKey());
            }
            DEFINITIONS.put(entry.getKey(), new UpgradeDefinition(entry.getKey(), weaponId,
                    buff.getRarity(), buff.getDescription(), entry.getValue()));
        }
        validateCoverage();
    }

    public static UpgradeDefinition get(String id) {
        return DEFINITIONS.get(id);
    }

    public static Map<String, UpgradeDefinition> getAll() {
        return Collections.unmodifiableMap(DEFINITIONS);
    }

    public static void validateCoverage() {
        List<String> missing = new ArrayList<>();
        int sourceCount = 0;
        for (List<UpgradeBuff> buffs : WeaponUpgrades.getAllWeaponUpgrades().values()) {
            for (UpgradeBuff buff : buffs) {
                if (buff.getRarity() == UpgradeRarity.EPIC) {
                    sourceCount++;
                    if (!DEFINITIONS.containsKey(buff.getId())) missing.add(buff.getId());
                }
            }
        }
        if (sourceCount != EXPECTED_COUNT || DEFINITIONS.size() != EXPECTED_COUNT || !missing.isEmpty()) {
            throw new IllegalStateException("Epic upgrade coverage failed: source=" + sourceCount
                    + ", definitions=" + DEFINITIONS.size() + ", missing=" + missing);
        }
        for (UpgradeDefinition definition : DEFINITIONS.values()) {
            if (definition.getEffects().isEmpty()) {
                throw new IllegalStateException("Epic upgrade has no executable effects: " + definition.getId());
            }
        }
    }

    private static String findWeaponId(String upgradeId) {
        for (Map.Entry<String, List<UpgradeBuff>> entry : WeaponUpgrades.getAllWeaponUpgrades().entrySet()) {
            for (UpgradeBuff buff : entry.getValue()) {
                if (upgradeId.equals(buff.getId())) return entry.getKey();
            }
        }
        return null;
    }

    private static void declareAll() {
        register("hmg_combat",
                passive(EffectAction.DAMAGE_MULTIPLIER, 1.40, "Damage +40%"),
                passive(EffectAction.FIRE_DELAY_MULTIPLIER, 1.0 / 1.20, "Fire Rate +20%"));
        register("hmg_shotgun",
                passive(EffectAction.PROJECTILE_COUNT_ADD, 4, "Shoots 5 bullets in a fan"),
                passive(EffectAction.PROJECTILE_DAMAGE_MULTIPLIER, 0.20, "less damage each"));
        register("hmg_armor_piercing", passive(EffectAction.ARMOR_PENETRATION_ADD,
                0.30, "Bullets ignore 30% of target armor"));

        register("db_hell_shot",
                passive(EffectAction.PROJECTILE_COUNT_MULTIPLIER, 2.0, "Fires both barrels at once"),
                passive(EffectAction.AMMO_CONSUMPTION_MULTIPLIER, 2.0, "uses both shells"),
                passive(EffectAction.INCENDIARY_DURATION, 100, "igniting target for 5 seconds"));
        register("db_deadly_duet", consecutive(2.0, "second shot deals +100% damage"));
        register("db_knockback", passive(EffectAction.KNOCKBACK_BONUS, 5.0,
                "Knocks target back 5 blocks on hit"));

        registerMany(lastRound(3.0, "Last round deals +200% damage"),
                "rev_deadly_shot", "gr_deadly_shot");
        registerMany(passive(EffectAction.FIRE_DELAY_MULTIPLIER, 1.0 / 1.40, "Fire Rate +40%"),
                "rev_fast_trigger", "gr_fast_trigger");
        registerMany(firstShot(1.50, "First shot after reload is critical x1.5"),
                "rev_precise_shot", "gr_royal_precision");

        registerMany(periodicDamage(5, 2.0, "Every 5th shot deals double damage"),
                "thom_fire_storm", "akm_fire_storm", "m4_fire_storm", "mac_fire_storm",
                "aug_fire_storm", "vec_fire_storm", "scar_fire_storm", "br_plasma_storm",
                "bs_plasma_storm", "p90_fire_storm", "pul_pulse_storm");
        registerMany(firingMovement(1.20, "+20% movement speed while firing"),
                "thom_gangster", "pist_fast_hand", "mac_gangster", "hb_demon_speed",
                "bio_biochemist", "tesla_electro_field", "vec_tactical", "lr_shield",
                "br_mobile", "p90_mobile", "lp_mobile");
        registerMany(firingMovement(1.15, "+15% movement speed while firing"),
                "m4_tactical", "aug_tactical", "scar_tactical");
        register("thom_long_burst", burstDamage(10, 1.20, false,
                "Burst of more than 10 shots deals +20% damage"));
        register("akm_long_burst", burstDamage(15, 1.20, false,
                "Burst of more than 15 shots deals +20% damage"));
        register("m4_long_burst", burstDamage(15, 1.20, false,
                "Burst of more than 15 shots deals +20% damage"));
        register("aug_long_burst", burstDamage(15, 1.20, false,
                "Burst of more than 15 shots deals +20% damage"));
        register("vec_long_burst", burstDamage(10, 1.20, false,
                "Burst of more than 10 shots deals +20% damage"));
        register("p90_long_burst", burstDamage(10, 1.20, false,
                "Burst of more than 10 shots deals +20% damage"));
        register("mac_hail", burstDamage(20, 1.25, true,
                "More than 20 shots without reloading gives +25% damage"));

        register("akm_reliable", passive(EffectAction.JAM_CHANCE_MULTIPLIER, 0.50,
                "Jam chance reduced by 50%"));
        register("bolt_scope_shot", scoped(20, 2.0,
                "Scoped for more than 1 second gives +100% damage"));
        register("bolt_professional", longRange(30, 1.30, "+30% damage at range >30 blocks"));
        register("bolt_silence", passive(EffectAction.SILENCE_SHOT, 1, "Shot makes no sound"));

        register("sab_quiet_killer", passive(EffectAction.QUIET_KILL_RADIUS, 10,
                "One-shot kills do not alert mobs within 10 blocks"));
        register("sab_sneaky", stationary(0, 1.20,
                "+20% damage if player is not moving before shot"));
        register("sab_sharpshooter", periodicDamage(3, 2.0,
                "Every 3rd shot in a burst deals double damage"));

        registerMany(consecutive(2.50, "Second consecutive hit deals +150% damage"),
                "pist_deadly_double", "hb_hell_pit", "bio_deadly_poison", "tesla_storm",
                "sr_deadly_resonance", "lp_deadly_double");
        registerMany(firstShot(1.50, "First shot after reload is critical x1.5"),
                "pist_sharpshooter", "hb_precise_shot", "tesla_precise_shot",
                "scar_sharpshooter", "lr_precise_shot", "br_precise_shot",
                "pul_precise_shot", "lp_precise_shot");
        register("bio_precise_shot", firstShot(2.0,
                "First shot after reload deals double poison damage"));

        register("cs_combat_shot", closeRange(3, 2.0,
                "Point-blank shot up to 3 blocks deals +100% damage"));
        register("cs_shot_storm", EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.MULTI_TARGET_DAMAGE_MULTIPLIER, 1.20,
                        "Hitting multiple enemies gives +20% damage to each")
                .condition(EffectCondition.MULTIPLE_TARGETS_SAME_SHOT, 0).build());
        register("cs_knockback", passive(EffectAction.KNOCKBACK_BONUS, 5.0,
                "Knocks target back 5 blocks on hit"));

        register("flame_tornado", EffectSpec.builder(EffectTrigger.BLOCK_IMPACT,
                        EffectAction.MOVING_FIRE_ZONE, 60,
                        "Firing at the ground creates a moving fire zone")
                .condition(EffectCondition.ALWAYS, 2).build());
        register("flame_pyromaniac", passive(EffectAction.PYROMANIAC_EXPLOSION, 2.0,
                "Killed enemies explode and ignite nearby enemies"));
        register("flame_hot_steel", passive(EffectAction.FIRE_RESISTANCE_BYPASS, 0.50,
                "Fire ignores 50% of fire resistance"));

        registerMany(new EffectSpec[]{
                        passive(EffectAction.DAMAGE_MULTIPLIER, 1.60, "Damage +60%"),
                        passive(EffectAction.EXPLOSION_RADIUS_MULTIPLIER, 1.80,
                                "Explosion radius +80%")},
                "baz_nuclear", "grp_nuclear", "lor_nuclear");
        registerMany(passive(EffectAction.IMPACT_SHOCKWAVE, 10,
                        "Knocks enemies back 10 blocks from epicenter"),
                "baz_shockwave", "grp_shockwave", "lor_shockwave");
        registerMany(passive(EffectAction.HEAT_SEEKING, 0.08,
                        "Rocket slightly tracks nearest target"),
                "baz_heat_seeking", "grp_heat_seeking");

        register("gl_shockwave", passive(EffectAction.IMPACT_SHOCKWAVE, 8,
                "Knocks enemies back 8 blocks from epicenter"));
        register("gl_smoke", passive(EffectAction.IMPACT_SMOKE, 60,
                "Smoke hides player and slows enemies"));
        register("gl_flashbang", EffectSpec.builder(EffectTrigger.BLOCK_IMPACT,
                        EffectAction.IMPACT_BLINDNESS, 60,
                        "Blinds enemies within 5 blocks for 3 seconds")
                .condition(EffectCondition.ALWAYS, 5).build());

        register("lmg_hail", periodicDamage(10, 2.0,
                "Every 10th shot deals double damage"));
        register("lmg_suppression", suppression(20, 5,
                "Enemies within 5 blocks of impact are slowed for 1 second"));
        register("lmg_long_burst", burstDamage(30, 1.25, true,
                "More than 30 shots without reloading gives +25% damage"));
        register("min_hail_lead", periodicDamage(10, 2.0,
                "Every 10th shot deals double damage"));
        register("min_suppression", suppression(30, 5,
                "Enemies within 5 blocks of impact are slowed for 1.5 seconds"));
        register("min_long_burst", burstDamage(50, 1.25, true,
                "More than 50 shots without reloading gives +25% damage"));

        register("as50_eagle_eye", stationary(60, 1.40,
                "Standing still for 3 seconds gives +40% damage"));
        register("as50_professional", longRange(40, 1.30,
                "+30% damage at range >40 blocks"));
        register("as50_silence", passive(EffectAction.SILENCE_SHOT, 1,
                "Shot makes no sound"));

        register("lr_piercing", passive(EffectAction.PIERCING_COUNT, 3,
                "Beam pierces up to 3 targets"));
        register("bs_point_blank", closeRange(2, 2.0,
                "Point-blank shot up to 2 blocks deals +100% damage"));
        register("bs_knockback", passive(EffectAction.KNOCKBACK_BONUS, 5,
                "Knocks target back 5 blocks on hit"));
        register("sr_shockwave", passive(EffectAction.KNOCKBACK_BONUS, 8,
                "Knocks target back 8 blocks on hit"));
        register("sr_silence", passive(EffectAction.SILENCE_SHOT, 1,
                "Shot makes no sound"));
        register("pul_shockwave", passive(EffectAction.KNOCKBACK_BONUS, 6,
                "Knocks target back 6 blocks on hit"));

        register("sp_shard_storm", EffectSpec.builder(EffectTrigger.PROJECTILE_SPAWN,
                        EffectAction.PERIODIC_EXTRA_PROJECTILES, 2,
                        "Every 5th shot creates +2 additional shards")
                .condition(EffectCondition.EVERY_NTH_SHOT, 5).build());
        register("sp_point_blank", closeRange(3, 1.75,
                "Point-blank shot up to 3 blocks deals +75% damage"));
        register("sp_chain_reaction", passive(EffectAction.PROJECTILE_RANGE_ADD, 3,
                "Shards fly 3 blocks further"));

        register("pf_deadly_charge", EffectSpec.builder(EffectTrigger.CHARGE,
                        EffectAction.CHARGED_DAMAGE_MULTIPLIER, 2.0,
                        "Charged attack deals +100% damage")
                .condition(EffectCondition.CHARGED_ATTACK, 0).build());
        register("pf_magnet", passive(EffectAction.PULL_TARGET, 5,
                "Pulls target in before striking from up to 5 blocks"));
        register("pf_shockwave_epic", passive(EffectAction.SHOCKWAVE_RADIUS, 4,
                "Knocks back all enemies within 4 blocks on hit"));

        registerMany(EffectSpec.builder(EffectTrigger.MELEE_ATTACK,
                        EffectAction.KILL_ATTACK_SPEED_BONUS, 1.20,
                        "Continuous-mode kill gives +20% attack speed for 3 seconds")
                        .condition(EffectCondition.CONTINUOUS_KILL, 60).build(),
                "chain_deadly_vortex", "drill_deadly_vortex");
        registerMany(new EffectSpec[]{
                        passive(EffectAction.MELEE_DAMAGE_MULTIPLIER, 1.50, "LMB damage +50%"),
                        passive(EffectAction.MELEE_ATTACK_SPEED_MULTIPLIER, 0.85,
                                "attack speed -15%")},
                "chain_heavy_strike", "drill_heavy_strike");
        registerMany(new EffectSpec[]{
                        passive(EffectAction.DAMAGE_MULTIPLIER, 1.50, "Damage +50%"),
                        passive(EffectAction.AMMO_CONSUMPTION_MULTIPLIER, 1.50,
                                "Fuel consumption +50%")},
                "chain_glutton", "drill_glutton");

        register("ad_deadly_radiation", EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.DAMAGE_RAMP, 0.10,
                        "Every 2 seconds of continuous contact adds 10% damage up to +100%")
                .condition(EffectCondition.CONTINUOUS_FIRE, 40).build());
        register("ad_glutton",
                passive(EffectAction.DAMAGE_MULTIPLIER, 1.50, "Damage +50%"),
                passive(EffectAction.AMMO_CONSUMPTION_MULTIPLIER, 1.50,
                        "Charge drain +50%"));
        register("ad_chain_beam", passive(EffectAction.CHAIN_BEAM_TARGETS, 1,
                "Beam jumps to the nearest enemy"));

        register("grf_deadly_shot", lastRound(3.50,
                "Last charge deals +250% damage"));
        register("grf_sniper", longRange(40, 1.40,
                "+40% damage at range >40 blocks"));
        register("grf_silence", passive(EffectAction.SILENCE_SHOT, 1,
                "Shot makes no sound"));
        register("lor_fast_lock", passive(EffectAction.LOCK_ON_TIME_MULTIPLIER, 0.50,
                "Lock-on time -50%"));

        register("bfg_nuclear",
                passive(EffectAction.CHARGED_DAMAGE_MULTIPLIER, 2.50,
                        "Charged projectile deals +150% damage"),
                passive(EffectAction.CHARGED_RADIUS_MULTIPLIER, 1.50,
                        "Charged projectile radius +50%"));
        register("bfg_shockwave", EffectSpec.builder(EffectTrigger.BLOCK_IMPACT,
                        EffectAction.IMPACT_SHOCKWAVE, 10,
                        "Large projectile knocks enemies back 10 blocks")
                .condition(EffectCondition.LARGE_PROJECTILE, 0).build());
        register("bfg_economy", passive(EffectAction.LMB_AMMO_CONSUMPTION_MULTIPLIER,
                0.80, "LMB charge drain -20%"));
    }

    private static EffectSpec passive(EffectAction action, double value, String fragment) {
        return EffectSpec.builder(EffectTrigger.PASSIVE, action, value, fragment).build();
    }

    private static EffectSpec periodicDamage(int interval, double multiplier, String fragment) {
        return EffectSpec.builder(EffectTrigger.PROJECTILE_SPAWN,
                        EffectAction.PERIODIC_SHOT_DAMAGE_MULTIPLIER, multiplier, fragment)
                .condition(EffectCondition.EVERY_NTH_SHOT, interval).build();
    }

    private static EffectSpec firingMovement(double multiplier, String fragment) {
        return EffectSpec.builder(EffectTrigger.WHILE_HELD,
                        EffectAction.FIRING_MOVEMENT_SPEED_MULTIPLIER, multiplier, fragment)
                .condition(EffectCondition.CONTINUOUS_FIRE, 0).build();
    }

    private static EffectSpec burstDamage(int threshold, double multiplier,
                                          boolean untilReload, String fragment) {
        return EffectSpec.builder(EffectTrigger.PROJECTILE_SPAWN,
                        EffectAction.BURST_THRESHOLD_DAMAGE_MULTIPLIER, multiplier, fragment)
                .condition(untilReload ? EffectCondition.SHOT_SEQUENCE_BEYOND
                        : EffectCondition.CONTINUOUS_FIRE, threshold).build();
    }

    private static EffectSpec firstShot(double multiplier, String fragment) {
        return EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.CONDITIONAL_DAMAGE_MULTIPLIER, multiplier, fragment)
                .condition(EffectCondition.FIRST_SHOT_AFTER_RELOAD, 0).build();
    }

    private static EffectSpec lastRound(double multiplier, String fragment) {
        return EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.CONDITIONAL_DAMAGE_MULTIPLIER, multiplier, fragment)
                .condition(EffectCondition.LAST_ROUND, 0).build();
    }

    private static EffectSpec closeRange(double range, double multiplier, String fragment) {
        return EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.CONDITIONAL_DAMAGE_MULTIPLIER, multiplier, fragment)
                .condition(EffectCondition.TARGET_WITHIN_DISTANCE, range).build();
    }

    private static EffectSpec longRange(double range, double multiplier, String fragment) {
        return EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.CONDITIONAL_DAMAGE_MULTIPLIER, multiplier, fragment)
                .condition(EffectCondition.TARGET_BEYOND_DISTANCE, range).build();
    }

    private static EffectSpec stationary(int ticks, double multiplier, String fragment) {
        return EffectSpec.builder(EffectTrigger.PROJECTILE_SPAWN,
                        EffectAction.STATIONARY_DAMAGE_MULTIPLIER, multiplier, fragment)
                .condition(EffectCondition.STATIONARY_FOR_TICKS, ticks).build();
    }

    private static EffectSpec scoped(int ticks, double multiplier, String fragment) {
        return EffectSpec.builder(EffectTrigger.PROJECTILE_SPAWN,
                        EffectAction.SCOPED_DAMAGE_MULTIPLIER, multiplier, fragment)
                .condition(EffectCondition.SCOPED_FOR_TICKS, ticks).build();
    }

    private static EffectSpec consecutive(double multiplier, String fragment) {
        return EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.CONSECUTIVE_TARGET_DAMAGE_MULTIPLIER, multiplier, fragment)
                .condition(EffectCondition.CONSECUTIVE_SAME_TARGET, 0).build();
    }

    private static EffectSpec suppression(int ticks, double radius, String fragment) {
        return EffectSpec.builder(EffectTrigger.BLOCK_IMPACT,
                        EffectAction.SUPPRESSION, ticks, fragment)
                .condition(EffectCondition.ALWAYS, radius).build();
    }

    private static void registerMany(EffectSpec effect, String... ids) {
        for (String id : ids) register(id, effect);
    }

    private static void registerMany(EffectSpec[] effects, String... ids) {
        for (String id : ids) register(id, effects);
    }

    private static void register(String id, EffectSpec... effects) {
        if (DECLARED.containsKey(id)) {
            throw new IllegalStateException("Duplicate Epic upgrade definition: " + id);
        }
        DECLARED.put(id, Collections.unmodifiableList(Arrays.asList(effects)));
    }
}

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

/** Explicit executable definitions for all Rare upgrades. */
public final class RareUpgradeDefinitions {
    public static final int EXPECTED_COUNT = 160;
    private static final Map<String, List<EffectSpec>> DECLARED = new LinkedHashMap<>();
    private static final Map<String, UpgradeDefinition> DEFINITIONS = new LinkedHashMap<>();
    private static boolean declared;

    private RareUpgradeDefinitions() {
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
                throw new IllegalStateException("Rare definition references unknown upgrade: " + entry.getKey());
            }
            if (buff.getRarity() != UpgradeRarity.RARE) {
                throw new IllegalStateException("Non-Rare upgrade in Rare registry: " + entry.getKey());
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
                if (buff.getRarity() == UpgradeRarity.RARE) {
                    sourceCount++;
                    if (!DEFINITIONS.containsKey(buff.getId())) missing.add(buff.getId());
                }
            }
        }
        if (sourceCount != EXPECTED_COUNT || DEFINITIONS.size() != EXPECTED_COUNT || !missing.isEmpty()) {
            throw new IllegalStateException("Rare upgrade coverage failed: source=" + sourceCount
                    + ", definitions=" + DEFINITIONS.size() + ", missing=" + missing);
        }
        for (UpgradeDefinition definition : DEFINITIONS.values()) {
            if (definition.getEffects().isEmpty()) {
                throw new IllegalStateException("Rare upgrade has no executable effects: " + definition.getId());
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
        registerMany(EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.INCENDIARY_DURATION, 60,
                        "Ignites target for 3 seconds").build(),
                "hmg_incendiary", "db_incendiary", "rev_incendiary", "thom_incendiary",
                "akm_incendiary", "bolt_incendiary", "m4_incendiary", "sab_incendiary",
                "pist_incendiary", "cs_incendiary", "mac_incendiary", "aug_incendiary",
                "hb_hellfire", "lmg_incendiary", "min_incendiary", "as50_incendiary",
                "vec_incendiary", "scar_incendiary", "lr_incendiary", "br_incendiary",
                "bs_incendiary", "p90_incendiary", "pul_incendiary", "sp_incendiary",
                "pf_fire", "chain_fire", "grf_incendiary", "drill_fire", "lp_incendiary");
        register("flame_napalm", EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                EffectAction.INCENDIARY_DURATION, 100,
                "Ignites enemies for 5 seconds").build());

        registerMany(EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.EXPLOSION_POWER, 1.5,
                        "Small explosion on impact").build(),
                "hmg_explosive", "db_explosive", "rev_explosive", "gr_explosive",
                "thom_explosive", "akm_explosive", "bolt_explosive", "m4_explosive",
                "sab_explosive", "pist_explosive", "cs_explosive", "mac_explosive",
                "aug_explosive", "hb_explosive_charge", "bio_explosive_acid", "lmg_explosive",
                "min_explosive", "as50_explosive", "vec_explosive", "scar_explosive",
                "lr_explosive", "br_explosive", "bs_explosive", "p90_explosive",
                "sp_explosive", "bfg_explosive", "grf_explosive", "lp_explosive");
        registerMany(EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.EXPLOSION_POWER, 2.0,
                        "2 block explosion on impact").build(),
                "rev_shatter", "sr_explosive", "pul_explosive");
        register("pf_explosive", EffectSpec.builder(EffectTrigger.MELEE_ATTACK,
                EffectAction.EXPLOSION_POWER, 3.0,
                "Small explosion on hit (3 block radius)").build());
        registerMany(EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.RANDOM_EXPLOSION_POWER, 1.5,
                        "10% chance of small explosion on hit").probability(0.10).build(),
                "flame_explosive", "tesla_explosive_arc", "chain_explosive", "ad_explosive",
                "drill_explosive");

        registerMany(passive(EffectAction.ARMOR_PENETRATION_ADD, 0.40,
                        "Ignores 40% of target armor"),
                "db_armor_piercing", "cs_armor_piercing", "hb_armor_piercing",
                "lr_armor_piercing", "pf_armor_piercing");
        registerMany(passive(EffectAction.ARMOR_PENETRATION_ADD, 0.50,
                        "Ignores 50% of target armor"),
                "bolt_armor_piercing", "as50_armor_piercing", "grf_armor_piercing");
        registerMany(passive(EffectAction.ARMOR_PENETRATION_ADD, 0.30,
                        "Ignores 30% of target armor"),
                "bio_acid", "scar_armor_piercing", "br_armor_piercing",
                "bs_armor_piercing", "chain_armor_piercing", "drill_armor_piercing");
        registerMany(passive(EffectAction.ARMOR_PENETRATION_ADD, 0.35,
                        "Ignores 35% of target armor"),
                "pul_armor_piercing", "ad_armor_piercing", "bfg_armor_piercing");
        registerMany(passive(EffectAction.ARMOR_PENETRATION_ADD, 0.25,
                        "Ignores 25% of target armor"),
                "sp_armor_piercing", "lp_armor_piercing");

        registerMany(passive(EffectAction.FIRE_DELAY_MULTIPLIER, 1.0 / 1.30,
                        "Fire Rate +30%"),
                "thom_speed_mag", "akm_speed_fire", "m4_speed_fire", "sab_speed_fire",
                "mac_speed_fire", "aug_speed_fire", "min_hurricane");
        registerMany(passive(EffectAction.FIRE_DELAY_MULTIPLIER, 1.0 / 1.35,
                        "Fire Rate +35%"),
                "pist_speed_trigger", "vec_speed_fire", "br_speed_shot", "p90_speed_fire");
        register("lmg_storm_fire", passive(EffectAction.FIRE_DELAY_MULTIPLIER,
                1.0 / 1.40, "Fire Rate +40%"));
        register("rev_fast_hand", EffectSpec.builder(EffectTrigger.BEFORE_SHOT,
                        EffectAction.CONDITIONAL_FIRE_DELAY_MULTIPLIER, 1.0 / 1.25,
                        "+25% Fire Rate when firing on the move")
                .condition(EffectCondition.MOVING, 0).build());

        registerMany(EffectSpec.builder(EffectTrigger.PASSIVE,
                        EffectAction.CONDITIONAL_SPREAD_MULTIPLIER, 1.0 / 1.20,
                        "+20% accuracy when firing bursts")
                        .condition(EffectCondition.BURST_FIRE, 0).build(),
                "thom_combat_stock", "akm_army_stock", "m4_army_stock", "aug_adaptive_stock");
        register("sab_camouflage", EffectSpec.builder(EffectTrigger.PASSIVE,
                        EffectAction.CONDITIONAL_SPREAD_MULTIPLIER, 1.0 / 1.25,
                        "+25% accuracy when firing from cover (crouching)")
                .condition(EffectCondition.PRONE, 0).build());

        register("hmg_long_barrel", passive(EffectAction.RANGE_MULTIPLIER, 1.50, "+50% range"));
        register("tesla_long_arc", passive(EffectAction.RANGE_MULTIPLIER, 1.50, "Arc range +50%"));

        register("gr_sixth_shot", EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.CONDITIONAL_DAMAGE_MULTIPLIER, 2.0,
                        "Last round in cylinder deals +100% damage")
                .condition(EffectCondition.LAST_ROUND, 0).build());
        register("bolt_first_shot", EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.CONDITIONAL_DAMAGE_MULTIPLIER, 1.50,
                        "First shot after reload deals +50% damage")
                .condition(EffectCondition.FIRST_SHOT_AFTER_RELOAD, 0).build());
        register("mac_small_beast", EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.CONDITIONAL_DAMAGE_MULTIPLIER, 1.25,
                        "+25% damage at close range (up to 5 blocks)")
                .condition(EffectCondition.TARGET_WITHIN_DISTANCE, 5).build());
        register("vec_supersonic", EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.CONDITIONAL_DAMAGE_MULTIPLIER, 1.20,
                        "+20% damage at range >15 blocks")
                .condition(EffectCondition.TARGET_BEYOND_DISTANCE, 15).build());

        register("hmg_triple_shot",
                randomProjectiles(1.0, 2, 0, "Shoots 3 bullets at once"),
                EffectSpec.builder(EffectTrigger.AMMO_CONSUMPTION,
                        EffectAction.EXTRA_PROJECTILE_AMMO_COST, 3.0,
                        "uses 4 ammo").build());
        register("db_cartridge", passive(EffectAction.PROJECTILE_COUNT_ADD, 3,
                "+3 additional pellets per shot"));
        register("cs_cartridge", passive(EffectAction.PROJECTILE_COUNT_ADD, 4,
                "+4 additional pellets"));
        register("pist_double_shot", randomProjectiles(0.15, 1, 1,
                "15% chance to fire twice (uses 2 ammo)"));
        register("hb_double_shot", randomProjectiles(0.15, 1, 1,
                "15% chance to fire twice (uses 2 charges)"));
        register("lr_double_beam", randomProjectiles(0.15, 1, 1,
                "15% chance to fire twice (uses 2 charges)"));
        register("bs_triple_shot", randomProjectiles(0.15, 2, 2,
                "15% chance to fire three times (uses 3 charges)"));
        register("sr_double_wave", randomProjectiles(0.15, 1, 1,
                "15% chance to fire twice (uses 2 charges)"));
        register("p90_double_burst", randomProjectiles(0.10, 1, 0,
                "10% chance to fire 2 bullets at once"));
        register("pul_double_pulse", randomProjectiles(0.15, 1, 1,
                "15% chance to fire twice (uses 2 charges)"));
        register("bfg_double_shot", randomProjectiles(0.10, 1, 0,
                "LMB has 10% chance to fire 2 projectiles"));
        register("lp_double_beam", randomProjectiles(0.15, 1, 1,
                "15% chance to fire twice (uses 2 charges)"));
        register("pf_double_strike", EffectSpec.builder(EffectTrigger.MELEE_ATTACK,
                EffectAction.RANDOM_DAMAGE_MULTIPLIER, 2.0,
                "15% chance to strike twice").probability(0.15).build());

        register("gr_glowing_ammo", EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                EffectAction.GLOW_DURATION, 60,
                "Highlighting target for 3 seconds").build());
        register("gr_treasure", EffectSpec.builder(EffectTrigger.MELEE_ATTACK,
                EffectAction.GOLD_DROP_CHANCE, 0,
                "10% chance to drop a gold ingot on kill").probability(0.10).build());
        register("flame_smoke", EffectSpec.builder(EffectTrigger.PROJECTILE_SPAWN,
                EffectAction.SMOKE_SCREEN, 1,
                "Creates a smoke cloud while firing, hiding the player").build());

        register("flame_hell",
                passive(EffectAction.DAMAGE_MULTIPLIER, 1.30, "Damage +30%"),
                EffectSpec.builder(EffectTrigger.AMMO_CONSUMPTION,
                        EffectAction.AMMO_CONSUMPTION_MULTIPLIER, 1.20,
                        "fuel consumption +20%").build());

        registerMany(EffectSpec.builder(EffectTrigger.BLOCK_IMPACT,
                        EffectAction.FIRE_ZONE_DURATION, 100,
                        "Creates a fire zone after explosion for 5 seconds").build(),
                "baz_incendiary", "grp_incendiary", "gl_incendiary", "lor_incendiary");
        registerMany(EffectSpec.builder(EffectTrigger.BLOCK_IMPACT,
                        EffectAction.CLUSTER_COUNT, 3,
                        "Splits into 3 smaller projectiles on explosion").build(),
                "baz_cluster", "grp_cluster", "gl_cluster", "lor_cluster");
        registerMany(new EffectSpec[]{
                        passive(EffectAction.DAMAGE_MULTIPLIER, 1.50, "Damage +50%"),
                        EffectSpec.builder(EffectTrigger.PROJECTILE_SPAWN,
                                EffectAction.PROJECTILE_SPEED_MULTIPLIER, 0.80,
                                "flight speed -20%").build()},
                "baz_heavy_rocket", "grp_heavy_rocket", "gl_heavy_grenade", "lor_heavy_rocket");
        registerMany(new EffectSpec[]{
                        passive(EffectAction.DAMAGE_MULTIPLIER, 1.20, "+20% damage"),
                        EffectSpec.builder(EffectTrigger.PROJECTILE_SPAWN,
                                EffectAction.EXPLOSION_RADIUS_MULTIPLIER, 1.20,
                                "+20% explosion radius").build()},
                "baz_tactical", "grp_tactical", "gl_tactical", "lor_tactical");

        register("bio_slow_action", EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                EffectAction.POISON_DURATION_ADD, 60,
                "Poison duration +3 seconds").build());
        register("bio_contagion", EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                EffectAction.CONTAGION_CHANCE, 3,
                "10% chance to infect nearby enemies (3 block radius)").probability(0.10).build());
        register("tesla_chain", EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                EffectAction.CHAIN_LIGHTNING_TARGETS, 2,
                "Electricity jumps to +2 enemies").build());
        register("tesla_paralyze", EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                EffectAction.STUN_DURATION, 20,
                "Stuns target for 1 second").build());
        register("sr_stun", EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                EffectAction.STUN_DURATION, 60,
                "Stun duration total 3 seconds").build());
        register("sr_piercing", EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                EffectAction.PIERCING_COUNT, 3,
                "Sound wave pierces up to 3 targets").build());
        register("sp_cascade", EffectSpec.builder(EffectTrigger.PROJECTILE_SPAWN,
                EffectAction.RANDOM_EXTRA_PROJECTILES, 1,
                "Shards can split again (10% chance)").probability(0.10)
                .condition(EffectCondition.ALWAYS, 0).build());

        registerMany(EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.RADIATION_DURATION, 100,
                        "Applies radiation effect for 5 seconds").build(),
                "ad_radioactive", "bfg_radioactive");
        register("ad_wide_beam", passive(EffectAction.BEAM_WIDTH_MULTIPLIER, 1.50,
                "Beam width +50%"));
        register("chain_bloody", EffectSpec.builder(EffectTrigger.MELEE_ATTACK,
                EffectAction.FUEL_RESTORE_ON_KILL, 20,
                "15% chance to restore 20 fuel on kill").probability(0.15).build());
        register("drill_wide", EffectSpec.builder(EffectTrigger.BLOCK_BREAK,
                EffectAction.MINING_AREA_SIZE, 5,
                "Mining radius increases to 5x5").build());

        registerMany(new EffectSpec[]{
                        passive(EffectAction.DAMAGE_MULTIPLIER, 1.30, "Damage +30%"),
                        passive(EffectAction.FIRE_DELAY_MULTIPLIER, 1.0 / 0.85, "Fire Rate -15%")},
                "lmg_heavy_barrel_2", "min_heavy_barrel_2", "scar_heavy_bullet",
                "grf_heavy_bullet");
        register("as50_heavy_bullet",
                passive(EffectAction.DAMAGE_MULTIPLIER, 1.35, "Damage +35%"),
                passive(EffectAction.FIRE_DELAY_MULTIPLIER, 1.0 / 0.85, "Fire Rate -15%"));
    }

    private static EffectSpec randomProjectiles(double chance, int extra, int extraAmmo, String fragment) {
        return EffectSpec.builder(EffectTrigger.PROJECTILE_SPAWN,
                        EffectAction.RANDOM_EXTRA_PROJECTILES, extra, fragment)
                .probability(chance).condition(EffectCondition.ALWAYS, extraAmmo).build();
    }

    private static EffectSpec passive(EffectAction action, double value, String fragment) {
        return EffectSpec.builder(EffectTrigger.PASSIVE, action, value, fragment).build();
    }

    private static void registerMany(EffectSpec effect, String... ids) {
        for (String id : ids) register(id, effect);
    }

    private static void registerMany(EffectSpec[] effects, String... ids) {
        for (String id : ids) register(id, effects);
    }

    private static void register(String id, EffectSpec... effects) {
        if (DECLARED.containsKey(id)) {
            throw new IllegalStateException("Duplicate Rare upgrade definition: " + id);
        }
        DECLARED.put(id, Collections.unmodifiableList(Arrays.asList(effects)));
    }
}

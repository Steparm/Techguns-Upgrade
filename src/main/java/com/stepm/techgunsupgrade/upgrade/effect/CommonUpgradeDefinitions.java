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

/** Explicit effect definitions for every Common upgrade in the original data set. */
public final class CommonUpgradeDefinitions {
    public static final int EXPECTED_COUNT = 160;

    private static final Map<String, List<EffectSpec>> DECLARED = new LinkedHashMap<>();
    private static final Map<String, UpgradeDefinition> DEFINITIONS = new LinkedHashMap<>();
    private static boolean declared;

    private CommonUpgradeDefinitions() {
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
                throw new IllegalStateException("Common effect definition references unknown upgrade: " + entry.getKey());
            }
            if (buff.getRarity() != UpgradeRarity.COMMON) {
                throw new IllegalStateException("Non-Common upgrade in Common registry: " + entry.getKey());
            }
            DEFINITIONS.put(entry.getKey(), new UpgradeDefinition(
                    entry.getKey(), weaponId, buff.getRarity(), buff.getDescription(), entry.getValue()));
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
        int commonCount = 0;
        for (List<UpgradeBuff> buffs : WeaponUpgrades.getAllWeaponUpgrades().values()) {
            for (UpgradeBuff buff : buffs) {
                if (buff.getRarity() == UpgradeRarity.COMMON) {
                    commonCount++;
                    if (!DEFINITIONS.containsKey(buff.getId())) missing.add(buff.getId());
                }
            }
        }
        if (commonCount != EXPECTED_COUNT || DEFINITIONS.size() != EXPECTED_COUNT || !missing.isEmpty()) {
            throw new IllegalStateException("Common upgrade coverage failed: source=" + commonCount
                    + ", definitions=" + DEFINITIONS.size() + ", missing=" + missing);
        }
        for (UpgradeDefinition definition : DEFINITIONS.values()) {
            if (definition.getEffects().isEmpty()) {
                throw new IllegalStateException("Upgrade has no executable effects: " + definition.getId());
            }
            for (EffectSpec effect : definition.getEffects()) {
                if (effect.getSourceFragment() == null || effect.getSourceFragment().trim().isEmpty()) {
                    throw new IllegalStateException("Effect has no source fragment: " + definition.getId());
                }
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
        registerMany(passive(EffectAction.DAMAGE_MULTIPLIER, 1.10, "Damage +10%"),
                "db_heavy_barrel", "rev_heavy_trigger", "gr_gold_trigger", "thom_heavy_barrel",
                "akm_heavy_barrel", "bolt_heavy_barrel", "m4_heavy_barrel", "sab_heavy_barrel",
                "pist_heavy_trigger", "cs_heavy_barrel", "mac_heavy_barrel", "aug_heavy_barrel",
                "hb_emitter", "bio_emitter", "tesla_coil", "lmg_heavy_barrel", "min_heavy_barrel",
                "as50_heavy_barrel", "vec_heavy_barrel", "scar_heavy_barrel", "lr_emitter",
                "br_emitter", "bs_emitter", "sr_emitter", "p90_heavy_barrel", "pul_emitter",
                "sp_emitter", "pf_strong_piston", "chain_heavy_chain", "ad_emitter", "grf_emitter",
                "drill_strong_motor", "bfg_emitter", "lp_emitter");
        registerMany(passive(EffectAction.DAMAGE_MULTIPLIER, 1.15, "Damage +15%"),
                "baz_heavy_barrel", "grp_heavy_barrel", "gl_heavy_barrel", "lor_heavy_barrel");

        register("hmg_rusty_barrel",
                passive(EffectAction.DAMAGE_MULTIPLIER, 1.10, "Damage +10%"),
                EffectSpec.builder(EffectTrigger.BEFORE_SHOT, EffectAction.JAM_CHANCE, 0.0,
                        "jam chance +5%").probability(0.05).build());
        register("db_short_barrel",
                EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.CONDITIONAL_DAMAGE_MULTIPLIER, 1.15,
                        "Damage +15% at close range (up to 5 blocks)")
                        .condition(EffectCondition.TARGET_WITHIN_DISTANCE, 5.0).build(),
                passive(EffectAction.RANGE_MULTIPLIER, 0.80, "-20% range"));
        register("cs_short_barrel",
                EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.CONDITIONAL_DAMAGE_MULTIPLIER, 1.15,
                        "Damage +15% at close range (up to 5 blocks)")
                        .condition(EffectCondition.TARGET_WITHIN_DISTANCE, 5.0).build(),
                passive(EffectAction.RANGE_MULTIPLIER, 0.80, "-20% range"));

        registerMany(passive(EffectAction.FIRE_DELAY_MULTIPLIER, 1.0 / 1.15, "Fire Rate +15%"),
                "rev_handle_grip", "gr_gold_grip", "thom_strong_spring", "akm_spring",
                "m4_strong_spring", "sab_strong_spring", "pist_grip", "mac_strong_spring",
                "aug_strong_spring", "hb_quick_charge", "tesla_quick_charge", "lmg_strong_spring",
                "vec_strong_spring", "scar_strong_spring", "lr_quick_charge", "br_quick_charge",
                "bs_quick_charge", "sr_quick_tune", "p90_strong_spring", "pul_quick_charge",
                "sp_quick_charge", "lp_quick_charge");
        register("bio_quick_feed", passive(EffectAction.FIRE_DELAY_MULTIPLIER, 1.0 / 1.15,
                "Fire Rate (LMB) +15%"));
        register("hmg_heavy_trigger", passive(EffectAction.FIRE_DELAY_MULTIPLIER, 1.0 / 0.90,
                "Fire Rate -10% (slower)"));

        registerMany(passive(EffectAction.SPREAD_MULTIPLIER, 1.0 / 1.15, "Spread -15%"),
                "bolt_stock", "hb_stabilizer", "bio_stabilizer", "tesla_stabilizer",
                "min_stabilizer", "as50_stock", "vec_stabilizer", "scar_stock", "lr_stabilizer",
                "br_stabilizer", "bs_stabilizer", "sr_stabilizer", "p90_stock", "pul_stabilizer",
                "sp_stabilizer", "ad_stabilizer", "grf_stabilizer", "bfg_stabilizer", "lp_stabilizer");
        register("cs_rifled_barrel", passive(EffectAction.SPREAD_MULTIPLIER, 1.0 / 1.15,
                "Spread -15% (slightly more accurate)"));
        register("db_smooth_barrel", passive(EffectAction.SPREAD_MULTIPLIER, 1.0 / 1.10,
                "Spread -10% (slightly more accurate)"));
        register("hmg_crooked_sight", passive(EffectAction.SPREAD_MULTIPLIER, 1.0 / 0.85,
                "Spread +15% (worse accuracy)"));
        register("gr_gold_inlay", passive(EffectAction.SPREAD_MULTIPLIER, 1.0 / 1.15,
                "+15% accuracy"));
        register("flame_stabilizer", passive(EffectAction.SPREAD_MULTIPLIER, 1.0 / 1.15,
                "Flame spread -15%"));
        registerMany(passive(EffectAction.SPREAD_MULTIPLIER, 1.0 / 1.15, "Rocket accuracy +15%"),
                "baz_scope", "grp_scope");
        register("gl_scope", passive(EffectAction.SPREAD_MULTIPLIER, 1.0 / 1.15,
                "Grenade accuracy +15%"));

        registerMany(EffectSpec.builder(EffectTrigger.PASSIVE,
                        EffectAction.CONDITIONAL_SPREAD_MULTIPLIER, 1.0 / 1.15,
                        "-15% spread when firing bursts")
                        .condition(EffectCondition.BURST_FIRE, 0).build(),
                "thom_bipod", "akm_stock", "m4_tactical_stock", "sab_tactical_stock", "mac_bipod");
        register("lmg_bipod", EffectSpec.builder(EffectTrigger.PASSIVE,
                        EffectAction.CONDITIONAL_SPREAD_MULTIPLIER, 1.0 / 1.20,
                        "-20% spread when prone")
                        .condition(EffectCondition.PRONE, 0).build());

        registerMany(passive(EffectAction.RANGE_MULTIPLIER, 1.30, "+30% range"),
                "db_long_barrel", "rev_long_barrel", "bolt_long_barrel", "as50_long_barrel");
        register("pist_long_barrel", passive(EffectAction.RANGE_MULTIPLIER, 1.25, "+25% range"));
        register("flame_valve", passive(EffectAction.RANGE_MULTIPLIER, 1.15, "Flame range +15%"));
        registerMany(passive(EffectAction.RANGE_MULTIPLIER, 1.30, "Rocket flight range +30%"),
                "baz_long_launcher", "grp_long_launcher", "lor_long_launcher");
        register("gl_long_barrel", passive(EffectAction.RANGE_MULTIPLIER, 1.30,
                "Grenade flight range +30%"));

        registerMany(passive(EffectAction.MAGAZINE_ADD, 1, "+1 ammo in cylinder (total 7)"),
                "rev_big_cylinder", "gr_gold_cylinder");
        register("hmg_big_mag", passive(EffectAction.MAGAZINE_ADD, 1, "+1 ammo in magazine"));
        register("bolt_big_mag", passive(EffectAction.MAGAZINE_ADD, 1, "+1 ammo (total 7)"));
        registerMany(passive(EffectAction.MAGAZINE_ADD, 1, "+1 charge (total 9)"),
                "sr_big_resonator", "grf_big_battery");
        register("cs_long_mag", passive(EffectAction.MAGAZINE_ADD, 2, "+2 ammo (total 10)"));
        register("as50_big_mag", passive(EffectAction.MAGAZINE_ADD, 2, "+2 ammo (total 12)"));
        register("hb_big_capacitor", passive(EffectAction.MAGAZINE_ADD, 2, "+2 charges (total 12)"));
        register("lmg_long_mag", passive(EffectAction.MAGAZINE_ADD, 20, "+20 ammo (total 120)"));
        register("flame_big_tank", passive(EffectAction.MAGAZINE_ADD, 20, "+20 fuel (total 120)"));
        register("pist_big_mag", passive(EffectAction.MAGAZINE_ADD, 3, "+3 ammo (total 21)"));
        registerMany(passive(EffectAction.MAGAZINE_ADD, 3, "+3 charges (total 23)"),
                "sp_big_battery", "bfg_big_battery", "lp_big_battery");
        register("scar_long_mag", passive(EffectAction.MAGAZINE_ADD, 4, "+4 ammo (total 24)"));
        register("mac_long_mag", passive(EffectAction.MAGAZINE_ADD, 4, "+4 ammo (total 36)"));
        register("ad_big_battery", passive(EffectAction.MAGAZINE_ADD, 4, "+4 charges (total 24)"));
        register("pul_big_battery", passive(EffectAction.MAGAZINE_ADD, 4, "+4 charges (total 40)"));
        register("min_long_mag", passive(EffectAction.MAGAZINE_ADD, 40, "+40 ammo (total 240)"));
        register("thom_long_mag", passive(EffectAction.MAGAZINE_ADD, 5, "+5 ammo (total 25)"));
        register("vec_long_mag", passive(EffectAction.MAGAZINE_ADD, 5, "+5 ammo (total 30)"));
        registerMany(passive(EffectAction.MAGAZINE_ADD, 5, "+5 ammo (total 35)"),
                "akm_mag", "m4_long_mag", "sab_long_mag", "aug_long_mag", "bio_big_tank");
        register("p90_long_mag", passive(EffectAction.MAGAZINE_ADD, 5, "+5 ammo (total 45)"));
        register("tesla_big_capacitor", passive(EffectAction.MAGAZINE_ADD, 5, "+5 charges (total 30)"));
        register("bs_big_battery", passive(EffectAction.MAGAZINE_ADD, 5, "+5 charges (total 45)"));
        register("lr_big_battery", passive(EffectAction.MAGAZINE_ADD, 5, "+5 charges (total 50)"));
        register("br_big_battery", passive(EffectAction.MAGAZINE_ADD, 5, "+5 charges (total 55)"));
        register("chain_big_tank", passive(EffectAction.MAGAZINE_ADD, 50, "+50 fuel (total 350)"));
        register("drill_big_tank", passive(EffectAction.MAGAZINE_MULTIPLIER, 1.25,
                "+25% max fuel"));

        registerMany(EffectSpec.builder(EffectTrigger.WHILE_HELD,
                        EffectAction.MOVEMENT_SPEED_MULTIPLIER, 1.10,
                        "Movement speed with weapon +10%").build(),
                "baz_light_frame", "grp_light_frame", "gl_light_frame", "aug_plastic_stock",
                "lor_light_frame");
        registerMany(EffectSpec.builder(EffectTrigger.MELEE_ATTACK,
                        EffectAction.MELEE_ATTACK_SPEED_MULTIPLIER, 1.15,
                        "Attack speed (LMB) +15%").build(),
                "chain_sharp", "drill_sharp");
        register("pf_stabilizer", EffectSpec.builder(EffectTrigger.MELEE_ATTACK,
                EffectAction.MELEE_ATTACK_SPEED_MULTIPLIER, 1.10, "Attack speed +10%").build());
        register("pf_heavy_frame", EffectSpec.builder(EffectTrigger.MELEE_ATTACK,
                EffectAction.KNOCKBACK_BONUS, 0.25, "Knockback +25%").build());

        registerMany(EffectSpec.builder(EffectTrigger.AMMO_CONSUMPTION,
                        EffectAction.AMMO_CONSUMPTION_MULTIPLIER, 0.85,
                        "Fuel consumption in continuous mode -15%")
                        .condition(EffectCondition.CONTINUOUS_FIRE, 0).build(),
                "chain_economy", "drill_economy");
        register("ad_quick_charge", EffectSpec.builder(EffectTrigger.AMMO_CONSUMPTION,
                EffectAction.AMMO_CONSUMPTION_MULTIPLIER, 0.90,
                "Charge drain speed -10%").build());
        register("pf_quick_reload", EffectSpec.builder(EffectTrigger.CHARGE,
                EffectAction.CHARGE_TIME_MULTIPLIER, 0.85, "Charge time -15%").build());
        register("bfg_quick_charge", EffectSpec.builder(EffectTrigger.CHARGE,
                EffectAction.CHARGE_TIME_MULTIPLIER, 1.0 / 1.15,
                "RMB charge speed +15%").build());
        register("min_strong_motor", EffectSpec.builder(EffectTrigger.BEFORE_SHOT,
                EffectAction.SPIN_UP_TIME_MULTIPLIER, 1.0 / 1.30,
                "Spin-up speed +30%").build());
        register("lor_scope", EffectSpec.builder(EffectTrigger.LOCK_ON,
                EffectAction.LOCK_ON_TIME_MULTIPLIER, 0.85,
                "Lock-on time -15%").build());
        register("grf_quick_reload", passive(EffectAction.RELOAD_TIME_MULTIPLIER, 0.85,
                "Reload time -15%"));
        register("flame_silencer", EffectSpec.builder(EffectTrigger.BEFORE_SHOT,
                EffectAction.DETECTION_RADIUS_MULTIPLIER, 0.70,
                "Enemies see fire from shorter distance").build());
    }

    private static EffectSpec passive(EffectAction action, double value, String fragment) {
        return EffectSpec.builder(EffectTrigger.PASSIVE, action, value, fragment).build();
    }

    private static void registerMany(EffectSpec effect, String... ids) {
        for (String id : ids) register(id, effect);
    }

    private static void register(String id, EffectSpec... effects) {
        if (DECLARED.containsKey(id)) {
            throw new IllegalStateException("Duplicate Common upgrade definition: " + id);
        }
        DECLARED.put(id, Collections.unmodifiableList(Arrays.asList(effects)));
    }
}

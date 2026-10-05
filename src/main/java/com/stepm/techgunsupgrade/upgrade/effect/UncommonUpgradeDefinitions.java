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

/** Explicit executable definitions for every Uncommon upgrade in the source data set. */
public final class UncommonUpgradeDefinitions {
    public static final int EXPECTED_COUNT = 160;

    private static final Map<String, List<EffectSpec>> DECLARED = new LinkedHashMap<>();
    private static final Map<String, UpgradeDefinition> DEFINITIONS = new LinkedHashMap<>();
    private static boolean declared;

    private UncommonUpgradeDefinitions() {
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
                throw new IllegalStateException("Uncommon definition references unknown upgrade: " + entry.getKey());
            }
            if (buff.getRarity() != UpgradeRarity.UNCOMMON) {
                throw new IllegalStateException("Non-Uncommon upgrade in Uncommon registry: " + entry.getKey());
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
        int sourceCount = 0;
        for (List<UpgradeBuff> buffs : WeaponUpgrades.getAllWeaponUpgrades().values()) {
            for (UpgradeBuff buff : buffs) {
                if (buff.getRarity() == UpgradeRarity.UNCOMMON) {
                    sourceCount++;
                    if (!DEFINITIONS.containsKey(buff.getId())) missing.add(buff.getId());
                }
            }
        }
        if (sourceCount != EXPECTED_COUNT || DEFINITIONS.size() != EXPECTED_COUNT || !missing.isEmpty()) {
            throw new IllegalStateException("Uncommon upgrade coverage failed: source=" + sourceCount
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
        registerMany(passive(EffectAction.DAMAGE_MULTIPLIER, 1.25, "Damage +25%"),
                "db_strong_ammo", "rev_magnum", "gr_royal_ammo", "thom_magnum", "akm_magnum",
                "bolt_magnum", "m4_magnum", "sab_magnum", "pist_magnum", "cs_magnum",
                "mac_magnum", "aug_magnum", "hb_magnum_charge", "tesla_power", "lmg_magnum",
                "min_magnum", "as50_magnum", "vec_magnum", "scar_magnum", "lr_power_beam",
                "br_power_charge", "bs_power_charge", "sr_power_wave", "p90_magnum", "pul_power",
                "sp_power", "pf_power", "chain_power", "ad_power", "grf_power", "drill_power",
                "bfg_power", "lp_power");
        register("db_magnum", passive(EffectAction.DAMAGE_MULTIPLIER, 1.30, "Damage +30%"));
        register("hmg_large_shot",
                passive(EffectAction.DAMAGE_MULTIPLIER, 1.20, "Damage +20%"),
                passive(EffectAction.SPREAD_MULTIPLIER, 1.10, "-10% accuracy"));
        register("hmg_sawed_off", EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.CONDITIONAL_DAMAGE_MULTIPLIER, 1.30,
                        "Damage +30% at close range")
                .condition(EffectCondition.TARGET_WITHIN_DISTANCE, 5.0).build());

        registerMany(passive(EffectAction.RELOAD_TIME_MULTIPLIER, 1.0 / 1.5, "Reload 1.5x faster"),
                "db_quick_reload", "rev_quick_reload", "gr_luxury_reload", "thom_quick_reload",
                "akm_quick_reload", "bolt_quick_reload", "m4_quick_reload", "sab_quick_reload",
                "pist_quick_reload", "cs_quick_reload", "mac_quick_reload", "baz_quick_reload",
                "grp_quick_reload", "gl_quick_reload", "aug_quick_reload", "hb_quick_reload",
                "bio_quick_reload", "tesla_quick_reload", "lmg_quick_reload", "min_quick_reload",
                "as50_quick_reload", "vec_quick_reload", "scar_quick_reload", "lr_quick_reload",
                "br_quick_reload", "bs_quick_reload", "sr_quick_reload", "p90_quick_reload",
                "pul_quick_reload", "sp_quick_reload", "ad_quick_reload", "grf_quick_reload_2",
                "lor_quick_reload", "bfg_quick_reload", "lp_quick_reload");

        registerMany(passive(EffectAction.SPREAD_MULTIPLIER, 1.0 / 1.20, "Spread -20%"),
                "rev_scope", "thom_scope", "akm_scope", "m4_red_dot", "sab_red_dot", "pist_scope",
                "mac_red_dot", "aug_holo", "hb_focus_lens", "bio_precision", "tesla_focus_lens",
                "lmg_scope", "min_scope", "vec_holo", "scar_scope", "lr_focus_lens",
                "br_focus_lens", "bs_focus_lens", "sr_focus_emitter", "p90_holo",
                "pul_focus_lens", "sp_focus_lens", "ad_focus_lens", "grf_focus_lens",
                "bfg_focus_lens", "lp_focus_lens");
        registerMany(passive(EffectAction.SPREAD_MULTIPLIER, 1.0 / 1.25, "Spread -25%"),
                "bolt_sniper_scope", "as50_sniper_scope");
        register("cs_stock", EffectSpec.builder(EffectTrigger.PASSIVE,
                        EffectAction.CONDITIONAL_SPREAD_MULTIPLIER, 0.80,
                        "-20% spread when crouching")
                .condition(EffectCondition.PRONE, 0).build());

        registerMany(passive(EffectAction.FIRE_DELAY_MULTIPLIER, 1.0 / 1.25, "Fire Rate +25%"),
                "rev_strong_spring", "gr_noble_trigger", "pist_strong_spring", "flame_high_pressure",
                "hb_power_pulse", "tesla_power_pulse", "lmg_power_bolt", "vec_power_bolt",
                "scar_power_bolt", "lr_power_pulse", "br_power_pulse", "sr_power_pulse",
                "p90_power_bolt", "pul_strong_capacitor", "grf_strong_capacitor", "bfg_strong_flow",
                "lp_power_pulse");
        registerMany(passive(EffectAction.FIRE_DELAY_MULTIPLIER, 1.0 / 1.20, "Fire Rate +20%"),
                "hmg_strong_spring", "bolt_fast_bolt", "min_power_motor", "as50_fast_bolt");

        registerMany(passive(EffectAction.RANGE_MULTIPLIER, 1.30, "+30% range"),
                "gr_gold_barrel", "flame_long_hose");

        register("db_double_shot",
                passive(EffectAction.PROJECTILE_COUNT_MULTIPLIER, 2.0,
                        "Fires both barrels at once"),
                EffectSpec.builder(EffectTrigger.AMMO_CONSUMPTION,
                        EffectAction.AMMO_CONSUMPTION_MULTIPLIER, 2.0,
                        "uses 2 ammo").build());
        register("cs_shot", passive(EffectAction.PROJECTILE_COUNT_ADD, 2,
                "+2 additional pellets"));
        register("bs_extra_shot", passive(EffectAction.PROJECTILE_COUNT_ADD, 1,
                "+1 plasma pellet"));
        register("sp_extra_shard", passive(EffectAction.PROJECTILE_COUNT_ADD, 1,
                "+1 shard on split"));

        registerSilencer("hmg_silencer", 0.50, "Shot 50% quieter");
        registerSilencer("thom_silencer", 0.60, "Shot 40% quieter");
        registerSilencer("akm_silencer", 0.70, "Shot 30% quieter");
        registerSilencer("m4_silencer", 0.60, "Shot 40% quieter");
        registerSilencer("sab_coating", 0.40, "Even quieter");
        registerSilencer("mac_silencer", 0.60, "Shot 40% quieter");
        registerSilencer("aug_silencer", 0.60, "Shot 40% quieter");

        registerMany(EffectSpec.builder(EffectTrigger.PROJECTILE_SPAWN,
                        EffectAction.EXPLOSION_RADIUS_MULTIPLIER, 1.30,
                        "Explosion radius +30%").build(),
                "baz_high_explosive", "grp_high_explosive", "gl_high_explosive",
                "lor_high_explosive");
        registerMany(EffectSpec.builder(EffectTrigger.PROJECTILE_SPAWN,
                        EffectAction.PROJECTILE_SPEED_MULTIPLIER, 1.40,
                        "Projectile flight speed +40%").build(),
                "baz_speed_rocket", "grp_speed_rocket", "gl_speed_grenade", "lor_speed_rocket");
        registerMany(passive(EffectAction.ARMOR_PENETRATION_ADD, 0.40,
                        "Ignores 40% of target armor"),
                "baz_armor_piercing", "grp_armor_piercing", "lor_armor_piercing");
        register("gl_armor_piercing", passive(EffectAction.ARMOR_PENETRATION_ADD, 0.30,
                "Ignores 30% of target armor"));

        register("flame_incendiary", EffectSpec.builder(EffectTrigger.DAMAGE_OVER_TIME,
                EffectAction.BURN_DAMAGE_MULTIPLIER, 1.30, "Burn damage +30%").build());
        register("bio_corrosive", EffectSpec.builder(EffectTrigger.DAMAGE_OVER_TIME,
                EffectAction.POISON_DAMAGE_MULTIPLIER, 1.30, "Poison damage +30%").build());
        register("flame_thickener", EffectSpec.builder(EffectTrigger.BLOCK_IMPACT,
                EffectAction.GROUND_FIRE_DURATION_ADD, 40,
                "Fire stays on ground 2 seconds longer").build());

        register("bio_strong_pump", EffectSpec.builder(EffectTrigger.CHARGE,
                EffectAction.CHARGE_TIME_MULTIPLIER, 1.0 / 1.30,
                "Charge speed (RMB) +30%").build());
        register("pf_quick_charge", EffectSpec.builder(EffectTrigger.CHARGE,
                EffectAction.CHARGE_TIME_MULTIPLIER, 0.75,
                "Charge time -25%").build());
        registerMany(EffectSpec.builder(EffectTrigger.MELEE_ATTACK,
                        EffectAction.MELEE_ATTACK_SPEED_MULTIPLIER, 1.25,
                        "Attack speed in continuous mode +25%").build(),
                "chain_turbo", "drill_turbo");
        register("chain_economy_2", EffectSpec.builder(EffectTrigger.AMMO_CONSUMPTION,
                EffectAction.AMMO_CONSUMPTION_MULTIPLIER, 0.75,
                "Fuel consumption -25%").build());
        register("ad_strong_flow", EffectSpec.builder(EffectTrigger.AMMO_CONSUMPTION,
                EffectAction.AMMO_CONSUMPTION_MULTIPLIER, 0.80,
                "Charge drain speed -20%").build());

        register("pf_kinetic", EffectSpec.builder(EffectTrigger.MELEE_ATTACK,
                EffectAction.KNOCKBACK_BONUS, 0.50, "Knockback +50%").build());
        register("pf_shockwave", EffectSpec.builder(EffectTrigger.MELEE_ATTACK,
                EffectAction.SHOCKWAVE_RADIUS, 2.0,
                "Creates small shockwave on hit (2 block radius)").build());
        registerMany(EffectSpec.builder(EffectTrigger.MELEE_ATTACK,
                        EffectAction.RANDOM_DAMAGE_MULTIPLIER, 2.0,
                        "15% chance to deal double damage").probability(0.15).build(),
                "chain_double_strike", "drill_double_strike");
        register("drill_diamond", EffectSpec.builder(EffectTrigger.BLOCK_BREAK,
                EffectAction.MINING_SPEED_MULTIPLIER, 1.50,
                "Mining blocks 1.5x faster").build());
    }

    private static EffectSpec passive(EffectAction action, double value, String fragment) {
        return EffectSpec.builder(EffectTrigger.PASSIVE, action, value, fragment).build();
    }

    private static void registerSilencer(String id, double remainingRadius, String fragment) {
        register(id, EffectSpec.builder(EffectTrigger.PROJECTILE_SPAWN,
                EffectAction.DETECTION_RADIUS_MULTIPLIER, remainingRadius, fragment).build());
    }

    private static void registerMany(EffectSpec effect, String... ids) {
        for (String id : ids) register(id, effect);
    }

    private static void register(String id, EffectSpec... effects) {
        if (DECLARED.containsKey(id)) {
            throw new IllegalStateException("Duplicate Uncommon upgrade definition: " + id);
        }
        DECLARED.put(id, Collections.unmodifiableList(Arrays.asList(effects)));
    }
}

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

/** Explicit executable definitions for all 119 Legendary upgrades. */
public final class LegendaryUpgradeDefinitions {
    public static final int EXPECTED_COUNT = 119;
    private static final Map<String, List<EffectSpec>> DECLARED = new LinkedHashMap<>();
    private static final Map<String, UpgradeDefinition> DEFINITIONS = new LinkedHashMap<>();
    private static boolean declared;

    private LegendaryUpgradeDefinitions() {
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
                throw new IllegalStateException("Legendary definition references unknown upgrade: "
                        + entry.getKey());
            }
            if (buff.getRarity() != UpgradeRarity.LEGENDARY) {
                throw new IllegalStateException("Non-Legendary upgrade in Legendary registry: "
                        + entry.getKey());
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
                if (buff.getRarity() == UpgradeRarity.LEGENDARY) {
                    sourceCount++;
                    if (!DEFINITIONS.containsKey(buff.getId())) missing.add(buff.getId());
                }
            }
        }
        if (sourceCount != EXPECTED_COUNT || DEFINITIONS.size() != EXPECTED_COUNT
                || !missing.isEmpty()) {
            throw new IllegalStateException("Legendary upgrade coverage failed: source="
                    + sourceCount + ", definitions=" + DEFINITIONS.size() + ", missing=" + missing);
        }
        for (UpgradeDefinition definition : DEFINITIONS.values()) {
            if (definition.getEffects().isEmpty()) {
                throw new IllegalStateException("Legendary upgrade has no executable effects: "
                        + definition.getId());
            }
        }
    }

    private static String findWeaponId(String upgradeId) {
        for (Map.Entry<String, List<UpgradeBuff>> entry
                : WeaponUpgrades.getAllWeaponUpgrades().entrySet()) {
            for (UpgradeBuff buff : entry.getValue()) {
                if (upgradeId.equals(buff.getId())) return entry.getKey();
            }
        }
        return null;
    }

    private static void declareAll() {
        register("hmg_critical_miss",
                EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                                EffectAction.RANDOM_DAMAGE_MULTIPLIER, 3.50,
                                "25% chance to deal +250% damage")
                        .probability(0.25).build(),
                EffectSpec.builder(EffectTrigger.BEFORE_SHOT,
                                EffectAction.SELF_EXPLOSION, 2.0,
                                "5% chance to explode in the shooter's hands")
                        .probability(0.05).build());
        register("hmg_monster", monster(1.80, 0.40, 0.30));

        registerMany(healOnKill(4.0),
                "db_blood_harvest", "rev_blood_harvest", "thom_blood_harvest",
                "akm_blood_harvest", "bolt_blood_harvest", "m4_blood_harvest",
                "pist_blood_harvest", "cs_blood_harvest", "mac_blood_harvest",
                "flame_blood_harvest", "baz_blood_harvest", "grp_blood_harvest",
                "gl_blood_harvest", "aug_blood_harvest", "hb_blood_harvest",
                "bio_blood_harvest", "tesla_blood_harvest", "lmg_blood_harvest",
                "min_blood_harvest", "as50_blood_harvest", "vec_blood_harvest",
                "scar_blood_harvest", "lr_blood_harvest", "br_blood_harvest",
                "bs_blood_harvest", "sr_blood_harvest", "p90_blood_harvest",
                "pul_blood_harvest", "sp_blood_harvest", "pf_blood_harvest",
                "chain_blood_harvest", "ad_blood_harvest", "grf_blood_harvest",
                "lor_blood_harvest", "drill_blood_harvest", "bfg_blood_harvest",
                "lp_blood_harvest");
        register("gr_gold_harvest", healOnKill(6.0));

        register("db_fire_storm",
                passive(EffectAction.FIRE_DELAY_MULTIPLIER, 0.50, "Fire Rate +100%"),
                passive(EffectAction.DAMAGE_MULTIPLIER, 1.30, "Damage +30%"),
                passive(EffectAction.SPREAD_MULTIPLIER, 1.20, "Accuracy -20%"));
        register("db_monster",
                passive(EffectAction.DAMAGE_MULTIPLIER, 1.70, "Damage +70%"),
                passive(EffectAction.PROJECTILE_COUNT_ADD, 5, "+5 pellets"),
                passive(EffectAction.RELOAD_TIME_MULTIPLIER, 2.0, "Reload 2x slower"));

        registerMany(damageAndFireRate(1.50, 0.30),
                "rev_six_shot_hell", "gr_king");
        registerMany(longRange(20, 1.40), "rev_sniper", "gr_long_shot");

        registerMany(monster(1.40, 0.40, 0.20),
                "thom_machine_gun_hell", "akm_monster", "m4_monster",
                "mac_monster", "aug_monster");
        registerMany(combat(1.30, 0.20),
                "thom_combat", "akm_combat", "m4_combat", "mac_combat",
                "aug_combat", "lmg_combat", "min_combat", "vec_combat", "p90_combat");

        register("bolt_sniper_monster", combat(1.50, 0.30));
        register("bolt_long_range", longRange(40, 1.40));

        register("sab_shadow", EffectSpec.builder(EffectTrigger.KILL,
                        EffectAction.KILL_INVISIBILITY, 60,
                        "25% chance to become invisible for 3 seconds on kill")
                .probability(0.25).build());
        register("sab_pro", combat(1.40, 0.30));
        register("sab_perfect_camo", firstShot(1.50));

        register("pist_machine",
                passive(EffectAction.FIRE_DELAY_MULTIPLIER, 0.50, "Fire Rate +100%"),
                passive(EffectAction.SPREAD_MULTIPLIER, 1.20, "Accuracy -20%"));
        register("pist_sniper", longRange(15, 1.50));

        register("cs_monster",
                passive(EffectAction.DAMAGE_MULTIPLIER, 1.60, "Damage +60%"),
                passive(EffectAction.PROJECTILE_COUNT_ADD, 5, "+5 pellets"),
                passive(EffectAction.RELOAD_TIME_MULTIPLIER, 2.0, "Reload 2x slower"));
        register("cs_fire_storm",
                passive(EffectAction.FIRE_DELAY_MULTIPLIER, 1.0 / 1.50,
                        "Fire Rate +50%"),
                passive(EffectAction.DAMAGE_MULTIPLIER, 1.30, "Damage +30%"),
                passive(EffectAction.SPREAD_MULTIPLIER, 1.20, "Accuracy -20%"));

        register("flame_storm",
                passive(EffectAction.RANGE_MULTIPLIER, 1.50, "Range +50%"),
                passive(EffectAction.DAMAGE_MULTIPLIER, 1.40, "Damage +40%"));
        register("flame_endless", unlimitedAmmo("Fuel is not consumed"));

        registerMany(damageAndRadius(1.80, 1.50),
                "baz_monster", "grp_monster", "lor_monster");
        register("gl_monster", damageAndRadius(1.70, 1.40));
        register("bfg_monster", damageAndRadius(1.50, 1.50));

        register("hb_monster", damageAndFireRate(1.50, 0.40));
        register("bio_monster",
                passive(EffectAction.DAMAGE_MULTIPLIER, 1.50, "Damage +50%"),
                passive(EffectAction.CHARGE_TIME_MULTIPLIER, 1.0 / 1.40,
                        "Charge speed +40%"));
        register("tesla_monster",
                passive(EffectAction.DAMAGE_MULTIPLIER, 1.50, "Damage +50%"),
                passive(EffectAction.CHAIN_LIGHTNING_TARGETS, 3, "+3 jump targets"));

        registerMany(monster(1.40, 0.50, 0.25), "lmg_monster", "min_monster");
        register("as50_monster", combat(1.60, 0.30));
        register("as50_long_range", longRange(50, 1.50));
        registerMany(monster(1.40, 0.50, 0.20), "vec_monster", "p90_monster");
        register("scar_monster", monster(1.50, 0.30, 0.15));
        register("scar_combat", combat(1.35, 0.20));

        register("lr_monster", damageFireAndAccuracy(1.50, 0.30, 0.20));
        register("br_monster", damageFireAndAccuracy(1.40, 0.40, 0.20));
        register("bs_monster",
                passive(EffectAction.DAMAGE_MULTIPLIER, 1.50, "Damage +50%"),
                passive(EffectAction.PROJECTILE_COUNT_ADD, 2, "+2 pellets"),
                passive(EffectAction.SPREAD_MULTIPLIER, 1.20, "Accuracy -20%"));
        register("sr_monster",
                passive(EffectAction.DAMAGE_MULTIPLIER, 1.50, "Damage +50%"),
                passive(EffectAction.RANGE_MULTIPLIER, 1.50, "Range +50%"));
        register("pul_monster", damageFireAndAccuracy(1.50, 0.30, 0.20));
        register("sp_monster",
                passive(EffectAction.DAMAGE_MULTIPLIER, 1.40, "Damage +40%"),
                passive(EffectAction.PROJECTILE_COUNT_ADD, 2, "+2 shards on split"));

        register("pf_monster",
                passive(EffectAction.DAMAGE_MULTIPLIER, 1.60, "Damage +60%"),
                passive(EffectAction.KNOCKBACK_MULTIPLIER, 1.50, "Knockback +50%"));
        register("pf_endless_charge", passive(EffectAction.INSTANT_CHARGE, 1,
                "Charged attack requires no charge time"));

        registerMany(new EffectSpec[]{
                        passive(EffectAction.DAMAGE_MULTIPLIER, 1.50, "Damage +50%"),
                        passive(EffectAction.MELEE_ATTACK_SPEED_MULTIPLIER, 1.40,
                                "Attack Speed +40%"),
                        passive(EffectAction.AMMO_CONSUMPTION_MULTIPLIER, 1.30,
                                "Fuel consumption +30%")},
                "chain_monster", "drill_monster");
        register("chain_endless", unlimitedAmmo("Fuel is not consumed"));
        register("drill_endless", unlimitedAmmo("Fuel is not consumed"));

        register("ad_monster",
                passive(EffectAction.DAMAGE_MULTIPLIER, 1.50, "Damage +50%"),
                passive(EffectAction.BEAM_WIDTH_MULTIPLIER, 2.0, "Beam Width +100%"));
        register("grf_monster", combat(1.60, 0.30));
        register("lp_monster", damageFireAndAccuracy(1.40, 0.40, 0.20));

        registerMany(ammoSave(0.15),
                "baz_endless_salvo", "grp_endless_salvo", "gl_endless_salvo",
                "ad_endless", "lor_endless_salvo", "bfg_endless");
        registerMany(ammoSave(0.20),
                "hb_endless_fire", "bio_endless_poison", "tesla_endless", "lr_endless",
                "br_endless", "bs_endless", "sr_endless", "pul_endless", "sp_endless",
                "lp_endless");
        register("grf_endless", ammoSave(0.25));
    }

    private static EffectSpec passive(EffectAction action, double value, String fragment) {
        return EffectSpec.builder(EffectTrigger.PASSIVE, action, value, fragment).build();
    }

    private static EffectSpec healOnKill(double health) {
        return EffectSpec.builder(EffectTrigger.KILL, EffectAction.HEAL_ON_KILL, health,
                "Each kill restores health").build();
    }

    private static EffectSpec ammoSave(double chance) {
        return EffectSpec.builder(EffectTrigger.AMMO_CONSUMPTION,
                        EffectAction.AMMO_SAVE_CHANCE, 1,
                        "Chance not to consume ammunition")
                .probability(chance).build();
    }

    private static EffectSpec unlimitedAmmo(String fragment) {
        return passive(EffectAction.UNLIMITED_AMMO, 1, fragment);
    }

    private static EffectSpec firstShot(double multiplier) {
        return EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.CONDITIONAL_DAMAGE_MULTIPLIER, multiplier,
                        "First shot after reload is critical")
                .condition(EffectCondition.FIRST_SHOT_AFTER_RELOAD, 0).build();
    }

    private static EffectSpec longRange(double distance, double multiplier) {
        return EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.CONDITIONAL_DAMAGE_MULTIPLIER, multiplier,
                        "Damage bonus beyond the stated range")
                .condition(EffectCondition.TARGET_BEYOND_DISTANCE, distance).build();
    }

    private static EffectSpec[] damageAndFireRate(double damage, double fireRateBonus) {
        return new EffectSpec[]{
                passive(EffectAction.DAMAGE_MULTIPLIER, damage, "Damage bonus"),
                passive(EffectAction.FIRE_DELAY_MULTIPLIER, 1.0 / (1.0 + fireRateBonus),
                        "Fire rate bonus")};
    }

    private static EffectSpec[] damageAndRadius(double damage, double radius) {
        return new EffectSpec[]{
                passive(EffectAction.DAMAGE_MULTIPLIER, damage, "Damage bonus"),
                passive(EffectAction.EXPLOSION_RADIUS_MULTIPLIER, radius,
                        "Explosion radius bonus")};
    }

    private static EffectSpec[] combat(double damage, double accuracyBonus) {
        return new EffectSpec[]{
                passive(EffectAction.DAMAGE_MULTIPLIER, damage, "Damage bonus"),
                passive(EffectAction.SPREAD_MULTIPLIER, 1.0 / (1.0 + accuracyBonus),
                        "Accuracy bonus")};
    }

    private static EffectSpec[] monster(double damage, double fireRateBonus,
                                        double accuracyPenalty) {
        return new EffectSpec[]{
                passive(EffectAction.DAMAGE_MULTIPLIER, damage, "Damage bonus"),
                passive(EffectAction.FIRE_DELAY_MULTIPLIER, 1.0 / (1.0 + fireRateBonus),
                        "Fire rate bonus"),
                passive(EffectAction.SPREAD_MULTIPLIER, 1.0 + accuracyPenalty,
                        "Accuracy penalty")};
    }

    private static EffectSpec[] damageFireAndAccuracy(double damage, double fireRateBonus,
                                                      double accuracyBonus) {
        return new EffectSpec[]{
                passive(EffectAction.DAMAGE_MULTIPLIER, damage, "Damage bonus"),
                passive(EffectAction.FIRE_DELAY_MULTIPLIER, 1.0 / (1.0 + fireRateBonus),
                        "Fire rate bonus"),
                passive(EffectAction.SPREAD_MULTIPLIER, 1.0 / (1.0 + accuracyBonus),
                        "Accuracy bonus")};
    }

    private static void registerMany(EffectSpec effect, String... ids) {
        for (String id : ids) register(id, effect);
    }

    private static void registerMany(EffectSpec[] effects, String... ids) {
        for (String id : ids) register(id, effects);
    }

    private static void register(String id, EffectSpec... effects) {
        if (DECLARED.containsKey(id)) {
            throw new IllegalStateException("Duplicate Legendary upgrade definition: " + id);
        }
        DECLARED.put(id, Collections.unmodifiableList(Arrays.asList(effects)));
    }
}

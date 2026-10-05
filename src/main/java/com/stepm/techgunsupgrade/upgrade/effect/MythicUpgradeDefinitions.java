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

/** Explicit executable definitions for all 80 Mythic upgrades. */
public final class MythicUpgradeDefinitions {
    public static final int EXPECTED_COUNT = 80;
    private static final Map<String, List<EffectSpec>> DECLARED = new LinkedHashMap<>();
    private static final Map<String, UpgradeDefinition> DEFINITIONS = new LinkedHashMap<>();
    private static boolean declared;

    private MythicUpgradeDefinitions() {
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
                throw new IllegalStateException("Mythic definition references unknown upgrade: "
                        + entry.getKey());
            }
            if (buff.getRarity() != UpgradeRarity.MYTHIC) {
                throw new IllegalStateException("Non-Mythic upgrade in Mythic registry: "
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
                if (buff.getRarity() == UpgradeRarity.MYTHIC) {
                    sourceCount++;
                    if (!DEFINITIONS.containsKey(buff.getId())) missing.add(buff.getId());
                }
            }
        }
        if (sourceCount != EXPECTED_COUNT || DEFINITIONS.size() != EXPECTED_COUNT
                || !missing.isEmpty()) {
            throw new IllegalStateException("Mythic upgrade coverage failed: source="
                    + sourceCount + ", definitions=" + DEFINITIONS.size() + ", missing=" + missing);
        }
        for (UpgradeDefinition definition : DEFINITIONS.values()) {
            if (definition.getEffects().isEmpty()) {
                throw new IllegalStateException("Mythic upgrade has no executable effects: "
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
        register("hmg_chaotic_charge",
                randomDamage(0.25, 4.0, "25% chance for +300% damage"),
                EffectSpec.builder(EffectTrigger.BEFORE_SHOT, EffectAction.RANDOM_SELF_DAMAGE,
                                0.50, "10% chance to take 50% maximum health damage")
                        .probability(0.10).build());
        register("hmg_railgun",
                passive(EffectAction.DAMAGE_MULTIPLIER, 2.0, "Damage +100%"),
                passive(EffectAction.RAILGUN_PIERCE, 96.0,
                        "Projectile pierces enemies and walls"));

        register("db_hell_duet", passive(EffectAction.IMPACT_EXPLOSION_RADIUS, 5.0,
                "Both barrels create a five-block impact explosion"));
        register("db_doom_shot",
                passive(EffectAction.PROJECTILE_DAMAGE_MULTIPLIER, 1.50,
                        "Each pellet deals +50% damage"),
                passive(EffectAction.JAM_CHANCE, 0.15, "Jam chance +15%"));

        register("rev_fatal_shot", randomDamage(0.20, 5.0,
                "20% chance for +400% damage"));
        register("rev_endless_cylinder", ammoSave(0.15));
        register("gr_golden_rain", randomDamage(0.20, 5.0,
                "20% chance for +400% damage"));
        register("gr_endless_cylinder", ammoSave(0.15));

        register("thom_virtuoso", ammoSave(0.20));
        register("thom_monster", damageAndFireRate(1.60, 0.50));
        register("akm_legend", ammoSave(0.20));
        register("akm_monster_ak", damageAndFireRate(1.60, 0.50));

        register("bolt_hunter", stack(EffectAction.STACK_DAMAGE_ON_KILL, 1.0, 200.0,
                "Each kill permanently adds 1% damage, up to 200%"));
        register("bolt_double_shot", randomExtraShot(0.15));
        register("m4_tactical_genius", stack(EffectAction.STACK_FIRE_RATE_ON_KILL,
                2.0, 100.0, "Each kill permanently adds 2% fire rate, up to 100%"));
        register("m4_deadly_queue", fullMagazine(30, 4.0,
                "The last round deals +300% after a full magazine hits one enemy"));

        register("sab_ghost", stack(EffectAction.STACK_DETECTION_ON_KILL, 2.0, 60.0,
                "Each kill permanently reduces detection radius by 2%, up to 60%"));
        register("sab_deadly_whisper", EffectSpec.builder(EffectTrigger.KILL,
                        EffectAction.LONG_RANGE_KILL_BLINDNESS, 100,
                        "A kill beyond 30 blocks blinds nearby mobs for five seconds")
                .condition(EffectCondition.TARGET_BEYOND_DISTANCE, 30).build());
        register("pist_duelist", stack(EffectAction.STACK_ACCURACY_ON_KILL, 1.0, 50.0,
                "Each kill permanently adds 1% accuracy, up to 50%"));
        register("pist_wild_shot", EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.RANDOM_DAMAGE_STUN, 40,
                        "10% chance for +400% damage and a two-second stun")
                .probability(0.10).condition(EffectCondition.ALWAYS, 5.0).build());

        register("cs_street_fighter", stack(EffectAction.STACK_CLOSE_DAMAGE_ON_KILL,
                2.0, 100.0, "Each kill permanently adds 2% close-range damage, up to 100%"));
        register("cs_deadly_shot", fullMagazine(8, 6.0,
                "The eighth hit deals +500% after all shots hit one target"));
        register("mac_hail_lead", stack(EffectAction.STACK_FIRE_RATE_ON_KILL,
                2.0, 100.0, "Each kill permanently adds 2% fire rate, up to 100%"));
        register("mac_deadly_fan", fullMagazine(32, 6.0,
                "The last round deals +500% after all 32 hit one target"));

        register("flame_burning_earth", stack(EffectAction.STACK_BURN_DAMAGE_ON_KILL,
                2.0, 100.0, "Each kill permanently adds 2% fire damage, up to 100%"));
        register("flame_deadly_heat", EffectSpec.builder(EffectTrigger.DAMAGE_OVER_TIME,
                        EffectAction.BURN_DURATION_DAMAGE, 3.0,
                        "After five seconds of burning, fire damage is increased by 200%")
                .condition(EffectCondition.TARGET_BURNING_FOR_TICKS, 100).build());
        register("baz_apocalypse", stack(EffectAction.STACK_DAMAGE_ON_KILL, 2.0, 100.0,
                "Each kill permanently adds 2% explosive damage, up to 100%"));
        register("baz_deadly_salvo", firstKillNext(4.0));

        register("grp_deadly_harvest",
                stack(EffectAction.STACK_DAMAGE_ON_KILL, 1.0, 100.0,
                        "Each kill permanently adds 1% damage, up to 100%"),
                stack(EffectAction.STACK_EXPLOSION_RADIUS_ON_KILL, 0.5, 50.0,
                        "Each kill permanently adds 0.5% explosion radius, up to 50%"));
        register("grp_salvo_death", fullMagazine(4, 6.0,
                "The fourth rocket deals +500% after all four hit one target"));
        register("gl_demolitionist", stack(EffectAction.STACK_DAMAGE_ON_KILL, 2.0, 100.0,
                "Each kill permanently adds 2% explosive damage, up to 100%"));
        register("gl_deadly_ricochet", EffectSpec.builder(EffectTrigger.BLOCK_IMPACT,
                        EffectAction.RICOCHET_NEAREST, 2.0,
                        "10% chance to ricochet and explode near the nearest enemy")
                .probability(0.10).condition(EffectCondition.ALWAYS, 10.0).build());

        register("aug_austrian_sniper", stack(EffectAction.STACK_ACCURACY_ON_KILL,
                1.0, 50.0, "Each kill permanently adds 1% accuracy, up to 50%"));
        register("aug_deadly_queue", fullMagazine(30, 3.50,
                "The last round deals +250% after all 30 hit one target"));
        register("hb_lord_of_hell", stack(EffectAction.STACK_BURN_DAMAGE_ON_KILL,
                2.0, 100.0, "Each kill permanently adds 2% fire damage, up to 100%"));
        register("hb_deadly_shot", EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.CONDITIONAL_DAMAGE_MULTIPLIER, 4.0,
                        "The last charge in the magazine deals +300% damage")
                .condition(EffectCondition.LAST_ROUND, 0).build());

        register("bio_lord_disease", stack(EffectAction.STACK_POISON_DAMAGE_ON_KILL,
                1.0, 100.0, "Each kill permanently adds 1% poison damage, up to 100%"));
        register("bio_deadly_infection", EffectSpec.builder(EffectTrigger.KILL,
                        EffectAction.KILL_CONTAGION, 100,
                        "20% chance to infect nearby enemies for five seconds on kill")
                .probability(0.20).build());
        register("tesla_lord_lightning", stack(EffectAction.STACK_DAMAGE_ON_KILL,
                1.0, 100.0, "Each kill permanently adds 1% lightning damage, up to 100%"));
        register("tesla_deadly_chain", EffectSpec.builder(EffectTrigger.KILL,
                EffectAction.KILL_CHAIN_LIGHTNING, 1,
                "An electrical kill automatically jumps to the next target").build());

        register("lmg_machine_gunner", stack(EffectAction.STACK_FIRE_RATE_ON_KILL,
                2.0, 100.0, "Each kill permanently adds 2% fire rate, up to 100%"));
        register("lmg_deadly_queue", fullMagazine(100, 6.0,
                "The last round deals +500% after all 100 hit one target"));
        register("min_hurricane_death", stack(EffectAction.STACK_FIRE_RATE_ON_KILL,
                2.0, 100.0, "Each kill permanently adds 2% fire rate, up to 100%"));
        register("min_deadly_storm", fullMagazine(200, 7.0,
                "The last round deals +600% after all 200 hit one target"));

        register("as50_sniper", stack(EffectAction.STACK_DAMAGE_ON_KILL, 1.0, 100.0,
                "Each kill permanently adds 1% damage, up to 100%"));
        register("as50_double_shot", randomExtraShot(0.20));
        register("vec_speed", stack(EffectAction.STACK_FIRE_RATE_ON_KILL, 2.0, 100.0,
                "Each kill permanently adds 2% fire rate, up to 100%"));
        register("vec_deadly_queue", EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.HIT_STREAK_DAMAGE, 4.0,
                        "After ten uninterrupted hits, the eleventh deals +300% damage")
                .condition(EffectCondition.HIT_STREAK, 10).build());

        register("scar_sniper", stack(EffectAction.STACK_DAMAGE_ON_KILL, 1.5, 75.0,
                "Each kill permanently adds 1.5% damage, up to 75%"));
        register("scar_deadly_shot", firstKillNext(4.0));
        register("lr_lord_light", stack(EffectAction.STACK_DAMAGE_ON_KILL, 1.0, 100.0,
                "Each kill permanently adds 1% laser damage, up to 100%"));
        register("lr_deadly_beam", EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.SCOPED_DAMAGE_MULTIPLIER, 3.0,
                        "After two seconds scoped, the next shot deals +200% damage")
                .condition(EffectCondition.SCOPED_FOR_TICKS, 40).build());

        register("br_lord_plasma", stack(EffectAction.STACK_DAMAGE_ON_KILL, 1.0, 100.0,
                "Each kill permanently adds 1% plasma damage, up to 100%"));
        register("br_speed_charge",
                EffectSpec.builder(EffectTrigger.CHARGE, EffectAction.PERIODIC_FREE_CHARGE, 1,
                                "Every third shot requires no charge")
                        .condition(EffectCondition.EVERY_NTH_SHOT, 3).build(),
                EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                                EffectAction.PERIODIC_SHOT_DAMAGE_MULTIPLIER, 1.50,
                                "Every third shot deals +50% damage")
                        .condition(EffectCondition.EVERY_NTH_SHOT, 3).build());
        register("bs_destroyer", stack(EffectAction.STACK_DAMAGE_ON_KILL, 1.5, 75.0,
                "Each kill permanently adds 1.5% plasma damage, up to 75%"));
        register("bs_deadly_fan", EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                EffectAction.SAME_SHOT_PELLET_DAMAGE, 1.30,
                "Each subsequent pellet hitting the same target deals +30% damage").build());

        register("sr_lord_sound", stack(EffectAction.STACK_DAMAGE_ON_KILL, 2.0, 100.0,
                "Each kill permanently adds 2% sonic damage, up to 100%"));
        register("sr_deadly_resonance_m", EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.STUNNED_TARGET_DAMAGE, 4.0,
                        "A stunned target takes +300% damage")
                .condition(EffectCondition.TARGET_STUNNED, 0).build());
        register("p90_speed", stack(EffectAction.STACK_FIRE_RATE_ON_KILL, 1.5, 75.0,
                "Each kill permanently adds 1.5% fire rate, up to 75%"));
        register("p90_deadly_mag", fullMagazine(40, 6.0,
                "The last round deals +500% after all 40 hit one target"));

        register("pul_lord_pulse", stack(EffectAction.STACK_DAMAGE_ON_KILL, 1.5, 75.0,
                "Each kill permanently adds 1.5% pulse damage, up to 75%"));
        register("pul_deadly_charge", EffectSpec.builder(EffectTrigger.CHARGE,
                        EffectAction.CHARGE_THRESHOLD_EXPLOSIVE_DAMAGE, 3.0,
                        "A charge beyond 1.5 seconds deals +200% damage and explodes")
                .condition(EffectCondition.CHARGE_BEYOND_TICKS, 30).build());
        register("sp_entropy", stack(EffectAction.STACK_DAMAGE_ON_KILL, 1.5, 75.0,
                "Each kill permanently adds 1.5% shard damage, up to 75%"));
        register("sp_cascade_death", EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                EffectAction.CASCADE_ON_HIT, 12,
                "A primary hit splits into additional shards, up to twelve").build());

        register("pf_crusher", stack(EffectAction.STACK_DAMAGE_ON_KILL, 2.0, 100.0,
                "Each kill permanently adds 2% damage, up to 100%"));
        register("pf_hell_strike", EffectSpec.builder(EffectTrigger.MELEE_ATTACK,
                        EffectAction.MELEE_AREA_EXPLOSION, 3.0,
                        "15% chance for +200% area damage and five seconds of fire")
                .probability(0.15).condition(EffectCondition.ALWAYS, 5.0).build());
        register("chain_butcher", stack(EffectAction.STACK_DAMAGE_ON_KILL, 2.0, 100.0,
                "Each kill permanently adds 2% damage, up to 100%"));
        register("chain_blood_bath", EffectSpec.builder(EffectTrigger.KILL,
                        EffectAction.KILL_DAMAGE_CLOUD, 4.0,
                        "A continuous-mode kill has 20% chance to create a damaging blood cloud")
                .probability(0.20).condition(EffectCondition.CONTINUOUS_KILL, 0).build());

        register("ad_lord_radiation", stack(EffectAction.STACK_DAMAGE_ON_KILL, 1.5, 75.0,
                "Each kill permanently adds 1.5% beam damage, up to 75%"));
        register("ad_deadly_heat", EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.CONTINUOUS_TARGET_EXPLOSION, 4.0,
                        "After five seconds on one target, the beam explodes for +300% damage")
                .condition(EffectCondition.TARGET_BURNING_FOR_TICKS, 100).build());
        register("grf_lord_pulse", stack(EffectAction.STACK_DAMAGE_ON_KILL, 2.0, 100.0,
                "Each kill permanently adds 2% damage, up to 100%"));
        register("grf_deadly_shot_m", firstKillNext(4.0));

        register("lor_hunter", stack(EffectAction.STACK_DAMAGE_ON_KILL, 2.0, 100.0,
                "Each kill permanently adds 2% rocket damage, up to 100%"));
        register("lor_deadly_lock", EffectSpec.builder(EffectTrigger.KILL,
                        EffectAction.KILL_INSTANT_LOCK, 1,
                        "A locked kill makes the next lock-on instant")
                .condition(EffectCondition.LOCKED_KILL, 0).build());
        register("drill_miner", stack(EffectAction.STACK_DAMAGE_ON_KILL, 2.0, 100.0,
                "Each kill permanently adds 2% damage, up to 100%"));
        register("drill_deadly_drilling", EffectSpec.builder(EffectTrigger.KILL,
                        EffectAction.KILL_TREMOR, 5.0,
                        "A continuous-mode kill has 20% chance for a damaging tremor")
                .probability(0.20).condition(EffectCondition.CONTINUOUS_KILL, 0).build());

        register("bfg_destroyer", stack(EffectAction.STACK_DAMAGE_ON_KILL, 1.5, 75.0,
                "Each kill permanently adds 1.5% nuclear damage, up to 75%"));
        register("bfg_deadly_charge", EffectSpec.builder(EffectTrigger.CHARGE,
                        EffectAction.CHARGED_NUCLEAR_CHANCE, 6.0,
                        "A fully charged projectile has 20% chance for +500% nuclear damage")
                .probability(0.20).condition(EffectCondition.CHARGED_ATTACK, 15.0).build());
        register("lp_lord_light", stack(EffectAction.STACK_ACCURACY_ON_KILL, 2.0, 50.0,
                "Each kill permanently adds 2% accuracy, up to 50%"));
        register("lp_deadly_beam", EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.HEADSHOT_SHOCKWAVE, 4.0,
                        "A headshot deals +300% damage and creates a shockwave")
                .condition(EffectCondition.HEADSHOT, 6.0).build());
    }

    private static EffectSpec passive(EffectAction action, double value, String fragment) {
        return EffectSpec.builder(EffectTrigger.PASSIVE, action, value, fragment).build();
    }

    private static EffectSpec stack(EffectAction action, double step, double maximum,
                                    String fragment) {
        return EffectSpec.builder(EffectTrigger.KILL, action, step, fragment)
                .condition(EffectCondition.ALWAYS, maximum).build();
    }

    private static EffectSpec randomDamage(double chance, double multiplier, String fragment) {
        return EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.RANDOM_DAMAGE_MULTIPLIER, multiplier, fragment)
                .probability(chance).build();
    }

    private static EffectSpec ammoSave(double chance) {
        return EffectSpec.builder(EffectTrigger.AMMO_CONSUMPTION,
                        EffectAction.AMMO_SAVE_CHANCE, 1,
                        "Chance not to consume ammunition")
                .probability(chance).build();
    }

    private static EffectSpec randomExtraShot(double chance) {
        return EffectSpec.builder(EffectTrigger.PROJECTILE_SPAWN,
                        EffectAction.RANDOM_EXTRA_PROJECTILES, 1,
                        "Chance to fire a second projectile without ammunition cost")
                .probability(chance).condition(EffectCondition.ALWAYS, 0).build();
    }

    private static EffectSpec fullMagazine(int rounds, double multiplier, String fragment) {
        return EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.FULL_MAG_TARGET_DAMAGE, multiplier, fragment)
                .condition(EffectCondition.FULL_MAGAZINE_SAME_TARGET, rounds).build();
    }

    private static EffectSpec firstKillNext(double multiplier) {
        return EffectSpec.builder(EffectTrigger.KILL, EffectAction.FIRST_SHOT_KILL_NEXT_DAMAGE,
                        multiplier, "A first-shot kill grants a damage bonus to the next shot")
                .condition(EffectCondition.FIRST_SHOT_KILL, 0).build();
    }

    private static EffectSpec[] damageAndFireRate(double damage, double fireRateBonus) {
        return new EffectSpec[]{
                passive(EffectAction.DAMAGE_MULTIPLIER, damage, "Damage bonus"),
                passive(EffectAction.FIRE_DELAY_MULTIPLIER, 1.0 / (1.0 + fireRateBonus),
                        "Fire rate bonus")};
    }

    private static void register(String id, EffectSpec... effects) {
        if (DECLARED.put(id, Arrays.asList(effects)) != null) {
            throw new IllegalStateException("Duplicate Mythic upgrade definition: " + id);
        }
    }
}

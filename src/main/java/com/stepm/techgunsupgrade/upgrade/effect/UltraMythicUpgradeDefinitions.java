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

/** Explicit executable definitions for all 40 Ultra-Mythic upgrades. */
public final class UltraMythicUpgradeDefinitions {
    public static final int EXPECTED_COUNT = 40;
    private static final Map<String, List<EffectSpec>> DECLARED = new LinkedHashMap<>();
    private static final Map<String, UpgradeDefinition> DEFINITIONS = new LinkedHashMap<>();
    private static boolean declared;

    private UltraMythicUpgradeDefinitions() {
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
                throw new IllegalStateException("Ultra-Mythic definition references unknown upgrade: "
                        + entry.getKey());
            }
            if (buff.getRarity() != UpgradeRarity.ULTRA_MYTHIC) {
                throw new IllegalStateException("Non-Ultra-Mythic upgrade in registry: "
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
                if (buff.getRarity() == UpgradeRarity.ULTRA_MYTHIC) {
                    sourceCount++;
                    if (!DEFINITIONS.containsKey(buff.getId())) missing.add(buff.getId());
                }
            }
        }
        if (sourceCount != EXPECTED_COUNT || DEFINITIONS.size() != EXPECTED_COUNT
                || !missing.isEmpty()) {
            throw new IllegalStateException("Ultra-Mythic coverage failed: source=" + sourceCount
                    + ", definitions=" + DEFINITIONS.size() + ", missing=" + missing);
        }
        for (UpgradeDefinition definition : DEFINITIONS.values()) {
            if (definition.getEffects().isEmpty()) {
                throw new IllegalStateException("Ultra-Mythic upgrade has no effects: "
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
        register("hmg_apocalypse", base(3.0,
                passive(EffectAction.EXPLOSION_POWER, 2.5, "Every shot explodes")));
        register("db_hell_duet_ultra", base(3.0,
                passive(EffectAction.PROJECTILE_COUNT_OVERRIDE, 20, "Fires exactly 20 pellets"),
                passive(EffectAction.INCENDIARY_DURATION, 100, "Every pellet ignites")));
        register("rev_judge", base(3.0,
                passive(EffectAction.EVERY_SHOT_LIGHTNING, 1, "Every shot summons lightning")));
        register("gr_gold_king", base(3.5,
                EffectSpec.builder(EffectTrigger.KILL, EffectAction.GOLD_DROP_CHANCE, 1,
                        "Every kill drops gold").probability(1.0).build()));

        register("thom_gangster_ultra", automatic(2.5, 2.0, 0.0));
        register("akm_immortal", automatic(3.0, 2.0, 1.0));
        register("bolt_deadly_scope", base(3.0,
                passive(EffectAction.RAILGUN_PIERCE, 128,
                        "Projectile pierces every enemy and wall")));
        register("m4_legendary", automatic(2.5, 1.5, 1.0));

        register("sab_invisible", base(3.0,
                passive(EffectAction.SILENCE_SHOT, 1, "Weapon is completely silent"),
                passive(EffectAction.DETECTION_RADIUS_MULTIPLIER, 0.05,
                        "Mobs detect shots only at point-blank range"),
                EffectSpec.builder(EffectTrigger.KILL, EffectAction.KILL_INVISIBILITY, 100,
                        "Five seconds of invisibility after every kill").probability(1.0).build()));
        register("pist_legendary", automatic(3.0, 2.0, 0.0,
                periodicDamage(3, 6.0, "Every third shot deals +500% damage")));
        register("cs_war_machine", base(3.0,
                passive(EffectAction.PROJECTILE_COUNT_ADD, 10, "+10 pellets"),
                passive(EffectAction.IMPACT_SHOCKWAVE, 5,
                        "Every shot creates a five-block shockwave")));
        register("mac_legendary", automatic(2.5, 2.0, 0.0,
                periodicRadial(10, 10, "Every tenth shot creates a radial hail"),
                passive(EffectAction.RADIAL_BURST_RANGE, 10, "Hail range")));

        register("flame_hellfire", base(3.0,
                passive(EffectAction.RANGE_MULTIPLIER, 2.0, "Range +100%"),
                passive(EffectAction.INCENDIARY_DURATION, 200, "Ignites for ten seconds"),
                EffectSpec.builder(EffectTrigger.PROJECTILE_SPAWN, EffectAction.FIRE_TRAIL, 200,
                        "Leaves a persistent burning trail").condition(EffectCondition.ALWAYS, 1).build()));
        register("baz_armageddon", explosive(3.0, 3.0,
                EffectSpec.builder(EffectTrigger.BLOCK_IMPACT, EffectAction.NUCLEAR_IMPACT, 30,
                        "10% chance for a thirty-block nuclear impact").probability(0.10).build()));
        register("grp_death_scythe", explosive(3.5, 3.0,
                EffectSpec.builder(EffectTrigger.KILL, EffectAction.KILL_AREA_EXECUTE, 50,
                        "15% chance to execute all enemies within fifty blocks")
                        .probability(0.15).build()));
        register("gl_artillery", explosive(3.0, 2.5,
                passive(EffectAction.EXTRA_IMPACT_EXPLOSIONS, 1,
                        "Grenades explode a second time"),
                EffectSpec.builder(EffectTrigger.BLOCK_IMPACT, EffectAction.ARTILLERY_STRIKE, 3,
                        "10% chance to call three extra explosions")
                        .probability(0.10).condition(EffectCondition.ALWAYS, 8).build()));

        register("aug_legendary", automatic(2.5, 1.5, 1.0,
                periodicDamage(3, 4.0, "Every third shot deals +300% damage")));
        register("hb_armageddon", base(3.5,
                passive(EffectAction.RANGE_MULTIPLIER, 2.0, "Range +100%"),
                passive(EffectAction.PIERCING_COUNT, 64, "Beam pierces all enemies"),
                EffectSpec.builder(EffectTrigger.BLOCK_IMPACT, EffectAction.FIRE_RAIN, 10,
                        "15% chance to summon ten fire projectiles that deal damage and ignite the ground")
                        .probability(0.15).condition(EffectCondition.ALWAYS, 15).build()));
        register("bio_bioapocalypse", base(3.0,
                passive(EffectAction.CHARGE_TIME_MULTIPLIER, 1.0 / 3.0,
                        "Charge speed +200%"),
                passive(EffectAction.CHARGED_EXTRA_PROJECTILES, 9,
                        "A charged attack creates ten projectiles"),
                EffectSpec.builder(EffectTrigger.BLOCK_IMPACT, EffectAction.ACID_CLOUD, 5,
                        "Each projectile creates a five-block acid cloud")
                        .condition(EffectCondition.ALWAYS, 100).build()));
        register("tesla_thunder", base(3.0,
                passive(EffectAction.CHAIN_ALL_RADIUS, 20,
                        "Electricity jumps to every enemy within twenty blocks"),
                EffectSpec.builder(EffectTrigger.KILL, EffectAction.KILL_LIGHTNING, 1,
                        "Every killed enemy summons lightning").build()));

        register("lmg_firestorm", automatic(2.5, 2.0, 0.0,
                periodicRadial(10, 10, "Every tenth shot creates ten radial bullets"),
                passive(EffectAction.RADIAL_BURST_RANGE, 5, "Firestorm range")));
        register("min_hurricane_death_ultra", automatic(3.0, 2.0, 0.0,
                passive(EffectAction.SPIN_UP_TIME_MULTIPLIER, 0.01, "Instant spin-up"),
                periodicRadial(10, 20, "Every tenth shot creates a radial bullet wave"),
                passive(EffectAction.RADIAL_BURST_RANGE, 10, "Wave range")));
        register("as50_annihilator", base(3.5,
                passive(EffectAction.RAILGUN_PIERCE, 160,
                        "Bullet pierces all enemies and walls"),
                EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION, EffectAction.SNIPER_STRIKE,
                        6.0, "15% chance for a +500% lightning sniper strike")
                        .probability(0.15).build()));
        register("vec_death_storm", automatic(2.5, 2.0, 0.0,
                periodicDamage(3, 4.0, "Every third shot deals +300% damage"),
                EffectSpec.builder(EffectTrigger.PROJECTILE_SPAWN,
                        EffectAction.TARGETED_PROJECTILE_BURST, 5,
                        "10% chance to fire five bullets at nearby enemies")
                        .probability(0.10).build(),
                passive(EffectAction.TARGETED_BURST_RANGE, 24, "Target search range")));

        register("scar_legendary", automatic(3.0, 1.5, 1.0,
                periodicDamage(3, 5.0, "Every third shot deals +400% damage"),
                EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.RANDOM_INSTANT_KILL, 1,
                        "5% chance to instantly kill the target").probability(0.05).build()));
        register("lr_doomsday", automatic(3.5, 2.0, 0.0,
                passive(EffectAction.RAILGUN_PIERCE, 160,
                        "Beam pierces enemies and walls"),
                EffectSpec.builder(EffectTrigger.BLOCK_IMPACT, EffectAction.LASER_RAIN, 10,
                        "10% chance to summon ten beams from the sky")
                        .probability(0.10).condition(EffectCondition.ALWAYS, 15).build()));
        register("br_star_destroyer", automatic(3.0, 2.0, 0.0,
                passive(EffectAction.PROJECTILE_SPEED_MULTIPLIER, 2.0,
                        "Projectiles fly twice as fast"),
                passive(EffectAction.PIERCING_COUNT, 64, "Projectiles pierce enemies"),
                EffectSpec.builder(EffectTrigger.BLOCK_IMPACT,
                        EffectAction.PERIODIC_AREA_EXPLOSION, 4.0,
                        "Every fifth shot creates an eight-block +300% plasma explosion")
                        .condition(EffectCondition.EVERY_NTH_SHOT, 5).build()));
        register("bs_plasma_apocalypse", base(3.0,
                passive(EffectAction.PROJECTILE_COUNT_ADD, 5, "+5 plasma projectiles"),
                passive(EffectAction.EXPLOSION_POWER, 2.5, "Every projectile explodes"),
                EffectSpec.builder(EffectTrigger.PROJECTILE_SPAWN,
                        EffectAction.RADIAL_PROJECTILE_BURST, 20,
                        "15% chance to fire twenty projectiles in all directions")
                        .probability(0.15).build(),
                passive(EffectAction.RADIAL_BURST_RANGE, 10, "Plasma wave range")));

        register("sr_symphony", explosive(3.5, 3.0,
                passive(EffectAction.RAILGUN_PIERCE, 160,
                        "Sound wave pierces all enemies and walls"),
                EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.RANDOM_SONIC_WAVE, 15,
                        "10% chance for a five-second +200% sonic stun wave")
                        .probability(0.10).condition(EffectCondition.ALWAYS, 3.0).build()));
        register("p90_machine_gun", automatic(2.5, 2.0, 0.0,
                periodicTargeted(5, 5, "Every fifth shot fires five targeted bullets"),
                passive(EffectAction.TARGETED_BURST_RANGE, 24, "Target search range"),
                EffectSpec.builder(EffectTrigger.PROJECTILE_SPAWN,
                        EffectAction.RADIAL_PROJECTILE_BURST, 10,
                        "10% chance to create a ten-bullet hail")
                        .probability(0.10).build(),
                passive(EffectAction.RADIAL_BURST_RANGE, 12, "Hail range")));
        register("pul_pulse_blast", automatic(3.0, 1.5, 0.0,
                passive(EffectAction.PIERCING_COUNT, 64, "Pulses pierce enemies"),
                EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.RANDOM_PULSE_WAVE, 10,
                        "15% chance for a +300% pulse wave with heavy knockback")
                        .probability(0.15).condition(EffectCondition.ALWAYS, 4.0).build()));
        register("sp_reality_split", base(3.0,
                EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.INFINITE_SPLIT, 2,
                        "Every shard creates two more shards while it keeps hitting")
                        .condition(EffectCondition.ALWAYS, 6).build(),
                EffectSpec.builder(EffectTrigger.BLOCK_IMPACT,
                        EffectAction.RANDOM_TIMED_ZONE, 8,
                        "15% chance for a five-second instability zone")
                        .probability(0.15).condition(EffectCondition.ALWAYS, 100).build()));

        register("pf_fist_of_god",
                passive(EffectAction.DAMAGE_MULTIPLIER, 4.0, "Damage +300%"),
                passive(EffectAction.INSTANT_CHARGE, 1, "Instant charge"),
                EffectSpec.builder(EffectTrigger.MELEE_ATTACK, EffectAction.SEISMIC_WAVE, 15,
                        "Every hit creates a seismic wave with twenty-block knockback")
                        .condition(EffectCondition.ALWAYS, 20).build(),
                EffectSpec.builder(EffectTrigger.KILL, EffectAction.KILL_EARTHQUAKE, 15,
                        "20% chance to create an earthquake on kill").probability(0.20).build());
        register("chain_doomsaw",
                passive(EffectAction.DAMAGE_MULTIPLIER, 3.0, "Damage +200%"),
                passive(EffectAction.MELEE_ATTACK_SPEED_MULTIPLIER, 3.0,
                        "Attack Speed +200%"), unlimited(),
                passive(EffectAction.MELEE_VORTEX, 5,
                        "Continuous attacks create a five-block blade vortex"),
                EffectSpec.builder(EffectTrigger.MELEE_ATTACK, EffectAction.MELEE_INSTANT_KILL, 1,
                        "LMB has 25% chance to instantly kill").probability(0.25).build(),
                EffectSpec.builder(EffectTrigger.KILL, EffectAction.KILL_BLOOD_RAIN, 1,
                        "25% chance to create blood rain on kill").probability(0.25).build());
        register("ad_nuclear_apocalypse", base(4.0,
                passive(EffectAction.BEAM_WIDTH_MULTIPLIER, 3.0, "Beam width +200%"),
                passive(EffectAction.RAILGUN_PIERCE, 160,
                        "Beam pierces enemies and walls"),
                EffectSpec.builder(EffectTrigger.BLOCK_IMPACT,
                        EffectAction.RANDOM_TIMED_ZONE, 10,
                        "10% chance for a ten-second decay zone")
                        .probability(0.10).condition(EffectCondition.ALWAYS, 200).build(),
                EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.CONTINUOUS_NUCLEAR_EXPLOSION, 2.0,
                        "After three seconds on one target, it creates a nuclear explosion")
                        .condition(EffectCondition.TARGET_BURNING_FOR_TICKS, 60).build(),
                EffectSpec.builder(EffectTrigger.KILL, EffectAction.NUCLEAR_ON_KILL, 1.0,
                        "A target killed by this weapon also creates a nuclear explosion")
                        .build()));
        register("grf_railgun", base(3.5,
                passive(EffectAction.INSTANT_CHARGE, 1, "Instant charge"),
                passive(EffectAction.RAILGUN_PIERCE, 160,
                        "Projectile pierces all enemies and walls"),
                EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION, EffectAction.RANDOM_EMP, 10,
                        "15% chance for a +200% three-second EMP")
                        .probability(0.15).condition(EffectCondition.ALWAYS, 3.0).build()));
        register("lor_armageddon", explosive(3.0, 3.0,
                passive(EffectAction.INSTANT_LOCK_ON, 1, "Lock-on is instant"),
                EffectSpec.builder(EffectTrigger.BLOCK_IMPACT, EffectAction.NUCLEAR_IMPACT, 30,
                        "10% chance for a thirty-block nuclear impact").probability(0.10).build(),
                passive(EffectAction.THROUGH_WALL_HOMING, 0.16,
                        "Rocket tracks targets through walls")));

        register("drill_doomsday",
                passive(EffectAction.DAMAGE_MULTIPLIER, 3.0, "Damage +200%"),
                passive(EffectAction.MELEE_ATTACK_SPEED_MULTIPLIER, 3.0,
                        "Attack Speed +200%"), unlimited(),
                passive(EffectAction.MINING_AREA_SIZE, 15, "Mining area 15x15"),
                passive(EffectAction.CONTINUOUS_EARTHQUAKE, 10,
                        "Continuous attack creates a ten-block earthquake"),
                EffectSpec.builder(EffectTrigger.MELEE_ATTACK,
                        EffectAction.RANDOM_MELEE_EXPLOSION, 5,
                        "LMB has 25% chance for an underground explosion")
                        .probability(0.25).build());
        register("bfg_apocalypse", explosive(4.0, 3.0,
                passive(EffectAction.RAILGUN_PIERCE, 160,
                        "Projectile pierces enemies and walls"),
                EffectSpec.builder(EffectTrigger.BLOCK_IMPACT, EffectAction.PLASMA_RAIN, 10,
                        "20% chance to summon ten plasma projectiles from the sky")
                        .probability(0.20).condition(EffectCondition.ALWAYS, 20).build(),
                EffectSpec.builder(EffectTrigger.KILL, EffectAction.KILL_EXPLOSION, 5,
                        "Every kill creates an extra explosion").build()));
        register("lp_light_of_doom", automatic(3.0, 2.0, 0.0,
                passive(EffectAction.PIERCING_COUNT, 64, "Beam pierces enemies"),
                EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.RANDOM_LIGHT_EXPLOSION, 10,
                        "10% chance for a five-second +200% blinding light explosion")
                        .probability(0.10).condition(EffectCondition.ALWAYS, 3.0).build()));
    }

    private static EffectSpec passive(EffectAction action, double value, String fragment) {
        return EffectSpec.builder(EffectTrigger.PASSIVE, action, value, fragment).build();
    }

    private static EffectSpec unlimited() {
        return passive(EffectAction.UNLIMITED_AMMO, 1, "Infinite magazine or fuel");
    }

    private static EffectSpec[] base(double damage, EffectSpec... extras) {
        EffectSpec[] effects = new EffectSpec[extras.length + 2];
        effects[0] = passive(EffectAction.DAMAGE_MULTIPLIER, damage, "Damage bonus");
        effects[1] = unlimited();
        System.arraycopy(extras, 0, effects, 2, extras.length);
        return effects;
    }

    private static EffectSpec[] automatic(double damage, double fireRateBonus,
                                          double accuracyBonus, EffectSpec... extras) {
        EffectSpec[] effects = new EffectSpec[extras.length + 3 + (accuracyBonus > 0 ? 1 : 0)];
        int index = 0;
        effects[index++] = passive(EffectAction.DAMAGE_MULTIPLIER, damage, "Damage bonus");
        effects[index++] = passive(EffectAction.FIRE_DELAY_MULTIPLIER,
                1.0 / (1.0 + fireRateBonus), "Fire rate bonus");
        if (accuracyBonus > 0) {
            effects[index++] = passive(EffectAction.SPREAD_MULTIPLIER,
                    1.0 / (1.0 + accuracyBonus), "Accuracy bonus");
        }
        effects[index++] = unlimited();
        System.arraycopy(extras, 0, effects, index, extras.length);
        return effects;
    }

    private static EffectSpec[] explosive(double damage, double radius, EffectSpec... extras) {
        EffectSpec[] effects = new EffectSpec[extras.length + 3];
        effects[0] = passive(EffectAction.DAMAGE_MULTIPLIER, damage, "Damage bonus");
        effects[1] = passive(EffectAction.EXPLOSION_RADIUS_MULTIPLIER, radius,
                "Explosion radius bonus");
        effects[2] = unlimited();
        System.arraycopy(extras, 0, effects, 3, extras.length);
        return effects;
    }

    private static EffectSpec periodicDamage(int interval, double multiplier, String fragment) {
        return EffectSpec.builder(EffectTrigger.DAMAGE_CALCULATION,
                        EffectAction.PERIODIC_SHOT_DAMAGE_MULTIPLIER, multiplier, fragment)
                .condition(EffectCondition.EVERY_NTH_SHOT, interval).build();
    }

    private static EffectSpec periodicRadial(int interval, int count, String fragment) {
        return EffectSpec.builder(EffectTrigger.PROJECTILE_SPAWN,
                        EffectAction.RADIAL_PROJECTILE_BURST, count, fragment)
                .probability(0.0).condition(EffectCondition.EVERY_NTH_SHOT, interval).build();
    }

    private static EffectSpec periodicTargeted(int interval, int count, String fragment) {
        return EffectSpec.builder(EffectTrigger.PROJECTILE_SPAWN,
                        EffectAction.TARGETED_PROJECTILE_BURST, count, fragment)
                .probability(0.0).condition(EffectCondition.EVERY_NTH_SHOT, interval).build();
    }

    private static void register(String id, EffectSpec... effects) {
        if (DECLARED.put(id, Arrays.asList(effects)) != null) {
            throw new IllegalStateException("Duplicate Ultra-Mythic definition: " + id);
        }
    }
}

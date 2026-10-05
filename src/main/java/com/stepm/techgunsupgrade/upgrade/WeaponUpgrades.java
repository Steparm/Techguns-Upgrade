package com.stepm.techgunsupgrade.upgrade;

import java.util.*;

public class WeaponUpgrades {

    private static final Map<String, List<UpgradeBuff>> WEAPON_UPGRADES = new HashMap<>();

    public static void registerAll() {
        registerHomemadeGun();
        registerDoubleBarrel();
        registerRevolver();
        registerGoldenRevolver();
        registerThompson();
        registerAKM47();
        registerBoltAction();
        registerM4A1();
        registerSaboteur();
        registerPistol();
        registerCombatShotgun();
        registerMAC10();
        registerFlamethrower();
        registerBazooka();
        registerGrimReaper();
        registerGrenadeLauncher();
        registerAUG();
        registerHellBlaster();
        registerBiogun();
        registerTeslaCannon();
        registerLMG();
        registerMinigun();
        registerAS50();
        registerVector();
        registerSCAR();
        registerLaserRifle();
        registerBlasterRifle();
        registerBlasterShotgun();
        registerSonicRifle();
        registerP90();
        registerPulseRifle();
        registerSplitterPistol();
        registerPowerFist();
        registerChainsaw();
        registerAtomicDisintegrator();
        registerGaussRifle();
        registerLockOnRocket();
        registerDrill();
        registerBFG10K();
        registerLaserPistol();
    }

    public static List<UpgradeBuff> getUpgradesForWeapon(String weaponId) {
        return WEAPON_UPGRADES.getOrDefault(weaponId, Collections.emptyList());
    }

    public static List<UpgradeBuff> getUpgradesByRarity(String weaponId, UpgradeRarity rarity) {
        List<UpgradeBuff> result = new ArrayList<>();
        List<UpgradeBuff> all = getUpgradesForWeapon(weaponId);
        for (UpgradeBuff buff : all) {
            if (buff.getRarity() == rarity) {
                result.add(buff);
            }
        }
        return result;
    }

    public static Map<String, List<UpgradeBuff>> getAllWeaponUpgrades() {
        return WEAPON_UPGRADES;
    }

    public static UpgradeBuff getUpgradeById(String upgradeId) {
        if (upgradeId == null || upgradeId.isEmpty()) return null;
        for (List<UpgradeBuff> buffs : WEAPON_UPGRADES.values()) {
            for (UpgradeBuff buff : buffs) {
                if (upgradeId.equals(buff.getId())) return buff;
            }
        }
        return null;
    }

    private static void registerHomemadeGun() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("hmg_rusty_barrel", "Rusty Barrel", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10% (but jam chance +5%)").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("hmg_crooked_sight", "Crooked Sight", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("Spread +15% (worse accuracy)").value(-0.15).build());
        buffs.add(new UpgradeBuff.Builder("hmg_heavy_trigger", "Heavy Trigger", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Fire Rate -10% (slower)").value(-0.1).build());
        buffs.add(new UpgradeBuff.Builder("hmg_big_mag", "Big Magazine", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+1 ammo in magazine").value(1).build());
        buffs.add(new UpgradeBuff.Builder("hmg_silencer", "Makeshift Silencer", UpgradeRarity.UNCOMMON, UpgradeType.SILENCER).description("Shot 50% quieter (mobs don't hear)").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("hmg_strong_spring", "Strong Spring", UpgradeRarity.UNCOMMON, UpgradeType.FIRE_RATE).description("Fire Rate +20%").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("hmg_large_shot", "Large Shot", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +20% (but -10% accuracy)").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("hmg_sawed_off", "Sawed-Off", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +30% at close range").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("hmg_explosive", "Explosive Payload", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("Bullet explodes on impact (small explosion)").value(0).build());
        buffs.add(new UpgradeBuff.Builder("hmg_incendiary", "Incendiary Mix", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Ignites target for 3 seconds").value(3).build());
        buffs.add(new UpgradeBuff.Builder("hmg_triple_shot", "Triple Shot", UpgradeRarity.RARE, UpgradeType.TRIPLE_SHOT).description("Shoots 3 bullets at once (uses 4 ammo)").value(3).build());
        buffs.add(new UpgradeBuff.Builder("hmg_long_barrel", "Long Barrel", UpgradeRarity.RARE, UpgradeType.RANGE).description("+50% range").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("hmg_combat", "Combat Homemade", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("Damage +40%, Fire Rate +20%").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("hmg_shotgun", "Makeshift Shotgun", UpgradeRarity.EPIC, UpgradeType.BURST_FIRE).description("Shoots 5 bullets in a fan (less damage each)").value(5).build());
        buffs.add(new UpgradeBuff.Builder("hmg_armor_piercing", "Armor Piercing Ammo", UpgradeRarity.EPIC, UpgradeType.ARMOR_PIERCING).description("Bullets ignore 30% of target armor").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("hmg_critical_miss", "Critical Miss", UpgradeRarity.LEGENDARY, UpgradeType.STACKING_CRITICAL).description("25% chance +250% damage (5% chance explode in hands)").value(250).build());
        buffs.add(new UpgradeBuff.Builder("hmg_monster", "Homemade Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +80%, Fire Rate +40%, Accuracy -30%").value(0.8).build());
        buffs.add(new UpgradeBuff.Builder("hmg_chaotic_charge", "Chaotic Charge", UpgradeRarity.MYTHIC, UpgradeType.DAMAGE).description("25% chance +300% damage, 10% chance take 50% damage").value(3.0).build());
        buffs.add(new UpgradeBuff.Builder("hmg_railgun", "Makeshift Railgun", UpgradeRarity.MYTHIC, UpgradeType.PIERCING).description("Bullet pierces enemies and walls (Damage +100%)").value(1.0).build());
        buffs.add(new UpgradeBuff.Builder("hmg_apocalypse", "Apocalypse", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.EXPLOSIVE).description("upgrade.hmg_apocalypse.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:handcannon", buffs);
    }

    private static void registerDoubleBarrel() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("db_heavy_barrel", "Heavy Barrel", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("db_short_barrel", "Short Barrel", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +15% at close range (up to 5 blocks), but -20% range").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("db_smooth_barrel", "Smooth Barrel", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("Spread -10% (slightly more accurate)").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("db_long_barrel", "Extended Barrel", UpgradeRarity.COMMON, UpgradeType.RANGE).description("+30% range").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("db_double_shot", "Double Shot", UpgradeRarity.UNCOMMON, UpgradeType.DOUBLE_SHOT).description("Fires both barrels at once (uses 2 ammo, double damage)").value(2).build());
        buffs.add(new UpgradeBuff.Builder("db_strong_ammo", "Strong Ammo", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("db_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("db_magnum", "Magnum Shells", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +30%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("db_incendiary", "Incendiary Shot", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Ignites target for 3 seconds").value(3).build());
        buffs.add(new UpgradeBuff.Builder("db_explosive", "Explosive Shot", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("Each pellet has small explosion on impact").value(0).build());
        buffs.add(new UpgradeBuff.Builder("db_armor_piercing", "Armor Piercing Shot", UpgradeRarity.RARE, UpgradeType.ARMOR_PIERCING).description("Pellets ignore 40% of target armor").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("db_cartridge", "Cartridge", UpgradeRarity.RARE, UpgradeType.BURST_FIRE).description("+3 additional pellets per shot (total 10)").value(3).build());
        buffs.add(new UpgradeBuff.Builder("db_hell_shot", "Hell Shot", UpgradeRarity.EPIC, UpgradeType.DOUBLE_SHOT).description("Fires both barrels at once, doubling damage and igniting target for 5 seconds").value(2).build());
        buffs.add(new UpgradeBuff.Builder("db_deadly_duet", "Deadly Duet", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("If both barrels hit the same target, second shot deals +100% damage").value(1.0).build());
        buffs.add(new UpgradeBuff.Builder("db_knockback", "Knockback Round", UpgradeRarity.EPIC, UpgradeType.KNOCKBACK).description("Knocks target back 5 blocks on hit").value(5).build());
        buffs.add(new UpgradeBuff.Builder("db_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("db_fire_storm", "Fire Storm", UpgradeRarity.LEGENDARY, UpgradeType.FIRE_RATE).description("+100% Fire Rate and +30% damage, but -20% accuracy").value(1.0).build());
        buffs.add(new UpgradeBuff.Builder("db_monster", "Shotgun Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +70%, +5 pellets, but reload 2x slower").value(0.7).build());
        buffs.add(new UpgradeBuff.Builder("db_hell_duet", "Hell Duet", UpgradeRarity.MYTHIC, UpgradeType.EXPLOSIVE).description("Firing both barrels simultaneously creates a 5-block radius explosion").value(5).build());
        buffs.add(new UpgradeBuff.Builder("db_doom_shot", "Doom Shot", UpgradeRarity.MYTHIC, UpgradeType.DAMAGE).description("Each pellet deals +50% damage, but jam chance +15%").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("db_hell_duet_ultra", "Hell Duet", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.EXPLOSIVE).description("upgrade.db_hell_duet_ultra.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:sawedoff", buffs);
    }

    private static void registerRevolver() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("rev_heavy_trigger", "Heavy Trigger", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("rev_long_barrel", "Long Barrel", UpgradeRarity.COMMON, UpgradeType.RANGE).description("+30% range").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("rev_handle_grip", "Handle Grip", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Fire Rate +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("rev_big_cylinder", "Big Cylinder", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+1 ammo in cylinder (total 7)").value(1).build());
        buffs.add(new UpgradeBuff.Builder("rev_magnum", "Magnum Rounds", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("rev_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("rev_scope", "Scoped Barrel", UpgradeRarity.UNCOMMON, UpgradeType.ACCURACY).description("Spread -20% (more accurate)").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("rev_strong_spring", "Strong Spring", UpgradeRarity.UNCOMMON, UpgradeType.FIRE_RATE).description("Fire Rate +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("rev_incendiary", "Incendiary Rounds", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Ignites target for 3 seconds").value(3).build());
        buffs.add(new UpgradeBuff.Builder("rev_shatter", "Shatter Rounds", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("Explodes on impact, dealing area damage (2 block radius)").value(2).build());
        buffs.add(new UpgradeBuff.Builder("rev_explosive", "Explosive Rounds", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("Small explosion on impact").value(0).build());
        buffs.add(new UpgradeBuff.Builder("rev_fast_hand", "Fast Hand", UpgradeRarity.RARE, UpgradeType.FIRE_RATE).description("+25% Fire Rate when firing on the move").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("rev_deadly_shot", "Deadly Shot", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("Last round in cylinder deals +200% damage").value(2.0).build());
        buffs.add(new UpgradeBuff.Builder("rev_fast_trigger", "Fast Trigger", UpgradeRarity.EPIC, UpgradeType.FIRE_RATE).description("Fire Rate +40%").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("rev_precise_shot", "Precise Shot", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("First shot after reload is always critical (x1.5 damage)").value(1.5).build());
        buffs.add(new UpgradeBuff.Builder("rev_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("rev_six_shot_hell", "Six Shot Hell", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +50%, Fire Rate +30%").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("rev_sniper", "Sniper Revolver", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("+40% damage at range >20 blocks").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("rev_fatal_shot", "Fatal Shot", UpgradeRarity.MYTHIC, UpgradeType.STACKING_CRITICAL).description("20% chance to deal +400% damage").value(4.0).build());
        buffs.add(new UpgradeBuff.Builder("rev_endless_cylinder", "Endless Cylinder", UpgradeRarity.MYTHIC, UpgradeType.UNLIMITED_AMMO).description("15% chance not to consume ammo").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("rev_judge", "Judge", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.LIGHTNING).description("upgrade.rev_judge.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:revolver", buffs);
    }

    private static void registerGoldenRevolver() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("gr_gold_trigger", "Golden Trigger", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("gr_gold_grip", "Golden Grip", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Fire Rate +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("gr_gold_cylinder", "Golden Cylinder", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+1 ammo in cylinder (total 7)").value(1).build());
        buffs.add(new UpgradeBuff.Builder("gr_gold_inlay", "Golden Inlay", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("+15% accuracy").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("gr_royal_ammo", "Royal Ammo", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("gr_luxury_reload", "Luxury Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("gr_gold_barrel", "Golden Barrel", UpgradeRarity.UNCOMMON, UpgradeType.RANGE).description("+30% range").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("gr_noble_trigger", "Noble Trigger", UpgradeRarity.UNCOMMON, UpgradeType.FIRE_RATE).description("Fire Rate +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("gr_glowing_ammo", "Glowing Ammo", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Bullets leave a glowing trail, highlighting target for 3 seconds").value(3).build());
        buffs.add(new UpgradeBuff.Builder("gr_explosive", "Explosive Rounds", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("Small explosion on impact").value(0).build());
        buffs.add(new UpgradeBuff.Builder("gr_sixth_shot", "Sixth Shot", UpgradeRarity.RARE, UpgradeType.DAMAGE).description("Last round in cylinder deals +100% damage").value(1.0).build());
        buffs.add(new UpgradeBuff.Builder("gr_treasure", "Treasure", UpgradeRarity.RARE, UpgradeType.STACKING_DAMAGE).description("10% chance to drop a gold ingot on kill").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("gr_deadly_shot", "Deadly Shot", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("Last round in cylinder deals +200% damage").value(2.0).build());
        buffs.add(new UpgradeBuff.Builder("gr_fast_trigger", "Fast Trigger", UpgradeRarity.EPIC, UpgradeType.FIRE_RATE).description("Fire Rate +40%").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("gr_royal_precision", "Royal Precision", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("First shot after reload is always critical (x1.5 damage)").value(1.5).build());
        buffs.add(new UpgradeBuff.Builder("gr_gold_harvest", "Golden Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 3 hearts").value(3).build());
        buffs.add(new UpgradeBuff.Builder("gr_king", "King of Revolvers", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +50%, Fire Rate +30%").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("gr_long_shot", "Long Shot", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("+40% damage at range >20 blocks").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("gr_golden_rain", "Golden Rain", UpgradeRarity.MYTHIC, UpgradeType.STACKING_CRITICAL).description("20% chance to deal +400% damage").value(4.0).build());
        buffs.add(new UpgradeBuff.Builder("gr_endless_cylinder", "Endless Cylinder", UpgradeRarity.MYTHIC, UpgradeType.UNLIMITED_AMMO).description("15% chance not to consume ammo").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("gr_gold_king", "Golden King", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.STACKING_DAMAGE).description("upgrade.gr_gold_king.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:goldenrevolver", buffs);
    }

    private static void registerThompson() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("thom_heavy_barrel", "Heavy Barrel", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("thom_long_mag", "Long Magazine", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+5 ammo (total 25)").value(5).build());
        buffs.add(new UpgradeBuff.Builder("thom_bipod", "Bipod", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("-15% spread when firing bursts").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("thom_strong_spring", "Strong Spring", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Fire Rate +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("thom_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("thom_scope", "Scope", UpgradeRarity.UNCOMMON, UpgradeType.ACCURACY).description("Spread -20%").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("thom_magnum", "Magnum Ammo", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("thom_silencer", "Silencer", UpgradeRarity.UNCOMMON, UpgradeType.SILENCER).description("Shot 40% quieter").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("thom_incendiary", "Incendiary Rounds", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Ignites target for 3 seconds").value(3).build());
        buffs.add(new UpgradeBuff.Builder("thom_explosive", "Explosive Rounds", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("Small explosion on impact").value(0).build());
        buffs.add(new UpgradeBuff.Builder("thom_speed_mag", "Speed Magazine", UpgradeRarity.RARE, UpgradeType.FIRE_RATE).description("Fire Rate +30%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("thom_combat_stock", "Combat Stock", UpgradeRarity.RARE, UpgradeType.ACCURACY).description("+20% accuracy when firing bursts").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("thom_fire_storm", "Fire Storm", UpgradeRarity.EPIC, UpgradeType.STACKING_CRITICAL).description("Every 5th shot deals double damage").value(2).build());
        buffs.add(new UpgradeBuff.Builder("thom_gangster", "Gangster", UpgradeRarity.EPIC, UpgradeType.FIRE_RATE).description("+20% movement speed while firing").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("thom_long_burst", "Long Burst", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("Burst of >10 shots deals +20% damage").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("thom_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("thom_machine_gun_hell", "Machine Gun Hell", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +40%, Fire Rate +40%, but -20% accuracy").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("thom_combat", "Combat Thompson", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +30%, Accuracy +20%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("thom_virtuoso", "Virtuoso", UpgradeRarity.MYTHIC, UpgradeType.UNLIMITED_AMMO).description("20% chance not to consume ammo").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("thom_monster", "Tommy Monster", UpgradeRarity.MYTHIC, UpgradeType.DAMAGE).description("Damage +60%, Fire Rate +50%").value(0.6).build());
        buffs.add(new UpgradeBuff.Builder("thom_gangster_ultra", "Gangster", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.FIRE_RATE).description("upgrade.thom_gangster_ultra.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:thompson", buffs);
    }

    private static void registerAKM47() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("akm_heavy_barrel", "Heavy Barrel", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("akm_mag", "Magazine", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+5 ammo (total 35)").value(5).build());
        buffs.add(new UpgradeBuff.Builder("akm_stock", "Wooden Stock", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("-15% spread when firing bursts").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("akm_spring", "Recoil Spring", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Fire Rate +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("akm_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("akm_scope", "Collimator Sight", UpgradeRarity.UNCOMMON, UpgradeType.ACCURACY).description("Spread -20%").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("akm_magnum", "Magnum Ammo", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("akm_silencer", "Flash Suppressor", UpgradeRarity.UNCOMMON, UpgradeType.SILENCER).description("Shot 30% quieter").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("akm_incendiary", "Incendiary Rounds", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Ignites target for 3 seconds").value(3).build());
        buffs.add(new UpgradeBuff.Builder("akm_explosive", "Explosive Rounds", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("Small explosion on impact").value(0).build());
        buffs.add(new UpgradeBuff.Builder("akm_speed_fire", "Speed Fire", UpgradeRarity.RARE, UpgradeType.FIRE_RATE).description("Fire Rate +30%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("akm_army_stock", "Army Stock", UpgradeRarity.RARE, UpgradeType.ACCURACY).description("+20% accuracy when firing bursts").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("akm_fire_storm", "Fire Storm", UpgradeRarity.EPIC, UpgradeType.STACKING_CRITICAL).description("Every 5th shot deals double damage").value(2).build());
        buffs.add(new UpgradeBuff.Builder("akm_reliable", "Reliable Rifle", UpgradeRarity.EPIC, UpgradeType.ACCURACY).description("Jam chance reduced by 50%").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("akm_long_burst", "Long Burst", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("Burst of >15 shots deals +20% damage").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("akm_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("akm_monster", "AK Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +40%, Fire Rate +40%, but -20% accuracy").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("akm_combat", "Combat AK", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +30%, Accuracy +20%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("akm_legend", "Gunsmith Legend", UpgradeRarity.MYTHIC, UpgradeType.UNLIMITED_AMMO).description("20% chance not to consume ammo").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("akm_monster_ak", "AK Monster", UpgradeRarity.MYTHIC, UpgradeType.DAMAGE).description("Damage +60%, Fire Rate +50%").value(0.6).build());
        buffs.add(new UpgradeBuff.Builder("akm_immortal", "Immortal Kalashnikov", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.DAMAGE).description("upgrade.akm_immortal.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:ak47", buffs);
    }

    private static void registerBoltAction() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("bolt_heavy_barrel", "Heavy Barrel", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("bolt_long_barrel", "Long Barrel", UpgradeRarity.COMMON, UpgradeType.RANGE).description("+30% range").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("bolt_stock", "Improved Stock", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("Spread -15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("bolt_big_mag", "Big Magazine", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+1 ammo (total 7)").value(1).build());
        buffs.add(new UpgradeBuff.Builder("bolt_magnum", "Magnum Rounds", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("bolt_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("bolt_sniper_scope", "Sniper Scope", UpgradeRarity.UNCOMMON, UpgradeType.ACCURACY).description("Spread -25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("bolt_fast_bolt", "Fast Bolt", UpgradeRarity.UNCOMMON, UpgradeType.FIRE_RATE).description("Fire Rate +20%").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("bolt_incendiary", "Incendiary Rounds", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Ignites target for 3 seconds").value(3).build());
        buffs.add(new UpgradeBuff.Builder("bolt_explosive", "Explosive Rounds", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("Small explosion on impact").value(0).build());
        buffs.add(new UpgradeBuff.Builder("bolt_armor_piercing", "Armor Piercing Rounds", UpgradeRarity.RARE, UpgradeType.ARMOR_PIERCING).description("Ignore 50% of target armor").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("bolt_first_shot", "First Shot", UpgradeRarity.RARE, UpgradeType.DAMAGE).description("First shot after reload deals +50% damage").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("bolt_scope_shot", "Scope Shot", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("If target is scoped for >1 second, +100% damage").value(1.0).build());
        buffs.add(new UpgradeBuff.Builder("bolt_professional", "Professional", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("+30% damage at range >30 blocks").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("bolt_silence", "Silence", UpgradeRarity.EPIC, UpgradeType.SILENCER).description("Shot makes no sound").value(1).build());
        buffs.add(new UpgradeBuff.Builder("bolt_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("bolt_sniper_monster", "Sniper Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +50%, Accuracy +30%").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("bolt_long_range", "Long Range", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("+40% damage at range >40 blocks").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("bolt_hunter", "Hunter", UpgradeRarity.MYTHIC, UpgradeType.STACKING_DAMAGE).description("Each kill gives +1% damage (permanent). Max +200%").value(1).stacking(200).build());
        buffs.add(new UpgradeBuff.Builder("bolt_double_shot", "Double Shot", UpgradeRarity.MYTHIC, UpgradeType.DOUBLE_SHOT).description("15% chance to fire a second shot without consuming ammo").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("bolt_deadly_scope", "Deadly Scope", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.PIERCING).description("upgrade.bolt_deadly_scope.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:boltaction", buffs);
    }

    private static void registerM4A1() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("m4_heavy_barrel", "Heavy Barrel", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("m4_long_mag", "Extended Magazine", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+5 ammo (total 35)").value(5).build());
        buffs.add(new UpgradeBuff.Builder("m4_tactical_stock", "Tactical Stock", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("-15% spread when firing bursts").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("m4_strong_spring", "Strong Spring", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Fire Rate +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("m4_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("m4_red_dot", "Red Dot Sight", UpgradeRarity.UNCOMMON, UpgradeType.ACCURACY).description("Spread -20%").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("m4_magnum", "Magnum Ammo", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("m4_silencer", "Silencer", UpgradeRarity.UNCOMMON, UpgradeType.SILENCER).description("Shot 40% quieter").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("m4_incendiary", "Incendiary Rounds", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Ignites target for 3 seconds").value(3).build());
        buffs.add(new UpgradeBuff.Builder("m4_explosive", "Explosive Rounds", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("Small explosion on impact").value(0).build());
        buffs.add(new UpgradeBuff.Builder("m4_speed_fire", "Speed Fire", UpgradeRarity.RARE, UpgradeType.FIRE_RATE).description("Fire Rate +30%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("m4_army_stock", "Army Stock", UpgradeRarity.RARE, UpgradeType.ACCURACY).description("+20% accuracy when firing bursts").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("m4_fire_storm", "Fire Storm", UpgradeRarity.EPIC, UpgradeType.STACKING_CRITICAL).description("Every 5th shot deals double damage").value(2).build());
        buffs.add(new UpgradeBuff.Builder("m4_tactical", "Tactical M4", UpgradeRarity.EPIC, UpgradeType.FIRE_RATE).description("+15% movement speed while firing").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("m4_long_burst", "Long Burst", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("Burst of >15 shots deals +20% damage").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("m4_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("m4_monster", "M4 Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +40%, Fire Rate +40%, but -20% accuracy").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("m4_combat", "Combat M4", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +30%, Accuracy +20%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("m4_tactical_genius", "Tactical Genius", UpgradeRarity.MYTHIC, UpgradeType.STACKING_FIRE_RATE).description("Each kill gives +2% Fire Rate (permanent). Max +100%").value(2).stacking(100).build());
        buffs.add(new UpgradeBuff.Builder("m4_deadly_queue", "Deadly Queue", UpgradeRarity.MYTHIC, UpgradeType.DAMAGE).description("If you empty the entire magazine into one enemy, last bullet deals +300% damage").value(3.0).build());
        buffs.add(new UpgradeBuff.Builder("m4_legendary", "Legendary M4", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.DAMAGE).description("upgrade.m4_legendary.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:m4", buffs);
    }

    private static void registerSaboteur() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("sab_heavy_barrel", "Heavy Barrel", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("sab_long_mag", "Extended Magazine", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+5 ammo (total 35)").value(5).build());
        buffs.add(new UpgradeBuff.Builder("sab_tactical_stock", "Tactical Stock", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("-15% spread when firing bursts").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("sab_strong_spring", "Strong Spring", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Fire Rate +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("sab_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("sab_red_dot", "Red Dot Sight", UpgradeRarity.UNCOMMON, UpgradeType.ACCURACY).description("Spread -20%").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("sab_magnum", "Magnum Ammo", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("sab_coating", "Special Coating", UpgradeRarity.UNCOMMON, UpgradeType.SILENCER).description("Even quieter (mobs hear from shorter distance)").value(0.6).build());
        buffs.add(new UpgradeBuff.Builder("sab_incendiary", "Incendiary Rounds", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Ignites target for 3 seconds").value(3).build());
        buffs.add(new UpgradeBuff.Builder("sab_explosive", "Explosive Rounds", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("Small explosion on impact").value(0).build());
        buffs.add(new UpgradeBuff.Builder("sab_speed_fire", "Speed Fire", UpgradeRarity.RARE, UpgradeType.FIRE_RATE).description("Fire Rate +30%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("sab_camouflage", "Camouflage", UpgradeRarity.RARE, UpgradeType.ACCURACY).description("+25% accuracy when firing from cover (crouching)").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("sab_quiet_killer", "Quiet Killer", UpgradeRarity.EPIC, UpgradeType.SILENCER).description("If you kill an enemy with one shot, other mobs within 10 blocks don't notice").value(10).build());
        buffs.add(new UpgradeBuff.Builder("sab_sneaky", "Sneaky Approach", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("+20% damage if player is not moving before shot").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("sab_sharpshooter", "Sharpshooter", UpgradeRarity.EPIC, UpgradeType.STACKING_CRITICAL).description("Every 3rd shot in a burst deals double damage").value(2).build());
        buffs.add(new UpgradeBuff.Builder("sab_shadow", "Shadow", UpgradeRarity.LEGENDARY, UpgradeType.ACCURACY).description("25% chance to become invisible for 3 seconds on kill").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("sab_pro", "Saboteur Pro", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +40%, Accuracy +30%").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("sab_perfect_camo", "Perfect Camouflage", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("First shot after reload is always critical (x1.5 damage)").value(1.5).build());
        buffs.add(new UpgradeBuff.Builder("sab_ghost", "Ghost", UpgradeRarity.MYTHIC, UpgradeType.STACKING_ACCURACY).description("Each kill gives +2% stealth (mobs see from shorter range). Max +60%").value(2).stacking(60).build());
        buffs.add(new UpgradeBuff.Builder("sab_deadly_whisper", "Deadly Whisper", UpgradeRarity.MYTHIC, UpgradeType.SILENCER).description("If you kill an enemy from >30 blocks, other mobs within 15 blocks get blinded for 5 seconds").value(15).build());
        buffs.add(new UpgradeBuff.Builder("sab_invisible", "Invisible", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.SILENCER).description("upgrade.sab_invisible.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:m4_infiltrator", buffs);
    }

    private static void registerPistol() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("pist_heavy_trigger", "Heavy Trigger", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("pist_long_barrel", "Long Barrel", UpgradeRarity.COMMON, UpgradeType.RANGE).description("+25% range").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("pist_grip", "Grip", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Fire Rate +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("pist_big_mag", "Big Magazine", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+3 ammo (total 21)").value(3).build());
        buffs.add(new UpgradeBuff.Builder("pist_magnum", "Magnum Rounds", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("pist_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("pist_scope", "Scoped Barrel", UpgradeRarity.UNCOMMON, UpgradeType.ACCURACY).description("Spread -20%").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("pist_strong_spring", "Strong Spring", UpgradeRarity.UNCOMMON, UpgradeType.FIRE_RATE).description("Fire Rate +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("pist_incendiary", "Incendiary Rounds", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Ignites target for 3 seconds").value(3).build());
        buffs.add(new UpgradeBuff.Builder("pist_explosive", "Explosive Rounds", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("Small explosion on impact").value(0).build());
        buffs.add(new UpgradeBuff.Builder("pist_speed_trigger", "Speed Trigger", UpgradeRarity.RARE, UpgradeType.FIRE_RATE).description("Fire Rate +35%").value(0.35).build());
        buffs.add(new UpgradeBuff.Builder("pist_double_shot", "Double Shot", UpgradeRarity.RARE, UpgradeType.DOUBLE_SHOT).description("15% chance to fire twice (uses 2 ammo)").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("pist_deadly_double", "Deadly Double", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("If two shots hit the same target consecutively, second deals +150% damage").value(1.5).build());
        buffs.add(new UpgradeBuff.Builder("pist_fast_hand", "Fast Hand", UpgradeRarity.EPIC, UpgradeType.FIRE_RATE).description("+20% movement speed while firing").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("pist_sharpshooter", "Sharpshooter", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("First shot after reload is always critical (x1.5 damage)").value(1.5).build());
        buffs.add(new UpgradeBuff.Builder("pist_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("pist_machine", "Machine Pistol", UpgradeRarity.LEGENDARY, UpgradeType.FIRE_RATE).description("Fire Rate +100%, but -20% accuracy").value(1.0).build());
        buffs.add(new UpgradeBuff.Builder("pist_sniper", "Sniper Pistol", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("+50% damage at range >15 blocks").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("pist_duelist", "Duelist", UpgradeRarity.MYTHIC, UpgradeType.STACKING_ACCURACY).description("Each kill gives +1% accuracy (permanent). Max +50%").value(1).stacking(50).build());
        buffs.add(new UpgradeBuff.Builder("pist_wild_shot", "Wild Shot", UpgradeRarity.MYTHIC, UpgradeType.STACKING_CRITICAL).description("10% chance to deal +400% damage and stun target for 2 seconds").value(4.0).build());
        buffs.add(new UpgradeBuff.Builder("pist_legendary", "Legendary Pistol", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.DAMAGE).description("upgrade.pist_legendary.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:pistol", buffs);
    }

    private static void registerCombatShotgun() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("cs_heavy_barrel", "Heavy Barrel", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("cs_short_barrel", "Short Barrel", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +15% at close range (up to 5 blocks), but -20% range").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("cs_rifled_barrel", "Rifled Barrel", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("Spread -15% (slightly more accurate)").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("cs_long_mag", "Extended Magazine", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+2 ammo (total 10)").value(2).build());
        buffs.add(new UpgradeBuff.Builder("cs_magnum", "Magnum Shells", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("cs_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("cs_shot", "Shot", UpgradeRarity.UNCOMMON, UpgradeType.BURST_FIRE).description("+2 additional pellets (total 10)").value(2).build());
        buffs.add(new UpgradeBuff.Builder("cs_stock", "Stock", UpgradeRarity.UNCOMMON, UpgradeType.ACCURACY).description("-20% spread when crouching").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("cs_incendiary", "Incendiary Shot", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Ignites target for 3 seconds").value(3).build());
        buffs.add(new UpgradeBuff.Builder("cs_explosive", "Explosive Shot", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("Each pellet has small explosion on impact").value(0).build());
        buffs.add(new UpgradeBuff.Builder("cs_armor_piercing", "Armor Piercing Shot", UpgradeRarity.RARE, UpgradeType.ARMOR_PIERCING).description("Pellets ignore 40% of target armor").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("cs_cartridge", "Cartridge", UpgradeRarity.RARE, UpgradeType.BURST_FIRE).description("+4 additional pellets (total 12)").value(4).build());
        buffs.add(new UpgradeBuff.Builder("cs_combat_shot", "Combat Shot", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("If fired point-blank (up to 3 blocks), +100% damage").value(1.0).build());
        buffs.add(new UpgradeBuff.Builder("cs_shot_storm", "Shot Storm", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("Hitting multiple enemies at once gives +20% damage to each").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("cs_knockback", "Knockback Round", UpgradeRarity.EPIC, UpgradeType.KNOCKBACK).description("Knocks target back 5 blocks on hit").value(5).build());
        buffs.add(new UpgradeBuff.Builder("cs_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("cs_monster", "Shotgun Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +60%, +5 pellets, but reload 2x slower").value(0.6).build());
        buffs.add(new UpgradeBuff.Builder("cs_fire_storm", "Fire Storm", UpgradeRarity.LEGENDARY, UpgradeType.FIRE_RATE).description("+50% Fire Rate and +30% damage, but -20% accuracy").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("cs_street_fighter", "Street Fighter", UpgradeRarity.MYTHIC, UpgradeType.STACKING_DAMAGE).description("Each kill gives +2% damage at close range (up to 5 blocks). Max +100%").value(2).stacking(100).build());
        buffs.add(new UpgradeBuff.Builder("cs_deadly_shot", "Deadly Shot", UpgradeRarity.MYTHIC, UpgradeType.DAMAGE).description("If all 8 shots hit the same target without reloading, last bullet deals +500% damage").value(5.0).build());
        buffs.add(new UpgradeBuff.Builder("cs_war_machine", "War Machine", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.BURST_FIRE).description("upgrade.cs_war_machine.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:combatshotgun", buffs);
    }

    private static void registerMAC10() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("mac_heavy_barrel", "Heavy Barrel", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("mac_long_mag", "Extended Magazine", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+4 ammo (total 36)").value(4).build());
        buffs.add(new UpgradeBuff.Builder("mac_bipod", "Bipod", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("-15% spread when firing bursts").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("mac_strong_spring", "Strong Spring", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Fire Rate +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("mac_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("mac_red_dot", "Red Dot Sight", UpgradeRarity.UNCOMMON, UpgradeType.ACCURACY).description("Spread -20%").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("mac_magnum", "Magnum Ammo", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("mac_silencer", "Silencer", UpgradeRarity.UNCOMMON, UpgradeType.SILENCER).description("Shot 40% quieter").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("mac_incendiary", "Incendiary Rounds", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Ignites target for 3 seconds").value(3).build());
        buffs.add(new UpgradeBuff.Builder("mac_explosive", "Explosive Rounds", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("Small explosion on impact").value(0).build());
        buffs.add(new UpgradeBuff.Builder("mac_speed_fire", "Speed Fire", UpgradeRarity.RARE, UpgradeType.FIRE_RATE).description("Fire Rate +30%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("mac_small_beast", "Small Beast", UpgradeRarity.RARE, UpgradeType.DAMAGE).description("+25% damage at close range (up to 5 blocks)").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("mac_fire_storm", "Fire Storm", UpgradeRarity.EPIC, UpgradeType.STACKING_CRITICAL).description("Every 5th shot deals double damage").value(2).build());
        buffs.add(new UpgradeBuff.Builder("mac_gangster", "Gangster", UpgradeRarity.EPIC, UpgradeType.FIRE_RATE).description("+20% movement speed while firing").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("mac_hail", "Hail of Bullets", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("If you fire >20 shots without reloading, damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("mac_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("mac_monster", "MAC Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +40%, Fire Rate +40%, but -20% accuracy").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("mac_combat", "Combat MAC", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +30%, Accuracy +20%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("mac_hail_lead", "Hail of Lead", UpgradeRarity.MYTHIC, UpgradeType.STACKING_FIRE_RATE).description("Each kill gives +2% Fire Rate (permanent). Max +100%").value(2).stacking(100).build());
        buffs.add(new UpgradeBuff.Builder("mac_deadly_fan", "Deadly Fan", UpgradeRarity.MYTHIC, UpgradeType.DAMAGE).description("If you empty all 32 shots into one enemy, last bullet deals +500% damage").value(5.0).build());
        buffs.add(new UpgradeBuff.Builder("mac_legendary", "Legendary MAC", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.FIRE_RATE).description("upgrade.mac_legendary.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:mac10", buffs);
    }

    private static void registerFlamethrower() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("flame_valve", "Enhanced Valve", UpgradeRarity.COMMON, UpgradeType.RANGE).description("Flame range +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("flame_big_tank", "Big Tank", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+20 fuel (total 120)").value(20).build());
        buffs.add(new UpgradeBuff.Builder("flame_stabilizer", "Flame Stabilizer", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("Flame spread -15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("flame_silencer", "Flame Silencer", UpgradeRarity.COMMON, UpgradeType.SILENCER).description("Enemies see fire from shorter distance").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("flame_incendiary", "Incendiary Mix", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Burn damage +30%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("flame_long_hose", "Extended Hose", UpgradeRarity.UNCOMMON, UpgradeType.RANGE).description("Flame range +30%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("flame_high_pressure", "High Pressure", UpgradeRarity.UNCOMMON, UpgradeType.FIRE_RATE).description("Flame spread speed +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("flame_thickener", "Thickener", UpgradeRarity.UNCOMMON, UpgradeType.INCENDIARY).description("Fire stays on ground 2 seconds longer").value(2).build());
        buffs.add(new UpgradeBuff.Builder("flame_napalm", "Napalm", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Ignites enemies for 5 seconds (instead of 3)").value(5).build());
        buffs.add(new UpgradeBuff.Builder("flame_explosive", "Explosive Mix", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("10% chance of small explosion on hit").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("flame_hell", "Hellfire", UpgradeRarity.RARE, UpgradeType.DAMAGE).description("Damage +30%, but fuel consumption +20%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("flame_smoke", "Smoke Screen", UpgradeRarity.RARE, UpgradeType.SILENCER).description("Creates a smoke cloud while firing, hiding the player").value(0).build());
        buffs.add(new UpgradeBuff.Builder("flame_tornado", "Fire Tornado", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("Firing at the ground creates a moving fire zone").value(0).build());
        buffs.add(new UpgradeBuff.Builder("flame_pyromaniac", "Pyromaniac", UpgradeRarity.EPIC, UpgradeType.EXPLOSIVE).description("Each killed enemy explodes, igniting nearby enemies").value(0).build());
        buffs.add(new UpgradeBuff.Builder("flame_hot_steel", "Hot Steel", UpgradeRarity.EPIC, UpgradeType.ARMOR_PIERCING).description("Fire ignores 50% of fire resistance").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("flame_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("flame_storm", "Fire Storm", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Range +50%, Damage +40%").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("flame_endless", "Endless Fire", UpgradeRarity.LEGENDARY, UpgradeType.UNLIMITED_AMMO).description("Infinite magazine (fuel not consumed)").value(0).build());
        buffs.add(new UpgradeBuff.Builder("flame_burning_earth", "Burning Earth", UpgradeRarity.MYTHIC, UpgradeType.STACKING_DAMAGE).description("Each kill gives +2% fire damage (permanent). Max +100%").value(2).stacking(100).build());
        buffs.add(new UpgradeBuff.Builder("flame_deadly_heat", "Deadly Heat", UpgradeRarity.MYTHIC, UpgradeType.DAMAGE).description("If burning for more than 5 seconds, target takes +200% fire damage").value(2.0).build());
        buffs.add(new UpgradeBuff.Builder("flame_hellfire", "Hellfire", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.INCENDIARY).description("upgrade.flame_hellfire.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:flamethrower", buffs);
    }

    private static void registerBazooka() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("baz_heavy_barrel", "Heavy Barrel", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("baz_long_launcher", "Extended Launcher", UpgradeRarity.COMMON, UpgradeType.RANGE).description("Rocket flight range +30%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("baz_light_frame", "Light Frame", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Movement speed with weapon +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("baz_scope", "Scope", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("Rocket accuracy +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("baz_high_explosive", "High Explosive", UpgradeRarity.UNCOMMON, UpgradeType.EXPLOSIVE).description("Explosion radius +30%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("baz_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("baz_armor_piercing", "Armor Piercing Rocket", UpgradeRarity.UNCOMMON, UpgradeType.ARMOR_PIERCING).description("Ignores 40% of target armor").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("baz_speed_rocket", "Speed Rocket", UpgradeRarity.UNCOMMON, UpgradeType.RANGE).description("Rocket flight speed +40%").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("baz_incendiary", "Incendiary Rocket", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Creates a fire zone after explosion for 5 seconds").value(5).build());
        buffs.add(new UpgradeBuff.Builder("baz_cluster", "Cluster Rocket", UpgradeRarity.RARE, UpgradeType.BURST_FIRE).description("Splits into 3 smaller rockets on explosion").value(3).build());
        buffs.add(new UpgradeBuff.Builder("baz_heavy_rocket", "Heavy Rocket", UpgradeRarity.RARE, UpgradeType.DAMAGE).description("Damage +50%, but flight speed -20%").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("baz_tactical", "Tactical Rocket", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("+20% explosion radius and +20% damage").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("baz_nuclear", "Nuclear Charge", UpgradeRarity.EPIC, UpgradeType.EXPLOSIVE).description("Explosion radius +80%, Damage +60%").value(0.8).build());
        buffs.add(new UpgradeBuff.Builder("baz_shockwave", "Shockwave", UpgradeRarity.EPIC, UpgradeType.KNOCKBACK).description("Knocks enemies back 10 blocks from epicenter").value(10).build());
        buffs.add(new UpgradeBuff.Builder("baz_heat_seeking", "Heat Seeking", UpgradeRarity.EPIC, UpgradeType.ACCURACY).description("Rocket slightly tracks nearest target").value(0).build());
        buffs.add(new UpgradeBuff.Builder("baz_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("baz_monster", "Bazooka Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +80%, Radius +50%").value(0.8).build());
        buffs.add(new UpgradeBuff.Builder("baz_endless_salvo", "Endless Salvo", UpgradeRarity.LEGENDARY, UpgradeType.UNLIMITED_AMMO).description("15% chance not to consume rocket").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("baz_apocalypse", "Apocalypse", UpgradeRarity.MYTHIC, UpgradeType.STACKING_DAMAGE).description("Each kill gives +2% explosive damage (permanent). Max +100%").value(2).stacking(100).build());
        buffs.add(new UpgradeBuff.Builder("baz_deadly_salvo", "Deadly Salvo", UpgradeRarity.MYTHIC, UpgradeType.DAMAGE).description("If you kill an enemy with the first shot, next shot deals +300% damage").value(3.0).build());
        buffs.add(new UpgradeBuff.Builder("baz_armageddon", "Armageddon", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.EXPLOSIVE).description("upgrade.baz_armageddon.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:rocketlauncher", buffs);
    }

    private static void registerGrimReaper() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("grp_heavy_barrel", "Heavy Barrel", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("grp_long_launcher", "Extended Launcher", UpgradeRarity.COMMON, UpgradeType.RANGE).description("Rocket flight range +30%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("grp_light_frame", "Light Frame", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Movement speed with weapon +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("grp_scope", "Scope", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("Rocket accuracy +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("grp_high_explosive", "High Explosive", UpgradeRarity.UNCOMMON, UpgradeType.EXPLOSIVE).description("Explosion radius +30%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("grp_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("grp_armor_piercing", "Armor Piercing Rocket", UpgradeRarity.UNCOMMON, UpgradeType.ARMOR_PIERCING).description("Ignores 40% of target armor").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("grp_speed_rocket", "Speed Rocket", UpgradeRarity.UNCOMMON, UpgradeType.RANGE).description("Rocket flight speed +40%").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("grp_incendiary", "Incendiary Rocket", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Creates a fire zone after explosion for 5 seconds").value(5).build());
        buffs.add(new UpgradeBuff.Builder("grp_cluster", "Cluster Rocket", UpgradeRarity.RARE, UpgradeType.BURST_FIRE).description("Splits into 3 smaller rockets on explosion").value(3).build());
        buffs.add(new UpgradeBuff.Builder("grp_heavy_rocket", "Heavy Rocket", UpgradeRarity.RARE, UpgradeType.DAMAGE).description("Damage +50%, but flight speed -20%").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("grp_tactical", "Tactical Rocket", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("+20% explosion radius and +20% damage").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("grp_nuclear", "Nuclear Charge", UpgradeRarity.EPIC, UpgradeType.EXPLOSIVE).description("Explosion radius +80%, Damage +60%").value(0.8).build());
        buffs.add(new UpgradeBuff.Builder("grp_shockwave", "Shockwave", UpgradeRarity.EPIC, UpgradeType.KNOCKBACK).description("Knocks enemies back 10 blocks from epicenter").value(10).build());
        buffs.add(new UpgradeBuff.Builder("grp_heat_seeking", "Heat Seeking", UpgradeRarity.EPIC, UpgradeType.ACCURACY).description("Rocket slightly tracks nearest target").value(0).build());
        buffs.add(new UpgradeBuff.Builder("grp_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("grp_monster", "Reaper Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +80%, Radius +50%").value(0.8).build());
        buffs.add(new UpgradeBuff.Builder("grp_endless_salvo", "Endless Salvo", UpgradeRarity.LEGENDARY, UpgradeType.UNLIMITED_AMMO).description("15% chance not to consume rocket").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("grp_deadly_harvest", "Deadly Harvest", UpgradeRarity.MYTHIC, UpgradeType.STACKING_DAMAGE).description("Each kill gives +1% damage and +0.5% radius (permanent). Max +100% damage and +50% radius").value(1).stacking(100).build());
        buffs.add(new UpgradeBuff.Builder("grp_salvo_death", "Salvo of Death", UpgradeRarity.MYTHIC, UpgradeType.DAMAGE).description("If you fire all 4 rockets at one target, the last deals +500% damage").value(5.0).build());
        buffs.add(new UpgradeBuff.Builder("grp_death_scythe", "Death Scythe", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.EXPLOSIVE).description("upgrade.grp_death_scythe.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:grimreaper", buffs);
    }

    private static void registerGrenadeLauncher() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("gl_heavy_barrel", "Heavy Barrel", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("gl_long_barrel", "Long Barrel", UpgradeRarity.COMMON, UpgradeType.RANGE).description("Grenade flight range +30%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("gl_light_frame", "Light Frame", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Movement speed with weapon +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("gl_scope", "Scope", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("Grenade accuracy +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("gl_high_explosive", "High Explosive", UpgradeRarity.UNCOMMON, UpgradeType.EXPLOSIVE).description("Explosion radius +30%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("gl_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("gl_armor_piercing", "Armor Piercing Grenade", UpgradeRarity.UNCOMMON, UpgradeType.ARMOR_PIERCING).description("Ignores 30% of target armor").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("gl_speed_grenade", "Speed Grenade", UpgradeRarity.UNCOMMON, UpgradeType.RANGE).description("Grenade flight speed +40%").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("gl_incendiary", "Incendiary Grenade", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Creates a fire zone after explosion for 5 seconds").value(5).build());
        buffs.add(new UpgradeBuff.Builder("gl_cluster", "Cluster Grenade", UpgradeRarity.RARE, UpgradeType.BURST_FIRE).description("Splits into 3 smaller grenades on explosion").value(3).build());
        buffs.add(new UpgradeBuff.Builder("gl_heavy_grenade", "Heavy Grenade", UpgradeRarity.RARE, UpgradeType.DAMAGE).description("Damage +50%, but flight speed -20%").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("gl_tactical", "Tactical Grenade", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("+20% explosion radius and +20% damage").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("gl_shockwave", "Shockwave", UpgradeRarity.EPIC, UpgradeType.KNOCKBACK).description("Knocks enemies back 8 blocks from epicenter").value(8).build());
        buffs.add(new UpgradeBuff.Builder("gl_smoke", "Smoke Grenade", UpgradeRarity.EPIC, UpgradeType.SILENCER).description("Creates a smoke cloud hiding the player and slowing enemies").value(0).build());
        buffs.add(new UpgradeBuff.Builder("gl_flashbang", "Flashbang", UpgradeRarity.EPIC, UpgradeType.ACCURACY).description("Blinds enemies within 5 blocks for 3 seconds").value(3).build());
        buffs.add(new UpgradeBuff.Builder("gl_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("gl_monster", "Grenade Launcher Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +70%, Radius +40%").value(0.7).build());
        buffs.add(new UpgradeBuff.Builder("gl_endless_salvo", "Endless Salvo", UpgradeRarity.LEGENDARY, UpgradeType.UNLIMITED_AMMO).description("15% chance not to consume grenade").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("gl_demolitionist", "Demolitionist", UpgradeRarity.MYTHIC, UpgradeType.STACKING_DAMAGE).description("Each kill gives +2% explosive damage (permanent). Max +100%").value(2).stacking(100).build());
        buffs.add(new UpgradeBuff.Builder("gl_deadly_ricochet", "Deadly Ricochet", UpgradeRarity.MYTHIC, UpgradeType.ACCURACY).description("Grenade can bounce off walls and explode near nearest enemy (10% chance)").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("gl_artillery", "Artillery Strike", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.EXPLOSIVE).description("upgrade.gl_artillery.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:grenadelauncher", buffs);
    }

    private static void registerAUG() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("aug_heavy_barrel", "Heavy Barrel", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("aug_long_mag", "Extended Magazine", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+5 ammo (total 35)").value(5).build());
        buffs.add(new UpgradeBuff.Builder("aug_plastic_stock", "Plastic Stock", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Movement speed with weapon +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("aug_strong_spring", "Strong Spring", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Fire Rate +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("aug_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("aug_holo", "Holographic Sight", UpgradeRarity.UNCOMMON, UpgradeType.ACCURACY).description("Spread -20%").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("aug_magnum", "Magnum Ammo", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("aug_silencer", "Silencer", UpgradeRarity.UNCOMMON, UpgradeType.SILENCER).description("Shot 40% quieter").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("aug_incendiary", "Incendiary Rounds", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Ignites target for 3 seconds").value(3).build());
        buffs.add(new UpgradeBuff.Builder("aug_explosive", "Explosive Rounds", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("Small explosion on impact").value(0).build());
        buffs.add(new UpgradeBuff.Builder("aug_speed_fire", "Speed Fire", UpgradeRarity.RARE, UpgradeType.FIRE_RATE).description("Fire Rate +30%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("aug_adaptive_stock", "Adaptive Stock", UpgradeRarity.RARE, UpgradeType.ACCURACY).description("+20% accuracy when firing bursts").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("aug_fire_storm", "Fire Storm", UpgradeRarity.EPIC, UpgradeType.STACKING_CRITICAL).description("Every 5th shot deals double damage").value(2).build());
        buffs.add(new UpgradeBuff.Builder("aug_tactical", "Tactical AUG", UpgradeRarity.EPIC, UpgradeType.FIRE_RATE).description("+15% movement speed while firing").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("aug_long_burst", "Long Burst", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("Burst of >15 shots deals +20% damage").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("aug_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("aug_monster", "AUG Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +40%, Fire Rate +40%, but -20% accuracy").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("aug_combat", "Combat AUG", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +30%, Accuracy +20%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("aug_austrian_sniper", "Austrian Sniper", UpgradeRarity.MYTHIC, UpgradeType.STACKING_ACCURACY).description("Each kill gives +1% accuracy (permanent). Max +50%").value(1).stacking(50).build());
        buffs.add(new UpgradeBuff.Builder("aug_deadly_queue", "Deadly Queue", UpgradeRarity.MYTHIC, UpgradeType.DAMAGE).description("If you empty all 30 shots into one enemy, last bullet deals +250% damage").value(2.5).build());
        buffs.add(new UpgradeBuff.Builder("aug_legendary", "Legendary AUG", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.DAMAGE).description("upgrade.aug_legendary.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:aug", buffs);
    }

    private static void registerHellBlaster() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("hb_emitter", "Enhanced Emitter", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("hb_big_capacitor", "Big Capacitor", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+2 charges (total 12)").value(2).build());
        buffs.add(new UpgradeBuff.Builder("hb_stabilizer", "Beam Stabilizer", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("Spread -15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("hb_quick_charge", "Quick Charge", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Fire Rate +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("hb_magnum_charge", "Magnum Charge", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("hb_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("hb_focus_lens", "Focus Lens", UpgradeRarity.UNCOMMON, UpgradeType.ACCURACY).description("Spread -20%").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("hb_power_pulse", "Power Pulse", UpgradeRarity.UNCOMMON, UpgradeType.FIRE_RATE).description("Fire Rate +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("hb_hellfire", "Hellfire", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Ignites target for 3 seconds").value(3).build());
        buffs.add(new UpgradeBuff.Builder("hb_explosive_charge", "Explosive Charge", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("Small explosion on impact").value(0).build());
        buffs.add(new UpgradeBuff.Builder("hb_armor_piercing", "Armor Piercing Beam", UpgradeRarity.RARE, UpgradeType.ARMOR_PIERCING).description("Ignores 40% of target armor").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("hb_double_shot", "Double Shot", UpgradeRarity.RARE, UpgradeType.DOUBLE_SHOT).description("15% chance to fire twice (uses 2 charges)").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("hb_hell_pit", "Hell Pit", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("If two shots hit the same target consecutively, second deals +150% damage").value(1.5).build());
        buffs.add(new UpgradeBuff.Builder("hb_demon_speed", "Demon Speed", UpgradeRarity.EPIC, UpgradeType.FIRE_RATE).description("+20% movement speed while firing").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("hb_precise_shot", "Precise Shot", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("First shot after reload is always critical (x1.5 damage)").value(1.5).build());
        buffs.add(new UpgradeBuff.Builder("hb_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("hb_monster", "Blaster Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +50%, Fire Rate +40%").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("hb_endless_fire", "Endless Fire", UpgradeRarity.LEGENDARY, UpgradeType.UNLIMITED_AMMO).description("20% chance not to consume charge").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("hb_lord_of_hell", "Lord of Hell", UpgradeRarity.MYTHIC, UpgradeType.STACKING_DAMAGE).description("Each kill gives +2% fire damage (permanent). Max +100%").value(2).stacking(100).build());
        buffs.add(new UpgradeBuff.Builder("hb_deadly_shot", "Deadly Shot", UpgradeRarity.MYTHIC, UpgradeType.DAMAGE).description("Last charge in magazine deals +300% damage").value(3.0).build());
        buffs.add(new UpgradeBuff.Builder("hb_armageddon", "Armageddon", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.DAMAGE).description("upgrade.hb_armageddon.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:netherblaster", buffs);
    }

    private static void registerBiogun() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("bio_emitter", "Enhanced Barrel", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("bio_big_tank", "Big Tank", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+5 ammo (total 35)").value(5).build());
        buffs.add(new UpgradeBuff.Builder("bio_stabilizer", "Flow Stabilizer", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("Spread -15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("bio_quick_feed", "Quick Feed", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Fire Rate (LMB) +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("bio_corrosive", "Corrosive Mix", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Poison damage +30%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("bio_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("bio_precision", "Precision Sprayer", UpgradeRarity.UNCOMMON, UpgradeType.ACCURACY).description("Spread -20%").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("bio_strong_pump", "Strong Pump", UpgradeRarity.UNCOMMON, UpgradeType.FIRE_RATE).description("Charge speed (RMB) +30%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("bio_acid", "Acid", UpgradeRarity.RARE, UpgradeType.ARMOR_PIERCING).description("Ignores 30% of target armor").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("bio_explosive_acid", "Explosive Acid", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("Small explosion on impact (RMB projectiles)").value(0).build());
        buffs.add(new UpgradeBuff.Builder("bio_slow_action", "Slow Action", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Poison duration +3 seconds").value(3).build());
        buffs.add(new UpgradeBuff.Builder("bio_contagion", "Contagion", UpgradeRarity.RARE, UpgradeType.ACCURACY).description("10% chance to infect nearby enemies (3 block radius)").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("bio_deadly_poison", "Deadly Poison", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("If two shots hit the same target consecutively, second deals +150% poison damage").value(1.5).build());
        buffs.add(new UpgradeBuff.Builder("bio_biochemist", "Biochemist", UpgradeRarity.EPIC, UpgradeType.FIRE_RATE).description("+20% movement speed while firing").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("bio_precise_shot", "Precise Shot", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("First shot after reload deals double poison damage").value(2.0).build());
        buffs.add(new UpgradeBuff.Builder("bio_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("bio_monster", "Biogun Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +50%, Charge speed +40%").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("bio_endless_poison", "Endless Poison", UpgradeRarity.LEGENDARY, UpgradeType.UNLIMITED_AMMO).description("20% chance not to consume ammo").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("bio_lord_disease", "Lord of Disease", UpgradeRarity.MYTHIC, UpgradeType.STACKING_DAMAGE).description("Each kill gives +1% poison damage (permanent). Max +100%").value(1).stacking(100).build());
        buffs.add(new UpgradeBuff.Builder("bio_deadly_infection", "Deadly Infection", UpgradeRarity.MYTHIC, UpgradeType.ACCURACY).description("20% chance to infect nearby enemies on kill for 5 seconds").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("bio_bioapocalypse", "Bioapocalypse", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.DAMAGE).description("upgrade.bio_bioapocalypse.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:biogun", buffs);
    }

    private static void registerTeslaCannon() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("tesla_coil", "Enhanced Coil", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("tesla_big_capacitor", "Big Capacitor", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+5 charges (total 30)").value(5).build());
        buffs.add(new UpgradeBuff.Builder("tesla_stabilizer", "Arc Stabilizer", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("Spread -15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("tesla_quick_charge", "Quick Charge", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Fire Rate +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("tesla_power", "Powerful Discharge", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("tesla_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("tesla_focus_lens", "Focus Lens", UpgradeRarity.UNCOMMON, UpgradeType.ACCURACY).description("Spread -20%").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("tesla_power_pulse", "Power Pulse", UpgradeRarity.UNCOMMON, UpgradeType.FIRE_RATE).description("Fire Rate +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("tesla_chain", "Chain Lightning", UpgradeRarity.RARE, UpgradeType.ACCURACY).description("Electricity jumps to +2 enemies (total 5)").value(2).build());
        buffs.add(new UpgradeBuff.Builder("tesla_explosive_arc", "Explosive Arc", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("10% chance of small explosion on impact").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("tesla_paralyze", "Paralyzing Shock", UpgradeRarity.RARE, UpgradeType.ACCURACY).description("Stuns target for 1 second").value(1).build());
        buffs.add(new UpgradeBuff.Builder("tesla_long_arc", "Long Arc", UpgradeRarity.RARE, UpgradeType.RANGE).description("Arc range +50%").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("tesla_storm", "Storm Discharge", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("If two shots hit the same target consecutively, second deals +150% damage").value(1.5).build());
        buffs.add(new UpgradeBuff.Builder("tesla_electro_field", "Electro Field", UpgradeRarity.EPIC, UpgradeType.FIRE_RATE).description("+20% movement speed while firing").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("tesla_precise_shot", "Precise Shot", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("First shot after reload is always critical (x1.5 damage)").value(1.5).build());
        buffs.add(new UpgradeBuff.Builder("tesla_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("tesla_monster", "Tesla Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +50%, +3 jump targets").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("tesla_endless", "Endless Discharge", UpgradeRarity.LEGENDARY, UpgradeType.UNLIMITED_AMMO).description("20% chance not to consume charge").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("tesla_lord_lightning", "Lord of Lightning", UpgradeRarity.MYTHIC, UpgradeType.STACKING_DAMAGE).description("Each kill gives +1% lightning damage (permanent). Max +100%").value(1).stacking(100).build());
        buffs.add(new UpgradeBuff.Builder("tesla_deadly_chain", "Deadly Chain", UpgradeRarity.MYTHIC, UpgradeType.ACCURACY).description("If electricity kills an enemy, it automatically jumps to the next (chain reaction)").value(0).build());
        buffs.add(new UpgradeBuff.Builder("tesla_thunder", "Thunder and Lightning", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.LIGHTNING).description("upgrade.tesla_thunder.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:teslagun", buffs);
    }

    private static void registerLMG() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("lmg_heavy_barrel", "Heavy Barrel", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("lmg_long_mag", "Extended Magazine", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+20 ammo (total 120)").value(20).build());
        buffs.add(new UpgradeBuff.Builder("lmg_bipod", "Bipod", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("-20% spread when prone").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("lmg_strong_spring", "Strong Spring", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Fire Rate +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("lmg_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("lmg_scope", "Scope", UpgradeRarity.UNCOMMON, UpgradeType.ACCURACY).description("Spread -20%").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("lmg_magnum", "Magnum Ammo", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("lmg_power_bolt", "Power Bolt", UpgradeRarity.UNCOMMON, UpgradeType.FIRE_RATE).description("Fire Rate +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("lmg_incendiary", "Incendiary Rounds", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Ignites target for 3 seconds").value(3).build());
        buffs.add(new UpgradeBuff.Builder("lmg_explosive", "Explosive Rounds", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("Small explosion on impact").value(0).build());
        buffs.add(new UpgradeBuff.Builder("lmg_storm_fire", "Storm Fire", UpgradeRarity.RARE, UpgradeType.FIRE_RATE).description("Fire Rate +40%").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("lmg_heavy_barrel_2", "Heavy Barrel II", UpgradeRarity.RARE, UpgradeType.DAMAGE).description("Damage +30%, but Fire Rate -15%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("lmg_hail", "Hail of Bullets", UpgradeRarity.EPIC, UpgradeType.STACKING_CRITICAL).description("Every 10th shot deals double damage").value(2).build());
        buffs.add(new UpgradeBuff.Builder("lmg_suppression", "Suppression", UpgradeRarity.EPIC, UpgradeType.ACCURACY).description("Enemies within 5 blocks of impact get slowed for 1 second").value(1).build());
        buffs.add(new UpgradeBuff.Builder("lmg_long_burst", "Long Burst", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("Firing more than 30 shots without reloading gives +25% damage").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("lmg_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("lmg_monster", "LMG Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +40%, Fire Rate +50%, but -25% accuracy").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("lmg_combat", "Combat LMG", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +30%, Accuracy +20%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("lmg_machine_gunner", "Machine Gunner", UpgradeRarity.MYTHIC, UpgradeType.STACKING_FIRE_RATE).description("Each kill gives +2% Fire Rate (permanent). Max +100%").value(2).stacking(100).build());
        buffs.add(new UpgradeBuff.Builder("lmg_deadly_queue", "Deadly Queue", UpgradeRarity.MYTHIC, UpgradeType.DAMAGE).description("If you empty all 100 shots into one enemy, last bullet deals +500% damage").value(5.0).build());
        buffs.add(new UpgradeBuff.Builder("lmg_firestorm", "Firestorm", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.FIRE_RATE).description("upgrade.lmg_firestorm.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:lmg", buffs);
    }

    private static void registerMinigun() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("min_heavy_barrel", "Heavy Barrel", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("min_long_mag", "Extended Magazine", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+40 ammo (total 240)").value(40).build());
        buffs.add(new UpgradeBuff.Builder("min_stabilizer", "Stabilizer", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("Spread -15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("min_strong_motor", "Strong Motor", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Spin-up speed +30%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("min_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("min_scope", "Scope", UpgradeRarity.UNCOMMON, UpgradeType.ACCURACY).description("Spread -20%").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("min_magnum", "Magnum Ammo", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("min_power_motor", "Power Motor", UpgradeRarity.UNCOMMON, UpgradeType.FIRE_RATE).description("Max Fire Rate +20%").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("min_incendiary", "Incendiary Rounds", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Ignites target for 3 seconds").value(3).build());
        buffs.add(new UpgradeBuff.Builder("min_explosive", "Explosive Rounds", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("Small explosion on impact").value(0).build());
        buffs.add(new UpgradeBuff.Builder("min_hurricane", "Hurricane Fire", UpgradeRarity.RARE, UpgradeType.FIRE_RATE).description("Max Fire Rate +30%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("min_heavy_barrel_2", "Heavy Barrel II", UpgradeRarity.RARE, UpgradeType.DAMAGE).description("Damage +30%, but Fire Rate -15%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("min_hail_lead", "Hail of Lead", UpgradeRarity.EPIC, UpgradeType.STACKING_CRITICAL).description("Every 10th shot deals double damage").value(2).build());
        buffs.add(new UpgradeBuff.Builder("min_suppression", "Suppression", UpgradeRarity.EPIC, UpgradeType.ACCURACY).description("Enemies within 5 blocks of impact get slowed for 1.5 seconds").value(1.5).build());
        buffs.add(new UpgradeBuff.Builder("min_long_burst", "Long Burst", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("Firing more than 50 shots without reloading gives +25% damage").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("min_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("min_monster", "Minigun Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +40%, Fire Rate +50%, but -25% accuracy").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("min_combat", "Combat Minigun", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +30%, Accuracy +20%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("min_hurricane_death", "Hurricane of Death", UpgradeRarity.MYTHIC, UpgradeType.STACKING_FIRE_RATE).description("Each kill gives +2% Fire Rate (permanent). Max +100%").value(2).stacking(100).build());
        buffs.add(new UpgradeBuff.Builder("min_deadly_storm", "Deadly Storm", UpgradeRarity.MYTHIC, UpgradeType.DAMAGE).description("If you empty all 200 shots into one enemy, last bullet deals +600% damage").value(6.0).build());
        buffs.add(new UpgradeBuff.Builder("min_hurricane_death_ultra", "Hurricane of Death", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.FIRE_RATE).description("upgrade.min_hurricane_death_ultra.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:minigun", buffs);
    }

    private static void registerAS50() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("as50_heavy_barrel", "Heavy Barrel", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("as50_long_barrel", "Long Barrel", UpgradeRarity.COMMON, UpgradeType.RANGE).description("+30% range").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("as50_stock", "Improved Stock", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("Spread -15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("as50_big_mag", "Big Magazine", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+2 ammo (total 12)").value(2).build());
        buffs.add(new UpgradeBuff.Builder("as50_magnum", "Magnum Rounds", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("as50_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("as50_sniper_scope", "Sniper Scope", UpgradeRarity.UNCOMMON, UpgradeType.ACCURACY).description("Spread -25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("as50_fast_bolt", "Fast Bolt", UpgradeRarity.UNCOMMON, UpgradeType.FIRE_RATE).description("Fire Rate +20%").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("as50_incendiary", "Incendiary Rounds", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Ignites target for 3 seconds").value(3).build());
        buffs.add(new UpgradeBuff.Builder("as50_explosive", "Explosive Rounds", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("Small explosion on impact").value(0).build());
        buffs.add(new UpgradeBuff.Builder("as50_armor_piercing", "Armor Piercing Rounds", UpgradeRarity.RARE, UpgradeType.ARMOR_PIERCING).description("Ignore 50% of target armor").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("as50_heavy_bullet", "Heavy Bullet", UpgradeRarity.RARE, UpgradeType.DAMAGE).description("Damage +35%, but Fire Rate -15%").value(0.35).build());
        buffs.add(new UpgradeBuff.Builder("as50_eagle_eye", "Eagle Eye", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("If player hasn't moved for 3 seconds before firing, +40% damage").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("as50_professional", "Professional", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("+30% damage at range >40 blocks").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("as50_silence", "Silence", UpgradeRarity.EPIC, UpgradeType.SILENCER).description("Shot makes no sound").value(1).build());
        buffs.add(new UpgradeBuff.Builder("as50_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("as50_monster", "AS50 Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +60%, Accuracy +30%").value(0.6).build());
        buffs.add(new UpgradeBuff.Builder("as50_long_range", "Long Range", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("+50% damage at range >50 blocks").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("as50_sniper", "Sniper", UpgradeRarity.MYTHIC, UpgradeType.STACKING_DAMAGE).description("Each kill gives +1% damage (permanent). Max +100%").value(1).stacking(100).build());
        buffs.add(new UpgradeBuff.Builder("as50_double_shot", "Double Shot", UpgradeRarity.MYTHIC, UpgradeType.DOUBLE_SHOT).description("20% chance to fire a second shot without consuming ammo").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("as50_annihilator", "Annihilator", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.PIERCING).description("upgrade.as50_annihilator.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:as50", buffs);
    }

    private static void registerVector() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("vec_heavy_barrel", "Heavy Barrel", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("vec_long_mag", "Extended Magazine", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+5 ammo (total 30)").value(5).build());
        buffs.add(new UpgradeBuff.Builder("vec_stabilizer", "Stabilizer", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("Spread -15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("vec_strong_spring", "Strong Spring", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Fire Rate +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("vec_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("vec_holo", "Holographic Sight", UpgradeRarity.UNCOMMON, UpgradeType.ACCURACY).description("Spread -20%").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("vec_magnum", "Magnum Ammo", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("vec_power_bolt", "Power Bolt", UpgradeRarity.UNCOMMON, UpgradeType.FIRE_RATE).description("Fire Rate +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("vec_incendiary", "Incendiary Rounds", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Ignites target for 3 seconds").value(3).build());
        buffs.add(new UpgradeBuff.Builder("vec_explosive", "Explosive Rounds", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("Small explosion on impact").value(0).build());
        buffs.add(new UpgradeBuff.Builder("vec_speed_fire", "Speed Fire", UpgradeRarity.RARE, UpgradeType.FIRE_RATE).description("Fire Rate +35%").value(0.35).build());
        buffs.add(new UpgradeBuff.Builder("vec_supersonic", "Supersonic Shot", UpgradeRarity.RARE, UpgradeType.DAMAGE).description("+20% damage at range >15 blocks").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("vec_fire_storm", "Fire Storm", UpgradeRarity.EPIC, UpgradeType.STACKING_CRITICAL).description("Every 5th shot deals double damage").value(2).build());
        buffs.add(new UpgradeBuff.Builder("vec_tactical", "Tactical Vector", UpgradeRarity.EPIC, UpgradeType.FIRE_RATE).description("+20% movement speed while firing").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("vec_long_burst", "Long Burst", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("Burst of >10 shots deals +20% damage").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("vec_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("vec_monster", "Vector Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +40%, Fire Rate +50%, but -20% accuracy").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("vec_combat", "Combat Vector", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +30%, Accuracy +20%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("vec_speed", "Speed", UpgradeRarity.MYTHIC, UpgradeType.STACKING_FIRE_RATE).description("Each kill gives +2% Fire Rate (permanent). Max +100%").value(2).stacking(100).build());
        buffs.add(new UpgradeBuff.Builder("vec_deadly_queue", "Deadly Queue", UpgradeRarity.MYTHIC, UpgradeType.DAMAGE).description("If you hit a target 10 times in a row without missing, the 11th shot deals +300% damage").value(3.0).build());
        buffs.add(new UpgradeBuff.Builder("vec_death_storm", "Death Storm", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.FIRE_RATE).description("upgrade.vec_death_storm.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:vector", buffs);
    }

    private static void registerSCAR() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("scar_heavy_barrel", "Heavy Barrel", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("scar_long_mag", "Extended Magazine", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+4 ammo (total 24)").value(4).build());
        buffs.add(new UpgradeBuff.Builder("scar_stock", "Tactical Stock", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("Spread -15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("scar_strong_spring", "Strong Spring", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Fire Rate +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("scar_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("scar_scope", "Scope", UpgradeRarity.UNCOMMON, UpgradeType.ACCURACY).description("Spread -20%").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("scar_magnum", "Magnum Ammo", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("scar_power_bolt", "Power Bolt", UpgradeRarity.UNCOMMON, UpgradeType.FIRE_RATE).description("Fire Rate +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("scar_incendiary", "Incendiary Rounds", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Ignites target for 3 seconds").value(3).build());
        buffs.add(new UpgradeBuff.Builder("scar_explosive", "Explosive Rounds", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("Small explosion on impact").value(0).build());
        buffs.add(new UpgradeBuff.Builder("scar_armor_piercing", "Armor Piercing Rounds", UpgradeRarity.RARE, UpgradeType.ARMOR_PIERCING).description("Ignore 30% of target armor").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("scar_heavy_bullet", "Heavy Bullet", UpgradeRarity.RARE, UpgradeType.DAMAGE).description("Damage +30%, but Fire Rate -15%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("scar_fire_storm", "Fire Storm", UpgradeRarity.EPIC, UpgradeType.STACKING_CRITICAL).description("Every 5th shot deals double damage").value(2).build());
        buffs.add(new UpgradeBuff.Builder("scar_tactical", "Tactical SCAR", UpgradeRarity.EPIC, UpgradeType.FIRE_RATE).description("+15% movement speed while firing").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("scar_sharpshooter", "Sharpshooter", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("First shot after reload is always critical (x1.5 damage)").value(1.5).build());
        buffs.add(new UpgradeBuff.Builder("scar_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("scar_monster", "SCAR Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +50%, Fire Rate +30%, but -15% accuracy").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("scar_combat", "Combat SCAR", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +35%, Accuracy +20%").value(0.35).build());
        buffs.add(new UpgradeBuff.Builder("scar_sniper", "Sniper SCAR", UpgradeRarity.MYTHIC, UpgradeType.STACKING_DAMAGE).description("Each kill gives +1.5% damage (permanent). Max +75%").value(1.5).stacking(75).build());
        buffs.add(new UpgradeBuff.Builder("scar_deadly_shot", "Deadly Shot", UpgradeRarity.MYTHIC, UpgradeType.DAMAGE).description("If you kill an enemy with the first shot, next shot deals +300% damage").value(3.0).build());
        buffs.add(new UpgradeBuff.Builder("scar_legendary", "Legendary SCAR", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.DAMAGE).description("upgrade.scar_legendary.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:scar", buffs);
    }

    private static void registerLaserRifle() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("lr_emitter", "Enhanced Emitter", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("lr_big_battery", "Big Battery", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+5 charges (total 50)").value(5).build());
        buffs.add(new UpgradeBuff.Builder("lr_stabilizer", "Beam Stabilizer", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("Spread -15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("lr_quick_charge", "Quick Charge", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Fire Rate +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("lr_power_beam", "Power Beam", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("lr_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("lr_focus_lens", "Focus Lens", UpgradeRarity.UNCOMMON, UpgradeType.ACCURACY).description("Spread -20%").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("lr_power_pulse", "Power Pulse", UpgradeRarity.UNCOMMON, UpgradeType.FIRE_RATE).description("Fire Rate +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("lr_incendiary", "Incendiary Beam", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Ignites target for 3 seconds").value(3).build());
        buffs.add(new UpgradeBuff.Builder("lr_explosive", "Explosive Beam", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("Small explosion on impact").value(0).build());
        buffs.add(new UpgradeBuff.Builder("lr_armor_piercing", "Armor Piercing Beam", UpgradeRarity.RARE, UpgradeType.ARMOR_PIERCING).description("Ignores 40% of target armor").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("lr_double_beam", "Double Beam", UpgradeRarity.RARE, UpgradeType.DOUBLE_SHOT).description("15% chance to fire twice (uses 2 charges)").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("lr_piercing", "Piercing Beam", UpgradeRarity.EPIC, UpgradeType.PIERCING).description("Beam pierces enemies (up to 3 targets)").value(3).build());
        buffs.add(new UpgradeBuff.Builder("lr_shield", "Energy Shield", UpgradeRarity.EPIC, UpgradeType.FIRE_RATE).description("+20% movement speed while firing").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("lr_precise_shot", "Precise Shot", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("First shot after reload is always critical (x1.5 damage)").value(1.5).build());
        buffs.add(new UpgradeBuff.Builder("lr_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("lr_monster", "Laser Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +50%, Fire Rate +30%, Accuracy +20%").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("lr_endless", "Endless Beam", UpgradeRarity.LEGENDARY, UpgradeType.UNLIMITED_AMMO).description("20% chance not to consume charge").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("lr_lord_light", "Lord of Light", UpgradeRarity.MYTHIC, UpgradeType.STACKING_DAMAGE).description("Each kill gives +1% laser damage (permanent). Max +100%").value(1).stacking(100).build());
        buffs.add(new UpgradeBuff.Builder("lr_deadly_beam", "Deadly Beam", UpgradeRarity.MYTHIC, UpgradeType.DAMAGE).description("If target is scoped for >2 seconds, next shot deals +200% damage").value(2.0).build());
        buffs.add(new UpgradeBuff.Builder("lr_doomsday", "Doomsday Device", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.PIERCING).description("upgrade.lr_doomsday.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:lasergun", buffs);
    }

    private static void registerBlasterRifle() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("br_emitter", "Enhanced Emitter", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("br_big_battery", "Big Battery", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+5 charges (total 55)").value(5).build());
        buffs.add(new UpgradeBuff.Builder("br_stabilizer", "Plasma Stabilizer", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("Spread -15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("br_quick_charge", "Quick Charge", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Fire Rate +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("br_power_charge", "Power Charge", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("br_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("br_focus_lens", "Focus Lens", UpgradeRarity.UNCOMMON, UpgradeType.ACCURACY).description("Spread -20%").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("br_power_pulse", "Power Pulse", UpgradeRarity.UNCOMMON, UpgradeType.FIRE_RATE).description("Fire Rate +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("br_incendiary", "Incendiary Plasma", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Ignites target for 3 seconds").value(3).build());
        buffs.add(new UpgradeBuff.Builder("br_explosive", "Explosive Plasma", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("Small explosion on impact").value(0).build());
        buffs.add(new UpgradeBuff.Builder("br_armor_piercing", "Armor Piercing Plasma", UpgradeRarity.RARE, UpgradeType.ARMOR_PIERCING).description("Ignores 30% of target armor").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("br_speed_shot", "Speed Shot", UpgradeRarity.RARE, UpgradeType.FIRE_RATE).description("Fire Rate +35%").value(0.35).build());
        buffs.add(new UpgradeBuff.Builder("br_plasma_storm", "Plasma Storm", UpgradeRarity.EPIC, UpgradeType.STACKING_CRITICAL).description("Every 5th shot deals double damage").value(2).build());
        buffs.add(new UpgradeBuff.Builder("br_mobile", "Mobile Shooter", UpgradeRarity.EPIC, UpgradeType.FIRE_RATE).description("+20% movement speed while firing").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("br_precise_shot", "Precise Shot", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("First shot after reload is always critical (x1.5 damage)").value(1.5).build());
        buffs.add(new UpgradeBuff.Builder("br_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("br_monster", "Blaster Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +40%, Fire Rate +40%, Accuracy +20%").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("br_endless", "Endless Charge", UpgradeRarity.LEGENDARY, UpgradeType.UNLIMITED_AMMO).description("20% chance not to consume charge").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("br_lord_plasma", "Lord of Plasma", UpgradeRarity.MYTHIC, UpgradeType.STACKING_DAMAGE).description("Each kill gives +1% plasma damage (permanent). Max +100%").value(1).stacking(100).build());
        buffs.add(new UpgradeBuff.Builder("br_speed_charge", "Speed Charge", UpgradeRarity.MYTHIC, UpgradeType.DAMAGE).description("Every 3rd shot doesn't consume charge and deals +50% damage").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("br_star_destroyer", "Star Destroyer", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.DAMAGE).description("upgrade.br_star_destroyer.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:blasterrifle", buffs);
    }

    private static void registerBlasterShotgun() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("bs_emitter", "Enhanced Emitter", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("bs_big_battery", "Big Battery", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+5 charges (total 45)").value(5).build());
        buffs.add(new UpgradeBuff.Builder("bs_stabilizer", "Plasma Stabilizer", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("Spread -15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("bs_quick_charge", "Quick Charge", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Fire Rate +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("bs_power_charge", "Power Charge", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("bs_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("bs_focus_lens", "Focus Lens", UpgradeRarity.UNCOMMON, UpgradeType.ACCURACY).description("Spread -20%").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("bs_extra_shot", "Extra Shot", UpgradeRarity.UNCOMMON, UpgradeType.BURST_FIRE).description("+1 plasma pellet (total 6-8)").value(1).build());
        buffs.add(new UpgradeBuff.Builder("bs_incendiary", "Incendiary Plasma", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Ignites target for 3 seconds").value(3).build());
        buffs.add(new UpgradeBuff.Builder("bs_explosive", "Explosive Plasma", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("Each pellet has small explosion on impact").value(0).build());
        buffs.add(new UpgradeBuff.Builder("bs_armor_piercing", "Armor Piercing Plasma", UpgradeRarity.RARE, UpgradeType.ARMOR_PIERCING).description("Pellets ignore 30% of target armor").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("bs_triple_shot", "Triple Shot", UpgradeRarity.RARE, UpgradeType.BURST_FIRE).description("15% chance to fire three times (uses 3 charges)").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("bs_plasma_storm", "Plasma Storm", UpgradeRarity.EPIC, UpgradeType.STACKING_CRITICAL).description("Every 5th shot deals double damage").value(2).build());
        buffs.add(new UpgradeBuff.Builder("bs_point_blank", "Point Blank", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("If fired point-blank (up to 2 blocks), +100% damage").value(1.0).build());
        buffs.add(new UpgradeBuff.Builder("bs_knockback", "Knockback Charge", UpgradeRarity.EPIC, UpgradeType.KNOCKBACK).description("Knocks target back 5 blocks on hit").value(5).build());
        buffs.add(new UpgradeBuff.Builder("bs_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("bs_monster", "Blaster Shotgun Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +50%, +2 pellets, but -20% accuracy").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("bs_endless", "Endless Charge", UpgradeRarity.LEGENDARY, UpgradeType.UNLIMITED_AMMO).description("20% chance not to consume charge").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("bs_destroyer", "Destroyer", UpgradeRarity.MYTHIC, UpgradeType.STACKING_DAMAGE).description("Each kill gives +1.5% plasma damage (permanent). Max +75%").value(1.5).stacking(75).build());
        buffs.add(new UpgradeBuff.Builder("bs_deadly_fan", "Deadly Fan", UpgradeRarity.MYTHIC, UpgradeType.DAMAGE).description("If all pellets hit the same target, each subsequent pellet deals +30% damage").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("bs_plasma_apocalypse", "Plasma Apocalypse", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.BURST_FIRE).description("upgrade.bs_plasma_apocalypse.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:scatterbeamrifle", buffs);
    }

    private static void registerSonicRifle() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("sr_emitter", "Enhanced Resonator", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("sr_big_resonator", "Big Resonator", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+1 charge (total 9)").value(1).build());
        buffs.add(new UpgradeBuff.Builder("sr_stabilizer", "Wave Stabilizer", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("Spread -15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("sr_quick_tune", "Quick Tune", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Fire Rate +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("sr_power_wave", "Power Wave", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("sr_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("sr_focus_emitter", "Focus Emitter", UpgradeRarity.UNCOMMON, UpgradeType.ACCURACY).description("Spread -20%").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("sr_power_pulse", "Power Pulse", UpgradeRarity.UNCOMMON, UpgradeType.FIRE_RATE).description("Fire Rate +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("sr_stun", "Stunning Wave", UpgradeRarity.RARE, UpgradeType.ACCURACY).description("Stun duration +1 second (total 3 seconds)").value(1).build());
        buffs.add(new UpgradeBuff.Builder("sr_explosive", "Explosive Wave", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("Small explosion on impact (+2 block radius)").value(2).build());
        buffs.add(new UpgradeBuff.Builder("sr_piercing", "Piercing Wave", UpgradeRarity.RARE, UpgradeType.PIERCING).description("Sound wave pierces enemies (up to 3 targets)").value(3).build());
        buffs.add(new UpgradeBuff.Builder("sr_double_wave", "Double Wave", UpgradeRarity.RARE, UpgradeType.DOUBLE_SHOT).description("15% chance to fire twice (uses 2 charges)").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("sr_shockwave", "Shockwave", UpgradeRarity.EPIC, UpgradeType.KNOCKBACK).description("Knocks target back 8 blocks on hit").value(8).build());
        buffs.add(new UpgradeBuff.Builder("sr_deadly_resonance", "Deadly Resonance", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("If two shots hit the same target consecutively, second deals +150% damage").value(1.5).build());
        buffs.add(new UpgradeBuff.Builder("sr_silence", "Silence", UpgradeRarity.EPIC, UpgradeType.SILENCER).description("Shot makes no sound").value(1).build());
        buffs.add(new UpgradeBuff.Builder("sr_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("sr_monster", "Sonic Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +50%, Range +50%").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("sr_endless", "Endless Wave", UpgradeRarity.LEGENDARY, UpgradeType.UNLIMITED_AMMO).description("20% chance not to consume charge").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("sr_lord_sound", "Lord of Sound", UpgradeRarity.MYTHIC, UpgradeType.STACKING_DAMAGE).description("Each kill gives +2% sonic damage (permanent). Max +100%").value(2).stacking(100).build());
        buffs.add(new UpgradeBuff.Builder("sr_deadly_resonance_m", "Deadly Resonance", UpgradeRarity.MYTHIC, UpgradeType.DAMAGE).description("If target is already stunned, next shot deals +300% damage").value(3.0).build());
        buffs.add(new UpgradeBuff.Builder("sr_symphony", "Symphony of Destruction", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.EXPLOSIVE).description("upgrade.sr_symphony.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:sonicshotgun", buffs);
    }

    private static void registerP90() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("p90_heavy_barrel", "Heavy Barrel", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("p90_long_mag", "Extended Magazine", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+5 ammo (total 45)").value(5).build());
        buffs.add(new UpgradeBuff.Builder("p90_stock", "Tactical Stock", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("Spread -15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("p90_strong_spring", "Strong Spring", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Fire Rate +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("p90_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("p90_holo", "Holographic Sight", UpgradeRarity.UNCOMMON, UpgradeType.ACCURACY).description("Spread -20%").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("p90_magnum", "Magnum Ammo", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("p90_power_bolt", "Power Bolt", UpgradeRarity.UNCOMMON, UpgradeType.FIRE_RATE).description("Fire Rate +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("p90_incendiary", "Incendiary Rounds", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Ignites target for 3 seconds").value(3).build());
        buffs.add(new UpgradeBuff.Builder("p90_explosive", "Explosive Rounds", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("Small explosion on impact").value(0).build());
        buffs.add(new UpgradeBuff.Builder("p90_speed_fire", "Speed Fire", UpgradeRarity.RARE, UpgradeType.FIRE_RATE).description("Fire Rate +35%").value(0.35).build());
        buffs.add(new UpgradeBuff.Builder("p90_double_burst", "Double Burst", UpgradeRarity.RARE, UpgradeType.DOUBLE_SHOT).description("10% chance to fire 2 bullets at once").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("p90_fire_storm", "Fire Storm", UpgradeRarity.EPIC, UpgradeType.STACKING_CRITICAL).description("Every 5th shot deals double damage").value(2).build());
        buffs.add(new UpgradeBuff.Builder("p90_mobile", "Mobile Shooter", UpgradeRarity.EPIC, UpgradeType.FIRE_RATE).description("+20% movement speed while firing").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("p90_long_burst", "Long Burst", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("Burst of >10 shots deals +20% damage").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("p90_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("p90_monster", "P90 Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +40%, Fire Rate +50%, but -20% accuracy").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("p90_combat", "Combat P90", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +30%, Accuracy +20%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("p90_speed", "Speed", UpgradeRarity.MYTHIC, UpgradeType.STACKING_FIRE_RATE).description("Each kill gives +1.5% Fire Rate (permanent). Max +75%").value(1.5).stacking(75).build());
        buffs.add(new UpgradeBuff.Builder("p90_deadly_mag", "Deadly Magazine", UpgradeRarity.MYTHIC, UpgradeType.DAMAGE).description("If you empty all 40 shots into one enemy, last bullet deals +500% damage").value(5.0).build());
        buffs.add(new UpgradeBuff.Builder("p90_machine_gun", "Machine Gun", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.FIRE_RATE).description("upgrade.p90_machine_gun.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:pdw", buffs);
    }

    private static void registerPulseRifle() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("pul_emitter", "Enhanced Emitter", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("pul_big_battery", "Big Battery", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+4 charges (total 40)").value(4).build());
        buffs.add(new UpgradeBuff.Builder("pul_stabilizer", "Pulse Stabilizer", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("Spread -15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("pul_quick_charge", "Quick Charge", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Fire Rate +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("pul_power", "Power Pulse", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("pul_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("pul_focus_lens", "Focus Lens", UpgradeRarity.UNCOMMON, UpgradeType.ACCURACY).description("Spread -20%").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("pul_strong_capacitor", "Strong Capacitor", UpgradeRarity.UNCOMMON, UpgradeType.FIRE_RATE).description("Fire Rate +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("pul_incendiary", "Incendiary Pulse", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Ignites target for 3 seconds").value(3).build());
        buffs.add(new UpgradeBuff.Builder("pul_explosive", "Explosive Pulse", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("Small explosion (+2 block radius)").value(2).build());
        buffs.add(new UpgradeBuff.Builder("pul_armor_piercing", "Armor Piercing Pulse", UpgradeRarity.RARE, UpgradeType.ARMOR_PIERCING).description("Ignores 35% of target armor").value(0.35).build());
        buffs.add(new UpgradeBuff.Builder("pul_double_pulse", "Double Pulse", UpgradeRarity.RARE, UpgradeType.DOUBLE_SHOT).description("15% chance to fire twice (uses 2 charges)").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("pul_pulse_storm", "Pulse Storm", UpgradeRarity.EPIC, UpgradeType.STACKING_CRITICAL).description("Every 5th shot deals double damage").value(2).build());
        buffs.add(new UpgradeBuff.Builder("pul_shockwave", "Shockwave", UpgradeRarity.EPIC, UpgradeType.KNOCKBACK).description("Knocks target back 6 blocks on hit").value(6).build());
        buffs.add(new UpgradeBuff.Builder("pul_precise_shot", "Precise Shot", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("First shot after reload is always critical (x1.5 damage)").value(1.5).build());
        buffs.add(new UpgradeBuff.Builder("pul_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("pul_monster", "Pulse Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +50%, Fire Rate +30%, Accuracy +20%").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("pul_endless", "Endless Pulse", UpgradeRarity.LEGENDARY, UpgradeType.UNLIMITED_AMMO).description("20% chance not to consume charge").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("pul_lord_pulse", "Lord of Pulse", UpgradeRarity.MYTHIC, UpgradeType.STACKING_DAMAGE).description("Each kill gives +1.5% pulse damage (permanent). Max +75%").value(1.5).stacking(75).build());
        buffs.add(new UpgradeBuff.Builder("pul_deadly_charge", "Deadly Charge", UpgradeRarity.MYTHIC, UpgradeType.DAMAGE).description("If you charge the shot for >1.5 seconds, it deals +200% damage and explodes").value(2.0).build());
        buffs.add(new UpgradeBuff.Builder("pul_pulse_blast", "Pulse Blast", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.EXPLOSIVE).description("upgrade.pul_pulse_blast.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:pulserifle", buffs);
    }

    private static void registerSplitterPistol() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("sp_emitter", "Enhanced Emitter", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("sp_big_battery", "Big Battery", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+3 charges (total 23)").value(3).build());
        buffs.add(new UpgradeBuff.Builder("sp_stabilizer", "Split Stabilizer", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("Spread -15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("sp_quick_charge", "Quick Charge", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Fire Rate +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("sp_power", "Power Charge", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("sp_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("sp_focus_lens", "Focus Lens", UpgradeRarity.UNCOMMON, UpgradeType.ACCURACY).description("Spread -20%").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("sp_extra_shard", "Extra Shard", UpgradeRarity.UNCOMMON, UpgradeType.BURST_FIRE).description("+1 shard on split (total 3-4)").value(1).build());
        buffs.add(new UpgradeBuff.Builder("sp_incendiary", "Incendiary Shards", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Shards ignite target for 3 seconds").value(3).build());
        buffs.add(new UpgradeBuff.Builder("sp_explosive", "Explosive Shards", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("Each shard has small explosion on impact").value(0).build());
        buffs.add(new UpgradeBuff.Builder("sp_armor_piercing", "Armor Piercing Shards", UpgradeRarity.RARE, UpgradeType.ARMOR_PIERCING).description("Shards ignore 25% of target armor").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("sp_cascade", "Cascade Split", UpgradeRarity.RARE, UpgradeType.BURST_FIRE).description("Shards can split again (10% chance)").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("sp_shard_storm", "Shard Storm", UpgradeRarity.EPIC, UpgradeType.BURST_FIRE).description("Every 5th shot creates +2 additional shards").value(2).build());
        buffs.add(new UpgradeBuff.Builder("sp_point_blank", "Point Blank", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("If fired point-blank (up to 3 blocks), all shards hit target, +75% damage").value(0.75).build());
        buffs.add(new UpgradeBuff.Builder("sp_chain_reaction", "Chain Reaction", UpgradeRarity.EPIC, UpgradeType.RANGE).description("Shards fly +3 blocks further on hit").value(3).build());
        buffs.add(new UpgradeBuff.Builder("sp_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("sp_monster", "Splitter Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +40%, +2 shards on split").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("sp_endless", "Endless Shard", UpgradeRarity.LEGENDARY, UpgradeType.UNLIMITED_AMMO).description("20% chance not to consume charge").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("sp_entropy", "Entropy", UpgradeRarity.MYTHIC, UpgradeType.STACKING_DAMAGE).description("Each kill gives +1.5% shard damage (permanent). Max +75%").value(1.5).stacking(75).build());
        buffs.add(new UpgradeBuff.Builder("sp_cascade_death", "Cascade of Death", UpgradeRarity.MYTHIC, UpgradeType.BURST_FIRE).description("If primary shot hits an enemy, shards split 2 more times (up to 12 shards)").value(0).build());
        buffs.add(new UpgradeBuff.Builder("sp_reality_split", "Reality Split", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.BURST_FIRE).description("upgrade.sp_reality_split.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:mibgun", buffs);
    }

    private static void registerPowerFist() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("pf_strong_piston", "Strong Piston", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("pf_heavy_frame", "Heavy Frame", UpgradeRarity.COMMON, UpgradeType.KNOCKBACK).description("Knockback +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("pf_quick_reload", "Quick Reload", UpgradeRarity.COMMON, UpgradeType.RELOAD_SPEED).description("Charge time -15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("pf_stabilizer", "Stabilizer", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Attack speed +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("pf_power", "Power Strike", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("pf_quick_charge", "Quick Charge", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Charge time -25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("pf_kinetic", "Kinetic Accumulator", UpgradeRarity.UNCOMMON, UpgradeType.KNOCKBACK).description("Knockback +50%").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("pf_shockwave", "Shockwave", UpgradeRarity.UNCOMMON, UpgradeType.KNOCKBACK).description("Creates small shockwave on hit (2 block radius)").value(2).build());
        buffs.add(new UpgradeBuff.Builder("pf_fire", "Flaming Fist", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Ignites target for 3 seconds").value(3).build());
        buffs.add(new UpgradeBuff.Builder("pf_explosive", "Explosive Strike", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("Small explosion on hit (3 block radius)").value(3).build());
        buffs.add(new UpgradeBuff.Builder("pf_armor_piercing", "Armor Piercing Fist", UpgradeRarity.RARE, UpgradeType.ARMOR_PIERCING).description("Ignores 40% of target armor").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("pf_double_strike", "Double Strike", UpgradeRarity.RARE, UpgradeType.DOUBLE_SHOT).description("15% chance to strike twice").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("pf_deadly_charge", "Deadly Charge", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("Charged attack deals +100% damage").value(1.0).build());
        buffs.add(new UpgradeBuff.Builder("pf_magnet", "Magnetic Fist", UpgradeRarity.EPIC, UpgradeType.KNOCKBACK).description("Pulls target in before striking (5 block range)").value(5).build());
        buffs.add(new UpgradeBuff.Builder("pf_shockwave_epic", "Shockwave", UpgradeRarity.EPIC, UpgradeType.KNOCKBACK).description("Knocks back all enemies within 4 blocks on hit").value(4).build());
        buffs.add(new UpgradeBuff.Builder("pf_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("pf_monster", "Fist Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +60%, Knockback +50%").value(0.6).build());
        buffs.add(new UpgradeBuff.Builder("pf_endless_charge", "Endless Charge", UpgradeRarity.LEGENDARY, UpgradeType.RELOAD_SPEED).description("Charged attack requires no charge time").value(1).build());
        buffs.add(new UpgradeBuff.Builder("pf_crusher", "Crusher", UpgradeRarity.MYTHIC, UpgradeType.STACKING_DAMAGE).description("Each kill gives +2% damage (permanent). Max +100%").value(2).stacking(100).build());
        buffs.add(new UpgradeBuff.Builder("pf_hell_strike", "Hell Strike", UpgradeRarity.MYTHIC, UpgradeType.EXPLOSIVE).description("15% chance to cause an explosion dealing +200% damage to all enemies within 5 blocks and igniting for 5 seconds").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("pf_fist_of_god", "Fist of God", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.KNOCKBACK).description("upgrade.pf_fist_of_god.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:powerhammer", buffs);
    }

    private static void registerChainsaw() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("chain_heavy_chain", "Heavy Chain", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("chain_big_tank", "Big Tank", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+50 fuel (total 350)").value(50).build());
        buffs.add(new UpgradeBuff.Builder("chain_sharp", "Sharp Edge", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Attack speed (LMB) +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("chain_economy", "Economy Engine", UpgradeRarity.COMMON, UpgradeType.RELOAD_SPEED).description("Fuel consumption in continuous mode -15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("chain_power", "Power Engine", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("chain_turbo", "Turbo Mode", UpgradeRarity.UNCOMMON, UpgradeType.FIRE_RATE).description("Attack speed in continuous mode +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("chain_economy_2", "Economy Mode", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Fuel consumption -25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("chain_double_strike", "Double Strike", UpgradeRarity.UNCOMMON, UpgradeType.DOUBLE_SHOT).description("LMB has 15% chance to deal double damage").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("chain_fire", "Flaming Chain", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Ignites target for 3 seconds").value(3).build());
        buffs.add(new UpgradeBuff.Builder("chain_explosive", "Explosive Chain", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("10% chance of small explosion on hit").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("chain_armor_piercing", "Armor Piercing Chain", UpgradeRarity.RARE, UpgradeType.ARMOR_PIERCING).description("Ignores 30% of target armor").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("chain_bloody", "Bloody Chain", UpgradeRarity.RARE, UpgradeType.RELOAD_SPEED).description("15% chance to restore 20 fuel on kill").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("chain_deadly_vortex", "Deadly Vortex", UpgradeRarity.EPIC, UpgradeType.FIRE_RATE).description("Killing an enemy in continuous mode increases attack speed by +20% for 3 seconds").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("chain_heavy_strike", "Heavy Strike", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("LMB deals +50% damage, but attack speed -15%").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("chain_glutton", "Gluttonous Saw", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("Fuel consumption +50%, but damage +50%").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("chain_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("chain_monster", "Chainsaw Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +50%, Attack Speed +40%, but fuel consumption +30%").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("chain_endless", "Endless Saw", UpgradeRarity.LEGENDARY, UpgradeType.UNLIMITED_AMMO).description("Fuel is not consumed").value(0).build());
        buffs.add(new UpgradeBuff.Builder("chain_butcher", "Butcher", UpgradeRarity.MYTHIC, UpgradeType.STACKING_DAMAGE).description("Each kill gives +2% damage (permanent). Max +100%").value(2).stacking(100).build());
        buffs.add(new UpgradeBuff.Builder("chain_blood_bath", "Blood Bath", UpgradeRarity.MYTHIC, UpgradeType.DAMAGE).description("Killing an enemy in continuous mode has 20% chance to create a blood cloud (damage in 4 block radius)").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("chain_doomsaw", "Doomsaw", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.DAMAGE).description("upgrade.chain_doomsaw.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:chainsaw", buffs);
    }

    private static void registerAtomicDisintegrator() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("ad_emitter", "Enhanced Emitter", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("ad_big_battery", "Big Battery", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+4 charges (total 24)").value(4).build());
        buffs.add(new UpgradeBuff.Builder("ad_stabilizer", "Beam Stabilizer", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("Spread -15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("ad_quick_charge", "Quick Charge", UpgradeRarity.COMMON, UpgradeType.RELOAD_SPEED).description("Charge drain speed -10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("ad_power", "Power Beam", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("ad_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("ad_focus_lens", "Focus Lens", UpgradeRarity.UNCOMMON, UpgradeType.ACCURACY).description("Spread -20%").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("ad_strong_flow", "Strong Flow", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Charge drain speed -20%").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("ad_radioactive", "Radioactive Beam", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Applies radiation effect for 5 seconds").value(5).build());
        buffs.add(new UpgradeBuff.Builder("ad_explosive", "Explosive Beam", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("10% chance of small explosion on impact").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("ad_armor_piercing", "Armor Piercing Beam", UpgradeRarity.RARE, UpgradeType.ARMOR_PIERCING).description("Ignores 35% of target armor").value(0.35).build());
        buffs.add(new UpgradeBuff.Builder("ad_wide_beam", "Wide Beam", UpgradeRarity.RARE, UpgradeType.RANGE).description("Beam width +50%").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("ad_deadly_radiation", "Deadly Radiation", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("The longer the beam hits a target, the more damage it deals (every 2 seconds +10% up to +100%)").value(10).build());
        buffs.add(new UpgradeBuff.Builder("ad_glutton", "Gluttonous Disintegrator", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("Charge drain +50%, but damage +50%").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("ad_chain_beam", "Chain Beam", UpgradeRarity.EPIC, UpgradeType.RANGE).description("Beam can jump to the nearest enemy").value(0).build());
        buffs.add(new UpgradeBuff.Builder("ad_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("ad_monster", "Disintegrator Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +50%, Beam Width +100%").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("ad_endless", "Endless Beam", UpgradeRarity.LEGENDARY, UpgradeType.UNLIMITED_AMMO).description("15% chance not to consume charge").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("ad_lord_radiation", "Lord of Radiation", UpgradeRarity.MYTHIC, UpgradeType.STACKING_DAMAGE).description("Each kill gives +1.5% beam damage (permanent). Max +75%").value(1.5).stacking(75).build());
        buffs.add(new UpgradeBuff.Builder("ad_deadly_heat", "Deadly Heat", UpgradeRarity.MYTHIC, UpgradeType.EXPLOSIVE).description("If the beam hits a target for >5 seconds, the target explodes (+300% damage in 6 block radius)").value(3.0).build());
        buffs.add(new UpgradeBuff.Builder("ad_nuclear_apocalypse", "Nuclear Apocalypse", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.EXPLOSIVE).description("upgrade.ad_nuclear_apocalypse.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:nucleardeathray", buffs);
    }

    private static void registerGaussRifle() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("grf_emitter", "Enhanced Emitter", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("grf_big_battery", "Big Battery", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+1 charge (total 9)").value(1).build());
        buffs.add(new UpgradeBuff.Builder("grf_stabilizer", "Pulse Stabilizer", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("Spread -15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("grf_quick_reload", "Quick Reload", UpgradeRarity.COMMON, UpgradeType.RELOAD_SPEED).description("Reload time -15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("grf_power", "Power Pulse", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("grf_quick_reload_2", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("grf_focus_lens", "Focus Lens", UpgradeRarity.UNCOMMON, UpgradeType.ACCURACY).description("Spread -20%").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("grf_strong_capacitor", "Strong Capacitor", UpgradeRarity.UNCOMMON, UpgradeType.FIRE_RATE).description("Fire Rate +20%").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("grf_incendiary", "Incendiary Shot", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Ignites target for 3 seconds").value(3).build());
        buffs.add(new UpgradeBuff.Builder("grf_explosive", "Explosive Shot", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("Small explosion on impact").value(0).build());
        buffs.add(new UpgradeBuff.Builder("grf_armor_piercing", "Armor Piercing Shot", UpgradeRarity.RARE, UpgradeType.ARMOR_PIERCING).description("Ignores 50% of target armor").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("grf_heavy_bullet", "Heavy Shot", UpgradeRarity.RARE, UpgradeType.DAMAGE).description("Damage +30%, but Fire Rate -15%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("grf_deadly_shot", "Deadly Shot", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("Last charge in magazine deals +250% damage").value(2.5).build());
        buffs.add(new UpgradeBuff.Builder("grf_sniper", "Sniper Shot", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("+40% damage at range >40 blocks").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("grf_silence", "Silence", UpgradeRarity.EPIC, UpgradeType.SILENCER).description("Shot makes no sound").value(1).build());
        buffs.add(new UpgradeBuff.Builder("grf_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("grf_monster", "Gauss Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +60%, Accuracy +30%").value(0.6).build());
        buffs.add(new UpgradeBuff.Builder("grf_endless", "Endless Charge", UpgradeRarity.LEGENDARY, UpgradeType.UNLIMITED_AMMO).description("25% chance not to consume charge").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("grf_lord_pulse", "Lord of Pulse", UpgradeRarity.MYTHIC, UpgradeType.STACKING_DAMAGE).description("Each kill gives +2% damage (permanent). Max +100%").value(2).stacking(100).build());
        buffs.add(new UpgradeBuff.Builder("grf_deadly_shot_m", "Deadly Shot", UpgradeRarity.MYTHIC, UpgradeType.DAMAGE).description("If you kill an enemy with the first shot, next shot deals +300% damage").value(3.0).build());
        buffs.add(new UpgradeBuff.Builder("grf_railgun", "Railgun", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.PIERCING).description("upgrade.grf_railgun.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:gaussrifle", buffs);
    }

    private static void registerLockOnRocket() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("lor_heavy_barrel", "Heavy Barrel", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("lor_long_launcher", "Extended Launcher", UpgradeRarity.COMMON, UpgradeType.RANGE).description("Rocket flight range +30%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("lor_light_frame", "Light Frame", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Movement speed with weapon +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("lor_scope", "Scope", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("Lock-on time -15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("lor_high_explosive", "High Explosive", UpgradeRarity.UNCOMMON, UpgradeType.EXPLOSIVE).description("Explosion radius +30%").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("lor_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("lor_armor_piercing", "Armor Piercing Rocket", UpgradeRarity.UNCOMMON, UpgradeType.ARMOR_PIERCING).description("Ignores 40% of target armor").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("lor_speed_rocket", "Speed Rocket", UpgradeRarity.UNCOMMON, UpgradeType.RANGE).description("Rocket flight speed +40%").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("lor_incendiary", "Incendiary Rocket", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Creates a fire zone after explosion for 5 seconds").value(5).build());
        buffs.add(new UpgradeBuff.Builder("lor_cluster", "Cluster Rocket", UpgradeRarity.RARE, UpgradeType.BURST_FIRE).description("Splits into 3 smaller rockets on explosion").value(3).build());
        buffs.add(new UpgradeBuff.Builder("lor_heavy_rocket", "Heavy Rocket", UpgradeRarity.RARE, UpgradeType.DAMAGE).description("Damage +50%, but flight speed -20%").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("lor_tactical", "Tactical Rocket", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("+20% explosion radius and +20% damage").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("lor_nuclear", "Nuclear Charge", UpgradeRarity.EPIC, UpgradeType.EXPLOSIVE).description("Explosion radius +80%, Damage +60%").value(0.8).build());
        buffs.add(new UpgradeBuff.Builder("lor_shockwave", "Shockwave", UpgradeRarity.EPIC, UpgradeType.KNOCKBACK).description("Knocks enemies back 10 blocks from epicenter").value(10).build());
        buffs.add(new UpgradeBuff.Builder("lor_fast_lock", "Fast Lock", UpgradeRarity.EPIC, UpgradeType.ACCURACY).description("Lock-on time -50%").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("lor_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("lor_monster", "Rocket Launcher Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +80%, Radius +50%").value(0.8).build());
        buffs.add(new UpgradeBuff.Builder("lor_endless_salvo", "Endless Salvo", UpgradeRarity.LEGENDARY, UpgradeType.UNLIMITED_AMMO).description("15% chance not to consume rocket").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("lor_hunter", "Hunter", UpgradeRarity.MYTHIC, UpgradeType.STACKING_DAMAGE).description("Each kill gives +2% rocket damage (permanent). Max +100%").value(2).stacking(100).build());
        buffs.add(new UpgradeBuff.Builder("lor_deadly_lock", "Deadly Lock", UpgradeRarity.MYTHIC, UpgradeType.ACCURACY).description("If you kill an enemy with lock-on, next shot has 100% instant lock-on").value(1).build());
        buffs.add(new UpgradeBuff.Builder("lor_armageddon", "Armageddon", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.EXPLOSIVE).description("upgrade.lor_armageddon.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:guidedmissilelauncher", buffs);
    }

    private static void registerDrill() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("drill_strong_motor", "Strong Motor", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("drill_sharp", "Sharp Tip", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Attack speed (LMB) +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("drill_economy", "Economy Mode", UpgradeRarity.COMMON, UpgradeType.RELOAD_SPEED).description("Fuel consumption in continuous mode -15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("drill_big_tank", "Big Tank", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+25% max fuel").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("drill_power", "Power Motor", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("drill_turbo", "Turbo Mode", UpgradeRarity.UNCOMMON, UpgradeType.FIRE_RATE).description("Attack speed in continuous mode +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("drill_diamond", "Diamond Tip", UpgradeRarity.UNCOMMON, UpgradeType.ACCURACY).description("Mining blocks 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("drill_double_strike", "Double Strike", UpgradeRarity.UNCOMMON, UpgradeType.DOUBLE_SHOT).description("LMB has 15% chance to deal double damage").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("drill_fire", "Flaming Drill", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Ignites target for 3 seconds").value(3).build());
        buffs.add(new UpgradeBuff.Builder("drill_explosive", "Explosive Drill", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("10% chance of small explosion on hit").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("drill_armor_piercing", "Armor Piercing Drill", UpgradeRarity.RARE, UpgradeType.ARMOR_PIERCING).description("Ignores 30% of target armor").value(0.3).build());
        buffs.add(new UpgradeBuff.Builder("drill_wide", "Wide Drill", UpgradeRarity.RARE, UpgradeType.RANGE).description("Mining radius increases to 5x5").value(5).build());
        buffs.add(new UpgradeBuff.Builder("drill_deadly_vortex", "Deadly Vortex", UpgradeRarity.EPIC, UpgradeType.FIRE_RATE).description("Killing an enemy in continuous mode increases attack speed by +20% for 3 seconds").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("drill_heavy_strike", "Heavy Strike", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("LMB deals +50% damage, but attack speed -15%").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("drill_glutton", "Gluttonous Drill", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("Fuel consumption +50%, but damage +50%").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("drill_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("drill_monster", "Drill Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +50%, Attack Speed +40%, but fuel consumption +30%").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("drill_endless", "Endless Drill", UpgradeRarity.LEGENDARY, UpgradeType.UNLIMITED_AMMO).description("Fuel is not consumed").value(0).build());
        buffs.add(new UpgradeBuff.Builder("drill_miner", "Miner", UpgradeRarity.MYTHIC, UpgradeType.STACKING_DAMAGE).description("Each kill gives +2% damage (permanent). Max +100%").value(2).stacking(100).build());
        buffs.add(new UpgradeBuff.Builder("drill_deadly_drilling", "Deadly Drilling", UpgradeRarity.MYTHIC, UpgradeType.KNOCKBACK).description("Killing an enemy in continuous mode has 20% chance to cause an underground tremor (damage and knockback in 5 block radius)").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("drill_doomsday", "Doomsday Drill", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.KNOCKBACK).description("upgrade.drill_doomsday.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:miningdrill", buffs);
    }

    private static void registerBFG10K() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("bfg_emitter", "Enhanced Emitter", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("bfg_big_battery", "Big Battery", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+3 charges (total 23)").value(3).build());
        buffs.add(new UpgradeBuff.Builder("bfg_stabilizer", "Projectile Stabilizer", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("Spread -15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("bfg_quick_charge", "Quick Charge", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("RMB charge speed +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("bfg_power", "Power Projectile", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("bfg_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("bfg_focus_lens", "Focus Lens", UpgradeRarity.UNCOMMON, UpgradeType.ACCURACY).description("Spread -20%").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("bfg_strong_flow", "Strong Flow", UpgradeRarity.UNCOMMON, UpgradeType.FIRE_RATE).description("LMB Fire Rate +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("bfg_radioactive", "Radioactive Projectile", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Applies radiation for 5 seconds").value(5).build());
        buffs.add(new UpgradeBuff.Builder("bfg_explosive", "Explosive Projectile", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("Small projectiles have small explosion").value(0).build());
        buffs.add(new UpgradeBuff.Builder("bfg_armor_piercing", "Armor Piercing Projectile", UpgradeRarity.RARE, UpgradeType.ARMOR_PIERCING).description("Ignores 35% of target armor").value(0.35).build());
        buffs.add(new UpgradeBuff.Builder("bfg_double_shot", "Double Shot", UpgradeRarity.RARE, UpgradeType.DOUBLE_SHOT).description("LMB has 10% chance to fire 2 projectiles").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("bfg_nuclear", "Nuclear Charge", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("RMB charged projectile deals +150% damage and +50% radius").value(1.5).build());
        buffs.add(new UpgradeBuff.Builder("bfg_shockwave", "Shockwave", UpgradeRarity.EPIC, UpgradeType.KNOCKBACK).description("Large projectile knocks enemies back 10 blocks on hit").value(10).build());
        buffs.add(new UpgradeBuff.Builder("bfg_economy", "Economy Mode", UpgradeRarity.EPIC, UpgradeType.RELOAD_SPEED).description("LMB charge drain -20%").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("bfg_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("bfg_monster", "BFG Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +50%, Explosion Radius +50%").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("bfg_endless", "Endless Charge", UpgradeRarity.LEGENDARY, UpgradeType.UNLIMITED_AMMO).description("15% chance not to consume charge").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("bfg_destroyer", "Destroyer", UpgradeRarity.MYTHIC, UpgradeType.STACKING_DAMAGE).description("Each kill gives +1.5% nuclear damage (permanent). Max +75%").value(1.5).stacking(75).build());
        buffs.add(new UpgradeBuff.Builder("bfg_deadly_charge", "Deadly Charge", UpgradeRarity.MYTHIC, UpgradeType.DAMAGE).description("Fully charged RMB projectile has 20% chance to deal +500% damage and create a nuclear mushroom in 15 block radius").value(5.0).build());
        buffs.add(new UpgradeBuff.Builder("bfg_apocalypse", "Apocalypse", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.EXPLOSIVE).description("upgrade.bfg_apocalypse.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:tfg", buffs);
    }

    private static void registerLaserPistol() {
        List<UpgradeBuff> buffs = new ArrayList<>();
        buffs.add(new UpgradeBuff.Builder("lp_emitter", "Enhanced Emitter", UpgradeRarity.COMMON, UpgradeType.DAMAGE).description("Damage +10%").value(0.1).build());
        buffs.add(new UpgradeBuff.Builder("lp_big_battery", "Big Battery", UpgradeRarity.COMMON, UpgradeType.MAGAZINE_SIZE).description("+3 charges (total 23)").value(3).build());
        buffs.add(new UpgradeBuff.Builder("lp_stabilizer", "Beam Stabilizer", UpgradeRarity.COMMON, UpgradeType.ACCURACY).description("Spread -15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("lp_quick_charge", "Quick Charge", UpgradeRarity.COMMON, UpgradeType.FIRE_RATE).description("Fire Rate +15%").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("lp_power", "Power Beam", UpgradeRarity.UNCOMMON, UpgradeType.DAMAGE).description("Damage +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("lp_quick_reload", "Quick Reload", UpgradeRarity.UNCOMMON, UpgradeType.RELOAD_SPEED).description("Reload 1.5x faster").value(0.5).build());
        buffs.add(new UpgradeBuff.Builder("lp_focus_lens", "Focus Lens", UpgradeRarity.UNCOMMON, UpgradeType.ACCURACY).description("Spread -20%").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("lp_power_pulse", "Power Pulse", UpgradeRarity.UNCOMMON, UpgradeType.FIRE_RATE).description("Fire Rate +25%").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("lp_incendiary", "Incendiary Beam", UpgradeRarity.RARE, UpgradeType.INCENDIARY).description("Ignites target for 3 seconds").value(3).build());
        buffs.add(new UpgradeBuff.Builder("lp_explosive", "Explosive Beam", UpgradeRarity.RARE, UpgradeType.EXPLOSIVE).description("Small explosion on impact").value(0).build());
        buffs.add(new UpgradeBuff.Builder("lp_armor_piercing", "Armor Piercing Beam", UpgradeRarity.RARE, UpgradeType.ARMOR_PIERCING).description("Ignores 25% of target armor").value(0.25).build());
        buffs.add(new UpgradeBuff.Builder("lp_double_beam", "Double Beam", UpgradeRarity.RARE, UpgradeType.DOUBLE_SHOT).description("15% chance to fire twice (uses 2 charges)").value(0.15).build());
        buffs.add(new UpgradeBuff.Builder("lp_deadly_double", "Deadly Double", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("If two shots hit the same target consecutively, second deals +150% damage").value(1.5).build());
        buffs.add(new UpgradeBuff.Builder("lp_mobile", "Mobile Shooter", UpgradeRarity.EPIC, UpgradeType.FIRE_RATE).description("+20% movement speed while firing").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("lp_precise_shot", "Precise Shot", UpgradeRarity.EPIC, UpgradeType.DAMAGE).description("First shot after reload is always critical (x1.5 damage)").value(1.5).build());
        buffs.add(new UpgradeBuff.Builder("lp_blood_harvest", "Blood Harvest", UpgradeRarity.LEGENDARY, UpgradeType.VAMPIRE).description("Each kill restores 2 hearts").value(2).build());
        buffs.add(new UpgradeBuff.Builder("lp_monster", "Laser Monster", UpgradeRarity.LEGENDARY, UpgradeType.DAMAGE).description("Damage +40%, Fire Rate +40%, Accuracy +20%").value(0.4).build());
        buffs.add(new UpgradeBuff.Builder("lp_endless", "Endless Beam", UpgradeRarity.LEGENDARY, UpgradeType.UNLIMITED_AMMO).description("20% chance not to consume charge").value(0.2).build());
        buffs.add(new UpgradeBuff.Builder("lp_lord_light", "Lord of Light", UpgradeRarity.MYTHIC, UpgradeType.STACKING_ACCURACY).description("Each kill gives +2% accuracy (permanent). Max +50%").value(2).stacking(50).build());
        buffs.add(new UpgradeBuff.Builder("lp_deadly_beam", "Deadly Beam", UpgradeRarity.MYTHIC, UpgradeType.DAMAGE).description("If you hit a critical zone/headshot, the shot deals +300% damage and creates a shockwave").value(3.0).build());
        buffs.add(new UpgradeBuff.Builder("lp_light_of_doom", "Light of Doom", UpgradeRarity.ULTRA_MYTHIC, UpgradeType.PIERCING).description("upgrade.lp_light_of_doom.desc").value(0).build());
        WEAPON_UPGRADES.put("techguns:laserpistol", buffs);
    }
}
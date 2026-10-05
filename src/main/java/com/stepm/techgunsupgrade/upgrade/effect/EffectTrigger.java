package com.stepm.techgunsupgrade.upgrade.effect;

/** The gameplay moment at which an upgrade effect is evaluated. */
public enum EffectTrigger {
    PASSIVE,
    BEFORE_SHOT,
    DAMAGE_CALCULATION,
    AMMO_CONSUMPTION,
    WHILE_HELD,
    MELEE_ATTACK,
    CHARGE,
    LOCK_ON,
    PROJECTILE_SPAWN,
    KILL,
    DAMAGE_OVER_TIME,
    BLOCK_IMPACT,
    BLOCK_BREAK
}

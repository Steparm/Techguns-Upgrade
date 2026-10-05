package com.stepm.techgunsupgrade.upgrade.effect;

import com.stepm.techgunsupgrade.upgrade.UpgradeRarity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Complete metadata and executable effects for one upgrade ID. */
public final class UpgradeDefinition {
    private final String id;
    private final String weaponId;
    private final UpgradeRarity rarity;
    private final String description;
    private final List<EffectSpec> effects;

    public UpgradeDefinition(String id, String weaponId, UpgradeRarity rarity,
                             String description, List<EffectSpec> effects) {
        this.id = id;
        this.weaponId = weaponId;
        this.rarity = rarity;
        this.description = description;
        this.effects = Collections.unmodifiableList(new ArrayList<>(effects));
    }

    public String getId() { return id; }
    public String getWeaponId() { return weaponId; }
    public UpgradeRarity getRarity() { return rarity; }
    public String getDescription() { return description; }
    public List<EffectSpec> getEffects() { return effects; }
}

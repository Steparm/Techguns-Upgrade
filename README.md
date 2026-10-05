# Techguns Upgrade

An addon for **Techguns Community Edition** on Minecraft 1.12.2. The mod adds an **Upgrade Station** where weapons receive up to two random modifications. The catalog contains **839 upgrades** for **40 weapons** and **1 106 individual executable effects**.

**Current version: 1.0.0.0**

## ✨ Features

- **839 unique upgrades** across 40 Techguns weapons
- **7 rarity tiers**: Common, Uncommon, Rare, Epic, Legendary, Mythic, Ultra-Mythic
- **Upgrade Station** with roll-based progression
- **6 ticket tiers** (Iron → Creative)
- **Ultra-Mythic effects** with nuclear explosions, lightning, plasma rain, and artillery
- **Configurable balance** — adjust the strength of every buff in the config
- **Full localization**: EN, RU, ZH

## 📦 Installation

Required mods (client and server must match):

1. **Minecraft Forge 1.12.2** — version 14.23.5.2861 or newer
2. **Techguns Community Edition** — version 2.2.0.1 or newer
3. **MixinBooter** — version 11.13 or newer
4. **Techguns Upgrade** — the JAR from Releases or CurseForge

Place all four JARs into the `mods` folder. Source code is not needed to run the game.

## 🎮 Getting Started

1. Craft the **Upgrade Station** (see JEI for the recipe).
2. Craft an **Iron Ticket** to start.
3. Place the Upgrade Station, right-click it.
4. Put a Techguns weapon in the top-left slot.
5. Put a ticket in the bottom-left slot.
6. Pull the lever — roll for a random upgrade.
7. The upgraded weapon appears in the output slot. Up to 2 upgrades per weapon.

## ⚙️ Configuration

The strength of every upgrade can be adjusted in `config/techgunsupgrade.cfg`:

- **Global multiplier** for all buffs
- **Separate multipliers** for damage and fire rate
- **Per-upgrade overrides** (rarity, value, max stack) in `config/techgunsupgrade/upgrades.cfg`

Default is set to **0.5×** for a more balanced gameplay experience — change it to **1.0** for vanilla-like upgrade strength.

## 🛠️ Commands

Debug and testing commands for pack developers and server owners (operator level 2+):

- `/tgu list [rarity] [page]` — list all upgrades
- `/tgu find <text> [page]` — search by ID, name, or description
- `/tgu give <upgrade_id>` — receive a compatible weapon
- `/tgu apply <upgrade_id>` — apply an upgrade to held weapon
- `/tgu inspect` — calculated weapon stats
- `/tgu clear` — remove all upgrades from held weapon
- `/tgu verify` — full audit of 839 upgrades and 8380 pairs
- `/tgu stress start [5..60]` — server-side stress test
- `/tgu safe on|off` — prevent block destruction from upgrade effects
- `/tgu force on|off` — force all random effects to trigger

See [README_TESTING.md](README_TESTING.md) for the complete testing guide.

## 📋 Requirements

| Component | Minimum | Recommended |
|-----------|---------|-------------|
| Minecraft | 1.12.2 | 1.12.2 |
| Forge | 14.23.5.2861 | latest 14.23.5.x |
| Techguns | 2.2.0.1 | latest 2.2.0.x |
| MixinBooter | 11.17 | latest 11.x |

## 🔧 Building from Source

Gradle runs on Java 25, while the mod code and tests are compiled with a Java 8 toolchain:

```bash
git clone https://github.com/Steparm-tesst/techguns-upgrade.git
cd techguns-upgrade
./gradlew clean build

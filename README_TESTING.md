# Testing Techguns Upgrade Station 1.0.0

The `/tgu` command is designed for developing and verifying all 839 upgrades. By default, it's only available to server operators with permission level 2 or higher. In singleplayer, you need to open the world to LAN with cheats enabled, or enable cheats in the world beforehand.

## Quick test scenario

1. Find the upgrade you need: `/tgu find apocalypse`
2. Get a compatible weapon: `/tgu give bfg_apocalypse`
3. View real calculated stats: `/tgu inspect`
4. Protect the test world from destruction: `/tgu safe on`
5. Create five targets with 200 health: `/tgu dummy 5 200`
6. Force rare effects to trigger: `/tgu force on`
7. After testing, run `/tgu force off`, `/tgu safe off`, and `/tgu dummy clear`

Before manually testing the whole build, run `/tgu verify`. It automatically checks all 839 upgrades and 8380 possible pairs on real Techguns items. Expected result: `Weapons: 40`, `Upgrades: 839`, `Pairs: 8380`, `Errors: 0`.

## All commands

- `/tgu help` — in-game help
- `/tgu list [rarity] [page]` — paginated list. Rarities: `common`, `uncommon`, `rare`, `epic`, `legendary`, `mythic`, `ultra_mythic`
- `/tgu find <text> [page]` — search by ID, name, description, or weapon ID
- `/tgu give <upgrade_id>` — find the weapon bound to the effect, install the upgrade, and give a ready-made item
- `/tgu apply <upgrade_id>` — add an effect to the weapon in your main hand. Checks compatibility and the two-upgrade limit
- `/tgu inspect` — show installed effects, NBT activity, infinite ammo flag, and "base → final" pairs for damage, fire rate, spread, range, magazine, reload, penetration, and projectile count
- `/tgu nbt [page]` — show calculated runtime fields under `techgunsupgrade`
- `/tgu clear` — remove all upgrades and their runtime state from the held weapon
- `/tgu stacks set <value>` — set progress for all stacking Mythic effects, capped by each effect's own maximum
- `/tgu stacks reset` — reset Mythic stacks
- `/tgu runtime reset` — reset shot counters, intervals, internal cooldowns, and accumulated ammo cost remainder
- `/tgu force on|off|status` — enable, disable, or check forced triggering of every nonzero random effect
- `/tgu safe on|off|status` — allow or prevent block destruction from TGU explosions and extra projectiles
- `/tgu dummy [1..20] [1..10000]` — create 1 to 20 stationary zombies with the given health
- `/tgu dummy clear` — remove test targets within 128 blocks
- `/tgu visuals` — send a reference electric arc, EMP, and golden flash from server to client
- `/tgu verify` — full automatic server-side audit of all single upgrades and all valid pairs
- `/tgu status` — show current global test settings and the item in your main hand

## Important notes

- `force on` forcibly enables not just useful chances, but also random penalties, self-damage, and in-hand explosions. Use it in a separate test world only.
- `safe on` applies to TechgunsUpgrade explosions and extra projectiles. Original Techguns weapon block destruction is not modified.
- `force` and `safe` are temporary global settings for the current server. They are not saved to the world and always reset to `OFF` after restart.
- Commands `give`, `apply`, `inspect`, `nbt`, `clear`, `stacks`, `runtime`, and `dummy` require a player and cannot run from the server console. `list`, `find`, `force`, `safe`, and `status` can be used from the console.
- `/tgu verify` can be run both from the game and from the server console. Verification is synchronous; on the tested build it took about 0.15 seconds.

## What still needs manual testing

The automatic audit fully covers registration, application, stats, and NBT combinations. In the client, you still need to visually and in real combat test tooltip display, particles, sound, extra projectile trajectories, damage zones, homing, block destruction, and balance feel. For rare triggers, use `force on`, and to protect the test world, use `safe on`.

### Manual test checklist

**Safe mode and nuclear effects**

1. Run `/tgu safe on`, `/tgu force on`, then give Nuclear Death Ray with Apocalypse and detonate it near test targets.
2. Charge-up, sound, flash, shockwaves, nuclear mushroom, damage, and knockback should all be preserved. No block should disappear, no crater should form.
3. Enable safe mode during a Nuclear Death Ray charge: the detonation should still complete with visuals and damage, but without block destruction.
4. Test a regular Techguns explosion or TNT with `/tgu safe on`: entities take damage, particles and sound are visible, block list remains untouched.
5. Run `/tgu safe off` and repeat the test: block destruction is allowed again.

**Visual effects on a dedicated server**

1. Remove all old TechgunsUpgrade JARs from both server and client, then place the same `techgunsupgrade-1.0.0.jar` in both `mods` folders.
2. Connect to the server and run `/tgu visuals`. An electric arc, EMP, and golden flash should all appear in front of the player.
3. Run `/tgu force on`, give a weapon with a visual effect via `/tgu give <upgrade_id>`, and have another player nearby check: both clients should see the effect.
4. Check `logs/latest.log` on client and server: the registration line should contain `start=0, visual=1, reset=2`; no packet decoding errors should appear.

**Cluster grenade and small arms**

1. Give `gl_cluster`, add `gl_artillery`, enable `/tgu force on`, and fire several times in open terrain. After the main hit, exactly three small grenades should appear; they should not travel beyond four blocks and should explode within eight ticks.
2. Repeat the sequence inside a building and in open terrain. In both cases, the integrated server should stay responsive, with no long-flying or bouncing child grenades.
3. Test the Homemade Gun in survival: with an empty magazine, Big Magazine pulls two stone rounds and loads two; Triple Shot fires three projectiles and consumes four rounds total.
4. In the Upgrade Table, test the `RESET` button, then inspect the model from top, bottom, and sides. No texture flickering should appear.
5. Run `/tgu safe on` and trigger a nuclear upgrade: the large explosion, crater, and mushroom should not appear.

**Nuclear charge animation**

1. Run `/tgu force on`, `/tgu safe off`, then `/tgu give ad_nuclear_apocalypse` and kill a target near other dummies. For about 0.6 seconds only collapsing rings and inward particles should be visible; until the charge completes, nearby targets take no nuclear damage and no crater forms.
2. After the charge completes, damage, instant crater, and one low-pitched sound should occur together. Then flash, shockwaves, debris, column, and mushroom cap should develop over about 1.5 seconds. Other game sounds should not disappear after several repeats.
3. With `/tgu safe on`, repeat the explosion: charge-up and all visuals are preserved, but blocks are not destroyed. In `config/techgunsupgrade.cfg` you can check the delay with values `6..30` ticks.
4. Run `/tgu give chain_doomsaw` and check Blood Rain: high dark-red rain, splashes, and two rings with no rainbow dots. Use `/tgu find` to check Acid Cloud, EMP, Sonic, Light, Vortex, Fire Rain, Plasma Rain, Artillery, Freeze, Legendary, and Death Scythe — silhouettes should clearly differ from each other.
5. In video settings, select `All`, `Decreased`, and `Minimal` particles in sequence. Effects should become less dense but not disappear entirely. With multiple simultaneous zones, client and integrated server should stay responsive.
6. Run `/tgu stress start 60`: final TPS should be at least 18, no `Can't keep up`, no OpenAL errors, and no uncontrolled entity growth in the log.

**Ammo consumption and infinite magazines**

1. Give a regular weapon with a bottomless magazine, e.g. `/tgu give akm_immortal`, and put compatible magazines in the inventory.
2. Switch to survival (`/gamemode 0`): creative mode does not consume ammo by default Techguns rules.
3. Fire beyond the original magazine size. There should be no reload, and compatible ammo in the inventory should decrease. Without spare ammo, the weapon should stop after consuming the already loaded remainder.
4. Test a charged energy weapon the same way: energy charges should be consumed without a reload animation.
5. For the Chainsaw, run `/tgu give chain_doomsaw`, `/tgu force on`, `/tgu dummy 5 200` and kill targets. Blood Rain should be visible as a high stream of red falling particles with splashes.
6. In survival, test the Flamethrower, Chainsaw, and Drill with infinite fuel: the fuel reserve should not decrease.

**Regression checks**

1. Run `/tgu force on` and `/tgu safe on`.
2. Test Rare effects with extra-shot chance on regular, beam, plasma, and charged weapons.
3. Test permanent `+N projectiles`: each new projectile should keep the native type, model, and behavior of the weapon.
4. Test `tesla_chain`: the shot should gain extra bounces but must not create weather lightning.
5. Test `ad_nuclear_apocalypse`: after the affected target dies, a nuclear explosion and visible mushroom should appear.
6. Test Acid Cloud and other persistent zones: particles should be visible throughout the zone's lifetime, and damage should apply once per second.
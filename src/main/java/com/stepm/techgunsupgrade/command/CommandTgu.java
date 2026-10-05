package com.stepm.techgunsupgrade.command;

import com.stepm.techgunsupgrade.debug.DebugSettings;
import com.stepm.techgunsupgrade.debug.TguStressHarness;
import com.stepm.techgunsupgrade.debug.UpgradeVerifier;
import com.stepm.techgunsupgrade.TechgunsUpgradeMod;
import com.stepm.techgunsupgrade.event.UpgradeVisualEffects;
import com.stepm.techgunsupgrade.manager.UpgradeApplicator;
import com.stepm.techgunsupgrade.upgrade.GunStatModifiers;
import com.stepm.techgunsupgrade.upgrade.UpgradeBuff;
import com.stepm.techgunsupgrade.upgrade.UpgradeData;
import com.stepm.techgunsupgrade.upgrade.UpgradeRarity;
import com.stepm.techgunsupgrade.upgrade.WeaponUpgrades;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import techguns.TGPackets;
import techguns.capabilities.TGExtendedPlayer;
import techguns.items.guns.GenericGun;
import techguns.packets.PacketTGExtendedPlayerSync;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Operator-only utilities for deterministic gameplay verification. */
public final class CommandTgu extends CommandBase {

    private static final int PAGE_SIZE = 8;
    private static final String[] SUBCOMMANDS = {
            "help", "list", "find", "give", "apply", "inspect", "nbt", "clear",
            "stacks", "runtime", "force", "safe", "dummy", "visuals", "verify", "stress", "status"
    };

    @Override
    public String getName() {
        return "tgu";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/tgu help";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
            throws CommandException {
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            showHelp(sender);
            return;
        }

        String action = args[0].toLowerCase(Locale.ROOT);
        switch (action) {
            case "list":
                list(sender, args);
                break;
            case "find":
                find(sender, args);
                break;
            case "give":
                give(sender, args);
                break;
            case "apply":
                apply(sender, args);
                break;
            case "inspect":
                inspect(sender);
                break;
            case "nbt":
                nbt(sender, args);
                break;
            case "clear":
                clear(sender);
                break;
            case "stacks":
                stacks(sender, args);
                break;
            case "runtime":
                runtime(sender, args);
                break;
            case "force":
                force(sender, args);
                break;
            case "safe":
                safe(server, sender, args);
                break;
            case "dummy":
                dummy(sender, args);
                break;
            case "visuals":
                visuals(sender);
                break;
            case "verify":
                verify(sender);
                break;
            case "stress":
                stress(server, sender, args);
                break;
            case "status":
                status(sender);
                break;
            default:
                throw new WrongUsageException(getUsage(sender));
        }
    }

    private static void showHelp(ICommandSender sender) {
        title(sender, "Techguns Upgrade Test Commands");
        line(sender, "/tgu list [rarity] [page] — list upgrades");
        line(sender, "/tgu find <text> [page] — search by ID, name, or description");
        line(sender, "/tgu give <upgrade_id> — give matching weapon");
        line(sender, "/tgu apply <upgrade_id> — apply upgrade to held weapon");
        line(sender, "/tgu inspect — calculated weapon stats");
        line(sender, "/tgu nbt [page] — page through runtime NBT");
        line(sender, "/tgu clear — remove upgrades from held weapon");
        line(sender, "/tgu stacks set <0..10000> | reset — Mythic stacks");
        line(sender, "/tgu runtime reset — reset counters and effect cooldowns");
        line(sender, "/tgu force on|off — force random procs");
        line(sender, "/tgu safe on|off — disable block damage from TGU effects");
        line(sender, "/tgu dummy [count] [health] | clear — test zombies");
        line(sender, "/tgu visuals — verify server-to-client visual delivery");
        line(sender, "/tgu verify — full audit of 839 effects and 8380 pairs");
        line(sender, "/tgu stress start [5..60] | status | stop — stress test");
    }

    private static void list(ICommandSender sender, String[] args) throws CommandException {
        UpgradeRarity rarity = null;
        int page = 1;
        if (args.length >= 2) {
            if (isInteger(args[1])) page = parseInt(args[1], 1);
            else rarity = parseRarity(args[1]);
        }
        if (args.length >= 3) page = parseInt(args[2], 1);

        List<Entry> entries = allEntries();
        if (rarity != null) {
            List<Entry> filtered = new ArrayList<>();
            for (Entry entry : entries) if (entry.buff.getRarity() == rarity) filtered.add(entry);
            entries = filtered;
        }
        showEntries(sender, entries, page, rarity == null ? "All upgrades" : rarity.getName());
    }

    private static void find(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) throw new WrongUsageException("/tgu find <text> [page]");
        String query = args[1].toLowerCase(Locale.ROOT);
        int page = args.length >= 3 ? parseInt(args[2], 1) : 1;
        List<Entry> matches = new ArrayList<>();
        for (Entry entry : allEntries()) {
            UpgradeBuff buff = entry.buff;
            String haystack = (buff.getId() + " " + buff.getDisplayName() + " "
                    + buff.getDescription() + " " + entry.weaponId).toLowerCase(Locale.ROOT);
            if (haystack.contains(query)) matches.add(entry);
        }
        showEntries(sender, matches, page, "Search: " + args[1]);
    }

    private static void showEntries(ICommandSender sender, List<Entry> entries, int page,
                                    String heading) throws CommandException {
        int pageCount = Math.max(1, (entries.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        if (page > pageCount) throw new CommandException("Page must be between 1 and " + pageCount);
        title(sender, heading + " — " + entries.size() + " items, page " + page + "/" + pageCount);
        int from = (page - 1) * PAGE_SIZE;
        int to = Math.min(entries.size(), from + PAGE_SIZE);
        for (int i = from; i < to; i++) {
            Entry entry = entries.get(i);
            line(sender, TextFormatting.AQUA + entry.buff.getId() + TextFormatting.GRAY + " ["
                    + entry.buff.getRarity().getName() + "] " + shortWeapon(entry.weaponId));
        }
        if (entries.isEmpty()) line(sender, TextFormatting.GRAY + "No results.");
    }

    private static void give(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 2) throw new WrongUsageException("/tgu give <upgrade_id>");
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        Entry entry = findEntry(args[1]);
        if (entry == null) throw new CommandException("Unknown upgrade: " + args[1]);

        Item item = Item.REGISTRY.getObject(new ResourceLocation(entry.weaponId));
        if (!(item instanceof GenericGun)) {
            throw new CommandException("Weapon not found in Techguns: " + entry.weaponId);
        }

        ItemStack stack = new ItemStack(item);
        UpgradeData.addUpgrade(stack, entry.buff.getId());
        UpgradeApplicator.applyUpgradesToGun(stack);
        if (!player.inventory.addItemStackToInventory(stack)) player.dropItem(stack, false);
        player.inventoryContainer.detectAndSendChanges();
        title(sender, "Given: " + stack.getDisplayName());
        line(sender, entry.buff.getId() + " — " + entry.buff.getDisplayName());
        line(sender, TextFormatting.GRAY + entry.buff.getDescription());
    }

    private static void apply(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 2) throw new WrongUsageException("/tgu apply <upgrade_id>");
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        ItemStack held = requireGun(player);
        Entry entry = findEntry(args[1]);
        if (entry == null) throw new CommandException("Unknown upgrade: " + args[1]);

        ResourceLocation heldId = held.getItem().getRegistryName();
        if (heldId == null || !entry.weaponId.equals(heldId.toString())) {
            throw new CommandException("Upgrade is for " + entry.weaponId
                    + ", held item is " + (heldId == null ? "unknown item" : heldId));
        }

        List<String> before = UpgradeData.getUpgrades(held);
        if (before.contains(entry.buff.getId())) throw new CommandException("This upgrade is already installed.");
        if (before.size() >= 2) throw new CommandException("The weapon already has two upgrades.");
        UpgradeData.addUpgrade(held, entry.buff.getId());
        UpgradeApplicator.applyUpgradesToGun(held);
        player.inventoryContainer.detectAndSendChanges();
        title(sender, "Upgrade applied to held weapon");
        line(sender, entry.buff.getId() + " — " + entry.buff.getDisplayName());
    }

    private static void inspect(ICommandSender sender) throws CommandException {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        ItemStack gun = requireGun(player);
        if (UpgradeApplicator.needsRefresh(gun)) UpgradeApplicator.applyUpgradesToGun(gun);
        NBTTagCompound tag = upgradeTag(gun);

        ResourceLocation itemId = gun.getItem().getRegistryName();
        title(sender, gun.getDisplayName() + " (" + itemId + ")");
        List<String> upgrades = UpgradeData.getUpgrades(gun);
        line(sender, "Upgrades: " + (upgrades.isEmpty() ? "none" : String.join(", ", upgrades)));
        line(sender, "Active: " + yesNo(GunStatModifiers.isActive(gun))
                + "; unlimited ammo: " + yesNo(GunStatModifiers.isUnlimitedAmmo(gun))
                + "; schema: " + tag.getInteger("modifier_schema"));
        statFloat(sender, "Damage", tag, "base_damage", gun, Stat.DAMAGE);
        statFloat(sender, "Min damage", tag, "base_damage_min", gun, Stat.DAMAGE);
        statInt(sender, "Fire delay", tag, "base_min_firetime", gun, Stat.FIRE_DELAY, " ticks");
        statFloat(sender, "Spread", tag, "base_accuracy", gun, Stat.ACCURACY);
        statFloat(sender, "Damage drop start", tag, "base_range_start", gun, Stat.RANGE);
        statFloat(sender, "Damage drop end", tag, "base_range_end", gun, Stat.RANGE);
        statInt(sender, "Magazine", tag, "base_clipsize", gun, Stat.CLIP, "");
        statInt(sender, "Reload", tag, "base_reloadtime", gun, Stat.RELOAD, " ticks");
        statFloat(sender, "Penetration", tag, "base_penetration", gun, Stat.PENETRATION);
        statInt(sender, "Projectiles", tag, "base_bulletcount", gun, Stat.BULLETS, "");
        line(sender, TextFormatting.GRAY + "Detailed NBT: /tgu nbt [page]");
    }

    private static void nbt(ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        NBTTagCompound tag = upgradeTag(requireGun(player));
        List<String> keys = new ArrayList<>(tag.getKeySet());
        Collections.sort(keys);
        int page = args.length >= 2 ? parseInt(args[1], 1) : 1;
        int pageCount = Math.max(1, (keys.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        if (page > pageCount) throw new CommandException("Page must be between 1 and " + pageCount);
        title(sender, "Techguns Upgrade NBT — page " + page + "/" + pageCount);
        int from = (page - 1) * PAGE_SIZE;
        for (int i = from; i < Math.min(keys.size(), from + PAGE_SIZE); i++) {
            String key = keys.get(i);
            NBTBase value = tag.getTag(key);
            String text = value == null ? "null" : value.toString();
            if (text.length() > 180) text = text.substring(0, 177) + "...";
            line(sender, TextFormatting.AQUA + key + TextFormatting.GRAY + " = " + text);
        }
        if (keys.isEmpty()) line(sender, TextFormatting.GRAY + "Upgrade NBT is empty.");
    }

    private static void clear(ICommandSender sender) throws CommandException {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        ItemStack gun = requireGun(player);
        NBTTagCompound root = gun.getTagCompound();
        if (root != null) root.removeTag("techgunsupgrade");
        player.inventoryContainer.detectAndSendChanges();
        title(sender, "All upgrades and runtime data removed.");
    }

    private static void stacks(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) throw new WrongUsageException("/tgu stacks set <value> | reset");
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        ItemStack gun = requireGun(player);
        NBTTagCompound tag = upgradeTag(gun);
        NBTTagCompound specs = tag.getCompoundTag("mythic_stack_specs");
        if (specs.getKeySet().isEmpty()) throw new CommandException("Weapon has no stacking Mythic effects.");

        float requested;
        if ("reset".equalsIgnoreCase(args[1])) requested = 0.0f;
        else {
            if (args.length < 3 || !"set".equalsIgnoreCase(args[1])) {
                throw new WrongUsageException("/tgu stacks set <value> | reset");
            }
            requested = (float) parseDouble(args[2], 0.0, 10000.0);
        }

        NBTTagCompound progress = tag.getCompoundTag("mythic_stack_progress");
        for (String key : specs.getKeySet()) {
            float maximum = Math.max(0.0f, specs.getCompoundTag(key).getFloat("max"));
            progress.setFloat(key, Math.min(requested, maximum));
        }
        tag.setTag("mythic_stack_progress", progress);
        player.inventoryContainer.detectAndSendChanges();
        title(sender, "Mythic stacks set: " + format(requested));
    }

    private static void runtime(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 2 || !"reset".equalsIgnoreCase(args[1])) {
            throw new WrongUsageException("/tgu runtime reset");
        }
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        NBTTagCompound tag = upgradeTag(requireGun(player));
        int removed = 0;
        for (String key : new ArrayList<>(tag.getKeySet())) {
            if (key.startsWith("runtime_") || "ammo_cost_accumulator".equals(key)) {
                tag.removeTag(key);
                removed++;
            }
        }
        player.inventoryContainer.detectAndSendChanges();
        title(sender, "Counters reset: " + removed);
    }

    private static void force(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 1 || "status".equalsIgnoreCase(args[1])) {
            title(sender, "Forced random effects: " + onOff(DebugSettings.isForceRandomEffects()));
            return;
        }
        boolean enabled = parseToggle(args[1]);
        DebugSettings.setForceRandomEffects(enabled);
        title(sender, "Forced random effects: " + onOff(enabled));
        if (enabled) line(sender, TextFormatting.YELLOW
                + "Random penalties and recoil are also forced.");
    }

    private static void safe(MinecraftServer server, ICommandSender sender, String[] args)
            throws CommandException {
        if (args.length == 1 || "status".equalsIgnoreCase(args[1])) {
            title(sender, "Safe mode: " + onOff(DebugSettings.isSafeMode()));
            return;
        }
        boolean enabled = parseToggle(args[1]);
        DebugSettings.setSafeMode(enabled);
        syncTechgunsSafeMode(server, enabled);
        title(sender, "Safe mode: " + onOff(enabled));
        line(sender, enabled
                ? "Block damage disabled. Explosion damage, sound, and particles remain."
                : TextFormatting.YELLOW
                + "Block damage from explosions and fire re-enabled.");
    }

    private static void syncTechgunsSafeMode(MinecraftServer server, boolean enabled) {
        if (server == null || server.getPlayerList() == null) return;
        for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            TGExtendedPlayer properties = TGExtendedPlayer.get(player);
            if (properties == null) continue;
            properties.enableSafemode = enabled;
            TGPackets.wrapper.sendTo(
                    new PacketTGExtendedPlayerSync(player, properties, true), player);
        }
    }

    private static void dummy(ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        if (args.length >= 2 && "clear".equalsIgnoreCase(args[1])) {
            AxisAlignedBB area = player.getEntityBoundingBox().grow(128.0);
            List<EntityZombie> zombies = player.world.getEntitiesWithinAABB(EntityZombie.class, area);
            int removed = 0;
            for (EntityZombie zombie : zombies) {
                if (zombie.getEntityData().getBoolean("tgu_test_dummy")) {
                    zombie.setDead();
                    removed++;
                }
            }
            title(sender, "Test targets removed: " + removed);
            return;
        }

        int count = args.length >= 2 ? parseInt(args[1], 1, 20) : 5;
        float health = args.length >= 3 ? (float) parseDouble(args[2], 1.0, 10000.0) : 200.0f;
        Vec3d forward = player.getLookVec();
        Vec3d horizontal = new Vec3d(forward.x, 0.0, forward.z);
        if (horizontal.lengthSquared() < 0.01) horizontal = new Vec3d(0.0, 0.0, 1.0);
        horizontal = horizontal.normalize();
        Vec3d side = new Vec3d(-horizontal.z, 0.0, horizontal.x);

        for (int i = 0; i < count; i++) {
            int row = i / 5;
            int column = i % 5;
            double lateral = (column - Math.min(4, count - 1) / 2.0) * 2.0;
            Vec3d pos = player.getPositionVector().add(horizontal.scale(6.0 + row * 3.0))
                    .add(side.scale(lateral));
            EntityZombie zombie = new EntityZombie(player.world);
            zombie.setPosition(pos.x, player.posY, pos.z);
            zombie.setNoAI(true);
            zombie.setSilent(true);
            zombie.enablePersistence();
            zombie.setCustomNameTag("TGU Dummy " + (i + 1));
            zombie.setAlwaysRenderNameTag(true);
            zombie.getEntityData().setBoolean("tgu_test_dummy", true);
            zombie.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(health);
            zombie.setHealth(health);
            player.world.spawnEntity(zombie);
        }
        title(sender, "Created targets: " + count + ", health: " + format(health));
    }

    private static void status(ICommandSender sender) {
        title(sender, "Test mode status");
        line(sender, "Forced effects: " + onOff(DebugSettings.isForceRandomEffects()));
        line(sender, "Safe mode: " + onOff(DebugSettings.isSafeMode()));
        if (sender.getCommandSenderEntity() instanceof EntityPlayerMP) {
            ItemStack held = ((EntityPlayerMP) sender.getCommandSenderEntity()).getHeldItemMainhand();
            line(sender, "In hand: " + (held.isEmpty() ? "empty" : held.getDisplayName()));
        }
    }

    private static void visuals(ICommandSender sender) throws CommandException {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        Vec3d look = player.getLookVec();
        Vec3d center = player.getPositionVector().add(look.scale(7.0)).add(0.0, 1.0, 0.0);
        Vec3d side = new Vec3d(-look.z, 0.0, look.x);
        if (side.lengthSquared() < 0.0001) side = new Vec3d(1.0, 0.0, 0.0);
        else side = side.normalize();

        UpgradeVisualEffects.beam(player.world,
                player.getPositionEyes(1.0f).add(look.scale(0.5)), center,
                UpgradeVisualEffects.BeamStyle.ARC);
        UpgradeVisualEffects.areaBurst(player.world, center.add(side.scale(2.5)), 2.5f,
                UpgradeVisualEffects.AreaStyle.EMP);
        UpgradeVisualEffects.specialBurst(player.world, center.subtract(side.scale(2.5)), 2.5f,
                UpgradeVisualEffects.SpecialStyle.LEGENDARY);

        title(sender, "Server sent three reference visual effects.");
        line(sender, "You should see an electric arc, an EMP, and a golden flash.");
        line(sender, TextFormatting.GRAY
                + "If not, ensure the same JAR is installed on both server and client.");
    }

    private static void stress(MinecraftServer server, ICommandSender sender, String[] args)
            throws CommandException {
        if (args.length == 1 || "status".equalsIgnoreCase(args[1])) {
            title(sender, TguStressHarness.status());
            return;
        }
        if ("stop".equalsIgnoreCase(args[1])) {
            title(sender, TguStressHarness.stop());
            return;
        }
        if (!"start".equalsIgnoreCase(args[1])) {
            throw new WrongUsageException("/tgu stress start [5..60] | status | stop");
        }
        int seconds = args.length >= 3 ? parseInt(args[2], 5, 60) : 60;
        title(sender, TguStressHarness.start(server, sender, seconds));
    }

    private static void verify(ICommandSender sender) {
        title(sender, "Full verification started. Server may pause for a few seconds...");
        UpgradeVerifier.Report report = UpgradeVerifier.verifyAll();
        title(sender, report.isSuccessful() ? TextFormatting.GREEN + "VERIFICATION PASSED"
                : TextFormatting.RED + "ERRORS FOUND");
        line(sender, "Weapons: " + report.getWeaponCount());
        line(sender, "Upgrades: " + report.getUpgradeCount());
        line(sender, "Effects: " + report.getEffectCount());
        line(sender, "Pairs: " + report.getPairCount());
        line(sender, "Errors: " + report.getFailures().size());
        line(sender, "Time: " + report.getElapsedMillis() + " ms");
        int shown = Math.min(8, report.getFailures().size());
        for (int i = 0; i < shown; i++) {
            line(sender, TextFormatting.RED + report.getFailures().get(i));
        }
        if (report.getFailures().size() > shown) {
            line(sender, TextFormatting.RED + "Remaining errors written to latest.log");
        }
        for (String failure : report.getFailures()) {
            TechgunsUpgradeMod.LOGGER.error("TGU verify: " + failure);
        }
    }

    private static ItemStack requireGun(EntityPlayerMP player) throws CommandException {
        ItemStack stack = player.getHeldItemMainhand();
        if (stack.isEmpty() || !(stack.getItem() instanceof GenericGun)) {
            throw new CommandException("Hold a Techguns weapon in your main hand.");
        }
        return stack;
    }

    private static NBTTagCompound upgradeTag(ItemStack stack) {
        return stack.getOrCreateSubCompound("techgunsupgrade");
    }

    private static Entry findEntry(String upgradeId) {
        for (Entry entry : allEntries()) {
            if (entry.buff.getId().equalsIgnoreCase(upgradeId)) return entry;
        }
        return null;
    }

    private static List<Entry> allEntries() {
        List<Entry> entries = new ArrayList<>();
        for (Map.Entry<String, List<UpgradeBuff>> weapon :
                WeaponUpgrades.getAllWeaponUpgrades().entrySet()) {
            for (UpgradeBuff buff : weapon.getValue()) {
                entries.add(new Entry(weapon.getKey(), buff));
            }
        }
        entries.sort(Comparator.comparing(entry -> entry.buff.getId()));
        return entries;
    }

    private static UpgradeRarity parseRarity(String value) throws CommandException {
        String normalized = value.toUpperCase(Locale.ROOT).replace('-', '_');
        try {
            return UpgradeRarity.valueOf(normalized);
        } catch (IllegalArgumentException ignored) {
            throw new CommandException("Unknown rarity: " + value);
        }
    }

    private static boolean parseToggle(String value) throws CommandException {
        if ("on".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value)
                || "1".equals(value)) return true;
        if ("off".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)
                || "0".equals(value)) return false;
        throw new CommandException("Use on or off.");
    }

    private static boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private static String shortWeapon(String id) {
        int separator = id.indexOf(':');
        return separator >= 0 ? id.substring(separator + 1) : id;
    }

    private static String yesNo(boolean value) {
        return value ? TextFormatting.GREEN + "yes" + TextFormatting.RESET
                : TextFormatting.RED + "no" + TextFormatting.RESET;
    }

    private static String onOff(boolean value) {
        return value ? TextFormatting.GREEN + "ON" : TextFormatting.RED + "OFF";
    }

    private static String format(float value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static void statFloat(ICommandSender sender, String name, NBTTagCompound tag,
                                  String baseKey, ItemStack gun, Stat stat) {
        if (!tag.hasKey(baseKey)) return;
        float base = tag.getFloat(baseKey);
        float current;
        switch (stat) {
            case DAMAGE: current = GunStatModifiers.damage(gun, base); break;
            case ACCURACY: current = GunStatModifiers.accuracy(gun, base); break;
            case RANGE: current = GunStatModifiers.range(gun, base); break;
            case PENETRATION: current = GunStatModifiers.penetration(gun, base); break;
            default: return;
        }
        line(sender, name + ": " + format(base) + " → " + TextFormatting.GREEN + format(current));
    }

    private static void statInt(ICommandSender sender, String name, NBTTagCompound tag,
                                String baseKey, ItemStack gun, Stat stat, String suffix) {
        if (!tag.hasKey(baseKey)) return;
        int base = tag.getInteger(baseKey);
        int current;
        switch (stat) {
            case FIRE_DELAY: current = GunStatModifiers.fireDelay(gun, base); break;
            case CLIP: current = GunStatModifiers.clipSize(gun, base); break;
            case RELOAD: current = GunStatModifiers.reloadTime(gun, base); break;
            case BULLETS: current = GunStatModifiers.bulletCount(gun, base); break;
            default: return;
        }
        line(sender, name + ": " + base + " → " + TextFormatting.GREEN + current + suffix);
    }

    private static void title(ICommandSender sender, String text) {
        sender.sendMessage(new TextComponentString(TextFormatting.GOLD + "[TGU] "
                + TextFormatting.RESET + text));
    }

    private static void line(ICommandSender sender, String text) {
        sender.sendMessage(new TextComponentString("  " + text));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender,
                                          String[] args, BlockPos targetPos) {
        if (args.length == 1) return getListOfStringsMatchingLastWord(args, SUBCOMMANDS);
        if (args.length == 2) {
            if ("give".equalsIgnoreCase(args[0]) || "apply".equalsIgnoreCase(args[0])) {
                List<String> ids = new ArrayList<>();
                for (Entry entry : allEntries()) ids.add(entry.buff.getId());
                return getListOfStringsMatchingLastWord(args, ids);
            }
            if ("list".equalsIgnoreCase(args[0])) {
                return getListOfStringsMatchingLastWord(args,
                        Arrays.asList("common", "uncommon", "rare", "epic", "legendary",
                                "mythic", "ultra_mythic"));
            }
            if ("force".equalsIgnoreCase(args[0]) || "safe".equalsIgnoreCase(args[0])) {
                return getListOfStringsMatchingLastWord(args, Arrays.asList("on", "off", "status"));
            }
            if ("stacks".equalsIgnoreCase(args[0])) {
                return getListOfStringsMatchingLastWord(args, Arrays.asList("set", "reset"));
            }
            if ("runtime".equalsIgnoreCase(args[0])) {
                return getListOfStringsMatchingLastWord(args, Collections.singletonList("reset"));
            }
            if ("dummy".equalsIgnoreCase(args[0])) {
                return getListOfStringsMatchingLastWord(args, Collections.singletonList("clear"));
            }
            if ("stress".equalsIgnoreCase(args[0])) {
                return getListOfStringsMatchingLastWord(args,
                        Arrays.asList("start", "status", "stop"));
            }
        }
        if (args.length == 3 && "stacks".equalsIgnoreCase(args[0])
                && "set".equalsIgnoreCase(args[1])) {
            return getListOfStringsMatchingLastWord(args, Arrays.asList("0", "25", "50", "100"));
        }
        if (args.length == 3 && "stress".equalsIgnoreCase(args[0])
                && "start".equalsIgnoreCase(args[1])) {
            return getListOfStringsMatchingLastWord(args, Arrays.asList("5", "30", "60"));
        }
        return Collections.emptyList();
    }

    private enum Stat { DAMAGE, FIRE_DELAY, ACCURACY, RANGE, CLIP, RELOAD, PENETRATION, BULLETS }

    private static final class Entry {
        private final String weaponId;
        private final UpgradeBuff buff;

        private Entry(String weaponId, UpgradeBuff buff) {
            this.weaponId = weaponId;
            this.buff = buff;
        }
    }
}
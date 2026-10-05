package com.stepm.techgunsupgrade.command;

import com.stepm.techgunsupgrade.apocalypse.ZombieApocalypseManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class CommandApocalypse extends CommandBase {

    @Override
    public String getName() {
        return "apocalypse";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/apocalypse [start|stop|status]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            sender.sendMessage(new TextComponentString("§cUsage: /apocalypse [start|stop|status]"));
            return;
        }

        World world = sender.getEntityWorld();
        if (world == null) {
            sender.sendMessage(new TextComponentString("§cError: world not found!"));
            return;
        }

        switch (args[0].toLowerCase()) {
            case "start":
                if (!ZombieApocalypseManager.canStartApocalypse(world)) {
                    sender.sendMessage(new TextComponentString("§cApocalypse is already running!"));
                    return;
                }
                ZombieApocalypseManager.startApocalypse(world);
                sender.sendMessage(new TextComponentString("§4Apocalypse started!"));
                break;

            case "stop":
                if (!ZombieApocalypseManager.isApocalypseActiveServer()) {
                    sender.sendMessage(new TextComponentString("§cApocalypse is not active!"));
                    return;
                }
                ZombieApocalypseManager.endApocalypse(world);
                sender.sendMessage(new TextComponentString("§6Apocalypse stopped!"));
                break;

            case "status":
                if (ZombieApocalypseManager.isApocalypseActiveServer()) {
                    int wave = ZombieApocalypseManager.getCurrentWaveServer();
                    int kills = ZombieApocalypseManager.getTotalKillsServer();
                    int alive = ZombieApocalypseManager.getZombiesAliveServer();
                    sender.sendMessage(new TextComponentString(
                        "§6Apocalypse active! Wave: §f" + wave +
                        "§6, Kills: §f" + kills +
                        "§6, Enemies: §f" + alive
                    ));
                } else {
                    sender.sendMessage(new TextComponentString("§7Apocalypse not active"));
                }
                break;

            default:
                sender.sendMessage(new TextComponentString("§cUnknown command. Use: start, stop, status"));
                break;
        }
    }
}
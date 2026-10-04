package io.github.anmolgoraya.odinportfolioplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class OdinCommand implements CommandExecutor {
    private final OdinPortfolioPlugin plugin;

    public OdinCommand(OdinPortfolioPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("odin.use")) {
            sender.sendMessage(plugin.raw(plugin.getConfig().getString("messages.no-permission", "<red>No permission.")));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("menu")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.raw("<red>This command requires a player."));
                return true;
            }
            plugin.openMenu(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "stats" -> {
                if (sender instanceof Player player) plugin.showStats(player);
                else sender.sendMessage(plugin.raw("<red>This command requires a player."));
            }
            case "setspawn" -> {
                if (!sender.hasPermission("odin.setspawn")) {
                    sender.sendMessage(plugin.raw(plugin.getConfig().getString("messages.no-permission", "<red>No permission.")));
                    return true;
                }
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(plugin.raw("<red>This command requires a player."));
                    return true;
                }
                plugin.setSpawn(player.getLocation());
                sender.sendMessage(plugin.msg(plugin.getConfig().getString("messages.spawn-set", "<green>Spawn saved.")));
            }
            case "reload" -> {
                if (!sender.hasPermission("odin.reload")) {
                    sender.sendMessage(plugin.raw(plugin.getConfig().getString("messages.no-permission", "<red>No permission.")));
                    return true;
                }
                plugin.reloadConfig();
                plugin.loadSpawn();
                sender.sendMessage(plugin.msg(plugin.getConfig().getString("messages.reloaded", "<green>Reloaded.")));
            }
            default -> sender.sendMessage(plugin.raw(plugin.getConfig().getString("messages.unknown-command", "<red>Usage: /odin <menu|stats|setspawn|reload>")));
        }
        return true;
    }
}

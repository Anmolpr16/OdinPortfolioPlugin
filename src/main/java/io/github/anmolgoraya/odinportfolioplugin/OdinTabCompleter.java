package io.github.anmolgoraya.odinportfolioplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public final class OdinTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) return List.of();
        return List.of("menu", "stats", "setspawn", "reload").stream()
                .filter(s -> s.startsWith(args[0].toLowerCase()))
                .toList();
    }
}

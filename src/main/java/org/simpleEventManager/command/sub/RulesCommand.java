package org.simpleEventManager.command.sub;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.simpleEventManager.SimpleEventManager;

public class RulesCommand implements SubCommand {

    private final SimpleEventManager plugin;

    public RulesCommand(SimpleEventManager plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!plugin.getLobbyState().isLobbyOpen()) {
            player.sendMessage("§cLes règles ne sont disponibles que dans le lobby.");
            return true;
        }

        // à terme : afficher les règles dynamiques de l'event en cours
        player.sendMessage("§eRègles de l’event : survivre, gagner, et s’amuser !");
        return true;
    }
}

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
            sender.sendMessage(plugin.getMessageManager().prefixed("only-players"));
            return true;
        }

        if (!plugin.getLobbyState().isLobbyOpen()) {
            player.sendMessage(plugin.getMessageManager().prefixed("rules-unavailable"));
            return true;
        }

        // Affichage des règles, à terme dynamiques
        player.sendMessage(plugin.getMessageManager().prefixed("rules-message"));
        return true;
    }
}

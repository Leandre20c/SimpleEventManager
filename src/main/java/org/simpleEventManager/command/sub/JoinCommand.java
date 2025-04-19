package org.simpleEventManager.command.sub;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.simpleEventManager.SimpleEventManager;

public class JoinCommand implements SubCommand {

    private final SimpleEventManager plugin;

    public JoinCommand(SimpleEventManager plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!plugin.getLobbyState().isLobbyOpen()) {
            player.sendMessage("§cAucun event n’est en préparation actuellement.");
            return true;
        }

        plugin.getLobbyState().addPlayer(player.getUniqueId());
        Location loc = plugin.getLobbySpawn();
        if (loc != null) {
            player.teleport(loc);
        }

        player.sendMessage("§aTu as rejoint le lobby de l’event !");
        return true;
    }
}

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
            player.sendMessage("§cAucun événement n'est en cours de préparation.");
            return true;
        }

        plugin.getParticipantManager().join(player);
        Location lobbySpawn = plugin.getLobbyLocation();
        if (lobbySpawn == null){
            player.sendMessage("§cLe lobby n'a pas été défini.");
            return true;
        }

        player.teleport(lobbySpawn);
        player.sendMessage("§aTu as rejoint le lobby de l’événement !");
        return true;
    }
}

package org.simpleEventManager.command.sub;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.simpleEventManager.SimpleEventManager;
import org.simpleEventManager.api.EventGame;
import org.simpleEventManager.state.LobbyState;

import java.util.List;

public class StartCommand implements SubCommand {

    private final SimpleEventManager plugin;

    public StartCommand(SimpleEventManager plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("event.admin")) {
            player.sendMessage("§cTu n'as pas la permission.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /event start <eventName>");
            return true;
        }

        String name = args[1];
        EventGame event = plugin.getEventLoader().getEventByName(name);
        if (event == null) {
            player.sendMessage("§cEvent '" + name + "' introuvable.");
            return true;
        }

        LobbyState lobby = plugin.getLobbyState();
        if (!lobby.isLobbyOpen()) {
            player.sendMessage("§cAucun lobby ouvert.");
            return true;
        }

        List<Player> players = lobby.getWaitingPlayers().stream()
                .map(Bukkit::getPlayer)
                .filter(p -> p != null && p.isOnline())
                .toList();

        if (players.isEmpty()) {
            player.sendMessage("§cAucun joueur dans le lobby.");
            return true;
        }

        event.start(players);
        lobby.setLobbyOpen(false);
        Bukkit.broadcastMessage("§6[Event] §aEvent '" + event.getEventName() + "' lancé avec " + players.size() + " joueurs !");
        return true;
    }
}

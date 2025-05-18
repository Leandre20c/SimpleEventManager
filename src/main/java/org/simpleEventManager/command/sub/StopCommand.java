package org.simpleEventManager.command.sub;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleEventManager.SimpleEventManager;
import org.simpleEventManager.api.EventGame;
import org.simpleEventManager.state.LobbyState;
import org.simpleEventManager.utils.LobbyStarter;

import java.util.Set;

public class StopCommand implements SubCommand {

    private final SimpleEventManager plugin;

    public StopCommand(SimpleEventManager plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        LobbyState lobbyState = plugin.getLobbyState();
        EventGame currentGame = plugin.getCurrentGame();

        if (!sender.hasPermission("event.admin")) {
            sender.sendMessage(plugin.getMessageManager().prefixed("no-permission"));
            return true;
        }

        // Si un lobby est actif
        if (lobbyState.isLobbyOpen()) {
            LobbyStarter.cancelCountdown();
            lobbyState.closeLobby();
            teleportAllParticipantsToSpawn();
            plugin.getParticipantManager().clear();
            Bukkit.broadcastMessage("§cLe lobby a été fermé prématurément. L’événement n’aura pas lieu.");
            return true;
        }

        // Si un event est en cours
        if (currentGame != null) {
            currentGame.stop();
            if (currentGame instanceof JavaPlugin jp) {
                Bukkit.getScheduler().cancelTasks(jp);
            }
            teleportAllParticipantsToSpawn();
            plugin.setCurrentGame(null);
            plugin.getParticipantManager().clear();
            Bukkit.broadcastMessage("§cL’événement en cours a été arrêté par un administrateur.");
            return true;
        }

        sender.sendMessage("§cAucun lobby ni événement actif actuellement.");
        return true;
    }

    private void teleportAllParticipantsToSpawn() {
        Set<Player> participants = plugin.getParticipantManager().getOnlineParticipants();
        participants.forEach(player -> player.performCommand("event leave"));
    }
}

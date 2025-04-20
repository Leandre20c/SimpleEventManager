package org.simpleEventManager.command.sub;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.simpleEventManager.SimpleEventManager;
import org.simpleEventManager.api.EventGame;
import org.simpleEventManager.state.LobbyState;

import java.util.ArrayList;
import java.util.Set;

public class StartCommand implements SubCommand {

    private final SimpleEventManager plugin;

    public StartCommand(SimpleEventManager plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        LobbyState lobbyState = plugin.getLobbyState();

        if (args.length == 2) {
            // /event start <event_name> -> ouvre le lobby
            if (lobbyState.isLobbyOpen()) {
                sender.sendMessage("§cUn lobby est déjà actif.");
                return true;
            }

            EventGame game = plugin.getEventLoader().getEventByName(args[1]);
            if (game == null) {
                sender.sendMessage("§cCet événement n'existe pas.");
                return true;
            }

            lobbyState.openLobby();
            plugin.setCurrentGame(game);
            Bukkit.broadcastMessage("§aUn event §e" + game.getEventName() + "§a va bientôt commencer ! Faites §e/event join §apour participer. Lancement dans 10 minutes.");

            // Timer 10 min automatique
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (lobbyState.isLobbyOpen()) {
                    launchEvent(sender, lobbyState, game);
                }
            }, 12000L); // 12000 ticks = 10 min

            return true;

        } else if (args.length == 1) {
            // /event start -> Lance directement si lobby ouvert
            if (!lobbyState.isLobbyOpen()) {
                sender.sendMessage("§cAucun lobby actif actuellement.");
                return true;
            }

            EventGame currentGame = plugin.getCurrentGame();
            if (currentGame == null) {
                sender.sendMessage("§cErreur interne : Aucun événement chargé.");
                return true;
            }

            launchEvent(sender, lobbyState, currentGame);
            return true;
        }

        sender.sendMessage("§cUsage : /event start <event_name> ou /event start");
        return true;
    }

    private void launchEvent(CommandSender sender, LobbyState lobbyState, EventGame game) {
        lobbyState.closeLobby();

        Set<Player> participants = plugin.getParticipantManager().getOnlineParticipants();
        if (participants.size() < 2) {
            Bukkit.broadcastMessage("§cPas assez de joueurs inscrits pour démarrer l'événement !");
            plugin.getParticipantManager().clear();
            return;
        }

        game.start(new ArrayList<>(participants));
        Bukkit.broadcastMessage("§aL'événement §e" + game.getEventName() + "§a commence maintenant avec §e" + participants.size() + " joueurs§a ! Bonne chance à tous !");
    }
}

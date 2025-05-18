package org.simpleEventManager.command.sub;

import org.bukkit.command.CommandSender;
import org.simpleEventManager.SimpleEventManager;
import org.simpleEventManager.api.EventGame;
import org.simpleEventManager.state.LobbyState;
import org.simpleEventManager.utils.LobbyStarter;

import java.util.Objects;

public class StartCommand implements SubCommand {

    private final SimpleEventManager plugin;

    public StartCommand(SimpleEventManager plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        LobbyState lobbyState = plugin.getLobbyState();

        if (args.length >= 2) {
            if (!sender.hasPermission("event.admin")) {
                sender.sendMessage(plugin.getMessageManager().prefixed("no-permission"));
                return true;
            }

            if (lobbyState.isLobbyOpen()) {
                if (Objects.equals(args[1], plugin.getCurrentGame().getEventName()))
                {
                    LobbyStarter.launchEvent(plugin, plugin.getCurrentGame());
                    return true;
                }
                sender.sendMessage("§cUn lobby est déjà actif.");
                return true;
            }

            EventGame game = plugin.getEventLoader().getEventByName(args[1]);
            if (game == null) {
                sender.sendMessage("§cCet événement n'existe pas.");
                return true;
            }

            String subEvent = args.length >= 3 ? args[2] : null;

            game.setMode(subEvent);


            LobbyStarter.startLobbyWithCountdown(plugin, game);

        } else if (args.length == 1) {
            if (!lobbyState.isLobbyOpen()) {
                sender.sendMessage("§cAucun lobby actif actuellement.");
                return true;
            }

            if (plugin.getCurrentGame() == null) {
                sender.sendMessage("§cErreur : aucun événement chargé.");
                return true;
            }

            LobbyStarter.launchEvent(plugin, plugin.getCurrentGame());
            return true;
        }

        sender.sendMessage("§cUsage : /event start <event_name> ou /event start");
        return true;
    }
}

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
                sender.sendMessage(plugin.getMessageManager().get("no-permission"));
                return true;
            }

            if (lobbyState.isLobbyOpen()) {
                // Vérifier si c'est pour forcer le démarrage du lobby actuel
                if (Objects.equals(args[1], plugin.getCurrentGame().getEventName())) {
                    LobbyStarter.launchEvent(plugin, plugin.getCurrentGame());
                    return true;
                }
                sender.sendMessage("§cUn lobby est déjà actif.");
                return true;
            }

            // Parser les arguments avec la nouvelle syntaxe
            EventConfig eventConfig = parseEventCommand(args, sender);
            if (eventConfig == null) {
                return true; // Erreur déjà affichée dans parseEventCommand
            }

            EventGame game = plugin.getEventLoader().getEventByName(eventConfig.eventName);
            if (game == null) {
                sender.sendMessage("§cCet événement n'existe pas.");
                return true;
            }

            // Configurer l'événement
            game.setMode(eventConfig.subEvent);
            //game.setRewardsEnabled(eventConfig.rewards);
            //game.setNotificationsEnabled(eventConfig.notifications);

            // Messages informatifs pour l'admin
            StringBuilder configMessage = new StringBuilder("§eLancement de l'événement §a" + eventConfig.eventName);
            if (eventConfig.subEvent != null) {
                configMessage.append(" §7(mode: §f").append(eventConfig.subEvent).append("§7)");
            }
            configMessage.append("\n§7├ Notifications Discord: ").append(eventConfig.notifications ? "§aON" : "§cOFF");
            configMessage.append("\n§7└ Récompenses: ").append(eventConfig.rewards ? "§aON" : "§cOFF");

            if (!eventConfig.rewards) {
                configMessage.append(" §8- Événement fun/test");
            }

            sender.sendMessage(configMessage.toString());

            // Démarrer l'événement
            LobbyStarter.startLobbyWithCountdown(plugin, game, eventConfig.notifications);
            return true;

        } else if (args.length == 1) {
            // Forcer le démarrage du lobby actuel
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

        // Afficher l'aide
        showUsage(sender);
        return true;
    }

    /**
     * Parse la commande avec les nouveaux flags
     */
    private EventConfig parseEventCommand(String[] args, CommandSender sender) {
        EventConfig config = new EventConfig();
        config.eventName = args[1];

        // Valeurs par défaut
        config.notifications = false; // OFF par défaut
        config.rewards = true;        // ON par défaut
        config.subEvent = null;

        // Parser les arguments à partir de l'index 2
        for (int i = 2; i < args.length; i++) {
            String arg = args[i];

            if (arg.equalsIgnoreCase("--notif")) {
                // Format: --notif on/off
                if (i + 1 < args.length) {
                    String value = args[i + 1];
                    if (value.equalsIgnoreCase("on") || value.equalsIgnoreCase("true")) {
                        config.notifications = true;
                        i++; // Skip next argument
                    } else if (value.equalsIgnoreCase("off") || value.equalsIgnoreCase("false")) {
                        config.notifications = false;
                        i++; // Skip next argument
                    } else {
                        sender.sendMessage("§cErreur: --notif doit être suivi de 'on' ou 'off'");
                        return null;
                    }
                } else {
                    sender.sendMessage("§cErreur: --notif doit être suivi de 'on' ou 'off'");
                    return null;
                }
            }
            else if (arg.equalsIgnoreCase("--rewards")) {
                // Format: --rewards on/off
                if (i + 1 < args.length) {
                    String value = args[i + 1];
                    if (value.equalsIgnoreCase("on") || value.equalsIgnoreCase("true")) {
                        config.rewards = true;
                        i++; // Skip next argument
                    } else if (value.equalsIgnoreCase("off") || value.equalsIgnoreCase("false")) {
                        config.rewards = false;
                        i++; // Skip next argument
                    } else {
                        sender.sendMessage("§cErreur: --rewards doit être suivi de 'on' ou 'off'");
                        return null;
                    }
                } else {
                    sender.sendMessage("§cErreur: --rewards doit être suivi de 'on' ou 'off'");
                    return null;
                }
            }
            else if (arg.equalsIgnoreCase("notif")) {
                // Compatibilité avec l'ancien format
                config.notifications = true;
            }
            else if (!arg.startsWith("--")) {
                // Si ce n'est pas un flag, c'est probablement le sous-mode
                if (config.subEvent == null) {
                    config.subEvent = arg;
                } else {
                    sender.sendMessage("§cErreur: Mode déjà défini. Arguments non reconnus: " + arg);
                    return null;
                }
            }
            else {
                sender.sendMessage("§cFlag inconnu: " + arg);
                return null;
            }
        }

        return config;
    }

    /**
     * Affiche l'aide de la commande
     */
    private void showUsage(CommandSender sender) {
        sender.sendMessage("§8§m----§r §6§lCommande /event start §8§m----§r");
        sender.sendMessage("§eUsage:");
        sender.sendMessage("§a/event start §7- Démarre le lobby actuel");
        sender.sendMessage("§a/event start <event> §7- Démarre un événement");
        sender.sendMessage("§a/event start <event> [mode] §7- Avec un mode spécifique");
        sender.sendMessage("");
        sender.sendMessage("§eFlags disponibles:");
        sender.sendMessage("§b--notif on/off §7- Notifications Discord (défaut: §coff§7)");
        sender.sendMessage("§b--rewards on/off §7- Récompenses (défaut: §aon§7)");
        sender.sendMessage("");
        sender.sendMessage("§8Note: §7--rewards off = événement fun sans récompenses");
        sender.sendMessage("§8§m-------------------------§r");
    }

    /**
     * Classe pour stocker la configuration d'un événement
     */
    private static class EventConfig {
        String eventName;
        String subEvent;
        boolean notifications;
        boolean rewards;
    }
}
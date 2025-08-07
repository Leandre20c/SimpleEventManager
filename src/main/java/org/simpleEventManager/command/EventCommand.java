package org.simpleEventManager.command;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.simpleEventManager.SimpleEventManager;
import org.simpleEventManager.command.sub.*;
import org.simpleEventManager.manager.*;

import java.util.*;

public class EventCommand implements CommandExecutor, TabCompleter {

    private final Map<String, SubCommand> subcommands = new HashMap<>();
    private final Map<String, String> commandPermissions = new HashMap<>();
    private final SimpleEventManager plugin;

    public EventCommand(SimpleEventManager plugin) {
        this.plugin = plugin;

        // Initialisation des sous-commandes
        initializeSubcommands();

        // Initialisation des permissions par commande
        initializePermissions();

        // Enregistrement de la commande principale
        PluginCommand command = plugin.getCommand("event");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        }
    }

    /**
     * Initialise toutes les sous-commandes
     */
    private void initializeSubcommands() {
        subcommands.put("start", new StartCommand(plugin));
        subcommands.put("setspawn", new SetSpawnCommand(plugin));
        subcommands.put("rules", new RulesCommand(plugin));
        subcommands.put("stop", new StopCommand(plugin));
        subcommands.put("join", new JoinCommand(plugin));
        subcommands.put("list", new ListCommand(plugin));
        subcommands.put("leave", new LeaveCommand(plugin));
    }

    /**
     * Définit les permissions requises pour chaque commande
     */
    private void initializePermissions() {
        // Commandes joueur
        commandPermissions.put("join", "event.join");
        commandPermissions.put("leave", "event.leave");
        commandPermissions.put("list", "event.list");
        commandPermissions.put("rules", "event.rules");

        // Commandes admin
        commandPermissions.put("start", "event.admin.start");
        commandPermissions.put("stop", "event.admin.stop");
        commandPermissions.put("setspawn", "event.admin.setspawn");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Vérification des arguments
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subcommands.get(subCommandName);

        if (subCommand == null) {
            sender.sendMessage(plugin.getMessageManager().prefixed("unknown-command"));
            sendHelpMessage(sender);
            return true;
        }

        // Vérification des permissions
        if (!hasPermission(sender, subCommandName)) {
            sender.sendMessage(plugin.getMessageManager().prefixed("no-permission"));
            return true;
        }

        // Exécution de la sous-commande
        try {
            return subCommand.execute(sender, args);
        } catch (Exception e) {
            sender.sendMessage(plugin.getMessageManager().prefixed("command-error"));
            plugin.getLogger().severe("Erreur lors de l'exécution de la commande " + subCommandName + ": " + e.getMessage());
            e.printStackTrace();
            return true;
        }
    }

    /**
     * Vérifie si le sender a la permission pour une commande
     */
    private boolean hasPermission(CommandSender sender, String subCommand) {
        String permission = commandPermissions.get(subCommand);

        // Si aucune permission n'est définie, autoriser
        if (permission == null) {
            return true;
        }

        // Si c'est la console, toujours autoriser
        if (!(sender instanceof Player)) {
            return true;
        }

        return sender.hasPermission(permission);
    }

    /**
     * Envoie le message d'aide personnalisé
     */
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage("§8§m----§r §6§lSimpleEventManager §8§m----§r");
        sender.sendMessage("§e/event join §8- §7Rejoindre un événement");
        sender.sendMessage("§e/event leave §8- §7Quitter un événement");
        sender.sendMessage("§e/event list §8- §7Voir les participants");
        sender.sendMessage("§e/event rules §8- §7Voir les règles");

        // Afficher les commandes admin seulement si le joueur a les permissions
        if (sender.hasPermission("event.admin") || sender.hasPermission("event.admin.start")) {
            sender.sendMessage("§c§lCommandes Admin:");
            sender.sendMessage("§c/event start <event> §8- §7Démarrer un événement");
            sender.sendMessage("§c/event stop §8- §7Arrêter un événement");
            sender.sendMessage("§c/event setspawn <location> §8- §7Définir un spawn");
        }

        sender.sendMessage("§8§m-------------------------§r");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return subcommands.keySet().stream()
                    .filter(name -> hasPermission(sender, name)) // Filtrer par permissions
                    .filter(name -> name.startsWith(partial))
                    .sorted()
                    .toList();
        }

        // Tab completion pour setspawn
        if (args.length == 2 && args[0].equalsIgnoreCase("setspawn") &&
                hasPermission(sender, "setspawn")) {
            List<String> completions = new ArrayList<>();
            completions.add("lobby");

            plugin.getEventLoader().getAllEvents().forEach(event ->
                    completions.add(event.getEventName().toLowerCase())
            );

            return completions.stream()
                    .filter(completion -> completion.startsWith(args[1].toLowerCase()))
                    .sorted()
                    .toList();
        }

        // Tab completion pour start
        if (args.length == 2 && args[0].equalsIgnoreCase("start") &&
                hasPermission(sender, "start")) {
            List<String> completions = new ArrayList<>();

            plugin.getEventLoader().getAllEvents().forEach(event ->
                    completions.add(event.getEventName().toLowerCase())
            );

            return completions.stream()
                    .filter(completion -> completion.startsWith(args[1].toLowerCase()))
                    .sorted()
                    .toList();
        }

        return Collections.emptyList();
    }

    /**
     * Retourne la liste des sous-commandes disponibles pour un sender
     */
    public List<String> getAvailableCommands(CommandSender sender) {
        return subcommands.keySet().stream()
                .filter(command -> hasPermission(sender, command))
                .sorted()
                .toList();
    }
}
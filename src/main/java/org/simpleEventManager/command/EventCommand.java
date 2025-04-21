package org.simpleEventManager.command;

import org.bukkit.command.*;
import org.simpleEventManager.SimpleEventManager;
import org.simpleEventManager.command.sub.*;
import org.simpleEventManager.manager.*;

import java.util.*;

public class EventCommand implements CommandExecutor, TabCompleter {

    private final Map<String, SubCommand> subcommands = new HashMap<>();
    private final SimpleEventManager plugin;

    public EventCommand(SimpleEventManager plugin) {
        this.plugin = plugin;

        subcommands.put("start", new StartCommand(plugin));
        subcommands.put("setspawn", new SetSpawnCommand(plugin));
        subcommands.put("rules", new RulesCommand(plugin));
        subcommands.put("stop", new StopCommand(plugin));
        subcommands.put("join", new JoinCommand(plugin));
        subcommands.put("list", new ListCommand(plugin));
        subcommands.put("leave", new LeaveCommand(plugin));


        PluginCommand command = plugin.getCommand("event");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(plugin.getMessageManager().get("usage"));
            return true;
        }

        SubCommand sub = subcommands.get(args[0].toLowerCase());
        if (sub != null) {
            return sub.execute(sender, args);
        }

        sender.sendMessage(plugin.getMessageManager().prefixed("unknown-command"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return subcommands.keySet().stream()
                    .filter(name -> name.startsWith(partial))
                    .sorted()
                    .toList();
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("setspawn")) {
            List<String> completions = new ArrayList<>();
            completions.add("lobby");
            plugin.getEventLoader().getAllEvents().forEach(event ->
                    completions.add(event.getEventName().toLowerCase())
            );
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("start")) {
            List<String> completions = new ArrayList<>();
            plugin.getEventLoader().getAllEvents().forEach(event ->
                    completions.add(event.getEventName().toLowerCase())
            );
            return completions;
        }

        return Collections.emptyList();
    }

}

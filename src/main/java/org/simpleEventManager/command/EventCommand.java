package org.simpleEventManager.command;

import org.bukkit.command.*;
import org.simpleEventManager.SimpleEventManager;
import org.simpleEventManager.command.sub.*;

import java.util.HashMap;
import java.util.Map;

public class EventCommand implements CommandExecutor {

    private final Map<String, SubCommand> subcommands = new HashMap<>();

    public EventCommand(SimpleEventManager plugin) {
        subcommands.put("start", new StartCommand(plugin));
        subcommands.put("setspawn", new SetSpawnCommand(plugin));
        subcommands.put("rules", new RulesCommand(plugin));
        subcommands.put("stop", new StopCommand(plugin));
        subcommands.put("join", new JoinCommand(plugin));
        subcommands.put("list", new ListCommand(plugin));


        plugin.getCommand("event").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§eUtilise /event <start|setspawn|rules|stop>");
            return true;
        }

        SubCommand sub = subcommands.get(args[0].toLowerCase());
        if (sub != null) {
            return sub.execute(sender, args);
        }

        sender.sendMessage("§cCommande inconnue.");
        return true;
    }
}

package org.simpleEventManager.command.sub;

import org.bukkit.command.CommandSender;
import org.simpleEventManager.SimpleEventManager;
import org.simpleEventManager.api.EventGame;

public class ListCommand implements SubCommand {

    private final SimpleEventManager plugin;

    public ListCommand(SimpleEventManager plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        var events = plugin.getEventLoader().getAllEvents();

        if (events.isEmpty()) {
            sender.sendMessage("§eIl n'y a pas d'event chargé.");
            return true;
        }

        sender.sendMessage("§6§lEvents disponibles:");
        for (EventGame event : events) {
            sender.sendMessage(" §e• §a" + event.getEventName() + "§7 - " + event.getEventDescription());
        }
        return true;
    }
}

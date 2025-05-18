package org.simpleEventManager.command.sub;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleEventManager.SimpleEventManager;

import java.util.ArrayList;
import java.util.List;

public class SetSpawnCommand implements SubCommand {

    private final SimpleEventManager plugin;

    public SetSpawnCommand(SimpleEventManager plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().prefixed("only-players"));
            return true;
        }

        if (!sender.hasPermission("event.admin")) {
            sender.sendMessage(plugin.getMessageManager().prefixed("no-permission"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /event setspawn <lobby|event_name>");
            return true;
        }

        String target = args[1].toLowerCase();
        Location loc = player.getLocation();

        if (target.equals("lobby")) {
            plugin.getConfig().set("event-lobby.world", loc.getWorld().getName());
            plugin.getConfig().set("event-lobby.x", loc.getX());
            plugin.getConfig().set("event-lobby.y", loc.getY());
            plugin.getConfig().set("event-lobby.z", loc.getZ());
            plugin.getConfig().set("event-lobby.yaw", loc.getYaw());
            plugin.getConfig().set("event-lobby.pitch", loc.getPitch());
            plugin.saveConfig();
            player.sendMessage("§aSpawn du lobby défini !");
        } else {
            String path = "event-spawns." + target;
            plugin.getConfig().set(path + ".world", loc.getWorld().getName());
            plugin.getConfig().set(path + ".x", loc.getX());
            plugin.getConfig().set(path + ".y", loc.getY());
            plugin.getConfig().set(path + ".z", loc.getZ());
            plugin.getConfig().set(path + ".yaw", loc.getYaw());
            plugin.getConfig().set(path + ".pitch", loc.getPitch());
            plugin.saveConfig();
            player.sendMessage("§aSpawn de l’événement §e" + target + " §adéfini !");
        }

        return true;
    }
}

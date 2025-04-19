package org.simpleEventManager.command.sub;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.simpleEventManager.SimpleEventManager;

public class SetSpawnCommand implements SubCommand {

    private final SimpleEventManager plugin;

    public SetSpawnCommand(SimpleEventManager plugin) {
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

        if (args.length < 2 || !args[1].equalsIgnoreCase("lobby")) {
            player.sendMessage("§cUsage: /event setspawn lobby");
            return true;
        }

        Location loc = player.getLocation();
        plugin.getConfig().set("event-lobby.world", loc.getWorld().getName());
        plugin.getConfig().set("event-lobby.x", loc.getX());
        plugin.getConfig().set("event-lobby.y", loc.getY());
        plugin.getConfig().set("event-lobby.z", loc.getZ());
        plugin.getConfig().set("event-lobby.yaw", loc.getYaw());
        plugin.getConfig().set("event-lobby.pitch", loc.getPitch());
        plugin.saveConfig();

        player.sendMessage("§aSpawn du lobby défini !");
        return true;
    }
}

package org.simpleEventManager.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.simpleEventManager.SimpleEventManager;

import java.util.List;

public class EventUtils {

    public static Location getEventSpawnLocation(SimpleEventManager plugin, String eventName) {
        FileConfiguration config = plugin.getConfig();
        String path = "event-spawns." + eventName.toLowerCase();

        if (!config.contains(path + ".world")) {
            plugin.getLogger().warning("[SimpleEventManager] Aucun spawn défini pour " + eventName);
            return null;
        }

        return new Location(
                Bukkit.getWorld(config.getString(path + ".world")),
                config.getDouble(path + ".x"),
                config.getDouble(path + ".y"),
                config.getDouble(path + ".z"),
                (float) config.getDouble(path + ".yaw"),
                (float) config.getDouble(path + ".pitch")
        );
    }

    public static void teleportToEventSpawn(SimpleEventManager plugin, String eventName, List<Player> players) {
        Location spawn = getEventSpawnLocation(plugin, eventName);
        if (spawn == null) {
            plugin.getLogger().warning("§cImpossible de téléporter les joueurs : aucun spawn pour " + eventName);
            return;
        }

        for (Player player : players) {
            player.teleport(spawn);
        }
    }
}

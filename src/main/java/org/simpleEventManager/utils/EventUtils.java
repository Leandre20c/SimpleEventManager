package org.simpleEventManager.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.simpleEventManager.SimpleEventManager;

public class EventUtils {

    public static Location getLobbyLocation(SimpleEventManager plugin) {
        FileConfiguration config = plugin.getConfig();
        if (!config.contains("event-lobby.world")) return null;
        return new Location(
                Bukkit.getWorld(config.getString("event-lobby.world")),
                config.getDouble("event-lobby.x"),
                config.getDouble("event-lobby.y"),
                config.getDouble("event-lobby.z"),
                (float) config.getDouble("event-lobby.yaw"),
                (float) config.getDouble("event-lobby.pitch")
        );
    }

    public static Location getEventSpawnLocation(SimpleEventManager plugin, String eventName) {
        FileConfiguration config = plugin.getConfig();
        String path = "event-spawns." + eventName.toLowerCase();

        if (!config.contains(path + ".world")) return null;
        return new Location(
                Bukkit.getWorld(config.getString(path + ".world")),
                config.getDouble(path + ".x"),
                config.getDouble(path + ".y"),
                config.getDouble(path + ".z"),
                (float) config.getDouble(path + ".yaw"),
                (float) config.getDouble(path + ".pitch")
        );
    }
}

package org.simpleEventManager.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.simpleEventManager.SimpleEventManager;

import java.util.List;

public class EventUtils {

    private static final SimpleEventManager plugin =
            (SimpleEventManager) Bukkit.getPluginManager().getPlugin("SimpleEventManager");

    public static Location getEventSpawnLocation(String eventName) {
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

    public static Location getEventSpawnLocation() {
        return getEventSpawnLocation(getCallingEventName());
    }

    public static void teleportToEventSpawn(List<Player> players) {
        Location spawn = getEventSpawnLocation();
        if (spawn == null) {
            Bukkit.getLogger().warning("[SimpleEventManager] Aucune location trouvée pour le spawn de l’event.");
            return;
        }
        for (Player player : players) {
            player.teleport(spawn);
        }
    }

    private static String getCallingEventName() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stack) {
            String className = element.getClassName();
            try {
                Class<?> clazz = Class.forName(className);
                Package pkg = clazz.getPackage();
                if (pkg != null && pkg.getName().startsWith("org.")) {
                    return pkg.getName().split("\\.")[1].replace("EventGame", "").toLowerCase();
                }
            } catch (ClassNotFoundException ignored) {}
        }
        return "unknown";
    }
}

package org.simpleEventManager.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class EventScheduler extends BukkitRunnable {
    private final Plugin plugin;
    private final Map<String, java.util.List<String>> schedule;

    public EventScheduler(Plugin plugin, Map<String, java.util.List<String>> schedule) {
        this.plugin = plugin;
        this.schedule = schedule;
    }

    @Override
    public void run() {
        String now = LocalTime.now().format(DateTimeFormatter.ofPattern("H:mm"));
        for (Map.Entry<String, java.util.List<String>> entry : schedule.entrySet()) {
            if (entry.getValue().contains(now)) {
                Bukkit.broadcastMessage("§e[EventManager] Prochain event : " + entry.getKey() + " commence dans 10 minutes !");
                // TODO : ouvrir le lobby et lancer l'event plus tard
            }
        }
    }
}

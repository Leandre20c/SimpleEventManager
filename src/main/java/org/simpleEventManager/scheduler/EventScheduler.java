package org.simpleEventManager.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;
import org.simpleEventManager.SimpleEventManager;
import org.simpleEventManager.api.EventGame;
import org.simpleEventManager.utils.LobbyStarter;

import java.time.LocalTime;
import java.util.List;

public class EventScheduler extends BukkitRunnable {

    private final SimpleEventManager plugin;

    public EventScheduler(SimpleEventManager plugin) {
        this.plugin = plugin;
    }

    public void start() {
        runTaskTimer(plugin, 20L, 20L * 60); // toutes les minutes
    }

    @Override
    public void run() {
        LocalTime now = LocalTime.now();
        ConfigurationSection scheduleSection = plugin.getConfig().getConfigurationSection("schedule");

        if (scheduleSection == null) {
            Bukkit.getLogger().warning("[SimpleEventManager] 'schedule' section is missing in config.yml");
            return;
        }

        for (String eventName : scheduleSection.getKeys(false)) {
            List<String> times = plugin.getConfig().getStringList("schedule." + eventName);
            for (String time : times) {
                String[] split = time.split(":");
                if (split.length != 2) continue;

                int hour = Integer.parseInt(split[0]);
                int minute = Integer.parseInt(split[1]);

                if (now.getHour() == hour && now.getMinute() == minute) {
                    EventGame game;

                    if (eventName.equalsIgnoreCase("RANDOM_EVENT")) {
                        List<EventGame> events = plugin.getEventLoader().getAllEvents();
                        if (events.isEmpty()) return;

                        game = events.get((int) (Math.random() * events.size()));
                        Bukkit.broadcastMessage("§e[RANDOM] Un événement aléatoire a été sélectionné : §6" + game.getEventName());
                    } else {
                        game = plugin.getEventLoader().getEventByName(eventName);
                    }

                    if (game != null) {
                        LobbyStarter.startLobbyWithCountdown(plugin, game, true);
                    }
                }
            }
        }
    }
}

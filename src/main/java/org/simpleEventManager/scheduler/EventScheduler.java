package org.simpleEventManager.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.simpleEventManager.SimpleEventManager;
import org.simpleEventManager.api.EventGame;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EventScheduler extends BukkitRunnable {

    private final SimpleEventManager plugin;

    public EventScheduler(SimpleEventManager plugin) {
        this.plugin = plugin;
    }

    public void start() {
        runTaskTimer(plugin, 20L, 20L * 60); // 1 min
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
                    EventGame game = plugin.getEventLoader().getEventByName(eventName);
                    if (game != null) {
                        plugin.getLobbyState().openLobby();
                        plugin.setCurrentGame(game);
                        Bukkit.broadcastMessage("§aL’événement §e" + eventName + "§a commencera dans 10 minutes ! Faites §e/event join§a.");

                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            if (plugin.getLobbyState().isLobbyOpen()) {
                                Set<Player> participants = plugin.getParticipantManager().getOnlineParticipants();
                                plugin.getLobbyState().closeLobby();

                                if (participants.size() >= 2) {
                                    game.start(new ArrayList<>(participants));
                                    Bukkit.broadcastMessage("§aL'événement §e" + eventName + "§a démarre avec §e" + participants.size() + " joueurs§a !");
                                } else {
                                    Bukkit.broadcastMessage("§cL'événement §e" + eventName + "§c a été annulé par manque de participants.");
                                    plugin.getParticipantManager().clear();
                                }
                            }
                        }, 20L * 60 * 10); // 10 minutes
                    }
                }
            }
        }
    }
}

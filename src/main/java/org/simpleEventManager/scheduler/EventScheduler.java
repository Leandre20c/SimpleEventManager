package org.simpleEventManager.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
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
        runTaskTimer(plugin, 20L, 20L * 60);
    }

    @Override
    public void run() {
        LocalTime now = LocalTime.now();
        plugin.getConfig().getConfigurationSection("schedule").getKeys(false).forEach(eventName -> {
            List<String> times = plugin.getConfig().getStringList("schedule." + eventName);
            for (String time : times) {
                String[] split = time.split(":");
                int hour = Integer.parseInt(split[0]);
                int minute = Integer.parseInt(split[1]);

                // À l'intérieur de EventScheduler.java (run méthode)
                if (now.getHour() == hour && now.getMinute() == minute) {
                    EventGame game = plugin.getEventLoader().getEventByName(eventName);
                    if (game != null) {
                        plugin.getLobbyState().openLobby();
                        plugin.setCurrentGame(game);
                        Bukkit.broadcastMessage("§aL’événement " + eventName + " commencera dans 10 minutes ! Faites /event join.");

                        // 10 minutes auto-start
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            if (plugin.getLobbyState().isLobbyOpen()) {
                                Set<Player> participants = plugin.getParticipantManager().getOnlineParticipants();
                                plugin.getLobbyState().closeLobby();
                                if (participants.size() >= 2) {
                                    game.start(new ArrayList<>(participants));
                                    Bukkit.broadcastMessage("§aL'événement §e" + eventName + "§a démarre avec §e" + participants.size() + " joueurs§a !");
                                } else {
                                    Bukkit.broadcastMessage("§cL'événement " + eventName + " a été annulé par manque de participants.");
                                    plugin.getParticipantManager().clear();
                                }
                            }
                        }, 12000L); // 10 min
                    }
                }

            }
        });
    }
}

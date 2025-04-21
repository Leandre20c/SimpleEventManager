package org.simpleEventManager.manager;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.simpleEventManager.SimpleEventManager;

import java.util.List;
import java.util.Set;

public class RewardManager {

    private final SimpleEventManager plugin;

    public RewardManager(SimpleEventManager plugin) {
        this.plugin = plugin;
    }

    public void distribute(List<Player> winners) {
        FileConfiguration config = plugin.getConfig();

        int place = 1;
        for (Player player : winners) {
            String cmd = config.getString("rewards." + place, config.getString("rewards.default"));
            if (cmd != null) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
            }
            place++;
        }

        // Petite récompense pour les autres ?
        Set<Player> all = plugin.getParticipantManager().getOnlineParticipants();
        all.removeAll(winners);
        for (Player player : all) {
            String cmd = config.getString("rewards.default");
            if (cmd != null) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
            }
        }
    }
}

package org.simpleEventManager.manager;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.simpleEventManager.SimpleEventManager;

import java.util.*;

public class RewardManager {

    private final SimpleEventManager plugin;

    public RewardManager(SimpleEventManager plugin) {
        this.plugin = plugin;
    }

    public void distribute(List<Player> winners) {
        if (winners == null || winners.isEmpty()) return;

        FileConfiguration config = plugin.getConfig();
        ConfigurationSection rewardSection = config.getConfigurationSection("rewards");
        if (rewardSection == null) return;

        for (int i = 0; i < winners.size(); i++) {
            Player player = winners.get(i);
            int rank = i + 1;

            List<String> commands = rewardSection.getStringList(String.valueOf(rank));
            if (commands.isEmpty()) {
                commands = rewardSection.getStringList("default");
            }

            for (String cmd : commands) {
                if (cmd != null && !cmd.isEmpty()) {
                    cmd = cmd.replace("%player%", player.getName());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }
            }
        }
    }
}

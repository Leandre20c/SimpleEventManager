package org.simpleEventManager.manager;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.simpleEventManager.SimpleEventManager;

import java.util.ArrayList;
import java.util.List;

public class RewardManager {

    private final SimpleEventManager plugin;

    public RewardManager(SimpleEventManager plugin) {
        this.plugin = plugin;
    }

    public void distribute(List<Player> winners) {
        if (winners == null || winners.isEmpty()) return;

        FileConfiguration config = plugin.getConfig(); // ou plugin.getRewardConfig() si tu gères rewards.yml séparément
        List<Player> modifiableWinners = new ArrayList<>(winners);

        for (int i = 0; i < modifiableWinners.size(); i++) {
            Player player = modifiableWinners.get(i);
            String command;

            if (config.contains("rewards." + (i + 1))) {
                command = config.getString("rewards." + (i + 1));
            } else {
                command = config.getString("rewards.default");
            }

            if (command != null) {
                command = command.replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
        }
    }
}

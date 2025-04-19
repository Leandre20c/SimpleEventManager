package org.simpleEventManager.reward;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class RewardDistributor {

    public static void distribute(List<Player> winners, List<Player> allParticipants, List<String> topCommands, String defaultCommand) {
        for (int i = 0; i < winners.size() && i < topCommands.size(); i++) {
            String cmd = topCommands.get(i).replace("%player%", winners.get(i).getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }

        for (Player player : allParticipants) {
            if (!winners.contains(player)) {
                String cmd = defaultCommand.replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        }
    }
}

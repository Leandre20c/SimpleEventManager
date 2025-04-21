package org.simpleEventManager.manager;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.simpleEventManager.SimpleEventManager;
import org.simpleEventManager.api.EventGame;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EventController {

    private final SimpleEventManager plugin;

    public EventController(SimpleEventManager plugin) {
        this.plugin = plugin;
    }

    public void endEvent(EventGame game) {
        Bukkit.broadcastMessage("§cL’événement §e" + game.getEventName() + " §cvient de se terminer !");

        List<Player> winners = new ArrayList<>();
        if (game.hasWinner()) {
            winners = game.getWinners();
            if (!winners.isEmpty()) {
                Bukkit.broadcastMessage("§6§l🏆 Classement des gagnants :");
                for (int i = 0; i < winners.size(); i++) {
                    Player winner = winners.get(i);
                    Bukkit.broadcastMessage("§e#" + (i + 1) + " §f" + winner.getName());
                }
            }
        }

        // Reset & téléportation
        for (Player player : plugin.getParticipantManager().getOnlineParticipants()) {
            resetPlayer(player);
            player.performCommand("warp spawn");

            player.sendMessage("§eVous avez été téléporté au spawn. Merci d’avoir participé !");
        }

        // Récompenses
        RewardManager rewardManager = new RewardManager(plugin);
        rewardManager.distribute(winners);

        plugin.getParticipantManager().clear();
        plugin.setCurrentGame(null);
        plugin.getLobbyState().closeLobby();
    }

    private void resetPlayer(Player player) {
        player.getInventory().clear();
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.setFallDistance(0);
        player.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
    }
}

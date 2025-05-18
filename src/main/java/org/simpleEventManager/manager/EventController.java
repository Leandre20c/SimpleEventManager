package org.simpleEventManager.manager;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.simpleEventManager.SimpleEventManager;
import org.simpleEventManager.api.EventGame;

import java.util.ArrayList;
import java.util.List;

public class EventController {

    private final SimpleEventManager plugin;

    public EventController(SimpleEventManager plugin) {
        this.plugin = plugin;
    }

    public void endEvent(EventGame game) {
        List<Player> winners = new ArrayList<>(game.getWinners());

        for (Player player : winners) {
            plugin.getWinManager().addWin(player.getUniqueId(), game.getEventName());
        }

        game.stop();

        Bukkit.broadcastMessage("§cL’événement §e" + game.getEventName() + " §cvient de se terminer !");

        if (!winners.isEmpty()) {
            Bukkit.broadcastMessage("§6§l🏆 Classement des gagnants :");
            for (int i = 0; i < winners.size(); i++) {
                Player w = winners.get(i);
                Bukkit.broadcastMessage("§e#" + (i + 1) + " §f" + w.getName());
            }
        }

        for (Player player : plugin.getParticipantManager().getOnlineParticipants()) {
            resetPlayer(player);
            player.performCommand("spawn");
            player.sendMessage("§eVous avez été téléporté au spawn. Merci d’avoir participé !");
        }

        new RewardManager(plugin).distribute(winners);

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

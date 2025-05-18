package org.simpleEventManager.utils;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.simpleEventManager.SimpleEventManager;
import org.simpleEventManager.api.EventGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LobbyStarter {

    private static BossBar activeBossBar = null;
    private static BukkitTask activeCountdownTask = null;

    public static void startLobbyWithCountdown(SimpleEventManager plugin, EventGame game) {
        cancelCountdown(); // stop tout précédent

        int waitTimeSeconds = plugin.getConfig().getInt("lobby-wait-time", 5) * 60;

        plugin.getLobbyState().openLobby();
        plugin.setCurrentGame(game);
        plugin.getParticipantManager().clear();

        String displayName = game.getEventName();
        String mode = game.getMode();
        if (mode != null && !mode.equals("default")) {
            displayName += " (§f" + mode + "§e)";
        }

        Bukkit.broadcastMessage("§aUn événement §e" + displayName + "§a va bientôt commencer !");

        Bukkit.broadcastMessage("§7Début dans §e" + (waitTimeSeconds / 60) + " §7minute(s). Faites §a/event join");

        BossBar bossBar = Bukkit.createBossBar("§aDébut dans §e" + (waitTimeSeconds / 60) + ":00", BarColor.GREEN, BarStyle.SOLID);
        bossBar.setVisible(true);
        plugin.getParticipantManager().getOnlineParticipants().forEach(bossBar::addPlayer);
        activeBossBar = bossBar;

        activeCountdownTask = new BukkitRunnable() {
            int remaining = waitTimeSeconds;

            @Override
            public void run() {
                if (remaining <= 0) {
                    cancelBossBar();
                    launchEvent(plugin, game);
                    cancel();
                    return;
                }

                int min = remaining / 60;
                int sec = remaining % 60;
                bossBar.setTitle("§aDébut dans §e" + min + "§f:" + String.format("%02d", sec));
                bossBar.setProgress(remaining / (double) waitTimeSeconds);
                bossBar.getPlayers().forEach(bossBar::removePlayer);
                plugin.getParticipantManager().getOnlineParticipants().forEach(bossBar::addPlayer);
                remaining--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public static void cancelBossBar() {
        if (activeBossBar != null) {
            activeBossBar.removeAll();
            activeBossBar = null;
        }
    }

    public static void cancelCountdown() {
        cancelBossBar();
        if (activeCountdownTask != null) {
            activeCountdownTask.cancel();
            activeCountdownTask = null;
        }
    }

    public static void launchEvent(SimpleEventManager plugin, EventGame game) {
        cancelCountdown();
        plugin.getLobbyState().closeLobby();

        Set<Player> participants = plugin.getParticipantManager().getOnlineParticipants();

        if (participants.size() >= 2) {
            game.start(new ArrayList<>(participants));
            Bukkit.broadcastMessage("§aL'événement §e" + game.getEventName() +
                    "§a commence avec §e" + participants.size() + " joueurs§a !");
        } else {
            Bukkit.broadcastMessage("§cL’événement " + game.getEventName() + " a été annulé : pas assez de joueurs.");
            plugin.getParticipantManager().clear();
            plugin.setCurrentGame(null);
        }
    }
}

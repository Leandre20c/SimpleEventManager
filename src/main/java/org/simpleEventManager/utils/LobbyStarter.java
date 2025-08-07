package org.simpleEventManager.utils;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.classyclanchallenges.sDMM.MessageType;
import org.classyclanchallenges.sDMM.api.DiscordAPI;
import org.simpleEventManager.SimpleEventManager;
import org.simpleEventManager.api.EventGame;

import java.util.ArrayList;
import java.util.Set;

public class LobbyStarter {

    private static BossBar activeBossBar = null;
    private static BukkitTask activeCountdownTask = null;

    public static void startLobbyWithCountdown(SimpleEventManager plugin, EventGame game, boolean notifyDiscord) {
        cancelCountdown();

        int waitTimeSeconds = plugin.getConfig().getInt("lobby-wait-time", 5) * 60;

        plugin.getLobbyState().openLobby();
        plugin.setCurrentGame(game);
        plugin.getParticipantManager().clear();

        final String displayName = buildDisplayName(game);

        // Méthode helper pour construire le nom d'affichage

        // Messages d'annonce adaptés selon le type d'événement
        String eventType = game.isRewardsEnabled() ? "Un événement" : "Un événement fun";
        Bukkit.broadcastMessage("§a" + eventType + " §e" + displayName + "§a va bientôt commencer !");
        Bukkit.broadcastMessage("§7Début dans §e" + (waitTimeSeconds / 60) + " §7minute(s). Faites §a/event join");

        // 🔔 Envoi Discord si demandé
        if (notifyDiscord) {
            try {
                String discordTitle = game.isRewardsEnabled() ?
                        "Event " + game.getEventName() :
                        "Event Fun " + game.getEventName();

                String discordMessage = "Commence dans " + (waitTimeSeconds / 60) + " minute(s).\nUtilise `/event join` pour participer !";
                if (!game.isRewardsEnabled()) {
                    discordMessage += "\n🎉 Événement fun - pas de récompenses, juste pour le plaisir !";
                }

                DiscordAPI.sendDiscordMessage(
                        MessageType.EVENT,
                        discordTitle,
                        discordMessage
                );
            } catch (Exception e) {
                plugin.getLogger().warning("❌ Impossible d'envoyer le message Discord : " + e.getMessage());
            }
        }

        // Boss bar optimisée avec informations complètes
        String initialTitle = formatBossBarTitle(waitTimeSeconds / 60, 0, 0);
        BossBar bossBar = Bukkit.createBossBar(initialTitle, BarColor.GREEN, BarStyle.SEGMENTED_10);
        bossBar.setVisible(true);

        // Boss bar uniquement pour les participants du lobby
        updateBossBarPlayers(bossBar, plugin);
        activeBossBar = bossBar;

        activeCountdownTask = new BukkitRunnable() {
            int remaining = waitTimeSeconds;
            int lastParticipantCount = 0;

            @Override
            public void run() {
                if (remaining <= 0) {
                    cancelBossBar();
                    launchEvent(plugin, game);
                    cancel();
                    return;
                }

                // Obtenir le nombre actuel de participants
                int currentParticipants = plugin.getParticipantManager().getOnlineCount();
                int min = remaining / 60;
                int sec = remaining % 60;

                // Mettre à jour la boss bar avec le nombre de joueurs
                String newTitle = formatBossBarTitle(min, sec, currentParticipants);
                bossBar.setTitle(newTitle);
                bossBar.setProgress(remaining / (double) waitTimeSeconds);

                // Changer la couleur selon le nombre de participants
                updateBossBarColor(bossBar, currentParticipants);

                // Mettre à jour la boss bar uniquement pour les participants
                updateBossBarPlayers(bossBar, plugin);

                // Annoncer les changements de participants
                if (currentParticipants != lastParticipantCount) {
                    if (currentParticipants > lastParticipantCount) {
                        // Message pour les participants seulement (tous les 5 ou dans la dernière minute)
                        if (currentParticipants % 5 == 0 || remaining <= 60) {
                            String participantMessage = "§8[§6Event§8] §e" + currentParticipants + " joueur" +
                                    (currentParticipants > 1 ? "s" : "") + " inscrit" +
                                    (currentParticipants > 1 ? "s" : "") + " !";
                            broadcastToParticipants(plugin, participantMessage);
                        }
                    }
                    lastParticipantCount = currentParticipants;
                }

                // Annonces temporelles pour TOUT LE SERVEUR
                String eventTypePrefix = game.isRewardsEnabled() ? "Événement" : "Événement fun";

                if (remaining == 300) { // 5 minutes
                    broadcastToNonParticipants(plugin, "§e⚡ " + eventTypePrefix + " §a" + displayName + " §edans 5 minutes ! §b/event join");
                } else if (remaining == 240) { // 4 minutes
                    broadcastToNonParticipants(plugin, "§e⚡ " + eventTypePrefix + " §a" + displayName + " §edans 4 minutes ! §b/event join");
                } else if (remaining == 180) { // 3 minutes
                    broadcastToNonParticipants(plugin, "§e⚡ " + eventTypePrefix + " §a" + displayName + " §edans 3 minutes ! §b/event join");
                } else if (remaining == 120) { // 2 minutes
                    broadcastToNonParticipants(plugin, "§6⏰ " + eventTypePrefix + " §a" + displayName + " §6dans 2 minutes ! §b/event join");
                    broadcastToParticipants(plugin, "§6⏰ Plus que 2 minutes ! Préparez-vous !");
                } else if (remaining == 60) { // 1 minute
                    broadcastToNonParticipants(plugin, "§c⚡ " + eventTypePrefix + " §a" + displayName + " §cdans 1 minute ! Dernière chance §b/event join");
                    broadcastToParticipants(plugin, "§c⚡ DERNIÈRE MINUTE ! L'événement commence bientôt !");
                } else if (remaining == 30) { // 30 secondes
                    Bukkit.broadcastMessage("§4§l⚠ " + eventTypePrefix.toUpperCase() + " " + displayName.toUpperCase() + " DANS 30 SECONDES ! ⚠");
                } else if (remaining <= 10 && remaining > 0) { // Décompte final
                    Bukkit.broadcastMessage("§4§l" + remaining + "...");
                }

                remaining--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    /**
     * Formate le titre de la boss bar avec timer et nombre de joueurs
     */
    private static String formatBossBarTitle(int minutes, int seconds, int playerCount) {
        String timeStr = minutes + "§f:" + String.format("%02d", seconds);
        String playersStr = playerCount + " joueur" + (playerCount > 1 ? "s" : "");

        return "§a⏱ Début dans §e" + timeStr + " §8| §b" + playersStr + " §8| §7Prêt pour l'action !";
    }

    /**
     * Met à jour la couleur de la boss bar selon le nombre de participants
     */
    private static void updateBossBarColor(BossBar bossBar, int participants) {
        if (participants >= 20) {
            bossBar.setColor(BarColor.PURPLE); // Événement très populaire
        } else if (participants >= 10) {
            bossBar.setColor(BarColor.BLUE); // Bon nombre de participants
        } else if (participants >= 5) {
            bossBar.setColor(BarColor.GREEN); // Correct
        } else if (participants >= 2) {
            bossBar.setColor(BarColor.YELLOW); // Minimum viable
        } else {
            bossBar.setColor(BarColor.RED); // Pas assez
        }
    }

    /**
     * Met à jour les joueurs qui voient la boss bar (uniquement les participants)
     */
    private static void updateBossBarPlayers(BossBar bossBar, SimpleEventManager plugin) {
        // Retirer tous les joueurs actuels
        bossBar.getPlayers().forEach(bossBar::removePlayer);

        // Ajouter uniquement les participants du lobby
        Set<Player> participants = plugin.getParticipantManager().getOnlineParticipants();
        participants.forEach(bossBar::addPlayer);
    }

    /**
     * Diffuse un message uniquement aux participants de l'événement
     */
    private static void broadcastToParticipants(SimpleEventManager plugin, String message) {
        Set<Player> participants = plugin.getParticipantManager().getOnlineParticipants();
        for (Player participant : participants) {
            participant.sendMessage(message);
        }
    }

    /**
     * Diffuse un message à tous les joueurs SAUF les participants
     */
    private static void broadcastToNonParticipants(SimpleEventManager plugin, String message) {
        Set<Player> participants = plugin.getParticipantManager().getOnlineParticipants();

        for (Player player : Bukkit.getOnlinePlayers()) {
            // Envoyer seulement aux joueurs qui ne participent PAS à l'événement
            if (!participants.contains(player)) {
                player.sendMessage(message);
            }
        }
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
        final String displayName = buildDisplayName(game);

        if (participants.size() >= 2) {
            game.start(new ArrayList<>(participants));
            Bukkit.broadcastMessage("§a🎮 L'événement §e" + displayName +
                    "§a commence avec §e" + participants.size() + " joueur" +
                    (participants.size() > 1 ? "s" : "") + "§a ! Bonne chance à tous !");
        } else {
            Bukkit.broadcastMessage("§c❌ L'événement " + displayName + " a été annulé : pas assez de joueurs.");

            // Téléporter les participants restants au spawn
            for (Player participant : participants) {
                participant.sendMessage("§cÉvénement annulé. Tu es renvoyé au spawn.");
                // Simple téléportation avec commande spawn
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawn " + participant.getName());
            }

            plugin.getParticipantManager().clear();
            plugin.setCurrentGame(null);
        }
    }

    /**
     * Construit le nom d'affichage de l'événement avec le mode s'il existe
     */
    private static String buildDisplayName(EventGame game) {
        String displayName = game.getEventName();
        String mode = game.getMode();
        if (mode != null && !mode.equals("default")) {
            displayName += " (§f" + mode + "§e)";
        }
        return displayName;
    }
}
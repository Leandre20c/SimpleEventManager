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

        // Gérer les récompenses et statistiques selon la configuration
        boolean rewardsEnabled = game.isRewardsEnabled();

        if (rewardsEnabled && !winners.isEmpty()) {
            // Ajouter la victoire aux statistiques seulement si les récompenses sont activées
            plugin.getWinManager().addWin(winners.get(0).getUniqueId(), game.getEventName());
        }

        game.stop();

        // Message de fin différent selon la configuration
        String eventDescription = game.getEventName();
        if (rewardsEnabled) {
            Bukkit.broadcastMessage("§cL'événement §e" + eventDescription + " §cvient de se terminer !");
        } else {
            Bukkit.broadcastMessage("§cL'événement fun §e" + eventDescription + " §cvient de se terminer !");
        }

        if (!winners.isEmpty()) {
            Bukkit.broadcastMessage("§6§l🏆 Classement des gagnants :");
            for (int i = 0; i < winners.size(); i++) {
                Player w = winners.get(i);
                if (w != null && w.isOnline()) {
                    String trophy = getTrophyIcon(i);
                    String statusIndicator = plugin.getParticipantManager().hasManuallyLeft(w) ? " §7(parti tôt)" : "";
                    Bukkit.broadcastMessage("§e" + trophy + " #" + (i + 1) + " §f" + w.getName() + statusIndicator);
                }
            }

            // Message différent selon le type d'événement
            if (rewardsEnabled) {
                Bukkit.broadcastMessage("§a💰 Distribution des récompenses en cours...");
            } else {
                Bukkit.broadcastMessage("§7✨ Bravo à tous ! (Événement fun - pas de récompenses)");
            }
        } else {
            if (rewardsEnabled) {
                Bukkit.broadcastMessage("§cAucun gagnant pour cet événement.");
            } else {
                Bukkit.broadcastMessage("§7Merci à tous pour votre participation !");
            }
        }

        // Séparer les participants actifs de ceux partis manuellement
        List<Player> activeParticipants = new ArrayList<>(plugin.getParticipantManager().getOnlineParticipants());
        List<Player> manuallyLeftParticipants = new ArrayList<>();

        // Identifier les participants qui ont quitté manuellement
        for (Player participant : plugin.getParticipantManager().getAllParticipantsForRewards()) {
            if (plugin.getParticipantManager().hasManuallyLeft(participant)) {
                manuallyLeftParticipants.add(participant);
            }
        }

        // Gérer les participants actifs (reset + téléportation)
        for (Player player : activeParticipants) {
            resetPlayer(player);
            sendEndMessages(player, winners, rewardsEnabled);
            // Téléportation au spawn
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawn " + player.getName());
        }

        // Gérer les participants partis manuellement (messages seulement, pas de téléportation)
        for (Player player : manuallyLeftParticipants) {
            sendEndMessages(player, winners, rewardsEnabled);
            // PAS de téléportation - ils sont déjà au spawn
            plugin.getLogger().info("Participant " + player.getName() + " déjà au spawn (parti manuellement)");
        }

        // Distribuer les récompenses seulement si activées (pour TOUS les participants)
        if (rewardsEnabled && !winners.isEmpty()) {
            try {
                RewardManager rewardManager = new RewardManager(plugin);
                rewardManager.distribute(winners);

                int totalParticipants = activeParticipants.size() + manuallyLeftParticipants.size();
                int manuallyLeftCount = manuallyLeftParticipants.size();

                plugin.getLogger().info("Récompenses distribuées pour l'événement " + game.getEventName() +
                        " à " + winners.size() + " gagnant(s) sur " + totalParticipants + " participants" +
                        (manuallyLeftCount > 0 ? " (dont " + manuallyLeftCount + " partis manuellement)" : ""));
            } catch (Exception e) {
                plugin.getLogger().warning("Erreur lors de la distribution des récompenses: " + e.getMessage());
                Bukkit.broadcastMessage("§cErreur lors de la distribution des récompenses. Contactez un administrateur.");
            }
        } else if (!rewardsEnabled) {
            plugin.getLogger().info("Événement fun terminé " + game.getEventName() +
                    " - pas de récompenses distribuées (normal)");
        }

        plugin.getParticipantManager().clear();
        plugin.setCurrentGame(null);
        plugin.getLobbyState().closeLobby();
    }

    /**
     * Envoie les messages de fin personnalisés à un joueur
     */
    private void sendEndMessages(Player player, List<Player> winners, boolean rewardsEnabled) {
        if (winners.contains(player)) {
            if (rewardsEnabled) {
                player.sendMessage("§a🎉 Félicitations ! Vous avez gagné et recevrez des récompenses !");
                if (plugin.getParticipantManager().hasManuallyLeft(player)) {
                    player.sendMessage("§e💡 Vos récompenses vous sont livrées même si vous êtes parti tôt !");
                }
            } else {
                player.sendMessage("§a🎉 Bien joué ! Merci d'avoir participé à cet événement fun !");
            }
        } else {
            if (rewardsEnabled) {
                player.sendMessage("§7Merci pour votre participation ! Tentez votre chance au prochain événement.");
            } else {
                player.sendMessage("§7Merci d'avoir participé à cet événement fun !");
            }
        }
    }

    /**
     * Obtient l'icône de trophée selon le rang
     */
    private String getTrophyIcon(int position) {
        return switch (position) {
            case 0 -> "🥇"; // 1er
            case 1 -> "🥈"; // 2e
            case 2 -> "🥉"; // 3e
            default -> "🏅"; // Autres
        };
    }

    /**
     * Remet le joueur dans un état normal
     */
    private void resetPlayer(Player player) {
        try {
            player.setGameMode(GameMode.SURVIVAL);
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            player.setFireTicks(0);
            player.setFallDistance(0);
            player.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
            player.clearActivePotionEffects();
            player.setInvulnerable(false);
        } catch (Exception e) {
            plugin.getLogger().warning("Erreur lors de la réinitialisation du joueur " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Force la fin d'un événement avec gestion des récompenses
     */
    public void forceEndEvent(String reason) {
        EventGame currentGame = plugin.getCurrentGame();
        if (currentGame == null) {
            return;
        }

        plugin.getLogger().info("Fin forcée de l'événement " + currentGame.getEventName() + ": " + reason);

        // Pas de gagnants en cas de fin forcée, donc pas de récompenses
        currentGame.stop();

        Bukkit.broadcastMessage("§c⚠ " + reason);

        // Gérer tous les participants (actifs et partis manuellement)
        List<Player> allParticipants = new ArrayList<>(plugin.getParticipantManager().getAllParticipantsForRewards());

        for (Player player : allParticipants) {
            player.sendMessage("§cÉvénement interrompu. Aucune récompense ne sera distribuée.");

            // Reset et téléportation seulement pour les participants actifs
            if (!plugin.getParticipantManager().hasManuallyLeft(player)) {
                resetPlayer(player);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawn " + player.getName());
            }
        }

        plugin.getParticipantManager().clear();
        plugin.setCurrentGame(null);
        plugin.getLobbyState().closeLobby();
    }

    /**
     * Obtient des informations sur l'événement en cours
     */
    public String getCurrentEventInfo() {
        EventGame currentGame = plugin.getCurrentGame();
        if (currentGame == null) {
            return "Aucun événement en cours";
        }

        StringBuilder info = new StringBuilder();
        info.append("Événement: ").append(currentGame.getEventName());
        info.append("\nParticipants actifs: ").append(plugin.getParticipantManager().getOnlineCount());
        info.append("\nParticipants totaux: ").append(plugin.getParticipantManager().getTotalCount());
        info.append("\nPartis manuellement: ").append(plugin.getParticipantManager().getManuallyLeftParticipants().size());
        info.append("\nRécompenses: ").append(currentGame.isRewardsEnabled() ? "Activées" : "Désactivées");
        info.append("\nNotifications: ").append(currentGame.isNotificationsEnabled() ? "Activées" : "Désactivées");

        return info.toString();
    }
}
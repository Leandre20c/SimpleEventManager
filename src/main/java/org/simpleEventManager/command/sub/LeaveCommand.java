package org.simpleEventManager.command.sub;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.simpleEventManager.SimpleEventManager;
import org.simpleEventManager.utils.EventUtils;

public class LeaveCommand implements SubCommand {

    private final SimpleEventManager plugin;

    public LeaveCommand(SimpleEventManager plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // Vérifier que c'est un joueur
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().prefixed("only-players"));
            return true;
        }

        // Vérifier la permission de quitter un événement
        if (!player.hasPermission("event.leave")) {
            player.sendMessage(plugin.getMessageManager().prefixed("no-permission"));
            return true;
        }

        boolean wasParticipant = plugin.getParticipantManager().isParticipant(player);
        boolean isInEventWorld = isPlayerInEventWorld(player);

        // Cas 1: Le joueur participe officiellement à un événement
        if (wasParticipant) {
            handleOfficialParticipantLeave(player);
            return true;
        }

        // Cas 2: Le joueur n'est pas participant mais est dans un monde d'événement
        if (isInEventWorld) {
            handleEventWorldLeave(player);
            return true;
        }

        // Cas 3: Le joueur n'est ni participant ni dans un monde d'événement
        // Mais on peut quand même l'aider en cas de problème
        handleGeneralLeave(player);
        return true;
    }

    /**
     * Gère le départ d'un participant officiel
     */
    private void handleOfficialParticipantLeave(Player player) {
        boolean isEventRunning = plugin.getCurrentGame() != null && plugin.getCurrentGame().hasWinner() == false;
        boolean isLobbyOpen = plugin.getLobbyState().isLobbyOpen();

        if (isEventRunning && !isLobbyOpen) {
            // CAS SPÉCIAL : Événement en cours - garde sa place pour les récompenses
            handleEventRunningLeave(player);
        } else {
            // CAS NORMAL : Lobby ou pas d'événement - retire complètement
            handleNormalLeave(player);
        }
    }

    /**
     * Gère le départ pendant un événement en cours
     */
    private void handleEventRunningLeave(Player player) {
        // Marquer comme parti manuellement (garde sa place pour les récompenses)
        plugin.getParticipantManager().markAsManuallyLeft(player);

        // Notifier le jeu que le joueur part (pour la logique interne)
        if (plugin.getCurrentGame() != null) {
            plugin.getCurrentGame().Removeplayer(player);
        }

        // Remettre le joueur en état normal et téléporter
        resetPlayerState(player);
        teleportToSpawnSafely(player);

        // Messages spéciaux
        player.sendMessage("§eTu as quitté l'événement en cours.");
        player.sendMessage("§aℹ Tu conserves ta place pour les récompenses si tu es gagnant !");

        // Annoncer aux autres participants
        if (plugin.getParticipantManager().getOnlineCount() > 0) {
            String leaveMessage = "§e" + player.getName() + " §7a quitté l'événement en cours. §8(§b" +
                    plugin.getParticipantManager().getOnlineCount() + " participants actifs§8)";

            for (Player participant : plugin.getParticipantManager().getOnlineParticipants()) {
                participant.sendMessage(leaveMessage);
            }
        }

        plugin.getLogger().info(player.getName() + " a quitté manuellement pendant l'événement " +
                plugin.getCurrentGame().getEventName() + " - garde sa place pour les récompenses");
    }

    /**
     * Gère le départ normal (lobby ou pas d'événement)
     */
    private void handleNormalLeave(Player player) {
        // Retirer le joueur du jeu en cours si nécessaire
        if (plugin.getCurrentGame() != null) {
            plugin.getCurrentGame().Removeplayer(player);
        }

        // Retirer complètement le joueur de la liste des participants
        plugin.getParticipantManager().leave(player);

        // Remettre le joueur en état normal
        resetPlayerState(player);
        teleportToSpawnSafely(player);

        // Message de confirmation
        player.sendMessage("§cTu as quitté l'événement.");

        // Annoncer aux autres participants qu'un joueur a quitté
        if (plugin.getParticipantManager().getOnlineCount() > 0) {
            String leaveMessage = "§e" + player.getName() + " §ca quitté l'événement. §8(§b" +
                    plugin.getParticipantManager().getOnlineCount() + " participants§8)";

            // Diffuser le message à tous les participants restants
            for (Player participant : plugin.getParticipantManager().getOnlineParticipants()) {
                participant.sendMessage(leaveMessage);
            }
        }
    }

    /**
     * Gère le départ d'un joueur dans un monde d'événement mais pas participant
     */
    private void handleEventWorldLeave(Player player) {
        resetPlayerState(player);
        teleportToSpawnSafely(player);
        player.sendMessage("§eTu as été renvoyé au spawn depuis le monde d'événement.");
        plugin.getLogger().info(player.getName() + " a utilisé /event leave depuis un monde d'événement sans être participant officiel");
    }

    /**
     * Gère le départ général (joueur pas dans un événement)
     */
    private void handleGeneralLeave(Player player) {
        player.sendMessage("§eTu n'es pas dans un event");
        plugin.getLogger().info(player.getName() + " a utilisé /event leave sans être dans un événement.");
    }

    /**
     * Remet le joueur dans un état normal
     */
    private void resetPlayerState(Player player) {
        try {
            player.setGameMode(GameMode.SURVIVAL);
            player.setInvulnerable(false);
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            player.setFireTicks(0);
            player.setFallDistance(0);
            player.setVelocity(player.getVelocity().zero());
            player.clearActivePotionEffects();

        } catch (Exception e) {
            plugin.getLogger().warning("Erreur lors de la réinitialisation de l'état du joueur " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Téléporte le joueur au spawn de manière sécurisée
     */
    private void teleportToSpawnSafely(Player player) {
        try {
            // Méthode 1: Commande spawn
            if (hasSpawnCommand()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawn " + player.getName());

                // Vérifier après un délai
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    verifyPlayerLocation(player);
                }, 40L); // 2 secondes
                return;
            }

            // Méthode 2: Spawn du monde principal
            fallbackTeleport(player);

        } catch (Exception e) {
            plugin.getLogger().severe("Erreur lors de la téléportation de " + player.getName() + ": " + e.getMessage());
            fallbackTeleport(player);
        }
    }

    /**
     * Méthode de secours pour téléporter le joueur
     */
    private void fallbackTeleport(Player player) {
        try {
            // Essayer le spawn du monde principal
            Location worldSpawn = Bukkit.getWorlds().get(0).getSpawnLocation();
            if (!player.teleport(worldSpawn)) {
                // Dernière tentative: téléporter avec un délai
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    player.teleport(worldSpawn);
                }, 10L);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Impossible de téléporter " + player.getName() + " au spawn: " + e.getMessage());
            player.sendMessage("§cErreur de téléportation. Utilisez §e/spawn §cpour retourner au spawn.");
        }
    }

    /**
     * Vérifie si le serveur a une commande spawn disponible
     */
    private boolean hasSpawnCommand() {
        try {
            return Bukkit.getPluginCommand("spawn") != null ||
                    Bukkit.getServer().getCommandMap().getCommand("spawn") != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Vérifie si le joueur est dans un monde d'événement
     */
    private boolean isPlayerInEventWorld(Player player) {
        if (plugin.getCurrentGame() == null) {
            // Vérifier si le joueur est dans un monde qui ressemble à un monde d'événement
            // Par exemple, si le nom du monde contient "event" ou correspond à un monde configuré
            String worldName = player.getWorld().getName().toLowerCase();

            // Vérifier contre les mondes d'événements configurés
            if (plugin.getConfig().contains("event-spawns")) {
                for (String eventName : plugin.getConfig().getConfigurationSection("event-spawns").getKeys(false)) {
                    String eventWorld = plugin.getConfig().getString("event-spawns." + eventName + ".world", "").toLowerCase();
                    if (worldName.equals(eventWorld)) {
                        return true;
                    }
                }
            }

            // Vérifier contre le monde du lobby
            if (plugin.getConfig().contains("event-lobby.world")) {
                String lobbyWorld = plugin.getConfig().getString("event-lobby.world", "").toLowerCase();
                if (worldName.equals(lobbyWorld)) {
                    return true;
                }
            }

            return false;
        }

        try {
            Location eventSpawn = EventUtils.getEventSpawnLocation(plugin, plugin.getCurrentGame().getEventName());
            if (eventSpawn != null && eventSpawn.getWorld() != null) {
                return player.getWorld().equals(eventSpawn.getWorld());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Erreur lors de la vérification du monde du joueur: " + e.getMessage());
        }

        return false;
    }

    private void verifyPlayerLocation(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        // Vérifier si le joueur est toujours dans un monde d'événement
        if (isPlayerInEventWorld(player)) {
            plugin.getLogger().info("Le joueur " + player.getName() + " est toujours dans un monde d'événement, nouvelle tentative de téléportation.");
            fallbackTeleport(player);
        }
    }
}
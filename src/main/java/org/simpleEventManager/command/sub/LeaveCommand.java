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

        // Vérifier que le joueur participe à un événement
        if (!plugin.getParticipantManager().isParticipant(player)) {
            player.sendMessage(plugin.getMessageManager().prefixed("not-in-event"));
            return true;
        }

        // Retirer le joueur du jeu en cours si nécessaire
        if (plugin.getCurrentGame() != null) {
            plugin.getCurrentGame().Removeplayer(player);
        }

        // Retirer le joueur de la liste des participants
        plugin.getParticipantManager().leave(player);

        // Remettre le joueur en état normal
        resetPlayerState(player);

        // S'assurer que le joueur va au spawn dans tous les cas
        teleportToSpawnSafely(player);

        // Messages de confirmation
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

        return true;
    }

    /**
     * Remet le joueur dans un état normal
     */
    private void resetPlayerState(Player player) {
        try {
            player.setGameMode(GameMode.SURVIVAL);
            player.setInvulnerable(false);
            player.getInventory().clear();
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            player.setFireTicks(0);
            player.setFallDistance(0);
            player.setVelocity(player.getVelocity().zero());
            player.clearActivePotionEffects();

            // Restaurer les permissions PvP si nécessaire
            plugin.togglePvp(true, player);

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
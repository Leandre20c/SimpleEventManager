package org.simpleEventManager.command.sub;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleEventManager.SimpleEventManager;
import org.simpleEventManager.api.EventGame;
import org.simpleEventManager.state.LobbyState;
import org.simpleEventManager.utils.LobbyStarter;

import java.util.Set;

public class StopCommand implements SubCommand {

    private final SimpleEventManager plugin;

    public StopCommand(SimpleEventManager plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        LobbyState lobbyState = plugin.getLobbyState();
        EventGame currentGame = plugin.getCurrentGame();

        if (!sender.hasPermission("event.admin")) {
            sender.sendMessage(plugin.getMessageManager().get("no-permission"));
            return true;
        }

        // Si un lobby est actif
        if (lobbyState.isLobbyOpen()) {
            LobbyStarter.cancelCountdown();
            lobbyState.closeLobby();
            forceCleanupAllParticipants("Le lobby a été fermé prématurément.");
            plugin.setCurrentGame(null);
            Bukkit.broadcastMessage("§cLe lobby a été fermé prématurément. L'événement n'aura pas lieu.");
            return true;
        }

        // Si un event est en cours
        if (currentGame != null) {
            currentGame.stop();
            if (currentGame instanceof JavaPlugin jp) {
                Bukkit.getScheduler().cancelTasks(jp);
            }
            forceCleanupAllParticipants("L'événement a été arrêté par un administrateur.");
            plugin.setCurrentGame(null);
            Bukkit.broadcastMessage("§cL'événement en cours a été arrêté par un administrateur.");
            return true;
        }

        sender.sendMessage("§cAucun lobby ni événement actif actuellement.");
        return true;
    }

    /**
     * Force le nettoyage de TOUS les participants (y compris ceux partis manuellement)
     * Sans déclencher la logique de "garde sa place pour les récompenses"
     */
    private void forceCleanupAllParticipants(String reason) {
        // Obtenir TOUS les participants, y compris ceux partis manuellement
        Set<Player> allParticipants = plugin.getParticipantManager().getAllParticipantsForRewards();

        plugin.getLogger().info("Force cleanup de " + allParticipants.size() + " participants: " + reason);

        for (Player player : allParticipants) {
            // Reset complet du joueur
            forceResetPlayer(player);

            // Message personnalisé
            player.sendMessage("§c⚠ " + reason);
            player.sendMessage("§7Tu as été automatiquement nettoyé et téléporté au spawn.");

            // Téléporter au spawn (seulement ceux qui ne sont pas déjà partis manuellement)
            if (!plugin.getParticipantManager().hasManuallyLeft(player)) {
                teleportToSpawn(player);
            } else {
                // Les joueurs partis manuellement sont déjà au spawn, juste les reset
                plugin.getLogger().info("Joueur " + player.getName() + " déjà au spawn (parti manuellement)");
            }
        }

        // Nettoyage complet du manager
        plugin.getParticipantManager().clear();
    }

    /**
     * Reset complet d'un joueur (état, effets, inventaire)
     */
    private void forceResetPlayer(Player player) {
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
            plugin.getLogger().warning("Erreur lors du reset forcé du joueur " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Téléporte un joueur au spawn de manière sécurisée
     */
    private void teleportToSpawn(Player player) {
        try {
            // Essayer avec la commande spawn d'abord
            if (hasSpawnCommand()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawn " + player.getName());
            } else {
                // Fallback vers le spawn du monde principal
                player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Erreur de téléportation pour " + player.getName() + ": " + e.getMessage());
            player.sendMessage("§cErreur de téléportation. Utilisez §e/spawn §cpour retourner manuellement.");
        }
    }

    /**
     * Vérifie si une commande spawn est disponible
     */
    private boolean hasSpawnCommand() {
        try {
            return Bukkit.getPluginCommand("spawn") != null ||
                    Bukkit.getServer().getCommandMap().getCommand("spawn") != null;
        } catch (Exception e) {
            return false;
        }
    }
}
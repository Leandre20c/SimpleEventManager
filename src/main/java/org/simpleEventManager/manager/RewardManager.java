package org.simpleEventManager.manager;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.simpleEventManager.SimpleEventManager;

import java.util.*;

public class RewardManager {

    private final SimpleEventManager plugin;

    public RewardManager(SimpleEventManager plugin) {
        this.plugin = plugin;
    }

    public void distribute(List<Player> winners) {
        if (winners == null || winners.isEmpty()) return;

        // Vérifier si les récompenses sont activées globalement
        if (!plugin.getConfig().getBoolean("rewards-enabled", true)) {
            plugin.getLogger().info("Distribution des récompenses ignorée : récompenses désactivées globalement");
            return;
        }

        FileConfiguration config = plugin.getConfig();
        ConfigurationSection rewardSection = config.getConfigurationSection("rewards");
        if (rewardSection == null) return;

        for (int i = 0; i < winners.size(); i++) {
            Player player = winners.get(i);
            if (player == null || !player.isOnline()) continue;

            int rank = i + 1;
            String rankKey = String.valueOf(rank);

            // Obtenir les commandes pour ce rang
            List<String> commands = config.getStringList("rewards." + rankKey);

            // Si aucune commande pour ce rang spécifique, utiliser "default"
            if (commands.isEmpty()) {
                commands = config.getStringList("rewards.default");
            }

            // Exécuter toutes les commandes
            for (String cmd : commands) {
                if (cmd != null && !cmd.isEmpty()) {
                    String finalCommand = cmd.replace("%player%", player.getName());

                    try {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
                        plugin.getLogger().info("Récompense exécutée pour " + player.getName() +
                                " (rang " + rank + "): " + finalCommand);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Erreur lors de l'exécution de la commande de récompense '" +
                                finalCommand + "' pour " + player.getName() + ": " + e.getMessage());
                    }
                }
            }

            // Message de confirmation au joueur
            if (!commands.isEmpty()) {
                player.sendMessage("§a💰 Vous avez reçu vos récompenses de rang " + rank + " !");
            }
        }
    }

    /**
     * Obtient un aperçu des récompenses configurées
     */
    public String getRewardsPreview() {
        if (!plugin.getConfig().getBoolean("rewards-enabled", true)) {
            return "§cRécompenses désactivées globalement";
        }

        StringBuilder preview = new StringBuilder("§6Récompenses configurées:\n");

        for (int rank = 1; rank <= 5; rank++) {
            List<String> commands = plugin.getConfig().getStringList("rewards." + rank);
            if (!commands.isEmpty()) {
                preview.append("§e").append(rank).append("er: §f").append(commands.size()).append(" récompense(s)\n");
            }
        }

        List<String> defaultCommands = plugin.getConfig().getStringList("rewards.default");
        if (!defaultCommands.isEmpty()) {
            preview.append("§7Autres: §f").append(defaultCommands.size()).append(" récompense(s)");
        }

        return preview.toString();
    }

    /**
     * Teste les récompenses pour un joueur (commande admin)
     */
    public void testRewards(Player player, int rank) {
        if (!plugin.getConfig().getBoolean("rewards-enabled", true)) {
            player.sendMessage("§cLes récompenses sont désactivées globalement");
            return;
        }

        List<String> commands = plugin.getConfig().getStringList("rewards." + rank);
        if (commands.isEmpty()) {
            commands = plugin.getConfig().getStringList("rewards.default");
        }

        if (commands.isEmpty()) {
            player.sendMessage("§cAucune récompense configurée pour le rang " + rank);
            return;
        }

        player.sendMessage("§aTest des récompenses pour le rang " + rank + ":");
        for (String cmd : commands) {
            String finalCommand = cmd.replace("%player%", player.getName());
            player.sendMessage("§7- " + finalCommand);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
        }
    }
}
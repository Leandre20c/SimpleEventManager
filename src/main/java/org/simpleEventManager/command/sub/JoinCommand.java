package org.simpleEventManager.command.sub;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.simpleEventManager.SimpleEventManager;

public class JoinCommand implements SubCommand {

    private final SimpleEventManager plugin;

    public JoinCommand(SimpleEventManager plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // Vérifier que c'est un joueur
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().prefixed("only-players"));
            return true;
        }

        // Vérifier la permission de rejoindre un événement
        if (!player.hasPermission("event.join")) {
            player.sendMessage(plugin.getMessageManager().prefixed("no-permission-join"));
            return true;
        }

        // Vérifier qu'un événement est en cours de préparation
        if (!plugin.getLobbyState().isLobbyOpen()) {
            player.sendMessage(plugin.getMessageManager().prefixed("no-event-preparing"));
            return true;
        }

        // Vérifier que le joueur n'est pas déjà dans l'événement
        if (plugin.getParticipantManager().isParticipant(player)) {
            player.sendMessage(plugin.getMessageManager().prefixed("already-in-event"));
            return true;
        }

        // Vérifier que le lobby n'est pas plein (si limite configurée)
        int maxParticipants = plugin.getConfig().getInt("event.max-participants", -1);
        if (maxParticipants > 0 && plugin.getParticipantManager().getOnlineCount() >= maxParticipants) {
            player.sendMessage(plugin.getMessageManager().prefixed("event-full"));
            return true;
        }

        // Vérifier que le spawn du lobby est défini
        Location lobbySpawn = plugin.getLobbyLocation();
        if (lobbySpawn == null) {
            player.sendMessage(plugin.getMessageManager().prefixed("lobby-spawn-not-set"));
            return true;
        }

        // Rejoindre l'événement
        plugin.getParticipantManager().join(player);

        // Téléporter le joueur au lobby
        boolean teleported = player.teleport(lobbySpawn);
        if (!teleported) {
            player.sendMessage("§cErreur de téléportation. Nouvelle tentative...");
            // Réessayer après un petit délai
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!player.teleport(lobbySpawn)) {
                    player.sendMessage("§cImpossible de vous téléporter au lobby. Contactez un administrateur.");
                    plugin.getParticipantManager().leave(player); // Retirer le joueur en cas d'échec
                }
            }, 5L);
        }

        // Configurer le joueur pour l'événement
        player.setInvulnerable(true);

        // Message de succès
        player.sendMessage(plugin.getMessageManager().prefixed("joined-event"));

        // Annoncer à tous les participants qu'un nouveau joueur a rejoint
        String joinMessage = "§e" + player.getName() + " §aa rejoint l'événement ! §8(§b" +
                plugin.getParticipantManager().getOnlineCount() + " participants§8)";

        // Diffuser le message à tous les participants
        for (Player participant : plugin.getParticipantManager().getOnlineParticipants()) {
            participant.sendMessage(joinMessage);
        }
        if (player.getWorld().getName().equalsIgnoreCase("event") || player.getWorld().getName().equalsIgnoreCase("BoatRace"))
        {
            player.getInventory().clear();
        }

        return true;
    }
}
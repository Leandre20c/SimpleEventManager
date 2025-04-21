package org.simpleEventManager.command.sub;

import org.bukkit.GameMode;
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
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!plugin.getParticipantManager().isParticipant(player)) {
            player.sendMessage("§cTu n'es pas dans le lobby.");
            return true;
        }

        if (!isInEventWorld(player)) {
            return true;
        }

        plugin.getParticipantManager().leave(player);
        resetPlayerState(player);

        player.teleport(player.getServer().getWorld("world").getSpawnLocation());

        player.sendMessage("§cTu as quitté l’événement.");
        return true;
    }

    private void resetPlayerState(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.setInvulnerable(false);
        player.getInventory().clear();
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.setFallDistance(0);
        player.setVelocity(player.getVelocity().zero());
        player.clearActivePotionEffects();
    }

    private boolean isInEventWorld(Player player) {
        return player.getWorld().equals(EventUtils.getEventSpawnLocation().getWorld());
    }
}

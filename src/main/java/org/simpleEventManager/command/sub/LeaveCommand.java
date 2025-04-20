package org.simpleEventManager.command.sub;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.simpleEventManager.SimpleEventManager;

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

        plugin.getParticipantManager().leave(player);
        player.teleport(player.getWorld().getSpawnLocation());
        player.sendMessage("§cTu as quitté l’événement.");
        return true;
    }
}

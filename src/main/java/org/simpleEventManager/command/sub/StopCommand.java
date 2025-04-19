package org.simpleEventManager.command.sub;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.simpleEventManager.SimpleEventManager;

public class StopCommand implements SubCommand {

    private final SimpleEventManager plugin;

    public StopCommand(SimpleEventManager plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("event.admin")) {
            player.sendMessage("§cTu n'as pas la permission.");
            return true;
        }

        Bukkit.broadcastMessage("§c[Event] L’event a été annulé.");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawn @a"); // ou autre commande

        plugin.getLobbyState().setLobbyOpen(false);
        return true;
    }
}

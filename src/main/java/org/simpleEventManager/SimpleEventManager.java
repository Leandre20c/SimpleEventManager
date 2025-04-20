package org.simpleEventManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleEventManager.api.EventGame;
import org.simpleEventManager.command.EventCommand;
import org.simpleEventManager.manager.EventLoader;
import org.simpleEventManager.manager.MessageManager;
import org.simpleEventManager.manager.ParticipantManager;
import org.simpleEventManager.reward.RewardDistributor;
import org.simpleEventManager.scheduler.EventScheduler;
import org.simpleEventManager.state.LobbyState;

import java.util.logging.Logger;

public class SimpleEventManager extends JavaPlugin {

    private final LobbyState lobbyState = new LobbyState();
    private final ParticipantManager participantManager = new ParticipantManager();
    private final EventLoader eventLoader = new EventLoader();
    private final MessageManager messageManager = new MessageManager(this);
    private EventGame currentGame;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        new EventCommand(this);
        new EventScheduler(this).start();
        eventLoader.loadEvents();

        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onQuit(PlayerQuitEvent event) {
                Player player = event.getPlayer();
                if (participantManager.isParticipant(player)) {
                    participantManager.leave(player);
                    player.teleport(getServer().getWorlds().get(0).getSpawnLocation());
                }
            }
        }, this);

        getLogger().info("SimpleEventManager enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("SimpleEventManager disabled.");
    }

    public LobbyState getLobbyState() {
        return lobbyState;
    }

    public ParticipantManager getParticipantManager() {
        return participantManager;
    }

    public EventLoader getEventLoader() {
        return eventLoader;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public EventGame getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(EventGame game) {
        this.currentGame = game;
    }

    public Location getLobbyLocation() {
        FileConfiguration config = getConfig();
        if (!config.contains("event-lobby.world")) return null;
        return new Location(
                Bukkit.getWorld(config.getString("event-lobby.world")),
                config.getDouble("event-lobby.x"),
                config.getDouble("event-lobby.y"),
                config.getDouble("event-lobby.z"),
                (float) config.getDouble("event-lobby.yaw"),
                (float) config.getDouble("event-lobby.pitch")
        );
    }
}

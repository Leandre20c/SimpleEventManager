package org.simpleEventManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleEventManager.command.EventCommand;
import org.simpleEventManager.manager.EventLoader;
import org.simpleEventManager.state.LobbyState;

public class SimpleEventManager extends JavaPlugin {

    private EventLoader eventLoader;
    private LobbyState lobbyState;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.eventLoader = new EventLoader();
        this.eventLoader.loadEvents();

        this.lobbyState = new LobbyState();

        new EventCommand(this);

        Bukkit.getLogger().info("SimpleEventManager enabled.");
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info("SimpleEventManager disabled.");
    }

    public EventLoader getEventLoader() {
        return eventLoader;
    }

    public LobbyState getLobbyState() {
        return lobbyState;
    }

    public Location getLobbySpawn() {
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

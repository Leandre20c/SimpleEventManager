package org.simpleEventManager.manager;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.simpleEventManager.api.EventGame;

import java.util.ArrayList;
import java.util.List;

public class EventLoader {
    private final List<EventGame> loadedEvents = new ArrayList<>();

    public void loadEvents() {
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin instanceof EventGame eventGame) {
                loadedEvents.add(eventGame);
                Bukkit.getLogger().info("[EventManager] Event chargé : " + eventGame.getEventName());
            }
        }
    }

    public List<EventGame> getAllEvents() {
        return loadedEvents;
    }

    public EventGame getEventByName(String name) {
        return loadedEvents.stream()
                .filter(e -> e.getEventName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

}

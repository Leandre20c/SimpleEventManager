package org.simpleEventManager.manager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.simpleEventManager.SimpleEventManager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {

    private final SimpleEventManager plugin;
    private final Map<String, String> messages = new HashMap<>();

    public MessageManager(SimpleEventManager plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    private void loadMessages() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            messages.put(key, config.getString(key));
        }
    }

    public String get(String key) {
        return messages.getOrDefault(key, "§cMessage introuvable: " + key);
    }

    public String prefixed(String key) {
        return get("prefix") + get(key);
    }
}

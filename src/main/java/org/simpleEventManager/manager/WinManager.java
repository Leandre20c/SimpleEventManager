package org.simpleEventManager.manager;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class WinManager {

    private final File file;
    private final YamlConfiguration config;

    public WinManager(File dataFolder) {
        this.file = new File(dataFolder, "victories.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public int getWins(UUID uuid, String key) {
        return config.getInt(uuid.toString() + "." + key, 0);
    }

    public void addWin(UUID uuid, String eventName) {
        String base = uuid.toString();

        // total
        int total = config.getInt(base + ".total", 0);
        config.set(base + ".total", total + 1);

        // event-specific
        int eventWins = config.getInt(base + "." + eventName, 0);
        config.set(base + "." + eventName, eventWins + 1);

        save();
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getTopPlayerName(int rank) {
        Map<UUID, Integer> totalWins = new HashMap<>();

        for (String key : config.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(key);
            } catch (IllegalArgumentException e) {
                continue;
            }
            int wins = config.getInt(key + ".total", 0);
            totalWins.put(uuid, wins);
        }

        List<Map.Entry<UUID, Integer>> sorted = totalWins.entrySet()
                .stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

        if (rank - 1 >= sorted.size()) return "";

        UUID target = sorted.get(rank - 1).getKey();
        return Optional.ofNullable(Bukkit.getOfflinePlayer(target).getName()).orElse("Inconnu");
    }

    public String getTopPlayerNameForEvent(int rank, String eventName) {
        Map<UUID, Integer> eventWins = new HashMap<>();

        for (String key : config.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(key);
            } catch (IllegalArgumentException e) {
                continue;
            }

            int wins = config.getInt(key + "." + eventName, 0);
            if (wins > 0) {
                eventWins.put(uuid, wins);
            }
        }

        List<Map.Entry<UUID, Integer>> sorted = eventWins.entrySet()
                .stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

        if (rank - 1 >= sorted.size()) return "";

        UUID target = sorted.get(rank - 1).getKey();
        return Optional.ofNullable(Bukkit.getOfflinePlayer(target).getName()).orElse("Inconnu");
    }

    public String getTopPlayerEntry(int rank) {
        Map<UUID, Integer> totalWins = new HashMap<>();

        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                int wins = config.getInt(key + ".total", 0);
                totalWins.put(uuid, wins);
            } catch (IllegalArgumentException e) {
                continue;
            }
        }

        List<Map.Entry<UUID, Integer>> sorted = totalWins.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .toList();

        if (rank - 1 >= sorted.size()) return "";

        UUID uuid = sorted.get(rank - 1).getKey();
        int wins = sorted.get(rank - 1).getValue();
        String name = Bukkit.getOfflinePlayer(uuid).getName();

        return (name != null ? name : "Inconnu") + ": " + wins;
    }

    public String getTopPlayerEntryForEvent(int rank, String eventName) {
        Map<UUID, Integer> eventWins = new HashMap<>();

        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                int wins = config.getInt(key + "." + eventName, 0);
                if (wins > 0) {
                    eventWins.put(uuid, wins);
                }
            } catch (IllegalArgumentException e) {
                continue;
            }
        }

        List<Map.Entry<UUID, Integer>> sorted = eventWins.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .toList();

        if (rank - 1 >= sorted.size()) return "";

        UUID uuid = sorted.get(rank - 1).getKey();
        int wins = sorted.get(rank - 1).getValue();
        String name = Bukkit.getOfflinePlayer(uuid).getName();

        return (name != null ? name : "Inconnu") + ": " + wins;
    }


}

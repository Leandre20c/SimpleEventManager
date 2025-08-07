package org.simpleEventManager.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.simpleEventManager.SimpleEventManager;

import java.util.UUID;

public class SEMPlaceholder extends PlaceholderExpansion {

    private final SimpleEventManager plugin;

    public SEMPlaceholder(SimpleEventManager plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "sem";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Toi";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) return "";

        UUID uuid = player.getUniqueId();

        if (identifier.equalsIgnoreCase("wins_total")) {
            return String.valueOf(plugin.getWinManager().getWins(uuid, "total"));
        }

        if (identifier.startsWith("wins_")) {
            String event = identifier.substring("wins_".length());
            return String.valueOf(plugin.getWinManager().getWins(uuid, event));
        }

        if (identifier.startsWith("top_")) {
            String[] parts = identifier.split("_");

            // %sem_top_1% → "Name : Value"
            if (parts.length == 2) {
                try {
                    int rank = Integer.parseInt(parts[1]);
                    String name = plugin.getWinManager().getTopPlayerName(rank);
                    int value = plugin.getWinManager().getTopPlayerEntry(rank);
                    return name + " : " + value;
                } catch (NumberFormatException e) {
                    return "";
                }
            }

            // %sem_top_<event>_<rank>% → "Name : Value"
            if (parts.length == 3) {
                String event = parts[1];
                try {
                    int rank = Integer.parseInt(parts[2]);
                    String name = plugin.getWinManager().getTopPlayerNameForEvent(rank, event);
                    int value = plugin.getWinManager().getTopPlayerEntryForEvent(rank, event);
                    return name + " : " + value;
                } catch (NumberFormatException e) {
                    return "";
                }
            }

            // %sem_top_name_1% ou %sem_top_value_1%
            if (parts.length == 3) {
                String type = parts[1]; // "name" ou "value"
                try {
                    int rank = Integer.parseInt(parts[2]);
                    if (type.equalsIgnoreCase("name")) {
                        return plugin.getWinManager().getTopPlayerName(rank);
                    } else if (type.equalsIgnoreCase("value")) {
                        return String.valueOf(plugin.getWinManager().getTopPlayerEntry(rank));
                    }
                } catch (NumberFormatException e) {
                    return "";
                }
            }

            // %sem_top_name_<event>_<rank>% ou %sem_top_value_<event>_<rank>%
            if (parts.length == 4) {
                String type = parts[1]; // "name" ou "value"
                String event = parts[2];
                try {
                    int rank = Integer.parseInt(parts[3]);
                    if (type.equalsIgnoreCase("name")) {
                        return plugin.getWinManager().getTopPlayerNameForEvent(rank, event);
                    } else if (type.equalsIgnoreCase("value")) {
                        return String.valueOf(plugin.getWinManager().getTopPlayerEntryForEvent(rank, event));
                    }
                } catch (NumberFormatException e) {
                    return "";
                }
            }
        }


        return "";
    }
}

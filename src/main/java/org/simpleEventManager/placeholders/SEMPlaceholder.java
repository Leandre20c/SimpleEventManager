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
            try {
                int rank = Integer.parseInt(identifier.substring("top_".length()));
                return plugin.getWinManager().getTopPlayerName(rank);
            } catch (NumberFormatException e) {
                return "";
            }
        }

        if (identifier.startsWith("top_")) {
            String[] parts = identifier.split("_");

            // %sem_top_1%
            if (parts.length == 2) {
                try {
                    int rank = Integer.parseInt(parts[1]);
                    return plugin.getWinManager().getTopPlayerEntry(rank);
                } catch (NumberFormatException e) {
                    return "";
                }
            }

            // %sem_top_<event>_<rank>%
            if (parts.length == 3) {
                String event = parts[1];
                try {
                    int rank = Integer.parseInt(parts[2]);
                    return plugin.getWinManager().getTopPlayerEntryForEvent(rank, event);
                } catch (NumberFormatException e) {
                    return "";
                }
            }
        }

        return "";
    }
}

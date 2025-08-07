package org.simpleEventManager.api;

import org.bukkit.entity.Player;
import java.util.List;

public interface EventGame {
    String getEventName();
    String getEventDescription();
    void start(List<Player> participants);
    void stop();
    boolean hasWinner();
    default List<Player> getWinners() {
        return List.of();
    }
    void Removeplayer(Player player);
    default void setMode(String mode) {
        // optional sub events
    }
    default String getMode() {
        return "default";
    }
    default boolean isRewardsEnabled() {
        return true;
    }
    default boolean isNotificationsEnabled() {
        return false;
    }
}
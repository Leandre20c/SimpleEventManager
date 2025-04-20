package org.simpleEventManager.api;

import org.bukkit.entity.Player;
import java.util.List;

public interface EventGame {
    String getEventName();
    String getEventDescription();
    void start(List<Player> participants);
    void stop();
    boolean hasWinner();
    Player getWinner();
}

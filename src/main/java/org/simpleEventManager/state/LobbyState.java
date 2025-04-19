package org.simpleEventManager.state;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LobbyState {
    private boolean lobbyOpen = false;
    private final Set<UUID> waitingPlayers = new HashSet<>();

    public boolean isLobbyOpen() {
        return lobbyOpen;
    }

    public void setLobbyOpen(boolean lobbyOpen) {
        this.lobbyOpen = lobbyOpen;
        if (!lobbyOpen) {
            waitingPlayers.clear();
        }
    }

    public void addPlayer(UUID uuid) {
        waitingPlayers.add(uuid);
    }

    public void removePlayer(UUID uuid) {
        waitingPlayers.remove(uuid);
    }

    public Set<UUID> getWaitingPlayers() {
        return waitingPlayers;
    }
}

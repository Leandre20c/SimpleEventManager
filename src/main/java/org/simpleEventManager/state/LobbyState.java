package org.simpleEventManager.state;

public class LobbyState {

    private boolean lobbyOpen = false;

    public void openLobby() {
        this.lobbyOpen = true;
    }

    public void closeLobby() {
        this.lobbyOpen = false;
    }

    public boolean isLobbyOpen() {
        return lobbyOpen;
    }
}

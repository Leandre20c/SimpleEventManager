package org.simpleEventManager.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Version simple du ParticipantManager sans events listeners
 * pour éviter les problèmes de chargement
 */
public class ParticipantManager {

    private final Set<UUID> participants = new HashSet<>();

    // Constructeur simple - pas d'events
    public ParticipantManager() {
        // Rien à faire, pas d'enregistrement d'events
    }

    public void join(Player player) {
        participants.add(player.getUniqueId());
    }

    public void leave(Player player) {
        participants.remove(player.getUniqueId());
    }

    public void removeIfOffline() {
        participants.removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
    }

    public void clear() {
        participants.clear();
    }

    public boolean isParticipant(Player player) {
        return participants.contains(player.getUniqueId());
    }

    public Set<Player> getOnlineParticipants() {
        removeIfOffline();
        Set<Player> online = new HashSet<>();
        for (UUID uuid : participants) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                online.add(p);
            }
        }
        return Collections.unmodifiableSet(online);
    }

    public int getOnlineCount() {
        return getOnlineParticipants().size();
    }
}
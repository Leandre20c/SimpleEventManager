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
    private final Set<UUID> manuallyLeftDuringEvent = new HashSet<>();

    // Constructeur simple - pas d'events
    public ParticipantManager() {
        // Rien à faire, pas d'enregistrement d'events
    }

    public void join(Player player) {
        participants.add(player.getUniqueId());
        // Retirer de la liste des "partis manuellement" si il y était
        manuallyLeftDuringEvent.remove(player.getUniqueId());
    }

    public void leave(Player player) {
        participants.remove(player.getUniqueId());
        manuallyLeftDuringEvent.remove(player.getUniqueId());
    }

    /**
     * Marque un joueur comme ayant quitté manuellement pendant l'événement
     * Il garde sa place pour les récompenses mais n'est plus téléporté
     */
    public void markAsManuallyLeft(Player player) {
        manuallyLeftDuringEvent.add(player.getUniqueId());
        // NE PAS le retirer des participants - il garde sa place pour les récompenses
    }

    /**
     * Vérifie si un joueur a quitté manuellement pendant l'événement
     */
    public boolean hasManuallyLeft(Player player) {
        return manuallyLeftDuringEvent.contains(player.getUniqueId());
    }

    /**
     * Vérifie si un joueur a quitté manuellement pendant l'événement (par UUID)
     */
    public boolean hasManuallyLeft(UUID uuid) {
        return manuallyLeftDuringEvent.contains(uuid);
    }

    /**
     * Obtient tous les participants qui ont quitté manuellement (pour les récompenses)
     */
    public Set<UUID> getManuallyLeftParticipants() {
        return Collections.unmodifiableSet(manuallyLeftDuringEvent);
    }

    /**
     * Nettoie les participants qui ont quitté manuellement et qui sont hors ligne
     */
    public void cleanupManuallyLeft() {
        manuallyLeftDuringEvent.removeIf(uuid -> Bukkit.getPlayer(uuid) == null || !Bukkit.getPlayer(uuid).isOnline());
    }

    public void removeIfOffline() {
        participants.removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
    }

    public void clear() {
        participants.clear();
        manuallyLeftDuringEvent.clear();
    }

    public boolean isParticipant(Player player) {
        return participants.contains(player.getUniqueId());
    }

    /**
     * Obtient tous les participants actifs (en ligne et pas partis manuellement)
     */
    public Set<Player> getOnlineParticipants() {
        removeIfOffline();
        Set<Player> online = new HashSet<>();
        for (UUID uuid : participants) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline() && !manuallyLeftDuringEvent.contains(uuid)) {
                online.add(p);
            }
        }
        return Collections.unmodifiableSet(online);
    }

    /**
     * Obtient TOUS les participants (même ceux partis manuellement) pour les récompenses
     */
    public Set<Player> getAllParticipantsForRewards() {
        Set<Player> allParticipants = new HashSet<>();
        for (UUID uuid : participants) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                allParticipants.add(p);
            }
        }
        return Collections.unmodifiableSet(allParticipants);
    }

    public int getOnlineCount() {
        return getOnlineParticipants().size();
    }

    /**
     * Compte total incluant ceux qui ont quitté manuellement
     */
    public int getTotalCount() {
        return getAllParticipantsForRewards().size();
    }
}
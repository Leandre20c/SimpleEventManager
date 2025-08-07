package org.simpleEventManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleEventManager.api.EventGame;
import org.simpleEventManager.command.EventCommand;
import org.simpleEventManager.manager.*;
import org.simpleEventManager.placeholders.SEMPlaceholder;
import org.simpleEventManager.scheduler.EventScheduler;
import org.simpleEventManager.state.LobbyState;

import java.util.List;

public class SimpleEventManager extends JavaPlugin {

    private final LobbyState lobbyState = new LobbyState();
    private final EventLoader eventLoader = new EventLoader();
    private final MessageManager messageManager = new MessageManager(this);

    // ✅ NE PAS initialiser ici - faire dans onEnable()
    private ParticipantManager participantManager;
    private RewardManager rewardManager;
    private EventController eventController;
    private EventGame currentGame;
    private WinManager winManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // ✅ Initialiser le ParticipantManager APRÈS que le plugin soit activé
        this.participantManager = new ParticipantManager();

        new EventCommand(this);
        new EventScheduler(this).start();
        this.eventController = new EventController(this);
        this.rewardManager = new RewardManager(this);
        eventLoader.loadEvents();

        this.winManager = new WinManager(getDataFolder());

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new SEMPlaceholder(this).register();
        }

        // Gestion du kick/retour au spawn si un joueur quitte
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onQuit(PlayerQuitEvent event) {
                Player player = event.getPlayer();
                if (participantManager.isParticipant(player)) {
                    participantManager.leave(player);
                    if (currentGame != null) {
                        currentGame.Removeplayer(player);
                    }
                    player.teleport(getServer().getWorlds().get(0).getSpawnLocation());
                }
            }
        }, this);

        // Scheduler qui vérifie chaque tick s'il y a un winner
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            EventGame game = getCurrentGame();
            if (game != null && game.hasWinner()) {
                // On désamorce immédiatement pour éviter tout double appel
                setCurrentGame(null);
                // Puis on termine proprement l'event (stop, rewards, cleanup…)
                getEventController().endEvent(game);
            }
        }, 20L, 20L);

        getLogger().info("SimpleEventManager enabled!");
    }

    @Override
    public void onDisable() {
        // Nettoyage du ParticipantManager s'il a des resources à libérer
        if (participantManager != null && participantManager instanceof Listener) {
            // Si c'est la version avec events, on peut faire un cleanup
            try {
                participantManager.getClass().getMethod("shutdown").invoke(participantManager);
            } catch (Exception ignored) {
                // La méthode shutdown n'existe pas, pas grave
            }
        }

        getLogger().info("SimpleEventManager disabled.");
    }

    public LobbyState getLobbyState() {
        return lobbyState;
    }

    public ParticipantManager getParticipantManager() {
        return participantManager;
    }

    public EventLoader getEventLoader() {
        return eventLoader;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public EventController getEventController() {
        return eventController;
    }

    public EventGame getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(EventGame game) {
        this.currentGame = game;
    }

    public Location getLobbyLocation() {
        FileConfiguration config = getConfig();
        if (!config.contains("event-lobby.world")) return null;
        return new Location(
                Bukkit.getWorld(config.getString("event-lobby.world")),
                config.getDouble("event-lobby.x"),
                config.getDouble("event-lobby.y"),
                config.getDouble("event-lobby.z"),
                (float) config.getDouble("event-lobby.yaw"),
                (float) config.getDouble("event-lobby.pitch")
        );
    }

    public void togglePvp(boolean activated, Player player){
        player.setInvulnerable(activated);
    }

    public void togglePvp(boolean activated, List<Player> players){
        for (Player player : players) {
            player.setInvulnerable(activated);
        }
    }

    public WinManager getWinManager() {
        return winManager;
    }
}
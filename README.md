Version : 1.15

This plugin is a manager of other events plugins.
It manage how events (from plugins you created or downloaded) will be started, you can schedule when the events will start, what event, or choose it randomly.
It handled players, it starts with teleporting to a waiting lobby that you can set ingame `/event setspawn lobby` wait for x minutes (or you can force it) and start the event (handled by other plugins).
When event is ended, it teleport all players to spawn, and give rewards to the players based on their ranking.

You can configure rewards in config.yml

API:

```java
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
```

It has to be declared in the events plugin and handled to get it working.

Exemple - In the main file bellow OnEnable()
```java
    @Override
    public void start(List<Player> players) {
        game = new MyMiniGame(players, this);
        game.start();
    }

    @Override
    public void stop() {
        if (game != null) {
            game.stop();
            game = null;
        }
    }

    @Override
    public boolean hasWinner() {
        return game != null && !game.getWinners().isEmpty();
    }

    @Override
    public List<Player> getWinners() {
        return game != null ? game.getWinners() : List.of();
    }

    @Override
    public String getEventName() {
        return "My Event Game";
    }

    @Override
    public String getEventDescription() {
        return "A game event template.";
    }
```

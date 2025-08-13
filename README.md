# SimpleEventManager

> A modular and extensible Minecraft Paper plugin that manages scheduled events, joinable lobbies, automatic reward distribution, and support for custom event games.

---

## 📌 Project Overview

**SimpleEventManager** is designed to:
- Launch events at scheduled times or via commands.
- Handle pre-game lobbies with countdowns.
- Manage event participation (only joined players).
- Automatically handle event ending and reward distribution.
- Support modular event logic via independent plugins that implement a simple Java interface.

This plugin **does not include game logic**. Each event is a **separate plugin**, detected dynamically by the core.

---

## ⚙️ Architecture

```
SimpleEventManager (core)
├── EventManager → manages scheduler, lobby, events
├── EventController → controls lifecycle (start, stop, rewards)
├── EventGame interface → required for any external plugin
├── SpawnManager → handles spawn/lobby positions
├── BossbarManager → manages countdown UI
├── RewardManager → distributes rewards
└── PlaceholderManager → registers PAPI placeholders
```

---

## 🧩 Event Plugin Structure

Each event is a **separate plugin** that implements:

```java
public interface EventGame {
    void start(Collection<Player> players);
    void stop();
    boolean hasWinner();
    List<Player> getWinners();
    String getEventName();
    String getEventDescription();
}
```

The core will:
- Automatically detect all `EventGame` implementations on startup.
- Use `/event list` to show them.
- Manage lifecycle via `start()`, `stop()`, `hasWinner()`, `getWinners()`.

> Event plugins must not manage player rewards or teleportations.

---

## 🧠 Example Event Lifecycle

1. Scheduler launches an event at 18:00 (`Spleef`).
2. A lobby is opened.
3. Players join via `/event join`.
4. Countdown (e.g. 5 minutes) shows with bossbar.
5. At the end of the countdown:
    - If ≥2 players → `start(players)` is called.
    - If <2 players → lobby is cancelled.
6. During the game, the event plugin manages player logic.
7. When `hasWinner()` returns `true`:
    - Core calls `getWinners()` to determine rankings.
    - Core resets players, teleports to spawn, and gives rewards.

---

## 🧪 Existing Event Plugins

| Name        | Description |
|-------------|-------------|
| `TemplateEventGame` | Empty base to create your own event |
| `Spleef`            | Break blocks under other players to eliminate them |
| `KnockPerl`         | PvP with knockback sticks and ender pearls, 3 lives |
| `AnvilRain`         | Dodge falling anvils until only one survives |

Each of them is in its **own plugin**, loaded dynamically.

---

## 🛠️ Setup Guide

### 1. Requirements
- Minecraft Paper 1.20+ server
- Java 17 or higher

### 2. Installation
1. Place `SimpleEventManager.jar` into `/plugins/`.
2. Add one or more compatible event plugins (e.g. `Spleef.jar`).
3. Start the server.
4. Set spawn locations with `/event setspawn ...`.
5. Configure scheduled events in `config.yml`.

---

## 📄 config.yml

```yaml
scheduler:
  - hour: "18:00"
    event: "Spleef"
  - hour: "20:00"
    event: "Anvil"

join-delay-minutes: 5

rewards:
  top-1: 1000
  top-2: 500
  top-3: 250
  others: 100

bossbar:
  text: "Event starts in {time}"
  color: "BLUE"
  style: "SEGMENTED_10"
```

---

## 💬 Commands

### Player Commands
| Command             | Description                          |
|---------------------|--------------------------------------|
| `/event`            | Alias of `/event join`               |
| `/event join`       | Join the current event lobby         |
| `/event leave`      | Leave the current event lobby        |
| `/event rules`      | Show the description of current event|

### Admin Commands
| Command                        | Description                                  |
|--------------------------------|----------------------------------------------|
| `/event start <event_name>`    | Opens a lobby for the event                  |
| `/event start`                 | Starts the event if the lobby is ready       |
| `/event stop`                  | Force-stops the current event                |
| `/event setspawn lobby`        | Set the spawn location of the lobby          |
| `/event setspawn <event_name>` | Set the spawn location of an event           |
| `/event list`                  | List all available event plugins             |

---

## 🧩 How to Create a Custom Event Plugin

1. Create a new plugin project (standard Bukkit plugin with `plugin.yml`).
2. Implement the `EventGame` interface.
3. Register it as a Spring-style bean or static reference.
4. Export it as a `.jar` and put it in `/plugins`.

You don't need to call `register()` – detection is automatic.

### Example:

```java
public class MyEventGame implements EventGame {
    // store internal state...

    public void start(Collection<Player> players) { /* ... */ }
    public void stop() { /* ... */ }
    public boolean hasWinner() { return ...; }
    public List<Player> getWinners() { return ...; }
    public String getEventName() { return "MyCoolEvent"; }
    public String getEventDescription() { return "Last one standing wins!"; }
}
```

---

## 🔌 Integrations

| System         | Usage                        |
|----------------|------------------------------|
| Vault          | Give money as rewards        |
| PlaceholderAPI | Display event stats          |

Placeholders available:
- `%sem_current_event%`
- `%sem_current_event_desc%`
- `%sem_top_1%`, `%sem_top_2%`, etc.
- `%sem_wins_<event>%` per player

---

## 🧠 Design Philosophy

- **Separation of concerns**: logic of events and logic of management are isolated.
- **Extensibility**: add new events without touching core.
- **Modularity**: each event is an independent plugin.
- **User-friendly**: simple commands, clean bossbar display.

---

## 🧪 Test Scenarios

- Event with < 2 players → should cancel.
- Event ends → all players teleported back, rewards given.
- Event plugin returns no winners → fallback to default message.
- Players disconnect → are removed from event.

---

## 📜 License

MIT License

---

## 🤝 Contributing

Feel free to:
- Submit new event plugins
- Improve the reward system
- Add advanced scoreboard/bossbar integrations

Fork, PR, test!

---

## 📥 Contact / Support

Open a GitHub issue or join the support Discord (TBD).

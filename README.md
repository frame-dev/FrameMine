# FrameMine

FrameMine is a Spigot/Bukkit plugin for creating and managing resettable mines. It lets admins select a cuboid region, save it as a named mine, fill it with blocks, configure weighted materials, and start automatic resets.

The plugin is designed to compile against older Spigot APIs while still running on newer servers where possible.

## Features

- Create named mine regions from two positions.
- Select positions with commands or a positioning tool.
- Fill new mines with stone during setup.
- Configure weighted block materials for mine resets.
- Start automatic mine resets on a timer.
- Enable mines to auto-start their reset task when the plugin loads.
- Manage mines through commands or an in-game inventory GUI.
- Teleport players inside the mine to the top before/after reset safety handling.
- Runtime compatibility helpers for old and new Spigot material/sound names.

## Compatibility

FrameMine is currently configured to build against:

- Java target: `8`
- Spigot API: `1.8.8-R0.1-SNAPSHOT`

This lower compile target is intentional. It avoids requiring newer server APIs at compile time and helps the same jar run on older and newer Spigot-compatible servers.

Notes:

- `api-version` is not set in `plugin.yml`, so older servers are not blocked from loading it.
- Newer material names are resolved at runtime where needed.
- Server-specific forks may still differ in behavior. Test the jar on your target server version before using it on a production server.

## Installation

1. Build or download the plugin jar.
2. Place `FrameMine-1.0-RELEASE.jar` into your server's `plugins` folder.
3. Start or restart the server.
4. Give trusted admins the `framemine.admin` permission.
5. Use `/mine tool` or `/mine pos1` and `/mine pos2` to select a mine area.

## Permissions

| Permission | Default | Description |
| --- | --- | --- |
| `framemine.admin` | `op` | Allows use of all FrameMine admin commands. |
| `framemine.*` | inherited | Includes `framemine.admin`. |

## Commands

Main command:

```text
/mine
```

Alias:

```text
/framemine
```

Available subcommands:

| Command | Description |
| --- | --- |
| `/mine help` | Shows the command help. |
| `/mine tool` | Gives a stick-based mine positioning tool. |
| `/mine pos1` | Sets position 1 to your current location. |
| `/mine pos2` | Sets position 2 to your current location. |
| `/mine setup <mineName>` | Creates a mine from the selected positions and fills it with stone. |
| `/mine info <mineName>` | Shows saved mine information. |
| `/mine addmaterial <mineName> <material> <chance>` | Adds a weighted material to a mine. |
| `/mine setreset <mineName> <minutes>` | Sets the reset interval in minutes. |
| `/mine setautostart <mineName> <true\|false>` | Enables or disables automatic startup resets for a mine. |
| `/mine start <mineName>` | Starts the auto-reset task for a mine. |
| `/mine gui` | Opens the main setup GUI. |
| `/mine reload` | Reloads `config.yml`, including configurable messages. |

### Material Chances

Material chances are weighted values, not strict percentages. For example:

```text
/mine addmaterial exampleMine STONE 70
/mine addmaterial exampleMine DIAMOND_ORE 5
/mine addmaterial exampleMine COAL_ORE 25
```

This makes stone much more likely than diamond ore. The plugin totals all configured weights and randomly chooses one material during resets.

If no valid materials are configured, the mine falls back to `STONE`.

## GUI Usage

Open the GUI:

```text
/mine gui
```

### Mine Setup GUI

The setup GUI includes:

- `Set Position 1`: Sets the first position to your current location.
- `Set Position 2`: Sets the second position to your current location.
- `Setup Mine`: Prompts you in chat for a mine name, then creates the mine.
- `Mine Selection`: Opens the list of saved mines.

### Mine Selection GUI

Shows all saved mines. Click a mine to select and manage it.

### Mine Management GUI

Once a mine is selected, the management GUI includes:

- `Materials`: Shows configured mine materials.
- `Add Materials`: Opens the material picker.
- `Start`: Starts or restarts the automatic reset task.
- `Reset Time`: Opens reset interval presets.
- `Autostart`: Toggles whether this mine starts resetting when the plugin enables.
- `Remove Mine`: Deletes the mine from config.
- `Info`: Shows mine details.

### Materials GUI

The materials GUI lists configured materials for the selected mine.

Controls:

- `Shift + Right Click`: Remove the material.
- `Shift + Left Click`: Halve the material's current chance.
- `Middle Click`: Open the chance selector for that material.

### Chance Selector

Choose a predefined chance value, then click `Set Chance`.

## Positioning Tool

Run:

```text
/mine tool
```

You receive a stick named `Mine Positioning Tool`.

Controls:

- Left-click a block to set position 1.
- Right-click a block to set position 2.

## Configuration

The generated `config.yml` contains plugin settings, configurable messages, and saved mine data. Mines are saved automatically under the `mine` section after setup.

Example structure:

```yaml
settings:
  prefix: "&7[&bFrameMine&7] &c» &7"
  position-tool-name: "&aMine Positioning Tool"
  default-reset-minutes: 5
  log-reset-messages: true
  log-next-reset: true

messages:
  mine-not-found: "{prefix}&cMine not found."
  mine-created: "{prefix}&aMine &b{mine} &ahas been created and filled with stone."

mine:
  exampleMine:
    name: exampleMine
    pos1: world,100.0,64.0,100.0,0.0,0.0
    pos2: world,120.0,80.0,120.0,0.0,0.0
    materials:
      STONE: 70.0
      COAL_ORE: 25.0
      DIAMOND_ORE: 5.0
    autostart: true
    reset: 5
```

Field meanings:

| Field | Description |
| --- | --- |
| `settings.prefix` | Prefix used by configurable messages. |
| `settings.position-tool-name` | Display name for the mine selection tool. |
| `settings.default-reset-minutes` | Default reset interval for newly-created mines. |
| `settings.log-reset-messages` | Enables reset completion logs in console. |
| `settings.log-next-reset` | Enables next-reset-time logs in console. |
| `messages` | Player-facing message templates. Supports color codes with `&`. |
| `name` | Saved mine name. |
| `pos1` | First corner location. |
| `pos2` | Second corner location. |
| `materials` | Weighted material map used during resets. |
| `autostart` | Whether the mine starts resetting when the plugin enables. |
| `reset` | Reset interval in minutes. |

Location format:

```text
world,x,y,z,yaw,pitch
```

The loader also accepts semicolon-separated values for compatibility:

```text
world;x;y;z;yaw;pitch
```

### Configurable Messages

Every player-facing command/chat feedback message is loaded from `messages`.

Supported common placeholders:

| Placeholder | Meaning |
| --- | --- |
| `{prefix}` | Configured plugin prefix. |
| `{mine}` | Mine name. |
| `{material}` | Material name. |
| `{chance}` | Material chance/weight. |
| `{reset}` | Reset interval in minutes. |
| `{autostart}` | `true` or `false`. |
| `{pos1}` | First saved mine position. |
| `{pos2}` | Second saved mine position. |
| `{materials}` | Formatted material list. |

After editing messages or settings, reload them in game:

```text
/mine reload
```

## Building From Source

Requirements:

- Java JDK 8 or newer
- Maven 3.x

Build:

```bash
mvn clean package
```

The jar will be created at:

```text
target/FrameMine-1.0-RELEASE.jar
```

Run checks:

```bash
mvn clean test
```

## Development Notes

- The plugin intentionally compiles against `spigot-api:1.8.8-R0.1-SNAPSHOT` for broad compatibility.
- The Spigot API is marked as `provided`, because the server provides Bukkit/Spigot classes at runtime.
- The project uses local helpers instead of an external `SpigotUtils` dependency.
- `ItemBuilder` is included locally for creating simple inventory items.
- Compatibility helpers live in `Utils`.

## Troubleshooting

### The plugin does not load

Check the server console for startup messages. Make sure the jar is in the `plugins` folder and that your server supports Bukkit/Spigot plugins.

### `/mine` is not available

Confirm `plugin.yml` is inside the jar and contains the `mine` command. The plugin logs a severe message and disables itself if the command is missing.

### A mine does not reset

Check:

- The mine exists in `config.yml`.
- Both positions are valid.
- The saved world is loaded.
- The reset interval is greater than `0`.
- You started it with `/mine start <mineName>` or enabled autostart.

### Materials are ignored

Use valid Bukkit material names for your server version. Newer material names may not exist on older servers. Invalid saved materials are skipped and logged.

### Players stay inside the mine during reset

The plugin teleports players inside the mine region to the top center of the mine before reset handling completes. If the top is unsafe, adjust the mine region or add a safe platform above it.

## License

This project includes a `LICENSE` file. See it for the full license text.

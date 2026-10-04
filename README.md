# OdinPortfolioPlugin v2

A portfolio-quality Paper plugin demonstrating practical Minecraft server development.

## Features

- `/odin menu` interactive GUI
- `/odin stats` player statistics
- `/odin setspawn` configurable server spawn
- `/odin reload` hot-reloads configuration
- Permission nodes
- Configurable MiniMessage UI
- Spawn teleport cooldown
- Async teleport API
- Player join event
- Inventory click event
- Tab completion
- Clean command/event separation

## Commands

| Command | Permission | Purpose |
|---|---|---|
| `/odin menu` | `odin.use` | Opens the GUI |
| `/odin stats` | `odin.use` | Shows player statistics |
| `/odin setspawn` | `odin.setspawn` | Saves current location as Odin spawn |
| `/odin reload` | `odin.reload` | Reloads configuration |

## Build

Requires Java 25 and Gradle.

```bash
gradle build
```

The JAR will be created under:

```text
build/libs/OdinPortfolioPlugin-2.0.0.jar
```

Paper's official documentation describes `plugin.yml` as the primary plugin metadata file and documents the 26.2 API line. See the Paper developer documentation before deploying to a production server.

## Portfolio note

This project intentionally demonstrates multiple real Paper concepts instead of being a single-command example. The next logical improvements would be persistent YAML/SQLite player data, automated tests for command logic, and a proper service layer for larger features.

<p align="center">
  <img src="images/plugin-logo.png" alt="Area Rewind" width="128" />
</p>
# Area-Rewind

Area-Rewind is an advanced Minecraft region protection and rollback plugin focused on fast recovery workflows, collaborative trust management, and visual administration tools.

## Features

- Protected region management with ownership and trust rules
- Backup lifecycle tools: create, list, diff, restore, undo, and cleanup
- Visual region controls including show, hide, and backup preview particles
- Automatic backup intervals for routine protection points
- Import and export support for schematic-style workflows
- Full GUI-driven management for players and admins
- Optional WorldEdit integration for enhanced editing workflows

## Commands

Main command:

- `/rewind` (aliases: `/arearewind`, `/protect`, `/ar`)

Common workflows:

| Command | Description |
| --- | --- |
| `/rewind save <name>` | Save a selected area as a protected region |
| `/rewind backup <area>` | Create a backup snapshot |
| `/rewind history <area> [page]` | View backup history |
| `/rewind restore <area> <backup_id\|latest\|oldest>` | Restore an area from a backup |
| `/rewind rollback <area> <time>` | Roll back to a time offset |
| `/rewind undo <area>` | Undo the last restore/rollback action |
| `/rewind trust <area> <player>` | Add trusted player access |
| `/rewind untrust <area> <player>` | Remove trusted player access |
| `/rewind show <area>` | Visualize area boundaries |
| `/rewind hide [area]` | Hide visualization |
| `/rewind cleanup <area> [days]` | Remove older backups |
| `/rewind export` | Export latest area backup for external workflows |

Administrative and utility commands:

- `/rewind reload`
- `/rewind list [all|owned|trusted]`
- `/rewind info <area>`
- `/rewind status`
- `/rewind help`
- `/rewind tool`
- `/rewind pos1`
- `/rewind pos2`
- `/rewind expand <area> <direction> <amount>`
- `/rewind contract <area> <direction> <amount>`
- `/rewind rename <old_name> <new_name>`
- `/rewind delete <area>`
- `/rewind scan <area>`
- `/rewind diff <area> <id1> <id2>`
- `/rewind permission <add|remove|list> <area> [player]`
- `/rewind seticon <area> <material>`
- `/rewind seticon backup <area> <backup_id> <material>`
- `/rewind interval <set|remove|check> <area> [minutes] [backup_id]`
- `/rewind teleport <area>`
- `/rewind preview <area> [backup_id] [particle_type]`

## Permissions

| Permission | Default | Description |
| --- | --- | --- |
| `arearewind.*` | op | Full access to all Area-Rewind capabilities |
| `arearewind.admin` | op | Administrative access |
| `arearewind.use` | true | Basic plugin usage |
| `arearewind.create` | true | Create areas |
| `arearewind.delete` | true | Delete owned areas |
| `arearewind.backup` | true | Create backups |
| `arearewind.restore` | true | Restore backups |
| `arearewind.trust` | true | Manage trusted users |
| `arearewind.visualize` | true | Visualization controls |
| `arearewind.gui` | true | GUI access |
| `arearewind.export` | true | Export area data |
| `arearewind.import` | true | Import area data |

## Installation

1. Download the latest jar from Releases.
2. Place the jar in your server `plugins` directory.
3. Restart the server.
4. Configure permissions (and optional WorldEdit integration).

## Compatibility

- API: Paper/Spigot/Bukkit 1.19+
- Optional integration: WorldEdit

## License

This project is licensed under the MIT License.
- `arearewind.visualize`: Visualize area boundaries (default: true)
- `arearewind.gui`: Use GUI interfaces (default: true)
- `arearewind.export`: Export areas (default: true)
- `arearewind.import`: Import areas (default: true)

## Installation

1. Build with Maven: `mvn clean package`
2. Place the generated JAR in your server's `plugins` folder.
3. Configure `config.yml` and `plugin.yml` as needed.

## Configuration

- `config.yml`: Plugin settings
- `plugin.yml`: Bukkit plugin metadata

## Authors

- Bernd Julian Hagen
- Andreas Hagen

## Website

[GitHub Repository](https://github.com/BerndHagen/Minecraft-Server-Plugins)

## License

See `LICENSE` file for details.

## Project Structure & Customization

Below is a guide to the main components of the plugin and where to modify each part:

- **Modern GUI System:**

  - Location: `src/main/java/arearewind/gui/`
    - `EnhancedGUIManager.java`: Coordinates the new GUI system
    - `utils/`: Base classes, item builders, and GUI utilities
    - `menus/`: Specific menu implementations for different features
  - Description: Comprehensive graphical interface system providing intuitive access to all plugin features. The new system includes a main menu, area management with creation wizards, advanced backup management, permission interfaces, and administrative tools. All functionality is accessible through easy-to-navigate menus with visual feedback and confirmation dialogs.

- **GUI Manager (Integration Layer):**

  - Location: `src/main/java/arearewind/managers/GUIManager.java`
  - Description: Clean integration layer that delegates to the enhanced GUI system while maintaining backwards compatibility. All legacy GUI code has been removed in favor of the modern, modular approach.

- **Core Functionality:**

  - Location: `src/main/java/arearewind/managers/`
    - `AreaManager.java`: Area creation, management, and logic
    - `BackupManager.java`: Backup and restore logic
    - `PermissionManager.java`: Permission checks and management
    - `FileManager.java`, `IntervalManager.java`, `VisualizationManager.java`: Supporting features
  - Description: These files implement the main logic for area protection, backup, permissions, and other features. Edit these to change how the plugin works internally.

- **Commands:**

  - Location: `src/main/java/arearewind/commands/CommandHandler.java`
  - Description: Handles all `/rewind` commands and their subcommands. Modify here to add, remove, or change command behavior.

- **Event Listeners:**

  - Location: `src/main/java/arearewind/listeners/`
  - Description: Responds to player actions and world events. Edit these files to customize plugin reactions to events.

- **Data Models:**

  - Location: `src/main/java/arearewind/data/`
  - Description: Classes representing protected areas, backups, and block info. Change these to adjust how data is stored and managed.

- **Configuration:**

  - Location: `src/main/resources/config.yml`
  - Description: Main plugin configuration file. Adjust settings, defaults, and options here.

- **Plugin Metadata & Permissions:**

  - Location: `src/main/resources/plugin.yml`
  - Description: Defines plugin name, version, commands, and permissions. Edit to change command registration and permission nodes.

- **Build Output:**
  - Location: `target/Area-Rewind-<version>.jar`
  - Description: The compiled plugin JAR to deploy on your server.

Refer to these locations when customizing or extending the plugin for your server's needs.


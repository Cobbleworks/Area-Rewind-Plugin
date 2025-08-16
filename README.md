# AreaRewind

Advanced area protection and backup system for Minecraft servers

🎉 **Special thanks to Bernd Hagen, the original author of AreaRewind!** 🎉

## Overview

AreaRewind is a powerful Minecraft plugin that allows server administrators and players to protect, manage, and restore areas in their world. It features an intuitive GUI, robust backup/restore capabilities, and fine-grained permissions for collaborative play.

## Features

- **Protected Areas**: Create, manage, and delete protected regions.
- **Backups**: Create backups of areas, restore previous states, preview and compare backups, undo/redo changes.
- **Enhanced GUI System**: Comprehensive graphical interface with:
  - Main menu with categorized access to all features
  - Area management with creation wizard and quick actions
  - Advanced backup management with visual timeline
  - Permission and trust management interfaces
  - Visualization controls and boundary editing
  - Import/export functionality
  - Administrative tools and settings
- **Permissions**: Fine-grained permission system for all plugin features, including area creation, deletion, backup, restore, trust management, visualization, import/export, and GUI access.
- **Trusted Players**: Manage trusted players for each area.
- **Visualization**: Visualize area boundaries and states.
- **Import/Export**: Export and import area data for portability. Either drag into folders, or use the /rewind export command to export to a WorldEdit schematic file.
- **WorldEdit Integration**: Enhanced functionality if WorldEdit is present.

## Commands

AreaRewind provides a rich set of commands for managing protected areas, backups, permissions, and more. Below is a comprehensive list of all available commands:

| Command      | Usage                                                                                     | Description                                                        | Aliases            |
| ------------ | ----------------------------------------------------------------------------------------- | ------------------------------------------------------------------ | ------------------ |
| rewind       | /rewind                                                                                   | Open the AreaRewind management GUI                                 | gui                |
| reload       | /rewind reload                                                                            | Reload plugin configuration                                        |                    |
| diff         | /rewind diff <area> <id1> <id2>                                                           | Compare two backups and show differences                           | compare            |
| scan         | /rewind scan <area>                                                                       | Scan for changes since the last backup                             |                    |
| contract     | /rewind contract <area> <direction> <amount>                                              | Contract an area in a specific direction                           |                    |
| delete       | /rewind delete <area>                                                                     | Delete an area and all its backups                                 |                    |
| expand       | /rewind expand <area> <direction> <amount>                                                | Expand an area in a specific direction                             |                    |
| pos1         | /rewind pos1                                                                              | Set the first position for area selection                          |                    |
| pos2         | /rewind pos2                                                                              | Set the second position for area selection                         |                    |
| rename       | /rewind rename <old_name> <new_name>                                                      | Rename an area                                                     |                    |
| save         | /rewind save <name>                                                                       | Save the selected area with a name                                 |                    |
| backup       | /rewind backup <area>                                                                     | Create a backup of an area                                         |                    |
| rollback     | /rewind rollback <area> <time>                                                            | Rollback an area to a specific time ago                            |                    |
| undo         | /rewind undo <area>                                                                       | Undo the last backup restore operation                             |                    |
| restoreblock | /rewind restoreblock <area> <backup_id\|latest\|oldest> [world]                           | Restore an area from a backup (for command blocks and console)     |                    |
| restore      | /rewind restore <area> <backup_id\|latest\|oldest>                                        | Restore an area from a backup                                      |                    |
| history      | /rewind history <area> [page]                                                             | View backup history for an area                                    |                    |
| info         | /rewind info <area>                                                                       | View detailed information about an area                            |                    |
| list         | /rewind list [all\|owned\|trusted]                                                        | List protected areas                                               |                    |
| cleanup      | /rewind cleanup <area> [days]                                                             | Clean up old backups for an area                                   |                    |
| permission   | /rewind permission <add\|remove\|list> <area> [player]                                    | Manage area permissions                                            | perm, permissions  |
| seticon      | /rewind seticon <area> <material> OR /rewind seticon backup <area> <backup_id> <material> | Set custom icons for areas and backups                             | icon, setitem      |
| trust        | /rewind trust <area> <player>                                                             | Add a player as trusted for an area                                |                    |
| untrust      | /rewind untrust <area> <player>                                                           | Remove a player's trust for an area                                |                    |
| interval     | /rewind interval <set\|remove\|check> <area> [minutes] [backup_id]                        | Manage automatic backup intervals                                  | timer, schedule    |
| teleport     | /rewind teleport <area>                                                                   | Teleport to an area                                                | tp                 |
| help         | /rewind help                                                                              | Show help information                                              |                    |
| hide         | /rewind hide [area]                                                                       | Hide area visualization                                            | clear              |
| preview      | /rewind preview <area> [backup_id] [particle_type]                                        | Show a visual preview of a backup with particles                   | particle           |
| show         | /rewind show <area>                                                                       | Show area visualization                                            | display, visualize |
| status       | /rewind status                                                                            | Show system status and statistics                                  |                    |
| tool         | /rewind tool                                                                              | Get the area selection tool                                        | wand, item         |
| export       | /rewind export                                                                            | Export an areas latest backup to a .schem file for WorldEdit usage |                    |

## Permissions

AreaRewind uses a hierarchical permission system. All permissions are configurable in `plugin.yml`:

- `arearewind.*`: Grants access to all features (default: op)
- `arearewind.admin`: Administrative access (default: op)
- `arearewind.use`: Basic usage (default: true)
- `arearewind.create`: Create protected areas (default: true)
- `arearewind.delete`: Delete owned areas (default: true)
- `arearewind.backup`: Create backups (default: true)
- `arearewind.restore`: Restore from backups (default: true)
- `arearewind.trust`: Manage trusted players (default: true)
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

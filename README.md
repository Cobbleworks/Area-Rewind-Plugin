<p align="center">
  <img src="images/plugin-logo.png" alt="Area Rewind" width="128" />
</p>
<h1 align="center">Area Rewind</h1>
<p align="center">
  <b>Advanced area protection and backup system for Minecraft servers.</b><br>
  <b>GUI management, automatic backups, undo/redo, and WorldEdit integration.</b>
</p>
<p align="center">
  <a href="https://github.com/Cobbleworks/Area-Rewind/releases"><img src="https://img.shields.io/github/v/release/Cobbleworks/Area-Rewind?include_prereleases&style=flat-square&color=4CAF50" alt="Latest Release"></a>&nbsp;&nbsp;<a href="https://github.com/Cobbleworks/Area-Rewind/blob/main/LICENSE"><img src="https://img.shields.io/badge/License-MIT-blue?style=flat-square" alt="License"></a>&nbsp;&nbsp;<img src="https://img.shields.io/badge/Java-17+-orange?style=flat-square" alt="Java Version">&nbsp;&nbsp;<img src="https://img.shields.io/badge/Minecraft-1.21+-green?style=flat-square" alt="Minecraft Version">&nbsp;&nbsp;<img src="https://img.shields.io/badge/Platform-Spigot%2FPaper-yellow?style=flat-square" alt="Platform">&nbsp;&nbsp;<img src="https://img.shields.io/badge/Status-Active-brightgreen?style=flat-square" alt="Status">
</p>

Area Rewind is an open-source Minecraft plugin that provides comprehensive area protection and backup management for Spigot and Paper servers. Players and administrators can create protected zones with a wooden hoe selection tool, schedule automatic backups at configurable intervals, and restore any area to a previous state using an intuitive GUI or command system. The plugin supports undo/redo history, WorldEdit schematic export, particle-based boundary visualization, and fine-grained permission management with owner and trusted player roles.

### **Core Features**

- **Enhanced GUI System:** Comprehensive graphical interface with main menu, area creation wizard, backup management timeline, permission and trust management, visualization controls, and administrative tools
- **Area Protection:** Create and manage protected areas using a wooden hoe selection tool with position-based corner selection
- **Automatic Backups:** Scheduled backups with configurable intervals and retention policies to protect against griefing and accidents
- **Manual Backups:** Create on-demand backups for important moments before major builds or events
- **Undo/Redo System:** Full undo/redo functionality for area changes with complete history tracking
- **Advanced Restoration:** Restore areas to any previous backup state with preview functionality and time-based rollback support
- **Permission System:** Owner and trusted player system with granular per-permission control
- **Visualization:** Particle-based area boundary visualization with customizable particle types and effects
- **Import/Export:** Export and import area data for portability, including WorldEdit schematic export
- **WorldEdit Integration:** Enhanced functionality and compatibility when WorldEdit is installed
- **Block State Support:** Full support for containers with inventory contents, signs with text, banners with patterns, and all complex block states
- **Custom Icons:** Set custom display icons for areas and individual backups in the GUI
- **Change Detection:** Scan for changes since the last backup and compare any two backups with a diff tool

### **Supported Platforms**

- **Server Software:** `Spigot`, `Paper`, `Purpur`, `CraftBukkit`
- **Minecraft Versions:** `1.21.5`, `1.21.6`, `1.21.7`, `1.21.8`, `1.21.9`, `1.21.10` and higher
- **Java Requirements:** `Java 17+`

### **Installation**

1. Download the latest `.jar` from the [Releases](https://github.com/Cobbleworks/Area-Rewind/releases) page
2. Stop your Minecraft server
3. Copy the `.jar` into your server's `plugins` folder
4. Start your server — a default configuration folder is generated at `plugins/AreaRewind/`

### **Player Commands**

| Command | Description |
|---------|-------------|
| `/rewind` | Open the Area Rewind management GUI |
| `/rewind tool` | Get the area selection tool (wooden hoe) |
| `/rewind pos1` | Set position 1 to the block you are looking at |
| `/rewind pos2` | Set position 2 to the block you are looking at |
| `/rewind save <name>` | Create a protected area with the current selection |
| `/rewind list [owned\|trusted\|all]` | List protected areas with filtering |
| `/rewind info <area>` | Show detailed information about an area |
| `/rewind teleport <area>` | Teleport to the center of an area |
| `/rewind backup <area>` | Create a manual backup of an area |
| `/rewind restore <area> <backup_id\|latest\|oldest>` | Restore an area from a backup |
| `/rewind undo <area>` | Undo the last change to an area |
| `/rewind history <area> [page]` | View backup history with pagination |
| `/rewind rollback <area> <time>` | Roll back to a specific time offset (e.g., `2h`, `30m`, `1d`) |
| `/rewind preview <area> [backup_id] [particle_type]` | Preview a backup with particle visualization |
| `/rewind show <area>` | Visualize area boundaries with particles |
| `/rewind hide [area]` | Hide area boundary visualization |
| `/rewind trust <area> <player>` | Add a trusted player to an area |
| `/rewind untrust <area> <player>` | Remove a trusted player from an area |
| `/rewind export` | Export the area's latest backup to a WorldEdit `.schem` file |

### **Administrative Commands**

| Command | Description |
|---------|-------------|
| `/rewind reload` | Reload plugin configuration |
| `/rewind delete <area>` | Delete an area and all its backups |
| `/rewind rename <old_name> <new_name>` | Rename an area |
| `/rewind expand <area> <direction> <amount>` | Expand an area in the specified direction |
| `/rewind contract <area> <direction> <amount>` | Shrink an area in the specified direction |
| `/rewind permission <add\|remove\|list> <area> [player]` | Manage area permissions |
| `/rewind seticon <area> <material>` | Set a custom display icon for an area |
| `/rewind seticon backup <area> <backup_id> <material>` | Set a custom icon for a specific backup |
| `/rewind cleanup <area> [days]` | Remove old backups older than the specified number of days (default: 7) |
| `/rewind scan <area>` | Scan for block changes since the last backup |
| `/rewind diff <area> <id1> <id2>` | Compare two backups and display differences |
| `/rewind interval <set\|remove\|check> <area> [minutes] [backup_id]` | Manage automatic backup intervals |
| `/rewind status` | Show system status and statistics |
| `/rewind help` | Show help information |

**Aliases:** `/ar`, `/arearewind` — **Tool:** Wooden hoe for area selection

### **License**

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.


## **Screenshots**

The screenshots below demonstrate the core features of the Area Rewind plugin, including the area management GUI, backup system, boundary visualization, and restore functionality.

<table>
  <tr>
    <th>Area Rewind - Management GUI</th>
    <th>Area Rewind - Backup System</th>
  </tr>
  <tr>
    <td><a href="https://github.com/Cobbleworks/Area-Rewind/raw/main/images/screenshot-gui.png" target="_blank" rel="noopener noreferrer"><img src="https://github.com/Cobbleworks/Area-Rewind/raw/main/images/screenshot-gui.png" alt="Management GUI" width="450"></a></td>
    <td><a href="https://github.com/Cobbleworks/Area-Rewind/raw/main/images/screenshot-backup.png" target="_blank" rel="noopener noreferrer"><img src="https://github.com/Cobbleworks/Area-Rewind/raw/main/images/screenshot-backup.png" alt="Backup System" width="450"></a></td>
  </tr>
  <tr>
    <th>Area Rewind - Area Visualization</th>
    <th>Area Rewind - Restore Features</th>
  </tr>
  <tr>
    <td><a href="https://github.com/Cobbleworks/Area-Rewind/raw/main/images/screenshot-visualization.png" target="_blank" rel="noopener noreferrer"><img src="https://github.com/Cobbleworks/Area-Rewind/raw/main/images/screenshot-visualization.png" alt="Area Visualization" width="450"></a></td>
    <td><a href="https://github.com/Cobbleworks/Area-Rewind/raw/main/images/screenshot-restore.png" target="_blank" rel="noopener noreferrer"><img src="https://github.com/Cobbleworks/Area-Rewind/raw/main/images/screenshot-restore.png" alt="Restore Features" width="450"></a></td>
  </tr>
</table>

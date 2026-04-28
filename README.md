<p align="center">
  <img src="images/plugin-logo.png" alt="Area Rewind Plugin" width="180" />
</p>
<h1 align="center">Area Rewind Plugin</h1>
<p align="center">
  <b>Advanced area protection and backup system with undo, diff, and schematic export.</b><br>
  <b>GUI-driven management, per-area intervals, and full WorldEdit integration.</b>
</p>
<p align="center">
  <a href="https://github.com/Cobbleworks/Area-Rewind-Plugin/releases"><img src="https://img.shields.io/github/v/release/Cobbleworks/Area-Rewind-Plugin?include_prereleases&style=flat-square&color=4CAF50" alt="Latest Release"></a>&nbsp;&nbsp;<a href="https://github.com/Cobbleworks/Area-Rewind-Plugin/blob/main/LICENSE"><img src="https://img.shields.io/badge/License-MIT-blue?style=flat-square" alt="License"></a>&nbsp;&nbsp;<img src="https://img.shields.io/badge/Java-17+-orange?style=flat-square" alt="Java Version">&nbsp;&nbsp;<img src="https://img.shields.io/badge/Minecraft-1.19+-green?style=flat-square" alt="Minecraft Version">&nbsp;&nbsp;<img src="https://img.shields.io/badge/Platform-Spigot%2FPaper-yellow?style=flat-square" alt="Platform">&nbsp;&nbsp;<img src="https://img.shields.io/badge/Status-Active-brightgreen?style=flat-square" alt="Status">&nbsp;&nbsp;<a href="https://github.com/Cobbleworks/Area-Rewind-Plugin/issues"><img src="https://img.shields.io/github/issues/Cobbleworks/Area-Rewind-Plugin?style=flat-square&color=orange" alt="Open Issues"></a>
</p>

Area Rewind is an open-source Minecraft plugin that gives server administrators and players a complete area protection and backup system. Define protected regions using a wooden hoe or coordinate commands, take named backups at any point, and restore any backup at any later time -- including from the past with time-based rollback. Every restoration runs in a batched, lag-aware manner so large areas (up to 1,000,000 blocks) can be rebuilt without freezing the server. The plugin stores each backup as individual block states, including full container inventories, sign text, and banner patterns, so nothing is lost on restore.

A built-in inventory GUI makes managing areas and backups accessible to any player without needing to memorize commands. Automatic backup intervals, backup comparison (diff), particle preview of what will change, WorldEdit `.schem` export, and a comprehensive trust and permission system round out the feature set.

### **Core Features**

- **Area Selection:** Select two corner positions with a wooden hoe (left-click = pos1, right-click = pos2) or using `/rewind pos1` and `/rewind pos2` commands; WorldEdit selections are also accepted if WorldEdit is installed
- **Protected Areas:** Named, owner-bound regions stored persistently in YAML files -- creation, deletion, rename, expand, and contract all supported
- **Manual & Automatic Backups:** Create on-demand backups at any moment, or configure per-area automatic backup intervals (in minutes) that run indefinitely without admin attention
- **Full Restoration:** Restore any saved backup by ID, by `latest` or `oldest` shorthand, or by time expression (e.g., `2h`, `30m`, `1d`) " all block states, inventories, signs, and banners are restored exactly
- **Undo/Redo:** Instantly revert or re-apply the most recent restore or change to an area without specifying a backup ID
- **Diff & Scan:** Compare any two backups side by side to see exactly which blocks changed (`/rewind diff`), or scan an area for changes since the last backup (`/rewind scan`)
- **Particle Preview:** Preview what a restoration will change before committing -- affected blocks are highlighted with configurable particle effects
- **Boundary Visualization:** Display particle-based area boundaries in real time to confirm region extents and avoid mistakes
- **WorldEdit Export:** Export an area's latest backup directly to a `.schem` file for use in WorldEdit, VoxelSniper, or external map editors
- **Trust System:** Grant specific players trust on individual areas, allowing them to create backups and restore without being the area owner
- **Granular Permissions:** 9 independent permission nodes (`arearewind.create`, `arearewind.backup`, `arearewind.restore`, etc.) for fine-grained access control via any permission manager
- **Full Block State Support:** Containers with inventories (chests, barrels, shulker boxes), signs with text, banners with patterns, and all other complex block states are fully serialized and restored
- **GUI Management:** Complete inventory-based GUI with area overview, backup timeline, trust management, visualization controls, icon customization, and admin tools -- all without entering commands
- **Performance Tuning:** Configurable restoration batch sizes (min/max blocks per tick), max area size cap, and rate-limit cooldown to balance restoration speed against server performance

### **Supported Platforms**

- **Server Software:** `Spigot`, `Paper`, `Purpur`, `CraftBukkit`
- **Minecraft Versions:** `1.19` and higher
- **Java Requirements:** `Java 17+`
- **Optional Dependencies:** WorldEdit (enhances selection tool and enables `.schem` export)

## **Table of Contents**

1. [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Installation Steps](#installation-steps)
    - [First Launch & Configuration](#first-launch--configuration)
    - [Verifying Installation](#verifying-installation)
2. [Configuration](#configuration)
    - [config.yml Reference](#configyml-reference)
3. [How It Works](#how-it-works)
    - [Area Selection](#area-selection)
    - [Backups and Restoration](#backups-and-restoration)
    - [Interval Backups](#interval-backups)
    - [Diff and Scan](#diff-and-scan)
    - [WorldEdit Export](#worldedit-export)
4. [Player Commands](#player-commands)
    - [Basic Area Commands](#basic-area-commands)
    - [Backup & Restore Commands](#backup--restore-commands)
    - [Visualization & Trust Commands](#visualization--trust-commands)
5. [Administrative Commands](#administrative-commands)
6. [Permissions](#permissions)
7. [Building from Source](#building-from-source)
8. [License](#license)
9. [Screenshots](#screenshots)

## **Getting Started**

### **Prerequisites**

Before installing Area Rewind, confirm the following requirements are met:

- A Minecraft server running **Spigot**, **Paper**, **Purpur**, or any compatible fork
- Server version **1.19 or higher** (`api-version: 1.19` is the minimum)
- **Java 17** or newer installed on the machine running the server
- Operator or console access to install plugin files

**Optional:** If [WorldEdit](https://enginehub.org/worldedit) is installed on the server, Area Rewind will use WorldEdit for selection compatibility and enable `.schem` schematic export for all areas. If WorldEdit is not present, the plugin's built-in wooden hoe selection mode is used automatically.

### **Installation Steps**

1. Download the latest `AreaRewind-x.x.x.jar` from the [Releases](https://github.com/Cobbleworks/Area-Rewind-Plugin/releases) page
2. (Optional) Download and install [WorldEdit](https://enginehub.org/worldedit) if you want schematic export support
3. **Stop your server completely** before placing any files
4. Copy the `.jar` into your server's `plugins/` directory
5. Start the server -- Area Rewind generates its configuration folder automatically on first boot

### **First Launch & Configuration**

On the first server start after installation, Area Rewind creates:

```
plugins/
"""" AreaRewind/
    "oe"" config.yml        + Performance, backup limits, and visualization settings
    "oe"" areas/            + One YAML file per protected area
    """" backups/          + One YAML file per backup
```

- **`config.yml`** controls backup retention limits, area size caps, restoration batch performance, rate-limit cooldowns, particle distances, and wooden hoe selection behavior. See the [Configuration](#configuration) section for all keys and their defaults.
- **`areas/`** is managed automatically by the plugin. Each protected area is stored as a separate YAML file named after its ID.
- **`backups/`** is managed automatically. Each backup is stored as a separate YAML file with full block-state serialization. Do not edit or delete these files manually.

### **Verifying Installation**

- Run `/plugins` in-game -- `AreaRewind` should appear green in the list
- Run `/version AreaRewind` to confirm the installed version
- Run `/rewind` in-game to open the management GUI -- a chest inventory should open
- Left-click a block with a wooden hoe to confirm pos1 selection is working
- If the plugin fails to load, check the server console for `AreaRewind` error messages

## **Configuration**

### **config.yml Reference**

```yaml
backup:
  max-backups-per-area: 50       # Maximum stored backups per area before oldest are pruned

performance:
  max-area-size: 1000000         # Maximum number of blocks an area may contain
  rate-limit-cooldown: 1000      # Milliseconds between consecutive backup/restore operations

  restore:
    max-batch-size: 400          # Max blocks restored per tick (large areas) " higher = faster, more lag
    min-batch-size: 100          # Min blocks restored per tick (small areas) " lower = slower, less lag

visualization:
  particle-distance: 50          # Max distance (blocks) at which visualization particles are rendered

selection:
  wooden-hoe:
    force-enabled: false         # Force wooden hoe mode on for all players (ignores WorldEdit)
    auto-fallback: true          # Automatically use wooden hoe if WorldEdit fails or is absent
```

| Key | Default | Description |
|-----|---------|-------------|
| `backup.max-backups-per-area` | `50` | Hard limit on stored backups per area; oldest are deleted when exceeded |
| `performance.max-area-size` | `1000000` | Maximum blocks in a single area; selections exceeding this are rejected |
| `performance.rate-limit-cooldown` | `1000` | Cooldown in ms between backup/restore operations to prevent spam |
| `performance.restore.max-batch-size` | `400` | Blocks restored per server tick for large areas; increase for speed at cost of TPS |
| `performance.restore.min-batch-size` | `100` | Blocks restored per server tick for small areas |
| `visualization.particle-distance` | `50` | Radius in blocks within which boundary particles are rendered for a player |
| `selection.wooden-hoe.force-enabled` | `false` | When `true`, always uses wooden hoe selection regardless of WorldEdit |
| `selection.wooden-hoe.auto-fallback` | `true` | Automatically activates wooden hoe selection if WorldEdit is unavailable |

> **Tip:** If your server hosts large builds (> 200,000 blocks), reduce `max-batch-size` to `200` and `min-batch-size` to `50` to keep TPS stable during restorations.

## **How It Works**

### **Area Selection**

Before creating a protected area, you must define its bounds using two corner positions. Area Rewind supports two selection modes:

- **Wooden Hoe Mode** (built-in, always available): Left-click any block to set pos1, right-click any block to set pos2. Run `/rewind save <name>` to create the area from the two corners.
- **WorldEdit Mode** (requires WorldEdit): Use WorldEdit's own selection tools (`//wand`, `//pos1`, `//pos2`, etc.). Area Rewind reads the current WorldEdit selection automatically when `/rewind save` is run.
- **Command Mode**: Use `/rewind pos1` and `/rewind pos2` to set positions to the block you are looking at, without holding the wooden hoe.

The two positions define an axis-aligned bounding box. All blocks within that box (inclusive) belong to the area. Selections exceeding `max-area-size` blocks are rejected.

### **Backups and Restoration**

Each backup captures the block type, block data (facing direction, open/closed state, etc.), and full block entity contents (chest inventories, sign text, banner patterns, etc.) for every block in the area at the moment the backup is created.

Restoration runs in batches across multiple server ticks to avoid TPS drops. The batch size scales with the area size between `min-batch-size` and `max-batch-size`. Players receive real-time progress feedback via chat messages during restoration.

Restores can be triggered by:
- **Backup ID**: `/rewind restore <area> <id>` -- use `/rewind history <area>` to see available IDs
- **Shorthand**: `/rewind restore <area> latest` or `/rewind restore <area> oldest`
- **Time expression**: `/rewind rollback <area> 2h` -- restores to the state closest to 2 hours ago
- **GUI**: select the area, browse the backup timeline, and click to restore
- **Command block / console**: `/rewind restoreblock <area> <id> [world]` -- safe for automation

### **Interval Backups**

Per-area automatic backup intervals let the plugin create backups on a schedule without any player or admin involvement. Set an interval with `/rewind interval set <area> <minutes>` and the plugin will create a named backup every N minutes indefinitely, up to the `max-backups-per-area` limit (oldest are pruned automatically).

Use `/rewind interval check <area>` to see the current interval and next scheduled time, and `/rewind interval remove <area>` to stop automatic backups for that area.

### **Diff and Scan**

**Scan** (`/rewind scan <area>`) compares the current world state against the area's latest backup and reports all blocks that have changed since the backup was created. Useful for spotting unauthorized modifications or grief after the fact.

**Diff** (`/rewind diff <area> <id1> <id2>`) compares two stored backups against each other and shows which blocks differ between the two snapshots. Use this to understand the exact changes between two points in time before deciding which backup to restore.

### **WorldEdit Export**

If WorldEdit is installed, Area Rewind can export any area's latest backup state directly to a `.schem` file (WorldEdit schematic format). Run `/rewind export` while standing in or near an area to generate the file. The `.schem` file is placed in the WorldEdit schematics folder and can immediately be pasted with `//paste` or shared with other servers and editors.

## **Player Commands**

All player-facing commands use `/rewind` (aliases: `/ar`, `/arearewind`, `/protect`). Permission defaults are shown in the [Permissions](#permissions) section.

### **Basic Area Commands**

| Command | Permission | Description |
|---------|------------|-------------|
| `/rewind` | `arearewind.use` | Open the Area Rewind management GUI |
| `/rewind tool` | `arearewind.use` | Get the wooden hoe selection tool |
| `/rewind pos1` | `arearewind.use` | Set pos1 to the block you are looking at |
| `/rewind pos2` | `arearewind.use` | Set pos2 to the block you are looking at |
| `/rewind save <name>` | `arearewind.create` | Create a protected area from the current selection |
| `/rewind list [owned/trusted/all]` | `arearewind.use` | List areas you own, are trusted on, or all visible areas |
| `/rewind info <area>` | `arearewind.use` | Show detailed info: owner, size, backup count, trust list |
| `/rewind teleport <area>` | `arearewind.use` | Teleport to the center of an area |
| `/rewind trust <area> <player>` | `arearewind.trust` | Grant a player trust access to your area |
| `/rewind untrust <area> <player>` | `arearewind.trust` | Revoke a player's trust access to your area |

### **Backup & Restore Commands**

| Command | Permission | Description |
|---------|------------|-------------|
| `/rewind backup <area>` | `arearewind.backup` | Create a manual backup of the area right now |
| `/rewind history <area> [page]` | `arearewind.use` | View all backups for the area with timestamps, IDs, and block counts |
| `/rewind restore <area> <id/latest/oldest>` | `arearewind.restore` | Restore the area to a specific backup |
| `/rewind rollback <area> <time>` | `arearewind.restore` | Roll the area back to the nearest backup before the given time offset (e.g., `2h`, `30m`, `1d`) |
| `/rewind undo <area>` | `arearewind.restore` | Undo the most recent restore (reverts to the state before the last restore ran) |
| `/rewind redo <area>` | `arearewind.restore` | Redo a previously undone restore |
| `/rewind preview <area> [id] [particle]` | `arearewind.visualize` | Show a particle preview of what will change if the backup is restored |
| `/rewind export` | `arearewind.export` | Export the nearest area's latest backup to a WorldEdit `.schem` file |

### **Visualization & Trust Commands**

| Command | Permission | Description |
|---------|------------|-------------|
| `/rewind show <area>` | `arearewind.visualize` | Start displaying particle-based area boundary visualization |
| `/rewind hide [area]` | `arearewind.visualize` | Stop displaying visualization (for a specific area or all areas) |
| `/rewind help` | `arearewind.use` | Show the in-game command reference |

## **Administrative Commands**

These commands require `arearewind.admin` (operator by default) and are intended for server administrators.

| Command | Description |
|---------|-------------|
| `/rewind reload` | Reload `config.yml` and restart all plugin tasks |
| `/rewind delete <area>` | Permanently delete an area and all its backups |
| `/rewind rename <old_name> <new_name>` | Rename an area without losing its backups |
| `/rewind expand <area> <direction> <amount>` | Expand the area boundary in a direction (north/south/east/west/up/down) |
| `/rewind contract <area> <direction> <amount>` | Shrink the area boundary in a direction |
| `/rewind permission <add/remove/list> <area> [player]` | Manage granular area permissions for specific players |
| `/rewind seticon <area> <material>` | Set the material used as the icon in the GUI for this area |
| `/rewind seticon backup <area> <id> <material>` | Set a custom icon for a specific backup in the GUI |
| `/rewind cleanup <area> [days]` | Delete all backups older than the specified number of days (default: 7) |
| `/rewind scan <area>` | Scan the current world state against the area's latest backup and report changes |
| `/rewind diff <area> <id1> <id2>` | Compare two backups and show which blocks differ |
| `/rewind interval set <area> <minutes>` | Set an automatic backup interval for the area |
| `/rewind interval remove <area>` | Remove the automatic backup interval for the area |
| `/rewind interval check <area>` | Show the current interval setting and next scheduled backup |
| `/rewind restoreblock <area> <id/latest/oldest> [world]` | Restore from a command block or console (safe for automation) |
| `/rewind status` | Show plugin status: loaded areas, backup count, running intervals, queue status |

**Aliases:** `/ar`, `/arearewind`, `/protect` -- all subcommands work with all aliases  
**Command aliases:** `perm` = `permission`, `tp` = `teleport`, `compare` = `diff`

## **Permissions**

Area Rewind uses a hierarchical permission system. `arearewind.admin` is a parent node that grants all child permissions. Most player-facing permissions default to `true` (all authenticated players), while admin and sensitive operations default to `op`.

| Permission | Description | Default |
|------------|-------------|---------|
| `arearewind.*` | Grants all permissions including admin | `op` |
| `arearewind.admin` | Full administrative access to all features | `op` |
| `arearewind.use` | Open GUI, list areas, view info, teleport, use help | `true` |
| `arearewind.create` | Create new protected areas with `/rewind save` | `true` |
| `arearewind.delete` | Delete own areas | `true` |
| `arearewind.backup` | Create manual backups | `true` |
| `arearewind.restore` | Restore from backups, rollback, undo, redo | `true` |
| `arearewind.trust` | Manage trust list for own areas | `true` |
| `arearewind.visualize` | Show/hide boundary visualization and previews | `true` |
| `arearewind.gui` | Access the management GUI | `true` |
| `arearewind.export` | Export areas to WorldEdit schematics | `op` |
| `arearewind.import` | Import areas from external sources | `op` |

Example using LuckPerms to restrict area creation to a specific rank:

```
/lp group default permission unset arearewind.create
/lp group builder permission set arearewind.create true
/lp group admin permission set arearewind.admin true
```

## **Building from Source**

Area Rewind uses **Apache Maven** as its build system. WorldEdit is a soft dependency -- the plugin compiles and runs without it, but including it enables additional features.

**Requirements:**
- Java 17 or newer
- Apache Maven 3.6 or newer

**Steps:**

```bash
# Clone the repository
git clone https://github.com/Cobbleworks/Area-Rewind-Plugin.git
cd Area-Rewind

# Compile and package
mvn clean package
```

The output JAR is written to `target/AreaRewind-x.x.x.jar`. Copy it into your server's `plugins/` folder as described in the [Installation Steps](#installation-steps) section.

**Project Structure:**

```
src/main/
"oe"" java/arearewind/
"   "oe"" AreaRewindPlugin.java              + Plugin entry point, manager initialization
"   "oe"" commands/                          + One class per subcommand (40+ commands)
"   "   "oe"" CommandHandler.java            + Routes all /rewind subcommands
"   "   "oe"" admin/, analysis/, area/
"   "   "oe"" backup/, export/, info/
"   "   "oe"" maintenance/, management/
"   "   "oe"" navigation/, utility/
"   "   """" base/, registry/
"   "oe"" data/
"   "   "oe"" ProtectedArea.java             + Area model (bounds, owner, trust list)
"   "   "oe"" AreaBackup.java                + Backup model (block states, timestamp)
"   "   """" BlockInfo.java                 + Block state serialization
"   "oe"" listeners/
"   "   """" PlayerInteractionListener.java + Wooden hoe selection, tool events
"   "oe"" managers/
"   "   "oe"" AreaManager.java               + Area CRUD and file persistence
"   "   "oe"" BackupManager.java             + Backup creation, restoration queue
"   "   "oe"" FileManager.java               + File I/O for areas and backups
"   "   "oe"" GUIManager.java                + Inventory GUI event handling
"   "   "oe"" IntervalManager.java           + Automatic interval scheduling
"   "   "oe"" PermissionManager.java         + Permission checks
"   "   "oe"" VisualizationManager.java      + Particle task management
"   "   """" backup/                        + Block state, entity, restore handlers
"   """" util/
"       """" ConfigurationManager.java      + Config loading and access
"""" resources/
    "oe"" config.yml                         + Server configuration
    """" plugin.yml                         + Plugin metadata, commands, permissions
```

## **License**

This project is licensed under the **MIT License** " see the [LICENSE](LICENSE) file for details.

## **Screenshots**

The screenshots below demonstrate Area Rewind across several scenarios: backup comparison, restoration in progress, schematic export, and different build environments showing the plugin protecting diverse structures.

<table>
  <tr>
    <th>Area Rewind Plugin - Lighthouse Restore</th>
    <th>Area Rewind Plugin - Mansion Backup Diff</th>
  </tr>
  <tr>
    <td><a href="https://github.com/Cobbleworks/Area-Rewind-Plugin/raw/main/images/screenshot-lighthouse-restore.png" target="_blank" rel="noopener noreferrer"><img src="https://github.com/Cobbleworks/Area-Rewind-Plugin/raw/main/images/screenshot-lighthouse-restore.png" alt="Lighthouse Restore" width="450"></a></td>
    <td><a href="https://github.com/Cobbleworks/Area-Rewind-Plugin/raw/main/images/screenshot-mansion-diff.png" target="_blank" rel="noopener noreferrer"><img src="https://github.com/Cobbleworks/Area-Rewind-Plugin/raw/main/images/screenshot-mansion-diff.png" alt="Mansion Backup Diff" width="450"></a></td>
  </tr>
  <tr>
    <th>Area Rewind Plugin - Factory Schematic Export</th>
    <th>Area Rewind Plugin - Asian Temple</th>
  </tr>
  <tr>
    <td><a href="https://github.com/Cobbleworks/Area-Rewind-Plugin/raw/main/images/screenshot-factory-export.png" target="_blank" rel="noopener noreferrer"><img src="https://github.com/Cobbleworks/Area-Rewind-Plugin/raw/main/images/screenshot-factory-export.png" alt="Factory Schematic Export" width="450"></a></td>
    <td><a href="https://github.com/Cobbleworks/Area-Rewind-Plugin/raw/main/images/screenshot-asian-temple.png" target="_blank" rel="noopener noreferrer"><img src="https://github.com/Cobbleworks/Area-Rewind-Plugin/raw/main/images/screenshot-asian-temple.png" alt="Asian Temple" width="450"></a></td>
  </tr>
  <tr>
    <th>Area Rewind Plugin - Bridge Restore</th>
    <th>Area Rewind Plugin - Pirate Ship</th>
  </tr>
  <tr>
    <td><a href="https://github.com/Cobbleworks/Area-Rewind-Plugin/raw/main/images/screenshot-bridge-restore.png" target="_blank" rel="noopener noreferrer"><img src="https://github.com/Cobbleworks/Area-Rewind-Plugin/raw/main/images/screenshot-bridge-restore.png" alt="Bridge Restore" width="450"></a></td>
    <td><a href="https://github.com/Cobbleworks/Area-Rewind-Plugin/raw/main/images/screenshot-pirate-ship.png" target="_blank" rel="noopener noreferrer"><img src="https://github.com/Cobbleworks/Area-Rewind-Plugin/raw/main/images/screenshot-pirate-ship.png" alt="Pirate Ship" width="450"></a></td>
  </tr>
</table>
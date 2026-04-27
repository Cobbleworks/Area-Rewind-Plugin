<p align="center">
  <img src="images/plugin-logo.png" alt="Area Rewind" width="128" />
</p>
<h1 align="center">Area Rewind</h1>
<p align="center">
  <b>Advanced area protection and backup system for Minecraft servers.</b><br>
  <b>GUI management, automatic backups, undo/redo, and WorldEdit integration.</b>
</p>
<p align="center">
  <a href="https://github.com/Cobbleworks/Area-Rewind/releases"><img src="https://img.shields.io/github/v/release/Cobbleworks/Area-Rewind?include_prereleases&style=flat-square&color=4CAF50" alt="Latest Release"></a>&nbsp;&nbsp;<a href="https://github.com/Cobbleworks/Area-Rewind/blob/main/LICENSE"><img src="https://img.shields.io/badge/License-MIT-blue?style=flat-square" alt="License"></a>&nbsp;&nbsp;<img src="https://img.shields.io/badge/Java-17+-orange?style=flat-square" alt="Java Version">&nbsp;&nbsp;<img src="https://img.shields.io/badge/Minecraft-1.19+-green?style=flat-square" alt="Minecraft Version">&nbsp;&nbsp;<img src="https://img.shields.io/badge/Platform-Spigot%2FPaper-yellow?style=flat-square" alt="Platform">&nbsp;&nbsp;<img src="https://img.shields.io/badge/Status-Active-brightgreen?style=flat-square" alt="Status">
</p>

Area Rewind is an open-source Minecraft plugin that provides comprehensive area protection and backup management for Spigot and Paper servers. Players and administrators can create protected zones using either a wooden hoe selection tool or WorldEdit, schedule automatic backups at configurable intervals, and restore any area to a previous state through an intuitive GUI or full command system. The plugin supports undo/redo history, time-based rollback, WorldEdit schematic export, particle-based boundary visualisation, backup comparison with a diff tool, change detection scanning, and fine-grained permission management with owner and trusted player roles. Restorations are performed asynchronously in configurable batch sizes to minimise server impact.

### **Core Features**

- **Area Creation:** Define 3D rectangular protected areas using a wooden hoe selection tool (`/rewind pos1` and `/rewind pos2`) or WorldEdit's existing selection, then save the area with a name
- **Manual Backups:** Create on-demand backups at any time with `/rewind backup`; up to 50 backups are stored per area by default
- **Automatic Backup Intervals:** Set periodic automatic restores for an area at any configurable interval (in minutes), pointing at a specific backup snapshot to repeatedly restore from
- **Restore & Rollback:** Restore from a specific backup ID, the latest, the oldest, or roll back to the closest backup at a given time offset (`30m`, `2h`, `1d`, `1w`)
- **Undo / Redo:** Revert or re-apply the last backup restore operation for any area
- **Backup Preview:** Visualise what a backup looked like using a particle-overlay of all changed blocks — supports any Bukkit particle type
- **Diff Tool:** Compare any two backup snapshots (or a snapshot vs. the current world state) and list all block differences
- **Change Detection:** Scan an area to find all blocks that have changed since the last backup was taken
- **Boundary Visualisation:** Display or hide particle-based area boundary outlines; each player sees their own toggled state
- **GUI Interface:** Full inventory-based management GUI covering area listing, backup timelines, trust management, settings, and per-backup icons
- **Trust System:** Area owners can add or remove trusted players who are allowed to create backups and trigger restores on that area
- **Expand / Contract:** Resize existing protected areas without recreating them
- **Custom Restore Speed:** Set a per-area restore rate (1–10 000 blocks per tick) or let the system dynamically choose an optimal batch size
- **Export to WorldEdit:** Export any area backup to a WorldEdit-compatible `.schem` file
- **Command Block Support:** `/rewind cmdrestore` can be triggered from command blocks or the server console to automate scheduled restores
- **Block State Support:** Full restoration of containers with inventory contents, signs with text, banners with patterns, and all complex block states (tile entities)
- **Custom Area Icons:** Assign any material as a display icon for an area or for an individual backup snapshot in the GUI
- **Player Configuration:** Each player can independently toggle wooden-hoe selection mode and restoration progress logging

### **Supported Platforms**

- **Server Software:** `Spigot`, `Paper`, `Purpur`, `CraftBukkit`
- **Minecraft Versions:** `1.19` and higher
- **Java Requirements:** `Java 17+`
- **Optional Dependency:** `WorldEdit` (for WorldEdit-based selection; wooden hoe is used as automatic fallback)

### **Installation**

1. Download the latest `.jar` from the [Releases](https://github.com/Cobbleworks/Area-Rewind/releases) page
2. Stop your Minecraft server
3. Copy the `.jar` into your server's `plugins/` folder
4. *(Optional)* Install [WorldEdit](https://enginehub.org/worldedit/) to enable WorldEdit-based area selection
5. Start the server — the plugin generates `plugins/AreaRewind/` containing `config.yml` and area data files

### **Configuration**

All settings are in `plugins/AreaRewind/config.yml`:

| Key | Default | Description |
|-----|---------|-------------|
| `backup.max-backups-per-area` | `50` | Maximum number of backups retained per area; oldest are deleted when exceeded |
| `performance.max-area-size` | `1000000` | Maximum allowed number of blocks in a single area |
| `performance.rate-limit-cooldown` | `1000` | Milliseconds a player must wait between commands (rate limiting) |
| `performance.restore.max-batch-size` | `400` | Maximum blocks restored per tick for large areas |
| `performance.restore.min-batch-size` | `100` | Minimum blocks restored per tick for small areas |
| `visualization.particle-distance` | `50` | Maximum distance in blocks at which boundary particles are rendered |
| `selection.wooden-hoe.force-enabled` | `false` | Force wooden hoe selection mode on for all players |
| `selection.wooden-hoe.auto-fallback` | `true` | Automatically use wooden hoe if WorldEdit is unavailable |

### **Player Commands**

| Command | Description |
|---------|-------------|
| `/rewind` | Open the Area Rewind management GUI (same as `/rewind gui`) |
| `/rewind gui` | Open the management GUI |
| `/rewind tool` | Receive the wooden hoe area selection tool |
| `/rewind pos1` | Set corner 1 of your selection at your current target block |
| `/rewind pos2` | Set corner 2 of your selection at your current target block |
| `/rewind save <name>` | Save the current selection as a named protected area |
| `/rewind list [all\|owned\|trusted]` | List protected areas; filter by `all`, `owned`, or `trusted` |
| `/rewind info <area>` | Show detailed information about an area (owner, size, trust list, backup count) |
| `/rewind history <area> [page]` | View paginated backup history for an area |
| `/rewind teleport <area>` | Teleport to the centre of an area |
| `/rewind backup <area>` | Create a manual backup of an area |
| `/rewind restore <area> <backup_id\|latest\|oldest>` | Restore an area from a specific backup, the latest, or the oldest |
| `/rewind rollback <area> <time>` | Roll back to the closest backup at a given time offset — valid units: `m` (minutes), `h` (hours), `d` (days), `w` (weeks); example: `2h`, `30m`, `1d` |
| `/rewind undo <area>` | Undo the last backup restore for an area |
| `/rewind redo <area>` | Redo the last undone restore operation |
| `/rewind preview <area> [backup_id] [particle_type]` | Show a particle-based visual overlay of a backup (default backup: 0, default particle: `FLAME`) |
| `/rewind show <area>` | Display particle boundary lines around an area |
| `/rewind hide [area]` | Hide boundary particles; omit `<area>` to hide all visible areas |
| `/rewind trust <area> <player>` | Grant a player trusted access to an area |
| `/rewind untrust <area> <player>` | Remove a player's trusted access from an area |
| `/rewind export <area_name> [backup_id]` | Export a backup to a WorldEdit `.schem` file (defaults to latest) |
| `/rewind speed <area> <1-10000\|dynamic>` | Set a custom restore speed (blocks/tick) for an area, or `dynamic` for automatic sizing |
| `/rewind config <setting> [value]` | View or toggle your personal settings (see Config Settings below) |
| `/rewind status` | Show overall plugin status, area count, and backup statistics |
| `/rewind help` | Display all available commands |

**Personal config settings** for `/rewind config`:

| Setting | Values | Description |
|---------|--------|-------------|
| `hoeselection` | `true` / `false` | Enable or disable wooden hoe selection mode for yourself |
| `progresslogs` | `true` / `false` | Enable or disable per-block progress messages during restore |
| `list` | — | Show your current personal settings |

### **Administrative Commands**

| Command | Description |
|---------|-------------|
| `/rewind delete <area>` | Permanently delete an area and all its backup data |
| `/rewind rename <old_name> <new_name>` | Rename an existing area |
| `/rewind expand <area> <direction> <amount>` | Expand an area boundary in the given direction (`north`, `south`, `east`, `west`, `up`, `down`) |
| `/rewind contract <area> <direction> <amount>` | Shrink an area boundary in the given direction |
| `/rewind permission <add\|remove\|list> <area> <player>` | Add, remove, or list trusted players for an area (equivalent to trust/untrust) |
| `/rewind seticon <area> <material\|hand>` | Set the GUI display icon for an area; use `hand` to use the item in your hand |
| `/rewind seticon backup <area> <backup_id> <material\|hand>` | Set the GUI icon for a specific backup snapshot |
| `/rewind cleanup <area> [days]` | Delete backups older than `[days]` days (default: 7) |
| `/rewind scan <area>` | Scan the area and report all block changes since the last backup |
| `/rewind diff <area> <id1\|current> <id2\|current>` | Compare two backups or a backup against the current world state; use `current` for live comparison |
| `/rewind interval <set\|remove\|check> <area> [minutes] [backup_id]` | Manage automatic periodic restores: `set <area> <minutes> <backup_id>`, `remove <area>`, `check <area>` |
| `/rewind cmdrestore <area> <backup_id\|latest\|oldest> [world]` | Restore from a command block or console; optionally specify the world name |
| `/rewind reload` | Reload the plugin configuration |

### **Permissions**

| Permission | Description | Default |
|------------|-------------|---------|
| `arearewind.*` | Full access to all plugin features | `op` |
| `arearewind.admin` | Administrative access (includes all other permissions) | `op` |
| `arearewind.use` | Basic permission to use the plugin | `true` |
| `arearewind.create` | Create protected areas | `true` |
| `arearewind.delete` | Delete owned areas | `true` |
| `arearewind.backup` | Create backups | `true` |
| `arearewind.restore` | Restore from backups | `true` |
| `arearewind.trust` | Manage trusted players | `true` |
| `arearewind.visualize` | Visualise area boundaries | `true` |
| `arearewind.gui` | Access the GUI | `true` |
| `arearewind.export` | Export areas to `.schem` files | `true` |
| `arearewind.import` | Import area files | `true` |

**Access control notes:**
- Admins (`arearewind.admin`) can operate on any area regardless of ownership
- Area owners can create backups, restore, manage trust, rename, and resize their own areas
- Trusted players can create backups and trigger restores on the area they are trusted in
- All players with `arearewind.use` can view area info, list areas, teleport to areas they have access to, and use the GUI

### **WorldEdit Integration**

Area Rewind optionally integrates with WorldEdit (soft dependency). When WorldEdit is installed, players can use their existing WorldEdit selection (wand or `//pos1`/`//pos2`) as the area boundary when running `/rewind save`. If WorldEdit is unavailable, or if the `auto-fallback` config option is enabled, the wooden hoe tool is used automatically. The wooden hoe selection mode can be forced on per-player using `/rewind config hoeselection true`.

**Aliases:** `/ar`, `/arearewind`, `/protect` — **Selection tool:** Wooden hoe

### **License**

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.


## **Screenshots**

The screenshots below demonstrate the core features of the Area Rewind plugin, including the area management GUI, backup system, boundary visualisation, and restore functionality.

<table>
  <tr>
    <th>Area Rewind - House Rebuild</th>
    <th>Area Rewind - Area Visualization</th>
  </tr>
  <tr>
    <td><a href="https://github.com/Cobbleworks/Area-Rewind/raw/main/images/screenshot-house-rebuild.png" target="_blank" rel="noopener noreferrer"><img src="https://github.com/Cobbleworks/Area-Rewind/raw/main/images/screenshot-house-rebuild.png" alt="House Rebuild" width="450"></a></td>
    <td><a href="https://github.com/Cobbleworks/Area-Rewind/raw/main/images/screenshot-visualization.png" target="_blank" rel="noopener noreferrer"><img src="https://github.com/Cobbleworks/Area-Rewind/raw/main/images/screenshot-visualization.png" alt="Area Visualization" width="450"></a></td>
  </tr>
</table>

# Changelog

All notable changes to Area Rewind will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

## [1.0.10] - 2026-04-28

Area Rewind v1.0.10 delivers stability improvements and maintenance fixes for restore-heavy server environments.

### Stability And Maintenance

- **Runtime Safety**: Improved error handling around area operations and batch restore tasks
- **Command Robustness**: Hardened command validation for edge-case inputs
- **General Refinements**: Applied maintenance updates for long-running servers

**Note:** If you encounter any bugs or issues, please don't hesitate to open an [issue](https://github.com/Cobbleworks/Area-Rewind-Plugin/issues). For any questions or to start a discussion, feel free to initiate a [discussion](https://github.com/Cobbleworks/Area-Rewind-Plugin/discussions) on the GitHub repository.

## [1.0.0] - 2026-04-01

Area Rewind v1.0.0 is the initial release, delivering a full area protection and backup system with restoration, diff, particle preview, WorldEdit integration, and a GUI.

### Area Selection And Protection

- **Wooden Hoe Selection**: Select two corner positions with left-click (pos1) and right-click (pos2), or use `/rewind pos1`/`pos2` commands
- **WorldEdit Import**: Accepts WorldEdit selections directly if WorldEdit is installed
- **Named Protected Regions**: Owner-bound areas stored persistently in YAML with creation, deletion, rename, expand, and contract support

### Backup And Restore

- **Manual Backups**: Create on-demand backups at any time
- **Automatic Intervals**: Per-area configurable backup intervals that run indefinitely without admin attention
- **Flexible Restore Targeting**: Restore by backup ID, `latest`/`oldest` shorthand, or time expression (`2h`, `30m`, `1d`)
- **Full Block State Support**: Container inventories, sign text, banner patterns, and all complex block states are fully serialized and restored
- **Undo/Redo**: Instantly revert or re-apply the most recent restore or change

### Inspection And Preview

- **Diff Comparison**: Compare any two backups side by side to see exactly which blocks changed
- **Area Scan**: Scan an area for changes since the last backup
- **Particle Preview**: Preview what a restoration will change before committing — affected blocks highlighted with configurable particles
- **Boundary Visualisation**: Display particle-based area boundaries in real time

### Administration And Export

- **WorldEdit Export**: Export an area's latest backup directly to a `.schem` file
- **Trust System**: Grant per-area access to other players for backup and restore
- **9 Permission Nodes**: Fine-grained access control via any permission manager
- **Batched Restoration**: Lag-aware batched processing supports areas up to 1,000,000 blocks
- **Inventory GUI**: Manage areas and backups without memorizing commands

**Note:** If you encounter any bugs or issues, please don't hesitate to open an [issue](https://github.com/Cobbleworks/Area-Rewind-Plugin/issues). For any questions or to start a discussion, feel free to initiate a [discussion](https://github.com/Cobbleworks/Area-Rewind-Plugin/discussions) on the GitHub repository.

package arearewind.commands;

import arearewind.data.AreaBackup;
import arearewind.data.ProtectedArea;
import arearewind.managers.AreaManager;
import arearewind.managers.BackupManager;
import arearewind.managers.GUIManager;
import arearewind.managers.VisualizationManager;
import arearewind.managers.IntervalManager;
import arearewind.managers.PermissionManager;
import arearewind.managers.FileManager;
import arearewind.util.ConfigurationManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.OfflinePlayer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class CommandHandler implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final AreaManager areaManager;
    private final BackupManager backupManager;
    private final GUIManager guiManager;
    private final VisualizationManager visualizationManager;
    private final PermissionManager permissionManager;
    private final ConfigurationManager configManager;
    private final FileManager fileManager;
    private final IntervalManager intervalManager;
    private final Map<UUID, Long> lastUsage = new HashMap<>();

    public CommandHandler(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
                          GUIManager guiManager, VisualizationManager visualizationManager,
                          PermissionManager permissionManager, ConfigurationManager configManager) {
        this.plugin = plugin;
        this.areaManager = areaManager;
        this.backupManager = backupManager;
        this.guiManager = guiManager;
        this.visualizationManager = visualizationManager;
        this.permissionManager = permissionManager;
        this.configManager = configManager;
        this.fileManager = new FileManager(plugin, configManager);
        this.intervalManager = new IntervalManager(plugin, backupManager, areaManager);
        this.fileManager.setupFiles();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (isRateLimited(player)) {
            player.sendMessage(ChatColor.RED + "Please wait before using another command!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "pos1": return handlePos1(player, args);
            case "pos2": return handlePos2(player, args);
            case "contract": return handleContract(player, args);
            case "save": return handleSave(player, args);
            case "delete": return handleDelete(player, args);
            case "particle": case "particles": return handleParticle(player, args);
            case "rename": return handleRename(player, args);
            case "list": return handleList(player, args);
            case "info": return handleInfo(player, args);
            case "teleport": case "tp": return handleTeleport(player, args);
            case "backup": return handleBackup(player, args);
            case "restore": return handleRestore(player, args);
            case "rollback": return handleRollback(player, args);
            case "undo": return handleUndo(player, args);
            case "redo": return handleRedo(player, args);
            case "history": return handleHistory(player, args);
            case "cleanup": return handleCleanup(player, args);
            case "scan": return handleScan(player, args);
            case "diff": return handleDiff(player, args);
            case "compare": return handleCompare(player, args);
            case "preview": return handlePreview(player, args);
            case "trust": return handleTrust(player, args);
            case "untrust": return handleUntrust(player, args);
            case "permissions": return handlePermissions(player, args);
            case "show": return handleShow(player, args);
            case "hide": return handleHide(player, args);
            case "gui": case "menu": return handleGUI(player, args);
            case "interval": return handleInterval(player, args);
            case "status": return handleStatus(player, args);
            case "reload": return handleReload(player, args);
            case "help": return sendHelp(player);
            default:
                player.sendMessage(ChatColor.RED + "Unknown command! Use /rewind help");
        }

        updateLastUsage(player);
        return true;
    }

    private boolean handlePos1(Player player, String[] args) {
        Block targetBlock = player.getTargetBlockExact(100);
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "No valid position found!");
            return true;
        }

        areaManager.setPosition1(player.getUniqueId(), targetBlock.getLocation());
        player.sendMessage(ChatColor.GREEN + "Position 1 set: " + ChatColor.WHITE +
                areaManager.locationToString(targetBlock.getLocation()));
        return true;
    }

    private boolean handlePos2(Player player, String[] args) {
        Block targetBlock = player.getTargetBlockExact(100);
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "No valid position found!");
            return true;
        }

        areaManager.setPosition2(player.getUniqueId(), targetBlock.getLocation());
        player.sendMessage(ChatColor.GREEN + "Position 2 set: " + ChatColor.WHITE +
                areaManager.locationToString(targetBlock.getLocation()));
        return true;
    }

    private boolean handleContract(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(ChatColor.RED + "Usage: /rewind contract <area> <direction> <amount>");
            return true;
        }

        String areaName = args[1];
        String direction = args[2];
        int amount;

        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid amount!");
            return true;
        }

        ProtectedArea area = areaManager.getArea(areaName);
        if (area == null) {
            player.sendMessage(ChatColor.RED + "Area '" + areaName + "' not found!");
            return true;
        }

        if (!permissionManager.hasAreaPermission(player, area)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to modify this area!");
            return true;
        }

        if (areaManager.contractArea(areaName, direction, amount)) {
            player.sendMessage(ChatColor.GREEN + "Area '" + areaName + "' contracted " +
                    direction + " by " + amount + " blocks!");
            areaManager.saveAreas();
        } else {
            player.sendMessage(ChatColor.RED + "Invalid direction! Use: north, south, east, west, up, down");
        }

        return true;
    }


    private boolean handleSave(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /rewind save <name>");
            return true;
        }

        String name = args[1];
        UUID playerId = player.getUniqueId();

        if (!areaManager.hasValidSelection(playerId)) {
            player.sendMessage(ChatColor.RED + "Set both positions first!");
            return true;
        }

        if (areaManager.areaExists(name)) {
            player.sendMessage(ChatColor.RED + "Area '" + name + "' already exists!");
            return true;
        }

        player.sendMessage(ChatColor.YELLOW + "Creating area '" + name + "'...");

        if (areaManager.createArea(name, playerId)) {
            ProtectedArea area = areaManager.getArea(name);

            if (area.getSize() > configManager.getMaxAreaSize()) {
                areaManager.deleteArea(name);
                player.sendMessage(ChatColor.RED + "Area too large! Maximum size: " +
                        configManager.getMaxAreaSize() + " blocks");
                return true;
            }

            player.sendMessage(ChatColor.YELLOW + "Creating initial backup for '" + name + "'... Please wait.");

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    backupManager.createBackup(name, area);

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        areaManager.saveAreas();

                        player.sendMessage(ChatColor.GREEN + "✓ Area '" + name + "' saved successfully!");
                        player.sendMessage(ChatColor.YELLOW + "Size: " + area.getSize() + " blocks");
                        player.sendMessage(ChatColor.GRAY + "Initial backup created automatically.");
                    });

                } catch (Exception e) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.sendMessage(ChatColor.RED + "✗ Failed to create backup for area '" + name + "'!");
                        player.sendMessage(ChatColor.GRAY + "Area was created but backup failed. Check console for details.");
                        plugin.getLogger().warning("Failed to create backup for area '" + name + "': " + e.getMessage());
                    });
                }
            });

        } else {
            player.sendMessage(ChatColor.RED + "Failed to create area!");
        }

        return true;
    }

    private boolean handleDelete(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /rewind delete <area>");
            return true;
        }

        String areaName = args[1];
        ProtectedArea area = areaManager.getArea(areaName);

        if (area == null) {
            player.sendMessage(ChatColor.RED + "Area '" + areaName + "' not found!");
            return true;
        }

        if (!permissionManager.hasAreaPermission(player, area)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to delete this area!");
            return true;
        }

        backupManager.deleteAllBackups(areaName);
        areaManager.deleteArea(areaName);
        areaManager.saveAreas();

        player.sendMessage(ChatColor.GREEN + "Area '" + areaName + "' and all its backups deleted!");
        return true;
    }

    private boolean handleList(Player player, String[] args) {
        String filter = args.length > 1 ? args[1].toLowerCase() : "all";
        List<ProtectedArea> areas = new ArrayList<>();

        switch (filter) {
            case "owned":
                areas = areaManager.getOwnedAreas(player.getUniqueId());
                break;
            case "trusted":
                areas = areaManager.getTrustedAreas(player.getUniqueId());
                break;
            default:
                areas = new ArrayList<>(areaManager.getProtectedAreas().values());
                break;
        }

        if (areas.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No protected areas found!");
            return true;
        }

        player.sendMessage(ChatColor.GOLD + "=== Protected Areas (" + filter + ") ===");
        for (ProtectedArea area : areas) {
            int backupCount = backupManager.getBackupHistory(area.getName()).size();
            String ownerName = Bukkit.getOfflinePlayer(area.getOwner()).getName();

            player.sendMessage(ChatColor.GREEN + area.getName() + ChatColor.WHITE +
                    " - " + area.getSize() + " blocks, " + backupCount + " backups" +
                    ChatColor.GRAY + " (Owner: " + ownerName + ")");
        }

        return true;
    }

    private boolean handleBackup(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /rewind backup <area>");
            return true;
        }

        String areaName = args[1];
        ProtectedArea area = areaManager.getArea(areaName);

        if (area == null) {
            player.sendMessage(ChatColor.RED + "Area '" + areaName + "' not found!");
            return true;
        }

        if (!permissionManager.hasAreaPermission(player, area)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to backup this area!");
            return true;
        }

        player.sendMessage(ChatColor.YELLOW + "Creating backup for '" + areaName + "'...");

        Bukkit.getScheduler().runTask(plugin, () -> {
            backupManager.createBackup(areaName, area);

            List<AreaBackup> backups = backupManager.getBackupHistory(areaName);
            AreaBackup lastBackup = backups.get(backups.size() - 1);

            player.sendMessage(ChatColor.GREEN + "Backup created for '" + areaName + "'!");
            player.sendMessage(ChatColor.YELLOW + "Backup ID: " + (backups.size() - 1) +
                    " (" + lastBackup.getBlocks().size() + " blocks)");
        });

        return true;
    }

    private boolean handleRestore(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /rewind restore <area> <backup_id|latest|oldest>");
            return true;
        }

        String areaName = args[1];
        String backupId = args[2];

        ProtectedArea area = areaManager.getArea(areaName);
        if (area == null) {
            player.sendMessage(ChatColor.RED + "Area '" + areaName + "' not found!");
            return true;
        }

        if (!permissionManager.hasAreaPermission(player, area)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to restore this area!");
            return true;
        }

        List<AreaBackup> backups = backupManager.getBackupHistory(areaName);
        if (backups.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No backups found for '" + areaName + "'!");
            return true;
        }

        int id;

        if (backupId.equalsIgnoreCase("latest")) {
            id = backups.size() - 1;
        } else if (backupId.equalsIgnoreCase("oldest")) {
            id = 0;
        } else {
            try {
                id = Integer.parseInt(backupId);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid backup ID! Use number, 'latest', or 'oldest'");
                return true;
            }
        }

        if (id < 0 || id >= backups.size()) {
            player.sendMessage(ChatColor.RED + "Backup ID not found! Available: 0-" + (backups.size() - 1));
            return true;
        }

        player.sendMessage(ChatColor.YELLOW + "Restoring backup " + id + " for '" + areaName + "'...");

        AreaBackup backup = backups.get(id);
        Bukkit.getScheduler().runTask(plugin, () -> {
            backupManager.restoreFromBackup(area, backup, player);
        });

        return true;
    }

    private boolean handleRollback(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /rewind rollback <area> <time>");
            return true;
        }
        String areaName = args[1];
        String timeStr = args[2];
        ProtectedArea area = areaManager.getArea(areaName);
        if (area == null) {
            player.sendMessage(ChatColor.RED + "Area '" + areaName + "' not found!");
            return true;
        }
        if (!permissionManager.canUndoRedo(player, area)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to rollback this area!");
            return true;
        }
        long minutes = parseTimeString(timeStr);
        if (minutes < 0) {
            player.sendMessage(ChatColor.RED + "Invalid time format! Use e.g. 1h, 30m, 2d");
            return true;
        }
        LocalDateTime targetTime = LocalDateTime.now().minusMinutes(minutes);
        AreaBackup backup = backupManager.findClosestBackup(areaName, targetTime);
        if (backup == null) {
            player.sendMessage(ChatColor.RED + "No suitable backup found!");
            return true;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            backupManager.restoreFromBackup(area, backup);
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage(ChatColor.GREEN + "Rollback successful for '" + areaName + "'.");
            });
        });
        return true;
    }

    private boolean handleScan(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /rewind scan <area>");
            return true;
        }
        String areaName = args[1];
        ProtectedArea area = areaManager.getArea(areaName);
        if (area == null) {
            player.sendMessage(ChatColor.RED + "Area '" + areaName + "' not found!");
            return true;
        }
        List<String> diffs = backupManager.getDifferencesFromLast(areaName, area);
        if (diffs.isEmpty()) {
            player.sendMessage(ChatColor.GREEN + "No differences from the last backup found.");
        } else {
            player.sendMessage(ChatColor.GOLD + "=== Changes since last backup ===");
            for (String diff : diffs) {
                player.sendMessage(diff);
            }
        }
        return true;
    }

    private boolean handleDiff(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(ChatColor.RED + "Usage: /rewind diff <area> <id1> <id2>");
            return true;
        }
        String areaName = args[1];
        int id1, id2;
        try {
            id1 = Integer.parseInt(args[2]);
            id2 = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid backup IDs!");
            return true;
        }
        List<AreaBackup> backups = backupManager.getBackupHistory(areaName);
        if (id1 < 0 || id1 >= backups.size() || id2 < 0 || id2 >= backups.size()) {
            player.sendMessage(ChatColor.RED + "Backup IDs not found!");
            return true;
        }
        List<String> diffs = backupManager.compareBackups(backups.get(id1), backups.get(id2));
        if (diffs.isEmpty()) {
            player.sendMessage(ChatColor.GREEN + "No differences between the backups.");
        } else {
            player.sendMessage(ChatColor.GOLD + "=== Differences between backup " + id1 + " and " + id2 + " ===");
            for (String diff : diffs) {
                player.sendMessage(diff);
            }
        }
        return true;
    }

    private boolean handleCompare(Player player, String[] args) {
        return handleDiff(player, args);
    }

    private boolean handleTrust(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /rewind trust <area> <player>");
            return true;
        }
        String areaName = args[1];
        String targetName = args[2];
        ProtectedArea area = areaManager.getArea(areaName);
        if (area == null) {
            player.sendMessage(ChatColor.RED + "Area '" + areaName + "' not found!");
            return true;
        }
        if (!permissionManager.canModifyTrust(player, area)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to trust players for this area!");
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (target == null || target.getUniqueId() == null) {
            player.sendMessage(ChatColor.RED + "Player not found: " + targetName);
            return true;
        }
        if (area.getTrustedPlayers().contains(target.getUniqueId())) {
            player.sendMessage(ChatColor.YELLOW + targetName + " is already trusted for this area.");
            return true;
        }
        area.addTrustedPlayer(target.getUniqueId());
        areaManager.saveAreas();
        player.sendMessage(ChatColor.GREEN + targetName + " was added as trusted for '" + areaName + "'.");
        return true;
    }

    private boolean handleUntrust(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /rewind untrust <area> <player>");
            return true;
        }
        String areaName = args[1];
        String targetName = args[2];
        ProtectedArea area = areaManager.getArea(areaName);
        if (area == null) {
            player.sendMessage(ChatColor.RED + "Area '" + areaName + "' not found!");
            return true;
        }
        if (!permissionManager.canModifyTrust(player, area)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to untrust players for this area!");
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (target == null || target.getUniqueId() == null) {
            player.sendMessage(ChatColor.RED + "Player not found: " + targetName);
            return true;
        }
        if (!area.getTrustedPlayers().contains(target.getUniqueId())) {
            player.sendMessage(ChatColor.YELLOW + targetName + " is not trusted for this area.");
            return true;
        }
        area.removeTrustedPlayer(target.getUniqueId());
        areaManager.saveAreas();
        player.sendMessage(ChatColor.GREEN + targetName + " was removed as trusted for '" + areaName + "'.");
        return true;
    }

    private boolean handlePermissions(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /rewind permissions <area>");
            return true;
        }
        String areaName = args[1];
        ProtectedArea area = areaManager.getArea(areaName);
        if (area == null) {
            player.sendMessage(ChatColor.RED + "Area '" + areaName + "' not found!");
            return true;
        }
        String ownerName = Bukkit.getOfflinePlayer(area.getOwner()).getName();
        player.sendMessage(ChatColor.GOLD + "=== Permissions for '" + areaName + "' ===");
        player.sendMessage(ChatColor.GRAY + "Owner: " + ChatColor.WHITE + ownerName);
        player.sendMessage(ChatColor.GRAY + "Trusted: " + ChatColor.WHITE + area.getTrustedPlayers().size());
        for (UUID uuid : area.getTrustedPlayers()) {
            String name = Bukkit.getOfflinePlayer(uuid).getName();
            player.sendMessage(ChatColor.GRAY + "- " + ChatColor.WHITE + name);
        }
        String level = permissionManager.getPermissionLevelString(player, area);
        player.sendMessage(ChatColor.GRAY + "Your permission level: " + ChatColor.WHITE + level);
        return true;
    }

    private boolean handleShow(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /rewind show <area>");
            return true;
        }
        String areaName = args[1];
        ProtectedArea area = areaManager.getArea(areaName);
        if (area == null) {
            player.sendMessage(ChatColor.RED + "Area '" + areaName + "' not found!");
            return true;
        }
        if (!permissionManager.canVisualize(player, area)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to visualize this area!");
            return true;
        }
        visualizationManager.showAreaBoundaries(player, area);
        player.sendMessage(ChatColor.GREEN + "Boundaries of '" + areaName + "' are being shown.");
        return true;
    }

    private boolean handleHide(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /rewind hide <area>");
            return true;
        }
        String areaName = args[1];
        ProtectedArea area = areaManager.getArea(areaName);
        if (area == null) {
            player.sendMessage(ChatColor.RED + "Area '" + areaName + "' not found!");
            return true;
        }
        player.sendMessage(ChatColor.YELLOW + "Hiding boundaries is currently not available.");
        return true;
    }

    private boolean handleGUI(Player player, String[] args) {
        if (!permissionManager.canUseGUI(player)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use the GUI!");
            return true;
        }
        guiManager.openAreasGUI(player);
        return true;
    }

    private boolean handleInterval(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /rewind interval <area> [minutes] [backup_id]");
            player.sendMessage(ChatColor.GRAY + "Use 0 minutes to disable interval");
            return true;
        }

        String areaName = args[1];
        ProtectedArea area = areaManager.getArea(areaName);

        if (area == null) {
            player.sendMessage(ChatColor.RED + "Area '" + areaName + "' not found!");
            return true;
        }

        if (!permissionManager.hasAreaPermission(player, area)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to set interval for this area!");
            return true;
        }

        if (args.length == 2) {
            IntervalManager.IntervalConfig config = intervalManager.getIntervalConfig(areaName);
            if (config == null) {
                player.sendMessage(ChatColor.YELLOW + "No auto-restore interval set for '" + areaName + "'");
            } else {
                player.sendMessage(ChatColor.GREEN + "Auto-restore interval for '" + areaName + "':");
                player.sendMessage(ChatColor.WHITE + "Every " + config.minutes + " minutes, restore to backup " + config.backupId);
            }
            return true;
        }

        if (args.length < 4) {
            player.sendMessage(ChatColor.RED + "Usage: /rewind interval <area> <minutes> <backup_id>");
            return true;
        }

        int minutes;
        int backupId;

        try {
            minutes = Integer.parseInt(args[2]);
            backupId = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid numbers!");
            return true;
        }

        if (minutes < 0) {
            player.sendMessage(ChatColor.RED + "Minutes cannot be negative!");
            return true;
        }

        if (minutes == 0) {
            intervalManager.clearInterval(areaName);
            player.sendMessage(ChatColor.GREEN + "Auto-restore interval cleared for '" + areaName + "'!");
            return true;
        }

        if (minutes < 1) {
            player.sendMessage(ChatColor.RED + "Minimum interval is 1 minute!");
            return true;
        }

        List<AreaBackup> backups = backupManager.getBackupHistory(areaName);
        if (backups.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No backups found for '" + areaName + "'!");
            return true;
        }

        if (backupId < 0 || backupId >= backups.size()) {
            player.sendMessage(ChatColor.RED + "Invalid backup ID! Available: 0-" + (backups.size() - 1));
            return true;
        }

        boolean success = intervalManager.setInterval(areaName, minutes, backupId, player.getUniqueId());
        if (success) {
            AreaBackup backup = backups.get(backupId);
            player.sendMessage(ChatColor.GREEN + "Auto-restore interval set for '" + areaName + "':");
            player.sendMessage(ChatColor.WHITE + "Every " + minutes + " minutes, restore to backup " + backupId);
            player.sendMessage(ChatColor.GRAY + "Backup from: " + backup.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } else {
            player.sendMessage(ChatColor.RED + "Failed to set interval!");
        }

        return true;
    }

    private boolean handleStatus(Player player, String[] args) {
        player.sendMessage(ChatColor.GOLD + "=== Area Rewind Status ===");
        player.sendMessage(ChatColor.GRAY + backupManager.getBackupStatistics());
        player.sendMessage(ChatColor.GRAY + fileManager.getDiskUsageStats());

        Map<String, IntervalManager.IntervalConfig> intervals = intervalManager.getAllIntervals();
        if (!intervals.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Active Auto-Restore Intervals:");
            for (IntervalManager.IntervalConfig config : intervals.values()) {
                player.sendMessage(ChatColor.WHITE + "- " + config.areaName + ": every " +
                        config.minutes + "min -> backup " + config.backupId);
            }
        } else {
            player.sendMessage(ChatColor.GRAY + "No active auto-restore intervals");
        }

        return true;
    }

    private boolean handleReload(Player player, String[] args) {
        if (!player.hasPermission("arearewind.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to reload the plugin!");
            return true;
        }
        player.sendMessage(ChatColor.YELLOW + "Area Rewind Plugin is reloading...");
        plugin.reloadConfig();
        configManager.reloadConfiguration();
        areaManager.loadAreas();
        backupManager.loadBackups();
        player.sendMessage(ChatColor.GREEN + "Plugin successfully reloaded!");
        return true;
    }

    private boolean isRateLimited(Player player) {
        long now = System.currentTimeMillis();
        long last = lastUsage.getOrDefault(player.getUniqueId(), 0L);
        if (now - last < 2000) return true;
        return false;
    }

    private void updateLastUsage(Player player) {
        lastUsage.put(player.getUniqueId(), System.currentTimeMillis());
    }

    private boolean sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Area Rewind Help ===");
        player.sendMessage(ChatColor.YELLOW + "/rewind pos1|pos2|contract");
        player.sendMessage(ChatColor.YELLOW + "/rewind save|delete|rename|list|info|tp");
        player.sendMessage(ChatColor.YELLOW + "/rewind backup|restore|rollback|undo|redo|history|cleanup");
        player.sendMessage(ChatColor.YELLOW + "/rewind scan|diff|compare|preview|particle");
        player.sendMessage(ChatColor.YELLOW + "/rewind trust|untrust|permissions");
        player.sendMessage(ChatColor.YELLOW + "/rewind show|hide|gui|interval|status|reload");
        player.sendMessage(ChatColor.GRAY + "Preview: /rewind preview <area> <id> (toggle on/off)");
        player.sendMessage(ChatColor.GRAY + "Particle: /rewind particle <name> (default: flame)");
        return true;
    }

    private boolean handleRename(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /rewind rename <old_name> <new_name>");
            return true;
        }
        String oldName = args[1];
        String newName = args[2];
        ProtectedArea area = areaManager.getArea(oldName);
        if (area == null) {
            player.sendMessage(ChatColor.RED + "Area '" + oldName + "' not found!");
            return true;
        }
        if (areaManager.areaExists(newName)) {
            player.sendMessage(ChatColor.RED + "Area '" + newName + "' already exists!");
            return true;
        }
        if (!permissionManager.hasAreaPermission(player, area)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to rename this area!");
            return true;
        }
        player.sendMessage(ChatColor.YELLOW + "Renaming backup files is currently not available.");
        area.setName(newName);
        areaManager.addArea(area);
        areaManager.deleteArea(oldName);
        areaManager.saveAreas();
        player.sendMessage(ChatColor.GREEN + "Area renamed from '" + oldName + "' to '" + newName + "'!");
        return true;
    }

    private boolean handleInfo(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /rewind info <area>");
            return true;
        }
        String areaName = args[1];
        ProtectedArea area = areaManager.getArea(areaName);
        if (area == null) {
            player.sendMessage(ChatColor.RED + "Area '" + areaName + "' not found!");
            return true;
        }
        List<AreaBackup> backups = backupManager.getBackupHistory(areaName);
        player.sendMessage(ChatColor.GOLD + "=== Area Info: " + areaName + " ===");
        player.sendMessage(ChatColor.GREEN + "Owner: " + ChatColor.WHITE + Bukkit.getOfflinePlayer(area.getOwner()).getName());
        player.sendMessage(ChatColor.GREEN + "World: " + ChatColor.WHITE + area.getPos1().getWorld().getName());
        player.sendMessage(ChatColor.GREEN + "Position 1: " + ChatColor.WHITE + areaManager.locationToString(area.getPos1()));
        player.sendMessage(ChatColor.GREEN + "Position 2: " + ChatColor.WHITE + areaManager.locationToString(area.getPos2()));
        player.sendMessage(ChatColor.GREEN + "Size: " + ChatColor.WHITE + area.getSize() + " blocks");
        player.sendMessage(ChatColor.GREEN + "Backups: " + ChatColor.WHITE + backups.size());
        if (!backups.isEmpty()) {
            AreaBackup lastBackup = backups.get(backups.size() - 1);
            player.sendMessage(ChatColor.GREEN + "Last Backup: " + ChatColor.WHITE + lastBackup.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (!area.getTrustedPlayers().isEmpty()) {
            List<String> trustedNames = new ArrayList<>();
            for (UUID uuid : area.getTrustedPlayers()) {
                trustedNames.add(Bukkit.getOfflinePlayer(uuid).getName());
            }
            player.sendMessage(ChatColor.GREEN + "Trusted Players: " + ChatColor.WHITE + String.join(", ", trustedNames));
        }
        return true;
    }

    private boolean handleTeleport(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /rewind teleport <area>");
            return true;
        }
        String areaName = args[1];
        ProtectedArea area = areaManager.getArea(areaName);
        if (area == null) {
            player.sendMessage(ChatColor.RED + "Area '" + areaName + "' not found!");
            return true;
        }
        if (!permissionManager.hasAreaPermission(player, area)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to teleport to this area!");
            return true;
        }
        org.bukkit.Location center = area.getCenter();
        center.setY(center.getY() + 1);
        player.teleport(center);
        player.sendMessage(ChatColor.GREEN + "Teleported to area '" + areaName + "'!");
        return true;
    }

    private boolean handleUndo(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /rewind undo <area>");
            return true;
        }
        String areaName = args[1];
        ProtectedArea area = areaManager.getArea(areaName);
        if (area == null) {
            player.sendMessage(ChatColor.RED + "Area '" + areaName + "' not found!");
            return true;
        }
        if (!permissionManager.canUndoRedo(player, area)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to undo changes to this area!");
            return true;
        }
        if (!backupManager.canUndo(areaName)) {
            player.sendMessage(ChatColor.RED + "No undo history available for '" + areaName + "'!");
            return true;
        }
        boolean success = backupManager.undoArea(areaName, area);
        if (!success) {
            player.sendMessage(ChatColor.RED + "Cannot undo further!");
            return true;
        }
        AreaBackup backup = backupManager.getBackupHistory(areaName).get(backupManager.getUndoPointer(areaName));
        player.sendMessage(ChatColor.YELLOW + "Undoing changes to '" + areaName + "'...");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            backupManager.restoreFromBackup(area, backup);
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage(ChatColor.GREEN + "Undo successful! Restored backup " + backupManager.getUndoPointer(areaName));
                player.sendMessage(ChatColor.YELLOW + "From: " + backup.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            });
        });
        return true;
    }

    private boolean handleRedo(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /rewind redo <area>");
            return true;
        }
        String areaName = args[1];
        ProtectedArea area = areaManager.getArea(areaName);
        if (area == null) {
            player.sendMessage(ChatColor.RED + "Area '" + areaName + "' not found!");
            return true;
        }
        if (!permissionManager.canUndoRedo(player, area)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to redo changes to this area!");
            return true;
        }
        if (!backupManager.canRedo(areaName)) {
            player.sendMessage(ChatColor.RED + "No redo history available for '" + areaName + "'!");
            return true;
        }
        boolean success = backupManager.redoArea(areaName, area);
        if (!success) {
            player.sendMessage(ChatColor.RED + "Cannot redo further!");
            return true;
        }
        AreaBackup backup = backupManager.getBackupHistory(areaName).get(backupManager.getUndoPointer(areaName));
        player.sendMessage(ChatColor.YELLOW + "Redoing changes to '" + areaName + "'...");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            backupManager.restoreFromBackup(area, backup);
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage(ChatColor.GREEN + "Redo successful! Restored backup " + backupManager.getUndoPointer(areaName));
                player.sendMessage(ChatColor.YELLOW + "From: " + backup.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            });
        });
        return true;
    }

    private boolean handleHistory(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /rewind history <area> [page]");
            return true;
        }
        String areaName = args[1];
        int page = 1;
        if (args.length > 2) {
            try {
                page = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid page number!");
                return true;
            }
        }
        ProtectedArea area = areaManager.getArea(areaName);
        if (area == null) {
            player.sendMessage(ChatColor.RED + "Area '" + areaName + "' not found!");
            return true;
        }
        List<AreaBackup> backups = backupManager.getBackupHistory(areaName);
        int itemsPerPage = 10;
        int maxPage = (int) Math.ceil((double) backups.size() / itemsPerPage);
        if (page < 1 || page > maxPage) {
            player.sendMessage(ChatColor.RED + "Invalid page! Valid pages: 1-" + maxPage);
            return true;
        }
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, backups.size());
        player.sendMessage(ChatColor.GOLD + "=== Backup History: " + areaName + " (Page " + page + "/" + maxPage + ") ===");
        for (int i = startIndex; i < endIndex; i++) {
            AreaBackup backup = backups.get(i);
            String timestamp = backup.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            int blockCount = backup.getBlocks().size();
            ChatColor color = (i == backupManager.getUndoPointer(areaName)) ? ChatColor.YELLOW : ChatColor.GREEN;
            player.sendMessage(color + "[" + i + "] " + ChatColor.WHITE + timestamp + ChatColor.GRAY + " (" + blockCount + " blocks)");
        }
        if (maxPage > 1) {
            player.sendMessage(ChatColor.YELLOW + "Use /rewind history " + areaName + " <page> to view other pages");
        }
        return true;
    }

    private boolean handleCleanup(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /rewind cleanup <area> [days]");
            return true;
        }
        String areaName = args[1];
        int days = 7;
        if (args.length > 2) {
            try {
                days = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid number of days!");
                return true;
            }
        }
        ProtectedArea area = areaManager.getArea(areaName);
        if (area == null) {
            player.sendMessage(ChatColor.RED + "Area '" + areaName + "' not found!");
            return true;
        }
        if (!permissionManager.hasAreaPermission(player, area)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to cleanup this area!");
            return true;
        }
        int removed = backupManager.cleanupBackups(areaName, days);
        if (removed > 0) {
            player.sendMessage(ChatColor.GREEN + "Cleaned up " + removed + " old backups for '" + areaName + "'!");
        } else {
            player.sendMessage(ChatColor.YELLOW + "No old backups found to cleanup for '" + areaName + "'!");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!(sender instanceof Player)) {
            return completions;
        }

        Player player = (Player) sender;

        if (args.length == 1) {
            List<String> commands = Arrays.asList(
                    "pos1", "pos2", "contract", "save", "delete", "rename",
                    "list", "info", "teleport", "tp", "backup", "restore",
                    "rollback", "undo", "redo", "history", "cleanup", "scan",
                    "diff", "compare", "preview", "particle", "particles",
                    "trust", "untrust", "permissions", "show", "hide",
                    "gui", "menu", "interval", "status", "reload", "help"
            );
            return commands.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        String subCommand = args[0].toLowerCase();

        List<String> areaCommands = Arrays.asList(
                "contract", "delete", "rename", "info", "teleport", "tp",
                "backup", "restore", "rollback", "undo", "redo", "history",
                "cleanup", "scan", "diff", "compare", "preview",
                "trust", "untrust", "permissions", "show", "hide", "interval"
        );

        List<String> particleCommands = Arrays.asList("particle", "particles");

        if (args.length == 2) {
            if (areaCommands.contains(subCommand)) {
                completions.addAll(areaManager.getProtectedAreas().keySet());
            } else if (particleCommands.contains(subCommand)) {
                completions.addAll(getParticleCompletions());
            } else if (subCommand.equals("list")) {
                completions.addAll(Arrays.asList("all", "owned", "trusted"));
            } else if (subCommand.equals("help")) {
                completions.addAll(Arrays.asList("commands", "permissions", "examples"));
            } else if (subCommand.equals("status")) {
                completions.addAll(Arrays.asList("full", "brief"));
            } else if (subCommand.equals("reload")) {
                completions.addAll(Arrays.asList("config", "areas", "all"));
            }

            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3) {
            switch (subCommand) {
                case "contract":
                    completions = Arrays.asList("north", "south", "east", "west", "up", "down");
                    break;

                case "restore":
                case "preview":
                    completions = getBackupIdCompletions(args[1]);
                    break;

                case "rollback":
                    completions = Arrays.asList("5m", "10m", "30m", "1h", "2h", "6h", "12h", "1d", "3d", "1w");
                    break;

                case "history":
                    completions = getPageNumberCompletions(args[1]);
                    break;

                case "cleanup":
                    completions = Arrays.asList("1", "3", "7", "14", "30", "60", "90");
                    break;

                case "diff":
                case "compare":
                    completions = getBackupIdCompletions(args[1]);
                    break;

                case "trust":
                case "untrust":
                    completions = getOnlinePlayerCompletions();
                    break;

                case "interval":
                    completions = Arrays.asList("0", "1", "5", "10", "15", "30", "60", "120", "180", "360");
                    break;
            }

            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 4) {
            switch (subCommand) {
                case "contract":
                    completions = Arrays.asList("1", "5", "10", "25", "50", "100");
                    break;

                case "diff":
                case "compare":
                    completions = getBackupIdCompletions(args[1]);
                    break;

                case "interval":
                    completions = getBackupIdCompletions(args[1]);
                    break;
            }

            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[3].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return completions;
    }

    private List<String> getBackupIdCompletions(String areaName) {
        List<String> completions = new ArrayList<>();

        if (areaName == null || areaName.isEmpty()) {
            return completions;
        }

        List<AreaBackup> backups = backupManager.getBackupHistory(areaName);
        for (int i = 0; i < backups.size(); i++) {
            completions.add(String.valueOf(i));
        }

        if (!backups.isEmpty()) {
            completions.add("latest");
            completions.add("oldest");
        }

        return completions;
    }

    private List<String> getPageNumberCompletions(String areaName) {
        List<String> completions = new ArrayList<>();

        if (areaName == null || areaName.isEmpty()) {
            return completions;
        }

        List<AreaBackup> backups = backupManager.getBackupHistory(areaName);
        int itemsPerPage = 10;
        int maxPage = Math.max(1, (int) Math.ceil((double) backups.size() / itemsPerPage));

        for (int i = 1; i <= Math.min(maxPage, 10); i++) {
            completions.add(String.valueOf(i));
        }

        return completions;
    }

    private List<String> getOnlinePlayerCompletions() {
        List<String> completions = new ArrayList<>();

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            completions.add(onlinePlayer.getName());
        }

        return completions;
    }

    private List<String> getParticleCompletions() {
        List<String> completions = new ArrayList<>();

        completions.addAll(Arrays.asList(
                "FLAME", "HEART", "ENCHANTMENT_TABLE", "END_ROD", "REDSTONE",
                "CRIT", "VILLAGER_HAPPY", "WATER_DROP", "LAVA", "SMOKE_NORMAL",
                "CLOUD", "SNOWBALL", "PORTAL", "EXPLOSION_NORMAL", "FIREWORKS_SPARK",
                "SPELL", "SPELL_WITCH", "DRIP_WATER", "DRIP_LAVA", "TOWN_AURA",
                "NOTE", "SUSPENDED", "SUSPENDED_DEPTH", "CRIT_MAGIC", "SMOKE_LARGE",
                "EXPLOSION_LARGE", "EXPLOSION_HUGE", "FALLING_DUST", "BLOCK_CRACK",
                "BLOCK_DUST", "WATER_BUBBLE", "WATER_SPLASH", "WATER_WAKE"
        ));

        return completions;
    }

    private long parseTimeString(String timeStr) {
        try {
            char unit = timeStr.charAt(timeStr.length() - 1);
            int amount = Integer.parseInt(timeStr.substring(0, timeStr.length() - 1));
            switch (unit) {
                case 'm': return amount;
                case 'h': return amount * 60;
                case 'd': return amount * 60 * 24;
                case 'w': return amount * 60 * 24 * 7;
                default: return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    private boolean handlePreview(Player player, String[] args) {
        if (args.length == 1) {
            boolean wasActive = visualizationManager.toggleBackupPreview(player);
            if (!wasActive) {
                player.sendMessage(ChatColor.YELLOW + "No active preview found!");
                player.sendMessage(ChatColor.GRAY + "Usage: /rewind preview <area> <backup_id|latest|oldest>");
            }
            return true;
        }

        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /rewind preview <area> <backup_id|latest|oldest>");
            player.sendMessage(ChatColor.GRAY + "Or: /rewind preview (to toggle off active preview)");
            return true;
        }

        String areaName = args[1];
        String backupId = args[2];

        ProtectedArea area = areaManager.getArea(areaName);
        if (area == null) {
            player.sendMessage(ChatColor.RED + "Area '" + areaName + "' not found!");
            return true;
        }

        if (!permissionManager.hasAreaPermission(player, area)) {
            player.sendMessage(ChatColor.RED + "You don't have permission for this area!");
            return true;
        }

        List<AreaBackup> backups = backupManager.getBackupHistory(areaName);
        if (backups.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No backups found for '" + areaName + "'!");
            return true;
        }

        AreaBackup backup = null;
        int id = -1;

        if (backupId.equalsIgnoreCase("latest")) {
            id = backups.size() - 1;
            backup = backups.get(id);
        } else if (backupId.equalsIgnoreCase("oldest")) {
            id = 0;
            backup = backups.get(id);
        } else {
            try {
                id = Integer.parseInt(backupId);
                if (id >= 0 && id < backups.size()) {
                    backup = backups.get(id);
                }
            } catch (NumberFormatException e) {
                for (int i = 0; i < backups.size(); i++) {
                    if (backups.get(i).getId().equals(backupId)) {
                        backup = backups.get(i);
                        id = i;
                        break;
                    }
                }
            }
        }

        if (backup == null) {
            player.sendMessage(ChatColor.RED + "Backup not found! Available: 0-" + (backups.size() - 1) + ", 'latest', 'oldest'");
            return true;
        }

        boolean isActive = visualizationManager.toggleBackupPreview(player, backup, area);

        if (isActive) {
            Particle currentParticle = visualizationManager.getPlayerParticle(player);
            player.sendMessage(ChatColor.GREEN + "Backup preview activated! Backup: " + id + " (" + backupId + ")");
            player.sendMessage(ChatColor.YELLOW + "Particle: " + currentParticle.name() + " | " + backup.getBlocks().size() + " blocks");
            player.sendMessage(ChatColor.GRAY + "To disable: /rewind preview");
        }

        return true;
    }

    private boolean handleParticle(Player player, String[] args) {
        if (args.length < 2) {
            Particle current = visualizationManager.getPlayerParticle(player);
            player.sendMessage(ChatColor.GREEN + "Current particle: " + ChatColor.YELLOW + current.name());
            player.sendMessage(ChatColor.GRAY + "Usage: /rewind particle <name>");
            player.sendMessage(ChatColor.GRAY + "Popular particles: flame, heart, enchantment_table, end_rod, redstone, crit");
            return true;
        }

        String particleName = args[1].toUpperCase();

        try {
            Particle particle = Particle.valueOf(particleName);
            visualizationManager.setPlayerParticle(player, particle);
            player.sendMessage(ChatColor.GREEN + "Particle set to: " + ChatColor.YELLOW + particle.name());

            if (visualizationManager.hasActiveBackupPreview(player)) {
                player.sendMessage(ChatColor.GRAY + "The new particle will be used in the next preview.");
            }

        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Unknown particle: " + particleName);
            player.sendMessage(ChatColor.YELLOW + "Popular particles:");
            player.sendMessage(ChatColor.GRAY + "- FLAME (default)");
            player.sendMessage(ChatColor.GRAY + "- HEART");
            player.sendMessage(ChatColor.GRAY + "- ENCHANTMENT_TABLE");
            player.sendMessage(ChatColor.GRAY + "- END_ROD");
            player.sendMessage(ChatColor.GRAY + "- REDSTONE");
            player.sendMessage(ChatColor.GRAY + "- CRIT");
            player.sendMessage(ChatColor.GRAY + "- VILLAGER_HAPPY");
            player.sendMessage(ChatColor.GRAY + "- WATER_DROP");
            player.sendMessage(ChatColor.GRAY + "- LAVA");
            player.sendMessage(ChatColor.GRAY + "- SMOKE_NORMAL");
        }

        return true;
    }
}
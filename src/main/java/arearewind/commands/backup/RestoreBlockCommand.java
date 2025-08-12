package arearewind.commands.backup;

import arearewind.commands.base.BaseCommand;
import arearewind.data.AreaBackup;
import arearewind.data.ProtectedArea;
import arearewind.managers.*;
import arearewind.util.ConfigurationManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command for restoring from backups via command blocks and console
 * This version doesn't require player permissions and is designed for
 * automation
 */
public class RestoreBlockCommand extends BaseCommand {

    public RestoreBlockCommand(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
            GUIManager guiManager, VisualizationManager visualizationManager,
            PermissionManager permissionManager, ConfigurationManager configManager,
            FileManager fileManager, IntervalManager intervalManager) {
        super(plugin, areaManager, backupManager, guiManager, visualizationManager,
                permissionManager, configManager, fileManager, intervalManager);
    }

    @Override
    public boolean execute(Player player, String[] args) {
        // This should not be called directly by players, but handle it gracefully
        if (player != null) {
            player.sendMessage(ChatColor.RED + "This command is for command blocks and console only!");
            player.sendMessage(ChatColor.GRAY + "Use " + ChatColor.GREEN + "/rewind restore" +
                    ChatColor.GRAY + " instead.");
            return true;
        }

        return executeForCommandBlock(Bukkit.getConsoleSender(), args);
    }

    /**
     * Execute the restore command for command blocks or console
     * 
     * @param sender The command sender (console or command block)
     * @param args   Command arguments
     * @return true if command was handled successfully
     */
    public boolean executeForCommandBlock(CommandSender sender, String[] args) {
        if (args.length < 2) {
            logToSender(sender, "Usage: /rewind restoreblock <area> <backup_id|latest|oldest> [world]");
            return true;
        }

        String areaName = args[0];
        String backupId = args[1];
        String worldName = args.length > 2 ? args[2] : null;

        // Validate area exists
        ProtectedArea area = areaManager.getArea(areaName);
        if (area == null) {
            logToSender(sender, "Area '" + areaName + "' not found!");
            return true;
        }

        // If world is specified, validate that the area is in that world
        if (worldName != null && !area.getPos1().getWorld().getName().equals(worldName)) {
            logToSender(sender, "Area '" + areaName + "' is not in world '" + worldName + "'!");
            logToSender(sender, "Area is in world: " + area.getPos1().getWorld().getName());
            return true;
        }

        List<AreaBackup> backups = backupManager.getBackupHistory(areaName);
        if (backups.isEmpty()) {
            logToSender(sender, "No backups found for '" + areaName + "'!");
            return true;
        }

        int id = parseBackupId(sender, backupId, backups);
        if (id == -1) {
            return true;
        }

        logToSender(sender, "Restoring backup " + id + " for area '" + areaName + "'...");

        // Execute restore asynchronously
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                // Don't create backup when restoring via command block, no player for progress
                // updates
                boolean success = backupManager.restoreArea(areaName, area, id, false, null);

                if (success) {
                    logToSender(sender, "Successfully restored backup " + id + " for area '" + areaName + "'");
                } else {
                    logToSender(sender, "Failed to restore backup " + id + " for area '" + areaName + "'");
                }
            } catch (Exception e) {
                logToSender(sender, "Error restoring backup: " + e.getMessage());
                plugin.getLogger().severe("Error in restoreblock command: " + e.getMessage());
                e.printStackTrace();
            }
        });

        return true;
    }

    private int parseBackupId(CommandSender sender, String backupId, List<AreaBackup> backups) {
        if (backupId.equalsIgnoreCase("latest")) {
            return backups.size() - 1;
        } else if (backupId.equalsIgnoreCase("oldest")) {
            return 0;
        } else {
            try {
                int id = Integer.parseInt(backupId);
                if (id < 0 || id >= backups.size()) {
                    logToSender(sender, "Backup ID not found! Available: 0-" + (backups.size() - 1));
                    return -1;
                }
                return id;
            } catch (NumberFormatException e) {
                logToSender(sender, "Invalid backup ID: " + backupId);
                logToSender(sender, "Use a number (0-" + (backups.size() - 1) + ") or 'latest'/'oldest'");
                return -1;
            }
        }
    }

    private void logToSender(CommandSender sender, String message) {
        if (sender != null) {
            sender.sendMessage(message);
        }
        // Also log to console for command blocks
        plugin.getLogger().info("[RestoreBlock] " + ChatColor.stripColor(message));
    }

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1) {
            return getAreaCompletions().stream()
                    .filter(area -> area.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            List<String> completions = getBackupIdCompletions(args[0]);
            return completions.stream()
                    .filter(completion -> completion.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 3) {
            // World completions
            return Bukkit.getWorlds().stream()
                    .map(world -> world.getName())
                    .filter(world -> world.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public String getName() {
        return "restoreblock";
    }

    @Override
    public String getDescription() {
        return "Restore an area from a backup (for command blocks and console)";
    }

    @Override
    public String getUsage() {
        return "/rewind restoreblock <area> <backup_id|latest|oldest> [world]";
    }
}

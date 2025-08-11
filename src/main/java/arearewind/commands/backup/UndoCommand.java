package arearewind.commands.backup;

import arearewind.commands.base.BaseCommand;
import arearewind.data.AreaBackup;
import arearewind.data.ProtectedArea;
import arearewind.managers.*;
import arearewind.util.ConfigurationManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command for undoing changes
 */
public class UndoCommand extends BaseCommand {

    public UndoCommand(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
            GUIManager guiManager, VisualizationManager visualizationManager,
            PermissionManager permissionManager, ConfigurationManager configManager,
            FileManager fileManager, IntervalManager intervalManager) {
        super(plugin, areaManager, backupManager, guiManager, visualizationManager,
                permissionManager, configManager, fileManager, intervalManager);
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (!validateMinArgs(player, args, 1)) {
            return true;
        }

        String areaName = args[0];
        ProtectedArea area = validateAndGetArea(player, areaName);
        if (area == null) {
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
                player.sendMessage(ChatColor.GREEN + "Undo successful! Restored backup " +
                        backupManager.getUndoPointer(areaName));
                player.sendMessage(ChatColor.YELLOW + "From: " +
                        backup.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            });
        });

        return true;
    }

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1) {
            return getAreaCompletions().stream()
                    .filter(area -> area.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public String getName() {
        return "undo";
    }

    @Override
    public String getDescription() {
        return "Undo the last change to an area";
    }

    @Override
    public String getUsage() {
        return "/rewind undo <area>";
    }
}

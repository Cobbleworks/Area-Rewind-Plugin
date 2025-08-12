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
 * Command for redoing changes
 */
public class RedoCommand extends BaseCommand {

    public RedoCommand(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
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
            player.sendMessage(ChatColor.RED + "You don't have permission to redo changes to this area!");
            return true;
        }

        if (!backupManager.canRedo(areaName)) {
            player.sendMessage(ChatColor.RED + "No redo history available for '" + areaName + "'!");
            return true;
        }

        player.sendMessage(ChatColor.YELLOW + "Redoing changes to '" + areaName + "'...");

        Bukkit.getScheduler().runTask(plugin, () -> {
            boolean success = backupManager.redoArea(areaName, area);
            if (!success) {
                player.sendMessage(ChatColor.RED + "Cannot redo further!");
                return;
            }

            AreaBackup backup = backupManager.getBackupHistory(areaName).get(backupManager.getUndoPointer(areaName));
            player.sendMessage(ChatColor.GREEN + "Redo successful! Restored backup " +
                    backupManager.getUndoPointer(areaName));
            player.sendMessage(ChatColor.YELLOW + "From: " +
                    backup.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
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
        return "redo";
    }

    @Override
    public String getDescription() {
        return "Redo the last undone change to an area";
    }

    @Override
    public String getUsage() {
        return "/rewind redo <area>";
    }
}

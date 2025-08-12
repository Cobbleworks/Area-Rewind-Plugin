package arearewind.commands.backup;

import arearewind.commands.base.BaseCommand;
import arearewind.data.ProtectedArea;
import arearewind.managers.*;
import arearewind.util.ConfigurationManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

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
            player.sendMessage(
                    ChatColor.RED + "No undo available for '" + areaName + "'! You need to restore a backup first.");
            return true;
        }

        player.sendMessage(ChatColor.YELLOW + "Undoing last restore for '" + areaName + "'...");

        Bukkit.getScheduler().runTask(plugin, () -> {
            boolean success = backupManager.undoArea(areaName, area, player);
            if (!success) {
                player.sendMessage(ChatColor.RED + "Undo failed!");
                return;
            }
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
        return "Undo the last backup restore operation";
    }

    @Override
    public String getUsage() {
        return "/rewind undo <area>";
    }
}

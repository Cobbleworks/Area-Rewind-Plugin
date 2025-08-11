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

import java.util.List;
import java.util.stream.Collectors;

/**
 * Command for creating backups
 */
public class BackupCommand extends BaseCommand {

    public BackupCommand(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
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

        if (!validateAreaPermission(player, area)) {
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
        return "backup";
    }

    @Override
    public String getDescription() {
        return "Create a backup of an area";
    }

    @Override
    public String getUsage() {
        return "/rewind backup <area>";
    }
}

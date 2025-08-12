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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command for restoring from backups
 */
public class RestoreCommand extends BaseCommand {

    public RestoreCommand(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
            GUIManager guiManager, VisualizationManager visualizationManager,
            PermissionManager permissionManager, ConfigurationManager configManager,
            FileManager fileManager, IntervalManager intervalManager) {
        super(plugin, areaManager, backupManager, guiManager, visualizationManager,
                permissionManager, configManager, fileManager, intervalManager);
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (!validateMinArgs(player, args, 2)) {
            return true;
        }

        String areaName = args[0];
        String backupId = args[1];

        ProtectedArea area = validateAndGetArea(player, areaName);
        if (area == null) {
            return true;
        }

        if (!validateAreaPermission(player, area)) {
            return true;
        }

        List<AreaBackup> backups = backupManager.getBackupHistory(areaName);
        if (backups.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No backups found for '" + areaName + "'!");
            return true;
        }

        int id = parseBackupId(player, backupId, backups);
        if (id == -1) {
            return true;
        }

        player.sendMessage(ChatColor.YELLOW + "Restoring backup " + id + " for '" + areaName + "'...");

        Bukkit.getScheduler().runTask(plugin, () -> {
            backupManager.restoreArea(areaName, area, id, false, player); // Don't create backup when manually
                                                                          // restoring, pass player for progress updates
        });

        return true;
    }

    private int parseBackupId(Player player, String backupId, List<AreaBackup> backups) {
        if (backupId.equalsIgnoreCase("latest")) {
            return backups.size() - 1;
        } else if (backupId.equalsIgnoreCase("oldest")) {
            return 0;
        } else {
            Integer id = parseInteger(player, backupId, "backup ID");
            if (id == null) {
                return -1;
            }

            if (id < 0 || id >= backups.size()) {
                player.sendMessage(ChatColor.RED + "Backup ID not found! Available: 0-" + (backups.size() - 1));
                return -1;
            }

            return id;
        }
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
        }
        return new ArrayList<>();
    }

    @Override
    public String getName() {
        return "restore";
    }

    @Override
    public String getDescription() {
        return "Restore an area from a backup";
    }

    @Override
    public String getUsage() {
        return "/rewind restore <area> <backup_id|latest|oldest>";
    }
}

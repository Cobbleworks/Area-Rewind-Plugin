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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command for rolling back to a specific time
 */
public class RollbackCommand extends BaseCommand {

    public RollbackCommand(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
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
        String timeStr = args[1];

        ProtectedArea area = validateAndGetArea(player, areaName);
        if (area == null) {
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

    private long parseTimeString(String timeStr) {
        try {
            char unit = timeStr.charAt(timeStr.length() - 1);
            int amount = Integer.parseInt(timeStr.substring(0, timeStr.length() - 1));
            switch (unit) {
                case 'm':
                    return amount;
                case 'h':
                    return amount * 60;
                case 'd':
                    return amount * 60 * 24;
                case 'w':
                    return amount * 60 * 24 * 7;
                default:
                    return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1) {
            return getAreaCompletions().stream()
                    .filter(area -> area.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            return Arrays.asList("5m", "10m", "30m", "1h", "2h", "6h", "12h", "1d", "3d", "1w").stream()
                    .filter(time -> time.startsWith(args[1]))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public String getName() {
        return "rollback";
    }

    @Override
    public String getDescription() {
        return "Rollback an area to a specific time ago";
    }

    @Override
    public String getUsage() {
        return "/rewind rollback <area> <time>";
    }
}

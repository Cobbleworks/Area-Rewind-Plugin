package arearewind.commands.maintenance;

import arearewind.commands.base.BaseCommand;
import arearewind.data.ProtectedArea;
import arearewind.managers.*;
import arearewind.util.ConfigurationManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command for cleaning up old backups
 */
public class CleanupCommand extends BaseCommand {

    public CleanupCommand(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
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
        int days = 7; // Default to 7 days

        if (args.length > 1) {
            Integer parsedDays = parseInteger(player, args[1], "number of days");
            if (parsedDays == null) {
                return true;
            }
            days = parsedDays;
        }

        ProtectedArea area = validateAndGetArea(player, areaName);
        if (area == null) {
            return true;
        }

        if (!validateAreaPermission(player, area)) {
            return true;
        }

        int removed = backupManager.cleanupBackups(areaName, days);

        if (removed > 0) {
            player.sendMessage(ChatColor.GREEN + "Cleaned up " + removed +
                    " old backups for '" + areaName + "'!");
        } else {
            player.sendMessage(ChatColor.YELLOW + "No old backups found to cleanup for '" + areaName + "'!");
        }

        return true;
    }

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1) {
            return getAreaCompletions().stream()
                    .filter(area -> area.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            return Arrays.asList("1", "3", "7", "14", "30", "60", "90").stream()
                    .filter(days -> days.startsWith(args[1]))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public String getName() {
        return "cleanup";
    }

    @Override
    public String getDescription() {
        return "Clean up old backups for an area";
    }

    @Override
    public String getUsage() {
        return "/rewind cleanup <area> [days]";
    }
}

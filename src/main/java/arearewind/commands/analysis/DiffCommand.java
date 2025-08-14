package arearewind.commands.analysis;

import arearewind.commands.base.BaseCommand;
import arearewind.data.AreaBackup;
import arearewind.managers.*;
import arearewind.util.ConfigurationManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command for comparing two backups
 */
public class DiffCommand extends BaseCommand {

    public DiffCommand(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
            GUIManager guiManager, VisualizationManager visualizationManager,
            PermissionManager permissionManager, ConfigurationManager configManager,
            FileManager fileManager, IntervalManager intervalManager) {
        super(plugin, areaManager, backupManager, guiManager, visualizationManager,
                permissionManager, configManager, fileManager, intervalManager);
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (!validateMinArgs(player, args, 3)) {
            return true;
        }

        String areaName = args[0];
        String arg1 = args[1];
        String arg2 = args[2];

        // Check if we have the area
        var area = areaManager.getArea(areaName);
        if (area == null) {
            player.sendMessage(ChatColor.RED + "Area '" + areaName + "' not found!");
            return true;
        }

        // Get backups
        List<AreaBackup> backups = backupManager.getBackupHistory(areaName);
        if (backups.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No backups found for area '" + areaName + "'!");
            return true;
        }

        // Parse backup IDs or handle "current" keyword
        AreaBackup backup1 = null;
        AreaBackup backup2 = null;
        String label1 = "";
        String label2 = "";

        // Handle first backup
        if ("current".equalsIgnoreCase(arg1)) {
            backup1 = backupManager.createBackupFromArea(area);
            label1 = "current world state";
        } else {
            Integer id1 = parseInteger(player, arg1, "first backup ID");
            if (id1 == null)
                return true;

            if (id1 < 0 || id1 >= backups.size()) {
                player.sendMessage(ChatColor.RED + "First backup ID not found! Available: 0-" + (backups.size() - 1)
                        + " or 'current'");
                return true;
            }
            backup1 = backups.get(id1);
            label1 = "backup " + id1;
        }

        // Handle second backup
        if ("current".equalsIgnoreCase(arg2)) {
            backup2 = backupManager.createBackupFromArea(area);
            label2 = "current world state";
        } else {
            Integer id2 = parseInteger(player, arg2, "second backup ID");
            if (id2 == null)
                return true;

            if (id2 < 0 || id2 >= backups.size()) {
                player.sendMessage(ChatColor.RED + "Second backup ID not found! Available: 0-" + (backups.size() - 1)
                        + " or 'current'");
                return true;
            }
            backup2 = backups.get(id2);
            label2 = "backup " + id2;
        }

        List<String> diffs = backupManager.compareBackups(backup1, backup2);

        if (diffs.isEmpty()) {
            player.sendMessage(ChatColor.GREEN + "No differences between " + label1 + " and " + label2 + ".");
        } else {
            player.sendMessage(ChatColor.GOLD + "=== Differences between " + label1 + " and " + label2 + " ===");
            for (String diff : diffs) {
                player.sendMessage(diff);
            }
        }

        return true;
    }

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1) {
            return getAreaCompletions().stream()
                    .filter(area -> area.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2 || args.length == 3) {
            List<String> completions = getBackupIdCompletions(args[0]).stream()
                    .filter(id -> id.startsWith(args[args.length - 1]))
                    .collect(Collectors.toList());

            // Add "current" as an option
            if ("current".startsWith(args[args.length - 1].toLowerCase())) {
                completions.add("current");
            }

            return completions;
        }
        return List.of();
    }

    @Override
    public String getName() {
        return "diff";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("compare");
    }

    @Override
    public String getDescription() {
        return "Compare two backups or a backup with current world state and show differences";
    }

    @Override
    public String getUsage() {
        return "/rewind diff <area> <id1|current> <id2|current>";
    }
}

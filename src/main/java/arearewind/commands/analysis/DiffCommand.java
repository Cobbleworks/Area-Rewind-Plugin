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
        Integer id1 = parseInteger(player, args[1], "first backup ID");
        Integer id2 = parseInteger(player, args[2], "second backup ID");

        if (id1 == null || id2 == null) {
            return true;
        }

        List<AreaBackup> backups = backupManager.getBackupHistory(areaName);

        if (id1 < 0 || id1 >= backups.size() || id2 < 0 || id2 >= backups.size()) {
            player.sendMessage(ChatColor.RED + "Backup IDs not found! Available: 0-" + (backups.size() - 1));
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

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1) {
            return getAreaCompletions().stream()
                    .filter(area -> area.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2 || args.length == 3) {
            return getBackupIdCompletions(args[0]).stream()
                    .filter(id -> id.startsWith(args[args.length - 1]))
                    .collect(Collectors.toList());
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
        return "Compare two backups and show differences";
    }

    @Override
    public String getUsage() {
        return "/rewind diff <area> <id1> <id2>";
    }
}

package arearewind.commands.analysis;

import arearewind.commands.base.BaseCommand;
import arearewind.data.ProtectedArea;
import arearewind.managers.*;
import arearewind.util.ConfigurationManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Command for scanning area changes since last backup
 */
public class ScanCommand extends BaseCommand {

    public ScanCommand(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
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

        List<String> diffs = backupManager.getDifferencesFromLast(areaName, area);

        if (diffs.isEmpty()) {
            player.sendMessage(ChatColor.GREEN + "No differences from the last backup found.");
        } else {
            player.sendMessage(ChatColor.GOLD + "=== Changes since last backup ===");
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
        }
        return List.of();
    }

    @Override
    public String getName() {
        return "scan";
    }

    @Override
    public String getDescription() {
        return "Scan for changes since the last backup";
    }

    @Override
    public String getUsage() {
        return "/rewind scan <area>";
    }
}

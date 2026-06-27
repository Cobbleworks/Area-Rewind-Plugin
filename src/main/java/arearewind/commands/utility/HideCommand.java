package arearewind.commands.utility;

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
 * Command for hiding visualization
 */
public class HideCommand extends BaseCommand {

    public HideCommand(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
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

        if (!permissionManager.canVisualize(player, area)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to hide visualization for this area!");
            return true;
        }

        boolean hidBounds = visualizationManager.hideVisualization(player, areaName);
        boolean hidPreview = visualizationManager.hideBackupPreview(player, areaName);

        if (hidBounds || hidPreview) {
            player.sendMessage(ChatColor.GREEN + "Hidden visualization for '" + areaName + "'.");
        } else {
            player.sendMessage(ChatColor.YELLOW + "No active visualization found for '" + areaName + "'.");
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
        return "hide";
    }

    @Override
    public String getDescription() {
        return "Hide area visualization";
    }

    @Override
    public String getUsage() {
        return "/rewind area <area> hide";
    }
}

package arearewind.commands.utility;

import arearewind.commands.base.BaseCommand;
import arearewind.managers.*;
import arearewind.util.ConfigurationManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
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
        String areaName = null;

        if (args.length > 0) {
            areaName = args[0];
            if (!areaManager.areaExists(areaName)) {
                player.sendMessage(ChatColor.RED + "Area '" + areaName + "' not found!");
                return true;
            }
        }

        try {
            if (areaName != null) {
                player.sendMessage(ChatColor.YELLOW + "Area bounds hiding not yet implemented");
                player.sendMessage(ChatColor.GRAY + "Would hide area bounds for: " + areaName);
            } else {
                player.sendMessage(ChatColor.YELLOW + "Hide all visualizations not yet implemented");
                player.sendMessage(ChatColor.GRAY + "Would hide all visualizations");
            }
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error hiding visualization: " + e.getMessage());
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
    public List<String> getAliases() {
        return Arrays.asList("clear");
    }

    @Override
    public String getDescription() {
        return "Hide area visualization";
    }

    @Override
    public String getUsage() {
        return "/rewind hide [area]";
    }
}

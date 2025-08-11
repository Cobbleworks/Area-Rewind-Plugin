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
 * Command for showing/hiding visualization
 */
public class ShowCommand extends BaseCommand {

    public ShowCommand(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
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

        if (!areaManager.areaExists(areaName)) {
            player.sendMessage(ChatColor.RED + "Area '" + areaName + "' not found!");
            return true;
        }

        try {
            // Placeholder for show functionality
            player.sendMessage(ChatColor.YELLOW + "Area bounds visualization not yet implemented");
            player.sendMessage(ChatColor.GRAY + "Would show area bounds for: " + areaName);
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error showing area: " + e.getMessage());
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
        return "show";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("display", "visualize");
    }

    @Override
    public String getDescription() {
        return "Show area visualization";
    }

    @Override
    public String getUsage() {
        return "/rewind show <area>";
    }
}

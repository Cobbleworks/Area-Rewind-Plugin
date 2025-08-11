package arearewind.commands.management;

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
 * Command for managing backup intervals
 */
public class IntervalCommand extends BaseCommand {

    public IntervalCommand(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
            GUIManager guiManager, VisualizationManager visualizationManager,
            PermissionManager permissionManager, ConfigurationManager configManager,
            FileManager fileManager, IntervalManager intervalManager) {
        super(plugin, areaManager, backupManager, guiManager, visualizationManager,
                permissionManager, configManager, fileManager, intervalManager);
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (!validateMinArgs(player, args, 3)) {
            player.sendMessage(ChatColor.YELLOW + "Usage: " + getUsage());
            return true;
        }

        String action = args[0].toLowerCase();
        String areaName = args[1];

        if (!areaManager.areaExists(areaName)) {
            player.sendMessage(ChatColor.RED + "Area '" + areaName + "' not found!");
            return true;
        }

        switch (action) {
            case "set":
                Integer interval = parseInteger(player, args[2], "interval");
                if (interval == null || interval < 1) {
                    player.sendMessage(ChatColor.RED + "Interval must be a positive number (minutes)!");
                    return true;
                }

                // For now, this is a placeholder - actual implementation would require
                // extending IntervalManager with these methods
                player.sendMessage(ChatColor.YELLOW + "Interval commands need implementation in IntervalManager");
                player.sendMessage(ChatColor.GRAY + "Use the existing setInterval method with backup ID");
                break;

            case "remove":
                intervalManager.clearInterval(areaName);
                player.sendMessage(ChatColor.GREEN + "Removed backup interval for " + areaName);
                break;

            case "check":
                var config = intervalManager.getIntervalConfig(areaName);
                if (config != null) {
                    player.sendMessage(ChatColor.GREEN + "Current interval for " + areaName +
                            ": " + config.minutes + " minutes (backup " + config.backupId + ")");
                } else {
                    player.sendMessage(ChatColor.YELLOW + "No interval set for " + areaName);
                }
                break;
            default:
                player.sendMessage(ChatColor.RED + "Invalid action! Use: set, remove, or check");
                break;
        }

        return true;
    }

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1) {
            List<String> actions = Arrays.asList("set", "remove", "check");
            return actions.stream()
                    .filter(action -> action.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            return getAreaCompletions().stream()
                    .filter(area -> area.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            return Arrays.asList("1", "5", "10", "15", "30", "60", "120").stream()
                    .filter(time -> time.startsWith(args[2]))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public String getName() {
        return "interval";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("timer", "schedule");
    }

    @Override
    public String getDescription() {
        return "Manage automatic backup intervals";
    }

    @Override
    public String getUsage() {
        return "/rewind interval <set|remove|check> <area> [minutes]";
    }
}

package arearewind.commands.area;

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
 * Command for expanding areas
 */
public class ExpandCommand extends BaseCommand {

    private static final List<String> DIRECTIONS = Arrays.asList("north", "south", "east", "west", "up", "down");
    private static final List<String> AMOUNTS = Arrays.asList("1", "5", "10", "25", "50", "100");

    public ExpandCommand(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
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
        String direction = args[1];

        Integer amount = parseInteger(player, args[2], "amount");
        if (amount == null) {
            return true;
        }

        ProtectedArea area = validateAndGetArea(player, areaName);
        if (area == null) {
            return true;
        }

        if (!validateAreaPermission(player, area)) {
            return true;
        }

        if (areaManager.expandArea(areaName, direction, amount)) {
            player.sendMessage(ChatColor.GREEN + "Area '" + areaName + "' expanded " +
                    direction + " by " + amount + " blocks!");
            areaManager.saveAreas();
        } else {
            player.sendMessage(ChatColor.RED + "Invalid direction! Use: north, south, east, west, up, down");
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
            return DIRECTIONS.stream()
                    .filter(dir -> dir.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 3) {
            return AMOUNTS.stream()
                    .filter(amt -> amt.startsWith(args[2]))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public String getName() {
        return "expand";
    }

    @Override
    public String getDescription() {
        return "Expand an area in a specific direction";
    }

    @Override
    public String getUsage() {
        return "/rewind expand <area> <direction> <amount>";
    }
}

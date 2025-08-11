package arearewind.commands.navigation;

import arearewind.commands.base.BaseCommand;
import arearewind.data.ProtectedArea;
import arearewind.managers.*;
import arearewind.util.ConfigurationManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command for teleporting to areas
 */
public class TeleportCommand extends BaseCommand {

    public TeleportCommand(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
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

        if (!validateAreaPermission(player, area)) {
            return true;
        }

        Location center = area.getCenter();
        center.setY(center.getY() + 1);
        player.teleport(center);
        player.sendMessage(ChatColor.GREEN + "Teleported to area '" + areaName + "'!");

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
        return "teleport";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("tp");
    }

    @Override
    public String getDescription() {
        return "Teleport to an area";
    }

    @Override
    public String getUsage() {
        return "/rewind teleport <area>";
    }
}

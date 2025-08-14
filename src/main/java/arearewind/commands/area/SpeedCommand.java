package arearewind.commands.area;

import arearewind.commands.base.BaseCommand;
import arearewind.data.ProtectedArea;
import arearewind.managers.*;
import arearewind.util.ConfigurationManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Arrays;

public class SpeedCommand extends BaseCommand {

    public SpeedCommand(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
            GUIManager guiManager, VisualizationManager visualizationManager,
            PermissionManager permissionManager, ConfigurationManager configManager,
            FileManager fileManager, IntervalManager intervalManager) {
        super(plugin, areaManager, backupManager, guiManager, visualizationManager,
                permissionManager, configManager, fileManager, intervalManager);
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (!validateMinArgs(player, args, 2)) {
            return true;
        }

        String areaName = args[0];
        String speedArg = args[1];

        ProtectedArea area = validateAndGetArea(player, areaName);
        if (area == null) {
            return true;
        }

        if (!validateAreaPermission(player, area)) {
            return true;
        }

        if (speedArg.equalsIgnoreCase("dynamic")) {
            // Reset to dynamic speed
            area.setCustomRestoreSpeed(null);
            areaManager.saveAreas();
            player.sendMessage(
                    ChatColor.GREEN + "Restore speed for area '" + areaName + "' set to dynamic (auto-calculated)");
            return true;
        }

        try {
            int speed = Integer.parseInt(speedArg);

            // Validate speed range
            if (speed < 1 || speed > 2000) {
                player.sendMessage(ChatColor.RED + "Speed must be between 1 and 2000 blocks per tick!");
                player.sendMessage(ChatColor.GRAY + "Lower values = slower but less server impact");
                player.sendMessage(ChatColor.GRAY + "Higher values = faster but more server impact");
                return true;
            }

            area.setCustomRestoreSpeed(speed);
            areaManager.saveAreas();

            player.sendMessage(
                    ChatColor.GREEN + "Restore speed for area '" + areaName + "' set to " + speed + " blocks per tick");
            player.sendMessage(
                    ChatColor.GRAY + "This will be used instead of dynamic calculation for all future restorations");

        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid speed value! Use a number between 10-1000 or 'dynamic'");
        }

        return true;
    }

    @Override
    public String getName() {
        return "speed";
    }

    @Override
    public String getUsage() {
        return "/rewind speed <area-name> <speed|dynamic>";
    }

    @Override
    public String getDescription() {
        return "Set custom restore speed for an area (1-2000 blocks/tick) or use 'dynamic'";
    }

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1) {
            return areaManager.getAreaNames(args[0]).stream()
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
        } else if (args.length == 2) {
            return Arrays.asList("dynamic", "1", "10", "50", "100", "200", "400", "800", "1600", "2000");
        }
        return List.of();
    }
}

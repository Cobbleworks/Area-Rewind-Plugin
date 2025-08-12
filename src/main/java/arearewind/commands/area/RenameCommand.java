package arearewind.commands.area;

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
 * Command for renaming areas
 */
public class RenameCommand extends BaseCommand {

    public RenameCommand(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
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

        String oldName = args[0];
        String newName = args[1];

        ProtectedArea area = validateAndGetArea(player, oldName);
        if (area == null) {
            return true;
        }

        if (areaManager.areaExists(newName)) {
            player.sendMessage(ChatColor.RED + "Area '" + newName + "' already exists!");
            return true;
        }

        if (!validateAreaPermission(player, area)) {
            return true;
        }

        // Note: Backup file renaming would need to be implemented in BackupManager
        player.sendMessage(ChatColor.YELLOW + "Renaming backup files is currently not available.");

        area.setName(newName);
        areaManager.addArea(area);
        areaManager.deleteArea(oldName);
        areaManager.saveAreas();

        player.sendMessage(ChatColor.GREEN + "Area renamed from '" + oldName + "' to '" + newName + "'!");

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
        return "rename";
    }

    @Override
    public String getDescription() {
        return "Rename an area";
    }

    @Override
    public String getUsage() {
        return "/rewind rename <old_name> <new_name>";
    }
}

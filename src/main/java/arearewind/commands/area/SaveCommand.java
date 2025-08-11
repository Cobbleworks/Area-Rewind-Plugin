package arearewind.commands.area;

import arearewind.commands.base.BaseCommand;
import arearewind.data.ProtectedArea;
import arearewind.managers.*;
import arearewind.util.ConfigurationManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Command for saving/creating new areas
 */
public class SaveCommand extends BaseCommand {

    public SaveCommand(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
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

        String name = args[0];
        UUID playerId = player.getUniqueId();

        if (!validateSelection(player)) {
            return true;
        }

        if (areaManager.areaExists(name)) {
            player.sendMessage(ChatColor.RED + "Area '" + name + "' already exists!");
            return true;
        }

        player.sendMessage(ChatColor.YELLOW + "Creating area '" + name + "'...");

        if (areaManager.createArea(name, playerId)) {
            ProtectedArea area = areaManager.getArea(name);

            if (area.getSize() > configManager.getMaxAreaSize()) {
                areaManager.deleteArea(name);
                player.sendMessage(ChatColor.RED + "Area too large! Maximum size: " +
                        configManager.getMaxAreaSize() + " blocks");
                return true;
            }

            player.sendMessage(ChatColor.YELLOW + "Creating initial backup for '" + name + "'... Please wait.");

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    backupManager.createBackup(name, area);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.sendMessage(ChatColor.GREEN + "Area '" + name + "' created with initial backup!");
                        player.sendMessage(ChatColor.YELLOW + "Size: " + area.getSize() + " blocks");
                    });
                } catch (Exception e) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.sendMessage(ChatColor.RED + "Failed to create initial backup!");
                        plugin.getLogger().severe("Failed to create backup for area " + name + ": " + e.getMessage());
                    });
                }
            });

        } else {
            player.sendMessage(ChatColor.RED + "Failed to create area!");
        }

        return true;
    }

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public String getName() {
        return "save";
    }

    @Override
    public String getDescription() {
        return "Save the selected area with a name";
    }

    @Override
    public String getUsage() {
        return "/rewind save <name>";
    }
}

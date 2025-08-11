package arearewind.commands.admin;

import arearewind.commands.base.BaseCommand;
import arearewind.managers.*;
import arearewind.util.ConfigurationManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Command for reloading plugin configuration
 */
public class ReloadCommand extends BaseCommand {

    public ReloadCommand(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
            GUIManager guiManager, VisualizationManager visualizationManager,
            PermissionManager permissionManager, ConfigurationManager configManager,
            FileManager fileManager, IntervalManager intervalManager) {
        super(plugin, areaManager, backupManager, guiManager, visualizationManager,
                permissionManager, configManager, fileManager, intervalManager);
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (!player.hasPermission("arearewind.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to reload the plugin!");
            return true;
        }

        player.sendMessage(ChatColor.YELLOW + "Area Rewind Plugin is fully reloading...");

        if (plugin instanceof arearewind.AreaRewindPlugin) {
            ((arearewind.AreaRewindPlugin) plugin).reloadAll();
            player.sendMessage(ChatColor.GREEN + "Plugin fully reloaded!");
        } else {
            player.sendMessage(ChatColor.RED + "Reload failed: plugin instance is not AreaRewindPlugin.");
        }

        return true;
    }

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reload plugin configuration";
    }

    @Override
    public String getUsage() {
        return "/rewind reload";
    }

    @Override
    public String getRequiredPermission() {
        return "arearewind.admin";
    }
}

package arearewind.commands.utility;

import arearewind.commands.base.BaseCommand;
import arearewind.managers.*;
import arearewind.util.ConfigurationManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Command for opening the GUI
 */
public class GUICommand extends BaseCommand {

    public GUICommand(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
            GUIManager guiManager, VisualizationManager visualizationManager,
            PermissionManager permissionManager, ConfigurationManager configManager,
            FileManager fileManager, IntervalManager intervalManager) {
        super(plugin, areaManager, backupManager, guiManager, visualizationManager,
                permissionManager, configManager, fileManager, intervalManager);
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (!permissionManager.canUseGUI(player)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use the GUI!");
            return true;
        }

        // Open the My Areas GUI as the main menu (start with player's own areas)
        guiManager.openMyAreasGUI(player);
        player.sendMessage(ChatColor.GREEN + "Opening AreaRewind areas menu...");
        return true;
    }

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public String getName() {
        return "gui";
    }

    @Override
    public List<String> getAliases() {
        return List.of("menu");
    }

    @Override
    public String getDescription() {
        return "Open the AreaRewind management GUI";
    }

    @Override
    public String getUsage() {
        return "/rewind gui";
    }
}

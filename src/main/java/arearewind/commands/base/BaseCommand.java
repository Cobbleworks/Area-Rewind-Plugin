package arearewind.commands.base;

import arearewind.data.ProtectedArea;
import arearewind.managers.*;
import arearewind.util.ConfigurationManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all commands providing common functionality
 */
public abstract class BaseCommand implements Command {

    protected final JavaPlugin plugin;
    protected final AreaManager areaManager;
    protected final BackupManager backupManager;
    protected final GUIManager guiManager;
    protected final VisualizationManager visualizationManager;
    protected final PermissionManager permissionManager;
    protected final ConfigurationManager configManager;
    protected final FileManager fileManager;
    protected final IntervalManager intervalManager;

    public BaseCommand(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
            GUIManager guiManager, VisualizationManager visualizationManager,
            PermissionManager permissionManager, ConfigurationManager configManager,
            FileManager fileManager, IntervalManager intervalManager) {
        this.plugin = plugin;
        this.areaManager = areaManager;
        this.backupManager = backupManager;
        this.guiManager = guiManager;
        this.visualizationManager = visualizationManager;
        this.permissionManager = permissionManager;
        this.configManager = configManager;
        this.fileManager = fileManager;
        this.intervalManager = intervalManager;
    }

    @Override
    public List<String> getAliases() {
        return new ArrayList<>();
    }

    @Override
    public String getRequiredPermission() {
        return null; // Override in subclasses if specific permission needed
    }

    /**
     * Validate that the player has a valid area selection
     */
    protected boolean validateSelection(Player player) {
        if (!areaManager.hasValidSelection(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Set both positions first!");
            return false;
        }
        return true;
    }

    /**
     * Validate that an area exists and return it
     */
    protected ProtectedArea validateAndGetArea(Player player, String areaName) {
        ProtectedArea area = areaManager.getArea(areaName);
        if (area == null) {
            player.sendMessage(ChatColor.RED + "Area '" + areaName + "' not found!");
            return null;
        }
        return area;
    }

    /**
     * Validate that player has permission for the area
     */
    protected boolean validateAreaPermission(Player player, ProtectedArea area) {
        if (!permissionManager.hasAreaPermission(player, area)) {
            player.sendMessage(ChatColor.RED + "You don't have permission for this area!");
            return false;
        }
        return true;
    }

    /**
     * Validate minimum argument count
     */
    protected boolean validateMinArgs(Player player, String[] args, int minArgs) {
        if (args.length < minArgs) {
            player.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return false;
        }
        return true;
    }

    /**
     * Parse integer safely
     */
    protected Integer parseInteger(Player player, String value, String fieldName) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid " + fieldName + ": " + value);
            return null;
        }
    }

    /**
     * Get backup ID completions for tab completion
     */
    protected List<String> getBackupIdCompletions(String areaName) {
        List<String> completions = new ArrayList<>();
        if (areaName == null || areaName.isEmpty()) {
            return completions;
        }

        var backups = backupManager.getBackupHistory(areaName);
        for (int i = 0; i < backups.size(); i++) {
            completions.add(String.valueOf(i));
        }

        if (!backups.isEmpty()) {
            completions.add("latest");
            completions.add("oldest");
        }

        return completions;
    }

    /**
     * Get area name completions for tab completion
     */
    protected List<String> getAreaCompletions() {
        return new ArrayList<>(areaManager.getProtectedAreas().keySet());
    }
}

package arearewind.managers;

import arearewind.gui.EnhancedGUIManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * GUI Manager - delegates to the enhanced GUI system
 * Maintains backwards compatibility while using the new GUI framework
 */
public class GUIManager implements Listener {
    private final EnhancedGUIManager enhancedGUIManager;

    public GUIManager(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
            PermissionManager permissionManager) {
        // Initialize the enhanced GUI system
        this.enhancedGUIManager = new EnhancedGUIManager(plugin, areaManager, backupManager, permissionManager);
    }

    /**
     * Open the main menu GUI
     */
    public void openMainMenu(Player player) {
        enhancedGUIManager.openMainMenu(player);
    }

    /**
     * Open areas GUI (backwards compatibility - redirects to main menu)
     */
    public void openAreasGUI(Player player) {
        enhancedGUIManager.openMainMenu(player);
        player.sendMessage(ChatColor.GREEN + "Opening AreaRewind main menu...");
    }

    /**
     * Open backups GUI (backwards compatibility - redirects to main menu)
     */
    public void openBackupsGUI(Player player, String areaName) {
        enhancedGUIManager.openMainMenu(player);
        player.sendMessage(ChatColor.GREEN + "Opening AreaRewind main menu...");
        player.sendMessage(ChatColor.YELLOW + "Navigate to Area Management → " + areaName + " → View Backups");
    }

    /**
     * Open area info GUI (backwards compatibility - redirects to main menu)
     */
    public void openAreaInfoGUI(Player player, String areaName) {
        enhancedGUIManager.openMainMenu(player);
        player.sendMessage(ChatColor.GREEN + "Opening AreaRewind main menu...");
        player.sendMessage(ChatColor.YELLOW + "Navigate to Area Management → " + areaName + " → Area Info");
    }

    /**
     * Check if a player has any GUI open (delegates to enhanced system)
     */
    public boolean hasGUIOpen(Player player) {
        return enhancedGUIManager.hasGUIOpen(player);
    }

    /**
     * Close any open GUI for a player (delegates to enhanced system)
     */
    public void closeGUI(Player player) {
        enhancedGUIManager.closeGUI(player);
    }

    /**
     * Get the type of GUI a player has open (backwards compatibility)
     */
    public String getOpenGUIType(Player player) {
        // For backwards compatibility, return a generic type
        return hasGUIOpen(player) ? "enhanced" : null;
    }
}

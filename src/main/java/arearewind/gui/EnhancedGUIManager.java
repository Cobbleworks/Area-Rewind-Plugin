package arearewind.gui;

import arearewind.gui.menus.MainMenuGUI;
import arearewind.gui.utils.GUIListener;
import arearewind.managers.AreaManager;
import arearewind.managers.BackupManager;
import arearewind.managers.PermissionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Enhanced GUI Manager that coordinates the new GUI system
 * while maintaining backwards compatibility with the existing GUIManager
 */
public class EnhancedGUIManager {
    private final JavaPlugin plugin;
    private final AreaManager areaManager;
    private final BackupManager backupManager;
    private final PermissionManager permissionManager;
    private final GUIListener guiListener;
    
    public EnhancedGUIManager(JavaPlugin plugin, AreaManager areaManager, 
                             BackupManager backupManager, PermissionManager permissionManager) {
        this.plugin = plugin;
        this.areaManager = areaManager;
        this.backupManager = backupManager;
        this.permissionManager = permissionManager;
        this.guiListener = new GUIListener();
        
        // Register the new GUI listener
        Bukkit.getPluginManager().registerEvents(guiListener, plugin);
    }
    
    /**
     * Open the main menu for a player
     */
    public void openMainMenu(Player player) {
        new MainMenuGUI(plugin, permissionManager).open(player);
    }
    
    /**
     * Check if a player has any GUI open (new system)
     */
    public boolean hasGUIOpen(Player player) {
        return arearewind.gui.utils.BaseGUI.hasGUIOpen(player);
    }
    
    /**
     * Close any open GUI for a player (new system)
     */
    public void closeGUI(Player player) {
        arearewind.gui.utils.BaseGUI gui = arearewind.gui.utils.BaseGUI.getOpenGUI(player);
        if (gui != null) {
            gui.close(player);
        }
    }
    
    // Backwards compatibility methods for the existing GUIManager
    
    /**
     * Open areas GUI (backwards compatibility)
     */
    public void openAreasGUI(Player player) {
        // For now, redirect to main menu
        // TODO: Could redirect to specific area management GUI
        openMainMenu(player);
    }
    
    /**
     * Open backups GUI (backwards compatibility)
     */
    public void openBackupsGUI(Player player, String areaName) {
        // For now, redirect to main menu
        // TODO: Could redirect to specific area backup GUI
        openMainMenu(player);
    }
    
    /**
     * Open area info GUI (backwards compatibility)
     */
    public void openAreaInfoGUI(Player player, String areaName) {
        // For now, redirect to main menu
        // TODO: Could redirect to specific area info GUI
        openMainMenu(player);
    }
}

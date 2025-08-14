package arearewind.managers;

import arearewind.listeners.PlayerInteractionListener;
import arearewind.managers.gui.AreaSettingsGUIPage;
import arearewind.managers.gui.AreasGUIPage;
import arearewind.managers.gui.BackupsGUIPage;
import arearewind.managers.gui.GUIPaginationHelper;
import arearewind.managers.gui.MaterialSelectorGUIPage;
import arearewind.managers.gui.MyAreasGUIPage;
import arearewind.managers.gui.SettingsGUIPage;
import arearewind.util.ConfigurationManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;

public class GUIManager implements Listener {
    private final Map<UUID, String> openGUIs = new HashMap<>();
    private final PermissionManager permissionManager;
    private final ConfigurationManager configManager;

    // GUI Pages
    private final AreasGUIPage areasPage;
    private final MyAreasGUIPage myAreasPage;
    private final BackupsGUIPage backupsPage;
    private final SettingsGUIPage settingsPage;
    private final AreaSettingsGUIPage areaSettingsPage;
    private final MaterialSelectorGUIPage materialSelectorPage;

    public GUIManager(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
            PermissionManager permissionManager, ConfigurationManager configManager, FileManager fileManager,
            IntervalManager intervalManager) {

        // Store references for later use
        this.permissionManager = permissionManager;
        this.configManager = configManager;

        // Initialize GUI pages
        this.areasPage = new AreasGUIPage(this, areaManager, backupManager, permissionManager, intervalManager);
        this.myAreasPage = new MyAreasGUIPage(this, areaManager, backupManager, permissionManager, intervalManager);
        this.backupsPage = new BackupsGUIPage(this, areaManager, backupManager, permissionManager, intervalManager);
        this.settingsPage = new SettingsGUIPage(this, permissionManager);
        this.areaSettingsPage = new AreaSettingsGUIPage(this, areaManager, backupManager, permissionManager);
        this.materialSelectorPage = new MaterialSelectorGUIPage(this, areaManager, backupManager, permissionManager,
                fileManager);
    }

    // Public methods for opening specific GUIs
    public void openAreasGUI(Player player) {
        areasPage.openGUI(player);
    }

    public void openAreasGUI(Player player, int page) {
        areasPage.openGUI(player, page);
    }

    public void openMyAreasGUI(Player player) {
        myAreasPage.openGUI(player);
    }

    public void openMyAreasGUI(Player player, int page) {
        myAreasPage.openGUI(player, page);
    }

    public void openBackupsGUI(Player player, String areaName) {
        backupsPage.openBackupsGUI(player, areaName);
    }

    public void openBackupsGUI(Player player, String areaName, int page) {
        backupsPage.openBackupsGUI(player, areaName, page);
    }

    public void openSettingsGUI(Player player) {
        settingsPage.openGUI(player);
    }

    public void openAreaSettingsGUI(Player player, String areaName) {
        areaSettingsPage.openAreaSettingsGUI(player, areaName);
    }

    public void openMaterialSelector(Player player, String type, String areaName, String backupId) {
        materialSelectorPage.openMaterialSelector(player, type, areaName, backupId, 0);
    }

    public void openMaterialSelector(Player player, String type, String areaName, String backupId, int page) {
        materialSelectorPage.openMaterialSelector(player, type, areaName, backupId, page);
    }

    public void setPlayerInteractionListener(PlayerInteractionListener playerListener) {
        // Set the listener for the main settings page
        settingsPage.setPlayerInteractionListener(playerListener);
    }

    // GUI state management methods
    public void registerOpenGUI(Player player, String guiType) {
        openGUIs.put(player.getUniqueId(), guiType);
    }

    public void closeGUI(Player player) {
        openGUIs.remove(player.getUniqueId());
        // Clear pagination data when GUI is closed
        GUIPaginationHelper.clearPaginationData(player.getUniqueId());
    }

    public boolean hasGUIOpen(Player player) {
        return openGUIs.containsKey(player.getUniqueId());
    }

    public String getOpenGUIType(Player player) {
        return openGUIs.get(player.getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;
        Player player = (Player) event.getWhoClicked();

        if (!openGUIs.containsKey(player.getUniqueId()))
            return;

        // Only cancel clicks in the plugin's GUI, not the player's inventory
        if (event.getClickedInventory() == null ||
                !isPluginGUI(event.getView().getTitle())) {
            return;
        }

        event.setCancelled(true);

        String guiType = openGUIs.get(player.getUniqueId());

        if (guiType.equals("all-areas")) {
            areasPage.handleClick(player, event);
        } else if (guiType.equals("my-areas")) {
            myAreasPage.handleClick(player, event);
        } else if (guiType.startsWith("backups:")) {
            backupsPage.handleClick(player, event);
        } else if (guiType.equals("settings")) {
            settingsPage.handleClick(player, event);
        } else if (guiType.startsWith("area-settings:")) {
            areaSettingsPage.handleClick(player, event);
        } else if (guiType.startsWith("material-selector:")) {
            materialSelectorPage.handleClick(player, event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player))
            return;
        Player player = (Player) event.getPlayer();

        // Check if the closed inventory is one of our plugin GUIs
        String title = event.getView().getTitle();
        if (isPluginGUI(title)) {
            openGUIs.remove(player.getUniqueId());
            // Clear pagination data when GUI is closed
            GUIPaginationHelper.clearPaginationData(player.getUniqueId());
        }
    }

    private boolean isPluginGUI(String title) {
        return title.contains("Protected Areas") ||
                title.contains("Area Management:") ||
                title.contains("Area Rewind Settings") ||
                title.contains("Personal Settings") ||
                title.contains("Area Settings:") ||
                title.contains("Set Icon:");
    }
}

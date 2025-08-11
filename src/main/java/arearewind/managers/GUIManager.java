package arearewind.managers;

import arearewind.managers.gui.AreasGUIPage;
import arearewind.managers.gui.BackupsGUIPage;
import arearewind.managers.gui.IGUIPage;
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
    private final JavaPlugin plugin;
    private final AreaManager areaManager;
    private final BackupManager backupManager;
    private final PermissionManager permissionManager;
    private final ConfigurationManager configManager;
    private final Map<UUID, String> openGUIs = new HashMap<>();

    // GUI Pages
    private final AreasGUIPage areasPage;
    private final BackupsGUIPage backupsPage;
    private final SettingsGUIPage settingsPage;

    public GUIManager(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
            PermissionManager permissionManager, ConfigurationManager configManager) {
        this.plugin = plugin;
        this.areaManager = areaManager;
        this.backupManager = backupManager;
        this.permissionManager = permissionManager;
        this.configManager = configManager;

        // Initialize GUI pages
        this.areasPage = new AreasGUIPage(this, areaManager, backupManager, permissionManager);
        this.backupsPage = new BackupsGUIPage(this, areaManager, backupManager, permissionManager);
        this.settingsPage = new SettingsGUIPage(this, permissionManager, configManager);
    }

    // Public methods for opening specific GUIs
    public void openAreasGUI(Player player) {
        areasPage.openGUI(player);
    }

    public void openBackupsGUI(Player player, String areaName) {
        backupsPage.openBackupsGUI(player, areaName);
    }

    public void openSettingsGUI(Player player) {
        settingsPage.openGUI(player);
    }

    // GUI state management methods
    public void registerOpenGUI(Player player, String guiType) {
        openGUIs.put(player.getUniqueId(), guiType);
    }

    public void closeGUI(Player player) {
        openGUIs.remove(player.getUniqueId());
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

        if (guiType.equals("areas")) {
            areasPage.handleClick(player, event);
        } else if (guiType.startsWith("backups:")) {
            backupsPage.handleClick(player, event);
        } else if (guiType.equals("settings")) {
            settingsPage.handleClick(player, event);
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
        }
    }

    private boolean isPluginGUI(String title) {
        return title.contains("Protected Areas") ||
                title.contains("Area Management:") ||
                title.contains("Area Rewind Settings");
    }
}

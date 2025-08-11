package arearewind.gui.menus;

import arearewind.gui.utils.BaseGUI;
import arearewind.gui.utils.GUIUtils;
import arearewind.gui.utils.ItemBuilder;
import arearewind.managers.PermissionManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Backup Menu GUI - main backup management interface
 */
public class BackupMenuGUI extends BaseGUI {
    private final PermissionManager permissionManager;
    private final BaseGUI parentGUI;
    
    public BackupMenuGUI(JavaPlugin plugin, PermissionManager permissionManager, BaseGUI parentGUI) {
        super(plugin, "&9&lBackup System", 45);
        this.permissionManager = permissionManager;
        this.parentGUI = parentGUI;
    }
    
    @Override
    public void initialize() {
        GUIUtils.fillBorder(this, GUIUtils.Slots.BORDER_45, Material.BLUE_STAINED_GLASS_PANE);
        GUIUtils.addNavigationButtons(this, parentGUI, true);
        
        // TODO: Implement backup menu functionality
        inventory.setItem(22, ItemBuilder.createInfoItem("Coming Soon", "&7Backup management interface", "&7Will include area-specific backup management"));
    }
    
    @Override
    public void handleClick(Player player, InventoryClickEvent event) {
        // TODO: Implement backup menu click handling
    }
}

package arearewind.gui.menus;

import arearewind.gui.utils.BaseGUI;
import arearewind.gui.utils.GUIUtils;
import arearewind.gui.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Confirmation dialog GUI
 */
public class ConfirmationGUI extends BaseGUI {
    private final String message;
    private final String warning;
    private final Runnable onConfirm;
    private final Runnable onCancel;
    private final BaseGUI parentGUI;
    
    public ConfirmationGUI(JavaPlugin plugin, String title, String message, String warning, 
                          Runnable onConfirm, Runnable onCancel, BaseGUI parentGUI) {
        super(plugin, title, 27);
        this.message = message;
        this.warning = warning;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
        this.parentGUI = parentGUI;
    }
    
    @Override
    public void initialize() {
        // Setup confirmation dialog layout
        GUIUtils.setupConfirmationDialog(this, onConfirm, onCancel);
        
        // Override center item with custom message
        inventory.setItem(13, ItemBuilder.createInfoItem("Confirmation", 
                "&7" + message,
                warning != null ? "&c" + warning : "",
                "",
                "&a&lLeft: &7Confirm",
                "&c&lRight: &7Cancel"));
        
        // Back button
        if (parentGUI != null) {
            inventory.setItem(GUIUtils.Slots.BACK_BUTTON_27, ItemBuilder.createBackButton());
            setAction(GUIUtils.Slots.BACK_BUTTON_27, (player, event) -> parentGUI.open(player));
        }
    }
    
    @Override
    public void handleClick(Player player, InventoryClickEvent event) {
        // All clicks handled by specific actions
    }
}

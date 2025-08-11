package arearewind.gui.utils;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * Event listener for the new GUI system
 */
public class GUIListener implements Listener {
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        BaseGUI gui = BaseGUI.getOpenGUI(player);
        
        if (gui != null && gui.isThisInventory(event.getClickedInventory())) {
            gui.processClick(player, event);
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        
        Player player = (Player) event.getPlayer();
        BaseGUI gui = BaseGUI.getOpenGUI(player);
        
        if (gui != null && gui.isThisInventory(event.getInventory())) {
            BaseGUI.removePlayer(player);
        }
    }
}

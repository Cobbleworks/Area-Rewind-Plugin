package arearewind.managers.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Interface for GUI pages in the Area Rewind plugin
 */
public interface IGUIPage {

    /**
     * Opens this GUI page for the specified player
     * 
     * @param player The player to open the GUI for
     */
    void openGUI(Player player);

    /**
     * Handles click events within this GUI page
     * 
     * @param player The player who clicked
     * @param event  The click event
     */
    void handleClick(Player player, InventoryClickEvent event);

    /**
     * Gets the identifier for this GUI page type
     * 
     * @return The GUI page identifier
     */
    String getPageType();
}

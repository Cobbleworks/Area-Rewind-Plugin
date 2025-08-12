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
     * Opens this GUI page for the specified player with pagination
     * 
     * @param player The player to open the GUI for
     * @param page   The page number to open (0-indexed)
     */
    default void openGUI(Player player, int page) {
        openGUI(player);
    }

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

    /**
     * Handles pagination navigation
     * 
     * @param player The player navigating
     * @param action The pagination action (PREVIOUS or NEXT)
     */
    default void handlePaginationAction(Player player, GUIPaginationHelper.PaginationAction action) {
        // Default implementation - can be overridden by pages that support pagination
    }
}

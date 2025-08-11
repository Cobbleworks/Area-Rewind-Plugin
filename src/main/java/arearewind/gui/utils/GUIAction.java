package arearewind.gui.utils;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Functional interface for GUI actions
 */
@FunctionalInterface
public interface GUIAction {
    void execute(Player player, InventoryClickEvent event);
}

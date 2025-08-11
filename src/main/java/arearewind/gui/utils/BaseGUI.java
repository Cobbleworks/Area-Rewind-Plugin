package arearewind.gui.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Base class for all GUI implementations in AreaRewind
 * Provides common functionality and structure for GUI management
 */
public abstract class BaseGUI {
    protected final JavaPlugin plugin;
    protected final String title;
    protected final int size;
    protected Inventory inventory;
    protected final Map<Integer, GUIAction> actions = new HashMap<>();
    
    // Static tracker for all open GUIs
    private static final Map<UUID, BaseGUI> openGUIs = new HashMap<>();
    
    public BaseGUI(JavaPlugin plugin, String title, int size) {
        this.plugin = plugin;
        this.title = ChatColor.translateAlternateColorCodes('&', title);
        this.size = size;
        this.inventory = Bukkit.createInventory(null, size, this.title);
    }
    
    /**
     * Initialize the GUI content - implemented by subclasses
     */
    public abstract void initialize();
    
    /**
     * Handle clicks that aren't handled by specific slot actions
     */
    public abstract void handleClick(Player player, InventoryClickEvent event);
    
    /**
     * Open the GUI for a player
     */
    public void open(Player player) {
        initialize();
        player.openInventory(inventory);
        openGUIs.put(player.getUniqueId(), this);
    }
    
    /**
     * Close the GUI for a player
     */
    public void close(Player player) {
        openGUIs.remove(player.getUniqueId());
        player.closeInventory();
    }
    
    /**
     * Register an action for a specific slot
     */
    protected void setAction(int slot, GUIAction action) {
        actions.put(slot, action);
    }
    
    /**
     * Process a click event
     */
    public final void processClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= size) return;
        
        GUIAction action = actions.get(slot);
        if (action != null) {
            action.execute(player, event);
        } else {
            handleClick(player, event);
        }
    }
    
    /**
     * Get the GUI that a player currently has open
     */
    public static BaseGUI getOpenGUI(Player player) {
        return openGUIs.get(player.getUniqueId());
    }
    
    /**
     * Check if a player has a GUI open
     */
    public static boolean hasGUIOpen(Player player) {
        return openGUIs.containsKey(player.getUniqueId());
    }
    
    /**
     * Remove a player from the open GUIs tracker
     */
    public static void removePlayer(Player player) {
        openGUIs.remove(player.getUniqueId());
    }
    
    /**
     * Get the inventory
     */
    public Inventory getInventory() {
        return inventory;
    }
    
    /**
     * Get the title
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Refresh the GUI content
     */
    public void refresh() {
        inventory.clear();
        actions.clear();
        initialize();
    }
    
    /**
     * Check if this inventory belongs to this GUI
     */
    public boolean isThisInventory(Inventory inventory) {
        return this.inventory.equals(inventory);
    }
}

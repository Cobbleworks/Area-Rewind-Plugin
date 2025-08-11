package arearewind.gui.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Utility class for common GUI layout patterns and navigation
 */
public class GUIUtils {
    
    // Common slot positions for different GUI sizes
    public static class Slots {
        // 9-slot GUI (1 row)
        public static final int[] ROW_1 = {0, 1, 2, 3, 4, 5, 6, 7, 8};
        
        // 18-slot GUI (2 rows)
        public static final int[] ROW_2 = {9, 10, 11, 12, 13, 14, 15, 16, 17};
        
        // 27-slot GUI (3 rows)
        public static final int[] ROW_3 = {18, 19, 20, 21, 22, 23, 24, 25, 26};
        
        // 36-slot GUI (4 rows)
        public static final int[] ROW_4 = {27, 28, 29, 30, 31, 32, 33, 34, 35};
        
        // 45-slot GUI (5 rows)
        public static final int[] ROW_5 = {36, 37, 38, 39, 40, 41, 42, 43, 44};
        
        // 54-slot GUI (6 rows)
        public static final int[] ROW_6 = {45, 46, 47, 48, 49, 50, 51, 52, 53};
        
        // Border slots for different GUI sizes
        public static final int[] BORDER_27 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26};
        public static final int[] BORDER_45 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44};
        public static final int[] BORDER_54 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53};
        
        // Center slots for different GUI sizes
        public static final int CENTER_9 = 4;
        public static final int CENTER_27 = 13;
        public static final int CENTER_45 = 22;
        public static final int CENTER_54 = 22;
        
        // Navigation button positions (bottom row)
        public static final int BACK_BUTTON_27 = 18;
        public static final int CLOSE_BUTTON_27 = 26;
        public static final int REFRESH_BUTTON_27 = 22;
        
        public static final int BACK_BUTTON_45 = 36;
        public static final int CLOSE_BUTTON_45 = 44;
        public static final int REFRESH_BUTTON_45 = 40;
        
        public static final int BACK_BUTTON_54 = 45;
        public static final int CLOSE_BUTTON_54 = 53;
        public static final int REFRESH_BUTTON_54 = 49;
        
        // Pagination buttons
        public static final int PREV_PAGE_45 = 37;
        public static final int NEXT_PAGE_45 = 43;
        public static final int PAGE_INFO_45 = 40;
        
        public static final int PREV_PAGE_54 = 46;
        public static final int NEXT_PAGE_54 = 52;
        public static final int PAGE_INFO_54 = 49;
    }
    
    /**
     * Fill border slots with glass panes
     */
    public static void fillBorder(BaseGUI gui, int[] borderSlots, Material glassMaterial) {
        ItemStack glass = ItemBuilder.createFillerGlass(glassMaterial);
        for (int slot : borderSlots) {
            gui.getInventory().setItem(slot, glass);
        }
    }
    
    /**
     * Add standard navigation buttons to a GUI
     */
    public static void addNavigationButtons(BaseGUI gui, BaseGUI backGUI, boolean showRefresh) {
        int size = gui.getInventory().getSize();
        
        // Determine button positions based on GUI size
        int backSlot, closeSlot, refreshSlot;
        switch (size) {
            case 27:
                backSlot = Slots.BACK_BUTTON_27;
                closeSlot = Slots.CLOSE_BUTTON_27;
                refreshSlot = Slots.REFRESH_BUTTON_27;
                break;
            case 45:
                backSlot = Slots.BACK_BUTTON_45;
                closeSlot = Slots.CLOSE_BUTTON_45;
                refreshSlot = Slots.REFRESH_BUTTON_45;
                break;
            case 54:
                backSlot = Slots.BACK_BUTTON_54;
                closeSlot = Slots.CLOSE_BUTTON_54;
                refreshSlot = Slots.REFRESH_BUTTON_54;
                break;
            default:
                return; // Unsupported size
        }
        
        // Back button
        if (backGUI != null) {
            gui.getInventory().setItem(backSlot, ItemBuilder.createBackButton());
            gui.setAction(backSlot, (player, event) -> backGUI.open(player));
        }
        
        // Close button
        gui.getInventory().setItem(closeSlot, ItemBuilder.createCloseButton());
        gui.setAction(closeSlot, (player, event) -> gui.close(player));
        
        // Refresh button
        if (showRefresh) {
            gui.getInventory().setItem(refreshSlot, ItemBuilder.createRefreshButton());
            gui.setAction(refreshSlot, (player, event) -> gui.refresh());
        }
    }
    
    /**
     * Get content slots for a GUI (excluding border and navigation)
     */
    public static int[] getContentSlots(int size) {
        switch (size) {
            case 27:
                return new int[]{10, 11, 12, 13, 14, 15, 16};
            case 45:
                return new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
            case 54:
                return new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
            default:
                // For other sizes, return all slots except last row
                int[] slots = new int[size - 9];
                for (int i = 0; i < slots.length; i++) {
                    slots[i] = i;
                }
                return slots;
        }
    }
    
    /**
     * Calculate the appropriate GUI size for a number of items
     */
    public static int calculateGUISize(int itemCount) {
        // Add space for navigation buttons
        int totalSlots = itemCount + 9; // Reserve bottom row for navigation
        
        if (totalSlots <= 27) return 27;
        if (totalSlots <= 45) return 45;
        return 54;
    }
    
    /**
     * Convert slot position to row and column
     */
    public static int[] slotToRowCol(int slot) {
        return new int[]{slot / 9, slot % 9};
    }
    
    /**
     * Convert row and column to slot position
     */
    public static int rowColToSlot(int row, int col) {
        return row * 9 + col;
    }
    
    /**
     * Create a confirmation dialog layout
     */
    public static void setupConfirmationDialog(BaseGUI gui, Runnable onConfirm, Runnable onCancel) {
        // Fill border with red glass
        fillBorder(gui, Slots.BORDER_27, Material.RED_STAINED_GLASS_PANE);
        
        // Confirm button (green)
        gui.getInventory().setItem(11, ItemBuilder.createConfirmItem());
        gui.setAction(11, (player, event) -> {
            onConfirm.run();
            gui.close(player);
        });
        
        // Cancel button (red)
        gui.getInventory().setItem(15, ItemBuilder.createCancelItem());
        gui.setAction(15, (player, event) -> {
            onCancel.run();
            gui.close(player);
        });
        
        // Info in center
        gui.getInventory().setItem(13, ItemBuilder.createInfoItem("Confirmation", "&7Are you sure?"));
    }
}

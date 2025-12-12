package arearewind.managers.gui;

import arearewind.data.ProtectedArea;
import arearewind.managers.AreaManager;
import arearewind.managers.BackupManager;
import arearewind.managers.GUIManager;
import arearewind.managers.IntervalManager;
import arearewind.managers.PermissionManager;
import arearewind.managers.gui.GUIPaginationHelper.PaginationInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GUI page for displaying and managing protected areas
 */
public class AreasGUIPage implements IGUIPage {

    private final GUIManager guiManager;
    private final AreaManager areaManager;
    private final BackupManager backupManager;
    private final PermissionManager permissionManager;
    private final IntervalManager intervalManager;

    // Pagination constants
    private static final int ITEMS_PER_PAGE = 35; // 5 rows of 7 items (slots 0-34)
    private static final int NAVIGATION_ROW_START = 45; // Bottom row for navigation
    private static final int FIRST_PAGE = 0; // Used for resetting to the first page

    public AreasGUIPage(GUIManager guiManager, AreaManager areaManager,
            BackupManager backupManager, PermissionManager permissionManager, IntervalManager intervalManager) {
        this.guiManager = guiManager;
        this.areaManager = areaManager;
        this.backupManager = backupManager;
        this.permissionManager = permissionManager;
        this.intervalManager = intervalManager;
    }

    @Override
    public void openGUI(Player player) {
        openGUI(player, FIRST_PAGE); // Default to first page
    }

    @Override
    public void openGUI(Player player, int page) {
        if (!permissionManager.canUseGUI(player)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use the GUI!");
            return;
        }

        // Get all accessible areas for this player
        List<Map.Entry<String, ProtectedArea>> allAreas = getAllAccessibleAreas(player);

        // Calculate pagination
        PaginationInfo paginationInfo = GUIPaginationHelper.calculatePagination(
                allAreas.size(), ITEMS_PER_PAGE, page);

        // Update pagination data for the player
        GUIPaginationHelper.updatePaginationData(player.getUniqueId(),
                paginationInfo.getCurrentPage(), paginationInfo.getMaxPage(), getPageType(), null);

        // Create inventory with improved title
        String title = ChatColor.DARK_GREEN + "🌍 All Protected Areas";
        if (paginationInfo.getMaxPage() > 0) {
            title += ChatColor.GRAY + " (" + (paginationInfo.getCurrentPage() + 1) + "/" 
                    + (paginationInfo.getMaxPage() + 1) + ")";
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        // Fill info row with glass
        fillInfoRow(gui);
        
        // Fill navigation row with black glass
        fillNavigationRow(gui);

        // Add area items for current page
        int slot = 0;
        for (int i = paginationInfo.getStartIndex(); i < paginationInfo.getEndIndex(); i++) {
            Map.Entry<String, ProtectedArea> entry = allAreas.get(i);
            String areaName = entry.getKey();
            ProtectedArea area = entry.getValue();

            // Use custom icon if set, otherwise default to GRASS_BLOCK
            ItemStack item = area.getIconItem() != null ? area.getIconItem().clone()
                    : new ItemStack(Material.GRASS_BLOCK);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + "✦ " + areaName);

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.WHITE + "Owner: " + ChatColor.GOLD + Bukkit.getOfflinePlayer(area.getOwner()).getName());
            lore.add(ChatColor.WHITE + "Size: " + ChatColor.AQUA + String.format("%,d", area.getSize()) + ChatColor.GRAY + " blocks");
            lore.add(ChatColor.WHITE + "Backups: " + ChatColor.YELLOW + backupManager.getBackupHistory(areaName).size());
            
            String permLevel = permissionManager.getPermissionLevelString(player, area);
            ChatColor permColor = permLevel.equals("Owner") ? ChatColor.RED : 
                                  permLevel.equals("Trusted") ? ChatColor.GREEN : ChatColor.GRAY;
            lore.add(ChatColor.WHITE + "Your Role: " + permColor + permLevel);

            // Add interval information
            lore.add("");
            var intervalConfig = intervalManager.getIntervalConfig(areaName);
            if (intervalConfig != null) {
                lore.add(ChatColor.LIGHT_PURPLE + "⏰ Auto-Restore: " + ChatColor.GREEN + "Every " + intervalConfig.minutes + "m");
            } else {
                lore.add(ChatColor.LIGHT_PURPLE + "⏰ Auto-Restore: " + ChatColor.DARK_GRAY + "Inactive");
            }

            lore.add("");
            lore.add(ChatColor.GRAY + "━━━━━━━━━━━━━━━━");
            lore.add(ChatColor.GREEN + "▶ Left-click: " + ChatColor.WHITE + "View Backups");
            if (permissionManager.canModifyBoundaries(player, area)) {
                lore.add(ChatColor.LIGHT_PURPLE + "▶ Middle-click: " + ChatColor.WHITE + "Set Icon");
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
            gui.setItem(slot++, item);
        }

        // Add pagination navigation if needed
        if (paginationInfo.getMaxPage() > 0) {
            GUIPaginationHelper.addPaginationButtons(gui, paginationInfo,
                    NAVIGATION_ROW_START, NAVIGATION_ROW_START + 8, -1);
        }

        // Add other navigation items
        addNavigationItems(gui, paginationInfo);

        player.openInventory(gui);
        guiManager.registerOpenGUI(player, getPageType());
    }

    private void fillInfoRow(Inventory gui) {
        ItemStack filler = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(" ");
        filler.setItemMeta(meta);
        for (int i = 35; i < 45; i++) {
            gui.setItem(i, filler.clone());
        }
    }

    private void fillNavigationRow(Inventory gui) {
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(" ");
        filler.setItemMeta(meta);
        for (int i = NAVIGATION_ROW_START; i < 54; i++) {
            gui.setItem(i, filler.clone());
        }
    }

    @Override
    public void handleClick(Player player, InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta())
            return;

        String displayName = item.getItemMeta().getDisplayName();
        
        // Ignore filler glass panes
        if (displayName.equals(" ")) {
            return;
        }
        
        String strippedName = ChatColor.stripColor(displayName);

        // Handle pagination navigation
        GUIPaginationHelper.PaginationAction paginationAction = GUIPaginationHelper.checkPaginationClick(item);
        if (paginationAction != GUIPaginationHelper.PaginationAction.NONE) {
            handlePaginationAction(player, paginationAction);
            return;
        }

        // Handle navigation buttons
        if (strippedName.contains("Close")) {
            player.closeInventory();
            return;
        } else if (strippedName.contains("Refresh")) {
            player.closeInventory();
            // Get current page from pagination data
            GUIPaginationHelper.PaginationData paginationData = GUIPaginationHelper
                    .getPaginationData(player.getUniqueId(), getPageType(), null);
            openGUI(player, paginationData.getCurrentPage());
            return;
        } else if (strippedName.contains("Settings")) {
            player.closeInventory();
            guiManager.openSettingsGUI(player);
            return;
        } else if (strippedName.contains("My Areas")) {
            // Switch to My Areas page
            player.closeInventory();
            guiManager.openMyAreasGUI(player);
            return;
        }

        // Handle area selection - remove the "✦ " prefix if present
        String areaName = strippedName.startsWith("✦ ") ? strippedName.substring(2) : strippedName;
        ProtectedArea area = areaManager.getArea(areaName);
        if (area == null)
            return;

        if (event.isLeftClick()) {
            player.closeInventory();
            guiManager.openBackupsGUI(player, areaName);
        } else if (event.getClick().name().contains("MIDDLE")) {
            // Middle click to set area icon
            if (permissionManager.canModifyBoundaries(player, area)) {
                player.closeInventory();
                guiManager.openMaterialSelector(player, "area", areaName, null);
            } else {
                player.sendMessage(ChatColor.RED + "You don't have permission to modify this area!");
            }
        }
    }

    @Override
    public String getPageType() {
        return "all-areas";
    }

    @Override
    public void handlePaginationAction(Player player, GUIPaginationHelper.PaginationAction action) {
        GUIPaginationHelper.PaginationData paginationData = GUIPaginationHelper.getPaginationData(player.getUniqueId(),
                getPageType(), null);

        int newPage = paginationData.getCurrentPage();

        switch (action) {
            case PREVIOUS:
                if (newPage > 0) {
                    newPage--;
                }
                break;
            case NEXT:
                if (newPage < paginationData.getMaxPage()) {
                    newPage++;
                }
                break;
            default:
                return;
        }

        player.closeInventory();
        openGUI(player, newPage);
    }

    private void addNavigationItems(Inventory gui, PaginationInfo paginationInfo) {
        // Fixed slot positions in navigation row
        int refreshSlot = 46;
        int myAreasSlot = 47;
        int closeSlot = 49;
        int settingsSlot = 52;

        // Refresh button
        ItemStack refreshItem = new ItemStack(Material.EMERALD);
        ItemMeta refreshMeta = refreshItem.getItemMeta();
        refreshMeta.setDisplayName(ChatColor.GREEN + "🔄 Refresh");
        List<String> refreshLore = new ArrayList<>();
        refreshLore.add("");
        refreshLore.add(ChatColor.GRAY + "Reload the area list");
        refreshMeta.setLore(refreshLore);
        refreshItem.setItemMeta(refreshMeta);
        gui.setItem(refreshSlot, refreshItem);

        // My Areas navigation button
        ItemStack myAreasItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta myAreasMeta = myAreasItem.getItemMeta();
        myAreasMeta.setDisplayName(ChatColor.BLUE + "👤 My Areas");
        List<String> myAreasLore = new ArrayList<>();
        myAreasLore.add("");
        myAreasLore.add(ChatColor.GRAY + "Show only areas");
        myAreasLore.add(ChatColor.GRAY + "you own.");
        myAreasLore.add("");
        myAreasLore.add(ChatColor.YELLOW + "▶ Click to switch");
        myAreasMeta.setLore(myAreasLore);
        myAreasItem.setItemMeta(myAreasMeta);
        gui.setItem(myAreasSlot, myAreasItem);

        // Close button
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName(ChatColor.RED + "✖ Close");
        closeItem.setItemMeta(closeMeta);
        gui.setItem(closeSlot, closeItem);

        // Settings button
        ItemStack settingsItem = new ItemStack(Material.COMPARATOR);
        ItemMeta settingsMeta = settingsItem.getItemMeta();
        settingsMeta.setDisplayName(ChatColor.YELLOW + "⚙ Settings");
        List<String> settingsLore = new ArrayList<>();
        settingsLore.add("");
        settingsLore.add(ChatColor.GRAY + "Configure your");
        settingsLore.add(ChatColor.GRAY + "personal preferences.");
        settingsLore.add("");
        settingsLore.add(ChatColor.YELLOW + "▶ Click to open");
        settingsMeta.setLore(settingsLore);
        settingsItem.setItemMeta(settingsMeta);
        gui.setItem(settingsSlot, settingsItem);
    }

    /**
     * Get all accessible areas for the player (this is now the default behavior)
     */
    private List<Map.Entry<String, ProtectedArea>> getAllAccessibleAreas(Player player) {
        List<Map.Entry<String, ProtectedArea>> accessibleAreas = new ArrayList<>();

        for (Map.Entry<String, ProtectedArea> entry : areaManager.getProtectedAreas().entrySet()) {
            ProtectedArea area = entry.getValue();

            // Check if player has permission to see this area
            if (permissionManager.hasAreaPermission(player, area)) {
                accessibleAreas.add(entry);
            }
        }

        // Sort by creation date, newest first
        accessibleAreas.sort((o1, o2) -> Long.compare(
                o2.getValue().getCreationDate(),
                o1.getValue().getCreationDate()));

        return accessibleAreas;
    }
}

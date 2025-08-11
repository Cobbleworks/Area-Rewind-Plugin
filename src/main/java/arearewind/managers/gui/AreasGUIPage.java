package arearewind.managers.gui;

import arearewind.data.ProtectedArea;
import arearewind.managers.AreaManager;
import arearewind.managers.BackupManager;
import arearewind.managers.GUIManager;
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

    // Pagination constants
    private static final int ITEMS_PER_PAGE = 35; // 5 rows of 7 items (slots 0-34)
    private static final int NAVIGATION_ROW_START = 45; // Bottom row for navigation

    public AreasGUIPage(GUIManager guiManager, AreaManager areaManager,
            BackupManager backupManager, PermissionManager permissionManager) {
        this.guiManager = guiManager;
        this.areaManager = areaManager;
        this.backupManager = backupManager;
        this.permissionManager = permissionManager;
    }

    @Override
    public void openGUI(Player player) {
        openGUI(player, 0); // Default to first page
    }

    @Override
    public void openGUI(Player player, int page) {
        if (!permissionManager.canUseGUI(player)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use the GUI!");
            return;
        }

        // Get all areas the player can access
        List<Map.Entry<String, ProtectedArea>> accessibleAreas = new ArrayList<>();
        for (Map.Entry<String, ProtectedArea> entry : areaManager.getProtectedAreas().entrySet()) {
            if (permissionManager.hasAreaPermission(player, entry.getValue())) {
                accessibleAreas.add(entry);
            }
        }

        // Calculate pagination
        PaginationInfo paginationInfo = GUIPaginationHelper.calculatePagination(
                accessibleAreas.size(), ITEMS_PER_PAGE, page);

        // Update pagination data for the player
        GUIPaginationHelper.updatePaginationData(player.getUniqueId(),
                paginationInfo.getCurrentPage(), paginationInfo.getMaxPage(), getPageType(), null);

        // Create inventory
        String title = ChatColor.DARK_GREEN + "Protected Areas" +
                (paginationInfo.getMaxPage() > 0
                        ? " (" + (paginationInfo.getCurrentPage() + 1) + "/" + (paginationInfo.getMaxPage() + 1) + ")"
                        : "");
        Inventory gui = Bukkit.createInventory(null, 54, title);

        // Add area items for current page
        int slot = 0;
        for (int i = paginationInfo.getStartIndex(); i < paginationInfo.getEndIndex(); i++) {
            Map.Entry<String, ProtectedArea> entry = accessibleAreas.get(i);
            String areaName = entry.getKey();
            ProtectedArea area = entry.getValue();

            ItemStack item = new ItemStack(Material.GRASS_BLOCK);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + areaName);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Owner: " + Bukkit.getOfflinePlayer(area.getOwner()).getName());
            lore.add(ChatColor.GRAY + "Size: " + area.getSize() + " blocks");
            lore.add(ChatColor.GRAY + "Backups: " + backupManager.getBackupHistory(areaName).size());
            lore.add(ChatColor.GRAY + "Permission: " + permissionManager.getPermissionLevelString(player, area));
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click: Manage Area & Backups");

            meta.setLore(lore);
            item.setItemMeta(meta);
            gui.setItem(slot++, item);
        }

        // Add pagination navigation if needed
        if (paginationInfo.getMaxPage() > 0) {
            GUIPaginationHelper.addPaginationButtons(gui, paginationInfo,
                    NAVIGATION_ROW_START, NAVIGATION_ROW_START + 8, NAVIGATION_ROW_START + 4);
        }

        // Add other navigation items
        addNavigationItems(gui, paginationInfo);

        player.openInventory(gui);
        guiManager.registerOpenGUI(player, getPageType());
    }

    @Override
    public void handleClick(Player player, InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta())
            return;

        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        // Handle pagination navigation
        GUIPaginationHelper.PaginationAction paginationAction = GUIPaginationHelper.checkPaginationClick(item);
        if (paginationAction != GUIPaginationHelper.PaginationAction.NONE) {
            handlePaginationAction(player, paginationAction);
            return;
        }

        // Handle navigation buttons
        if (displayName.equals("Close")) {
            player.closeInventory();
            return;
        } else if (displayName.equals("Refresh")) {
            player.closeInventory();
            // Get current page from pagination data
            GUIPaginationHelper.PaginationData paginationData = GUIPaginationHelper
                    .getPaginationData(player.getUniqueId(), getPageType(), null);
            openGUI(player, paginationData.getCurrentPage());
            return;
        } else if (displayName.equals("Settings")) {
            player.closeInventory();
            guiManager.openSettingsGUI(player);
            return;
        }

        // Handle area selection - only left click to open backup management
        ProtectedArea area = areaManager.getArea(displayName);
        if (area == null)
            return;

        if (event.isLeftClick()) {
            player.closeInventory();
            guiManager.openBackupsGUI(player, displayName);
        }
    }

    @Override
    public String getPageType() {
        return "areas";
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
        // Adjust slot positions based on whether pagination is present
        int refreshSlot = paginationInfo.getMaxPage() > 0 ? NAVIGATION_ROW_START + 1 : NAVIGATION_ROW_START;
        int closeSlot = paginationInfo.getMaxPage() > 0 ? NAVIGATION_ROW_START + 3 : NAVIGATION_ROW_START + 4;
        int settingsSlot = paginationInfo.getMaxPage() > 0 ? NAVIGATION_ROW_START + 7 : NAVIGATION_ROW_START + 8;

        ItemStack refreshItem = new ItemStack(Material.EMERALD);
        ItemMeta refreshMeta = refreshItem.getItemMeta();
        refreshMeta.setDisplayName(ChatColor.GREEN + "Refresh");
        refreshItem.setItemMeta(refreshMeta);
        gui.setItem(refreshSlot, refreshItem);

        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName(ChatColor.RED + "Close");
        closeItem.setItemMeta(closeMeta);
        gui.setItem(closeSlot, closeItem);

        ItemStack settingsItem = new ItemStack(Material.COMPARATOR);
        ItemMeta settingsMeta = settingsItem.getItemMeta();
        settingsMeta.setDisplayName(ChatColor.YELLOW + "Settings");
        settingsItem.setItemMeta(settingsMeta);
        gui.setItem(settingsSlot, settingsItem);
    }
}

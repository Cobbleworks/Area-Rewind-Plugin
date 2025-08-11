package arearewind.managers.gui;

import arearewind.data.AreaBackup;
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

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * GUI page for managing backups and area information
 */
public class BackupsGUIPage implements IGUIPage {

    private final GUIManager guiManager;
    private final AreaManager areaManager;
    private final BackupManager backupManager;
    private final PermissionManager permissionManager;

    // Pagination constants
    private static final int ITEMS_PER_PAGE = 28; // 4 rows of 7 items (slots 0-27)
    private static final int INFO_ROW_START = 35; // Row for area info (35-44)
    private static final int NAVIGATION_ROW_START = 45; // Bottom row for navigation

    public BackupsGUIPage(GUIManager guiManager, AreaManager areaManager,
            BackupManager backupManager, PermissionManager permissionManager) {
        this.guiManager = guiManager;
        this.areaManager = areaManager;
        this.backupManager = backupManager;
        this.permissionManager = permissionManager;
    }

    @Override
    public void openGUI(Player player) {
        // This will be called through openBackupsGUI(player, areaName)
        throw new UnsupportedOperationException("Use openBackupsGUI(player, areaName) instead");
    }

    public void openBackupsGUI(Player player, String areaName) {
        openBackupsGUI(player, areaName, 0); // Default to first page
    }

    public void openBackupsGUI(Player player, String areaName, int page) {
        ProtectedArea area = areaManager.getArea(areaName);
        if (area == null) {
            player.sendMessage(ChatColor.RED + "Area not found!");
            return;
        }

        if (!permissionManager.canViewBackupHistory(player, area)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to view backups for this area!");
            return;
        }

        List<AreaBackup> backups = backupManager.getBackupHistory(areaName);

        // Calculate pagination
        PaginationInfo paginationInfo = GUIPaginationHelper.calculatePagination(
                backups.size(), ITEMS_PER_PAGE, page);

        // Update pagination data for the player
        GUIPaginationHelper.updatePaginationData(player.getUniqueId(),
                paginationInfo.getCurrentPage(), paginationInfo.getMaxPage(), getPageType(), areaName);

        // Create inventory with page info in title
        String title = ChatColor.DARK_BLUE + "Area Management: " + areaName;
        if (paginationInfo.getMaxPage() > 0) {
            title += " (" + (paginationInfo.getCurrentPage() + 1) + "/" + (paginationInfo.getMaxPage() + 1) + ")";
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        // Add backup items for current page
        int slot = 0;
        for (int i = paginationInfo.getStartIndex(); i < paginationInfo.getEndIndex(); i++) {
            AreaBackup backup = backups.get(i);

            // Use custom icon if set, otherwise default to CHEST
            Material iconMaterial = backup.getIcon() != null ? backup.getIcon() : Material.CHEST;
            ItemStack item = new ItemStack(iconMaterial);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + "Backup #" + i);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Created: " + backup.getTimestamp().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            lore.add(ChatColor.GRAY + "Blocks: " + backup.getBlocks().size());

            int undoPointer = backupManager.getUndoPointer(areaName);
            if (i == undoPointer) {
                lore.add(ChatColor.YELLOW + "← Current State");
            }

            lore.add("");
            if (permissionManager.canRestoreBackup(player, area)) {
                lore.add(ChatColor.YELLOW + "Left Click: Restore");
            }
            lore.add(ChatColor.YELLOW + "Right Click: Preview");
            lore.add(ChatColor.YELLOW + "Shift+Click: Compare with current");

            meta.setLore(lore);
            item.setItemMeta(meta);
            gui.setItem(slot++, item);
        }

        // Add pagination navigation if needed
        if (paginationInfo.getMaxPage() > 0) {
            GUIPaginationHelper.addPaginationButtons(gui, paginationInfo,
                    NAVIGATION_ROW_START, NAVIGATION_ROW_START + 8, -1); // No info button for backups page
        }

        // Add area info in slots 35-44 (second row from bottom)
        addAreaInfoItems(gui, area, areaName);

        // Add control items in bottom row (45-53)
        addControlItems(gui, areaName, area, player, paginationInfo);

        player.openInventory(gui);
        guiManager.registerOpenGUI(player, getPageType() + ":" + areaName);
    }

    @Override
    public void handleClick(Player player, InventoryClickEvent event) {
        String guiData = guiManager.getOpenGUIType(player);
        if (!guiData.startsWith("backups:"))
            return;

        String areaName = guiData.substring(8);
        handleBackupsGUIClick(player, event, areaName);
    }

    @Override
    public String getPageType() {
        return "backups";
    }

    @Override
    public void handlePaginationAction(Player player, GUIPaginationHelper.PaginationAction action) {
        String guiData = guiManager.getOpenGUIType(player);
        if (!guiData.startsWith("backups:"))
            return;

        String areaName = guiData.substring(8);

        GUIPaginationHelper.PaginationData paginationData = GUIPaginationHelper.getPaginationData(player.getUniqueId(),
                getPageType(), areaName);

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
        openBackupsGUI(player, areaName, newPage);
    }

    private void handleBackupsGUIClick(Player player, InventoryClickEvent event, String areaName) {
        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta())
            return;

        String displayName = item.getItemMeta().getDisplayName();

        // Handle pagination navigation
        GUIPaginationHelper.PaginationAction paginationAction = GUIPaginationHelper.checkPaginationClick(item);
        if (paginationAction != GUIPaginationHelper.PaginationAction.NONE) {
            handlePaginationAction(player, paginationAction);
            return;
        }

        // Handle navigation and control buttons
        if (displayName.contains("Back to Areas")) {
            player.closeInventory();
            guiManager.openAreasGUI(player);
            return;
        } else if (displayName.contains("Create Backup")) {
            player.closeInventory();
            player.performCommand("rewind backup " + areaName);
            return;
        } else if (displayName.contains("Undo")) {
            player.closeInventory();
            player.performCommand("rewind undo " + areaName);
            return;
        } else if (displayName.contains("Redo")) {
            player.closeInventory();
            player.performCommand("rewind redo " + areaName);
            return;
        } else if (displayName.contains("Teleport to Area")) {
            player.closeInventory();
            player.performCommand("rewind teleport " + areaName);
            return;
        } else if (displayName.contains("Preview Area")) {
            player.closeInventory();
            player.performCommand("rewind preview " + areaName);
            return;
        } else if (displayName.contains("Area Settings")) {
            player.closeInventory();
            guiManager.openAreaSettingsGUI(player, areaName);
            return;
        } else if (displayName.contains("Permissions")) {
            player.closeInventory();
            guiManager.openAreaSettingsGUI(player, areaName);
            return;
        } else if (displayName.contains("Area Management")) {
            player.closeInventory();
            guiManager.openAreaSettingsGUI(player, areaName);
            return;
        } else if (displayName.contains("Permissions")) {
            player.closeInventory();
            player.performCommand("rewind permissions " + areaName);
            return;
        } else if (displayName.contains("Area Management")) {
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Area Management Commands:");
            player.sendMessage(ChatColor.GREEN + "/rewind expand " + areaName + " <direction> <amount>");
            player.sendMessage(ChatColor.GREEN + "/rewind contract " + areaName + " <direction> <amount>");
            player.sendMessage(ChatColor.GREEN + "/rewind delete " + areaName);
            player.sendMessage(ChatColor.GREEN + "/rewind rename " + areaName + " <new_name>");
            return;
        }

        // Handle backup selection
        if (displayName.contains("Backup #")) {
            String backupId = displayName.replaceAll(".*#", "");

            if (event.isShiftClick()) {
                player.closeInventory();
                // Compare selected backup with current state
                int currentStateId = backupManager.getUndoPointer(areaName);
                player.performCommand("rewind diff " + areaName + " " + backupId + " " + currentStateId);
            } else if (event.isLeftClick()) {
                player.closeInventory();
                player.performCommand("rewind restore " + areaName + " " + backupId);
            } else if (event.isRightClick()) {
                player.closeInventory();
                player.performCommand("rewind preview " + areaName + " " + backupId);
            }
        }
    }

    private void addAreaInfoItems(Inventory gui, ProtectedArea area, String areaName) {
        // Area Information
        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(ChatColor.GREEN + "Area Information");
        List<String> infoLore = new ArrayList<>();
        infoLore.add(ChatColor.GRAY + "Name: " + areaName);
        infoLore.add(ChatColor.GRAY + "Owner: " + Bukkit.getOfflinePlayer(area.getOwner()).getName());
        infoLore.add(ChatColor.GRAY + "World: " + area.getPos1().getWorld().getName());
        infoLore.add(ChatColor.GRAY + "Size: " + area.getSize() + " blocks");
        infoLore.add("");
        infoLore.add(ChatColor.YELLOW + "For detailed settings, click Area Settings");
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        gui.setItem(44, infoItem);

        // Area Settings Button
        ItemStack settingsItem = new ItemStack(Material.COMPARATOR);
        ItemMeta settingsMeta = settingsItem.getItemMeta();
        settingsMeta.setDisplayName(ChatColor.GOLD + "Area Settings");
        List<String> settingsLore = new ArrayList<>();
        settingsLore.add(ChatColor.GRAY + "Manage area settings and permissions");
        settingsLore.add(ChatColor.GRAY + "View backup statistics");
        settingsLore.add(ChatColor.GRAY + "Area management options");
        settingsLore.add("");
        settingsLore.add(ChatColor.YELLOW + "Click to open settings");
        settingsMeta.setLore(settingsLore);
        settingsItem.setItemMeta(settingsMeta);
        gui.setItem(36, settingsItem);
    }

    private void addControlItems(Inventory gui, String areaName, ProtectedArea area, Player player,
            PaginationInfo paginationInfo) {
        // Adjust slot positions based on whether pagination is present
        int createSlot = paginationInfo.getMaxPage() > 0 ? NAVIGATION_ROW_START + 1 : NAVIGATION_ROW_START;
        int undoSlot = paginationInfo.getMaxPage() > 0 ? NAVIGATION_ROW_START + 2 : NAVIGATION_ROW_START + 1;
        int redoSlot = paginationInfo.getMaxPage() > 0 ? NAVIGATION_ROW_START + 3 : NAVIGATION_ROW_START + 2;
        int teleportSlot = paginationInfo.getMaxPage() > 0 ? NAVIGATION_ROW_START + 4 : NAVIGATION_ROW_START + 3;
        int previewSlot = paginationInfo.getMaxPage() > 0 ? NAVIGATION_ROW_START + 5 : NAVIGATION_ROW_START + 4;
        int backSlot = paginationInfo.getMaxPage() > 0 ? NAVIGATION_ROW_START + 7 : NAVIGATION_ROW_START + 8;

        // Create Backup
        if (permissionManager.canCreateBackup(player, area)) {
            ItemStack createItem = new ItemStack(Material.CRAFTING_TABLE);
            ItemMeta createMeta = createItem.getItemMeta();
            createMeta.setDisplayName(ChatColor.GREEN + "Create Backup");
            createItem.setItemMeta(createMeta);
            gui.setItem(createSlot, createItem);
        }

        // Undo/Redo
        if (permissionManager.canUndoRedo(player, area) && backupManager.canUndo(areaName)) {
            ItemStack undoItem = new ItemStack(Material.ARROW);
            ItemMeta undoMeta = undoItem.getItemMeta();
            undoMeta.setDisplayName(ChatColor.YELLOW + "Undo Last Change");
            undoItem.setItemMeta(undoMeta);
            gui.setItem(undoSlot, undoItem);
        }

        if (permissionManager.canUndoRedo(player, area) && backupManager.canRedo(areaName)) {
            ItemStack redoItem = new ItemStack(Material.ARROW);
            ItemMeta redoMeta = redoItem.getItemMeta();
            redoMeta.setDisplayName(ChatColor.YELLOW + "Redo Last Undo");
            redoItem.setItemMeta(redoMeta);
            gui.setItem(redoSlot, redoItem);
        }

        // Teleport
        if (permissionManager.hasAreaPermission(player, area)) {
            ItemStack teleportItem = new ItemStack(Material.ENDER_PEARL);
            ItemMeta teleportMeta = teleportItem.getItemMeta();
            teleportMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Teleport to Area");
            teleportItem.setItemMeta(teleportMeta);
            gui.setItem(teleportSlot, teleportItem);
        }

        // Preview
        if (permissionManager.canVisualize(player, area)) {
            ItemStack previewItem = new ItemStack(Material.SPYGLASS);
            ItemMeta previewMeta = previewItem.getItemMeta();
            previewMeta.setDisplayName(ChatColor.AQUA + "Preview Area");
            previewItem.setItemMeta(previewMeta);
            gui.setItem(previewSlot, previewItem);
        }

        // Back button
        ItemStack backItem = new ItemStack(Material.SPECTRAL_ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.GRAY + "Back to Areas");
        backItem.setItemMeta(backMeta);
        gui.setItem(backSlot, backItem);
    }
}

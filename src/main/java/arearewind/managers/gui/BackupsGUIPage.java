package arearewind.managers.gui;

import arearewind.data.AreaBackup;
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
    private final IntervalManager intervalManager;

    // Pagination constants
    private static final int ITEMS_PER_PAGE = 28; // 4 rows of 7 items (slots 0-27)
    private static final int NAVIGATION_ROW_START = 45; // Bottom row for navigation

    public BackupsGUIPage(GUIManager guiManager, AreaManager areaManager,
            BackupManager backupManager, PermissionManager permissionManager, IntervalManager intervalManager) {
        this.guiManager = guiManager;
        this.areaManager = areaManager;
        this.backupManager = backupManager;
        this.permissionManager = permissionManager;
        this.intervalManager = intervalManager;
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

        // Create inventory with improved title
        String title = ChatColor.DARK_BLUE + "📦 Backups: " + ChatColor.WHITE + areaName;
        if (paginationInfo.getMaxPage() > 0) {
            title += ChatColor.GRAY + " (" + (paginationInfo.getCurrentPage() + 1) + "/" + (paginationInfo.getMaxPage() + 1) + ")";
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        // Fill info row with glass
        fillInfoRow(gui);
        
        // Fill navigation row with black glass
        fillNavigationRow(gui);

        // Add backup items for current page
        int slot = 0;
        int currentPointer = backupManager.getUndoPointer(areaName);
        for (int i = paginationInfo.getStartIndex(); i < paginationInfo.getEndIndex(); i++) {
            AreaBackup backup = backups.get(i);

            // Use custom icon if set, otherwise default to CHEST
            ItemStack item = backup.getIconItem() != null ? backup.getIconItem().clone()
                    : new ItemStack(Material.CHEST);
            ItemMeta meta = item.getItemMeta();
            
            // Highlight current state backup
            boolean isCurrent = (currentPointer == i);
            String prefix = isCurrent ? ChatColor.GREEN + "▶ " : ChatColor.AQUA + "";
            meta.setDisplayName(prefix + "Backup #" + i);

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.WHITE + "Created: " + ChatColor.YELLOW + backup.getTimestamp().format(
                    DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss")));
            lore.add(ChatColor.WHITE + "Blocks: " + ChatColor.AQUA + String.format("%,d", backup.getBlocksNonAirOnly().size()));

            // Check if this backup is used for auto-restore
            var intervalConfig = intervalManager.getIntervalConfig(areaName);
            if (intervalConfig != null && intervalConfig.backupId == i) {
                lore.add(ChatColor.LIGHT_PURPLE + "⏰ Auto-Restore: " + ChatColor.GREEN + "Every " + intervalConfig.minutes + "m");
            }

            // Show "← Current State" on the backup that represents the current state
            if (isCurrent) {
                lore.add("");
                lore.add(ChatColor.GREEN + "★ Current State");
            }

            lore.add("");
            lore.add(ChatColor.GRAY + "━━━━━━━━━━━━━━━━");
            if (permissionManager.canRestoreBackup(player, area)) {
                lore.add(ChatColor.GREEN + "▶ Left-click: " + ChatColor.WHITE + "Restore");
            }
            lore.add(ChatColor.YELLOW + "▶ Shift-click: " + ChatColor.WHITE + "Compare");
            lore.add(ChatColor.RED + "▶ Shift+Right: " + ChatColor.WHITE + "Delete");
            if (permissionManager.canModifyBoundaries(player, area)) {
                lore.add(ChatColor.LIGHT_PURPLE + "▶ Middle-click: " + ChatColor.WHITE + "Set Icon");
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
            gui.setItem(slot++, item);
        }

        // Add info item in info row
        addInfoItem(gui, areaName, area, backups, paginationInfo);

        // Add pagination navigation if needed
        if (paginationInfo.getMaxPage() > 0) {
            GUIPaginationHelper.addPaginationButtons(gui, paginationInfo,
                    NAVIGATION_ROW_START, NAVIGATION_ROW_START + 8, -1);
        }

        // Add control items in bottom row (45-53)
        addControlItems(gui, areaName, area, player, paginationInfo);

        player.openInventory(gui);
        guiManager.registerOpenGUI(player, getPageType() + ":" + areaName);
    }

    private void fillInfoRow(Inventory gui) {
        ItemStack filler = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(" ");
        filler.setItemMeta(meta);
        for (int i = 28; i < 45; i++) {
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

    private void addInfoItem(Inventory gui, String areaName, ProtectedArea area, List<AreaBackup> backups, PaginationInfo paginationInfo) {
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(ChatColor.AQUA + "📊 Backup Overview");
        List<String> infoLore = new ArrayList<>();
        infoLore.add("");
        infoLore.add(ChatColor.WHITE + "Area: " + ChatColor.GREEN + areaName);
        infoLore.add(ChatColor.WHITE + "Owner: " + ChatColor.GOLD + Bukkit.getOfflinePlayer(area.getOwner()).getName());
        infoLore.add(ChatColor.WHITE + "Total Backups: " + ChatColor.YELLOW + backups.size());
        
        int currentPointer = backupManager.getUndoPointer(areaName);
        infoLore.add(ChatColor.WHITE + "Current Position: " + ChatColor.AQUA + "#" + currentPointer);
        
        if (paginationInfo.getMaxPage() > 0) {
            infoLore.add("");
            infoLore.add(ChatColor.GRAY + "Showing: " + 
                    (paginationInfo.getStartIndex() + 1) + "-" + paginationInfo.getEndIndex() +
                    " of " + backups.size());
        }
        
        // Auto-restore status
        infoLore.add("");
        var intervalConfig = intervalManager.getIntervalConfig(areaName);
        if (intervalConfig != null) {
            infoLore.add(ChatColor.LIGHT_PURPLE + "⏰ Auto-Restore: " + ChatColor.GREEN + "Active");
        } else {
            infoLore.add(ChatColor.LIGHT_PURPLE + "⏰ Auto-Restore: " + ChatColor.DARK_GRAY + "Inactive");
        }
        
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        gui.setItem(36, infoItem);
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

        // Handle navigation and control buttons
        if (displayName.contains("Back to My Areas")) {
            player.closeInventory();
            guiManager.openMyAreasGUI(player);
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
        } else if (displayName.contains("Auto-Restore Settings")) {
            player.closeInventory();

            var intervalConfig = intervalManager.getIntervalConfig(areaName);
            if (intervalConfig != null) {
                if (event.isLeftClick()) {
                    // Disable auto-restore
                    intervalManager.clearInterval(areaName);
                    player.sendMessage(ChatColor.GREEN + "Auto-restore disabled for " + areaName);
                } else if (event.isRightClick()) {
                    // Show current settings and how to change them
                    player.sendMessage(ChatColor.AQUA + "Current auto-restore settings for " + areaName + ":");
                    player.sendMessage(ChatColor.GRAY + "Interval: " + intervalConfig.minutes + " minutes");
                    player.sendMessage(ChatColor.GRAY + "Backup: #" + intervalConfig.backupId);
                    player.sendMessage(ChatColor.YELLOW + "To change settings: /rewind interval set " + areaName
                            + " <minutes> <backup_id>");
                    player.sendMessage(ChatColor.YELLOW + "To disable: /rewind interval remove " + areaName);
                }
            } else {
                // No interval set, show help
                var backups = backupManager.getBackupHistory(areaName);
                if (backups.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "No backups available! Create a backup first.");
                } else {
                    player.sendMessage(ChatColor.YELLOW + "Set up auto-restore for " + areaName + ":");
                    player.sendMessage(
                            ChatColor.GRAY + "Usage: /rewind interval set " + areaName + " <minutes> <backup_id>");
                    player.sendMessage(ChatColor.GRAY + "Available backups: 0-" + (backups.size() - 1));
                    player.sendMessage(ChatColor.GRAY + "Example: /rewind interval set " + areaName + " 30 0");
                }
            }
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

            if (event.getClick().isShiftClick() && event.isRightClick()) {
                // Shift + Right Click: Delete backup
                player.closeInventory();
                try {
                    int backupIndex = Integer.parseInt(backupId);
                    ProtectedArea area = areaManager.getArea(areaName);

                    if (area != null && permissionManager.canCreateBackup(player, area)) {
                        boolean success = backupManager.deleteBackup(areaName, backupIndex);
                        if (success) {
                            player.sendMessage(ChatColor.GREEN + "Backup #" + backupIndex + " deleted for area '"
                                    + areaName + "'!");
                        } else {
                            player.sendMessage(ChatColor.RED + "Failed to delete backup #" + backupIndex + "!");
                        }
                    } else {
                        player.sendMessage(
                                ChatColor.RED + "You don't have permission to delete backups for this area!");
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid backup ID!");
                }
            } else if (event.getClick().isShiftClick()) {
                // Shift + Left Click: Compare with current
                player.closeInventory();
                // Get current backup ID (the one that was last restored)
                int currentBackupId = backupManager.canUndo(areaName) ? backupManager.getUndoPointer(areaName) : -1;

                if (currentBackupId != -1) {
                    // Compare the selected backup with the current backup
                    player.performCommand("rewind diff " + areaName + " " + backupId + " " + currentBackupId);
                } else {
                    // If no current backup state, compare with live area state
                    player.performCommand("rewind diff " + areaName + " " + backupId + " current");
                }
            } else if (event.isLeftClick()) {
                player.closeInventory();
                player.performCommand("rewind restore " + areaName + " " + backupId);
            } else if (event.getClick().name().contains("MIDDLE")) {
                // Middle click to set icon - pass the actual index instead of extracting from
                // display name
                ProtectedArea area = areaManager.getArea(areaName);
                if (area != null && permissionManager.canModifyBoundaries(player, area)) {
                    player.closeInventory();
                    // Instead of extracting from display name, we need to find which backup was
                    // clicked
                    // based on the slot and pagination
                    GUIPaginationHelper.PaginationData paginationData = GUIPaginationHelper.getPaginationData(
                            player.getUniqueId(), getPageType(), areaName);

                    int clickedSlot = event.getSlot();
                    if (clickedSlot >= 0 && clickedSlot < ITEMS_PER_PAGE) {
                        int backupIndex = paginationData.getCurrentPage() * ITEMS_PER_PAGE + clickedSlot;
                        List<AreaBackup> allBackups = backupManager.getBackupHistory(areaName);
                        if (backupIndex < allBackups.size()) {
                            guiManager.openMaterialSelector(player, "backup", areaName, String.valueOf(backupIndex));
                        }
                    }
                }
            }
        }
    }

    private void addAreaSettingsButton(Inventory gui, ProtectedArea area, String areaName, int slot) {
        // Area Settings Button
        ItemStack settingsItem = new ItemStack(Material.COMPARATOR);
        ItemMeta settingsMeta = settingsItem.getItemMeta();
        settingsMeta.setDisplayName(ChatColor.GOLD + "⚙ Area Settings");
        List<String> settingsLore = new ArrayList<>();
        settingsLore.add("");
        settingsLore.add(ChatColor.GRAY + "Manage area settings,");
        settingsLore.add(ChatColor.GRAY + "permissions, and more.");
        settingsLore.add("");
        settingsLore.add(ChatColor.YELLOW + "▶ Click to open");
        settingsMeta.setLore(settingsLore);
        settingsItem.setItemMeta(settingsMeta);
        gui.setItem(slot, settingsItem);
    }

    private void addControlItems(Inventory gui, String areaName, ProtectedArea area, Player player,
            PaginationInfo paginationInfo) {
        // Fixed slot positions in navigation row
        int createSlot = 46;
        int undoSlot = 47;
        int redoSlot = 48;
        int teleportSlot = 49;
        int backSlot = 50;
        int previewSlot = 51;
        int intervalSlot = 52;
        int settingsSlot = 53;

        // Create Backup
        if (permissionManager.canCreateBackup(player, area)) {
            ItemStack createItem = new ItemStack(Material.EMERALD);
            ItemMeta createMeta = createItem.getItemMeta();
            createMeta.setDisplayName(ChatColor.GREEN + "➕ Create Backup");
            List<String> createLore = new ArrayList<>();
            createLore.add("");
            createLore.add(ChatColor.GRAY + "Save the current state");
            createLore.add(ChatColor.GRAY + "of this area.");
            createLore.add("");
            createLore.add(ChatColor.YELLOW + "▶ Click to create");
            createMeta.setLore(createLore);
            createItem.setItemMeta(createMeta);
            gui.setItem(createSlot, createItem);
        }

        // Undo
        if (permissionManager.canUndoRedo(player, area) && backupManager.canUndo(areaName)) {
            ItemStack undoItem = new ItemStack(Material.ORANGE_DYE);
            ItemMeta undoMeta = undoItem.getItemMeta();
            undoMeta.setDisplayName(ChatColor.GOLD + "↩ Undo");
            List<String> undoLore = new ArrayList<>();
            undoLore.add("");
            undoLore.add(ChatColor.GRAY + "Revert to previous");
            undoLore.add(ChatColor.GRAY + "backup state.");
            undoMeta.setLore(undoLore);
            undoItem.setItemMeta(undoMeta);
            gui.setItem(undoSlot, undoItem);
        }

        // Redo
        if (permissionManager.canUndoRedo(player, area) && backupManager.canRedo(areaName)) {
            ItemStack redoItem = new ItemStack(Material.LIME_DYE);
            ItemMeta redoMeta = redoItem.getItemMeta();
            redoMeta.setDisplayName(ChatColor.GREEN + "↪ Redo");
            List<String> redoLore = new ArrayList<>();
            redoLore.add("");
            redoLore.add(ChatColor.GRAY + "Redo the last");
            redoLore.add(ChatColor.GRAY + "undo operation.");
            redoMeta.setLore(redoLore);
            redoItem.setItemMeta(redoMeta);
            gui.setItem(redoSlot, redoItem);
        }

        // Teleport
        if (permissionManager.hasAreaPermission(player, area)) {
            ItemStack teleportItem = new ItemStack(Material.ENDER_PEARL);
            ItemMeta teleportMeta = teleportItem.getItemMeta();
            teleportMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "🌀 Teleport to Area");
            List<String> teleportLore = new ArrayList<>();
            teleportLore.add("");
            teleportLore.add(ChatColor.GRAY + "Teleport to the");
            teleportLore.add(ChatColor.GRAY + "area center.");
            teleportMeta.setLore(teleportLore);
            teleportItem.setItemMeta(teleportMeta);
            gui.setItem(teleportSlot, teleportItem);
        }

        // Back button
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.GRAY + "◀ Back to My Areas");
        List<String> backLore = new ArrayList<>();
        backLore.add("");
        backLore.add(ChatColor.GRAY + "Return to areas list.");
        backMeta.setLore(backLore);
        backItem.setItemMeta(backMeta);
        gui.setItem(backSlot, backItem);

        // Preview Area
        if (permissionManager.canVisualize(player, area)) {
            ItemStack previewItem = new ItemStack(Material.SPYGLASS);
            ItemMeta previewMeta = previewItem.getItemMeta();
            previewMeta.setDisplayName(ChatColor.AQUA + "👁 Preview Area");

            List<String> previewLore = new ArrayList<>();
            previewLore.add("");
            previewLore.add(ChatColor.GRAY + "Visualize area boundaries");
            previewLore.add(ChatColor.GRAY + "with particle effects.");
            previewLore.add("");
            previewLore.add(ChatColor.YELLOW + "▶ Click to preview");

            previewMeta.setLore(previewLore);
            previewItem.setItemMeta(previewMeta);
            gui.setItem(previewSlot, previewItem);
        }

        // Interval Management
        if (permissionManager.canModifyBoundaries(player, area)) {
            var intervalConfig = intervalManager.getIntervalConfig(areaName);
            ItemStack intervalItem = new ItemStack(intervalConfig != null ? Material.CLOCK : Material.GRAY_DYE);
            ItemMeta intervalMeta = intervalItem.getItemMeta();

            if (intervalConfig != null) {
                intervalMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "⏰ Auto-Restore");
                List<String> intervalLore = new ArrayList<>();
                intervalLore.add("");
                intervalLore.add(ChatColor.GREEN + "✔ Active");
                intervalLore.add(ChatColor.WHITE + "Interval: " + ChatColor.AQUA + intervalConfig.minutes + "m");
                intervalLore.add(ChatColor.WHITE + "Backup: " + ChatColor.YELLOW + "#" + intervalConfig.backupId);
                intervalLore.add("");
                intervalLore.add(ChatColor.GREEN + "Left-click: " + ChatColor.WHITE + "Disable");
                intervalLore.add(ChatColor.YELLOW + "Right-click: " + ChatColor.WHITE + "Configure");
                intervalMeta.setLore(intervalLore);
            } else {
                intervalMeta.setDisplayName(ChatColor.GRAY + "⏰ Auto-Restore");
                List<String> intervalLore = new ArrayList<>();
                intervalLore.add("");
                intervalLore.add(ChatColor.RED + "✘ Inactive");
                intervalLore.add("");
                intervalLore.add(ChatColor.YELLOW + "▶ Click to set up");
                intervalMeta.setLore(intervalLore);
            }

            intervalItem.setItemMeta(intervalMeta);
            gui.setItem(intervalSlot, intervalItem);
        }

        // Area Settings button
        addAreaSettingsButton(gui, area, areaName, settingsSlot);
    }
}

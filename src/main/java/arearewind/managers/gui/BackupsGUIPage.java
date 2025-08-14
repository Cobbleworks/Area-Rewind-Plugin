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
            ItemStack item = backup.getIconItem() != null ? backup.getIconItem().clone()
                    : new ItemStack(Material.CHEST);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + "Backup #" + i);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Created: " + backup.getTimestamp().format(
                    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
            lore.add(ChatColor.GRAY + "Blocks: " + backup.getBlocksNonAirOnly().size());

            // Check if this backup is used for auto-restore
            var intervalConfig = intervalManager.getIntervalConfig(areaName);
            if (intervalConfig != null && intervalConfig.backupId == i) {
                lore.add(ChatColor.GREEN + "Used for auto-restore (" + intervalConfig.minutes + "m)");
            }

            // Show "← Current State" on the backup that represents the current state of the
            // area
            if (backupManager.getUndoPointer(areaName) == i) {
                lore.add(ChatColor.GREEN + "← Current State");
            }

            lore.add("");
            if (permissionManager.canRestoreBackup(player, area)) {
                lore.add(ChatColor.YELLOW + "Left Click: Restore");
            }
            lore.add(ChatColor.YELLOW + "Shift+Click: Compare with current");
            lore.add(ChatColor.RED + "Shift+Right Click: Delete Backup");
            if (permissionManager.canModifyBoundaries(player, area)) {
                lore.add(ChatColor.YELLOW + "Middle Click: Set Icon");
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
            gui.setItem(slot++, item);
        }

        // Add pagination navigation if needed
        if (paginationInfo.getMaxPage() > 0) {
            GUIPaginationHelper.addPaginationButtons(gui, paginationInfo,
                    NAVIGATION_ROW_START, NAVIGATION_ROW_START + 8, -1); // No info button for backups page
        }

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
        // Area Settings Button (moved from addAreaInfoItems)
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
        gui.setItem(slot, settingsItem);
    }

    private void addControlItems(Inventory gui, String areaName, ProtectedArea area, Player player,
            PaginationInfo paginationInfo) {
        // Adjust slot positions based on whether pagination is present
        // New order: create backup, undo + redo, teleport to, return to areas, preview,
        // interval, empty, "Area Settings"
        int createSlot = paginationInfo.getMaxPage() > 0 ? NAVIGATION_ROW_START + 1 : NAVIGATION_ROW_START;
        int undoSlot = paginationInfo.getMaxPage() > 0 ? NAVIGATION_ROW_START + 2 : NAVIGATION_ROW_START + 1;
        int redoSlot = paginationInfo.getMaxPage() > 0 ? NAVIGATION_ROW_START + 3 : NAVIGATION_ROW_START + 2;
        int teleportSlot = paginationInfo.getMaxPage() > 0 ? NAVIGATION_ROW_START + 4 : NAVIGATION_ROW_START + 3;
        int backSlot = paginationInfo.getMaxPage() > 0 ? NAVIGATION_ROW_START + 5 : NAVIGATION_ROW_START + 4;
        int previewSlot = paginationInfo.getMaxPage() > 0 ? NAVIGATION_ROW_START + 6 : NAVIGATION_ROW_START + 5;
        int intervalSlot = paginationInfo.getMaxPage() > 0 ? NAVIGATION_ROW_START + 7 : NAVIGATION_ROW_START + 6;
        // Slot 7/8 is empty
        int settingsSlot = NAVIGATION_ROW_START + 8; // Always at the end

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
            undoMeta.setDisplayName(ChatColor.YELLOW + "Undo Last Restore");
            List<String> undoLore = new ArrayList<>();
            undoLore.add(ChatColor.GRAY + "Restore the area to its state");
            undoLore.add(ChatColor.GRAY + "before the last backup restore");
            undoMeta.setLore(undoLore);
            undoItem.setItemMeta(undoMeta);
            gui.setItem(undoSlot, undoItem);
        }

        if (permissionManager.canUndoRedo(player, area) && backupManager.canRedo(areaName)) {
            ItemStack redoItem = new ItemStack(Material.ARROW);
            ItemMeta redoMeta = redoItem.getItemMeta();
            redoMeta.setDisplayName(ChatColor.YELLOW + "Redo Last Undo");
            List<String> redoLore = new ArrayList<>();
            redoLore.add(ChatColor.GRAY + "Redo the last undo operation");
            redoMeta.setLore(redoLore);
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

        // Preview Area - NOW WITH AREA INFO IN TOOLTIP
        if (permissionManager.canVisualize(player, area)) {
            ItemStack previewItem = new ItemStack(Material.SPYGLASS);
            ItemMeta previewMeta = previewItem.getItemMeta();
            previewMeta.setDisplayName(ChatColor.AQUA + "Preview Area");

            // Add area information to the preview button tooltip
            List<String> previewLore = new ArrayList<>();
            previewLore.add(ChatColor.GRAY + "Visualize area boundaries");
            previewLore.add("");
            previewLore.add(ChatColor.GREEN + "Area Information:");
            previewLore.add(ChatColor.GRAY + "Name: " + areaName);
            previewLore.add(ChatColor.GRAY + "Owner: " + Bukkit.getOfflinePlayer(area.getOwner()).getName());
            previewLore.add(ChatColor.GRAY + "World: " + area.getPos1().getWorld().getName());
            previewLore.add(ChatColor.GRAY + "Size: " + area.getSize() + " blocks");

            // Add interval information to preview button
            var intervalConfig = intervalManager.getIntervalConfig(areaName);
            if (intervalConfig != null) {
                previewLore.add("");
                previewLore.add(ChatColor.AQUA + "Auto-Restore: " + ChatColor.GREEN + "Active");
                previewLore.add(ChatColor.GRAY + "Interval: " + intervalConfig.minutes + " minutes");
                previewLore.add(ChatColor.GRAY + "Backup: #" + intervalConfig.backupId);
            } else {
                previewLore.add("");
                previewLore.add(ChatColor.AQUA + "Auto-Restore: " + ChatColor.RED + "Inactive");
            }

            previewLore.add("");
            previewLore.add(ChatColor.YELLOW + "Click to preview area boundaries");

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
                intervalMeta.setDisplayName(ChatColor.AQUA + "Auto-Restore Settings");
                List<String> intervalLore = new ArrayList<>();
                intervalLore.add(ChatColor.GREEN + "Auto-restore is active");
                intervalLore.add(ChatColor.GRAY + "Interval: " + intervalConfig.minutes + " minutes");
                intervalLore.add(ChatColor.GRAY + "Backup: #" + intervalConfig.backupId);
                intervalLore.add("");
                intervalLore.add(ChatColor.YELLOW + "Left Click: Disable auto-restore");
                intervalLore.add(ChatColor.YELLOW + "Right Click: Configure settings");
                intervalMeta.setLore(intervalLore);
            } else {
                intervalMeta.setDisplayName(ChatColor.GRAY + "Auto-Restore Settings");
                List<String> intervalLore = new ArrayList<>();
                intervalLore.add(ChatColor.RED + "Auto-restore is inactive");
                intervalLore.add("");
                intervalLore.add(ChatColor.YELLOW + "Click to set up auto-restore");
                intervalMeta.setLore(intervalLore);
            }

            intervalItem.setItemMeta(intervalMeta);
            gui.setItem(intervalSlot, intervalItem);
        }

        // Back button
        ItemStack backItem = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.GRAY + "Back to My Areas");
        backItem.setItemMeta(backMeta);
        gui.setItem(backSlot, backItem);

        // Area Settings button (always at the end)
        addAreaSettingsButton(gui, area, areaName, settingsSlot);
    }
}

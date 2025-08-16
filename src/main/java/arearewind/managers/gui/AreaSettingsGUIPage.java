package arearewind.managers.gui;

import arearewind.data.AreaBackup;
import arearewind.data.ProtectedArea;
import arearewind.managers.AreaManager;
import arearewind.managers.BackupManager;
import arearewind.managers.GUIManager;
import arearewind.managers.PermissionManager;
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
 * GUI page for area settings and management
 */
public class AreaSettingsGUIPage implements IGUIPage {

    private final GUIManager guiManager;
    private final AreaManager areaManager;
    private final BackupManager backupManager;
    private final PermissionManager permissionManager;

    public AreaSettingsGUIPage(GUIManager guiManager, AreaManager areaManager,
            BackupManager backupManager, PermissionManager permissionManager) {
        this.guiManager = guiManager;
        this.areaManager = areaManager;
        this.backupManager = backupManager;
        this.permissionManager = permissionManager;
    }

    @Override
    public void openGUI(Player player) {
        // This will be called through openAreaSettingsGUI(player, areaName)
        throw new UnsupportedOperationException("Use openAreaSettingsGUI(player, areaName) instead");
    }

    public void openAreaSettingsGUI(Player player, String areaName) {
        ProtectedArea area = areaManager.getArea(areaName);
        if (area == null) {
            player.sendMessage(ChatColor.RED + "Area not found!");
            return;
        }

        if (!permissionManager.hasAreaPermission(player, area)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to view settings for this area!");
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 45, ChatColor.DARK_GREEN + "Area Settings: " + areaName);

        // Area Information (top row)
        addAreaInfoItem(gui, area, areaName, 10);

        // Backup Statistics (top row)
        addBackupStatisticsItem(gui, areaName, 12);

        // Permission Information (top row)
        addPermissionInfoItem(gui, area, player, 14);

        // Management options (middle rows)
        addManagementItems(gui, area, areaName, player);

        // Navigation (bottom row)
        addNavigationItems(gui, areaName);

        player.openInventory(gui);
        guiManager.registerOpenGUI(player, getPageType() + ":" + areaName);
    }

    @Override
    public void handleClick(Player player, InventoryClickEvent event) {
        String guiData = guiManager.getOpenGUIType(player);
        if (!guiData.startsWith("area-settings:"))
            return;

        String areaName = guiData.substring(14);
        handleAreaSettingsClick(player, event, areaName);
    }

    @Override
    public String getPageType() {
        return "area-settings";
    }

    private void handleAreaSettingsClick(Player player, InventoryClickEvent event, String areaName) {
        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta())
            return;

        String displayName = item.getItemMeta().getDisplayName();

        // Handle navigation buttons
        if (displayName.contains("Back to Backups")) {
            player.closeInventory();
            guiManager.openBackupsGUI(player, areaName);
            return;
        } else if (displayName.contains("Back to My Areas")) {
            player.closeInventory();
            guiManager.openMyAreasGUI(player);
            return;
        }

        // Handle management actions
        if (displayName.contains("Rename Area")) {
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "To rename this area, use: " + ChatColor.GREEN + "/rewind rename "
                    + areaName + " <new-name>");
            return;
        } else if (displayName.contains("Modify Boundaries")) {
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Area Boundary Management:");
            player.sendMessage(ChatColor.GREEN + "/rewind expand " + areaName + " <direction> <amount>");
            player.sendMessage(ChatColor.GREEN + "/rewind contract " + areaName + " <direction> <amount>");
            player.sendMessage(ChatColor.GREEN + "Or use /rewind pos1 and /rewind pos2 to redefine boundaries");
            return;
        } else if (displayName.contains("Manage Trust")) {
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Trust Management:");
            player.sendMessage(ChatColor.GREEN + "/rewind trust " + areaName + " <player>");
            player.sendMessage(ChatColor.GREEN + "/rewind untrust " + areaName + " <player>");
            player.sendMessage(ChatColor.GREEN + "/rewind trustlist " + areaName);
            return;
        } else if (displayName.contains("Transfer Ownership")) {
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "To transfer ownership, use: " + ChatColor.GREEN + "/rewind transfer "
                    + areaName + " <new-owner>");
            return;
        } else if (displayName.contains("Set Icon")) {
            player.closeInventory();
            guiManager.openMaterialSelector(player, "area", areaName, null);
            return;
        } else if (displayName.contains("Restore Speed")) {
            ProtectedArea area = areaManager.getArea(areaName);
            if (area == null) {
                player.sendMessage(ChatColor.RED + "Area not found!");
                return;
            }

            if (event.isRightClick() && area.hasCustomRestoreSpeed()) {
                // Reset to dynamic
                area.setCustomRestoreSpeed(null);
                areaManager.saveAreas();
                player.sendMessage(ChatColor.GREEN + "Restore speed reset to dynamic for area '" + areaName + "'");

                // Refresh the GUI
                player.closeInventory();
                openAreaSettingsGUI(player, areaName);
                return;
            } else {
                // Set custom speed
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "=== Restore Speed Configuration ===");
                player.sendMessage(ChatColor.GRAY + "Current: " +
                        (area.hasCustomRestoreSpeed() ? area.getCustomRestoreSpeed() + " blocks/tick (custom)"
                                : "Dynamic (auto-calculated)"));
                player.sendMessage("");
                player.sendMessage(ChatColor.GREEN + "To set custom speed: " + ChatColor.WHITE + "/rewind speed "
                        + areaName + " <10-1000>");
                player.sendMessage(ChatColor.GREEN + "To reset to dynamic: " + ChatColor.WHITE + "/rewind speed "
                        + areaName + " dynamic");
                player.sendMessage("");
                player.sendMessage(ChatColor.GRAY + "Lower values = slower but less server impact");
                player.sendMessage(ChatColor.GRAY + "Higher values = faster but more server impact");
                return;
            }
        } else if (displayName.contains("Delete Area")) {
            // Confirmation required for deletion
            if (event.isShiftClick()) {
                player.closeInventory();
                player.performCommand("rewind delete " + areaName + " confirm");
            } else {
                player.sendMessage(ChatColor.RED + "To delete this area and ALL its backups:");
                player.sendMessage(ChatColor.YELLOW + "SHIFT+CLICK the delete button or use:");
                player.sendMessage(ChatColor.GREEN + "/rewind delete " + areaName + " confirm");
                player.sendMessage(ChatColor.RED + "This action cannot be undone!");
            }
            return;
        } else if (displayName.contains("Delete All Backups Except Last")) {
            // Confirmation required for this destructive action
            if (event.isShiftClick()) {
                ProtectedArea area = areaManager.getArea(areaName);
                if (area == null) {
                    player.sendMessage(ChatColor.RED + "Area not found!");
                    return;
                }

                if (!permissionManager.canDeleteArea(player, area)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to delete backups for this area!");
                    return;
                }

                // Perform deletion via BackupManager: keep last backup, remove others
                int removed = backupManager.deleteAllBackupsExceptLast(areaName);
                player.closeInventory();
                player.sendMessage(
                        ChatColor.GREEN + "Deleted " + removed + " backups for area '" + areaName + "' (kept latest)");
            } else {
                player.sendMessage(ChatColor.RED + "To delete all backups except the last one:");
                player.sendMessage(ChatColor.YELLOW + "SHIFT+CLICK the button or use:");
                player.sendMessage(ChatColor.RED + "This action cannot be undone for the removed backups!");
            }
            return;
        } else if (displayName.contains("Export to Schematic")) {
            ProtectedArea area = areaManager.getArea(areaName);
            if (area == null) {
                player.sendMessage(ChatColor.RED + "Area not found!");
                return;
            }
            if (!permissionManager.canExport(player, area)) {
                player.sendMessage(ChatColor.RED + "You don't have permission to export this area!");
                return;
            }
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Exporting latest backup to schematic file...");
            player.performCommand("rewind export " + areaName);
            return;
        }
    }

    private void addAreaInfoItem(Inventory gui, ProtectedArea area, String areaName, int slot) {
        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(ChatColor.GREEN + "Area Information");
        List<String> infoLore = new ArrayList<>();
        infoLore.add(ChatColor.GRAY + "Name: " + areaName);
        infoLore.add(ChatColor.GRAY + "Owner: " + Bukkit.getOfflinePlayer(area.getOwner()).getName());
        infoLore.add(ChatColor.GRAY + "World: " + area.getPos1().getWorld().getName());
        infoLore.add(ChatColor.GRAY + "Size: " + area.getSize() + " blocks");
        infoLore.add(ChatColor.GRAY + "Pos1: " + areaManager.locationToString(area.getPos1()));
        infoLore.add(ChatColor.GRAY + "Pos2: " + areaManager.locationToString(area.getPos2()));
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        gui.setItem(slot, infoItem);
    }

    private void addBackupStatisticsItem(Inventory gui, String areaName, int slot) {
        ItemStack backupInfoItem = new ItemStack(Material.CHEST);
        ItemMeta backupMeta = backupInfoItem.getItemMeta();
        backupMeta.setDisplayName(ChatColor.BLUE + "Backup Statistics");
        List<String> backupLore = new ArrayList<>();
        List<AreaBackup> backups = backupManager.getBackupHistory(areaName);
        backupLore.add(ChatColor.GRAY + "Total Backups: " + backups.size());
        if (!backups.isEmpty()) {
            AreaBackup lastBackup = backups.get(backups.size() - 1);
            backupLore.add(ChatColor.GRAY + "Last Backup: " +
                    lastBackup.getTimestamp().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));

            AreaBackup firstBackup = backups.get(0);
            backupLore.add(ChatColor.GRAY + "First Backup: " +
                    firstBackup.getTimestamp().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        }

        int undoPointer = backupManager.getUndoPointer(areaName);
        backupLore.add(ChatColor.GRAY + "Current State: Backup #" + undoPointer);

        backupMeta.setLore(backupLore);
        backupInfoItem.setItemMeta(backupMeta);
        gui.setItem(slot, backupInfoItem);
    }

    private void addPermissionInfoItem(Inventory gui, ProtectedArea area, Player player, int slot) {
        ItemStack permItem = new ItemStack(Material.NAME_TAG);
        ItemMeta permMeta = permItem.getItemMeta();
        permMeta.setDisplayName(ChatColor.YELLOW + "Permission Information");
        List<String> permLore = new ArrayList<>();
        permLore.add(ChatColor.GRAY + "Your Level: " + permissionManager.getPermissionLevelString(player, area));
        permLore.add(ChatColor.GRAY + "Trusted Players: " + area.getTrustedPlayers().size());

        if (!area.getTrustedPlayers().isEmpty()) {
            permLore.add(ChatColor.GRAY + "Trust List:");
            for (java.util.UUID trustedUuid : area.getTrustedPlayers()) {
                String trustedName = Bukkit.getOfflinePlayer(trustedUuid).getName();
                permLore.add(ChatColor.GRAY + "  - " + trustedName);
            }
        }

        permLore.add("");
        permLore.add(ChatColor.YELLOW + "Permissions:");
        permLore.add(ChatColor.GRAY + "Create Backup: "
                + (permissionManager.canCreateBackup(player, area) ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"));
        permLore.add(ChatColor.GRAY + "Restore Backup: "
                + (permissionManager.canRestoreBackup(player, area) ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"));
        permLore.add(ChatColor.GRAY + "Undo/Redo: "
                + (permissionManager.canUndoRedo(player, area) ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"));
        permLore.add(ChatColor.GRAY + "Modify Boundaries: "
                + (permissionManager.canModifyBoundaries(player, area) ? ChatColor.GREEN + "Yes"
                        : ChatColor.RED + "No"));

        permMeta.setLore(permLore);
        permItem.setItemMeta(permMeta);
        gui.setItem(slot, permItem);
    }

    private void addManagementItems(Inventory gui, ProtectedArea area, String areaName, Player player) {
        // Rename Area
        if (permissionManager.canModifyBoundaries(player, area)) {
            ItemStack renameItem = new ItemStack(Material.WRITABLE_BOOK);
            ItemMeta renameMeta = renameItem.getItemMeta();
            renameMeta.setDisplayName(ChatColor.AQUA + "Rename Area");
            List<String> renameLore = new ArrayList<>();
            renameLore.add(ChatColor.GRAY + "Change the name of this area");
            renameLore.add("");
            renameLore.add(ChatColor.YELLOW + "Click for rename command");
            renameMeta.setLore(renameLore);
            renameItem.setItemMeta(renameMeta);
            gui.setItem(19, renameItem);
        }

        // Modify Boundaries
        if (permissionManager.canModifyBoundaries(player, area)) {
            ItemStack boundaryItem = new ItemStack(Material.IRON_PICKAXE);
            ItemMeta boundaryMeta = boundaryItem.getItemMeta();
            boundaryMeta.setDisplayName(ChatColor.GOLD + "Modify Boundaries");
            List<String> boundaryLore = new ArrayList<>();
            boundaryLore.add(ChatColor.GRAY + "Expand or contract area boundaries");
            boundaryLore.add("");
            boundaryLore.add(ChatColor.YELLOW + "Click for boundary commands");
            boundaryMeta.setLore(boundaryLore);
            boundaryItem.setItemMeta(boundaryMeta);
            gui.setItem(21, boundaryItem);
        }

        // Manage Trust
        if (permissionManager.canModifyBoundaries(player, area)) {
            ItemStack trustItem = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta trustMeta = trustItem.getItemMeta();
            trustMeta.setDisplayName(ChatColor.GREEN + "Manage Trust");
            List<String> trustLore = new ArrayList<>();
            trustLore.add(ChatColor.GRAY + "Add or remove trusted players");
            trustLore.add("");
            trustLore.add(ChatColor.YELLOW + "Click for trust commands");
            trustMeta.setLore(trustLore);
            trustItem.setItemMeta(trustMeta);
            gui.setItem(23, trustItem);
        }

        // Set Icon
        if (permissionManager.canModifyBoundaries(player, area)) {
            ItemStack iconItem = area.getIconItem() != null ? area.getIconItem().clone()
                    : new ItemStack(Material.GRASS_BLOCK);
            ItemMeta iconMeta = iconItem.getItemMeta();
            iconMeta.setDisplayName(ChatColor.AQUA + "Set Icon");
            List<String> iconLore = new ArrayList<>();
            iconLore.add(ChatColor.GRAY + "Change the icon for this area");
            iconLore.add(
                    ChatColor.GRAY + "Current: " + (area.getIcon() != null ? area.getIcon().name() : "GRASS_BLOCK"));
            iconLore.add("");
            iconLore.add(ChatColor.YELLOW + "Click to select new icon");
            iconMeta.setLore(iconLore);
            iconItem.setItemMeta(iconMeta);
            gui.setItem(24, iconItem);
        }

        // Restore Speed Setting
        if (permissionManager.canModifyBoundaries(player, area)) {
            ItemStack speedItem = new ItemStack(Material.FEATHER);
            ItemMeta speedMeta = speedItem.getItemMeta();
            speedMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Restore Speed");
            List<String> speedLore = new ArrayList<>();
            speedLore.add(ChatColor.GRAY + "Configure restoration speed for this area");

            if (area.hasCustomRestoreSpeed()) {
                speedLore.add(ChatColor.YELLOW + "Current: " + ChatColor.WHITE + area.getCustomRestoreSpeed()
                        + " blocks/tick (custom)");
            } else {
                speedLore.add(ChatColor.YELLOW + "Current: " + ChatColor.WHITE + "Dynamic (auto-calculated)");
            }

            speedLore.add("");
            speedLore.add(ChatColor.GRAY + "Dynamic: Automatically calculates optimal speed");
            speedLore.add(ChatColor.GRAY + "Custom: Set fixed speed (10-1000 blocks/tick)");
            speedLore.add("");

            if (area.hasCustomRestoreSpeed()) {
                speedLore.add(ChatColor.GREEN + "Left-click: " + ChatColor.GRAY + "Modify speed");
                speedLore.add(ChatColor.RED + "Right-click: " + ChatColor.GRAY + "Reset to dynamic");
            } else {
                speedLore.add(ChatColor.GREEN + "Click: " + ChatColor.GRAY + "Set custom speed");
            }

            speedMeta.setLore(speedLore);
            speedItem.setItemMeta(speedMeta);
            gui.setItem(25, speedItem);
        }

        // Transfer Ownership
        if (area.getOwner().equals(player.getUniqueId())) {
            ItemStack transferItem = new ItemStack(Material.GOLDEN_HELMET);
            ItemMeta transferMeta = transferItem.getItemMeta();
            transferMeta.setDisplayName(ChatColor.YELLOW + "Transfer Ownership");
            List<String> transferLore = new ArrayList<>();
            transferLore.add(ChatColor.GRAY + "Transfer ownership to another player");
            transferLore.add("");
            transferLore.add(ChatColor.YELLOW + "Click for transfer command");
            transferMeta.setLore(transferLore);
            transferItem.setItemMeta(transferMeta);
            gui.setItem(26, transferItem);
        }

        // Delete Area (dangerous action)
        if (permissionManager.canDeleteArea(player, area)) {
            ItemStack deleteItem = new ItemStack(Material.TNT);
            ItemMeta deleteMeta = deleteItem.getItemMeta();
            deleteMeta.setDisplayName(ChatColor.DARK_RED + "Delete Area");
            List<String> deleteLore = new ArrayList<>();
            deleteLore.add(ChatColor.RED + "Delete this area and ALL backups");
            deleteLore.add(ChatColor.RED + "This action CANNOT be undone!");
            deleteLore.add("");
            deleteLore.add(ChatColor.YELLOW + "SHIFT+CLICK to delete");
            deleteLore.add(ChatColor.GRAY + "or use: /rewind delete " + areaName + " confirm");
            deleteMeta.setLore(deleteLore);
            deleteItem.setItemMeta(deleteMeta);
            gui.setItem(31, deleteItem);
        }

        // Delete All Backups Except Last (dangerous action but less destructive)
        if (permissionManager.canDeleteArea(player, area)) {
            ItemStack deleteExceptLast = new ItemStack(Material.REDSTONE);
            ItemMeta delExceptMeta = deleteExceptLast.getItemMeta();
            delExceptMeta.setDisplayName(ChatColor.DARK_RED + "Delete All Backups Except Last");
            List<String> delExceptLore = new ArrayList<>();
            delExceptLore.add(ChatColor.RED + "Delete all backups for this area,");
            delExceptLore.add(ChatColor.RED + "except the most recent one");
            delExceptLore.add("");
            delExceptLore.add(ChatColor.YELLOW + "SHIFT+CLICK to perform");
            delExceptMeta.setLore(delExceptLore);
            deleteExceptLast.setItemMeta(delExceptMeta);
            gui.setItem(30, deleteExceptLast);
        }
        // Export to Schematic button
        if (permissionManager.canExport(player, area)) {
            ItemStack exportItem = new ItemStack(Material.COMPASS);
            ItemMeta exportMeta = exportItem.getItemMeta();
            exportMeta.setDisplayName(ChatColor.AQUA + "Export to Schematic");
            List<String> exportLore = new ArrayList<>();
            exportLore.add(ChatColor.GRAY + "Export the latest backup of this area");
            exportLore.add(ChatColor.GRAY + "to a WorldEdit .schem file");
            exportLore.add("");
            exportLore.add(ChatColor.YELLOW + "Click to export");
            exportMeta.setLore(exportLore);
            exportItem.setItemMeta(exportMeta);
            gui.setItem(28, exportItem);
        }
    }

    private void addNavigationItems(Inventory gui, String areaName) {
        // Back to Backups
        ItemStack backToBackupsItem = new ItemStack(Material.CHEST);
        ItemMeta backToBackupsMeta = backToBackupsItem.getItemMeta();
        backToBackupsMeta.setDisplayName(ChatColor.BLUE + "Back to Backups");
        backToBackupsItem.setItemMeta(backToBackupsMeta);
        gui.setItem(38, backToBackupsItem);

        // Back to Areas
        ItemStack backToAreasItem = new ItemStack(Material.BARRIER);
        ItemMeta backToAreasMeta = backToAreasItem.getItemMeta();
        backToAreasMeta.setDisplayName(ChatColor.GRAY + "Back to My Areas");
        backToAreasItem.setItemMeta(backToAreasMeta);
        gui.setItem(40, backToAreasItem);
    }
}

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

        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "⚙ " + areaName + " Settings");

        // Fill background with dark gray glass
        fillBackground(gui);

        // === TOP SECTION: Info Panel (Row 1) ===
        addSectionHeader(gui, 4, Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + "▼ Information");
        
        // Info items centered in row 2
        addAreaInfoItem(gui, area, areaName, 11);
        addBackupStatisticsItem(gui, areaName, 13);
        addPermissionInfoItem(gui, area, player, 15);

        // === MIDDLE SECTION: Management Actions (Rows 3-4) ===
        addSectionHeader(gui, 22, Material.YELLOW_STAINED_GLASS_PANE, ChatColor.YELLOW + "▼ Management");
        
        // Management options in rows 3-4 (slots 27-35)
        addManagementItems(gui, area, areaName, player);

        // === BOTTOM SECTION: Danger Zone (Row 5) ===
        addDangerZone(gui, area, areaName, player);

        // === NAVIGATION BAR (Row 6) ===
        addNavigationBar(gui, areaName);

        player.openInventory(gui);
        guiManager.registerOpenGUI(player, getPageType() + ":" + areaName);
    }

    private void fillBackground(Inventory gui) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(" ");
        filler.setItemMeta(meta);
        
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, filler.clone());
        }
    }

    private void addSectionHeader(Inventory gui, int slot, Material material, String title) {
        ItemStack header = new ItemStack(material);
        ItemMeta meta = header.getItemMeta();
        meta.setDisplayName(title);
        header.setItemMeta(meta);
        gui.setItem(slot, header);
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
        
        // Ignore filler glass panes
        if (displayName.equals(" ") || displayName.startsWith("▼")) {
            return;
        }

        // Handle navigation buttons
        if (displayName.contains("View Backups")) {
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
                        + areaName + " <1-10000>");
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
        } else if (displayName.contains("Clear Old Backups")) {
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
                player.sendMessage(ChatColor.YELLOW + "SHIFT+CLICK the button!");
                player.sendMessage(ChatColor.RED + "This action cannot be undone!");
            }
            return;
        } else if (displayName.contains("Export Schematic")) {
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
        ItemStack infoItem = new ItemStack(Material.FILLED_MAP);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(ChatColor.GREEN + "📋 Area Details");
        List<String> infoLore = new ArrayList<>();
        infoLore.add("");
        infoLore.add(ChatColor.WHITE + "Name: " + ChatColor.AQUA + areaName);
        infoLore.add(ChatColor.WHITE + "Owner: " + ChatColor.GOLD + Bukkit.getOfflinePlayer(area.getOwner()).getName());
        infoLore.add(ChatColor.WHITE + "World: " + ChatColor.YELLOW + area.getPos1().getWorld().getName());
        infoLore.add("");
        infoLore.add(ChatColor.GRAY + "━━━━━━━━━━━━━━━━");
        infoLore.add(ChatColor.WHITE + "Size: " + ChatColor.GREEN + String.format("%,d", area.getSize()) + ChatColor.GRAY + " blocks");
        infoLore.add(ChatColor.DARK_GRAY + "Corner 1: " + ChatColor.GRAY + areaManager.locationToString(area.getPos1()));
        infoLore.add(ChatColor.DARK_GRAY + "Corner 2: " + ChatColor.GRAY + areaManager.locationToString(area.getPos2()));
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        gui.setItem(slot, infoItem);
    }

    private void addBackupStatisticsItem(Inventory gui, String areaName, int slot) {
        ItemStack backupInfoItem = new ItemStack(Material.CHEST);
        ItemMeta backupMeta = backupInfoItem.getItemMeta();
        backupMeta.setDisplayName(ChatColor.BLUE + "📊 Backup Statistics");
        List<String> backupLore = new ArrayList<>();
        List<AreaBackup> backups = backupManager.getBackupHistory(areaName);
        
        backupLore.add("");
        backupLore.add(ChatColor.WHITE + "Total Backups: " + ChatColor.AQUA + backups.size());
        
        if (!backups.isEmpty()) {
            backupLore.add("");
            backupLore.add(ChatColor.GRAY + "━━━━━━━━━━━━━━━━");
            AreaBackup lastBackup = backups.get(backups.size() - 1);
            backupLore.add(ChatColor.WHITE + "Latest: " + ChatColor.GREEN +
                    lastBackup.getTimestamp().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));

            AreaBackup firstBackup = backups.get(0);
            backupLore.add(ChatColor.WHITE + "Oldest: " + ChatColor.GRAY +
                    firstBackup.getTimestamp().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
        }

        int undoPointer = backupManager.getUndoPointer(areaName);
        backupLore.add("");
        backupLore.add(ChatColor.YELLOW + "Current Position: " + ChatColor.WHITE + "#" + undoPointer + ChatColor.DARK_GRAY + " / " + backups.size());

        backupMeta.setLore(backupLore);
        backupInfoItem.setItemMeta(backupMeta);
        gui.setItem(slot, backupInfoItem);
    }

    private void addPermissionInfoItem(Inventory gui, ProtectedArea area, Player player, int slot) {
        ItemStack permItem = new ItemStack(Material.SHIELD);
        ItemMeta permMeta = permItem.getItemMeta();
        permMeta.setDisplayName(ChatColor.GOLD + "🔐 Permissions");
        List<String> permLore = new ArrayList<>();
        
        permLore.add("");
        String levelStr = permissionManager.getPermissionLevelString(player, area);
        ChatColor levelColor = levelStr.equals("Owner") ? ChatColor.RED : 
                               levelStr.equals("Trusted") ? ChatColor.GREEN : ChatColor.GRAY;
        permLore.add(ChatColor.WHITE + "Your Role: " + levelColor + levelStr);
        permLore.add(ChatColor.WHITE + "Trusted: " + ChatColor.AQUA + area.getTrustedPlayers().size() + ChatColor.GRAY + " players");

        if (!area.getTrustedPlayers().isEmpty()) {
            permLore.add("");
            permLore.add(ChatColor.GRAY + "━━━ Trust List ━━━");
            int shown = 0;
            for (java.util.UUID trustedUuid : area.getTrustedPlayers()) {
                if (shown >= 5) {
                    permLore.add(ChatColor.DARK_GRAY + "  ... and " + (area.getTrustedPlayers().size() - 5) + " more");
                    break;
                }
                String trustedName = Bukkit.getOfflinePlayer(trustedUuid).getName();
                permLore.add(ChatColor.GRAY + "  • " + ChatColor.WHITE + trustedName);
                shown++;
            }
        }

        permLore.add("");
        permLore.add(ChatColor.GRAY + "━━━ Your Access ━━━");
        permLore.add(formatPermission("Create Backup", permissionManager.canCreateBackup(player, area)));
        permLore.add(formatPermission("Restore Backup", permissionManager.canRestoreBackup(player, area)));
        permLore.add(formatPermission("Undo/Redo", permissionManager.canUndoRedo(player, area)));
        permLore.add(formatPermission("Modify Area", permissionManager.canModifyBoundaries(player, area)));

        permMeta.setLore(permLore);
        permItem.setItemMeta(permMeta);
        gui.setItem(slot, permItem);
    }

    private String formatPermission(String name, boolean hasPermission) {
        return (hasPermission ? ChatColor.GREEN + "✔ " : ChatColor.RED + "✘ ") + 
               ChatColor.GRAY + name;
    }

    private void addManagementItems(Inventory gui, ProtectedArea area, String areaName, Player player) {
        // Row 4 (slots 27-35): Management actions in a clean grid
        
        // Rename Area - slot 28
        if (permissionManager.canModifyBoundaries(player, area)) {
            ItemStack renameItem = new ItemStack(Material.NAME_TAG);
            ItemMeta renameMeta = renameItem.getItemMeta();
            renameMeta.setDisplayName(ChatColor.AQUA + "✏ Rename Area");
            List<String> renameLore = new ArrayList<>();
            renameLore.add("");
            renameLore.add(ChatColor.GRAY + "Change the display name");
            renameLore.add(ChatColor.GRAY + "of this protected area.");
            renameLore.add("");
            renameLore.add(ChatColor.YELLOW + "▶ Click for command");
            renameMeta.setLore(renameLore);
            renameItem.setItemMeta(renameMeta);
            gui.setItem(28, renameItem);
        }

        // Modify Boundaries - slot 29
        if (permissionManager.canModifyBoundaries(player, area)) {
            ItemStack boundaryItem = new ItemStack(Material.SCAFFOLDING);
            ItemMeta boundaryMeta = boundaryItem.getItemMeta();
            boundaryMeta.setDisplayName(ChatColor.GOLD + "⬛ Modify Boundaries");
            List<String> boundaryLore = new ArrayList<>();
            boundaryLore.add("");
            boundaryLore.add(ChatColor.GRAY + "Expand or contract the");
            boundaryLore.add(ChatColor.GRAY + "area in any direction.");
            boundaryLore.add("");
            boundaryLore.add(ChatColor.YELLOW + "▶ Click for commands");
            boundaryMeta.setLore(boundaryLore);
            boundaryItem.setItemMeta(boundaryMeta);
            gui.setItem(29, boundaryItem);
        }

        // Manage Trust - slot 30
        if (permissionManager.canModifyBoundaries(player, area)) {
            ItemStack trustItem = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta trustMeta = trustItem.getItemMeta();
            trustMeta.setDisplayName(ChatColor.GREEN + "👥 Manage Trust");
            List<String> trustLore = new ArrayList<>();
            trustLore.add("");
            trustLore.add(ChatColor.GRAY + "Add or remove players");
            trustLore.add(ChatColor.GRAY + "who can access this area.");
            trustLore.add("");
            trustLore.add(ChatColor.WHITE + "Currently Trusted: " + ChatColor.AQUA + area.getTrustedPlayers().size());
            trustLore.add("");
            trustLore.add(ChatColor.YELLOW + "▶ Click for commands");
            trustMeta.setLore(trustLore);
            trustItem.setItemMeta(trustMeta);
            gui.setItem(30, trustItem);
        }

        // Set Icon - slot 32
        if (permissionManager.canModifyBoundaries(player, area)) {
            ItemStack iconItem = area.getIconItem() != null ? area.getIconItem().clone()
                    : new ItemStack(Material.PAINTING);
            ItemMeta iconMeta = iconItem.getItemMeta();
            iconMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "🎨 Set Icon");
            List<String> iconLore = new ArrayList<>();
            iconLore.add("");
            iconLore.add(ChatColor.GRAY + "Customize the icon shown");
            iconLore.add(ChatColor.GRAY + "in the areas list GUI.");
            iconLore.add("");
            iconLore.add(ChatColor.WHITE + "Current: " + ChatColor.AQUA + 
                    (area.getIcon() != null ? formatMaterialName(area.getIcon()) : "Grass Block"));
            iconLore.add("");
            iconLore.add(ChatColor.YELLOW + "▶ Click to select");
            iconMeta.setLore(iconLore);
            iconItem.setItemMeta(iconMeta);
            gui.setItem(32, iconItem);
        }

        // Restore Speed - slot 33
        if (permissionManager.canModifyBoundaries(player, area)) {
            ItemStack speedItem = new ItemStack(Material.CLOCK);
            ItemMeta speedMeta = speedItem.getItemMeta();
            speedMeta.setDisplayName(ChatColor.YELLOW + "⚡ Restore Speed");
            List<String> speedLore = new ArrayList<>();
            speedLore.add("");

            if (area.hasCustomRestoreSpeed()) {
                speedLore.add(ChatColor.WHITE + "Mode: " + ChatColor.GOLD + "Custom");
                speedLore.add(ChatColor.WHITE + "Speed: " + ChatColor.AQUA + area.getCustomRestoreSpeed() + ChatColor.GRAY + " blocks/tick");
            } else {
                speedLore.add(ChatColor.WHITE + "Mode: " + ChatColor.GREEN + "Dynamic");
                speedLore.add(ChatColor.DARK_GRAY + "(Auto-calculated)");
            }

            speedLore.add("");
            speedLore.add(ChatColor.GRAY + "Lower = slower, less lag");
            speedLore.add(ChatColor.GRAY + "Higher = faster, more lag");
            speedLore.add("");

            if (area.hasCustomRestoreSpeed()) {
                speedLore.add(ChatColor.GREEN + "Left-click: " + ChatColor.WHITE + "Modify");
                speedLore.add(ChatColor.RED + "Right-click: " + ChatColor.WHITE + "Reset to dynamic");
            } else {
                speedLore.add(ChatColor.YELLOW + "▶ Click to customize");
            }

            speedMeta.setLore(speedLore);
            speedItem.setItemMeta(speedMeta);
            gui.setItem(33, speedItem);
        }

        // Transfer Ownership - slot 34
        if (area.getOwner().equals(player.getUniqueId())) {
            ItemStack transferItem = new ItemStack(Material.GOLDEN_HELMET);
            ItemMeta transferMeta = transferItem.getItemMeta();
            transferMeta.setDisplayName(ChatColor.GOLD + "👑 Transfer Ownership");
            List<String> transferLore = new ArrayList<>();
            transferLore.add("");
            transferLore.add(ChatColor.GRAY + "Give ownership of this");
            transferLore.add(ChatColor.GRAY + "area to another player.");
            transferLore.add("");
            transferLore.add(ChatColor.RED + "⚠ This cannot be undone!");
            transferLore.add("");
            transferLore.add(ChatColor.YELLOW + "▶ Click for command");
            transferMeta.setLore(transferLore);
            transferItem.setItemMeta(transferMeta);
            gui.setItem(34, transferItem);
        }

        // Export to Schematic - slot 31
        if (permissionManager.canExport(player, area)) {
            ItemStack exportItem = new ItemStack(Material.MAP);
            ItemMeta exportMeta = exportItem.getItemMeta();
            exportMeta.setDisplayName(ChatColor.AQUA + "📤 Export Schematic");
            List<String> exportLore = new ArrayList<>();
            exportLore.add("");
            exportLore.add(ChatColor.GRAY + "Export the latest backup");
            exportLore.add(ChatColor.GRAY + "to a WorldEdit .schem file.");
            exportLore.add("");
            exportLore.add(ChatColor.YELLOW + "▶ Click to export");
            exportMeta.setLore(exportLore);
            exportItem.setItemMeta(exportMeta);
            gui.setItem(31, exportItem);
        }
    }

    private String formatMaterialName(Material material) {
        String name = material.name().toLowerCase().replace('_', ' ');
        StringBuilder result = new StringBuilder();
        boolean capitalize = true;
        for (char c : name.toCharArray()) {
            if (c == ' ') {
                capitalize = true;
                result.append(c);
            } else if (capitalize) {
                result.append(Character.toUpperCase(c));
                capitalize = false;
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    private void addDangerZone(Inventory gui, ProtectedArea area, String areaName, Player player) {
        // Red glass pane separator for danger zone
        addSectionHeader(gui, 40, Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "▼ Danger Zone");
        
        // Delete All Backups Except Last - slot 46
        if (permissionManager.canDeleteArea(player, area)) {
            ItemStack deleteExceptLast = new ItemStack(Material.LAVA_BUCKET);
            ItemMeta delExceptMeta = deleteExceptLast.getItemMeta();
            delExceptMeta.setDisplayName(ChatColor.RED + "🗑 Clear Old Backups");
            List<String> delExceptLore = new ArrayList<>();
            delExceptLore.add("");
            delExceptLore.add(ChatColor.GRAY + "Delete all backups except");
            delExceptLore.add(ChatColor.GRAY + "the most recent one.");
            delExceptLore.add("");
            delExceptLore.add(ChatColor.RED + "⚠ Cannot be undone!");
            delExceptLore.add("");
            delExceptLore.add(ChatColor.YELLOW + "SHIFT+CLICK" + ChatColor.GRAY + " to confirm");
            delExceptMeta.setLore(delExceptLore);
            deleteExceptLast.setItemMeta(delExceptMeta);
            gui.setItem(47, deleteExceptLast);
        }

        // Delete Area - slot 49
        if (permissionManager.canDeleteArea(player, area)) {
            ItemStack deleteItem = new ItemStack(Material.TNT);
            ItemMeta deleteMeta = deleteItem.getItemMeta();
            deleteMeta.setDisplayName(ChatColor.DARK_RED + "💣 Delete Area");
            List<String> deleteLore = new ArrayList<>();
            deleteLore.add("");
            deleteLore.add(ChatColor.RED + "Permanently delete this");
            deleteLore.add(ChatColor.RED + "area and ALL its backups!");
            deleteLore.add("");
            deleteLore.add(ChatColor.DARK_RED + "⚠ CANNOT BE UNDONE!");
            deleteLore.add("");
            deleteLore.add(ChatColor.YELLOW + "SHIFT+CLICK" + ChatColor.GRAY + " to confirm");
            deleteMeta.setLore(deleteLore);
            deleteItem.setItemMeta(deleteMeta);
            gui.setItem(51, deleteItem);
        }
    }

    private void addNavigationBar(Inventory gui, String areaName) {
        // Clear navigation row slots first with black glass
        ItemStack navFiller = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta navMeta = navFiller.getItemMeta();
        navMeta.setDisplayName(" ");
        navFiller.setItemMeta(navMeta);
        for (int i = 45; i < 54; i++) {
            // Only fill if not already set with an action item
            if (gui.getItem(i) == null || gui.getItem(i).getType() == Material.GRAY_STAINED_GLASS_PANE) {
                gui.setItem(i, navFiller.clone());
            }
        }

        // Back to Backups - slot 45
        ItemStack backToBackupsItem = new ItemStack(Material.CHEST);
        ItemMeta backToBackupsMeta = backToBackupsItem.getItemMeta();
        backToBackupsMeta.setDisplayName(ChatColor.BLUE + "◀ View Backups");
        List<String> backupLore = new ArrayList<>();
        backupLore.add("");
        backupLore.add(ChatColor.GRAY + "Return to the backup");
        backupLore.add(ChatColor.GRAY + "list for this area.");
        backToBackupsMeta.setLore(backupLore);
        backToBackupsItem.setItemMeta(backToBackupsMeta);
        gui.setItem(45, backToBackupsItem);

        // Back to My Areas / Cancel - slot 53
        ItemStack backToAreasItem = new ItemStack(Material.ARROW);
        ItemMeta backToAreasMeta = backToAreasItem.getItemMeta();
        backToAreasMeta.setDisplayName(ChatColor.GRAY + "◀ Back to My Areas / Cancel");
        List<String> areasLore = new ArrayList<>();
        areasLore.add("");
        areasLore.add(ChatColor.GRAY + "Return to your");
        areasLore.add(ChatColor.GRAY + "areas overview.");
        backToAreasMeta.setLore(areasLore);
        backToAreasItem.setItemMeta(backToAreasMeta);
        gui.setItem(53, backToAreasItem);
    }
}

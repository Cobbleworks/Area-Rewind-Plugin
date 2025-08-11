package arearewind.managers;

import arearewind.data.AreaBackup;
import arearewind.data.ProtectedArea;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GUIManager implements Listener {
    private final JavaPlugin plugin;
    private final AreaManager areaManager;
    private final BackupManager backupManager;
    private final PermissionManager permissionManager;
    private final Map<UUID, String> openGUIs = new HashMap<>();

    public GUIManager(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
            PermissionManager permissionManager) {
        this.plugin = plugin;
        this.areaManager = areaManager;
        this.backupManager = backupManager;
        this.permissionManager = permissionManager;
    }

    public void openAreasGUI(Player player) {
        if (!permissionManager.canUseGUI(player)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use the GUI!");
            return;
        }
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "Protected Areas");
        int slot = 0;
        for (Map.Entry<String, ProtectedArea> entry : areaManager.getProtectedAreas().entrySet()) {
            if (slot >= 45)
                break;
            String areaName = entry.getKey();
            ProtectedArea area = entry.getValue();
            if (!permissionManager.hasAreaPermission(player, area)) {
                continue;
            }
            ItemStack item = new ItemStack(Material.GRASS_BLOCK);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + areaName);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Owner: " + Bukkit.getOfflinePlayer(area.getOwner()).getName());
            lore.add(ChatColor.GRAY + "Size: " + area.getSize() + " blocks");
            lore.add(ChatColor.GRAY + "Backups: " + backupManager.getBackupHistory(areaName).size());
            lore.add(ChatColor.GRAY + "Permission: " + permissionManager.getPermissionLevelString(player, area));
            lore.add("");
            lore.add(ChatColor.YELLOW + "Left Click: Manage Backups");
            lore.add(ChatColor.YELLOW + "Right Click: Teleport");
            lore.add(ChatColor.YELLOW + "Shift+Click: Area Info");

            meta.setLore(lore);
            item.setItemMeta(meta);

            gui.setItem(slot++, item);
        }

        addNavigationItems(gui, "areas");

        player.openInventory(gui);
        openGUIs.put(player.getUniqueId(), "areas");
    }

    public void openBackupsGUI(Player player, String areaName) {
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
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_BLUE + "Backups: " + areaName);

        int slot = 0;
        for (int i = Math.max(0, backups.size() - 45); i < backups.size() && slot < 45; i++) {
            AreaBackup backup = backups.get(i);

            ItemStack item = new ItemStack(Material.CHEST);
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

        addBackupControlItems(gui, areaName, area, player);

        player.openInventory(gui);
        openGUIs.put(player.getUniqueId(), "backups:" + areaName);
    }

    public void openAreaInfoGUI(Player player, String areaName) {
        ProtectedArea area = areaManager.getArea(areaName);
        if (area == null) {
            player.sendMessage(ChatColor.RED + "Area not found!");
            return;
        }

        if (!permissionManager.canViewAreaInfo(player, area)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to view this area's info!");
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "Info: " + areaName);

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
        gui.setItem(10, infoItem);

        ItemStack backupInfoItem = new ItemStack(Material.CHEST);
        ItemMeta backupMeta = backupInfoItem.getItemMeta();
        backupMeta.setDisplayName(ChatColor.BLUE + "Backup Information");
        List<String> backupLore = new ArrayList<>();
        List<AreaBackup> backups = backupManager.getBackupHistory(areaName);
        backupLore.add(ChatColor.GRAY + "Total Backups: " + backups.size());
        if (!backups.isEmpty()) {
            AreaBackup lastBackup = backups.get(backups.size() - 1);
            backupLore.add(ChatColor.GRAY + "Last Backup: " +
                    lastBackup.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }
        backupLore.add("");
        backupLore.add(ChatColor.YELLOW + "Click to view backups");
        backupMeta.setLore(backupLore);
        backupInfoItem.setItemMeta(backupMeta);
        gui.setItem(12, backupInfoItem);

        ItemStack permItem = new ItemStack(Material.NAME_TAG);
        ItemMeta permMeta = permItem.getItemMeta();
        permMeta.setDisplayName(ChatColor.YELLOW + "Permissions");
        List<String> permLore = new ArrayList<>();
        permLore.add(ChatColor.GRAY + "Your Level: " + permissionManager.getPermissionLevelString(player, area));
        if (!area.getTrustedPlayers().isEmpty()) {
            permLore.add(ChatColor.GRAY + "Trusted Players: " + area.getTrustedPlayers().size());
        }
        permLore.add("");
        permLore.add(ChatColor.YELLOW + "Click for detailed permissions");
        permMeta.setLore(permLore);
        permItem.setItemMeta(permMeta);
        gui.setItem(14, permItem);

        if (permissionManager.canModifyBoundaries(player, area)) {
            ItemStack manageItem = new ItemStack(Material.IRON_PICKAXE);
            ItemMeta manageMeta = manageItem.getItemMeta();
            manageMeta.setDisplayName(ChatColor.RED + "Area Management");
            List<String> manageLore = new ArrayList<>();
            manageLore.add(ChatColor.GRAY + "Expand, contract, or delete area");
            manageLore.add("");
            manageLore.add(ChatColor.YELLOW + "Click for management options");
            manageMeta.setLore(manageLore);
            manageItem.setItemMeta(manageMeta);
            gui.setItem(16, manageItem);
        }

        player.openInventory(gui);
        openGUIs.put(player.getUniqueId(), "info:" + areaName);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;
        Player player = (Player) event.getWhoClicked();

        if (!openGUIs.containsKey(player.getUniqueId()))
            return;

        // Only cancel clicks in the plugin's GUI, not the player's inventory
        if (event.getClickedInventory() == null ||
                !event.getView().getTitle().contains("Protected Areas") &&
                        !event.getView().getTitle().contains("Backups:") &&
                        !event.getView().getTitle().contains("Info:")) {
            return;
        }

        event.setCancelled(true);

        String guiType = openGUIs.get(player.getUniqueId());
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR)
            return;

        if (guiType.equals("areas")) {
            handleAreasGUIClick(player, event);
        } else if (guiType.startsWith("backups:")) {
            handleBackupsGUIClick(player, event, guiType.substring(8));
        } else if (guiType.startsWith("info:")) {
            handleInfoGUIClick(player, event, guiType.substring(5));
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player))
            return;
        Player player = (Player) event.getPlayer();

        // Check if the closed inventory is one of our plugin GUIs
        String title = event.getView().getTitle();
        if (title.contains("Protected Areas") ||
                title.contains("Backups:") ||
                title.contains("Info:")) {
            openGUIs.remove(player.getUniqueId());
        }
    }

    private void handleAreasGUIClick(Player player, InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta())
            return;

        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        // Handle navigation buttons
        if (displayName.equals("Close")) {
            player.closeInventory();
            return;
        } else if (displayName.equals("Refresh")) {
            player.closeInventory();
            openAreasGUI(player);
            return;
        } else if (displayName.equals("Settings")) {
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Settings functionality coming soon!");
            return;
        }

        // Handle area selection
        ProtectedArea area = areaManager.getArea(displayName);
        if (area == null)
            return;

        if (event.isShiftClick()) {
            player.closeInventory();
            openAreaInfoGUI(player, displayName);
        } else if (event.isLeftClick()) {
            // Changed: Left click now opens backup management instead of teleporting
            player.closeInventory();
            openBackupsGUI(player, displayName);
        } else if (event.isRightClick()) {
            // Changed: Right click now teleports instead of opening backups
            player.closeInventory();
            player.performCommand("rewind teleport " + displayName);
        }
    }

    private void handleBackupsGUIClick(Player player, InventoryClickEvent event, String areaName) {
        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta())
            return;

        String displayName = item.getItemMeta().getDisplayName();

        // Handle navigation and control buttons
        if (displayName.contains("Back to Areas")) {
            player.closeInventory();
            openAreasGUI(player);
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
        }

        // Handle backup selection
        if (displayName.contains("Backup #")) {
            String backupId = displayName.replaceAll(".*#", "");

            if (event.isShiftClick()) {
                player.closeInventory();
                player.performCommand("rewind diff " + areaName);
            } else if (event.isLeftClick()) {
                player.closeInventory();
                player.performCommand("rewind restore " + areaName + " " + backupId);
            } else if (event.isRightClick()) {
                player.closeInventory();
                player.performCommand("rewind preview " + areaName + " " + backupId);
            }
        }
    }

    private void handleInfoGUIClick(Player player, InventoryClickEvent event, String areaName) {
        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta())
            return;

        String displayName = item.getItemMeta().getDisplayName();

        if (displayName.contains("Backup Information")) {
            player.closeInventory();
            openBackupsGUI(player, areaName);
        } else if (displayName.contains("Permissions")) {
            player.closeInventory();
            player.performCommand("rewind permissions " + areaName);
        } else if (displayName.contains("Area Management")) {
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Area Management Commands:");
            player.sendMessage(ChatColor.GREEN + "/rewind expand " + areaName + " <direction> <amount>");
            player.sendMessage(ChatColor.GREEN + "/rewind contract " + areaName + " <direction> <amount>");
            player.sendMessage(ChatColor.GREEN + "/rewind delete " + areaName);
            player.sendMessage(ChatColor.GREEN + "/rewind rename " + areaName + " <new_name>");
        }
    }

    private void addNavigationItems(Inventory gui, String currentGUI) {
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName(ChatColor.RED + "Close");
        closeItem.setItemMeta(closeMeta);
        gui.setItem(49, closeItem);

        ItemStack refreshItem = new ItemStack(Material.EMERALD);
        ItemMeta refreshMeta = refreshItem.getItemMeta();
        refreshMeta.setDisplayName(ChatColor.GREEN + "Refresh");
        refreshItem.setItemMeta(refreshMeta);
        gui.setItem(45, refreshItem);

        ItemStack settingsItem = new ItemStack(Material.COMPARATOR);
        ItemMeta settingsMeta = settingsItem.getItemMeta();
        settingsMeta.setDisplayName(ChatColor.YELLOW + "Settings");
        settingsItem.setItemMeta(settingsMeta);
        gui.setItem(53, settingsItem);
    }

    private void addBackupControlItems(Inventory gui, String areaName, ProtectedArea area, Player player) {
        if (permissionManager.canCreateBackup(player, area)) {
            ItemStack createItem = new ItemStack(Material.CRAFTING_TABLE);
            ItemMeta createMeta = createItem.getItemMeta();
            createMeta.setDisplayName(ChatColor.GREEN + "Create Backup");
            createItem.setItemMeta(createMeta);
            gui.setItem(45, createItem);
        }

        if (permissionManager.canUndoRedo(player, area) && backupManager.canUndo(areaName)) {
            ItemStack undoItem = new ItemStack(Material.ARROW);
            ItemMeta undoMeta = undoItem.getItemMeta();
            undoMeta.setDisplayName(ChatColor.YELLOW + "Undo Last Change");
            undoItem.setItemMeta(undoMeta);
            gui.setItem(46, undoItem);
        }

        if (permissionManager.canUndoRedo(player, area) && backupManager.canRedo(areaName)) {
            ItemStack redoItem = new ItemStack(Material.ARROW);
            ItemMeta redoMeta = redoItem.getItemMeta();
            redoMeta.setDisplayName(ChatColor.YELLOW + "Redo Last Undo");
            redoItem.setItemMeta(redoMeta);
            gui.setItem(47, redoItem);
        }

        // Area-specific actions - Teleport and Preview
        if (permissionManager.hasAreaPermission(player, area)) {
            ItemStack teleportItem = new ItemStack(Material.ENDER_PEARL);
            ItemMeta teleportMeta = teleportItem.getItemMeta();
            teleportMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Teleport to Area");
            List<String> teleportLore = new ArrayList<>();
            teleportLore.add(ChatColor.GRAY + "Teleport to this area");
            teleportMeta.setLore(teleportLore);
            teleportItem.setItemMeta(teleportMeta);
            gui.setItem(48, teleportItem);
        }

        if (permissionManager.canVisualize(player, area)) {
            ItemStack previewItem = new ItemStack(Material.SPYGLASS);
            ItemMeta previewMeta = previewItem.getItemMeta();
            previewMeta.setDisplayName(ChatColor.AQUA + "Preview Area");
            List<String> previewLore = new ArrayList<>();
            previewLore.add(ChatColor.GRAY + "Preview current state of area");
            previewMeta.setLore(previewLore);
            previewItem.setItemMeta(previewMeta);
            gui.setItem(49, previewItem);
        }

        // Back button
        ItemStack backItem = new ItemStack(Material.SPECTRAL_ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.GRAY + "Back to Areas");
        backItem.setItemMeta(backMeta);
        gui.setItem(53, backItem);
    }

    public void closeGUI(Player player) {
        openGUIs.remove(player.getUniqueId());
    }

    public boolean hasGUIOpen(Player player) {
        return openGUIs.containsKey(player.getUniqueId());
    }

    public String getOpenGUIType(Player player) {
        return openGUIs.get(player.getUniqueId());
    }
}

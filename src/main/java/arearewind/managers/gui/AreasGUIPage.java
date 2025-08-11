package arearewind.managers.gui;

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

    public AreasGUIPage(GUIManager guiManager, AreaManager areaManager,
            BackupManager backupManager, PermissionManager permissionManager) {
        this.guiManager = guiManager;
        this.areaManager = areaManager;
        this.backupManager = backupManager;
        this.permissionManager = permissionManager;
    }

    @Override
    public void openGUI(Player player) {
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
            lore.add(ChatColor.YELLOW + "Click: Manage Area & Backups");

            meta.setLore(lore);
            item.setItemMeta(meta);
            gui.setItem(slot++, item);
        }

        addNavigationItems(gui);

        player.openInventory(gui);
        guiManager.registerOpenGUI(player, getPageType());
    }

    @Override
    public void handleClick(Player player, InventoryClickEvent event) {
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
            openGUI(player);
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

    private void addNavigationItems(Inventory gui) {
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
}

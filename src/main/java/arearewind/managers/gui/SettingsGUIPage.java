package arearewind.managers.gui;

import arearewind.managers.GUIManager;
import arearewind.managers.PermissionManager;
import arearewind.util.ConfigurationManager;
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

/**
 * GUI page for plugin settings and configuration
 */
public class SettingsGUIPage implements IGUIPage {

    private final GUIManager guiManager;
    private final PermissionManager permissionManager;
    private final ConfigurationManager configManager;

    public SettingsGUIPage(GUIManager guiManager, PermissionManager permissionManager,
            ConfigurationManager configManager) {
        this.guiManager = guiManager;
        this.permissionManager = permissionManager;
        this.configManager = configManager;
    }

    @Override
    public void openGUI(Player player) {
        if (!permissionManager.canUseGUI(player)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use the GUI!");
            return;
        }

        if (!permissionManager.hasAdminPermission(player)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to access settings!");
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 45, ChatColor.DARK_PURPLE + "Area Rewind Settings");

        // Plugin Information
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(ChatColor.GOLD + "Plugin Information");
        List<String> infoLore = new ArrayList<>();
        infoLore.add(ChatColor.GRAY + "Area Rewind Plugin");
        infoLore.add(ChatColor.GRAY + "Version: 1.0.5-SNAPSHOT");
        infoLore.add(ChatColor.GRAY + "Manage area backups and restoration");
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        gui.setItem(10, infoItem);

        // Backup Settings
        ItemStack backupItem = new ItemStack(Material.CHEST);
        ItemMeta backupMeta = backupItem.getItemMeta();
        backupMeta.setDisplayName(ChatColor.BLUE + "Backup Settings");
        List<String> backupLore = new ArrayList<>();
        backupLore.add(ChatColor.GRAY + "Configure automatic backups");
        backupLore.add(ChatColor.GRAY + "Set backup intervals and limits");
        backupLore.add("");
        backupLore.add(ChatColor.YELLOW + "Click to configure");
        backupMeta.setLore(backupLore);
        backupItem.setItemMeta(backupMeta);
        gui.setItem(12, backupItem);

        // Permission Settings
        ItemStack permItem = new ItemStack(Material.NAME_TAG);
        ItemMeta permMeta = permItem.getItemMeta();
        permMeta.setDisplayName(ChatColor.GREEN + "Permission Settings");
        List<String> permLore = new ArrayList<>();
        permLore.add(ChatColor.GRAY + "Configure default permissions");
        permLore.add(ChatColor.GRAY + "Manage access levels");
        permLore.add("");
        permLore.add(ChatColor.YELLOW + "Click to configure");
        permMeta.setLore(permLore);
        permItem.setItemMeta(permMeta);
        gui.setItem(14, permItem);

        // Visualization Settings
        ItemStack visualItem = new ItemStack(Material.SPYGLASS);
        ItemMeta visualMeta = visualItem.getItemMeta();
        visualMeta.setDisplayName(ChatColor.AQUA + "Visualization Settings");
        List<String> visualLore = new ArrayList<>();
        visualLore.add(ChatColor.GRAY + "Configure area visualization");
        visualLore.add(ChatColor.GRAY + "Set preview options");
        visualLore.add("");
        visualLore.add(ChatColor.YELLOW + "Click to configure");
        visualMeta.setLore(visualLore);
        visualItem.setItemMeta(visualMeta);
        gui.setItem(16, visualItem);

        // Performance Settings
        ItemStack perfItem = new ItemStack(Material.REDSTONE);
        ItemMeta perfMeta = perfItem.getItemMeta();
        perfMeta.setDisplayName(ChatColor.RED + "Performance Settings");
        List<String> perfLore = new ArrayList<>();
        perfLore.add(ChatColor.GRAY + "Configure performance options");
        perfLore.add(ChatColor.GRAY + "Set operation limits");
        perfLore.add("");
        perfLore.add(ChatColor.YELLOW + "Click to configure");
        perfMeta.setLore(perfLore);
        perfItem.setItemMeta(perfMeta);
        gui.setItem(28, perfItem);

        // Storage Settings
        ItemStack storageItem = new ItemStack(Material.ENDER_CHEST);
        ItemMeta storageMeta = storageItem.getItemMeta();
        storageMeta.setDisplayName(ChatColor.DARK_PURPLE + "Storage Settings");
        List<String> storageLore = new ArrayList<>();
        storageLore.add(ChatColor.GRAY + "Configure data storage");
        storageLore.add(ChatColor.GRAY + "Manage file cleanup");
        storageLore.add("");
        storageLore.add(ChatColor.YELLOW + "Click to configure");
        storageMeta.setLore(storageLore);
        storageItem.setItemMeta(storageMeta);
        gui.setItem(30, storageItem);

        // Reload Configuration
        ItemStack reloadItem = new ItemStack(Material.EMERALD);
        ItemMeta reloadMeta = reloadItem.getItemMeta();
        reloadMeta.setDisplayName(ChatColor.GREEN + "Reload Configuration");
        List<String> reloadLore = new ArrayList<>();
        reloadLore.add(ChatColor.GRAY + "Reload plugin configuration");
        reloadLore.add(ChatColor.GRAY + "Apply configuration changes");
        reloadLore.add("");
        reloadLore.add(ChatColor.YELLOW + "Click to reload");
        reloadMeta.setLore(reloadLore);
        reloadItem.setItemMeta(reloadMeta);
        gui.setItem(32, reloadItem);

        // Debug Mode
        ItemStack debugItem = new ItemStack(Material.REPEATER);
        ItemMeta debugMeta = debugItem.getItemMeta();
        debugMeta.setDisplayName(ChatColor.YELLOW + "Debug Mode");
        List<String> debugLore = new ArrayList<>();
        debugLore.add(ChatColor.GRAY + "Enable/disable debug logging");
        debugLore.add(ChatColor.GRAY + "Current: " + ChatColor.YELLOW + "Use console");
        debugLore.add("");
        debugLore.add(ChatColor.YELLOW + "Click to toggle");
        debugMeta.setLore(debugLore);
        debugItem.setItemMeta(debugMeta);
        gui.setItem(34, debugItem);

        // Back button
        ItemStack backItem = new ItemStack(Material.SPECTRAL_ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.GRAY + "Back to Areas");
        backItem.setItemMeta(backMeta);
        gui.setItem(40, backItem);

        player.openInventory(gui);
        guiManager.registerOpenGUI(player, getPageType());
    }

    @Override
    public void handleClick(Player player, InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta())
            return;

        String displayName = item.getItemMeta().getDisplayName();

        if (displayName.contains("Back to Areas")) {
            player.closeInventory();
            guiManager.openAreasGUI(player);
            return;
        } else if (displayName.contains("Backup Settings")) {
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Backup Settings Configuration:");
            player.sendMessage(ChatColor.GREEN + "/rewind config backup.auto-interval <minutes>");
            player.sendMessage(ChatColor.GREEN + "/rewind config backup.max-backups <number>");
            player.sendMessage(ChatColor.GREEN + "/rewind config backup.compression <true/false>");
            return;
        } else if (displayName.contains("Permission Settings")) {
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Permission Settings Configuration:");
            player.sendMessage(ChatColor.GREEN + "/rewind config permissions.default-level <level>");
            player.sendMessage(ChatColor.GREEN + "/rewind config permissions.require-explicit <true/false>");
            return;
        } else if (displayName.contains("Visualization Settings")) {
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Visualization Settings Configuration:");
            player.sendMessage(ChatColor.GREEN + "/rewind config visualization.particle-type <type>");
            player.sendMessage(ChatColor.GREEN + "/rewind config visualization.duration <seconds>");
            return;
        } else if (displayName.contains("Performance Settings")) {
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Performance Settings Configuration:");
            player.sendMessage(ChatColor.GREEN + "/rewind config performance.max-blocks-per-tick <number>");
            player.sendMessage(ChatColor.GREEN + "/rewind config performance.async-operations <true/false>");
            return;
        } else if (displayName.contains("Storage Settings")) {
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Storage Settings Configuration:");
            player.sendMessage(ChatColor.GREEN + "/rewind config storage.cleanup-old-backups <days>");
            player.sendMessage(ChatColor.GREEN + "/rewind config storage.compression-level <1-9>");
            return;
        } else if (displayName.contains("Reload Configuration")) {
            player.closeInventory();
            player.performCommand("rewind reload");
            return;
        } else if (displayName.contains("Debug Mode")) {
            player.closeInventory();
            // Simple toggle without configuration persistence for now
            player.sendMessage(ChatColor.YELLOW + "Debug mode toggle functionality coming soon!");
            player.sendMessage(ChatColor.GREEN + "Use server console commands to enable/disable debug mode");
            return;
        }
    }

    @Override
    public String getPageType() {
        return "settings";
    }
}

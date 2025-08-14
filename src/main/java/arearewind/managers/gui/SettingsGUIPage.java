package arearewind.managers.gui;

import arearewind.listeners.PlayerInteractionListener;
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
 * Comprehensive GUI page for both personal and admin settings
 */
public class SettingsGUIPage implements IGUIPage {

    private final GUIManager guiManager;
    private final PermissionManager permissionManager;
    private final ConfigurationManager configManager;
    private PlayerInteractionListener playerListener;

    public SettingsGUIPage(GUIManager guiManager, PermissionManager permissionManager,
            ConfigurationManager configManager) {
        this.guiManager = guiManager;
        this.permissionManager = permissionManager;
        this.configManager = configManager;
    }

    public void setPlayerInteractionListener(PlayerInteractionListener playerListener) {
        this.playerListener = playerListener;
    }

    @Override
    public void openGUI(Player player) {
        if (!permissionManager.canUseGUI(player)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use the GUI!");
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "Area Rewind Settings");
        boolean isAdmin = permissionManager.hasAdminPermission(player);

        // === PERSONAL SETTINGS (Always available) ===
        if (playerListener != null) {
            // Wooden Hoe Selection Setting
            boolean hoeEnabled = playerListener.getPlayerWoodenHoeMode(player);
            ItemStack hoeItem = new ItemStack(hoeEnabled ? Material.WOODEN_HOE : Material.GRAY_DYE);
            ItemMeta hoeMeta = hoeItem.getItemMeta();
            hoeMeta.setDisplayName(ChatColor.YELLOW + "Wooden Hoe Selection");
            List<String> hoeLore = new ArrayList<>();
            hoeLore.add(ChatColor.GRAY + "Enable wooden hoe for area selection");
            hoeLore.add("");
            hoeLore.add(ChatColor.YELLOW + "Current: " +
                    (hoeEnabled ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
            hoeLore.add("");
            if (hoeEnabled) {
                hoeLore.add(ChatColor.RED + "Click to disable");
            } else {
                hoeLore.add(ChatColor.GREEN + "Click to enable");
            }
            hoeMeta.setLore(hoeLore);
            hoeItem.setItemMeta(hoeMeta);
            gui.setItem(10, hoeItem);

            // Progress Logging Setting
            boolean progressEnabled = playerListener.getPlayerProgressLoggingMode(player);
            ItemStack progressItem = new ItemStack(progressEnabled ? Material.PAPER : Material.GRAY_DYE);
            ItemMeta progressMeta = progressItem.getItemMeta();
            progressMeta.setDisplayName(ChatColor.YELLOW + "Progress Logging");
            List<String> progressLore = new ArrayList<>();
            progressLore.add(ChatColor.GRAY + "Show progress messages during");
            progressLore.add(ChatColor.GRAY + "long restore operations");
            progressLore.add("");
            progressLore.add(ChatColor.YELLOW + "Current: " +
                    (progressEnabled ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
            progressLore.add("");
            if (progressEnabled) {
                progressLore.add(ChatColor.RED + "Click to disable");
                progressLore.add(ChatColor.GRAY + "Hide progress messages");
            } else {
                progressLore.add(ChatColor.GREEN + "Click to enable");
                progressLore.add(ChatColor.GRAY + "Show progress messages");
            }
            progressMeta.setLore(progressLore);
            progressItem.setItemMeta(progressMeta);
            gui.setItem(12, progressItem);
        }

        // === GLOBAL SETTINGS (Admin only) ===
        if (isAdmin) {
            // Auto Backup Toggle
            boolean autoBackupEnabled = configManager.isAutoBackupEnabled();
            ItemStack autoBackupItem = new ItemStack(autoBackupEnabled ? Material.CHEST : Material.GRAY_DYE);
            ItemMeta autoBackupMeta = autoBackupItem.getItemMeta();
            autoBackupMeta.setDisplayName(ChatColor.BLUE + "Auto Backup");
            List<String> autoBackupLore = new ArrayList<>();
            autoBackupLore.add(ChatColor.GRAY + "Enable/disable automatic backups");
            autoBackupLore.add("");
            autoBackupLore.add(ChatColor.YELLOW + "Current: " +
                    (autoBackupEnabled ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
            autoBackupLore.add("");
            if (autoBackupEnabled) {
                autoBackupLore.add(ChatColor.RED + "Click to disable");
            } else {
                autoBackupLore.add(ChatColor.GREEN + "Click to enable");
            }
            autoBackupMeta.setLore(autoBackupLore);
            autoBackupItem.setItemMeta(autoBackupMeta);
            gui.setItem(28, autoBackupItem);

            // Compression Toggle
            boolean compressionEnabled = configManager.isCompressionEnabled();
            ItemStack compressionItem = new ItemStack(compressionEnabled ? Material.PISTON : Material.GRAY_DYE);
            ItemMeta compressionMeta = compressionItem.getItemMeta();
            compressionMeta.setDisplayName(ChatColor.BLUE + "Backup Compression");
            List<String> compressionLore = new ArrayList<>();
            compressionLore.add(ChatColor.GRAY + "Enable/disable backup compression");
            compressionLore.add(ChatColor.GRAY + "Reduces storage space usage");
            compressionLore.add("");
            compressionLore.add(ChatColor.YELLOW + "Current: " +
                    (compressionEnabled ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
            compressionLore.add("");
            if (compressionEnabled) {
                compressionLore.add(ChatColor.RED + "Click to disable");
            } else {
                compressionLore.add(ChatColor.GREEN + "Click to enable");
            }
            compressionMeta.setLore(compressionLore);
            compressionItem.setItemMeta(compressionMeta);
            gui.setItem(30, compressionItem);

            // Global Wooden Hoe Toggle
            boolean globalHoeEnabled = configManager.isWoodenHoeEnabled();
            ItemStack globalHoeItem = new ItemStack(globalHoeEnabled ? Material.GOLDEN_HOE : Material.GRAY_DYE);
            ItemMeta globalHoeMeta = globalHoeItem.getItemMeta();
            globalHoeMeta.setDisplayName(ChatColor.GOLD + "Global Wooden Hoe");
            List<String> globalHoeLore = new ArrayList<>();
            globalHoeLore.add(ChatColor.GRAY + "Global default for wooden hoe selection");
            globalHoeLore.add(ChatColor.GRAY + "Players can override this individually");
            globalHoeLore.add("");
            globalHoeLore.add(ChatColor.YELLOW + "Current: " +
                    (globalHoeEnabled ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
            globalHoeLore.add("");
            if (globalHoeEnabled) {
                globalHoeLore.add(ChatColor.RED + "Click to disable");
            } else {
                globalHoeLore.add(ChatColor.GREEN + "Click to enable");
            }
            globalHoeMeta.setLore(globalHoeLore);
            globalHoeItem.setItemMeta(globalHoeMeta);
            gui.setItem(32, globalHoeItem);

            // Global Progress Logging Toggle
            boolean globalProgressEnabled = configManager.isRestoreProgressLoggingEnabled();
            ItemStack globalProgressItem = new ItemStack(
                    globalProgressEnabled ? Material.WRITABLE_BOOK : Material.GRAY_DYE);
            ItemMeta globalProgressMeta = globalProgressItem.getItemMeta();
            globalProgressMeta.setDisplayName(ChatColor.GOLD + "Global Progress Logging");
            List<String> globalProgressLore = new ArrayList<>();
            globalProgressLore.add(ChatColor.GRAY + "Global default for progress logging");
            globalProgressLore.add(ChatColor.GRAY + "Players can override this individually");
            globalProgressLore.add("");
            globalProgressLore.add(ChatColor.YELLOW + "Current: " +
                    (globalProgressEnabled ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
            globalProgressLore.add("");
            if (globalProgressEnabled) {
                globalProgressLore.add(ChatColor.RED + "Click to disable");
            } else {
                globalProgressLore.add(ChatColor.GREEN + "Click to enable");
            }
            globalProgressMeta.setLore(globalProgressLore);
            globalProgressItem.setItemMeta(globalProgressMeta);
            gui.setItem(34, globalProgressItem);

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
            gui.setItem(16, reloadItem);
        }

        // === INFORMATION ===
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(ChatColor.GOLD + "Plugin Information");
        List<String> infoLore = new ArrayList<>();
        infoLore.add(ChatColor.GRAY + "Area Rewind Plugin");
        infoLore.add(ChatColor.GRAY + "Version: 1.0.6-SNAPSHOT");
        infoLore.add(ChatColor.GRAY + "Manage area backups and restoration");
        infoLore.add("");
        if (playerListener != null) {
            infoLore.add(ChatColor.AQUA + "Personal Settings Available");
        }
        if (isAdmin) {
            infoLore.add(ChatColor.LIGHT_PURPLE + "Admin Settings Available");
        } else {
            infoLore.add(ChatColor.GRAY + "Admin settings require permission");
        }
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        gui.setItem(4, infoItem);

        // Back button
        ItemStack backItem = new ItemStack(Material.SPECTRAL_ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.GRAY + "Back to Areas");
        backItem.setItemMeta(backMeta);
        gui.setItem(49, backItem);

        player.openInventory(gui);
        guiManager.registerOpenGUI(player, getPageType());
    }

    @Override
    public void handleClick(Player player, InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta())
            return;

        String displayName = item.getItemMeta().getDisplayName();
        boolean isAdmin = permissionManager.hasAdminPermission(player);

        if (displayName.contains("Back to Areas")) {
            player.closeInventory();
            guiManager.openAreasGUI(player);
            return;
        }

        // === PERSONAL SETTINGS ===
        if (playerListener != null) {
            if (displayName.contains("Wooden Hoe Selection")) {
                // Toggle personal wooden hoe setting
                boolean current = playerListener.getPlayerWoodenHoeMode(player);
                playerListener.setPlayerWoodenHoeMode(player, !current);

                // Refresh the GUI to show the new state
                player.closeInventory();
                openGUI(player);
                return;
            } else if (displayName.contains("Progress Logging") && !displayName.contains("Global")) {
                // Toggle personal progress logging setting
                boolean current = playerListener.getPlayerProgressLoggingMode(player);
                playerListener.setPlayerProgressLoggingMode(player, !current);

                // Refresh the GUI to show the new state
                player.closeInventory();
                openGUI(player);
                return;
            }
        }

        // === ADMIN SETTINGS ===
        if (!isAdmin) {
            player.sendMessage(ChatColor.RED + "You need admin permissions to change global settings!");
            return;
        }

        if (displayName.contains("Auto Backup")) {
            // Toggle auto backup setting
            boolean current = configManager.isAutoBackupEnabled();
            configManager.setAutoBackupEnabled(!current);

            player.sendMessage(ChatColor.GREEN + "Auto backup " +
                    (!current ? "enabled" : "disabled") + "!");

            // Refresh the GUI to show the new state
            player.closeInventory();
            openGUI(player);
            return;
        } else if (displayName.contains("Backup Compression")) {
            // Toggle compression setting
            boolean current = configManager.isCompressionEnabled();
            configManager.setCompressionEnabled(!current);

            player.sendMessage(ChatColor.GREEN + "Backup compression " +
                    (!current ? "enabled" : "disabled") + "!");

            // Refresh the GUI to show the new state
            player.closeInventory();
            openGUI(player);
            return;
        } else if (displayName.contains("Global Wooden Hoe")) {
            // Toggle global wooden hoe setting
            boolean current = configManager.isWoodenHoeEnabled();
            configManager.setWoodenHoeEnabled(!current);

            player.sendMessage(ChatColor.GREEN + "Global wooden hoe selection " +
                    (!current ? "enabled" : "disabled") + "!");

            // Refresh the GUI to show the new state
            player.closeInventory();
            openGUI(player);
            return;
        } else if (displayName.contains("Global Progress Logging")) {
            // Toggle global progress logging setting
            boolean current = configManager.isRestoreProgressLoggingEnabled();
            configManager.setRestoreProgressLoggingEnabled(!current);

            player.sendMessage(ChatColor.GREEN + "Global progress logging " +
                    (!current ? "enabled" : "disabled") + "!");

            // Refresh the GUI to show the new state
            player.closeInventory();
            openGUI(player);
            return;
        } else if (displayName.contains("Reload Configuration")) {
            player.closeInventory();
            player.performCommand("rewind reload");
            return;
        }
    }

    @Override
    public String getPageType() {
        return "settings";
    }
}

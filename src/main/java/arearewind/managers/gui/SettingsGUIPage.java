package arearewind.managers.gui;

import arearewind.listeners.PlayerInteractionListener;
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

/**
 * GUI page for personal settings
 */
public class SettingsGUIPage implements IGUIPage {

    private final GUIManager guiManager;
    private final PermissionManager permissionManager;
    private PlayerInteractionListener playerListener;

    public SettingsGUIPage(GUIManager guiManager, PermissionManager permissionManager) {
        this.guiManager = guiManager;
        this.permissionManager = permissionManager;
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
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        gui.setItem(4, infoItem);

        // Back button
        ItemStack backItem = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.GRAY + "Back to My Areas");
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

        if (displayName.contains("Back to My Areas")) {
            player.closeInventory();
            guiManager.openMyAreasGUI(player);
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
            } else if (displayName.contains("Progress Logging")) {
                // Toggle personal progress logging setting
                boolean current = playerListener.getPlayerProgressLoggingMode(player);
                playerListener.setPlayerProgressLoggingMode(player, !current);

                // Refresh the GUI to show the new state
                player.closeInventory();
                openGUI(player);
                return;
            }
        }
    }

    @Override
    public String getPageType() {
        return "settings";
    }
}

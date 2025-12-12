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

        Inventory gui = Bukkit.createInventory(null, 45, ChatColor.DARK_PURPLE + "⚙ Personal Settings");

        // Fill background
        fillBackground(gui);

        // === HEADER ===
        addHeader(gui, player);

        // === PERSONAL SETTINGS (Always available) ===
        if (playerListener != null) {
            addPersonalSettings(gui, player);
        }

        // === NAVIGATION BAR ===
        addNavigationBar(gui);

        player.openInventory(gui);
        guiManager.registerOpenGUI(player, getPageType());
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

    private void addHeader(Inventory gui, Player player) {
        // Plugin Info header (center top)
        ItemStack infoItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(ChatColor.GOLD + "✦ Area Rewind");
        List<String> infoLore = new ArrayList<>();
        infoLore.add("");
        infoLore.add(ChatColor.GRAY + "━━━━━━━━━━━━━━━━");
        infoLore.add(ChatColor.WHITE + "Version: " + ChatColor.AQUA + "1.0.9-SNAPSHOT");
        infoLore.add(ChatColor.GRAY + "━━━━━━━━━━━━━━━━");
        infoLore.add("");
        infoLore.add(ChatColor.GRAY + "Manage area backups and");
        infoLore.add(ChatColor.GRAY + "restoration with ease.");
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        gui.setItem(4, infoItem);

        // Section header for settings
        ItemStack sectionHeader = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta sectionMeta = sectionHeader.getItemMeta();
        sectionMeta.setDisplayName(ChatColor.AQUA + "▼ Your Preferences");
        sectionHeader.setItemMeta(sectionMeta);
        gui.setItem(13, sectionHeader);
    }

    private void addPersonalSettings(Inventory gui, Player player) {
        // Wooden Hoe Selection Setting - slot 20
        boolean hoeEnabled = playerListener.getPlayerWoodenHoeMode(player);
        ItemStack hoeItem = new ItemStack(hoeEnabled ? Material.WOODEN_HOE : Material.BARRIER);
        ItemMeta hoeMeta = hoeItem.getItemMeta();
        hoeMeta.setDisplayName(ChatColor.YELLOW + "🪓 Wooden Hoe Selection");
        List<String> hoeLore = new ArrayList<>();
        hoeLore.add("");
        hoeLore.add(ChatColor.GRAY + "Use a wooden hoe to");
        hoeLore.add(ChatColor.GRAY + "select area corners.");
        hoeLore.add("");
        hoeLore.add(ChatColor.GRAY + "━━━━━━━━━━━━━━━━");
        hoeLore.add(ChatColor.WHITE + "Status: " + 
                (hoeEnabled ? ChatColor.GREEN + "✔ Enabled" : ChatColor.RED + "✘ Disabled"));
        hoeLore.add("");
        hoeLore.add((hoeEnabled ? ChatColor.RED + "▶ Click to disable" : ChatColor.GREEN + "▶ Click to enable"));
        hoeMeta.setLore(hoeLore);
        hoeItem.setItemMeta(hoeMeta);
        gui.setItem(20, hoeItem);

        // Progress Logging Setting - slot 24
        boolean progressEnabled = playerListener.getPlayerProgressLoggingMode(player);
        ItemStack progressItem = new ItemStack(progressEnabled ? Material.WRITABLE_BOOK : Material.BARRIER);
        ItemMeta progressMeta = progressItem.getItemMeta();
        progressMeta.setDisplayName(ChatColor.AQUA + "📝 Progress Messages");
        List<String> progressLore = new ArrayList<>();
        progressLore.add("");
        progressLore.add(ChatColor.GRAY + "Show progress updates");
        progressLore.add(ChatColor.GRAY + "during restore operations.");
        progressLore.add("");
        progressLore.add(ChatColor.GRAY + "━━━━━━━━━━━━━━━━");
        progressLore.add(ChatColor.WHITE + "Status: " +
                (progressEnabled ? ChatColor.GREEN + "✔ Enabled" : ChatColor.RED + "✘ Disabled"));
        progressLore.add("");
        progressLore.add((progressEnabled ? ChatColor.RED + "▶ Click to disable" : ChatColor.GREEN + "▶ Click to enable"));
        progressMeta.setLore(progressLore);
        progressItem.setItemMeta(progressMeta);
        gui.setItem(24, progressItem);
    }

    private void addNavigationBar(Inventory gui) {
        // Black glass for nav bar
        ItemStack navFiller = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta navMeta = navFiller.getItemMeta();
        navMeta.setDisplayName(" ");
        navFiller.setItemMeta(navMeta);
        for (int i = 36; i < 45; i++) {
            gui.setItem(i, navFiller.clone());
        }

        // Back button - slot 40
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.GRAY + "◀ Back to My Areas");
        List<String> backLore = new ArrayList<>();
        backLore.add("");
        backLore.add(ChatColor.GRAY + "Return to your");
        backLore.add(ChatColor.GRAY + "areas overview.");
        backMeta.setLore(backLore);
        backItem.setItemMeta(backMeta);
        gui.setItem(40, backItem);
    }

    @Override
    public void handleClick(Player player, InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta())
            return;

        String displayName = item.getItemMeta().getDisplayName();
        
        // Ignore filler glass panes
        if (displayName.equals(" ") || displayName.startsWith("▼")) {
            return;
        }

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
            } else if (displayName.contains("Progress")) {
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

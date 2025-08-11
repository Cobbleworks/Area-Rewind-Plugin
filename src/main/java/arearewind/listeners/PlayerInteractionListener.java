package arearewind.listeners;

import arearewind.managers.AreaManager;
import arearewind.managers.PermissionManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerInteractionListener implements Listener {

    private final JavaPlugin plugin;
    private final AreaManager areaManager;
    private final Map<UUID, Long> lastToolUsage = new HashMap<>();
    private static final long TOOL_COOLDOWN = 100;
    private static final Material SELECTION_TOOL = Material.WOODEN_HOE;
    private static final String TOOL_NAME = "Area Selection Tool";

    public PlayerInteractionListener(JavaPlugin plugin, AreaManager areaManager) {
        this.plugin = plugin;
        this.areaManager = areaManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = (Player) event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() != SELECTION_TOOL) {
            return;
        }

        if (!player.hasPermission(PermissionManager.PERMISSION_USE)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use the area selection tool!");
            player.sendMessage(ChatColor.GRAY + "Required permission: " + PermissionManager.PERMISSION_USE);
            return;
        }

        if (isOnCooldown(player)) {
            return;
        }

        boolean isSpecialTool = isSelectionTool(item);

        if (event.getClickedBlock() == null) {
            if (event.getAction().name().contains("RIGHT")) {
                if (isSpecialTool) {
                    showToolInfo(player);
                } else {
                    showBasicSelectionInfo(player);
                }
                event.setCancelled(true);
            }
            return;
        }

        Location clickedLocation = event.getClickedBlock().getLocation();

        if (event.getAction().name().contains("LEFT")) {
            handleLeftClick(player, clickedLocation);
            event.setCancelled(true);
        } else if (event.getAction().name().contains("RIGHT")) {
            handleRightClick(player, clickedLocation);
            event.setCancelled(true);
        }

        updateLastUsage(player);
    }

    private void handleLeftClick(Player player, Location location) {
        areaManager.setPosition1(player.getUniqueId(), location);
        String locationStr = areaManager.locationToString(location);
        player.sendMessage(ChatColor.GREEN + "Position 1 set: " + ChatColor.WHITE + locationStr);

        if (areaManager.hasValidSelection(player.getUniqueId())) {
            showSelectionInfo(player);
        }
    }

    private void handleRightClick(Player player, Location location) {
        areaManager.setPosition2(player.getUniqueId(), location);
        String locationStr = areaManager.locationToString(location);
        player.sendMessage(ChatColor.GREEN + "Position 2 set: " + ChatColor.WHITE + locationStr);

        if (areaManager.hasValidSelection(player.getUniqueId())) {
            showSelectionInfo(player);
        }
    }

    private void showSelectionInfo(Player player) {
        String selectionInfo = areaManager.getSelectionInfo(player.getUniqueId());
        player.sendMessage(ChatColor.YELLOW + "Selection: " + ChatColor.WHITE + selectionInfo);
        player.sendMessage(ChatColor.GRAY + "Use " + ChatColor.GREEN + "/rewind save <name>" +
                ChatColor.GRAY + " to create a protected area");
    }

    private void showBasicSelectionInfo(Player player) {
        String selectionInfo = areaManager.getSelectionInfo(player.getUniqueId());

        player.sendMessage(ChatColor.GOLD + "=== Area Selection (Wooden Hoe) ===");
        player.sendMessage(ChatColor.YELLOW + "Current selection: " + ChatColor.WHITE + selectionInfo);

        if (areaManager.hasValidSelection(player.getUniqueId())) {
            player.sendMessage(ChatColor.GREEN + "Ready to create area! Use /rewind save <name>");
        } else {
            player.sendMessage(ChatColor.GRAY + "Left click: Set position 1");
            player.sendMessage(ChatColor.GRAY + "Right click: Set position 2");
        }
    }

    private void showToolInfo(Player player) {
        String selectionInfo = areaManager.getSelectionInfo(player.getUniqueId());

        player.sendMessage(ChatColor.GOLD + "=== Area Selection Tool ===");
        player.sendMessage(ChatColor.YELLOW + "Current selection: " + ChatColor.WHITE + selectionInfo);

        if (areaManager.hasValidSelection(player.getUniqueId())) {
            player.sendMessage(ChatColor.GREEN + "Ready to create area! Use /rewind save <name>");
        } else {
            player.sendMessage(ChatColor.GRAY + "Click blocks to set positions");
        }
    }

    private boolean isOnCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long lastUsage = lastToolUsage.getOrDefault(playerId, 0L);
        return (currentTime - lastUsage) < TOOL_COOLDOWN;
    }

    private void updateLastUsage(Player player) {
        lastToolUsage.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission(PermissionManager.PERMISSION_USE)) {
            giveSelectionToolIfNeeded(player);
        }

        if (!player.hasPlayedBefore()) {
            showWelcomeMessage(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        lastToolUsage.remove(playerId);
    }

    private void giveSelectionToolIfNeeded(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == SELECTION_TOOL) {
                return;
            }
        }

        ItemStack selectionTool = createSelectionTool();
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(selectionTool);
            player.sendMessage(ChatColor.YELLOW + "You received the " + TOOL_NAME + "!");
            player.sendMessage(ChatColor.GRAY + "Left click to set position 1, right click to set position 2");
            player.sendMessage(ChatColor.AQUA + "Note: Any wooden hoe can be used for area selection!");
        } else {
            player.getWorld().dropItem(player.getLocation(), selectionTool);
            player.sendMessage(ChatColor.YELLOW + "Selection tool dropped at your feet (inventory full)");
        }
    }

    public static boolean isSelectionTool(ItemStack item) {
        if (item == null || item.getType() != SELECTION_TOOL) {
            return false;
        }

        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }

        return item.getItemMeta().getDisplayName().contains(TOOL_NAME);
    }

    public static ItemStack createSelectionTool() {
        ItemStack tool = new ItemStack(SELECTION_TOOL);
        ItemMeta meta = tool.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + TOOL_NAME);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Left click: Set position 1");
            lore.add(ChatColor.GRAY + "Right click: Set position 2");
            lore.add("");
            lore.add(ChatColor.GREEN + "Use /rewind save <name> to create area");
            lore.add("");
            lore.add(ChatColor.AQUA + "Note: Any wooden hoe works for selection!");
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "Area Rewind Plugin");
            meta.setLore(lore);

            tool.setItemMeta(meta);
        }

        return tool;
    }

    private void showWelcomeMessage(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "=== Welcome to Area Rewind ===");
        player.sendMessage(ChatColor.YELLOW + "This plugin helps protect your builds with automatic backups!");
        player.sendMessage("");
        player.sendMessage(ChatColor.GREEN + "Getting Started:");
        player.sendMessage(ChatColor.WHITE + "1. Use ANY wooden hoe to select an area");
        player.sendMessage(ChatColor.WHITE + "2. Use " + ChatColor.GREEN + "/rewind save <name>" + ChatColor.WHITE + " to protect it");
        player.sendMessage(ChatColor.WHITE + "3. Use " + ChatColor.GREEN + "/rewind help" + ChatColor.WHITE + " for more commands");
        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY + "Tip: Type " + ChatColor.GREEN + "/rewind gui" + ChatColor.GRAY + " for an easy interface!");
        player.sendMessage(ChatColor.AQUA + "Any wooden hoe can be used for area selection!");
        player.sendMessage("");
    }
}
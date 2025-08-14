package arearewind.listeners;

import arearewind.managers.AreaManager;
import arearewind.managers.PermissionManager;
import arearewind.util.ConfigurationManager;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.SessionManager;
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
    private final ConfigurationManager configManager;
    private final Map<UUID, Long> lastToolUsage = new HashMap<>();
    private final Map<UUID, Boolean> playerWoodenHoeEnabled = new HashMap<>(); // Per-player wooden hoe override
    private final Map<UUID, Boolean> playerProgressLoggingEnabled = new HashMap<>(); // Per-player progress logging
                                                                                     // override
    private static final long TOOL_COOLDOWN = 100;
    private static final Material SELECTION_TOOL = Material.WOODEN_HOE;
    private static final String TOOL_NAME = "Area Selection Tool";

    // WorldEdit integration
    private WorldEditPlugin worldEditPlugin;
    private boolean worldEditEnabled = false;

    public PlayerInteractionListener(JavaPlugin plugin, AreaManager areaManager, ConfigurationManager configManager) {
        this.plugin = plugin;
        this.areaManager = areaManager;
        this.configManager = configManager;
        initializeWorldEdit();
    }

    private void initializeWorldEdit() {
        try {
            worldEditPlugin = (WorldEditPlugin) plugin.getServer().getPluginManager().getPlugin("WorldEdit");
            if (worldEditPlugin != null && worldEditPlugin.isEnabled()) {
                worldEditEnabled = true;
                plugin.getLogger().info("WorldEdit integration enabled!");
            }
        } catch (Exception e) {
            plugin.getLogger().info("WorldEdit not found or incompatible version. Using built-in selection only.");
            worldEditEnabled = false;
        }
    }

    private boolean isWoodenHoeEnabledForPlayer(Player player) {
        UUID playerId = player.getUniqueId();

        // Check if player has explicitly enabled/disabled wooden hoe mode
        if (playerWoodenHoeEnabled.containsKey(playerId)) {
            return playerWoodenHoeEnabled.get(playerId);
        }

        // Check auto-fallback setting if WorldEdit is not available or failed
        if (configManager.isWoodenHoeAutoFallbackEnabled()) {
            return !worldEditEnabled || hasWorldEditFailed(player);
        }

        return false;
    }

    private boolean hasWorldEditFailed(Player player) {
        // This could be enhanced to track WorldEdit failures per player
        // For now, just check if WorldEdit is enabled
        return !worldEditEnabled;
    }

    public void setPlayerWoodenHoeMode(Player player, boolean enabled) {
        playerWoodenHoeEnabled.put(player.getUniqueId(), enabled);

        String status = enabled ? "enabled" : "disabled";
        player.sendMessage(ChatColor.GREEN + "Wooden hoe selection " + status + " for you!");

        if (enabled) {
            player.sendMessage(ChatColor.GRAY + "You can now use wooden hoes for area selection.");
            giveSelectionToolIfNeeded(player);
        } else {
            player.sendMessage(
                    ChatColor.GRAY + "Wooden hoe selection disabled. WorldEdit wand will be used if available.");
        }
    }

    public boolean getPlayerWoodenHoeMode(Player player) {
        return isWoodenHoeEnabledForPlayer(player);
    }

    public void setPlayerProgressLoggingMode(Player player, boolean enabled) {
        playerProgressLoggingEnabled.put(player.getUniqueId(), enabled);

        String status = enabled ? "enabled" : "disabled";
        player.sendMessage(ChatColor.GREEN + "Restore progress logging " + status + " for you!");

        if (enabled) {
            player.sendMessage(ChatColor.GRAY + "You will receive progress messages during long restore operations.");
        } else {
            player.sendMessage(ChatColor.GRAY + "Progress messages will be hidden during restore operations.");
        }
    }

    public boolean getPlayerProgressLoggingMode(Player player) {
        return isProgressLoggingEnabledForPlayer(player);
    }

    private boolean isProgressLoggingEnabledForPlayer(Player player) {
        UUID playerId = player.getUniqueId();

        // Check if player has explicitly enabled/disabled progress logging
        if (playerProgressLoggingEnabled.containsKey(playerId)) {
            return playerProgressLoggingEnabled.get(playerId);
        }

        // Default to true if no preference is set
        return true;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = (Player) event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if it's our selection tool or WorldEdit wand
        boolean isOurTool = item != null && item.getType() == SELECTION_TOOL && isWoodenHoeEnabledForPlayer(player);
        boolean isWorldEditWand = worldEditEnabled && isWorldEditWand(player, item);

        if (!isOurTool && !isWorldEditWand) {
            return;
        }

        if (!player.hasPermission(PermissionManager.PERMISSION_USE)) {
            // Only show message for our tool, let WorldEdit handle its own permissions
            if (isOurTool) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use the area selection tool!");
                player.sendMessage(ChatColor.GRAY + "Required permission: " + PermissionManager.PERMISSION_USE);
            }
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
                } else if (isOurTool) {
                    showBasicSelectionInfo(player);
                } else if (isWorldEditWand) {
                    showWorldEditSelectionInfo(player);
                }

                // Don't cancel WorldEdit wand events
                if (isOurTool) {
                    event.setCancelled(true);
                }
            }
            return;
        }

        Location clickedLocation = event.getClickedBlock().getLocation();

        // Handle our tool interactions (only if wooden hoe is enabled for this player)
        if (isOurTool) {
            if (event.getAction().name().contains("LEFT")) {
                handleLeftClick(player, clickedLocation);
                event.setCancelled(true);
            } else if (event.getAction().name().contains("RIGHT")) {
                handleRightClick(player, clickedLocation);
                event.setCancelled(true);
            }
            updateLastUsage(player);
        }
        // For WorldEdit wand, we let WorldEdit handle the selection, then sync
        // afterwards
        else if (isWorldEditWand) {
            // Delay syncing to let WorldEdit process the selection first
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                syncWorldEditSelection(player);
            }, 1L);
            updateLastUsage(player);
        }
    }

    // Better method: Check if player is holding WorldEdit wand
    private boolean isWorldEditWand(Player player, ItemStack item) {
        if (!worldEditEnabled || item == null) {
            return false;
        }

        try {
            // Simple but effective: check if it's a wooden axe AND the player has WorldEdit
            // permissions
            // This covers 99% of use cases since most people use the default wand
            if (item.getType() == Material.WOODEN_AXE && player.hasPermission("worldedit.wand")) {
                return true;
            }

            // TODO: For advanced users who change their wand, we could add a config option
            // or check WorldEdit's session data, but this adds complexity
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private void syncWorldEditSelection(Player player) {
        if (!worldEditEnabled) {
            return;
        }

        try {
            SessionManager sessionManager = WorldEdit.getInstance().getSessionManager();
            LocalSession session = sessionManager.get(BukkitAdapter.adapt(player));

            // Check if player has a selection in the current world
            com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(player.getWorld());
            Region region = session.getSelection(weWorld);
            if (region != null) {
                // Get the bounding box for any type of WorldEdit selection
                // This ensures we always work with a rectangular area
                com.sk89q.worldedit.math.BlockVector3 min = region.getMinimumPoint();
                com.sk89q.worldedit.math.BlockVector3 max = region.getMaximumPoint();

                Location pos1 = new Location(player.getWorld(), min.getX(), min.getY(), min.getZ());
                Location pos2 = new Location(player.getWorld(), max.getX(), max.getY(), max.getZ());

                // Validate selection size (prevent huge selections)
                long blockCount = region.getVolume();
                if (blockCount > 1000000) { // 1M block limit
                    player.sendMessage(ChatColor.RED + "Selection too large! Maximum 1,000,000 blocks.");
                    return;
                }

                areaManager.setPosition1(player.getUniqueId(), pos1);
                areaManager.setPosition2(player.getUniqueId(), pos2);

                // Provide feedback about the conversion
                // showSelectionInfo(player);
            }
        } catch (IncompleteRegionException e) {
            // Selection is incomplete, this is normal - don't spam the player
        } catch (Exception e) {
            plugin.getLogger()
                    .warning("Failed to sync WorldEdit selection for " + player.getName() + ": " + e.getMessage());
            player.sendMessage(
                    ChatColor.YELLOW + "Could not sync WorldEdit selection. Try again or use wooden hoe instead.");
        }
    }

    private void showWorldEditSelectionInfo(Player player) {
        String selectionInfo = areaManager.getSelectionInfo(player.getUniqueId());

        player.sendMessage(ChatColor.GOLD + "=== Area Selection (WorldEdit) ===");
        player.sendMessage(ChatColor.YELLOW + "Current selection: " + ChatColor.WHITE + selectionInfo);

        if (worldEditEnabled) {
            player.sendMessage(
                    ChatColor.AQUA + "WorldEdit selections are automatically converted to rectangular areas");
            player.sendMessage(ChatColor.GRAY + "Supported: Cuboid, Polygon, Ellipsoid, Cylinder, Sphere, etc.");
        }

        if (areaManager.hasValidSelection(player.getUniqueId())) {
            player.sendMessage(ChatColor.GREEN + "Ready to create area! Use /rewind save <name>");
        }
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
        // Keep wooden hoe preference across sessions
        // playerWoodenHoeEnabled.remove(playerId);
        // Keep progress logging preference across sessions
        // playerProgressLoggingEnabled.remove(playerId);
    }

    private void giveSelectionToolIfNeeded(Player player) {
        // Only give tool if wooden hoe mode is enabled for this player
        if (!isWoodenHoeEnabledForPlayer(player)) {
            return;
        }

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

            if (worldEditEnabled) {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "WorldEdit wand is also supported!");
            }
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
            lore.add(ChatColor.LIGHT_PURPLE + "WorldEdit wand converts all selection types!");
            lore.add(ChatColor.GRAY + "(cuboid, polygon, ellipsoid, cylinder, etc.)");
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

        if (worldEditEnabled) {
            player.sendMessage(ChatColor.WHITE + "1. Use your " + ChatColor.LIGHT_PURPLE + "WorldEdit wand"
                    + ChatColor.WHITE + " to select an area");
            player.sendMessage(ChatColor.GRAY + "   (Any selection type: cuboid, polygon, ellipsoid, etc.)");
            player.sendMessage(ChatColor.GRAY + "   (Wooden hoe is available as fallback)");
        } else {
            player.sendMessage(ChatColor.WHITE + "1. Use " + ChatColor.GREEN + "/rewind tool" + ChatColor.WHITE
                    + " to enable wooden hoe selection");
            player.sendMessage(ChatColor.GRAY + "   (WorldEdit not detected)");
        }

        player.sendMessage(ChatColor.WHITE + "2. Use " + ChatColor.GREEN + "/rewind save <name>" + ChatColor.WHITE
                + " to protect it");
        player.sendMessage(ChatColor.WHITE + "3. Use " + ChatColor.GREEN + "/rewind help" + ChatColor.WHITE
                + " for more commands");
        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY + "Tip: Type " + ChatColor.GREEN + "/rewind gui" + ChatColor.GRAY
                + " for an easy interface!");

        if (!worldEditEnabled) {
            player.sendMessage(ChatColor.YELLOW + "Use " + ChatColor.GREEN + "/rewind tool enable" + ChatColor.YELLOW
                    + " to enable wooden hoe selection!");
        }

        player.sendMessage("");
    }

    // Public method to check if WorldEdit is enabled (for other classes)
    public boolean isWorldEditEnabled() {
        return worldEditEnabled;
    }
}

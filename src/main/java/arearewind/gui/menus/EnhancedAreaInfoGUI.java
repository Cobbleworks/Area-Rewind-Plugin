package arearewind.gui.menus;

import arearewind.data.ProtectedArea;
import arearewind.gui.utils.BaseGUI;
import arearewind.gui.utils.GUIUtils;
import arearewind.gui.utils.ItemBuilder;
import arearewind.managers.AreaManager;
import arearewind.managers.BackupManager;
import arearewind.managers.PermissionManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Enhanced Area Information GUI - detailed area information and quick actions
 */
public class EnhancedAreaInfoGUI extends BaseGUI {
    private final PermissionManager permissionManager;
    private final AreaManager areaManager;
    private final BackupManager backupManager;
    private final BaseGUI parentGUI;
    private final String areaName;
    private final ProtectedArea area;
    
    public EnhancedAreaInfoGUI(JavaPlugin plugin, PermissionManager permissionManager, 
                             BaseGUI parentGUI, String areaName) {
        super(plugin, "&a&lArea Info: &f" + areaName, 45);
        this.permissionManager = permissionManager;
        this.parentGUI = parentGUI;
        this.areaName = areaName;
        this.areaManager = ((arearewind.AreaRewindPlugin) plugin).getAreaManager();
        this.backupManager = ((arearewind.AreaRewindPlugin) plugin).getBackupManager();
        this.area = areaManager.getArea(areaName);
    }
    
    @Override
    public void initialize() {
        if (area == null) {
            inventory.setItem(22, ItemBuilder.createInfoItem("Error", "&cArea not found!"));
            GUIUtils.addNavigationButtons(this, parentGUI, false);
            return;
        }
        
        // Fill border
        GUIUtils.fillBorder(this, GUIUtils.Slots.BORDER_45, Material.GREEN_STAINED_GLASS_PANE);
        
        // Add navigation buttons
        GUIUtils.addNavigationButtons(this, parentGUI, true);
        
        // Basic area information (center top)
        String ownerName = Bukkit.getOfflinePlayer(area.getOwner()).getName();
        inventory.setItem(4, new ItemBuilder(Material.GRASS_BLOCK)
                .name("&a&l" + areaName)
                .lore("&7Basic area information")
                .glow()
                .build());
        
        // Information panels
        addInformationPanels(ownerName);
        
        // Action buttons
        addActionButtons();
    }
    
    private void addInformationPanels(String ownerName) {
        // Owner information
        inventory.setItem(10, new ItemBuilder(Material.PLAYER_HEAD)
                .name("&e&lOwnership")
                .lore("&7Owner: &f" + ownerName,
                      "&7UUID: &f" + area.getOwner().toString(),
                      "&7Trusted Players: &f" + area.getTrustedPlayers().size())
                .build());
        
        // Location information
        inventory.setItem(11, new ItemBuilder(Material.COMPASS)
                .name("&b&lLocation")
                .lore("&7World: &f" + area.getPos1().getWorld().getName(),
                      "&7Position 1: &f" + formatLocation(area.getPos1()),
                      "&7Position 2: &f" + formatLocation(area.getPos2()),
                      "&7Area Size: &f" + area.getSize() + " blocks",
                      "&7Dimensions: &f" + getDimensions())
                .build());
        
        // Backup information
        int backupCount = backupManager.getBackupHistory(areaName).size();
        inventory.setItem(12, new ItemBuilder(Material.CHEST)
                .name("&9&lBackup Status")
                .lore("&7Total Backups: &f" + backupCount,
                      "&7Can Undo: &f" + (backupManager.canUndo(areaName) ? "Yes" : "No"),
                      "&7Can Redo: &f" + (backupManager.canRedo(areaName) ? "Yes" : "No"),
                      "&7Undo Position: &f" + backupManager.getUndoPointer(areaName))
                .build());
        
        // Permission information
        inventory.setItem(13, new ItemBuilder(Material.NAME_TAG)
                .name("&6&lPermissions")
                .lore("&7Your access level in this area",
                      "",
                      "&7Click for detailed permissions")
                .build());
        setAction(13, (player, event) -> {
            player.closeInventory();
            player.performCommand("rewind permissions " + areaName);
        });
        
        // Statistics
        inventory.setItem(14, new ItemBuilder(Material.PAPER)
                .name("&7&lStatistics")
                .lore("&7Area creation date: &fUnknown", // Could be added to ProtectedArea
                      "&7Last backup: &f" + getLastBackupTime(),
                      "&7Total restorations: &fN/A") // Could track this
                .build());
    }
    
    private void addActionButtons() {
        // Quick actions row
        inventory.setItem(19, new ItemBuilder(Material.ENDER_PEARL)
                .name("&d&lTeleport")
                .lore("&7Teleport to this area")
                .build());
        setAction(19, (player, event) -> {
            player.closeInventory();
            player.performCommand("rewind teleport " + areaName);
        });
        
        inventory.setItem(20, new ItemBuilder(Material.CHEST)
                .name("&9&lManage Backups")
                .lore("&7Open backup management")
                .build());
        setAction(20, (player, event) -> {
            new EnhancedAreaBackupGUI(plugin, permissionManager, this, areaName).open(player);
        });
        
        inventory.setItem(21, new ItemBuilder(Material.NAME_TAG)
                .name("&e&lManage Trust")
                .lore("&7Manage trusted players")
                .build());
        setAction(21, (player, event) -> {
            // Would open trust management GUI
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Trust management GUI coming soon!");
        });
        
        inventory.setItem(22, new ItemBuilder(Material.REDSTONE)
                .name("&c&lVisualize")
                .lore("&7Show area boundaries")
                .build());
        setAction(22, (player, event) -> {
            player.closeInventory();
            player.performCommand("rewind show " + areaName);
        });
        
        inventory.setItem(23, new ItemBuilder(Material.GOLDEN_AXE)
                .name("&6&lEdit Boundaries")
                .lore("&7Expand or contract area")
                .build());
        setAction(23, (player, event) -> {
            // Would open boundary edit GUI
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Boundary editing GUI coming soon!");
            player.sendMessage(ChatColor.GRAY + "Use: /rewind expand " + areaName + " <direction> <amount>");
            player.sendMessage(ChatColor.GRAY + "Use: /rewind contract " + areaName + " <direction> <amount>");
        });
        
        inventory.setItem(24, new ItemBuilder(Material.WRITABLE_BOOK)
                .name("&f&lExport Area")
                .lore("&7Export area data")
                .build());
        setAction(24, (player, event) -> {
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Area export functionality coming soon!");
        });
        
        inventory.setItem(25, new ItemBuilder(Material.TNT)
                .name("&c&lDelete Area")
                .lore("&cPermanently delete this area",
                      "&cThis action cannot be undone!")
                .build());
        setAction(25, (player, event) -> {
            if (permissionManager.canDeleteArea(player, area)) {
                new ConfirmationGUI(plugin,
                        "&c&lDelete Area: " + areaName,
                        "Are you sure you want to delete this area?",
                        "This action cannot be undone!",
                        () -> {
                            player.performCommand("rewind delete " + areaName);
                            parentGUI.open(player);
                        },
                        () -> this.open(player),
                        this).open(player);
            } else {
                player.sendMessage(ChatColor.RED + "You don't have permission to delete this area!");
            }
        });
    }
    
    private String formatLocation(org.bukkit.Location loc) {
        return String.format("%d, %d, %d", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
    
    private String getDimensions() {
        int x = Math.abs(area.getPos2().getBlockX() - area.getPos1().getBlockX()) + 1;
        int y = Math.abs(area.getPos2().getBlockY() - area.getPos1().getBlockY()) + 1;
        int z = Math.abs(area.getPos2().getBlockZ() - area.getPos1().getBlockZ()) + 1;
        return x + "×" + y + "×" + z;
    }
    
    private String getLastBackupTime() {
        var backups = backupManager.getBackupHistory(areaName);
        if (backups.isEmpty()) {
            return "Never";
        }
        return backups.get(backups.size() - 1).getTimestamp().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
    
    @Override
    public void handleClick(Player player, InventoryClickEvent event) {
        // All clicks handled by specific actions
    }
}

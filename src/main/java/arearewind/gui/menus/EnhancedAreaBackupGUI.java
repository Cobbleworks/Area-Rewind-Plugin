package arearewind.gui.menus;

import arearewind.data.AreaBackup;
import arearewind.data.ProtectedArea;
import arearewind.gui.utils.BaseGUI;
import arearewind.gui.utils.GUIUtils;
import arearewind.gui.utils.ItemBuilder;
import arearewind.gui.utils.PaginatedContent;
import arearewind.managers.AreaManager;
import arearewind.managers.BackupManager;
import arearewind.managers.PermissionManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Enhanced Area Backup GUI - complete backup management for a specific area
 */
public class EnhancedAreaBackupGUI extends BaseGUI {
    private final PermissionManager permissionManager;
    private final AreaManager areaManager;
    private final BackupManager backupManager;
    private final BaseGUI parentGUI;
    private final String areaName;
    private final ProtectedArea area;
    private PaginatedContent<AreaBackup> paginatedBackups;
    
    public EnhancedAreaBackupGUI(JavaPlugin plugin, PermissionManager permissionManager, 
                               BaseGUI parentGUI, String areaName) {
        super(plugin, "&9&lBackups: &f" + areaName, 54);
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
        GUIUtils.fillBorder(this, GUIUtils.Slots.BORDER_54, Material.BLUE_STAINED_GLASS_PANE);
        
        // Add navigation buttons
        GUIUtils.addNavigationButtons(this, parentGUI, true);
        
        // Get backups
        List<AreaBackup> backups = backupManager.getBackupHistory(areaName);
        paginatedBackups = new PaginatedContent<>(backups, 28); // 28 backup slots per page
        
        // Control buttons (top row)
        addControlButtons();
        
        // Display backups
        displayBackups();
        
        // Add pagination controls
        PaginatedContent.addPaginationControls(this, paginatedBackups, 
                GUIUtils.Slots.PREV_PAGE_54, GUIUtils.Slots.NEXT_PAGE_54, GUIUtils.Slots.PAGE_INFO_54);
    }
    
    private void addControlButtons() {
        // Create new backup
        if (permissionManager.canCreateBackup(null, area)) { // Player check will be done in action
            inventory.setItem(1, new ItemBuilder(Material.CRAFTING_TABLE)
                    .name("&a&lCreate New Backup")
                    .lore("&7Create a backup of the current state",
                          "&7of this area",
                          "",
                          "&eClick to create backup!")
                    .glow()
                    .build());
            setAction(1, (player, event) -> {
                if (permissionManager.canCreateBackup(player, area)) {
                    player.closeInventory();
                    player.performCommand("rewind backup " + areaName);
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to create backups!");
                }
            });
        }
        
        // Undo/Redo controls
        int undoPointer = backupManager.getUndoPointer(areaName);
        
        if (permissionManager.canUndoRedo(null, area) && backupManager.canUndo(areaName)) {
            inventory.setItem(3, new ItemBuilder(Material.ARROW)
                    .name("&6&lUndo Last Change")
                    .lore("&7Undo the most recent change",
                          "&7Current position: " + undoPointer,
                          "",
                          "&eClick to undo!")
                    .build());
            setAction(3, (player, event) -> {
                if (permissionManager.canUndoRedo(player, area)) {
                    player.closeInventory();
                    player.performCommand("rewind undo " + areaName);
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to undo changes!");
                }
            });
        }
        
        if (permissionManager.canUndoRedo(null, area) && backupManager.canRedo(areaName)) {
            inventory.setItem(5, new ItemBuilder(Material.ARROW)
                    .name("&6&lRedo Last Undo")
                    .lore("&7Redo the most recent undo",
                          "&7Current position: " + undoPointer,
                          "",
                          "&eClick to redo!")
                    .build());
            setAction(5, (player, event) -> {
                if (permissionManager.canUndoRedo(player, area)) {
                    player.closeInventory();
                    player.performCommand("rewind redo " + areaName);
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to redo changes!");
                }
            });
        }
        
        // Compare with current
        inventory.setItem(7, new ItemBuilder(Material.COMPARATOR)
                .name("&d&lCompare with Current")
                .lore("&7Compare current state with",
                      "&7the most recent backup",
                      "",
                      "&eClick to view differences!")
                .build());
        setAction(7, (player, event) -> {
            player.closeInventory();
            player.performCommand("rewind diff " + areaName);
        });
    }
    
    private void displayBackups() {
        // Clear previous backup items
        int[] contentSlots = {10, 11, 12, 13, 14, 15, 16, // Row 2
                             19, 20, 21, 22, 23, 24, 25, // Row 3  
                             28, 29, 30, 31, 32, 33, 34, // Row 4
                             37, 38, 39, 40, 41, 42, 43}; // Row 5
        
        for (int slot : contentSlots) {
            inventory.setItem(slot, null);
            actions.remove(slot);
        }
        
        List<AreaBackup> currentBackups = paginatedBackups.getCurrentPageItems();
        int undoPointer = backupManager.getUndoPointer(areaName);
        
        for (int i = 0; i < currentBackups.size() && i < contentSlots.length; i++) {
            AreaBackup backup = currentBackups.get(i);
            int backupIndex = paginatedBackups.getCurrentPage() * paginatedBackups.getItemsPerPage() + i;
            int slot = contentSlots[i];
            
            Material icon = Material.CHEST;
            boolean isCurrent = (backupIndex == undoPointer);
            
            if (isCurrent) {
                icon = Material.ENDER_CHEST;
            }
            
            ItemBuilder builder = new ItemBuilder(icon)
                    .name("&9&lBackup #" + backupIndex)
                    .lore("&7Created: &f" + backup.getTimestamp().format(
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                          "&7Blocks: &f" + backup.getBlocks().size(),
                          "&7Size: &f" + formatSize(backup.getBlocks().size()));
            
            if (isCurrent) {
                builder.addLore("", "&a&l⬅ Current State");
                builder.glow();
            }
            
            builder.addLore("",
                    "&e▶ Left Click: &7Restore to this backup",
                    "&e▶ Right Click: &7Preview this backup",
                    "&e▶ Shift+Click: &7Compare with current");
            
            inventory.setItem(slot, builder.build());
            
            final int finalBackupIndex = backupIndex;
            setAction(slot, (player, event) -> {
                if (event.isShiftClick()) {
                    // Compare with current
                    player.closeInventory();
                    player.performCommand("rewind compare " + areaName + " " + finalBackupIndex);
                } else if (event.isRightClick()) {
                    // Preview backup
                    player.closeInventory();
                    player.performCommand("rewind preview " + areaName + " " + finalBackupIndex);
                } else if (event.isLeftClick()) {
                    // Restore backup
                    if (permissionManager.canRestoreBackup(player, area)) {
                        new ConfirmationGUI(plugin,
                                "&9&lRestore Backup #" + finalBackupIndex,
                                "Restore area '" + areaName + "' to backup #" + finalBackupIndex + "?",
                                "This will overwrite the current state!",
                                () -> {
                                    player.performCommand("rewind restore " + areaName + " " + finalBackupIndex);
                                    this.open(player);
                                },
                                () -> this.open(player),
                                this).open(player);
                    } else {
                        player.sendMessage(ChatColor.RED + "You don't have permission to restore backups!");
                    }
                }
            });
        }
    }
    
    private String formatSize(int blockCount) {
        if (blockCount < 1000) {
            return blockCount + " blocks";
        } else if (blockCount < 1000000) {
            return String.format("%.1fK blocks", blockCount / 1000.0);
        } else {
            return String.format("%.1fM blocks", blockCount / 1000000.0);
        }
    }
    
    @Override
    public void handleClick(Player player, InventoryClickEvent event) {
        // All clicks handled by specific actions
    }
    
    @Override
    public void refresh() {
        super.refresh();
        
        // Refresh backup list
        List<AreaBackup> backups = backupManager.getBackupHistory(areaName);
        paginatedBackups = new PaginatedContent<>(backups, 28);
        
        addControlButtons();
        displayBackups();
        
        // Update pagination controls
        PaginatedContent.addPaginationControls(this, paginatedBackups, 
                GUIUtils.Slots.PREV_PAGE_54, GUIUtils.Slots.NEXT_PAGE_54, GUIUtils.Slots.PAGE_INFO_54);
    }
}

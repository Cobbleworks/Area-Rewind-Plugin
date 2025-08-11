package arearewind.gui.menus;

import arearewind.data.ProtectedArea;
import arearewind.gui.utils.BaseGUI;
import arearewind.gui.utils.GUIUtils;
import arearewind.gui.utils.ItemBuilder;
import arearewind.gui.utils.PaginatedContent;
import arearewind.managers.AreaManager;
import arearewind.managers.BackupManager;
import arearewind.managers.PermissionManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Area Management GUI - view and manage protected areas
 */
public class AreaManagementGUI extends BaseGUI {
    private final PermissionManager permissionManager;
    private final AreaManager areaManager;
    private final BackupManager backupManager;
    private final BaseGUI parentGUI;
    private PaginatedContent<String> paginatedAreas;
    
    public AreaManagementGUI(JavaPlugin plugin, PermissionManager permissionManager, BaseGUI parentGUI) {
        super(plugin, "&a&lArea Management", 54);
        this.permissionManager = permissionManager;
        this.parentGUI = parentGUI;
        
        // Get managers from plugin - this is a simplified approach
        // In a real implementation, you'd inject these dependencies properly
        this.areaManager = ((arearewind.AreaRewindPlugin) plugin).getAreaManager();
        this.backupManager = ((arearewind.AreaRewindPlugin) plugin).getBackupManager();
    }
    
    @Override
    public void initialize() {
        // Fill border
        GUIUtils.fillBorder(this, GUIUtils.Slots.BORDER_54, Material.LIME_STAINED_GLASS_PANE);
        
        // Add navigation buttons
        GUIUtils.addNavigationButtons(this, parentGUI, true);
        
        // Get all areas the player can access
        List<String> accessibleAreas = new ArrayList<>();
        for (Map.Entry<String, ProtectedArea> entry : areaManager.getProtectedAreas().entrySet()) {
            // For now, add all areas - permission check will be done when opening
            accessibleAreas.add(entry.getKey());
        }
        
        // Setup pagination
        paginatedAreas = new PaginatedContent<>(accessibleAreas, 21); // 21 items per page
        
        // Add area creation button
        inventory.setItem(4, new ItemBuilder(Material.EMERALD_BLOCK)
                .name("&a&lCreate New Area")
                .lore("&7Create a new protected area",
                      "&7Make sure you have selected",
                      "&7two positions first!",
                      "",
                      "&eClick to start creation wizard!")
                .glow()
                .build());
        setAction(4, (player, event) -> new AreaCreationWizard(plugin, permissionManager, this).open(player));
        
        // Display current page of areas
        displayAreas();
        
        // Add pagination controls
        PaginatedContent.addPaginationControls(this, paginatedAreas, 
                GUIUtils.Slots.PREV_PAGE_54, GUIUtils.Slots.NEXT_PAGE_54, -1);
    }
    
    private void displayAreas() {
        // Clear previous area items
        int[] contentSlots = GUIUtils.getContentSlots(54);
        for (int slot : contentSlots) {
            if (slot != 4) { // Don't clear the create button
                inventory.setItem(slot, null);
                actions.remove(slot);
            }
        }
        
        List<String> currentAreas = paginatedAreas.getCurrentPageItems();
        int slotIndex = 0;
        
        for (String areaName : currentAreas) {
            if (slotIndex >= contentSlots.length - 1) break; // Reserve slot for create button
            
            int slot = contentSlots[slotIndex == 0 ? 1 : slotIndex]; // Skip slot 0 (create button is at 4)
            if (slot == 4) slot = contentSlots[++slotIndex]; // Skip create button slot
            
            ProtectedArea area = areaManager.getArea(areaName);
            if (area == null) continue;
            
            Material icon = Material.GRASS_BLOCK;
            String ownerName = Bukkit.getOfflinePlayer(area.getOwner()).getName();
            int backupCount = backupManager.getBackupHistory(areaName).size();
            
            inventory.setItem(slot, new ItemBuilder(icon)
                    .name("&a" + areaName)
                    .lore("&7Owner: &f" + ownerName,
                          "&7Size: &f" + area.getSize() + " blocks",
                          "&7Backups: &f" + backupCount,
                          "&7World: &f" + area.getPos1().getWorld().getName(),
                          "",
                          "&e▶ Left Click: &7Quick Actions",
                          "&e▶ Right Click: &7Teleport", 
                          "&e▶ Shift+Click: &7Detailed Info")
                    .build());
            
            setAction(slot, (player, event) -> {
                if (event.isShiftClick()) {
                    new EnhancedAreaInfoGUI(plugin, permissionManager, this, areaName).open(player);
                } else if (event.isRightClick()) {
                    player.closeInventory();
                    player.performCommand("rewind teleport " + areaName);
                } else {
                    new AreaQuickActionsGUI(plugin, permissionManager, this, areaName).open(player);
                }
            });
            
            slotIndex++;
        }
        
        // Update pagination info
        if (paginatedAreas.getTotalPages() > 1) {
            inventory.setItem(GUIUtils.Slots.PAGE_INFO_54, 
                    ItemBuilder.createInfoItem("Page Info", 
                            "&7" + paginatedAreas.getPageInfo(),
                            "&7Total Areas: " + paginatedAreas.getTotalItems()));
        }
    }
    
    @Override
    public void handleClick(Player player, InventoryClickEvent event) {
        // All clicks handled by specific actions
    }
    
    @Override
    public void refresh() {
        super.refresh();
        displayAreas();
    }
}

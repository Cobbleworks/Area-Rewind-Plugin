package arearewind.gui.menus;

import arearewind.gui.utils.BaseGUI;
import arearewind.gui.utils.GUIUtils;
import arearewind.gui.utils.ItemBuilder;
import arearewind.managers.AreaManager;
import arearewind.managers.PermissionManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Area Creation Wizard GUI - step-by-step area creation
 */
public class AreaCreationWizard extends BaseGUI {
    private final PermissionManager permissionManager;
    private final AreaManager areaManager;
    private final BaseGUI parentGUI;
    
    public AreaCreationWizard(JavaPlugin plugin, PermissionManager permissionManager, BaseGUI parentGUI) {
        super(plugin, "&a&lArea Creation Wizard", 27);
        this.permissionManager = permissionManager;
        this.parentGUI = parentGUI;
        this.areaManager = ((arearewind.AreaRewindPlugin) plugin).getAreaManager();
    }
    
    @Override
    public void initialize() {
        // Fill border
        GUIUtils.fillBorder(this, GUIUtils.Slots.BORDER_27, Material.LIME_STAINED_GLASS_PANE);
        
        // Add navigation buttons
        GUIUtils.addNavigationButtons(this, parentGUI, false);
        
        // Step 1: Position Selection
        inventory.setItem(10, new ItemBuilder(Material.GOLDEN_AXE)
                .name("&6&l1. Select Positions")
                .lore("&7Select two positions to define your area",
                      "",
                      "&e▶ Left Click: &7Set Position 1",
                      "&e▶ Right Click: &7Set Position 2",
                      "&e▶ Shift+Click: &7Auto-select current area")
                .build());
        setAction(10, (player, event) -> {
            if (event.isShiftClick()) {
                // Auto-select area around player
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "Auto-selecting area around you...");
                // This would implement auto-selection logic
                player.sendMessage(ChatColor.YELLOW + "Use your selection tool or /rewind pos1 and /rewind pos2");
            } else if (event.isLeftClick()) {
                player.closeInventory();
                player.performCommand("rewind pos1");
            } else if (event.isRightClick()) {
                player.closeInventory();
                player.performCommand("rewind pos2");
            }
        });
        
        // Step 2: Name the Area
        inventory.setItem(12, new ItemBuilder(Material.NAME_TAG)
                .name("&e&l2. Name Your Area")
                .lore("&7Give your area a unique name",
                      "",
                      "&7After selecting positions, use:",
                      "&f/rewind create <name>",
                      "",
                      "&e▶ Click: &7Get help with naming")
                .build());
        setAction(12, (player, event) -> {
            player.closeInventory();
            player.sendMessage(ChatColor.GREEN + "=== Area Naming Tips ===");
            player.sendMessage(ChatColor.YELLOW + "• Use descriptive names like 'MyHouse' or 'FarmArea'");
            player.sendMessage(ChatColor.YELLOW + "• Names must be unique and contain no spaces");
            player.sendMessage(ChatColor.YELLOW + "• Use underscores for multi-word names: 'my_castle'");
            player.sendMessage(ChatColor.YELLOW + "• Command: " + ChatColor.WHITE + "/rewind create <area_name>");
        });
        
        // Step 3: Configure Settings
        inventory.setItem(14, new ItemBuilder(Material.REDSTONE)
                .name("&c&l3. Configure Settings")
                .lore("&7Set up permissions and features",
                      "",
                      "&7Available after area creation:",
                      "&f• Trust management",
                      "&f• Backup settings", 
                      "&f• Visualization options",
                      "",
                      "&e▶ Click: &7View configuration guide")
                .build());
        setAction(14, (player, event) -> {
            player.closeInventory();
            player.sendMessage(ChatColor.GREEN + "=== Area Configuration Guide ===");
            player.sendMessage(ChatColor.YELLOW + "After creating your area, you can:");
            player.sendMessage(ChatColor.WHITE + "/rewind trust <player> <area> " + ChatColor.GRAY + "- Add trusted players");
            player.sendMessage(ChatColor.WHITE + "/rewind backup <area> " + ChatColor.GRAY + "- Create first backup");
            player.sendMessage(ChatColor.WHITE + "/rewind show <area> " + ChatColor.GRAY + "- Visualize boundaries");
            player.sendMessage(ChatColor.WHITE + "/rewind gui " + ChatColor.GRAY + "- Return to GUI management");
        });
        
        // Quick reference
        inventory.setItem(16, new ItemBuilder(Material.BOOK)
                .name("&9&lQuick Reference")
                .lore("&7Essential commands for area creation",
                      "",
                      "&f/rewind pos1 &7- Set first position",
                      "&f/rewind pos2 &7- Set second position", 
                      "&f/rewind create <name> &7- Create area",
                      "&f/rewind list &7- View your areas",
                      "",
                      "&e▶ Click: &7Copy commands to chat")
                .build());
        setAction(16, (player, event) -> {
            player.closeInventory();
            player.sendMessage(ChatColor.GREEN + "=== Essential Commands ===");
            player.sendMessage(ChatColor.YELLOW + "1. " + ChatColor.WHITE + "/rewind pos1");
            player.sendMessage(ChatColor.YELLOW + "2. " + ChatColor.WHITE + "/rewind pos2");
            player.sendMessage(ChatColor.YELLOW + "3. " + ChatColor.WHITE + "/rewind create <area_name>");
            player.sendMessage(ChatColor.GRAY + "Tip: Look at a block and use pos1/pos2 commands!");
        });
    }
    
    @Override
    public void handleClick(Player player, InventoryClickEvent event) {
        // All clicks handled by specific actions
    }
}

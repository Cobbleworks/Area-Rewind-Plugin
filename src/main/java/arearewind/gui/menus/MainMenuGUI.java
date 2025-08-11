package arearewind.gui.menus;

import arearewind.gui.utils.BaseGUI;
import arearewind.gui.utils.GUIUtils;
import arearewind.gui.utils.ItemBuilder;
import arearewind.managers.PermissionManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main menu GUI - entry point for all AreaRewind functionality
 */
public class MainMenuGUI extends BaseGUI {
    private final PermissionManager permissionManager;

    public MainMenuGUI(JavaPlugin plugin, PermissionManager permissionManager) {
        super(plugin, "&2&lAreaRewind &8- &7Main Menu", 45);
        this.permissionManager = permissionManager;
    }

    @Override
    public void initialize() {
        // Fill border with green glass
        GUIUtils.fillBorder(this, GUIUtils.Slots.BORDER_45, Material.GREEN_STAINED_GLASS_PANE);

        // Add navigation buttons (no back button for main menu)
        GUIUtils.addNavigationButtons(this, null, true);

        // Area Management Section (Top Row)
        inventory.setItem(10, new ItemBuilder(Material.GRASS_BLOCK)
                .name("&a&lArea Management")
                .lore("&7Manage your protected areas",
                        "&7• View all areas",
                        "&7• Create new areas",
                        "&7• Edit existing areas",
                        "",
                        "&eClick to open!")
                .glow()
                .build());
        setAction(10, (player, event) -> new AreaManagementGUI(plugin, permissionManager, this).open(player));

        inventory.setItem(11, new ItemBuilder(Material.CHEST)
                .name("&9&lBackup System")
                .lore("&7Manage area backups",
                        "&7• Create backups",
                        "&7• Restore from backups",
                        "&7• View backup history",
                        "&7• Compare changes",
                        "",
                        "&eClick to open!")
                .build());
        setAction(11, (player, event) -> new BackupMenuGUI(plugin, permissionManager, this).open(player));

        inventory.setItem(12, new ItemBuilder(Material.ENDER_PEARL)
                .name("&d&lTeleportation")
                .lore("&7Quick area navigation",
                        "&7• Teleport to areas",
                        "&7• Set home locations",
                        "&7• Recent locations",
                        "",
                        "&eClick to open!")
                .build());
        setAction(12, (player, event) -> new TeleportMenuGUI(plugin, permissionManager, this).open(player));

        // Tools Section (Middle Row)
        inventory.setItem(19, new ItemBuilder(Material.GOLDEN_AXE)
                .name("&6&lArea Tools")
                .lore("&7Area creation and editing tools",
                        "&7• Position selection",
                        "&7• Area expansion/contraction",
                        "&7• Boundary visualization",
                        "",
                        "&eClick to open!")
                .build());
        setAction(19, (player, event) -> new AreaToolsGUI(plugin, permissionManager, this).open(player));

        inventory.setItem(20, new ItemBuilder(Material.NAME_TAG)
                .name("&e&lPermissions & Trust")
                .lore("&7Manage area permissions",
                        "&7• Trust/untrust players",
                        "&7• View permissions",
                        "&7• Manage access levels",
                        "",
                        "&eClick to open!")
                .build());
        setAction(20, (player, event) -> new PermissionMenuGUI(plugin, permissionManager, this).open(player));

        inventory.setItem(21, new ItemBuilder(Material.REDSTONE)
                .name("&c&lVisualization")
                .lore("&7Area and backup visualization",
                        "&7• Show/hide area boundaries",
                        "&7• Preview backup changes",
                        "&7• Particle effects",
                        "&7• Comparison tools",
                        "",
                        "&eClick to open!")
                .build());
        setAction(21, (player, event) -> new VisualizationMenuGUI(plugin, permissionManager, this).open(player));

        // Advanced Section (Bottom content row)
        inventory.setItem(28, new ItemBuilder(Material.CLOCK)
                .name("&b&lScheduled Backups")
                .lore("&7Automatic backup management",
                        "&7• Set backup intervals",
                        "&7• View scheduled backups",
                        "&7• Manage automation",
                        "",
                        "&eClick to open!")
                .build());
        setAction(28, (player, event) -> new IntervalMenuGUI(plugin, permissionManager, this).open(player));

        inventory.setItem(29, new ItemBuilder(Material.WRITABLE_BOOK)
                .name("&f&lImport/Export")
                .lore("&7Area data management",
                        "&7• Export area data",
                        "&7• Import saved areas",
                        "&7• Share configurations",
                        "",
                        "&eClick to open!")
                .build());
        setAction(29, (player, event) -> new ImportExportGUI(plugin, permissionManager, this).open(player));

        // Admin Section (if has admin perms)
        // Always show admin button, check permissions in action
        inventory.setItem(30, new ItemBuilder(Material.COMMAND_BLOCK)
                .name("&4&lAdmin Tools")
                .lore("&7Administrative functions",
                        "&7• Server maintenance",
                        "&7• Cleanup operations",
                        "&7• Global settings",
                        "&7• System status",
                        "",
                        "&cAdmin only!",
                        "&eClick to open!")
                .build());
        setAction(30, (player, event) -> {
            if (permissionManager.hasAdminPermission(player)) {
                new AdminMenuGUI(plugin, permissionManager, this).open(player);
            } else {
                player.sendMessage(ChatColor.RED + "You don't have permission to access admin tools!");
            }
        });

        // Help & Settings
        inventory.setItem(34, new ItemBuilder(Material.BOOK)
                .name("&7&lHelp & Documentation")
                .lore("&7Get help and information",
                        "&7• Command reference",
                        "&7• Feature documentation",
                        "&7• Tips and tricks",
                        "",
                        "&eClick to open!")
                .build());
        setAction(34, (player, event) -> new HelpMenuGUI(plugin, permissionManager, this).open(player));

        // Quick Stats
        inventory.setItem(22, new ItemBuilder(Material.PAPER)
                .name("&7&lQuick Stats")
                .lore("&7Your AreaRewind statistics",
                        "&7Click to view detailed stats")
                .build());
        setAction(22, (player, event) -> new StatsGUI(plugin, permissionManager, this, player).open(player));
    }

    @Override
    public void handleClick(Player player, InventoryClickEvent event) {
        // No additional click handling needed - all handled by specific actions
    }
}

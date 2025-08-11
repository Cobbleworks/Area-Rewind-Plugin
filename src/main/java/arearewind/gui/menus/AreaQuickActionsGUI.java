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
 * Quick Actions GUI for a specific area
 */
public class AreaQuickActionsGUI extends BaseGUI {
    private final PermissionManager permissionManager;
    private final AreaManager areaManager;
    private final BackupManager backupManager;
    private final BaseGUI parentGUI;
    private final String areaName;
    private final ProtectedArea area;

    public AreaQuickActionsGUI(JavaPlugin plugin, PermissionManager permissionManager, BaseGUI parentGUI,
            String areaName) {
        super(plugin, "&a&l" + areaName + " &8- &7Quick Actions", 45);
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
            // Area doesn't exist
            inventory.setItem(22, ItemBuilder.createInfoItem("Error", "&cArea not found!"));
            GUIUtils.addNavigationButtons(this, parentGUI, false);
            return;
        }

        // Fill border
        GUIUtils.fillBorder(this, GUIUtils.Slots.BORDER_45, Material.CYAN_STAINED_GLASS_PANE);

        // Add navigation buttons
        GUIUtils.addNavigationButtons(this, parentGUI, true);

        // Area info (center top)
        String ownerName = Bukkit.getOfflinePlayer(area.getOwner()).getName();
        inventory.setItem(4, new ItemBuilder(Material.GRASS_BLOCK)
                .name("&a&l" + areaName)
                .lore("&7Owner: &f" + ownerName,
                        "&7Size: &f" + area.getSize() + " blocks",
                        "&7World: &f" + area.getPos1().getWorld().getName())
                .glow()
                .build());

        // Quick actions row 1
        inventory.setItem(10, new ItemBuilder(Material.ENDER_PEARL)
                .name("&d&lTeleport")
                .lore("&7Teleport to this area",
                        "",
                        "&eClick to teleport!")
                .build());
        setAction(10, (player, event) -> {
            player.closeInventory();
            player.performCommand("rewind teleport " + areaName);
        });

        inventory.setItem(11, new ItemBuilder(Material.CHEST)
                .name("&9&lCreate Backup")
                .lore("&7Create a backup of this area",
                        "",
                        "&eClick to create backup!")
                .build());
        setAction(11, (player, event) -> {
            if (permissionManager.canCreateBackup(player, area)) {
                player.closeInventory();
                player.performCommand("rewind backup " + areaName);
            } else {
                player.sendMessage(ChatColor.RED + "You don't have permission to create backups for this area!");
            }
        });

        inventory.setItem(12, new ItemBuilder(Material.BOOK)
                .name("&6&lView Backups")
                .lore("&7View and manage backups",
                        "",
                        "&eClick to open backup menu!")
                .build());
        setAction(12, (player, event) -> {
            new EnhancedAreaBackupGUI(plugin, permissionManager, this, areaName).open(player);
        });

        inventory.setItem(13, new ItemBuilder(Material.REDSTONE)
                .name("&c&lVisualize")
                .lore("&7Show/hide area boundaries",
                        "",
                        "&e▶ Left Click: &7Show boundaries",
                        "&e▶ Right Click: &7Hide boundaries")
                .build());
        setAction(13, (player, event) -> {
            player.closeInventory();
            if (event.isLeftClick()) {
                player.performCommand("rewind show " + areaName);
            } else {
                player.performCommand("rewind hide " + areaName);
            }
        });

        inventory.setItem(14, new ItemBuilder(Material.NAME_TAG)
                .name("&e&lManage Trust")
                .lore("&7Add or remove trusted players",
                        "",
                        "&eClick to open trust menu!")
                .build());
        setAction(14, (player, event) -> {
            new TrustManagementGUI(plugin, permissionManager, this, areaName).open(player);
        });

        inventory.setItem(15, new ItemBuilder(Material.PAPER)
                .name("&7&lArea Information")
                .lore("&7View detailed area information",
                        "",
                        "&eClick for full details!")
                .build());
        setAction(15, (player, event) -> {
            new EnhancedAreaInfoGUI(plugin, permissionManager, this, areaName).open(player);
        });

        inventory.setItem(16, new ItemBuilder(Material.GOLDEN_AXE)
                .name("&6&lEdit Boundaries")
                .lore("&7Expand or contract area",
                        "",
                        "&eClick to open boundary editor!")
                .build());
        setAction(16, (player, event) -> {
            new BoundaryEditGUI(plugin, permissionManager, this, areaName).open(player);
        });

        // Quick actions row 2 (if has permissions)
        if (permissionManager.canUndoRedo(null, area)) {
            if (backupManager.canUndo(areaName)) {
                inventory.setItem(19, new ItemBuilder(Material.ARROW)
                        .name("&a&lUndo Last Change")
                        .lore("&7Undo the last change",
                                "",
                                "&eClick to undo!")
                        .build());
                setAction(19, (player, event) -> {
                    player.closeInventory();
                    player.performCommand("rewind undo " + areaName);
                });
            }

            if (backupManager.canRedo(areaName)) {
                inventory.setItem(20, new ItemBuilder(Material.ARROW)
                        .name("&a&lRedo Last Undo")
                        .lore("&7Redo the last undo",
                                "",
                                "&eClick to redo!")
                        .build());
                setAction(20, (player, event) -> {
                    player.closeInventory();
                    player.performCommand("rewind redo " + areaName);
                });
            }
        }

        // Dangerous actions (if owner or admin)
        if (permissionManager.canDeleteArea(null, area)) {
            inventory.setItem(25, new ItemBuilder(Material.TNT)
                    .name("&c&lDelete Area")
                    .lore("&7Permanently delete this area",
                            "&c&lWARNING: This cannot be undone!",
                            "",
                            "&cClick to delete area!")
                    .build());
            setAction(25, (player, event) -> {
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
            });
        }
    }

    @Override
    public void handleClick(Player player, InventoryClickEvent event) {
        // All clicks handled by specific actions
    }
}

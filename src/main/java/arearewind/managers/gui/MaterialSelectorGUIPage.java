package arearewind.managers.gui;

import arearewind.data.AreaBackup;
import arearewind.data.ProtectedArea;
import arearewind.managers.AreaManager;
import arearewind.managers.BackupManager;
import arearewind.managers.FileManager;
import arearewind.managers.GUIManager;
import arearewind.managers.PermissionManager;
import arearewind.managers.gui.GUIPaginationHelper.PaginationInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * GUI page for selecting materials as icons for areas and backups
 */
public class MaterialSelectorGUIPage implements IGUIPage {

    private final GUIManager guiManager;
    private final AreaManager areaManager;
    private final BackupManager backupManager;
    private final PermissionManager permissionManager;
    private final FileManager fileManager;

    // Pagination constants
    private static final int ITEMS_PER_PAGE = 35; // 5 rows of 7 items (slots 0-34)
    private static final int INFO_ROW_START = 35; // Row for info (35-44)
    private static final int NAVIGATION_ROW_START = 45; // Bottom row for navigation

    // Common materials for selection
    private static final List<Material> AREA_MATERIALS = Arrays.asList(
            Material.GRASS_BLOCK, Material.DIAMOND_BLOCK, Material.EMERALD_BLOCK, Material.GOLD_BLOCK,
            Material.IRON_BLOCK, Material.REDSTONE_BLOCK, Material.LAPIS_BLOCK, Material.COAL_BLOCK,
            Material.QUARTZ_BLOCK, Material.OBSIDIAN, Material.BEDROCK, Material.STONE,
            Material.COBBLESTONE, Material.BRICKS, Material.NETHER_BRICKS, Material.END_STONE,
            Material.PURPUR_BLOCK, Material.PRISMARINE, Material.SEA_LANTERN, Material.GLOWSTONE,
            Material.BEACON, Material.ENCHANTING_TABLE, Material.ANVIL, Material.CRAFTING_TABLE,
            Material.FURNACE, Material.CHEST, Material.ENDER_CHEST, Material.BOOKSHELF,
            Material.JUKEBOX, Material.NOTE_BLOCK, Material.DRAGON_EGG, Material.CONDUIT,
            Material.RESPAWN_ANCHOR, Material.LODESTONE, Material.NETHERITE_BLOCK);

    private static final List<Material> BACKUP_MATERIALS = Arrays.asList(
            Material.CHEST, Material.ENDER_CHEST, Material.BARREL, Material.SHULKER_BOX,
            Material.WHITE_SHULKER_BOX, Material.ORANGE_SHULKER_BOX, Material.MAGENTA_SHULKER_BOX,
            Material.LIGHT_BLUE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX, Material.LIME_SHULKER_BOX,
            Material.PINK_SHULKER_BOX, Material.GRAY_SHULKER_BOX, Material.LIGHT_GRAY_SHULKER_BOX,
            Material.CYAN_SHULKER_BOX, Material.PURPLE_SHULKER_BOX, Material.BLUE_SHULKER_BOX,
            Material.BROWN_SHULKER_BOX, Material.GREEN_SHULKER_BOX, Material.RED_SHULKER_BOX,
            Material.BLACK_SHULKER_BOX, Material.COMPASS, Material.CLOCK, Material.BOOK,
            Material.WRITABLE_BOOK, Material.ENCHANTED_BOOK, Material.PAPER, Material.MAP,
            Material.FILLED_MAP, Material.NAME_TAG, Material.LEAD, Material.BUNDLE,
            Material.MUSIC_DISC_13, Material.MUSIC_DISC_CAT, Material.MUSIC_DISC_BLOCKS,
            Material.MUSIC_DISC_CHIRP, Material.MUSIC_DISC_FAR);

    public MaterialSelectorGUIPage(GUIManager guiManager, AreaManager areaManager,
            BackupManager backupManager, PermissionManager permissionManager, FileManager fileManager) {
        this.guiManager = guiManager;
        this.areaManager = areaManager;
        this.backupManager = backupManager;
        this.permissionManager = permissionManager;
        this.fileManager = fileManager;
    }

    @Override
    public void openGUI(Player player) {
        // This should not be called directly
        throw new UnsupportedOperationException("Use openMaterialSelector with specific parameters");
    }

    public void openMaterialSelector(Player player, String type, String areaName, String backupId, int page) {
        List<Material> materials = type.equals("area") ? AREA_MATERIALS : BACKUP_MATERIALS;

        // Calculate pagination
        PaginationInfo paginationInfo = GUIPaginationHelper.calculatePagination(
                materials.size(), ITEMS_PER_PAGE, page);

        // Update pagination data for the player
        GUIPaginationHelper.updatePaginationData(player.getUniqueId(),
                paginationInfo.getCurrentPage(), paginationInfo.getMaxPage(), getPageType(),
                type + ":" + areaName + (backupId != null ? ":" + backupId : ""));

        // Create inventory with page info in title
        String targetName = type.equals("area") ? areaName : "Backup #" + backupId;
        String title = ChatColor.GOLD + "Set Icon: " + targetName;
        if (paginationInfo.getMaxPage() > 0) {
            title += " (" + (paginationInfo.getCurrentPage() + 1) + "/" + (paginationInfo.getMaxPage() + 1) + ")";
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        // Add material items for current page
        int slot = 0;
        for (int i = paginationInfo.getStartIndex(); i < paginationInfo.getEndIndex(); i++) {
            Material material = materials.get(i);

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + formatMaterialName(material));

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Material: " + material.name());
            lore.add("");
            lore.add(ChatColor.GREEN + "Click to select this material");

            meta.setLore(lore);
            item.setItemMeta(meta);
            gui.setItem(slot++, item);
        }

        // Add pagination navigation if needed
        if (paginationInfo.getMaxPage() > 0) {
            GUIPaginationHelper.addPaginationButtons(gui, paginationInfo,
                    NAVIGATION_ROW_START, NAVIGATION_ROW_START + 8, -1);
        }

        // Add info and navigation items
        addInfoAndNavigationItems(gui, type, areaName, backupId);

        player.openInventory(gui);
        guiManager.registerOpenGUI(player, getPageType() + ":" + type + ":" + areaName +
                (backupId != null ? ":" + backupId : ""));
    }

    @Override
    public void handleClick(Player player, InventoryClickEvent event) {
        String guiData = guiManager.getOpenGUIType(player);
        if (!guiData.startsWith("material-selector:"))
            return;

        String[] parts = guiData.substring(18).split(":");
        if (parts.length < 2)
            return;

        String type = parts[0];
        String areaName = parts[1];
        String backupId = parts.length > 2 ? parts[2] : null;

        handleMaterialSelectorClick(player, event, type, areaName, backupId);
    }

    @Override
    public String getPageType() {
        return "material-selector";
    }

    @Override
    public void handlePaginationAction(Player player, GUIPaginationHelper.PaginationAction action) {
        String guiData = guiManager.getOpenGUIType(player);
        if (!guiData.startsWith("material-selector:"))
            return;

        String[] parts = guiData.substring(18).split(":");
        if (parts.length < 2)
            return;

        String type = parts[0];
        String areaName = parts[1];
        String backupId = parts.length > 2 ? parts[2] : null;

        GUIPaginationHelper.PaginationData paginationData = GUIPaginationHelper.getPaginationData(
                player.getUniqueId(), getPageType(), type + ":" + areaName +
                        (backupId != null ? ":" + backupId : ""));

        int newPage = paginationData.getCurrentPage();

        switch (action) {
            case PREVIOUS:
                if (newPage > 0) {
                    newPage--;
                }
                break;
            case NEXT:
                if (newPage < paginationData.getMaxPage()) {
                    newPage++;
                }
                break;
            default:
                return;
        }

        player.closeInventory();
        openMaterialSelector(player, type, areaName, backupId, newPage);
    }

    private void handleMaterialSelectorClick(Player player, InventoryClickEvent event, String type,
            String areaName, String backupId) {
        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta())
            return;

        String displayName = item.getItemMeta().getDisplayName();

        // Handle pagination navigation
        GUIPaginationHelper.PaginationAction paginationAction = GUIPaginationHelper.checkPaginationClick(item);
        if (paginationAction != GUIPaginationHelper.PaginationAction.NONE) {
            handlePaginationAction(player, paginationAction);
            return;
        }

        // Handle navigation buttons
        if (displayName.contains("Cancel")) {
            player.closeInventory();
            if (type.equals("area")) {
                guiManager.openAreaSettingsGUI(player, areaName);
            } else {
                guiManager.openBackupsGUI(player, areaName);
            }
            return;
        } else if (displayName.contains("Use Item in Hand")) {
            ItemStack handItem = player.getInventory().getItemInMainHand();
            if (handItem == null || handItem.getType() == Material.AIR) {
                player.sendMessage(ChatColor.RED + "You must hold an item to use as an icon!");
                return;
            }
            setIconFromItem(player, type, areaName, backupId, handItem);
            return;
        }

        // Handle material selection
        Material selectedMaterial = item.getType();
        if (selectedMaterial != Material.AIR) {
            setIcon(player, type, areaName, backupId, selectedMaterial);
        }
    }

    private void setIcon(Player player, String type, String areaName, String backupId, Material material) {
        if (type.equals("area")) {
            ProtectedArea area = areaManager.getArea(areaName);
            if (area == null) {
                player.sendMessage(ChatColor.RED + "Area not found!");
                return;
            }

            if (!permissionManager.canModifyBoundaries(player, area)) {
                player.sendMessage(ChatColor.RED + "You don't have permission to modify this area!");
                return;
            }

            area.setIcon(material);
            areaManager.saveAreas();

            player.sendMessage(ChatColor.GREEN + "Set icon for area '" + areaName + "' to " +
                    ChatColor.YELLOW + formatMaterialName(material) + ChatColor.GREEN + "!");

            player.closeInventory();
            guiManager.openAreaSettingsGUI(player, areaName);

        } else {
            ProtectedArea area = areaManager.getArea(areaName);
            if (area == null) {
                player.sendMessage(ChatColor.RED + "Area not found!");
                return;
            }

            if (!permissionManager.canModifyBoundaries(player, area)) {
                player.sendMessage(ChatColor.RED + "You don't have permission to modify this area!");
                return;
            }

            List<AreaBackup> backups = backupManager.getBackupHistory(areaName);
            AreaBackup targetBackup = null;

            try {
                // backupId is now the index as a string
                int backupIndex = Integer.parseInt(backupId);
                if (backupIndex >= 0 && backupIndex < backups.size()) {
                    targetBackup = backups.get(backupIndex);
                }
            } catch (NumberFormatException e) {
                // Fallback to old ID-based lookup for compatibility
                for (AreaBackup backup : backups) {
                    if (backup.getId().equals(backupId)) {
                        targetBackup = backup;
                        break;
                    }
                }
            }

            if (targetBackup == null) {
                player.sendMessage(ChatColor.RED + "Backup not found!");
                return;
            }

            targetBackup.setIcon(material);
            fileManager.saveBackupToFile(areaName, targetBackup);

            player.sendMessage(ChatColor.GREEN + "Set icon for backup #" + backupId + " to " +
                    ChatColor.YELLOW + formatMaterialName(material) + ChatColor.GREEN + "!");

            player.closeInventory();
            guiManager.openBackupsGUI(player, areaName);
        }
    }

    private void setIconFromItem(Player player, String type, String areaName, String backupId, ItemStack item) {
        if (type.equals("area")) {
            ProtectedArea area = areaManager.getArea(areaName);
            if (area == null) {
                player.sendMessage(ChatColor.RED + "Area not found!");
                return;
            }

            if (!permissionManager.canModifyBoundaries(player, area)) {
                player.sendMessage(ChatColor.RED + "You don't have permission to modify this area!");
                return;
            }

            // For player heads or other special items, use the full ItemStack
            // For regular materials, just use the material
            if (isCustomPlayerHead(item)) {
                area.setIconItem(item);
                player.sendMessage(ChatColor.GREEN + "Set custom icon for area '" + areaName + "' to " +
                        ChatColor.YELLOW + "custom player head" + ChatColor.GREEN + "!");
            } else {
                area.setIcon(item.getType());
                player.sendMessage(ChatColor.GREEN + "Set icon for area '" + areaName + "' to " +
                        ChatColor.YELLOW + formatMaterialName(item.getType()) + ChatColor.GREEN + "!");
            }

            areaManager.saveAreas();
            player.closeInventory();
            guiManager.openAreaSettingsGUI(player, areaName);

        } else {
            ProtectedArea area = areaManager.getArea(areaName);
            if (area == null) {
                player.sendMessage(ChatColor.RED + "Area not found!");
                return;
            }

            if (!permissionManager.canModifyBoundaries(player, area)) {
                player.sendMessage(ChatColor.RED + "You don't have permission to modify this area!");
                return;
            }

            List<AreaBackup> backups = backupManager.getBackupHistory(areaName);
            AreaBackup targetBackup = null;

            try {
                // backupId is now the index as a string
                int backupIndex = Integer.parseInt(backupId);
                if (backupIndex >= 0 && backupIndex < backups.size()) {
                    targetBackup = backups.get(backupIndex);
                }
            } catch (NumberFormatException e) {
                // Fallback to old ID-based lookup for compatibility
                for (AreaBackup backup : backups) {
                    if (backup.getId().equals(backupId)) {
                        targetBackup = backup;
                        break;
                    }
                }
            }

            if (targetBackup == null) {
                player.sendMessage(ChatColor.RED + "Backup not found!");
                return;
            }

            // For player heads or other special items, use the full ItemStack
            // For regular materials, just use the material
            if (isCustomPlayerHead(item)) {
                targetBackup.setIconItem(item);
                player.sendMessage(ChatColor.GREEN + "Set custom icon for backup #" + backupId + " to " +
                        ChatColor.YELLOW + "custom player head" + ChatColor.GREEN + "!");
            } else {
                targetBackup.setIcon(item.getType());
                player.sendMessage(ChatColor.GREEN + "Set icon for backup #" + backupId + " to " +
                        ChatColor.YELLOW + formatMaterialName(item.getType()) + ChatColor.GREEN + "!");
            }

            fileManager.saveBackupToFile(areaName, targetBackup);
            player.closeInventory();
            guiManager.openBackupsGUI(player, areaName);
        }
    }

    /**
     * Checks if an ItemStack is a custom player head (has skull meta with texture
     * data)
     */
    private boolean isCustomPlayerHead(ItemStack item) {
        if (item == null || item.getType() != Material.PLAYER_HEAD) {
            return false;
        }

        // Check if the item has skull meta with custom texture data
        if (item.hasItemMeta() && item.getItemMeta() instanceof org.bukkit.inventory.meta.SkullMeta) {
            org.bukkit.inventory.meta.SkullMeta skullMeta = (org.bukkit.inventory.meta.SkullMeta) item.getItemMeta();

            // Check if it has a custom texture (either from player profile or texture URL)
            return skullMeta.hasOwner() ||
                    (skullMeta.getOwnerProfile() != null && skullMeta.getOwnerProfile().getTextures() != null) ||
                    skullMeta.getOwningPlayer() != null;
        }

        return false;
    }

    private void addInfoAndNavigationItems(Inventory gui, String type, String areaName, String backupId) {
        // Info item
        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(ChatColor.AQUA + "Icon Selection");
        List<String> infoLore = new ArrayList<>();
        infoLore.add(ChatColor.GRAY + "Setting icon for: " +
                (type.equals("area") ? "Area '" + areaName + "'" : "Backup #" + backupId));
        infoLore.add("");
        infoLore.add(ChatColor.YELLOW + "Click any material to select it");
        infoLore.add(ChatColor.YELLOW + "Or use the item in your hand");
        infoLore.add("");
        infoLore.add(ChatColor.AQUA + "Pro tip: Hold a custom player head");
        infoLore.add(ChatColor.AQUA + "and use 'Use Item in Hand' to set");
        infoLore.add(ChatColor.AQUA + "a custom head with textures!");
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        gui.setItem(INFO_ROW_START + 4, infoItem);

        // Use item in hand button
        ItemStack handItem = new ItemStack(Material.STICK);
        ItemMeta handMeta = handItem.getItemMeta();
        handMeta.setDisplayName(ChatColor.GREEN + "Use Item in Hand");
        List<String> handLore = new ArrayList<>();
        handLore.add(ChatColor.GRAY + "Use the item you're currently holding");
        handLore.add(ChatColor.GRAY + "as the icon material");
        handLore.add("");
        handLore.add(ChatColor.AQUA + "Supports custom player heads with");
        handLore.add(ChatColor.AQUA + "textures and special properties!");
        handLore.add("");
        handLore.add(ChatColor.YELLOW + "Click to use held item");
        handMeta.setLore(handLore);
        handItem.setItemMeta(handMeta);
        gui.setItem(NAVIGATION_ROW_START + 3, handItem);

        // Cancel button
        ItemStack cancelItem = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "Cancel");
        List<String> cancelLore = new ArrayList<>();
        cancelLore.add(ChatColor.GRAY + "Cancel icon selection");
        cancelLore.add(ChatColor.GRAY + "Return to previous menu");
        cancelMeta.setLore(cancelLore);
        cancelItem.setItemMeta(cancelMeta);
        gui.setItem(NAVIGATION_ROW_START + 5, cancelItem);
    }

    private String formatMaterialName(Material material) {
        String name = material.name().toLowerCase().replace('_', ' ');
        StringBuilder formatted = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : name.toCharArray()) {
            if (capitalizeNext) {
                formatted.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                formatted.append(c);
            }
            if (c == ' ') {
                capitalizeNext = true;
            }
        }

        return formatted.toString();
    }
}

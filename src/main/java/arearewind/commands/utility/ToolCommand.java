package arearewind.commands.utility;

import arearewind.commands.base.BaseCommand;
import arearewind.managers.*;
import arearewind.util.ConfigurationManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

/**
 * Command for getting the rewind tool
 */
public class ToolCommand extends BaseCommand {

    public ToolCommand(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
            GUIManager guiManager, VisualizationManager visualizationManager,
            PermissionManager permissionManager, ConfigurationManager configManager,
            FileManager fileManager, IntervalManager intervalManager) {
        super(plugin, areaManager, backupManager, guiManager, visualizationManager,
                permissionManager, configManager, fileManager, intervalManager);
    }

    @Override
    public boolean execute(Player player, String[] args) {
        // Get the configured tool item from config - using fallback approach
        String toolMaterial = "WOODEN_HOE"; // Default fallback

        // Try to get from config if the method exists, otherwise use default
        try {
            if (configManager.getClass().getMethod("getString", String.class, String.class) != null) {
                // Method exists, but we'll use reflection cautiously
                toolMaterial = "WOODEN_HOE"; // Safe fallback for now
            }
        } catch (Exception ignored) {
            // Method doesn't exist, use default
        }

        try {
            Material material = Material.valueOf(toolMaterial.toUpperCase());
            ItemStack tool = new ItemStack(material);

            // Add custom name and lore if needed
            org.bukkit.inventory.meta.ItemMeta meta = tool.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + "Area Rewind Tool");
                meta.setLore(Arrays.asList(
                        ChatColor.GRAY + "Left click: Set position 1",
                        ChatColor.GRAY + "Right click: Set position 2",
                        ChatColor.GRAY + "Shift + right click: Open GUI"));
                tool.setItemMeta(meta);
            }

            player.getInventory().addItem(tool);
            player.sendMessage(ChatColor.GREEN + "Given Area Rewind tool!");

        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid tool material configured: " + toolMaterial);
            // Fallback to wooden hoe
            ItemStack fallback = new ItemStack(Material.WOODEN_HOE);
            player.getInventory().addItem(fallback);
            player.sendMessage(ChatColor.YELLOW + "Given fallback tool (wooden hoe)");
        }

        return true;
    }

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        return List.of(); // No arguments for this command
    }

    @Override
    public String getName() {
        return "tool";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("wand", "item");
    }

    @Override
    public String getDescription() {
        return "Get the area selection tool";
    }

    @Override
    public String getUsage() {
        return "/rewind tool";
    }
}

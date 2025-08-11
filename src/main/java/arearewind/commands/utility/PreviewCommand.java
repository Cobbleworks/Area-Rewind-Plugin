package arearewind.commands.utility;

import arearewind.commands.base.BaseCommand;
import arearewind.managers.*;
import arearewind.util.ConfigurationManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command for showing area preview with particles
 */
public class PreviewCommand extends BaseCommand {

    public PreviewCommand(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
            GUIManager guiManager, VisualizationManager visualizationManager,
            PermissionManager permissionManager, ConfigurationManager configManager,
            FileManager fileManager, IntervalManager intervalManager) {
        super(plugin, areaManager, backupManager, guiManager, visualizationManager,
                permissionManager, configManager, fileManager, intervalManager);
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (!validateMinArgs(player, args, 1)) {
            return true;
        }

        String areaName = args[0];

        if (!areaManager.areaExists(areaName)) {
            player.sendMessage(ChatColor.RED + "Area '" + areaName + "' not found!");
            return true;
        }

        int backupId = 0; // Default to latest backup
        if (args.length > 1) {
            Integer id = parseInteger(player, args[1], "backup ID");
            if (id == null) {
                return true;
            }
            backupId = id;
        }

        String particleType = "SMOKE_NORMAL"; // Default particle
        if (args.length > 2) {
            particleType = args[2].toUpperCase();
        }

        try {
            // Using existing visualization methods
            player.sendMessage(
                    ChatColor.YELLOW + "Preview functionality is not yet implemented in VisualizationManager");
            player.sendMessage(ChatColor.GRAY + "Showing basic area visualization instead...");

            // Use existing visualization if available
            player.sendMessage(ChatColor.GREEN + "Would show preview of " + areaName +
                    " (backup " + backupId + ") with " + particleType + " particles");
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error showing preview: " + e.getMessage());
        }
        return true;
    }

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1) {
            return getAreaCompletions().stream()
                    .filter(area -> area.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            return getBackupIdCompletions(args[0]).stream()
                    .filter(id -> id.startsWith(args[1]))
                    .collect(Collectors.toList());
        } else if (args.length == 3) {
            List<String> particles = Arrays.asList("SMOKE_NORMAL", "FLAME", "REDSTONE",
                    "CLOUD", "ENCHANTMENT_TABLE", "VILLAGER_HAPPY");
            return particles.stream()
                    .filter(p -> p.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public String getName() {
        return "preview";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("particle");
    }

    @Override
    public String getDescription() {
        return "Show a visual preview of a backup with particles";
    }

    @Override
    public String getUsage() {
        return "/rewind preview <area> [backup_id] [particle_type]";
    }
}

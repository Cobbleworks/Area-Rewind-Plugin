package arearewind.commands.utility;

import arearewind.commands.base.BaseCommand;
import arearewind.commands.registry.CommandRegistry;
import arearewind.managers.*;
import arearewind.util.ConfigurationManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Command for displaying help information
 */
public class HelpCommand extends BaseCommand {

    public HelpCommand(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
            GUIManager guiManager, VisualizationManager visualizationManager,
            PermissionManager permissionManager, ConfigurationManager configManager,
            FileManager fileManager, IntervalManager intervalManager,
            CommandRegistry commandRegistry) {
        super(plugin, areaManager, backupManager, guiManager, visualizationManager,
                permissionManager, configManager, fileManager, intervalManager);
    }

    @Override
    public boolean execute(Player player, String[] args) {
        player.sendMessage(ChatColor.GOLD + "=== Area Rewind Help ===");

        // Group commands by category
        player.sendMessage(ChatColor.YELLOW + "Area Management:");
        player.sendMessage(ChatColor.GRAY + "  /rewind pos1|pos2 - Set selection positions");
        player.sendMessage(ChatColor.GRAY + "  /rewind save <name> - Create new area");
        player.sendMessage(ChatColor.GRAY + "  /rewind delete <area> - Delete area");
        player.sendMessage(ChatColor.GRAY + "  /rewind contract <area> <dir> <amount> - Contract area");
        player.sendMessage(ChatColor.GRAY + "  /rewind seticon <area> <material> - Set area icon");

        player.sendMessage(ChatColor.YELLOW + "Backup Operations:");
        player.sendMessage(ChatColor.GRAY + "  /rewind backup <area> - Create backup");
        player.sendMessage(ChatColor.GRAY + "  /rewind restore <area> <id> - Restore backup");
        player.sendMessage(ChatColor.GRAY + "  /rewind restoreblock <area> <id> [world] - Restore via command block");
        player.sendMessage(ChatColor.GRAY + "  /rewind history <area> - View backup history");
        player.sendMessage(ChatColor.GRAY + "  /rewind seticon backup <area> <id> <material> - Set backup icon");

        player.sendMessage(ChatColor.YELLOW + "Information & Management:");
        player.sendMessage(ChatColor.GRAY + "  /rewind list [owned|trusted] - List areas");
        player.sendMessage(ChatColor.GRAY + "  /rewind info <area> - Area information");
        player.sendMessage(ChatColor.GRAY + "  /rewind gui - Open management GUI");
        player.sendMessage(ChatColor.GRAY + "  /rewind config <setting> [value] - Configure personal settings");
        player.sendMessage(ChatColor.GRAY + "  /rewind status - System status");

        return true;
    }

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Show help information";
    }

    @Override
    public String getUsage() {
        return "/rewind help";
    }
}

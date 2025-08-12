package arearewind.commands.utility;

import arearewind.commands.base.BaseCommand;
import arearewind.managers.*;
import arearewind.util.ConfigurationManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Command for showing system status
 */
public class StatusCommand extends BaseCommand {

    public StatusCommand(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
            GUIManager guiManager, VisualizationManager visualizationManager,
            PermissionManager permissionManager, ConfigurationManager configManager,
            FileManager fileManager, IntervalManager intervalManager) {
        super(plugin, areaManager, backupManager, guiManager, visualizationManager,
                permissionManager, configManager, fileManager, intervalManager);
    }

    @Override
    public boolean execute(Player player, String[] args) {
        player.sendMessage(ChatColor.GOLD + "=== Area Rewind Status ===");
        player.sendMessage(ChatColor.GRAY + backupManager.getBackupStatistics());
        player.sendMessage(ChatColor.GRAY + fileManager.getDiskUsageStats());

        Map<String, IntervalManager.IntervalConfig> intervals = intervalManager.getAllIntervals();
        if (!intervals.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Active Auto-Restore Intervals:");
            for (IntervalManager.IntervalConfig config : intervals.values()) {
                player.sendMessage(ChatColor.WHITE + "- " + config.areaName + ": every " +
                        config.minutes + "min -> backup " + config.backupId);
            }
        } else {
            player.sendMessage(ChatColor.GRAY + "No active auto-restore intervals");
        }

        return true;
    }

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public String getName() {
        return "status";
    }

    @Override
    public String getDescription() {
        return "Show system status and statistics";
    }

    @Override
    public String getUsage() {
        return "/rewind status";
    }
}

package arearewind.commands.info;

import arearewind.commands.base.BaseCommand;
import arearewind.data.AreaBackup;
import arearewind.data.ProtectedArea;
import arearewind.managers.*;
import arearewind.util.ConfigurationManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command for viewing backup history
 */
public class HistoryCommand extends BaseCommand {

    public HistoryCommand(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
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
        int page = 1;

        if (args.length > 1) {
            Integer parsedPage = parseInteger(player, args[1], "page number");
            if (parsedPage == null) {
                return true;
            }
            page = parsedPage;
        }

        ProtectedArea area = validateAndGetArea(player, areaName);
        if (area == null) {
            return true;
        }

        List<AreaBackup> backups = backupManager.getBackupHistory(areaName);
        int itemsPerPage = 10;
        int maxPage = (int) Math.ceil((double) backups.size() / itemsPerPage);

        if (page < 1 || page > maxPage) {
            player.sendMessage(ChatColor.RED + "Invalid page! Valid pages: 1-" + maxPage);
            return true;
        }

        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, backups.size());

        player.sendMessage(ChatColor.GOLD + "=== Backup History: " + areaName +
                " (Page " + page + "/" + maxPage + ") ===");

        for (int i = startIndex; i < endIndex; i++) {
            AreaBackup backup = backups.get(i);
            String timestamp = backup.getTimestamp().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
            int blockCount = backup.getBlocks().size();

            ChatColor color = (i == backupManager.getUndoPointer(areaName)) ? ChatColor.YELLOW : ChatColor.GREEN;
            player.sendMessage(color + "[" + i + "] " + ChatColor.WHITE + timestamp +
                    ChatColor.GRAY + " (" + blockCount + " blocks)");
        }

        if (maxPage > 1) {
            player.sendMessage(ChatColor.YELLOW + "Use /rewind history " + areaName +
                    " <page> to view other pages");
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
            List<AreaBackup> backups = backupManager.getBackupHistory(args[0]);
            int itemsPerPage = 10;
            int maxPage = Math.max(1, (int) Math.ceil((double) backups.size() / itemsPerPage));

            return Arrays.asList("1", "2", "3", "4", "5").stream()
                    .filter(pageNum -> Integer.parseInt(pageNum) <= maxPage)
                    .filter(pageNum -> pageNum.startsWith(args[1]))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public String getName() {
        return "history";
    }

    @Override
    public String getDescription() {
        return "View backup history for an area";
    }

    @Override
    public String getUsage() {
        return "/rewind history <area> [page]";
    }
}

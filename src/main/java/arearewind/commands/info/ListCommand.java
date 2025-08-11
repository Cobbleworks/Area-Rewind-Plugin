package arearewind.commands.info;

import arearewind.commands.base.BaseCommand;
import arearewind.data.ProtectedArea;
import arearewind.managers.*;
import arearewind.util.ConfigurationManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command for listing areas
 */
public class ListCommand extends BaseCommand {

    private static final List<String> FILTERS = Arrays.asList("all", "owned", "trusted");

    public ListCommand(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
            GUIManager guiManager, VisualizationManager visualizationManager,
            PermissionManager permissionManager, ConfigurationManager configManager,
            FileManager fileManager, IntervalManager intervalManager) {
        super(plugin, areaManager, backupManager, guiManager, visualizationManager,
                permissionManager, configManager, fileManager, intervalManager);
    }

    @Override
    public boolean execute(Player player, String[] args) {
        String filter = args.length > 0 ? args[0].toLowerCase() : "all";
        List<ProtectedArea> areas = new ArrayList<>();

        switch (filter) {
            case "owned":
                areas = areaManager.getOwnedAreas(player.getUniqueId());
                break;
            case "trusted":
                areas = areaManager.getTrustedAreas(player.getUniqueId());
                break;
            default:
                areas = new ArrayList<>(areaManager.getProtectedAreas().values());
                break;
        }

        if (areas.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No protected areas found!");
            return true;
        }

        player.sendMessage(ChatColor.GOLD + "=== Protected Areas (" + filter + ") ===");
        for (ProtectedArea area : areas) {
            int backupCount = backupManager.getBackupHistory(area.getName()).size();
            String ownerName = plugin.getServer().getOfflinePlayer(area.getOwner()).getName();

            player.sendMessage(ChatColor.GREEN + area.getName() + ChatColor.WHITE +
                    " - " + area.getSize() + " blocks, " + backupCount + " backups" +
                    ChatColor.GRAY + " (Owner: " + ownerName + ")");
        }

        return true;
    }

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1) {
            return FILTERS.stream()
                    .filter(filter -> filter.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getDescription() {
        return "List protected areas";
    }

    @Override
    public String getUsage() {
        return "/rewind list [all|owned|trusted]";
    }
}

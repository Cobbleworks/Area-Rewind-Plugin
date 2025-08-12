package arearewind.commands.management;

import arearewind.commands.base.BaseCommand;
import arearewind.data.ProtectedArea;
import arearewind.managers.*;
import arearewind.util.ConfigurationManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command for managing trusted players
 */
public class TrustCommand extends BaseCommand {

    public TrustCommand(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
            GUIManager guiManager, VisualizationManager visualizationManager,
            PermissionManager permissionManager, ConfigurationManager configManager,
            FileManager fileManager, IntervalManager intervalManager) {
        super(plugin, areaManager, backupManager, guiManager, visualizationManager,
                permissionManager, configManager, fileManager, intervalManager);
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (!validateMinArgs(player, args, 2)) {
            return true;
        }

        String areaName = args[0];
        String targetName = args[1];

        ProtectedArea area = validateAndGetArea(player, areaName);
        if (area == null) {
            return true;
        }

        if (!permissionManager.canModifyTrust(player, area)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to trust players for this area!");
            return true;
        }

        // Try to find online player first
        Player onlineTarget = Bukkit.getPlayerExact(targetName);
        OfflinePlayer target = onlineTarget;

        if (target == null) {
            // If not online, search through all known offline players
            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                if (targetName.equalsIgnoreCase(offlinePlayer.getName())) {
                    target = offlinePlayer;
                    break;
                }
            }
        }

        if (target == null || target.getUniqueId() == null) {
            player.sendMessage(ChatColor.RED + "Player not found: " + targetName);
            player.sendMessage(ChatColor.GRAY + "Player must have joined the server before.");
            return true;
        }

        if (area.getTrustedPlayers().contains(target.getUniqueId())) {
            player.sendMessage(ChatColor.YELLOW + targetName + " is already trusted for this area.");
            return true;
        }

        area.addTrustedPlayer(target.getUniqueId());
        areaManager.saveAreas();

        player.sendMessage(ChatColor.GREEN + targetName + " was added as trusted for '" + areaName + "'.");

        return true;
    }

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1) {
            return getAreaCompletions().stream()
                    .filter(area -> area.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            // Return online player names
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public String getName() {
        return "trust";
    }

    @Override
    public String getDescription() {
        return "Add a player as trusted for an area";
    }

    @Override
    public String getUsage() {
        return "/rewind trust <area> <player>";
    }
}

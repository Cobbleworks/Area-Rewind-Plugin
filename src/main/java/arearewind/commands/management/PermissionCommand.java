package arearewind.commands.management;

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
 * Command for managing permissions
 */
public class PermissionCommand extends BaseCommand {

    public PermissionCommand(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
            GUIManager guiManager, VisualizationManager visualizationManager,
            PermissionManager permissionManager, ConfigurationManager configManager,
            FileManager fileManager, IntervalManager intervalManager) {
        super(plugin, areaManager, backupManager, guiManager, visualizationManager,
                permissionManager, configManager, fileManager, intervalManager);
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (!validateMinArgs(player, args, 3)) {
            player.sendMessage(ChatColor.YELLOW + "Usage: " + getUsage());
            return true;
        }

        String action = args[0].toLowerCase();
        String areaName = args[1];
        String playerName = args[2];

        if (!areaManager.areaExists(areaName)) {
            player.sendMessage(ChatColor.RED + "Area '" + areaName + "' not found!");
            return true;
        }

        switch (action) {
            case "add":
                // Using trust functionality as a placeholder for permissions
                var area = areaManager.getArea(areaName);
                if (area == null) {
                    player.sendMessage(ChatColor.RED + "Area not found!");
                    return true;
                }

                var targetPlayer = plugin.getServer().getPlayer(playerName);
                if (targetPlayer == null) {
                    player.sendMessage(ChatColor.RED + "Player '" + playerName + "' not found!");
                    return true;
                }

                if (area.getTrustedPlayers().contains(targetPlayer.getUniqueId())) {
                    player.sendMessage(ChatColor.YELLOW + playerName + " is already trusted in " + areaName);
                } else {
                    area.addTrustedPlayer(targetPlayer.getUniqueId());
                    player.sendMessage(ChatColor.GREEN + "Added " + playerName + " to area " + areaName);
                }
                break;

            case "remove":
                var areaToRemove = areaManager.getArea(areaName);
                if (areaToRemove == null) {
                    player.sendMessage(ChatColor.RED + "Area not found!");
                    return true;
                }

                var targetToRemove = plugin.getServer().getPlayer(playerName);
                if (targetToRemove == null) {
                    player.sendMessage(ChatColor.RED + "Player '" + playerName + "' not found!");
                    return true;
                }

                if (areaToRemove.getTrustedPlayers().contains(targetToRemove.getUniqueId())) {
                    areaToRemove.removeTrustedPlayer(targetToRemove.getUniqueId());
                    player.sendMessage(ChatColor.GREEN + "Removed " + playerName + " from area " + areaName);
                } else {
                    player.sendMessage(ChatColor.YELLOW + playerName + " is not trusted in " + areaName);
                }
                break;

            case "list":
                var areaToList = areaManager.getArea(areaName);
                if (areaToList == null) {
                    player.sendMessage(ChatColor.RED + "Area not found!");
                    return true;
                }

                var trusted = areaToList.getTrustedPlayers();
                if (trusted.isEmpty()) {
                    player.sendMessage(ChatColor.YELLOW + "No trusted players for area " + areaName);
                } else {
                    player.sendMessage(ChatColor.GOLD + "Trusted players for area " + areaName + ":");
                    for (var uuid : trusted) {
                        var p = plugin.getServer().getOfflinePlayer(uuid);
                        player.sendMessage(
                                ChatColor.WHITE + "- " + (p.getName() != null ? p.getName() : uuid.toString()));
                    }
                }
                break;
            default:
                player.sendMessage(ChatColor.RED + "Invalid action! Use: add, remove, or list");
                break;
        }

        return true;
    }

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1) {
            List<String> actions = Arrays.asList("add", "remove", "list");
            return actions.stream()
                    .filter(action -> action.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            return getAreaCompletions().stream()
                    .filter(area -> area.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 3 && !args[0].equalsIgnoreCase("list")) {
            return plugin.getServer().getOnlinePlayers().stream()
                    .map(p -> p.getName())
                    .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public String getName() {
        return "permission";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("perm", "permissions");
    }

    @Override
    public String getDescription() {
        return "Manage area permissions";
    }

    @Override
    public String getUsage() {
        return "/rewind permission <add|remove|list> <area> [player]";
    }
}

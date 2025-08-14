package arearewind.commands.info;

import arearewind.commands.base.BaseCommand;
import arearewind.data.AreaBackup;
import arearewind.data.ProtectedArea;
import arearewind.managers.*;
import arearewind.util.ConfigurationManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command for viewing area information
 */
public class InfoCommand extends BaseCommand {

    public InfoCommand(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
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
        ProtectedArea area = validateAndGetArea(player, areaName);
        if (area == null) {
            return true;
        }

        List<AreaBackup> backups = backupManager.getBackupHistory(areaName);

        player.sendMessage(ChatColor.GOLD + "=== Area Info: " + areaName + " ===");
        player.sendMessage(ChatColor.GREEN + "Owner: " + ChatColor.WHITE +
                Bukkit.getOfflinePlayer(area.getOwner()).getName());
        player.sendMessage(ChatColor.GREEN + "World: " + ChatColor.WHITE +
                area.getPos1().getWorld().getName());
        player.sendMessage(ChatColor.GREEN + "Position 1: " + ChatColor.WHITE +
                areaManager.locationToString(area.getPos1()));
        player.sendMessage(ChatColor.GREEN + "Position 2: " + ChatColor.WHITE +
                areaManager.locationToString(area.getPos2()));
        player.sendMessage(ChatColor.GREEN + "Size: " + ChatColor.WHITE +
                area.getSize() + " blocks");
        player.sendMessage(ChatColor.GREEN + "Backups: " + ChatColor.WHITE + backups.size());

        if (!backups.isEmpty()) {
            AreaBackup lastBackup = backups.get(backups.size() - 1);
            player.sendMessage(ChatColor.GREEN + "Last Backup: " + ChatColor.WHITE +
                    lastBackup.getTimestamp().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
        }

        if (!area.getTrustedPlayers().isEmpty()) {
            List<String> trustedNames = area.getTrustedPlayers().stream()
                    .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                    .collect(Collectors.toList());
            player.sendMessage(ChatColor.GREEN + "Trusted Players: " + ChatColor.WHITE +
                    String.join(", ", trustedNames));
        }

        return true;
    }

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1) {
            return getAreaCompletions().stream()
                    .filter(area -> area.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getDescription() {
        return "View detailed information about an area";
    }

    @Override
    public String getUsage() {
        return "/rewind info <area>";
    }
}

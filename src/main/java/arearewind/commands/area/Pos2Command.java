package arearewind.commands.area;

import arearewind.commands.base.BaseCommand;
import arearewind.managers.*;
import arearewind.util.ConfigurationManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Command for setting position 2
 */
public class Pos2Command extends BaseCommand {

    public Pos2Command(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
            GUIManager guiManager, VisualizationManager visualizationManager,
            PermissionManager permissionManager, ConfigurationManager configManager,
            FileManager fileManager, IntervalManager intervalManager) {
        super(plugin, areaManager, backupManager, guiManager, visualizationManager,
                permissionManager, configManager, fileManager, intervalManager);
    }

    @Override
    public boolean execute(Player player, String[] args) {
        Block targetBlock = player.getTargetBlockExact(100);
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "No valid position found!");
            return true;
        }

        areaManager.setPosition2(player.getUniqueId(), targetBlock.getLocation());
        player.sendMessage(ChatColor.GREEN + "Position 2 set: " + ChatColor.WHITE +
                areaManager.locationToString(targetBlock.getLocation()));
        return true;
    }

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public String getName() {
        return "pos2";
    }

    @Override
    public String getDescription() {
        return "Set the second position for area selection";
    }

    @Override
    public String getUsage() {
        return "/rewind pos2";
    }
}

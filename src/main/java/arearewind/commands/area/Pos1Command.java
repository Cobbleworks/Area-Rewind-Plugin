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
 * Command for setting position 1
 */
public class Pos1Command extends BaseCommand {

    public Pos1Command(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
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

        areaManager.setPosition1(player.getUniqueId(), targetBlock.getLocation());
        player.sendMessage(ChatColor.GREEN + "Position 1 set: " + ChatColor.WHITE +
                areaManager.locationToString(targetBlock.getLocation()));
        return true;
    }

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public String getName() {
        return "pos1";
    }

    @Override
    public String getDescription() {
        return "Set the first position for area selection";
    }

    @Override
    public String getUsage() {
        return "/rewind pos1";
    }
}

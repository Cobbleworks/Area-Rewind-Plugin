package arearewind.commands.utility;

import arearewind.commands.base.BaseCommand;
import arearewind.managers.*;
import arearewind.util.ConfigurationManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Particle;

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

        String particleType = "FLAME"; // Default particle
        if (args.length > 2) {
            particleType = args[2].toUpperCase();
        }

        try {
            // Get area and backup
            arearewind.data.ProtectedArea area = areaManager.getArea(areaName);
            if (area == null) {
                player.sendMessage(ChatColor.RED + "Area not found!");
                return true;
            }
            arearewind.data.AreaBackup backup = backupManager.getBackup(areaName, backupId);
            if (backup == null) {
                player.sendMessage(ChatColor.RED + "Backup not found!");
                return true;
            }

            // Set particle type
            Particle particle;
            try {
                particle = Particle.valueOf(particleType);
            } catch (IllegalArgumentException e) {
                particle = Particle.FLAME;
                player.sendMessage(ChatColor.RED + "Unknown particle type, using default: FLAME");
            }
            visualizationManager.setPlayerParticle(player, particle);

            // Show backup preview
            boolean toggled = visualizationManager.toggleBackupPreview(player, backup, area);
            if (toggled) {
                player.sendMessage(ChatColor.GREEN + "Showing preview of " + areaName +
                        " (backup " + backupId + ") with " + particleType + " particles");
            } else {
                player.sendMessage(ChatColor.YELLOW + "Preview hidden for " + areaName);
            }
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
            List<String> particles = Arrays.asList(
                    "SMOKE_NORMAL", "FLAME", "SOUL_FIRE_FLAME", "REDSTONE", "CLOUD", "ENCHANTMENT_TABLE",
                    "VILLAGER_HAPPY",
                    "HEART", "CRIT", "SPELL", "NOTE", "PORTAL", "EXPLOSION_NORMAL", "LAVA", "WATER_SPLASH",
                    "SLIME", "SNOWBALL", "TOTEM", "DRAGON_BREATH", "FIREWORKS_SPARK", "BUBBLE_POP", "COMPOSTER",
                    "SNEEZE", "ASH", "SOUL_FIRE_FLAME", "SOUL", "WAX_ON", "WAX_OFF", "ELECTRIC_SPARK");
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

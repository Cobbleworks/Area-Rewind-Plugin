package arearewind.commands.area;

import arearewind.commands.base.BaseCommand;
import arearewind.commands.analysis.DiffCommand;
import arearewind.commands.analysis.ScanCommand;
import arearewind.commands.info.InfoCommand;
import arearewind.commands.maintenance.CleanupCommand;
import arearewind.commands.management.IntervalCommand;
import arearewind.commands.management.SetIconCommand;
import arearewind.commands.management.TrustCommand;
import arearewind.commands.management.UntrustCommand;
import arearewind.data.AreaBackup;
import arearewind.data.ProtectedArea;
import arearewind.managers.*;
import arearewind.util.ConfigurationManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Namespaced command for area-specific settings and management.
 */
public class AreaCommand extends BaseCommand {

    private static final List<String> SETTINGS = Arrays.asList(
            "info", "name", "icon", "backup-icon", "speed", "trust", "interval", "scan", "diff", "cleanup");

    private final RenameCommand renameCommand;
    private final SetIconCommand setIconCommand;
    private final TrustCommand trustCommand;
    private final UntrustCommand untrustCommand;
    private final SpeedCommand speedCommand;
    private final IntervalCommand intervalCommand;
    private final ScanCommand scanCommand;
    private final DiffCommand diffCommand;
    private final InfoCommand infoCommand;
    private final CleanupCommand cleanupCommand;

    public AreaCommand(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
            GUIManager guiManager, VisualizationManager visualizationManager,
            PermissionManager permissionManager, ConfigurationManager configManager,
            FileManager fileManager, IntervalManager intervalManager) {
        super(plugin, areaManager, backupManager, guiManager, visualizationManager,
                permissionManager, configManager, fileManager, intervalManager);
        this.renameCommand = new RenameCommand(plugin, areaManager, backupManager, guiManager,
                visualizationManager, permissionManager, configManager, fileManager, intervalManager);
        this.setIconCommand = new SetIconCommand(plugin, areaManager, backupManager, guiManager,
                visualizationManager, permissionManager, configManager, fileManager, intervalManager);
        this.trustCommand = new TrustCommand(plugin, areaManager, backupManager, guiManager,
                visualizationManager, permissionManager, configManager, fileManager, intervalManager);
        this.untrustCommand = new UntrustCommand(plugin, areaManager, backupManager, guiManager,
                visualizationManager, permissionManager, configManager, fileManager, intervalManager);
        this.speedCommand = new SpeedCommand(plugin, areaManager, backupManager, guiManager,
                visualizationManager, permissionManager, configManager, fileManager, intervalManager);
        this.intervalCommand = new IntervalCommand(plugin, areaManager, backupManager, guiManager,
                visualizationManager, permissionManager, configManager, fileManager, intervalManager);
        this.scanCommand = new ScanCommand(plugin, areaManager, backupManager, guiManager,
                visualizationManager, permissionManager, configManager, fileManager, intervalManager);
        this.diffCommand = new DiffCommand(plugin, areaManager, backupManager, guiManager,
                visualizationManager, permissionManager, configManager, fileManager, intervalManager);
        this.infoCommand = new InfoCommand(plugin, areaManager, backupManager, guiManager,
                visualizationManager, permissionManager, configManager, fileManager, intervalManager);
        this.cleanupCommand = new CleanupCommand(plugin, areaManager, backupManager, guiManager,
                visualizationManager, permissionManager, configManager, fileManager, intervalManager);
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            sendUsage(player);
            return true;
        }

        if (args.length < 2) {
            sendUsage(player);
            return true;
        }

        String areaName = args[0];
        String setting = args[1].toLowerCase();

        switch (setting) {
            case "info":
            case "view":
                if (args.length != 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /rewind area <area> info");
                    return true;
                }
                return infoCommand.execute(player, new String[] { areaName });

            case "name":
            case "rename":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /rewind area <area> name <new_name>");
                    return true;
                }
                return renameCommand.execute(player, new String[] { areaName, args[2] });

            case "icon":
            case "seticon":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /rewind area <area> icon <material|hand>");
                    return true;
                }
                return setIconCommand.execute(player, new String[] { areaName, args[2] });

            case "backup-icon":
            case "backupicon":
                if (args.length < 4) {
                    player.sendMessage(ChatColor.RED
                            + "Usage: /rewind area <area> backup-icon <backup_id> <material|hand>");
                    return true;
                }
                return setIconCommand.execute(player, new String[] { "backup", areaName, args[2], args[3] });

            case "speed":
            case "restore-speed":
            case "restorespeed":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /rewind area <area> speed <speed|dynamic>");
                    return true;
                }
                return speedCommand.execute(player, new String[] { areaName, args[2] });

            case "trust":
                return handleTrust(player, areaName, args);

            case "interval":
                return handleInterval(player, areaName, args);

            case "scan":
                if (args.length != 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /rewind area <area> scan");
                    return true;
                }
                return scanCommand.execute(player, new String[] { areaName });

            case "diff":
                if (args.length < 4) {
                    player.sendMessage(ChatColor.RED
                            + "Usage: /rewind area <area> diff <id1|current> <id2|current>");
                    return true;
                }
                return diffCommand.execute(player, new String[] { areaName, args[2], args[3] });

            case "cleanup":
            case "clean":
                if (args.length > 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /rewind area <area> cleanup [days]");
                    return true;
                }
                if (args.length == 3) {
                    return cleanupCommand.execute(player, new String[] { areaName, args[2] });
                }
                return cleanupCommand.execute(player, new String[] { areaName });

            default:
                player.sendMessage(ChatColor.RED + "Unknown area setting: " + setting);
                player.sendMessage(ChatColor.GRAY + "Use " + ChatColor.GREEN + "/rewind area help"
                        + ChatColor.GRAY + " for area settings.");
                return true;
        }
    }

    private boolean handleTrust(Player player, String areaName, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /rewind area <area> trust <add|remove|list> [player]");
            return true;
        }

        String action = args[2].toLowerCase();
        switch (action) {
            case "add":
            case "grant":
                if (args.length < 4) {
                    player.sendMessage(ChatColor.RED + "Usage: /rewind area <area> trust add <player>");
                    return true;
                }
                return trustCommand.execute(player, new String[] { areaName, args[3] });

            case "remove":
            case "revoke":
            case "delete":
                if (args.length < 4) {
                    player.sendMessage(ChatColor.RED + "Usage: /rewind area <area> trust remove <player>");
                    return true;
                }
                return untrustCommand.execute(player, new String[] { areaName, args[3] });

            case "list":
                return listTrustedPlayers(player, areaName);

            default:
                player.sendMessage(ChatColor.RED + "Invalid trust action. Use: add, remove, or list");
                return true;
        }
    }

    private boolean handleInterval(Player player, String areaName, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED
                    + "Usage: /rewind area <area> interval <set|remove|check> [minutes] [backup_id]");
            return true;
        }

        String[] intervalArgs = new String[args.length - 1];
        intervalArgs[0] = args[2];
        intervalArgs[1] = areaName;
        if (args.length > 3) {
            System.arraycopy(args, 3, intervalArgs, 2, args.length - 3);
        }

        return intervalCommand.execute(player, intervalArgs);
    }

    private boolean listTrustedPlayers(Player player, String areaName) {
        ProtectedArea area = validateAndGetArea(player, areaName);
        if (area == null) {
            return true;
        }

        if (!permissionManager.hasAreaPermission(player, area)) {
            player.sendMessage(ChatColor.RED + "You don't have permission for this area!");
            return true;
        }

        if (area.getTrustedPlayers().isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No trusted players for area '" + areaName + "'.");
            return true;
        }

        player.sendMessage(ChatColor.GOLD + "Trusted players for area '" + areaName + "':");
        for (UUID uuid : area.getTrustedPlayers()) {
            String playerName = Bukkit.getOfflinePlayer(uuid).getName();
            player.sendMessage(ChatColor.GRAY + "- " + ChatColor.WHITE
                    + (playerName != null ? playerName : uuid.toString()));
        }
        return true;
    }

    private void sendUsage(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Area Commands ===");
        player.sendMessage(ChatColor.GRAY + "  /rewind area <area> info");
        player.sendMessage(ChatColor.GRAY + "  /rewind area <area> name <new_name>");
        player.sendMessage(ChatColor.GRAY + "  /rewind area <area> icon <material|hand>");
        player.sendMessage(ChatColor.GRAY + "  /rewind area <area> backup-icon <backup_id> <material|hand>");
        player.sendMessage(ChatColor.GRAY + "  /rewind area <area> speed <speed|dynamic>");
        player.sendMessage(ChatColor.GRAY + "  /rewind area <area> trust <add|remove|list> [player]");
        player.sendMessage(ChatColor.GRAY + "  /rewind area <area> interval <set|remove|check> [minutes] [backup_id]");
        player.sendMessage(ChatColor.GRAY + "  /rewind area <area> scan");
        player.sendMessage(ChatColor.GRAY + "  /rewind area <area> diff <id1|current> <id2|current>");
        player.sendMessage(ChatColor.GRAY + "  /rewind area <area> cleanup [days]");
    }

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>(getAreaCompletions());
            completions.add("help");
            return filterCompletions(completions, args[0]);
        }

        if (args.length == 2) {
            return filterCompletions(SETTINGS, args[1]);
        }

        String areaName = args[0];
        String setting = args[1].toLowerCase();

        switch (setting) {
            case "info":
            case "view":
            case "name":
            case "rename":
                return List.of();
            case "icon":
            case "seticon":
                return setIconCommand.getTabCompletions(player, new String[] { areaName, args[2] });
            case "backup-icon":
            case "backupicon":
                return getBackupIconCompletions(player, areaName, args);
            case "speed":
            case "restore-speed":
            case "restorespeed":
                return speedCommand.getTabCompletions(player, new String[] { areaName, args[2] });
            case "trust":
                return getTrustCompletions(player, areaName, args);
            case "interval":
                return getIntervalCompletions(player, areaName, args);
            case "scan":
                return List.of();
            case "diff":
                return getDiffCompletions(player, areaName, args);
            case "cleanup":
            case "clean":
                return getCleanupCompletions(player, areaName, args);
            default:
                return List.of();
        }
    }

    private List<String> getBackupIconCompletions(Player player, String areaName, String[] args) {
        if (args.length == 3) {
            ProtectedArea area = areaManager.getArea(areaName);
            if (area == null || !permissionManager.hasAreaPermission(player, area)) {
                return List.of();
            }
            return backupManager.getBackupHistory(areaName).stream()
                    .map(AreaBackup::getId)
                    .filter(id -> id.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 4) {
            return setIconCommand.getTabCompletions(player,
                    new String[] { "backup", areaName, args[2], args[3] });
        }

        return List.of();
    }

    private List<String> getTrustCompletions(Player player, String areaName, String[] args) {
        if (args.length == 3) {
            return filterCompletions(Arrays.asList("add", "remove", "list"), args[2]);
        }

        if (args.length == 4) {
            if (args[2].equalsIgnoreCase("add") || args[2].equalsIgnoreCase("grant")) {
                return trustCommand.getTabCompletions(player, new String[] { areaName, args[3] });
            }
            if (args[2].equalsIgnoreCase("remove") || args[2].equalsIgnoreCase("revoke")
                    || args[2].equalsIgnoreCase("delete")) {
                return untrustCommand.getTabCompletions(player, new String[] { areaName, args[3] });
            }
        }

        return List.of();
    }

    private List<String> getIntervalCompletions(Player player, String areaName, String[] args) {
        if (args.length == 3) {
            return intervalCommand.getTabCompletions(player, new String[] { args[2] });
        }

        String[] intervalArgs = new String[args.length - 1];
        intervalArgs[0] = args[2];
        intervalArgs[1] = areaName;
        if (args.length > 3) {
            System.arraycopy(args, 3, intervalArgs, 2, args.length - 3);
        }

        return intervalCommand.getTabCompletions(player, intervalArgs);
    }

    private List<String> getDiffCompletions(Player player, String areaName, String[] args) {
        if (args.length == 3 || args.length == 4) {
            String partial = args[args.length - 1].toLowerCase();
            List<String> completions = getBackupIdCompletions(areaName).stream()
                    .filter(id -> id.startsWith(partial))
                    .collect(Collectors.toList());

            if ("current".startsWith(partial)) {
                completions.add("current");
            }

            return completions;
        }

        return List.of();
    }

    private List<String> getCleanupCompletions(Player player, String areaName, String[] args) {
        if (args.length == 3) {
            return cleanupCommand.getTabCompletions(player, new String[] { areaName, args[2] });
        }

        return List.of();
    }

    private List<String> filterCompletions(List<String> values, String partial) {
        String lowerPartial = partial.toLowerCase();
        return values.stream()
                .filter(value -> value.toLowerCase().startsWith(lowerPartial))
                .collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return "area";
    }

    @Override
    public String getDescription() {
        return "Manage and inspect area-specific options";
    }

    @Override
    public String getUsage() {
        return "/rewind area <area> <info|name|icon|backup-icon|speed|trust|interval|scan|diff|cleanup> ...";
    }

    @Override
    public String getRequiredPermission() {
        return PermissionManager.PERMISSION_USE;
    }
}

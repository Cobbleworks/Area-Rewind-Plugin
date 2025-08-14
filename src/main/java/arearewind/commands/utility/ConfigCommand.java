package arearewind.commands.utility;

import arearewind.commands.base.BaseCommand;
import arearewind.listeners.PlayerInteractionListener;
import arearewind.managers.*;
import arearewind.util.ConfigurationManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Command for managing player configuration settings
 */
public class ConfigCommand extends BaseCommand {

    private PlayerInteractionListener playerListener;

    public ConfigCommand(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
            GUIManager guiManager, VisualizationManager visualizationManager,
            PermissionManager permissionManager, ConfigurationManager configManager,
            FileManager fileManager, IntervalManager intervalManager) {
        super(plugin, areaManager, backupManager, guiManager, visualizationManager,
                permissionManager, configManager, fileManager, intervalManager);
    }

    public void setPlayerInteractionListener(PlayerInteractionListener listener) {
        this.playerListener = listener;
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (args.length == 0) {
            showConfigHelp(player);
            return true;
        }

        String setting = args[0].toLowerCase();

        switch (setting) {
            case "hoeselection":
            case "hoe":
            case "woodenhoe":
                return handleHoeSelection(player, args);

            case "progresslogs":
            case "progress":
            case "logging":
                return handleProgressLogging(player, args);

            case "list":
            case "show":
                return showCurrentSettings(player);

            default:
                player.sendMessage(ChatColor.RED + "Unknown setting: " + setting);
                showConfigHelp(player);
                return true;
        }
    }

    private boolean handleHoeSelection(Player player, String[] args) {
        if (playerListener == null) {
            player.sendMessage(ChatColor.RED + "Wooden hoe configuration is not available!");
            return true;
        }

        if (args.length < 2) {
            boolean current = playerListener.getPlayerWoodenHoeMode(player);
            player.sendMessage(ChatColor.YELLOW + "Wooden hoe selection: " +
                    (current ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
            player.sendMessage(ChatColor.GRAY + "Usage: /rewind config hoeselection <true|false|on|off>");
            return true;
        }

        boolean enabled = parseBooleanValue(args[1]);
        if (enabled == Boolean.parseBoolean("null")) {
            player.sendMessage(ChatColor.RED + "Invalid value! Use: true, false, on, or off");
            return true;
        }

        playerListener.setPlayerWoodenHoeMode(player, enabled);
        return true;
    }

    private boolean handleProgressLogging(Player player, String[] args) {
        if (playerListener == null) {
            player.sendMessage(ChatColor.RED + "Progress logging configuration is not available!");
            return true;
        }

        if (args.length < 2) {
            boolean current = playerListener.getPlayerProgressLoggingMode(player);
            player.sendMessage(ChatColor.YELLOW + "Progress logging: " +
                    (current ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
            player.sendMessage(ChatColor.GRAY + "Usage: /rewind config progresslogs <true|false|on|off>");
            return true;
        }

        boolean enabled = parseBooleanValue(args[1]);
        if (enabled == Boolean.parseBoolean("null")) {
            player.sendMessage(ChatColor.RED + "Invalid value! Use: true, false, on, or off");
            return true;
        }

        playerListener.setPlayerProgressLoggingMode(player, enabled);
        return true;
    }

    private boolean showCurrentSettings(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Your Current Settings ===");

        if (playerListener != null) {
            boolean hoeMode = playerListener.getPlayerWoodenHoeMode(player);
            boolean progressLogging = playerListener.getPlayerProgressLoggingMode(player);

            player.sendMessage(ChatColor.YELLOW + "Wooden Hoe Selection: " +
                    (hoeMode ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
            player.sendMessage(ChatColor.YELLOW + "Progress Logging: " +
                    (progressLogging ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
        } else {
            player.sendMessage(ChatColor.RED + "Configuration system not available!");
        }

        return true;
    }

    private void showConfigHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Configuration Help ===");
        player.sendMessage(ChatColor.YELLOW + "Available settings:");
        player.sendMessage(ChatColor.GREEN + "/rewind config hoeselection <true|false>" +
                ChatColor.GRAY + " - Toggle wooden hoe selection");
        player.sendMessage(ChatColor.GREEN + "/rewind config progresslogs <true|false>" +
                ChatColor.GRAY + " - Toggle restore progress messages");
        player.sendMessage(ChatColor.GREEN + "/rewind config list" +
                ChatColor.GRAY + " - Show your current settings");
        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY + "You can use: true/false or on/off");
    }

    private boolean parseBooleanValue(String value) {
        String val = value.toLowerCase();
        switch (val) {
            case "true":
            case "on":
            case "yes":
            case "enable":
            case "enabled":
                return true;
            case "false":
            case "off":
            case "no":
            case "disable":
            case "disabled":
                return false;
            default:
                return Boolean.parseBoolean("null"); // Invalid value
        }
    }

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.addAll(Arrays.asList("hoeselection", "progresslogs", "list"));
            return completions.stream()
                    .filter(completion -> completion.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        } else if (args.length == 2) {
            String setting = args[0].toLowerCase();
            if (setting.equals("hoeselection") || setting.equals("progresslogs")) {
                List<String> booleanValues = Arrays.asList("true", "false", "on", "off");
                return booleanValues.stream()
                        .filter(value -> value.toLowerCase().startsWith(args[1].toLowerCase()))
                        .toList();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public String getName() {
        return "config";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("settings", "configure", "set");
    }

    @Override
    public String getDescription() {
        return "Configure your personal settings";
    }

    @Override
    public String getUsage() {
        return "/rewind config <setting> [value]";
    }
}

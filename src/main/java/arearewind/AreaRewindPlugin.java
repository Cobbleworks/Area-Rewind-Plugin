package arearewind;

import arearewind.listeners.PlayerInteractionListener;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import arearewind.util.ConfigurationManager;
import arearewind.commands.CommandHandler;
import arearewind.managers.*;
import java.util.List;

public class AreaRewindPlugin extends JavaPlugin {
    private ConfigurationManager configManager;
    private AreaManager areaManager;
    private BackupManager backupManager;
    private FileManager fileManager;
    private GUIManager guiManager;
    private VisualizationManager visualizationManager;
    private PermissionManager permissionManager;
    private IntervalManager intervalManager;
    private CommandHandler commandHandler;
    private PlayerInteractionListener playerListener;

    @Override
    public void onEnable() {
        configManager = new ConfigurationManager(this);
        configManager.loadConfiguration();
        fileManager = new FileManager(this);
        fileManager.setupFiles();

        areaManager = new AreaManager(this, fileManager);
        backupManager = new BackupManager(this, configManager, fileManager);
        permissionManager = new PermissionManager();
        intervalManager = new IntervalManager(this, backupManager, areaManager);
        guiManager = new GUIManager(this, areaManager, backupManager, permissionManager, configManager, fileManager,
                intervalManager);
        visualizationManager = new VisualizationManager(this, areaManager);
        commandHandler = new CommandHandler(this, areaManager, backupManager,
                guiManager, visualizationManager, permissionManager, configManager, fileManager, intervalManager);
        playerListener = new PlayerInteractionListener(this, areaManager, configManager);

        // Set the player listener reference in the command handler for tool commands
        commandHandler.setPlayerInteractionListener(playerListener);

        // Set the player listener reference in the GUI manager for settings GUI
        guiManager.setPlayerInteractionListener(playerListener);
        // Set the player listener reference in the backup manager for progress logging
        // preferences
        backupManager.setPlayerInteractionListener(playerListener);

        areaManager.loadAreas();
        backupManager.loadBackups();
        this.getCommand("rewind").setExecutor(commandHandler);
        this.getCommand("rewind").setTabCompleter(commandHandler);
        Bukkit.getPluginManager().registerEvents(playerListener, this);
        Bukkit.getPluginManager().registerEvents(guiManager, this);
        visualizationManager.startVisualizationTask();
        getLogger().info("Area Rewind Plugin enabled successfully!");
        getLogger().info("Loaded " + areaManager.getProtectedAreas().size() + " protected areas");
    }

    /**
     * Fully reloads and re-initializes all plugin components.
     */
    public void reloadAll() {
        // Stop running tasks and listeners
        if (visualizationManager != null)
            visualizationManager.stopVisualizationTask();

        // Save areas before reload
        if (areaManager != null)
            areaManager.saveAreas();

        // Re-initialize config and managers
        reloadConfig();
        configManager = new ConfigurationManager(this);
        configManager.loadConfiguration();
        fileManager = new FileManager(this);
        fileManager.setupFiles();

        areaManager = new AreaManager(this, fileManager);
        backupManager = new BackupManager(this, configManager, fileManager);
        permissionManager = new PermissionManager();
        intervalManager = new IntervalManager(this, backupManager, areaManager);
        guiManager = new GUIManager(this, areaManager, backupManager, permissionManager, configManager, fileManager,
                intervalManager);
        visualizationManager = new VisualizationManager(this, areaManager);
        commandHandler = new CommandHandler(this, areaManager, backupManager,
                guiManager, visualizationManager, permissionManager, configManager, fileManager, intervalManager);
        playerListener = new PlayerInteractionListener(this, areaManager, configManager);

        // Set the player listener reference in the command handler for tool commands
        commandHandler.setPlayerInteractionListener(playerListener);

        // Set the player listener reference in the GUI manager for settings GUI
        guiManager.setPlayerInteractionListener(playerListener);
        // Set the player listener reference in the backup manager for progress logging
        // preferences
        backupManager.setPlayerInteractionListener(playerListener);

        areaManager.loadAreas();
        backupManager.loadBackups();

        // Re-register command and listeners
        this.getCommand("rewind").setExecutor(commandHandler);
        this.getCommand("rewind").setTabCompleter(commandHandler);
        Bukkit.getPluginManager().registerEvents(playerListener, this);
        Bukkit.getPluginManager().registerEvents(guiManager, this);
        visualizationManager.startVisualizationTask();
        getLogger().info("Area Rewind Plugin fully reloaded!");
        getLogger().info("Loaded " + areaManager.getProtectedAreas().size() + " protected areas");
    }

    @Override
    public void onDisable() {
        areaManager.saveAreas();
        visualizationManager.stopVisualizationTask();
        intervalManager.stopAll();
        getLogger().info("Area Rewind Plugin disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return commandHandler.onCommand(sender, command, label, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return commandHandler.onTabComplete(sender, command, alias, args);
    }

    public ConfigurationManager getConfigManager() {
        return configManager;
    }

    public AreaManager getAreaManager() {
        return areaManager;
    }

    public BackupManager getBackupManager() {
        return backupManager;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public GUIManager getGUIManager() {
        return guiManager;
    }

    public VisualizationManager getVisualizationManager() {
        return visualizationManager;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public IntervalManager getIntervalManager() {
        return intervalManager;
    }
}

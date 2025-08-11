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
    private CommandHandler commandHandler;
    private PlayerInteractionListener playerListener;

    @Override
    public void onEnable() {
        configManager = new ConfigurationManager(this);
        configManager.loadConfiguration();
        fileManager = new FileManager(this, configManager);
        fileManager.setupFiles();
        fileManager.cleanupLegacyBackups();

        areaManager = new AreaManager(this, fileManager);
        backupManager = new BackupManager(this, configManager, fileManager);
        permissionManager = new PermissionManager();
        guiManager = new GUIManager(this, areaManager, backupManager, permissionManager);
        visualizationManager = new VisualizationManager(this, areaManager);
        commandHandler = new CommandHandler(this, areaManager, backupManager,
                guiManager, visualizationManager, permissionManager, configManager);
        playerListener = new PlayerInteractionListener(this, areaManager);
        areaManager.loadAreas();
        backupManager.loadBackups();
        this.getCommand("rewind").setExecutor(commandHandler);
        this.getCommand("rewind").setTabCompleter(commandHandler);
        Bukkit.getPluginManager().registerEvents(playerListener, this);
        Bukkit.getPluginManager().registerEvents(guiManager, this);
        if (configManager.isAutoBackupEnabled()) {
            backupManager.startAutomaticBackup();
        }
        visualizationManager.startVisualizationTask();
        getLogger().info("Area Rewind Plugin enabled successfully!");
        getLogger().info("Loaded " + areaManager.getProtectedAreas().size() + " protected areas");
    }

    @Override
    public void onDisable() {
        areaManager.saveAreas();
        visualizationManager.stopVisualizationTask();
        backupManager.stopAutomaticBackup();
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

    public ConfigurationManager getConfigManager() { return configManager; }
    public AreaManager getAreaManager() { return areaManager; }
    public BackupManager getBackupManager() { return backupManager; }
    public FileManager getFileManager() { return fileManager; }
    public GUIManager getGUIManager() { return guiManager; }
    public VisualizationManager getVisualizationManager() { return visualizationManager; }
    public PermissionManager getPermissionManager() { return permissionManager; }
}
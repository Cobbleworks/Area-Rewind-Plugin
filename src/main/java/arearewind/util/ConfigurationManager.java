package arearewind.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigurationManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private int backupInterval = 30;
    private int maxBackupsPerArea = 50;
    private int maxAreaSize = 1000000;
    private boolean compressionEnabled = true;
    private boolean autoBackupEnabled = true;
    private int rateLimitCooldown = 1000;
    private int visualizationParticleDistance = 50;

    public ConfigurationManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfiguration() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        try {
            backupInterval = config.getInt("backup.auto-interval", 30);
            maxBackupsPerArea = config.getInt("backup.max-backups-per-area", 50);
            maxAreaSize = config.getInt("performance.max-area-size", 1000000);
            compressionEnabled = config.getBoolean("backup.compression", true);
            autoBackupEnabled = config.getBoolean("backup.auto-backup", true);
            rateLimitCooldown = config.getInt("performance.rate-limit-cooldown", 1000);
            visualizationParticleDistance = config.getInt("visualization.particle-distance", 50);

            plugin.getLogger().info("Configuration loaded successfully");
            plugin.getLogger().info("Auto-backup: " + (autoBackupEnabled ? "enabled" : "disabled"));
            plugin.getLogger().info("Max area size: " + maxAreaSize + " blocks");
            plugin.getLogger().info("Max backups per area: " + maxBackupsPerArea);

        } catch (Exception e) {
            plugin.getLogger().warning("Error loading configuration, using defaults: " + e.getMessage());

            backupInterval = 30;
            maxBackupsPerArea = 50;
            maxAreaSize = 1000000;
            compressionEnabled = true;
            autoBackupEnabled = true;
            rateLimitCooldown = 1000;
            visualizationParticleDistance = 50;
        }
    }

    public void reloadConfiguration() {
        plugin.reloadConfig();
        loadConfiguration();
    }

    public int getBackupInterval() { return backupInterval; }
    public int getMaxBackupsPerArea() { return maxBackupsPerArea; }
    public int getMaxAreaSize() { return maxAreaSize; }
    public boolean isCompressionEnabled() { return compressionEnabled; }
    public boolean isAutoBackupEnabled() { return autoBackupEnabled; }
    public int getRateLimitCooldown() { return rateLimitCooldown; }
    public int getVisualizationParticleDistance() { return visualizationParticleDistance; }
}
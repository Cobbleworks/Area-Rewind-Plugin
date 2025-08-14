package arearewind.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigurationManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private int maxBackupsPerArea = 50;
    private int maxAreaSize = 1000000;
    private int rateLimitCooldown = 1000;
    private int visualizationParticleDistance = 50;
    private boolean woodenHoeAutoFallback = true;

    // Performance settings for restoration
    private int restoreMaxBatchSize = 400;
    private int restoreMinBatchSize = 100;

    public ConfigurationManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfiguration() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        try {
            maxBackupsPerArea = config.getInt("backup.max-backups-per-area", 50);
            maxAreaSize = config.getInt("performance.max-area-size", 1000000);
            rateLimitCooldown = config.getInt("performance.rate-limit-cooldown", 1000);
            visualizationParticleDistance = config.getInt("visualization.particle-distance", 50);
            woodenHoeAutoFallback = config.getBoolean("selection.wooden-hoe.auto-fallback", true);

            // Load performance settings
            restoreMaxBatchSize = config.getInt("performance.restore.max-batch-size", 400);
            restoreMinBatchSize = config.getInt("performance.restore.min-batch-size", 100);
            plugin.getLogger().info("Configuration loaded successfully");
            plugin.getLogger().info("Max area size: " + maxAreaSize + " blocks");
            plugin.getLogger().info("Max backups per area: " + maxBackupsPerArea);
            plugin.getLogger()
                    .info("Restore batch sizes - Regular: " + restoreMinBatchSize + "-" + restoreMaxBatchSize);

        } catch (Exception e) {
            plugin.getLogger().warning("Error loading configuration, using defaults: " + e.getMessage());

            maxBackupsPerArea = 50;
            maxAreaSize = 1000000;
            rateLimitCooldown = 1000;
            visualizationParticleDistance = 50;
            woodenHoeAutoFallback = true;
            restoreMaxBatchSize = 400;
            restoreMinBatchSize = 100;
        }
    }

    public void reloadConfiguration() {
        plugin.reloadConfig();
        loadConfiguration();
    }

    public int getMaxBackupsPerArea() {
        return maxBackupsPerArea;
    }

    public int getMaxAreaSize() {
        return maxAreaSize;
    }

    public int getRateLimitCooldown() {
        return rateLimitCooldown;
    }

    public int getVisualizationParticleDistance() {
        return visualizationParticleDistance;
    }

    public boolean isWoodenHoeAutoFallbackEnabled() {
        return woodenHoeAutoFallback;
    }

    // Performance settings getters
    public int getRestoreMaxBatchSize() {
        return restoreMaxBatchSize;
    }

    public int getRestoreMinBatchSize() {
        return restoreMinBatchSize;
    }

}

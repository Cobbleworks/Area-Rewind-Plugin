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
    private boolean woodenHoeEnabled = false;
    private boolean woodenHoeAutoFallback = true;
    private boolean restoreProgressLogging = true;

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
            woodenHoeEnabled = config.getBoolean("selection.wooden-hoe.enabled", false);
            woodenHoeAutoFallback = config.getBoolean("selection.wooden-hoe.auto-fallback", true);
            restoreProgressLogging = config.getBoolean("restore.progress-logging", true);

            plugin.getLogger().info("Configuration loaded successfully");
            plugin.getLogger().info("Max area size: " + maxAreaSize + " blocks");
            plugin.getLogger().info("Max backups per area: " + maxBackupsPerArea);
            plugin.getLogger().info("Wooden hoe selection: " + (woodenHoeEnabled ? "enabled" : "disabled"));
            plugin.getLogger().info("Progress logging: " + (restoreProgressLogging ? "enabled" : "disabled"));

        } catch (Exception e) {
            plugin.getLogger().warning("Error loading configuration, using defaults: " + e.getMessage());

            maxBackupsPerArea = 50;
            maxAreaSize = 1000000;
            rateLimitCooldown = 1000;
            visualizationParticleDistance = 50;
            woodenHoeEnabled = false;
            woodenHoeAutoFallback = true;
            restoreProgressLogging = true;
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

    public boolean isWoodenHoeEnabled() {
        return woodenHoeEnabled;
    }

    public boolean isWoodenHoeAutoFallbackEnabled() {
        return woodenHoeAutoFallback;
    }

    public void setWoodenHoeEnabled(boolean enabled) {
        this.woodenHoeEnabled = enabled;
        config.set("selection.wooden-hoe.enabled", enabled);
        plugin.saveConfig();
    }

    public boolean isRestoreProgressLoggingEnabled() {
        return restoreProgressLogging;
    }

    public void setRestoreProgressLoggingEnabled(boolean enabled) {
        this.restoreProgressLogging = enabled;
        config.set("restore.progress-logging", enabled);
        plugin.saveConfig();
    }

}

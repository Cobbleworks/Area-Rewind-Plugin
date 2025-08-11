package arearewind.managers;
import arearewind.data.AreaBackup;
import arearewind.data.ProtectedArea;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;
public class IntervalManager {
    private final JavaPlugin plugin;
    private final BackupManager backupManager;
    private final AreaManager areaManager;
    private final Map<String, IntervalConfig> intervalConfigs = new HashMap<>();
    private final Map<String, BukkitTask> activeTasks = new HashMap<>();
    public IntervalManager(JavaPlugin plugin, BackupManager backupManager, AreaManager areaManager) {
        this.plugin = plugin;
        this.backupManager = backupManager;
        this.areaManager = areaManager;
    }
    public boolean setInterval(String areaName, int minutes, int backupId, UUID requestedBy) {
        if (minutes <= 0) {
            clearInterval(areaName);
            return true;
        }
        ProtectedArea area = areaManager.getArea(areaName);
        if (area == null) return false;
        List<AreaBackup> backups = backupManager.getBackupHistory(areaName);
        if (backupId < 0 || backupId >= backups.size()) return false;
        clearInterval(areaName);
        IntervalConfig config = new IntervalConfig(areaName, minutes, backupId, requestedBy);
        intervalConfigs.put(areaName, config);
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            executeInterval(areaName);
        }, minutes * 60 * 20L, minutes * 60 * 20L);
        activeTasks.put(areaName, task);
        return true;
    }
    public void clearInterval(String areaName) {
        BukkitTask task = activeTasks.remove(areaName);
        if (task != null) {
            task.cancel();
        }
        intervalConfigs.remove(areaName);
    }

    private void executeInterval(String areaName) {
        IntervalConfig config = intervalConfigs.get(areaName);
        if (config == null) return;

        ProtectedArea area = areaManager.getArea(areaName);
        if (area == null) {
            clearInterval(areaName);
            return;
        }

        List<AreaBackup> backups = backupManager.getBackupHistory(areaName);
        if (config.backupId >= backups.size()) {
            clearInterval(areaName);
            return;
        }

        AreaBackup backup = backups.get(config.backupId);

        Bukkit.getScheduler().runTask(plugin, () -> {
            backupManager.restoreFromBackup(area, backup);

            Player owner = Bukkit.getPlayer(area.getOwner());
            if (owner != null && owner.isOnline()) {
                owner.sendMessage(ChatColor.GREEN + "Auto-restore: Area '" + areaName +
                        "' restored to backup " + config.backupId);
            }
        });
    }

    public IntervalConfig getIntervalConfig(String areaName) {
        return intervalConfigs.get(areaName);
    }

    public Map<String, IntervalConfig> getAllIntervals() {
        return new HashMap<>(intervalConfigs);
    }

    public void stopAll() {
        for (BukkitTask task : activeTasks.values()) {
            task.cancel();
        }
        activeTasks.clear();
        intervalConfigs.clear();
    }

    public static class IntervalConfig {
        public final String areaName;
        public final int minutes;
        public final int backupId;
        public final UUID requestedBy;

        public IntervalConfig(String areaName, int minutes, int backupId, UUID requestedBy) {
            this.areaName = areaName;
            this.minutes = minutes;
            this.backupId = backupId;
            this.requestedBy = requestedBy;
        }
    }
}
package arearewind.managers;
import arearewind.data.AreaBackup;
import arearewind.data.BlockInfo;
import arearewind.data.ProtectedArea;
import arearewind.util.ConfigurationManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

public class FileManager {
    private final JavaPlugin plugin;
    private final ConfigurationManager configManager;
    private File dataFolder;
    private File backupFolder;
    private File areasFile;
    private File exportsFolder;

    public FileManager(JavaPlugin plugin, ConfigurationManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void setupFiles() {
        dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        backupFolder = new File(dataFolder, "backups");
        if (!backupFolder.exists()) {
            backupFolder.mkdirs();
        }
        exportsFolder = new File(dataFolder, "exports");
        if (!exportsFolder.exists()) {
            exportsFolder.mkdirs();
        }
        areasFile = new File(dataFolder, "areas.yml");
        plugin.getLogger().info("File structure initialized");
    }

    public void saveBackupToFile(String areaName, AreaBackup backup) {
        try {
            File backupFile = new File(backupFolder, areaName + "_" + backup.getId() + ".yml");
            backupFile.getParentFile().mkdirs();

            YamlConfiguration config = new YamlConfiguration();

            config.set("backup.id", backup.getId());
            config.set("backup.timestamp", backup.getTimestamp().toString());

            Map<String, Object> blockData = new HashMap<>();
            for (Map.Entry<String, BlockInfo> entry : backup.getBlocks().entrySet()) {
                try {
                    blockData.put(entry.getKey(), entry.getValue().serialize());
                } catch (Exception blockError) {
                    plugin.getLogger().warning("Failed to serialize block at " + entry.getKey() +
                            ": " + blockError.getMessage() + " - Skipping this block");
                }
            }
            config.set("backup.blocks", blockData);

            if (backup.getEntities() != null && !backup.getEntities().isEmpty()) {
                config.set("backup.entities", backup.getEntities());
            }

            config.save(backupFile);
            plugin.getLogger().fine("Successfully saved backup file: " + backupFile.getName());

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save backup to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public AreaBackup loadBackupFromFile(String areaName, String backupId) {
        try {
            File backupFile = new File(backupFolder, areaName + "_" + backupId + ".yml");
            if (!backupFile.exists()) {
                plugin.getLogger().warning("Backup file not found: " + backupFile.getName());
                return null;
            }

            YamlConfiguration config = new YamlConfiguration();
            config.load(backupFile);

            try {
                String id = config.getString("backup.id", backupId);
                String timestampStr = config.getString("backup.timestamp");
                LocalDateTime timestamp = timestampStr != null ?
                        LocalDateTime.parse(timestampStr) : LocalDateTime.now();

                Map<String, BlockInfo> blocks = new HashMap<>();

                if (config.contains("backup.blocks")) {
                    Object blocksObj = config.get("backup.blocks");
                    if (blocksObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> blockData = (Map<String, Object>) blocksObj;

                        for (Map.Entry<String, Object> entry : blockData.entrySet()) {
                            try {
                                if (entry.getValue() instanceof Map) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> blockMap = (Map<String, Object>) entry.getValue();
                                    BlockInfo blockInfo = BlockInfo.deserialize(blockMap);
                                    blocks.put(entry.getKey(), blockInfo);
                                }
                            } catch (Exception blockError) {
                                plugin.getLogger().warning("Failed to deserialize block " + entry.getKey() +
                                        ": " + blockError.getMessage() + " - Using air block");
                                blocks.put(entry.getKey(), new BlockInfo(org.bukkit.Material.AIR, org.bukkit.Material.AIR.createBlockData()));
                            }
                        }
                    }
                }

                Map<String, Object> entities = new HashMap<>();
                if (config.contains("backup.entities")) {
                    Object entitiesObj = config.get("backup.entities");
                    if (entitiesObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> entityData = (Map<String, Object>) entitiesObj;
                        entities.putAll(entityData);
                    }
                }

                AreaBackup backup = new AreaBackup(id, timestamp, blocks, entities);
                plugin.getLogger().fine("Successfully loaded backup manually: " + backupFile.getName() +
                        " (Blocks: " + blocks.size() + ", Entities: " + entities.size() + ")");
                return backup;

            } catch (Exception manualError) {
                plugin.getLogger().warning("Manual deserialization failed for " + backupFile.getName() +
                        ": " + manualError.getMessage() + " - Trying legacy format");
            }

            try {
                Object backupObj = config.get("backup");
                if (backupObj != null) {
                    plugin.getLogger().info("Detected legacy backup format for " + backupFile.getName() + " - skipping for now");
                    return null;
                }
            } catch (Exception legacyError) {
                plugin.getLogger().warning("Legacy format loading also failed for " + backupFile.getName());
            }

            return null;

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load backup from file: " + e.getMessage());
            return null;
        }
    }

    public void cleanupLegacyBackups() {
        try {
            File[] files = backupFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files == null) return;

            int cleaned = 0;
            for (File file : files) {
                try {
                    YamlConfiguration config = new YamlConfiguration();
                    config.load(file);

                    if (config.contains("backup") && !config.contains("backup.id")) {
                        File backupFile = new File(file.getParentFile(), file.getName() + ".legacy");
                        if (file.renameTo(backupFile)) {
                            cleaned++;
                            plugin.getLogger().info("Moved legacy backup to: " + backupFile.getName());
                        }
                    }
                } catch (Exception e) {

                }
            }

            if (cleaned > 0) {
                plugin.getLogger().info("Moved " + cleaned + " legacy backup files. New backups will be created in the new format.");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to cleanup legacy backups: " + e.getMessage());
        }
    }

    public void deleteBackupFile(String areaName, String backupId) {
        File backupFile = new File(backupFolder, areaName + "_" + backupId + ".yml");
        if (backupFile.exists()) {
            if (backupFile.delete()) {
                plugin.getLogger().fine("Deleted backup file: " + backupFile.getName());
            } else {
                plugin.getLogger().warning("Failed to delete backup file: " + backupFile.getName());
            }
        }
    }

    public void deleteBackupFiles(String areaName) {
        File[] files = backupFolder.listFiles((dir, name) ->
                name.startsWith(areaName + "_") && name.endsWith(".yml"));

        if (files != null) {
            int deletedCount = 0;
            for (File file : files) {
                if (file.delete()) {
                    deletedCount++;
                }
            }
            plugin.getLogger().info("Deleted " + deletedCount + " backup files for area: " + areaName);
        }
    }

    public void renameBackupFiles(String oldName, String newName) {
        File[] files = backupFolder.listFiles((dir, name) ->
                name.startsWith(oldName + "_") && name.endsWith(".yml"));

        if (files != null) {
            int renamedCount = 0;
            for (File file : files) {
                String fileName = file.getName();
                String newFileName = fileName.replace(oldName + "_", newName + "_");
                File newFile = new File(backupFolder, newFileName);
                if (file.renameTo(newFile)) {
                    renamedCount++;
                }
            }
            plugin.getLogger().info("Renamed " + renamedCount + " backup files from '" + oldName + "' to '" + newName + "'");
        }
    }

    public long getTotalBackupFileSize() {
        long totalSize = 0;
        File[] files = backupFolder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    totalSize += file.length();
                }
            }
        }

        return totalSize;
    }

    public File[] getBackupFiles(String areaName) {
        return backupFolder.listFiles((dir, name) ->
                name.startsWith(areaName + "_") && name.endsWith(".yml"));
    }

    public File[] getAllBackupFiles() {
        return backupFolder.listFiles((dir, name) -> name.endsWith(".yml"));
    }

    public String getDiskUsageStats() {
        long backupSize = getTotalBackupFileSize();
        long freeSpace = dataFolder.getFreeSpace();
        long totalSpace = dataFolder.getTotalSpace();

        return String.format("Backup storage: %s | Free space: %s | Total space: %s",
                formatFileSize(backupSize),
                formatFileSize(freeSpace),
                formatFileSize(totalSpace));
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    public File getDataFolder() { return dataFolder; }
    public File getBackupFolder() { return backupFolder; }
    public File getAreasFile() { return areasFile; }
    public File getExportsFolder() { return exportsFolder; }
}
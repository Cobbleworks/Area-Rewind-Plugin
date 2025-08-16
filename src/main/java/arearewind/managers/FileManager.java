package arearewind.managers;

import arearewind.data.AreaBackup;
import arearewind.data.BlockInfo;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPInputStream;

public class FileManager {
    private final JavaPlugin plugin;
    private File dataFolder;
    private File backupFolder;
    private File areasFile;
    private File exportsFolder;

    // Compression settings
    private static final boolean USE_COMPRESSION = true;
    private static final String COMPRESSED_EXTENSION = ".yml.gz";
    private static final String UNCOMPRESSED_EXTENSION = ".yml";

    public FileManager(JavaPlugin plugin) {
        this.plugin = plugin;
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
        plugin.getLogger().info("File structure initialized with compression " +
                (USE_COMPRESSION ? "enabled" : "disabled"));
    }

    public void saveBackupToFile(String areaName, AreaBackup backup) {
        try {
            String extension = USE_COMPRESSION ? COMPRESSED_EXTENSION : UNCOMPRESSED_EXTENSION;
            File backupFile = new File(backupFolder, areaName + "_" + backup.getId() + extension);
            backupFile.getParentFile().mkdirs();

            YamlConfiguration config = new YamlConfiguration();

            config.set("backup.id", backup.getId());
            config.set("backup.timestamp", backup.getTimestamp().toString());
            config.set("backup.compressed", USE_COMPRESSION);

            // Optimize block data storage
            Map<String, Object> optimizedBlockData = optimizeBlockData(backup.getBlocks());
            config.set("backup.blocks", optimizedBlockData);

            if (backup.getEntities() != null && !backup.getEntities().isEmpty()) {
                config.set("backup.entities", backup.getEntities());
            }

            // Save icon as ItemStack - special handling for player heads
            ItemStack iconItem = backup.getIconItem();
            if (iconItem != null) {
                if (iconItem.getType() == org.bukkit.Material.PLAYER_HEAD) {
                    String base64Data = saveItemStackAsBase64(iconItem);
                    if (base64Data != null) {
                        config.set("backup.iconItem-base64", base64Data);
                    } else {
                        config.set("backup.iconItem", iconItem.serialize());
                    }
                } else {
                    config.set("backup.iconItem", iconItem.serialize());
                }
            }

            // Save with or without compression
            if (USE_COMPRESSION) {
                saveCompressedYaml(config, backupFile);
            } else {
                config.save(backupFile);
            }

            long fileSize = backupFile.length();
            plugin.getLogger().info(String.format("Successfully saved backup file: %s (Size: %s)",
                    backupFile.getName(), formatFileSize(fileSize)));

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save backup to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public AreaBackup loadBackupFromFile(String areaName, String backupId) {
        // Try to find the backup file (compressed or uncompressed)
        File backupFile = findBackupFile(areaName, backupId);
        if (backupFile == null) {
            plugin.getLogger().warning("Backup file not found for: " + areaName + "_" + backupId);
            return null;
        }

        try {
            YamlConfiguration config;
            boolean isCompressed = backupFile.getName().endsWith(COMPRESSED_EXTENSION);

            if (isCompressed) {
                config = loadCompressedYaml(backupFile);
            } else {
                config = YamlConfiguration.loadConfiguration(backupFile);
            }

            String id = config.getString("backup.id", backupId);
            String timestampStr = config.getString("backup.timestamp");
            if (timestampStr == null) {
                plugin.getLogger().warning("No timestamp found in backup file: " + backupFile.getName());
                return null;
            }
            LocalDateTime timestamp = LocalDateTime.parse(timestampStr);

            // Load and restore block data
            Map<String, BlockInfo> blocks = loadOptimizedBlockData(config);

            Map<String, Object> entities = new HashMap<>();
            org.bukkit.configuration.ConfigurationSection entitiesSection = config
                    .getConfigurationSection("backup.entities");
            if (entitiesSection != null) {
                for (String key : entitiesSection.getKeys(false)) {
                    entities.put(key, entitiesSection.get(key));
                }
            }

            AreaBackup backup = new AreaBackup(id, timestamp, blocks, entities);

            // Load icon (same logic as before)
            loadBackupIcon(config, backup, backupId, backupFile);

            plugin.getLogger().fine(String.format("Loaded backup %s (%s, %d blocks)",
                    backupId, isCompressed ? "compressed" : "uncompressed",
                    blocks.size()));

            return backup;
        } catch (Exception e) {
            plugin.getLogger()
                    .severe("Failed to load backup from file " + backupFile.getName() + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Optimizes block data by grouping similar blocks and reducing redundancy
     */
    private Map<String, Object> optimizeBlockData(Map<String, BlockInfo> blocks) {
        Map<String, Object> optimized = new HashMap<>();
        Map<String, Object> blockPalette = new HashMap<>();
        Map<String, String> positionMappings = new HashMap<>();
        Map<String, String> signatureToPaletteKey = new HashMap<>(); // Efficient lookup

        int paletteIndex = 0;

        for (Map.Entry<String, BlockInfo> entry : blocks.entrySet()) {
            try {
                Map<String, Object> blockData = entry.getValue().serialize();
                String blockSignature = createBlockSignature(blockData);

                String paletteKey;
                if (signatureToPaletteKey.containsKey(blockSignature)) {
                    // Reuse existing palette entry
                    paletteKey = signatureToPaletteKey.get(blockSignature);
                } else {
                    // Add new block type to palette
                    paletteKey = "b" + paletteIndex++;
                    blockPalette.put(paletteKey, blockData);
                    signatureToPaletteKey.put(blockSignature, paletteKey);
                }

                positionMappings.put(entry.getKey(), paletteKey);

            } catch (Exception blockError) {
                plugin.getLogger().warning("Failed to optimize block at " + entry.getKey() +
                        ": " + blockError.getMessage() + " - Using fallback storage");
                optimized.put(entry.getKey(), entry.getValue().serialize());
            }
        }

        // Only use palette optimization if it actually saves space
        if (blockPalette.size() < blocks.size() * 0.8) {
            optimized.put("palette", blockPalette);
            optimized.put("positions", positionMappings);
            plugin.getLogger().fine(String.format("Block palette optimization: %d unique types from %d blocks",
                    blockPalette.size(), blocks.size()));
        } else {
            // Fallback to original method if optimization doesn't help much
            for (Map.Entry<String, BlockInfo> entry : blocks.entrySet()) {
                try {
                    optimized.put(entry.getKey(), entry.getValue().serialize());
                } catch (Exception blockError) {
                    plugin.getLogger().warning("Failed to serialize block at " + entry.getKey() +
                            ": " + blockError.getMessage() + " - Skipping this block");
                }
            }
        }

        return optimized;
    }

    /**
     * Loads optimized block data back into the standard format
     */
    private Map<String, BlockInfo> loadOptimizedBlockData(YamlConfiguration config) {
        Map<String, BlockInfo> blocks = new HashMap<>();
        org.bukkit.configuration.ConfigurationSection blocksSection = config
                .getConfigurationSection("backup.blocks");

        if (blocksSection == null) {
            plugin.getLogger().warning("No blocks data found in backup file");
            return blocks;
        }

        // Check if this uses palette optimization
        if (blocksSection.contains("palette") && blocksSection.contains("positions")) {
            // Load palette-optimized data
            org.bukkit.configuration.ConfigurationSection paletteSection = blocksSection
                    .getConfigurationSection("palette");
            org.bukkit.configuration.ConfigurationSection positionsSection = blocksSection
                    .getConfigurationSection("positions");

            if (paletteSection != null && positionsSection != null) {
                // Build palette lookup
                Map<String, Map<String, Object>> palette = new HashMap<>();
                for (String paletteKey : paletteSection.getKeys(false)) {
                    org.bukkit.configuration.ConfigurationSection blockSection = paletteSection
                            .getConfigurationSection(paletteKey);
                    if (blockSection != null) {
                        palette.put(paletteKey, blockSection.getValues(false));
                    }
                }

                // Apply palette to positions
                for (String position : positionsSection.getKeys(false)) {
                    String paletteKey = positionsSection.getString(position);
                    if (paletteKey != null && palette.containsKey(paletteKey)) {
                        try {
                            BlockInfo info = BlockInfo.deserialize(palette.get(paletteKey));
                            if (info != null) {
                                blocks.put(position, info);
                            }
                        } catch (Exception ex) {
                            plugin.getLogger().warning("Error loading palette block at " + position +
                                    ": " + ex.getMessage());
                        }
                    }
                }

                plugin.getLogger().fine("Loaded " + blocks.size() + " blocks from palette optimization");
            }
        } else {
            // Load standard block data
            for (String key : blocksSection.getKeys(false)) {
                if ("palette".equals(key) || "positions".equals(key))
                    continue;

                try {
                    org.bukkit.configuration.ConfigurationSection section = blocksSection
                            .getConfigurationSection(key);
                    if (section != null) {
                        Map<String, Object> data = section.getValues(false);
                        BlockInfo info = BlockInfo.deserialize(data);
                        if (info != null) {
                            blocks.put(key, info);
                        } else {
                            plugin.getLogger().warning("Failed to deserialize block at " + key);
                        }
                    }
                } catch (Exception ex) {
                    plugin.getLogger().warning("Error loading block at " + key + ": " + ex.getMessage());
                }
            }
        }

        return blocks;
    }

    /**
     * Creates a signature string for a block to identify duplicates
     */
    private String createBlockSignature(Map<String, Object> blockData) {
        StringBuilder signature = new StringBuilder();

        // Key components that make blocks unique
        signature.append(blockData.get("material"));
        if (blockData.containsKey("blockDataString")) {
            signature.append(":").append(blockData.get("blockDataString"));
        }

        // Include special data to make blocks more unique
        if (blockData.containsKey("bannerPatterns")) {
            signature.append(":banner:").append(blockData.get("bannerPatterns").toString().hashCode());
        }
        if (blockData.containsKey("signLines")) {
            signature.append(":sign:").append(blockData.get("signLines").toString().hashCode());
        }
        if (blockData.containsKey("containerContentsBase64")) {
            signature.append(":container:").append(blockData.get("containerContentsBase64").toString().hashCode());
        }
        if (blockData.containsKey("jukeboxRecord")) {
            signature.append(":jukebox:").append(blockData.get("jukeboxRecord").toString().hashCode());
        }
        if (blockData.containsKey("skullOwner")) {
            signature.append(":skull:").append(blockData.get("skullOwner"));
        }
        if (blockData.containsKey("skullData")) {
            signature.append(":skullData:").append(blockData.get("skullData").toString().hashCode());
        }
        if (blockData.containsKey("flowerPotItem")) {
            signature.append(":pot:").append(blockData.get("flowerPotItem"));
        }

        return signature.toString();
    }

    /**
     * Saves YAML configuration with GZIP compression
     */
    private void saveCompressedYaml(YamlConfiguration config, File file) throws IOException {
        String yamlContent = config.saveToString();

        try (FileOutputStream fos = new FileOutputStream(file);
                GZIPOutputStream gzos = new GZIPOutputStream(fos);
                OutputStreamWriter writer = new OutputStreamWriter(gzos, "UTF-8")) {

            writer.write(yamlContent);
        }
    }

    /**
     * Loads YAML configuration from GZIP compressed file
     */
    private YamlConfiguration loadCompressedYaml(File file) throws IOException {
        StringBuilder yamlContent = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(file);
                GZIPInputStream gzis = new GZIPInputStream(fis);
                InputStreamReader reader = new InputStreamReader(gzis, "UTF-8");
                BufferedReader br = new BufferedReader(reader)) {

            String line;
            while ((line = br.readLine()) != null) {
                yamlContent.append(line).append('\n');
            }
        }

        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(yamlContent.toString());
        } catch (org.bukkit.configuration.InvalidConfigurationException e) {
            throw new IOException("Invalid YAML configuration in compressed file: " + file.getName(), e);
        }
        return config;
    }

    /**
     * Finds backup file, trying both compressed and uncompressed versions
     */
    private File findBackupFile(String areaName, String backupId) {
        // Try compressed first
        File compressedFile = new File(backupFolder, areaName + "_" + backupId + COMPRESSED_EXTENSION);
        if (compressedFile.exists()) {
            return compressedFile;
        }

        // Try uncompressed
        File uncompressedFile = new File(backupFolder, areaName + "_" + backupId + UNCOMPRESSED_EXTENSION);
        if (uncompressedFile.exists()) {
            return uncompressedFile;
        }

        return null;
    }

    /**
     * Loads backup icon with all the existing logic
     */
    private void loadBackupIcon(YamlConfiguration config, AreaBackup backup, String backupId, File backupFile) {
        if (config.contains("backup.iconItem-base64")) {
            // Base64 format (for complex player heads)
            String base64Data = config.getString("backup.iconItem-base64");
            if (base64Data != null) {
                try {
                    ItemStack iconItem = loadItemStackFromBase64(base64Data);
                    if (iconItem != null) {
                        backup.setIconItem(iconItem);
                        plugin.getLogger()
                                .fine("Loaded Base64 icon for backup " + backupId + ": " + iconItem.getType());
                    } else {
                        backup.setIcon(org.bukkit.Material.CHEST);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error loading Base64 backup icon: " + e.getMessage());
                    backup.setIcon(org.bukkit.Material.CHEST);
                }
            }
        } else if (config.contains("backup.iconItem")) {
            // New ItemStack format
            org.bukkit.configuration.ConfigurationSection iconSection = config
                    .getConfigurationSection("backup.iconItem");
            if (iconSection != null) {
                try {
                    Map<String, Object> iconData = new HashMap<>();
                    for (String iconKey : iconSection.getKeys(true)) {
                        iconData.put(iconKey, iconSection.get(iconKey));
                    }
                    ItemStack iconItem = ItemStack.deserialize(iconData);
                    backup.setIconItem(iconItem);
                    plugin.getLogger()
                            .fine("Loaded custom icon for backup " + backupId + ": " + iconItem.getType());
                } catch (Exception e) {
                    plugin.getLogger().warning("Invalid backup icon item data in file "
                            + backupFile.getName() + ", using default: " + e.getMessage());
                    backup.setIcon(org.bukkit.Material.CHEST);
                }
            }
        } else if (config.contains("backup.icon")) {
            // Legacy Material format
            String iconName = config.getString("backup.icon");
            if (iconName != null) {
                try {
                    backup.setIcon(org.bukkit.Material.valueOf(iconName));
                    plugin.getLogger().fine("Loaded legacy icon for backup " + backupId + ": " + iconName);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid backup icon material '" + iconName + "' in file "
                            + backupFile.getName() + ", using default");
                    backup.setIcon(org.bukkit.Material.CHEST);
                }
            }
        }
    }

    public void deleteBackupFile(String areaName, String backupId) {
        File backupFile = findBackupFile(areaName, backupId);
        if (backupFile != null && backupFile.exists()) {
            if (backupFile.delete()) {
                plugin.getLogger().fine("Deleted backup file: " + backupFile.getName());
            } else {
                plugin.getLogger().warning("Failed to delete backup file: " + backupFile.getName());
            }
        }
    }

    public void deleteBackupFiles(String areaName) {
        File[] compressedFiles = backupFolder
                .listFiles((dir, name) -> name.startsWith(areaName + "_") && name.endsWith(COMPRESSED_EXTENSION));
        File[] uncompressedFiles = backupFolder
                .listFiles((dir, name) -> name.startsWith(areaName + "_") && name.endsWith(UNCOMPRESSED_EXTENSION));

        int deletedCount = 0;

        if (compressedFiles != null) {
            for (File file : compressedFiles) {
                if (file.delete()) {
                    deletedCount++;
                }
            }
        }

        if (uncompressedFiles != null) {
            for (File file : uncompressedFiles) {
                if (file.delete()) {
                    deletedCount++;
                }
            }
        }

        plugin.getLogger().info("Deleted " + deletedCount + " backup files for area: " + areaName);
    }

    public void renameBackupFiles(String oldName, String newName) {
        File[] compressedFiles = backupFolder
                .listFiles((dir, name) -> name.startsWith(oldName + "_") && name.endsWith(COMPRESSED_EXTENSION));
        File[] uncompressedFiles = backupFolder
                .listFiles((dir, name) -> name.startsWith(oldName + "_") && name.endsWith(UNCOMPRESSED_EXTENSION));

        int renamedCount = 0;

        if (compressedFiles != null) {
            for (File file : compressedFiles) {
                String fileName = file.getName();
                String newFileName = fileName.replace(oldName + "_", newName + "_");
                File newFile = new File(backupFolder, newFileName);
                if (file.renameTo(newFile)) {
                    renamedCount++;
                }
            }
        }

        if (uncompressedFiles != null) {
            for (File file : uncompressedFiles) {
                String fileName = file.getName();
                String newFileName = fileName.replace(oldName + "_", newName + "_");
                File newFile = new File(backupFolder, newFileName);
                if (file.renameTo(newFile)) {
                    renamedCount++;
                }
            }
        }

        plugin.getLogger()
                .info("Renamed " + renamedCount + " backup files from '" + oldName + "' to '" + newName + "'");
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
        File[] compressedFiles = backupFolder
                .listFiles((dir, name) -> name.startsWith(areaName + "_") && name.endsWith(COMPRESSED_EXTENSION));
        File[] uncompressedFiles = backupFolder
                .listFiles((dir, name) -> name.startsWith(areaName + "_") && name.endsWith(UNCOMPRESSED_EXTENSION));

        // Combine both arrays
        if (compressedFiles == null && uncompressedFiles == null) {
            return new File[0];
        } else if (compressedFiles == null) {
            return uncompressedFiles;
        } else if (uncompressedFiles == null) {
            return compressedFiles;
        } else {
            File[] combined = new File[compressedFiles.length + uncompressedFiles.length];
            System.arraycopy(compressedFiles, 0, combined, 0, compressedFiles.length);
            System.arraycopy(uncompressedFiles, 0, combined, compressedFiles.length, uncompressedFiles.length);
            return combined;
        }
    }

    public File[] getAllBackupFiles() {
        return backupFolder
                .listFiles((dir, name) -> name.endsWith(COMPRESSED_EXTENSION) || name.endsWith(UNCOMPRESSED_EXTENSION));
    }

    public String getDiskUsageStats() {
        long backupSize = getTotalBackupFileSize();
        long freeSpace = dataFolder.getFreeSpace();
        long totalSpace = dataFolder.getTotalSpace();

        return String.format("Backup storage: %s | Free space: %s | Total space: %s | Compression: %s",
                formatFileSize(backupSize),
                formatFileSize(freeSpace),
                formatFileSize(totalSpace),
                USE_COMPRESSION ? "enabled" : "disabled");
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        if (bytes < 1024 * 1024)
            return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024)
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public File getBackupFolder() {
        return backupFolder;
    }

    public File getAreasFile() {
        return areasFile;
    }

    public File getExportsFolder() {
        return exportsFolder;
    }

    /**
     * Saves an ItemStack as Base64 encoded bytes to preserve all data
     */
    private String saveItemStackAsBase64(ItemStack item) {
        try {
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            org.bukkit.util.io.BukkitObjectOutputStream dataOutput = new org.bukkit.util.io.BukkitObjectOutputStream(
                    outputStream);

            dataOutput.writeObject(item);
            dataOutput.close();

            return java.util.Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save ItemStack as Base64: " + e.getMessage());
            return null;
        }
    }

    /**
     * Loads an ItemStack from Base64 encoded bytes
     */
    private ItemStack loadItemStackFromBase64(String base64Data) {
        try {
            byte[] data = java.util.Base64.getDecoder().decode(base64Data);
            java.io.ByteArrayInputStream inputStream = new java.io.ByteArrayInputStream(data);
            org.bukkit.util.io.BukkitObjectInputStream dataInput = new org.bukkit.util.io.BukkitObjectInputStream(
                    inputStream);

            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();

            return item;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load ItemStack from Base64: " + e.getMessage());
            return null;
        }
    }
}

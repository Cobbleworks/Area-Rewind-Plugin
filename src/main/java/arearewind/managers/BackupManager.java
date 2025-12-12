package arearewind.managers;

import arearewind.data.AreaBackup;
import arearewind.data.BlockInfo;
import arearewind.data.ProtectedArea;
import arearewind.listeners.PlayerInteractionListener;
import arearewind.managers.backup.BlockRestoreHandler;
import arearewind.managers.backup.BlockStateHandler;
import arearewind.managers.backup.EntityBackupHandler;
import arearewind.util.ConfigurationManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages backup creation, restoration, undo/redo operations, and backup history.
 * Delegates specialized operations to handler classes:
 * - BlockStateHandler: Captures block state data
 * - BlockRestoreHandler: Restores block states and container contents
 * - EntityBackupHandler: Handles entity backup and restoration
 */
public class BackupManager {

    private final JavaPlugin plugin;
    private final ConfigurationManager configManager;
    private final FileManager fileManager;
    
    // Specialized handlers
    private final BlockStateHandler blockStateHandler;
    private final BlockRestoreHandler blockRestoreHandler;
    private final EntityBackupHandler entityBackupHandler;
    
    private PlayerInteractionListener playerListener;
    final Map<String, List<AreaBackup>> backupHistory;
    private final Map<String, Integer> undoPointers;
    private final Map<String, AreaBackup> beforeRestoreBackups;

    public BackupManager(JavaPlugin plugin, ConfigurationManager configManager, FileManager fileManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.fileManager = fileManager;
        this.backupHistory = new ConcurrentHashMap<>();
        this.undoPointers = new ConcurrentHashMap<>();
        this.beforeRestoreBackups = new ConcurrentHashMap<>();
        
        // Initialize specialized handlers
        this.blockStateHandler = new BlockStateHandler(plugin);
        this.blockRestoreHandler = new BlockRestoreHandler(plugin, blockStateHandler);
        this.entityBackupHandler = new EntityBackupHandler(plugin);
    }

    public void setPlayerInteractionListener(PlayerInteractionListener playerListener) {
        this.playerListener = playerListener;
    }

    private boolean isProgressLoggingEnabledForPlayer(Player player) {
        if (playerListener != null) {
            return playerListener.getPlayerProgressLoggingMode(player);
        }
        return true;
    }

    // ==================== BACKUP CREATION ====================

    public AreaBackup createBackupFromArea(ProtectedArea area) {
        Map<String, BlockInfo> blocks = new HashMap<>();
        Location min = area.getMin();
        Location max = area.getMax();
        World world = min.getWorld();

        int totalBlocks = area.getSize();
        int processedBlocks = 0;
        int errorBlocks = 0;

        plugin.getLogger().info("Creating backup for area '" + area.getName() + "' with " + totalBlocks + " blocks");

        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                    try {
                        Block block = world.getBlockAt(x, y, z);
                        String key = x + "," + y + "," + z;
                        BlockInfo blockInfo = blockStateHandler.createBlockInfo(block);
                        blocks.put(key, blockInfo);
                        processedBlocks++;
                    } catch (Exception e) {
                        errorBlocks++;
                        plugin.getLogger().warning("Failed to backup block at " + x + "," + y + "," + z + ": " + e.getMessage());
                        String key = x + "," + y + "," + z;
                        blocks.put(key, new BlockInfo(Material.AIR, Material.AIR.createBlockData()));
                    }
                }
            }
        }

        plugin.getLogger().info("Backup completed: " + processedBlocks + " blocks processed, " + errorBlocks + " errors");

        Map<String, Object> entities = entityBackupHandler.backupEntitiesInArea(area);
        plugin.getLogger().info("Backup completed: " + entities.size() + " entities captured");

        return new AreaBackup(LocalDateTime.now(), blocks, entities);
    }

    private AreaBackup createHiddenBackup(ProtectedArea area) {
        AreaBackup backup = createBackupFromArea(area);
        backup.setHidden(true);
        return backup;
    }

    public void createBackup(String areaName, ProtectedArea area) {
        try {
            if (!Bukkit.isPrimaryThread()) {
                Bukkit.getScheduler().runTask(plugin, () -> createBackup(areaName, area));
                return;
            }

            plugin.getLogger().info("Starting backup creation for area: " + areaName);
            AreaBackup backup = createBackupFromArea(area);

            if (!backupHistory.containsKey(areaName)) {
                backupHistory.put(areaName, new ArrayList<>());
            }

            List<AreaBackup> backups = backupHistory.get(areaName);
            backups.add(backup);
            undoPointers.put(areaName, backups.size() - 1);

            int maxBackups = configManager.getMaxBackupsPerArea();
            if (backups.size() > maxBackups) {
                AreaBackup removed = backups.remove(0);
                fileManager.deleteBackupFile(areaName, removed.getId());
                plugin.getLogger().info("Removed oldest backup for area: " + areaName);

                Integer currentPointer = undoPointers.get(areaName);
                if (currentPointer != null && currentPointer > 0) {
                    undoPointers.put(areaName, currentPointer - 1);
                }
            }

            try {
                fileManager.saveBackupToFile(areaName, backup);
                plugin.getLogger().info("Successfully saved backup for area: " + areaName +
                        " (ID: " + backup.getId() + ", Blocks: " + backup.getBlocks().size() + ")");
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to save backup file for area: " + areaName + " - " + e.getMessage());
                e.printStackTrace();
            }

        } catch (Exception e) {
            plugin.getLogger().severe("Critical error during backup creation for area: " + areaName + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== RESTORATION ====================

    public void restoreFromBackup(ProtectedArea area, AreaBackup backup) {
        restoreFromBackup(area, backup, null);
    }

    public void restoreFromBackup(ProtectedArea area, AreaBackup backup, Player player) {
        restoreFromBackup(area, backup, player, "✓ Restoration complete!");
    }

    public void restoreFromBackup(ProtectedArea area, AreaBackup backup, Player player, String completionMessage) {
        restoreFromBackupOptimized(area, backup, player, completionMessage);
    }

    private void restoreFromBackupOptimized(ProtectedArea area, AreaBackup backup, Player player, String completionMessage) {
        Location min = area.getMin();
        Location max = area.getMax();
        World world = min.getWorld();
        Map<String, BlockInfo> blocks = backup.getBlocks();

        final int total = blocks.size();
        final int batchSize = area.hasCustomRestoreSpeed() ? area.getCustomRestoreSpeed() : calculateOptimalBatchSize(total);

        if (player != null) {
            if (isProgressLoggingEnabledForPlayer(player)) {
                player.sendMessage(ChatColor.YELLOW + "Starting optimized restoration of " + total + " blocks...");
                player.sendMessage(ChatColor.GRAY + "Using batch size: " + batchSize + " blocks/tick" +
                        (area.hasCustomRestoreSpeed() ? " (custom speed)" : " (dynamic)"));
            } else {
                player.sendMessage(ChatColor.YELLOW + "Starting restoration of " + total + " blocks...");
            }
        }

        Set<Chunk> chunksToLoad = preloadChunks(world, min, max);
        plugin.getLogger().info("Pre-loaded " + chunksToLoad.size() + " chunks for restoration");

        // Categorize blocks for phased processing
        List<String> regularBlocks = new ArrayList<>();
        List<String> specialBlocks = new ArrayList<>();
        List<String> containerBlocks = new ArrayList<>();

        for (Map.Entry<String, BlockInfo> entry : blocks.entrySet()) {
            String key = entry.getKey();
            BlockInfo info = entry.getValue();

            if (info.hasContainerContents()) {
                containerBlocks.add(key);
            } else if (blockStateHandler.hasSpecialProperties(info)) {
                specialBlocks.add(key);
            } else {
                regularBlocks.add(key);
            }
        }

        // Start restoration task
        startRestorationTask(area, backup, player, completionMessage, world, blocks,
                regularBlocks, specialBlocks, containerBlocks, batchSize);
    }

    private void startRestorationTask(ProtectedArea area, AreaBackup backup, Player player,
            String completionMessage, World world, Map<String, BlockInfo> blocks,
            List<String> regularBlocks, List<String> specialBlocks, List<String> containerBlocks,
            int batchSize) {

        new BukkitRunnable() {
            int regularIndex = 0;
            int specialIndex = 0;
            int containerIndex = 0;
            int phase = 1;
            int containerCount = 0;
            long startTime = System.currentTimeMillis();
            long lastProgressTime = startTime;

            @Override
            public void run() {
                try {
                    if (phase == 1) {
                        int processed = processRegularBlocks(regularBlocks, blocks, world, regularIndex, batchSize);
                        regularIndex += processed;

                        if (regularIndex >= regularBlocks.size()) {
                            phase = 2;
                            if (player != null && isProgressLoggingEnabledForPlayer(player)) {
                                if (!specialBlocks.isEmpty()) {
                                    player.sendMessage(ChatColor.YELLOW + "Regular blocks complete! Processing special blocks...");
                                } else if (!containerBlocks.isEmpty()) {
                                    player.sendMessage(ChatColor.YELLOW + "Regular blocks complete! Loading container contents...");
                                }
                            }
                        }
                    } else if (phase == 2) {
                        int specialBatchSize = Math.max(10, batchSize / 4);
                        int processed = processSpecialBlocks(specialBlocks, blocks, world, specialIndex, specialBatchSize);
                        specialIndex += processed;

                        if (specialIndex >= specialBlocks.size()) {
                            phase = 3;
                            if (player != null && isProgressLoggingEnabledForPlayer(player) && !containerBlocks.isEmpty()) {
                                player.sendMessage(ChatColor.YELLOW + "Special blocks complete! Loading container contents...");
                            }
                        }
                    } else if (phase == 3) {
                        int containerBatchSize = Math.max(5, batchSize / 8);
                        int processed = processContainerBlocks(containerBlocks, blocks, world, containerIndex, containerBatchSize);
                        containerIndex += processed;
                        containerCount += processed;

                        if (containerIndex >= containerBlocks.size()) {
                            phase = 4;
                        }
                    } else if (phase == 4) {
                        // Restore entities
                        entityBackupHandler.restoreEntitiesInArea(area, backup.getEntities());

                        long duration = System.currentTimeMillis() - startTime;
                        plugin.getLogger().info("Restoration completed in " + duration + "ms" +
                                " (Regular: " + regularBlocks.size() + ", Special: " + specialBlocks.size() +
                                ", Containers: " + containerBlocks.size() + ")");

                        if (player != null) {
                            player.sendMessage(ChatColor.GREEN + completionMessage);
                            if (isProgressLoggingEnabledForPlayer(player)) {
                                player.sendMessage(ChatColor.GRAY + "Time: " + (duration / 1000.0) + "s | " +
                                        "Containers restored: " + containerCount);
                            }
                        }
                        cancel();
                        return;
                    }

                    // Progress updates
                    long currentTime = System.currentTimeMillis();
                    if (player != null && isProgressLoggingEnabledForPlayer(player) && currentTime - lastProgressTime >= 2000) {
                        int totalProcessed = regularIndex + specialIndex + containerIndex;
                        int totalBlocks = regularBlocks.size() + specialBlocks.size() + containerBlocks.size();
                        int percent = (int) ((totalProcessed * 100.0) / totalBlocks);
                        player.sendMessage(ChatColor.GRAY + "Progress: " + percent + "% (" + totalProcessed + "/" + totalBlocks + ")");
                        lastProgressTime = currentTime;
                    }

                } catch (Exception e) {
                    plugin.getLogger().severe("Error during restoration: " + e.getMessage());
                    e.printStackTrace();
                    if (player != null) {
                        player.sendMessage(ChatColor.RED + "Error during restoration: " + e.getMessage());
                    }
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private int processRegularBlocks(List<String> blockKeys, Map<String, BlockInfo> blocks,
            World world, int startIndex, int batchSize) {
        int processed = 0;
        int index = startIndex;

        while (index < blockKeys.size() && processed < batchSize) {
            String key = blockKeys.get(index);
            BlockInfo info = blocks.get(key);
            String[] parts = key.split(",");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);

            try {
                Block block = world.getBlockAt(x, y, z);
                block.setType(info.getMaterial(), false);
                block.setBlockData(info.getBlockData(), false);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to restore regular block at " + x + "," + y + "," + z + ": " + e.getMessage());
            }

            index++;
            processed++;
        }

        return processed;
    }

    private int processSpecialBlocks(List<String> blockKeys, Map<String, BlockInfo> blocks,
            World world, int startIndex, int batchSize) {
        int processed = 0;
        int index = startIndex;

        while (index < blockKeys.size() && processed < batchSize) {
            String key = blockKeys.get(index);
            BlockInfo info = blocks.get(key);
            String[] parts = key.split(",");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);

            try {
                Block block = world.getBlockAt(x, y, z);
                block.setType(info.getMaterial(), false);
                block.setBlockData(info.getBlockData(), false);
                blockRestoreHandler.restoreNonContainerSpecialData(block, info);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to restore special block at " + x + "," + y + "," + z + ": " + e.getMessage());
            }

            index++;
            processed++;
        }

        return processed;
    }

    private int processContainerBlocks(List<String> blockKeys, Map<String, BlockInfo> blocks,
            World world, int startIndex, int batchSize) {
        int processed = 0;
        int index = startIndex;

        while (index < blockKeys.size() && processed < batchSize) {
            String key = blockKeys.get(index);
            BlockInfo info = blocks.get(key);
            String[] parts = key.split(",");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);

            try {
                Block block = world.getBlockAt(x, y, z);
                block.setType(info.getMaterial(), false);
                block.setBlockData(info.getBlockData(), false);
                blockRestoreHandler.restoreContainerContents(block, info);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to restore container block at " + x + "," + y + "," + z + ": " + e.getMessage());
            }

            index++;
            processed++;
        }

        return processed;
    }

    private Set<Chunk> preloadChunks(World world, Location min, Location max) {
        Set<Chunk> chunks = new HashSet<>();

        int minChunkX = min.getBlockX() >> 4;
        int maxChunkX = max.getBlockX() >> 4;
        int minChunkZ = min.getBlockZ() >> 4;
        int maxChunkZ = max.getBlockZ() >> 4;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                Chunk chunk = world.getChunkAt(chunkX, chunkZ);
                if (!chunk.isLoaded()) {
                    chunk.load();
                }
                chunks.add(chunk);
            }
        }

        return chunks;
    }

    private int calculateOptimalBatchSize(int totalBlocks) {
        final int minBatch = 100;
        final int maxBatch = 1000;

        if (totalBlocks < 1000) return minBatch;
        if (totalBlocks < 5000) return minBatch + 50;
        if (totalBlocks < 10000) return minBatch + 100;
        if (totalBlocks < 50000) return Math.min(maxBatch - 50, minBatch + 150);
        return maxBatch;
    }

    // ==================== UNDO/REDO ====================

    public boolean restoreArea(String areaName, ProtectedArea area, int backupIndex) {
        return restoreArea(areaName, area, backupIndex, true);
    }

    public boolean restoreArea(String areaName, ProtectedArea area, int backupIndex, boolean createBackupFirst) {
        return restoreArea(areaName, area, backupIndex, createBackupFirst, null);
    }

    public boolean restoreArea(String areaName, ProtectedArea area, int backupIndex, boolean createBackupFirst, Player player) {
        AreaBackup backup = getBackup(areaName, backupIndex);
        if (backup == null) return false;

        AreaBackup beforeRestore = createHiddenBackup(area);
        beforeRestoreBackups.put(areaName, beforeRestore);

        if (createBackupFirst) {
            createBackup(areaName, area);
        }

        restoreFromBackup(area, backup, player);
        undoPointers.put(areaName, backupIndex);
        plugin.getLogger().info("Set current state pointer to restored backup #" + backupIndex + " for area: " + areaName);

        return true;
    }

    public boolean undoArea(String areaName, ProtectedArea area) {
        return undoArea(areaName, area, null);
    }

    public boolean undoArea(String areaName, ProtectedArea area, Player player) {
        AreaBackup beforeRestoreBackup = beforeRestoreBackups.get(areaName);
        if (beforeRestoreBackup == null) {
            return false;
        }

        restoreFromBackup(area, beforeRestoreBackup, player, "✓ Undo successful! Restored to state before last backup restore.");
        beforeRestoreBackups.remove(areaName);
        plugin.getLogger().info("Undo completed for area: " + areaName);

        return true;
    }

    public boolean redoArea(String areaName, ProtectedArea area) {
        return redoArea(areaName, area, null);
    }

    public boolean redoArea(String areaName, ProtectedArea area, Player player) {
        return false; // No redo functionality
    }

    public boolean canUndo(String areaName) {
        return beforeRestoreBackups.containsKey(areaName);
    }

    public boolean canRedo(String areaName) {
        return false;
    }

    // ==================== BACKUP HISTORY MANAGEMENT ====================

    public List<AreaBackup> getBackupHistory(String areaName) {
        List<AreaBackup> allBackups = backupHistory.getOrDefault(areaName, new ArrayList<>());
        return allBackups.stream()
                .filter(backup -> !backup.isHidden())
                .collect(java.util.stream.Collectors.toList());
    }

    public List<AreaBackup> getAllBackups(String areaName) {
        return backupHistory.getOrDefault(areaName, new ArrayList<>());
    }

    public AreaBackup getBackup(String areaName, int index) {
        List<AreaBackup> visibleBackups = getBackupHistory(areaName);
        if (visibleBackups == null || index < 0 || index >= visibleBackups.size()) {
            return null;
        }
        return visibleBackups.get(index);
    }

    public int getUndoPointer(String areaName) {
        List<AreaBackup> backups = backupHistory.get(areaName);
        if (backups == null) return -1;
        return undoPointers.getOrDefault(areaName, backups.size() - 1);
    }

    public AreaBackup findClosestBackup(String areaName, LocalDateTime targetTime) {
        List<AreaBackup> backups = backupHistory.get(areaName);
        if (backups == null || backups.isEmpty()) return null;

        AreaBackup closestBackup = null;
        long closestDiff = Long.MAX_VALUE;

        for (AreaBackup backup : backups) {
            long diff = Math.abs(java.time.Duration.between(backup.getTimestamp(), targetTime).toMinutes());
            if (diff < closestDiff) {
                closestDiff = diff;
                closestBackup = backup;
            }
        }

        return closestBackup;
    }

    // ==================== BACKUP CLEANUP ====================

    public int cleanupBackups(String areaName, int daysOld) {
        if (!backupHistory.containsKey(areaName)) return 0;

        List<AreaBackup> backups = backupHistory.get(areaName);
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysOld);

        int removedCount = 0;
        List<AreaBackup> toRemove = new ArrayList<>();
        for (AreaBackup backup : backups) {
            if (backup.getTimestamp().isBefore(cutoffTime)) {
                toRemove.add(backup);
            }
        }

        if (toRemove.size() >= backups.size()) {
            toRemove.sort((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()));
            for (int i = 0; i < toRemove.size() - 1; i++) {
                AreaBackup backup = toRemove.get(i);
                fileManager.deleteBackupFile(areaName, backup.getId());
                backups.remove(backup);
                removedCount++;
            }
        } else {
            for (AreaBackup backup : toRemove) {
                fileManager.deleteBackupFile(areaName, backup.getId());
                backups.remove(backup);
                removedCount++;
            }
        }

        return removedCount;
    }

    public boolean deleteBackup(String areaName, int backupIndex) {
        List<AreaBackup> backups = backupHistory.get(areaName);
        if (backups == null || backupIndex < 0 || backupIndex >= backups.size()) {
            return false;
        }

        AreaBackup backupToDelete = backups.remove(backupIndex);
        fileManager.deleteBackupFile(areaName, backupToDelete.getId());

        Integer undoPointer = undoPointers.get(areaName);
        if (undoPointer != null) {
            if (undoPointer == backupIndex) {
                undoPointers.remove(areaName);
                beforeRestoreBackups.remove(areaName);
            } else if (undoPointer > backupIndex) {
                undoPointers.put(areaName, undoPointer - 1);
            }
        }

        plugin.getLogger().info("Deleted backup #" + backupIndex + " for area: " + areaName);
        return true;
    }

    public void deleteAllBackups(String areaName) {
        List<AreaBackup> backups = backupHistory.remove(areaName);
        if (backups != null) {
            for (AreaBackup backup : backups) {
                fileManager.deleteBackupFile(areaName, backup.getId());
            }
        }
        undoPointers.remove(areaName);
        beforeRestoreBackups.remove(areaName);
    }

    public int deleteAllBackupsExceptLast(String areaName) {
        List<AreaBackup> backups = backupHistory.get(areaName);
        if (backups == null || backups.size() <= 1) {
            return 0;
        }

        backups.sort((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()));

        int lastIndex = backups.size() - 1;
        AreaBackup lastBackup = backups.get(lastIndex);

        int removedCount = 0;
        for (int i = 0; i < lastIndex; i++) {
            AreaBackup toRemove = backups.get(i);
            try {
                fileManager.deleteBackupFile(areaName, toRemove.getId());
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to delete backup file for area " + areaName + ": " + e.getMessage());
            }
            removedCount++;
        }

        List<AreaBackup> newList = new ArrayList<>();
        newList.add(lastBackup);
        backupHistory.put(areaName, newList);
        undoPointers.put(areaName, 0);
        beforeRestoreBackups.remove(areaName);

        plugin.getLogger().info("Deleted " + removedCount + " backups for area: " + areaName + " (kept latest)");
        return removedCount;
    }

    public void renameAreaBackups(String oldName, String newName) {
        List<AreaBackup> backups = backupHistory.remove(oldName);
        if (backups != null) {
            backupHistory.put(newName, backups);
            fileManager.renameBackupFiles(oldName, newName);
        }

        Integer undoPointer = undoPointers.remove(oldName);
        if (undoPointer != null) {
            undoPointers.put(newName, undoPointer);
        }

        AreaBackup beforeRestore = beforeRestoreBackups.remove(oldName);
        if (beforeRestore != null) {
            beforeRestoreBackups.put(newName, beforeRestore);
        }
    }

    // ==================== COMPARISON ====================

    public List<String> compareBackups(AreaBackup backup1, AreaBackup backup2) {
        List<String> differences = new ArrayList<>();
        Map<String, BlockInfo> blocks1 = backup1.getBlocks();
        Map<String, BlockInfo> blocks2 = backup2.getBlocks();

        for (String pos : blocks1.keySet()) {
            if (!blocks2.containsKey(pos)) {
                differences.add(ChatColor.RED + "[-] " + pos + " - " + blocks1.get(pos).getMaterial());
            } else if (!blocks1.get(pos).equals(blocks2.get(pos))) {
                differences.add(ChatColor.YELLOW + "[~] " + pos + " - " +
                        blocks1.get(pos).getMaterial() + " → " + blocks2.get(pos).getMaterial());
            }
        }

        for (String pos : blocks2.keySet()) {
            if (!blocks1.containsKey(pos)) {
                differences.add(ChatColor.GREEN + "[+] " + pos + " - " + blocks2.get(pos).getMaterial());
            }
        }

        return differences;
    }

    public List<String> getDifferencesFromLast(String areaName, ProtectedArea area) {
        List<AreaBackup> backups = backupHistory.get(areaName);
        if (backups == null || backups.isEmpty()) {
            return new ArrayList<>();
        }

        AreaBackup currentState = createBackupFromArea(area);
        AreaBackup lastBackup = backups.get(backups.size() - 1);

        return compareBackups(lastBackup, currentState);
    }

    // ==================== PERSISTENCE ====================

    public void loadBackups() {
        plugin.getLogger().info("Loading backup history from files...");

        File backupFolder = fileManager.getBackupFolder();
        if (!backupFolder.exists()) {
            plugin.getLogger().info("No backup folder found");
            return;
        }

        File[] backupFiles = backupFolder.listFiles((dir, name) -> name.endsWith(".yml.gz") || name.endsWith(".yml"));
        if (backupFiles == null || backupFiles.length == 0) {
            plugin.getLogger().info("No backup files found");
            return;
        }

        plugin.getLogger().info("Found " + backupFiles.length + " backup files to process");

        int totalLoaded = 0;
        int totalFailed = 0;
        Map<String, Integer> areaBackupCounts = new HashMap<>();

        for (File file : backupFiles) {
            try {
                String fileName = file.getName();
                if (fileName.endsWith(".yml.gz")) {
                    fileName = fileName.replace(".yml.gz", "");
                } else if (fileName.endsWith(".yml")) {
                    fileName = fileName.replace(".yml", "");
                }

                int lastUnderscoreIndex = fileName.lastIndexOf("_");
                if (lastUnderscoreIndex > 0 && lastUnderscoreIndex < fileName.length() - 1) {
                    String areaName = fileName.substring(0, lastUnderscoreIndex);
                    String backupId = fileName.substring(lastUnderscoreIndex + 1);

                    AreaBackup backup = fileManager.loadBackupFromFile(areaName, backupId);
                    if (backup != null) {
                        if (!backupHistory.containsKey(areaName)) {
                            backupHistory.put(areaName, new ArrayList<>());
                        }

                        List<AreaBackup> areaBackups = backupHistory.get(areaName);
                        boolean exists = areaBackups.stream().anyMatch(b -> b.getId().equals(backup.getId()));

                        if (!exists) {
                            areaBackups.add(backup);
                            totalLoaded++;
                            areaBackupCounts.put(areaName, areaBackupCounts.getOrDefault(areaName, 0) + 1);
                        }
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load backup file " + file.getName() + ": " + e.getMessage());
                totalFailed++;
            }
        }

        for (Map.Entry<String, List<AreaBackup>> entry : backupHistory.entrySet()) {
            String areaName = entry.getKey();
            List<AreaBackup> areaBackups = entry.getValue();
            areaBackups.sort((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()));

            if (!areaBackups.isEmpty()) {
                undoPointers.put(areaName, areaBackups.size() - 1);
            }
        }

        plugin.getLogger().info("Successfully loaded " + totalLoaded + " backups for " +
                backupHistory.size() + " areas (" + totalFailed + " failed)");
    }

    // ==================== UTILITIES ====================

    public long parseTimeString(String timeStr) {
        try {
            char unit = timeStr.charAt(timeStr.length() - 1);
            int amount = Integer.parseInt(timeStr.substring(0, timeStr.length() - 1));

            switch (unit) {
                case 'm': return amount;
                case 'h': return amount * 60L;
                case 'd': return amount * 60L * 24;
                case 'w': return amount * 60L * 24 * 7;
                default: return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    public int getTotalBackupCount() {
        return backupHistory.values().stream().mapToInt(List::size).sum();
    }

    public String getBackupStatistics() {
        int totalAreas = backupHistory.size();
        int totalBackups = getTotalBackupCount();
        long totalFileSize = fileManager.getTotalBackupFileSize();

        return String.format("Areas with backups: %d | Total backups: %d | Storage used: %s",
                totalAreas, totalBackups, formatFileSize(totalFileSize));
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
}

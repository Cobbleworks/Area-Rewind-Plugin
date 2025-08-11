package arearewind.managers;

import arearewind.data.AreaBackup;
import arearewind.data.BlockInfo;
import arearewind.data.ProtectedArea;
import arearewind.util.ConfigurationManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.Banner;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BackupManager {

    private final JavaPlugin plugin;
    private final ConfigurationManager configManager;
    private final FileManager fileManager;
    private final Map<String, List<AreaBackup>> backupHistory;
    private final Map<String, Integer> undoPointers;
    private BukkitTask automaticBackupTask;

    public BackupManager(JavaPlugin plugin, ConfigurationManager configManager, FileManager fileManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.fileManager = fileManager;
        this.backupHistory = new ConcurrentHashMap<>();
        this.undoPointers = new ConcurrentHashMap<>();
    }

    private final Set<Material> POI_BLOCKS = Set.of(
            Material.LECTERN, Material.CARTOGRAPHY_TABLE, Material.FLETCHING_TABLE,
            Material.SMITHING_TABLE, Material.LOOM, Material.STONECUTTER,
            Material.GRINDSTONE, Material.BARREL, Material.SMOKER, Material.BLAST_FURNACE,
            Material.FURNACE, Material.BREWING_STAND, Material.COMPOSTER, Material.BELL
    );

    private final Set<Material> BED_BLOCKS = Set.of(
            Material.WHITE_BED, Material.ORANGE_BED, Material.MAGENTA_BED, Material.LIGHT_BLUE_BED,
            Material.YELLOW_BED, Material.LIME_BED, Material.PINK_BED, Material.GRAY_BED,
            Material.LIGHT_GRAY_BED, Material.CYAN_BED, Material.PURPLE_BED, Material.BLUE_BED,
            Material.BROWN_BED, Material.GREEN_BED, Material.RED_BED, Material.BLACK_BED
    );

    private BlockInfo createBlockInfo(Block block) {
        try {
            if (!Bukkit.isPrimaryThread()) {
                plugin.getLogger().warning("createBlockInfo called from async thread for block at " + block.getLocation());
                return new BlockInfo(block.getType(), block.getBlockData());
            }

            BlockInfo blockInfo = new BlockInfo(block.getType(), block.getBlockData());

            try {
                if (block.getState() instanceof Banner) {
                    Banner banner = (Banner) block.getState();
                    if (banner.getPatterns() != null) {
                        blockInfo.setBannerPatterns(new ArrayList<>(banner.getPatterns()));
                    }
                }
                else if (block.getState() instanceof Sign) {
                    Sign sign = (Sign) block.getState();
                    if (sign.getLines() != null) {
                        blockInfo.setSignLines(sign.getLines());
                    }
                }
                else if (block.getState() instanceof org.bukkit.block.Container) {
                    org.bukkit.block.Container container = (org.bukkit.block.Container) block.getState();
                    if (container.getInventory() != null) {
                        ItemStack[] contents = container.getInventory().getContents();
                        blockInfo.setContainerContents(contents);

                        plugin.getLogger().fine("Backup: Container (" + block.getType() + ") at " +
                                block.getLocation() + " has " + (contents != null ? contents.length : 0) +
                                " slots with items: " + getContainerSummary(contents));
                    }
                }
                else if (block.getState() instanceof org.bukkit.block.Jukebox) {
                    org.bukkit.block.Jukebox jukebox = (org.bukkit.block.Jukebox) block.getState();
                    if (jukebox.getRecord() != null) {
                        blockInfo.setJukeboxRecord(jukebox.getRecord());
                    }
                }
                else if (block.getState() instanceof org.bukkit.block.Skull) {
                    org.bukkit.block.Skull skull = (org.bukkit.block.Skull) block.getState();
                    if (skull.getOwningPlayer() != null) {
                        blockInfo.setSkullOwner(skull.getOwningPlayer().getName());
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to read special block state for " + block.getType() +
                        " at " + block.getLocation() + ": " + e.getMessage() +
                        " (Block data will be preserved but special properties may be lost)");
            }

            return blockInfo;

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to create BlockInfo for block at " + block.getLocation() +
                    ": " + e.getMessage() + " - Using fallback");

            try {
                return new BlockInfo(block.getType(), block.getBlockData());
            } catch (Exception fallbackError) {
                plugin.getLogger().severe("Critical error creating BlockInfo fallback: " + fallbackError.getMessage());
                return new BlockInfo(Material.AIR, Material.AIR.createBlockData());
            }
        }
    }

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

                        BlockInfo blockInfo = createBlockInfo(block);
                        blocks.put(key, blockInfo);
                        processedBlocks++;

                    } catch (Exception e) {
                        errorBlocks++;
                        plugin.getLogger().warning("Failed to backup block at " + x + "," + y + "," + z +
                                ": " + e.getMessage());

                        String key = x + "," + y + "," + z;
                        blocks.put(key, new BlockInfo(Material.AIR, Material.AIR.createBlockData()));
                    }
                }
            }
        }

        plugin.getLogger().info("Backup completed: " + processedBlocks + " blocks processed, " +
                errorBlocks + " errors (replaced with air)");

        return new AreaBackup(LocalDateTime.now(), blocks);
    }

    public void restoreFromBackup(ProtectedArea area, AreaBackup backup) {
        restoreFromBackup(area, backup, null);
    }

    public void restoreFromBackup(ProtectedArea area, AreaBackup backup, Player player) {
        Location min = area.getMin();
        Location max = area.getMax();
        World world = min.getWorld();
        Map<String, BlockInfo> blocks = backup.getBlocks();
        List<String> keys = new ArrayList<>(blocks.keySet());
        final int batchSize = 30;
        final int total = keys.size();

        if (player != null) {
            player.sendMessage(ChatColor.YELLOW + "Starting restoration of " + total + " blocks...");
        }

        final List<String> containerKeys = new ArrayList<>();
        for (String key : keys) {
            BlockInfo info = blocks.get(key);
            if (info.hasContainerContents()) {
                containerKeys.add(key);
            }
        }

        new BukkitRunnable() {
            int index = 0;
            int containerCount = 0;
            int phase = 1;

            @Override
            public void run() {
                if (phase == 1) {
                    int processed = 0;
                    while (index < total && processed < batchSize) {
                        String key = keys.get(index);
                        BlockInfo info = blocks.get(key);
                        String[] parts = key.split(",");
                        int x = Integer.parseInt(parts[0]);
                        int y = Integer.parseInt(parts[1]);
                        int z = Integer.parseInt(parts[2]);

                        Block block = world.getBlockAt(x, y, z);

                        if (!block.getChunk().isLoaded()) {
                            block.getChunk().load();
                        }

                        block.setType(info.getMaterial(), false);
                        block.setBlockData(info.getBlockData(), false);

                        restoreNonContainerSpecialData(block, info);

                        index++;
                        processed++;
                    }

                    if (index >= total) {
                        phase = 2;
                        index = 0;
                        if (player != null && !containerKeys.isEmpty()) {
                            player.sendMessage(ChatColor.YELLOW + "Blocks placed! Loading container contents...");
                        }
                    }
                } else if (phase == 2) {
                    int processed = 0;
                    while (index < containerKeys.size() && processed < 10) {
                        String key = containerKeys.get(index);
                        BlockInfo info = blocks.get(key);
                        String[] parts = key.split(",");
                        int x = Integer.parseInt(parts[0]);
                        int y = Integer.parseInt(parts[1]);
                        int z = Integer.parseInt(parts[2]);

                        Block block = world.getBlockAt(x, y, z);

                        if (restoreContainerContents(block, info)) {
                            containerCount++;
                        }

                        index++;
                        processed++;
                    }

                    if (index >= containerKeys.size()) {
                        if (player != null) {
                            player.sendMessage(ChatColor.GREEN + "✓ Restoration complete!");
                            player.sendMessage(ChatColor.GRAY + "Restored: " + total + " blocks, " +
                                    containerCount + " containers with contents");
                        }
                        plugin.getLogger().info("Restoration completed: " + total + " blocks, " +
                                containerCount + " containers restored");
                        this.cancel();
                    }
                }

                if (player != null && phase == 1 && index % 500 == 0 && index > 0) {
                    int progress = (int) ((double) index / total * 100);
                    player.sendMessage(ChatColor.YELLOW + "Progress: " + progress + "% (" +
                            index + "/" + total + ")");
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void restoreNonContainerSpecialData(Block block, BlockInfo info) {
        try {
            if (block.getState() instanceof Banner && info.getBannerPatterns() != null) {
                Banner banner = (Banner) block.getState();
                banner.setPatterns(info.getBannerPatterns());
                banner.update(true, false);
            }
            else if (block.getState() instanceof Sign && info.getSignLines() != null) {
                Sign sign = (Sign) block.getState();
                String[] lines = info.getSignLines();
                for (int i = 0; i < lines.length && i < 4; i++) {
                    if (lines[i] != null) {
                        sign.setLine(i, ChatColor.translateAlternateColorCodes('&', lines[i]));
                    }
                }
                sign.update(true, false);
            }
            else if (block.getState() instanceof org.bukkit.block.Jukebox && info.getJukeboxRecord() != null) {
                org.bukkit.block.Jukebox jukebox = (org.bukkit.block.Jukebox) block.getState();
                jukebox.setRecord(info.getJukeboxRecord());
                jukebox.update(true, false);
            }
            else if (block.getState() instanceof org.bukkit.block.Skull && info.getSkullOwner() != null) {
                org.bukkit.block.Skull skull = (org.bukkit.block.Skull) block.getState();
                org.bukkit.OfflinePlayer owner = Bukkit.getOfflinePlayer(info.getSkullOwner());
                skull.setOwningPlayer(owner);
                skull.update(true, false);
            }

            if (isPOIBlock(block.getType())) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    try {
                        block.getState().update(true, true);
                        block.getChunk().load();
                    } catch (Exception e) {
                    }
                }, 2L);
            }

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to restore special data for block at " +
                    block.getLocation() + ": " + e.getMessage());
        }
    }

    private boolean isPOIBlock(Material material) {
        return POI_BLOCKS.contains(material) || BED_BLOCKS.contains(material);
    }

    public Map<String, Object> backupEntitiesInArea(ProtectedArea area) {
        Map<String, Object> entities = new HashMap<>();
        Location min = area.getMin();
        Location max = area.getMax();
        World world = min.getWorld();

        try {
            world.getEntities().stream()
                    .filter(entity -> {
                        Location loc = entity.getLocation();
                        return loc.getBlockX() >= min.getBlockX() && loc.getBlockX() <= max.getBlockX() &&
                                loc.getBlockY() >= min.getBlockY() && loc.getBlockY() <= max.getBlockY() &&
                                loc.getBlockZ() >= min.getBlockZ() && loc.getBlockZ() <= max.getBlockZ();
                    })
                    .forEach(entity -> {
                        if (entity instanceof org.bukkit.entity.ItemFrame) {
                            org.bukkit.entity.ItemFrame frame = (org.bukkit.entity.ItemFrame) entity;

                            Map<String, Object> frameData = new HashMap<>();
                            frameData.put("type", "ITEM_FRAME");
                            frameData.put("x", entity.getLocation().getX());
                            frameData.put("y", entity.getLocation().getY());
                            frameData.put("z", entity.getLocation().getZ());
                            frameData.put("facing", frame.getFacing().name());
                            frameData.put("rotation", frame.getRotation().name());

                            if (frame.getItem() != null && frame.getItem().getType() != Material.AIR) {
                                frameData.put("item", frame.getItem());
                            }

                            String key = "frame_" + entity.getLocation().getBlockX() + "_" +
                                    entity.getLocation().getBlockY() + "_" +
                                    entity.getLocation().getBlockZ();
                            entities.put(key, frameData);
                        }
                        else if (entity instanceof org.bukkit.entity.ArmorStand) {
                        }
                    });

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to backup entities: " + e.getMessage());
        }

        return entities;
    }

    public void restoreEntitiesInArea(ProtectedArea area, Map<String, Object> entityData) {
        if (entityData == null || entityData.isEmpty()) return;

        Location min = area.getMin();
        Location max = area.getMax();
        World world = min.getWorld();

        world.getEntities().stream()
                .filter(entity -> {
                    Location loc = entity.getLocation();
                    return loc.getBlockX() >= min.getBlockX() && loc.getBlockX() <= max.getBlockX() &&
                            loc.getBlockY() >= min.getBlockY() && loc.getBlockY() <= max.getBlockY() &&
                            loc.getBlockZ() >= min.getBlockZ() && loc.getBlockZ() <= max.getBlockZ();
                })
                .filter(entity -> entity instanceof org.bukkit.entity.ItemFrame)
                .forEach(org.bukkit.entity.Entity::remove);

        for (Map.Entry<String, Object> entry : entityData.entrySet()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) entry.getValue();

                if ("ITEM_FRAME".equals(data.get("type"))) {
                    double x = (Double) data.get("x");
                    double y = (Double) data.get("y");
                    double z = (Double) data.get("z");

                    Location loc = new Location(world, x, y, z);
                    org.bukkit.block.BlockFace facing = org.bukkit.block.BlockFace.valueOf((String) data.get("facing"));

                    org.bukkit.entity.ItemFrame frame = world.spawn(loc, org.bukkit.entity.ItemFrame.class, itemFrame -> {
                        itemFrame.setFacingDirection(facing);

                        if (data.containsKey("rotation")) {
                            org.bukkit.Rotation rotation = org.bukkit.Rotation.valueOf((String) data.get("rotation"));
                            itemFrame.setRotation(rotation);
                        }

                        if (data.containsKey("item")) {
                            ItemStack item = (ItemStack) data.get("item");
                            itemFrame.setItem(item);
                        }
                    });

                    plugin.getLogger().fine("Restored item frame at " + loc + " with item: " +
                            (frame.getItem() != null ? frame.getItem().getType() : "none"));
                }

            } catch (Exception e) {
                plugin.getLogger().warning("Failed to restore entity " + entry.getKey() + ": " + e.getMessage());
            }
        }
    }

    private boolean restoreContainerContents(Block block, BlockInfo info) {
        if (!info.hasContainerContents()) {
            return false;
        }

        try {
            if (!(block.getState() instanceof org.bukkit.block.Container)) {
                plugin.getLogger().warning("Block at " + block.getLocation() +
                        " is not a container but has container contents! Type: " + block.getType() +
                        ", State: " + block.getState().getClass().getSimpleName());
                return false;
            }

            plugin.getLogger().info("Restoring container at " + block.getLocation() +
                    " - Type: " + block.getType() + ", State: " + block.getState().getClass().getSimpleName());

            org.bukkit.block.Container container = (org.bukkit.block.Container) block.getState();
            ItemStack[] contents = info.getContainerContents();

            if (contents != null) {
                container.getInventory().clear();

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    try {
                        org.bukkit.block.Container freshContainer = (org.bukkit.block.Container) block.getState();

                        ItemStack[] currentContents = freshContainer.getInventory().getContents();
                        plugin.getLogger().info("Container before restore: " + getContainerSummary(currentContents));

                        ItemStack[] safeCopy = new ItemStack[freshContainer.getInventory().getSize()];
                        for (int i = 0; i < safeCopy.length && i < contents.length; i++) {
                            if (contents[i] != null && contents[i].getType() != Material.AIR) {
                                safeCopy[i] = contents[i].clone();
                            }
                        }

                        freshContainer.getInventory().setContents(safeCopy);
                        freshContainer.update(true, true);

                        try {
                            freshContainer.getInventory().clear();
                            for (int i = 0; i < contents.length && i < freshContainer.getInventory().getSize(); i++) {
                                if (contents[i] != null && contents[i].getType() != Material.AIR) {
                                    freshContainer.getInventory().setItem(i, contents[i].clone());
                                }
                            }
                        } catch (Exception e2) {
                            plugin.getLogger().warning("Alternative restore method failed: " + e2.getMessage());
                        }

                        ItemStack[] afterContents = freshContainer.getInventory().getContents();
                        plugin.getLogger().info("Container after restore: " + getContainerSummary(afterContents));

                        block.getState().update(true, true);

                        if (block.getChunk().isLoaded()) {
                            for (int dx = -1; dx <= 1; dx++) {
                                for (int dy = -1; dy <= 1; dy++) {
                                    for (int dz = -1; dz <= 1; dz++) {
                                        Block neighbor = block.getRelative(dx, dy, dz);
                                        if (neighbor.getType() != Material.AIR) {
                                            neighbor.getState().update(false, false);
                                        }
                                    }
                                }
                            }
                        }

                        plugin.getLogger().info("Successfully restored container (" + block.getType() +
                                ") at " + block.getLocation() + " with " + getContainerSummary(contents));

                    } catch (Exception e) {
                        plugin.getLogger().severe("Failed to restore container contents (delayed) at " +
                                block.getLocation() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }, 2L);

                return true;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to restore container contents at " +
                    block.getLocation() + ": " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    private String getContainerSummary(ItemStack[] contents) {
        if (contents == null) return "empty";

        int itemCount = 0;
        Map<Material, Integer> materialCounts = new HashMap<>();

        for (ItemStack item : contents) {
            if (item != null && item.getType() != Material.AIR) {
                itemCount++;
                materialCounts.put(item.getType(),
                        materialCounts.getOrDefault(item.getType(), 0) + item.getAmount());
            }
        }

        if (itemCount == 0) return "empty";

        StringBuilder summary = new StringBuilder();
        summary.append(itemCount).append(" items (");

        int count = 0;
        for (Map.Entry<Material, Integer> entry : materialCounts.entrySet()) {
            if (count > 0) summary.append(", ");
            summary.append(entry.getKey()).append(" x").append(entry.getValue());
            count++;
            if (count >= 3) {
                if (materialCounts.size() > 3) {
                    summary.append(", +").append(materialCounts.size() - 3).append(" more");
                }
                break;
            }
        }
        summary.append(")");

        return summary.toString();
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

            int maxBackups = configManager.getMaxBackupsPerArea();
            if (backups.size() > maxBackups) {
                AreaBackup removed = backups.remove(0);
                fileManager.deleteBackupFile(areaName, removed.getId());
                plugin.getLogger().info("Removed oldest backup for area: " + areaName);
            }

            try {
                fileManager.saveBackupToFile(areaName, backup);
                plugin.getLogger().info("Successfully saved backup for area: " + areaName +
                        " (ID: " + backup.getId() + ", Blocks: " + backup.getBlocks().size() + ")");
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to save backup file for area: " + areaName +
                        " - " + e.getMessage());
                e.printStackTrace();
            }

        } catch (Exception e) {
            plugin.getLogger().severe("Critical error during backup creation for area: " + areaName +
                    " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<AreaBackup> getBackupHistory(String areaName) {
        return backupHistory.getOrDefault(areaName, new ArrayList<>());
    }

    public AreaBackup getBackup(String areaName, int index) {
        List<AreaBackup> backups = backupHistory.get(areaName);
        if (backups == null || index < 0 || index >= backups.size()) {
            return null;
        }
        return backups.get(index);
    }

    public boolean restoreArea(String areaName, ProtectedArea area, int backupIndex) {
        AreaBackup backup = getBackup(areaName, backupIndex);
        if (backup == null) return false;

        createBackup(areaName, area);

        restoreFromBackup(area, backup);

        List<AreaBackup> backups = backupHistory.get(areaName);
        undoPointers.put(areaName, backups.size() - 1);

        return true;
    }

    public boolean undoArea(String areaName, ProtectedArea area) {
        if (!backupHistory.containsKey(areaName) || backupHistory.get(areaName).isEmpty()) {
            return false;
        }

        List<AreaBackup> backups = backupHistory.get(areaName);
        int currentPointer = undoPointers.getOrDefault(areaName, backups.size() - 1);

        if (currentPointer <= 0) {
            return false;
        }

        currentPointer--;
        undoPointers.put(areaName, currentPointer);

        AreaBackup backup = backups.get(currentPointer);
        restoreFromBackup(area, backup);

        return true;
    }

    public boolean redoArea(String areaName, ProtectedArea area) {
        if (!backupHistory.containsKey(areaName) || backupHistory.get(areaName).isEmpty()) {
            return false;
        }

        List<AreaBackup> backups = backupHistory.get(areaName);
        int currentPointer = undoPointers.getOrDefault(areaName, backups.size() - 1);

        if (currentPointer >= backups.size() - 1) {
            return false;
        }

        currentPointer++;
        undoPointers.put(areaName, currentPointer);

        AreaBackup backup = backups.get(currentPointer);
        restoreFromBackup(area, backup);

        return true;
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

    public int cleanupBackups(String areaName, int daysOld) {
        if (!backupHistory.containsKey(areaName)) return 0;

        List<AreaBackup> backups = backupHistory.get(areaName);
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysOld);

        int removedCount = 0;
        Iterator<AreaBackup> iterator = backups.iterator();

        while (iterator.hasNext()) {
            AreaBackup backup = iterator.next();
            if (backup.getTimestamp().isBefore(cutoffTime)) {
                fileManager.deleteBackupFile(areaName, backup.getId());
                iterator.remove();
                removedCount++;
            }
        }

        return removedCount;
    }

    public void deleteAllBackups(String areaName) {
        List<AreaBackup> backups = backupHistory.remove(areaName);
        if (backups != null) {
            for (AreaBackup backup : backups) {
                fileManager.deleteBackupFile(areaName, backup.getId());
            }
        }
        undoPointers.remove(areaName);
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
    }

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

    public void loadBackups() {
        plugin.getLogger().info("Loading backup history from files...");

        File backupFolder = fileManager.getBackupFolder();
        if (!backupFolder.exists()) {
            plugin.getLogger().info("No backup folder found");
            return;
        }

        File[] backupFiles = backupFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (backupFiles == null || backupFiles.length == 0) {
            plugin.getLogger().info("No backup files found");
            return;
        }

        int totalLoaded = 0;
        Map<String, Integer> areaBackupCounts = new HashMap<>();

        for (File file : backupFiles) {
            try {
                String fileName = file.getName().replace(".yml", "");
                String[] parts = fileName.split("_");

                if (parts.length >= 2) {
                    String areaName = parts[0];
                    String backupId = parts[1];

                    AreaBackup backup = fileManager.loadBackupFromFile(areaName, backupId);
                    if (backup != null) {
                        if (!backupHistory.containsKey(areaName)) {
                            backupHistory.put(areaName, new ArrayList<>());
                        }

                        List<AreaBackup> areaBackups = backupHistory.get(areaName);

                        boolean exists = false;
                        for (AreaBackup existing : areaBackups) {
                            if (existing.getId().equals(backup.getId())) {
                                exists = true;
                                break;
                            }
                        }

                        if (!exists) {
                            areaBackups.add(backup);
                            totalLoaded++;
                            areaBackupCounts.put(areaName, areaBackupCounts.getOrDefault(areaName, 0) + 1);
                        }
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load backup file " + file.getName() + ": " + e.getMessage());
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
                backupHistory.size() + " areas");

        for (Map.Entry<String, Integer> entry : areaBackupCounts.entrySet()) {
            plugin.getLogger().info("Area '" + entry.getKey() + "': " + entry.getValue() + " backups loaded");
        }
    }

    public void startAutomaticBackup() {
        stopAutomaticBackup();

        int intervalMinutes = configManager.getBackupInterval();

        automaticBackupTask = new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getLogger().info("Automatic backup task running (placeholder)");
            }
        }.runTaskTimerAsynchronously(plugin,
                20L * 60 * intervalMinutes,
                20L * 60 * intervalMinutes);

        plugin.getLogger().info("Automatic backup started with " + intervalMinutes + " minute interval");
    }

    public void stopAutomaticBackup() {
        if (automaticBackupTask != null) {
            automaticBackupTask.cancel();
            automaticBackupTask = null;
        }
    }

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

    public int getUndoPointer(String areaName) {
        List<AreaBackup> backups = backupHistory.get(areaName);
        if (backups == null) return -1;
        return undoPointers.getOrDefault(areaName, backups.size() - 1);
    }

    public boolean canUndo(String areaName) {
        return getUndoPointer(areaName) > 0;
    }

    public boolean canRedo(String areaName) {
        List<AreaBackup> backups = backupHistory.get(areaName);
        if (backups == null) return false;
        return getUndoPointer(areaName) < backups.size() - 1;
    }
}
package arearewind.managers.backup;

import arearewind.data.BlockInfo;
import org.bukkit.*;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles restoring block states including special block data and container contents.
 */
public class BlockRestoreHandler {

    private final JavaPlugin plugin;
    private final BlockStateHandler blockStateHandler;

    public BlockRestoreHandler(JavaPlugin plugin, BlockStateHandler blockStateHandler) {
        this.plugin = plugin;
        this.blockStateHandler = blockStateHandler;
    }

    /**
     * Restores special block data (signs, banners, spawners, etc.) that isn't handled as container contents.
     */
    public void restoreNonContainerSpecialData(Block block, BlockInfo info) {
        try {
            // Banner
            if (block.getState() instanceof Banner && info.getBannerPatterns() != null) {
                restoreBanner(block, info);
            }
            // Sign
            else if (block.getState() instanceof Sign && info.getSignLines() != null) {
                restoreSign(block, info);
            }
            // Lectern
            else if (block.getState() instanceof org.bukkit.block.Lectern && info.getLecternBook() != null) {
                restoreLectern(block, info);
            }
            // Campfire
            else if (block.getState() instanceof org.bukkit.block.Campfire && info.getCampfireItems() != null) {
                restoreCampfire(block, info);
            }
            // Beehive
            else if (block.getState() instanceof org.bukkit.block.Beehive) {
                restoreBeehive(block, info);
            }
            // Brewing Stand
            else if (block.getState() instanceof org.bukkit.block.BrewingStand) {
                restoreBrewingStand(block, info);
            }
            // Spawner
            else if (block.getState() instanceof org.bukkit.block.CreatureSpawner && info.getSpawnerEntityType() != null) {
                restoreSpawner(block, info);
            }
            // Command Block
            else if (block.getState() instanceof org.bukkit.block.CommandBlock && info.getCommandBlockCommand() != null) {
                restoreCommandBlock(block, info);
            }
            // Decorated Pot
            else if (block.getState() instanceof org.bukkit.block.DecoratedPot && info.getDecoratedPotSherds() != null) {
                restoreDecoratedPot(block, info);
            }
            // Structure Block
            else if (block.getState() instanceof org.bukkit.block.Structure && info.getStructureBlockName() != null) {
                restoreStructureBlock(block, info);
            }
            // Chiseled Bookshelf
            else if (block.getState() instanceof org.bukkit.block.ChiseledBookshelf && info.getChiseledBookshelfBooks() != null) {
                restoreChiseledBookshelf(block, info);
            }
            // End Gateway
            else if (block.getState() instanceof org.bukkit.block.EndGateway && info.getEndGatewayExitLocation() != null) {
                restoreEndGateway(block, info);
            }
            // Comparator
            else if (info.getComparatorMode() != null && block.getBlockData() instanceof org.bukkit.block.data.type.Comparator) {
                restoreComparator(block, info);
            }
            // Jukebox
            else if (block.getState() instanceof org.bukkit.block.Jukebox && info.getJukeboxRecord() != null) {
                restoreJukebox(block, info);
            }
            // Skull
            else if (block.getState() instanceof org.bukkit.block.Skull && info.getSkullOwner() != null) {
                restoreSkull(block, info);
            }

            // Handle POI blocks for villager job sites
            if (blockStateHandler.isPOIBlock(block.getType())) {
                schedulePOIUpdate(block);
            }

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to restore special data for block at " +
                    block.getLocation() + ": " + e.getMessage());
        }
    }

    private void restoreBanner(Block block, BlockInfo info) {
        Banner banner = (Banner) block.getState();
        banner.setPatterns(info.getBannerPatterns());
        banner.update(true, false);
    }

    private void restoreSign(Block block, BlockInfo info) {
        Sign sign = (Sign) block.getState();
        try {
            // Front side
            org.bukkit.block.sign.SignSide front = sign.getSide(org.bukkit.block.sign.Side.FRONT);
            String[] frontLines = info.getSignLines();
            for (int i = 0; i < frontLines.length && i < 4; i++) {
                if (frontLines[i] != null) {
                    front.setLine(i, ChatColor.translateAlternateColorCodes('&', frontLines[i]));
                }
            }
            front.setGlowingText(info.isSignGlowing());
            if (info.getSignColor() != null) {
                try {
                    front.setColor(DyeColor.valueOf(info.getSignColor()));
                } catch (IllegalArgumentException e) {
                    // Ignore invalid color
                }
            }

            // Back side
            String[] backLines = info.getSignBackLines();
            if (backLines != null) {
                org.bukkit.block.sign.SignSide back = sign.getSide(org.bukkit.block.sign.Side.BACK);
                for (int i = 0; i < backLines.length && i < 4; i++) {
                    if (backLines[i] != null) {
                        back.setLine(i, ChatColor.translateAlternateColorCodes('&', backLines[i]));
                    }
                }
                back.setGlowingText(info.isSignBackGlowing());
                if (info.getSignBackColor() != null) {
                    try {
                        back.setColor(DyeColor.valueOf(info.getSignBackColor()));
                    } catch (IllegalArgumentException e) {
                        // Ignore invalid color
                    }
                }
            }
        } catch (Exception e) {
            // Fallback for older versions
            String[] lines = info.getSignLines();
            for (int i = 0; i < lines.length && i < 4; i++) {
                if (lines[i] != null) {
                    try {
                        java.lang.reflect.Method setLineMethod = sign.getClass().getMethod("setLine", int.class, String.class);
                        setLineMethod.invoke(sign, i, ChatColor.translateAlternateColorCodes('&', lines[i]));
                    } catch (Exception ex) {
                        plugin.getLogger().fine("Could not restore sign line " + i + ": " + ex.getMessage());
                    }
                }
            }
        }
        sign.update(true, false);
    }

    private void restoreLectern(Block block, BlockInfo info) {
        org.bukkit.block.Lectern lectern = (org.bukkit.block.Lectern) block.getState();
        lectern.getSnapshotInventory().setItem(0, info.getLecternBook());
        lectern.setPage(info.getLecternPage());
        lectern.update(true, false);
    }

    private void restoreCampfire(Block block, BlockInfo info) {
        org.bukkit.block.Campfire campfire = (org.bukkit.block.Campfire) block.getState();
        ItemStack[] items = info.getCampfireItems();
        int[] cookTimes = info.getCampfireCookTimes();
        int[] cookTimesTotal = info.getCampfireCookTimesTotal();
        for (int i = 0; i < 4 && i < items.length; i++) {
            if (items[i] != null) {
                campfire.setItem(i, items[i]);
                if (cookTimes != null && i < cookTimes.length) {
                    campfire.setCookTime(i, cookTimes[i]);
                }
                if (cookTimesTotal != null && i < cookTimesTotal.length) {
                    campfire.setCookTimeTotal(i, cookTimesTotal[i]);
                }
            }
        }
        campfire.update(true, false);
    }

    private void restoreBeehive(Block block, BlockInfo info) {
        if (info.getBeehiveHoneyLevel() > 0) {
            org.bukkit.block.data.type.Beehive beehiveData = (org.bukkit.block.data.type.Beehive) block.getBlockData();
            beehiveData.setHoneyLevel(info.getBeehiveHoneyLevel());
            block.setBlockData(beehiveData, false);
        }
    }

    private void restoreBrewingStand(Block block, BlockInfo info) {
        org.bukkit.block.BrewingStand brewingStand = (org.bukkit.block.BrewingStand) block.getState();
        brewingStand.setFuelLevel(info.getBrewingFuel());
        brewingStand.setBrewingTime(info.getBrewingTime());
        // Container contents
        if (info.hasContainerContents() && brewingStand.getInventory() != null) {
            ItemStack[] contents = info.getContainerContents();
            for (int i = 0; i < contents.length && i < brewingStand.getInventory().getSize(); i++) {
                brewingStand.getInventory().setItem(i, contents[i]);
            }
        }
        brewingStand.update(true, false);
    }

    private void restoreSpawner(Block block, BlockInfo info) {
        org.bukkit.block.CreatureSpawner spawner = (org.bukkit.block.CreatureSpawner) block.getState();
        try {
            spawner.setSpawnedType(org.bukkit.entity.EntityType.valueOf(info.getSpawnerEntityType()));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Unknown entity type for spawner: " + info.getSpawnerEntityType());
        }
        spawner.setDelay(info.getSpawnerDelay());
        spawner.setMinSpawnDelay(info.getSpawnerMinDelay());
        spawner.setMaxSpawnDelay(info.getSpawnerMaxDelay());
        spawner.setSpawnCount(info.getSpawnerSpawnCount());
        spawner.setMaxNearbyEntities(info.getSpawnerMaxNearbyEntities());
        spawner.setRequiredPlayerRange(info.getSpawnerRequiredPlayerRange());
        spawner.setSpawnRange(info.getSpawnerSpawnRange());
        spawner.update(true, false);
    }

    private void restoreCommandBlock(Block block, BlockInfo info) {
        org.bukkit.block.CommandBlock cmdBlock = (org.bukkit.block.CommandBlock) block.getState();
        cmdBlock.setCommand(info.getCommandBlockCommand());
        if (info.getCommandBlockName() != null) {
            cmdBlock.setName(info.getCommandBlockName());
        }
        cmdBlock.update(true, false);
    }

    private void restoreDecoratedPot(Block block, BlockInfo info) {
        try {
            org.bukkit.block.DecoratedPot pot = (org.bukkit.block.DecoratedPot) block.getState();
            String[] sherdNames = info.getDecoratedPotSherds();
            if (sherdNames.length >= 4) {
                pot.setSherd(org.bukkit.block.DecoratedPot.Side.BACK, Material.valueOf(sherdNames[0]));
                pot.setSherd(org.bukkit.block.DecoratedPot.Side.LEFT, Material.valueOf(sherdNames[1]));
                pot.setSherd(org.bukkit.block.DecoratedPot.Side.RIGHT, Material.valueOf(sherdNames[2]));
                pot.setSherd(org.bukkit.block.DecoratedPot.Side.FRONT, Material.valueOf(sherdNames[3]));
                pot.update(true, false);
            }
        } catch (Exception e) {
            plugin.getLogger().fine("Could not restore decorated pot sherds: " + e.getMessage());
        }
    }

    private void restoreStructureBlock(Block block, BlockInfo info) {
        org.bukkit.block.Structure structure = (org.bukkit.block.Structure) block.getState();
        try {
            structure.setUsageMode(org.bukkit.block.structure.UsageMode.valueOf(info.getStructureBlockMode()));
        } catch (IllegalArgumentException e) {
            // Ignore invalid mode
        }
        structure.setStructureName(info.getStructureBlockName());
        if (info.getStructureBlockAuthor() != null) {
            structure.setAuthor(info.getStructureBlockAuthor());
        }
        int[] pos = info.getStructureBlockPosition();
        if (pos != null && pos.length >= 3) {
            structure.setRelativePosition(new org.bukkit.util.BlockVector(pos[0], pos[1], pos[2]));
        }
        int[] size = info.getStructureBlockSize();
        if (size != null && size.length >= 3) {
            structure.setStructureSize(new org.bukkit.util.BlockVector(size[0], size[1], size[2]));
        }
        if (info.getStructureBlockMirror() != null) {
            try {
                structure.setMirror(org.bukkit.block.structure.Mirror.valueOf(info.getStructureBlockMirror()));
            } catch (IllegalArgumentException e) {
                // Ignore invalid mirror
            }
        }
        if (info.getStructureBlockRotation() != null) {
            try {
                structure.setRotation(org.bukkit.block.structure.StructureRotation.valueOf(info.getStructureBlockRotation()));
            } catch (IllegalArgumentException e) {
                // Ignore invalid rotation
            }
        }
        structure.setIntegrity(info.getStructureBlockIntegrity());
        structure.setSeed(info.getStructureBlockSeed());
        structure.setIgnoreEntities(info.isStructureBlockIgnoreEntities());
        structure.setBoundingBoxVisible(info.isStructureBlockShowBoundingBox());
        structure.update(true, false);
    }

    private void restoreChiseledBookshelf(Block block, BlockInfo info) {
        org.bukkit.block.ChiseledBookshelf bookshelf = (org.bukkit.block.ChiseledBookshelf) block.getState();
        ItemStack[] books = info.getChiseledBookshelfBooks();
        if (bookshelf.getInventory() != null) {
            for (int i = 0; i < books.length && i < bookshelf.getInventory().getSize(); i++) {
                bookshelf.getInventory().setItem(i, books[i]);
            }
        }
        bookshelf.update(true, false);
    }

    private void restoreEndGateway(Block block, BlockInfo info) {
        org.bukkit.block.EndGateway gateway = (org.bukkit.block.EndGateway) block.getState();
        gateway.setExitLocation(info.getEndGatewayExitLocation());
        gateway.setExactTeleport(info.isEndGatewayExactTeleport());
        gateway.setAge(info.getEndGatewayAge());
        gateway.update(true, false);
    }

    private void restoreComparator(Block block, BlockInfo info) {
        org.bukkit.block.data.type.Comparator comparatorData = (org.bukkit.block.data.type.Comparator) block.getBlockData();
        try {
            comparatorData.setMode(org.bukkit.block.data.type.Comparator.Mode.valueOf(info.getComparatorMode()));
            block.setBlockData(comparatorData, false);
        } catch (IllegalArgumentException e) {
            // Ignore invalid mode
        }
    }

    private void restoreJukebox(Block block, BlockInfo info) {
        org.bukkit.block.Jukebox jukebox = (org.bukkit.block.Jukebox) block.getState();
        jukebox.setRecord(info.getJukeboxRecord());
        jukebox.update(true, false);
    }

    private void restoreSkull(Block block, BlockInfo info) {
        org.bukkit.block.Skull skull = (org.bukkit.block.Skull) block.getState();
        try {
            java.util.UUID uuid = java.util.UUID.fromString(info.getSkullOwner());
            org.bukkit.OfflinePlayer owner = Bukkit.getOfflinePlayer(uuid);
            skull.setOwningPlayer(owner);
        } catch (IllegalArgumentException e) {
            @SuppressWarnings("deprecation")
            org.bukkit.OfflinePlayer owner = Bukkit.getOfflinePlayer(info.getSkullOwner());
            skull.setOwningPlayer(owner);
        }
        skull.update(true, false);
    }

    private void schedulePOIUpdate(Block block) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                block.getState().update(true, true);
                block.getChunk().load();
            } catch (Exception e) {
                // Ignore
            }
        }, 2L);
    }

    /**
     * Restores container contents with delayed execution for proper state updates.
     * @return true if container contents were found and restoration was scheduled
     */
    public boolean restoreContainerContents(Block block, BlockInfo info) {
        if (!info.hasContainerContents()) {
            return false;
        }

        try {
            if (block.getState() instanceof org.bukkit.block.Container) {
                plugin.getLogger().info("Restoring container at " + block.getLocation() +
                        " - Type: " + block.getType() + ", State: " + block.getState().getClass().getSimpleName());

                org.bukkit.block.Container container = (org.bukkit.block.Container) block.getState();
                ItemStack[] contents = info.getContainerContents();

                if (contents != null) {
                    container.getInventory().clear();

                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        try {
                            restoreContainerContentsDelayed(block, contents);
                        } catch (Exception e) {
                            plugin.getLogger().severe("Failed to restore container contents (delayed) at " +
                                    block.getLocation() + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    }, 2L);

                    return true;
                }
            } else {
                plugin.getLogger().warning("Block at " + block.getLocation() +
                        " is not a supported container type but has container contents! Type: " + block.getType() +
                        ", State: " + block.getState().getClass().getSimpleName());
                return false;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to restore container contents at " +
                    block.getLocation() + ": " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    private void restoreContainerContentsDelayed(Block block, ItemStack[] contents) {
        org.bukkit.block.Container freshContainer = (org.bukkit.block.Container) block.getState();

        plugin.getLogger().info("Restoring container contents (" + block.getType() + ") at " + block.getLocation());

        ItemStack[] safeCopy = new ItemStack[freshContainer.getInventory().getSize()];
        for (int i = 0; i < safeCopy.length && i < contents.length; i++) {
            if (contents[i] != null && contents[i].getType() != Material.AIR) {
                safeCopy[i] = contents[i].clone();
            }
        }

        freshContainer.getInventory().setContents(safeCopy);
        freshContainer.update(true, true);

        // Alternative method as backup
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

        // Update neighboring blocks
        if (block.getChunk().isLoaded()) {
            updateNeighboringBlocks(block);
        }

        plugin.getLogger().info("Successfully restored container (" + block.getType() +
                ") at " + block.getLocation() + " with " + getContainerSummary(contents));
    }

    private void updateNeighboringBlocks(Block block) {
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

    /**
     * Returns a summary of container contents for logging.
     */
    public String getContainerSummary(ItemStack[] contents) {
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
}

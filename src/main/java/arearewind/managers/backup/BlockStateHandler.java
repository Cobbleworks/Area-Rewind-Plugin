package arearewind.managers.backup;

import arearewind.data.BlockInfo;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Banner;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Set;

/**
 * Handles capturing and analyzing block states for backup purposes.
 * Extracts all special block data (signs, containers, spawners, etc.) into BlockInfo objects.
 */
public class BlockStateHandler {

    private final JavaPlugin plugin;

    private static final Set<Material> POI_BLOCKS = Set.of(
            Material.CARTOGRAPHY_TABLE, Material.FLETCHING_TABLE,
            Material.SMITHING_TABLE, Material.LOOM, Material.STONECUTTER,
            Material.GRINDSTONE, Material.BARREL, Material.SMOKER, Material.BLAST_FURNACE,
            Material.FURNACE, Material.COMPOSTER, Material.BELL);

    private static final Set<Material> BED_BLOCKS = Set.of(
            Material.WHITE_BED, Material.ORANGE_BED, Material.MAGENTA_BED, Material.LIGHT_BLUE_BED,
            Material.YELLOW_BED, Material.LIME_BED, Material.PINK_BED, Material.GRAY_BED,
            Material.LIGHT_GRAY_BED, Material.CYAN_BED, Material.PURPLE_BED, Material.BLUE_BED,
            Material.BROWN_BED, Material.GREEN_BED, Material.RED_BED, Material.BLACK_BED);

    public BlockStateHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Creates a BlockInfo object capturing all block state data including special properties.
     * Must be called from the main thread.
     */
    public BlockInfo createBlockInfo(Block block) {
        try {
            if (!Bukkit.isPrimaryThread()) {
                plugin.getLogger().warning("createBlockInfo called from async thread for block at " + block.getLocation());
                return new BlockInfo(block.getType(), block.getBlockData());
            }

            BlockInfo blockInfo = new BlockInfo(block.getType(), block.getBlockData());

            try {
                captureSpecialBlockState(block, blockInfo);
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

    /**
     * Captures special block state data based on block type.
     */
    private void captureSpecialBlockState(Block block, BlockInfo blockInfo) {
        // Banner
        if (block.getState() instanceof Banner) {
            captureBannerState(block, blockInfo);
        }
        // Sign - capture both sides, colors, and glow state
        else if (block.getState() instanceof Sign) {
            captureSignState(block, blockInfo);
        }
        // Lectern
        else if (block.getState() instanceof org.bukkit.block.Lectern) {
            captureLecternState(block, blockInfo);
        }
        // Campfire
        else if (block.getState() instanceof org.bukkit.block.Campfire) {
            captureCampfireState(block, blockInfo);
        }
        // Beehive/Bee Nest
        else if (block.getState() instanceof org.bukkit.block.Beehive) {
            captureBeehiveState(block, blockInfo);
        }
        // Brewing Stand
        else if (block.getState() instanceof org.bukkit.block.BrewingStand) {
            captureBrewingStandState(block, blockInfo);
        }
        // Spawner
        else if (block.getState() instanceof org.bukkit.block.CreatureSpawner) {
            captureSpawnerState(block, blockInfo);
        }
        // Command Block
        else if (block.getState() instanceof org.bukkit.block.CommandBlock) {
            captureCommandBlockState(block, blockInfo);
        }
        // Decorated Pot
        else if (block.getState() instanceof org.bukkit.block.DecoratedPot) {
            captureDecoratedPotState(block, blockInfo);
        }
        // Structure Block
        else if (block.getState() instanceof org.bukkit.block.Structure) {
            captureStructureBlockState(block, blockInfo);
        }
        // Chiseled Bookshelf
        else if (block.getState() instanceof org.bukkit.block.ChiseledBookshelf) {
            captureChiseledBookshelfState(block, blockInfo);
        }
        // End Gateway
        else if (block.getState() instanceof org.bukkit.block.EndGateway) {
            captureEndGatewayState(block, blockInfo);
        }
        // Comparator
        else if (block.getState() instanceof org.bukkit.block.Comparator) {
            captureComparatorState(block, blockInfo);
        }
        // Generic Container (chests, barrels, hoppers, dispensers, droppers, shulker boxes, etc.)
        else if (block.getState() instanceof org.bukkit.block.Container) {
            captureContainerState(block, blockInfo);
        }
        // Jukebox
        else if (block.getState() instanceof org.bukkit.block.Jukebox) {
            captureJukeboxState(block, blockInfo);
        }
        // Skull
        else if (block.getState() instanceof org.bukkit.block.Skull) {
            captureSkullState(block, blockInfo);
        }
    }

    private void captureBannerState(Block block, BlockInfo blockInfo) {
        Banner banner = (Banner) block.getState();
        if (banner.getPatterns() != null) {
            blockInfo.setBannerPatterns(new ArrayList<>(banner.getPatterns()));
        }
    }

    private void captureSignState(Block block, BlockInfo blockInfo) {
        Sign sign = (Sign) block.getState();
        try {
            // Front side
            org.bukkit.block.sign.SignSide front = sign.getSide(org.bukkit.block.sign.Side.FRONT);
            String[] frontLines = new String[4];
            for (int i = 0; i < 4; i++) {
                frontLines[i] = front.getLine(i);
            }
            blockInfo.setSignLines(frontLines);
            blockInfo.setSignGlowing(front.isGlowingText());
            if (front.getColor() != null) {
                blockInfo.setSignColor(front.getColor().name());
            }

            // Back side
            org.bukkit.block.sign.SignSide back = sign.getSide(org.bukkit.block.sign.Side.BACK);
            String[] backLines = new String[4];
            for (int i = 0; i < 4; i++) {
                backLines[i] = back.getLine(i);
            }
            blockInfo.setSignBackLines(backLines);
            blockInfo.setSignBackGlowing(back.isGlowingText());
            if (back.getColor() != null) {
                blockInfo.setSignBackColor(back.getColor().name());
            }
        } catch (Exception e) {
            // Fallback to legacy method for older versions
            @SuppressWarnings("deprecation")
            String[] legacyLines = sign.getLines();
            if (legacyLines != null) {
                blockInfo.setSignLines(legacyLines);
            }
        }
    }

    private void captureLecternState(Block block, BlockInfo blockInfo) {
        org.bukkit.block.Lectern lectern = (org.bukkit.block.Lectern) block.getState();
        if (lectern.getInventory().getItem(0) != null) {
            blockInfo.setLecternBook(lectern.getInventory().getItem(0));
            blockInfo.setLecternPage(lectern.getPage());
        }
    }

    private void captureCampfireState(Block block, BlockInfo blockInfo) {
        org.bukkit.block.Campfire campfire = (org.bukkit.block.Campfire) block.getState();
        ItemStack[] items = new ItemStack[4];
        int[] cookTimes = new int[4];
        int[] cookTimesTotal = new int[4];
        boolean hasItems = false;
        for (int i = 0; i < 4; i++) {
            items[i] = campfire.getItem(i);
            cookTimes[i] = campfire.getCookTime(i);
            cookTimesTotal[i] = campfire.getCookTimeTotal(i);
            if (items[i] != null) hasItems = true;
        }
        if (hasItems) {
            blockInfo.setCampfireItems(items);
            blockInfo.setCampfireCookTimes(cookTimes);
            blockInfo.setCampfireCookTimesTotal(cookTimesTotal);
        }
    }

    private void captureBeehiveState(Block block, BlockInfo blockInfo) {
        org.bukkit.block.Beehive beehive = (org.bukkit.block.Beehive) block.getState();
        org.bukkit.block.data.type.Beehive beehiveData = (org.bukkit.block.data.type.Beehive) block.getBlockData();
        blockInfo.setBeehiveHoneyLevel(beehiveData.getHoneyLevel());
        blockInfo.setBeehiveBeeCount(beehive.getEntityCount());
    }

    private void captureBrewingStandState(Block block, BlockInfo blockInfo) {
        org.bukkit.block.BrewingStand brewingStand = (org.bukkit.block.BrewingStand) block.getState();
        blockInfo.setBrewingFuel(brewingStand.getFuelLevel());
        blockInfo.setBrewingTime(brewingStand.getBrewingTime());
        if (brewingStand.getInventory() != null) {
            blockInfo.setContainerContents(brewingStand.getInventory().getContents());
        }
    }

    private void captureSpawnerState(Block block, BlockInfo blockInfo) {
        org.bukkit.block.CreatureSpawner spawner = (org.bukkit.block.CreatureSpawner) block.getState();
        if (spawner.getSpawnedType() != null) {
            blockInfo.setSpawnerEntityType(spawner.getSpawnedType().name());
        }
        blockInfo.setSpawnerDelay(spawner.getDelay());
        blockInfo.setSpawnerMinDelay(spawner.getMinSpawnDelay());
        blockInfo.setSpawnerMaxDelay(spawner.getMaxSpawnDelay());
        blockInfo.setSpawnerSpawnCount(spawner.getSpawnCount());
        blockInfo.setSpawnerMaxNearbyEntities(spawner.getMaxNearbyEntities());
        blockInfo.setSpawnerRequiredPlayerRange(spawner.getRequiredPlayerRange());
        blockInfo.setSpawnerSpawnRange(spawner.getSpawnRange());
    }

    private void captureCommandBlockState(Block block, BlockInfo blockInfo) {
        org.bukkit.block.CommandBlock cmdBlock = (org.bukkit.block.CommandBlock) block.getState();
        blockInfo.setCommandBlockCommand(cmdBlock.getCommand());
        blockInfo.setCommandBlockName(cmdBlock.getName());
    }

    private void captureDecoratedPotState(Block block, BlockInfo blockInfo) {
        try {
            org.bukkit.block.DecoratedPot pot = (org.bukkit.block.DecoratedPot) block.getState();
            java.util.Map<org.bukkit.block.DecoratedPot.Side, Material> sherds = pot.getSherds();
            String[] sherdNames = new String[4];
            sherdNames[0] = sherds.getOrDefault(org.bukkit.block.DecoratedPot.Side.BACK, Material.BRICK).name();
            sherdNames[1] = sherds.getOrDefault(org.bukkit.block.DecoratedPot.Side.LEFT, Material.BRICK).name();
            sherdNames[2] = sherds.getOrDefault(org.bukkit.block.DecoratedPot.Side.RIGHT, Material.BRICK).name();
            sherdNames[3] = sherds.getOrDefault(org.bukkit.block.DecoratedPot.Side.FRONT, Material.BRICK).name();
            blockInfo.setDecoratedPotSherds(sherdNames);
        } catch (Exception e) {
            plugin.getLogger().fine("Could not capture decorated pot sherds: " + e.getMessage());
        }
    }

    private void captureStructureBlockState(Block block, BlockInfo blockInfo) {
        org.bukkit.block.Structure structure = (org.bukkit.block.Structure) block.getState();
        blockInfo.setStructureBlockMode(structure.getUsageMode().name());
        blockInfo.setStructureBlockName(structure.getStructureName());
        blockInfo.setStructureBlockAuthor(structure.getAuthor());
        blockInfo.setStructureBlockPosition(new int[]{
            structure.getRelativePosition().getBlockX(),
            structure.getRelativePosition().getBlockY(),
            structure.getRelativePosition().getBlockZ()
        });
        blockInfo.setStructureBlockSize(new int[]{
            structure.getStructureSize().getBlockX(),
            structure.getStructureSize().getBlockY(),
            structure.getStructureSize().getBlockZ()
        });
        blockInfo.setStructureBlockMirror(structure.getMirror().name());
        blockInfo.setStructureBlockRotation(structure.getRotation().name());
        blockInfo.setStructureBlockIntegrity(structure.getIntegrity());
        blockInfo.setStructureBlockSeed(structure.getSeed());
        blockInfo.setStructureBlockIgnoreEntities(structure.isIgnoreEntities());
        blockInfo.setStructureBlockShowBoundingBox(structure.isBoundingBoxVisible());
    }

    private void captureChiseledBookshelfState(Block block, BlockInfo blockInfo) {
        org.bukkit.block.ChiseledBookshelf bookshelf = (org.bukkit.block.ChiseledBookshelf) block.getState();
        if (bookshelf.getInventory() != null) {
            blockInfo.setChiseledBookshelfBooks(bookshelf.getInventory().getContents());
        }
    }

    private void captureEndGatewayState(Block block, BlockInfo blockInfo) {
        org.bukkit.block.EndGateway gateway = (org.bukkit.block.EndGateway) block.getState();
        if (gateway.getExitLocation() != null) {
            blockInfo.setEndGatewayExitLocation(gateway.getExitLocation());
        }
        blockInfo.setEndGatewayExactTeleport(gateway.isExactTeleport());
        blockInfo.setEndGatewayAge(gateway.getAge());
    }

    private void captureComparatorState(Block block, BlockInfo blockInfo) {
        org.bukkit.block.data.type.Comparator comparatorData =
            (org.bukkit.block.data.type.Comparator) block.getBlockData();
        blockInfo.setComparatorMode(comparatorData.getMode().name());
    }

    private void captureContainerState(Block block, BlockInfo blockInfo) {
        org.bukkit.block.Container container = (org.bukkit.block.Container) block.getState();
        if (container.getInventory() != null) {
            ItemStack[] contents = container.getInventory().getContents();
            blockInfo.setContainerContents(contents);

            plugin.getLogger().fine("Backup: Container (" + block.getType() + ") at " +
                    block.getLocation() + " has " + (contents != null ? contents.length : 0) +
                    " slots with items: " + getContainerSummary(contents));
        }
    }

    private void captureJukeboxState(Block block, BlockInfo blockInfo) {
        org.bukkit.block.Jukebox jukebox = (org.bukkit.block.Jukebox) block.getState();
        if (jukebox.getRecord() != null) {
            blockInfo.setJukeboxRecord(jukebox.getRecord());
        }
    }

    private void captureSkullState(Block block, BlockInfo blockInfo) {
        org.bukkit.block.Skull skull = (org.bukkit.block.Skull) block.getState();
        if (skull.getOwningPlayer() != null) {
            blockInfo.setSkullOwner(skull.getOwningPlayer().getUniqueId().toString());
        }
    }

    /**
     * Returns a summary of container contents for logging.
     */
    private String getContainerSummary(ItemStack[] contents) {
        if (contents == null) return "null";
        int nonNull = 0;
        for (ItemStack item : contents) {
            if (item != null && item.getType() != Material.AIR) nonNull++;
        }
        return nonNull + " items";
    }

    /**
     * Checks if a BlockInfo has any special properties that require additional processing during restore.
     */
    public boolean hasSpecialProperties(BlockInfo info) {
        return info.getBannerPatterns() != null ||
               info.getSignLines() != null ||
               info.getJukeboxRecord() != null ||
               info.getSkullOwner() != null ||
               info.getLecternBook() != null ||
               info.getCampfireItems() != null ||
               info.getBeehiveHoneyLevel() > 0 ||
               info.getSpawnerEntityType() != null ||
               info.getCommandBlockCommand() != null ||
               info.getDecoratedPotSherds() != null ||
               info.getStructureBlockName() != null ||
               info.getChiseledBookshelfBooks() != null ||
               info.getEndGatewayExitLocation() != null ||
               info.getComparatorMode() != null;
    }

    /**
     * Checks if a material is a POI (Point of Interest) block used by villagers.
     */
    public boolean isPOIBlock(Material material) {
        return POI_BLOCKS.contains(material) || BED_BLOCKS.contains(material);
    }

    /**
     * Returns the set of POI blocks.
     */
    public Set<Material> getPOIBlocks() {
        return POI_BLOCKS;
    }

    /**
     * Returns the set of bed blocks.
     */
    public Set<Material> getBedBlocks() {
        return BED_BLOCKS;
    }
}

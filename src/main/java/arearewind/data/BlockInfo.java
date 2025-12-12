package arearewind.data;

import org.bukkit.*;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.*;

@SerializableAs("BlockInfo")
public class BlockInfo implements Serializable, ConfigurationSerializable {

    private static final long serialVersionUID = 2L; // Updated for new fields

    private Material material;
    private String blockDataString;
    private List<Pattern> bannerPatterns;
    private String[] signLines;
    private String[] signBackLines; // For signs with text on both sides (1.20+)
    private boolean signGlowing;
    private boolean signBackGlowing;
    private String signColor; // DyeColor name
    private String signBackColor;
    private ItemStack[] containerContents;
    private ItemStack jukeboxRecord;
    private String skullOwner;
    private String skullData; // Base64 serialized skull data for custom heads
    private Material flowerPotItem;
    private String containerContentsDebug;
    private String containerContentsBase64;
    
    // Lectern data
    private ItemStack lecternBook;
    private int lecternPage;
    
    // Campfire data
    private ItemStack[] campfireItems; // 4 cooking slots
    private int[] campfireCookTimes;
    private int[] campfireCookTimesTotal;
    
    // Beehive/Bee Nest data
    private int beehiveHoneyLevel;
    private int beehiveBeeCount;
    
    // Brewing Stand data
    private int brewingFuel;
    private int brewingTime;
    
    // Spawner data
    private String spawnerEntityType;
    private int spawnerDelay;
    private int spawnerMinDelay;
    private int spawnerMaxDelay;
    private int spawnerSpawnCount;
    private int spawnerMaxNearbyEntities;
    private int spawnerRequiredPlayerRange;
    private int spawnerSpawnRange;
    
    // Command Block data
    private String commandBlockCommand;
    private String commandBlockName;
    private boolean commandBlockTrackOutput;
    
    // Decorated Pot data (sherds stored as material names)
    private String[] decoratedPotSherds; // 4 sherds: back, left, right, front
    
    // Structure Block data
    private String structureBlockMode;
    private String structureBlockName;
    private String structureBlockAuthor;
    private int[] structureBlockPosition; // relative position x, y, z
    private int[] structureBlockSize; // size x, y, z
    private String structureBlockMirror;
    private String structureBlockRotation;
    private float structureBlockIntegrity;
    private long structureBlockSeed;
    private boolean structureBlockIgnoreEntities;
    private boolean structureBlockShowBoundingBox;
    private boolean structureBlockShowAir;
    
    // Chiseled Bookshelf data
    private ItemStack[] chiseledBookshelfBooks; // 6 book slots
    
    // End Gateway data
    private Location endGatewayExitLocation;
    private boolean endGatewayExactTeleport;
    private long endGatewayAge;
    
    // Comparator data
    private String comparatorMode;
    
    // Note Block data
    private String noteBlockInstrument;
    private int noteBlockNote;

    public BlockInfo(Material material, BlockData blockData) {
        this.material = material;
        this.blockDataString = blockData.getAsString();
    }

    public BlockInfo(Material material, BlockData blockData, List<Pattern> bannerPatterns) {
        this(material, blockData);
        this.bannerPatterns = bannerPatterns != null ? new ArrayList<>(bannerPatterns) : null;
    }

    public BlockInfo(Material material, BlockData blockData, String[] signLines) {
        this(material, blockData);
        this.signLines = signLines != null ? signLines.clone() : null;
    }

    public BlockInfo(Material material, BlockData blockData, ItemStack[] containerContents) {
        this(material, blockData);
        setContainerContents(containerContents);
    }

    public BlockInfo(Material material, BlockData blockData, ItemStack jukeboxRecord, boolean isJukebox) {
        this(material, blockData);
        this.jukeboxRecord = jukeboxRecord;
    }

    public BlockInfo(Material material, BlockData blockData, String skullOwner) {
        this(material, blockData);
        this.skullOwner = skullOwner;
    }

    public Material getMaterial() {
        return material;
    }

    public BlockData getBlockData() {
        try {
            return Bukkit.createBlockData(blockDataString);
        } catch (IllegalArgumentException e) {
            return material.createBlockData();
        }
    }

    public List<Pattern> getBannerPatterns() {
        return bannerPatterns != null ? new ArrayList<>(bannerPatterns) : null;
    }

    public void setBannerPatterns(List<Pattern> bannerPatterns) {
        this.bannerPatterns = bannerPatterns != null ? new ArrayList<>(bannerPatterns) : null;
    }

    public String[] getSignLines() {
        return signLines != null ? signLines.clone() : null;
    }

    public void setSignLines(String[] signLines) {
        this.signLines = signLines != null ? signLines.clone() : null;
    }

    public ItemStack[] getContainerContents() {
        return containerContents != null ? cloneItemStackArray(containerContents) : null;
    }

    public void setContainerContents(ItemStack[] contents) {
        if (contents != null) {
            this.containerContents = cloneItemStackArray(contents);
            this.containerContentsDebug = Arrays.toString(contents);
            this.containerContentsBase64 = serializeItemStackArrayToBase64(contents);
        } else {
            this.containerContents = null;
            this.containerContentsDebug = null;
            this.containerContentsBase64 = null;
        }
    }

    public boolean hasContainerContents() {
        return containerContents != null && containerContents.length > 0;
    }

    public ItemStack getJukeboxRecord() {
        return jukeboxRecord != null ? jukeboxRecord.clone() : null;
    }

    public void setJukeboxRecord(ItemStack jukeboxRecord) {
        this.jukeboxRecord = jukeboxRecord != null ? jukeboxRecord.clone() : null;
    }

    public String getSkullOwner() {
        return skullOwner;
    }

    public void setSkullOwner(String skullOwner) {
        this.skullOwner = skullOwner;
    }

    public String getSkullData() {
        return skullData;
    }

    public void setSkullData(String skullData) {
        this.skullData = skullData;
    }

    public Material getFlowerPotItem() {
        return flowerPotItem;
    }

    public void setFlowerPotItem(Material flowerPotItem) {
        this.flowerPotItem = flowerPotItem;
    }

    public String getContainerContentsDebug() {
        return containerContentsDebug;
    }

    public String getContainerContentsBase64() {
        return containerContentsBase64;
    }

    // Sign back side getters/setters (1.20+)
    public String[] getSignBackLines() {
        return signBackLines != null ? signBackLines.clone() : null;
    }

    public void setSignBackLines(String[] signBackLines) {
        this.signBackLines = signBackLines != null ? signBackLines.clone() : null;
    }

    public boolean isSignGlowing() {
        return signGlowing;
    }

    public void setSignGlowing(boolean signGlowing) {
        this.signGlowing = signGlowing;
    }

    public boolean isSignBackGlowing() {
        return signBackGlowing;
    }

    public void setSignBackGlowing(boolean signBackGlowing) {
        this.signBackGlowing = signBackGlowing;
    }

    public String getSignColor() {
        return signColor;
    }

    public void setSignColor(String signColor) {
        this.signColor = signColor;
    }

    public String getSignBackColor() {
        return signBackColor;
    }

    public void setSignBackColor(String signBackColor) {
        this.signBackColor = signBackColor;
    }

    // Lectern getters/setters
    public ItemStack getLecternBook() {
        return lecternBook != null ? lecternBook.clone() : null;
    }

    public void setLecternBook(ItemStack lecternBook) {
        this.lecternBook = lecternBook != null ? lecternBook.clone() : null;
    }

    public int getLecternPage() {
        return lecternPage;
    }

    public void setLecternPage(int lecternPage) {
        this.lecternPage = lecternPage;
    }

    // Campfire getters/setters
    public ItemStack[] getCampfireItems() {
        return campfireItems != null ? cloneItemStackArray(campfireItems) : null;
    }

    public void setCampfireItems(ItemStack[] campfireItems) {
        this.campfireItems = campfireItems != null ? cloneItemStackArray(campfireItems) : null;
    }

    public int[] getCampfireCookTimes() {
        return campfireCookTimes != null ? campfireCookTimes.clone() : null;
    }

    public void setCampfireCookTimes(int[] campfireCookTimes) {
        this.campfireCookTimes = campfireCookTimes != null ? campfireCookTimes.clone() : null;
    }

    public int[] getCampfireCookTimesTotal() {
        return campfireCookTimesTotal != null ? campfireCookTimesTotal.clone() : null;
    }

    public void setCampfireCookTimesTotal(int[] campfireCookTimesTotal) {
        this.campfireCookTimesTotal = campfireCookTimesTotal != null ? campfireCookTimesTotal.clone() : null;
    }

    // Beehive getters/setters
    public int getBeehiveHoneyLevel() {
        return beehiveHoneyLevel;
    }

    public void setBeehiveHoneyLevel(int beehiveHoneyLevel) {
        this.beehiveHoneyLevel = beehiveHoneyLevel;
    }

    public int getBeehiveBeeCount() {
        return beehiveBeeCount;
    }

    public void setBeehiveBeeCount(int beehiveBeeCount) {
        this.beehiveBeeCount = beehiveBeeCount;
    }

    // Brewing Stand getters/setters
    public int getBrewingFuel() {
        return brewingFuel;
    }

    public void setBrewingFuel(int brewingFuel) {
        this.brewingFuel = brewingFuel;
    }

    public int getBrewingTime() {
        return brewingTime;
    }

    public void setBrewingTime(int brewingTime) {
        this.brewingTime = brewingTime;
    }

    // Spawner getters/setters
    public String getSpawnerEntityType() {
        return spawnerEntityType;
    }

    public void setSpawnerEntityType(String spawnerEntityType) {
        this.spawnerEntityType = spawnerEntityType;
    }

    public int getSpawnerDelay() {
        return spawnerDelay;
    }

    public void setSpawnerDelay(int spawnerDelay) {
        this.spawnerDelay = spawnerDelay;
    }

    public int getSpawnerMinDelay() {
        return spawnerMinDelay;
    }

    public void setSpawnerMinDelay(int spawnerMinDelay) {
        this.spawnerMinDelay = spawnerMinDelay;
    }

    public int getSpawnerMaxDelay() {
        return spawnerMaxDelay;
    }

    public void setSpawnerMaxDelay(int spawnerMaxDelay) {
        this.spawnerMaxDelay = spawnerMaxDelay;
    }

    public int getSpawnerSpawnCount() {
        return spawnerSpawnCount;
    }

    public void setSpawnerSpawnCount(int spawnerSpawnCount) {
        this.spawnerSpawnCount = spawnerSpawnCount;
    }

    public int getSpawnerMaxNearbyEntities() {
        return spawnerMaxNearbyEntities;
    }

    public void setSpawnerMaxNearbyEntities(int spawnerMaxNearbyEntities) {
        this.spawnerMaxNearbyEntities = spawnerMaxNearbyEntities;
    }

    public int getSpawnerRequiredPlayerRange() {
        return spawnerRequiredPlayerRange;
    }

    public void setSpawnerRequiredPlayerRange(int spawnerRequiredPlayerRange) {
        this.spawnerRequiredPlayerRange = spawnerRequiredPlayerRange;
    }

    public int getSpawnerSpawnRange() {
        return spawnerSpawnRange;
    }

    public void setSpawnerSpawnRange(int spawnerSpawnRange) {
        this.spawnerSpawnRange = spawnerSpawnRange;
    }

    // Command Block getters/setters
    public String getCommandBlockCommand() {
        return commandBlockCommand;
    }

    public void setCommandBlockCommand(String commandBlockCommand) {
        this.commandBlockCommand = commandBlockCommand;
    }

    public String getCommandBlockName() {
        return commandBlockName;
    }

    public void setCommandBlockName(String commandBlockName) {
        this.commandBlockName = commandBlockName;
    }

    public boolean isCommandBlockTrackOutput() {
        return commandBlockTrackOutput;
    }

    public void setCommandBlockTrackOutput(boolean commandBlockTrackOutput) {
        this.commandBlockTrackOutput = commandBlockTrackOutput;
    }

    // Decorated Pot getters/setters
    public String[] getDecoratedPotSherds() {
        return decoratedPotSherds != null ? decoratedPotSherds.clone() : null;
    }

    public void setDecoratedPotSherds(String[] decoratedPotSherds) {
        this.decoratedPotSherds = decoratedPotSherds != null ? decoratedPotSherds.clone() : null;
    }

    // Structure Block getters/setters
    public String getStructureBlockMode() {
        return structureBlockMode;
    }

    public void setStructureBlockMode(String structureBlockMode) {
        this.structureBlockMode = structureBlockMode;
    }

    public String getStructureBlockName() {
        return structureBlockName;
    }

    public void setStructureBlockName(String structureBlockName) {
        this.structureBlockName = structureBlockName;
    }

    public String getStructureBlockAuthor() {
        return structureBlockAuthor;
    }

    public void setStructureBlockAuthor(String structureBlockAuthor) {
        this.structureBlockAuthor = structureBlockAuthor;
    }

    public int[] getStructureBlockPosition() {
        return structureBlockPosition != null ? structureBlockPosition.clone() : null;
    }

    public void setStructureBlockPosition(int[] structureBlockPosition) {
        this.structureBlockPosition = structureBlockPosition != null ? structureBlockPosition.clone() : null;
    }

    public int[] getStructureBlockSize() {
        return structureBlockSize != null ? structureBlockSize.clone() : null;
    }

    public void setStructureBlockSize(int[] structureBlockSize) {
        this.structureBlockSize = structureBlockSize != null ? structureBlockSize.clone() : null;
    }

    public String getStructureBlockMirror() {
        return structureBlockMirror;
    }

    public void setStructureBlockMirror(String structureBlockMirror) {
        this.structureBlockMirror = structureBlockMirror;
    }

    public String getStructureBlockRotation() {
        return structureBlockRotation;
    }

    public void setStructureBlockRotation(String structureBlockRotation) {
        this.structureBlockRotation = structureBlockRotation;
    }

    public float getStructureBlockIntegrity() {
        return structureBlockIntegrity;
    }

    public void setStructureBlockIntegrity(float structureBlockIntegrity) {
        this.structureBlockIntegrity = structureBlockIntegrity;
    }

    public long getStructureBlockSeed() {
        return structureBlockSeed;
    }

    public void setStructureBlockSeed(long structureBlockSeed) {
        this.structureBlockSeed = structureBlockSeed;
    }

    public boolean isStructureBlockIgnoreEntities() {
        return structureBlockIgnoreEntities;
    }

    public void setStructureBlockIgnoreEntities(boolean structureBlockIgnoreEntities) {
        this.structureBlockIgnoreEntities = structureBlockIgnoreEntities;
    }

    public boolean isStructureBlockShowBoundingBox() {
        return structureBlockShowBoundingBox;
    }

    public void setStructureBlockShowBoundingBox(boolean structureBlockShowBoundingBox) {
        this.structureBlockShowBoundingBox = structureBlockShowBoundingBox;
    }

    public boolean isStructureBlockShowAir() {
        return structureBlockShowAir;
    }

    public void setStructureBlockShowAir(boolean structureBlockShowAir) {
        this.structureBlockShowAir = structureBlockShowAir;
    }

    // Chiseled Bookshelf getters/setters
    public ItemStack[] getChiseledBookshelfBooks() {
        return chiseledBookshelfBooks != null ? cloneItemStackArray(chiseledBookshelfBooks) : null;
    }

    public void setChiseledBookshelfBooks(ItemStack[] chiseledBookshelfBooks) {
        this.chiseledBookshelfBooks = chiseledBookshelfBooks != null ? cloneItemStackArray(chiseledBookshelfBooks) : null;
    }

    // End Gateway getters/setters
    public Location getEndGatewayExitLocation() {
        return endGatewayExitLocation != null ? endGatewayExitLocation.clone() : null;
    }

    public void setEndGatewayExitLocation(Location endGatewayExitLocation) {
        this.endGatewayExitLocation = endGatewayExitLocation != null ? endGatewayExitLocation.clone() : null;
    }

    public boolean isEndGatewayExactTeleport() {
        return endGatewayExactTeleport;
    }

    public void setEndGatewayExactTeleport(boolean endGatewayExactTeleport) {
        this.endGatewayExactTeleport = endGatewayExactTeleport;
    }

    public long getEndGatewayAge() {
        return endGatewayAge;
    }

    public void setEndGatewayAge(long endGatewayAge) {
        this.endGatewayAge = endGatewayAge;
    }

    // Comparator getters/setters
    public String getComparatorMode() {
        return comparatorMode;
    }

    public void setComparatorMode(String comparatorMode) {
        this.comparatorMode = comparatorMode;
    }

    // Note Block getters/setters
    public String getNoteBlockInstrument() {
        return noteBlockInstrument;
    }

    public void setNoteBlockInstrument(String noteBlockInstrument) {
        this.noteBlockInstrument = noteBlockInstrument;
    }

    public int getNoteBlockNote() {
        return noteBlockNote;
    }

    public void setNoteBlockNote(int noteBlockNote) {
        this.noteBlockNote = noteBlockNote;
    }

    // Helper method to check if block has any special data
    public boolean hasSpecialData() {
        return bannerPatterns != null ||
               signLines != null ||
               containerContents != null ||
               jukeboxRecord != null ||
               skullOwner != null ||
               flowerPotItem != null ||
               lecternBook != null ||
               campfireItems != null ||
               spawnerEntityType != null ||
               commandBlockCommand != null ||
               decoratedPotSherds != null ||
               structureBlockName != null ||
               chiseledBookshelfBooks != null ||
               endGatewayExitLocation != null;
    }

    private ItemStack[] cloneItemStackArray(ItemStack[] original) {
        if (original == null)
            return null;

        ItemStack[] clone = new ItemStack[original.length];
        for (int i = 0; i < original.length; i++) {
            clone[i] = original[i] != null ? original[i].clone() : null;
        }
        return clone;
    }

    public static String serializeItemStackArrayToBase64(ItemStack[] items) {
        if (items == null)
            return null;

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(items.length);

            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }

            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ItemStack[] deserializeItemStackArrayFromBase64(String data) {
        if (data == null || data.isEmpty())
            return null;

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            int length = dataInput.readInt();
            ItemStack[] items = new ItemStack[length];

            for (int i = 0; i < length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            dataInput.close();
            return items;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BlockInfo))
            return false;
        BlockInfo other = (BlockInfo) obj;
        return material == other.material &&
                blockDataString.equals(other.blockDataString) &&
                Objects.equals(bannerPatterns, other.bannerPatterns) &&
                Arrays.equals(signLines, other.signLines) &&
                Arrays.deepEquals(containerContents, other.containerContents) &&
                Objects.equals(jukeboxRecord, other.jukeboxRecord) &&
                Objects.equals(skullOwner, other.skullOwner) &&
                Objects.equals(skullData, other.skullData) &&
                Objects.equals(flowerPotItem, other.flowerPotItem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(material, blockDataString, bannerPatterns,
                Arrays.hashCode(signLines), Arrays.deepHashCode(containerContents),
                jukeboxRecord, skullOwner, skullData, flowerPotItem);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(material.name()).append("[").append(blockDataString).append("]");

        if (bannerPatterns != null && !bannerPatterns.isEmpty()) {
            sb.append(" (Banner: ").append(bannerPatterns.size()).append(" patterns)");
        }
        if (signLines != null) {
            sb.append(" (Sign: ").append(String.join(" | ", signLines)).append(")");
        }
        if (containerContents != null) {
            int itemCount = 0;
            for (ItemStack item : containerContents) {
                if (item != null && item.getType() != Material.AIR)
                    itemCount++;
            }
            sb.append(" (Container: ").append(itemCount).append(" items)");
        }
        if (jukeboxRecord != null) {
            sb.append(" (Jukebox: ").append(jukeboxRecord.getType()).append(")");
        }
        if (skullOwner != null) {
            sb.append(" (Skull: ").append(skullOwner);
            if (skullData != null) {
                sb.append(" [Custom]");
            }
            sb.append(")");
        }
        if (flowerPotItem != null) {
            sb.append(" (Pot: ").append(flowerPotItem).append(")");
        }

        return sb.toString();
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("material", material.name());
        map.put("blockDataString", blockDataString);

        if (bannerPatterns != null) {
            map.put("bannerPatterns", bannerPatterns);
        }
        if (signLines != null) {
            map.put("signLines", Arrays.asList(signLines));
        }
        if (signBackLines != null) {
            map.put("signBackLines", Arrays.asList(signBackLines));
        }
        if (signGlowing) {
            map.put("signGlowing", true);
        }
        if (signBackGlowing) {
            map.put("signBackGlowing", true);
        }
        if (signColor != null) {
            map.put("signColor", signColor);
        }
        if (signBackColor != null) {
            map.put("signBackColor", signBackColor);
        }
        if (containerContents != null) {
            map.put("containerContentsBase64", containerContentsBase64);
            map.put("containerContentsDebug", containerContentsDebug);
        }
        if (jukeboxRecord != null) {
            map.put("jukeboxRecord", jukeboxRecord);
        }
        if (skullOwner != null) {
            map.put("skullOwner", skullOwner);
        }
        if (skullData != null) {
            map.put("skullData", skullData);
        }
        if (flowerPotItem != null) {
            map.put("flowerPotItem", flowerPotItem.name());
        }
        
        // Lectern
        if (lecternBook != null) {
            map.put("lecternBook", lecternBook);
            map.put("lecternPage", lecternPage);
        }
        
        // Campfire
        if (campfireItems != null) {
            map.put("campfireItemsBase64", serializeItemStackArrayToBase64(campfireItems));
            if (campfireCookTimes != null) {
                map.put("campfireCookTimes", Arrays.stream(campfireCookTimes).boxed().toList());
            }
            if (campfireCookTimesTotal != null) {
                map.put("campfireCookTimesTotal", Arrays.stream(campfireCookTimesTotal).boxed().toList());
            }
        }
        
        // Beehive
        if (beehiveHoneyLevel > 0 || beehiveBeeCount > 0) {
            map.put("beehiveHoneyLevel", beehiveHoneyLevel);
            map.put("beehiveBeeCount", beehiveBeeCount);
        }
        
        // Brewing Stand
        if (brewingFuel > 0 || brewingTime > 0) {
            map.put("brewingFuel", brewingFuel);
            map.put("brewingTime", brewingTime);
        }
        
        // Spawner
        if (spawnerEntityType != null) {
            map.put("spawnerEntityType", spawnerEntityType);
            map.put("spawnerDelay", spawnerDelay);
            map.put("spawnerMinDelay", spawnerMinDelay);
            map.put("spawnerMaxDelay", spawnerMaxDelay);
            map.put("spawnerSpawnCount", spawnerSpawnCount);
            map.put("spawnerMaxNearbyEntities", spawnerMaxNearbyEntities);
            map.put("spawnerRequiredPlayerRange", spawnerRequiredPlayerRange);
            map.put("spawnerSpawnRange", spawnerSpawnRange);
        }
        
        // Command Block
        if (commandBlockCommand != null) {
            map.put("commandBlockCommand", commandBlockCommand);
            if (commandBlockName != null) {
                map.put("commandBlockName", commandBlockName);
            }
            map.put("commandBlockTrackOutput", commandBlockTrackOutput);
        }
        
        // Decorated Pot
        if (decoratedPotSherds != null) {
            map.put("decoratedPotSherds", Arrays.asList(decoratedPotSherds));
        }
        
        // Structure Block
        if (structureBlockName != null) {
            map.put("structureBlockMode", structureBlockMode);
            map.put("structureBlockName", structureBlockName);
            if (structureBlockAuthor != null) {
                map.put("structureBlockAuthor", structureBlockAuthor);
            }
            if (structureBlockPosition != null) {
                map.put("structureBlockPosition", Arrays.stream(structureBlockPosition).boxed().toList());
            }
            if (structureBlockSize != null) {
                map.put("structureBlockSize", Arrays.stream(structureBlockSize).boxed().toList());
            }
            if (structureBlockMirror != null) {
                map.put("structureBlockMirror", structureBlockMirror);
            }
            if (structureBlockRotation != null) {
                map.put("structureBlockRotation", structureBlockRotation);
            }
            map.put("structureBlockIntegrity", structureBlockIntegrity);
            map.put("structureBlockSeed", structureBlockSeed);
            map.put("structureBlockIgnoreEntities", structureBlockIgnoreEntities);
            map.put("structureBlockShowBoundingBox", structureBlockShowBoundingBox);
            map.put("structureBlockShowAir", structureBlockShowAir);
        }
        
        // Chiseled Bookshelf
        if (chiseledBookshelfBooks != null) {
            map.put("chiseledBookshelfBooksBase64", serializeItemStackArrayToBase64(chiseledBookshelfBooks));
        }
        
        // End Gateway
        if (endGatewayExitLocation != null) {
            Map<String, Object> exitLoc = new HashMap<>();
            exitLoc.put("world", endGatewayExitLocation.getWorld() != null ? endGatewayExitLocation.getWorld().getName() : "");
            exitLoc.put("x", endGatewayExitLocation.getX());
            exitLoc.put("y", endGatewayExitLocation.getY());
            exitLoc.put("z", endGatewayExitLocation.getZ());
            map.put("endGatewayExitLocation", exitLoc);
            map.put("endGatewayExactTeleport", endGatewayExactTeleport);
            map.put("endGatewayAge", endGatewayAge);
        }
        
        // Comparator
        if (comparatorMode != null) {
            map.put("comparatorMode", comparatorMode);
        }
        
        // Note Block
        if (noteBlockInstrument != null) {
            map.put("noteBlockInstrument", noteBlockInstrument);
            map.put("noteBlockNote", noteBlockNote);
        }

        return map;
    }

    public static BlockInfo deserialize(Map<String, Object> map) {
        Material material = Material.valueOf((String) map.get("material"));
        String blockDataString = (String) map.get("blockDataString");
        BlockData blockData = Bukkit.createBlockData(blockDataString);

        BlockInfo blockInfo = new BlockInfo(material, blockData);

        if (map.containsKey("bannerPatterns")) {
            @SuppressWarnings("unchecked")
            List<Pattern> patterns = (List<Pattern>) map.get("bannerPatterns");
            blockInfo.setBannerPatterns(patterns);
        }

        if (map.containsKey("signLines")) {
            @SuppressWarnings("unchecked")
            List<String> lines = (List<String>) map.get("signLines");
            blockInfo.setSignLines(lines.toArray(new String[0]));
        }
        
        if (map.containsKey("signBackLines")) {
            @SuppressWarnings("unchecked")
            List<String> lines = (List<String>) map.get("signBackLines");
            blockInfo.setSignBackLines(lines.toArray(new String[0]));
        }
        
        if (map.containsKey("signGlowing")) {
            blockInfo.setSignGlowing((Boolean) map.get("signGlowing"));
        }
        
        if (map.containsKey("signBackGlowing")) {
            blockInfo.setSignBackGlowing((Boolean) map.get("signBackGlowing"));
        }
        
        if (map.containsKey("signColor")) {
            blockInfo.setSignColor((String) map.get("signColor"));
        }
        
        if (map.containsKey("signBackColor")) {
            blockInfo.setSignBackColor((String) map.get("signBackColor"));
        }

        if (map.containsKey("containerContentsBase64")) {
            String base64Data = (String) map.get("containerContentsBase64");
            ItemStack[] contents = deserializeItemStackArrayFromBase64(base64Data);
            blockInfo.setContainerContents(contents);
        }

        if (map.containsKey("jukeboxRecord")) {
            blockInfo.setJukeboxRecord((ItemStack) map.get("jukeboxRecord"));
        }

        if (map.containsKey("skullOwner")) {
            blockInfo.setSkullOwner((String) map.get("skullOwner"));
        }

        if (map.containsKey("skullData")) {
            blockInfo.setSkullData((String) map.get("skullData"));
        }

        if (map.containsKey("flowerPotItem")) {
            blockInfo.setFlowerPotItem(Material.valueOf((String) map.get("flowerPotItem")));
        }
        
        // Lectern
        if (map.containsKey("lecternBook")) {
            blockInfo.setLecternBook((ItemStack) map.get("lecternBook"));
            blockInfo.setLecternPage(((Number) map.get("lecternPage")).intValue());
        }
        
        // Campfire
        if (map.containsKey("campfireItemsBase64")) {
            String base64Data = (String) map.get("campfireItemsBase64");
            blockInfo.setCampfireItems(deserializeItemStackArrayFromBase64(base64Data));
            
            if (map.containsKey("campfireCookTimes")) {
                @SuppressWarnings("unchecked")
                List<Number> times = (List<Number>) map.get("campfireCookTimes");
                blockInfo.setCampfireCookTimes(times.stream().mapToInt(Number::intValue).toArray());
            }
            if (map.containsKey("campfireCookTimesTotal")) {
                @SuppressWarnings("unchecked")
                List<Number> times = (List<Number>) map.get("campfireCookTimesTotal");
                blockInfo.setCampfireCookTimesTotal(times.stream().mapToInt(Number::intValue).toArray());
            }
        }
        
        // Beehive
        if (map.containsKey("beehiveHoneyLevel")) {
            blockInfo.setBeehiveHoneyLevel(((Number) map.get("beehiveHoneyLevel")).intValue());
            blockInfo.setBeehiveBeeCount(((Number) map.get("beehiveBeeCount")).intValue());
        }
        
        // Brewing Stand
        if (map.containsKey("brewingFuel")) {
            blockInfo.setBrewingFuel(((Number) map.get("brewingFuel")).intValue());
            blockInfo.setBrewingTime(((Number) map.get("brewingTime")).intValue());
        }
        
        // Spawner
        if (map.containsKey("spawnerEntityType")) {
            blockInfo.setSpawnerEntityType((String) map.get("spawnerEntityType"));
            blockInfo.setSpawnerDelay(((Number) map.get("spawnerDelay")).intValue());
            blockInfo.setSpawnerMinDelay(((Number) map.get("spawnerMinDelay")).intValue());
            blockInfo.setSpawnerMaxDelay(((Number) map.get("spawnerMaxDelay")).intValue());
            blockInfo.setSpawnerSpawnCount(((Number) map.get("spawnerSpawnCount")).intValue());
            blockInfo.setSpawnerMaxNearbyEntities(((Number) map.get("spawnerMaxNearbyEntities")).intValue());
            blockInfo.setSpawnerRequiredPlayerRange(((Number) map.get("spawnerRequiredPlayerRange")).intValue());
            blockInfo.setSpawnerSpawnRange(((Number) map.get("spawnerSpawnRange")).intValue());
        }
        
        // Command Block
        if (map.containsKey("commandBlockCommand")) {
            blockInfo.setCommandBlockCommand((String) map.get("commandBlockCommand"));
            if (map.containsKey("commandBlockName")) {
                blockInfo.setCommandBlockName((String) map.get("commandBlockName"));
            }
            blockInfo.setCommandBlockTrackOutput((Boolean) map.get("commandBlockTrackOutput"));
        }
        
        // Decorated Pot
        if (map.containsKey("decoratedPotSherds")) {
            @SuppressWarnings("unchecked")
            List<String> sherds = (List<String>) map.get("decoratedPotSherds");
            blockInfo.setDecoratedPotSherds(sherds.toArray(new String[0]));
        }
        
        // Structure Block
        if (map.containsKey("structureBlockName")) {
            blockInfo.setStructureBlockMode((String) map.get("structureBlockMode"));
            blockInfo.setStructureBlockName((String) map.get("structureBlockName"));
            if (map.containsKey("structureBlockAuthor")) {
                blockInfo.setStructureBlockAuthor((String) map.get("structureBlockAuthor"));
            }
            if (map.containsKey("structureBlockPosition")) {
                @SuppressWarnings("unchecked")
                List<Number> pos = (List<Number>) map.get("structureBlockPosition");
                blockInfo.setStructureBlockPosition(pos.stream().mapToInt(Number::intValue).toArray());
            }
            if (map.containsKey("structureBlockSize")) {
                @SuppressWarnings("unchecked")
                List<Number> size = (List<Number>) map.get("structureBlockSize");
                blockInfo.setStructureBlockSize(size.stream().mapToInt(Number::intValue).toArray());
            }
            if (map.containsKey("structureBlockMirror")) {
                blockInfo.setStructureBlockMirror((String) map.get("structureBlockMirror"));
            }
            if (map.containsKey("structureBlockRotation")) {
                blockInfo.setStructureBlockRotation((String) map.get("structureBlockRotation"));
            }
            blockInfo.setStructureBlockIntegrity(((Number) map.get("structureBlockIntegrity")).floatValue());
            blockInfo.setStructureBlockSeed(((Number) map.get("structureBlockSeed")).longValue());
            blockInfo.setStructureBlockIgnoreEntities((Boolean) map.get("structureBlockIgnoreEntities"));
            blockInfo.setStructureBlockShowBoundingBox((Boolean) map.get("structureBlockShowBoundingBox"));
            blockInfo.setStructureBlockShowAir((Boolean) map.get("structureBlockShowAir"));
        }
        
        // Chiseled Bookshelf
        if (map.containsKey("chiseledBookshelfBooksBase64")) {
            String base64Data = (String) map.get("chiseledBookshelfBooksBase64");
            blockInfo.setChiseledBookshelfBooks(deserializeItemStackArrayFromBase64(base64Data));
        }
        
        // End Gateway
        if (map.containsKey("endGatewayExitLocation")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> exitLoc = (Map<String, Object>) map.get("endGatewayExitLocation");
            String worldName = (String) exitLoc.get("world");
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                Location loc = new Location(world,
                    ((Number) exitLoc.get("x")).doubleValue(),
                    ((Number) exitLoc.get("y")).doubleValue(),
                    ((Number) exitLoc.get("z")).doubleValue());
                blockInfo.setEndGatewayExitLocation(loc);
            }
            blockInfo.setEndGatewayExactTeleport((Boolean) map.get("endGatewayExactTeleport"));
            blockInfo.setEndGatewayAge(((Number) map.get("endGatewayAge")).longValue());
        }
        
        // Comparator
        if (map.containsKey("comparatorMode")) {
            blockInfo.setComparatorMode((String) map.get("comparatorMode"));
        }
        
        // Note Block
        if (map.containsKey("noteBlockInstrument")) {
            blockInfo.setNoteBlockInstrument((String) map.get("noteBlockInstrument"));
            blockInfo.setNoteBlockNote(((Number) map.get("noteBlockNote")).intValue());
        }

        return blockInfo;
    }

    static {
        org.bukkit.configuration.serialization.ConfigurationSerialization.registerClass(BlockInfo.class);
        org.bukkit.configuration.serialization.ConfigurationSerialization.registerClass(BlockInfo.class, "BlockInfo");
    }
}

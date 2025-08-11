package arearewind.data;

import org.bukkit.*;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.*;

@SerializableAs("BlockInfo")
public class BlockInfo implements Serializable, ConfigurationSerializable {

    private static final long serialVersionUID = 1L;

    private Material material;
    private String blockDataString;
    private List<Pattern> bannerPatterns;
    private String[] signLines;
    private ItemStack[] containerContents;
    private ItemStack jukeboxRecord;
    private String skullOwner;
    private Material flowerPotItem;
    private String containerContentsDebug;
    private String containerContentsBase64;

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

    private ItemStack[] cloneItemStackArray(ItemStack[] original) {
        if (original == null) return null;

        ItemStack[] clone = new ItemStack[original.length];
        for (int i = 0; i < original.length; i++) {
            clone[i] = original[i] != null ? original[i].clone() : null;
        }
        return clone;
    }

    public static String serializeItemStackArrayToBase64(ItemStack[] items) {
        if (items == null) return null;

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
        if (data == null || data.isEmpty()) return null;

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
        if (!(obj instanceof BlockInfo)) return false;
        BlockInfo other = (BlockInfo) obj;
        return material == other.material &&
                blockDataString.equals(other.blockDataString) &&
                Objects.equals(bannerPatterns, other.bannerPatterns) &&
                Arrays.equals(signLines, other.signLines) &&
                Arrays.deepEquals(containerContents, other.containerContents) &&
                Objects.equals(jukeboxRecord, other.jukeboxRecord) &&
                Objects.equals(skullOwner, other.skullOwner) &&
                Objects.equals(flowerPotItem, other.flowerPotItem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(material, blockDataString, bannerPatterns,
                Arrays.hashCode(signLines), Arrays.deepHashCode(containerContents),
                jukeboxRecord, skullOwner, flowerPotItem);
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
                if (item != null && item.getType() != Material.AIR) itemCount++;
            }
            sb.append(" (Container: ").append(itemCount).append(" items)");
        }
        if (jukeboxRecord != null) {
            sb.append(" (Jukebox: ").append(jukeboxRecord.getType()).append(")");
        }
        if (skullOwner != null) {
            sb.append(" (Skull: ").append(skullOwner).append(")");
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
        if (flowerPotItem != null) {
            map.put("flowerPotItem", flowerPotItem.name());
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

        if (map.containsKey("flowerPotItem")) {
            blockInfo.setFlowerPotItem(Material.valueOf((String) map.get("flowerPotItem")));
        }

        return blockInfo;
    }

    static {
        org.bukkit.configuration.serialization.ConfigurationSerialization.registerClass(BlockInfo.class);
        org.bukkit.configuration.serialization.ConfigurationSerialization.registerClass(BlockInfo.class, "BlockInfo");
    }
}
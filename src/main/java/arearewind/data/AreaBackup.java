package arearewind.data;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * One snapshot of an area. Its blocks and entities can be unloaded while the
 * backup is idle: they are then read back from the backup file the next time
 * {@link #getBlocks()} or {@link #getEntities()} is called. Metadata (id, time,
 * icon, counts) always stays in memory.
 */
@SerializableAs("AreaBackup")
public class AreaBackup implements ConfigurationSerializable, Serializable {

    /** Reads a backup's blocks and entities back from storage. */
    @FunctionalInterface
    public interface Loader {
        /** The stored backup, or null when it cannot be read. */
        AreaBackup load();
    }

    private String id;
    private LocalDateTime timestamp;
    private Map<String, BlockInfo> blocks;
    private Map<String, Object> entities;
    private ItemStack iconItem;
    private boolean hidden; // For beforeRestore backups that shouldn't appear in lists
    private transient int nonAirBlockCount = -1;
    /** Counts kept for while the blocks and entities are unloaded. */
    private transient int blockCount = -1;
    private transient int entityCount = -1;
    private transient Loader loader;
    /** True once the blocks and entities are on disk, so they can be unloaded. */
    private transient boolean persisted;
    private transient volatile long lastUsed = System.currentTimeMillis();

    public AreaBackup(LocalDateTime timestamp, Map<String, BlockInfo> blocks) {
        this.id = generateUniqueId();
        this.timestamp = timestamp;
        this.blocks = blocks;
        this.entities = new HashMap<>();
        this.iconItem = new ItemStack(Material.CHEST); // Default backup icon
        this.hidden = false;
    }

    public AreaBackup(LocalDateTime timestamp, Map<String, BlockInfo> blocks, Map<String, Object> entities) {
        this.id = generateUniqueId();
        this.timestamp = timestamp;
        this.blocks = blocks;
        this.entities = entities != null ? entities : new HashMap<>();
        this.iconItem = new ItemStack(Material.CHEST); // Default backup icon
        this.hidden = false;
    }

    public AreaBackup(String id, LocalDateTime timestamp, Map<String, BlockInfo> blocks) {
        this.id = id;
        this.timestamp = timestamp;
        this.blocks = blocks;
        this.entities = new HashMap<>();
        this.iconItem = new ItemStack(Material.CHEST); // Default backup icon
        this.hidden = false;
    }

    public AreaBackup(String id, LocalDateTime timestamp, Map<String, BlockInfo> blocks, Map<String, Object> entities) {
        this.id = id;
        this.timestamp = timestamp;
        this.blocks = blocks;
        this.entities = entities != null ? entities : new HashMap<>();
        this.iconItem = new ItemStack(Material.CHEST); // Default backup icon
        this.hidden = false;
    }

    public AreaBackup(String id, LocalDateTime timestamp, Map<String, BlockInfo> blocks, Map<String, Object> entities,
            boolean hidden) {
        this.id = id;
        this.timestamp = timestamp;
        this.blocks = blocks;
        this.entities = entities != null ? entities : new HashMap<>();
        this.iconItem = new ItemStack(Material.CHEST); // Default backup icon
        this.hidden = hidden;
    }

    private String generateUniqueId() {
        // Use a combination of timestamp and UUID for uniqueness
        String timestampStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return timestampStr + "-" + uuid;
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * A backup known from its index file only: blocks and entities load on first
     * use through {@code loader}.
     */
    public static AreaBackup unloaded(String id, LocalDateTime timestamp, int blockCount, int nonAirBlockCount,
            int entityCount) {
        AreaBackup backup = new AreaBackup(id, timestamp, null, null);
        backup.entities = null;
        backup.blockCount = blockCount;
        backup.nonAirBlockCount = nonAirBlockCount;
        backup.entityCount = entityCount;
        backup.persisted = true;
        return backup;
    }

    public synchronized Map<String, BlockInfo> getBlocks() {
        load();
        return blocks;
    }

    /** Reads blocks and entities back if they were unloaded. */
    private synchronized void load() {
        lastUsed = System.currentTimeMillis();
        if (blocks != null) {
            return;
        }
        AreaBackup stored = loader == null ? null : loader.load();
        if (stored == null || stored.blocks == null) {
            throw new IllegalStateException("Backup " + id + " could not be read back from its file");
        }
        blocks = stored.blocks;
        entities = stored.entities != null ? stored.entities : new HashMap<>();
    }

    public synchronized boolean isLoaded() {
        return blocks != null;
    }

    public void setLoader(Loader loader) {
        this.loader = loader;
    }

    public boolean isPersisted() {
        return persisted;
    }

    public void markPersisted() {
        persisted = true;
    }

    public long getLastUsed() {
        return lastUsed;
    }

    /**
     * Drops blocks and entities from memory, keeping their counts. Only possible
     * once they are on disk and there is a loader to read them back. Code that
     * holds the block map (a restore in progress) keeps its own reference.
     */
    public synchronized boolean unload() {
        if (blocks == null || loader == null || !persisted) {
            return false;
        }
        getNonAirBlockCount();
        blockCount = blocks.size();
        entityCount = entities != null ? entities.size() : 0;
        blocks = null;
        entities = null;
        return true;
    }

    public Map<String, BlockInfo> getBlocksNonAirOnly() {
        Map<String, BlockInfo> nonAirBlocks = new HashMap<>();
        for (Map.Entry<String, BlockInfo> entry : getBlocks().entrySet()) {
            Material material = entry.getValue().getMaterial();
            if (material != null && material != Material.AIR) {
                nonAirBlocks.put(entry.getKey(), entry.getValue());
            }
        }
        return nonAirBlocks;
    }

    public synchronized Map<String, Object> getEntities() {
        load();
        return entities;
    }

    public Material getIcon() {
        return iconItem != null ? iconItem.getType() : Material.CHEST;
    }

    public ItemStack getIconItem() {
        return iconItem != null ? iconItem.clone() : new ItemStack(Material.CHEST);
    }

    public void setIcon(Material icon) {
        this.iconItem = new ItemStack(icon != null ? icon : Material.CHEST);
    }

    public void setIconItem(ItemStack iconItem) {
        this.iconItem = iconItem != null ? iconItem.clone() : new ItemStack(Material.CHEST);
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public synchronized int getBlockCount() {
        return blocks != null ? blocks.size() : blockCount;
    }

    /** Blocks that are not air, counted once; for lists that show a count. */
    public synchronized int getNonAirBlockCount() {
        if (nonAirBlockCount < 0) {
            int count = 0;
            for (BlockInfo info : getBlocks().values()) {
                Material material = info.getMaterial();
                if (material != null && material != Material.AIR) {
                    count++;
                }
            }
            nonAirBlockCount = count;
        }
        return nonAirBlockCount;
    }

    public synchronized int getEntityCount() {
        return entities != null ? entities.size() : entityCount;
    }

    public boolean containsPosition(String position) {
        return getBlocks().containsKey(position);
    }

    public BlockInfo getBlockAt(String position) {
        return getBlocks().get(position);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("timestamp", timestamp != null ? timestamp.toString() : null);
        map.put("blocks", getBlocks());
        map.put("entities", getEntities());
        // Serialize the icon ItemStack
        if (iconItem != null) {
            map.put("iconItem", iconItem.serialize());
        } else {
            map.put("iconItem", new ItemStack(Material.CHEST).serialize());
        }
        map.put("hidden", hidden);
        return map;
    }

    @SuppressWarnings("unchecked")
    public static AreaBackup deserialize(Map<String, Object> map) {
        String id = (String) map.get("id");
        LocalDateTime timestamp = map.get("timestamp") != null ? LocalDateTime.parse((String) map.get("timestamp"))
                : null;
        Map<String, BlockInfo> blocks = (Map<String, BlockInfo>) map.get("blocks");
        Map<String, Object> entities = (Map<String, Object>) map.getOrDefault("entities", new HashMap<>());
        boolean hidden = (Boolean) map.getOrDefault("hidden", false);

        AreaBackup backup = new AreaBackup(id, timestamp, blocks, entities, hidden);

        // Handle icon - support both old Material format and new ItemStack format
        if (map.containsKey("iconItem")) {
            // New ItemStack format
            Map<String, Object> iconData = (Map<String, Object>) map.get("iconItem");
            if (iconData != null) {
                try {
                    ItemStack iconItem = ItemStack.deserialize(iconData);
                    backup.setIconItem(iconItem);
                } catch (Exception e) {
                    backup.setIcon(Material.CHEST); // Fallback to default
                }
            }
        } else if (map.containsKey("icon")) {
            // Legacy Material format
            String iconName = (String) map.get("icon");
            if (iconName != null) {
                try {
                    backup.setIcon(Material.valueOf(iconName));
                } catch (IllegalArgumentException e) {
                    backup.setIcon(Material.CHEST); // Fallback to default
                }
            }
        }

        return backup;
    }

    static {
        org.bukkit.configuration.serialization.ConfigurationSerialization.registerClass(AreaBackup.class);
        org.bukkit.configuration.serialization.ConfigurationSerialization.registerClass(AreaBackup.class, "AreaBackup");
    }
}

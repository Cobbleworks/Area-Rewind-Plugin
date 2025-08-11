package arearewind.data;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SerializableAs("AreaBackup")
public class AreaBackup implements ConfigurationSerializable, Serializable {
    private String id;
    private LocalDateTime timestamp;
    private Map<String, BlockInfo> blocks;
    private Map<String, Object> entities;

    public AreaBackup(LocalDateTime timestamp, Map<String, BlockInfo> blocks) {
        this.id = generateUniqueId();
        this.timestamp = timestamp;
        this.blocks = blocks;
        this.entities = new HashMap<>();
    }

    public AreaBackup(LocalDateTime timestamp, Map<String, BlockInfo> blocks, Map<String, Object> entities) {
        this.id = generateUniqueId();
        this.timestamp = timestamp;
        this.blocks = blocks;
        this.entities = entities != null ? entities : new HashMap<>();
    }

    public AreaBackup(String id, LocalDateTime timestamp, Map<String, BlockInfo> blocks) {
        this.id = id;
        this.timestamp = timestamp;
        this.blocks = blocks;
        this.entities = new HashMap<>();
    }

    public AreaBackup(String id, LocalDateTime timestamp, Map<String, BlockInfo> blocks, Map<String, Object> entities) {
        this.id = id;
        this.timestamp = timestamp;
        this.blocks = blocks;
        this.entities = entities != null ? entities : new HashMap<>();
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

    public Map<String, BlockInfo> getBlocks() {
        return blocks;
    }

    public Map<String, Object> getEntities() {
        return entities;
    }

    public int getBlockCount() {
        return blocks.size();
    }

    public int getEntityCount() {
        return entities.size();
    }

    public boolean containsPosition(String position) {
        return blocks.containsKey(position);
    }

    public BlockInfo getBlockAt(String position) {
        return blocks.get(position);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("timestamp", timestamp != null ? timestamp.toString() : null);
        map.put("blocks", blocks);
        map.put("entities", entities);
        return map;
    }

    @SuppressWarnings("unchecked")
    public static AreaBackup deserialize(Map<String, Object> map) {
        String id = (String) map.get("id");
        LocalDateTime timestamp = map.get("timestamp") != null ? LocalDateTime.parse((String) map.get("timestamp"))
                : null;
        Map<String, BlockInfo> blocks = (Map<String, BlockInfo>) map.get("blocks");
        Map<String, Object> entities = (Map<String, Object>) map.getOrDefault("entities", new HashMap<>());
        return new AreaBackup(id, timestamp, blocks, entities);
    }

    static {
        org.bukkit.configuration.serialization.ConfigurationSerialization.registerClass(AreaBackup.class);
        org.bukkit.configuration.serialization.ConfigurationSerialization.registerClass(AreaBackup.class, "AreaBackup");
    }
}

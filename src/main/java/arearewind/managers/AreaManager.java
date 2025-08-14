package arearewind.managers;

import arearewind.data.ProtectedArea;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AreaManager {

    private final JavaPlugin plugin;
    private final FileManager fileManager;
    private final Map<String, ProtectedArea> protectedAreas;
    private final Map<UUID, Location> pos1Map;
    private final Map<UUID, Location> pos2Map;

    public AreaManager(JavaPlugin plugin, FileManager fileManager) {
        this.plugin = plugin;
        this.fileManager = fileManager;
        this.protectedAreas = new ConcurrentHashMap<>();
        this.pos1Map = new HashMap<>();
        this.pos2Map = new HashMap<>();
    }

    public void setPosition1(UUID playerId, Location location) {
        pos1Map.put(playerId, location);
    }

    public void setPosition2(UUID playerId, Location location) {
        pos2Map.put(playerId, location);
    }

    public Location getPosition1(UUID playerId) {
        return pos1Map.get(playerId);
    }

    public Location getPosition2(UUID playerId) {
        return pos2Map.get(playerId);
    }

    public boolean hasValidSelection(UUID playerId) {
        return pos1Map.containsKey(playerId) && pos2Map.containsKey(playerId);
    }

    public boolean createArea(String name, UUID ownerId) {
        if (protectedAreas.containsKey(name)) {
            return false;
        }

        if (!hasValidSelection(ownerId)) {
            return false;
        }

        Location pos1 = pos1Map.get(ownerId);
        Location pos2 = pos2Map.get(ownerId);

        if (!pos1.getWorld().equals(pos2.getWorld())) {
            return false;
        }

        ProtectedArea area = new ProtectedArea(name, pos1, pos2, ownerId);
        protectedAreas.put(name, area);

        return true;
    }

    public boolean deleteArea(String name) {
        return protectedAreas.remove(name) != null;
    }

    public boolean renameArea(String oldName, String newName) {
        if (!protectedAreas.containsKey(oldName) || protectedAreas.containsKey(newName)) {
            return false;
        }

        ProtectedArea area = protectedAreas.remove(oldName);
        area.setName(newName);
        protectedAreas.put(newName, area);

        return true;
    }

    public ProtectedArea getArea(String name) {
        return protectedAreas.get(name);
    }

    public Map<String, ProtectedArea> getProtectedAreas() {
        return new HashMap<>(protectedAreas);
    }

    public List<ProtectedArea> getOwnedAreas(UUID playerId) {
        return protectedAreas.values().stream()
                .filter(area -> area.getOwner().equals(playerId))
                .collect(Collectors.toList());
    }

    public List<ProtectedArea> getTrustedAreas(UUID playerId) {
        return protectedAreas.values().stream()
                .filter(area -> area.getTrustedPlayers().contains(playerId))
                .collect(Collectors.toList());
    }

    public List<ProtectedArea> getAccessibleAreas(UUID playerId) {
        return protectedAreas.values().stream()
                .filter(area -> area.getOwner().equals(playerId) ||
                        area.getTrustedPlayers().contains(playerId))
                .collect(Collectors.toList());
    }

    public boolean contractArea(String areaName, String direction, int amount) {
        String oppositeDirection = getOppositeDirection(direction);
        return expandArea(areaName, oppositeDirection, amount);
    }

    public boolean expandArea(String areaName, String direction, int amount) {
        ProtectedArea area = protectedAreas.get(areaName);
        if (area == null)
            return false;

        if (amount <= 0)
            return false;

        Location pos1 = area.getPos1().clone();
        Location pos2 = area.getPos2().clone();

        Location min = area.getMin();
        Location max = area.getMax();

        switch (direction.toLowerCase()) {
            case "north":
                pos1.setZ(min.getZ() - amount);
                pos2.setZ(max.getZ());
                break;
            case "south":
                pos1.setZ(min.getZ());
                pos2.setZ(max.getZ() + amount);
                break;
            case "east":
                pos1.setX(min.getX());
                pos2.setX(max.getX() + amount);
                break;
            case "west":
                pos1.setX(min.getX() - amount);
                pos2.setX(max.getX());
                break;
            case "up":
                pos1.setY(min.getY());
                pos2.setY(max.getY() + amount);
                break;
            case "down":
                pos1.setY(min.getY() - amount);
                pos2.setY(max.getY());
                break;
            default:
                return false;
        }

        ProtectedArea tempArea = new ProtectedArea("temp", pos1, pos2, area.getOwner());
        if (tempArea.getSize() > 2000000) {
            return false;
        }

        area.updatePositions(pos1, pos2);
        return true;
    }

    private String getOppositeDirection(String direction) {
        switch (direction.toLowerCase()) {
            case "north":
                return "south";
            case "south":
                return "north";
            case "east":
                return "west";
            case "west":
                return "east";
            case "up":
                return "down";
            case "down":
                return "up";
            default:
                return direction;
        }
    }

    public boolean areaExists(String name) {
        return protectedAreas.containsKey(name);
    }

    public List<String> getAreaNames(String filter) {
        return protectedAreas.keySet().stream()
                .filter(name -> filter == null || name.toLowerCase().contains(filter.toLowerCase()))
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> getAllAreaNames() {
        return getAreaNames(null);
    }

    public List<ProtectedArea> getAreasAtLocation(Location location) {
        return protectedAreas.values().stream()
                .filter(area -> area.contains(location))
                .collect(Collectors.toList());
    }

    public void loadAreas() {
        File areasFile = fileManager.getAreasFile();
        if (!areasFile.exists()) {
            plugin.getLogger().info("No areas file found, starting fresh");
            return;
        }

        try {
            FileConfiguration areasConfig = YamlConfiguration.loadConfiguration(areasFile);
            int loadedCount = 0;

            for (String key : areasConfig.getKeys(false)) {
                try {
                    String worldName = areasConfig.getString(key + ".world");
                    World world = Bukkit.getWorld(worldName);
                    if (world == null) {
                        plugin.getLogger().warning("World '" + worldName + "' not found for area '" + key + "'");
                        continue;
                    }

                    double x1 = areasConfig.getDouble(key + ".pos1.x");
                    double y1 = areasConfig.getDouble(key + ".pos1.y");
                    double z1 = areasConfig.getDouble(key + ".pos1.z");

                    double x2 = areasConfig.getDouble(key + ".pos2.x");
                    double y2 = areasConfig.getDouble(key + ".pos2.y");
                    double z2 = areasConfig.getDouble(key + ".pos2.z");

                    Location pos1 = new Location(world, x1, y1, z1);
                    Location pos2 = new Location(world, x2, y2, z2);

                    String ownerString = areasConfig.getString(key + ".owner");
                    UUID owner = UUID.fromString(ownerString);

                    ProtectedArea area = new ProtectedArea(key, pos1, pos2, owner);

                    List<String> trustedList = areasConfig.getStringList(key + ".trusted");
                    for (String trustedString : trustedList) {
                        try {
                            UUID trustedUUID = UUID.fromString(trustedString);
                            area.addTrustedPlayer(trustedUUID);
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger()
                                    .warning("Invalid trusted player UUID in area '" + key + "': " + trustedString);
                        }
                    }

                    // Load custom restore speed
                    if (areasConfig.contains(key + ".customRestoreSpeed")) {
                        Integer customSpeed = areasConfig.getInt(key + ".customRestoreSpeed");
                        if (customSpeed > 0) {
                            area.setCustomRestoreSpeed(customSpeed);
                        }
                    }

                    // Load icon - support Base64 format, ItemStack format, and legacy Material
                    // format
                    plugin.getLogger().info("Loading icon for area '" + key + "'...");
                    if (areasConfig.contains(key + ".iconItem-base64")) {
                        // Base64 format (for complex player heads)
                        plugin.getLogger().info("Found Base64 icon data for area '" + key + "'");
                        String base64Data = areasConfig.getString(key + ".iconItem-base64");
                        if (base64Data != null) {
                            try {
                                ItemStack iconItem = loadItemStackFromBase64(base64Data);
                                if (iconItem != null) {
                                    area.setIconItem(iconItem);
                                    plugin.getLogger().info("Successfully loaded Base64 icon for area '" + key + "': "
                                            + iconItem.getType());
                                } else {
                                    plugin.getLogger().warning(
                                            "Failed to load Base64 icon for area '" + key + "', using default");
                                    area.setIcon(org.bukkit.Material.GRASS_BLOCK);
                                }
                            } catch (Exception e) {
                                plugin.getLogger()
                                        .warning("Error loading Base64 icon for area '" + key + "': " + e.getMessage());
                                area.setIcon(org.bukkit.Material.GRASS_BLOCK);
                            }
                        }
                    } else if (areasConfig.contains(key + ".iconItem")) {
                        // New ItemStack format
                        org.bukkit.configuration.ConfigurationSection iconSection = areasConfig
                                .getConfigurationSection(key + ".iconItem");
                        if (iconSection != null) {
                            try {
                                // Convert ConfigurationSection to Map for ItemStack deserialization
                                Map<String, Object> iconData = new HashMap<>();
                                for (String iconKey : iconSection.getKeys(true)) {
                                    iconData.put(iconKey, iconSection.get(iconKey));
                                }

                                // Debug logging
                                plugin.getLogger().info("Icon data for area '" + key + "': " + iconData);

                                ItemStack iconItem = ItemStack.deserialize(iconData);
                                area.setIconItem(iconItem);

                                // Check if it's a player head and log additional details
                                if (iconItem.getType() == Material.PLAYER_HEAD) {
                                    plugin.getLogger().info("Player head detected for area '" + key + "'");
                                    if (iconItem.hasItemMeta()) {
                                        plugin.getLogger().info("ItemMeta present: "
                                                + iconItem.getItemMeta().getClass().getSimpleName());
                                        if (iconItem.getItemMeta() instanceof org.bukkit.inventory.meta.SkullMeta) {
                                            org.bukkit.inventory.meta.SkullMeta skullMeta = (org.bukkit.inventory.meta.SkullMeta) iconItem
                                                    .getItemMeta();
                                            plugin.getLogger().info(
                                                    "SkullMeta - hasOwner: " + (skullMeta.getOwningPlayer() != null));
                                            // Check if skull has custom texture data
                                            plugin.getLogger().info("SkullMeta toString: " + skullMeta.toString());
                                        }
                                    }
                                }

                                plugin.getLogger()
                                        .info("Loaded custom icon for area '" + key + "': " + iconItem.getType());
                            } catch (Exception e) {
                                plugin.getLogger().warning(
                                        "Invalid icon item data for area '" + key + "', using default: "
                                                + e.getMessage());
                                area.setIcon(org.bukkit.Material.GRASS_BLOCK);
                            }
                        }
                    } else if (areasConfig.contains(key + ".icon")) {
                        // Legacy Material format
                        String iconName = areasConfig.getString(key + ".icon");
                        if (iconName != null) {
                            try {
                                area.setIcon(org.bukkit.Material.valueOf(iconName));
                                plugin.getLogger().info("Loaded legacy icon for area '" + key + "': " + iconName);
                            } catch (IllegalArgumentException e) {
                                plugin.getLogger().warning(
                                        "Invalid icon material '" + iconName + "' for area '" + key
                                                + "', using default");
                                area.setIcon(org.bukkit.Material.GRASS_BLOCK);
                            }
                        }
                    }

                    protectedAreas.put(key, area);

                    loadedCount++;
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load area '" + key + "': " + e.getMessage());
                }
            }

            plugin.getLogger().info("Loaded " + loadedCount + " protected areas");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load areas: " + e.getMessage());
        }
    }

    public void saveAreas() {
        try {
            FileConfiguration areasConfig = new YamlConfiguration();

            for (Map.Entry<String, ProtectedArea> entry : protectedAreas.entrySet()) {
                String name = entry.getKey();
                ProtectedArea area = entry.getValue();

                areasConfig.set(name + ".world", area.getPos1().getWorld().getName());
                areasConfig.set(name + ".pos1.x", area.getPos1().getX());
                areasConfig.set(name + ".pos1.y", area.getPos1().getY());
                areasConfig.set(name + ".pos1.z", area.getPos1().getZ());
                areasConfig.set(name + ".pos2.x", area.getPos2().getX());
                areasConfig.set(name + ".pos2.y", area.getPos2().getY());
                areasConfig.set(name + ".pos2.z", area.getPos2().getZ());
                areasConfig.set(name + ".owner", area.getOwner().toString());

                List<String> trustedList = area.getTrustedPlayers().stream()
                        .map(UUID::toString)
                        .collect(Collectors.toList());
                areasConfig.set(name + ".trusted", trustedList);

                // Save custom restore speed
                if (area.hasCustomRestoreSpeed()) {
                    areasConfig.set(name + ".customRestoreSpeed", area.getCustomRestoreSpeed());
                }

                // Save icon as ItemStack
                ItemStack iconItem = area.getIconItem();
                if (iconItem != null) {
                    try {
                        // Special handling for player heads to preserve texture data
                        if (iconItem.getType() == Material.PLAYER_HEAD) {
                            plugin.getLogger().info("Saving player head icon for area '" + name + "'");

                            // Use Base64 serialization for player heads to preserve all data
                            String base64Data = saveItemStackAsBase64(iconItem);
                            if (base64Data != null) {
                                areasConfig.set(name + ".iconItem-base64", base64Data);
                                plugin.getLogger().info("Saved player head as Base64 data for area '" + name + "'");
                            } else {
                                // Fallback to standard serialization
                                Map<String, Object> serializedIcon = iconItem.serialize();
                                areasConfig.set(name + ".iconItem", serializedIcon);
                                plugin.getLogger()
                                        .info("Fallback: Saved player head with standard serialization for area '"
                                                + name + "'");
                            }
                        } else {
                            // Standard serialization for non-player-head items
                            Map<String, Object> serializedIcon = iconItem.serialize();
                            areasConfig.set(name + ".iconItem", serializedIcon);
                        }
                    } catch (Exception e) {
                        plugin.getLogger()
                                .warning("Failed to serialize icon for area '" + name + "': " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            areasConfig.save(fileManager.getAreasFile());
            plugin.getLogger().info("Saved " + protectedAreas.size() + " protected areas");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save areas: " + e.getMessage());
        }
    }

    public String locationToString(Location loc) {
        if (loc == null)
            return "null";
        return loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
    }

    public void clearSelection(UUID playerId) {
        pos1Map.remove(playerId);
        pos2Map.remove(playerId);
    }

    public String getSelectionInfo(UUID playerId) {
        Location pos1 = pos1Map.get(playerId);
        Location pos2 = pos2Map.get(playerId);

        if (pos1 == null && pos2 == null) {
            return "No selection";
        }

        StringBuilder info = new StringBuilder();
        if (pos1 != null) {
            info.append("Pos1: ").append(locationToString(pos1));
        }
        if (pos2 != null) {
            if (info.length() > 0)
                info.append(" | ");
            info.append("Pos2: ").append(locationToString(pos2));
        }

        if (pos1 != null && pos2 != null) {
            ProtectedArea tempArea = new ProtectedArea("temp", pos1, pos2, playerId);
            info.append(" | Size: ").append(tempArea.getSize()).append(" blocks");
        }

        return info.toString();
    }

    public void addArea(ProtectedArea area) {
        if (area != null && area.getName() != null) {
            protectedAreas.put(area.getName(), area);
        }
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

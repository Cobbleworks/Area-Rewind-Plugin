package arearewind.data;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ProtectedArea implements Serializable {
    private String name;
    private Location pos1, pos2;
    private UUID owner;
    private Set<UUID> trustedPlayers;
    private ItemStack iconItem;
    private Integer customRestoreSpeed; // Custom restore speed (blocks per tick), null = use dynamic
    private long creationDate; // Timestamp in milliseconds since epoch

    public ProtectedArea(String name, Location pos1, Location pos2, UUID owner) {
        this.name = name;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.owner = owner;
        this.trustedPlayers = new HashSet<>();
        this.iconItem = new ItemStack(Material.GRASS_BLOCK); // Default icon
        this.customRestoreSpeed = null; // Use dynamic sizing by default
        this.creationDate = System.currentTimeMillis(); // Set creation date to now
    }

    /**
     * Get the creation date as a long timestamp (milliseconds since epoch)
     * for easy sorting and comparison
     */
    public long getCreationDate() {
        return creationDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getPos1() {
        return pos1;
    }

    public Location getPos2() {
        return pos2;
    }

    public UUID getOwner() {
        return owner;
    }

    public Set<UUID> getTrustedPlayers() {
        return trustedPlayers;
    }

    public Material getIcon() {
        return iconItem != null ? iconItem.getType() : Material.GRASS_BLOCK;
    }

    public ItemStack getIconItem() {
        return iconItem != null ? iconItem.clone() : new ItemStack(Material.GRASS_BLOCK);
    }

    public void setIcon(Material icon) {
        this.iconItem = new ItemStack(icon != null ? icon : Material.GRASS_BLOCK);
    }

    public void setIconItem(ItemStack iconItem) {
        this.iconItem = iconItem != null ? iconItem.clone() : new ItemStack(Material.GRASS_BLOCK);
    }

    public Integer getCustomRestoreSpeed() {
        return customRestoreSpeed;
    }

    public void setCustomRestoreSpeed(Integer speed) {
        // Validate speed range (10-1000 blocks per tick, or null for dynamic)
        if (speed != null && (speed < 10 || speed > 1000)) {
            throw new IllegalArgumentException("Custom restore speed must be between 10 and 1000 blocks per tick");
        }
        this.customRestoreSpeed = speed;
    }

    public boolean hasCustomRestoreSpeed() {
        return customRestoreSpeed != null;
    }

    public void updatePositions(Location pos1, Location pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    public void addTrustedPlayer(UUID player) {
        trustedPlayers.add(player);
    }

    public void removeTrustedPlayer(UUID player) {
        trustedPlayers.remove(player);
    }

    public Location getMin() {
        return new Location(pos1.getWorld(),
                Math.min(pos1.getBlockX(), pos2.getBlockX()),
                Math.min(pos1.getBlockY(), pos2.getBlockY()),
                Math.min(pos1.getBlockZ(), pos2.getBlockZ()));
    }

    public Location getMax() {
        return new Location(pos1.getWorld(),
                Math.max(pos1.getBlockX(), pos2.getBlockX()),
                Math.max(pos1.getBlockY(), pos2.getBlockY()),
                Math.max(pos1.getBlockZ(), pos2.getBlockZ()));
    }

    public Location getCenter() {
        Location min = getMin();
        Location max = getMax();
        return new Location(min.getWorld(),
                (min.getBlockX() + max.getBlockX()) / 2.0,
                (min.getBlockY() + max.getBlockY()) / 2.0,
                (min.getBlockZ() + max.getBlockZ()) / 2.0);
    }

    public int getSize() {
        Location min = getMin();
        Location max = getMax();
        return (max.getBlockX() - min.getBlockX() + 1) *
                (max.getBlockY() - min.getBlockY() + 1) *
                (max.getBlockZ() - min.getBlockZ() + 1);
    }

    public boolean contains(Location location) {
        if (!location.getWorld().equals(pos1.getWorld())) {
            return false;
        }

        Location min = getMin();
        Location max = getMax();
        return location.getBlockX() >= min.getBlockX() && location.getBlockX() <= max.getBlockX() &&
                location.getBlockY() >= min.getBlockY() && location.getBlockY() <= max.getBlockY() &&
                location.getBlockZ() >= min.getBlockZ() && location.getBlockZ() <= max.getBlockZ();
    }
}

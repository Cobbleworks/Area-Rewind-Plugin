package arearewind.data;

import org.bukkit.Location;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ProtectedArea implements Serializable {
    private String name;
    private Location pos1, pos2;
    private UUID owner;
    private Set<UUID> trustedPlayers;

    public ProtectedArea(String name, Location pos1, Location pos2, UUID owner) {
        this.name = name;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.owner = owner;
        this.trustedPlayers = new HashSet<>();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Location getPos1() { return pos1; }
    public Location getPos2() { return pos2; }
    public UUID getOwner() { return owner; }
    public Set<UUID> getTrustedPlayers() { return trustedPlayers; }

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

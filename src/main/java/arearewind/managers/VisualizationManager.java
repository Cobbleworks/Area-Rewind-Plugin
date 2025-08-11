package arearewind.managers;

import arearewind.data.ProtectedArea;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.*;

public class VisualizationManager {
    private final JavaPlugin plugin;
    private final AreaManager areaManager;
    private final Map<UUID, String> activeVisualizations = new HashMap<>();
    private final Map<UUID, VisualizationSettings> playerSettings = new HashMap<>();
    private final Map<UUID, BackupPreview> activeBackupPreviews = new HashMap<>();
    private final Map<UUID, Particle> playerParticleSettings = new HashMap<>();
    private BukkitTask visualizationTask;
    private static final Particle DEFAULT_PARTICLE = Particle.FLAME;
    private static final int DEFAULT_PARTICLE_COUNT = 1;
    private static final double DEFAULT_MAX_DISTANCE = 50.0;
    private static final int DEFAULT_UPDATE_INTERVAL = 20;

    public VisualizationManager(JavaPlugin plugin, AreaManager areaManager) {
        this.plugin = plugin;
        this.areaManager = areaManager;
    }

    public void startVisualizationTask() {
        if (visualizationTask != null) {
            visualizationTask.cancel();
        }
        visualizationTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateVisualizations();
                updateBackupPreviews();
            }
        }.runTaskTimer(plugin, 0L, DEFAULT_UPDATE_INTERVAL);
        plugin.getLogger().info("Visualization task started");
    }

    public void stopVisualizationTask() {
        if (visualizationTask != null) {
            visualizationTask.cancel();
            visualizationTask = null;
        }
    }

    private void updateVisualizations() {
        Iterator<Map.Entry<UUID, String>> iterator = activeVisualizations.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, String> entry = iterator.next();
            Player player = Bukkit.getPlayer(entry.getKey());
            String areaName = entry.getValue();
            if (player == null || !player.isOnline()) {
                iterator.remove();
                continue;
            }
            ProtectedArea area = areaManager.getArea(areaName);
            if (area == null) {
                iterator.remove();
                continue;
            }
            showAreaBoundaries(player, area);
        }
    }

    private void updateBackupPreviews() {
        Iterator<Map.Entry<UUID, BackupPreview>> iterator = activeBackupPreviews.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, BackupPreview> entry = iterator.next();
            Player player = Bukkit.getPlayer(entry.getKey());
            BackupPreview preview = entry.getValue();

            if (player == null || !player.isOnline()) {
                iterator.remove();
                continue;
            }

            showBackupPreviewParticles(player, preview);
        }
    }

    public void showAreaBoundaries(Player player, ProtectedArea area) {
        VisualizationSettings settings = getPlayerSettings(player);
        if (!area.getPos1().getWorld().equals(player.getWorld())) {
            return;
        }

        Location playerLoc = player.getLocation();
        switch (settings.getVisualizationType()) {
            case CORNERS:
                showCorners(player, area, settings.withHighVisibility());
                break;
            case EDGES:
                showEdges(player, area, settings.withHighVisibility());
                break;
            case FACES:
                showFaces(player, area, settings.withHighVisibility());
                break;
            case FULL:
                showFullOutline(player, area, settings.withHighVisibility());
                break;
        }
    }

    private void showCorners(Player player, ProtectedArea area, VisualizationSettings settings) {
        Location min = area.getMin();
        Location max = area.getMax();
        World world = min.getWorld();
        Location[] corners = {
                new Location(world, min.getX(), min.getY(), min.getZ()),
                new Location(world, max.getX(), min.getY(), min.getZ()),
                new Location(world, min.getX(), max.getY(), min.getZ()),
                new Location(world, max.getX(), max.getY(), min.getZ()),
                new Location(world, min.getX(), min.getY(), max.getZ()),
                new Location(world, max.getX(), min.getY(), max.getZ()),
                new Location(world, min.getX(), max.getY(), max.getZ()),
                new Location(world, max.getX(), max.getY(), max.getZ())
        };
        for (Location corner : corners) {
            if (corner.distance(player.getLocation()) <= settings.getMaxDistance()) {
                player.spawnParticle(settings.getParticle(), corner.add(0.5, 0.5, 0.5),
                        settings.getParticleCount());
            }
        }
    }

    private void showEdges(Player player, ProtectedArea area, VisualizationSettings settings) {
        Location min = area.getMin();
        Location max = area.getMax();
        World world = min.getWorld();
        int step = Math.max(1, (int) (area.getSize() / 1000));
        showEdgeLine(player, new Location(world, min.getX(), min.getY(), min.getZ()),
                new Location(world, max.getX(), min.getY(), min.getZ()), step, settings);
        showEdgeLine(player, new Location(world, min.getX(), min.getY(), min.getZ()),
                new Location(world, min.getX(), min.getY(), max.getZ()), step, settings);
        showEdgeLine(player, new Location(world, max.getX(), min.getY(), min.getZ()),
                new Location(world, max.getX(), min.getY(), max.getZ()), step, settings);
        showEdgeLine(player, new Location(world, min.getX(), min.getY(), max.getZ()),
                new Location(world, max.getX(), min.getY(), max.getZ()), step, settings);
        showEdgeLine(player, new Location(world, min.getX(), max.getY(), min.getZ()),
                new Location(world, max.getX(), max.getY(), min.getZ()), step, settings);
        showEdgeLine(player, new Location(world, min.getX(), max.getY(), min.getZ()),
                new Location(world, min.getX(), max.getY(), max.getZ()), step, settings);
        showEdgeLine(player, new Location(world, max.getX(), max.getY(), min.getZ()),
                new Location(world, max.getX(), max.getY(), max.getZ()), step, settings);
        showEdgeLine(player, new Location(world, min.getX(), max.getY(), max.getZ()),
                new Location(world, max.getX(), max.getY(), max.getZ()), step, settings);
        showEdgeLine(player, new Location(world, min.getX(), min.getY(), min.getZ()),
                new Location(world, min.getX(), max.getY(), min.getZ()), step, settings);
        showEdgeLine(player, new Location(world, max.getX(), min.getY(), min.getZ()),
                new Location(world, max.getX(), max.getY(), min.getZ()), step, settings);
        showEdgeLine(player, new Location(world, min.getX(), min.getY(), max.getZ()),
                new Location(world, min.getX(), max.getY(), max.getZ()), step, settings);
        showEdgeLine(player, new Location(world, max.getX(), min.getY(), max.getZ()),
                new Location(world, max.getX(), max.getY(), max.getZ()), step, settings);
    }

    private void showFaces(Player player, ProtectedArea area, VisualizationSettings settings) {
        Location min = area.getMin();
        Location max = area.getMax();
        World world = min.getWorld();
        int step = Math.max(2, (int) Math.sqrt(area.getSize() / 100));
        Location playerLoc = player.getLocation();
        Location center = area.getCenter();
        if (playerLoc.getX() < center.getX()) {
            showFaceParticles(player, min.getX(), min.getY(), max.getY(), min.getZ(), max.getZ(),
                    true, false, false, step, settings);
        }
        if (playerLoc.getX() > center.getX()) {
            showFaceParticles(player, max.getX(), min.getY(), max.getY(), min.getZ(), max.getZ(),
                    true, false, false, step, settings);
        }
        if (playerLoc.getZ() < center.getZ()) {
            showFaceParticles(player, min.getX(), max.getX(), min.getY(), max.getY(), min.getZ(),
                    false, false, true, step, settings);
        }
        if (playerLoc.getZ() > center.getZ()) {
            showFaceParticles(player, min.getX(), max.getX(), min.getY(), max.getY(), max.getZ(),
                    false, false, true, step, settings);
        }
    }

    private void showFullOutline(Player player, ProtectedArea area, VisualizationSettings settings) {
        VisualizationSettings denseSettings = new VisualizationSettings(settings);
        denseSettings.setParticleCount(settings.getParticleCount() * 2);
        showCorners(player, area, denseSettings);
        showEdges(player, area, denseSettings);
    }

    private void showEdgeLine(Player player, Location start, Location end, int step, VisualizationSettings settings) {
        double distance = start.distance(end);
        if (distance == 0) return;
        double stepSize = Math.max(1.0, distance / 20);
        for (double i = 0; i <= distance; i += stepSize) {
            double ratio = i / distance;
            Location point = start.clone().add(
                    (end.getX() - start.getX()) * ratio,
                    (end.getY() - start.getY()) * ratio,
                    (end.getZ() - start.getZ()) * ratio
            );
            if (point.distance(player.getLocation()) <= settings.getMaxDistance()) {
                player.spawnParticle(settings.getParticle(), point.add(0.5, 0.5, 0.5),
                        settings.getParticleCount());
            }
        }
    }

    private void showFaceParticles(Player player, double x1, double x2, double y1, double y2, double z,
                                   boolean fixedX, boolean fixedY, boolean fixedZ, int step, VisualizationSettings settings) {
        World world = player.getWorld();
        for (double x = Math.min(x1, x2); x <= Math.max(x1, x2); x += step) {
            for (double y = Math.min(y1, y2); y <= Math.max(y1, y2); y += step) {
                Location point = new Location(world, fixedX ? x1 : x, fixedY ? y1 : y, fixedZ ? z :
                        (fixedX ? z : (fixedY ? z : y)));
                if (point.distance(player.getLocation()) <= settings.getMaxDistance()) {
                    player.spawnParticle(settings.getParticle(), point.add(0.5, 0.5, 0.5),
                            settings.getParticleCount());
                }
            }
        }
    }

    public VisualizationSettings getPlayerSettings(Player player) {
        return playerSettings.computeIfAbsent(player.getUniqueId(),
                uuid -> new VisualizationSettings());
    }

    public void setPlayerSettings(Player player, VisualizationSettings settings) {
        playerSettings.put(player.getUniqueId(), settings);
    }

    public Set<UUID> getActiveViewers() {
        return new HashSet<>(activeVisualizations.keySet());
    }

    public void stopVisualizationsForArea(String areaName) {
        activeVisualizations.entrySet().removeIf(entry -> entry.getValue().equals(areaName));
    }

    public VisualizationSettings getDefaultSettings() {
        VisualizationSettings settings = new VisualizationSettings();
        settings.setParticle(Particle.END_ROD);
        return settings;
    }

    public boolean toggleBackupPreview(Player player, arearewind.data.AreaBackup backup, arearewind.data.ProtectedArea area) {
        UUID playerId = player.getUniqueId();

        if (activeBackupPreviews.containsKey(playerId)) {
            activeBackupPreviews.remove(playerId);
            player.sendMessage(ChatColor.RED + "Backup preview disabled!");
            return false;
        } else {
            if (backup == null || area == null) {
                player.sendMessage(ChatColor.RED + "Backup or area not found!");
                return false;
            }

            BackupPreview preview = new BackupPreview(backup, area);
            activeBackupPreviews.put(playerId, preview);
            player.sendMessage(ChatColor.GREEN + "Backup preview enabled! Use /rewind preview to disable.");
            return true;
        }
    }

    public boolean toggleBackupPreview(Player player) {
        UUID playerId = player.getUniqueId();

        if (activeBackupPreviews.containsKey(playerId)) {
            activeBackupPreviews.remove(playerId);
            player.sendMessage(ChatColor.RED + "Backup preview disabled!");
            return false;
        } else {
            player.sendMessage(ChatColor.YELLOW + "No active backup preview found!");
            player.sendMessage(ChatColor.GRAY + "Usage: /rewind preview <area> <backup_id>");
            return false;
        }
    }

    public void setPlayerParticle(Player player, Particle particle) {
        playerParticleSettings.put(player.getUniqueId(), particle);
    }

    public Particle getPlayerParticle(Player player) {
        return playerParticleSettings.getOrDefault(player.getUniqueId(), DEFAULT_PARTICLE);
    }

    public boolean hasActiveBackupPreview(Player player) {
        return activeBackupPreviews.containsKey(player.getUniqueId());
    }

    public void stopBackupPreview(Player player) {
        activeBackupPreviews.remove(player.getUniqueId());
    }

    private void showBackupPreviewParticles(Player player, BackupPreview preview) {
        if (preview == null || !player.getWorld().equals(preview.getArea().getPos1().getWorld())) {
            return;
        }

        ProtectedArea area = preview.getArea();
        Location playerLoc = player.getLocation();
        Particle playerParticle = getPlayerParticle(player);

        Location min = area.getMin();
        Location max = area.getMax();
        int sizeX = (int) Math.abs(max.getX() - min.getX()) + 1;
        int sizeZ = (int) Math.abs(max.getZ() - min.getZ()) + 1;

        if (sizeX > 80 || sizeZ > 80) {
            if (System.currentTimeMillis() % 20000 < 1000) {
                player.sendMessage(ChatColor.RED + "Area too large for particle preview! Maximum: 80x80, Current: " + sizeX + "x" + sizeZ);
            }
            return;
        }

        double minX = min.getBlockX();
        double maxX = max.getBlockX() + 1;
        double minY = min.getBlockY();
        double maxY = max.getBlockY() + 1;
        double minZ = min.getBlockZ();
        double maxZ = max.getBlockZ() + 1;

        World world = area.getPos1().getWorld();

        drawWorldEditStyleOutline(player, world, minX, maxX, minY, maxY, minZ, maxZ, playerParticle, playerLoc);
    }

    private void drawWorldEditStyleOutline(Player player, World world, double minX, double maxX,
                                           double minY, double maxY, double minZ, double maxZ,
                                           Particle particle, Location playerLoc) {

        double step = 0.25;
        double maxDistance = 50.0;

        drawEdgeLine(player, world, minX, minY, minZ, maxX, minY, minZ, step, particle, playerLoc, maxDistance);
        drawEdgeLine(player, world, maxX, minY, minZ, maxX, minY, maxZ, step, particle, playerLoc, maxDistance);
        drawEdgeLine(player, world, maxX, minY, maxZ, minX, minY, maxZ, step, particle, playerLoc, maxDistance);
        drawEdgeLine(player, world, minX, minY, maxZ, minX, minY, minZ, step, particle, playerLoc, maxDistance);

        if (maxY - minY > 1) {
            drawEdgeLine(player, world, minX, maxY, minZ, maxX, maxY, minZ, step, particle, playerLoc, maxDistance);
            drawEdgeLine(player, world, maxX, maxY, minZ, maxX, maxY, maxZ, step, particle, playerLoc, maxDistance);
            drawEdgeLine(player, world, maxX, maxY, maxZ, minX, maxY, maxZ, step, particle, playerLoc, maxDistance);
            drawEdgeLine(player, world, minX, maxY, maxZ, minX, maxY, minZ, step, particle, playerLoc, maxDistance);
        }

        if (maxY - minY > 1) {
            drawEdgeLine(player, world, minX, minY, minZ, minX, maxY, minZ, step, particle, playerLoc, maxDistance);
            drawEdgeLine(player, world, maxX, minY, minZ, maxX, maxY, minZ, step, particle, playerLoc, maxDistance);
            drawEdgeLine(player, world, maxX, minY, maxZ, maxX, maxY, maxZ, step, particle, playerLoc, maxDistance);
            drawEdgeLine(player, world, minX, minY, maxZ, minX, maxY, maxZ, step, particle, playerLoc, maxDistance);
        }
    }

    private void drawEdgeLine(Player player, World world, double x1, double y1, double z1,
                              double x2, double y2, double z2, double step, Particle particle,
                              Location playerLoc, double maxDistance) {

        double deltaX = x2 - x1;
        double deltaY = y2 - y1;
        double deltaZ = z2 - z1;
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

        if (distance == 0) return;

        int steps = Math.max(1, (int) Math.ceil(distance / step));

        for (int i = 0; i <= steps; i++) {
            double ratio = (double) i / steps;
            double x = x1 + deltaX * ratio;
            double y = y1 + deltaY * ratio;
            double z = z1 + deltaZ * ratio;

            Location particleLoc = new Location(world, x, y, z);

            if (particleLoc.distance(playerLoc) <= maxDistance) {
                player.spawnParticle(particle, particleLoc, 1, 0.0, 0.0, 0.0, 0);
            }
        }
    }

    private static class BackupPreview {
        private final arearewind.data.AreaBackup backup;
        private final arearewind.data.ProtectedArea area;

        public BackupPreview(arearewind.data.AreaBackup backup, arearewind.data.ProtectedArea area) {
            this.backup = backup;
            this.area = area;
        }

        public arearewind.data.AreaBackup getBackup() {
            return backup;
        }

        public arearewind.data.ProtectedArea getArea() {
            return area;
        }
    }

    public static class VisualizationSettings {
        private Particle particle = DEFAULT_PARTICLE;
        private int particleCount = DEFAULT_PARTICLE_COUNT;
        private double maxDistance = DEFAULT_MAX_DISTANCE;
        private VisualizationType visualizationType = VisualizationType.CORNERS;
        private ChatColor color = ChatColor.GREEN;

        public VisualizationSettings() {}

        public VisualizationSettings(VisualizationSettings other) {
            this.particle = other.particle;
            this.particleCount = other.particleCount;
            this.maxDistance = other.maxDistance;
            this.visualizationType = other.visualizationType;
            this.color = other.color;
        }

        public Particle getParticle() { return particle; }
        public void setParticle(Particle particle) { this.particle = particle; }

        public int getParticleCount() { return particleCount; }
        public void setParticleCount(int particleCount) { this.particleCount = Math.max(1, particleCount); }

        public double getMaxDistance() { return maxDistance; }
        public void setMaxDistance(double maxDistance) { this.maxDistance = Math.max(1.0, maxDistance); }

        public VisualizationType getVisualizationType() { return visualizationType; }
        public void setVisualizationType(VisualizationType visualizationType) { this.visualizationType = visualizationType; }

        public ChatColor getColor() { return color; }
        public void setColor(ChatColor color) { this.color = color; }

        public VisualizationSettings withHighVisibility() {
            VisualizationSettings copy = new VisualizationSettings(this);
            copy.particleCount = Math.max(10, this.particleCount * 3);
            copy.maxDistance = 200.0;
            return copy;
        }
    }

    public enum VisualizationType {
        CORNERS,
        EDGES,
        FACES,
        FULL
    }
}
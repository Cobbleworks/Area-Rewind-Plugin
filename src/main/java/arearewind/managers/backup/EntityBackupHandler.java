package arearewind.managers.backup;

import arearewind.data.ProtectedArea;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Handles backup and restoration of entities (item frames, armor stands, paintings).
 */
public class EntityBackupHandler {

    private final JavaPlugin plugin;

    public EntityBackupHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Backs up all supported entities within the area bounds.
     * Supported entities: ItemFrame, GlowItemFrame, ArmorStand, Painting
     */
    public Map<String, Object> backupEntitiesInArea(ProtectedArea area) {
        Map<String, Object> entities = new HashMap<>();
        Location min = area.getMin();
        Location max = area.getMax();
        World world = min.getWorld();
        int entityCounter = 0;

        try {
            for (Entity entity : world.getEntities()) {
                Location loc = entity.getLocation();
                if (!isWithinBounds(loc, min, max)) {
                    continue;
                }

                Map<String, Object> entityData = captureEntityData(entity);
                if (entityData != null) {
                    String key = getEntityKey(entity, entityCounter++);
                    entities.put(key, entityData);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to backup entities: " + e.getMessage());
            e.printStackTrace();
        }

        return entities;
    }

    /**
     * Restores entities from backup data within the area.
     * First removes existing entities of supported types, then spawns backed up entities.
     */
    public void restoreEntitiesInArea(ProtectedArea area, Map<String, Object> entityData) {
        if (entityData == null || entityData.isEmpty()) return;

        Location min = area.getMin();
        Location max = area.getMax();
        World world = min.getWorld();

        // Remove existing entities of the types we're restoring
        removeExistingEntities(world, min, max);

        // Restore each entity
        for (Map.Entry<String, Object> entry : entityData.entrySet()) {
            try {
                restoreEntity(world, entry.getKey(), entry.getValue());
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to restore entity " + entry.getKey() + ": " + e.getMessage());
            }
        }
    }

    private boolean isWithinBounds(Location loc, Location min, Location max) {
        return loc.getBlockX() >= min.getBlockX() && loc.getBlockX() <= max.getBlockX() &&
               loc.getBlockY() >= min.getBlockY() && loc.getBlockY() <= max.getBlockY() &&
               loc.getBlockZ() >= min.getBlockZ() && loc.getBlockZ() <= max.getBlockZ();
    }

    private String getEntityKey(Entity entity, int counter) {
        if (entity instanceof org.bukkit.entity.ItemFrame) {
            return "frame_" + counter;
        } else if (entity instanceof org.bukkit.entity.ArmorStand) {
            return "armorstand_" + counter;
        } else if (entity instanceof org.bukkit.entity.Painting) {
            return "painting_" + counter;
        }
        return "entity_" + counter;
    }

    private Map<String, Object> captureEntityData(Entity entity) {
        if (entity instanceof org.bukkit.entity.ItemFrame) {
            return captureItemFrameData((org.bukkit.entity.ItemFrame) entity);
        } else if (entity instanceof org.bukkit.entity.ArmorStand) {
            return captureArmorStandData((org.bukkit.entity.ArmorStand) entity);
        } else if (entity instanceof org.bukkit.entity.Painting) {
            return capturePaintingData((org.bukkit.entity.Painting) entity);
        }
        return null;
    }

    private Map<String, Object> captureItemFrameData(org.bukkit.entity.ItemFrame frame) {
        Map<String, Object> frameData = new HashMap<>();
        Location loc = frame.getLocation();

        boolean isGlow = frame instanceof org.bukkit.entity.GlowItemFrame;
        frameData.put("type", isGlow ? "GLOW_ITEM_FRAME" : "ITEM_FRAME");
        frameData.put("x", loc.getX());
        frameData.put("y", loc.getY());
        frameData.put("z", loc.getZ());
        frameData.put("yaw", loc.getYaw());
        frameData.put("pitch", loc.getPitch());
        frameData.put("facing", frame.getFacing().name());
        frameData.put("rotation", frame.getRotation().name());
        frameData.put("fixed", frame.isFixed());
        frameData.put("visible", frame.isVisible());
        frameData.put("invulnerable", frame.isInvulnerable());

        if (frame.getItem() != null && frame.getItem().getType() != Material.AIR) {
            frameData.put("item", frame.getItem());
        }
        if (frame.getCustomName() != null) {
            frameData.put("customName", frame.getCustomName());
        }
        frameData.put("customNameVisible", frame.isCustomNameVisible());

        return frameData;
    }

    private Map<String, Object> captureArmorStandData(org.bukkit.entity.ArmorStand stand) {
        Map<String, Object> standData = new HashMap<>();
        Location loc = stand.getLocation();

        standData.put("type", "ARMOR_STAND");
        standData.put("x", loc.getX());
        standData.put("y", loc.getY());
        standData.put("z", loc.getZ());
        standData.put("yaw", loc.getYaw());
        standData.put("pitch", loc.getPitch());

        // Equipment
        org.bukkit.inventory.EntityEquipment equipment = stand.getEquipment();
        if (equipment != null) {
            captureEquipment(standData, equipment);
        }

        // Poses
        standData.put("headPose", eulerAngleToArray(stand.getHeadPose()));
        standData.put("bodyPose", eulerAngleToArray(stand.getBodyPose()));
        standData.put("leftArmPose", eulerAngleToArray(stand.getLeftArmPose()));
        standData.put("rightArmPose", eulerAngleToArray(stand.getRightArmPose()));
        standData.put("leftLegPose", eulerAngleToArray(stand.getLeftLegPose()));
        standData.put("rightLegPose", eulerAngleToArray(stand.getRightLegPose()));

        // Properties
        standData.put("visible", stand.isVisible());
        standData.put("arms", stand.hasArms());
        standData.put("basePlate", stand.hasBasePlate());
        standData.put("small", stand.isSmall());
        standData.put("marker", stand.isMarker());
        standData.put("invulnerable", stand.isInvulnerable());
        standData.put("gravity", stand.hasGravity());
        standData.put("glowing", stand.isGlowing());

        if (stand.getCustomName() != null) {
            standData.put("customName", stand.getCustomName());
        }
        standData.put("customNameVisible", stand.isCustomNameVisible());

        return standData;
    }

    private void captureEquipment(Map<String, Object> data, org.bukkit.inventory.EntityEquipment equipment) {
        if (equipment.getHelmet() != null && equipment.getHelmet().getType() != Material.AIR) {
            data.put("helmet", equipment.getHelmet());
        }
        if (equipment.getChestplate() != null && equipment.getChestplate().getType() != Material.AIR) {
            data.put("chestplate", equipment.getChestplate());
        }
        if (equipment.getLeggings() != null && equipment.getLeggings().getType() != Material.AIR) {
            data.put("leggings", equipment.getLeggings());
        }
        if (equipment.getBoots() != null && equipment.getBoots().getType() != Material.AIR) {
            data.put("boots", equipment.getBoots());
        }
        if (equipment.getItemInMainHand() != null && equipment.getItemInMainHand().getType() != Material.AIR) {
            data.put("mainHand", equipment.getItemInMainHand());
        }
        if (equipment.getItemInOffHand() != null && equipment.getItemInOffHand().getType() != Material.AIR) {
            data.put("offHand", equipment.getItemInOffHand());
        }
    }

    private Map<String, Object> capturePaintingData(org.bukkit.entity.Painting painting) {
        Map<String, Object> paintingData = new HashMap<>();
        Location loc = painting.getLocation();

        paintingData.put("type", "PAINTING");
        paintingData.put("x", loc.getX());
        paintingData.put("y", loc.getY());
        paintingData.put("z", loc.getZ());
        paintingData.put("facing", painting.getFacing().name());
        paintingData.put("art", painting.getArt().name());

        return paintingData;
    }

    private double[] eulerAngleToArray(org.bukkit.util.EulerAngle angle) {
        return new double[]{angle.getX(), angle.getY(), angle.getZ()};
    }

    private org.bukkit.util.EulerAngle arrayToEulerAngle(Object obj) {
        if (obj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Number> list = (List<Number>) obj;
            if (list.size() >= 3) {
                return new org.bukkit.util.EulerAngle(
                    list.get(0).doubleValue(),
                    list.get(1).doubleValue(),
                    list.get(2).doubleValue()
                );
            }
        }
        return org.bukkit.util.EulerAngle.ZERO;
    }

    private void removeExistingEntities(World world, Location min, Location max) {
        world.getEntities().stream()
            .filter(entity -> isWithinBounds(entity.getLocation(), min, max))
            .filter(entity -> entity instanceof org.bukkit.entity.ItemFrame ||
                              entity instanceof org.bukkit.entity.ArmorStand ||
                              entity instanceof org.bukkit.entity.Painting)
            .forEach(Entity::remove);
    }

    @SuppressWarnings("unchecked")
    private void restoreEntity(World world, String key, Object value) {
        Map<String, Object> data = (Map<String, Object>) value;
        String type = (String) data.get("type");

        double x = ((Number) data.get("x")).doubleValue();
        double y = ((Number) data.get("y")).doubleValue();
        double z = ((Number) data.get("z")).doubleValue();
        float yaw = data.containsKey("yaw") ? ((Number) data.get("yaw")).floatValue() : 0;
        float pitch = data.containsKey("pitch") ? ((Number) data.get("pitch")).floatValue() : 0;
        Location loc = new Location(world, x, y, z, yaw, pitch);

        switch (type) {
            case "ITEM_FRAME":
            case "GLOW_ITEM_FRAME":
                restoreItemFrame(world, loc, data, type);
                break;
            case "ARMOR_STAND":
                restoreArmorStand(world, loc, data);
                break;
            case "PAINTING":
                restorePainting(world, loc, data);
                break;
        }
    }

    private void restoreItemFrame(World world, Location loc, Map<String, Object> data, String type) {
        org.bukkit.block.BlockFace facing = org.bukkit.block.BlockFace.valueOf((String) data.get("facing"));

        Class<? extends org.bukkit.entity.ItemFrame> frameClass =
            "GLOW_ITEM_FRAME".equals(type) ? org.bukkit.entity.GlowItemFrame.class : org.bukkit.entity.ItemFrame.class;

        world.spawn(loc, frameClass, frame -> {
            frame.setFacingDirection(facing);

            if (data.containsKey("rotation")) {
                frame.setRotation(org.bukkit.Rotation.valueOf((String) data.get("rotation")));
            }
            if (data.containsKey("item")) {
                frame.setItem((ItemStack) data.get("item"));
            }
            if (data.containsKey("fixed")) {
                frame.setFixed((Boolean) data.get("fixed"));
            }
            if (data.containsKey("visible")) {
                frame.setVisible((Boolean) data.get("visible"));
            }
            if (data.containsKey("invulnerable")) {
                frame.setInvulnerable((Boolean) data.get("invulnerable"));
            }
            if (data.containsKey("customName")) {
                frame.setCustomName((String) data.get("customName"));
            }
            if (data.containsKey("customNameVisible")) {
                frame.setCustomNameVisible((Boolean) data.get("customNameVisible"));
            }
        });
    }

    private void restoreArmorStand(World world, Location loc, Map<String, Object> data) {
        world.spawn(loc, org.bukkit.entity.ArmorStand.class, stand -> {
            org.bukkit.inventory.EntityEquipment equipment = stand.getEquipment();

            // Equipment
            if (equipment != null) {
                restoreEquipment(equipment, data);
            }

            // Poses
            if (data.containsKey("headPose")) {
                stand.setHeadPose(arrayToEulerAngle(data.get("headPose")));
            }
            if (data.containsKey("bodyPose")) {
                stand.setBodyPose(arrayToEulerAngle(data.get("bodyPose")));
            }
            if (data.containsKey("leftArmPose")) {
                stand.setLeftArmPose(arrayToEulerAngle(data.get("leftArmPose")));
            }
            if (data.containsKey("rightArmPose")) {
                stand.setRightArmPose(arrayToEulerAngle(data.get("rightArmPose")));
            }
            if (data.containsKey("leftLegPose")) {
                stand.setLeftLegPose(arrayToEulerAngle(data.get("leftLegPose")));
            }
            if (data.containsKey("rightLegPose")) {
                stand.setRightLegPose(arrayToEulerAngle(data.get("rightLegPose")));
            }

            // Properties
            if (data.containsKey("visible")) {
                stand.setVisible((Boolean) data.get("visible"));
            }
            if (data.containsKey("arms")) {
                stand.setArms((Boolean) data.get("arms"));
            }
            if (data.containsKey("basePlate")) {
                stand.setBasePlate((Boolean) data.get("basePlate"));
            }
            if (data.containsKey("small")) {
                stand.setSmall((Boolean) data.get("small"));
            }
            if (data.containsKey("marker")) {
                stand.setMarker((Boolean) data.get("marker"));
            }
            if (data.containsKey("invulnerable")) {
                stand.setInvulnerable((Boolean) data.get("invulnerable"));
            }
            if (data.containsKey("gravity")) {
                stand.setGravity((Boolean) data.get("gravity"));
            }
            if (data.containsKey("glowing")) {
                stand.setGlowing((Boolean) data.get("glowing"));
            }
            if (data.containsKey("customName")) {
                stand.setCustomName((String) data.get("customName"));
            }
            if (data.containsKey("customNameVisible")) {
                stand.setCustomNameVisible((Boolean) data.get("customNameVisible"));
            }
        });
    }

    private void restoreEquipment(org.bukkit.inventory.EntityEquipment equipment, Map<String, Object> data) {
        if (data.containsKey("helmet")) {
            equipment.setHelmet((ItemStack) data.get("helmet"));
        }
        if (data.containsKey("chestplate")) {
            equipment.setChestplate((ItemStack) data.get("chestplate"));
        }
        if (data.containsKey("leggings")) {
            equipment.setLeggings((ItemStack) data.get("leggings"));
        }
        if (data.containsKey("boots")) {
            equipment.setBoots((ItemStack) data.get("boots"));
        }
        if (data.containsKey("mainHand")) {
            equipment.setItemInMainHand((ItemStack) data.get("mainHand"));
        }
        if (data.containsKey("offHand")) {
            equipment.setItemInOffHand((ItemStack) data.get("offHand"));
        }
    }

    private void restorePainting(World world, Location loc, Map<String, Object> data) {
        org.bukkit.block.BlockFace facing = org.bukkit.block.BlockFace.valueOf((String) data.get("facing"));
        String artName = (String) data.get("art");

        world.spawn(loc, org.bukkit.entity.Painting.class, painting -> {
            painting.setFacingDirection(facing);
            try {
                painting.setArt(org.bukkit.Art.valueOf(artName));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().fine("Unknown art type: " + artName);
            }
        });
    }
}

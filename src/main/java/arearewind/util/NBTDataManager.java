package arearewind.util;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Banner;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

/**
 * Utility class for handling NBT data capture and restoration for complex
 * blocks
 * Separated from BackupManager for better organization and maintainability
 */
public class NBTDataManager {

    private final JavaPlugin plugin;

    public NBTDataManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Captures complete NBT data for any block that has complex state data
     * This unified approach works for skulls, banners, signs, lecterns, and other
     * NBT-dependent blocks by directly capturing the block state
     */
    public String captureCompleteNBTData(Block block) {
        try {
            // Only capture NBT for blocks that actually have complex state data
            if (!hasComplexNBTData(block)) {
                return null;
            }

            // Try to capture block state directly first (works better for skulls,
            // containers, etc.)
            String blockStateData = captureBlockStateDirectly(block);
            if (blockStateData != null) {
                plugin.getLogger().fine("Captured block state NBT data for " + block.getType() +
                        " at " + block.getLocation());
                return blockStateData;
            }

            // Fallback: Use getDrops() for blocks where direct state capture doesn't work
            Collection<ItemStack> drops = block.getDrops();
            for (ItemStack drop : drops) {
                if (drop != null && drop.getType() == block.getType() && hasSignificantNBTData(drop)) {
                    String nbtData = saveItemStackAsBase64(drop);
                    if (nbtData != null) {
                        plugin.getLogger().fine("Captured item drop NBT data for " + block.getType() +
                                " at " + block.getLocation());
                        return nbtData;
                    }
                }
            }

            return null;

        } catch (Exception e) {
            plugin.getLogger().fine("Could not capture NBT data for " + block.getType() +
                    " at " + block.getLocation() + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Captures block state directly by serializing the BlockState itself
     * This preserves ALL data including custom textures, container contents, etc.
     */
    private String captureBlockStateDirectly(Block block) {
        try {
            org.bukkit.block.BlockState blockState = block.getState();

            // For containers, capture both the state and contents together
            if (blockState instanceof org.bukkit.block.Container) {
                org.bukkit.block.Container container = (org.bukkit.block.Container) blockState;
                ItemStack[] contents = container.getInventory().getContents();

                // Create a compound data structure that includes both state and contents
                Map<String, Object> containerData = new HashMap<>();
                containerData.put("blockState", saveBlockStateAsBase64(blockState));
                containerData.put("contents", serializeContainerContents(contents));

                return saveMapAsBase64(containerData);
            }

            // For skulls and other special blocks, capture the state directly
            return saveBlockStateAsBase64(blockState);

        } catch (Exception e) {
            plugin.getLogger().fine("Could not capture block state directly for " + block.getType() +
                    " at " + block.getLocation() + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Saves a BlockState as Base64 encoded bytes
     */
    private String saveBlockStateAsBase64(org.bukkit.block.BlockState blockState) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeObject(blockState);
            dataOutput.close();

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            plugin.getLogger().fine("Failed to save BlockState as Base64: " + e.getMessage());
            return null;
        }
    }

    /**
     * Saves a Map as Base64 encoded bytes
     */
    private String saveMapAsBase64(Map<String, Object> map) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeObject(map);
            dataOutput.close();

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            plugin.getLogger().fine("Failed to save Map as Base64: " + e.getMessage());
            return null;
        }
    }

    /**
     * Serializes container contents to a string representation
     */
    private String serializeContainerContents(ItemStack[] contents) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(contents.length);
            for (ItemStack item : contents) {
                dataOutput.writeObject(item);
            }
            dataOutput.close();

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            plugin.getLogger().fine("Failed to serialize container contents: " + e.getMessage());
            return null;
        }
    }

    /**
     * Restores complete NBT data to a block using a unified approach
     */
    public boolean restoreCompleteNBTData(Block block, String nbtData) {
        if (nbtData == null || nbtData.isEmpty()) {
            return false;
        }

        try {
            // First, try to restore from direct block state capture
            if (restoreFromBlockState(block, nbtData)) {
                plugin.getLogger().fine("Successfully restored " + block.getType() +
                        " from block state data at " + block.getLocation());
                return true;
            }

            // Fallback: try to restore from ItemStack data
            ItemStack restoredItem = loadItemStackFromBase64(nbtData);
            if (restoredItem != null && restoredItem.getType() == block.getType()) {
                if (restoreFromItemStack(block, restoredItem)) {
                    plugin.getLogger().fine("Successfully restored " + block.getType() +
                            " from item data at " + block.getLocation());
                    return true;
                }
            }

            plugin.getLogger().fine("Could not restore NBT data for " + block.getType() +
                    " at " + block.getLocation());
            return false;

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to restore NBT data for " + block.getType() +
                    " at " + block.getLocation() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Attempts to restore a block from captured block state data
     */
    private boolean restoreFromBlockState(Block block, String nbtData) {
        try {
            // Try to load as a container data map first
            Map<String, Object> containerData = loadMapFromBase64(nbtData);
            if (containerData != null && containerData.containsKey("blockState")
                    && containerData.containsKey("contents")) {
                // This is container data with both state and contents
                String blockStateData = (String) containerData.get("blockState");
                String contentsData = (String) containerData.get("contents");

                org.bukkit.block.BlockState restoredState = loadBlockStateFromBase64(blockStateData);
                if (restoredState != null && restoredState instanceof org.bukkit.block.Container) {
                    // Restore the container and its contents
                    org.bukkit.block.Container originalContainer = (org.bukkit.block.Container) block.getState();

                    // Copy state data for special containers
                    boolean specialRestored = copySpecialContainerData(block, restoredState);

                    // Copy inventory contents
                    ItemStack[] restoredContents = deserializeContainerContents(contentsData);
                    if (restoredContents != null) {
                        originalContainer.getInventory().clear();
                        originalContainer.getInventory().setContents(restoredContents);
                        originalContainer.update(true, false);

                        if (specialRestored) {
                            plugin.getLogger().fine("Successfully restored special container data for " +
                                    block.getType() + " at " + block.getLocation());
                        }

                        return true;
                    }
                }
            }

            // Try to load as direct block state
            org.bukkit.block.BlockState restoredState = loadBlockStateFromBase64(nbtData);
            if (restoredState != null) {
                // For skulls and other special blocks, copy the relevant data
                return copyBlockStateData(block, restoredState);
            }

            return false;
        } catch (Exception e) {
            plugin.getLogger().fine("Could not restore from block state: " + e.getMessage());
            return false;
        }
    }

    /**
     * Loads a Map from Base64 encoded bytes
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> loadMapFromBase64(String base64Data) {
        try {
            byte[] data = Base64.getDecoder().decode(base64Data);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            Map<String, Object> map = (Map<String, Object>) dataInput.readObject();
            dataInput.close();

            return map;
        } catch (Exception e) {
            plugin.getLogger().fine("Failed to load Map from Base64: " + e.getMessage());
            return null;
        }
    }

    /**
     * Loads a BlockState from Base64 encoded bytes
     */
    private org.bukkit.block.BlockState loadBlockStateFromBase64(String base64Data) {
        try {
            byte[] data = Base64.getDecoder().decode(base64Data);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            org.bukkit.block.BlockState blockState = (org.bukkit.block.BlockState) dataInput.readObject();
            dataInput.close();

            return blockState;
        } catch (Exception e) {
            plugin.getLogger().fine("Failed to load BlockState from Base64: " + e.getMessage());
            return null;
        }
    }

    /**
     * Deserializes container contents from Base64 string
     */
    private ItemStack[] deserializeContainerContents(String base64Data) {
        try {
            byte[] data = Base64.getDecoder().decode(base64Data);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            int length = dataInput.readInt();
            ItemStack[] contents = new ItemStack[length];

            for (int i = 0; i < length; i++) {
                contents[i] = (ItemStack) dataInput.readObject();
            }

            dataInput.close();
            return contents;
        } catch (Exception e) {
            plugin.getLogger().fine("Failed to deserialize container contents: " + e.getMessage());
            return null;
        }
    }

    /**
     * Copies data from a restored block state to the current block
     */
    @SuppressWarnings("deprecation")
    private boolean copyBlockStateData(Block block, org.bukkit.block.BlockState restoredState) {
        try {
            org.bukkit.block.BlockState currentState = block.getState();

            // Handle skulls
            if (currentState instanceof org.bukkit.block.Skull && restoredState instanceof org.bukkit.block.Skull) {
                org.bukkit.block.Skull currentSkull = (org.bukkit.block.Skull) currentState;
                org.bukkit.block.Skull restoredSkull = (org.bukkit.block.Skull) restoredState;

                // Copy the owning player if it exists
                if (restoredSkull.getOwningPlayer() != null) {
                    currentSkull.setOwningPlayer(restoredSkull.getOwningPlayer());
                }

                // Try to copy player profile for custom textures using modern API
                try {
                    // Try the modern PlayerProfile API first
                    java.lang.reflect.Method getPlayerProfile = restoredSkull.getClass().getMethod("getPlayerProfile");
                    Object profile = getPlayerProfile.invoke(restoredSkull);
                    if (profile != null) {
                        java.lang.reflect.Method setPlayerProfile = currentSkull.getClass()
                                .getMethod("setPlayerProfile", profile.getClass());
                        setPlayerProfile.invoke(currentSkull, profile);
                        plugin.getLogger().fine("Copied player profile for skull at " + block.getLocation());
                    }
                } catch (Exception e) {
                    // Fallback to older methods if needed
                    try {
                        java.lang.reflect.Method getOwnerProfile = restoredSkull.getClass()
                                .getMethod("getOwnerProfile");
                        Object profile = getOwnerProfile.invoke(restoredSkull);
                        if (profile != null) {
                            java.lang.reflect.Method setOwnerProfile = currentSkull.getClass()
                                    .getMethod("setOwnerProfile", profile.getClass());
                            setOwnerProfile.invoke(currentSkull, profile);
                            plugin.getLogger().fine("Copied owner profile for skull at " + block.getLocation());
                        }
                    } catch (Exception e2) {
                        plugin.getLogger().fine(
                                "Could not copy skull profile using reflection, using basic owner: " + e2.getMessage());
                        // Just use the basic owner setting which we already did above
                    }
                }

                currentSkull.update(true, false);
                return true;
            }

            // Handle banners
            if (currentState instanceof org.bukkit.block.Banner && restoredState instanceof org.bukkit.block.Banner) {
                org.bukkit.block.Banner currentBanner = (org.bukkit.block.Banner) currentState;
                org.bukkit.block.Banner restoredBanner = (org.bukkit.block.Banner) restoredState;

                currentBanner.setPatterns(restoredBanner.getPatterns());
                currentBanner.update(true, false);
                return true;
            }

            // Handle signs
            if (currentState instanceof org.bukkit.block.Sign && restoredState instanceof org.bukkit.block.Sign) {
                org.bukkit.block.Sign currentSign = (org.bukkit.block.Sign) currentState;
                org.bukkit.block.Sign restoredSign = (org.bukkit.block.Sign) restoredState;

                // Use the modern sign API if available, fallback to legacy if needed
                try {
                    // Try to use getSide() method for modern signs
                    Object frontSide = currentSign.getClass().getMethod("getSide", org.bukkit.block.sign.Side.class)
                            .invoke(currentSign, org.bukkit.block.sign.Side.FRONT);
                    Object restoredFrontSide = restoredSign.getClass()
                            .getMethod("getSide", org.bukkit.block.sign.Side.class)
                            .invoke(restoredSign, org.bukkit.block.sign.Side.FRONT);

                    // Copy lines from restored side to current side
                    for (int i = 0; i < 4; i++) {
                        String line = (String) restoredFrontSide.getClass().getMethod("getLine", int.class)
                                .invoke(restoredFrontSide, i);
                        frontSide.getClass().getMethod("setLine", int.class, String.class)
                                .invoke(frontSide, i, line);
                    }
                } catch (Exception e) {
                    // Fallback to legacy method for older versions
                    for (int i = 0; i < 4; i++) {
                        try {
                            String line = restoredSign.getLine(i);
                            currentSign.setLine(i, line);
                        } catch (Exception legacyEx) {
                            plugin.getLogger().fine("Could not restore sign line " + i + ": " + legacyEx.getMessage());
                        }
                    }
                }

                currentSign.update(true, false);
                return true;
            }

            return false;
        } catch (Exception e) {
            plugin.getLogger().fine("Failed to copy block state data: " + e.getMessage());
            return false;
        }
    }

    /**
     * Copies special data for container blocks that have extra properties beyond
     * just inventory. Lecterns, brewing stands, and chiseled bookshelves are now
     * handled purely as containers.
     */
    private boolean copySpecialContainerData(Block block, org.bukkit.block.BlockState restoredState) {
        try {
            // Currently no special container handling needed
            // All containers are handled through standard inventory restoration
            return false;
        } catch (Exception e) {
            plugin.getLogger().fine("Failed to copy special container data: " + e.getMessage());
            return false;
        }
    }

    /**
     * Unified method to restore from ItemStack data
     */
    private boolean restoreFromItemStack(Block block, ItemStack restoredItem) {
        try {
            // Handle skulls
            if (block.getType() == Material.PLAYER_HEAD || block.getType() == Material.PLAYER_WALL_HEAD) {
                return restoreSkullFromItemStack(block, restoredItem);
            }

            // Handle banners
            if (block.getType().name().contains("BANNER")) {
                return restoreBannerFromItemStack(block, restoredItem);
            }

            // Handle signs
            if (block.getType().name().contains("SIGN")) {
                return restoreSignFromItemStack(block, restoredItem);
            }

            // Handle spawners
            if (block.getType() == Material.SPAWNER) {
                return restoreSpawnerFromItemStack(block, restoredItem);
            }

            return false;
        } catch (Exception e) {
            plugin.getLogger().fine("Failed to restore from ItemStack: " + e.getMessage());
            return false;
        }
    }

    /**
     * Restore a skull from an ItemStack with complete NBT data
     */
    private boolean restoreSkullFromItemStack(Block block, ItemStack skullItem) {
        try {
            if (!(block.getState() instanceof org.bukkit.block.Skull)) {
                return false;
            }

            org.bukkit.block.Skull skull = (org.bukkit.block.Skull) block.getState();

            if (skullItem.getItemMeta() instanceof org.bukkit.inventory.meta.SkullMeta) {
                org.bukkit.inventory.meta.SkullMeta skullMeta = (org.bukkit.inventory.meta.SkullMeta) skullItem
                        .getItemMeta();

                // Apply basic skull data
                if (skullMeta.getOwningPlayer() != null) {
                    skull.setOwningPlayer(skullMeta.getOwningPlayer());
                }

                // Try to apply owner profile with custom textures if available
                boolean profileSet = false;
                try {
                    java.lang.reflect.Method getOwnerProfile = skullMeta.getClass().getMethod("getOwnerProfile");
                    Object profile = getOwnerProfile.invoke(skullMeta);
                    if (profile != null) {
                        try {
                            java.lang.reflect.Method setOwnerProfile = skull.getClass().getMethod("setOwnerProfile",
                                    profile.getClass());
                            setOwnerProfile.invoke(skull, profile);
                            profileSet = true;
                            plugin.getLogger()
                                    .info("Applied custom texture profile to skull at " + block.getLocation());
                        } catch (Exception setEx) {
                            try {
                                java.lang.reflect.Method setPlayerProfile = skull.getClass()
                                        .getMethod("setPlayerProfile", profile.getClass());
                                setPlayerProfile.invoke(skull, profile);
                                profileSet = true;
                                plugin.getLogger().info("Applied player profile to skull at " + block.getLocation());
                            } catch (Exception altEx) {
                                plugin.getLogger().fine("Could not set profile on skull block: " + setEx.getMessage());
                            }
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().fine("Owner profile not available for restoration: " + e.getMessage());
                }

                // Force block update to ensure texture is applied
                skull.update(true, false);

                // Additional update to ensure the texture loads properly
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    try {
                        Block updatedBlock = skull.getBlock();
                        if (updatedBlock.getState() instanceof org.bukkit.block.Skull) {
                            org.bukkit.block.Skull updatedSkull = (org.bukkit.block.Skull) updatedBlock.getState();
                            updatedSkull.update(true, true);
                            // Reload the chunk to ensure texture appears
                            updatedBlock.getChunk().load();
                        }
                    } catch (Exception ex) {
                        // Ignore update errors
                    }
                }, 1L);

                if (profileSet || skullMeta.getOwningPlayer() != null) {
                    plugin.getLogger().info("Successfully restored skull from NBT data at " + block.getLocation());
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            plugin.getLogger().warning(
                    "Failed to restore skull from ItemStack at " + block.getLocation() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Restore a banner from an ItemStack with complete NBT data
     */
    private boolean restoreBannerFromItemStack(Block block, ItemStack bannerItem) {
        try {
            if (!(block.getState() instanceof Banner)) {
                return false;
            }

            Banner banner = (Banner) block.getState();

            if (bannerItem.getItemMeta() instanceof org.bukkit.inventory.meta.BannerMeta) {
                org.bukkit.inventory.meta.BannerMeta bannerMeta = (org.bukkit.inventory.meta.BannerMeta) bannerItem
                        .getItemMeta();
                banner.setPatterns(bannerMeta.getPatterns());
                banner.update(true, false);

                plugin.getLogger().fine("Successfully restored banner from NBT data at " + block.getLocation());
                return true;
            }

            return false;
        } catch (Exception e) {
            plugin.getLogger().warning(
                    "Failed to restore banner from ItemStack at " + block.getLocation() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Restore a sign from an ItemStack with complete NBT data
     */
    private boolean restoreSignFromItemStack(Block block, ItemStack signItem) {
        try {
            if (!(block.getState() instanceof Sign)) {
                return false;
            }

            // For signs, the NBT restoration is complex and we'll stick with legacy method
            // for now
            plugin.getLogger()
                    .fine("Sign NBT restoration not implemented, using legacy method at " + block.getLocation());
            return false;
        } catch (Exception e) {
            plugin.getLogger()
                    .warning("Failed to restore sign from ItemStack at " + block.getLocation() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Restore a spawner from an ItemStack with complete NBT data
     */
    private boolean restoreSpawnerFromItemStack(Block block, ItemStack spawnerItem) {
        try {
            if (!(block.getState() instanceof org.bukkit.block.CreatureSpawner)) {
                return false;
            }

            // Try to extract spawner data from NBT
            if (spawnerItem.hasItemMeta()) {
                // This is a complex restoration that would require NBT manipulation
                plugin.getLogger().fine("Spawner NBT restoration not fully implemented at " + block.getLocation());
            }

            return false;
        } catch (Exception e) {
            plugin.getLogger().warning(
                    "Failed to restore spawner from ItemStack at " + block.getLocation() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a block type has complex NBT data that should be preserved
     */
    public boolean hasComplexNBTData(Block block) {
        Material type = block.getType();

        // Skulls with custom textures
        if (type == Material.PLAYER_HEAD || type == Material.PLAYER_WALL_HEAD) {
            return true;
        }

        // Banners with patterns
        if (type.name().contains("BANNER")) {
            return true;
        }

        // Signs with text (though we handle this separately for compatibility)
        if (type.name().contains("SIGN")) {
            return true;
        }

        // Note: Lecterns, chiseled bookshelves, and brewing stands are now treated as
        // containers only
        // They no longer need special NBT handling

        // Command blocks
        if (type == Material.COMMAND_BLOCK || type == Material.REPEATING_COMMAND_BLOCK ||
                type == Material.CHAIN_COMMAND_BLOCK) {
            return true;
        }

        // Structure blocks
        if (type == Material.STRUCTURE_BLOCK) {
            return true;
        }

        // Jigsaw blocks
        if (type == Material.JIGSAW) {
            return true;
        }

        // Beehives and bee nests (with bees)
        if (type == Material.BEEHIVE || type == Material.BEE_NEST) {
            return true;
        }

        // Spawners
        if (type == Material.SPAWNER) {
            return true;
        }

        // Note: Brewing stands are now treated as containers only, not special NBT
        // blocks

        return false;
    }

    /**
     * Checks if an ItemStack has significant NBT data worth preserving
     */
    public boolean hasSignificantNBTData(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        // For skulls, check if it has owner data
        if (item.getType() == Material.PLAYER_HEAD) {
            return isCustomPlayerHead(item);
        }

        // For banners, check if it has patterns
        if (item.getType().name().contains("BANNER")) {
            return item.getItemMeta() instanceof org.bukkit.inventory.meta.BannerMeta &&
                    !((org.bukkit.inventory.meta.BannerMeta) item.getItemMeta()).getPatterns().isEmpty();
        }

        // For books, check if they have content
        if (item.getType() == Material.WRITTEN_BOOK) {
            return item.getItemMeta() instanceof org.bukkit.inventory.meta.BookMeta &&
                    (((org.bukkit.inventory.meta.BookMeta) item.getItemMeta()).hasTitle() ||
                            ((org.bukkit.inventory.meta.BookMeta) item.getItemMeta()).hasAuthor() ||
                            !((org.bukkit.inventory.meta.BookMeta) item.getItemMeta()).getPages().isEmpty());
        }

        if (item.getType() == Material.WRITABLE_BOOK) {
            return item.getItemMeta() instanceof org.bukkit.inventory.meta.BookMeta &&
                    !((org.bukkit.inventory.meta.BookMeta) item.getItemMeta()).getPages().isEmpty();
        }

        // For other items, check if they have any custom meta data
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        return meta.hasDisplayName() || meta.hasLore() || meta.hasEnchants();
    }

    /**
     * Checks if an ItemStack is a custom player head (has skull meta with texture
     * data)
     */
    public boolean isCustomPlayerHead(ItemStack item) {
        if (item == null || item.getType() != Material.PLAYER_HEAD) {
            return false;
        }

        // Check if the item has skull meta with custom texture data
        if (item.hasItemMeta() && item.getItemMeta() instanceof org.bukkit.inventory.meta.SkullMeta) {
            org.bukkit.inventory.meta.SkullMeta skullMeta = (org.bukkit.inventory.meta.SkullMeta) item.getItemMeta();

            // Check if it has an owning player (custom skulls have this)
            if (skullMeta.hasOwner() || skullMeta.getOwningPlayer() != null) {
                return true;
            }

            // Also check for custom texture data via reflection (if available)
            try {
                // Try the modern Paper/Spigot API first
                java.lang.reflect.Method getOwnerProfile = skullMeta.getClass().getMethod("getOwnerProfile");
                Object profile = getOwnerProfile.invoke(skullMeta);
                if (profile != null) {
                    // Try to check if the profile has texture properties
                    try {
                        java.lang.reflect.Method getProperties = profile.getClass().getMethod("getProperties");
                        Object properties = getProperties.invoke(profile);
                        if (properties != null) {
                            // Check if properties collection is not empty
                            if (properties instanceof java.util.Collection
                                    && !((java.util.Collection<?>) properties).isEmpty()) {
                                return true;
                            }
                            // Check for texture property specifically using multimap API
                            try {
                                java.lang.reflect.Method get = properties.getClass().getMethod("get", Object.class);
                                Object textureProperty = get.invoke(properties, "textures");
                                if (textureProperty != null && textureProperty instanceof java.util.Collection) {
                                    return !((java.util.Collection<?>) textureProperty).isEmpty();
                                }
                            } catch (Exception e) {
                                // Try alternative approach for different API versions
                                java.lang.reflect.Method containsKey = properties.getClass().getMethod("containsKey",
                                        Object.class);
                                Boolean hasTextures = (Boolean) containsKey.invoke(properties, "textures");
                                if (hasTextures != null && hasTextures) {
                                    return true;
                                }
                            }
                        }
                    } catch (Exception propEx) {
                        // If we can't check properties, assume it's custom if profile exists
                        return true;
                    }
                }
            } catch (Exception e) {
                // Try alternative reflection approaches for different server versions
                try {
                    // Check for Bukkit's internal profile system
                    java.lang.reflect.Field profileField = skullMeta.getClass().getDeclaredField("profile");
                    profileField.setAccessible(true);
                    Object profile = profileField.get(skullMeta);
                    if (profile != null) {
                        // If we have any profile data, consider it custom
                        return true;
                    }
                } catch (Exception e2) {
                    // If all reflection fails, fall back to basic check
                    return skullMeta.hasOwner();
                }
            }
        }

        return false;
    }

    /**
     * Saves an ItemStack as Base64 encoded bytes to preserve all data
     */
    public String saveItemStackAsBase64(ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeObject(item);
            dataOutput.close();

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save ItemStack as Base64: " + e.getMessage());
            return null;
        }
    }

    /**
     * Loads an ItemStack from Base64 encoded bytes
     */
    public ItemStack loadItemStackFromBase64(String base64Data) {
        try {
            byte[] data = Base64.getDecoder().decode(base64Data);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();

            return item;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load ItemStack from Base64: " + e.getMessage());
            return null;
        }
    }

}

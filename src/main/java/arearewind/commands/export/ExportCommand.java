package arearewind.commands.export;

import arearewind.commands.base.BaseCommand;
import arearewind.data.AreaBackup;
import arearewind.data.BlockInfo;
import arearewind.data.ProtectedArea;
import arearewind.managers.*;
import arearewind.util.ConfigurationManager;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Command for exporting protected areas to WorldEdit schematic files
 */
public class ExportCommand extends BaseCommand {

    public ExportCommand(JavaPlugin plugin, AreaManager areaManager, BackupManager backupManager,
            GUIManager guiManager, VisualizationManager visualizationManager,
            PermissionManager permissionManager, ConfigurationManager configManager,
            FileManager fileManager, IntervalManager intervalManager) {
        super(plugin, areaManager, backupManager, guiManager, visualizationManager,
                permissionManager, configManager, fileManager, intervalManager);
    }

    @Override
    public String getName() {
        return "export";
    }

    @Override
    public List<String> getAliases() {
        List<String> aliases = new ArrayList<>();
        aliases.add("exp");
        aliases.add("schematic");
        return aliases;
    }

    @Override
    public String getDescription() {
        return "Export an area's latest backup to a WorldEdit .schem file";
    }

    @Override
    public String getUsage() {
        return "/rewind export <area_name> [backup_id]";
    }

    @Override
    public String getRequiredPermission() {
        return PermissionManager.PERMISSION_EXPORT;
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (!validateMinArgs(player, args, 1)) {
            return true;
        }

        String areaName = args[0];
        ProtectedArea area = areaManager.getArea(areaName);

        if (area == null) {
            player.sendMessage(ChatColor.RED + "Area '" + areaName + "' not found!");
            return true;
        }

        if (!permissionManager.canExport(player, area)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to export this area!");
            return true;
        }

        // Check if WorldEdit is available
        if (!isWorldEditAvailable()) {
            player.sendMessage(ChatColor.RED + "WorldEdit is required for exporting to .schem files!");
            player.sendMessage(ChatColor.GRAY + "Please install WorldEdit to use this feature.");
            return true;
        }

        // Get the backup to export
        AreaBackup backup;
        final String finalBackupId;

        if (args.length > 1) {
            // Use specified backup ID
            finalBackupId = args[1];
            backup = findBackupById(areaName, finalBackupId);
            if (backup == null) {
                player.sendMessage(
                        ChatColor.RED + "Backup '" + finalBackupId + "' not found for area '" + areaName + "'!");
                return true;
            }
        } else {
            // Use latest backup
            List<AreaBackup> backups = backupManager.getBackupHistory(areaName);
            if (backups.isEmpty()) {
                player.sendMessage(ChatColor.RED + "No backups found for area '" + areaName + "'!");
                player.sendMessage(ChatColor.GRAY + "Create a backup first with /rewind backup " + areaName);
                return true;
            }
            backup = backups.get(0); // Latest backup
            finalBackupId = backup.getId();
        }

        player.sendMessage(ChatColor.YELLOW + "Exporting backup '" + finalBackupId + "' of area '" + areaName
                + "' to .schem file...");

        // Export asynchronously to avoid blocking the main thread
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                File schematicFile = exportBackupToSchematic(area, backup, areaName, finalBackupId);

                // Send success message on main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage(ChatColor.GREEN + "Successfully exported to: " + schematicFile.getName());
                    player.sendMessage(ChatColor.GRAY + "Plugin export folder: " + schematicFile.getAbsolutePath());
                    // Also show WorldEdit schematics folder location
                    File serverRoot = plugin.getServer().getWorldContainer();
                    File pluginsFolder = new File(serverRoot, "plugins");
                    File worldEditFolder = new File(pluginsFolder, "WorldEdit");
                    File schematicsFolder = new File(worldEditFolder, "schematics");
                    File worldEditFile = new File(schematicsFolder, schematicFile.getName());
                    player.sendMessage(
                            ChatColor.GRAY + "WorldEdit schematics folder: " + worldEditFile.getAbsolutePath());
                    player.sendMessage(ChatColor.GRAY + "You can now use //schem load "
                            + schematicFile.getName().replace(".schem", "") + " in WorldEdit");
                });

            } catch (Exception e) {
                plugin.getLogger().severe(
                        "Failed to export area '" + areaName + "' backup '" + finalBackupId + "': " + e.getMessage());
                e.printStackTrace();

                // Send error message on main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage(ChatColor.RED + "Failed to export backup to schematic file!");
                    player.sendMessage(ChatColor.GRAY + "Check the console for details.");
                });
            }
        });

        return true;
    }

    /**
     * Find a backup by its ID string
     */
    private AreaBackup findBackupById(String areaName, String backupId) {
        List<AreaBackup> backups = backupManager.getBackupHistory(areaName);
        return backups.stream()
                .filter(backup -> backup.getId().equals(backupId))
                .findFirst()
                .orElse(null);
    }

    private File exportBackupToSchematic(ProtectedArea area, AreaBackup backup, String areaName, String backupId)
            throws Exception {
        // Calculate area dimensions
        int minX = Math.min(area.getPos1().getBlockX(), area.getPos2().getBlockX());
        int minY = Math.min(area.getPos1().getBlockY(), area.getPos2().getBlockY());
        int minZ = Math.min(area.getPos1().getBlockZ(), area.getPos2().getBlockZ());
        int maxX = Math.max(area.getPos1().getBlockX(), area.getPos2().getBlockX());
        int maxY = Math.max(area.getPos1().getBlockY(), area.getPos2().getBlockY());
        int maxZ = Math.max(area.getPos1().getBlockZ(), area.getPos2().getBlockZ());

        // Create WorldEdit region
        BlockVector3 min = BlockVector3.at(minX, minY, minZ);
        BlockVector3 max = BlockVector3.at(maxX, maxY, maxZ);
        CuboidRegion region = new CuboidRegion(min, max);

        // Create clipboard
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

        // Get WorldEdit world
        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(area.getPos1().getWorld());

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
            // Copy blocks from backup to clipboard
            Map<String, BlockInfo> blocks = backup.getBlocks();
            for (Map.Entry<String, BlockInfo> entry : blocks.entrySet()) {
                String[] coords = entry.getKey().split(",");
                if (coords.length != 3)
                    continue;

                try {
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    int z = Integer.parseInt(coords[2]);

                    BlockInfo blockInfo = entry.getValue();
                    // Use BlockData from BlockInfo to preserve orientation and properties
                    org.bukkit.block.data.BlockData blockData = blockInfo.getBlockData();
                    BlockState blockState = BukkitAdapter.adapt(blockData);

                    // Set block in clipboard
                    BlockVector3 pos = BlockVector3.at(x, y, z);
                    if (region.contains(pos)) {
                        clipboard.setBlock(pos, blockState);
                    }

                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid coordinates in backup: " + entry.getKey());
                }
            }

            // Prepare export file: use only area name, add numeric suffix if needed
            File exportsFolder = fileManager.getExportsFolder();
            String baseName = areaName;
            String fileName = baseName + ".schem";
            File schematicFile = new File(exportsFolder, fileName);
            int counter = 1;
            while (schematicFile.exists()) {
                fileName = baseName + "_" + counter + ".schem";
                schematicFile = new File(exportsFolder, fileName);
                counter++;
            }

            // Write schematic file
            try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC
                    .getWriter(new FileOutputStream(schematicFile))) {
                writer.write(clipboard);
            }
            plugin.getLogger()
                    .info("Exported area '" + areaName + "' backup '" + backupId + "' to: " + schematicFile.getName());
            // After export, copy to WorldEdit schematics folder
            copyToWorldEditSchematics(schematicFile);
            return schematicFile;
        }
    }

    /**
     * Copies the schematic file to WorldEdit's schematics folder for direct access
     */
    private void copyToWorldEditSchematics(File schematicFile) {
        try {
            // Get WorldEdit plugin folder (serverRoot/plugins/WorldEdit)
            File serverRoot = plugin.getServer().getWorldContainer();
            File pluginsFolder = new File(serverRoot, "plugins");
            File worldEditFolder = new File(pluginsFolder, "WorldEdit");
            File schematicsFolder = new File(worldEditFolder, "schematics");
            if (!schematicsFolder.exists()) {
                schematicsFolder.mkdirs();
            }
            // Use same filename logic: area name only, numeric suffix if needed
            String baseName = schematicFile.getName().replaceFirst("\\.schem$", "");
            String fileName = baseName + ".schem";
            File destFile = new File(schematicsFolder, fileName);
            int counter = 1;
            while (destFile.exists()) {
                fileName = baseName + "_" + counter + ".schem";
                destFile = new File(schematicsFolder, fileName);
                counter++;
            }
            // Copy file
            try (java.io.FileInputStream in = new java.io.FileInputStream(schematicFile);
                    java.io.FileOutputStream out = new java.io.FileOutputStream(destFile)) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
            }
            plugin.getLogger().info("Copied schematic to WorldEdit folder: " + destFile.getAbsolutePath());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to copy schematic to WorldEdit schematics folder: " + e.getMessage());
        }
    }

    private boolean isWorldEditAvailable() {
        try {
            Class.forName("com.sk89q.worldedit.WorldEdit");
            return plugin.getServer().getPluginManager().getPlugin("WorldEdit") != null;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Area name completion
            String partial = args[0].toLowerCase();
            for (Map.Entry<String, ProtectedArea> entry : areaManager.getProtectedAreas().entrySet()) {
                ProtectedArea area = entry.getValue();
                if (permissionManager.canExport(player, area)) {
                    String areaName = entry.getKey();
                    if (areaName.toLowerCase().startsWith(partial)) {
                        completions.add(areaName);
                    }
                }
            }
        } else if (args.length == 2) {
            // Backup ID completion
            String areaName = args[0];
            ProtectedArea area = areaManager.getArea(areaName);
            if (area != null && permissionManager.canExport(player, area)) {
                String partial = args[1].toLowerCase();
                List<AreaBackup> backups = backupManager.getBackupHistory(areaName);
                for (AreaBackup backup : backups) {
                    String backupId = backup.getId();
                    if (backupId.toLowerCase().startsWith(partial)) {
                        completions.add(backupId);
                    }
                }
            }
        }

        return completions;
    }
}

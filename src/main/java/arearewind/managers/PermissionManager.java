package arearewind.managers;
import arearewind.data.ProtectedArea;
import org.bukkit.entity.Player;

public class PermissionManager {
    public static final String PERMISSION_USE = "arearewind.use";
    public static final String PERMISSION_ADMIN = "arearewind.admin";
    public static final String PERMISSION_CREATE = "arearewind.create";
    public static final String PERMISSION_DELETE = "arearewind.delete";
    public static final String PERMISSION_BACKUP = "arearewind.backup";
    public static final String PERMISSION_RESTORE = "arearewind.restore";
    public static final String PERMISSION_TRUST = "arearewind.trust";
    public static final String PERMISSION_EXPORT = "arearewind.export";
    public static final String PERMISSION_IMPORT = "arearewind.import";
    public static final String PERMISSION_VISUALIZE = "arearewind.visualize";
    public static final String PERMISSION_GUI = "arearewind.gui";

    public boolean hasUsePermission(Player player) {
        return player.hasPermission(PERMISSION_USE) || player.hasPermission(PERMISSION_ADMIN);
    }

    public boolean hasAdminPermission(Player player) {
        return player.hasPermission(PERMISSION_ADMIN);
    }

    public boolean hasAreaPermission(Player player, ProtectedArea area) {
        if (hasAdminPermission(player)) return true;
        if (area.getOwner().equals(player.getUniqueId())) return true;
        if (area.getTrustedPlayers().contains(player.getUniqueId())) return true;
        return false;
    }

    public boolean canCreateArea(Player player) {
        return hasAdminPermission(player) || player.hasPermission(PERMISSION_CREATE);
    }

    public boolean canDeleteArea(Player player, ProtectedArea area) {
        if (hasAdminPermission(player)) return true;
        if (area.getOwner().equals(player.getUniqueId()) && player.hasPermission(PERMISSION_DELETE)) return true;
        return false;
    }

    public boolean canCreateBackup(Player player, ProtectedArea area) {
        if (!player.hasPermission(PERMISSION_BACKUP)) return false;
        return hasAreaPermission(player, area);
    }

    public boolean canRestoreBackup(Player player, ProtectedArea area) {
        if (!player.hasPermission(PERMISSION_RESTORE)) return false;
        return hasAreaPermission(player, area);
    }

    public boolean canModifyTrust(Player player, ProtectedArea area) {
        if (hasAdminPermission(player)) return true;
        if (area.getOwner().equals(player.getUniqueId()) && player.hasPermission(PERMISSION_TRUST)) return true;
        return false;
    }

    public boolean canExport(Player player, ProtectedArea area) {
        if (!player.hasPermission(PERMISSION_EXPORT)) return false;
        return hasAreaPermission(player, area);
    }

    public boolean canImport(Player player) {
        return player.hasPermission(PERMISSION_IMPORT) || hasAdminPermission(player);
    }

    public boolean canVisualize(Player player, ProtectedArea area) {
        if (!player.hasPermission(PERMISSION_VISUALIZE)) return false;
        return hasAreaPermission(player, area);
    }

    public boolean canUseGUI(Player player) {
        return player.hasPermission(PERMISSION_GUI) || hasUsePermission(player);
    }

    public boolean canModifyBoundaries(Player player, ProtectedArea area) {
        if (hasAdminPermission(player)) return true;
        return area.getOwner().equals(player.getUniqueId());
    }

    public boolean canRenameArea(Player player, ProtectedArea area) {
        if (hasAdminPermission(player)) return true;
        return area.getOwner().equals(player.getUniqueId());
    }

    public boolean canViewAreaInfo(Player player, ProtectedArea area) {
        if (!hasUsePermission(player)) return false;
        return hasAreaPermission(player, area);
    }

    public String getPermissionLevelString(Player player, ProtectedArea area) {
        if (hasAdminPermission(player)) return "Admin";
        if (area.getOwner().equals(player.getUniqueId())) return "Owner";
        if (area.getTrustedPlayers().contains(player.getUniqueId())) return "Member";
        return "None";
    }

    public boolean canViewBackupHistory(Player player, ProtectedArea area) {
        return hasAdminPermission(player) || area.getOwner().equals(player.getUniqueId());
    }

    public boolean canUndoRedo(Player player, ProtectedArea area) {
        return hasAdminPermission(player) || area.getOwner().equals(player.getUniqueId());
    }
}
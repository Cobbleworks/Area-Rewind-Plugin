# Area Rewind Icon Feature - Testing Guide

## Overview

The Area Rewind plugin now supports custom icons for both protected areas and their backup entries. This allows players to visually distinguish between different areas and backups in the GUI for better organization and user experience.

## Features Implemented

### 1. Custom Area Icons

- Areas can have custom Material icons instead of the default GRASS_BLOCK
- Icons are displayed in the Areas GUI page
- Icons persist through server restarts (saved in areas.yml)
- Default icon: GRASS_BLOCK

### 2. Custom Backup Icons

- Individual backup entries can have custom Material icons instead of the default CHEST
- Icons are displayed in the Backups GUI page
- Icons persist through server restarts (saved in backup files)
- Default icon: CHEST

### 3. SetIcon Command

- **Area Icon:** `/rewind seticon <area> <material>`
- **Backup Icon:** `/rewind seticon backup <area> <backup_id> <material>`
- Full tab completion for area names, backup IDs, and common materials
- Permission requirement: `arearewind.manage`
- Validates that materials are valid items (not AIR or non-items)

## Command Examples

```
# Set area icon to a diamond block
/rewind seticon myhouse DIAMOND_BLOCK

# Set area icon to an emerald block
/rewind seticon storage EMERALD_BLOCK

# Set a specific backup's icon to an ender chest
/rewind seticon backup myhouse 20240811-143052-a1b2c3d4 ENDER_CHEST

# Set backup icon to a red shulker box
/rewind seticon backup storage 20240811-150123-e5f6g7h8 RED_SHULKER_BOX
```

## Common Materials Suggested

### Area Icons

- GRASS_BLOCK (default)
- DIAMOND_BLOCK, EMERALD_BLOCK, GOLD_BLOCK, IRON_BLOCK
- REDSTONE_BLOCK, LAPIS_BLOCK, COAL_BLOCK, QUARTZ_BLOCK
- OBSIDIAN, BEDROCK, STONE, COBBLESTONE, BRICKS

### Backup Icons

- CHEST (default)
- ENDER_CHEST, BARREL, SHULKER_BOX
- Colored Shulker Boxes: WHITE_SHULKER_BOX, RED_SHULKER_BOX, etc.
- Utility items: COMPASS, CLOCK, BOOK, PAPER, NAME_TAG

## Testing Steps

### Setup

1. Start the Minecraft server with the updated plugin
2. Create a test area using `/rewind pos1`, `/rewind pos2`, `/rewind save testarea`
3. Create a few backups using `/rewind backup testarea`

### Test Area Icons

1. Open the areas GUI: `/rewind gui`
2. Note the default GRASS_BLOCK icon for the test area
3. Set a custom icon: `/rewind seticon testarea DIAMOND_BLOCK`
4. Reopen the GUI and verify the icon changed to a diamond block
5. Restart the server and verify the icon persists

### Test Backup Icons

1. Open the backups GUI for the test area by clicking on it
2. Note the default CHEST icons for all backups
3. Get a backup ID using `/rewind history testarea`
4. Set a custom icon: `/rewind seticon backup testarea <backup_id> ENDER_CHEST`
5. Reopen the backups GUI and verify the specific backup now shows an ender chest
6. Restart the server and verify the backup icon persists

### Test Error Handling

1. Try invalid material: `/rewind seticon testarea INVALID_MATERIAL`
2. Try non-item material: `/rewind seticon testarea AIR`
3. Try non-existent area: `/rewind seticon nonexistent STONE`
4. Try non-existent backup: `/rewind seticon backup testarea invalid-id CHEST`

### Test Permissions

1. Test with a player who doesn't have `arearewind.manage` permission
2. Verify they cannot use the seticon command
3. Test with area owner vs non-owner permissions

## Expected Behavior

### Success Cases

- Custom icons appear immediately in GUIs after setting
- Icons persist through server restarts
- Tab completion works for areas, backup IDs, and materials
- Success messages show the material name that was set

### Error Cases

- Invalid materials show helpful error messages with examples
- Permission denied messages for unauthorized players
- Not found messages for non-existent areas/backups
- Clear usage messages for incorrect command syntax

## Benefits

1. **Visual Organization:** Players can quickly identify different areas and backups
2. **Personalization:** Players can customize their areas with meaningful icons
3. **Better UX:** Color-coded or themed icons improve navigation in the GUI
4. **Backup Management:** Different icon types can represent different backup purposes

## Future Enhancements

Potential future improvements could include:

- Preset icon themes (e.g., "house", "farm", "storage")
- Icon inheritance (backups inherit area icon by default)
- GUI-based icon picker instead of command-only
- Custom texture pack integration
- Icon categories and filters in GUI

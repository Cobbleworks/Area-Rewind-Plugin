# Custom Skulls and Container Contents Fix

## Issues Fixed

This update addresses four critical issues with block restoration:

### 1. Custom Player Heads (Skulls) from "Skulls" Plugin

**Problem**: Custom skinned player heads created by the "skulls" plugin were being restored as regular player heads, losing their custom textures.

**Solution**: Enhanced the NBT data capture and restoration for skulls with multiple approaches:

- Improved skull profile detection using reflection for multiple API versions
- Added robust texture data preservation through PlayerProfile and OwnerProfile APIs
- Implemented fallback methods for different server implementations
- Enhanced direct field access for GameProfile data
- Added detection of custom skulls based on display names, lore, and NBT content

### 2. Chiseled Bookshelf Contents Not Saved

**Problem**: Books placed in chiseled bookshelves were not being backed up or restored properly.

**Solution**:

- Added `CHISELED_BOOKSHELF` to the list of blocks with complex NBT data
- Implemented explicit container handling for chiseled bookshelves in backup creation
- Added specialized restoration method `restoreChiseledBookshelfContents()`
- Ensured both NBT-based and manual container restoration approaches work

### 3. Brewing Stand Contents Not Saved

**Problem**: Potions and ingredients in brewing stands were not being preserved during backup/restore operations.

**Solution**:

- Added `BREWING_STAND` to the list of blocks requiring NBT data preservation
- Implemented explicit container handling for brewing stands in backup creation
- Added specialized restoration method `restoreBrewingStandContents()`
- Enhanced container detection to properly identify brewing stands as containers

### 4. Lectern Contents Not Saved

**Problem**: Books placed in lecterns were not being backed up or restored properly.

**Solution**:

- Added explicit container handling for lecterns in backup creation
- Added specialized restoration method `restoreLecternContents()`
- Enhanced container detection to properly handle lectern inventories
- Ensured both NBT-based and manual container restoration approaches work

## Technical Implementation

### BackupManager.java Changes

1. **Enhanced Container Detection**: Added explicit handling for brewing stands, chiseled bookshelves, and lecterns in `createBlockInfo()` method
2. **Specialized Restoration**: Created dedicated methods for restoring specific container types
3. **Improved Error Handling**: Better logging and fallback mechanisms for container restoration

### NBTDataManager.java Changes

1. **Custom Skull Detection**: Significantly improved `isCustomPlayerHead()` method with multiple detection approaches
2. **Profile Texture Checking**: New `hasProfileTextures()` helper method to detect custom texture data
3. **Enhanced NBT Capture**: Added brewing stands to complex NBT data detection
4. **Multi-API Support**: Improved reflection-based access for different server implementations

## Testing Recommendations

After applying this fix, test the following scenarios:

1. **Custom Skulls**:

   - Place custom skulls from the "skulls" plugin
   - Create backup and restore
   - Verify textures are preserved

2. **Chiseled Bookshelves**:

   - Fill bookshelves with written books and book & quill
   - Create backup and restore
   - Verify all books are preserved in correct positions

3. **Brewing Stands**:

   - Add water bottles and nether wart to brewing stands
   - Start brewing process if desired
   - Create backup and restore
   - Verify all items and brewing state are preserved

4. **Lecterns**:
   - Place written books or book & quill in lecterns
   - Set specific pages if desired
   - Create backup and restore
   - Verify books are preserved with correct page positions

## Compatibility

This fix maintains backward compatibility with existing backups while improving support for:

- Modern Bukkit/Spigot/Paper APIs
- Multiple server implementations
- Various versions of the "skulls" plugin
- Other custom skull creation plugins

## Performance Impact

The changes have minimal performance impact:

- Container detection is only performed during backup creation
- Reflection-based skull handling only occurs for actual skull blocks
- Specialized restoration methods only run when needed
- All changes maintain the existing batch processing approach

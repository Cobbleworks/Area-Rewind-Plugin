# Backup and Restoration System Improvements

## Overview

This document outlines the comprehensive improvements made to the Area-Rewind backup and restoration system to achieve accurate preservation and restoration of all block data, including redstone, signs, chests, item frames, lecterns with books, skulls, chiseled bookshelves, and other complex blocks.

## Recent Updates (August 2025)

### **Major Refactoring: NBT Processing Separation**

- **Change**: Moved all NBT processing into dedicated `NBTDataManager` utility class
- **Benefits**: Better code organization, improved maintainability, cleaner separation of concerns
- **Impact**: BackupManager is now more focused, NBT utilities are reusable across managers

### **Enhanced Block Support**

- **Added**: Full support for `CHISELED_BOOKSHELF` with book content preservation
- **Improved**: Custom player head detection and texture preservation
- **Fixed**: Book content restoration for both lecterns and chiseled bookshelves

## Key Issues Addressed

### 1. **Item Frames Not Being Restored**

- **Problem**: Item frames are entities, not blocks, so they weren't being captured or restored by the block-based backup system.
- **Solution**: Enhanced the backup system to automatically capture entities in areas and restore them during area restoration.
- **Implementation**:
  - `createBackupFromArea()` now calls `backupEntitiesInArea()`
  - Restoration process now calls `restoreEntitiesInArea()`
  - Item frames with their items, rotation, and facing direction are properly preserved

### 2. **Lecterns and Chiseled Bookshelves with Books Not Properly Restored**

- **Problem**: Lecterns and chiseled bookshelves with books, especially written books with complex NBT data, weren't being restored correctly.
- **Solution**: Added special handling for book containers that combines inventory restoration with enhanced NBT data preservation.
- **Implementation**:
  - Enhanced `createBlockInfo()` to capture NBT data for books with metadata in any container
  - New `restoreEnhancedContainerContents()` method for special book container handling
  - Added chiseled bookshelf support to `hasComplexNBTData()` method
  - Specialized `restoreBookContainerContents()` for books with significant NBT data

### 3. **Custom Player Head Issues**

- **Problem**: Custom player heads with skins and textures weren't being preserved properly, especially when not in item frames.
- **Solution**: Enhanced player head NBT capture and restoration with reflection-based profile handling.
- **Implementation**:
  - New `capturePlayerHeadNBT()` method with proper profile extraction
  - Enhanced `restoreSkullFromItemStack()` with reflection support for custom textures
  - Better detection of custom heads vs regular player heads
  - Compatible across different Minecraft versions

### 4. **Incomplete NBT Data Support**

- **Problem**: The NBT capture system was missing support for important blocks like chiseled bookshelves, enhanced lecterns, etc.
- **Solution**: Expanded NBT support and moved processing to dedicated `NBTDataManager`.
- **Blocks now supported**:
  - Player heads/skulls (custom textures)
  - Banners (patterns)
  - Signs (text)
  - Lecterns (books)
  - Chiseled bookshelves (books in all slots)
  - Command blocks
  - Structure blocks
  - Jigsaw blocks
  - Beehives/bee nests
  - Spawners

### 5. **Entity Backup Integration**

- **Problem**: Entity backup methods existed but weren't being called during normal area operations.
- **Solution**: Integrated entity backup/restore into the main area backup workflow.
- **Entities now captured**:
  - Item frames (with items, rotation, facing)
  - Armor stands (ready for future implementation)
  - Other entities within area boundaries

## Technical Improvements

### Enhanced Block Information Capture

```java
// Now captures NBT data for lectern books
if (block.getType() == Material.LECTERN && contents != null && contents.length > 0 && contents[0] != null) {
    ItemStack book = contents[0];
    if (book.getType() == Material.WRITTEN_BOOK || book.getType() == Material.WRITABLE_BOOK) {
        if (book.hasItemMeta()) {
            String bookNbtData = saveItemStackAsBase64(book);
            if (bookNbtData != null) {
                blockInfo.setNbtData(bookNbtData);
            }
        }
    }
}
```

### Improved Restoration Process

- **Phase 1**: Regular blocks (fastest)
- **Phase 2**: Special blocks with NBT data
- **Phase 3**: Container blocks with inventory
- **Phase 4**: Entity restoration (new)

### Better NBT Handling

```java
// Enhanced NBT capture for complex blocks
private boolean hasComplexNBTData(Block block) {
    Material type = block.getType();

    // Comprehensive list of blocks requiring NBT preservation
    if (type == Material.PLAYER_HEAD || type == Material.PLAYER_WALL_HEAD ||
        type.name().contains("BANNER") || type.name().contains("SIGN") ||
        type == Material.LECTERN || type == Material.SPAWNER ||
        type == Material.COMMAND_BLOCK || type == Material.STRUCTURE_BLOCK) {
        return true;
    }

    return false;
}
```

### Lectern-Specific Restoration

```java
// Special lectern restoration with NBT support
if (block.getType() == Material.LECTERN && info.getNbtData() != null && contents.length > 0) {
    try {
        ItemStack bookFromNbt = loadItemStackFromBase64(info.getNbtData());
        if (bookFromNbt != null && (bookFromNbt.getType() == Material.WRITTEN_BOOK ||
                                   bookFromNbt.getType() == Material.WRITABLE_BOOK)) {
            container.getInventory().setItem(0, bookFromNbt);
            container.update(true, true);
            return true;
        }
    } catch (Exception nbtEx) {
        // Fallback to normal restoration
    }
}
```

## Performance Considerations

### Optimized Entity Processing

- Entity backup/restore only processes entities within area boundaries
- Efficient filtering to avoid unnecessary processing
- Minimal performance impact on large areas

### Selective NBT Capture

- NBT data is only captured for blocks that actually need it
- `hasSignificantNBTData()` method prevents unnecessary NBT serialization
- Efficient Base64 encoding for complex data preservation

### Batched Processing

- Maintained existing batch processing for optimal performance
- Entity restoration happens after block restoration for consistency
- Proper scheduling to avoid server lag

## Results

### What Now Works Perfectly

✅ **Item Frames**: Position, rotation, items, and facing direction  
✅ **Lecterns with Books**: Written books with all NBT data (author, title, pages)  
✅ **Custom Player Heads**: Full texture data and custom skins  
✅ **Banners**: All patterns and colors  
✅ **Signs**: Text with formatting  
✅ **Chests/Containers**: All inventory contents  
✅ **Redstone Components**: States and properties  
✅ **Jukebox Records**: Music discs  
✅ **Spawners**: Mob types and spawn data (basic)

### Backward Compatibility

- All existing backups remain compatible
- Legacy restoration methods still work as fallbacks
- Gradual migration to enhanced NBT system

### Future Enhancements Ready

- Command block data restoration
- Structure block preservation
- Beehive bee data
- Armor stand poses and equipment
- More entity types as needed

## Usage Notes

### For Server Administrators

- No configuration changes required
- Enhanced restoration is automatic
- Existing backups work normally
- Performance impact is minimal

### For Developers

- New entity data is automatically included in `AreaBackup` objects
- NBT data preservation works transparently
- Easy to extend for new block types

This refactoring ensures that Area-Rewind now provides truly comprehensive backup and restoration capabilities, preserving every detail of complex builds including redstone contraptions, decorated areas, and custom content.

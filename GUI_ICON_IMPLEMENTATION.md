# GUI Icon Feature Implementation

## Overview

I have successfully implemented a comprehensive GUI-based icon setting feature for both areas and backups in the Area-Rewind plugin. This enhances the existing command-line icon functionality with an intuitive graphical interface.

## Features Implemented

### 1. MaterialSelectorGUIPage

- **New GUI Page**: Created a dedicated material selection interface with pagination support
- **Two Material Categories**:
  - Area Materials: Common building blocks and decorative items suitable for area representation
  - Backup Materials: Storage containers, tools, and utility items suitable for backup representation
- **Intuitive Interface**: Players can browse materials visually and select them by clicking
- **Item in Hand Support**: Players can use the item they're currently holding as an icon
- **Pagination**: Supports browsing through large lists of materials with navigation buttons

### 2. Area Icon Setting (AreaSettingsGUIPage)

- **Set Icon Button**: Added a new "Set Icon" button to the area settings management panel
- **Current Icon Display**: The button shows the area's current icon material
- **Permission-Based**: Only players with `canModifyBoundaries` permission can access this feature
- **Visual Feedback**: Displays current icon material in the button's lore text

### 3. Backup Icon Setting (BackupsGUIPage)

- **Middle Click Support**: Added middle-click functionality to backup items for setting icons
- **Clear Instructions**: Added tooltip text to backup items explaining the middle-click action
- **Permission-Based**: Only players with `canModifyBoundaries` permission can set backup icons
- **Seamless Integration**: Works alongside existing backup interaction options

### 4. Enhanced GUIManager

- **New Methods**: Added `openMaterialSelector()` methods with overloads for pagination
- **Updated Dependencies**: Modified constructor to include FileManager for backup saving
- **Click Handling**: Added support for material selector GUI click events
- **Title Recognition**: Updated GUI detection to recognize material selector windows

## Technical Implementation Details

### Material Collections

- **Area Materials (35 items)**: Includes blocks like GRASS_BLOCK, DIAMOND_BLOCK, EMERALD_BLOCK, etc.
- **Backup Materials (35 items)**: Includes containers like CHEST, ENDER_CHEST, various SHULKER_BOXes, etc.
- **Smart Formatting**: Material names are automatically formatted from ENUM_CASE to Title Case

### GUI Layout

- **Pagination Layout**: 35 items per page (5 rows × 7 columns)
- **Info Section**: Row 35-44 for information display
- **Navigation Section**: Row 45-53 for pagination and action buttons
- **Cancel/Hand Item**: Options to cancel selection or use item in hand

### Integration Points

- **Area Settings**: Slot 24 in the area settings GUI (moved Transfer Ownership to slot 26)
- **Backup Interaction**: Middle-click on any backup item in the backups GUI
- **Material Selection**: Full-page GUI with pagination for material browsing

## User Experience

### For Area Icons:

1. Open area settings GUI (`/rewind gui <area>` → Area Settings)
2. Click the "Set Icon" button (shows current icon)
3. Browse available materials or use item in hand
4. Click desired material to set as area icon
5. Automatic save and return to area settings

### For Backup Icons:

1. Open backups GUI (`/rewind gui <area>`)
2. Middle-click on any backup item
3. Browse available materials or use item in hand
4. Click desired material to set as backup icon
5. Automatic save and return to backups GUI

## Backward Compatibility

- **Command Support**: All existing `/rewind seticon` commands continue to work
- **Default Icons**: Areas default to GRASS_BLOCK, backups default to CHEST
- **Permission System**: Uses existing permission structure
- **Data Storage**: Leverages existing icon storage in ProtectedArea and AreaBackup classes

## Error Handling

- **Permission Checks**: Validates permissions before allowing icon changes
- **Item Validation**: Ensures selected materials are valid items (not AIR or non-items)
- **Area Validation**: Checks that areas and backups exist before processing
- **User Feedback**: Provides clear success/error messages

This implementation provides a user-friendly alternative to command-line icon setting while maintaining full compatibility with existing functionality.

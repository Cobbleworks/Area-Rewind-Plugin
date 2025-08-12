# GUI Pagination Implementation

## Overview

I have successfully implemented pagination functionality for the Area Rewind plugin's GUI system to support data that exceeds the regular inventory grid. This implementation provides a scalable solution for displaying large numbers of areas and backups.

## Key Components

### 1. GUIPaginationHelper.java

A utility class that manages pagination logic and data:

**Features:**

- `PaginationData` class to store pagination state per player
- `PaginationInfo` class containing pagination calculation results
- Helper methods for calculating pagination based on total items and items per page
- Navigation button generation (Previous/Next/Info)
- Pagination action detection and handling
- Player-specific pagination data management

**Key Methods:**

- `calculatePagination()` - Calculates page boundaries and navigation state
- `addPaginationButtons()` - Adds navigation buttons to inventories
- `checkPaginationClick()` - Determines if clicked item is pagination-related
- `getPaginationData()` / `updatePaginationData()` - Manages per-player pagination state

### 2. Enhanced IGUIPage Interface

Updated to support pagination:

**New Methods:**

- `openGUI(Player player, int page)` - Opens GUI at specific page
- `handlePaginationAction()` - Handles pagination navigation events

### 3. Updated AreasGUIPage

Enhanced areas GUI with pagination support:

**Configuration:**

- 35 items per page (5 rows × 7 columns)
- Navigation buttons in bottom row
- Dynamic title showing current page info

**Features:**

- Filters areas by player permissions before pagination
- Previous/Next navigation with visual indicators
- Page information display
- Preserves current page on refresh
- Smart button positioning based on pagination state

### 4. Updated BackupsGUIPage

Enhanced backup management GUI with pagination:

**Configuration:**

- 28 items per page (4 rows × 7 columns)
- Dedicated space for area info and controls
- Page navigation integrated with existing controls

**Features:**

- Chronological backup display with pagination
- Maintains backup interaction functionality (restore, preview, compare)
- Page-aware control button positioning
- Seamless navigation between backup pages

### 5. Enhanced GUIManager

Updated to support pagination across all GUI types:

**New Features:**

- Pagination-aware GUI opening methods
- Automatic pagination data cleanup on GUI close
- Enhanced event handling for pagination actions

## Implementation Details

### Pagination Layout Strategy

**Areas GUI (54 slots):**

```
[0-34]  Area items (35 slots)
[35-44] Reserved/unused
[45-53] Navigation (Prev, Controls, Next, etc.)
```

**Backups GUI (54 slots):**

```
[0-27]  Backup items (28 slots)
[28-34] Reserved/unused
[35-44] Area information
[45-53] Navigation and controls
```

### Navigation Button Design

- **Previous Page**: Arrow with page info
- **Next Page**: Arrow with page info
- **Page Info**: Book showing current/total pages and item counts
- **Disabled State**: Gray glass panes when no more pages

### State Management

- Per-player pagination data stored in `GUIPaginationHelper`
- Automatic cleanup on GUI close/player disconnect
- Page state preserved during GUI refresh
- Area-specific pagination for backup GUIs

## Benefits

1. **Scalability**: Supports unlimited areas and backups
2. **Performance**: Only renders items for current page
3. **User Experience**: Intuitive navigation with clear page indicators
4. **Flexibility**: Easy to adjust items per page
5. **Maintainability**: Centralized pagination logic
6. **Memory Efficient**: Pagination data automatically cleaned up

## Usage Examples

### Opening Areas GUI at Specific Page

```java
guiManager.openAreasGUI(player, 2); // Opens page 3 (0-indexed)
```

### Opening Backups GUI at Specific Page

```java
guiManager.openBackupsGUI(player, "MyArea", 1); // Opens page 2 of backups
```

### Navigation Handling

The system automatically:

- Detects pagination button clicks
- Updates page state
- Reopens GUI at new page
- Preserves GUI context (area name for backups)

## Technical Notes

- **Thread Safety**: Uses ConcurrentHashMap for pagination data
- **Null Safety**: Comprehensive null checking throughout
- **Performance**: Efficient pagination calculations
- **Compatibility**: Backward compatible with existing GUI functionality

## Future Enhancements

Potential improvements that could be added:

1. Jump-to-page functionality
2. Search/filter integration
3. Configurable items per page
4. Sorting options with pagination
5. Bookmarking frequently accessed pages

This implementation provides a robust foundation for handling large datasets in the GUI while maintaining excellent user experience and system performance.

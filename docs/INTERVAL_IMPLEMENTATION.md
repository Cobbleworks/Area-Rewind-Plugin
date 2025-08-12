# Interval Functionality Implementation

## Overview

The interval functionality has been successfully implemented to allow automatic backup restoration based on configured intervals. This feature enables users to set up automated restoration of areas to specific backup states at regular intervals.

## Features Implemented

### 1. Enhanced IntervalCommand

- **Command**: `/rewind interval <set|remove|check> <area> [minutes] [backup_id]`
- **Set Interval**: `/rewind interval set <area> <minutes> <backup_id>` - Sets up automatic restoration
- **Remove Interval**: `/rewind interval remove <area>` - Disables automatic restoration
- **Check Interval**: `/rewind interval check <area>` - Shows current interval configuration
- **Tab Completion**: Provides suggestions for minutes (1, 5, 10, 15, 30, 60, 120) and available backup IDs

### 2. GUI Integration

#### Areas GUI Page

- **Interval Status**: Shows auto-restore status in area item lore
  - `Auto-Restore: Active (30m #2)` - Shows interval minutes and backup ID
  - `Auto-Restore: Inactive` - Shows when no interval is set

#### Backups GUI Page

- **Area Information Panel**: Displays interval configuration in the area info section
- **Auto-Restore Settings Button**: New control button for managing intervals
  - Shows as a CLOCK icon when active, GRAY_DYE when inactive
  - Left Click: Disable auto-restore (when active)
  - Right Click: Show configuration commands (when active)
  - Click: Show setup instructions (when inactive)
- **Backup Item Enhancement**: Individual backup items show if they're used for auto-restore

### 3. Backend Integration

- **IntervalManager**: Properly integrated with the main plugin lifecycle
- **Command System**: IntervalCommand fully implemented with proper validation
- **Plugin Integration**: IntervalManager added to main plugin class with proper initialization and shutdown
- **Dependency Management**: Updated all GUI pages and command handlers to use shared IntervalManager instance

## Usage Examples

### Setting Up Auto-Restore

```
/rewind interval set myarea 30 2
```

This will restore "myarea" to backup #2 every 30 minutes.

### Checking Current Settings

```
/rewind interval check myarea
```

Shows: "Current interval for myarea: 30 minutes (backup 2)"

### Removing Auto-Restore

```
/rewind interval remove myarea
```

Disables automatic restoration for the area.

### GUI Usage

1. Open the Areas GUI (`/rewind gui`)
2. Click on an area to open its backup management
3. Use the "Auto-Restore Settings" button (clock icon) to configure intervals
4. Individual backup items show if they're used for auto-restore

## Technical Implementation

### Core Components

1. **IntervalManager**: Manages active intervals and executes scheduled restorations
2. **IntervalCommand**: Handles command-line interface for interval management
3. **GUI Integration**: Updated AreasGUIPage and BackupsGUIPage with interval information
4. **Plugin Lifecycle**: Proper initialization and shutdown of interval services

### Key Features

- **Real-time Updates**: GUI shows live interval status
- **Permission Integration**: Respects existing area permissions
- **Error Handling**: Validates backup existence and area access
- **User Feedback**: Clear messages and visual indicators
- **Persistence**: Intervals survive server restarts (managed by IntervalManager)

### Validation

- Backup ID validation ensures the specified backup exists
- Area existence validation prevents invalid configurations
- Permission checks ensure users can only manage intervals for areas they have access to
- Positive interval validation ensures meaningful time periods

## Files Modified

### Core Classes

- `AreaRewindPlugin.java` - Added IntervalManager integration
- `CommandHandler.java` - Updated to use shared IntervalManager
- `IntervalCommand.java` - Fully implemented command functionality

### GUI Components

- `GUIManager.java` - Updated constructor to include IntervalManager
- `AreasGUIPage.java` - Added interval status display in area items
- `BackupsGUIPage.java` - Added auto-restore settings panel and backup indicators

### Manager Integration

- `IntervalManager.java` - Used existing class, integrated into plugin lifecycle

## Compatibility

- Maintains all existing functionality
- Backward compatible with existing commands
- No breaking changes to plugin API
- Preserves existing permission system

## User Experience

The interval functionality is now seamlessly integrated into both the command-line interface and the GUI system, providing users with multiple ways to configure and monitor automatic backup restoration for their protected areas.

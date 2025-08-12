# Undo/Redo System Revamp

## Overview

The undo/redo logic has been completely revamped to provide a more intuitive experience. The old system simply moved between existing backup indices, which wasn't very useful. The new system creates hidden "beforeRestore" backups that provide true undo functionality.

## Key Changes

### 1. Hidden Backup Support

- Added `hidden` field to `AreaBackup` class
- Hidden backups don't appear in backup lists or GUI
- Used for storing states before restore operations

### 2. New Undo/Redo Logic

**Before (Old System):**

- Maintained an "undo pointer" that tracked position in backup history
- Undo/redo simply moved this pointer and restored to that backup
- No actual state preservation before operations

**After (New System):**

- When a backup is restored, the current area state is saved as a hidden "beforeRestore" backup
- Undo restores from this hidden backup (returning to pre-restore state)
- Redo can restore the state before the undo operation
- Each operation preserves the state before it occurs

### 3. BackupManager Changes

- Added `beforeRestoreBackups` map to track hidden backups per area
- Modified `restoreArea()` to create hidden backup before restoration
- Rewrote `undoArea()` to use hidden backups instead of pointer navigation
- Updated `redoArea()` to work with the new system
- Modified `getBackupHistory()` to filter out hidden backups
- Added `getAllBackups()` method for internal use with hidden backups
- Updated cleanup methods to handle hidden backups

### 4. Command Updates

- **UndoCommand**: Now undoes the last restore operation specifically
- **RedoCommand**: Redoes the last undo operation
- Updated descriptions and messages to be clearer
- Improved error messages to explain when undo/redo is not available

### 5. GUI Updates

- Updated button tooltips to explain the new functionality
- Removed the "Current State" pointer display (no longer relevant)
- Added clearer descriptions for undo/redo buttons
- Updated diff command integration

## How It Works Now

1. **Normal Usage**: Create and restore backups as usual
2. **After Restoring**: A hidden backup of the pre-restore state is automatically created
3. **Undo**: Restores the area to its state before the last restore operation
4. **Redo**: If you've used undo, redo will restore to the state before the undo

## Benefits

- **Intuitive**: Undo actually undoes the last restore operation
- **Safe**: Never lose data - the state before each operation is preserved
- **Clean**: Hidden backups don't clutter the backup list
- **Flexible**: Can undo and redo back and forth between states

## Migration Notes

- Existing backups and areas are not affected
- The old `undoPointers` system is kept for compatibility but not used in the new logic
- No data loss during the transition

## Example Workflow

1. Area has state A
2. Restore backup X → Area becomes state X, hidden backup of A is created
3. Undo → Area returns to state A, hidden backup of X is created
4. Redo → Area returns to state X, hidden backup of A is created
5. Can continue undoing/redoing between A and X states

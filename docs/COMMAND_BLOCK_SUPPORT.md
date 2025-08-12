# Command Block Support for Area Rewind

## Overview

The Area Rewind plugin now supports command block execution for the restore functionality through the new `restoreblock` command. This allows for automated area restoration without requiring a player to be online.

## Command Block Command

### Syntax

```
/rewind restoreblock <area> <backup_id|latest|oldest> [world]
```

### Parameters

- `area`: The name of the protected area to restore
- `backup_id`: The backup ID to restore from, or use:
  - `latest`: Restore from the most recent backup
  - `oldest`: Restore from the oldest backup
- `world` (optional): Specify the world name for validation (ensures the area is in the expected world)

### Examples

```
/rewind restoreblock myarea latest
/rewind restoreblock myarea 5
/rewind restoreblock myarea oldest
/rewind restoreblock myarea latest world
```

## Differences from Player Command

### What's Different:

1. **No Permission Checks**: Command blocks bypass player permission validation
2. **Console Logging**: All output is logged to the server console in addition to command block output
3. **World Validation**: Optional world parameter for additional safety
4. **No Player Context**: Doesn't require a player to be online or have area access
5. **Simplified Feedback**: Designed for automation rather than interactive use

### What's the Same:

- Same backup restoration logic
- Same backup validation
- Creates undo snapshots for manual reversal if needed
- Supports all backup ID formats (numeric, latest, oldest)

## Security Considerations

1. **Command Block Protection**: Ensure command blocks are properly protected from unauthorized access
2. **World Validation**: Use the optional world parameter when automating across multiple worlds
3. **Backup Verification**: Always verify backup IDs exist before automation
4. **Monitoring**: Check server logs for restoration results

## Use Cases

### Automated Reset Systems

```
# Reset an area to its original state every hour
/rewind restoreblock spawn_area oldest world
```

### Event Management

```
# Restore arena to clean state after events
/rewind restoreblock pvp_arena latest
```

### Scheduled Maintenance

```
# Restore areas during maintenance windows
/rewind restoreblock build_area 0
```

## Error Handling

The command provides clear error messages for:

- Area not found
- Invalid backup IDs
- World mismatches
- Empty backup history

All errors are logged to both the command output and server console for debugging.

## Integration with Existing Features

- **Undo/Redo**: Creates hidden backups for manual undo if needed
- **Backup History**: Uses the same backup system as player commands
- **Area Management**: Validates against existing protected areas
- **Logging**: Integrates with existing plugin logging system

## Troubleshooting

### Common Issues:

1. **"Area not found"**: Verify the area name exists in the area list
2. **"Backup ID not found"**: Check available backup IDs with the history command
3. **"World mismatch"**: Ensure the area is in the specified world
4. **No output**: Check server console for detailed error messages

### Debugging:

- Enable debug logging in the plugin configuration
- Check server console for detailed execution logs
- Verify command block setup and permissions
- Test with player commands first to ensure area/backup validity

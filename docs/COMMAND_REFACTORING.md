# Command Handler Refactoring

## Overview

The `CommandHandler.java` has been refactored to improve maintainability, extensibility, and follow better design patterns. The new architecture uses the Command Pattern to separate concerns and make the codebase more modular.

## New Architecture

### 1. Command Pattern Implementation

- **`Command` interface**: Defines the contract for all commands
- **`BaseCommand` abstract class**: Provides common functionality and validation methods
- **Individual command classes**: Each command has its own class implementing specific logic

### 2. Command Registry

- **`CommandRegistry`**: Manages command registration, routing, and execution
- Supports command aliases
- Handles permission checking
- Provides centralized tab completion

### 3. Organized Package Structure

```
commands/
├── base/
│   ├── Command.java           # Command interface
│   └── BaseCommand.java       # Base implementation with common utilities
├── registry/
│   └── CommandRegistry.java   # Command management and routing
├── area/                      # Area management commands
│   ├── Pos1Command.java
│   ├── Pos2Command.java
│   ├── SaveCommand.java
│   └── ContractCommand.java
├── backup/                    # Backup operation commands
│   ├── BackupCommand.java
│   └── RestoreCommand.java
├── utility/                   # Utility commands
│   └── HelpCommand.java
└── CommandHandlerNew.java     # Main handler using new architecture
```

## Benefits

### 1. **Single Responsibility Principle**

- Each command class has one responsibility
- BaseCommand handles common validation logic
- CommandRegistry manages command routing

### 2. **Open/Closed Principle**

- Easy to add new commands without modifying existing code
- New commands just need to implement the Command interface

### 3. **DRY (Don't Repeat Yourself)**

- Common validation logic extracted to BaseCommand
- Shared utilities available to all commands

### 4. **Better Testability**

- Individual commands can be unit tested in isolation
- Dependencies are clearly defined

### 5. **Improved Maintainability**

- Smaller, focused classes are easier to understand and modify
- Clear separation of concerns

## Migration Strategy

### Phase 1: Gradual Migration ✅

- Create new command structure alongside existing code
- Implement core commands (pos1, pos2, save, backup, restore)
- `CommandHandlerNew` falls back to old system for unmigrated commands

### Phase 2: Complete Migration (TODO)

- Migrate remaining commands to new structure
- Update main plugin to use `CommandHandlerNew`
- Remove old `CommandHandler`

### Phase 3: Enhancement (Future)

- Add command categories for better organization
- Implement async command execution where appropriate
- Add command history/logging
- Add command-specific permissions

## How to Add New Commands

### 1. Create Command Class

```java
public class MyNewCommand extends BaseCommand {
    @Override
    public boolean execute(Player player, String[] args) {
        // Command logic here
        return true;
    }

    @Override
    public String getName() {
        return "mynewcommand";
    }

    @Override
    public String getUsage() {
        return "/rewind mynewcommand <args>";
    }

    // Implement other required methods...
}
```

### 2. Register in CommandHandlerNew

```java
commandRegistry.registerCommand(new MyNewCommand(/* dependencies */));
```

### 3. Add Tab Completion (Optional)

```java
@Override
public List<String> getTabCompletions(Player player, String[] args) {
    // Return completion suggestions
}
```

## Current Implementation Status

### ✅ Implemented Commands

- `pos1` - Set first position
- `pos2` - Set second position
- `save` - Create new area
- `contract` - Contract area
- `backup` - Create backup
- `restore` - Restore from backup
- `help` - Show help information

### 📋 TODO: Commands to Migrate

- `delete` - Delete area
- `list` - List areas
- `info` - Area information
- `teleport`/`tp` - Teleport to area
- `rollback` - Rollback to time
- `undo`/`redo` - Undo/redo operations
- `history` - View backup history
- `cleanup` - Clean old backups
- `scan` - Scan for changes
- `diff`/`compare` - Compare backups
- `preview` - Preview backup
- `particle` - Set particles
- `trust`/`untrust` - Trust management
- `permissions` - View permissions
- `show`/`hide` - Visualization
- `gui`/`menu` - Open GUI
- `interval` - Auto-restore intervals
- `status` - System status
- `reload` - Reload config
- `tool` - Tool management

## Usage

To switch to the new command handler, update the main plugin class to use `CommandHandlerNew` instead of `CommandHandler`. The new handler is backward compatible and will handle the transition gracefully.

## Future Enhancements

1. **Command Categories**: Group related commands for better help organization
2. **Async Execution**: Move long-running operations to async execution
3. **Command Validation**: Add parameter validation annotations
4. **Command History**: Track command usage for debugging
5. **Contextual Help**: Show relevant help based on current context
6. **Command Aliases**: Support custom user-defined aliases
7. **Command Metrics**: Track usage statistics and performance

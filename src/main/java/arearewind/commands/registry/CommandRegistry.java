package arearewind.commands.registry;

import arearewind.commands.base.Command;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Registry for managing all commands
 */
public class CommandRegistry {

    private final Map<String, Command> commands = new HashMap<>();

    /**
     * Register a command
     */
    public void registerCommand(Command command) {
        commands.put(command.getName().toLowerCase(), command);
    }

    /**
     * Get command by name
     */
    public Command getCommand(String name) {
        return commands.get(name.toLowerCase());
    }

    /**
     * Get all registered commands
     */
    public Collection<Command> getAllCommands() {
        return commands.values();
    }

    /**
     * Get command name completions
     */
    public List<String> getCommandCompletions(String partial) {
        String lowerPartial = partial.toLowerCase();

        return commands.keySet().stream()
                .filter(name -> name.startsWith(lowerPartial))
                .collect(Collectors.toList());
    }

    /**
     * Execute a command
     */
    public boolean executeCommand(Player player, String commandName, String[] args) {
        Command command = getCommand(commandName);
        if (command == null) {
            return false;
        }

        // Check permissions
        String requiredPermission = command.getRequiredPermission();
        if (requiredPermission != null && !player.hasPermission(requiredPermission)) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        return command.execute(player, args);
    }

    /**
     * Get tab completions for a command
     */
    public List<String> getTabCompletions(Player player, String commandName, String[] args) {
        Command command = getCommand(commandName);
        if (command == null) {
            return new ArrayList<>();
        }

        return command.getTabCompletions(player, args);
    }
}

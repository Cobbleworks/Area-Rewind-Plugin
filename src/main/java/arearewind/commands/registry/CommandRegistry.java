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
    private final Map<String, String> aliases = new HashMap<>();

    /**
     * Register a command
     */
    public void registerCommand(Command command) {
        commands.put(command.getName().toLowerCase(), command);

        // Register aliases
        for (String alias : command.getAliases()) {
            aliases.put(alias.toLowerCase(), command.getName().toLowerCase());
        }
    }

    /**
     * Get command by name or alias
     */
    public Command getCommand(String name) {
        String commandName = name.toLowerCase();

        // Check if it's an alias first
        if (aliases.containsKey(commandName)) {
            commandName = aliases.get(commandName);
        }

        return commands.get(commandName);
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

        List<String> completions = new ArrayList<>();

        // Add command names
        completions.addAll(commands.keySet().stream()
                .filter(name -> name.startsWith(lowerPartial))
                .collect(Collectors.toList()));

        // Add aliases
        completions.addAll(aliases.keySet().stream()
                .filter(alias -> alias.startsWith(lowerPartial))
                .collect(Collectors.toList()));

        return completions;
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

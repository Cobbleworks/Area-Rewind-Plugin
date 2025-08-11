package arearewind.commands.base;

import org.bukkit.entity.Player;

import java.util.List;

/**
 * Interface for all Area Rewind commands
 */
public interface Command {

    /**
     * Execute the command
     * 
     * @param player The player executing the command
     * @param args   Command arguments (excluding the main command and subcommand)
     * @return true if command was handled successfully
     */
    boolean execute(Player player, String[] args);

    /**
     * Get tab completions for this command
     * 
     * @param player The player requesting completions
     * @param args   Current arguments
     * @return List of possible completions
     */
    List<String> getTabCompletions(Player player, String[] args);

    /**
     * Get the command name (e.g., "save", "restore")
     * 
     * @return command name
     */
    String getName();

    /**
     * Get command aliases
     * 
     * @return list of aliases for this command
     */
    List<String> getAliases();

    /**
     * Get command description for help
     * 
     * @return command description
     */
    String getDescription();

    /**
     * Get command usage
     * 
     * @return usage string
     */
    String getUsage();

    /**
     * Get required permission for this command
     * 
     * @return permission string or null if no specific permission required
     */
    String getRequiredPermission();
}

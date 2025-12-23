package ce.ajneb97.manager.commandregister;

import ce.ajneb97.ConditionalEvents;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;

public class CommandRegisterManager {

    private final ConditionalEvents plugin;

    public CommandRegisterManager(ConditionalEvents plugin) {
        this.plugin = plugin;
    }

    public CommandMap getCommandMap() {
        CommandMap commandMap = null;
        try {
            Field f = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            f.setAccessible(true);
            commandMap = (CommandMap) f.get(Bukkit.getPluginManager());
        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException | SecurityException e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting CommandMap", e);
        }
        return commandMap;
    }

    public void registerCommands() {
        FileConfiguration config = plugin.getConfigsManager().getMainConfigManager().getConfig();
        if (config.contains("Config.register_commands")) {
            List<String> commands = config.getStringList("Config.register_commands");
            for (String commandName : commands) {
                registerCommand(commandName);
            }
        }
    }

    public void registerCommand(String commandName) {
        CECommand ceCommand = new CECommand(commandName);
        CommandMap commandMap = getCommandMap();
        commandMap.register("ConditionalEvents", ceCommand);
    }
}

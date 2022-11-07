package ce.ajneb97.managers.commandregister;

import ce.ajneb97.ConditionalEvents;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommandRegisterManager {

    private ConditionalEvents plugin;

    public CommandRegisterManager(ConditionalEvents plugin){
        this.plugin = plugin;
    }

    public CommandMap getCommandMap() {
        CommandMap commandMap = null;
        try {
            Field f = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            f.setAccessible(true);
            commandMap = (CommandMap) f.get(Bukkit.getPluginManager());
        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException | SecurityException e) {
            e.printStackTrace();
        }
        return commandMap;
    }

    public void registerCommands(){
        FileConfiguration config = plugin.getConfigsManager().getMainConfigManager().getConfig();
        if(config.contains("Config.register_commands")){
            List<String> commands = config.getStringList("Config.register_commands");
            for(String commandName : commands){
                registerCommand(commandName);
            }
        }
    }

    public void registerCommand(String commandName) {
        CECommand ceCommand = new CECommand(commandName);
        CommandMap commandMap = getCommandMap();
        commandMap.register("ConditionalEvents",ceCommand);
    }
}

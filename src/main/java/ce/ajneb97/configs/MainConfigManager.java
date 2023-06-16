package ce.ajneb97.configs;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.managers.MessagesManager;
import ce.ajneb97.managers.RepetitiveManager;
import ce.ajneb97.model.CEEvent;
import ce.ajneb97.model.CustomEventProperties;
import ce.ajneb97.model.EventType;
import ce.ajneb97.model.ToConditionGroup;
import ce.ajneb97.model.actions.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MainConfigManager {

    private CEConfig configFile;
    private ConditionalEvents plugin;

    private boolean updateNotifications;
    private ArrayList<ToConditionGroup> toConditionGroups;
    public MainConfigManager(ConditionalEvents plugin){
        this.plugin = plugin;
        this.configFile = new CEConfig("config.yml",plugin,null);
        configFile.registerConfig();
        checkMessagesUpdate();
    }

    public void configure(){
        FileConfiguration config = configFile.getConfig();

        updateNotifications = config.getBoolean("Config.update_notification");
        toConditionGroups = new ArrayList<ToConditionGroup>();
        String path = "Config.to_condition_groups";
        if(config.contains(path)){
            for(String key : config.getConfigurationSection(path).getKeys(false)){
                ToConditionGroup group = new ToConditionGroup(key,config.getStringList(path+"."+key));
                toConditionGroups.add(group);
            }
        }

        //Configure messages
        MessagesManager msgManager = new MessagesManager();
        msgManager.setTimeSeconds(config.getString("Messages.seconds"));
        msgManager.setTimeMinutes(config.getString("Messages.minutes"));
        msgManager.setTimeHours(config.getString("Messages.hours"));
        msgManager.setTimeDays(config.getString("Messages.days"));
        msgManager.setPrefix(config.getString("Messages.prefix"));
        msgManager.setPlaceholderAPICooldownNameError(config.getString("Messages.placeholderAPICooldownNameError"));
        msgManager.setPlaceholderAPICooldownReady(config.getString("Messages.placeholderAPICooldownReady"));

        this.plugin.setMessagesManager(msgManager);
    }

    public boolean reloadConfig(){
        if(!configFile.reloadConfig()){
            return false;
        }
        configure();
        return true;
    }

    public FileConfiguration getConfig(){
        return configFile.getConfig();
    }

    public CEConfig getConfigFile(){
        return this.configFile;
    }

    public void saveConfig(){
        configFile.saveConfig();
    }

    public void checkMessagesUpdate(){
        Path pathConfig = Paths.get(configFile.getRoute());
        try{
            String text = new String(Files.readAllBytes(pathConfig));
            if(!text.contains("commandCallError:")){
                getConfig().set("Messages.commandCallError", "&cUse &7/ce call <event> (optional)%variable1%=<value1>;%variableN%=<valueN>");
                getConfig().set("Messages.commandCallInvalidEvent", "&cYou can only execute a CALL event.");
                getConfig().set("Messages.commandCallCorrect", "&aEvent &7%event% &asuccessfully executed.");
                getConfig().set("Messages.commandCallFailed", "&cEvent &7%event% &ccould not be executed. Maybe a format error?");
                saveConfig();
            }
            if(!text.contains("register_commands:")){
                List<String> commands = new ArrayList<>();
                getConfig().set("Config.register_commands", commands);
                saveConfig();
            }
            if(!text.contains("placeholderAPICooldownReady:")){
                getConfig().set("Messages.placeholderAPICooldownReady", "Ready!");
                getConfig().set("Messages.placeholderAPICooldownNameError", "No event with that name!");
                saveConfig();
            }
            if(!text.contains("eventDataResetAll:")){
                getConfig().set("Messages.eventDataResetAll", "&aAll data reset for player &e%player%&a!");
                saveConfig();
            }
            if(!text.contains("eventDataReset:")){
                getConfig().set("Messages.eventDataReset", "&aData reset for player &e%player% &aon event &e%event%&a!");
                saveConfig();
            }
            if(!text.contains("data_save_time:")){
                getConfig().set("Config.data_save_time", 5);
                saveConfig();
            }
            if(!text.contains("commandDebugError:")){
                getConfig().set("Messages.commandDebugError", "&cUse &7/ce debug <event>");
                getConfig().set("Messages.debugEnabled", "&aDebug now enabled for event &7%event%&a!");
                getConfig().set("Messages.debugDisabled", "&aDebug disabled for event &7%event%&a!");
                getConfig().set("Messages.onlyPlayerCommand", "&cThis command can be only used by a player.");
                getConfig().set("Messages.playerDoesNotExists", "&cThat player doesn''t have any data.");
                saveConfig();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public boolean isUpdateNotifications() {
        return updateNotifications;
    }

    public ToConditionGroup getToConditionGroup(String name){
        for(ToConditionGroup group : toConditionGroups){
            if(group.getName().equals(name)) {
                return group;
            }
        }
        return null;
    }
}

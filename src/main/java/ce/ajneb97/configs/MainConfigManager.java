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
        this.configFile = new CEConfig("config.yml",plugin);
        configFile.registerConfig();
        checkMessagesUpdate();
    }

    public void configure(){
        FileConfiguration config = configFile.getConfig();

        //Configure events
        ArrayList<CEEvent> events = new ArrayList<CEEvent>();
        if(config.contains("Events")){
            for(String key : config.getConfigurationSection("Events").getKeys(false)){
                String path = "Events."+key;
                List<String> conditions = new ArrayList<String>();
                List<ActionGroup> actionGroups = new ArrayList<ActionGroup>();
                boolean oneTime = false;
                String ignoreWithPermission = null;
                long cooldown = 0;
                boolean enabled = true;

                CEEvent event = new CEEvent(key);
                EventType eventType = null;
                try{
                    eventType = EventType.valueOf(config.getString(path+".type").toUpperCase());
                }catch(Exception e){
                    continue;
                }

                if(config.contains(path+".conditions")) {
                    conditions = config.getStringList(path+".conditions");
                }
                if(config.contains(path+".actions")) {
                    for(String groupName : config.getConfigurationSection(path+".actions").getKeys(false)) {
                        List<String> actionsList = config.getStringList(path+".actions."+groupName);
                        List<CEAction> ceActions = new ArrayList<CEAction>();
                        for(String action : actionsList){
                            ActionTargeter targeter = new ActionTargeter(ActionTargeterType.NORMAL);

                            if(action.startsWith("to_all: ")){
                                // to_all: message: hi
                                action = action.replace("to_all: ","");
                                targeter.setType(ActionTargeterType.TO_ALL);
                            }else if(action.startsWith("to_target: ")){
                                // to_target: message: hi
                                action = action.replace("to_target: ","");
                                targeter.setType(ActionTargeterType.TO_TARGET);
                            }else if(action.startsWith("to_world: ") || action.startsWith("to_range: ")
                                    || action.startsWith("to_condition: ")){
                                // to_world: parkour: message: hi
                                // to_range: 5;true: message: hi
                                // to_condition: toConditionGroup1: message: hi
                                ActionTargeterType targeterType = null;
                                if(action.startsWith("to_world: ")){
                                    targeterType = ActionTargeterType.TO_WORLD;
                                }else if(action.startsWith("to_range: ")){
                                    targeterType = ActionTargeterType.TO_RANGE;
                                }else if(action.startsWith("to_condition: ")){
                                    targeterType = ActionTargeterType.TO_CONDITION;
                                }
                                targeter.setType(targeterType);

                                action = action.replace(targeterType.name().toLowerCase()+": ","");
                                String parameter = action.substring(0, action.indexOf(":"));
                                String replace = action.substring(0, action.indexOf(":")+2);
                                action = action.replace(replace, "");

                                targeter.setParameter(parameter);
                            }

                            String actionTypeText = null;
                            ActionType actionType = null;
                            try{
                                actionTypeText = action.substring(0,action.indexOf(":"));
                                actionType = ActionType.valueOf(actionTypeText.toUpperCase());
                            }catch(Exception e){
                                continue;
                            }

                            String actionLine = action.replace(actionTypeText+": ","");

                            CEAction ceAction = new CEAction(actionType,actionLine,targeter);
                            ceActions.add(ceAction);
                        }

                        ActionGroup actionGroup = new ActionGroup(groupName,ceActions);
                        actionGroups.add(actionGroup);
                    }
                }
                if(config.contains(path+".cooldown")) {
                    cooldown = Long.valueOf(config.getString(path+".cooldown"));
                }
                if(config.contains(path+".ignore_with_permission")) {
                    ignoreWithPermission = config.getString(path+".ignore_with_permission");
                }
                if(config.contains(path+".one_time")) {
                    oneTime = Boolean.valueOf(config.getString(path+".one_time"));
                }
                if(config.contains(path+".enabled")) {
                    enabled = Boolean.valueOf(config.getString(path+".enabled"));
                }

                event.setEventType(eventType);
                event.setConditions(conditions);
                event.setActionGroups(actionGroups);
                event.setCooldown(cooldown);
                event.setIgnoreWithPermission(ignoreWithPermission);
                event.setOneTime(oneTime);
                event.setEnabled(enabled);

                if(event.getEventType().equals(EventType.CUSTOM)) {
                    String eventPackage = config.getString(path+".custom_event_data.event");
                    String playerVariable = null;
                    if(config.contains(path+".custom_event_data.player_variable")) {
                        playerVariable = config.getString(path+".custom_event_data.player_variable");
                    }
                    List<String> variablesToCapture = new ArrayList<String>();
                    if(config.contains(path+".custom_event_data.variables_to_capture")) {
                        variablesToCapture = config.getStringList(path+".custom_event_data.variables_to_capture");
                    }

                    event.setCustomEventProperties(new CustomEventProperties(
                            eventPackage,playerVariable,variablesToCapture
                    ));
                }

                if(event.getEventType().equals(EventType.REPETITIVE) || event.getEventType().equals(EventType.REPETITIVE_SERVER)){
                    int repetitiveTime = config.getInt(path+".repetitive_time");
                    RepetitiveManager repetitiveManager = new RepetitiveManager(plugin,event,repetitiveTime);
                    event.setRepetitiveManager(repetitiveManager);
                    if(event.isEnabled()){
                        repetitiveManager.start();
                    }
                }

                events.add(event);
            }
        }

        plugin.getEventsManager().setEvents(events);

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

        this.plugin.setMessagesManager(msgManager);
    }

    public void reloadConfig(){
        configFile.reloadConfig();
        configure();
    }

    public FileConfiguration getConfig(){
        return configFile.getConfig();
    }

    public void saveConfig(){
        configFile.saveConfig();
    }

    public void checkMessagesUpdate(){
        Path pathConfig = Paths.get(configFile.getRoute());
        try{
            String text = new String(Files.readAllBytes(pathConfig));
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

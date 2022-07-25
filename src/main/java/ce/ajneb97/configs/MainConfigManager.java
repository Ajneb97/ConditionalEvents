package ce.ajneb97.configs;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.managers.MessagesManager;
import ce.ajneb97.managers.RepetitiveManager;
import ce.ajneb97.model.CEEvent;
import ce.ajneb97.model.CustomEventProperties;
import ce.ajneb97.model.EventType;
import ce.ajneb97.model.actions.ActionGroup;
import ce.ajneb97.model.actions.ActionTargeter;
import ce.ajneb97.model.actions.ActionType;
import ce.ajneb97.model.actions.CEAction;
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
    public MainConfigManager(ConditionalEvents plugin){
        this.plugin = plugin;
        this.configFile = new CEConfig("config.yml",plugin);
        configFile.registerConfig();
        checkMessagesUpdate();
    }

    public void configure(){
        FileConfiguration config = configFile.getConfig();

        //Configure events
        ArrayList<CEEvent> events = new ArrayList<>();
        if(config.contains("Events")){
            for(String key : config.getConfigurationSection("Events").getKeys(false)){
                String path = "Events."+key;
                List<String> conditions = new ArrayList<>();
                List<ActionGroup> actionGroups = new ArrayList<>();
                boolean oneTime = false;
                String oneTimeErrorMessage = null;
                String ignoreWithPermission = null;
                long cooldown = 0;
                String cooldownErrorMessage = null;
                boolean enabled = true;

                CEEvent event = new CEEvent(key);
                EventType eventType = EventType.valueOf(config.getString(path+".type").toUpperCase());

                if(config.contains(path+".conditions")) {
                    conditions = config.getStringList(path+".conditions");
                }
                if(config.contains(path+".actions")) {
                    for(String groupName : config.getConfigurationSection(path+".actions").getKeys(false)) {
                        List<String> actionsList = config.getStringList(path+".actions."+groupName);
                        List<CEAction> ceActions = new ArrayList<>();
                        for(String action : actionsList){
                            ActionTargeter targeter = ActionTargeter.NORMAL;
                            if(action.startsWith("to_all: ")){
                                // to_all: message: hi
                                action = action.replace("to_all: ","");
                                targeter = ActionTargeter.TO_ALL;
                            }else if(action.startsWith("to_target: ")){
                                // to_target: message: hi
                                action = action.replace("to_target: ","");
                                targeter = ActionTargeter.TO_TARGET;
                            }else if(action.startsWith("to_world: ")){
                                // to_world: parkour: message: hi
                                action = action.replace("to_world: ","");
                                String parameter = action.substring(0, action.indexOf(":"));
                                String replace = action.substring(0, action.indexOf(":")+2);
                                action = action.replace(replace, "");

                                targeter = ActionTargeter.TO_WORLD;
                                targeter.setParameter(parameter);
                            }else if(action.startsWith("to_range: ")){
                                // to_range: 5;true: message: hi
                                action = action.replace("to_range: ", "");
                                String parameter = action.substring(0, action.indexOf(":"));
                                String replace = action.substring(0, action.indexOf(":")+2);
                                action = action.replace(replace, "");

                                targeter = ActionTargeter.TO_RANGE;
                                targeter.setParameter(parameter);
                            }

                            String actionTypeText = action.substring(0,action.indexOf(":"));
                            ActionType actionType = ActionType.valueOf(actionTypeText.toUpperCase());
                            String actionLine = action.replace(actionTypeText+": ","");

                            CEAction ceAction = new CEAction(actionType,actionLine,targeter);
                            ceActions.add(ceAction);
                        }

                        ActionGroup actionGroup = new ActionGroup(groupName,ceActions);
                        actionGroups.add(actionGroup);
                    }
                }
                if(config.contains(path+".cooldown")) {
                    cooldown = Long.parseLong(config.getString(path+".cooldown"));
                }
                if(config.contains(path+".cooldown_error_message")) {
                    cooldownErrorMessage = config.getString(path+".cooldown_error_message");
                }
                if(config.contains(path+".ignore_with_permission")) {
                    ignoreWithPermission = config.getString(path+".ignore_with_permission");
                }
                if(config.contains(path+".one_time")) {
                    oneTime = Boolean.parseBoolean(config.getString(path+".one_time"));
                }
                if(config.contains(path+".one_time_error_message")) {
                    oneTimeErrorMessage = config.getString(path+".one_time_error_message");
                }
                if(config.contains(path+".enabled")) {
                    enabled = Boolean.parseBoolean(config.getString(path+".enabled"));
                }

                event.setEventType(eventType);
                event.setConditions(conditions);
                event.setActionGroups(actionGroups);
                event.setCooldown(cooldown);
                event.setCooldownErrorMessage(cooldownErrorMessage);
                event.setIgnoreWithPermission(ignoreWithPermission);
                event.setOneTime(oneTime);
                event.setOneTimeErrorMessage(oneTimeErrorMessage);
                event.setEnabled(enabled);

                if(event.getEventType().equals(EventType.CUSTOM)) {
                    String eventPackage = config.getString(path+".custom_event_data.event");
                    String playerVariable = null;
                    if(config.contains(path+".custom_event_data.player_variable")) {
                        playerVariable = config.getString(path+".custom_event_data.player_variable");
                    }
                    List<String> variablesToCapture = new ArrayList<>();
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

        //Configure messages
        MessagesManager msgManager = new MessagesManager();
        msgManager.setTimeSeconds(config.getString("Messages.seconds"));
        msgManager.setTimeMinutes(config.getString("Messages.minutes"));
        msgManager.setTimeHours(config.getString("Messages.hours"));
        msgManager.setTimeDays(config.getString("Messages.days"));

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
}

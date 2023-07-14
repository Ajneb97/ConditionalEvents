package ce.ajneb97.configs;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.managers.RepetitiveManager;
import ce.ajneb97.model.CEEvent;
import ce.ajneb97.model.CustomEventProperties;
import ce.ajneb97.model.EventType;
import ce.ajneb97.model.actions.*;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ConfigsManager {

    private MainConfigManager mainConfigManager;
    private PlayerConfigsManager playerConfigsManager;
    private DataFolderConfigManager dataFolderConfigManager;
    private ConditionalEvents plugin;
    public ConfigsManager(ConditionalEvents plugin){
        mainConfigManager = new MainConfigManager(plugin);
        playerConfigsManager = new PlayerConfigsManager(plugin);
        dataFolderConfigManager = new DataFolderConfigManager(plugin,"events");
        this.plugin = plugin;
    }

    public void configure(){
        mainConfigManager.configure();
        playerConfigsManager.configure();
        dataFolderConfigManager.configure();
        configureEvents();
    }

    public MainConfigManager getMainConfigManager() {
        return mainConfigManager;
    }

    public PlayerConfigsManager getPlayerConfigsManager() {
        return playerConfigsManager;
    }

    public void configureEvents(){
        ArrayList<CEConfig> ceConfigs = getEventConfigs();

        ArrayList<CEEvent> events = new ArrayList<CEEvent>();

        for(CEConfig configFile : ceConfigs){
            FileConfiguration config = configFile.getConfig();

            if(config.contains("Events")){
                for(String key : config.getConfigurationSection("Events").getKeys(false)){
                    String path = "Events."+key;
                    List<String> conditions = new ArrayList<String>();
                    List<ActionGroup> actionGroups = new ArrayList<ActionGroup>();
                    boolean oneTime = false;
                    String ignoreWithPermission = null;
                    long cooldown = 0;
                    boolean enabled = true;
                    boolean ignoreIfCancelled = false;
                    boolean allowMathFormulasInConditions = false;

                    List<String> preventCooldownActivationActionGroups = new ArrayList<String>();
                    List<String> preventOneTimeActivationActionGroups = new ArrayList<String>();

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
                                        || action.startsWith("to_condition: ") || action.startsWith("to_player: ")){
                                    // to_world: parkour: message: hi
                                    // to_range: 5;true: message: hi
                                    // to_condition: toConditionGroup1: message: hi
                                    // to_player: <player>: message: hi
                                    ActionTargeterType targeterType = null;
                                    if(action.startsWith("to_world: ")){
                                        targeterType = ActionTargeterType.TO_WORLD;
                                    }else if(action.startsWith("to_range: ")){
                                        targeterType = ActionTargeterType.TO_RANGE;
                                    }else if(action.startsWith("to_condition: ")){
                                        targeterType = ActionTargeterType.TO_CONDITION;
                                    }else if(action.startsWith("to_player: ")){
                                        targeterType = ActionTargeterType.TO_PLAYER;
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
                                    if(action.equalsIgnoreCase("close_inventory")){
                                        actionTypeText = action;
                                    }else{
                                        actionTypeText = action.substring(0,action.indexOf(":"));
                                    }

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
                    if(config.contains(path+".ignore_if_cancelled")) {
                        ignoreIfCancelled = Boolean.valueOf(config.getString(path+".ignore_if_cancelled"));
                    }
                    if(config.contains(path+".prevent_cooldown_activation")){
                        preventCooldownActivationActionGroups = config.getStringList(path+".prevent_cooldown_activation");
                    }
                    if(config.contains(path+".prevent_one_time_activation")){
                        preventOneTimeActivationActionGroups = config.getStringList(path+".prevent_one_time_activation");
                    }
                    if(config.contains(path+".allow_math_formulas_in_conditions")) {
                        allowMathFormulasInConditions = Boolean.valueOf(config.getString(path+".allow_math_formulas_in_conditions"));
                    }

                    event.setEventType(eventType);
                    event.setConditions(conditions);
                    event.setActionGroups(actionGroups);
                    event.setCooldown(cooldown);
                    event.setIgnoreWithPermission(ignoreWithPermission);
                    event.setOneTime(oneTime);
                    event.setEnabled(enabled);
                    event.setIgnoreIfCancelled(ignoreIfCancelled);
                    event.setPreventCooldownActivationActionGroups(preventCooldownActivationActionGroups);
                    event.setPreventOneTimeActivationActionGroups(preventOneTimeActivationActionGroups);
                    event.setAllowMathFormulasInConditions(allowMathFormulasInConditions);

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
                        RepetitiveManager repetitiveManager = new RepetitiveManager(plugin, event, repetitiveTime);
                        event.setRepetitiveManager(repetitiveManager);
                        if(event.isEnabled()){
                            repetitiveManager.start();
                        }
                    }

                    events.add(event);
                }
            }
        }

        plugin.getEventsManager().setEvents(events);
    }

    public void saveEvent(CEEvent event){
        ArrayList<CEConfig> ceConfigs = getEventConfigs();

        String eventName = event.getName();

        for(CEConfig configFile : ceConfigs){
            FileConfiguration config = configFile.getConfig();
            if(config.contains("Events."+eventName)){
                config.set("Events."+eventName+".enabled",event.isEnabled());
                configFile.saveConfig();
            }
        }
    }

    public ArrayList<CEConfig> getEventConfigs() {
        ArrayList<CEConfig> configs = new ArrayList<CEConfig>();

        configs.add(mainConfigManager.getConfigFile());
        configs.addAll(dataFolderConfigManager.getConfigs());

        return configs;
    }

    public boolean reload(){
        for(CEEvent event : plugin.getEventsManager().getEvents()){
            if(event.getRepetitiveManager() != null){
                event.getRepetitiveManager().end();
            }
        }

        playerConfigsManager.savePlayerData();
        if(!mainConfigManager.reloadConfig()){
            return false;
        }
        dataFolderConfigManager.reloadConfigs();
        configureEvents();

        plugin.reloadEvents();
        plugin.getVerifyManager().verifyEvents();
        plugin.reloadPlayerDataSaveTask();
        return true;
    }
}

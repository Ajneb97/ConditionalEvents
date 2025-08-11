package ce.ajneb97.managers;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.configs.model.CommonConfig;
import ce.ajneb97.model.CEEvent;
import ce.ajneb97.model.ConditionalType;
import ce.ajneb97.model.EventType;
import ce.ajneb97.model.actions.ActionGroup;
import ce.ajneb97.model.actions.ActionType;
import ce.ajneb97.model.actions.CEAction;
import ce.ajneb97.model.verify.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class VerifyManager {
    private ConditionalEvents plugin;
    private ArrayList<CEError> errors;
    public VerifyManager(ConditionalEvents plugin) {
        this.plugin = plugin;
        this.errors = new ArrayList<CEError>();
    }

    public void sendVerification(Player player) {
        player.sendMessage(MessagesManager.getColoredMessage("&f&l- - - - - - - - &b&lEVENTS VERIFY &f&l- - - - - - - -"));
        player.sendMessage(MessagesManager.getColoredMessage(""));
        if(errors.isEmpty()) {
            player.sendMessage(MessagesManager.getColoredMessage("&aThere are no errors in your events ;)"));
        }else {
            player.sendMessage(MessagesManager.getColoredMessage("&e&oHover on the errors to see more information."));
            for(CEError error : errors) {
                error.sendMessage(player);
            }
        }
        player.sendMessage(MessagesManager.getColoredMessage(""));
        player.sendMessage(MessagesManager.getColoredMessage("&f&l- - - - - - - - &b&lEVENTS VERIFY &f&l- - - - - - - -"));
    }

    public void verifyEvents() {
        this.errors = new ArrayList<CEError>();
        ArrayList<CEEvent> events = plugin.getEventsManager().getEvents();

        //Loaded events
        for(CEEvent event : events) {
            verifyEvent(event);
        }

        //Unloaded events
        ArrayList<CommonConfig> ceConfigs = plugin.getConfigsManager().getEventConfigs();
        for(CommonConfig ceConfig : ceConfigs){
            FileConfiguration config = ceConfig.getConfig();
            if(!config.contains("Events")){
                return;
            }
            for (String key : config.getConfigurationSection("Events").getKeys(false)) {
                String eventType = config.getString("Events."+key+".type");
                try{
                    EventType.valueOf(eventType.toUpperCase());
                }catch(Exception e){
                    errors.add(new CEErrorEventType(key, eventType));
                }

                String pathActions = "Events."+key+".actions";
                if(!config.contains(pathActions)) {
                    continue;
                }
                for (String groupName : config.getConfigurationSection(pathActions).getKeys(false)) {
                    String path = pathActions+"."+groupName;
                    List<String> actionsList = config.getStringList(path);
                    for(int i=0;i<actionsList.size();i++){
                        String action = actionsList.get(i);
                        String actionOriginal = action;
                        if(action.startsWith("to_all: ")){
                            action = action.replace("to_all: ","");
                        }else if(action.startsWith("to_target: ")){
                            action = action.replace("to_target: ","");
                        }else if(action.startsWith("to_world: ")){
                            action = action.replace("to_world: ","");
                            String replace = action.substring(0, action.indexOf(":")+2);
                            action = action.replace(replace, "");
                        }else if(action.startsWith("to_range: ")){
                            action = action.replace("to_range: ", "");
                            String replace = action.substring(0, action.indexOf(":")+2);
                            action = action.replace(replace, "");
                        }else if(action.startsWith("to_condition: ")){
                            action = action.replace("to_condition: ", "");
                            String replace = action.substring(0, action.indexOf(":")+2);
                            action = action.replace(replace, "");
                        }else if(action.startsWith("to_player: ")){
                            action = action.replace("to_player: ", "");
                            String replace = action.substring(0, action.indexOf(":")+2);
                            action = action.replace(replace, "");
                        }

                        try{
                            String actionTypeText;
                            if(action.equalsIgnoreCase("close_inventory") ||
                                    action.equalsIgnoreCase("clear_inventory")){
                                actionTypeText = action;
                            }else{
                                actionTypeText = action.substring(0,action.indexOf(":"));
                            }

                            //Check API actions
                            if(plugin.getApiManager().getApiAction(actionTypeText) != null){
                                continue;
                            }else{
                                ActionType.valueOf(actionTypeText.toUpperCase());
                            }
                        }catch(Exception e){
                            errors.add(new CEErrorAction(key, actionOriginal, (i+1), groupName));
                        }
                    }
                }
            }
        }
    }

    public void verifyEvent(CEEvent event) {
        List<String> conditions = event.getConditions();
        for(int i=0;i<conditions.size();i++) {
            if(!verifyCondition(conditions.get(i))) {
                errors.add(new CEErrorCondition(event.getName(),conditions.get(i), (i+1)));
            }
            if(!verifyRandomVariable(conditions.get(i))){
                errors.add(new CEErrorRandomVariable(event.getName(), conditions.get(i)));
            }
        }

        List<ActionGroup> actionGroups = event.getActionGroups();
        for(ActionGroup actionGroup : actionGroups){
            List<CEAction> actions = actionGroup.getActions();
            for(CEAction action : actions){
                if(!verifyRandomVariable(action.getActionLine())){
                    errors.add(new CEErrorRandomVariable(event.getName(), action.getActionLine()));
                }
            }
        }

    }

    private boolean verifyRandomVariable(String line){
        for(int c=0;c<line.length();c++) {
            if(line.charAt(c) == '%') {
                int startPos = c+1;
                if(startPos < line.length()) {
                    int lastPos = line.indexOf('%', c+1);
                    if(lastPos == -1) {
                        continue;
                    }
                    if(line.charAt(startPos) == ' ' || line.charAt(lastPos-1) == ' ') {
                        continue;
                    }
                    String variable = line.substring(startPos,lastPos);

                    if(variable.startsWith("random_") && !variable.startsWith("random_player")){
                        String[] sep = variable.split("_");
                        if(sep.length != 3){
                            return false;
                        }
                    }

                    c = lastPos;
                }
            }
        }
        return true;
    }

    public boolean verifyCondition(String line) {
        String conditionLine = line.split(" execute ")[0];
        String[] separatedConditions = null;
        if(conditionLine.contains(" or ")){
            separatedConditions = conditionLine.split(" or ");
        }else if(conditionLine.contains(" and ")){
            separatedConditions = conditionLine.split(" and ");
        }else{
            separatedConditions = new String[]{conditionLine};
        }

        for (String miniCondition : separatedConditions) {
            boolean textContainsConditional = textContainsConditional(miniCondition);
            if (!textContainsConditional) {
                return false;
            }
        }

        return true;
    }

    private boolean textContainsConditional(String text){
        for (ConditionalType conditionalType : ConditionalType.values()) {
            String textToFind = " " + conditionalType.getText() + " ";
            if(text.contains(textToFind)) {
                return true;
            }
        }
        return false;
    }
}

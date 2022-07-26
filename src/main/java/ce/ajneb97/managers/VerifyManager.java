package ce.ajneb97.managers;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.model.CEEvent;
import ce.ajneb97.model.EventType;
import ce.ajneb97.model.actions.ActionTargeter;
import ce.ajneb97.model.actions.ActionType;
import ce.ajneb97.model.verify.CEError;
import ce.ajneb97.model.verify.CEErrorAction;
import ce.ajneb97.model.verify.CEErrorCondition;
import ce.ajneb97.model.verify.CEErrorEventType;
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
        FileConfiguration config = plugin.getConfigsManager().getMainConfigManager().getConfig();
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
                    }

                    try{
                        String actionTypeText = action.substring(0,action.indexOf(":"));
                        ActionType.valueOf(actionTypeText.toUpperCase());
                    }catch(Exception e){
                        errors.add(new CEErrorAction(key, actionOriginal, (i+1), groupName));
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
        }
    }


    public boolean verifyCondition(String linea) {
        String[] sepExecute = linea.split(" execute ");
        String[] sepOr = sepExecute[0].split(" or ");
        for(int i=0;i<sepOr.length;i++) {
            String[] sep = sepOr[i].split(" ");
            if(sep.length < 3) {
                return false;
            }
            if(!sep[1].equals("!=") && !sep[1].equals("==") && !sep[1].equals(">=") &&
                    !sep[1].equals("<=") && !sep[1].equals(">") && !sep[1].equals("<")
                    && !sep[1].equals("equals") && !sep[1].equals("!equals") && !sep[1].equals("contains")
                    && !sep[1].equals("!contains") && !sep[1].equals("startsWith") && !sep[1].equals("!startsWith")
                    && !sep[1].equals("equalsIgnoreCase") && !sep[1].equals("!equalsIgnoreCase")) {
                return false;
            }
        }
        return true;
    }
}

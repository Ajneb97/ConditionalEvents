package ce.ajneb97.managers;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.model.CEEvent;
import ce.ajneb97.model.verify.CEError;
import ce.ajneb97.model.verify.CEErrorType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class VerifyManager {
    private ConditionalEvents plugin;
    private ArrayList<CEError> errors;
    public VerifyManager(ConditionalEvents plugin) {
        this.plugin = plugin;
        this.errors = new ArrayList<>();
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
        this.errors = new ArrayList<>();
        ArrayList<CEEvent> events = plugin.getEventsManager().getEvents();
        for(CEEvent event : events) {
            verifyEvent(event);
        }
    }

    public void verifyEvent(CEEvent event) {
        List<String> conditions = event.getConditions();
        for(int i=0;i<conditions.size();i++) {
            if(!verifyCondition(conditions.get(i))) {
                errors.add(new CEError(CEErrorType.INVALID_CONDITION, event.getName(), (i+1),conditions.get(i)));
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

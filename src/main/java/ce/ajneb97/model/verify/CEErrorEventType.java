package ce.ajneb97.model.verify;

import ce.ajneb97.utils.JSONMessage;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CEErrorEventType extends CEError {

    public CEErrorEventType(String event, String errorText) {
        super(event, errorText);
    }

    @Override
    public void sendMessage(Player player) {
        String message = "&c⚠ ";
        List<String> hover = new ArrayList<>();

        JSONMessage jsonMessage = new JSONMessage(player, message + "&7Event &6" + event + " &7has an invalid type.");
        hover.add("&eTHIS IS AN ERROR!");
        hover.add("&fThe type for this event is invalid, maybe");
        hover.add("&fyou misspelled it?:");
        for (String m : getFixedErrorText()) {
            hover.add("&c" + m);
        }
        hover.add(" ");
        hover.add("&fRemember to use a valid event types from this list:");
        hover.add("&ahttps://ajneb97.gitbook.io/conditionalevents/event-types");
        jsonMessage.hover(hover).send();
    }

}

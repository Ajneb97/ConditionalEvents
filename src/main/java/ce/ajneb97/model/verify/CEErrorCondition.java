package ce.ajneb97.model.verify;

import ce.ajneb97.utils.JSONMessage;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CEErrorCondition extends CEError{

    private int conditionLine;

    public CEErrorCondition(String event, String errorText,int conditionLine) {
        super(event, errorText);
        this.conditionLine = conditionLine;
    }

    @Override
    public void sendMessage(Player player) {
        String message = "&e⚠ ";
        List<String> hover = new ArrayList<String>();

        JSONMessage jsonMessage = new JSONMessage(player,message+"&7Condition &6"+conditionLine+" &7on Event &6"+event+" &7is not valid.");
        hover.add("&eTHIS IS A WARNING!");
        hover.add("&fThe condition defined for this event");
        hover.add("&fis probably not formatted correctly:");
        for(String m : getFixedErrorText()) {
            hover.add("&c"+m);
        }
        hover.add(" ");
        hover.add("&fRemember to use a valid condition from this list:");
        hover.add("&ahttps://ajneb97.gitbook.io/conditionalevents/conditions");
        jsonMessage.hover(hover).send();
    }

}

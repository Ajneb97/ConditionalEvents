package ce.ajneb97.model.verify;

import ce.ajneb97.utils.JSONMessage;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CEError {
    private CEErrorType type;
    private List<String> tooltip;

    private String eventName;
    private int errorLine;
    private String errorText;

    public CEError(CEErrorType type,String eventName,int errorLine,String errorText) {
        this.type = type;
        this.eventName = eventName;
        this.errorLine = errorLine;
        this.errorText = errorText;
    }
    public List<String> getTooltip() {
        return tooltip;
    }
    public void setTooltip(List<String> tooltip) {
        this.tooltip = tooltip;
    }
    public void sendMessage(Player player) {
        String message = "&e⚠ ";
        JSONMessage jsonMessage = null;
        List<String> hover = new ArrayList<>();

        List<String> sepText = new ArrayList<>();
        int currentPos = 0;
        for(int i=0;i<errorText.length();i++) {
            if(currentPos >= 35 && errorText.charAt(i) == ' ') {
                String m = errorText.substring(i-currentPos, i);
                currentPos = 0;
                sepText.add(m);
            }else {
                currentPos++;
            }
            if(i==errorText.length()-1) {
                String m = errorText.substring(i-currentPos+1, errorText.length());
                sepText.add(m);
            }
        }

        if(type == CEErrorType.INVALID_CONDITION) {
            jsonMessage = new JSONMessage(player,message+"&7Condition &6"+errorLine+" &7on Event &6"+eventName+" &7is not valid.");
            hover.add("&eTHIS IS A WARNING!");
            hover.add("&fThe condition defined for this event");
            hover.add("&fis probably not formatted correctly:");
            for(String m : sepText) {
                hover.add("&c"+m);
            }
            hover.add(" ");
            hover.add("&fRemember to use a valid condition from this list:");
            hover.add("&ahttps://ajneb97.gitbook.io/conditionalevents/conditions");
            jsonMessage.hover(hover).send();
        }
    }
}

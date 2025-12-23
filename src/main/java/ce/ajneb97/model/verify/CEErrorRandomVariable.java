package ce.ajneb97.model.verify;

import ce.ajneb97.utils.JSONMessage;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CEErrorRandomVariable extends CEError {

    public CEErrorRandomVariable(String event, String errorText) {
        super(event, errorText);
    }

    @Override
    public void sendMessage(Player player) {
        String message = "&c⚠ ";
        List<String> hover = new ArrayList<>();

        JSONMessage jsonMessage = new JSONMessage(player, message + "&7Event &6" + event + " &7uses the %random_min_max% variable wrongly!");
        hover.add("&eTHIS IS AN ERROR!");
        hover.add("&fIt seems you are not using the random number");
        hover.add("&fvariable correctly:");
        for (String m : getFixedErrorText()) {
            hover.add("&c" + m);
        }
        hover.add(" ");
        hover.add("&fThe format of the random variable changed recently.");
        hover.add("&fUse the new one instead: &e%random_min_max%");
        jsonMessage.hover(hover).send();
    }

}

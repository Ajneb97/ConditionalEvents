package ce.ajneb97.model.verify;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class CEError {

    protected String event;
    protected String errorText;

    public CEError(String event,String errorText){
        this.event = event;
        this.errorText = errorText;
    }

    public List<String> getFixedErrorText(){
        List<String> sepText = new ArrayList<String>();
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
        return sepText;
    }

    public abstract void sendMessage(Player player);
}

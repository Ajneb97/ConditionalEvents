package ce.ajneb97.managers;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.model.internal.DebugSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class DebugManager {

    private ConditionalEvents plugin;
    private ArrayList<DebugSender> debugSenders;
    public DebugManager(ConditionalEvents plugin){
        this.plugin = plugin;
        this.debugSenders = new ArrayList<>();
    }

    public boolean setDebugSender(CommandSender sender,String event){
        DebugSender debugSender = getDebugSender(sender);
        if(debugSender == null){
            this.debugSenders.add(new DebugSender(sender,event));
            return true;
        }
        if(debugSender.getEvent().equals(event)){
            //If the same, then remove it
            removeDebugSender(sender);
            return false;
        }
        //If not the same, update it
        debugSender.setEvent(event);
        return true;
    }

    public DebugSender getDebugSender(CommandSender sender){
        for(DebugSender debugSender : debugSenders){
            if(debugSender.getSender().equals(sender)){
                return debugSender;
            }
        }
        return null;
    }

    public void removeDebugSender(CommandSender sender){
        for(int i=0;i<debugSenders.size();i++){
            if(debugSenders.get(i).getSender().equals(sender)){
                debugSenders.remove(i);
                return;
            }
        }
    }

    public void sendConditionMessage(String event, String condition, boolean approved, Player player, boolean start){
        String result = "&a&lAPPROVED";
        if(!approved){
            result = "&c&lDENIED";
        }
        String playerInfo = "";
        if(player != null){
            playerInfo = ","+player.getName();
        }
        String startText = "";
        if(start){
            startText = ",&estart";
        }

        String debugMessage = MessagesManager.getColoredMessage("&8[&c"+event+playerInfo+startText+"&8] &7Checking for: &f"+condition
                              +" &8| &7Result: "+result);

        for(DebugSender debugSender : debugSenders){
            if(debugSender.getEvent().equals(event)){
                debugSender.getSender().sendMessage(debugMessage);
            }
        }
    }

    public void sendActionsMessage(String event, String actionGroup, Player player){
        String playerInfo = "";
        if(player != null){
            playerInfo = ","+player.getName();
        }

        String debugMessage = MessagesManager.getColoredMessage("&8[&c"+event+playerInfo+
                                     ",&eend&8] &7Executing actions from action group: &f"+actionGroup);

        for(DebugSender debugSender : debugSenders){
            if(debugSender.getEvent().equals(event)){
                debugSender.getSender().sendMessage(debugMessage);
            }
        }
    }

}

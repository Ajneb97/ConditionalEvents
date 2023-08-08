package ce.ajneb97.managers;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.model.actions.ActionTargeter;
import ce.ajneb97.model.actions.ActionTargeterType;
import ce.ajneb97.model.actions.ActionType;
import ce.ajneb97.model.internal.DebugSender;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class DebugManager {

    private ConditionalEvents plugin;
    private ArrayList<DebugSender> debugSenders;
    public DebugManager(ConditionalEvents plugin){
        this.plugin = plugin;
        this.debugSenders = new ArrayList<DebugSender>();
    }

    public boolean setDebugSender(CommandSender sender,String event,String playerName){
        DebugSender debugSender = getDebugSender(sender);
        if(debugSender == null){
            this.debugSenders.add(new DebugSender(sender,event,playerName));
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
        if(debugSenders.isEmpty()){
            return;
        }
        if(condition.endsWith(" or ")){
            condition = condition.substring(0,condition.length()-4);
        }else if(condition.endsWith(" and ")){
            condition = condition.substring(0,condition.length()-5);
        }

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
        String debugMessage = MessagesManager.getColoredMessage("&8[&c"+event+playerInfo+startText+"&8] &7Checking for: &f")
                +condition+MessagesManager.getColoredMessage(" &8| &7Result: "+result);

        for(DebugSender debugSender : debugSenders){
            if(debugSender.getEvent().equals(event)){
                if(player != null && debugSender.getPlayerName() != null){
                    if(!debugSender.getPlayerName().equals(player.getName())){
                        continue;
                    }
                }
                debugSender.getSender().sendMessage(debugMessage);
            }
        }
    }

    public void sendActionsMessage(String event, String actionGroup, Player player){
        if(debugSenders.isEmpty()){
            return;
        }

        String playerInfo = "";
        if(player != null){
            playerInfo = ","+player.getName();
        }

        //Fix actionGroup if parameters are present
        int pos = actionGroup.indexOf("{");
        if(pos != -1){
            actionGroup = actionGroup.substring(0, pos);
        }

        String debugMessage = MessagesManager.getColoredMessage("&8[&c"+event+playerInfo+
                                     "&8] &7Executing actions from action group: &f"+actionGroup);

        for(DebugSender debugSender : debugSenders){
            if(debugSender.getEvent().equals(event)){
                if(player != null && debugSender.getPlayerName() != null){
                    if(!debugSender.getPlayerName().equals(player.getName())){
                        continue;
                    }
                }
                debugSender.getSender().sendMessage(debugMessage);
            }
        }
    }

    public void sendActionMessage(String event, String actionLine, Player player, ActionType actionType, ActionTargeter actionTargeter){
        if(debugSenders.isEmpty()){
            return;
        }

        String playerInfo = "";
        if(player != null){
            playerInfo = ","+player.getName();
        }

        String actionTargeterInfo = "";
        if(!actionTargeter.getType().equals(ActionTargeterType.NORMAL)){
            if(actionTargeter.getParameter() == null){
                actionTargeterInfo = " &6("+actionTargeter.getType()+")";
            }else{
                actionTargeterInfo = " &6("+actionTargeter.getType()+" "+actionTargeter.getParameter()+")";
            }
        }

        String debugMessage = MessagesManager.getColoredMessage("&8[&c"+event+playerInfo+
                ",&eaction&8] &7Executing action:"+actionTargeterInfo+" &6["+actionType+"&6] &f")+actionLine;

        for(DebugSender debugSender : debugSenders){
            if(debugSender.getEvent().equals(event)){
                if(player != null && debugSender.getPlayerName() != null){
                    if(!debugSender.getPlayerName().equals(player.getName())){
                        continue;
                    }
                }
                debugSender.getSender().sendMessage(debugMessage);
            }
        }
    }
}

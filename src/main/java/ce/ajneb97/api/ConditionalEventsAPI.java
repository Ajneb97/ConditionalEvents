package ce.ajneb97.api;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.managers.MessagesManager;
import ce.ajneb97.model.CEEvent;
import ce.ajneb97.utils.TimeUtils;
import org.bukkit.entity.Player;

public class ConditionalEventsAPI {

    private static ConditionalEvents plugin;
    public ConditionalEventsAPI(ConditionalEvents plugin){
        this.plugin = plugin;
    }

    public static String getEventCooldown(Player player, String event){
        CEEvent ceEvent = plugin.getEventsManager().getEvent(event);
        MessagesManager messagesManager = plugin.getMessagesManager();

        if(ceEvent == null){
            return messagesManager.getPlaceholderAPICooldownNameError();
        }

        long eventCooldownMillis = plugin.getPlayerManager().getEventCooldown(event,player)+(ceEvent.getCooldown()*1000);
        long currentTimeMillis = System.currentTimeMillis();


        if(eventCooldownMillis > currentTimeMillis){
            return TimeUtils.getTime((eventCooldownMillis-currentTimeMillis)/1000, messagesManager);
        }else{
            return messagesManager.getPlaceholderAPICooldownReady();
        }
    }

    public static String getOneTimeReady(Player player, String event){
        CEEvent ceEvent = plugin.getEventsManager().getEvent(event);

        if(ceEvent == null){
            return "no";
        }

        boolean oneTime = plugin.getPlayerManager().getEventOneTime(event,player);
        if(oneTime){
            return "yes";
        }else{
            return "no";
        }
    }
}

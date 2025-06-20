package ce.ajneb97.listeners;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.model.EventType;
import ce.ajneb97.model.StoredVariable;
import ce.ajneb97.model.internal.ConditionEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class PlayerEventsListenerNew1_9 implements Listener {

    public ConditionalEvents plugin;
    public PlayerEventsListenerNew1_9(ConditionalEvents plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerSwapHand(PlayerSwapHandItemsEvent event){
        Player player = event.getPlayer();
        ItemStack item = event.getMainHandItem();
        ItemStack itemOffHand = event.getOffHandItem();
        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.PLAYER_SWAP_HAND, null);
        if(!conditionEvent.containsValidEvents()) return;
        conditionEvent.setCommonItemVariables(item,null).setCommonItemVariables(itemOffHand,"offhand")
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTabComplete(TabCompleteEvent event){
        if(!(event.getSender() instanceof Player)){
            return;
        }

        Player player = (Player) event.getSender();
        String command = event.getBuffer();
        String[] args = command.split(" ",-1);
        ArrayList<StoredVariable> eventVariables = new ArrayList<>();
        for(int i=1;i<args.length;i++) {
            eventVariables.add(new StoredVariable("%arg_"+(i)+"%",args[i]));
        }

        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.PLAYER_TAB_COMPLETE, null);
        if(!conditionEvent.containsValidEvents()) return;
        conditionEvent.addVariables(
                        new StoredVariable("%main_command%",args[0]),
                        new StoredVariable("%args_length%",(args.length-1)+"")
                ).addVariables(eventVariables)
                .checkEvent();
    }
}

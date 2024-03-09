package ce.ajneb97.listeners;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.model.EventType;
import ce.ajneb97.model.internal.ConditionEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

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
}

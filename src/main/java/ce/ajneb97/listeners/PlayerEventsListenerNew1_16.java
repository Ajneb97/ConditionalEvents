package ce.ajneb97.listeners;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.model.EventType;
import ce.ajneb97.model.StoredVariable;
import ce.ajneb97.model.internal.ConditionEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class PlayerEventsListenerNew1_16 implements Listener {

    public ConditionalEvents plugin;
    public PlayerEventsListenerNew1_16(ConditionalEvents plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerFoodLevelChange(FoodLevelChangeEvent event){
        Player player = (Player) event.getEntity();

        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.PLAYER_CHANGE_FOOD, null);
        if(!conditionEvent.containsValidEvents()) return;
        conditionEvent.addVariables(
                    new StoredVariable("%old_food_level%",player.getFoodLevel()+""),
                    new StoredVariable("%new_food_level%",event.getFoodLevel()+"")
                ).checkEvent();
    }
}

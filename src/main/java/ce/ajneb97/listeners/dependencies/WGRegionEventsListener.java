package ce.ajneb97.listeners.dependencies;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.model.EventType;
import ce.ajneb97.model.StoredVariable;
import ce.ajneb97.model.internal.ConditionEvent;
import net.raidstone.wgevents.events.RegionEnteredEvent;
import net.raidstone.wgevents.events.RegionLeftEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WGRegionEventsListener implements Listener {

    public ConditionalEvents plugin;
    public WGRegionEventsListener(ConditionalEvents plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRegionEnter(RegionEnteredEvent event){
        Player player = event.getPlayer();

        new ConditionEvent(plugin, player, event, EventType.WGEVENTS_REGION_ENTER, null)
                .addVariables(
                        new StoredVariable("%region%", event.getRegionName())
                )
                .checkEvent();
    }

    @EventHandler
    public void onRegionLeave(RegionLeftEvent event){
        Player player = event.getPlayer();

        new ConditionEvent(plugin, player, event, EventType.WGEVENTS_REGION_LEAVE, null)
                .addVariables(
                        new StoredVariable("%region%", event.getRegionName())
                )
                .checkEvent();
    }
}

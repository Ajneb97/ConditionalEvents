package ce.ajneb97.listeners;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.model.EventType;
import ce.ajneb97.model.StoredVariable;
import ce.ajneb97.model.internal.ConditionEvent;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PaperEventsListener implements Listener {

    public ConditionalEvents plugin;
    public PaperEventsListener(ConditionalEvents plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJump(PlayerJumpEvent event){
        Player player = event.getPlayer();
        Location locationFrom = event.getFrom();
        Location locationTo = event.getTo();

        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.PLAYER_JUMP, null);
        if(!conditionEvent.containsValidEvents()) return;
        conditionEvent.addVariables(
               new StoredVariable("%from_x%",locationFrom.getX()+""),
               new StoredVariable("%from_y%",locationFrom.getY()+""),
               new StoredVariable("%from_z%",locationFrom.getZ()+""),
               new StoredVariable("%from_world%",locationFrom.getWorld().getName()),
               new StoredVariable("%from_yaw%",locationFrom.getYaw()+""),
               new StoredVariable("%from_pitch%",locationFrom.getPitch()+""),
               new StoredVariable("%to_x%",locationTo.getX()+""),
               new StoredVariable("%to_y%",locationTo.getY()+""),
               new StoredVariable("%to_z%",locationTo.getZ()+""),
               new StoredVariable("%to_world%",locationTo.getWorld().getName()),
               new StoredVariable("%to_yaw%",locationTo.getYaw()+""),
               new StoredVariable("%to_pitch%",locationTo.getPitch()+"")
        ).checkEvent();
    }
}

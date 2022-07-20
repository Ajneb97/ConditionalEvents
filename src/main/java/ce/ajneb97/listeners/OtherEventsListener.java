package ce.ajneb97.listeners;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.model.EventType;
import ce.ajneb97.model.StoredVariable;
import ce.ajneb97.model.internal.ConditionEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.ArrayList;

public class OtherEventsListener implements Listener {

    public final ConditionalEvents plugin;
    public OtherEventsListener(ConditionalEvents plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntitySpawn(CreatureSpawnEvent event){
        new ConditionEvent(plugin, null, event, EventType.ENTITY_SPAWN, null)
                .addVariables(
                        new StoredVariable("%entity%",event.getEntityType().name()),
                        new StoredVariable("%entity_world%",event.getLocation().getWorld().getName()),
                        new StoredVariable("%reason%",event.getSpawnReason().name())
                ).checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onConsoleCommand(ServerCommandEvent event) {
        String command = event.getCommand();
        String[] args = command.split(" ");

        ArrayList<StoredVariable> eventVariables = new ArrayList<>();
        for(int i=1;i<args.length;i++) {
            eventVariables.add(new StoredVariable("%arg_"+(i)+"%",args[i]));
        }

        new ConditionEvent(plugin, null, event, EventType.CONSOLE_COMMAND, null)
                .addVariables(
                        new StoredVariable("%command%",command),
                        new StoredVariable("%args_length%",Integer.toString(args.length-1))
                ).addVariables(eventVariables)
                .checkEvent();
    }
}

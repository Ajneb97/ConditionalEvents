package ce.ajneb97.listeners;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.libs.itemselectevent.ItemSelectEvent;
import ce.ajneb97.model.EventType;
import ce.ajneb97.model.StoredVariable;
import ce.ajneb97.model.internal.ConditionEvent;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class OtherEventsListener implements Listener {

    public ConditionalEvents plugin;
    public OtherEventsListener(ConditionalEvents plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntitySpawn(CreatureSpawnEvent event){
        Entity entity = event.getEntity();
        new ConditionEvent(plugin, null, event, EventType.ENTITY_SPAWN, null)
                .addVariables(
                        new StoredVariable("%reason%",event.getSpawnReason().name())
                )
                .setCommonEntityVariables(entity).checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onConsoleCommand(ServerCommandEvent event) {
        String command = event.getCommand();
        String[] args = command.split(" ");

        ArrayList<StoredVariable> eventVariables = new ArrayList<StoredVariable>();
        for(int i=1;i<args.length;i++) {
            eventVariables.add(new StoredVariable("%arg_"+(i)+"%",args[i]));
        }

        new ConditionEvent(plugin, null, event, EventType.CONSOLE_COMMAND, null)
                .addVariables(
                        new StoredVariable("%command%",command),
                        new StoredVariable("%main_command%",args[0]),
                        new StoredVariable("%args_length%",(args.length-1)+"")
                ).addVariables(eventVariables)
                .checkEvent();
    }
}

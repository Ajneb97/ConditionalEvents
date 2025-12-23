package ce.ajneb97.listeners;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.api.ConditionalEventsCallEvent;
import ce.ajneb97.manager.EventsManager;
import ce.ajneb97.model.CEEvent;
import ce.ajneb97.model.EventType;
import ce.ajneb97.model.StoredVariable;
import ce.ajneb97.model.internal.ConditionEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;

@SuppressWarnings("deprecation")
public class OtherEventsListener implements Listener {

    public ConditionalEvents plugin;

    public OtherEventsListener(ConditionalEvents plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntitySpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        new ConditionEvent(plugin, null, event, EventType.ENTITY_SPAWN, null)
                .addVariables(
                        new StoredVariable("%reason%", event.getSpawnReason().name())
                )
                .setCommonEntityVariables(entity).checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onConsoleCommand(ServerCommandEvent event) {
        String command = event.getCommand();
        String[] args = command.split(" ");

        ArrayList<StoredVariable> eventVariables = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            eventVariables.add(new StoredVariable("%arg_" + (i) + "%", args[i]));
        }

        new ConditionEvent(plugin, null, event, EventType.CONSOLE_COMMAND, null)
                .addVariables(
                        new StoredVariable("%command%", command),
                        new StoredVariable("%main_command%", args[0]),
                        new StoredVariable("%args_length%", (args.length - 1) + "")
                ).addVariables(eventVariables)
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onConditionalEventsCallEvent(ConditionalEventsCallEvent event) {
        Player player = event.getPlayer();
        String eventName = event.getEvent();

        EventsManager eventsManager = plugin.getEventsManager();
        CEEvent ceEvent = eventsManager.getEvent(eventName);
        if (!ceEvent.getEventType().equals(EventType.CALL)) {
            return;
        }

        if (!ceEvent.isEnabled()) {
            return;
        }

        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.CALL, null)
                .addVariables(event.getVariables());
        eventsManager.checkSingularEvent(conditionEvent, ceEvent);
    }

    //Not for CE events
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        if (projectile.getShooter() instanceof Player) {
            Player player = (Player) projectile.getShooter();
            ItemStack usedItem = player.getItemInHand();
            projectile.setMetadata("conditionaleventes_projectile_item", new FixedMetadataValue(plugin, usedItem.clone()));
        }
    }

    @EventHandler
    public void fireworkDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (event.getEntity() instanceof Player && damager.getType().name().contains("FIREWORK")) {
            if (damager.hasMetadata("conditionalevents")) {
                event.setCancelled(true);
            }
        }
    }
}

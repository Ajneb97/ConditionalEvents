package ce.ajneb97.managers;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.model.CEEvent;
import ce.ajneb97.model.EventType;
import ce.ajneb97.model.internal.ConditionEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RepetitiveManager {

    private ConditionalEvents plugin;
    private CEEvent ceEvent;
    private long ticks;
    private boolean mustEnd;
    private boolean started;

    public RepetitiveManager(ConditionalEvents plugin,CEEvent ceEvent,long ticks){
        this.plugin = plugin;
        this.ceEvent = ceEvent;
        this.ticks = ticks;
    }

    public boolean isStarted() {
        return started;
    }

    public void end() {
        this.mustEnd = true;
        this.started = false;
    }

    public void start(){
        this.mustEnd = false;
        this.started = true;
        new BukkitRunnable(){
            @Override
            public void run() {
                if(mustEnd || !execute()){
                    this.cancel();
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0L, ticks);
    }

    public boolean execute(){
        if(ceEvent == null){
            return false;
        }

        EventsManager eventsManager = plugin.getEventsManager();
        if(ceEvent.getEventType().equals(EventType.REPETITIVE)){
            for(Player player : Bukkit.getOnlinePlayers()){
                ConditionEvent conditionEvent = new ConditionEvent(plugin, player, null, EventType.REPETITIVE, null);
                conditionEvent.setAsync(true);
                eventsManager.checkSingularEvent(conditionEvent,ceEvent);
            }
        }else{
            //Repetitive server
            ConditionEvent conditionEvent = new ConditionEvent(plugin, null, null, EventType.REPETITIVE_SERVER, null);
            conditionEvent.setAsync(true);
            eventsManager.checkSingularEvent(conditionEvent,ceEvent);
        }
        return true;
    }
}

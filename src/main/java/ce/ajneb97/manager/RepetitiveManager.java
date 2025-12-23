package ce.ajneb97.manager;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.model.CEEvent;
import ce.ajneb97.model.EventType;
import ce.ajneb97.model.internal.ConditionEvent;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.TimeUnit;

public class RepetitiveManager {

    private final ConditionalEvents plugin;
    private final CEEvent ceEvent;
    private final long ticks;
    private boolean started;
    private Runnable cancel;

    public RepetitiveManager(ConditionalEvents plugin, CEEvent ceEvent, long ticks) {
        this.plugin = plugin;
        this.ceEvent = ceEvent;
        this.ticks = ticks;
    }

    public boolean isStarted() {
        return started;
    }

    public void start() {
        this.started = true;
        Runnable runnable = () -> {
            if (!execute()) {
                stop();
            }
        };

        if (plugin.isFolia) {
            long period = ticks * 50L;
            ScheduledTask task = plugin.getServer().getAsyncScheduler().runAtFixedRate(
                    plugin,
                    t -> runnable.run(),
                    period,
                    period,
                    TimeUnit.MILLISECONDS
            );
            cancel = task::cancel;
        } else {
            BukkitTask task = plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                    plugin,
                    runnable,
                    0L,
                    ticks
            );
            cancel = task::cancel;
        }
    }

    public void stop() {
        if (cancel != null) {
            cancel.run();
        }
        this.started = false;
    }

    private boolean execute() {
        if (ceEvent == null) {
            return false;
        }

        EventsManager eventsManager = plugin.getEventsManager();
        if (ceEvent.getEventType().equals(EventType.REPETITIVE)) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                ConditionEvent conditionEvent = new ConditionEvent(plugin, player, null, EventType.REPETITIVE, null);
                conditionEvent.setAsync(true);
                eventsManager.checkSingularEvent(conditionEvent, ceEvent);
            }
        } else {
            //Repetitive server
            ConditionEvent conditionEvent = new ConditionEvent(plugin, null, null, EventType.REPETITIVE_SERVER, null);
            conditionEvent.setAsync(true);
            eventsManager.checkSingularEvent(conditionEvent, ceEvent);
        }
        return true;
    }
}
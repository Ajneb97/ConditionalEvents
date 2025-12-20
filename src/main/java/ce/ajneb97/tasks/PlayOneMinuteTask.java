package ce.ajneb97.tasks;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.model.EventType;
import ce.ajneb97.model.StoredVariable;
import ce.ajneb97.model.internal.ConditionEvent;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.TimeUnit;

public class PlayOneMinuteTask {

    private final ConditionalEvents plugin;
    private final boolean isFolia;
    private Runnable cancel;

    public PlayOneMinuteTask(ConditionalEvents plugin) {
        this.plugin = plugin;
        this.isFolia = plugin.isFolia;
    }

    public void start() {
        Runnable runnable = this::execute;

        if (isFolia) {
            ScheduledTask task = plugin.getServer().getAsyncScheduler().runAtFixedRate(
                    plugin,
                    t -> runnable.run(),
                    1,
                    1,
                    TimeUnit.MINUTES
            );
            cancel = task::cancel;
        } else {
            BukkitTask task = plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                    plugin,
                    runnable,
                    1200L,
                    1200L
            );
            cancel = task::cancel;
        }
    }

    public void stop() {
        if (cancel != null) {
            cancel.run();
        }
    }

    private void execute() {
        Bukkit.getOnlinePlayers().forEach(this::execute);
    }

    private void execute(Player player) {
        ConditionEvent conditionEvent = new ConditionEvent(this.plugin, player, null, EventType.PLAYER_STATISTIC, null);
        if (conditionEvent.containsValidEvents()) {
            this.addVariables(conditionEvent, player);
            conditionEvent.checkEvent();
        }
    }

    private void addVariables(ConditionEvent conditionEvent, Player player) {
        conditionEvent.addVariables(
                new StoredVariable("%statistic_name%", "PLAY_ONE_MINUTE"),
                new StoredVariable("%previous_value%", String.valueOf(player.getStatistic(Statistic.PLAY_ONE_MINUTE) - 1)),
                new StoredVariable("%new_value%", String.valueOf(player.getStatistic(Statistic.PLAY_ONE_MINUTE)))
        );
    }
}
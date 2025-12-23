package ce.ajneb97.tasks;

import ce.ajneb97.ConditionalEvents;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.TimeUnit;

public class PlayerDataSaveTask {

    private final ConditionalEvents plugin;
    private final boolean isFolia;
    private Runnable cancel;

    public PlayerDataSaveTask(ConditionalEvents plugin) {
        this.plugin = plugin;
        this.isFolia = plugin.isFolia;
    }

    public void start(int minutes) {
        long period = minutes * 60L * 20L;

        Runnable runnable = this::execute;

        if (isFolia) {
            ScheduledTask task = plugin.getServer().getAsyncScheduler().runAtFixedRate(
                    plugin,
                    t -> runnable.run(),
                    minutes,
                    minutes,
                    TimeUnit.MINUTES
            );
            cancel = task::cancel;
        } else {
            BukkitTask task = plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                    plugin,
                    runnable,
                    period,
                    period
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
        plugin.getPlayerDataManager().saveAllData();
    }
}

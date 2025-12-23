package ce.ajneb97.model.internal;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.scheduler.BukkitTask;

public class WaitActionTask {

    private final String playerName;
    private String eventName;
    private final Object task;

    public WaitActionTask(String playerName, String eventName, Object task) {
        this.playerName = playerName;
        this.eventName = eventName;
        this.task = task;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void cancel() {
        if (task instanceof BukkitTask) {
            ((BukkitTask) task).cancel();
        } else if (task instanceof ScheduledTask) {
            ((ScheduledTask) task).cancel();
        }
    }

    public int getTaskId() {
        if (task instanceof BukkitTask) {
            return ((BukkitTask) task).getTaskId();
        } else if (task instanceof ScheduledTask) {
            return task.hashCode();
        }
        return -1;
    }
}

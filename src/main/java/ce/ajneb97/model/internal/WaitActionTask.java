package ce.ajneb97.model.internal;

import org.bukkit.scheduler.BukkitTask;

public class WaitActionTask {
    private String playerName;
    private String eventName;
    private BukkitTask task;

    public WaitActionTask(String playerName, String eventName, BukkitTask task) {
        this.playerName = playerName;
        this.eventName = eventName;
        this.task = task;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public BukkitTask getTask() {
        return task;
    }

    public void setTask(BukkitTask task) {
        this.task = task;
    }
}

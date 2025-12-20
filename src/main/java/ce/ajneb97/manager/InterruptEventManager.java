package ce.ajneb97.manager;

import ce.ajneb97.model.internal.WaitActionTask;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class InterruptEventManager {

    private final ArrayList<WaitActionTask> tasks;

    public InterruptEventManager() {
        this.tasks = new ArrayList<>();
    }

    public void addTask(String playerName, String eventName, BukkitTask bukkitTask) {
        tasks.add(new WaitActionTask(playerName, eventName, bukkitTask));
    }

    public void addTask(String playerName, String eventName, ScheduledTask scheduledTask) {
        tasks.add(new WaitActionTask(playerName, eventName, scheduledTask));
    }

    public void removeTaskById(int taskId) {
        tasks.removeIf(task -> task.getTaskId() == taskId);
    }

    // Interrupt actions for a specific event, globally or per player
    public void interruptEvent(String eventName, String playerName) {
        tasks.removeIf(task -> {
            if (playerName == null) {
                if (task.getEventName().equals(eventName)) {
                    task.cancel();
                    return true;
                }
            } else {
                if (task.getPlayerName() != null && task.getPlayerName().equals(playerName) && task.getEventName().equals(eventName)) {
                    task.cancel();
                    return true;
                }
            }
            return false;
        });
    }
}

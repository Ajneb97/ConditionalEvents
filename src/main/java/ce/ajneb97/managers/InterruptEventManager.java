package ce.ajneb97.managers;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.model.internal.WaitActionTask;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class InterruptEventManager {
    private ConditionalEvents plugin;
    private ArrayList<WaitActionTask> tasks;

    public InterruptEventManager(ConditionalEvents plugin){
        this.plugin = plugin;
        this.tasks = new ArrayList<>();
    }

    public void addTask(String playerName, String eventName, BukkitTask bukkitTask){
        tasks.add(new WaitActionTask(playerName,eventName,bukkitTask));
    }

    public void removeTaskById(int taskId){
        tasks.removeIf(task -> task.getTask().getTaskId() == taskId);
    }

    // Interrupt actions for a specific event, globally or per player
    public void interruptEvent(String eventName, String playerName){
        tasks.removeIf(task -> {
            if(playerName == null){
                if(task.getEventName().equals(eventName)){
                    task.getTask().cancel();
                    return true;
                }
            }else{
                if(task.getPlayerName() != null && task.getPlayerName().equals(playerName) && task.getEventName().equals(eventName)){
                    task.getTask().cancel();
                    return true;
                }
            }
            return false;
        });
    }
}

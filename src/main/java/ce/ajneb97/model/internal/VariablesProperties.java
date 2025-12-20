package ce.ajneb97.model.internal;

import ce.ajneb97.model.CEEvent;
import ce.ajneb97.model.StoredVariable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.ArrayList;

public class VariablesProperties {

    private ArrayList<StoredVariable> eventVariables;
    private Player player;
    private Player target;
    private Player toTarget;
    private boolean isPlaceholderAPI;
    private CEEvent event;
    private Event minecraftEvent;

    public VariablesProperties(ArrayList<StoredVariable> eventVariables, Player player, Player target, boolean isPlaceholderAPI, CEEvent event, Event minecraftEvent) {
        this.eventVariables = eventVariables;
        this.player = player;
        this.target = target;
        this.isPlaceholderAPI = isPlaceholderAPI;
        this.event = event;
        this.minecraftEvent = minecraftEvent;
    }

    public ArrayList<StoredVariable> getEventVariables() {
        return eventVariables;
    }

    public void setEventVariables(ArrayList<StoredVariable> eventVariables) {
        this.eventVariables = eventVariables;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getTarget() {
        return target;
    }

    public void setTarget(Player target) {
        this.target = target;
    }

    public boolean isPlaceholderAPI() {
        return isPlaceholderAPI;
    }

    public void setPlaceholderAPI(boolean placeholderAPI) {
        isPlaceholderAPI = placeholderAPI;
    }

    public CEEvent getEvent() {
        return event;
    }

    public void setEvent(CEEvent event) {
        this.event = event;
    }

    public Event getMinecraftEvent() {
        return minecraftEvent;
    }

    public void setMinecraftEvent(Event minecraftEvent) {
        this.minecraftEvent = minecraftEvent;
    }

    public Player getToTarget() {
        return toTarget;
    }

    public void setToTarget(Player toTarget) {
        this.toTarget = toTarget;
    }
}

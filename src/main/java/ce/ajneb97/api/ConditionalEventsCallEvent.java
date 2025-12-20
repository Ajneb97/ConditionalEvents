package ce.ajneb97.api;

import ce.ajneb97.model.StoredVariable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ConditionalEventsCallEvent extends Event {

    private final Player player;
    private final ArrayList<StoredVariable> variables;
    private final String event;
    private static final HandlerList handlers = new HandlerList();

    public ConditionalEventsCallEvent(Player player, ArrayList<StoredVariable> variables, String event) {
        this.player = player;
        this.variables = variables;
        this.event = event;
    }

    public Player getPlayer() {
        return player;
    }

    public String getEvent() {
        return event;
    }

    public ArrayList<StoredVariable> getVariables() {
        return variables;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}

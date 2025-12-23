package ce.ajneb97.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ConditionalEventsEvent extends Event {

    private final Player player;
    private final String event;
    private final String actionGroup;
    private static final HandlerList handlers = new HandlerList();

    //Event called when conditions for an event are achieved
    public ConditionalEventsEvent(Player player, String event, String actionGroup) {
        this.player = player;
        this.event = event;
        this.actionGroup = actionGroup;
    }

    public Player getPlayer() {
        return player;
    }

    public String getEvent() {
        return event;
    }

    public String getActionGroup() {
        return actionGroup;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}

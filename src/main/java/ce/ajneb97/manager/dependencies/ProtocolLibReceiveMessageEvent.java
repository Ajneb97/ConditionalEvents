package ce.ajneb97.manager.dependencies;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ProtocolLibReceiveMessageEvent extends Event implements Cancellable {

    private final Player player;
    private final String jsonMessage;
    private final String normalMessage;
    private boolean cancelled;
    private static final HandlerList handlers = new HandlerList();

    public ProtocolLibReceiveMessageEvent(Player player, String jsonMessage, String normalMessage) {
        this.player = player;
        this.jsonMessage = jsonMessage;
        this.normalMessage = normalMessage;
        this.cancelled = false;
    }

    public Player getPlayer() {
        return player;
    }

    public String getJsonMessage() {
        return jsonMessage;
    }

    public String getNormalMessage() {
        return normalMessage;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}

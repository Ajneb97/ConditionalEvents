package ce.ajneb97.model.internal;

import org.bukkit.command.CommandSender;

public class DebugSender {
    private CommandSender sender;
    private String event;

    public DebugSender(CommandSender sender, String event) {
        this.sender = sender;
        this.event = event;
    }

    public CommandSender getSender() {
        return sender;
    }

    public void setSender(CommandSender sender) {
        this.sender = sender;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }
}

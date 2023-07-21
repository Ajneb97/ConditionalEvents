package ce.ajneb97.model.internal;

import org.bukkit.command.CommandSender;

public class DebugSender {
    private CommandSender sender;
    private String event;
    private String playerName;

    public DebugSender(CommandSender sender, String event, String playerName) {
        this.sender = sender;
        this.event = event;
        this.playerName = playerName;
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

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}

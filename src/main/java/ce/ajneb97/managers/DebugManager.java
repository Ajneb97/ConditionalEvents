package ce.ajneb97.managers;

import ce.ajneb97.model.actions.ActionTargeter;
import ce.ajneb97.model.actions.ActionTargeterType;
import ce.ajneb97.model.actions.ActionType;
import ce.ajneb97.model.internal.Debugger;
import lombok.val;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DebugManager {

    private static final Map<String, Debugger> debuggers = new HashMap<>();

    public boolean addDebugger(@Nonnull final CommandSender sender,
                               @Nonnull final String event,
                               @Nullable final String playerName) {

        val debugger = debuggers.get(sender.getName());

        if (debugger != null) {
            if (Objects.equals(debugger.getEvent(), event)) {
                removeDebugger(sender);
                return false;
            } else {
                debugger.setEvent(event);
            }

            if (!Objects.equals(debugger.getPlayerName(), playerName)) {
                debugger.setPlayerName(playerName);
            }
            return true;
        }

        debuggers.put(sender.getName(), new Debugger(sender, event, playerName));
        return true;
    }

    public void removeDebugger(@Nonnull final CommandSender sender) {
        debuggers.remove(sender.getName());
    }

    public boolean hasDebuggers() {
        return !debuggers.isEmpty();
    }

    public void sendConditionMessage(@Nonnull final String event,
                                     @Nonnull String condition,
                                     final boolean approved,
                                     @Nullable final Player player,
                                     final boolean start) {
        if (debuggers.isEmpty()) {
            return;
        }
        if (condition.endsWith(" or ")) {
            condition = condition.substring(0, condition.length() - 4);
        } else if (condition.endsWith(" and ")) {
            condition = condition.substring(0, condition.length() - 5);
        }

        val result = approved ? "&a&lAPPROVED" : "&c&lDENIED";
        val playerInfo = player != null ? "," + player.getName() : "";
        val startText = start ? ",&estart" : "";

        val message = MessagesManager.getColoredMessage("&8[&c" + event + playerInfo + startText + "&8] &7Checking for: &f")
                      + condition + MessagesManager.getColoredMessage(" &8| &7Result: " + result);

        broadcast(event, player, message);
    }

    public void broadcast(@Nonnull final String event,
                          @Nullable final Player player,
                          @Nonnull final String message) {
        for (val debugger : debuggers.values()) {
            if (!Objects.equals(debugger.getEvent(), event)) continue;
            if (player != null && debugger.getPlayerName() != null
                && !Objects.equals(player.getName(), debugger.getPlayerName()))
                continue;
            debugger.getSender().sendMessage(message);
        }
    }

    public void sendActionsMessage(@Nonnull final String event,
                                   @Nonnull String actionGroup,
                                   @Nullable Player player) {
        if (debuggers.isEmpty()) {
            return;
        }

        String playerInfo = player != null ? "," + player.getName() : "";

        //Fix actionGroup if parameters are present
        int pos = actionGroup.indexOf("{");
        if (pos != -1) {
            actionGroup = actionGroup.substring(0, pos);
        }

        val message = MessagesManager.getColoredMessage("&8[&c" + event + playerInfo +
                                                        "&8] &7Executing actions from action group: &f" + actionGroup);

        broadcast(event, player, message);
    }

    public void sendActionMessage(@Nonnull final String event,
                                  final String actionLine,
                                  @Nullable final Player player,
                                  final ActionType actionType,
                                  final ActionTargeter actionTargeter) {
        if (debuggers.isEmpty()) {
            return;
        }

        val playerInfo = player != null ? "," + player.getName() : "";

        String actionTargeterInfo = "";
        if (!actionTargeter.getType().equals(ActionTargeterType.NORMAL)) {
            if (actionTargeter.getParameter() == null) {
                actionTargeterInfo = " &6(" + actionTargeter.getType() + ")";
            } else {
                actionTargeterInfo = " &6(" + actionTargeter.getType() + " " + actionTargeter.getParameter() + ")";
            }
        }

        val message = MessagesManager.getColoredMessage("&8[&c" + event + playerInfo +
                                                        ",&eaction&8] &7Executing action:"
                                                        + actionTargeterInfo
                                                        + " &6[" + actionType + "&6] &f") + actionLine;

        broadcast(event, player, message);
    }
}

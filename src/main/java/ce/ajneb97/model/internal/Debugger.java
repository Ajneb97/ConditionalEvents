package ce.ajneb97.model.internal;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.command.CommandSender;

@Data
@AllArgsConstructor
public class Debugger {

    private final CommandSender sender;

    private String event;
    private String playerName;

}

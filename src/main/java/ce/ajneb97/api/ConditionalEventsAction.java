package ce.ajneb97.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class ConditionalEventsAction {

    protected String name;
    protected JavaPlugin plugin;

    public ConditionalEventsAction(String name){
        this.name = name;
    }
    public abstract void execute(Player player, String actionLine, Event minecraftEvent);

    public String getName() {
        return name;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public void setPlugin(JavaPlugin plugin) {
        this.plugin = plugin;
    }
}

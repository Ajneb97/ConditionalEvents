package ce.ajneb97.managers;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.api.ConditionalEventsAction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class APIManager {

    private ConditionalEvents plugin;
    private ArrayList<ConditionalEventsAction> apiActions;
    public APIManager(ConditionalEvents plugin){
        this.plugin = plugin;
        apiActions = new ArrayList<>();
    }

    public void registerApiActions(JavaPlugin plugin, ConditionalEventsAction... actions){
        for(ConditionalEventsAction a : actions){
            a.setPlugin(plugin);
            Bukkit.getConsoleSender().sendMessage(ConditionalEvents.prefix+
                    MessagesManager.getColoredMessage(" &7Custom API Action &a"+a.getName()+" &7registered from plugin &e"+a.getPlugin().getName()));
            apiActions.add(a);
        }
        this.plugin.getConfigsManager().endRepetitiveEvents();
        this.plugin.getConfigsManager().configureEvents();
        this.plugin.getVerifyManager().verifyEvents();
    }

    public void unregisterApiActions(JavaPlugin plugin){
        apiActions.removeIf(a -> a.getPlugin() == plugin);
        this.plugin.getConfigsManager().endRepetitiveEvents();
        this.plugin.getConfigsManager().configureEvents();
        this.plugin.getVerifyManager().verifyEvents();
    }

    public ConditionalEventsAction getApiAction(String actionName){
        for(ConditionalEventsAction action : apiActions){
            if(action.getName().equals(actionName)){
                return action;
            }
        }
        return null;
    }

    public void executeAction(String actionName, Player player, String actionLine){
        ConditionalEventsAction action = getApiAction(actionName);
        if(action != null){
            action.execute(player,actionLine);
        }
    }
}

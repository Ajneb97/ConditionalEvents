package ce.ajneb97.configs;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.model.CEEvent;

public class ConfigsManager {

    private MainConfigManager mainConfigManager;
    private PlayerConfigsManager playerConfigsManager;
    private ConditionalEvents plugin;
    public ConfigsManager(ConditionalEvents plugin){
        mainConfigManager = new MainConfigManager(plugin);
        playerConfigsManager = new PlayerConfigsManager(plugin);
        this.plugin = plugin;
    }

    public void configure(){
        mainConfigManager.configure();
        playerConfigsManager.configure();
    }

    public MainConfigManager getMainConfigManager() {
        return mainConfigManager;
    }

    public PlayerConfigsManager getPlayerConfigsManager() {
        return playerConfigsManager;
    }

    public boolean reload(){
        for(CEEvent event : plugin.getEventsManager().getEvents()){
            if(event.getRepetitiveManager() != null){
                event.getRepetitiveManager().end();
            }
        }

        playerConfigsManager.savePlayerData();
        if(!mainConfigManager.reloadConfig()){
            return false;
        }
        plugin.reloadEvents();
        plugin.getVerifyManager().verifyEvents();
        plugin.reloadPlayerDataSaveTask();
        return true;
    }
}

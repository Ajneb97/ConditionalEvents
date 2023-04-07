package ce.ajneb97.managers;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.managers.dependencies.ProtocolLibManager;
import org.bukkit.Bukkit;

public class DependencyManager {

    private boolean placeholderAPI;
    private boolean citizens;
    private boolean worldGuardEvents;
    private ProtocolLibManager protocolLibManager;

    public DependencyManager(ConditionalEvents plugin){
        placeholderAPI = false;
        citizens = false;
        worldGuardEvents = false;
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null
            && Bukkit.getPluginManager().getPlugin("PlaceholderAPI").isEnabled()){
            placeholderAPI = true;
        }
        if(Bukkit.getPluginManager().getPlugin("Citizens") != null
            && Bukkit.getPluginManager().getPlugin("Citizens").isEnabled()){
            citizens = true;
        }
        if(Bukkit.getPluginManager().getPlugin("WorldGuardEvents") != null
            && Bukkit.getPluginManager().getPlugin("WorldGuardEvents").isEnabled()){
            worldGuardEvents = true;
        }
        if(Bukkit.getPluginManager().getPlugin("ProtocolLib") != null
                && Bukkit.getPluginManager().getPlugin("ProtocolLib").isEnabled()){
            protocolLibManager = new ProtocolLibManager(plugin);
        }
    }

    public boolean isPlaceholderAPI() {
        return placeholderAPI;
    }

    public boolean isCitizens() {
        return citizens;
    }

    public boolean isWorldGuardEvents() {
        return worldGuardEvents;
    }

    public ProtocolLibManager getProtocolLibManager() {
        return protocolLibManager;
    }
}

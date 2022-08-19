package ce.ajneb97.managers;

import org.bukkit.Bukkit;

public class DependencyManager {

    private boolean placeholderAPI;
    private boolean citizens;
    private boolean worldGuardEvents;

    public DependencyManager(){
        placeholderAPI = false;
        citizens = false;
        worldGuardEvents = false;
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
            placeholderAPI = true;
        }
        if(Bukkit.getPluginManager().getPlugin("Citizens") != null){
            citizens = true;
        }
        if(Bukkit.getPluginManager().getPlugin("WorldGuardEvents") != null){
            worldGuardEvents = true;
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
}

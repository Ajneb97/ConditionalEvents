package ce.ajneb97.managers;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.managers.dependencies.DiscordSRVManager;
import ce.ajneb97.managers.dependencies.ProtocolLibManager;
import org.bukkit.Bukkit;

public class DependencyManager {

    private boolean placeholderAPI;
    private boolean citizens;
    private boolean worldGuardEvents;
    private boolean paper;
    private ProtocolLibManager protocolLibManager;
    private DiscordSRVManager discordSRVManager;

    public DependencyManager(ConditionalEvents plugin){
        placeholderAPI = false;
        citizens = false;
        worldGuardEvents = false;
        paper = false;
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
        if(Bukkit.getPluginManager().getPlugin("DiscordSRV") != null
                && Bukkit.getPluginManager().getPlugin("DiscordSRV").isEnabled()){
            discordSRVManager = new DiscordSRVManager(plugin);
        }

        try{
            Class.forName("com.destroystokyo.paper.ParticleBuilder");
            paper = true;
        }catch(Exception e){

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

    public DiscordSRVManager getDiscordSRVManager() {
        return discordSRVManager;
    }

    public boolean isPaper() {
        return paper;
    }
}

package ce.ajneb97.managers;

import org.bukkit.Bukkit;

public class DependencyManager {

    private boolean placeholderAPI;

    public DependencyManager(){
        placeholderAPI = false;
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
            placeholderAPI = true;
        }
    }

    public boolean isPlaceholderAPI() {
        return placeholderAPI;
    }
}

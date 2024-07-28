package ce.ajneb97.configs;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.managers.MessagesManager;
import ce.ajneb97.model.ToConditionGroup;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SavedItemsConfigManager {

    private CEConfig configFile;
    private ConditionalEvents plugin;

    public SavedItemsConfigManager(ConditionalEvents plugin){
        this.plugin = plugin;
        this.configFile = new CEConfig("saved_items.yml",plugin,null);
        configFile.registerConfig();
    }

    public void configure(){
        Map<String, ItemStack> savedItems = new HashMap<>();

        FileConfiguration config = configFile.getConfig();
        if(config.contains("items")){
            for(String key : config.getConfigurationSection("items").getKeys(false)){
                ItemStack item = config.getItemStack("items."+key);
                savedItems.put(key,item);
            }
        }

        plugin.getSavedItemsManager().setSavedItems(savedItems);
    }

    public void saveItem(String name,ItemStack item){
        FileConfiguration config = configFile.getConfig();
        config.set("items."+name,item);
        saveConfig();
    }

    public void removeItem(String name){
        FileConfiguration config = configFile.getConfig();
        config.set("items."+name,null);
        saveConfig();
    }

    public boolean reloadConfig(){
        if(!configFile.reloadConfig()){
            return false;
        }
        configure();
        return true;
    }

    public FileConfiguration getConfig(){
        return configFile.getConfig();
    }

    public CEConfig getConfigFile(){
        return this.configFile;
    }

    public void saveConfig(){
        configFile.saveConfig();
    }
}

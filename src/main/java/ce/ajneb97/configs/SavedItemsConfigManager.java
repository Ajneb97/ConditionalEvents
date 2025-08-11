package ce.ajneb97.configs;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.configs.model.CommonConfig;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;

public class SavedItemsConfigManager {

    private CommonConfig configFile;
    private ConditionalEvents plugin;

    public SavedItemsConfigManager(ConditionalEvents plugin){
        this.plugin = plugin;
        this.configFile = new CommonConfig("saved_items.yml",plugin,null,false);
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

    public CommonConfig getConfigFile(){
        return this.configFile;
    }

    public void saveConfig(){
        configFile.saveConfig();
    }
}

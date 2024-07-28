package ce.ajneb97.managers;

import ce.ajneb97.ConditionalEvents;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class SavedItemsManager {
    private ConditionalEvents plugin;
    private Map<String, ItemStack> savedItems;
    public SavedItemsManager(ConditionalEvents plugin){
        this.plugin = plugin;
        this.savedItems = new HashMap<>();
    }

    public Map<String, ItemStack> getSavedItems() {
        return savedItems;
    }

    public void setSavedItems(Map<String, ItemStack> savedItems) {
        this.savedItems = savedItems;
    }

    public ItemStack getItem(String name){
        if(!savedItems.containsKey(name)){
            return null;
        }
        return savedItems.get(name).clone();
    }

    public void addItem(String name,ItemStack item){
        savedItems.put(name,item);
        plugin.getConfigsManager().getSavedItemsConfigManager().saveItem(name,item);
    }

    public void removeItem(String name){
        savedItems.remove(name);
        plugin.getConfigsManager().getSavedItemsConfigManager().removeItem(name);
    }
}

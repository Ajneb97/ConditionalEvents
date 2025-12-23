package ce.ajneb97.manager;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.utils.OtherUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
public class SavedItemsManager {

    private final ConditionalEvents plugin;
    private Map<String, ItemStack> savedItems;

    public SavedItemsManager(ConditionalEvents plugin) {
        this.plugin = plugin;
        this.savedItems = new HashMap<>();
    }

    public Map<String, ItemStack> getSavedItems() {
        return savedItems;
    }

    public void setSavedItems(Map<String, ItemStack> savedItems) {
        this.savedItems = savedItems;
    }

    public ItemStack getItem(String name, Player player) {
        if (!savedItems.containsKey(name)) {
            return null;
        }

        ItemStack item = savedItems.get(name).clone();
        if (player == null) {
            return item;
        }

        // Placeholders
        ItemMeta meta = item.getItemMeta();
        if (meta.hasDisplayName()) {
            String displayName = OtherUtils.replaceGlobalVariables(meta.getDisplayName(), player, plugin);
            meta.setDisplayName(displayName);
        }
        if (meta.hasLore()) {
            List<String> lore = meta.getLore();

            if (lore != null) {
                lore.replaceAll(text -> OtherUtils.replaceGlobalVariables(text, player, plugin));
            }

            meta.setLore(lore);
        }
        item.setItemMeta(meta);

        return item;
    }

    public void addItem(String name, ItemStack item) {
        savedItems.put(name, item);
        plugin.getConfigsManager().getSavedItemsConfigManager().saveItem(name, item);
    }

    public void removeItem(String name) {
        savedItems.remove(name);
        plugin.getConfigsManager().getSavedItemsConfigManager().removeItem(name);
    }
}

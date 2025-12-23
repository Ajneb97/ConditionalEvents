package ce.ajneb97.libs.itemselectevent;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

public class ItemSelectListenerNew implements Listener {

    @EventHandler
    public void onChangeHand(PlayerSwapHandItemsEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack itemMain = event.getMainHandItem();
        ItemStack itemOff = event.getOffHandItem();
        ArrayList<ItemStack> items = new ArrayList<>();
        items.add(itemMain);
        items.add(itemOff);
        for (int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i);
            if (item != null && !item.getType().equals(Material.AIR)) {

                SelectType action;
                if (i == 0) {
                    action = SelectType.SELECT;
                } else {
                    action = SelectType.DESELECT;
                }

                ItemSelectEvent selectEvent = new ItemSelectEvent(player, item, action);
                Bukkit.getServer().getPluginManager().callEvent(selectEvent);
            }
        }
    }
}

package ce.ajneb97.libs.offhandevent;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.utils.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

public class OffHandListener implements Listener {

    private ConditionalEvents plugin;
    public OffHandListener(ConditionalEvents plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerSwapHand(PlayerSwapHandItemsEvent event){
        if(event.isCancelled()){
            return;
        }
        Player player = event.getPlayer();
        ItemStack item = event.getMainHandItem();
        ItemStack itemOffHand = event.getOffHandItem();

        OffHandEvent swapEvent = new OffHandEvent(player,itemOffHand,item);
        Bukkit.getServer().getPluginManager().callEvent(swapEvent);
        if(swapEvent.isCancelled()){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMoveOffHand(InventoryClickEvent event) {
        if(event.isCancelled()){
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();
        ItemStack item2 = null;
        if (event.getClick().equals(ClickType.NUMBER_KEY)) {
            int slotHotbar = event.getHotbarButton();
            //Two items swap places
            item2 = player.getInventory().getItem(slotHotbar);
        }

        InventoryType.SlotType slotType = event.getSlotType();
        ClickType click = event.getClick();
        ItemStack itemInOffhand = player.getEquipment().getItemInOffHand();
        int slot = event.getSlot();

        OffHandEvent swapEvent = null;
        ServerVersion serverVersion = ConditionalEvents.serverVersion;
        if(slotType.equals(InventoryType.SlotType.QUICKBAR) && slot == 40){
            if(click.equals(ClickType.NUMBER_KEY)){
                swapEvent = new OffHandEvent(player,item2,itemInOffhand);
            }else{
                swapEvent = new OffHandEvent(player,cursorItem,itemInOffhand);
            }
        }else if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_16_R3)){
            if(click.equals(ClickType.SWAP_OFFHAND)){
                swapEvent = new OffHandEvent(player,clickedItem,itemInOffhand);
            }
        }

        if(swapEvent != null){
            Bukkit.getServer().getPluginManager().callEvent(swapEvent);
            if(swapEvent.isCancelled()){
                event.setCancelled(true);
            }
        }
    }
}

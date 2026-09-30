package ce.ajneb97.libs.repairevent;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.utils.InventoryUtils;
import ce.ajneb97.utils.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.view.AnvilView;

// 1.16.5+
public class RepairListener implements Listener {

    private ConditionalEvents plugin;
    public RepairListener(ConditionalEvents plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if(!event.getInventory().equals(InventoryUtils.getTopInventory(player))){
            return;
        }

        if(event.getRawSlot() != 2){
            return;
        }

        String renameText = "";
        RepairEvent.RepairType repairType;
        ItemStack item;
        InventoryType inventoryType = event.getInventory().getType();
        if(inventoryType == InventoryType.ANVIL){
            if(!(event.getInventory() instanceof AnvilInventory)){
                return;
            }
            AnvilInventory inv = (AnvilInventory) event.getInventory();

            ServerVersion serverVersion = ConditionalEvents.serverVersion;
            if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_21_R3)){
                AnvilView view = (AnvilView) event.getView();
                if(player.getLevel() < view.getRepairCost()){
                    return;
                }
                renameText = view.getRenameText();
            }else{
                if(player.getLevel() < inv.getRepairCost()){
                    return;
                }
                renameText = inv.getRenameText();
            }
            ItemStack resultItem = inv.getItem(2);
            if(resultItem == null || resultItem.getType().equals(Material.AIR)){
                return;
            }

            item = inv.getItem(0);
            repairType = RepairEvent.RepairType.ANVIL;
        }else if(inventoryType == InventoryType.GRINDSTONE){
            if(!(event.getInventory() instanceof GrindstoneInventory)){
                return;
            }
            GrindstoneInventory inv = (GrindstoneInventory) event.getInventory();
            ItemStack resultItem = inv.getItem(2);
            if(resultItem == null || resultItem.getType().equals(Material.AIR)){
                return;
            }
            item = inv.getItem(0);
            repairType = RepairEvent.RepairType.GRINDSTONE;
        }else{
            return;
        }

        RepairEvent repairEvent = new RepairEvent(player,item,repairType,renameText);
        Bukkit.getServer().getPluginManager().callEvent(repairEvent);
        if(repairEvent.isCancelled()){
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMend(PlayerItemMendEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        RepairEvent repairEvent = new RepairEvent(player,item, RepairEvent.RepairType.MENDING,"");
        Bukkit.getServer().getPluginManager().callEvent(repairEvent);
        if(repairEvent.isCancelled()){
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCraft(CraftItemEvent event) {
        CraftingInventory inv = event.getInventory();
        ItemStack[] matrix = inv.getMatrix();

        ItemStack first = null;
        ItemStack second = null;
        for (ItemStack item : matrix) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            if (first == null) {
                first = item;
            } else if (second == null) {
                second = item;
            } else {
                // More than two
                return;
            }
        }

        if (first == null || second == null) {
            return;
        }

        // Same type
        if (first.getType() != second.getType()) {
            return;
        }

        // Must be repairable
        if (first.getType().getMaxDurability() <= 0) {
            return;
        }

        ItemStack result = event.getCurrentItem();
        if (result == null || result.getType() != first.getType()) {
            // Must return the same item
            return;
        }

        RepairEvent repairEvent = new RepairEvent((Player)event.getWhoClicked(),result, RepairEvent.RepairType.INVENTORY,"");
        Bukkit.getServer().getPluginManager().callEvent(repairEvent);
        if(repairEvent.isCancelled()){
            event.setCancelled(true);
        }
    }
}

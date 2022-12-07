package ce.ajneb97.listeners;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.libs.itemselectevent.ItemSelectEvent;
import ce.ajneb97.model.CEEvent;
import ce.ajneb97.model.EventType;
import ce.ajneb97.model.StoredVariable;
import ce.ajneb97.model.internal.ConditionEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class ItemEventsListener implements Listener {

    public ConditionalEvents plugin;
    public ItemEventsListener(ConditionalEvents plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if(!Bukkit.getVersion().contains("1.8") && !Bukkit.getVersion().contains("1.9")) {
            if(!event.getAction().equals(Action.PHYSICAL) && (event.getHand() == null || (!event.getHand().equals(EquipmentSlot.HAND)
                    && !event.getHand().equals(EquipmentSlot.OFF_HAND)))) {
                return;
            }
        }

        if(item == null){
            return;
        }

        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.ITEM_INTERACT, null);
        if(!conditionEvent.containsValidEvents()) return;
        conditionEvent.setCommonActionVariables(event.getAction(),player)
                .setCommonItemVariables(item)
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.ITEM_CONSUME, null);
        if(!conditionEvent.containsValidEvents()) return;
        conditionEvent.setCommonItemVariables(item)
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemPickUp(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem().getItemStack();

        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.ITEM_PICKUP, null);
        if(!conditionEvent.containsValidEvents()) return;
        conditionEvent.setCommonItemVariables(item)
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemMove(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if(event.getClick().equals(ClickType.NUMBER_KEY)) {
            int slotHotbar = event.getHotbarButton();
            ItemStack item2 = player.getInventory().getItem(slotHotbar);
            if(item2 == null || item2.getType().equals(Material.AIR)) {

            }else if(item == null || item.getType().equals(Material.AIR)){
                item = item2;
            }
        }
        String inventoryType = "";
        InventoryView view = player.getOpenInventory();
        if(view != null) {
            inventoryType = view.getType().name();
        }
        int slot = event.getSlot();

        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.ITEM_MOVE, null);
        if(!conditionEvent.containsValidEvents()) return;
        conditionEvent.addVariables(
                new StoredVariable("%inventory_type%",inventoryType),
                new StoredVariable("%slot%",slot+"")
        ).setCommonItemVariables(item)
                .checkEvent();
    }

    @EventHandler
    public void onItemCraft(CraftItemEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getRecipe().getResult();

        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.ITEM_CRAFT, null);
        if(!conditionEvent.containsValidEvents()) return;
        conditionEvent.setCommonItemVariables(item)
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();

        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.ITEM_DROP, null);
        if(!conditionEvent.containsValidEvents()) return;
        conditionEvent.setCommonItemVariables(item)
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemSelect(ItemSelectEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.ITEM_SELECT, null);
        if(!conditionEvent.containsValidEvents()) return;
        conditionEvent.addVariables(
                new StoredVariable("%select_type%",event.getSelectType().name())
        ).setCommonItemVariables(item)
                .checkEvent();
    }
}

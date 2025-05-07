package ce.ajneb97.listeners;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.libs.itemselectevent.ItemSelectEvent;
import ce.ajneb97.model.CEEvent;
import ce.ajneb97.model.EventType;
import ce.ajneb97.model.StoredVariable;
import ce.ajneb97.model.internal.ConditionEvent;
import ce.ajneb97.utils.InventoryUtils;
import ce.ajneb97.utils.OtherUtils;
import ce.ajneb97.utils.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.AnvilView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
                .setCommonItemVariables(item,null)
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.ITEM_CONSUME, null);
        if(!conditionEvent.containsValidEvents()) return;
        conditionEvent.setCommonItemVariables(item,null)
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemPickUp(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem().getItemStack();

        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.ITEM_PICKUP, null);
        if(!conditionEvent.containsValidEvents()) return;
        conditionEvent.setCommonItemVariables(item,null)
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemRepair(InventoryClickEvent event) {
        if(!event.getInventory().getType().equals(InventoryType.ANVIL)){
            return;
        }
        if(OtherUtils.isLegacy()){
            return;
        }
        if(!(event.getInventory() instanceof AnvilInventory)){
            return;
        }
        AnvilInventory inv = (AnvilInventory) event.getInventory();

        Player player = (Player) event.getWhoClicked();

        if(!inv.equals(InventoryUtils.getTopInventory(player))){
            return;
        }
        if(event.getRawSlot() != 2){
            return;
        }

        String renameText = "";
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

        ItemStack item = inv.getItem(0);
        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.ITEM_REPAIR, null);
        if(!conditionEvent.containsValidEvents()) return;
        conditionEvent.addVariables(
                        new StoredVariable("%rename_text%",renameText)
                ).setCommonItemVariables(item,null)
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemMove(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();

        ArrayList<ItemStack> items = new ArrayList<>();
        items.add(item);

        if(event.getClick().equals(ClickType.NUMBER_KEY)) {
            int slotHotbar = event.getHotbarButton();
            ItemStack item2 = player.getInventory().getItem(slotHotbar);

            //Two items swap places
            items.add(item2);
        }
        String inventoryType = "";
        String inventoryTitle = "";
        InventoryView view = player.getOpenInventory();
        if(view != null) {
            inventoryType = InventoryUtils.getOpenInventoryViewType(player).name();
            inventoryTitle = ChatColor.stripColor(InventoryUtils.getOpenInventoryViewTitle(player));
        }
        int slot = event.getSlot();

        for(ItemStack i : items){
            ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.ITEM_MOVE, null);
            if(!conditionEvent.containsValidEvents()) return;
            conditionEvent.addVariables(
                            new StoredVariable("%inventory_type%",inventoryType),
                            new StoredVariable("%slot%",slot+""),
                            new StoredVariable("%inventory_title%", inventoryTitle)
                    ).setCommonItemVariables(i,null)
                    .checkEvent();
        }
    }

    @EventHandler
    public void onItemCraft(CraftItemEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getRecipe().getResult();

        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.ITEM_CRAFT, null);
        if(!conditionEvent.containsValidEvents()) return;
        conditionEvent.setCommonItemVariables(item,null)
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();

        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.ITEM_DROP, null);
        if(!conditionEvent.containsValidEvents()) return;
        conditionEvent.setCommonItemVariables(item,null)
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
        ).setCommonItemVariables(item,null)
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemEnchant(EnchantItemEvent event) {
        Player player = event.getEnchanter();

        ItemStack item = event.getItem();

        String enchantmentStringList = "";
        List<Map.Entry<Enchantment,Integer>> enchantmentList = new ArrayList<>(event.getEnchantsToAdd().entrySet());
        for(int i=0;i<enchantmentList.size();i++){
            String enchant = enchantmentList.get(i).getKey().getName()+":"+enchantmentList.get(i).getValue();
            enchantmentStringList += enchant;
            if(i < enchantmentList.size()-1){
                enchantmentStringList += ";";
            }
        }

        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.ITEM_ENCHANT, null);
        if(!conditionEvent.containsValidEvents()) return;
        conditionEvent.addVariables(
                new StoredVariable("%enchantment_list%",enchantmentStringList)
        ).setCommonItemVariables(item,null)
                .checkEvent();
    }
}

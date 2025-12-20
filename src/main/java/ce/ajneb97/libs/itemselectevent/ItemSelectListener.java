package ce.ajneb97.libs.itemselectevent;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import ce.ajneb97.ConditionalEvents;

public class ItemSelectListener implements Listener {

    private final ArrayList<Player> players = new ArrayList<>();
    private final ConditionalEvents plugin;

    public ItemSelectListener(ConditionalEvents plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemChanged(PlayerItemHeldEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();

        int previousSlot = event.getPreviousSlot();
        int newSlot = event.getNewSlot();
        ItemStack newItem = player.getInventory().getItem(newSlot);
        ItemStack previousItem = player.getInventory().getItem(previousSlot);
        ArrayList<ItemStack> items = new ArrayList<>();
        items.add(newItem);
        items.add(previousItem);
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

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();
        if (!players.contains(player)) {
            players.add(player);
            PlayerCustomDropEvent dropEvent = new PlayerCustomDropEvent(player, item, DropType.PLAYER, player.getInventory().getHeldItemSlot());
            Bukkit.getServer().getPluginManager().callEvent(dropEvent);
        }
    }

    @EventHandler
    public void onCustomItemDrop(PlayerCustomDropEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        int slot = event.getSlot();
        int selectedSlot = player.getInventory().getHeldItemSlot();

        if (slot == selectedSlot) {
            SelectType action = SelectType.DESELECT;
            ItemSelectEvent selectEvent = new ItemSelectEvent(player, item, action);
            Bukkit.getServer().getPluginManager().callEvent(selectEvent);
        }

        if (plugin.isFolia) {
            player.getScheduler().runDelayed(plugin, (task) -> players.remove(player), null, 3L);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, () -> players.remove(player), 3L);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem().getItemStack();
        int selectedSlot = player.getInventory().getHeldItemSlot();
        int newSlot = player.getInventory().firstEmpty();
        if (newSlot == selectedSlot) {
            SelectType action = SelectType.SELECT;
            ItemSelectEvent selectEvent = new ItemSelectEvent(player, item, action);
            Bukkit.getServer().getPluginManager().callEvent(selectEvent);
        }
    }

    @EventHandler
    public void onItemInventoryClick(InventoryClickEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        InventoryAction action = event.getAction();
        int slot = event.getSlot();
        int selectedSlot = player.getInventory().getHeldItemSlot();
        if (action.name().contains("DROP")) {
            players.add(player);
            PlayerCustomDropEvent dropEvent = new PlayerCustomDropEvent(player, event.getCurrentItem(), DropType.INVENTORY, slot);
            Bukkit.getServer().getPluginManager().callEvent(dropEvent);
            return;
        }

        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        int slotHotbar = event.getHotbarButton();
        if (event.getClick().equals(ClickType.NUMBER_KEY)) {
            ItemStack item2 = player.getInventory().getItem(slotHotbar);
            if (item2 != null && !item2.getType().equals(Material.AIR)) {
                cursor = item2;
            } else if (current == null || current.getType().equals(Material.AIR)) {
                current = item2;
            }
        }

        ArrayList<ItemStack> items = new ArrayList<>();
        if (selectedSlot == slot) {
            items.add(current);
            items.add(cursor);
        } else if (selectedSlot == slotHotbar) {
            items.add(cursor);
            items.add(current);
        }
        for (int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i);
            if (item != null && !item.getType().equals(Material.AIR)) {
                SelectType select;
                if (i == 0) {
                    select = SelectType.DESELECT;
                } else {
                    select = SelectType.SELECT;
                }

                ItemSelectEvent selectEvent = new ItemSelectEvent(player, item, select);
                Bukkit.getServer().getPluginManager().callEvent(selectEvent);
            }
        }
    }

    @EventHandler
    public void onItemBreak(PlayerItemBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getBrokenItem();

        SelectType action = SelectType.DESELECT;
        ItemSelectEvent selectEvent = new ItemSelectEvent(player, item, action);
        Bukkit.getServer().getPluginManager().callEvent(selectEvent);
    }
}
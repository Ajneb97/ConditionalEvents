package ce.ajneb97.libs.itemselectevent;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.Variable;
import ce.ajneb97.eventos.Evento;
import ce.ajneb97.eventos.TipoEvento;
import ce.ajneb97.utils.EventoUtils;
import ce.ajneb97.utils.ItemUtils;

public class ItemSelectListener implements Listener{
	
	private ArrayList<Player> jugadores = new ArrayList<Player>();
	private ConditionalEvents plugin;
	public ItemSelectListener(ConditionalEvents plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void alCambiarDeItem(PlayerItemHeldEvent event) {
		if(event.isCancelled()) {
			return;
		}
		
		Player jugador = event.getPlayer();

		int previousSlot = event.getPreviousSlot();
		int newSlot = event.getNewSlot();
		ItemStack newItem = jugador.getInventory().getItem(newSlot);
		ItemStack previousItem = jugador.getInventory().getItem(previousSlot);
		ArrayList<ItemStack> items = new ArrayList<ItemStack>();
		items.add(newItem);items.add(previousItem);
		for(int i=0;i<items.size();i++) {
			ItemStack item = items.get(i);
			if(item != null && !item.getType().equals(Material.AIR)) {
				SelectType action = null;
				if(i==0) {
					action = SelectType.SELECT;
				}else {
					action = SelectType.DESELECT;
				}
				
				ItemSelectEvent selectEvent = new ItemSelectEvent(jugador,item,action);
				Bukkit.getServer().getPluginManager().callEvent(selectEvent);
			}
		}
	}
	
	@EventHandler
	//Cuando se dropea el item tambien se deberia deseleccionar
	public void alDropearItem(PlayerDropItemEvent event) {
		if(event.isCancelled()) {
			return;
		}
		
		
		Player jugador = event.getPlayer();
		ItemStack item = event.getItemDrop().getItemStack();
		//jugador.sendMessage("drop normal");
		if(!jugadores.contains(jugador)) {
			jugadores.add(jugador);
			PlayerCustomDropEvent dropEvent = new PlayerCustomDropEvent(jugador,item,DropType.PLAYER,jugador.getInventory().getHeldItemSlot());
			Bukkit.getServer().getPluginManager().callEvent(dropEvent);
		}
		
	}
	
	@EventHandler
	public void alDropearItemCustom(PlayerCustomDropEvent event) {
		Player jugador = event.getPlayer();
		ItemStack item = event.getItem();
		DropType type = event.getDropType();
		int slot = event.getSlot();
		int slotSeleccionado = jugador.getInventory().getHeldItemSlot();
		//jugador.sendMessage("drop final "+item+" "+type+" "+slot+" "+slotSeleccionado);
		if(slot == slotSeleccionado) {
			SelectType action = SelectType.DESELECT;
			ItemSelectEvent selectEvent = new ItemSelectEvent(jugador,item,action);
			Bukkit.getServer().getPluginManager().callEvent(selectEvent);
		}
		
		new BukkitRunnable() {
			@Override
			public void run() {
				jugadores.remove(jugador);
			}
		}.runTaskLater(plugin, 3L);
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	//Cuando se agarra el item desde el suelo y llega al slot seleccionado
	public void alRecogerItem(PlayerPickupItemEvent event) {
		if(event.isCancelled()) {
			return;
		}
		
		Player jugador = event.getPlayer();
		ItemStack item = event.getItem().getItemStack();
		int slotSeleccionado = jugador.getInventory().getHeldItemSlot();
		int slotNuevo = jugador.getInventory().firstEmpty();
		if(slotNuevo == slotSeleccionado) {
			SelectType action = SelectType.SELECT;
			ItemSelectEvent selectEvent = new ItemSelectEvent(jugador,item,action);
			Bukkit.getServer().getPluginManager().callEvent(selectEvent);
		}
	}
	
	@EventHandler
	//Cuando se intenta sacar el item del inventario
	public void alClickearItemInventario(InventoryClickEvent event) {
		if(event.isCancelled()) {
			return;
		}
		Player jugador = (Player) event.getWhoClicked();
		InventoryAction action = event.getAction();
		int slot = event.getSlot();
		int slotSeleccionado = jugador.getInventory().getHeldItemSlot();
		//jugador.sendMessage("drop inventario "+action);
		if(action.name().contains("DROP")) {
			
			jugadores.add(jugador);
			PlayerCustomDropEvent dropEvent = new PlayerCustomDropEvent(jugador,event.getCurrentItem(),DropType.INVENTORY,slot);
			Bukkit.getServer().getPluginManager().callEvent(dropEvent);
			
			return;
		}
		
		ItemStack current = event.getCurrentItem();
		ItemStack cursor = event.getCursor();
		int slotHotbar = event.getHotbarButton();
		if(event.getClick().equals(ClickType.NUMBER_KEY)) {
			ItemStack item2 = jugador.getInventory().getItem(slotHotbar);
			//jugador.sendMessage(slotHotbar+" "+item2);
			if(item2 != null && !item2.getType().equals(Material.AIR)) {
				cursor = item2;
			}else if(current == null || current.getType().equals(Material.AIR)){
				current = item2;
			}
			
		}
		
		
		
		//jugador.sendMessage(current+" "+cursor+" "+slotSeleccionado+" "+slot+" "+event.getAction());
		ArrayList<ItemStack> items = new ArrayList<ItemStack>();
		if(slotSeleccionado == slot) {
			items.add(current);items.add(cursor);
		}else if(slotSeleccionado == slotHotbar) {
			items.add(cursor);items.add(current);
		}
		for(int i=0;i<items.size();i++) {
			ItemStack item = items.get(i);
			if(item != null && !item.getType().equals(Material.AIR)) {
				SelectType select = null;
				if(i==0) {
					select = SelectType.DESELECT;
				}else {
					select = SelectType.SELECT;
				}
				
				ItemSelectEvent selectEvent = new ItemSelectEvent(jugador,item,select);
				Bukkit.getServer().getPluginManager().callEvent(selectEvent);
			}
		}
	}
	
	@EventHandler
	public void alRomper(PlayerItemBreakEvent event) {
		Player jugador = event.getPlayer();
		ItemStack item = event.getBrokenItem();
		
		SelectType action = SelectType.DESELECT;
		ItemSelectEvent selectEvent = new ItemSelectEvent(jugador,item,action);
		Bukkit.getServer().getPluginManager().callEvent(selectEvent);
	}
	
//	@EventHandler
//	public void alMorir(PlayerDeathEvent event) {
//		Player jugador = event.getEntity();
//		ItemStack item = jugador.getItemInHand();
//		if(item != null && !item.getType().equals(Material.AIR)) {
//			SelectType action = SelectType.DESELECT;
//			ItemSelectEvent selectEvent = new ItemSelectEvent(jugador,item,action);
//			Bukkit.getServer().getPluginManager().callEvent(selectEvent);
//		}
//	}
}

package ce.ajneb97.libs.itemselectevent;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

public class ItemSelectEvent extends PlayerEvent{
	
	private static final HandlerList handlers = new HandlerList();
	private SelectType selectType;
	private ItemStack item;

	public ItemSelectEvent(Player player,ItemStack item,SelectType selectType) {
		super(player);
		this.item = item;
		this.selectType = selectType;
	}

	public SelectType getSelectType() {
		return selectType;
	}

	public ItemStack getItem() {
		return item;
	}

	@Override
	public HandlerList getHandlers() {
		// TODO Auto-generated method stub
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
	    return handlers;
	}
}

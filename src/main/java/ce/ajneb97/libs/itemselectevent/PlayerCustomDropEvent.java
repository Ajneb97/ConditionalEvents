package ce.ajneb97.libs.itemselectevent;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerCustomDropEvent extends PlayerEvent{

	private static final HandlerList handlers = new HandlerList();
	private DropType dropType;
	private ItemStack item;
	private int slot;

	public PlayerCustomDropEvent(Player player,ItemStack item,DropType dropType,int slot) {
		super(player);
		this.item = item;
		this.dropType = dropType;
		this.slot = slot;
	}

	public DropType getDropType() {
		return dropType;
	}

	public int getSlot() {
		return slot;
	}

	public ItemStack getItem() {
		return item;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
	    return handlers;
	}
}

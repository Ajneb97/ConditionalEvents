package ce.ajneb97.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class ConditionalEventsEvent extends Event{

	private Player player;
	private String event;
	private String action;
	private static final HandlerList handlers = new HandlerList();
	
	//Event called when a player has finished a conversation.
	public ConditionalEventsEvent(Player player,String event,String action){
		this.player = player;
		this.event = event;
		this.action = action;
	}	
	
	public Player getPlayer() {
		return player;
	}

	public String getEvent() {
		return event;
	}

	public String getAction() {
		return action;
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

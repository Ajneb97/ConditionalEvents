package ce.ajneb97.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class ConditionalEventsEvent extends Event{

	private Player player;
	private String event;
	private String actionGroup;
	private static final HandlerList handlers = new HandlerList();

	//Event called when conditions for an event are accomplished
	public ConditionalEventsEvent(Player player,String event,String actionGroup){
		this.player = player;
		this.event = event;
		this.actionGroup = actionGroup;
	}	
	
	public Player getPlayer() {
		return player;
	}

	public String getEvent() {
		return event;
	}

	public String getActionGroup() {
		return actionGroup;
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

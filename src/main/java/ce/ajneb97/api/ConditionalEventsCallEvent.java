package ce.ajneb97.api;

import ce.ajneb97.model.StoredVariable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.List;


public class ConditionalEventsCallEvent extends Event{

	private Player player;
	private ArrayList<StoredVariable> variables;
	private String event;
	private static final HandlerList handlers = new HandlerList();

	//Event called when conditions for an event are accomplished
	public ConditionalEventsCallEvent(Player player, ArrayList<StoredVariable> variables, String event){
		this.player = player;
		this.variables = variables;
		this.event = event;
	}	
	
	public Player getPlayer() {
		return player;
	}

	public String getEvent() {
		return event;
	}

	public ArrayList<StoredVariable> getVariables() {
		return variables;
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

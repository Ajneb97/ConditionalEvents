package ce.ajneb97.api;

import ce.ajneb97.model.StoredVariable;
import ce.ajneb97.model.internal.AdditionalEventStorage;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.List;


public class ConditionalEventsCallEvent extends Event{

	private Player player;
	private ArrayList<StoredVariable> variables;
	private String event;
	private LivingEntity target;
	private AdditionalEventStorage additionalEventStorage;
	private static final HandlerList handlers = new HandlerList();

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

	public LivingEntity getTarget() {
		return target;
	}

	public AdditionalEventStorage getAdditionalEventStorage() {
		return additionalEventStorage;
	}

	public void setTarget(LivingEntity target) {
		this.target = target;
	}

	public void setAdditionalEventStorage(AdditionalEventStorage additionalEventStorage) {
		this.additionalEventStorage = additionalEventStorage;
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

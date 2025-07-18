package ce.ajneb97.api;

import ce.ajneb97.ConditionalEvents;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class ExpansionCE extends PlaceholderExpansion {

    // We get an instance of the plugin later.
    private ConditionalEvents plugin;

    public ExpansionCE(ConditionalEvents plugin) {
    	this.plugin = plugin;
    }

    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public boolean canRegister(){
        return true;
    }

    @Override
    public String getAuthor(){
        return "Ajneb97";
    }

    @Override
    public String getIdentifier(){
        return "conditionalevents";
    }

    @Override
    public String getVersion(){
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier){

        if(player == null){
            return "";
        }

        if(identifier.startsWith("cooldown_")){
        	// %conditionalevents_cooldown_<event>%
        	String event = identifier.replace("cooldown_", "");
            return ConditionalEventsAPI.getEventCooldown(player,event);
        }else if(identifier.startsWith("onetime_ready_")){
            // %conditionalevents_onetime_ready_<event>%
            String event = identifier.replace("onetime_ready_", "");
            return ConditionalEventsAPI.getOneTimeReady(player,event);
        }else if(identifier.startsWith("is_enabled_")){
            // %conditionalevents_is_enabled_<event>%
            String event = identifier.replace("is_enabled_", "");
            return ConditionalEventsAPI.isEventEnabled(event)+"";
        }

        return null;
    }
}

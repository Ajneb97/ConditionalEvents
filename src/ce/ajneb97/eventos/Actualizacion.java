package ce.ajneb97.eventos;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import ce.ajneb97.ConditionalEvents;

public class Actualizacion implements Listener{
	private ConditionalEvents plugin;
	public Actualizacion(ConditionalEvents  plugin){		
		this.plugin = plugin;		
	}
	@EventHandler
	public void Entrar(PlayerJoinEvent event){
		Player jugador = event.getPlayer();
		FileConfiguration config = plugin.getConfig();	
		if(config.getString("Config.update_notification").equals("true")) {
			if(jugador.isOp() && !(plugin.version.equals(plugin.latestversion))){
				jugador.sendMessage(plugin.nombrePlugin + ChatColor.RED +" There is a new version available. "+ChatColor.YELLOW+
		  				  "("+ChatColor.GRAY+plugin.latestversion+ChatColor.YELLOW+")");
		  		    jugador.sendMessage(ChatColor.RED+"You can download it at: "+ChatColor.GREEN+"https://www.spigotmc.org/resources/82271/");			 
			}
		}
	}
}

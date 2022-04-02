package ce.ajneb97.managers;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerCommandEvent;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.Variable;
import ce.ajneb97.eventos.Evento;
import ce.ajneb97.eventos.TipoEvento;
import ce.ajneb97.utils.EventoUtils;

public class OtrosListener implements Listener{

	public ConditionalEvents plugin;
	public OtrosListener(ConditionalEvents plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void alSpawnearEntidad(CreatureSpawnEvent event) {
		String tipoEntidad = event.getEntityType().name();
		String mundo = event.getLocation().getWorld().getName();
		String razonSpawn = event.getSpawnReason().name();
		
		ArrayList<Evento> eventos = EventoUtils.getEventosAceptados(plugin.getEventos(), TipoEvento.ENTITY_SPAWN);
		for(Evento e : eventos) {
			ArrayList<Variable> variables = EventoUtils.getVariablesAVerificar(e.getCondiciones(), e.getAcciones(), null, false);
			EventoUtils.remplazarVariable(variables, "%entity%", tipoEntidad);
			EventoUtils.remplazarVariable(variables, "%entity_world%", mundo);
			EventoUtils.remplazarVariable(variables, "%reason%", razonSpawn);
			EventoUtils.comprobarEvento(e, null, variables, event, plugin, false);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void alUsarComandoConsola(ServerCommandEvent event) {
		String comando = event.getCommand();
		String[] args = comando.split(" ");
		ArrayList<Evento> eventos = EventoUtils.getEventosAceptados(plugin.getEventos(), TipoEvento.CONSOLE_COMMAND);
		for(Evento e : eventos) {
			ArrayList<Variable> variables = EventoUtils.getVariablesAVerificar(e.getCondiciones(), e.getAcciones(), null, false);
			EventoUtils.remplazarVariable(variables, "%command%", comando);
			for(int i=1;i<args.length;i++) {
				EventoUtils.remplazarVariable(variables, "%arg_"+(i)+"%", args[i]);
			}
			EventoUtils.remplazarVariable(variables, "%args_length%", (args.length-1)+"");
			
			EventoUtils.comprobarEvento(e, null, variables, event, plugin, false);
		}
	}
}

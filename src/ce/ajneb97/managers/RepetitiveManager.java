package ce.ajneb97.managers;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.Variable;
import ce.ajneb97.eventos.Evento;
import ce.ajneb97.utils.EventoUtils;

public class RepetitiveManager {

	private ConditionalEvents plugin;
	private Evento evento;
	private long ticks;
	int taskID;
	private boolean terminar;
	
	public RepetitiveManager(ConditionalEvents plugin,Evento evento,long ticks){		
		this.plugin = plugin;		
		this.evento = evento;
		this.ticks = ticks;
		this.terminar = false;
	}
	
	public void terminar() {
		this.terminar = true;
	}
	
	public void iniciarPlayers(){
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
 	    taskID = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
		public void run(){
			if(terminar || !ejecutarPlayers()){
				Bukkit.getScheduler().cancelTask(taskID);
				return;
			}	
 		   }
 	   }, 0L, ticks);
	}

	protected boolean ejecutarPlayers() {
		if(evento == null) {
			return false;
		}else {
			new BukkitRunnable() {
				@Override
				public void run() {
					for(Player jugador : Bukkit.getOnlinePlayers()) {
						ArrayList<Variable> variables = EventoUtils.getVariablesAVerificar(evento.getCondiciones(), evento.getAcciones(), jugador, false);
						EventoUtils.comprobarEvento(evento, jugador, variables, null, plugin);
					}
				}
			}.runTaskAsynchronously(plugin);
			
			return true;
		}
	}
	
	public void iniciarServer(){
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
 	    taskID = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
		public void run(){
			if(terminar || !ejecutarServer()){
				Bukkit.getScheduler().cancelTask(taskID);
				return;
			}	
 		   }
 	   }, 0L, ticks);
	}

	protected boolean ejecutarServer() {
		if(evento == null) {
			return false;
		}else {
			new BukkitRunnable() {
				@Override
				public void run() {
					ArrayList<Variable> variables = EventoUtils.getVariablesAVerificar(evento.getCondiciones(), evento.getAcciones(), null, false);
					EventoUtils.comprobarEvento(evento, null, variables, null, plugin);
				}
			}.runTaskAsynchronously(plugin);
			
			return true;
		}
	}
}

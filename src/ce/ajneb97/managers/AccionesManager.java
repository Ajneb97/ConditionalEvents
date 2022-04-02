package ce.ajneb97.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.Variable;
import ce.ajneb97.api.ConditionalEventsAPI;
import ce.ajneb97.api.ConditionalEventsEvent;
import ce.ajneb97.eventos.Acciones;
import ce.ajneb97.eventos.Evento;
import ce.ajneb97.eventos.PropiedadesRange;
import ce.ajneb97.eventos.PropiedadesWorld;
import ce.ajneb97.eventos.TipoEvento;
import ce.ajneb97.libs.actionbar.ActionBarAPI;
import ce.ajneb97.utils.BlockUtils;
import ce.ajneb97.utils.ItemUtils;
import ce.ajneb97.utils.MensajeUtils;
import ce.ajneb97.utils.Utilidades;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class AccionesManager {
	
	private Player jugador;
	private Evento evento;
	private ArrayList<Variable> variables;
	private Event minecraftEvent;
	private ConditionalEvents plugin;
	
	private List<String> acciones;
	private int currentPos;
	
	//Para BlockBreakEvent
	private Material blockMaterial;
	private Location blockLocation;
	private BlockData blockData;
	private String headTexture;
	
	//Para PLAYER_ATTACK
	private LivingEntity victima;
	
	//Para ServerCommandEvent
	private CommandSender sender;
	
	private int taskID = -1;
	
	public AccionesManager(Player jugador, Evento evento, ArrayList<Variable> variables, Event event, ConditionalEvents plugin) {
		this.jugador = jugador;
		this.evento = evento;
		this.variables = variables;
		this.minecraftEvent = event;
		this.plugin = plugin;
		
		if(minecraftEvent instanceof BlockBreakEvent) {
			BlockBreakEvent breakEvent = (BlockBreakEvent) minecraftEvent;
			Block bloqueDestruido = breakEvent.getBlock();
			if(bloqueDestruido != null) {
				blockMaterial = bloqueDestruido.getType();
				blockLocation = bloqueDestruido.getLocation().clone();
				headTexture = BlockUtils.getHeadTextureData(bloqueDestruido);
				if(Utilidades.esVersionNueva()) {
					blockData = bloqueDestruido.getState().getBlockData();
				}
			}
		}else if(minecraftEvent instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) minecraftEvent;
			if(damageEvent.getEntity() instanceof LivingEntity) {
				victima = (LivingEntity) damageEvent.getEntity();
			}
		}else if(minecraftEvent instanceof EntityDeathEvent) {
			EntityDeathEvent deathEvent = (EntityDeathEvent) minecraftEvent;
			if(deathEvent.getEntity() instanceof LivingEntity) {
				victima = (LivingEntity) deathEvent.getEntity();
			}
		}else if(minecraftEvent instanceof PlayerInteractEntityEvent) {
			PlayerInteractEntityEvent interactEvent = (PlayerInteractEntityEvent) minecraftEvent;
			if(interactEvent.getRightClicked() instanceof LivingEntity) {
				victima = (LivingEntity) interactEvent.getRightClicked();
			}
		}
	}
	
	public void setCommandSender(CommandSender sender) {
		this.sender = sender;
	}

	public void ejecutarTodo(String tipo,boolean isAsync) {
		this.currentPos = 0;
		List<String> acciones = new ArrayList<String>();
		for(Acciones a : evento.getAcciones()) {
			if(a.getNombre().equals(tipo)) {
				acciones = new ArrayList<String>(a.getAcciones());
			}
		}
		this.acciones = acciones;
		
		//Esto se ejecuta sync, pero async en los repetitive events lo que puede
		//llevar a bugs
		//Hay que detectar el cancel_event primero y ejecutar TODAS las acciones
		//en sync.
		cancelarEvento();
		
		if(isAsync) {
			new BukkitRunnable() {
				@Override
				public void run() {
					ejecutarAcciones(true);
					//API
					ConditionalEventsEvent event = new ConditionalEventsEvent(jugador,evento.getNombre(),tipo);
					plugin.getServer().getPluginManager().callEvent(event);
				}
			}.runTask(plugin);
		}else {
			ejecutarAcciones(true);
			//API
			ConditionalEventsEvent event = new ConditionalEventsEvent(jugador,evento.getNombre(),tipo);
			plugin.getServer().getPluginManager().callEvent(event);
		}
	}
	
	public void cancelarEvento() {
		for(int i=0;i<acciones.size();i++) {
			String linea = acciones.get(i);
			if(linea.startsWith("cancel_event: ")) {
				cancelEvent(linea);
			}
		}
	}
	
	public void ejecutarAcciones(boolean incluyeWait) {
		plugin.removerAccionesManager(this);
		for(int i=currentPos;i<acciones.size();i++) {
			Player jugador = this.jugador;
			LivingEntity target = null;
			
			//Remplazar variables
			for(Variable v : variables) {
				String variable = v.getNombre();
				if(acciones.get(i).contains(variable)) {
					acciones.set(i, acciones.get(i).replace(variable, v.getValor()));
				}
			}
			String linea = acciones.get(i);
			
			PropiedadesWorld pWorld = new PropiedadesWorld(false, null);
			PropiedadesRange pRange = new PropiedadesRange(false, 0);
			boolean toAll = false;
			
			if(linea.startsWith("to_target: ")) {
				// to_target: message: hola
				linea = linea.replace("to_target: ", "");
				if(this.victima instanceof Player) {
					jugador = (Player) this.victima;
				}else {
					target = this.victima;
				}	
			}else if(linea.startsWith("to_world: ")) {
				// to_world: parkour: message: hola
				linea = linea.replace("to_world: ", "");
				String world = linea.substring(0, linea.indexOf(":"));
				String replace = linea.substring(0, linea.indexOf(":")+2);
				linea = linea.replace(replace, "");
				pWorld.setActivado(true);
				pWorld.setWorld(world);
			}else if(linea.startsWith("to_range: ")) {
				// to_range: 5: message: hola
				linea = linea.replace("to_range: ", "");
				double range = Double.valueOf(linea.substring(0, linea.indexOf(":")));
				String replace = linea.substring(0, linea.indexOf(":")+2);
				linea = linea.replace(replace, "");
				pRange.setActivado(true);
				pRange.setRadio(range);
			}else if(linea.startsWith("to_all: ")) {
				// to_all: message: hola
				linea = linea.replace("to_all: ", "");
				toAll = true;	
			}
			
			if(linea.startsWith("cancel_event: ")) {
				continue;
			}else if(linea.startsWith("wait: ") && incluyeWait) {
				wait(i,linea);
				return;
			}else if(linea.startsWith("wait_ticks: ") && incluyeWait) {
				waitTicks(i,linea);
				return;
			}
			else {
				String lineaFinal = linea;
				boolean toAllFinal = toAll;
				Player jugadorFinal = jugador;
				LivingEntity targetFinal = target;
				ejecutarLinea(jugadorFinal,lineaFinal,pWorld,pRange,toAllFinal,targetFinal);
			}
		}
	}
	
	public void ejecutarLinea(Player jugador,String linea,PropiedadesWorld pWorld,PropiedadesRange pRange,boolean toAll,LivingEntity target) {
		if(linea.startsWith("message: ")) {
			message(jugador,linea,pRange,pWorld,toAll,false);
			return;
		}
		if(linea.startsWith("centered_message: ")) {
			message(jugador,linea,pRange,pWorld,toAll,true);
			return;
		}
		if(linea.startsWith("json_message: ")) {
			jsonMessage(jugador,linea,pRange,pWorld,toAll);
			return;
		}
		if(linea.startsWith("console_message: ")) {
			consoleMessage(linea);
			return;
		}
		if(linea.startsWith("console_command: ")) {
			consoleCommand(jugador,linea);
			return;
		}
		if(linea.startsWith("player_command: ")) {
			playerCommand(jugador,linea,pRange,pWorld,toAll);
			return;
		}
		if(linea.startsWith("player_command_as_op: ")) {
			playerCommandAsOp(jugador,linea,pRange,pWorld,toAll);
			return;
		}
		if(linea.startsWith("player_send_chat: ")) {
			playerSendChat(jugador,linea,pRange,pWorld,toAll);
			return;
		}
		if(linea.startsWith("send_to_server: ")) {
			sendToServer(jugador,linea,pRange,pWorld,toAll);
			return;
		}
		if(linea.startsWith("teleport: ")) {
			teleport(jugador,linea,pRange,pWorld,toAll);
			return;
		}
		if(linea.startsWith("give_potion_effect: ")) {
			givePotionEffect(jugador,linea,pRange,pWorld,target,toAll);
			return;
		}
		if(linea.startsWith("remove_potion_effect: ")) {
			removePotionEffect(jugador,linea,pRange,pWorld,target,toAll);
			return;
		}
		if(linea.startsWith("kick: ")) {
			kick(jugador,linea,pRange,pWorld,toAll);
			return;
		}
		if(linea.startsWith("playsound: ")) {
			playSound(jugador,linea,pRange,pWorld,toAll);
			return;
		}
		if(linea.startsWith("playsound_resource_pack: ")) {
			playSoundResourcePack(jugador,linea,pRange,pWorld,toAll);
			return;
		}
		if(linea.startsWith("actionbar: ")) {
			actionbar(jugador,linea,pRange,pWorld,toAll);
			return;
		}
		if(linea.startsWith("title: ")) {
			title(jugador,linea,pRange,pWorld,toAll);
			return;
		}
		if(linea.startsWith("firework: ")) {
			firework(jugador,linea,pRange,pWorld,toAll);
			return;
		}
		if(linea.startsWith("gamemode: ")) {
			gamemode(jugador,linea,pRange,pWorld,toAll);
			return;
		}
		if(linea.startsWith("remove_item: ")) {
			removeItem(jugador,linea,pRange,pWorld,toAll);
			return;
		}
		
		if(linea.startsWith("restore_block")) {
			restoreBlock();
			return;
		}
		if(linea.startsWith("keep_items: ")) {
			keepItems(linea);
			return;
		}
	}
	
	public void ejecutarWait(int segundos,int pos) {
		plugin.agregarAccionesManager(this);
		taskID = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
		     public void run() {
		          currentPos = pos+1;
		          ejecutarAcciones(true);
		     }
		}, (segundos * 20));
	}
	
	public void ejecutarWaitTicks(int ticks,int pos) {
		plugin.agregarAccionesManager(this);
		taskID = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
		     public void run() {
		          currentPos = pos+1;
		          ejecutarAcciones(true);
		     }
		}, ticks);
	}
	
	public void cancelarTask() {
		if(taskID != -1) {
			Bukkit.getServer().getScheduler().cancelTask(taskID);
	        ejecutarAcciones(false);
		}
	}
	
	public static List<Player> getJugadoresCercanos(Player jugador,double radio){
		List<Player> jugadores = new ArrayList<Player>();
		for(Entity e : jugador.getWorld().getNearbyEntities(jugador.getLocation(), radio, radio, radio)) {
			if(e instanceof Player) {
				Player p = (Player) e;
				if(!p.getName().equals(jugador.getName())) {
					jugadores.add(p);
				}
			}
		}
		return jugadores;
	}
	
	
	//METODOS DE LAS ACCIONES
	
	public void message(Player jugador,String linea,PropiedadesRange toRange,PropiedadesWorld toWorld,boolean toAll,boolean centrado) {
		if(centrado) {
			linea = linea.replace("centered_message: ", "");
			linea = MensajeUtils.getMensajeColor(linea);
			linea = MensajeUtils.getMensajeCentrado(linea);
		}else {
			linea = linea.replace("message: ", "");
			linea = MensajeUtils.getMensajeColor(linea);
		}
		
		if(sender != null) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', linea));
			return;
		}
		if(toRange.isActivado()) {
			double radio = toRange.getRadio();
			for(Player p : AccionesManager.getJugadoresCercanos(jugador, radio)) {
				p.sendMessage(linea);
			}
		}else if(toWorld.isActivado()) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				if(p.getWorld().getName().equals(toWorld.getWorld())) {
					p.sendMessage(linea);
				}
			}
		}else if(toAll) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				p.sendMessage(linea);
			}
		}
		else {
			if(jugador != null) {
				jugador.sendMessage(linea);
			}
		}
	}
	
	public void consoleMessage(String linea) {
		linea = linea.replace("console_message: ", "");
		linea = MensajeUtils.getMensajeColor(linea);
		Bukkit.getConsoleSender().sendMessage(linea);
	}
	
	public void jsonMessage(Player jugador,String linea,PropiedadesRange toRange,PropiedadesWorld toWorld,boolean toAll) {
		linea = linea.replace("json_message: ", "");
		if(toRange.isActivado()) {
			double radio = toRange.getRadio();
			for(Player p : AccionesManager.getJugadoresCercanos(jugador, radio)) {
				MensajeUtils.enviarMensajeJSON(p, linea);
			}
		}else if(toWorld.isActivado()) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				if(p.getWorld().getName().equals(toWorld.getWorld())) {
					MensajeUtils.enviarMensajeJSON(p, linea);
				}
			}
		}else if(toAll) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				MensajeUtils.enviarMensajeJSON(p, linea);
			}
		}
		else {
			if(jugador != null) {
				MensajeUtils.enviarMensajeJSON(jugador, linea);
			}
		}
	}
	
	public void consoleCommand(Player jugador,String linea) {
		linea = linea.replace("console_command: ", "");
		ConsoleCommandSender sender = Bukkit.getConsoleSender();
		Bukkit.dispatchCommand(sender, linea);
	}
	
	public void playerCommand(Player jugador,String linea,PropiedadesRange toRange,PropiedadesWorld toWorld,boolean toAll) {
		linea = linea.replace("player_command: ", "");
		if(toRange.isActivado()) {
			double radio = toRange.getRadio();
			for(Player p : AccionesManager.getJugadoresCercanos(jugador, radio)) {
				p.performCommand(linea);
			}
		}else if(toWorld.isActivado()) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				if(p.getWorld().getName().equals(toWorld.getWorld())) {
					p.performCommand(linea);
				}
			}
		}else if(toAll) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				p.performCommand(linea);
			}
		}
		else {
			if(jugador != null) {
				jugador.performCommand(linea);
			}
			
		}
	}
	
	public void playerCommandAsOp(Player jugador,String linea,PropiedadesRange toRange,PropiedadesWorld toWorld,boolean toAll) {
		linea = linea.replace("player_command_as_op: ", "");
		if(toRange.isActivado()) {
			double radio = toRange.getRadio();
			for(Player p : AccionesManager.getJugadoresCercanos(jugador, radio)) {
				boolean esOp = true;
				if(!p.isOp()) {
					esOp = false;
				}
				p.setOp(true);
				p.performCommand(linea);
				if(!esOp) {
					p.setOp(false);
				}
				
			}
		}else if(toWorld.isActivado()) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				if(p.getWorld().getName().equals(toWorld.getWorld())) {
					boolean esOp = true;
					if(!p.isOp()) {
						esOp = false;
					}
					p.setOp(true);
					p.performCommand(linea);
					if(!esOp) {
						p.setOp(false);
					}
				}
			}
		}else if(toAll) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				boolean esOp = true;
				if(!p.isOp()) {
					esOp = false;
				}
				p.setOp(true);
				p.performCommand(linea);
				if(!esOp) {
					p.setOp(false);
				}
			}
		}
		else {
			if(jugador != null) {
				boolean esOp = true;
				if(!jugador.isOp()) {
					esOp = false;
				}
				jugador.setOp(true);
				jugador.performCommand(linea);
				if(!esOp) {
					jugador.setOp(false);
				}
			}
		}
	}
	
	public void playerSendChat(Player jugador,String linea,PropiedadesRange toRange,PropiedadesWorld toWorld,boolean toAll) {
		linea = linea.replace("player_send_chat: ", "");
		if(toRange.isActivado()) {
			double radio = toRange.getRadio();
			for(Player p : AccionesManager.getJugadoresCercanos(jugador, radio)) {
				p.chat(linea);
			}
		}else if(toWorld.isActivado()) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				if(p.getWorld().getName().equals(toWorld.getWorld())) {
					p.chat(linea);
				}
			}
		}else if(toAll) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				p.chat(linea);
			}
		}
		else {
			if(jugador != null) {
				jugador.chat(linea);
			}
		}
	}
	
	public void sendToServer(Player jugador,String linea,PropiedadesRange toRange,PropiedadesWorld toWorld,boolean toAll) {
		linea = linea.replace("send_to_server: ", "");
		if(toRange.isActivado()) {
			double radio = toRange.getRadio();
			for(Player p : AccionesManager.getJugadoresCercanos(jugador, radio)) {
				ConditionalEventsAPI.sendToServer(p, linea);
			}
		}else if(toWorld.isActivado()) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				if(p.getWorld().getName().equals(toWorld.getWorld())) {
					ConditionalEventsAPI.sendToServer(p, linea);
				}
			}
		}else if(toAll) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				ConditionalEventsAPI.sendToServer(p, linea);
			}
		}
		else {
			if(jugador != null) {
				ConditionalEventsAPI.sendToServer(jugador, linea);
			}
		}
	}
	
	public void teleport(Player jugador,String linea,PropiedadesRange toRange,PropiedadesWorld toWorld,boolean toAll) {
		String[] sep = linea.replace("teleport: ", "").split(";");
		World world = Bukkit.getWorld(sep[0]);
		double x = Double.valueOf(sep[1]);
		double y = Double.valueOf(sep[2]);
		double z = Double.valueOf(sep[3]);
		float yaw = Float.valueOf(sep[4]);
		float pitch = Float.valueOf(sep[5]);
		Location l = new Location(world,x,y,z,yaw,pitch);
		
		if(toRange.isActivado()) {
			double radio = toRange.getRadio();
			for(Player p : AccionesManager.getJugadoresCercanos(jugador, radio)) {
				if(!(minecraftEvent instanceof PlayerRespawnEvent)) {
					p.teleport(l);
				}
			}
		}else if(toWorld.isActivado()) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				if(p.getWorld().getName().equals(toWorld.getWorld())) {
					if(!(minecraftEvent instanceof PlayerRespawnEvent)) {
						p.teleport(l);
					}
				}
			}
		}else if(toAll) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				p.teleport(l);
			}
		}
		else {
			if(jugador == null) {
				return;
			}
			if(minecraftEvent instanceof PlayerRespawnEvent) {
				PlayerRespawnEvent respawnEvent = (PlayerRespawnEvent) minecraftEvent;
				respawnEvent.setRespawnLocation(l);
			}else {
				jugador.teleport(l);
			}
		}
	}
	
	public void removeItem(Player jugador,String linea,PropiedadesRange toRange,PropiedadesWorld toWorld,boolean toAll) {
		if(toRange.isActivado()) {
			double radio = toRange.getRadio();
			for(Player p : AccionesManager.getJugadoresCercanos(jugador, radio)) {
				ItemUtils.removeItem(p, linea);
			}
		}else if(toWorld.isActivado()) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				if(p.getWorld().getName().equals(toWorld.getWorld())) {
					ItemUtils.removeItem(p, linea);
				}
			}
		}else if(toAll) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				ItemUtils.removeItem(p, linea);
			}
		}
		else {
			if(jugador == null) {
				return;
			}
			ItemUtils.removeItem(jugador, linea);
		}
	}
	
	public void givePotionEffect(Player jugador,String linea,PropiedadesRange toRange,PropiedadesWorld toWorld,LivingEntity target,boolean toAll) {
		String[] sep = linea.replace("give_potion_effect: ", "").split(";");
		PotionEffectType tipoPocion = PotionEffectType.getByName(sep[0]);
		int duracionPocion = Integer.valueOf(sep[1]);
		int nivelPocion = Integer.valueOf(sep[2])-1;
		PotionEffect effect = new PotionEffect(tipoPocion,duracionPocion,nivelPocion);
		
		if(toRange.isActivado()) {
			double radio = toRange.getRadio();
			for(Player p : AccionesManager.getJugadoresCercanos(jugador, radio)) {
				p.addPotionEffect(effect);
			}
		}else if(toWorld.isActivado()) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				if(p.getWorld().getName().equals(toWorld.getWorld())) {
					p.addPotionEffect(effect);
				}
			}
		}else if(toAll) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				p.addPotionEffect(effect);
			}
		}
		else {
			if(target != null) {
				target.addPotionEffect(effect);
			}else if(jugador != null){
				jugador.addPotionEffect(effect);
			}
		}
	}
	
	public void removePotionEffect(Player jugador,String linea,PropiedadesRange toRange,PropiedadesWorld toWorld,LivingEntity target,boolean toAll) {
		String nombreEfecto = linea.replace("remove_potion_effect: ", "");
		PotionEffectType tipoPocion = PotionEffectType.getByName(nombreEfecto);
		
		if(toRange.isActivado()) {
			double radio = toRange.getRadio();
			for(Player p : AccionesManager.getJugadoresCercanos(jugador, radio)) {
				p.removePotionEffect(tipoPocion);
			}
		}else if(toWorld.isActivado()) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				if(p.getWorld().getName().equals(toWorld.getWorld())) {
					p.removePotionEffect(tipoPocion);
				}
			}
		}else if(toAll) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				p.removePotionEffect(tipoPocion);
			}
		}
		else {
			if(target != null) {
				target.removePotionEffect(tipoPocion);
			}else if(jugador != null){
				jugador.removePotionEffect(tipoPocion);
			}
		}
	}
	
	public void cancelEvent(String linea) {
		linea = linea.replace("cancel_event: ", "");
		boolean cancela = Boolean.valueOf(linea);
		if(minecraftEvent instanceof Cancellable) {
			Cancellable cancellableEvent = (Cancellable) minecraftEvent;
			cancellableEvent.setCancelled(cancela);
		}
	}
	
	public void kick(Player jugador,String linea,PropiedadesRange toRange,PropiedadesWorld toWorld,boolean toAll) {
		linea = linea.replace("kick: ", "");
		
		if(toRange.isActivado()) {
			double radio = toRange.getRadio();
			for(Player p : AccionesManager.getJugadoresCercanos(jugador, radio)) {
				p.kickPlayer(ChatColor.translateAlternateColorCodes('&', linea));
			}
		}else if(toWorld.isActivado()) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				if(p.getWorld().getName().equals(toWorld.getWorld())) {
					p.kickPlayer(ChatColor.translateAlternateColorCodes('&', linea));
				}
			}
		}else if(toAll) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				p.kickPlayer(ChatColor.translateAlternateColorCodes('&', linea));
			}
		}
		else {
			if(jugador == null) {
				return;
			}
			jugador.kickPlayer(ChatColor.translateAlternateColorCodes('&', linea));
		}
	}
	
	public void playSound(Player jugador,String linea,PropiedadesRange toRange,PropiedadesWorld toWorld,boolean toAll) {
		String[] sep = linea.replace("playsound: ", "").split(";");
		Sound sonido = null;
		int volumen = 0;
		float pitch = 0;
		try {
			sonido = Sound.valueOf(sep[0]);
			volumen = Integer.valueOf(sep[1]);
			pitch = Float.valueOf(sep[2]);
		}catch(Exception e ) {
			Bukkit.getConsoleSender().sendMessage(ConditionalEvents.nombrePlugin+ChatColor.translateAlternateColorCodes('&', 
					"&7Sound Name: &c"+sep[0]+" &7is not valid. Change it in the config!"));
			return;
		}
		
		if(toRange.isActivado()) {
			double radio = toRange.getRadio();
			for(Player p : AccionesManager.getJugadoresCercanos(jugador, radio)) {
				p.playSound(jugador.getLocation(), sonido, volumen, pitch);
			}
		}else if(toWorld.isActivado()) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				if(p.getWorld().getName().equals(toWorld.getWorld())) {
					p.playSound(jugador.getLocation(), sonido, volumen, pitch);
				}
			}
		}else if(toAll) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				p.playSound(jugador.getLocation(), sonido, volumen, pitch);
			}
		}
		else {
			if(jugador == null) {
				return;
			}
			jugador.playSound(jugador.getLocation(), sonido, volumen, pitch);
		}
	}
	
	public void playSoundResourcePack(Player jugador,String linea,PropiedadesRange toRange,PropiedadesWorld toWorld,boolean toAll) {
		String[] sep = linea.replace("playsound_resource_pack: ", "").split(";");
		String sonido = sep[0];
		int volumen = Integer.valueOf(sep[1]);
		float pitch = Float.valueOf(sep[2]);

		if(toRange.isActivado()) {
			double radio = toRange.getRadio();
			for(Player p : AccionesManager.getJugadoresCercanos(jugador, radio)) {
				p.playSound(jugador.getLocation(), sonido, volumen, pitch);
			}
		}else if(toWorld.isActivado()) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				if(p.getWorld().getName().equals(toWorld.getWorld())) {
					p.playSound(jugador.getLocation(), sonido, volumen, pitch);
				}
			}
		}else if(toAll) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				p.playSound(jugador.getLocation(), sonido, volumen, pitch);
			}
		}
		else {
			if(jugador == null) {
				return;
			}
			jugador.playSound(jugador.getLocation(), sonido, volumen, pitch);
		}
	}
	
	public void actionbar(Player jugador,String linea,PropiedadesRange toRange,PropiedadesWorld toWorld,boolean toAll) {
		String[] sep = linea.replace("actionbar: ", "").split(";");
		String texto = sep[0];
		int duracion = Integer.valueOf(sep[1]);
		
		if(toRange.isActivado()) {
			double radio = toRange.getRadio();
			for(Player p : AccionesManager.getJugadoresCercanos(jugador, radio)) {
				ConditionalEventsAPI.sendActionBar(p, texto, duracion);
			}
		}else if(toWorld.isActivado()) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				if(p.getWorld().getName().equals(toWorld.getWorld())) {
					ConditionalEventsAPI.sendActionBar(p, texto, duracion);
				}
			}
		}else if(toAll) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				ConditionalEventsAPI.sendActionBar(p, texto, duracion);
			}
		}
		else {
			if(jugador == null) {
				return;
			}
			ConditionalEventsAPI.sendActionBar(jugador, texto, duracion);
		}
	}
	
	public void title(Player jugador,String linea,PropiedadesRange toRange,PropiedadesWorld toWorld,boolean toAll) {
		
		String[] sep = linea.replace("title: ", "").split(";");
		
		// title: 20;40;20;Hola que tal;Hola que tal
		// title: 20;40;20;Hola que tal;none
		int fadeIn = Integer.valueOf(sep[0]);
		int stay = Integer.valueOf(sep[1]);
		int fadeOut = Integer.valueOf(sep[2]);

		String title = sep[3];
		String subtitle = sep[4];
		if(title.equals("none")) {
			title = "";
		}
		if(subtitle.equals("none")) {
			subtitle = "";
		}
		if(toRange.isActivado()) {
			double radio = toRange.getRadio();
			for(Player p : AccionesManager.getJugadoresCercanos(jugador, radio)) {
				ConditionalEventsAPI.sendTitle(p, fadeIn, stay, fadeOut, title, subtitle);
			}
		}else if(toWorld.isActivado()) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				if(p.getWorld().getName().equals(toWorld.getWorld())) {
					ConditionalEventsAPI.sendTitle(p, fadeIn, stay, fadeOut, title, subtitle);
				}
			}
		}else if(toAll) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				ConditionalEventsAPI.sendTitle(p, fadeIn, stay, fadeOut, title, subtitle);
			}
		}
		else {
			if(jugador == null) {
				return;
			}
			ConditionalEventsAPI.sendTitle(jugador, fadeIn, stay, fadeOut, title, subtitle);
		}
	}
	
	private static void firework(Player jugador,String linea,PropiedadesRange toRange,PropiedadesWorld toWorld,boolean toAll) {
		ArrayList<Color> colors = new ArrayList<Color>();
		Type type = null;
		ArrayList<Color> fadeColors = new ArrayList<Color>();
		int power = 0;

		String[] sep = linea.split(" ");
		for(String s : sep) {
			if(s.startsWith("colors:")) {
				s = s.replace("colors:", "");
				String[] colorsSep = s.split(",");
				for(String colorSep : colorsSep) {
					colors.add(getColorFromName(colorSep));
				}
			}else if(s.startsWith("type:")) {
				s = s.replace("type:", "");
				type = Type.valueOf(s);
			}else if(s.startsWith("fade:")) {
				s = s.replace("fade:", "");
				String[] colorsSep = s.split(",");
				for(String colorSep : colorsSep) {
					fadeColors.add(getColorFromName(colorSep));
				}
			}else if(s.startsWith("power:")) {
				s = s.replace("power:", "");
				power = Integer.valueOf(s);
			}
		}
		
		if(toRange.isActivado()) {
			double radio = toRange.getRadio();
			for(Player p : AccionesManager.getJugadoresCercanos(jugador, radio)) {
				ConditionalEventsAPI.spawnFirework(p.getLocation(), colors, type, fadeColors, power);
			}
		}else if(toWorld.isActivado()) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				if(p.getWorld().getName().equals(toWorld.getWorld())) {
					ConditionalEventsAPI.spawnFirework(p.getLocation(), colors, type, fadeColors, power);
				}
			}
		}else if(toAll) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				ConditionalEventsAPI.spawnFirework(p.getLocation(), colors, type, fadeColors, power);
			}
		}
		else {
			if(jugador == null) {
				return;
			}
			ConditionalEventsAPI.spawnFirework(jugador.getLocation(), colors, type, fadeColors, power);
		}
		
		
	}
	
	public void gamemode(Player jugador,String linea,PropiedadesRange toRange,PropiedadesWorld toWorld,boolean toAll) {
		linea = linea.replace("gamemode: ", "");
		
		if(toRange.isActivado()) {
			double radio = toRange.getRadio();
			for(Player p : AccionesManager.getJugadoresCercanos(jugador, radio)) {
				p.setGameMode(GameMode.valueOf(linea));
			}
		}else if(toWorld.isActivado()) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				if(p.getWorld().getName().equals(toWorld.getWorld())) {
					p.setGameMode(GameMode.valueOf(linea));
				}
			}
		}else if(toAll) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				p.setGameMode(GameMode.valueOf(linea));
			}
		}
		else {
			if(jugador == null) {
				return;
			}
			jugador.setGameMode(GameMode.valueOf(linea));
		}
	}
	
	public void wait(int i,String linea) {
		linea = linea.replace("wait: ", "");
		int waitTime = Integer.valueOf(linea);
		int pos = i;
		ejecutarWait(waitTime,pos);
	}
	
	public void waitTicks(int i,String linea) {
		linea = linea.replace("wait_ticks: ", "");
		int waitTime = Integer.valueOf(linea);
		int pos = i;
		ejecutarWaitTicks(waitTime,pos);
	}
	
	public void restoreBlock() {
		if(blockMaterial != null) {
			
			blockLocation.getBlock().setType(blockMaterial);
			Block block = blockLocation.getBlock();
			if(blockData != null) {
				block.setBlockData(blockData);
				block.getState().update(true);
			}
			BlockUtils.setHeadTextureData(block, headTexture);
			
		}
	}
	
	public void keepItems(String linea) {
		linea = linea.replace("keep_items: ", "");
		if(minecraftEvent instanceof PlayerDeathEvent) {
			PlayerDeathEvent deathEvent = (PlayerDeathEvent) minecraftEvent;
			if(linea.equals("items")) {
				deathEvent.setKeepInventory(true);
				deathEvent.getDrops().clear();
			}else if(linea.equals("xp")) {
				deathEvent.setKeepLevel(true);
				deathEvent.setDroppedExp(0);
			}else if(linea.equals("all")) {
				deathEvent.setKeepInventory(true);
				deathEvent.setKeepLevel(true);
				deathEvent.getDrops().clear();
				deathEvent.setDroppedExp(0);
			}
		}
	}
	
	private static Color getColorFromName(String colorName) {
		switch(colorName) {
		case "AQUA":
			return Color.AQUA;
		case "BLACK":
			return Color.BLACK;
		case "BLUE":
			return Color.BLUE;
		case "FUCHSIA":
			return Color.FUCHSIA;
		case "GRAY":
			return Color.GRAY;
		case "GREEN":
			return Color.GREEN;
		case "LIME":
			return Color.LIME;
		case "MAROON":
			return Color.MAROON;
		case "NAVY":
			return Color.NAVY;
		case "OLIVE":
			return Color.OLIVE;
		case "ORANGE":
			return Color.ORANGE;
		case "PURPLE":
			return Color.PURPLE;
		case "RED":
			return Color.RED;
		case "SILVER":
			return Color.SILVER;
		case "TEAL":
			return Color.TEAL;
		case "WHITE":
			return Color.WHITE;
		case "YELLOW":
			return Color.YELLOW;
		}
		return null;
	}
}

package ce.ajneb97.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.Variable;
import ce.ajneb97.eventos.Acciones;
import ce.ajneb97.eventos.Evento;
import ce.ajneb97.eventos.TipoEvento;
import ce.ajneb97.managers.AccionesManager;
import ce.ajneb97.managers.JugadorManager;
import me.clip.placeholderapi.PlaceholderAPI;

public class EventoUtils {

	
	public static ArrayList<Evento> getEventosAceptados(ArrayList<Evento> eventos,TipoEvento tipoEvento){
		ArrayList<Evento> eventosAceptados = new ArrayList<Evento>();
		for(Evento evento : eventos) {
			ArrayList<TipoEvento> tipos = evento.getTipos();
			if(tipos.contains(tipoEvento) && evento.isActivado()) {
				eventosAceptados.add(evento);
			}
		}
		return eventosAceptados;
	}
	
	public static ArrayList<Variable> getVariablesAVerificar(List<String> condiciones,List<Acciones> acciones,Player jugador, boolean target){
		return getVariablesAVerificarMain(new ArrayList<Variable>(),condiciones,acciones,jugador,target);
	}
	
	public static ArrayList<Variable> getVariablesAVerificarTarget(ArrayList<Variable> variables,List<String> condiciones,List<Acciones> acciones,Player jugador, boolean target){
		return getVariablesAVerificarMain(variables,condiciones,acciones,jugador,target);
	}
	
	public static ArrayList<Variable> getVariablesAVerificarMain(ArrayList<Variable> variables,List<String> condiciones,List<Acciones> acciones,Player jugador, boolean target){
		for(int i=0;i<condiciones.size();i++) {
			String linea = condiciones.get(i);
			for(int c=0;c<linea.length();c++) {
				if(linea.charAt(c) == '%') {
					if(c+1 < linea.length()) {
						int lastPos = linea.indexOf("%", c+1);
						if(lastPos == -1) {
							continue;
						}
						String variable = linea.substring(c,lastPos+1);
						if(!listaYaContieneVariable(variables,variable)) {
							if((variable.contains("target:") && target)
									|| (!variable.contains("target:") && !target)) {
								agregarVariable(variables,variable,jugador,target);
							}
						}
						c = lastPos;
					}
				}
			}
		}
		for(Acciones a : acciones) {
			List<String> accionesLista = a.getAcciones();
			for(int i=0;i<accionesLista.size();i++) {
				
				String linea = accionesLista.get(i);
				for(int c=0;c<linea.length();c++) {
					if(linea.charAt(c) == '%') {
						if(c+1 < linea.length()) {
							int lastPos = linea.indexOf("%", c+1);
							if(lastPos == -1) {
								continue;
							}
							String variable = linea.substring(c,lastPos+1);
							if(!listaYaContieneVariable(variables,variable)) {
								if((variable.contains("target:") && target)
										|| (!variable.contains("target:") && !target)) {
									agregarVariable(variables,variable,jugador,target);
								}
							}
							c = lastPos;
						}
					}
				}
			}
			
		}
		return variables;
	}
	
	//Cuando la variabla target es true:
	//antes de la variabe se le agrega "target" y si hay una variable asi, remplazarla con
	//los datos del jugador target
	//Ejemplo de la variable del nombre del jugador:
	//Normal: %player%
	//Target: %target:player%
	private static void agregarVariable(ArrayList<Variable> variables, String variable, Player jugador, boolean target) {
		String targetText = "";
		if(target) {
			targetText = "target:";
		}
		
		if(variable.equals("%"+targetText+"player_x%")) {
			variables.add(new Variable("%"+targetText+"player_x%",jugador.getLocation().getBlockX()+""));
			return;
		}else if(variable.equals("%"+targetText+"player_y%")) {
			variables.add(new Variable("%"+targetText+"player_y%",jugador.getLocation().getBlockY()+""));
			return;
		}else if(variable.equals("%"+targetText+"player_z%")) {
			variables.add(new Variable("%"+targetText+"player_z%",jugador.getLocation().getBlockZ()+""));
			return;
		}else if(variable.equals("%"+targetText+"player_world%")) {
			variables.add(new Variable("%"+targetText+"player_world%",jugador.getLocation().getWorld().getName()));
			return;
		}else if(variable.equals("%"+targetText+"player_gamemode%")) {
			variables.add(new Variable("%"+targetText+"player_gamemode%",jugador.getGameMode().name()));
			return;
		}else if(variable.equals("%"+targetText+"player%")) {
			variables.add(new Variable("%"+targetText+"player%",jugador.getName()));
			return;
		}else if(variable.equals("%"+targetText+"player_is_sneaking%")) {
			variables.add(new Variable("%"+targetText+"player_is_sneaking%",jugador.isSneaking()+""));
			return;
		}
		else if(variable.startsWith("%"+targetText+"player_has_potioneffect_")) {
			// %player_has_potioneffect_<type>%
			String tipoEfecto = variable.replace("%"+targetText+"player_has_potioneffect_", "").replace("%", "");
			PotionEffectType efecto = PotionEffectType.getByName(tipoEfecto);
			boolean tieneEfecto = jugador.hasPotionEffect(efecto);
			variables.add(new Variable(variable,tieneEfecto+""));
		}
		else if(variable.equals("%random_player%")) {
			int random = new Random().nextInt(Bukkit.getOnlinePlayers().size());
			ArrayList<Player> players = new ArrayList<Player>(Bukkit.getOnlinePlayers());
			if(players.size() == 0) {
				variables.add(new Variable("%random_player%","none"));
			}else {
				Player player = players.get(random); 
				variables.add(new Variable("%random_player%",player.getName()));
			}
			
			return;
		}else if(variable.startsWith("%random_player_")) {
			String variableLR = variable.replace("%random_player_", "").replace("%", "");
			try {
				ArrayList<Player> playersWorld = new ArrayList<Player>();
				World world = Bukkit.getWorld(variableLR);
				for(Player p : Bukkit.getOnlinePlayers()) {
					if(p.getWorld().getName().equals(world.getName())) {
						playersWorld.add(p);
					}
				}
				if(playersWorld.size() == 0) {
					variables.add(new Variable(variable,"none"));
				}else {
					int random = new Random().nextInt(playersWorld.size());
					Player player = playersWorld.get(random); 
					variables.add(new Variable(variable,player.getName()));
				}
			}catch(Exception e) {
				
			}
			return;
		}
		
		if(variable.startsWith("%random_")) {
			// %random_min-max%
			String variableLR = variable.replace("%random_", "").replace("%", "");
			String[] variableLRSplit = variableLR.split("-");
			int num1 = Integer.valueOf(variableLRSplit[0]);
			int num2 = Integer.valueOf(variableLRSplit[1]);
			int numFinal = Utilidades.getNumeroAleatorio(num1, num2);
			variables.add(new Variable(variable,numFinal+""));
		}else if(variable.startsWith("%"+targetText+"armor_name_")) {
			// %armor_name_<type>%
			String tipoArmor = variable.replace("%"+targetText+"armor_name_", "").replace("%", "");
			ItemStack item = null;
			if(tipoArmor.equals("helmet")) {
				item = jugador.getEquipment().getHelmet();
			}else if(tipoArmor.equals("chestplate")) {
				item = jugador.getEquipment().getChestplate();
			}else if(tipoArmor.equals("leggings")) {
				item = jugador.getEquipment().getLeggings();
			}else {
				item = jugador.getEquipment().getBoots();
			}
			ItemUtils itemUtils = new ItemUtils(item);
			String name = itemUtils.getNombre();
			variables.add(new Variable(variable,name));
		}else if(variable.startsWith("%"+targetText+"armor_")) {
			// %armor_<type>%
			String tipoArmor = variable.replace("%"+targetText+"armor_", "").replace("%", "");
			ItemStack item = null;
			String material = "AIR";
			if(tipoArmor.equals("helmet")) {
				item = jugador.getEquipment().getHelmet();
			}else if(tipoArmor.equals("chestplate")) {
				item = jugador.getEquipment().getChestplate();
			}else if(tipoArmor.equals("leggings")) {
				item = jugador.getEquipment().getLeggings();
			}else {
				item = jugador.getEquipment().getBoots();
			}
			if(item != null) {
				material = item.getType().name();
			}
			variables.add(new Variable(variable,material));
		}else if(variable.startsWith("%block_at_")) {
			// %block_at_x_y_z_world%
			String variableLR = variable.replace("%block_at_", "").replace("%", "");
			String[] variableLRSplit = variableLR.split("_");
			try {
				int x = Integer.valueOf(variableLRSplit[0]);
				int y = Integer.valueOf(variableLRSplit[1]);
				int z = Integer.valueOf(variableLRSplit[2]);
				String worldName = "";
				for(int i=3;i<variableLRSplit.length;i++) {
					if(i == variableLRSplit.length - 1) {
						worldName = worldName+variableLRSplit[i];
					}else {
						worldName = worldName+variableLRSplit[i]+"_";
					}
					
				}
				World world = Bukkit.getWorld(worldName);
				Material material = world.getBlockAt(x, y, z).getType();
				variables.add(new Variable(variable,material.name()));
			}catch(Exception e) {
				//Solo se guarda la variable pero no se obtiene el valor
				variables.add(new Variable(variable,""));
			}
		}else if(variable.startsWith("%is_nearby_")) {
			// %is_nearby_x_y_z_world_radius%
			String variableLR = variable.replace("%is_nearby_", "").replace("%", "");
			String[] variableLRSplit = variableLR.split("_");
			try {
				int x = Integer.valueOf(variableLRSplit[0]);
				int y = Integer.valueOf(variableLRSplit[1]);
				int z = Integer.valueOf(variableLRSplit[2]);
				String worldName = "";
				for(int i=3;i<variableLRSplit.length-1;i++) {
					if(i == variableLRSplit.length - 2) {
						worldName = worldName+variableLRSplit[i];
					}else {
						worldName = worldName+variableLRSplit[i]+"_";
					}
					
				}
				World world = Bukkit.getWorld(worldName);
				double radius = Double.valueOf(variableLRSplit[variableLRSplit.length-1]);
				
				Location l1 = new Location(world,x,y,z);
				Location l2 = jugador.getLocation();
				double distance = l1.distance(l2);
				
				if(distance <= radius) {
					//Si
					variables.add(new Variable(variable,"true"));
				}else {
					//No
					variables.add(new Variable(variable,"false"));
				}
			}catch(Exception e) {
				variables.add(new Variable(variable,"false"));
			}
		}else if(variable.startsWith("%world_time_")) {
			String variableLR = variable.replace("%world_time_", "").replace("%", "");
			World world = Bukkit.getWorld(variableLR);
			variables.add(new Variable(variable,world.getTime()+""));
		}
		else if(variable.equals("%empty%")) {
			variables.add(new Variable(variable,""));
		}else if(variable.equals("%block_below%")) {
			Location l = jugador.getLocation().clone().add(0, -1, 0);
			Block block = l.getBlock();
			String blockType = "AIR";
			if(block != null) {
				blockType = block.getType().name();
			}
			variables.add(new Variable(variable,blockType));
		}
		else {
			if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
				//Ejemplo de variable target:
				// %target:statistic_jumps%
				
				String variableNueva = variable.replace("target:", "");
				if(jugador == null) {
					//Se remplaza la variable usando un jugador aleatorio
					ArrayList<Player> allPlayers = new ArrayList<Player>();
					for(Player player : Bukkit.getOnlinePlayers()) {
						allPlayers.add(player);
					}
					if(allPlayers.size() == 0) {
						//Solo se guarda la variable pero no se obtiene el valor
						variables.add(new Variable(variable,""));
					}else {
						int random = new Random().nextInt(allPlayers.size());
						Player player = allPlayers.get(random); 
						String valor = PlaceholderAPI.setPlaceholders(player, variableNueva);
						variables.add(new Variable(variable,valor));
					}
				}else {
					String valor = PlaceholderAPI.setPlaceholders(jugador, variableNueva);
					variables.add(new Variable(variable,valor));
				}
			}else {
				//Solo se guarda la variable pero no se obtiene el valor
				variables.add(new Variable(variable,""));
			}
		}
	}

	public static boolean listaYaContieneVariable(ArrayList<Variable> variables,String variable) {
		for(Variable v : variables) {
			if(v.getNombre().equals(variable)) {
				return true;
			}
		}
		return false;
	}
	
	public static void remplazarVariable(ArrayList<Variable> variables,String variable,String valor) {
		for(Variable v : variables) {
			if(v.getNombre().equals(variable)) {
				v.setValor(valor);
			}
		}
	}
	
	public static void comprobarEvento(Evento e,Player jugador,ArrayList<Variable> variables,Event event,ConditionalEvents plugin,boolean isAsync) {
		String permisoParaIgnorar = e.getPermisoParaIgnorar();
		if(jugador != null && permisoParaIgnorar != null && jugador.hasPermission(permisoParaIgnorar)) {
			//Ignora el evento y pasa al siguiente
			return;
		}
		
		JugadorManager jManager = plugin.getJugadorManager();
		String resultadoCondiciones = jManager.pasaCondiciones(variables, e.getCondiciones());
		if(!resultadoCondiciones.equals("false")) {
			//Permiso
			String permiso = e.getPermiso();
			if(jugador != null && permiso != null && !jugador.hasPermission(permiso)) {
				String mensajePermiso = e.getMensajeErrorPermiso();
				if(mensajePermiso != null) {
					jugador.sendMessage(MensajeUtils.getMensajeColor(mensajePermiso));
				}
				return;
			}
			
			//One Time
			if(jugador != null && e.isOneTime() && e.getOneTimes().contains(jugador.getName())) {
				String mensajeOneTime = e.getMensajeErrorOneTime();
				if(mensajeOneTime != null) {
					jugador.sendMessage(MensajeUtils.getMensajeColor(mensajeOneTime));
				}
				return;
			}
			
			//Cooldown
			if(jugador != null) {
				String cooldown = jManager.getCooldown(e, jugador);
				if(!cooldown.equals("listo")) {
					String mensajeCooldown = e.getMensajeErrorCooldown();
					if(mensajeCooldown != null) {
						jugador.sendMessage(MensajeUtils.getMensajeColor(mensajeCooldown)
								.replace("%time%", cooldown));
					}
					return;
				}
			}
			
			//Acciones
			AccionesManager accionesManager = new AccionesManager(jugador, e, variables,event,plugin);
			if(event != null && event instanceof ServerCommandEvent) {
				accionesManager.setCommandSender(((ServerCommandEvent) event).getSender());
			}
			if(resultadoCondiciones.equals("true")) {
				if(jugador != null) {
					jManager.reiniciarCooldown(e, jugador.getName());
				}
				accionesManager.ejecutarTodo("default",isAsync);
				if(jugador != null) {
					jManager.setCooldown(e, jugador);
					if(e.isOneTime()) {
						e.agregarOneTime(jugador.getName());
					}
				}
			}else {
				accionesManager.ejecutarTodo(resultadoCondiciones,isAsync);
			}
		}
	}
	
	public static void registrarComandos(Evento evento) {
		List<String> condiciones = evento.getCondiciones();
		for(int i=0;i<condiciones.size();i++) {
			String linea = condiciones.get(i);
			if(linea.contains(" execute ")) {
				String[] splitExecutes = linea.split(" execute ");
				linea = splitExecutes[0];
			}
			
			String[] condicionesOR = linea.split(" or ");
			for(String condicionMini : condicionesOR) {
				if(condicionMini.startsWith("%command%")) {
					String[] sep = condicionMini.split(" ");
					String comando = sep[2].replace("/", "");
					
				}
			}
		}
	}
}

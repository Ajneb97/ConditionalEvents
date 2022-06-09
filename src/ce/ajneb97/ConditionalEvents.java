package ce.ajneb97;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import ce.ajneb97.api.ConditionalEventsAPI;
import ce.ajneb97.eventos.Acciones;
import ce.ajneb97.eventos.Actualizacion;
import ce.ajneb97.eventos.Evento;
import ce.ajneb97.eventos.TipoEvento;
import ce.ajneb97.libs.armorequipevent.ArmorListener;
import ce.ajneb97.libs.formulas.FormulasAPI;
import ce.ajneb97.libs.itemselectevent.ItemSelectListener;
import ce.ajneb97.libs.itemselectevent.ItemSelectListenerNew;
import ce.ajneb97.managers.AccionesManager;
import ce.ajneb97.managers.CustomEventListener;
import ce.ajneb97.managers.JugadorListener;
import ce.ajneb97.managers.JugadorManager;
import ce.ajneb97.managers.OtrosListener;
import ce.ajneb97.managers.RepetitiveManager;
import ce.ajneb97.managers.VerifyManager;
import ce.ajneb97.utils.EventoUtils;


public class ConditionalEvents extends JavaPlugin implements PluginMessageListener{
  
	PluginDescriptionFile pdfFile = getDescription();
	public String version = pdfFile.getVersion();
	public String latestversion;
	private FileConfiguration config = null;
	private File configFile = null;
	private String rutaConfig;
	private FileConfiguration players = null;
	private File playersFile = null;
	
	private ArrayList<Evento> eventos;
	private ArrayList<AccionesManager> accionesWait;
	
	private VerifyManager verificador;
	private JugadorManager jugadorManager;
	
	private FormulasAPI formulasAPI;
	
	public static String nombrePlugin = ChatColor.translateAlternateColorCodes('&', "&4[&bConditionalEvents&4] ");
	private boolean primeraVez = false;
	
	public void onEnable(){
	   this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
	   this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
	   eventos = new ArrayList<Evento>();
	   accionesWait = new ArrayList<AccionesManager>();
	   jugadorManager = new JugadorManager(this);
	   formulasAPI = new FormulasAPI();
	   registerCommands();
	   registerConfig();
	   registerPlayers();
	   if(primeraVez) {
		   generarEventosDefault();
	   }
	   cargarEventos();
	   registerEvents();
	   ConditionalEventsAPI api = new ConditionalEventsAPI(this);
	   
	   comprobarEventos();
	   
	   checkMessagesUpdate();
	   
	   Bukkit.getConsoleSender().sendMessage(nombrePlugin+ChatColor.YELLOW + "Has been enabled! " + ChatColor.WHITE + "Version: " + version);
	   Bukkit.getConsoleSender().sendMessage(nombrePlugin+ChatColor.YELLOW + "Thanks for using my plugin!  " + ChatColor.WHITE + "~Ajneb97");
	   updateChecker();
	}
	  
	public void onDisable(){
		for(int i=0;i<accionesWait.size();i++) {
			accionesWait.get(i).cancelarTask();
			i--;
		}
		guardarEventos();
		Bukkit.getConsoleSender().sendMessage(nombrePlugin+ChatColor.YELLOW + "Has been disabled! " + ChatColor.WHITE + "Version: " + version);
	}
	
	public void recargarEvents() {
		//unregister y register de eventos
		HandlerList.unregisterAll(this);
		registerEvents();
	}
	
	public VerifyManager getVerificador() {
		return verificador;
	}

	public void comprobarEventos() {
		this.verificador = new VerifyManager(this);
	    this.verificador.comprobarEventos();
	}
	
	public ArrayList<Evento> getEventos(){
		return this.eventos;
	}
	
	public JugadorManager getJugadorManager() {
		return jugadorManager;
	}

	public Evento getEvento(String nombre) {
		for(Evento e : eventos) {
			if(e.getNombre().equals(nombre)) {
				return e;
			}
		}
		return null;
	}
	
	public void agregarAccionesManager(AccionesManager a) {
		this.accionesWait.add(a);
	}
	
	public void removerAccionesManager(AccionesManager a) {
		this.accionesWait.remove(a);
	}
	
	public void registerCommands(){
		this.getCommand("ce").setExecutor(new Comando(this));
	}
	

	public FormulasAPI getFormulasAPI() {
		return formulasAPI;
	}

	public void registerEvents(){
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new JugadorListener(this), this);
		pm.registerEvents(new Actualizacion(this), this);
		pm.registerEvents(new CustomEventListener(this), this);
		pm.registerEvents(new OtrosListener(this), this);
		pm.registerEvents(new ArmorListener(new ArrayList<String>()), this);
		pm.registerEvents(new ItemSelectListener(this), this);
		if(!Bukkit.getVersion().contains("1.8")) {
			pm.registerEvents(new ItemSelectListenerNew(), this);
		}
	}
	
	public void registerConfig(){	
		configFile = new File(this.getDataFolder(), "config.yml");
		rutaConfig = configFile.getPath();
	    if(!configFile.exists()){
	    	primeraVez = true;
	    	this.getConfig().options().copyDefaults(true);
			saveConfig();  
	    }
	}
	
	@Override
	public void saveConfig() {
		 try {
			 config.save(configFile);
		 } catch (IOException e) {
			 e.printStackTrace();
	 	}
	 }
	  
	@Override
	  public FileConfiguration getConfig() {
		    if (config == null) {
		        recargarConfig(false);
		    }
		    return config;
		}
	  
	  public boolean recargarConfig(boolean cargaEventos) {
		    if (config == null) {
		    	configFile = new File(getDataFolder(), "config.yml");
		    }
		    config = loadConfiguration(configFile);	
		    if(config == null) {
		    	return false;
		    }

		    Reader defConfigStream;
			try {
				defConfigStream = new InputStreamReader(this.getResource("config.yml"), "UTF8");
				if (defConfigStream != null) {
			        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			        config.setDefaults(defConfig);
			    }
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return false;
			}   
			guardarEventos();
			eliminarTaskActuales();
			if(cargaEventos) {
				cargarEventos();
			}
			
			return true;
		}
	  
	  public static YamlConfiguration loadConfiguration(File file) {
	    	Validate.notNull(file, "File cannot be null");

	        YamlConfiguration config = new YamlConfiguration();

	        try {
	            config.load(file);
	        } catch (FileNotFoundException ex) {
	        } catch (IOException ex) {
	            ex.printStackTrace();
	            return null;
	        } catch (InvalidConfigurationException ex) {
	            ex.printStackTrace();
	            return null;
	        }

	        return config;
	    }
	  
	  public void registerPlayers(){
		  playersFile = new File(this.getDataFolder(), "players.yml");
		  if(!playersFile.exists()){
		    	this.getPlayers().options().copyDefaults(true);
				savePlayers();
		    }
	  }
	  public void savePlayers() {
		 try {
			 players.save(playersFile);
		 } catch (IOException e) {
			 e.printStackTrace();
	 	}
	 }
	  
	  public FileConfiguration getPlayers() {
		    if (players == null) {
		        reloadPlayers();
		    }
		    return players;
		}
	  
	  public void reloadPlayers() {
		    if (players == null) {
		    playersFile = new File(getDataFolder(), "players.yml");
		    }
		    players = YamlConfiguration.loadConfiguration(playersFile);

		    Reader defConfigStream;
			try {
				defConfigStream = new InputStreamReader(this.getResource("players.yml"), "UTF8");
				if (defConfigStream != null) {
			        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			        players.setDefaults(defConfig);
			    }
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}	    
		}
	
	public void guardarEventos() {
		FileConfiguration players = getPlayers();
		for(Evento e : eventos) {
			List<String> cooldowns = e.getCooldowns();
			if(!cooldowns.isEmpty()) {
				for(int i=0;i<cooldowns.size();i++) {
					String[] sep = cooldowns.get(i).split(";");
					long millisProx = Long.valueOf(sep[1]);
					long millisActuales = System.currentTimeMillis();
					if(millisActuales >= millisProx) {
						cooldowns.remove(i);
					}
				}	
				players.set("Players.Cooldowns."+e.getNombre(), cooldowns);
			}else {
				players.set("Players.Cooldowns."+e.getNombre(), null);
			}
			List<String> oneTimes = e.getOneTimes();
			if(!oneTimes.isEmpty()) {
				players.set("Players.OneTime."+e.getNombre(), oneTimes);
			}else {
				players.set("Players.OneTime."+e.getNombre(), null);
			}
		}
		savePlayers();
	}
	
	public void eliminarTaskActuales() {
		//Eliminar los repetitive task actuales
		for(Evento e : eventos) {
			if(e.getrManager() != null) {
				e.getrManager().terminar();
			}
		}		
	}
	
	public void cargarEventos() {
		eventos = new ArrayList<Evento>();
		FileConfiguration config = getConfig();
		FileConfiguration players = getPlayers();
		if(players.contains("Players.Cooldowns")) {
			for(String key : players.getConfigurationSection("Players.Cooldowns").getKeys(false)) {
				if(!config.contains("Events."+key)) {
					players.set("Players.Cooldowns."+key, null);
				}
			}
		}
		if(players.contains("Players.OneTime")) {
			for(String key : players.getConfigurationSection("Players.OneTime").getKeys(false)) {
				if(!config.contains("Events."+key)) {
					players.set("Players.OneTime."+key, null);
				}
			}
		}
		if(config.contains("Events")) {
			for(String key : config.getConfigurationSection("Events").getKeys(false)) {
				Evento evento = new Evento(key);
				ArrayList<TipoEvento> tipos = new ArrayList<TipoEvento>();
				List<String> condiciones = new ArrayList<String>();
				List<Acciones> acciones = new ArrayList<Acciones>();
				String permiso = null;
				String mensajeErrorPermiso = null;
				String permisoParaIgnorar = null;
				long cooldown = 0;
				String mensajeErrorCooldown = null;
				List<String> cooldowns = new ArrayList<String>();
				List<String> oneTimes = new ArrayList<String>();
				String mensajeErrorOneTime = null;
				boolean oneTime = false;
				boolean activado = true;
				boolean registraComando = false;
				
				RepetitiveManager rManager = null;
				TipoEvento tipoRepetitive = null;
				
				String[] tiposSep = config.getString("Events."+key+".type").split(";");
				for(int i=0;i<tiposSep.length;i++) {
					TipoEvento tipo = TipoEvento.valueOf(tiposSep[i].toUpperCase());
					tipos.add(tipo);
					if((tipo.equals(TipoEvento.REPETITIVE) || tipo.equals(TipoEvento.REPETITIVE_SERVER)) 
							&& config.contains("Events."+key+".repetitive_time")) {
						long time = Long.valueOf(config.getString("Events."+key+".repetitive_time"));
						rManager = new RepetitiveManager(this,evento,time);
						evento.setrManager(rManager);
						tipoRepetitive = tipo;
					}else if(tipo.equals(TipoEvento.CUSTOM)) {
						String customEvent = config.getString("Events."+key+".custom_event_data.event");
						String playerVariable = null;
						if(config.contains("Events."+key+".custom_event_data.player_variable")) {
							playerVariable = config.getString("Events."+key+".custom_event_data.player_variable");
						}
						List<String> variablesToCapture = new ArrayList<String>();
						if(config.contains("Events."+key+".custom_event_data.variables_to_capture")) {
							variablesToCapture = config.getStringList("Events."+key+".custom_event_data.variables_to_capture");
						}
						evento.setCustomEvent(customEvent);
						evento.setPlayerVariable(playerVariable);
						evento.setVariablesToCapture(variablesToCapture);
					}
				}
				if(config.contains("Events."+key+".conditions")) {
					condiciones = config.getStringList("Events."+key+".conditions");
				}
				if(config.contains("Events."+key+".actions")) {
					for(String action : config.getConfigurationSection("Events."+key+".actions").getKeys(false)) {
						List<String> listaAcciones = config.getStringList("Events."+key+".actions."+action);
						Acciones accionesA = new Acciones(action,listaAcciones);
						acciones.add(accionesA);
					}
				}
				if(config.contains("Events."+key+".permission")) {
					permiso = config.getString("Events."+key+".permission");
				}
				if(config.contains("Events."+key+".permission_error_message")) {
					mensajeErrorPermiso = config.getString("Events."+key+".permission_error_message");
				}
				if(config.contains("Events."+key+".cooldown")) {
					cooldown = Integer.valueOf(config.getString("Events."+key+".cooldown"));
				}
				if(config.contains("Events."+key+".cooldown_error_message")) {
					mensajeErrorCooldown = config.getString("Events."+key+".cooldown_error_message");
				}
				if(config.contains("Events."+key+".ignore_with_permission")) {
					permisoParaIgnorar = config.getString("Events."+key+".ignore_with_permission");
				}
				if(config.contains("Events."+key+".one_time")) {
					oneTime = Boolean.valueOf(config.getString("Events."+key+".one_time"));
				}
				if(config.contains("Events."+key+".one_time_error_message")) {
					mensajeErrorOneTime = config.getString("Events."+key+".one_time_error_message");
				}
				if(players.contains("Players.Cooldowns."+key)) {
					cooldowns = players.getStringList("Players.Cooldowns."+key);
				}
				if(players.contains("Players.OneTime."+key)) {
					oneTimes = players.getStringList("Players.OneTime."+key);
				}
				if(config.contains("Events."+key+".enabled")) {
					activado = Boolean.valueOf(config.getString("Events."+key+".enabled"));
				}
//				if(config.contains("Events."+key+".register_command")) {
//					registraComando = Boolean.valueOf(config.getString("Events."+key+".register_command"));
//				}
				
				evento.setAcciones(acciones);
				evento.setCondiciones(condiciones);
				evento.setCooldown(cooldown);
				evento.setMensajeErrorCooldown(mensajeErrorCooldown);
				evento.setMensajeErrorPermiso(mensajeErrorPermiso);
				evento.setPermiso(permiso);
				evento.setPermisoParaIgnorar(permisoParaIgnorar);
				evento.setTipos(tipos);
				evento.setCooldowns(cooldowns);
				evento.setOneTimes(oneTimes);
				evento.setOneTime(oneTime);
				evento.setMensajeErrorOneTime(mensajeErrorOneTime);
				evento.setActivado(activado);
				evento.setRegistraComando(registraComando);
				
				if(rManager != null && activado) {
					if(tipoRepetitive.equals(TipoEvento.REPETITIVE_SERVER)) {
						evento.getrManager().iniciarServer();
					}else {
						evento.getrManager().iniciarPlayers();
					}
				}
				
//				if(registraComando && activado) {
//					EventoUtils.registrarComandos(evento);
//				}
				
				
				eventos.add(evento);
			}
		}
	}
	
	public void updateChecker(){
		  try {
			  HttpURLConnection con = (HttpURLConnection) new URL(
	                  "https://api.spigotmc.org/legacy/update.php?resource=82271").openConnection();
	          int timed_out = 1250;
	          con.setConnectTimeout(timed_out);
	          con.setReadTimeout(timed_out);
	          latestversion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
	          if (latestversion.length() <= 7) {
	        	  if(!version.equals(latestversion)){
	        		  Bukkit.getConsoleSender().sendMessage(ChatColor.RED +"There is a new version available. "+ChatColor.YELLOW+
	        				  "("+ChatColor.GRAY+latestversion+ChatColor.YELLOW+")");
	        		  Bukkit.getConsoleSender().sendMessage(ChatColor.RED+"You can download it at: "+ChatColor.WHITE+"https://www.spigotmc.org/resources/82271/");  
	        	  }      	  
	          }
	      } catch (Exception ex) {
	    	  Bukkit.getConsoleSender().sendMessage(nombrePlugin + ChatColor.RED +"Error while checking update.");
	      }
	  }
	
	public void generarEventosDefault() {
		FileConfiguration config = getConfig();
		
		config.set("Events.event1.type", "player_respawn");
		List<String> lista = new ArrayList<String>();
		lista.add("%player_world% equals pvp1 or %player_world% equals pvp2");
		config.set("Events.event1.conditions", lista);
		lista = new ArrayList<String>();
		lista.add("teleport: lobby;0;60;0;90;0");
		lista.add("message: &cYou died. Teleporting you back to the PvP Lobby...");
		config.set("Events.event1.actions.default", lista);
		
		config.set("Events.event2.type", "block_interact");
		lista = new ArrayList<String>();
		lista.add("%block_x% == 20");lista.add("%block_y% == 60");lista.add("%block_z% == 20");
		lista.add("%block_world% equals lobby");lista.add("%block% equals STONE_BUTTON");lista.add("%action_type% equals RIGHT_CLICK");
		config.set("Events.event2.conditions", lista);
		lista = new ArrayList<String>();
		lista.add("message: &aYou've received $500!");
		lista.add("console_command: eco give %player% 500");
		lista.add("playsound: ENTITY_PLAYER_LEVELUP;10;2");
		config.set("Events.event2.actions.default", lista);
		config.set("Events.event2.permission", "conditionalevents.event.event2");
		config.set("Events.event2.permission_error_message", "&cYou need to have a rank to use this button.");
		config.set("Events.event2.one_time", true);
		config.set("Events.event2.one_time_error_message", "&cYou can claim this reward just once!");
		
		config.set("Events.event3.type", "player_attack");
		lista = new ArrayList<String>();
		lista.add("%victim% equals PLAYER");lista.add("%item% equals DIAMOND_SWORD");
		lista.add("%item_name% equals Super Sword");lista.add("%random_1-10% >= 8");
		config.set("Events.event3.conditions", lista);
		lista = new ArrayList<String>();
		lista.add("message: &aYour diamond sword poison effect was activated!");
		lista.add("to_target: give_potion_effect: POISON;120;1");
		lista.add("to_target: message: &cYou were poisoned by &e%player%&c!");
		config.set("Events.event3.actions.default", lista);
		
		config.set("Events.event4.type", "block_break;block_place");
		lista = new ArrayList<String>();
		lista.add("%block_world% equals spawn");
		config.set("Events.event4.conditions", lista);
		lista = new ArrayList<String>();
		lista.add("cancel_event: true");
		lista.add("message: &cYou can't break or place blocks on this world.");
		lista.add("playsound: BLOCK_NOTE_BLOCK_PLING;10;0.1");
		config.set("Events.event4.actions.default", lista);
		config.set("Events.event4.ignore_with_permission", "conditionalevents.ignore.event4");
		
		config.set("Events.event5.type", "player_command");
		lista = new ArrayList<String>();
		lista.add("%command% startsWith //calc or %command% startsWith //solve or %command% startsWith //eval");
		config.set("Events.event5.conditions", lista);
		lista = new ArrayList<String>();
		lista.add("cancel_event: true");
		lista.add("kick: &cWhat are you trying to do?");
		config.set("Events.event5.actions.default", lista);
		config.set("Events.event5.ignore_with_permission", "conditionalevents.ignore.event5");
		
		config.set("Events.event6.type", "block_interact");
		lista = new ArrayList<String>();
		lista.add("%block_x% == 40");lista.add("%block_y% == 60");lista.add("%block_z% == 40");
		lista.add("%block_world% equals lobby");lista.add("%block% equals STONE_BUTTON");lista.add("%action_type% equals RIGHT_CLICK");
		lista.add("%statistic_jump% < 1000 execute actions2");
		config.set("Events.event6.conditions", lista);
		lista = new ArrayList<String>();
		lista.add("message: &aYou've received $5000!");
		lista.add("console_command: eco give %player% 5000");
		config.set("Events.event6.actions.default", lista);
		lista = new ArrayList<String>();
		lista.add("message: &cYou need at least 1000 jumps to use this button.");
		config.set("Events.event6.actions.actions2", lista);
		config.set("Events.event6.cooldown", 3600);
		config.set("Events.event6.cooldown_error_message", "&cYou need to wait &e%time% &cbefore claiming your reward again.");
		
		config.set("Events.event7.type", "repetitive");
		config.set("Events.event7.repetitive_time", 10);
		lista = new ArrayList<String>();
		lista.add("%player_world% equals plotworld");
		lista.add("%player_gamemode% !equals CREATIVE");
		config.set("Events.event7.conditions", lista);
		lista = new ArrayList<String>();
		lista.add("gamemode: CREATIVE");
		lista.add("actionbar: &6Changing gamemode to creative.;100");
		config.set("Events.event7.actions.default", lista);
		
		saveConfig();
	}
	
	public void checkMessagesUpdate(){
		  Path archivo = Paths.get(rutaConfig);
		  try{
			  String texto = new String(Files.readAllBytes(archivo));
			  if(!texto.contains("update_notification:")){
				    getConfig().set("Config.update_notification", true);
				  saveConfig();
			  }
			  if(!texto.contains("eventEnableError:")){
				  getConfig().set("Messages.eventEnableError", "&cUse &7/ce enable <event>");
				  getConfig().set("Messages.eventDisableError", "&cUse &7/ce disable <event>");
				  getConfig().set("Messages.eventEnabled", "&aEvent &7%event% &aenabled.");
				  getConfig().set("Messages.eventDisabled", "&aEvent &7%event% &adisabled.");
				  saveConfig();
			  }
		  }catch(IOException e){
			  e.printStackTrace();
		  }
	}

	@Override
	public void onPluginMessageReceived(String channel, Player jugador, byte[] message) {
		// TODO Auto-generated method stub
		if (!channel.equals("BungeeCord")) {
		      return;
		}
	}
}

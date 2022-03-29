package ce.ajneb97.managers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.Variable;
import ce.ajneb97.eventos.Evento;
import ce.ajneb97.eventos.TipoEvento;
import ce.ajneb97.utils.EventoUtils;

public class CustomEventListener implements Listener{
	
	private ConditionalEvents plugin;
	public CustomEventListener(ConditionalEvents plugin) {
		this.plugin = plugin;
		iniciar();
	}

	@SuppressWarnings("unchecked")
	public void iniciar() {
		ArrayList<Evento> eventos = EventoUtils.getEventosAceptados(plugin.getEventos(), TipoEvento.CUSTOM);
		for(Evento e : eventos) {
			String eventClass = e.getCustomEvent();
			try {
    			Class<? extends Event> clase = (Class<? extends Event>) Class.forName(eventClass);
    			EventExecutor eventExecutor = (listener, event) -> ejecutaEvento(event,e);
    			plugin.getServer().getPluginManager().registerEvent(clase, this, EventPriority.MONITOR, eventExecutor, plugin);
    		} catch (ClassNotFoundException ex) {
    			// TODO Auto-generated catch block
    			Bukkit.getConsoleSender().sendMessage(ConditionalEvents.nombrePlugin+ChatColor.translateAlternateColorCodes('&', 
    					"&cClass "+eventClass+" &cdoesn't exists for custom event."));
    		}
		}
	  }

	  public void ejecutaEvento(Event event,Evento e) {
		Class<?> clase = event.getClass();
//		Bukkit.getConsoleSender().sendMessage("Encuentra evento con clase: "+clase.getCanonicalName());
//		Bukkit.getConsoleSender().sendMessage("LLAMA EVENTO! y Encuentra metodos:");
		try {
			if (!Class.forName(e.getCustomEvent()).isAssignableFrom(clase)) {
			    return;
			}
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		List<String> variablesToCapture = e.getVariablesToCapture();
		ArrayList<Variable> variablesToCaptureRemplazadas = new ArrayList<Variable>();
		for(String linea : variablesToCapture) {
			String[] sepLinea = linea.split(";");
			String variable = sepLinea[0];
			String metodo = sepLinea[1].replace("(", "").replace(")", "");
//			Bukkit.getConsoleSender().sendMessage("metodos: "+metodo);
			Object objectFinal = null;
			if(metodo.contains(".")) {
				String[] metodos = metodo.split("\\.");
				// getPlayer
				// getName
				Class<?> claseNueva = clase;
				for(int i=0;i<metodos.length;i++) {
					String metodoP = metodos[i];
//					Bukkit.getConsoleSender().sendMessage("metodo: "+metodoP);
					Method m = obtenerMetodo(claseNueva,metodoP);
					if(i == 0) {
						objectFinal = obtenerObjeto(m,event);
					}else {
						objectFinal = obtenerObjetoDesdeObjeto(m,objectFinal);
					}
					claseNueva = objectFinal.getClass();
//					Bukkit.getConsoleSender().sendMessage("clase nueva: "+claseNueva.getCanonicalName());
				}
//				Bukkit.getConsoleSender().sendMessage("Obteniendo variable final: "+objectFinal.toString());
			}else {
				Method m = obtenerMetodo(clase,metodo);
				objectFinal = obtenerObjeto(m,event);
//				Bukkit.getConsoleSender().sendMessage("Obteniendo variable final: "+objectFinal.toString());
			}
			
			variablesToCaptureRemplazadas.add(new Variable(variable,objectFinal.toString()));
		}
		
		//Obtener jugador
		Object objectFinal = null;
		if(e.getPlayerVariable() != null) {
			String metodo = e.getPlayerVariable().replace("(", "").replace(")", "");
			if(metodo.contains(".")) {
				String[] metodos = metodo.split("\\.");
				// getPlayer
				// getName
				Class<?> claseNueva = clase;
				for(int i=0;i<metodos.length;i++) {
					String metodoP = metodos[i];
//					Bukkit.getConsoleSender().sendMessage("metodo: "+metodoP);
					Method m = obtenerMetodo(claseNueva,metodoP);
					if(i == 0) {
						objectFinal = obtenerObjeto(m,event);
					}else {
						objectFinal = obtenerObjetoDesdeObjeto(m,objectFinal);
					}
					claseNueva = objectFinal.getClass();
//					Bukkit.getConsoleSender().sendMessage("clase nueva: "+claseNueva.getCanonicalName());
				}
//				Bukkit.getConsoleSender().sendMessage("Obteniendo variable final: "+objectFinal.toString());
			}else {
				Method m = obtenerMetodo(clase,metodo);
				objectFinal = obtenerObjeto(m,event);
//				Bukkit.getConsoleSender().sendMessage("Obteniendo variable final: "+objectFinal.toString());
			}
		}
		
		
		try {
			Player jugador = null;
			if(objectFinal != null) {
				jugador = (Player) objectFinal;
			}
			
			ArrayList<Variable> variables = EventoUtils.getVariablesAVerificar(e.getCondiciones(), e.getAcciones(), jugador, false);
			for(Variable v : variablesToCaptureRemplazadas) {
				EventoUtils.remplazarVariable(variables, v.getNombre(), v.getValor());
			}
			EventoUtils.comprobarEvento(e, jugador, variables, event, plugin);
		}catch(Exception ex) {
			
		}
	  }
	  
	  public Method obtenerMetodo(Class<?> clase,String metodo) {
		  try {
				Method m = clase.getMethod(metodo);
				return m;
			} catch (NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  return null;
	  }
	  
	  public Object obtenerObjeto(Method m,Event event) {
		 try {
			Object object = m.invoke(event);
			return object;
		 } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		 }
		 return null;
	  }
	  
	  public Object obtenerObjetoDesdeObjeto(Method m,Object objeto) {
			 try {
				Object object = m.invoke(objeto);
				return object;
			 } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			 }
			 return null;
		  }
	  
//	  public String getSeccionConfig(String nombreClase,FileConfiguration config) {
//		  if(config.contains("Config.custom_events")) {
//		    	for(String key : config.getConfigurationSection("Config.custom_events").getKeys(false)) {
//		    		if(config.getString("Config.custom_events."+key+".class_event").equals(nombreClase)) {
//		    			return key;
//		    		}
//		    	}
//		  }
//		  return null;
//	  }

}

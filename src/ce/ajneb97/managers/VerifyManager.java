package ce.ajneb97.managers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.eventos.Acciones;
import ce.ajneb97.eventos.Evento;
import ce.ajneb97.utils.MensajeUtils;

public class VerifyManager {

	private ConditionalEvents plugin;
	private ArrayList<EventoError> errores;
	public VerifyManager(ConditionalEvents plugin) {
		this.plugin = plugin;
		this.errores = new ArrayList<EventoError>();
	}
	
	public void enviarVerificacion(Player jugador) {
		jugador.sendMessage(MensajeUtils.getMensajeColor("&f&l- - - - - - - - &b&lEVENTS VERIFY &f&l- - - - - - - -"));
		jugador.sendMessage(MensajeUtils.getMensajeColor(""));
		if(errores.isEmpty()) {
			jugador.sendMessage(MensajeUtils.getMensajeColor("&aThere are no errors in your events ;)"));
		}else {
			jugador.sendMessage(MensajeUtils.getMensajeColor("&e&oHover on the errors to see more information."));
			for(EventoError error : errores) {
				error.enviarMensaje(jugador);
			}
		}
		jugador.sendMessage(MensajeUtils.getMensajeColor(""));
		jugador.sendMessage(MensajeUtils.getMensajeColor("&f&l- - - - - - - - &b&lEVENTS VERIFY &f&l- - - - - - - -"));
	}
	
	public void comprobarEventos() {
		ArrayList<Evento> eventos = plugin.getEventos();
		for(Evento evento : eventos) {
			comprobarEvento(evento);
		}
	}
	
	public void comprobarEvento(Evento evento) {
		List<Acciones> acciones = evento.getAcciones();
		for(Acciones a : acciones) {
			List<String> listaAcciones = a.getAcciones();
			for(int i=0;i<listaAcciones.size();i++) {
				if(!comprobarAccion(listaAcciones.get(i))) {
					errores.add(new EventoError(EventoErrorTipo.INVALID_ACTION,evento.getNombre(),a.getNombre(),
							(i+1),0,listaAcciones.get(i)));
				}
			}
		}
		List<String> listaCondiciones = evento.getCondiciones();
		for(int i=0;i<listaCondiciones.size();i++) {
			if(!comprobarCondicion(listaCondiciones.get(i))) {
				errores.add(new EventoError(EventoErrorTipo.INVALID_CONDITION,evento.getNombre(),null,
						0,(i+1),listaCondiciones.get(i)));
			}
		}
	}
	
	
	public boolean comprobarCondicion(String linea) {
		//Formatos de condiciones
		//%variable% equals cosa
		//%variable% == 2 or %variable% == 4
		//%variable% == 2 or %variable% == 4 execute lista_accion
		String[] sepExecute = linea.split(" execute ");
		String[] sepOr = sepExecute[0].split(" or ");
		for(int i=0;i<sepOr.length;i++) {
			String[] sep = sepOr[i].split(" ");
			if(sep.length < 3) {
				return false;
			}
			if(!sep[1].equals("!=") && !sep[1].equals("==") && !sep[1].equals(">=") && 
					!sep[1].equals("<=") && !sep[1].equals(">") && !sep[1].equals("<")
					&& !sep[1].equals("equals") && !sep[1].equals("!equals") && !sep[1].equals("contains")
					&& !sep[1].equals("!contains") && !sep[1].equals("startsWith") && !sep[1].equals("!startsWith")
					&& !sep[1].equals("equalsIgnoreCase") && !sep[1].equals("!equalsIgnoreCase")) {
				return false;
			}
			if(sep[0].charAt(0) != '%' || sep[0].charAt(sep[0].length()-1) != '%') {
				return false;
			}
		}
		return true;
	}
	
	public boolean comprobarAccion(String accion) {
		if(accion.startsWith("to_world: ") || accion.startsWith("to_range: ")) {
			return true;
		}
		accion = accion.replace("to_target: ", "").replace("to_all: ", "");
		
		List<String> acciones = new ArrayList<String>();
		acciones.add("message: ");acciones.add("centered_message: ");acciones.add("json_message: ");acciones.add("console_message: ");
		acciones.add("console_command: ");acciones.add("player_command: ");acciones.add("player_command_as_op: ");
		acciones.add("player_send_chat: ");
		acciones.add("send_to_server: ");acciones.add("teleport: ");acciones.add("give_potion_effect: ");
		acciones.add("remove_potion_effect: ");acciones.add("cancel_event: ");acciones.add("kick: ");
		acciones.add("playsound: ");acciones.add("playsound_resource_pack: ");acciones.add("actionbar: ");acciones.add("title: ");
		acciones.add("gamemode: ");acciones.add("remove_item: ");acciones.add("wait: ");
		acciones.add("restore_block");acciones.add("wait_ticks: ");
		boolean empieza = false;
		for(String a : acciones) {
			if(accion.startsWith(a)) {
				empieza = true;
				break;
			}
		}
		
		return empieza;
	}
}

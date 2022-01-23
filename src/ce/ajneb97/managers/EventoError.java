package ce.ajneb97.managers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import ce.ajneb97.utils.MensajeJSON;

public class EventoError {

	private EventoErrorTipo tipo;
	private String texto;
	private List<String> tooltip;
	
	private String nombreEvento;
	private String nombreAccion;
	private int lineaAccion;
	private int lineaCondicion;
	
	public EventoError(EventoErrorTipo tipo,String nombreEvento,String nombreAccion,int lineaAccion,int lineaCondicion,String texto) {
		super();
		this.tipo = tipo;
		this.nombreEvento = nombreEvento;
		this.nombreAccion = nombreAccion;
		this.lineaAccion = lineaAccion;
		this.lineaCondicion = lineaCondicion;
		this.texto = texto;
	}
	public EventoErrorTipo getTipo() {
		return tipo;
	}
	public void setTipo(EventoErrorTipo tipo) {
		this.tipo = tipo;
	}
	public List<String> getTooltip() {
		return tooltip;
	}
	public void setTooltip(List<String> tooltip) {
		this.tooltip = tooltip;
	}
	public void enviarMensaje(Player jugador) {
		String mensaje = "&e⚠ ";
		MensajeJSON msg = null;
		List<String> hover = new ArrayList<String>();
		
		//Separar texto si asi se requiere
		List<String> textoSeparado = new ArrayList<String>();
		int posActual = 0;
		for(int i=0;i<texto.length();i++) {
			if(posActual >= 35 && texto.charAt(i) == ' ') {
				String m = texto.substring(i-posActual, i);
				posActual = 0;
				textoSeparado.add(m);
			}else {
				posActual++;
			}
			if(i==texto.length()-1) {
				String m = texto.substring(i-posActual+1, texto.length());
				textoSeparado.add(m);
			}
		}
		
		switch(tipo) {
		case INVALID_ACTION:
			msg = new MensajeJSON(jugador,mensaje+"&7Action &6"+lineaAccion+" &7from &6"+nombreAccion+" &7actions on Event &6"+nombreEvento+" &7is not valid.");
			hover.add("&eTHIS IS A WARNING!");
			hover.add("&fThe action defined for this event is probably");
			hover.add("&fnot formatted correctly or doesn't exists:");
			for(String m : textoSeparado) {
				hover.add("&c"+m);
			}
			hover.add(" ");
			hover.add("&fRemember to use a valid action from this list:");
			hover.add("&ahttps://ajneb97.gitbook.io/conditionalevents/actions");
			msg.hover(hover).enviar();
			break;
		case INVALID_CONDITION:
			msg = new MensajeJSON(jugador,mensaje+"&7Condition &6"+lineaCondicion+" &7on Event &6"+nombreEvento+" &7is not valid.");
			hover.add("&eTHIS IS A WARNING!");
			hover.add("&fThe condition defined for this event");
			hover.add("&fis probably not formatted correctly:");
			for(String m : textoSeparado) {
				hover.add("&c"+m);
			}
			hover.add(" ");
			hover.add("&fRemember to use a valid condition from this list:");
			hover.add("&ahttps://ajneb97.gitbook.io/conditionalevents/conditions");
			msg.hover(hover).enviar();
			break;
		default:
			break;
		}
	}
}

package ce.ajneb97.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;

public class MensajeJSON {

	private Player jugador;
	private String texto;
	private BaseComponent[] hover;
	private String sugerirComando;
	private String ejecutarComando;
	
	public MensajeJSON(Player jugador,String texto) {
		this.jugador = jugador;
		this.hover = null;
		this.texto = texto;	
	}
	
	public MensajeJSON hover(List<String> lista) {
		hover = new BaseComponent[lista.size()];
		for(int i=0;i<lista.size();i++) {
			TextComponent linea = new TextComponent();
			if(i == lista.size()-1) {
				//ultimo
				linea.setText(ChatColor.translateAlternateColorCodes('&', lista.get(i)));
			}else {
				linea.setText(ChatColor.translateAlternateColorCodes('&', lista.get(i))+"\n");
//				TextComponent[] hoverComponent = {linea};
//				Text text = new Text(hoverComponent);
			}
			hover[i] = linea;
		}
		return this;
	}
	
	public MensajeJSON sugerirComando(String comando) {
		this.sugerirComando = comando;
		return this;
	}
	
	public MensajeJSON ejecutarComando(String comando) {
		this.ejecutarComando = comando;
		return this;
	}
	
	public void enviar() {
		TextComponent mensaje = new TextComponent();
		mensaje.setText(ChatColor.translateAlternateColorCodes('&', texto));
		if(hover != null) {
			mensaje.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
		}
		if(sugerirComando != null) {
			mensaje.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, sugerirComando));
		}
		if(ejecutarComando != null) {
			mensaje.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ejecutarComando));
		}
		jugador.spigot().sendMessage(mensaje);
	}
}

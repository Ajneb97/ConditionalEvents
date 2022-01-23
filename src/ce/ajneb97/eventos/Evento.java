package ce.ajneb97.eventos;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import ce.ajneb97.managers.RepetitiveManager;

public class Evento {

	private String nombre;
	private ArrayList<TipoEvento> tipos;
	private List<String> condiciones;
	private List<Acciones> acciones;
	private List<String> cooldowns;
	
	private List<String> oneTimes;
	private String mensajeErrorOneTime;
	private boolean oneTime;
	
	private String permiso;
	private String mensajeErrorPermiso;
	private String permisoParaIgnorar;
	
	private long cooldown;
	private String mensajeErrorCooldown;
	
	private RepetitiveManager rManager;
	
	private boolean registraComando;
	
	private boolean activado;
	
	//Eventos custom
	private String customEvent;
	private String playerVariable;
	private List<String> variablesToCapture;
	
	public Evento(String nombre) {
		this.nombre = nombre;
	}
	
	public String getCustomEvent() {
		return customEvent;
	}

	public void setCustomEvent(String customEvent) {
		this.customEvent = customEvent;
	}

	public String getPlayerVariable() {
		return playerVariable;
	}

	public void setPlayerVariable(String playerVariable) {
		this.playerVariable = playerVariable;
	}

	public List<String> getVariablesToCapture() {
		return variablesToCapture;
	}

	public void setVariablesToCapture(List<String> variablesToCapture) {
		this.variablesToCapture = variablesToCapture;
	}

	public boolean isOneTime() {
		return oneTime;
	}

	public void setOneTime(boolean oneTime) {
		this.oneTime = oneTime;
	}

	public String getMensajeErrorOneTime() {
		return mensajeErrorOneTime;
	}

	public void setMensajeErrorOneTime(String mensajeErrorOneTime) {
		this.mensajeErrorOneTime = mensajeErrorOneTime;
	}

	public RepetitiveManager getrManager() {
		return rManager;
	}

	public void setrManager(RepetitiveManager rManager) {
		this.rManager = rManager;
	}

	public void agregarCooldown(String cooldown) {
		this.cooldowns.add(cooldown);
	}

	public List<String> getCooldowns() {
		return cooldowns;
	}

	public void setCooldowns(List<String> cooldowns) {
		this.cooldowns = cooldowns;
	}
	
	public void agregarOneTime(String jugador) {
		this.oneTimes.add(jugador);
	}
	
	public List<String> getOneTimes() {
		return oneTimes;
	}

	public void setOneTimes(List<String> oneTimes) {
		this.oneTimes = oneTimes;
	}

	public void reiniciarCooldown(String jugador) {
		for(int i=0;i<cooldowns.size();i++) {
			String[] sep = cooldowns.get(i).split(";");
			if(sep[0].equalsIgnoreCase(jugador)) {
				cooldowns.remove(i);
				return;
			}
		}
	}
	
	public void reiniciarOneTime(String jugador) {
		for(int i=0;i<oneTimes.size();i++) {
			if(oneTimes.get(i).equalsIgnoreCase(jugador)) {
				oneTimes.remove(i);
				return;
			}
		}
		
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public ArrayList<TipoEvento> getTipos() {
		return tipos;
	}

	public void setTipos(ArrayList<TipoEvento> tipos) {
		this.tipos = tipos;
	}

	public List<String> getCondiciones() {
		return condiciones;
	}

	public void setCondiciones(List<String> condiciones) {
		this.condiciones = condiciones;
	}

	public List<Acciones> getAcciones() {
		return acciones;
	}

	public void setAcciones(List<Acciones> acciones) {
		this.acciones = acciones;
	}

	public String getPermiso() {
		return permiso;
	}

	public void setPermiso(String permiso) {
		this.permiso = permiso;
	}

	public String getMensajeErrorPermiso() {
		return mensajeErrorPermiso;
	}

	public void setMensajeErrorPermiso(String mensajeErrorPermiso) {
		this.mensajeErrorPermiso = mensajeErrorPermiso;
	}

	public String getPermisoParaIgnorar() {
		return permisoParaIgnorar;
	}

	public void setPermisoParaIgnorar(String permisoParaIgnorar) {
		this.permisoParaIgnorar = permisoParaIgnorar;
	}

	public long getCooldown() {
		return cooldown;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public String getMensajeErrorCooldown() {
		return mensajeErrorCooldown;
	}

	public void setMensajeErrorCooldown(String mensajeErrorCooldown) {
		this.mensajeErrorCooldown = mensajeErrorCooldown;
	}

	public boolean isActivado() {
		return activado;
	}

	public void setActivado(boolean activado) {
		this.activado = activado;
	}

	public boolean isRegistraComando() {
		return registraComando;
	}

	public void setRegistraComando(boolean registraComando) {
		this.registraComando = registraComando;
	}
	
	
}

package ce.ajneb97.eventos;

public class PropiedadesWorld {

	private boolean activado;
	private String world;
	
	public PropiedadesWorld(boolean activado, String world) {
		this.activado = activado;
		this.world = world;
	}
	public boolean isActivado() {
		return activado;
	}
	public void setActivado(boolean activado) {
		this.activado = activado;
	}
	public String getWorld() {
		return world;
	}
	public void setWorld(String world) {
		this.world = world;
	}
	
	
}

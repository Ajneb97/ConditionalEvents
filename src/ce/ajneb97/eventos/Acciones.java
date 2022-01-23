package ce.ajneb97.eventos;

import java.util.List;

public class Acciones {

	private String nombre;
	private List<String> acciones;
	public Acciones(String nombre, List<String> acciones) {
		this.nombre = nombre;
		this.acciones = acciones;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public List<String> getAcciones() {
		return acciones;
	}
	public void setAcciones(List<String> acciones) {
		this.acciones = acciones;
	}
	
	
}

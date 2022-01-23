package ce.ajneb97.eventos;

public class PropiedadesRange {

	private boolean activado;
	private double radio;
	public PropiedadesRange(boolean activado, double radio) {
		this.activado = activado;
		this.radio = radio;
	}
	public boolean isActivado() {
		return activado;
	}
	public void setActivado(boolean activado) {
		this.activado = activado;
	}
	public double getRadio() {
		return radio;
	}
	public void setRadio(double radio) {
		this.radio = radio;
	}
	
	
}

package ce.ajneb97.managers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.Variable;
import ce.ajneb97.eventos.Evento;
import ce.ajneb97.libs.formulas.FormulasAPI;


public class JugadorManager {
	
	private ConditionalEvents plugin;
	public JugadorManager(ConditionalEvents plugin) {
		this.plugin = plugin;
	}

	public String pasaCondiciones(ArrayList<Variable> variablesGuardadas, List<String> condiciones) {
		//Retorna:
		//"true" -> Cuando pasa las condiciones normales, llega hasta el final y se deben ejecutar las acciones por default.
		//"false" -> Cuando no pasa las condiciones normales.
		//"accion" -> Cuando se deben ejecutar otras acciones diferentes a las default.
		
		FormulasAPI formulasAPI = plugin.getFormulasAPI();
		
		for(int i=0;i<condiciones.size();i++) {
			String linea = condiciones.get(i);
			//Cada linea sigue este formato:
			//%block% equals DIAMOND_ORE or %block% equals EMERALD_ORE
			
			//Una linea puede tener el formato de
			//%block% equals DIAMOND_ORE or %block% equals EMERALD_ORE executes <accion>
			String executedActions = null;
			if(linea.contains(" execute ")) {
				String[] splitExecutes = linea.split(" execute ");
				linea = splitExecutes[0];
				executedActions = splitExecutes[1];
			}

			//Se verifica que realmente la linea tiene un condicional
			//Condicionales validos:
			//equals, !equals, ==, >=, <=, >, <, !=, startsWith, !startsWith
			//Si no tiene condicional, la condicion se da como APROBADA
			//Se separan primero los OR
			String[] condicionesOR = linea.split(" or ");

			boolean lineaAprobada = false;
			for(String condicionMini : condicionesOR) {
				//Si una condicionMini no es APROBADA, pasa a la siguiente (aumentado la variable noAprobadas)
				//Si es APROBADA, termina el ciclo
				//Si despues de comprobar la linea entera, ninguna condicion se cumple y la variable lineaAprobada
				//sigue siendo false, todo el metodo retorna FALSE
				
				try {
					if(condicionMini.contains(" equals ")) {
						String[] condicionMiniSep = condicionMini.split(" equals ");
						String variableAntes = getValorVariableAntes(condicionMiniSep,variablesGuardadas);
						String variableDespues = getValorVariableDespues(condicionMiniSep,variablesGuardadas,null);
						if(variableAntes.equals(variableDespues)) {
							lineaAprobada = true;
							if(executedActions != null) {
								return executedActions;
							}
						}else if(executedActions != null) {
							lineaAprobada = true;
						}
					}else if(condicionMini.contains(" !equals ")) {
						String[] condicionMiniSep = condicionMini.split(" !equals ");
						String variableAntes = getValorVariableAntes(condicionMiniSep,variablesGuardadas);
						String variableDespues = getValorVariableDespues(condicionMiniSep,variablesGuardadas,null);
						if(!variableAntes.equals(variableDespues)) {
							lineaAprobada = true;
							if(executedActions != null) {
								return executedActions;
							}
						}else if(executedActions != null) {
							lineaAprobada = true;
						}
					}else if(condicionMini.contains(" equalsIgnoreCase ")) {
						String[] condicionMiniSep = condicionMini.split(" equalsIgnoreCase ");
						String variableAntes = getValorVariableAntes(condicionMiniSep,variablesGuardadas);
						String variableDespues = getValorVariableDespues(condicionMiniSep,variablesGuardadas,null);
						if(variableAntes.equalsIgnoreCase(variableDespues)) {
							lineaAprobada = true;
							if(executedActions != null) {
								return executedActions;
							}
						}else if(executedActions != null) {
							lineaAprobada = true;
						}
					}else if(condicionMini.contains(" !equalsIgnoreCase ")) {
						String[] condicionMiniSep = condicionMini.split(" !equalsIgnoreCase ");
						String variableAntes = getValorVariableAntes(condicionMiniSep,variablesGuardadas);
						String variableDespues = getValorVariableDespues(condicionMiniSep,variablesGuardadas,null);
						if(!variableAntes.equalsIgnoreCase(variableDespues)) {
							lineaAprobada = true;
							if(executedActions != null) {
								return executedActions;
							}
						}else if(executedActions != null) {
							lineaAprobada = true;
						}
					}
					else if(condicionMini.contains(" startsWith ")) {
						String[] condicionMiniSep = condicionMini.split(" startsWith ");
						String variableAntes = getValorVariableAntes(condicionMiniSep,variablesGuardadas);
						String variableDespues = getValorVariableDespues(condicionMiniSep,variablesGuardadas,null);
						if(variableAntes.toLowerCase().startsWith(variableDespues.toLowerCase())) {
							lineaAprobada = true;
							if(executedActions != null) {
								return executedActions;
							}
						}else if(executedActions != null) {
							lineaAprobada = true;
						}
					}else if(condicionMini.contains(" !startsWith ")) {
						String[] condicionMiniSep = condicionMini.split(" !startsWith ");
						String variableAntes = getValorVariableAntes(condicionMiniSep,variablesGuardadas);
						String variableDespues = getValorVariableDespues(condicionMiniSep,variablesGuardadas,null);
						if(!variableAntes.toLowerCase().startsWith(variableDespues.toLowerCase())) {
							lineaAprobada = true;
							if(executedActions != null) {
								return executedActions;
							}
						}else if(executedActions != null) {
							lineaAprobada = true;
						}
					}else if(condicionMini.contains(" contains ")) {
						String[] condicionMiniSep = condicionMini.split(" contains ");
						String variableAntes = getValorVariableAntes(condicionMiniSep,variablesGuardadas);
						String variableDespues = getValorVariableDespues(condicionMiniSep,variablesGuardadas,null);
						if(variableAntes.toLowerCase().contains(variableDespues.toLowerCase())) {
							lineaAprobada = true;
							if(executedActions != null) {
								return executedActions;
							}
						}else if(executedActions != null) {
							lineaAprobada = true;
						}
					}else if(condicionMini.contains(" !contains ")) {
						String[] condicionMiniSep = condicionMini.split(" !contains ");
						String variableAntes = getValorVariableAntes(condicionMiniSep,variablesGuardadas);
						String variableDespues = getValorVariableDespues(condicionMiniSep,variablesGuardadas,null);
						if(!variableAntes.toLowerCase().contains(variableDespues.toLowerCase())) {
							lineaAprobada = true;
							if(executedActions != null) {
								return executedActions;
							}
						}else if(executedActions != null) {
							lineaAprobada = true;
						}
					}
					else if(condicionMini.contains(" >= ")) {
						String[] condicionMiniSep = condicionMini.split(" >= ");
						String variableAntes = getValorVariableAntes(condicionMiniSep,variablesGuardadas);
						String variableDespues = getValorVariableDespues(condicionMiniSep,variablesGuardadas,formulasAPI);
						double variableAntesFix = Double.valueOf(variableAntes);
						double variableDespuesFix = Double.valueOf(variableDespues);
						if(variableAntesFix >= variableDespuesFix) {
							lineaAprobada = true;
							if(executedActions != null) {
								return executedActions;
							}
						}else if(executedActions != null) {
							lineaAprobada = true;
						}
					}else if(condicionMini.contains(" <= ")) {
						String[] condicionMiniSep = condicionMini.split(" <= ");
						String variableAntes = getValorVariableAntes(condicionMiniSep,variablesGuardadas);
						String variableDespues = getValorVariableDespues(condicionMiniSep,variablesGuardadas,formulasAPI);
						double variableAntesFix = Double.valueOf(variableAntes);
						double variableDespuesFix = Double.valueOf(variableDespues);
						if(variableAntesFix <= variableDespuesFix) {
							lineaAprobada = true;
							if(executedActions != null) {
								return executedActions;
							}
						}else if(executedActions != null) {
							lineaAprobada = true;
						}
					}else if(condicionMini.contains(" == ")) {
						String[] condicionMiniSep = condicionMini.split(" == ");
						String variableAntes = getValorVariableAntes(condicionMiniSep,variablesGuardadas);
						String variableDespues = getValorVariableDespues(condicionMiniSep,variablesGuardadas,formulasAPI);
						double variableAntesFix = Double.valueOf(variableAntes);
						double variableDespuesFix = Double.valueOf(variableDespues);
						if(variableAntesFix == variableDespuesFix) {
							lineaAprobada = true;
							if(executedActions != null) {
								return executedActions;
							}
						}else if(executedActions != null) {
							lineaAprobada = true;
						}
					}else if(condicionMini.contains(" != ")) {
						String[] condicionMiniSep = condicionMini.split(" != ");
						String variableAntes = getValorVariableAntes(condicionMiniSep,variablesGuardadas);
						String variableDespues = getValorVariableDespues(condicionMiniSep,variablesGuardadas,formulasAPI);
						double variableAntesFix = Double.valueOf(variableAntes);
						double variableDespuesFix = Double.valueOf(variableDespues);
						if(variableAntesFix != variableDespuesFix) {
							lineaAprobada = true;
							if(executedActions != null) {
								return executedActions;
							}
						}else if(executedActions != null) {
							lineaAprobada = true;
						}
					}else if(condicionMini.contains(" > ")) {
						String[] condicionMiniSep = condicionMini.split(" > ");
						String variableAntes = getValorVariableAntes(condicionMiniSep,variablesGuardadas);
						String variableDespues = getValorVariableDespues(condicionMiniSep,variablesGuardadas,formulasAPI);
						double variableAntesFix = Double.valueOf(variableAntes);
						double variableDespuesFix = Double.valueOf(variableDespues);
						if(variableAntesFix > variableDespuesFix) {
							lineaAprobada = true;
							if(executedActions != null) {
								return executedActions;
							}
						}else if(executedActions != null) {
							lineaAprobada = true;
						}
					}else if(condicionMini.contains(" < ")) {
						String[] condicionMiniSep = condicionMini.split(" < ");
						String variableAntes = getValorVariableAntes(condicionMiniSep,variablesGuardadas);
						String variableDespues = getValorVariableDespues(condicionMiniSep,variablesGuardadas,formulasAPI);
						double variableAntesFix = Double.valueOf(variableAntes);
						double variableDespuesFix = Double.valueOf(variableDespues);
						if(variableAntesFix < variableDespuesFix) {
							lineaAprobada = true;
							if(executedActions != null) {
								return executedActions;
							}
						}else if(executedActions != null) {
							lineaAprobada = true;
						}
					}
				}catch(Exception e) {
					
				}
				
				
				
				if(lineaAprobada) {
					break;
				}
			}
			if(!lineaAprobada) {
				return "false";
			}
		}
		return "true";
	}
	
	public void setCooldown(Evento e,Player jugador) {
		long cooldown = e.getCooldown();
		if(cooldown != 0) {
			long millisProx = System.currentTimeMillis()+cooldown*1000;
			e.agregarCooldown(jugador.getName()+";"+millisProx);
		}
	}
	
	public void reiniciarCooldown(Evento e,String jugador) {
		e.reiniciarCooldown(jugador);
	}
	
	public String getCooldown(Evento e,Player jugador) {
		long cooldown = e.getCooldown();
		if(cooldown != 0) {
			FileConfiguration config = plugin.getConfig();
			List<String> cooldowns = e.getCooldowns();
			for(String c : cooldowns) {
				String[] sep = c.split(";");
				long millisProx = Long.valueOf(sep[1]);
				if(sep[0].equals(jugador.getName())) {
					long millisActuales = System.currentTimeMillis();
					if(millisActuales >= millisProx) {
						return "listo";
					}else {
						long espera = millisProx-millisActuales;
						long esperatotalseg = espera/1000;
				 	    long esperatotalmin = esperatotalseg/60;
				 	    long esperatotalhour = esperatotalmin/60;
				 	    long esperatotalday = esperatotalhour/24;
						if(esperatotalseg > 59){
				 			   esperatotalseg = esperatotalseg - 60*esperatotalmin;
				 		   }
				 		   String time = esperatotalseg+config.getString("Messages.seconds");		    		
				 		   if(esperatotalmin > 59){
				 			   esperatotalmin = esperatotalmin - 60*esperatotalhour;
				 		   }	
				 		   if(esperatotalmin > 0){
				 			   time = esperatotalmin+config.getString("Messages.minutes")+" "+time;
				 		   }
				 		   if(esperatotalhour > 24) {
				 			  esperatotalhour = esperatotalhour - 24*esperatotalday;
				 		   }
				 		   if(esperatotalhour > 0){
				 			   time = esperatotalhour+ config.getString("Messages.hours")+" " + time;
				 		   }
				 		   if(esperatotalday > 0) {
				 			  time = esperatotalday+ config.getString("Messages.days")+" " + time;
				 		   }
				 		   
				 	    return time;
					}
				}
			}
		}
		return "listo";
	}
	
	public String getValorVariableAntes(String[] condicionMiniSep,ArrayList<Variable> variablesGuardadas) {
		String variableAntes = condicionMiniSep[0];
		for(Variable v : variablesGuardadas) {
			if(variableAntes.equals(v.getNombre())) {
				variableAntes = v.getValor();
				break;
			}
		}
		return variableAntes;
	}
	
	public String getValorVariableDespues(String[] condicionMiniSep,ArrayList<Variable> variablesGuardadas,FormulasAPI formulasAPI) {
		String variableDespues = condicionMiniSep[1];
		if(variableDespues.contains("%")) {
			for(Variable v : variablesGuardadas) {
				variableDespues = variableDespues.replace(v.getNombre(), v.getValor());
			}
			if(formulasAPI != null) {
				variableDespues = formulasAPI.calcular(variableDespues);
			}
		}
		return variableDespues;
	}
}

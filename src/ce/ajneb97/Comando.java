package ce.ajneb97;




import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.yaml.snakeyaml.parser.ParserException;
import org.yaml.snakeyaml.scanner.ScannerException;

import ce.ajneb97.eventos.Evento;
import ce.ajneb97.eventos.TipoEvento;
import ce.ajneb97.utils.MensajeUtils;




public class Comando implements CommandExecutor {
	
	public ConditionalEvents plugin;
	public Comando(ConditionalEvents plugin) {
		this.plugin = plugin;
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
	   FileConfiguration config = plugin.getConfig();
	   String prefix = ChatColor.translateAlternateColorCodes('&', config.getString("Messages.prefix"));
	   if (!(sender instanceof Player)){
		   if(args.length >= 1) {
			   if(args[0].equalsIgnoreCase("reload")) {
				   if(plugin.recargarConfig(true)) {
					   plugin.recargarEvents();
					   plugin.comprobarEventos();
					   sender.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', config.getString("Messages.commandReload")));
				   }else {
					   sender.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', "&cError reloading Config, check console for errors."));
				   }
			   }else if(args[0].equalsIgnoreCase("reset")) {
				   if(args.length >= 3) {
					   // /ce reset <player> <event>
					   String player = args[1];
					   String event = args[2];
					   Evento e = plugin.getEvento(event);
					   if(e != null) {
						   e.reiniciarCooldown(player);
						   e.reiniciarOneTime(player);
						   sender.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', config.getString("Messages.cooldownReset")
								   .replace("%player%", player).replace("%event%", event)));
					   }else {
						   sender.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', config.getString("Messages.eventDoesNotExists"))); 
					   }
				   }else {
					   sender.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', config.getString("Messages.commandResetError")));
				   }
			   }else if(args[0].equalsIgnoreCase("enable")) {
				   enable(args,sender,config,prefix);
			   }else if(args[0].equalsIgnoreCase("disable")) {
				   disable(args,sender,config,prefix);
			   }
			   else if(args[0].equalsIgnoreCase("help")) {
				   enviarHelp(sender);
			   }else {
				   enviarHelp(sender);
			   }
		   }else {
			   enviarHelp(sender);
		   }
		   return false;   	
	   }
	   Player jugador = (Player)sender;
	   
	   
	   if(jugador.isOp() || jugador.hasPermission("conditionalevents.admin")) {
		   if(args.length >= 1) {
			   if(args[0].equalsIgnoreCase("reload")) {
				   if(plugin.recargarConfig(true)) {
					   plugin.recargarEvents();
					   plugin.comprobarEventos();
					   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', config.getString("Messages.commandReload")));
				   }else {
					   sender.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', "&cError reloading Config, check console for errors."));
				   }
				   
				   
			   }else if(args[0].equalsIgnoreCase("help")) {
				   enviarHelp(jugador);
			   }else if(args[0].equalsIgnoreCase("verify")) {
				   plugin.getVerificador().enviarVerificacion(jugador);
			   }
			   else if(args[0].equalsIgnoreCase("reset")) {
				   if(args.length >= 3) {
					   // /ce reset <player> <event>
					   String player = args[1];
					   String event = args[2];
					   Evento e = plugin.getEvento(event);
					   if(e != null) {
						   e.reiniciarCooldown(player);
						   e.reiniciarOneTime(player);
						   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', config.getString("Messages.cooldownReset")
								   .replace("%player%", player).replace("%event%", event)));
					   }else {
						   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', config.getString("Messages.eventDoesNotExists"))); 
					   }
				   }else {
					   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', config.getString("Messages.commandResetError")));
				   }
			   }else if(args[0].equalsIgnoreCase("enable")) {
				   enable(args,sender,config,prefix);
			   }else if(args[0].equalsIgnoreCase("disable")) {
				   disable(args,sender,config,prefix);
			   }
			   else {
				   enviarHelp(jugador);
			   }
		   }else {
			   enviarHelp(jugador);
		   }
	   }else {
		   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', config.getString("Messages.commandNoPermissions")));
	   }
	   
	   return true;
	   
	}
	
	public void enable(String[] args,CommandSender sender,FileConfiguration messages,String prefix) {
		// /ce enable <event>
		if(args.length <= 1) {
			sender.sendMessage(prefix+MensajeUtils.getMensajeColor(messages.getString("Messages.eventEnableError")));
			return;
		}
		
		Evento evento = plugin.getEvento(args[1]);
		if(evento == null) {
			sender.sendMessage(prefix+MensajeUtils.getMensajeColor(messages.getString("Messages.eventDoesNotExists")));
			return;
		}
		
		evento.setActivado(true);
		if(evento.getrManager() != null) {
			for(TipoEvento tipo : evento.getTipos()) {
				if(tipo.equals(TipoEvento.REPETITIVE_SERVER)) {
					evento.getrManager().iniciarServer();
					break;
				}else if(tipo.equals(TipoEvento.REPETITIVE)){
					evento.getrManager().iniciarPlayers();
					break;
				}
			}
		}
		plugin.getConfig().set("Events."+evento.getNombre()+".enabled", true);
		plugin.saveConfig();
		
		sender.sendMessage(prefix+MensajeUtils.getMensajeColor(messages.getString("Messages.eventEnabled").replace("%event%", args[1])));
	}
	
	public void disable(String[] args,CommandSender sender,FileConfiguration messages,String prefix) {
		// /ce disable <event>
		if(args.length <= 1) {
			sender.sendMessage(prefix+MensajeUtils.getMensajeColor(messages.getString("Messages.eventDisableError")));
			return;
		}
		
		Evento evento = plugin.getEvento(args[1]);
		if(evento == null) {
			sender.sendMessage(prefix+MensajeUtils.getMensajeColor(messages.getString("Messages.eventDoesNotExists")));
			return;
		}
		
		evento.setActivado(false);
		if(evento.getrManager() != null) {
			evento.getrManager().terminar();
		}
		plugin.getConfig().set("Events."+evento.getNombre()+".enabled", false);
		plugin.saveConfig();
		
		sender.sendMessage(prefix+MensajeUtils.getMensajeColor(messages.getString("Messages.eventDisabled").replace("%event%", args[1])));
	}
	
	public static void enviarHelp(CommandSender jugador) {
		jugador.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7[ [ &8[&bConditionalEvents&8] &7] ]"));
		jugador.sendMessage(ChatColor.translateAlternateColorCodes('&',""));
		jugador.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/ce help &8Shows this message."));
		jugador.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/ce reload &8Reloads the config."));
		jugador.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/ce verify &8Checks ALL events for errors."));
		jugador.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/ce reset <player> <event> &8Resets an event data for a player."));
		jugador.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/ce enable/disable <event> &8Enable or disables an event."));
		jugador.sendMessage(ChatColor.translateAlternateColorCodes('&',""));
		jugador.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7[ [ &8[&bConditionalEvents&8] &7] ]"));
	}
}

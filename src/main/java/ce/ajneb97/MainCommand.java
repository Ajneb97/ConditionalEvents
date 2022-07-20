package ce.ajneb97;

import ce.ajneb97.configs.MainConfigManager;
import ce.ajneb97.managers.MessagesManager;
import ce.ajneb97.model.CEEvent;
import ce.ajneb97.model.player.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainCommand implements CommandExecutor, TabCompleter {
	
	private ConditionalEvents plugin;
	private MainConfigManager mainConfigManager;
	public MainCommand(ConditionalEvents plugin) {
		this.plugin = plugin;
		this.mainConfigManager = plugin.getConfigsManager().getMainConfigManager();
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		FileConfiguration config = mainConfigManager.getConfig();
	   	String prefix = MessagesManager.getColoredMessage(config.getString("Messages.prefix"));

	   	if(sender.isOp() || sender.hasPermission("conditionalevents.admin")) {
		   if(args.length >= 1) {
				String argument = args[0].toLowerCase(Locale.ROOT);
				switch(argument) {
					case "reload": 
						plugin.getConfigsManager().reload();
						sender.sendMessage(prefix+MessagesManager.getColoredMessage(config.getString("Messages.commandReload")));
						break;
					case "help": help(sender); break;
					case "reset": reset(args,sender,config,prefix); break;
					case "enable": enable(args,sender,config,prefix); break;
					case "disable": disable(args,sender,config,prefix); break;
					case "debug": debug(args,sender,config,prefix); break;
					case "verify":
						if(sender instanceof Player){
							plugin.getVerifyManager().sendVerification((Player)sender);
						}else{
							sender.sendMessage(prefix+MessagesManager.getColoredMessage(config.getString("Messages.onlyPlayerCommand")));
						}
						break;
					default: help(sender); break;
				}
		   }else {
			   help(sender);
		   }
	   	}else {
		   sender.sendMessage(prefix+MessagesManager.getColoredMessage(config.getString("Messages.commandNoPermissions")));
	   	}
	   
	   	return true;
	}

	public void reset(String[] args,CommandSender sender,FileConfiguration config,String prefix){
		if(args.length >= 3) {
			// /ce reset <player> <event>
			String player = args[1];
			String eventName = args[2];
			CEEvent e = plugin.getEventsManager().getEvent(eventName);
			if(e == null){
				sender.sendMessage(prefix+MessagesManager.getColoredMessage(config.getString("Messages.eventDoesNotExists")));
				return;
			}

			PlayerData playerData = plugin.getPlayerManager().getPlayerDataByName(player);
			if(playerData == null){
				sender.sendMessage(prefix+MessagesManager.getColoredMessage(config.getString("Messages.playerDoesNotExists")));
				return;
			}

			playerData.resetCooldown(eventName);
			playerData.setOneTime(eventName,false);
			sender.sendMessage(prefix+MessagesManager.getColoredMessage(config.getString("Messages.cooldownReset")
					.replace("%player%", player).replace("%event%", eventName)));
		}else {
			sender.sendMessage(prefix+MessagesManager.getColoredMessage(config.getString("Messages.commandResetError")));
		}
	}
	
	public void enable(String[] args,CommandSender sender,FileConfiguration config,String prefix) {
		// /ce enable <event>
		if(args.length <= 1) {
			sender.sendMessage(prefix+MessagesManager.getColoredMessage(config.getString("Messages.eventEnableError")));
			return;
		}

		String eventName = args[1];
		CEEvent e = plugin.getEventsManager().getEvent(eventName);
		if(e == null) {
			sender.sendMessage(prefix+MessagesManager.getColoredMessage(config.getString("Messages.eventDoesNotExists")));
			return;
		}
		
		e.enable();
		config.set("Events."+eventName+".enabled", true);
		mainConfigManager.saveConfig();
		
		sender.sendMessage(prefix+MessagesManager.getColoredMessage(config.getString("Messages.eventEnabled").replace("%event%", eventName)));
	}
	
	public void disable(String[] args,CommandSender sender,FileConfiguration config,String prefix) {
		// /ce disable <event>
		if(args.length <= 1) {
			sender.sendMessage(prefix+MessagesManager.getColoredMessage(config.getString("Messages.eventDisableError")));
			return;
		}

		String eventName = args[1];
		CEEvent e = plugin.getEventsManager().getEvent(eventName);
		if(e == null) {
			sender.sendMessage(prefix+MessagesManager.getColoredMessage(config.getString("Messages.eventDoesNotExists")));
			return;
		}

		e.disable();
		config.set("Events."+eventName+".enabled", false);
		mainConfigManager.saveConfig();

		sender.sendMessage(prefix+MessagesManager.getColoredMessage(config.getString("Messages.eventDisabled").replace("%event%", eventName)));
	}

	public void debug(String[] args,CommandSender sender,FileConfiguration config,String prefix) {
		// /ce debug <event>
		if(args.length <= 1) {
			sender.sendMessage(prefix+MessagesManager.getColoredMessage(config.getString("Messages.commandDebugError")));
			return;
		}

		String eventName = args[1];
		CEEvent e = plugin.getEventsManager().getEvent(eventName);
		if(e == null) {
			sender.sendMessage(prefix+MessagesManager.getColoredMessage(config.getString("Messages.eventDoesNotExists")));
			return;
		}

		boolean result = plugin.getDebugManager().setDebugSender(sender,eventName);
		if(result){
			sender.sendMessage(prefix+MessagesManager.getColoredMessage(config.getString("Messages.debugEnabled").replace("%event%", eventName)));
		}else{
			sender.sendMessage(prefix+MessagesManager.getColoredMessage(config.getString("Messages.debugDisabled").replace("%event%", eventName)));
		}
	}
	
	public static void help(CommandSender sender) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7[ [ &8[&bConditionalEvents&8] &7] ]"));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',""));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/ce help &8Shows this message."));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/ce reload &8Reloads the config."));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/ce verify &8Checks ALL events for errors."));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/ce debug <event> &8Enables/disables debug information for an event."));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/ce reset <player> <event> &8Resets an event data for a player."));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/ce enable/disable <event> &8Enable or disables an event."));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',""));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7[ [ &8[&bConditionalEvents&8] &7] ]"));
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if(sender.isOp() || sender.hasPermission("conditionalevents.admin")) {
			if(args.length == 1) {
				List<String> completions = new ArrayList<>();
				List<String> commands = new ArrayList<>();
				commands.add("help");commands.add("reload");commands.add("verify");
				commands.add("reset");commands.add("debug");commands.add("enable");
				commands.add("disable");
				for(String c : commands) {
					if(args[0].isEmpty() || c.startsWith(args[0].toLowerCase(Locale.ROOT))) {
						completions.add(c);
					}
				}
				return completions;
			}else {
				if((args[0].equalsIgnoreCase("debug") || args[0].equalsIgnoreCase("enable")
						|| args[0].equalsIgnoreCase("disable")) && args.length == 2) {
					return getEventsCompletions(args,1);
				}else if(args[0].equalsIgnoreCase("reset") && args.length == 3) {
					return getEventsCompletions(args,2);
				}
			}
		}

		return null;
	}

	public List<String> getEventsCompletions(String[] args,int argEventPos){
		List<String> completions = new ArrayList<>();

		String argEvent = args[argEventPos];
		ArrayList<CEEvent> events = plugin.getEventsManager().getEvents();
		for(CEEvent event : events) {
			if(argEvent.isEmpty() || event.getName().toLowerCase().startsWith(argEvent.toLowerCase())) {
				completions.add(event.getName());
			}
		}

		if(completions.isEmpty()){
			return null;
		}
		return completions;
	}
}

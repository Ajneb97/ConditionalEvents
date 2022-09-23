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


public class MainCommand implements CommandExecutor, TabCompleter {
	
	private ConditionalEvents plugin;
	private MainConfigManager mainConfigManager;
	public MainCommand(ConditionalEvents plugin) {
		this.plugin = plugin;
		this.mainConfigManager = plugin.getConfigsManager().getMainConfigManager();
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		FileConfiguration config = mainConfigManager.getConfig();
	   	String prefix = plugin.getMessagesManager().getPrefix();

	   	if(sender.isOp() || sender.hasPermission("conditionalevents.admin")) {
		   if(args.length >= 1) {
			   if(args[0].equalsIgnoreCase("reload")) {
				   if(!plugin.getConfigsManager().reload()){
					   sender.sendMessage(ConditionalEvents.prefix+MessagesManager.getColoredMessage(" &cThere was an error reloading the config, check the console."));
					   return true;
				   }
				   sender.sendMessage(MessagesManager.getColoredMessage(prefix+config.getString("Messages.commandReload")));
			   }else if(args[0].equalsIgnoreCase("help")) {
				   help(sender);
			   }else if(args[0].equalsIgnoreCase("reset")) {
				   reset(args,sender,config,prefix);
			   }else if(args[0].equalsIgnoreCase("enable")) {
				   enable(args,sender,config,prefix);
			   }else if(args[0].equalsIgnoreCase("disable")) {
				   disable(args,sender,config,prefix);
			   }else if(args[0].equalsIgnoreCase("debug")) {
				   debug(args,sender,config,prefix);
			   }else if(args[0].equalsIgnoreCase("verify")) {
				   if(sender instanceof Player){
					   plugin.getVerifyManager().sendVerification((Player)sender);
				   }else{
					   sender.sendMessage(prefix+MessagesManager.getColoredMessage(config.getString("Messages.onlyPlayerCommand")));
				   }
			   }
			   else {
				   help(sender);
			   }
		   }else {
			   help(sender);
		   }
	   	}else {
		   sender.sendMessage(MessagesManager.getColoredMessage(prefix+config.getString("Messages.commandNoPermissions")));
	   	}
	   
	   	return true;
	}

	public void reset(String[] args,CommandSender sender,FileConfiguration config,String prefix){
		// /ce reset <player> <event>
		if(args.length <= 2){
			sender.sendMessage(MessagesManager.getColoredMessage(prefix+config.getString("Messages.commandResetError")));
			return;
		}

		String player = args[1];
		String eventName = args[2];

		PlayerData playerData = plugin.getPlayerManager().getPlayerDataByName(player);
		if(playerData == null){
			sender.sendMessage(MessagesManager.getColoredMessage(prefix+config.getString("Messages.playerDoesNotExists")));
			return;
		}

		if(eventName.equals("all")){
			playerData.resetAll();
			sender.sendMessage(MessagesManager.getColoredMessage(prefix+config.getString("Messages.eventDataResetAll")
					.replace("%player%", player)));
		}else{
			CEEvent e = plugin.getEventsManager().getEvent(eventName);
			if(e == null){
				sender.sendMessage(MessagesManager.getColoredMessage(prefix+config.getString("Messages.eventDoesNotExists")));
				return;
			}
			playerData.resetCooldown(eventName);
			playerData.setOneTime(eventName,false);
			sender.sendMessage(MessagesManager.getColoredMessage(prefix+config.getString("Messages.eventDataReset")
					.replace("%player%", player).replace("%event%", eventName)));
		}
	}
	
	public void enable(String[] args,CommandSender sender,FileConfiguration config,String prefix) {
		// /ce enable <event>
		if(args.length <= 1) {
			sender.sendMessage(MessagesManager.getColoredMessage(prefix+config.getString("Messages.eventEnableError")));
			return;
		}

		String eventName = args[1];
		CEEvent e = plugin.getEventsManager().getEvent(eventName);
		if(e == null) {
			sender.sendMessage(MessagesManager.getColoredMessage(prefix+config.getString("Messages.eventDoesNotExists")));
			return;
		}
		
		e.enable();
		plugin.getConfigsManager().saveEvent(e);
		
		sender.sendMessage(MessagesManager.getColoredMessage(prefix+config.getString("Messages.eventEnabled").replace("%event%", eventName)));
	}
	
	public void disable(String[] args,CommandSender sender,FileConfiguration config,String prefix) {
		// /ce disable <event>
		if(args.length <= 1) {
			sender.sendMessage(MessagesManager.getColoredMessage(prefix+config.getString("Messages.eventDisableError")));
			return;
		}

		String eventName = args[1];
		CEEvent e = plugin.getEventsManager().getEvent(eventName);
		if(e == null) {
			sender.sendMessage(MessagesManager.getColoredMessage(prefix+config.getString("Messages.eventDoesNotExists")));
			return;
		}

		e.disable();
		plugin.getConfigsManager().saveEvent(e);

		sender.sendMessage(MessagesManager.getColoredMessage(prefix+config.getString("Messages.eventDisabled").replace("%event%", eventName)));
	}

	public void debug(String[] args,CommandSender sender,FileConfiguration config,String prefix) {
		// /ce debug <event>
		if(args.length <= 1) {
			sender.sendMessage(MessagesManager.getColoredMessage(prefix+config.getString("Messages.commandDebugError")));
			return;
		}

		String eventName = args[1];
		CEEvent e = plugin.getEventsManager().getEvent(eventName);
		if(e == null) {
			sender.sendMessage(MessagesManager.getColoredMessage(prefix+config.getString("Messages.eventDoesNotExists")));
			return;
		}

		boolean result = plugin.getDebugManager().setDebugSender(sender,eventName);
		if(result){
			sender.sendMessage(MessagesManager.getColoredMessage(prefix+config.getString("Messages.debugEnabled").replace("%event%", eventName)));
		}else{
			sender.sendMessage(MessagesManager.getColoredMessage(prefix+config.getString("Messages.debugDisabled").replace("%event%", eventName)));
		}
	}
	
	public static void help(CommandSender sender) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7[ [ &8[&bConditionalEvents&8] &7] ]"));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',""));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/ce help &8Shows this message."));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/ce reload &8Reloads the config."));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/ce verify &8Checks ALL events for errors."));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/ce debug <event> &8Enables/disables debug information for an event."));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/ce reset <player> <event>/all &8Resets an event data for a player."));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/ce enable/disable <event> &8Enable or disables an event."));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',""));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7[ [ &8[&bConditionalEvents&8] &7] ]"));
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if(sender.isOp() || sender.hasPermission("conditionalevents.admin")) {
			if(args.length == 1) {
				List<String> completions = new ArrayList<String>();
				List<String> commands = new ArrayList<String>();
				commands.add("help");commands.add("reload");commands.add("verify");
				commands.add("reset");commands.add("debug");commands.add("enable");
				commands.add("disable");
				for(String c : commands) {
					if(args[0].isEmpty() || c.startsWith(args[0].toLowerCase())) {
						completions.add(c);
					}
				}
				return completions;
			}else {
				if((args[0].equalsIgnoreCase("debug") || args[0].equalsIgnoreCase("enable")
						|| args[0].equalsIgnoreCase("disable")) && args.length == 2) {
					List<String> completions = getEventsCompletions(args,1);
					return completions;
				}else if(args[0].equalsIgnoreCase("reset") && args.length == 3) {
					List<String> completions = getEventsCompletions(args,2);
					completions.add("all");
					return completions;
				}
			}
		}


		return null;
	}

	public List<String> getEventsCompletions(String[] args,int argEventPos){
		List<String> completions = new ArrayList<String>();

		String argEvent = args[argEventPos];
		ArrayList<CEEvent> events = plugin.getEventsManager().getEvents();
		for(CEEvent event : events) {
			if(argEvent.toLowerCase().isEmpty() || event.getName().toLowerCase().startsWith(argEvent.toLowerCase())) {
				completions.add(event.getName());
			}
		}

		if(completions.isEmpty()){
			return null;
		}
		return completions;
	}
}

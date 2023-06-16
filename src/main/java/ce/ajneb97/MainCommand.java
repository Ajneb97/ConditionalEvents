package ce.ajneb97;




import ce.ajneb97.configs.MainConfigManager;
import ce.ajneb97.managers.MessagesManager;
import ce.ajneb97.model.CEEvent;
import ce.ajneb97.model.EventType;
import ce.ajneb97.model.player.PlayerData;
import ce.ajneb97.utils.ActionUtils;
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
		MessagesManager msgManager = plugin.getMessagesManager();

	   	if(sender.isOp() || sender.hasPermission("conditionalevents.admin")) {
		   if(args.length >= 1) {
			   if(args[0].equalsIgnoreCase("reload")) {
				   if(!plugin.getConfigsManager().reload()){
					   sender.sendMessage(ConditionalEvents.prefix+MessagesManager.getColoredMessage(" &cThere was an error reloading the config, check the console."));
					   return true;
				   }
				   msgManager.sendMessage(sender,config.getString("Messages.commandReload"),true);
			   }else if(args[0].equalsIgnoreCase("help")) {
				   help(sender);
			   }else if(args[0].equalsIgnoreCase("reset")) {
				   reset(args,sender,config,msgManager);
			   }else if(args[0].equalsIgnoreCase("enable")) {
				   enable(args,sender,config,msgManager);
			   }else if(args[0].equalsIgnoreCase("disable")) {
				   disable(args,sender,config,msgManager);
			   }else if(args[0].equalsIgnoreCase("debug")) {
				   debug(args,sender,config,msgManager);
			   }else if(args[0].equalsIgnoreCase("verify")) {
				   if(sender instanceof Player){
					   plugin.getVerifyManager().sendVerification((Player)sender);
				   }else{
					   msgManager.sendMessage(sender,config.getString("Messages.onlyPlayerCommand"),true);
				   }
			   }else if(args[0].equalsIgnoreCase("call")) {
				   call(args,sender,config,msgManager);
			   }
			   else {
				   help(sender);
			   }
		   }else {
			   help(sender);
		   }
	   	}else {
			msgManager.sendMessage(sender,config.getString("Messages.commandNoPermissions"),true);
	   	}
	   
	   	return true;
	}

	public void reset(String[] args,CommandSender sender,FileConfiguration config,MessagesManager msgManager){
		// /ce reset <player> <event>
		if(args.length <= 2){
			msgManager.sendMessage(sender,config.getString("Messages.commandResetError"),true);
			return;
		}

		String player = args[1];
		String eventName = args[2];

		PlayerData playerData = plugin.getPlayerManager().getPlayerDataByName(player);
		if(playerData == null){
			msgManager.sendMessage(sender,config.getString("Messages.playerDoesNotExists"),true);
			return;
		}

		if(eventName.equals("all")){
			playerData.resetAll();
			msgManager.sendMessage(sender,config.getString("Messages.eventDataResetAll")
					.replace("%player%", player),true);
		}else{
			CEEvent e = plugin.getEventsManager().getEvent(eventName);
			if(e == null){
				msgManager.sendMessage(sender,config.getString("Messages.eventDoesNotExists"),true);
				return;
			}
			playerData.resetCooldown(eventName);
			playerData.setOneTime(eventName,false);
			msgManager.sendMessage(sender,config.getString("Messages.eventDataReset")
					.replace("%player%", player).replace("%event%", eventName),true);
		}
	}
	
	public void enable(String[] args,CommandSender sender,FileConfiguration config,MessagesManager msgManager) {
		// /ce enable <event>
		if(args.length <= 1) {
			msgManager.sendMessage(sender,config.getString("Messages.eventEnableError"),true);
			return;
		}

		String eventName = args[1];
		CEEvent e = plugin.getEventsManager().getEvent(eventName);
		if(e == null) {
			msgManager.sendMessage(sender,config.getString("Messages.eventDoesNotExists"),true);
			return;
		}
		
		e.enable();
		plugin.getConfigsManager().saveEvent(e);

		msgManager.sendMessage(sender,config.getString("Messages.eventEnabled")
				.replace("%event%", eventName),true);
	}
	
	public void disable(String[] args,CommandSender sender,FileConfiguration config,MessagesManager msgManager) {
		// /ce disable <event>
		if(args.length <= 1) {
			msgManager.sendMessage(sender,config.getString("Messages.eventDisableError"),true);
			return;
		}

		String eventName = args[1];
		CEEvent e = plugin.getEventsManager().getEvent(eventName);
		if(e == null) {
			msgManager.sendMessage(sender,config.getString("Messages.eventDoesNotExists"),true);
			return;
		}

		e.disable();
		plugin.getConfigsManager().saveEvent(e);

		msgManager.sendMessage(sender,config.getString("Messages.eventDisabled").replace("%event%", eventName),true);
	}

	public void debug(String[] args,CommandSender sender,FileConfiguration config,MessagesManager msgManager) {
		// /ce debug <event>
		if(args.length <= 1) {
			msgManager.sendMessage(sender,config.getString("Messages.commandDebugError"),true);
			return;
		}

		String eventName = args[1];
		CEEvent e = plugin.getEventsManager().getEvent(eventName);
		if(e == null) {
			msgManager.sendMessage(sender,config.getString("Messages.eventDoesNotExists"),true);
			return;
		}

		boolean result = plugin.getDebugManager().setDebugSender(sender,eventName);
		if(result){
			msgManager.sendMessage(sender,config.getString("Messages.debugEnabled").replace("%event%", eventName),true);
		}else{
			msgManager.sendMessage(sender,config.getString("Messages.debugDisabled").replace("%event%", eventName),true);
		}
	}

	public void call(String[] args,CommandSender sender,FileConfiguration config,MessagesManager msgManager) {
		// /ce call <event> %variable1%=<value1>;%variable2%=<value2>
		if(args.length <= 1) {
			msgManager.sendMessage(sender,config.getString("Messages.commandCallError"),true);
			return;
		}

		String eventName = args[1];
		CEEvent e = plugin.getEventsManager().getEvent(eventName);
		if(e == null) {
			msgManager.sendMessage(sender,config.getString("Messages.eventDoesNotExists"),true);
			return;
		}
		if(!e.getEventType().equals(EventType.CALL)){
			msgManager.sendMessage(sender,config.getString("Messages.commandCallInvalidEvent"),true);
			return;
		}

		String actionLine = eventName;
		if(args.length >= 3){
			actionLine += ";"+args[2];
		}

		Player player = null;
		if(sender instanceof Player){
			player = (Player) sender;
		}

		if(ActionUtils.callEvent(actionLine,player,plugin)){
			msgManager.sendMessage(sender,config.getString("Messages.commandCallCorrect")
					.replace("%event%",eventName),true);
		}else{
			msgManager.sendMessage(sender,config.getString("Messages.commandCallFailed")
					.replace("%event%",eventName),true);
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
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/ce call <event> (optional)%variable1%=<value1>;%variableN%=<valueN> &8Executes a 'call' event."));
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
				commands.add("disable");commands.add("call");
				for(String c : commands) {
					if(args[0].isEmpty() || c.startsWith(args[0].toLowerCase())) {
						completions.add(c);
					}
				}
				return completions;
			}else {
				if((args[0].equalsIgnoreCase("debug") || args[0].equalsIgnoreCase("enable")
						|| args[0].equalsIgnoreCase("disable") || args[0].equalsIgnoreCase("call")) && args.length == 2) {
					List<String> completions = getEventsCompletions(args,1,false);
					return completions;
				}else if(args[0].equalsIgnoreCase("reset") && args.length == 3) {
					List<String> completions = getEventsCompletions(args,2,true);
					return completions;
				}
			}
		}


		return null;
	}

	public List<String> getEventsCompletions(String[] args,int argEventPos,boolean addAll){
		List<String> completions = new ArrayList<String>();

		String argEvent = args[argEventPos];

		if(addAll) {
			if(argEvent.isEmpty() || "all".startsWith(argEvent.toLowerCase())) {
				completions.add("all");
			}
		}

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

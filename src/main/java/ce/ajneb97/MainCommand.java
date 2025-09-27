package ce.ajneb97;




import ce.ajneb97.configs.MainConfigManager;
import ce.ajneb97.managers.MessagesManager;
import ce.ajneb97.managers.PlayerManager;
import ce.ajneb97.managers.SavedItemsManager;
import ce.ajneb97.model.CEEvent;
import ce.ajneb97.model.EventType;
import ce.ajneb97.model.player.PlayerData;
import ce.ajneb97.utils.ActionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;


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
			   }else if(args[0].equalsIgnoreCase("item")) {
				   if(sender instanceof Player){
					   item(args,(Player)sender,config,msgManager);
				   }else{
					   msgManager.sendMessage(sender,config.getString("Messages.onlyPlayerCommand"),true);
				   }
			   }else if(args[0].equalsIgnoreCase("interrupt")) {
				   interrupt(args,sender,config,msgManager);
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

		PlayerManager playerManager = plugin.getPlayerManager();
		PlayerData playerData = playerManager.getPlayerDataByName(player);
		if(!player.equals("*") && playerData == null){
			msgManager.sendMessage(sender,config.getString("Messages.playerDoesNotExists"),true);
			return;
		}

		boolean silent = args.length >= 4 && args[3].equals("silent:true");

		if(eventName.equals("all")){
			if(player.equals("*")){
				//ALL player data reset
				playerManager.resetAllDataForPlayers();
				if(silent){
					return;
				}
				msgManager.sendMessage(sender,config.getString("Messages.eventDataResetAllForAllPlayers"),true);
			}else{
				//Reset ALL event data for a player
				playerData.resetAll();
				if(silent){
					return;
				}
				msgManager.sendMessage(sender,config.getString("Messages.eventDataResetAll")
						.replace("%player%", player),true);
			}
		}else{
			CEEvent e = plugin.getEventsManager().getEvent(eventName);
			if(e == null){
				msgManager.sendMessage(sender,config.getString("Messages.eventDoesNotExists"),true);
				return;
			}
			if(player.equals("*")){
				//Reset an event for ALL players
				playerManager.resetEventDataForPlayers(eventName);
				if(silent){
					return;
				}
				msgManager.sendMessage(sender,config.getString("Messages.eventDataResetForAllPlayers")
						.replace("%event%", eventName),true);
			}else{
				//Reset an event for a player
				playerData.resetCooldown(eventName);
				playerData.setOneTime(eventName,false);
				if(silent){
					return;
				}
				msgManager.sendMessage(sender,config.getString("Messages.eventDataReset")
						.replace("%player%", player).replace("%event%", eventName),true);
			}
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

		boolean silent = args.length >= 3 && args[2].equals("silent:true");
		
		e.enable();
		plugin.getConfigsManager().saveEvent(e);

		if(silent){
			return;
		}
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

		boolean silent = args.length >= 3 && args[2].equals("silent:true");

		e.disable();
		plugin.getInterruptEventManager().interruptEvent(eventName,null);

		plugin.getConfigsManager().saveEvent(e);

		if(silent){
			return;
		}
		msgManager.sendMessage(sender,config.getString("Messages.eventDisabled").replace("%event%", eventName),true);
	}

	public void debug(String[] args,CommandSender sender,FileConfiguration config,MessagesManager msgManager) {
		// /ce debug <event> (optional)<player>
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

		String playerName = null;
		if(args.length >= 3){
			playerName = args[2];
		}

		boolean result = plugin.getDebugManager().setDebugSender(sender,eventName,playerName);
		if(result){
			if(playerName != null){
				msgManager.sendMessage(sender,config.getString("Messages.debugEnabledPlayer")
						.replace("%event%", eventName).replace("%player%",playerName),true);
			}else{
				msgManager.sendMessage(sender,config.getString("Messages.debugEnabled").replace("%event%", eventName),true);
			}
		}else{
			if(playerName != null){
				msgManager.sendMessage(sender,config.getString("Messages.debugDisabledPlayer")
						.replace("%event%", eventName).replace("%player%",playerName),true);
			}else{
				msgManager.sendMessage(sender,config.getString("Messages.debugDisabled").replace("%event%", eventName),true);
			}
		}
	}

	public void call(String[] args,CommandSender sender,FileConfiguration config,MessagesManager msgManager) {
		// /ce call <event> (optional)%variable1%=<value1>;%variable2%=<value2> (optional)player:<player> (optional)silent:true
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
		String playerName = null;
		boolean silent = false;

		if(args.length >= 3){
			for(int i=2;i<args.length;i++){
				String arg = args[i];
				if(arg.contains("%")){
					actionLine += ";"+args[i];
				}else if(arg.startsWith("player:")){
					playerName = args[i].replace("player:","");
				}else if(args[i].equals("silent:true")){
					silent = true;
				}
			}
		}

		Player player = null;
		if(playerName != null){
			player = Bukkit.getPlayer(playerName);
			if(player == null){
				msgManager.sendMessage(sender,config.getString("Messages.playerNotOnline"),true);
				return;
			}
		}else if(sender instanceof Player){
			player = (Player) sender;
		}

		if(ActionUtils.callEvent(actionLine,player,plugin,null)){
			if(silent){
				return;
			}
			if(player != null){
				msgManager.sendMessage(sender,config.getString("Messages.commandCallCorrectPlayer")
						.replace("%event%",eventName).replace("%player%",player.getName()),true);
			}else{
				msgManager.sendMessage(sender,config.getString("Messages.commandCallCorrect")
						.replace("%event%",eventName),true);
			}
		}else{
			if(silent){
				return;
			}
			msgManager.sendMessage(sender,config.getString("Messages.commandCallFailed")
					.replace("%event%",eventName),true);
		}

	}

	public void item(String[] args,Player player,FileConfiguration config,MessagesManager msgManager){
		// /ce item <save/remove> <name>
		if(args.length <= 2){
			msgManager.sendMessage(player,config.getString("Messages.commandItemError"),true);
			return;
		}

		SavedItemsManager savedItemsManager = plugin.getSavedItemsManager();
		String type = args[1];
		String name = args[2];

		if(type.equals("save")){
			if(savedItemsManager.getItem(name,player) != null){
				msgManager.sendMessage(player,config.getString("Messages.savedItemAlreadyExists"),true);
				return;
			}

			ItemStack item = player.getItemInHand();
			if(item == null || item.getType().equals(Material.AIR)) {
				msgManager.sendMessage(player, config.getString("Messages.mustHaveItemInHand"), true);
				return;
			}

			savedItemsManager.addItem(name,item.clone());
			msgManager.sendMessage(player,config.getString("Messages.savedItemAdded")
					.replace("%name%",name),true);
		}else if(type.equals("remove")){
			if(savedItemsManager.getItem(name,player) == null){
				msgManager.sendMessage(player,config.getString("Messages.savedItemDoesNotExists"),true);
				return;
			}

			savedItemsManager.removeItem(name);
			msgManager.sendMessage(player,config.getString("Messages.savedItemRemoved")
					.replace("%name%",name),true);
		}else{
			msgManager.sendMessage(player,config.getString("Messages.commandItemError"),true);
		}
	}

	public void interrupt(String[] args,CommandSender sender,FileConfiguration config,MessagesManager msgManager) {
		// /ce interrupt <event> (optional)<player>
		if (args.length <= 1) {
			msgManager.sendMessage(sender, config.getString("Messages.commandInterruptError"), true);
			return;
		}

		String eventName = args[1];
		CEEvent e = plugin.getEventsManager().getEvent(eventName);
		if (e == null) {
			msgManager.sendMessage(sender, config.getString("Messages.eventDoesNotExists"), true);
			return;
		}

		String playerName = null;
		if (args.length >= 3 && !args[2].equals("silent:true")) {
			playerName = args[2];
		}

		boolean silent = args[args.length-1].equals("silent:true");

		plugin.getInterruptEventManager().interruptEvent(eventName,playerName);
		if(silent){
			return;
		}
		if(playerName == null){
			msgManager.sendMessage(sender, config.getString("Messages.commandInterruptCorrect")
					.replace("%event%",eventName), true);
		}else{
			msgManager.sendMessage(sender, config.getString("Messages.commandInterruptCorrectPlayer")
					.replace("%event%",eventName).replace("%player%",playerName), true);
		}
	}
	
	public static void help(CommandSender sender) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7[ [ &8[&bConditionalEvents&8] &7] ]"));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',""));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/ce help &8Shows this message."));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/ce reload &8Reloads the config."));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/ce verify &8Checks ALL events for errors."));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/ce debug <event> (optional)<player> &8Enables/disables debug information for an event."));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/ce reset <player>/* <event>/all &8Resets an event data for a player."));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/ce enable/disable <event> &8Enable or disables an event."));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/ce call <event> (optional)%variable1%=<value1>;%variableN%=<valueN> (optional)player:<player> (optional)silent:true &8Executes a 'call' event."));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/ce item <save/remove> <name> &8Save and remove items for some actions."));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/ce interrupt <event> (optional)<player> &8Stops the execution of actions for an event."));
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
				commands.add("disable");commands.add("call");commands.add("item");
				commands.add("interrupt");
				for(String c : commands) {
					if(args[0].isEmpty() || c.startsWith(args[0].toLowerCase())) {
						completions.add(c);
					}
				}
				return completions;
			}else {
				if((args[0].equalsIgnoreCase("debug") || args[0].equalsIgnoreCase("enable")
						|| args[0].equalsIgnoreCase("disable") || args[0].equalsIgnoreCase("interrupt"))
						&& args.length == 2) {
                    return getEventsCompletions(args,1,false,null);
				}else if(args[0].equalsIgnoreCase("call") && args.length == 2) {
                    return getEventsCompletions(args,1,false,EventType.CALL);
				}else if(args[0].equalsIgnoreCase("reset") && args.length == 3) {
					return getEventsCompletions(args,2,true,null);
				}else if(args[0].equalsIgnoreCase("reset") && args.length == 2) {
					List<String> completions = new ArrayList<>();
					for(Player p : Bukkit.getOnlinePlayers()) {
						if(args[1].isEmpty() || p.getName().toLowerCase().startsWith(args[1].toLowerCase())){
							completions.add(p.getName());
						}
					}
					if(args[1].isEmpty() || "*".startsWith(args[1].toLowerCase())) {
						completions.add("*");
					}
					return completions;
				}else if(args[0].equalsIgnoreCase("item") && args.length == 2) {
					List<String> completions = new ArrayList<>();
					completions.add("remove");completions.add("save");
					return completions;
				}else if(args.length == 3 && args[0].equalsIgnoreCase("item") && args[1].equalsIgnoreCase("remove")) {
                    return getSavedItemsCompletions(args,2);
				}
			}
		}


		return null;
	}

	public List<String> getEventsCompletions(String[] args,int argEventPos,boolean addAll,EventType eventType){
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
				if(eventType != null){
					if(event.getEventType().equals(eventType)){
						completions.add(event.getName());
					}
				}else{
					completions.add(event.getName());
				}
			}
		}

		if(completions.isEmpty()){
			return null;
		}
		return completions;
	}

	public List<String> getSavedItemsCompletions(String[] args,int argItemPos){
		List<String> completions = new ArrayList<>();

		String argItem = args[argItemPos];
		for(Map.Entry<String, ItemStack> entry : plugin.getSavedItemsManager().getSavedItems().entrySet()){
			if(argItem.isEmpty() || entry.getKey().toLowerCase().startsWith(argItem.toLowerCase())) {
				completions.add(entry.getKey());
			}
		}

		if(completions.isEmpty()){
			return null;
		}
		return completions;
	}
}

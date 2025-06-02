package ce.ajneb97.configs;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.model.player.EventData;
import ce.ajneb97.model.player.PlayerData;
import ce.ajneb97.utils.OtherUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;

public class PlayerConfigsManager {

	private ConditionalEvents plugin;
	
	public PlayerConfigsManager(ConditionalEvents plugin) {
		this.plugin = plugin;
	}
	
	public void configure() {
		createPlayersFolder();
		loadPlayerData();
	}
	
	public void createPlayersFolder(){
		File folder;
        try {
            folder = new File(plugin.getDataFolder() + File.separator + "players");
            if(!folder.exists()){
                folder.mkdirs();
            }
        } catch(SecurityException e) {
            folder = null;
        }
	}

	private PlayerConfig getPlayerConfig(String pathName) {
		PlayerConfig playerConfig = new PlayerConfig(pathName,plugin);
		playerConfig.registerPlayerConfig();
		return playerConfig;
	}
	
	public void loadPlayerData() {
		ArrayList<PlayerData> playerData = new ArrayList<>();

		String path = plugin.getDataFolder() + File.separator + "players";
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles) {
			if (file.isFile()) {
				String pathName = file.getName();
				String ext = OtherUtils.getFileExtension(pathName);
				if (!ext.equals("yml")) {
					continue;
				}
				PlayerConfig config = new PlayerConfig(pathName, plugin);
				config.registerPlayerConfig();

				FileConfiguration players = config.getConfig();
				String name = players.getString("name");
				String uuid = config.getPath().replace(".yml", "");

				PlayerData p = new PlayerData(uuid,name);
				ArrayList<EventData> eventData = new ArrayList<>();

				if(players.contains("events")){
					for(String key : players.getConfigurationSection("events").getKeys(false)){
						boolean oneTime = players.getBoolean("events."+key+".one_time");
						long cooldown = players.getLong("events."+key+".cooldown");
						EventData event = new EventData(key,cooldown,oneTime);

						eventData.add(event);
					}
				}

				p.setEventData(eventData);
				playerData.add(p);
			}
		}

		plugin.getPlayerManager().setPlayerData(playerData);
	}

	public void savePlayer(PlayerData player){
		String playerName = player.getName();
		PlayerConfig playerConfig = getPlayerConfig(player.getUuid()+".yml");
		FileConfiguration players = playerConfig.getConfig();

		players.set("name", playerName);
		players.set("events", null);

		for(EventData event : player.getEventData()){
			String path = "events."+event.getName();
			players.set(path+".one_time", event.isOneTime());
			players.set(path+".cooldown", event.getCooldown());
		}

		playerConfig.savePlayerConfig();
	}
	
	public void savePlayerData() {
		for(PlayerData player : plugin.getPlayerManager().getPlayerData()) {
			if(player.isModified()){
				savePlayer(player);
			}
			player.setModified(false);
		}
	}
}

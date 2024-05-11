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
	
	private ArrayList<PlayerConfig> configPlayers;
	private ConditionalEvents plugin;
	
	public PlayerConfigsManager(ConditionalEvents plugin) {
		this.plugin = plugin;
		this.configPlayers = new ArrayList<PlayerConfig>();
	}
	
	public void configure() {
		createPlayersFolder();
		registerPlayers();
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
	
	public void savePlayers() {
		for(int i=0;i<configPlayers.size();i++) {
			configPlayers.get(i).savePlayerConfig();
		}
	}
	
	public void registerPlayers(){
		String path = plugin.getDataFolder() + File.separator + "players";
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		for (int i=0;i<listOfFiles.length;i++) {
			if(listOfFiles[i].isFile()) {
		        String pathName = listOfFiles[i].getName();
				String ext = OtherUtils.getFileExtension(pathName);
				if(!ext.equals("yml")) {
					continue;
				}
		        PlayerConfig config = new PlayerConfig(pathName,plugin);
		        config.registerPlayerConfig();
		        configPlayers.add(config);
		    }
		}
	}
	
	public ArrayList<PlayerConfig> getConfigPlayers(){
		return this.configPlayers;
	}
	
	public boolean fileAlreadyRegistered(String pathName) {
		for(int i=0;i<configPlayers.size();i++) {
			if(configPlayers.get(i).getPath().equals(pathName)) {
				return true;
			}
		}
		return false;
	}
	
	public PlayerConfig getPlayerConfig(String pathName) {
		for(int i=0;i<configPlayers.size();i++) {
			if(configPlayers.get(i).getPath().equals(pathName)) {
				return configPlayers.get(i);
			}
		}
		return null;
	}
	
	public ArrayList<PlayerConfig> getPlayerConfigs() {
		return this.configPlayers;
	}
	
	public boolean registerPlayer(String pathName) {
		if(!fileAlreadyRegistered(pathName)) {
			PlayerConfig config = new PlayerConfig(pathName,plugin);
	        config.registerPlayerConfig();
	        configPlayers.add(config);
	        return true;
		}else {
			return false;
		}
	}
	
	public void removeConfigPlayer(String path) {
		for(int i=0;i<configPlayers.size();i++) {
			if(configPlayers.get(i).getPath().equals(path)) {
				configPlayers.remove(i);
			}
		}
	}
	
	public void loadPlayerData() {
		ArrayList<PlayerData> playerData = new ArrayList<PlayerData>();
		
		for(PlayerConfig playerConfig : configPlayers) {
			FileConfiguration players = playerConfig.getConfig();
			String name = players.getString("name");
			String uuid = playerConfig.getPath().replace(".yml", "");

			PlayerData p = new PlayerData(uuid,name);
			ArrayList<EventData> eventData = new ArrayList<EventData>();

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
		plugin.getPlayerManager().setPlayerData(playerData);
	}

	public void savePlayer(PlayerData player){
		String playerName = player.getName();
		PlayerConfig playerConfig = getPlayerConfig(player.getUuid()+".yml");
		if(playerConfig == null) {
			registerPlayer(player.getUuid()+".yml");
			playerConfig = getPlayerConfig(player.getUuid()+".yml");
		}
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

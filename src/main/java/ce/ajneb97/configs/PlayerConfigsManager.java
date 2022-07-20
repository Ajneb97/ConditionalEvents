package ce.ajneb97.configs;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.model.player.EventData;
import ce.ajneb97.model.player.PlayerData;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class PlayerConfigsManager {
	
	private ArrayList<PlayerConfig> configPlayers;
	private ConditionalEvents plugin;
	
	public PlayerConfigsManager(ConditionalEvents plugin) {
		this.plugin = plugin;
		this.configPlayers = new ArrayList<>();
	}
	
	public void configure() {
		createPlayersFolder();
		registerPlayers();
		loadPlayerData();
	}
	
	public void createPlayersFolder(){
        try {
            Path folder = plugin.getDataFolder().toPath().resolve("players");
            if(Files.notExists(folder)){
                Files.createDirectory(folder);
            }
        } catch(IOException | SecurityException e) {
			e.printStackTrace();
        }
	}
	
	public void savePlayers() {
		for(int i=0;i<configPlayers.size();i++) {
			configPlayers.get(i).savePlayerConfig();
		}
	}
	
	public void registerPlayers(){
		Path path = plugin.getDataFolder().toPath().resolve("players");
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, Files::isRegularFile)) {
			for (Path file : stream) {
		        PlayerConfig config = new PlayerConfig(file);
		        config.registerPlayerConfig();
		        configPlayers.add(config);
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<PlayerConfig> getConfigPlayers(){
		return this.configPlayers;
	}
	
	public boolean fileAlreadyRegistered(String pathName) {
		for(int i=0;i<configPlayers.size();i++) {
			if(configPlayers.get(i).getPath().endsWith(pathName)) {
				return true;
			}
		}
		return false;
	}
	
	public PlayerConfig getPlayerConfig(String pathName) {
		for(int i=0;i<configPlayers.size();i++) {
			if(configPlayers.get(i).getPath().endsWith(pathName)) {
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
			PlayerConfig config = new PlayerConfig(plugin.getDataFolder().toPath().resolve("players").resolve(pathName));
	        config.registerPlayerConfig();
	        configPlayers.add(config);
	        return true;
		}else {
			return false;
		}
	}
	
	public void removeConfigPlayer(String path) {
		for(int i=0;i<configPlayers.size();i++) {
			if(configPlayers.get(i).getPath().endsWith(path)) {
				configPlayers.remove(i);
			}
		}
	}
	
	public void loadPlayerData() {
		ArrayList<PlayerData> playerData = new ArrayList<>();
		
		for(PlayerConfig playerConfig : configPlayers) {
			FileConfiguration players = playerConfig.getConfig();
			String name = players.getString("name");
			String uuid = playerConfig.getPath().getFileName().toString().replace(".yml", "");

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
		plugin.getPlayerManager().setPlayerData(playerData);
	}
	
	public void savePlayerData() {
		for(PlayerData player : plugin.getPlayerManager().getPlayerData()) {
			String playerName = player.getName();
			PlayerConfig playerConfig = getPlayerConfig(player.getUuid()+".yml");
			if(playerConfig == null) {
				registerPlayer(player.getUuid()+".yml");
				playerConfig = getPlayerConfig(player.getUuid()+".yml");
			}
			FileConfiguration players = playerConfig.getConfig();
			
			players.set("name", playerName);

			for(EventData event : player.getEventData()){
				String path = "events."+event.getName();
				players.set(path+".one_time", event.isOneTime());
				players.set(path+".cooldown", event.getCooldown());
			}
		}
		savePlayers();
	}
}

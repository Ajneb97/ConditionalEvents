package ce.ajneb97.configs;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.configs.model.CommonConfig;
import ce.ajneb97.model.player.EventData;
import ce.ajneb97.model.player.PlayerData;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.ArrayList;

public class PlayersConfigsManager extends DataFolderConfigManager{
	
	public PlayersConfigsManager(ConditionalEvents plugin, String folderName) {
		super(plugin, folderName);
	}

	@Override
	public void createFiles() {

	}


	@Override
	public void loadConfigs() {
		ArrayList<PlayerData> playerData = new ArrayList<>();

		ArrayList<CommonConfig> configs = getConfigs();
		for(CommonConfig commonConfig : configs){
			FileConfiguration players = commonConfig.getConfig();
			String name = players.getString("name");
			String uuid = commonConfig.getPath().replace(".yml", "");

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

	public void savePlayer(PlayerData player){
		String playerName = player.getName();
		CommonConfig playerConfig = getConfigFile(player.getUuid()+".yml");
		FileConfiguration players = playerConfig.getConfig();

		players.set("name", playerName);
		players.set("events", null);

		for(EventData event : player.getEventData()){
			String path = "events."+event.getName();
			players.set(path+".one_time", event.isOneTime());
			players.set(path+".cooldown", event.getCooldown());
		}

		playerConfig.saveConfig();
	}

	@Override
	public void saveConfigs() {
		for(PlayerData player : plugin.getPlayerManager().getPlayerData()) {
			if(player.isModified()){
				savePlayer(player);
			}
			player.setModified(false);
		}
	}
}

package ce.ajneb97.configs;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.configs.model.CommonConfig;
import ce.ajneb97.model.player.EventData;
import ce.ajneb97.model.player.GenericCallback;
import ce.ajneb97.model.player.PlayerData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class PlayersConfigsManager extends DataFolderConfigManager{
	
	public PlayersConfigsManager(ConditionalEvents plugin, String folderName) {
		super(plugin, folderName);
	}

	@Override
	public void createFiles() {

	}

	@Override
	public void loadConfigs() {
		// No use for player config
	}

	public void loadConfig(UUID uuid, GenericCallback<PlayerData> callback){
		new BukkitRunnable(){
			@Override
			public void run() {
				PlayerData playerData = null;
				CommonConfig playerConfig = getConfigFile(uuid+".yml",false);
				if(playerConfig != null){
					// If config exists
					FileConfiguration config = playerConfig.getConfig();
					String name = config.getString("name");

					playerData = new PlayerData(uuid,name);
					ArrayList<EventData> eventData = new ArrayList<>();
					if(config.contains("events")){
						for(String key : config.getConfigurationSection("events").getKeys(false)){
							boolean oneTime = config.getBoolean("events."+key+".one_time");
							long cooldown = config.getLong("events."+key+".cooldown");
							EventData event = new EventData(key,cooldown,oneTime);

							eventData.add(event);
						}
					}
					playerData.setEventData(eventData);
				}

				PlayerData finalPlayer = playerData;

				new BukkitRunnable(){
					@Override
					public void run() {
						callback.onDone(finalPlayer);
					}
				}.runTask(plugin);
			}
		}.runTaskAsynchronously(plugin);
	}

	public void saveConfig(PlayerData player){
		String playerName = player.getName();
		CommonConfig playerConfig = getConfigFile(player.getUuid().toString()+".yml",true);
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
		Map<UUID, PlayerData> players = plugin.getPlayerManager().getPlayers();
		for(Map.Entry<UUID, PlayerData> entry : players.entrySet()) {
			PlayerData playerData = entry.getValue();
			if(playerData.isModified()){
				saveConfig(playerData);
			}
			playerData.setModified(false);
		}
	}

	public void resetDataForAllPlayers(String eventName){
		ArrayList<CommonConfig> configs = getConfigs();
		for(CommonConfig commonConfig : configs) {
			FileConfiguration config = commonConfig.getConfig();
			if(eventName.equals("all")){
				config.set("events",null);
			}else{
				if(!config.contains("events."+eventName)){
					continue;
				}
				config.set("events."+eventName,null);
			}

			commonConfig.saveConfig();
		}
	}
}

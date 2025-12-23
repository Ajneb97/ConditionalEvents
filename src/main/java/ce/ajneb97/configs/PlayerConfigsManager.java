package ce.ajneb97.configs;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.configs.model.CommonConfig;
import ce.ajneb97.model.player.EventData;
import ce.ajneb97.model.player.GenericCallback;
import ce.ajneb97.model.player.PlayerData;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("DataFlowIssue")
public class PlayerConfigsManager extends DataFolderConfigManager {

    private final boolean isFolia;

    public PlayerConfigsManager(ConditionalEvents plugin, String folderName) {
        super(plugin, folderName);
        this.isFolia = plugin.isFolia;
    }

    @Override
    public void createFiles() {
    }

    @Override
    public void loadConfigs() {
        // No use for player config
    }

    public void loadData(UUID uuid, GenericCallback<PlayerData> callback) {
        Runnable asyncTask = () -> {
            AtomicReference<PlayerData> playerData = new AtomicReference<>();

            CommonConfig playerConfig = getConfigFile(uuid + ".yml", false);
            if (playerConfig != null) {
                // If config exists
                FileConfiguration config = playerConfig.getConfig();
                String name = config.getString("name");

                playerData.set(new PlayerData(uuid, name));
                ArrayList<EventData> eventData = new ArrayList<>();
                if (config.contains("events")) {
                    for (String key : config.getConfigurationSection("events").getKeys(false)) {
                        boolean oneTime = config.getBoolean("events." + key + ".one_time");
                        long cooldown = config.getLong("events." + key + ".cooldown");
                        EventData event = new EventData(key, cooldown, oneTime);

                        eventData.add(event);
                    }
                }
                playerData.get().setEventData(eventData);
            }

            Runnable mainThreadTask = () -> callback.onDone(playerData.get());

            if (isFolia) {
                plugin.getServer().getGlobalRegionScheduler().execute(plugin, mainThreadTask);
            } else {
                plugin.getServer().getScheduler().runTask(plugin, mainThreadTask);
            }
        };

        if (isFolia) {
            plugin.getServer().getAsyncScheduler().runNow(plugin, scheduledTask -> asyncTask.run());
        } else {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, asyncTask);
        }
    }

    public void saveData(PlayerData player) {
        String playerName = player.getName();
        CommonConfig playerConfig = getConfigFile(player.getUuid().toString() + ".yml", true);
        FileConfiguration players = playerConfig.getConfig();

        players.set("name", playerName);
        players.set("events", null);

        for (EventData event : player.getEventData()) {
            String path = "events." + event.getName();
            players.set(path + ".one_time", event.isOneTime());
            players.set(path + ".cooldown", event.getCooldown());
        }

        playerConfig.saveConfig();
    }

    @Override
    public void saveAllData() {
        Map<UUID, PlayerData> players = plugin.getPlayerManager().getPlayers();
        for (Map.Entry<UUID, PlayerData> entry : players.entrySet()) {
            PlayerData playerData = entry.getValue();
            if (playerData.isModified()) {
                saveData(playerData);
            }
            playerData.setModified(false);
        }
    }

    public void resetDataForAllPlayers(String eventName) {
        ArrayList<CommonConfig> configs = getConfigs();
        for (CommonConfig commonConfig : configs) {
            FileConfiguration config = commonConfig.getConfig();
            if (eventName.equals("all")) {
                config.set("events", null);
            } else {
                if (!config.contains("events." + eventName)) {
                    continue;
                }
                config.set("events." + eventName, null);
            }

            commonConfig.saveConfig();
        }
    }
}
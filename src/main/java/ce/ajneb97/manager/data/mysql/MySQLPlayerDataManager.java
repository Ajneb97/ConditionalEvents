package ce.ajneb97.manager.data.mysql;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.configs.PlayerConfigsManager;
import ce.ajneb97.configs.model.CommonConfig;
import ce.ajneb97.manager.data.PlayerDataManager;
import ce.ajneb97.model.player.EventData;
import ce.ajneb97.model.player.GenericCallback;
import ce.ajneb97.model.player.PlayerData;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("DataFlowIssue")
public class MySQLPlayerDataManager extends PlayerDataManager {

    private final MySQLConnection mySQLConnection;

    public MySQLPlayerDataManager(ConditionalEvents plugin) {
        this.mySQLConnection = new MySQLConnection(plugin);
        this.mySQLConnection.setupMySql();
    }

    @Override
    public void loadData(UUID uuid, GenericCallback<PlayerData> callback) {
        mySQLConnection.getPlayer(uuid, callback);
    }

    @Override
    public void saveData(PlayerData playerData) {
        mySQLConnection.saveData(playerData);
    }

    @Override
    public void saveAllData() {
        mySQLConnection.saveAllData();
    }

    @Override
    public void resetDataForAllPlayers(String event) {
        mySQLConnection.resetDataForAllPlayers(event);
    }

    public CompletableFuture<Integer> migrate(PlayerConfigsManager playerConfigsManager) {
        return CompletableFuture.supplyAsync(() -> {
            ArrayList<CommonConfig> configs = playerConfigsManager.getConfigs();
            int count = 0;
            for (CommonConfig commonConfig : configs) {
                String fileName = commonConfig.getFile().getName();
                if (!fileName.endsWith(".yml")) continue;

                try {
                    UUID uuid = UUID.fromString(fileName.replace(".yml", ""));
                    FileConfiguration config = commonConfig.getConfig();
                    String name = config.getString("name");

                    PlayerData playerData = new PlayerData(uuid, name);
                    ArrayList<EventData> eventDataList = new ArrayList<>();
                    if (config.contains("events")) {
                        for (String key : config.getConfigurationSection("events").getKeys(false)) {
                            boolean oneTime = config.getBoolean("events." + key + ".one_time");
                            long cooldown = config.getLong("events." + key + ".cooldown");
                            eventDataList.add(new EventData(key, cooldown, oneTime));
                        }
                    }
                    playerData.setEventData(eventDataList);
                    saveData(playerData);
                    count++;
                } catch (Exception ignored) {
                }
            }
            return count;
        });
    }

    public void close() {
        if (mySQLConnection != null) {
            mySQLConnection.close();
        }
    }
}
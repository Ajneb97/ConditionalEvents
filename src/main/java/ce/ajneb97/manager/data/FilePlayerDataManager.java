package ce.ajneb97.manager.data;

import ce.ajneb97.configs.PlayerConfigsManager;
import ce.ajneb97.model.player.GenericCallback;
import ce.ajneb97.model.player.PlayerData;

import java.util.UUID;

public class FilePlayerDataManager extends PlayerDataManager {

    private final PlayerConfigsManager configsManager;

    public FilePlayerDataManager(PlayerConfigsManager configsManager){
        this.configsManager = configsManager;
    }

    @Override
    public void loadData(UUID uuid, GenericCallback<PlayerData> callback) {
        configsManager.loadData(uuid, callback);
    }

    @Override
    public void saveData(PlayerData playerData) {
        configsManager.saveData(playerData);
    }

    @Override
    public void saveAllData() {
        configsManager.saveAllData();
    }

    @Override
    public void resetDataForAllPlayers(String event) {
        configsManager.resetDataForAllPlayers(event);
    }
}

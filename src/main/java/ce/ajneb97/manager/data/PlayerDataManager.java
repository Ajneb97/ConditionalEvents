package ce.ajneb97.manager.data;

import ce.ajneb97.model.player.GenericCallback;
import ce.ajneb97.model.player.PlayerData;

import java.util.UUID;

public abstract class PlayerDataManager {

    public abstract void loadData(UUID uuid, GenericCallback<PlayerData> callback);

    public abstract void saveData(PlayerData playerData);

    public abstract void saveAllData();

    public abstract void resetDataForAllPlayers(String event);
}

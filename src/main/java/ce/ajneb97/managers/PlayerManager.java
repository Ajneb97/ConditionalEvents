package ce.ajneb97.managers;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.model.player.PlayerData;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class PlayerManager {

    private ConditionalEvents plugin;
    private ArrayList<PlayerData> playerData;
    public PlayerManager(ConditionalEvents plugin){
        this.plugin = plugin;
        this.playerData = new ArrayList<PlayerData>();
    }

    public ArrayList<PlayerData> getPlayerData() {
        return playerData;
    }

    public void setPlayerData(ArrayList<PlayerData> playerData) {
        this.playerData = playerData;
    }

    public PlayerData getPlayerData(Player player){
        for(PlayerData p : playerData){
            if(p.getUuid().equals(player.getUniqueId().toString())){
                return p;
            }
        }
        return null;
    }

    public PlayerData getPlayerDataByName(String playerName){
        for(PlayerData p : playerData){
            if(p.getName().equals(playerName)){
                return p;
            }
        }
        return null;
    }

    private PlayerData createPlayerData(Player player){
        PlayerData p = getPlayerData(player);
        if(p == null){
            p = new PlayerData(player.getUniqueId().toString(),player.getName());
            p.setModified(true);
            playerData.add(p);
        }
        return p;
    }

    public void setEventCooldown(String eventName,Player player){
        PlayerData p = createPlayerData(player);
        p.setCooldown(eventName,System.currentTimeMillis());
        p.setModified(true);
    }

    public long getEventCooldown(String eventName,Player player){
        PlayerData p = getPlayerData(player);
        if(p == null){
            return 0;
        }else{
            return p.getCooldown(eventName);
        }
    }

    public void setEventOneTime(String eventName,Player player){
        PlayerData p = createPlayerData(player);
        p.setOneTime(eventName,true);
        p.setModified(true);
    }

    public boolean getEventOneTime(String eventName,Player player){
        PlayerData p = getPlayerData(player);
        if(p == null){
            return false;
        }else{
            return p.isEventOneTime(eventName);
        }
    }

    public void resetEventDataForPlayers(String eventName){
        for(PlayerData p : playerData){
            p.resetCooldown(eventName);
            p.setOneTime(eventName,false);
            p.setModified(true);
        }
    }

    public void resetAllDataForPlayers(){
        for(PlayerData p : playerData){
            p.resetAll();
            p.setModified(true);
        }
    }
}

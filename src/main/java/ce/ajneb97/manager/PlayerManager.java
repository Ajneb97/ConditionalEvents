package ce.ajneb97.manager;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.model.player.GenericCallback;
import ce.ajneb97.model.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("DataFlowIssue")
public class PlayerManager {

    private final ConditionalEvents plugin;
    private final Map<UUID, PlayerData> players;
    private final Map<String, UUID> playerNames;

    public PlayerManager(ConditionalEvents plugin) {
        this.plugin = plugin;
        this.players = new HashMap<>();
        this.playerNames = new HashMap<>();
    }

    public Map<UUID, PlayerData> getPlayers() {
        return players;
    }

    public void addPlayer(PlayerData p) {
        players.put(p.getUuid(), p);
        playerNames.put(p.getName(), p.getUuid());
    }

    public PlayerData getPlayer(Player player, boolean create) {
        PlayerData playerData = players.get(player.getUniqueId());
        if (playerData == null && create) {
            playerData = new PlayerData(player.getUniqueId(), player.getName());
            addPlayer(playerData);
        }
        return playerData;
    }

    private void updatePlayerName(String oldName, String newName, UUID uuid) {
        if (oldName != null) {
            playerNames.remove(oldName);
        }
        playerNames.put(newName, uuid);
    }

    private UUID getPlayerUUID(String name) {
        return playerNames.get(name);
    }

    public PlayerData getPlayerByName(String name) {
        UUID uuid = getPlayerUUID(name);
        return players.get(uuid);
    }

    public void removePlayer(PlayerData playerData) {
        players.remove(playerData.getUuid());
        playerNames.remove(playerData.getName());
    }

    public void setEventCooldown(String eventName, Player player) {
        PlayerData p = getPlayer(player, true);
        p.setCooldown(eventName, System.currentTimeMillis());
        p.setModified(true);
    }

    public long getEventCooldown(String eventName, Player player) {
        PlayerData p = getPlayer(player, false);
        if (p == null) {
            return 0;
        } else {
            return p.getCooldown(eventName);
        }
    }

    public void setEventOneTime(String eventName, Player player) {
        PlayerData p = getPlayer(player, true);
        p.setOneTime(eventName, true);
        p.setModified(true);
    }

    public boolean getEventOneTime(String eventName, Player player) {
        PlayerData p = getPlayer(player, false);
        if (p == null) {
            return false;
        } else {
            return p.isEventOneTime(eventName);
        }
    }

    public String resetDataForPlayer(String playerName, String eventName, FileConfiguration messagesConfig) {
        if (Bukkit.getPlayer(playerName) == null) {
            return messagesConfig.getString("Messages.playerNotOnline");
        }

        PlayerData playerData = getPlayerByName(playerName);
        if (playerData == null) {
            return messagesConfig.getString("Messages.playerDoesNotExists");
        }

        if (eventName.equals("all")) {
            // All data
            playerData.resetAll();
            return messagesConfig.getString("Messages.eventDataResetAll").replace("%player%", playerName);
        } else {
            // Specific event
            playerData.resetEvent(eventName);
            return messagesConfig.getString("Messages.eventDataReset").replace("%player%", playerName)
                    .replace("%event%", eventName);
        }
    }

    public void resetDataForAllPlayers(String eventName, FileConfiguration messagesConfig, GenericCallback<String> callback) {
        if (plugin.isFolia) {
            plugin.getServer().getAsyncScheduler().runNow(plugin, scheduledTask -> {
                plugin.getPlayerDataManager().resetDataForAllPlayers(eventName);

                plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
                    String result;
                    if (eventName.equals("all")) {
                        // All data, Online:
                        players.values().forEach(PlayerData::resetAll);
                        result = messagesConfig.getString("Messages.eventDataResetAllForAllPlayers");
                    } else {
                        // Specific event, Online:
                        players.values().forEach(p -> p.resetEvent(eventName));
                        result = messagesConfig.getString("Messages.eventDataResetForAllPlayers")
                                .replace("%event%", eventName);
                    }

                    callback.onDone(result);
                });
            });
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getPlayerDataManager().resetDataForAllPlayers(eventName);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            String result;
                            if (eventName.equals("all")) {
                                // All data, Online:
                                players.values().forEach(PlayerData::resetAll);
                                result = messagesConfig.getString("Messages.eventDataResetAllForAllPlayers");
                            } else {
                                // Specific event, Online:
                                players.values().forEach(p -> p.resetEvent(eventName));
                                result = messagesConfig.getString("Messages.eventDataResetForAllPlayers")
                                        .replace("%event%", eventName);
                            }

                            callback.onDone(result);
                        }
                    }.runTask(plugin);
                }
            }.runTaskAsynchronously(plugin);
        }
    }

    public void manageJoin(Player player) {
        // Load player data from a file if exists
        plugin.getPlayerDataManager().loadData(player.getUniqueId(), playerData -> {
            if (playerData != null) {
                addPlayer(playerData);
                if (playerData.getName() == null || !playerData.getName().equals(player.getName())) {
                    updatePlayerName(playerData.getName(), player.getName(), player.getUniqueId());
                    playerData.setName(player.getName());
                    playerData.setModified(true);
                }
            }
        });
    }

    public void manageLeave(Player player) {
        // Save player data into file and remove from a map
        PlayerData playerData = getPlayer(player, false);
        if (playerData != null) {
            if (playerData.isModified()) {
                plugin.getPlayerDataManager().saveData(playerData);
            }
            removePlayer(playerData);
        }
    }
}
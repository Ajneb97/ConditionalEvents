package ce.ajneb97.manager.data.mysql;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.model.player.EventData;
import ce.ajneb97.model.player.GenericCallback;
import ce.ajneb97.model.player.PlayerData;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class MySQLConnection {

    private final ConditionalEvents plugin;
    private HikariDataSource dataSource;

    public MySQLConnection(ConditionalEvents plugin) {
        this.plugin = plugin;
    }

    public void setupMySql() {
        HikariConfig hikariConfig = getHikariConfig();
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.dataSource = new HikariDataSource(hikariConfig);
        createTables();
        plugin.getLogger().info("Successfully connected to the MySQL Database.");
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    private void createTables() {
        try (Connection connection = getConnection()) {
            PreparedStatement st1 = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS ce_players (UUID VARCHAR(36) NOT NULL, NAME VARCHAR(50), PRIMARY KEY (UUID))");
            st1.executeUpdate();
            PreparedStatement st2 = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS ce_events_data (UUID VARCHAR(36) NOT NULL, EVENT_NAME VARCHAR(100) NOT NULL, " +
                            "COOLDOWN BIGINT, ONE_TIME BOOLEAN, PRIMARY KEY (UUID, EVENT_NAME), " +
                            "FOREIGN KEY (UUID) REFERENCES ce_players(UUID) ON DELETE CASCADE)");
            st2.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Error while creating MySQL tables", e);
        }
    }

    public void getPlayer(UUID uuid, GenericCallback<PlayerData> callback) {
        Runnable task = () -> {
            PlayerData player = null;
            try (Connection connection = getConnection()) {
                PreparedStatement st = connection.prepareStatement(
                        "SELECT p.NAME, e.EVENT_NAME, e.COOLDOWN, e.ONE_TIME FROM ce_players p " +
                                "LEFT JOIN ce_events_data e ON p.UUID = e.UUID WHERE p.UUID = ?");
                st.setString(1, uuid.toString());
                ResultSet rs = st.executeQuery();

                ArrayList<EventData> events = new ArrayList<>();
                while (rs.next()) {
                    if (player == null) {
                        player = new PlayerData(uuid, rs.getString("NAME"));
                    }
                    String eventName = rs.getString("EVENT_NAME");
                    if (eventName != null) {
                        events.add(new EventData(eventName, rs.getLong("COOLDOWN"), rs.getBoolean("ONE_TIME")));
                    }
                }
                if (player != null) player.setEventData(events);

            } catch (SQLException e) {
                plugin.getLogger().log(java.util.logging.Level.SEVERE, "Error while loading player data from MySQL for: " + uuid, e);
            }

            PlayerData finalPlayer = player;
            if (plugin.isFolia) {
                plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> callback.onDone(finalPlayer));
            } else {
                plugin.getServer().getScheduler().runTask(plugin, () -> callback.onDone(finalPlayer));
            }
        };

        if (plugin.isFolia) {
            plugin.getServer().getAsyncScheduler().runNow(plugin, s -> task.run());
        } else {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    public void saveData(PlayerData player) {
        try (Connection connection = getConnection()) {
            // Update or Insert player
            PreparedStatement st1 = connection.prepareStatement(
                    "INSERT INTO ce_players (UUID, NAME) VALUES (?, ?) ON DUPLICATE KEY UPDATE NAME = ?");
            st1.setString(1, player.getUuid().toString());
            st1.setString(2, player.getName());
            st1.setString(3, player.getName());
            st1.executeUpdate();

            // Clear old event data for this player or update?
            // Efficiency wise: we can replace all for simplicity or use ON DUPLICATE KEY.
            for (EventData event : player.getEventData()) {
                PreparedStatement st2 = connection.prepareStatement(
                        "INSERT INTO ce_events_data (UUID, EVENT_NAME, COOLDOWN, ONE_TIME) VALUES (?, ?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE COOLDOWN = ?, ONE_TIME = ?");
                st2.setString(1, player.getUuid().toString());
                st2.setString(2, event.getName());
                st2.setLong(3, event.getCooldown());
                st2.setBoolean(4, event.isOneTime());
                st2.setLong(5, event.getCooldown());
                st2.setBoolean(6, event.isOneTime());
                st2.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Error while saving player data to MySQL for: " + player.getName(), e);
        }
    }

    public void saveAllData() {
        Map<UUID, PlayerData> players = plugin.getPlayerManager().getPlayers();
        for (PlayerData player : players.values()) {
            if (player.isModified()) {
                saveData(player);
                player.setModified(false);
            }
        }
    }

    public void resetDataForAllPlayers(String eventName) {
        try (Connection connection = getConnection()) {
            PreparedStatement st;
            if (eventName.equals("all")) {
                st = connection.prepareStatement("DELETE FROM ce_events_data");
            } else {
                st = connection.prepareStatement("DELETE FROM ce_events_data WHERE EVENT_NAME = ?");
                st.setString(1, eventName);
            }
            st.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Error while resetting MySQL data for event: " + eventName, e);
        }
    }

    private @NotNull HikariConfig getHikariConfig() {
        FileConfiguration config = plugin.getConfigsManager().getMainConfigManager().getConfig();
        String host = config.getString("Config.mysql_database.host");
        int port = config.getInt("Config.mysql_database.port");
        String username = config.getString("Config.mysql_database.username");
        String password = config.getString("Config.mysql_database.password");
        String database = config.getString("Config.mysql_database.database");

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + config.getBoolean("Config.mysql_database.advanced.useSSL"));
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        return hikariConfig;
    }
}
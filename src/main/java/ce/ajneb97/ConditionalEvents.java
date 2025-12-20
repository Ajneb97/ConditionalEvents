package ce.ajneb97;

import ce.ajneb97.api.ConditionalEventsAPI;
import ce.ajneb97.api.ExpansionCE;
import ce.ajneb97.command.MainCommand;
import ce.ajneb97.configs.ConfigsManager;
import ce.ajneb97.libs.armorequipevent.ArmorListener;
import ce.ajneb97.libs.itemselectevent.ItemSelectListener;
import ce.ajneb97.libs.itemselectevent.ItemSelectListenerNew;
import ce.ajneb97.libs.offhandevent.OffHandListener;
import ce.ajneb97.listeners.*;
import ce.ajneb97.listeners.dependencies.CitizensListener;
import ce.ajneb97.listeners.dependencies.WGRegionEventsListener;
import ce.ajneb97.manager.*;
import ce.ajneb97.manager.commandregister.CommandRegisterManager;
import ce.ajneb97.manager.data.FilePlayerDataManager;
import ce.ajneb97.manager.data.PlayerDataManager;
import ce.ajneb97.manager.data.mysql.MySQLPlayerDataManager;
import ce.ajneb97.manager.dependencies.Metrics;
import ce.ajneb97.model.EventType;
import ce.ajneb97.model.internal.ConditionEvent;
import ce.ajneb97.model.internal.UpdateCheckerResult;
import ce.ajneb97.tasks.PlayerDataSaveTask;
import ce.ajneb97.utils.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

@SuppressWarnings("deprecation")
public class ConditionalEvents extends JavaPlugin {

    PluginDescriptionFile pdfFile = getDescription();
    public String version = pdfFile.getVersion();
    public static ServerVersion serverVersion;
    public static String prefix;
    private EventsManager eventsManager;
    private DependencyManager dependencyManager;
    private PlayerManager playerManager;
    private ConfigsManager configsManager;
    private PlayerDataManager playerDataManager;
    private DebugManager debugManager;
    private BungeeMessagingManager bungeeMessagingManager;
    private MessagesManager messagesManager;
    private VerifyManager verifyManager;
    private UpdateCheckerManager updateCheckerManager;
    private SavedItemsManager savedItemsManager;
    private APIManager apiManager;
    private InterruptEventManager interruptEventManager;
    private PlayerDataSaveTask playerDataSaveTask;
    public final boolean isFolia = checkFolia();

    public void onEnable() {
        setVersion();
        setPrefix();

        this.eventsManager = new EventsManager(this);
        this.dependencyManager = new DependencyManager(this);
        this.bungeeMessagingManager = new BungeeMessagingManager(this);
        this.debugManager = new DebugManager();
        this.playerManager = new PlayerManager(this);
        this.savedItemsManager = new SavedItemsManager(this);
        this.apiManager = new APIManager(this);
        this.interruptEventManager = new InterruptEventManager();
        this.configsManager = new ConfigsManager(this);
        this.configsManager.configure();

        FileConfiguration mainConfig = configsManager.getMainConfigManager().getConfig();
        if (mainConfig.getBoolean("Config.mysql_database.enabled")) {
            MySQLPlayerDataManager mysqlManager = new MySQLPlayerDataManager(this);
            this.playerDataManager = mysqlManager;

            if (mainConfig.getBoolean("Config.mysql_database.migrate")) {
                getLogger().info("Starting MySQL migration...");
                mysqlManager.migrate(configsManager.getPlayerConfigsManager()).thenAccept(count -> {
                    getLogger().info("Successfully migrated " + count + " players to MySQL.");
                    mainConfig.set("Config.mysql_database.migrate", false);
                    configsManager.getMainConfigManager().saveConfig();
                }).exceptionally(ex -> {
                    getLogger().severe("Error during MySQL migration: " + ex.getMessage());
                    return null;
                });
            }
        } else {
            this.playerDataManager = new FilePlayerDataManager(configsManager.getPlayerConfigsManager());
        }

        registerEvents();
        registerCommands();

        this.verifyManager = new VerifyManager(this);
        this.verifyManager.verifyEvents();

        CommandRegisterManager commandRegisterManager = new CommandRegisterManager(this);
        commandRegisterManager.registerCommands();

        reloadPlayerDataSaveTask();

        ConditionalEventsAPI.init(this);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ExpansionCE(this).register();
        }

        new Metrics(this, 19371);

        Bukkit.getConsoleSender().sendMessage(MessagesManager.getLegacyColoredMessage(prefix + " &eHas been enabled! &fVersion: " + version));
        Bukkit.getConsoleSender().sendMessage(MessagesManager.getLegacyColoredMessage(prefix + " &eThanks for using my plugin!   &f~Ajneb97"));

        updateCheckerManager = new UpdateCheckerManager(version);
        if (configsManager.getMainConfigManager().isUpdateNotifications()) {
            updateMessage(updateCheckerManager.check());
        }

        new ConditionEvent(this, null, null, EventType.SERVER_START, null).checkEvent();
    }

    public void onDisable() {
        new ConditionEvent(this, null, null, EventType.SERVER_STOP, null).checkEvent();

        playerDataManager.saveAllData();

        if (playerDataManager instanceof MySQLPlayerDataManager) {
            ((MySQLPlayerDataManager) playerDataManager).close();
        }

        Bukkit.getConsoleSender().sendMessage(MessagesManager.getLegacyColoredMessage(prefix + " &eHas been disabled! &fVersion: " + version));
    }

    public void registerEvents() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerEventsListener(this), this);
        pm.registerEvents(new ItemEventsListener(this), this);
        pm.registerEvents(new ArmorListener(new ArrayList<>()), this);
        pm.registerEvents(new ItemSelectListener(this), this);
        pm.registerEvents(new OtherEventsListener(this), this);
        pm.registerEvents(new CustomEventListener(this), this);

        if (serverVersion.serverVersionGreaterEqualThan(serverVersion, ServerVersion.v1_9_R1)) {
            pm.registerEvents(new ItemSelectListenerNew(), this);
            pm.registerEvents(new PlayerEventsListenerNew1_9(this), this);
            pm.registerEvents(new OffHandListener(), this);
        }
        if (serverVersion.serverVersionGreaterEqualThan(serverVersion, ServerVersion.v1_16_R1)) {
            pm.registerEvents(new PlayerEventsListenerNew1_16(this), this);
        }

        if (dependencyManager.isCitizens()) {
            pm.registerEvents(new CitizensListener(this), this);
        }
        if (dependencyManager.isWorldGuardEvents()) {
            pm.registerEvents(new WGRegionEventsListener(this), this);
        }
    }

    public void setPrefix() {
        prefix = MessagesManager.getLegacyColoredMessage("&4[&bConditionalEvents&4]");
    }

    public void setVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String bukkitVersion = Bukkit.getServer().getBukkitVersion().split("-")[0];
        switch (bukkitVersion) {
            case "1.20.5":
            case "1.20.6":
                serverVersion = ServerVersion.v1_20_R4;
                break;
            case "1.21":
            case "1.21.1":
                serverVersion = ServerVersion.v1_21_R1;
                break;
            case "1.21.2":
            case "1.21.3":
                serverVersion = ServerVersion.v1_21_R2;
                break;
            case "1.21.4":
                serverVersion = ServerVersion.v1_21_R3;
                break;
            case "1.21.5":
                serverVersion = ServerVersion.v1_21_R4;
                break;
            case "1.21.6":
            case "1.21.7":
            case "1.21.8":
                serverVersion = ServerVersion.v1_21_R5;
                break;
            case "1.21.9":
            case "1.21.10":
                serverVersion = ServerVersion.v1_21_R6;
                break;
            case "1.21.11":
                serverVersion = ServerVersion.v1_21_R7;
                break;
            default:
                try {
                    serverVersion = ServerVersion.valueOf(packageName.replace("org.bukkit.craftbukkit.", ""));
                } catch (Exception e) {
                    serverVersion = ServerVersion.v1_21_R7;
                }
        }
    }

    public void reloadEvents() {
        HandlerList.unregisterAll(this);
        registerEvents();
    }

    public void reloadPlayerDataSaveTask() {
        if (playerDataSaveTask != null) playerDataSaveTask.stop();
        playerDataSaveTask = new PlayerDataSaveTask(this);
        playerDataSaveTask.start(configsManager.getMainConfigManager().getConfig().getInt("Config.data_save_time"));
    }

    public void registerCommands() {
        PluginCommand command = getCommand("conditionalevents");
        if (command != null) {
            command.setExecutor(new MainCommand(this));
        }
    }

    public EventsManager getEventsManager() {
        return eventsManager;
    }

    public DependencyManager getDependencyManager() {
        return dependencyManager;
    }

    public ConfigsManager getConfigsManager() {
        return configsManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public DebugManager getDebugManager() {
        return debugManager;
    }

    public BungeeMessagingManager getBungeeMessagingManager() {
        return bungeeMessagingManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public MessagesManager getMessagesManager() {
        return messagesManager;
    }

    public void setMessagesManager(MessagesManager messagesManager) {
        this.messagesManager = messagesManager;
    }

    public VerifyManager getVerifyManager() {
        return verifyManager;
    }

    public UpdateCheckerManager getUpdateCheckerManager() {
        return updateCheckerManager;
    }

    public APIManager getApiManager() {
        return apiManager;
    }

    public SavedItemsManager getSavedItemsManager() {
        return savedItemsManager;
    }

    public InterruptEventManager getInterruptEventManager() {
        return interruptEventManager;
    }

    public void updateMessage(UpdateCheckerResult result) {
        if (!result.isError()) {
            String latestVersion = result.getLatestVersion();
            if (latestVersion != null) {
                Bukkit.getConsoleSender().sendMessage(MessagesManager.getLegacyColoredMessage("&cThere is a new version available. &e(&7" + latestVersion + "&e)"));
                Bukkit.getConsoleSender().sendMessage(MessagesManager.getLegacyColoredMessage("&cYou can download it at: &fhttps://modrinth.com/plugin/conditionalevents"));
            }
        } else {
            Bukkit.getConsoleSender().sendMessage(MessagesManager.getLegacyColoredMessage(prefix + " &cError while checking update."));
        }
    }

    private boolean checkFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}

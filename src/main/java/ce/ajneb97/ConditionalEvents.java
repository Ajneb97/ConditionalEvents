package ce.ajneb97;


import ce.ajneb97.api.ConditionalEventsAPI;
import ce.ajneb97.configs.ConfigsManager;
import ce.ajneb97.libs.armorequipevent.ArmorListener;
import ce.ajneb97.libs.itemselectevent.ItemSelectListener;
import ce.ajneb97.libs.itemselectevent.ItemSelectListenerNew;
import ce.ajneb97.listeners.CustomEventListener;
import ce.ajneb97.listeners.ItemEventsListener;
import ce.ajneb97.listeners.OtherEventsListener;
import ce.ajneb97.listeners.PlayerEventsListener;
import ce.ajneb97.managers.*;
import ce.ajneb97.model.internal.UpdateCheckerResult;
import ce.ajneb97.tasks.PlayerDataSaveTask;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;

public class ConditionalEvents extends JavaPlugin {
  
	PluginDescriptionFile pdfFile = getDescription();
	public String version = pdfFile.getVersion();
	public static String prefix = MessagesManager.getColoredMessage("&4[&bConditionalEvents&4]");

	private EventsManager eventsManager;
	private DependencyManager dependencyManager;
	private PlayerManager playerManager;
	private ConfigsManager configsManager;
	private DebugManager debugManager;
	private BungeeMessagingManager bungeeMessagingManager;
	private MessagesManager messagesManager;
	private VerifyManager verifyManager;
	private UpdateCheckerManager updateCheckerManager;
	private PlayerDataSaveTask playerDataSaveTask;

  @Override
	public void onEnable(){
		this.eventsManager = new EventsManager(this);
		this.dependencyManager = new DependencyManager();
		this.bungeeMessagingManager = new BungeeMessagingManager(this);
		this.debugManager = new DebugManager(this);
		this.playerManager =  new PlayerManager(this);
		this.configsManager = new ConfigsManager(this);
		this.configsManager.configure();
		registerEvents();
		registerCommands();

		this.verifyManager = new VerifyManager(this);
		this.verifyManager.verifyEvents();

		reloadPlayerDataSaveTask();

		ConditionalEventsAPI api = new ConditionalEventsAPI(this);

		Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage(prefix+" &eHas been enabled! &fVersion: "+version));
        Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage(prefix+" &eThanks for using my plugin!   &f~Ajneb97"));

		updateCheckerManager = new UpdateCheckerManager(version);
		updateMessage(updateCheckerManager.check());
	}
	
	@Override
	public void onDisable(){
		this.configsManager.getPlayerConfigsManager().savePlayerData();
		Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage(prefix+" &eHas been disabled! &fVersion: "+version));
	}

	public void registerEvents() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PlayerEventsListener(this), this);
		pm.registerEvents(new ItemEventsListener(this), this);
		pm.registerEvents(new ArmorListener(new ArrayList<>()), this);
		pm.registerEvents(new ItemSelectListener(this), this);
		pm.registerEvents(new OtherEventsListener(this), this);
		pm.registerEvents(new CustomEventListener(this), this);
		if(!Bukkit.getVersion().contains("1.8")) {
			pm.registerEvents(new ItemSelectListenerNew(), this);
		}
	}

	public void reloadEvents(){
		HandlerList.unregisterAll(this);
		registerEvents();
	}

	public void reloadPlayerDataSaveTask() {
		if(playerDataSaveTask != null) {
			playerDataSaveTask.end();
		}
		playerDataSaveTask = new PlayerDataSaveTask(this);
		playerDataSaveTask.start(configsManager.getMainConfigManager().getConfig().getInt("Config.data_save_time"));
	}

	public void registerCommands(){
		this.getCommand("conditionalevents").setExecutor(new MainCommand(this));
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

	public void updateMessage(UpdateCheckerResult result){
		if(!result.isError()){
			String latestVersion = result.getLatestVersion();
			if(latestVersion != null){
				Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage("&cThere is a new version available. &e(&7"+latestVersion+"&e)"));
				Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage("&cYou can download it at: &fhttps://www.spigotmc.org/resources/82271/"));
			}
		}else{
			Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage(prefix+" &cError while checking update."));
		}
	}

}

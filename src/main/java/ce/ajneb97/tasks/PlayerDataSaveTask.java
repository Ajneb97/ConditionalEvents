package ce.ajneb97.tasks;

import ce.ajneb97.ConditionalEvents;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerDataSaveTask {

	private ConditionalEvents plugin;
	private boolean end;
	public PlayerDataSaveTask(ConditionalEvents plugin) {
		this.plugin = plugin;
		this.end = false;
	}
	
	public void end() {
		end = true;
	}
	
	public void start(int minutes) {
		long ticks = minutes*60*20;
		
		new BukkitRunnable() {
			@Override
			public void run() {
				if(end) {
					this.cancel();
				}else {
					execute();
				}
			}
			
		}.runTaskTimerAsynchronously(plugin, 0L, ticks);
	}
	
	public void execute() {
		plugin.getConfigsManager().getPlayerConfigsManager().saveConfigs();
	}
}

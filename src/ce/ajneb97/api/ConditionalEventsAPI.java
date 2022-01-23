package ce.ajneb97.api;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.libs.actionbar.ActionBarAPI;
import ce.ajneb97.libs.titleapi.TitleAPI;

public class ConditionalEventsAPI {

	private static ConditionalEvents plugin;
	
	public ConditionalEventsAPI(ConditionalEvents plugin) {
		this.plugin = plugin;
	}
	
	public static void sendToServer(Player player,String server) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Connect");
		out.writeUTF(server);
		player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
	}
	
	public static void sendActionBar(Player player,String message,int duration) {
		ActionBarAPI.sendActionBar(player, ChatColor.translateAlternateColorCodes('&', message), duration, plugin);
	}
	
	public static void sendTitle(Player player,int fadeIn,int stay,int fadeOut,String title,String subtitle) {
		TitleAPI.sendTitle(player, fadeIn, stay, fadeOut, title, subtitle);
	}
}

package ce.ajneb97.api;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

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
	
	public static void spawnFirework(Location location,ArrayList<Color> colors,FireworkEffect.Type type,ArrayList<Color> fadeColors,int power) {
		Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
		FireworkMeta fireworkMeta = firework.getFireworkMeta();
		FireworkEffect effect = FireworkEffect.builder().flicker(false)
				.withColor(colors)
				.with(type)
				.withFade(fadeColors)
				.build();
		fireworkMeta.addEffect(effect);
		fireworkMeta.setPower(power);
		firework.setFireworkMeta(fireworkMeta);
	}
}

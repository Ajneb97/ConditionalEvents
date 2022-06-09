package ce.ajneb97.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import ce.ajneb97.libs.centeredmessages.DefaultFontInfo;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class MensajeUtils {

	public static void enviarMensajeJSON(Player jugador,String json) {
		BaseComponent[] base = ComponentSerializer.parse(json);
		jugador.spigot().sendMessage(base);
	}
	
	public static String getMensajeColor(String texto) {
		if(Bukkit.getVersion().contains("1.16") || Bukkit.getVersion().contains("1.17") || Bukkit.getVersion().contains("1.18")
				 || Bukkit.getVersion().contains("1.19")) {
			Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
			Matcher match = pattern.matcher(texto);
			
			while(match.find()) {
				String color = texto.substring(match.start(),match.end());
				texto = texto.replace(color, ChatColor.of(color)+"");
				
				match = pattern.matcher(texto);
			}
		}
		
		return ChatColor.translateAlternateColorCodes('&', texto);
	}
	
	public static String getMensajeCentrado(String message){
		int CENTER_PX = 154;
		int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;
       
        for(char c : message.toCharArray()){
                if(c == '§'){
                        previousCode = true;
                        continue;
                }else if(previousCode == true){
                        previousCode = false;
                        if(c == 'l' || c == 'L'){
                                isBold = true;
                                continue;
                        }else isBold = false;
                }else{
                        DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                        messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                        messagePxSize++;
                }
        }
       
        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while(compensated < toCompensate){
                sb.append(" ");
                compensated += spaceLength;
        }
        return (sb.toString() + message);       
     }
}

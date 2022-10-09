package ce.ajneb97.managers;

import ce.ajneb97.libs.centeredmessages.DefaultFontInfo;
import ce.ajneb97.utils.OtherUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessagesManager {

	private String timeSeconds;
	private String timeMinutes;
	private String timeHours;
	private String timeDays;
	private String prefix;
	private String placeholderAPICooldownReady;
	private String placeholderAPICooldownNameError;


	public String getTimeSeconds() {
		return timeSeconds;
	}

	public void setTimeSeconds(String timeSeconds) {
		this.timeSeconds = timeSeconds;
	}

	public String getTimeMinutes() {
		return timeMinutes;
	}

	public void setTimeMinutes(String timeMinutes) {
		this.timeMinutes = timeMinutes;
	}

	public String getTimeHours() {
		return timeHours;
	}

	public void setTimeHours(String timeHours) {
		this.timeHours = timeHours;
	}

	public String getTimeDays() {
		return timeDays;
	}

	public void setTimeDays(String timeDays) {
		this.timeDays = timeDays;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getPlaceholderAPICooldownReady() {
		return placeholderAPICooldownReady;
	}

	public void setPlaceholderAPICooldownReady(String placeholderAPICooldownReady) {
		this.placeholderAPICooldownReady = placeholderAPICooldownReady;
	}

	public String getPlaceholderAPICooldownNameError() {
		return placeholderAPICooldownNameError;
	}

	public void setPlaceholderAPICooldownNameError(String placeholderAPICooldownNameError) {
		this.placeholderAPICooldownNameError = placeholderAPICooldownNameError;
	}

	public void sendMessage(CommandSender sender, String message, boolean prefix){
		if(!message.isEmpty()){
			if(prefix){
				sender.sendMessage(getColoredMessage(this.prefix+message));
			}else{
				sender.sendMessage(getColoredMessage(message));
			}
		}
	}

	public static String getColoredMessage(String message) {
		if(OtherUtils.isNew()) {
			Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
			Matcher match = pattern.matcher(message);
			
			while(match.find()) {
				String color = message.substring(match.start(),match.end());
				message = message.replace(color, ChatColor.of(color)+"");
				
				match = pattern.matcher(message);
			}
		}

		message = ChatColor.translateAlternateColorCodes('&', message);
		return message;
	}
	
	public static String getCenteredMessage(String message){
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

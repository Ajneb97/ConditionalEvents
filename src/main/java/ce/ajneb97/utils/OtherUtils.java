package ce.ajneb97.utils;
import ce.ajneb97.ConditionalEvents;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Color;

public class OtherUtils {

    public static boolean isChatNew() {
        return ServerVersion.serverVersionGreaterEqualThan(ConditionalEvents.serverVersion, ServerVersion.v1_19_R1);
    }

    public static boolean isNew() {
        return ServerVersion.serverVersionGreaterEqualThan(ConditionalEvents.serverVersion, ServerVersion.v1_16_R1);
    }

    public static boolean isLegacy() {
        return !ServerVersion.serverVersionGreaterEqualThan(ConditionalEvents.serverVersion, ServerVersion.v1_13_R1);
    }

    public static Color getFireworkColorFromName(String colorName) {
        try {
            return (Color) Color.class.getDeclaredField(colorName).get(Color.class);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String fromJsonMessageToNormalMessage(String jsonMessage){
        try{
            BaseComponent[] base = ComponentSerializer.parse(jsonMessage);
            return BaseComponent.toLegacyText(base);
        }catch(Exception e){
            return null;
        }
    }

    public static String getFileExtension(String filePath) {
        int lastIndex = filePath.lastIndexOf(".");
        if (lastIndex > 0 && lastIndex < filePath.length() - 1) {
            return filePath.substring(lastIndex+1);
        } else {
            return "invalid";
        }
    }
}

package ce.ajneb97.utils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Color;

public class OtherUtils {

    public static boolean isNew() {
        if(Bukkit.getVersion().contains("1.16") || Bukkit.getVersion().contains("1.17")
                || Bukkit.getVersion().contains("1.18") || Bukkit.getVersion().contains("1.19")
                || Bukkit.getVersion().contains("1.20")) {
            return true;
        }else {
            return false;
        }
    }

    public static boolean isLegacy() {
        if(Bukkit.getVersion().contains("1.13") || Bukkit.getVersion().contains("1.14") ||
                Bukkit.getVersion().contains("1.15") || isNew()) {
            return false;
        }else {
            return true;
        }
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
        BaseComponent[] base = ComponentSerializer.parse(jsonMessage);
        return BaseComponent.toLegacyText(base);
    }
}

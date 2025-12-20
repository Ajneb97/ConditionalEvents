package ce.ajneb97.utils;

import ce.ajneb97.ConditionalEvents;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Color;
import org.bukkit.entity.Player;

@SuppressWarnings("deprecation")
public class OtherUtils {

    public static boolean isChatNew() {
        ServerVersion serverVersion = ConditionalEvents.serverVersion;
        return serverVersion.serverVersionGreaterEqualThan(serverVersion, ServerVersion.v1_19_R1);
    }

    public static boolean isNew() {
        ServerVersion serverVersion = ConditionalEvents.serverVersion;
        return serverVersion.serverVersionGreaterEqualThan(serverVersion, ServerVersion.v1_16_R1);
    }

    public static boolean isLegacy() {
        ServerVersion serverVersion = ConditionalEvents.serverVersion;
        return !serverVersion.serverVersionGreaterEqualThan(serverVersion, ServerVersion.v1_13_R1);
    }

    public static Color getFireworkColorFromName(String colorName) {
        if (colorName.startsWith("#")) {
            int rgbValue = Integer.parseInt(colorName.substring(1), 16);
            return Color.fromRGB(rgbValue);
        }
        try {
            return (Color) Color.class.getDeclaredField(colorName).get(Color.class);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String fromJsonMessageToNormalMessage(String jsonMessage) {
        try {
            BaseComponent[] base = ComponentSerializer.parse(jsonMessage);
            return BaseComponent.toLegacyText(base);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getFileExtension(String filePath) {
        int lastIndex = filePath.lastIndexOf(".");
        if (lastIndex > 0 && lastIndex < filePath.length() - 1) {
            return filePath.substring(lastIndex + 1);
        } else {
            return "invalid";
        }
    }

    public static String replaceGlobalVariables(String text, Player player, ConditionalEvents plugin) {
        if (player == null) {
            return text;
        }
        text = text.replace("%player%", player.getName());
        if (plugin.getDependencyManager().isPlaceholderAPI()) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }

        return text;
    }
}

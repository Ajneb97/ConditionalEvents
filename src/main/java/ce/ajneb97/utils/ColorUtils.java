package ce.ajneb97.utils;

import org.bukkit.ChatColor;

import javax.annotation.Nullable;

public final class ColorUtils {

    public static String stripColor(@Nullable final String text) {
        if (text == null) return null;
        //noinspection deprecation
        return ChatColor.stripColor(text);
    }

    private ColorUtils() {
    }

}

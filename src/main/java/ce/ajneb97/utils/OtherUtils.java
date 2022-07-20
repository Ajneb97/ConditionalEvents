package ce.ajneb97.utils;

import org.bukkit.Bukkit;
import org.bukkit.Color;

public class OtherUtils {
    private static final boolean isNew;
    private static final boolean isLegacy;

    static {
        isNew = Bukkit.getVersion().contains("1.16") || Bukkit.getVersion().contains("1.17")
                || Bukkit.getVersion().contains("1.18") || Bukkit.getVersion().contains("1.19");
        isLegacy = !(Bukkit.getVersion().contains("1.13") || Bukkit.getVersion().contains("1.14") ||
                    Bukkit.getVersion().contains("1.15") || isNew());
    }

    public static boolean isNew() {
        return isNew;
    }

    public static boolean isLegacy() {
        return isLegacy;
    }

    public static Color getFireworkColorFromName(String colorName) {
        try {
            return (Color) Color.class.getDeclaredField(colorName).get(Color.class);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }
}

package ce.ajneb97.utils;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class OtherUtils {

    public static boolean isNew() {
        if(Bukkit.getVersion().contains("1.16") || Bukkit.getVersion().contains("1.17")
                || Bukkit.getVersion().contains("1.18") || Bukkit.getVersion().contains("1.19")) {
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
}

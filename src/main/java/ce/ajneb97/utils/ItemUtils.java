package ce.ajneb97.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.UUID;

public class ItemUtils {

    @SuppressWarnings("deprecation")
    public static ItemStack createItemFromID(String id) {
        ItemStack item = null;
        if(id.contains(":")){
            String[] sep = id.split(":");
            Material mat = Material.getMaterial(sep[0].toUpperCase());
            item = new ItemStack(mat,1,Short.parseShort(sep[1]));
        }else{
            Material mat = Material.getMaterial(id.toUpperCase());
            item = new ItemStack(mat,1);
        }
        return item;
    }

    @SuppressWarnings("deprecation")
    public static void setSkullData(ItemStack item,String texture,String id,String owner){
        String typeName = item.getType().name();
        if(!typeName.equals("PLAYER_HEAD") && !typeName.equals("SKULL_ITEM")) {
            return;
        }
        SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
        if(owner != null) {
            skullMeta.setOwner(owner);
            item.setItemMeta(skullMeta);
            return;
        }

        if(texture == null && id == null){
            return;
        }

        GameProfile profile = null;
        if(id == null) {
            profile = new GameProfile(UUID.randomUUID(), owner);
        }else {
            profile = new GameProfile(UUID.fromString(id), owner);
        }
        profile.getProperties().put("textures", new Property("textures", texture));

        try {
            Field profileField = skullMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(skullMeta, profile);
        } catch (IllegalArgumentException|NoSuchFieldException|SecurityException|IllegalAccessException error) {
            error.printStackTrace();
        }
        item.setItemMeta(skullMeta);
    }
}

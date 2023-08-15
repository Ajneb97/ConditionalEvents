package ce.ajneb97.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.block.data.BlockData;

import java.lang.reflect.Field;
import java.util.Collection;

public class BlockUtils {

    public static String getHeadTextureData(Block block) {
        if(block == null) {
            return "";
        }

        Material material = block.getType();
        if(material.name().equals("PLAYER_HEAD") || material.name().equals("SKULL") || material.name().equals("PLAYER_WALL_HEAD")) {
            Skull skullBlock = (Skull) block.getState();
            Field profileField;
            try {
                profileField = skullBlock.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                try {
                    GameProfile gameProfile = (GameProfile) profileField.get(skullBlock);
                    if(gameProfile != null && gameProfile.getProperties() != null &&
                            gameProfile.getProperties().containsKey("textures")) {
                        Collection<Property> properties = gameProfile.getProperties().get("textures");
                        for(Property p : properties) {
                            if(p.getName().equals("textures")) {
                                return p.getValue();
                            }
                        }
                    }
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            } catch (NoSuchFieldException | SecurityException e) {
                e.printStackTrace();
            }
        }

        return "";
    }

    public static String getBlockDataStringFromObject(BlockData blockData){
        String text = blockData.getAsString();
        int index = text.indexOf("[");
        if(index == -1){
            return "";
        }
        return text.substring(index+1,text.length()-1);
    }

    public static BlockData getBlockDataFromString(String blockDataString,Material material){
        String minecraftMaterial = material.getKey().getNamespace()+":"+material.getKey().getKey();
        String blockDataText = minecraftMaterial+"["+blockDataString+"]";
        return Bukkit.createBlockData(blockDataText);
    }
}

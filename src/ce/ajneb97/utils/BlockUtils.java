package ce.ajneb97.utils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;


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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (NoSuchFieldException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		return "";
	}
	
	public static void setHeadTextureData(Block block,String texture) {
		if(texture == null || texture.isEmpty()) {
			return;
		}
		
		Skull skullBlock = (Skull) block.getState();
		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", texture));

        try {
            Field profileField = skullBlock.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(skullBlock, profile);
        } catch (IllegalArgumentException|NoSuchFieldException|SecurityException|IllegalAccessException error) {
            error.printStackTrace();
        }
		skullBlock.update();
	}
}

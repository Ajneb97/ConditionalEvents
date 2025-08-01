package ce.ajneb97.utils;

import ce.ajneb97.ConditionalEvents;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.block.data.BlockData;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.Collection;
import java.util.UUID;

public class BlockUtils {

    public static String getHeadTextureData(Block block) {
        if(block == null) {
            return "";
        }

        Material material = block.getType();
        if(material.name().equals("PLAYER_HEAD") || material.name().equals("SKULL") || material.name().equals("PLAYER_WALL_HEAD")) {
            Skull skullBlock = (Skull) block.getState();

            ServerVersion serverVersion = ConditionalEvents.serverVersion;
            if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_21_R1)){
                if(skullBlock.getOwnerProfile() == null){
                    return "";
                }
                PlayerTextures textures = skullBlock.getOwnerProfile().getTextures();
                if(textures.getSkin() == null){
                    return "";
                }

                JsonObject skinJsonObject = new JsonObject();
                skinJsonObject.addProperty("url", textures.getSkin().toString());
                JsonObject texturesJsonObject = new JsonObject();
                texturesJsonObject.add("SKIN", skinJsonObject);
                JsonObject minecraftTexturesJsonObject = new JsonObject();
                minecraftTexturesJsonObject.add("textures", texturesJsonObject);
                return new String(Base64.getEncoder().encode(minecraftTexturesJsonObject.toString().getBytes()));
            }else{
                Field profileField;
                try {
                    profileField = skullBlock.getClass().getDeclaredField("profile");
                    profileField.setAccessible(true);
                    GameProfile gameProfile = (GameProfile) profileField.get(skullBlock);
                    if(gameProfile != null && gameProfile.getProperties() != null &&
                            gameProfile.getProperties().containsKey("textures")) {
                        Collection<Property> properties = gameProfile.getProperties().get("textures");
                        for(Property p : properties) {
                            if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_20_R2)){
                                String pName = (String)p.getClass().getMethod("name").invoke(p);
                                if(pName.equals("textures")){
                                    return (String)p.getClass().getMethod("value").invoke(p);
                                }
                            }else{
                                if(p.getName().equals("textures")) {
                                    return p.getValue();
                                }
                            }
                        }
                    }
                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException
                         | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        }

        return "";
    }

    public static void setHeadTextureData(Block block,String texture,String owner){
        Skull skullBlock = (Skull) block.getState();
        if(OtherUtils.isLegacy()) {
            skullBlock.setSkullType(SkullType.PLAYER);
            skullBlock.setRawData((byte)1);
        }

        if(owner != null){
            skullBlock.setOwner(owner);
            skullBlock.update();
            return;
        }

        ServerVersion serverVersion = ConditionalEvents.serverVersion;
        if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_20_R2)){
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID(),"ce");
            PlayerTextures textures = profile.getTextures();
            URL url;
            try {
                String decoded = new String(Base64.getDecoder().decode(texture));
                String decodedFormatted = decoded.replaceAll("\\s", "");
                JsonObject jsonObject = new Gson().fromJson(decodedFormatted, JsonObject.class);
                String urlText = jsonObject.get("textures").getAsJsonObject().get("SKIN")
                        .getAsJsonObject().get("url").getAsString();

                url = new URL(urlText);
            } catch (Exception error) {
                error.printStackTrace();
                return;
            }
            textures.setSkin(url);
            profile.setTextures(textures);
            skullBlock.setOwnerProfile(profile);
        }else{
            GameProfile profile = new GameProfile(UUID.randomUUID(), "");
            profile.getProperties().put("textures", new Property("textures", texture));
            try {
                Field profileField = skullBlock.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(skullBlock, profile);
            } catch (IllegalArgumentException | NoSuchFieldException | SecurityException | IllegalAccessException error) {
                error.printStackTrace();
            }
        }

        skullBlock.update();
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

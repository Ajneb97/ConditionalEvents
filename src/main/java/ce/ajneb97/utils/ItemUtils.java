package ce.ajneb97.utils;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.api.ConditionalEventsAPI;
import ce.ajneb97.managers.MessagesManager;
import ce.ajneb97.managers.SavedItemsManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

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

    public static ItemStack createItemFromString(String string, Player player){
        ItemStack item = null;
        if(string.startsWith("e")){
            item = ItemUtils.createHead();
            ItemUtils.setSkullData(item,string,null,null);
            return item;
        }

        if(string.startsWith("saved_item:")){
            return ConditionalEventsAPI.getPlugin().getSavedItemsManager().getItem(string.replace("saved_item:",""),player);
        }

        item = ItemUtils.createItemFromID(string);
        return item;
    }

    public static boolean isAir(Material material){
        if(material.name().contains("_AIR") || material.name().equals("AIR")){
            return true;
        }
        return false;
    }

    public static ItemStack createHead(){
        if(OtherUtils.isLegacy()){
            return new ItemStack(Material.valueOf("SKULL_ITEM"),1,(short)3);
        }else{
            return new ItemStack(Material.PLAYER_HEAD);
        }
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
        }

        if(texture != null){
            ServerVersion serverVersion = ConditionalEvents.serverVersion;
            if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_20_R2)){
                UUID uuid = id != null ? UUID.fromString(id) : UUID.randomUUID();
                PlayerProfile profile = Bukkit.createPlayerProfile(uuid,"ce");
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
                skullMeta.setOwnerProfile(profile);
            }else{
                GameProfile profile = null;
                if(id == null) {
                    profile = new GameProfile(UUID.randomUUID(), owner != null ? owner : "");
                }else {
                    profile = new GameProfile(UUID.fromString(id), owner != null ? owner : "");
                }
                profile.getProperties().put("textures", new Property("textures", texture));

                try {
                    Field profileField = skullMeta.getClass().getDeclaredField("profile");
                    profileField.setAccessible(true);
                    profileField.set(skullMeta, profile);
                } catch (IllegalArgumentException | NoSuchFieldException | SecurityException | IllegalAccessException error) {
                    error.printStackTrace();
                }
            }
        }

        item.setItemMeta(skullMeta);
    }

    public static ItemStack getItemFromProperties(String[] properties,Player player){
        String id = null;
        int amount = 1;
        short durability = 0;
        String name = null;
        List<String> lore = new ArrayList<>();
        List<String> flags = new ArrayList<>();
        List<String> enchants = new ArrayList<>();
        int customModelData = 0;

        String skullTexture = null;
        String skullId = null;
        String skullOwner = null;

        boolean hasCustomModelComponentData = false;
        List<String> customModelComponentDataStrings = new ArrayList<>();
        List<String> customModelComponentDataFlags = new ArrayList<>();
        List<String> customModelComponentDataFloats = new ArrayList<>();
        List<String> customModelComponentDataColors = new ArrayList<>();

        String itemModel = null;

        ItemStack savedItem = null;

        for(String property : properties) {
            if(property.startsWith("id:")) {
                id = property.replace("id:", "");
            }else if(property.startsWith("amount:")) {
                amount = Integer.parseInt(property.replace("amount:", ""));
            }else if(property.startsWith("custom_model_data:")) {
                customModelData = Integer.parseInt(property.replace("custom_model_data:", ""));
            }else if(property.startsWith("custom_model_component_data_strings:")){
                String[] splitC = property.replace("custom_model_component_data_strings:", "").split("\\|");
                customModelComponentDataStrings.addAll(Arrays.asList(splitC));
                hasCustomModelComponentData = true;
            }else if(property.startsWith("custom_model_component_data_floats:")){
                String[] splitC = property.replace("custom_model_component_data_floats:", "").split("\\|");
                customModelComponentDataFloats.addAll(Arrays.asList(splitC));
                hasCustomModelComponentData = true;
            }else if(property.startsWith("custom_model_component_data_flags:")){
                String[] splitC = property.replace("custom_model_component_data_flags:", "").split("\\|");
                customModelComponentDataFlags.addAll(Arrays.asList(splitC));
                hasCustomModelComponentData = true;
            }else if(property.startsWith("custom_model_component_data_colors:")){
                String[] splitC = property.replace("custom_model_component_data_colors:", "").split("\\|");
                customModelComponentDataColors.addAll(Arrays.asList(splitC));
                hasCustomModelComponentData = true;
            }else if(property.startsWith("item_model:")){
                itemModel = property.replace("item_model:","");
            }else if(property.startsWith("durability:")) {
                durability = Short.parseShort(property.replace("durability:", ""));
            }else if(property.startsWith("name:")) {
                name = property.replace("name:", "");
            }else if(property.startsWith("lore:")) {
                String[] splitLore = property.replace("lore:", "").split("\\|");
                for(String loreLine : splitLore) {
                    lore.add(loreLine);
                }
            }else if(property.startsWith("enchants:")) {
                String[] splitEnchants = property.replace("enchants:", "").split("\\|");
                for(String enchantLine : splitEnchants) {
                    String[] splitEnchants2 = enchantLine.split("-");
                    enchants.add(splitEnchants2[0]+";"+splitEnchants2[1]);
                }
            }else if(property.startsWith("flags:")) {
                String[] splitFlags = property.replace("flags:", "").split("\\|");
                for(String flagLine : splitFlags) {
                    flags.add(flagLine);
                }
            }else if(property.startsWith("skull_texture:")) {
                skullTexture = property.replace("skull_texture:", "");
            }else if(property.startsWith("skull_owner:")) {
                skullOwner = property.replace("skull_owner:", "");
            }else if(property.startsWith("skull_id")) {
                skullId = property.replace("skull_id:", "");
            }else if(property.startsWith("saved_item")){
                savedItem = ConditionalEventsAPI.getPlugin().getSavedItemsManager().getItem(property.replace("saved_item:", ""),player);
            }
        }

        if(savedItem != null){
            savedItem.setAmount(amount);
            return savedItem;
        }

        ItemStack item = ItemUtils.createItemFromID(id);
        item.setAmount(amount);
        if(durability != 0) {
            item.setDurability(durability);
        }

        //Main Meta
        ItemMeta meta = item.getItemMeta();
        if(name != null) {
            meta.setDisplayName(MessagesManager.getColoredMessage(name));
        }
        if(!lore.isEmpty()) {
            List<String> loreCopy = new ArrayList<>(lore);
            for(int i=0;i<loreCopy.size();i++) {
                loreCopy.set(i, MessagesManager.getColoredMessage(loreCopy.get(i)));
            }
            meta.setLore(loreCopy);
        }

        if(customModelData != 0) {
            meta.setCustomModelData(customModelData);
        }

        ServerVersion serverVersion = ConditionalEvents.serverVersion;
        if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_21_R3) && hasCustomModelComponentData){
            CustomModelDataComponent customModelDataComponent = meta.getCustomModelDataComponent();

            customModelDataComponent.setFlags(customModelComponentDataFlags.stream()
                    .map(Boolean::parseBoolean)
                    .collect(Collectors.toList()));
            customModelDataComponent.setFloats(customModelComponentDataFloats.stream()
                    .map(Float::parseFloat)
                    .collect(Collectors.toList()));
            customModelDataComponent.setColors(customModelComponentDataColors.stream()
                    .map(rgb -> Color.fromRGB(Integer.parseInt(rgb)))
                    .collect(Collectors.toList()));
            customModelDataComponent.setStrings(new ArrayList<>(customModelComponentDataStrings));
            meta.setCustomModelDataComponent(customModelDataComponent);
        }

        if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_21_R3)){
            if(itemModel != null){
                String[] sep = itemModel.split("\\|");
                meta.setItemModel(new NamespacedKey(sep[0],sep[1]));
            }
        }

        if(!enchants.isEmpty()) {
            for(int i=0;i<enchants.size();i++) {
                String[] sep2 = enchants.get(i).split(";");
                String enchantName = sep2[0];
                int enchantLevel = Integer.valueOf(sep2[1]);
                meta.addEnchant(Enchantment.getByName(enchantName), enchantLevel, true);
            }
        }
        if(!flags.isEmpty()) {
            for(int i=0;i<flags.size();i++) {
                meta.addItemFlags(ItemFlag.valueOf(flags.get(i)));
            }
        }
        item.setItemMeta(meta);

        //Other Meta
        ItemUtils.setSkullData(item,skullTexture,skullId,skullOwner);

        return item;
    }
}

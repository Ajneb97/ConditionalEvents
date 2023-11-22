package ce.ajneb97.utils;

import ce.ajneb97.managers.MessagesManager;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
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
            profile = new GameProfile(UUID.randomUUID(), "");
        }else {
            profile = new GameProfile(UUID.fromString(id), "");
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

    public static ItemStack getItemFromProperties(String[] properties){
        String id = null;
        int amount = 1;
        short durability = 0;
        String name = null;
        List<String> lore = new ArrayList<String>();
        List<String> flags = new ArrayList<String>();
        List<String> enchants = new ArrayList<String>();
        int customModelData = 0;
        String skullTexture = null;
        String skullId = null;
        String skullOwner = null;

        for(String property : properties) {
            if(property.startsWith("id:")) {
                id = property.replace("id:", "");
            }else if(property.startsWith("amount:")) {
                amount = Integer.parseInt(property.replace("amount:", ""));
            }else if(property.startsWith("custom_model_data:")) {
                customModelData = Integer.parseInt(property.replace("custom_model_data:", ""));
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
            }
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

package ce.ajneb97.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GlobalVariablesUtils {

    public static String variablePlayer(Player finalPlayer){
        return finalPlayer.getName();
    }

    public static String variablePlayerBlockBelow(Player finalPlayer,String variable){
        int distance = Integer.parseInt(variable.replace("playerblock_below_", ""));
        Location l = finalPlayer.getLocation().clone().add(0, -distance, 0);
        return getBlockTypeInLocation(l);
    }

    public static String variablePlayerBlockAbove(Player finalPlayer,String variable){
        int distance = Integer.parseInt(variable.replace("playerblock_above_", ""));
        Location l = finalPlayer.getLocation().clone().add(0, distance, 0);
        return getBlockTypeInLocation(l);
    }

    public static String variablePlayerBlockInside(Player finalPlayer){
        Location l = finalPlayer.getLocation();
        return getBlockTypeInLocation(l);
    }

    public static String variablePlayerIsOutside(Player finalPlayer){
        Location l = finalPlayer.getLocation();
        Block block = getNextHighestBlock(l);
        if(block == null){
            return "true";
        }else{
            return "false";
        }
    }

    public static String variableRandomPlayer(){
        int random = new Random().nextInt(Bukkit.getOnlinePlayers().size());
        ArrayList<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if(players.size() == 0) {
            return "none";
        }else {
            return players.get(random).getName();
        }
    }

    public static String variableRandomPlayerWorld(String variable){
        String worldName = variable.replace("random_player_", "");
        try {
            World world = Bukkit.getWorld(worldName);
            List<Player> players = world.getPlayers();
            if(players.size() == 0) {
                return "none";
            }else {
                int random = new Random().nextInt(players.size());
                return players.get(random).getName();
            }
        }catch(Exception e) {
            return "none";
        }
    }

    public static String variableRandomMinMax(String variable){
        String variableLR = variable.replace("random_", "");
        String[] variableLRSplit = variableLR.split("_");
        int num1 = Integer.valueOf(variableLRSplit[0]);
        int num2 = Integer.valueOf(variableLRSplit[1]);
        int numFinal = MathUtils.getRandomNumber(num1, num2);
        return numFinal+"";
    }

    public static String variableRandomWorld(String variable){
        String variableLR = variable.replace("randomword_", "");
        String[] variableLRSplit = variableLR.split("-");
        Random r = new Random();
        String word = variableLRSplit[r.nextInt(variableLRSplit.length)];
        return word;
    }

    public static String variablePlayerArmorName(Player finalPlayer,String variable){
        String armorType = variable.replace("playerarmor_name_", "");
        ItemStack item = getArmorItem(finalPlayer,armorType);
        String name = "";
        if(item != null && item.hasItemMeta()){
            ItemMeta meta = item.getItemMeta();
            if(meta.hasDisplayName()){
                name = ChatColor.stripColor(meta.getDisplayName());
            }
        }
        return name;
    }

    public static String variablePlayerArmorType(Player finalPlayer,String variable){
        String armorType = variable.replace("playerarmor_", "");
        ItemStack item = getArmorItem(finalPlayer,armorType);
        String material = "AIR";
        if(item != null) {
            material = item.getType().name();
        }
        return material;
    }

    public static String variableBlockAt(String variable){
        String variableLR = variable.replace("block_at_", "");
        String[] variableLRSplit = variableLR.split("_");
        try {
            int x = Integer.valueOf(variableLRSplit[0]);
            int y = Integer.valueOf(variableLRSplit[1]);
            int z = Integer.valueOf(variableLRSplit[2]);
            String worldName = "";
            for(int i=3;i<variableLRSplit.length;i++) {
                if(i == variableLRSplit.length - 1) {
                    worldName = worldName+variableLRSplit[i];
                }else {
                    worldName = worldName+variableLRSplit[i]+"_";
                }
            }
            World world = Bukkit.getWorld(worldName);
            return world.getBlockAt(x, y, z).getType().name();
        }catch(Exception e) {
            return variable;
        }
    }

    public static String variableIsNearby(Player finalPlayer,String variable){
        String variableLR = variable.replace("is_nearby_", "");
        String[] variableLRSplit = variableLR.split("_");
        try {
            int x = Integer.valueOf(variableLRSplit[0]);
            int y = Integer.valueOf(variableLRSplit[1]);
            int z = Integer.valueOf(variableLRSplit[2]);
            String worldName = "";
            for(int i=3;i<variableLRSplit.length-1;i++) {
                if(i == variableLRSplit.length - 2) {
                    worldName = worldName+variableLRSplit[i];
                }else {
                    worldName = worldName+variableLRSplit[i]+"_";
                }
            }
            World world = Bukkit.getWorld(worldName);
            double radius = Double.valueOf(variableLRSplit[variableLRSplit.length-1]);

            Location l1 = new Location(world,x,y,z);
            Location l2 = finalPlayer.getLocation();
            double distance = l1.distance(l2);

            if(distance <= radius) {
                return "true";
            }else {
                return "false";
            }
        }catch(Exception e) {
            return "false";
        }
    }

    public static String variableWorldTime(String variable){
        String variableLR = variable.replace("world_time_", "");
        World world = Bukkit.getWorld(variableLR);
        return world.getTime()+"";
    }

    public static String variableWorldIsRaining(Player finalPlayer){
        World world = finalPlayer.getWorld();
        return world.hasStorm()+"";
    }

    public static String isNumber(String variable){
        String variableLR = variable.replace("is_number_", "");
        return MathUtils.isParsable(variableLR) ? "true" : "false";
    }


    public static ItemStack getArmorItem(Player player, String armorType){
        ItemStack item = null;
        if(armorType.equals("helmet")) {
            item = player.getEquipment().getHelmet();
        }else if(armorType.equals("chestplate")) {
            item = player.getEquipment().getChestplate();
        }else if(armorType.equals("leggings")) {
            item = player.getEquipment().getLeggings();
        }else if(armorType.equals("boots")){
            item = player.getEquipment().getBoots();
        }
        return item;
    }

    public static Block getNextHighestBlock(Location location){
        int y = location.getBlockY();
        Location locationClone = location.clone();
        for(int i=y+1;i<location.getWorld().getMaxHeight();i++){
            Block nextBlock = locationClone.add(0,1,0).getBlock();
            if(!nextBlock.getType().isAir()){
                return nextBlock;
            }
        }
        return null;
    }

    public static String getBlockTypeInLocation(Location location){
        Block block = location.getBlock();
        String blockType = "AIR";
        if(block != null) {
            blockType = block.getType().name();
        }
        return blockType;
    }
}

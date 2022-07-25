package ce.ajneb97.utils;

import ce.ajneb97.model.StoredVariable;
import me.clip.placeholderapi.PlaceholderAPI;
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

public class VariablesUtils {
    private static final Random rm = new Random();

    public static String replaceAllVariablesInLine(String textLine, ArrayList<StoredVariable> eventVariables,
                                                   Player player, Player target, boolean isPlaceholderAPI){

        //Separate line into only variables
        String auxTextLine = textLine;
        for(int c=0;c<textLine.length();c++) {
            if(textLine.charAt(c) == '%') {
                if(c+1 < textLine.length()) {
                    int lastPos = textLine.indexOf("%", c+1);
                    if(lastPos == -1) {
                        continue;
                    }
                    //Must not be an empty space at start and end.
                    if(textLine.charAt(c+1) == ' ' || textLine.charAt(lastPos-1) == ' ') {
                        continue;
                    }

                    String variable = textLine.substring(c,lastPos+1);
                    //Bukkit.getConsoleSender().sendMessage("found variable: "+variable);
                    auxTextLine = auxTextLine.replace(variable,replaceVariable(variable,player,target,isPlaceholderAPI
                        ,eventVariables));
                    //Bukkit.getConsoleSender().sendMessage("fixed variable: "+auxTextLine);
                    c = lastPos;
                }
            }
        }

        //Event variables
        for(StoredVariable variable : eventVariables){
            auxTextLine = auxTextLine.replace(variable.getName(), variable.getValue());
        }

        return auxTextLine;
    }

    public static String replaceEventVariablesPost(String variable,ArrayList<StoredVariable> eventVariables){
        //PLAYER_COMMAND:
        if(variable.startsWith("%args_substring_")){
            //%args_substring_<param1>_<param2>%
            String variableLR = variable.replace("args_substring_", "").replace("%", "");
            String[] variableLRSplit = variableLR.split("-");
            String param1 = variableLRSplit[0];
            String param2 = variableLRSplit[1];

            StringBuilder finalSubstring = new StringBuilder();
            boolean started = false;
            for(StoredVariable storedVariable : eventVariables){
                String name = storedVariable.getName();
                String value = storedVariable.getValue();
                if(name.equals("%arg_"+param1+"%")){
                    started = true;
                }
                if(started){
                    if(name.equals("%arg_"+param2+"%")){
                        finalSubstring.append(value);
                        started = false;
                    }else{
                        finalSubstring.append(value).append(' ');
                    }
                }
            }
            return finalSubstring.toString();
        }
        return variable;
    }

    public static String replaceVariable(String variable,Player originalPlayer, Player target, boolean isPlaceholderAPI
        ,ArrayList<StoredVariable> eventVariables){
        Player finalPlayer = originalPlayer;
        if(variable.startsWith("%target:") && target != null){
            finalPlayer = target;
            variable = variable.replace("target:","");
        }

        //Global variables
        if(variable.equals("%player%")){
            return finalPlayer.getName();
        }else if(variable.equals("%block_below%")){
            Location l = finalPlayer.getLocation().clone().add(0, -1, 0);
            return getBlockTypeInLocation(l);
        }else if(variable.equals("%block_inside%")){
            Location l = finalPlayer.getLocation();
            return getBlockTypeInLocation(l);
        }else if(variable.equals("%random_player%")) {
            int random = rm.nextInt(Bukkit.getOnlinePlayers().size());
            ArrayList<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
            if(players.isEmpty()) {
                return "none";
            }else {
                return players.get(random).getName();
            }
        }else if(variable.startsWith("%random_player_")) {
            // %random_player_<world>%
            String worldName = variable.replace("%random_player_", "").replace("%", "");
            try {
                World world = Bukkit.getWorld(worldName);
                List<Player> players = world.getPlayers();
                if(players.isEmpty()) {
                    return "none";
                }else {
                    int random = rm.nextInt(players.size());
                    return players.get(random).getName();
                }
            }catch(Exception e) {
                return "none";
            }
        }else if(variable.startsWith("%random_")) {
            // %random_min-max%
            String variableLR = variable.replace("random_", "").replace("%", "");
            String[] variableLRSplit = variableLR.split("-");
            int num1 = Integer.parseInt(variableLRSplit[0]);
            int num2 = Integer.parseInt(variableLRSplit[1]);
            int numFinal = MathUtils.getRandomNumber(num1, num2);
            return Integer.toString(numFinal);
        }else if(variable.startsWith("%armor_")) {
            // %armor_<type>%
            String armorType = variable.replace("%armor_", "").replace("%", "");
            ItemStack item = getArmorItem(finalPlayer,armorType);
            String material = "AIR";
            if(item != null) {
                material = item.getType().name();
            }
            return material;
        }else if(variable.startsWith("%armor_name_")) {
            // %armor_name_<type>%
            String armorType = variable.replace("%armor_name_", "").replace("%", "");
            ItemStack item = getArmorItem(finalPlayer,armorType);
            String name = "";
            if(item.hasItemMeta()){
                ItemMeta meta = item.getItemMeta();
                if(meta.hasDisplayName()){
                    name = ChatColor.stripColor(meta.getDisplayName());
                }
            }
            return name;
        }else if(variable.startsWith("%block_at_")) {
            // %block_at_x_y_z_world%
            String variableLR = variable.replace("%block_at_", "").replace("%", "");
            String[] variableLRSplit = variableLR.split("_");
            try {
                int x = Integer.parseInt(variableLRSplit[0]);
                int y = Integer.parseInt(variableLRSplit[1]);
                int z = Integer.parseInt(variableLRSplit[2]);
                StringBuilder worldName = new StringBuilder();
                for(int i=3;i<variableLRSplit.length;i++) {
                    if(i == variableLRSplit.length - 1) {
                        worldName.append(variableLRSplit[i]);
                    }else {
                        worldName.append(worldName+variableLRSplit[i]).append('_');
                    }
                }
                World world = Bukkit.getWorld(worldName.toString());
                return world.getBlockAt(x, y, z).getType().name();
            }catch(Exception e) {
                return variable;
            }
        }else if(variable.startsWith("%is_nearby_")) {
            // %is_nearby_x_y_z_world_radius%
            String variableLR = variable.replace("%is_nearby_", "").replace("%", "");
            String[] variableLRSplit = variableLR.split("_");
            try {
                int x = Integer.parseInt(variableLRSplit[0]);
                int y = Integer.parseInt(variableLRSplit[1]);
                int z = Integer.parseInt(variableLRSplit[2]);
                StringBuilder worldName = new StringBuilder();
                for(int i=3;i<variableLRSplit.length-1;i++) {
                    if(i == variableLRSplit.length - 2) {
                        worldName.append(variableLRSplit[i]);
                    }else {
                        worldName.append(worldName+variableLRSplit[i]).append('_');
                    }
                }
                World world = Bukkit.getWorld(worldName.toString());
                double radius = Double.parseDouble(variableLRSplit[variableLRSplit.length-1]);

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
        }else if(variable.startsWith("%world_time_")) {
            // %world_time_<world>%
            String variableLR = variable.replace("%world_time_", "").replace("%", "");
            World world = Bukkit.getWorld(variableLR);
            return Long.toString(world.getTime());
        }else if(variable.equals("%empty%")) {
            return "";
        }

        //Post variables
        variable = replaceEventVariablesPost(variable,eventVariables);

        //PlaceholderAPI variables
        if(isPlaceholderAPI){
            variable = PlaceholderAPI.setPlaceholders(finalPlayer,variable);
        }

        return variable;
    }

    private static ItemStack getArmorItem(Player player,String armorType){
        ItemStack item = null;
        if(armorType.equals("helmet")) {
            item = player.getEquipment().getHelmet();
        }else if(armorType.equals("chestplate")) {
            item = player.getEquipment().getChestplate();
        }else if(armorType.equals("leggings")) {
            item = player.getEquipment().getLeggings();
        }else {
            item = player.getEquipment().getBoots();
        }
        return item;
    }

    private static String getBlockTypeInLocation(Location location){
        Block block = location.getBlock();
        String blockType = "AIR";
        if(block != null) {
            blockType = block.getType().name();
        }
        return blockType;
    }
}

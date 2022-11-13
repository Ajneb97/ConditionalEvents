package ce.ajneb97.utils;

import ce.ajneb97.model.EventType;
import ce.ajneb97.model.StoredVariable;
import ce.ajneb97.model.internal.PostEventVariableResult;
import ce.ajneb97.model.internal.VariablesProperties;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VariablesUtils {

    public static String replaceAllVariablesInLine(String textLine,VariablesProperties variablesProperties,boolean smallVariables){
        String auxTextLine = textLine;

        char startChar = '%';
        char endChar = '%';
        if(smallVariables){
            startChar = '{';
            endChar = '}';
        }

        //For special variables like ParseOther
        int currentSmallVariableCount = 0;
        boolean isParseOther = false;
        if(textLine.startsWith("%parseother_")){
            isParseOther = true;
        }

        for(int c=0;c<textLine.length();c++) {
            if(textLine.charAt(c) == startChar) {
                if(c+1 < textLine.length()) {
                    int lastPos = textLine.indexOf(endChar, c+1);
                    if(lastPos == -1) {
                        continue;
                    }

                    //Must not be an empty space at start and end.
                    if(textLine.charAt(c+1) == ' ' || textLine.charAt(lastPos-1) == ' ') {
                        continue;
                    }

                    String bigVariable = textLine.substring(c,lastPos+1);
                    String auxBigVariable = null;
                    if(!smallVariables){
                        auxBigVariable = replaceAllVariablesInLine(bigVariable,variablesProperties,true);
                    }else{
                        auxBigVariable = bigVariable;
                    }

                    if(smallVariables && isParseOther && currentSmallVariableCount == 1){
                        continue;
                    }

                    String auxBigVariableWithoutChars = auxBigVariable.replace(startChar+"","").replace(endChar+"","");

                    String replaceVariableResult = replaceVariable(auxBigVariableWithoutChars,variablesProperties,smallVariables);

                    if(replaceVariableResult.equals(auxBigVariableWithoutChars)){
                        auxTextLine = auxTextLine.replace(bigVariable,startChar+replaceVariableResult+endChar);
                    }else{
                        if(isParseOther && smallVariables && currentSmallVariableCount == 0){
                            if(!replaceVariableResult.startsWith(startChar+"")){
                                replaceVariableResult = startChar+replaceVariableResult+endChar;
                            }
                        }
                        auxTextLine = auxTextLine.replace(bigVariable,replaceVariableResult);
                    }

                    c = lastPos;
                    currentSmallVariableCount++;
                }
            }
        }

        return auxTextLine;
    }

    //Post-Event variables
    public static PostEventVariableResult replaceEventVariablesPost(String variable, VariablesProperties variablesProperties){
        EventType eventType = variablesProperties.getEvent().getEventType();
        if(eventType.equals(EventType.PLAYER_COMMAND) || eventType.equals(EventType.CONSOLE_COMMAND)){
            return replaceCommandEventsVariables(variable,variablesProperties);
        }else if(eventType.name().startsWith("BLOCK_")){
            return replaceBlockEventsVariables(variable,variablesProperties,eventType);
        }

        return PostEventVariableResult.noReplaced();
    }

    public static PostEventVariableResult replaceCommandEventsVariables(String variable,VariablesProperties variablesProperties){
        if(variable.startsWith("args_substring_")){
            //%args_substring_<param1>-<param2>%
            String variableLR = variable.replace("args_substring_", "");
            String[] variableLRSplit = variableLR.split("-");
            String param1 = variableLRSplit[0];
            String param2 = variableLRSplit[1];

            String finalSubstring = "";
            boolean started = false;
            for(StoredVariable storedVariable : variablesProperties.getEventVariables()){
                String name = storedVariable.getName();
                String value = storedVariable.getValue();
                if(name.equals("%arg_"+param1+"%")){
                    started = true;
                }
                if(started){
                    if(name.equals("%arg_"+param2+"%")){
                        finalSubstring = finalSubstring+value;
                        started = false;
                    }else{
                        finalSubstring = finalSubstring+value+" ";
                    }
                }
            }
            return PostEventVariableResult.replaced(finalSubstring);
        }
        return PostEventVariableResult.noReplaced();
    }

    public static PostEventVariableResult replaceBlockEventsVariables(String variable,VariablesProperties variablesProperties,EventType eventType){
        Event minecraftEvent = variablesProperties.getMinecraftEvent();
        if(minecraftEvent == null){
            return PostEventVariableResult.noReplaced();
        }

        Block block = null;
        if(eventType.equals(EventType.BLOCK_INTERACT)){
            block = ((PlayerInteractEvent) minecraftEvent).getClickedBlock();
        }else{
            block = ((BlockEvent) minecraftEvent).getBlock();
        }

        if(block != null){
            if(variable.startsWith("block_below_")){
                // %block_below_<distance>%
                int distance = Integer.parseInt(variable.replace("block_below_", ""));
                Location l = block.getLocation().clone().add(0, -distance, 0);
                return PostEventVariableResult.replaced(getBlockTypeInLocation(l));
            }else if(variable.startsWith("block_above_")){
                // %block_above_<distance>%
                int distance = Integer.parseInt(variable.replace("block_above_", ""));
                Location l = block.getLocation().clone().add(0, distance, 0);
                return PostEventVariableResult.replaced(getBlockTypeInLocation(l));
            }
        }

        return PostEventVariableResult.noReplaced();
    }

    //Global ConditionalEvents variables
    public static String replaceVariable(String variable,VariablesProperties variablesProperties,boolean smallVariable){
        Player finalPlayer = variablesProperties.getPlayer();
        Player target = variablesProperties.getTarget();
        if(variable.startsWith("target:") && target != null){
            finalPlayer = target;
            variable = variable.replace("target:","");
        }

        //Event variables
        ArrayList<StoredVariable> eventVariables = variablesProperties.getEventVariables();
        for(StoredVariable storedVariable : eventVariables){
            if(storedVariable.getValue() != null && variable.equals(storedVariable.getName().replace("%",""))){
                return storedVariable.getValue();
            }
        }

        //Global variables
        if(variable.equals("player")){
            return finalPlayer.getName();
        }else if(variable.startsWith("playerblock_below_")){
            // %playerblock_below_<distance>%
            int distance = Integer.parseInt(variable.replace("playerblock_below_", ""));
            Location l = finalPlayer.getLocation().clone().add(0, -distance, 0);
            return getBlockTypeInLocation(l);
        }else if(variable.startsWith("%playerblock_above_")){
            // %playerblock_above_<distance>%
            int distance = Integer.parseInt(variable.replace("playerblock_above_", ""));
            Location l = finalPlayer.getLocation().clone().add(0, distance, 0);
            return getBlockTypeInLocation(l);
        }else if(variable.equals("playerblock_inside")){
            // %playerblock_inside%
            Location l = finalPlayer.getLocation();
            return getBlockTypeInLocation(l);
        }else if(variable.equals("random_player")) {
            int random = new Random().nextInt(Bukkit.getOnlinePlayers().size());
            ArrayList<Player> players = new ArrayList<Player>(Bukkit.getOnlinePlayers());
            if(players.size() == 0) {
                return "none";
            }else {
                return players.get(random).getName();
            }
        }else if(variable.startsWith("random_player_")) {
            // %random_player_<world>%
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
        }else if(variable.startsWith("random_")) {
            // %random_min-max%
            String variableLR = variable.replace("random_", "");
            String[] variableLRSplit = variableLR.split("-");
            int num1 = Integer.valueOf(variableLRSplit[0]);
            int num2 = Integer.valueOf(variableLRSplit[1]);
            int numFinal = MathUtils.getRandomNumber(num1, num2);
            return numFinal+"";
        }else if(variable.startsWith("playerarmor_name_")) {
            // %playerarmor_name_<type>%
            String armorType = variable.replace("playerarmor_name_", "");
            ItemStack item = getArmorItem(finalPlayer,armorType);
            String name = "";
            if(item.hasItemMeta()){
                ItemMeta meta = item.getItemMeta();
                if(meta.hasDisplayName()){
                    name = ChatColor.stripColor(meta.getDisplayName());
                }
            }
            return name;
        }else if(variable.startsWith("playerarmor_")) {
            // %playerarmor_<type>%
            String armorType = variable.replace("playerarmor_", "");
            ItemStack item = getArmorItem(finalPlayer,armorType);
            String material = "AIR";
            if(item != null) {
                material = item.getType().name();
            }
            return material;
        }else if(variable.startsWith("block_at_")) {
            // %block_at_x_y_z_world%
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
        }else if(variable.startsWith("is_nearby_")) {
            // %is_nearby_x_y_z_world_radius%
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
        }else if(variable.startsWith("world_time_")) {
            // %world_time_<world>%
            String variableLR = variable.replace("world_time_", "");
            World world = Bukkit.getWorld(variableLR);
            return world.getTime()+"";
        }else if(variable.equals("empty")) {
            return "";
        }

        //Post-Event variables
        PostEventVariableResult result = replaceEventVariablesPost(variable,variablesProperties);
        if(result.isReplaced()){
            return result.getVariable();
        }

        //PlaceholderAPI variables
        if(variablesProperties.isPlaceholderAPI()){
            String variableBefore = variable;
            variable = PlaceholderAPI.setPlaceholders(finalPlayer,"%"+variable+"%");
            if(("%"+variableBefore+"%").equals(variable)){
                //Was not replaced
                if(smallVariable){
                    variable = "{"+variableBefore+"}";
                }else{
                    variable = "%"+variableBefore+"%";
                }
            }
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
        }else if(armorType.equals("boots")){
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

package ce.ajneb97.utils;

import ce.ajneb97.model.EventType;
import ce.ajneb97.model.StoredVariable;
import ce.ajneb97.model.internal.PostEventVariableResult;
import ce.ajneb97.model.internal.VariablesProperties;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariablesUtilsExperimental {

    private static final Pattern subVariablesPattern = Pattern.compile("\\{([^{}]+)}");


    public static String replaceAllVariablesInLine(String textLine,VariablesProperties variablesProperties){
        StringBuilder newText = new StringBuilder();

        int pos = 0;
        while(pos < textLine.length()){
            char posChar = textLine.charAt(pos);
            if(posChar == '%'){
                int indexLast = textLine.indexOf('%',pos+1);
                if(indexLast != -1){
                    String variable = textLine.substring(pos,indexLast+1);
                    String replacedSubVariables = replaceSubVariables(variable,variablesProperties);
                    String finalReplaced = manageVariableReplacement(replacedSubVariables.substring(1,replacedSubVariables.length()-1),variablesProperties,false);
                    pos = indexLast;
                    if(variable.equals(finalReplaced)){
                        pos--;
                        finalReplaced = finalReplaced.substring(0,finalReplaced.length()-1);
                    }

                    pos++;
                    newText.append(finalReplaced);
                    continue;
                }
            }
            newText.append(posChar);
            pos++;
        }

        return newText.toString();
    }

    private static String replaceSubVariables(String input,VariablesProperties variablesProperties) {
        boolean parseOther = input.contains("parseother_");
        Matcher matcher = subVariablesPattern.matcher(input);

        StringBuffer buffer = new StringBuffer();
        int finds = 0;
        while (matcher.find()) {
            if(parseOther && finds >= 1){
                break;
            }
            String variable = matcher.group(1);
            String replacement = manageVariableReplacement(variable,variablesProperties,true);
            if(parseOther && finds == 0){
                replacement = "{"+replacement+"}";
            }
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));

            finds++;
        }
        matcher.appendTail(buffer);

        String replaced = buffer.toString();
        if (!parseOther && !replaced.equals(input)) {
            return replaceSubVariables(replaced,variablesProperties);
        }
        return replaced;
    }

    //Post-Event variables
    private static PostEventVariableResult replaceEventVariablesPost(String variable, VariablesProperties variablesProperties){
        EventType eventType = variablesProperties.getEvent().getEventType();
        if(eventType.equals(EventType.PLAYER_COMMAND) || eventType.equals(EventType.CONSOLE_COMMAND)){
            return replaceCommandEventsVariables(variable,variablesProperties);
        }else if(eventType.name().startsWith("BLOCK_")){
            return replaceBlockEventsVariables(variable,variablesProperties,eventType);
        }

        return PostEventVariableResult.noReplaced();
    }

    private static PostEventVariableResult replaceCommandEventsVariables(String variable,VariablesProperties variablesProperties){
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

    private static PostEventVariableResult replaceBlockEventsVariables(String variable,VariablesProperties variablesProperties,EventType eventType){
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
                return PostEventVariableResult.replaced(GlobalVariablesUtils.getBlockTypeInLocation(l));
            }else if(variable.startsWith("block_above_")){
                // %block_above_<distance>%
                int distance = Integer.parseInt(variable.replace("block_above_", ""));
                Location l = block.getLocation().clone().add(0, distance, 0);
                return PostEventVariableResult.replaced(GlobalVariablesUtils.getBlockTypeInLocation(l));
            }
        }

        return PostEventVariableResult.noReplaced();
    }

    //Global ConditionalEvents variables
    private static String manageVariableReplacement(String variable,VariablesProperties variablesProperties,boolean smallVariable){
        Player finalPlayer = variablesProperties.getPlayer();
        Player target = variablesProperties.getTarget();
        Player to = variablesProperties.getToTarget();
        if(variable.startsWith("target:") && target != null){
            finalPlayer = target;
            variable = variable.replace("target:","");
        }else if(variable.startsWith("to:") && to != null){
            finalPlayer = to;
            variable = variable.replace("to:","");
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
            // %player%
            return GlobalVariablesUtils.variablePlayer(finalPlayer);
        }else if(variable.startsWith("playerblock_below_")){
            // %playerblock_below_<distance>%
            return GlobalVariablesUtils.variablePlayerBlockBelow(finalPlayer,variable);
        }else if(variable.startsWith("playerblock_above_")){
            // %playerblock_above_<distance>%
            return GlobalVariablesUtils.variablePlayerBlockAbove(finalPlayer,variable);
        }else if(variable.equals("playerblock_inside")){
            // %playerblock_inside%
            return GlobalVariablesUtils.variablePlayerBlockInside(finalPlayer);
        }else if(variable.equals("player_is_outside")){
            // %player_is_outside%
            return GlobalVariablesUtils.variablePlayerIsOutside(finalPlayer);
        }else if(variable.equals("random_player")) {
            // %random_player%
            return GlobalVariablesUtils.variableRandomPlayer();
        }else if(variable.startsWith("random_player_")) {
            // %random_player_<world>%
            return GlobalVariablesUtils.variableRandomPlayerWorld(variable);
        }else if(variable.startsWith("random_")) {
            // %random_min_max%
            return GlobalVariablesUtils.variableRandomMinMax(variable);
        }else if(variable.startsWith("randomword_")) {
            // %randomword_word1-word2-word3%
            return GlobalVariablesUtils.variableRandomWorld(variable);
        }else if(variable.startsWith("playerarmor_name_")) {
            // %playerarmor_name_<type>%
            return GlobalVariablesUtils.variablePlayerArmorName(finalPlayer,variable);
        }else if(variable.startsWith("playerarmor_")) {
            // %playerarmor_<type>%
            return GlobalVariablesUtils.variablePlayerArmorType(finalPlayer,variable);
        }else if(variable.startsWith("block_at_")) {
            // %block_at_x_y_z_world%
            return GlobalVariablesUtils.variableBlockAt(variable);
        }else if(variable.startsWith("block_data_at_")) {
            // %block_data_at_x_y_z_world%
            return GlobalVariablesUtils.variableBlockDataAt(variable);
        }else if(variable.startsWith("is_nearby_")) {
            // %is_nearby_x_y_z_world_radius%
            return GlobalVariablesUtils.variableIsNearby(finalPlayer,variable);
        }else if(variable.startsWith("world_time_")) {
            // %world_time_<world>%
            return GlobalVariablesUtils.variableWorldTime(variable);
        }else if(variable.equals("world_is_raining")){
            // %world_is_raining%
            return GlobalVariablesUtils.variableWorldIsRaining(finalPlayer);
        }else if(variable.startsWith("is_number_")) {
            // %is_number_<variable>%
            return GlobalVariablesUtils.isNumber(variable);
        }
        else if(variable.equals("empty")) {
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
            if(!("%"+variableBefore+"%").equals(variable)){
                //Was replaced
                return variable;
            }else{
                variable = variableBefore;
            }
        }

        if(smallVariable){
            return "{"+variable+"}";
        }else{
            return "%"+variable+"%";
        }
    }
}

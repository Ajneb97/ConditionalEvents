package ce.ajneb97.managers;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.model.CEEvent;
import ce.ajneb97.model.ConditionalType;
import ce.ajneb97.model.EventType;
import ce.ajneb97.model.StoredVariable;
import ce.ajneb97.model.internal.CheckConditionsResult;
import ce.ajneb97.model.internal.ConditionEvent;
import ce.ajneb97.model.internal.ExecutedEvent;
import ce.ajneb97.utils.MathUtils;
import ce.ajneb97.utils.TimeUtils;
import ce.ajneb97.utils.VariablesUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EventsManager {

    private ConditionalEvents plugin;
    private ArrayList<CEEvent> events;
    public EventsManager(ConditionalEvents plugin){
        this.plugin = plugin;
        this.events = new ArrayList<>();
    }

    public void setEvents(ArrayList<CEEvent> events) {
        this.events = events;
    }

    public ArrayList<CEEvent> getEvents() {
        return events;
    }

    public CEEvent getEvent(String name){
        for(CEEvent event : events){
            if(event.getName().equals(name)){
                return event;
            }
        }
        return null;
    }

    public void checkEvent(ConditionEvent conditionEvent){
        ArrayList<CEEvent> validEvents = getValidEvents(conditionEvent.getEventType());
        for(CEEvent event : validEvents){
            checkSingularEvent(conditionEvent,event,false);
        }
    }

    public void checkSingularEvent(ConditionEvent conditionEvent,CEEvent event,boolean isAsync){
        Player player = conditionEvent.getPlayer();
        boolean isPlaceholderAPI = plugin.getDependencyManager().isPlaceholderAPI();
        conditionEvent.setCurrentEvent(event);

        //Ignore with permission
        if(player != null && event.getIgnoreWithPermission() != null && player.hasPermission(event.getIgnoreWithPermission())){
            return;
        }

        //Check condition list
        CheckConditionsResult conditionsResult = checkConditions(conditionEvent,isPlaceholderAPI);
        if(!conditionsResult.isConditionsAccomplished()){
            return;
        }

        PlayerManager playerManager = plugin.getPlayerManager();
        MessagesManager messagesManager = plugin.getMessagesManager();

        if(player != null){
            //Check One time
            if(event.isOneTime()){
                boolean isOneTime = playerManager.getEventOneTime(event.getName(),player);
                if(isOneTime){
                    String errorMessage = event.getOneTimeErrorMessage();
                    if(errorMessage != null){
                        player.sendMessage(MessagesManager.getColoredMessage(errorMessage));
                    }
                    return;
                }
            }

            //Check Cooldown
            if(event.getCooldown() != 0){
                long eventCooldownMillis = playerManager.getEventCooldown(event.getName(),player)+(event.getCooldown()*1000);
                long currentTimeMillis = System.currentTimeMillis();
                if(eventCooldownMillis > currentTimeMillis){
                    String timeString = TimeUtils.getTime((eventCooldownMillis-currentTimeMillis)/1000,messagesManager);
                    String errorMessage = event.getCooldownErrorMessage();
                    if(errorMessage != null){
                        player.sendMessage(MessagesManager.getColoredMessage(errorMessage.replace("%time%",timeString)));
                    }
                    return;
                }
            }
        }


        if(player != null){
            //Set One Time
            if(event.isOneTime()){
                playerManager.setEventOneTime(event.getName(),player);
            }

            //Set Cooldown
            if(event.getCooldown() != 0){
                playerManager.setEventCooldown(event.getName(),player);
            }
        }

        //Execute actions
        ExecutedEvent executedEvent = new ExecutedEvent(player, conditionEvent.getEventVariables(), event,
               conditionsResult.getExecuteActionGroup(), conditionEvent.getMinecraftEvent(), conditionEvent.getTarget(), plugin);
        executedEvent.executeActions(isPlaceholderAPI,isAsync);
    }

    public ArrayList<CEEvent> getValidEvents(EventType eventType){
        ArrayList<CEEvent> validEvents = new ArrayList<>();
        for(CEEvent event : events){
            if(event.getEventType().equals(eventType) && event.isEnabled()){
                validEvents.add(event);
            }
        }
        return validEvents;
    }

    private CheckConditionsResult checkConditions(ConditionEvent conditionEvent, boolean isPlaceholderAPI){
        List<String> conditions = conditionEvent.getCurrentEvent().getConditions();
        String eventName = conditionEvent.getCurrentEvent().getName();
        Player player = conditionEvent.getPlayer();
        Player target = conditionEvent.getTarget();
        DebugManager debugManager = plugin.getDebugManager();

        //Replace variables
        /*
        Before:
        - %player_world% == plotworld
        - %statistic_jump% >= %player_level%+5
        - %player_has_permission_<something>% == yes or %player_is_op% == yes
        */
        List<String> replacedConditions = new ArrayList<>();
        for(String conditionLine : conditions){
            //Bukkit.getConsoleSender().sendMessage("condicion antes: "+conditionLine);
            //Event variables
            ArrayList<StoredVariable> storedVariables = conditionEvent.getEventVariables();

            conditionLine =  VariablesUtils.replaceAllVariablesInLine(conditionLine,storedVariables,player
                ,target,isPlaceholderAPI);

            //Bukkit.getConsoleSender().sendMessage("condicion despues: "+conditionLine);
            replacedConditions.add(conditionLine);
        }

        //Check condition lines
        /*
        Now:
        - world == plotworld
        - 1060 >= 10+5
        - yes == yes or no == yes
        */
        for(int i=0;i<replacedConditions.size();i++){
            String conditionLine = replacedConditions.get(i);
            boolean approvedLine = false;
            String executedActionGroup = null;
            if(conditionLine.contains(" execute ")){
                String[] sep = conditionLine.split(" execute ");
                conditionLine = sep[0];
                executedActionGroup = sep[1];
            }

            String[] orConditions = conditionLine.split(" or ");
            for(String miniCondition : orConditions){
                //If a miniCondition is NOT accomplished, it will check the next.
                //If a miniCondition IS accomplished, it will finish this cycle.
                //If this cycle is finished with NO accomplished miniConditions, the
                // whole condition was not satisfied so the checkConditions method returns FALSE.

                for(ConditionalType conditionalType : ConditionalType.values()){
                    String textToFind = " "+conditionalType.getText()+" ";
                    if(miniCondition.contains(textToFind)){
                        int textToFindIndex = miniCondition.indexOf(textToFind);
                        String arg1 = miniCondition.substring(0, textToFindIndex);
                        String arg2 = miniCondition.substring(textToFindIndex+conditionalType.getText().length()+2);
                        String firstArg = MathUtils.calculate(arg1);
                        String secondArg = MathUtils.calculate(arg2);
                        //Bukkit.getConsoleSender().sendMessage("check1: "+firstArg);
                        //Bukkit.getConsoleSender().sendMessage("check2: "+secondArg);
                        String firstArgLower = firstArg.toLowerCase(Locale.ROOT);
                        String secondArgLower = secondArg.toLowerCase(Locale.ROOT);
                        double firstArgNum = 0;
                        double secondArgNum = 0;
                        try{
                            firstArgNum = Double.parseDouble(firstArg);
                            secondArgNum = Double.parseDouble(secondArg);
                        }catch(NumberFormatException e){

                        }

                        switch(conditionalType){
                            case EQUALS:
                            case EQUALS_LEGACY:
                                if(firstArg.equals(secondArg)) approvedLine = true;break;
                            case NOT_EQUALS:
                            case NOT_EQUALS_LEGACY:
                                if(!firstArg.equals(secondArg)) approvedLine = true;break;
                            case EQUALS_IGNORE_CASE:
                                if(firstArg.equalsIgnoreCase(secondArg)) approvedLine = true;break;
                            case NOT_EQUALS_IGNORE_CASE:
                                if(!firstArg.equalsIgnoreCase(secondArg)) approvedLine = true;break;
                            case STARTS_WITH:
                                if(firstArgLower.startsWith(secondArgLower)) approvedLine = true;break;
                            case NOT_STARTS_WITH:
                                if(!firstArgLower.startsWith(secondArgLower)) approvedLine = true;break;
                            case CONTAINS:
                                if(firstArgLower.contains(secondArgLower)) approvedLine = true;break;
                            case NOT_CONTAINS:
                                if(!firstArgLower.contains(secondArgLower)) approvedLine = true;break;
                            case GREATER:
                                if(firstArgNum > secondArgNum) approvedLine = true;break;
                            case GREATER_EQUALS:
                                if(firstArgNum >= secondArgNum) approvedLine = true;break;
                            case LOWER:
                                if(firstArgNum < secondArgNum) approvedLine = true;break;
                            case LOWER_EQUALS:
                                if(firstArgNum <= secondArgNum) approvedLine = true;break;
                        }
                    }
                    if(approvedLine){
                        break;
                    }
                }

                if(approvedLine){
                    break;
                }
            }
            //Bukkit.getConsoleSender().sendMessage(conditionLine+" -> ¿aprobada? "+approvedLine);
            debugManager.sendConditionMessage(eventName,conditionLine,approvedLine,conditionEvent.getPlayer(),i==0);
            //If approvedLine is false, the conditions are not satisfied. Returns FALSE.
            //If approvedLine is false, but there is an executedActionGroup, it will continue with the next
            // condition line.
            //If approvedLine is true, it will continue with the next condition line.
            //If approvedLine is true and there is an executedActionGroup selected, the method
            // will return this action group.
            if(!approvedLine){
                if(executedActionGroup == null){
                    return new CheckConditionsResult(false,null);
                }
                continue;
            }
            if(approvedLine && executedActionGroup != null){
                debugManager.sendActionsMessage(eventName,executedActionGroup,conditionEvent.getPlayer());
                return new CheckConditionsResult(true,executedActionGroup);
            }
        }

        //If all condition lines are approved, the event is executed with the default actions.
        debugManager.sendActionsMessage(eventName,"default",conditionEvent.getPlayer());
        return new CheckConditionsResult(true,null);
    }
}

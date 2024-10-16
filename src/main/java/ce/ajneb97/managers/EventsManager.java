package ce.ajneb97.managers;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.model.CEEvent;
import ce.ajneb97.model.ConditionalType;
import ce.ajneb97.model.EventType;
import ce.ajneb97.model.StoredVariable;
import ce.ajneb97.model.internal.*;
import ce.ajneb97.utils.MathUtils;
import ce.ajneb97.utils.TimeUtils;
import ce.ajneb97.utils.VariablesUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.List;

public class EventsManager {

    private ConditionalEvents plugin;
    private ArrayList<CEEvent> events;
    public EventsManager(ConditionalEvents plugin){
        this.plugin = plugin;
        this.events = new ArrayList<CEEvent>();
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
            checkSingularEvent(conditionEvent,event);
        }
    }

    public void checkSingularEvent(ConditionEvent conditionEvent,CEEvent event){
        Player player = conditionEvent.getPlayer();
        boolean isPlaceholderAPI = plugin.getDependencyManager().isPlaceholderAPI();
        conditionEvent.setCurrentEvent(event);

        //Ignore with permission
        if(player != null && event.getIgnoreWithPermission() != null && player.hasPermission(event.getIgnoreWithPermission())){
            return;
        }

        //Ignore if cancelled
        if(event.isIgnoreIfCancelled()){
            Event minecraftEvent = conditionEvent.getMinecraftEvent();
            if(minecraftEvent instanceof Cancellable){
                Cancellable c = (Cancellable) minecraftEvent;
                if(c.isCancelled()){
                    return;
                }
            }
        }

        //Check condition list
        CheckConditionsResult conditionsResult = checkConditions(conditionEvent,isPlaceholderAPI);
        if(!conditionsResult.isConditionsAccomplished()){
            return;
        }
        String executeActionGroup = conditionsResult.getExecuteActionGroup();

        PlayerManager playerManager = plugin.getPlayerManager();
        MessagesManager messagesManager = plugin.getMessagesManager();

        boolean bypassCooldown = false;

        if(player != null){
            bypassCooldown = player.hasPermission("conditionalevents.bypasscooldown."+event.getName());

            //Check One time
            if(event.isOneTime()){
                boolean isOneTime = playerManager.getEventOneTime(event.getName(),player);
                if(isOneTime){
                    ExecutedEvent executedEvent = new ExecutedEvent(player, conditionEvent.getEventVariables(), event,
                         "one_time", conditionEvent.getMinecraftEvent(), conditionEvent.getTarget(), plugin);
                    executedEvent.executeActions();
                    return;
                }
            }

            //Check Cooldown
            if(event.getCooldown() != 0 && !bypassCooldown){
                long eventCooldownMillis = playerManager.getEventCooldown(event.getName(),player)+(event.getCooldown()*1000);
                long currentTimeMillis = System.currentTimeMillis();
                if(eventCooldownMillis > currentTimeMillis){
                    String timeString = TimeUtils.getTime((eventCooldownMillis-currentTimeMillis)/1000,messagesManager);
                    conditionEvent.getEventVariables().add(new StoredVariable("%time%",timeString));
                    ExecutedEvent executedEvent = new ExecutedEvent(player, conditionEvent.getEventVariables(), event,
                          "cooldown", conditionEvent.getMinecraftEvent(), conditionEvent.getTarget(), plugin);
                    executedEvent.executeActions();
                    return;
                }
            }
        }

        if(player != null){
            //Set One Time
            if(event.isOneTime()){
                if(!event.getPreventOneTimeActivationActionGroups().contains(executeActionGroup)){
                    playerManager.setEventOneTime(event.getName(),player);
                }
            }

            //Set Cooldown
            if(event.getCooldown() != 0 && !bypassCooldown){
                if(!event.getPreventCooldownActivationActionGroups().contains(executeActionGroup)){
                    playerManager.setEventCooldown(event.getName(),player);
                }
            }
        }

        //Execute actions
        ExecutedEvent executedEvent = new ExecutedEvent(player, conditionEvent.getEventVariables(), event,
                executeActionGroup, conditionEvent.getMinecraftEvent(), conditionEvent.getTarget(), plugin);
        executedEvent.executeActions();
    }

    public ArrayList<CEEvent> getValidEvents(EventType eventType){
        ArrayList<CEEvent> validEvents = new ArrayList<CEEvent>();
        for(CEEvent event : events){
            if(event.getEventType().equals(eventType) && event.isEnabled()){
                validEvents.add(event);
            }
        }
        return validEvents;
    }

    public ArrayList<CEEvent> getEventsByType(EventType eventType){
        ArrayList<CEEvent> validEvents = new ArrayList<>();
        for(CEEvent event : events){
            if(event.getEventType().equals(eventType)){
                validEvents.add(event);
            }
        }
        return validEvents;
    }

    private CheckConditionsResult checkConditions(ConditionEvent conditionEvent, boolean isPlaceholderAPI){
        List<String> conditions = new ArrayList<String>(conditionEvent.getCurrentEvent().getConditions());
        String eventName = conditionEvent.getCurrentEvent().getName();
        Player player = conditionEvent.getPlayer();
        Player target = conditionEvent.getTarget();
        CEEvent event = conditionEvent.getCurrentEvent();
        Event minecraftEvent = conditionEvent.getMinecraftEvent();
        DebugManager debugManager = plugin.getDebugManager();
        boolean mathFormulas = conditionEvent.getCurrentEvent().isAllowMathFormulasInConditions();

        //Check condition lines
        ArrayList<StoredVariable> storedVariables = conditionEvent.getEventVariables();

        for(int i=0;i<conditions.size();i++) {
            String conditionLine = conditions.get(i);
            boolean approvedLine = false;
            String executedActionGroup = null;
            if (conditionLine.contains(" execute ")) {
                String[] sep = conditionLine.split(" execute ");
                conditionLine = sep[0];
                executedActionGroup = sep[1];
            }

            String[] separatedConditions = null;
            SeparatorType separatorType = SeparatorType.NONE;
            if(conditionLine.contains(" or ")){
                separatedConditions = conditionLine.split(" or ");
                separatorType = SeparatorType.OR;
            }else if(conditionLine.contains(" and ")){
                separatedConditions = conditionLine.split(" and ");
                separatorType = SeparatorType.AND;
            }else{
                separatedConditions = new String[]{conditionLine};
            }

            String conditionLineWithReplacements = "";

            //AND
            //If a miniCondition is NOT accomplished, it will finish this cycle.
            //If a miniCondition is accomplished, it will check the next.

            //OR
            //If a miniCondition is NOT accomplished, it will check the next.
            //If a miniCondition IS accomplished, it will finish this cycle.
            //If this cycle is finished with NO accomplished miniConditions, the
            // whole condition was not satisfied so the checkConditions method returns FALSE.
            for(int c=0;c<separatedConditions.length;c++){
                String miniCondition = separatedConditions[c];

                for(ConditionalType conditionalType : ConditionalType.values()){
                    String textToFind = " "+conditionalType.getText()+" ";
                    if(miniCondition.contains(textToFind)){
                        int textToFindIndex = miniCondition.indexOf(textToFind);
                        String arg1 = miniCondition.substring(0, textToFindIndex);
                        String arg2 = miniCondition.substring(textToFindIndex+conditionalType.getText().length()+2);
                        //Replace variables
                        VariablesProperties variablesProperties = new VariablesProperties(
                                storedVariables,player,target,isPlaceholderAPI,event,minecraftEvent
                        );
                        arg1 = VariablesUtils.replaceAllVariablesInLine(arg1,variablesProperties,false);
                        arg2 = VariablesUtils.replaceAllVariablesInLine(arg2,variablesProperties,false);

                        String firstArg = !mathFormulas ? arg1 : MathUtils.calculate(arg1);
                        String secondArg = !mathFormulas ? arg2 : MathUtils.calculate(arg2);

                        conditionLineWithReplacements = conditionLineWithReplacements+"'"+firstArg+"'"+textToFind+"'"+secondArg+"'";
                        if(c != separatedConditions.length-1){
                            if(separatorType.equals(SeparatorType.OR)){
                                conditionLineWithReplacements = conditionLineWithReplacements+" or ";
                            }else if(separatorType.equals(SeparatorType.AND)){
                                conditionLineWithReplacements = conditionLineWithReplacements+" and ";
                            }
                        }

                        String firstArgLower = firstArg.toLowerCase();String secondArgLower = secondArg.toLowerCase();
                        double firstArgNum = 0;
                        double secondArgNum = 0;
                        if(MathUtils.isParsable(firstArg)){
                            firstArgNum = Double.parseDouble(firstArg);
                        }
                        if(MathUtils.isParsable(secondArg)){
                            secondArgNum = Double.parseDouble(secondArg);
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
                            case ENDS_WITH:
                                if(firstArgLower.endsWith(secondArgLower)) approvedLine = true;break;
                            case NOT_ENDS_WITH:
                                if(!firstArgLower.endsWith(secondArgLower)) approvedLine = true;break;
                            case MATCHES:
                                if(firstArgLower.matches(secondArgLower)) approvedLine = true;break;
                            case NOT_MATCHES:
                                if(!firstArgLower.matches(secondArgLower)) approvedLine = true;break;
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

                if(separatorType.equals(SeparatorType.AND)){
                    if(approvedLine){
                        if(c != separatedConditions.length-1){
                            //If not last condition, should verify again for the next
                            approvedLine = false;
                        }
                        continue;
                    }else{
                        break;
                    }
                }

                if(approvedLine){
                    break;
                }
            }

            debugManager.sendConditionMessage(eventName,conditionLineWithReplacements,approvedLine,conditionEvent.getPlayer(),i==0);
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

    public boolean checkToConditionAction(List<String> conditionGroup, Player player, boolean isPlaceholderAPI,
                                          CEEvent event, Event minecraftEvent){
        boolean mathFormulas = event.isAllowMathFormulasInConditions();
        for(int i=0;i<conditionGroup.size();i++) {
            String conditionLine = conditionGroup.get(i);
            boolean approvedLine = false;
            ArrayList<StoredVariable> storedVariables = new ArrayList<StoredVariable>();

            String[] separatedConditions = null;
            SeparatorType separatorType = SeparatorType.NONE;
            if(conditionLine.contains(" or ")){
                separatedConditions = conditionLine.split(" or ");
                separatorType = SeparatorType.OR;
            }else if(conditionLine.contains(" and ")){
                separatedConditions = conditionLine.split(" and ");
                separatorType = SeparatorType.AND;
            }else{
                separatedConditions = new String[]{conditionLine};
            }


            for(int c=0;c<separatedConditions.length;c++){
                String miniCondition = separatedConditions[c];

                for(ConditionalType conditionalType : ConditionalType.values()){
                    String textToFind = " "+conditionalType.getText()+" ";
                    if(miniCondition.contains(textToFind)){
                        int textToFindIndex = miniCondition.indexOf(textToFind);
                        String arg1 = miniCondition.substring(0, textToFindIndex);
                        String arg2 = miniCondition.substring(textToFindIndex+conditionalType.getText().length()+2);

                        VariablesProperties variablesProperties = new VariablesProperties(
                                storedVariables,player,null,isPlaceholderAPI,event,minecraftEvent
                        );
                        arg1 = VariablesUtils.replaceAllVariablesInLine(arg1,variablesProperties,false);
                        arg2 = VariablesUtils.replaceAllVariablesInLine(arg2,variablesProperties,false);

                        String firstArg = !mathFormulas ? arg1 : MathUtils.calculate(arg1);
                        String secondArg = !mathFormulas ? arg2 : MathUtils.calculate(arg2);

                        String firstArgLower = firstArg.toLowerCase();String secondArgLower = secondArg.toLowerCase();
                        double firstArgNum = 0;
                        double secondArgNum = 0;
                        if(MathUtils.isParsable(firstArg)){
                            firstArgNum = Double.parseDouble(firstArg);
                        }
                        if(MathUtils.isParsable(secondArg)){
                            secondArgNum = Double.parseDouble(secondArg);
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
                            case ENDS_WITH:
                                if(firstArgLower.endsWith(secondArgLower)) approvedLine = true;break;
                            case NOT_ENDS_WITH:
                                if(!firstArgLower.endsWith(secondArgLower)) approvedLine = true;break;
                            case MATCHES:
                                if(firstArgLower.matches(secondArgLower)) approvedLine = true;break;
                            case NOT_MATCHES:
                                if(!firstArgLower.matches(secondArgLower)) approvedLine = true;break;
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

                if(separatorType.equals(SeparatorType.AND)){
                    if(approvedLine){
                        if(c != separatedConditions.length-1){
                            //If not last condition, should verify again for the next
                            approvedLine = false;
                        }
                        continue;
                    }else{
                        break;
                    }
                }

                if(approvedLine){
                    break;
                }
            }

            if(!approvedLine){
                return false;
            }
        }
        return true;
    }
}

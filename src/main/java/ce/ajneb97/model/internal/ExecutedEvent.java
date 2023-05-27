package ce.ajneb97.model.internal;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.api.ConditionalEventsEvent;
import ce.ajneb97.managers.EventsManager;
import ce.ajneb97.model.CEEvent;
import ce.ajneb97.model.EventType;
import ce.ajneb97.model.StoredVariable;
import ce.ajneb97.model.ToConditionGroup;
import ce.ajneb97.model.actions.*;
import ce.ajneb97.utils.ActionUtils;
import ce.ajneb97.utils.VariablesUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

// Represent an event that has accomplished all conditions and actions
// are being executed.
public class ExecutedEvent {
    private Player player;
    private Player target;
    private ArrayList<StoredVariable> eventVariables;
    private CEEvent event;
    private Event minecraftEvent;
    private String actionGroupName;
    private ConditionalEvents plugin;


    private boolean isPlaceholderAPI;
    private List<CEAction> actions;

    private boolean onWait;
    private int currentActionPos;

    public ExecutedEvent(Player player, ArrayList<StoredVariable> eventVariables, CEEvent event, String actionGroupName
        ,Event minecraftEvent, Player target, ConditionalEvents plugin) {
        this.player = player;
        this.eventVariables = eventVariables;
        this.event = event;
        this.actionGroupName = actionGroupName;
        this.target = target;
        this.minecraftEvent = minecraftEvent;
        this.plugin = plugin;

        this.onWait = false;
        this.currentActionPos = 0;
    }

    public boolean setActionGroup(String actionGroupName){
        //Check if parameters are present
        int pos = actionGroupName.indexOf("{");
        if(pos != -1){
            String parameters = actionGroupName.substring(pos+1, actionGroupName.length()-1);
            String[] sep = parameters.split(";");
            for(int i=0;i<sep.length;i++){
                String[] variableLineSep = sep[i].split("=");
                eventVariables.add(new StoredVariable(variableLineSep[0],variableLineSep[1]));
            }
            this.actionGroupName = actionGroupName.substring(0, pos);
        }

        ActionGroup actionGroup = event.getActionGroup(this.actionGroupName);
        if(actionGroup == null){
            return false;
        }
        this.actions = new ArrayList<CEAction>(actionGroup.getActions());
        return true;
    }

    public void executeActions(){
        this.isPlaceholderAPI = plugin.getDependencyManager().isPlaceholderAPI();
        if(!setActionGroup(actionGroupName)){
            return;
        }

        //For API only
        ConditionalEventsEvent ceEvent = new ConditionalEventsEvent(player, event.getName(), actionGroupName);

        //Check cancel event, always first to prevent issues with async events.
        executeCancelEventAction();

        boolean isPlayerPreJoin = event.getEventType().equals(EventType.PLAYER_PRE_JOIN);

        if(!Bukkit.isPrimaryThread()){
            new BukkitRunnable(){
                @Override
                public void run() {
                    plugin.getServer().getPluginManager().callEvent(ceEvent);
                    if(!isPlayerPreJoin){
                        executeActionsFinal();
                    }
                }
            }.runTask(plugin);
            if(isPlayerPreJoin){
                executeActionsFinal();
            }
        }else{
            plugin.getServer().getPluginManager().callEvent(ceEvent);
            executeActionsFinal();
        }
    }

    public void executeCancelEventAction(){
        for(CEAction ceAction : actions){
            if(ceAction.getType().equals(ActionType.CANCEL_EVENT)){
                ActionUtils.cancelEvent(ceAction.getActionLine(),minecraftEvent);
                return;
            }
        }
    }

    public void continueWithActions(){
        this.onWait = false;
        executeActionsFinal();
    }

    private void executeActionsFinal(){
        for(int i=currentActionPos;i<actions.size();i++){
            CEAction action = actions.get(i);
            ActionType actionType = action.getType();

            //Replace variables
            String actionLine = action.getActionLine();
            VariablesProperties variablesProperties = new VariablesProperties(
                eventVariables,player,target,isPlaceholderAPI,event,minecraftEvent
            );

            actionLine = VariablesUtils.replaceAllVariablesInLine(actionLine,variablesProperties,false);

            ActionTargeter targeter = action.getTargeter();
            ActionTargeterType targeterType = targeter.getType();

            String parametersLine = targeter.getParameter();
            if(parametersLine != null){
                parametersLine = VariablesUtils.replaceAllVariablesInLine(parametersLine,variablesProperties,false);
            }

            if(targeterType.equals(ActionTargeterType.TO_ALL)) {
                for(Player globalPlayer : Bukkit.getOnlinePlayers()) {
                    executeAction(globalPlayer,actionType,actionLine);
                }
            }else if(targeterType.equals(ActionTargeterType.TO_TARGET)){
                executeAction(target,actionType,actionLine);
            }else if(targeterType.equals(ActionTargeterType.TO_WORLD)){
                String world = parametersLine;
                for(Player globalPlayer : Bukkit.getOnlinePlayers()) {
                    if(globalPlayer.getWorld().getName().equals(world)){
                        executeAction(globalPlayer,actionType,actionLine);
                    }
                }
            }else if(targeterType.equals(ActionTargeterType.TO_RANGE)){
                String[] sep = parametersLine.split(";");
                double range = Double.valueOf(sep[0]);
                boolean includePlayer = Boolean.valueOf(sep[1]);
                ArrayList<Player> globalPlayers = new ArrayList<Player>();
                if(includePlayer){
                    globalPlayers.add(player);
                }
                for(Entity e : player.getWorld().getNearbyEntities(player.getLocation(), range, range, range)) {
                    if(e instanceof Player) {
                        Player p = (Player) e;
                        if(!p.getName().equals(player.getName())){
                            globalPlayers.add(p);
                        }
                    }
                }
                for(Player globalPlayer : globalPlayers){
                    executeAction(globalPlayer,actionType,actionLine);
                }
            }else if(targeterType.equals(ActionTargeterType.TO_CONDITION)) {
                String toConditionGroup = parametersLine;
                ToConditionGroup group = plugin.getConfigsManager().getMainConfigManager().getToConditionGroup(toConditionGroup);
                if(group == null){
                    continue;
                }
                EventsManager eventsManager = plugin.getEventsManager();
                ArrayList<Player> players = new ArrayList<Player>();
                for(Player globalPlayer : Bukkit.getOnlinePlayers()) {
                    //Check for conditions
                    boolean accomplishesConditions = eventsManager.checkToConditionAction(group.getConditions()
                            ,globalPlayer,isPlaceholderAPI,event,minecraftEvent);
                    if(accomplishesConditions){
                        players.add(globalPlayer);
                    }
                }

                for(Player globalPlayer : players){
                    executeAction(globalPlayer,actionType,actionLine);
                }
            }
            else {
                executeAction(player,actionType,actionLine);
            }

            if(onWait){
                currentActionPos = i+1;
                return;
            }
        }
    }

    private void executeAction(Player player,ActionType type,String actionLine){
        //Non player actions
        switch(type){
            case CONSOLE_MESSAGE:
                ActionUtils.consoleMessage(actionLine);
                return;
            case CONSOLE_COMMAND:
                ActionUtils.consoleCommand(actionLine);
                return;
            case WAIT:
                ActionUtils.wait(actionLine,this);
                return;
            case WAIT_TICKS:
                ActionUtils.waitTicks(actionLine,this);
                return;
            case KEEP_ITEMS:
                ActionUtils.keepItems(actionLine,minecraftEvent);
                return;
            case CANCEL_DROP:
                ActionUtils.cancelDrop(actionLine,minecraftEvent);
                return;
            case SET_DAMAGE:
                ActionUtils.setDamage(actionLine,minecraftEvent);
                return;
            case HIDE_JOIN_MESSAGE:
                ActionUtils.hideJoinMessage(actionLine,minecraftEvent);
                return;
            case HIDE_LEAVE_MESSAGE:
                ActionUtils.hideLeaveMessage(actionLine,minecraftEvent);
                return;
            case SET_DEATH_MESSAGE:
                ActionUtils.setDeathMessage(actionLine,minecraftEvent);
                return;
            case PREVENT_JOIN:
                ActionUtils.preventJoin(actionLine,minecraftEvent);
                return;
            case DISCORDSRV_EMBED:
                ActionUtils.discordSRVEmbed(actionLine,plugin);
                return;
            // Could or could not be a player event
            case CALL_EVENT:
                ActionUtils.callEvent(actionLine,player,plugin);
                return;
        }

        //Player actions
        if(player == null){
            return;
        }
        switch(type) {
            case MESSAGE:
                ActionUtils.message(player, actionLine);
                return;
            case CENTERED_MESSAGE:
                ActionUtils.centeredMessage(player, actionLine);
                return;
            case JSON_MESSAGE:
                ActionUtils.jsonMessage(player, actionLine);
                return;
            case PLAYER_COMMAND:
                ActionUtils.playerCommand(player, actionLine);
                return;
            case PLAYER_COMMAND_AS_OP:
                ActionUtils.playerCommandAsOp(player, actionLine);
                return;
            case PLAYER_SEND_CHAT:
                ActionUtils.playerSendChat(player, actionLine);
                return;
            case SEND_TO_SERVER:
                ActionUtils.sendToServer(player, actionLine, plugin);
                return;
            case TELEPORT:
                ActionUtils.teleport(player, actionLine, minecraftEvent);
                return;
            case REMOVE_ITEM:
                ActionUtils.removeItem(player, actionLine);
                return;
            case GIVE_POTION_EFFECT:
                ActionUtils.givePotionEffect(player, actionLine);
                return;
            case REMOVE_POTION_EFFECT:
                ActionUtils.removePotionEffect(player, actionLine);
                return;
            case KICK:
                ActionUtils.kick(player, actionLine);
                return;
            case PLAYSOUND:
                ActionUtils.playSound(player, actionLine);
                return;
            case PLAYSOUND_RESOURCE_PACK:
                ActionUtils.playSoundResourcePack(player, actionLine);
                return;
            case ACTIONBAR:
                ActionUtils.actionbar(player, actionLine, plugin);
                return;
            case TITLE:
                ActionUtils.title(player, actionLine);
                return;
            case FIREWORK:
                ActionUtils.firework(player, actionLine);
                return;
            case GAMEMODE:
                ActionUtils.gamemode(player, actionLine);
                return;
        }
    }

    public void setOnWait(boolean onWait) {
        this.onWait = onWait;
    }

    public ConditionalEvents getPlugin() {
        return plugin;
    }

}

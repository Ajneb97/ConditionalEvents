package ce.ajneb97.model.internal;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.api.ConditionalEventsEvent;
import ce.ajneb97.model.CEEvent;
import ce.ajneb97.model.StoredVariable;
import ce.ajneb97.model.actions.ActionGroup;
import ce.ajneb97.model.actions.ActionTargeter;
import ce.ajneb97.model.actions.ActionType;
import ce.ajneb97.model.actions.CEAction;
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

    public void executeActions(boolean isPlaceholderAPI,boolean isAsync){
        ActionGroup actionGroup = event.getActionGroup(actionGroupName);
        this.actions = new ArrayList<CEAction>(actionGroup.getActions());
        this.isPlaceholderAPI = isPlaceholderAPI;

        ConditionalEventsEvent ceEvent = new ConditionalEventsEvent(player, event.getName(), actionGroupName);

        if(isAsync){
            new BukkitRunnable(){
                @Override
                public void run() {
                    plugin.getServer().getPluginManager().callEvent(ceEvent);
                    executeActionsFinal();
                }
            }.runTask(plugin);
        }else{
            plugin.getServer().getPluginManager().callEvent(ceEvent);
            executeActionsFinal();
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
            actionLine = VariablesUtils.replaceAllVariablesInLine(actionLine,eventVariables,player
                    ,target,isPlaceholderAPI);


            ActionTargeter targeter = action.getTargeter();
            if(targeter.equals(ActionTargeter.TO_ALL)) {
                for(Player globalPlayer : Bukkit.getOnlinePlayers()) {
                    executeAction(globalPlayer,actionType,actionLine);
                }
            }else if(targeter.equals(ActionTargeter.TO_TARGET)){
                executeAction(target,actionType,actionLine);
            }else if(targeter.equals(ActionTargeter.TO_WORLD)){
                String world = targeter.getParameter();
                for(Player globalPlayer : Bukkit.getOnlinePlayers()) {
                    if(globalPlayer.getWorld().getName().equals(world)){
                        executeAction(globalPlayer,actionType,actionLine);
                    }
                }
            }else if(targeter.equals(ActionTargeter.TO_RANGE)){
                String[] sep = targeter.getParameter().split(";");
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
            }else {
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
            case CANCEL_EVENT:
                ActionUtils.cancelEvent(actionLine,minecraftEvent);
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

package ce.ajneb97.model.internal;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.model.CEEvent;
import ce.ajneb97.model.EventType;
import ce.ajneb97.model.StoredVariable;
import ce.ajneb97.utils.BlockUtils;
import ce.ajneb97.utils.OtherUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

// Represent an event that is being executed and conditions need
// to be checked.
public class ConditionEvent {

    private ConditionalEvents plugin;
    private Player player;
    private Player target;
    private ArrayList<StoredVariable> eventVariables;
    private Event minecraftEvent;
    private EventType eventType;
    private CEEvent currentEvent; //The current event that is being checked

    public ConditionEvent(ConditionalEvents plugin,Player player, Event minecraftEvent, EventType eventType
        , Player target) {
        this.plugin = plugin;
        this.player = player;
        this.eventVariables = new ArrayList<StoredVariable>();
        this.minecraftEvent = minecraftEvent;
        this.eventType = eventType;
        this.target = target;
    }

    public Player getPlayer() {
        return player;
    }

    public Player getTarget() {
        return target;
    }

    public ArrayList<StoredVariable> getEventVariables() {
        return eventVariables;
    }

    public Event getMinecraftEvent() {
        return minecraftEvent;
    }

    public EventType getEventType() {
        return eventType;
    }

    public CEEvent getCurrentEvent() {
        return currentEvent;
    }

    public void setCurrentEvent(CEEvent currentEvent) {
        this.currentEvent = currentEvent;
    }

    public void checkEvent(){
        plugin.getEventsManager().checkEvent(this);
    }

    public boolean containsValidEvents(){
        ArrayList<CEEvent> validEvents = plugin.getEventsManager().getValidEvents(eventType);
        if(validEvents.size() == 0){
            return false;
        }
        return true;
    }

    public ConditionEvent addVariables(StoredVariable... storedVariables){
        for(StoredVariable v : storedVariables){
            eventVariables.add(v);
        }
        return this;
    }

    public ConditionEvent addVariables(ArrayList<StoredVariable> storedVariables){
        eventVariables.addAll(storedVariables);
        return this;
    }

    public ConditionEvent setCommonBlockVariables(Block block){
        Location l = block.getLocation();
        eventVariables.add(new StoredVariable("%block_x%",l.getBlockX()+""));
        eventVariables.add(new StoredVariable("%block_y%",l.getBlockY()+""));
        eventVariables.add(new StoredVariable("%block_z%",l.getBlockZ()+""));
        eventVariables.add(new StoredVariable("%block_world%",l.getWorld().getName()));
        eventVariables.add(new StoredVariable("%block%",block.getType().name()));
        eventVariables.add(new StoredVariable("%block_head_texture%", BlockUtils.getHeadTextureData(block)));
        return this;
    }

    public ConditionEvent setCommonActionVariables(Action action,Player player){
        String actionVariable = null;
        if(player.isSneaking()) {
            if(action.equals(Action.RIGHT_CLICK_BLOCK)) {
                actionVariable = "SHIFT_RIGHT_CLICK";
            }else if(action.equals(Action.LEFT_CLICK_BLOCK)) {
                actionVariable = "SHIFT_LEFT_CLICK";
            }
        }else {
            if(action.equals(Action.RIGHT_CLICK_BLOCK)) {
                actionVariable = "RIGHT_CLICK";
            }else if(action.equals(Action.LEFT_CLICK_BLOCK)) {
                actionVariable = "LEFT_CLICK";
            }else if(action.equals(Action.PHYSICAL)) {
                actionVariable = "PHYSICAL";
            }
        }

        if(actionVariable != null){
            eventVariables.add(new StoredVariable("%action_type%",actionVariable));
        }
        return this;
    }

    public ConditionEvent setCommonVictimVariables(Entity entity){
        String victimType = entity.getType().name();
        String victimName = "";
        if(entity.getCustomName() != null) {
            victimName = entity.getCustomName();
        }
        Location location = entity.getLocation();

        eventVariables.add(new StoredVariable("%victim%",victimType));
        eventVariables.add(new StoredVariable("%victim_name%",victimName));
        eventVariables.add(new StoredVariable("%victim_block_x%",location.getBlockX()+""));
        eventVariables.add(new StoredVariable("%victim_block_y%",location.getBlockY()+""));
        eventVariables.add(new StoredVariable("%victim_block_z%",location.getBlockZ()+""));
        eventVariables.add(new StoredVariable("%victim_block_world%",location.getWorld().getName()));
        return this;
    }

    public ConditionEvent setCommonItemVariables(ItemStack item){
        String name = "";
        String material = "";
        String loreString = "";
        short durability = 0;
        List<String> loreList = new ArrayList<String>();
        int customModelData = 0;

        if(item != null) {
            durability = item.getDurability();
            material = item.getType().name();
            if(item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if(meta.hasDisplayName()) {
                    name = ChatColor.stripColor(meta.getDisplayName());
                }
                if(meta.hasLore()) {
                    List<String> lore = meta.getLore();
                    for(int i=0;i<lore.size();i++) {
                        loreList.add(ChatColor.stripColor(lore.get(i)));
                        if(i == lore.size()-1) {
                            loreString = loreString+ChatColor.stripColor(lore.get(i));
                        }else {
                            loreString = loreString+ChatColor.stripColor(lore.get(i))+" ";
                        }
                    }
                }
                if(OtherUtils.isNew() && meta.hasCustomModelData()){
                    customModelData = meta.getCustomModelData();
                }
            }
        }

        eventVariables.add(new StoredVariable("%item%",material));
        eventVariables.add(new StoredVariable("%item_name%",name));
        eventVariables.add(new StoredVariable("%item_durability%",durability+""));
        eventVariables.add(new StoredVariable("%item_lore%",loreString));
        eventVariables.add(new StoredVariable("%item_custom_model_data%",customModelData+""));
        for(int i=0;i<loreList.size();i++) {
            eventVariables.add(new StoredVariable("%item_lore_line_"+(i+1)+"%", loreList.get(i)));
        }

        return this;
    }
}

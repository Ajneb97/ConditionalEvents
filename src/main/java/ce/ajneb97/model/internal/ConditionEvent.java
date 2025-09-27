package ce.ajneb97.model.internal;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.model.CEEvent;
import ce.ajneb97.model.EventType;
import ce.ajneb97.model.StoredVariable;
import ce.ajneb97.utils.BlockUtils;
import ce.ajneb97.utils.OtherUtils;
import ce.ajneb97.utils.ServerVersion;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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
    private LivingEntity target;
    private ArrayList<StoredVariable> eventVariables;
    private Event minecraftEvent;
    private EventType eventType;
    private CEEvent currentEvent; //The current event that is being checked

    private boolean async;

    public ConditionEvent(ConditionalEvents plugin,Player player, Event minecraftEvent, EventType eventType
        , LivingEntity target) {
        this.plugin = plugin;
        this.player = player;
        this.eventVariables = new ArrayList<>();
        this.minecraftEvent = minecraftEvent;
        this.eventType = eventType;
        this.target = target;
        this.async = false;
    }

    public Player getPlayer() {
        return player;
    }

    public LivingEntity getTarget() {
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
        if(OtherUtils.isLegacy()){
            eventVariables.add(new StoredVariable("%block_data%",block.getData()+""));
        }else{
            eventVariables.add(new StoredVariable("%block_data%",
                    BlockUtils.getBlockDataStringFromObject(block.getBlockData())));
        }

        return this;
    }

    public ConditionEvent setCommonActionVariables(Action action,Player player){
        String actionVariable = null;
        String actionName = action.name();
        if(player.isSneaking()) {
            if(actionName.contains("RIGHT_CLICK")) {
                actionVariable = "SHIFT_RIGHT_CLICK";
            }else if(actionName.contains("LEFT_CLICK")) {
                actionVariable = "SHIFT_LEFT_CLICK";
            }else if(action.equals(Action.PHYSICAL)) {
                actionVariable = "PHYSICAL";
            }
        }else {
            if(actionName.contains("RIGHT_CLICK")) {
                actionVariable = "RIGHT_CLICK";
            }else if(actionName.contains("LEFT_CLICK")) {
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
        String victimNameColorFormat = "";
        String victimUuid = entity.getUniqueId().toString();
        if(entity.getCustomName() != null) {
            victimName = ChatColor.stripColor(entity.getCustomName());
            victimNameColorFormat = entity.getCustomName().replace("§", "&");
        }
        Location location = entity.getLocation();
        double health = 0;
        if(entity instanceof LivingEntity){
            health = ((LivingEntity)entity).getHealth();
        }

        eventVariables.add(new StoredVariable("%victim%",victimType));
        eventVariables.add(new StoredVariable("%victim_name%",victimName));
        eventVariables.add(new StoredVariable("%victim_health%",health+""));
        eventVariables.add(new StoredVariable("%victim_color_format_name%",victimNameColorFormat));
        eventVariables.add(new StoredVariable("%victim_block_x%",location.getBlockX()+""));
        eventVariables.add(new StoredVariable("%victim_block_y%",location.getBlockY()+""));
        eventVariables.add(new StoredVariable("%victim_block_z%",location.getBlockZ()+""));
        eventVariables.add(new StoredVariable("%victim_block_world%",location.getWorld().getName()));
        eventVariables.add(new StoredVariable("%victim_uuid%",victimUuid));
        return this;
    }

    public ConditionEvent setCommonItemVariables(ItemStack item,String otherItemTag){
        String name = "";
        String colorFormatName = "";
        String material = "";
        String loreString = "";
        String colorFormatLoreString = "";
        short durability = 0;
        int amount = 0;
        List<String> loreList = new ArrayList<>();
        List<String> colorFormatLoreList = new ArrayList<>();
        int customModelData = 0;
        String metaString = "";

        if(item != null) {
            durability = item.getDurability();
            material = item.getType().name();
            amount = item.getAmount();
            if(item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();

                boolean itemMetaVariableEnabled = plugin.getConfigsManager().getMainConfigManager().isItemMetaVariableEnabled();
                if(itemMetaVariableEnabled){
                    metaString = meta.toString();
                }else{
                    metaString = "variable disabled";
                }

                if(meta.hasDisplayName()) {
                    name = ChatColor.stripColor(meta.getDisplayName());
                    colorFormatName = meta.getDisplayName().replace("§", "&");
                }
                if(meta.hasLore()) {
                    List<String> lore = meta.getLore();
                    for(int i=0;i<lore.size();i++) {
                        loreList.add(ChatColor.stripColor(lore.get(i)));
                        colorFormatLoreList.add(lore.get(i).replace("§", "&"));
                        if(i == lore.size()-1) {
                            loreString = loreString+ChatColor.stripColor(lore.get(i));
                            colorFormatLoreString = colorFormatLoreString+lore.get(i).replace("§", "&");
                        }else {
                            loreString = loreString+ChatColor.stripColor(lore.get(i))+" ";
                            colorFormatLoreString = colorFormatLoreString+lore.get(i).replace("§", "&")+" ";
                        }
                    }
                }
                if(OtherUtils.isNew() && meta.hasCustomModelData()){
                    ServerVersion serverVersion = ConditionalEvents.serverVersion;
                    if(!serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_21_R3)){
                        customModelData = meta.getCustomModelData();
                    }
                }
            }
        }

        //Example: %offhand:<variable>%
        if(otherItemTag == null){
            otherItemTag = "%";
        }else{
            otherItemTag = "%"+otherItemTag+":";
        }

        eventVariables.add(new StoredVariable(otherItemTag+"item%",material));
        eventVariables.add(new StoredVariable(otherItemTag+"item_name%",name));
        eventVariables.add(new StoredVariable(otherItemTag+"item_color_format_name%",colorFormatName));
        eventVariables.add(new StoredVariable(otherItemTag+"item_durability%",durability+""));
        eventVariables.add(new StoredVariable(otherItemTag+"item_amount%",amount+""));
        eventVariables.add(new StoredVariable(otherItemTag+"item_lore%",loreString));
        eventVariables.add(new StoredVariable(otherItemTag+"item_color_format_lore%",colorFormatLoreString));
        eventVariables.add(new StoredVariable(otherItemTag+"item_custom_model_data%",customModelData+""));
        eventVariables.add(new StoredVariable(otherItemTag+"item_meta%",metaString));
        for(int i=0;i<loreList.size();i++) {
            eventVariables.add(new StoredVariable(otherItemTag+"item_lore_line_"+(i+1)+"%", loreList.get(i)));
        }
        for(int i=0;i<colorFormatLoreList.size();i++) {
            eventVariables.add(new StoredVariable(otherItemTag+"item_color_format_lore_line_"+(i+1)+"%", colorFormatLoreList.get(i)));
        }

        return this;
    }

    public ConditionEvent setCommonEntityVariables(Entity entity){
        String entityType = entity.getType().name();
        String entityName = "";
        String entityNameColorFormat = "";
        String entityUuid = entity.getUniqueId().toString();
        if(entity.getCustomName() != null) {
            entityName = ChatColor.stripColor(entity.getCustomName());
            entityNameColorFormat = entity.getCustomName().replace("§", "&");
        }
        Location location = entity.getLocation();

        eventVariables.add(new StoredVariable("%entity%",entityType));
        eventVariables.add(new StoredVariable("%entity_name%",entityName));
        eventVariables.add(new StoredVariable("%entity_color_format_name%",entityNameColorFormat));
        eventVariables.add(new StoredVariable("%entity_x%",location.getBlockX()+""));
        eventVariables.add(new StoredVariable("%entity_y%",location.getBlockY()+""));
        eventVariables.add(new StoredVariable("%entity_z%",location.getBlockZ()+""));
        eventVariables.add(new StoredVariable("%entity_world%",location.getWorld().getName()));
        eventVariables.add(new StoredVariable("%entity_uuid%",entityUuid));
        return this;
    }

    public boolean isAsync() {
        return async;
    }

    public ConditionEvent setAsync(boolean async) {
        this.async = async;
        return this;
    }
}

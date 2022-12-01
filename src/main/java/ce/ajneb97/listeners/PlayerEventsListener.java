package ce.ajneb97.listeners;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.libs.armorequipevent.ArmorEquipEvent;
import ce.ajneb97.managers.MessagesManager;
import ce.ajneb97.model.EventType;
import ce.ajneb97.model.StoredVariable;
import ce.ajneb97.model.internal.ConditionEvent;
import ce.ajneb97.model.player.PlayerData;
import ce.ajneb97.utils.MathUtils;
import ce.ajneb97.utils.OtherUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class PlayerEventsListener implements Listener {

    public ConditionalEvents plugin;
    public PlayerEventsListener(ConditionalEvents plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        new ConditionEvent(plugin, player, event, EventType.PLAYER_JOIN, null)
                .checkEvent();

        //Update player data
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if(playerData != null){
            playerData.setName(player.getName());
        }

        //Update notification
        String latestVersion = plugin.getUpdateCheckerManager().getLatestVersion();
        if(player.isOp() && !(plugin.version.equals(latestVersion)) &&
                plugin.getConfigsManager().getMainConfigManager().isUpdateNotifications()){
            player.sendMessage(MessagesManager.getColoredMessage(plugin.prefix+" &cThere is a new version available. &e(&7"+latestVersion+"&e)"));
            player.sendMessage(MessagesManager.getColoredMessage("&cYou can download it at: &ahttps://www.spigotmc.org/resources/82271/"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(PlayerQuitEvent event){
        Player player = event.getPlayer();
        new ConditionEvent(plugin, player, event, EventType.PLAYER_LEAVE, null)
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event){
        Player player = event.getPlayer();
        new ConditionEvent(plugin, player, event, EventType.PLAYER_RESPAWN, null)
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event){
        Player player = event.getEntity();
        String cause = "";
        if(player.getLastDamageCause() != null) {
            cause = player.getLastDamageCause().getCause().name();
        }

        new ConditionEvent(plugin, player, event, EventType.PLAYER_DEATH, null)
                .addVariables(new StoredVariable("%cause%",cause))
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if(!Bukkit.getVersion().contains("1.8") && !Bukkit.getVersion().contains("1.9")) {
            if(!event.getAction().equals(Action.PHYSICAL) && (event.getHand() == null || !event.getHand().equals(EquipmentSlot.HAND))) {
                return;
            }
        }
        if(block == null){
            return;
        }

        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.BLOCK_INTERACT, null);
        if(!conditionEvent.containsValidEvents()) return;
        conditionEvent.setCommonBlockVariables(block)
                .setCommonActionVariables(event.getAction(),player)
                .setCommonItemVariables(player.getItemInHand())
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityInteract(PlayerInteractAtEntityEvent event){
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        if(!Bukkit.getVersion().contains("1.8") && !event.getHand().equals(EquipmentSlot.HAND)) {
            return;
        }
        if(entity == null){
            return;
        }

        Player target = null;
        if(entity instanceof Player){
            target = (Player) entity;
        }

        new ConditionEvent(plugin, player, event, EventType.ENTITY_INTERACT, target)
                .setCommonEntityVariables(entity)
                .setCommonItemVariables(player.getItemInHand())
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerAttack(EntityDamageByEntityEvent event){
        Entity attacker = event.getDamager();
        Entity damaged = event.getEntity();
        Player player = null;
        if(attacker == null || damaged == null) {
            return;
        }

        if(attacker instanceof Arrow) {
            Arrow arrow = (Arrow) attacker;
            if(arrow.getShooter() instanceof Player) {
                player = (Player) arrow.getShooter();
            }else{
                return;
            }
        }else if(attacker instanceof Player) {
            player = (Player) attacker;
        }else{
            return;
        }

        Player target = null;
        if(damaged instanceof Player){
            target = (Player) damaged;
        }

        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.PLAYER_ATTACK, target);
        if(!conditionEvent.containsValidEvents()) return;
        conditionEvent.addVariables(
                new StoredVariable("%damage%",MathUtils.truncate(event.getFinalDamage())+"")
        ).setCommonItemVariables(player.getItemInHand())
                .setCommonVictimVariables(damaged)
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWorldChange(PlayerChangedWorldEvent event){
        Player player = event.getPlayer();

        new ConditionEvent(plugin, player, event, EventType.PLAYER_WORLD_CHANGE, null)
                .addVariables(
                        new StoredVariable("%world_from%",event.getFrom().getName()),
                        new StoredVariable("%world_to%",player.getWorld().getName()),
                        new StoredVariable("%online_players_from%",event.getFrom().getPlayers().size()+""),
                        new StoredVariable("%online_players_to%",player.getWorld().getPlayers().size()+"")
                ).checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamaged(EntityDamageEvent event){
        Entity damaged = event.getEntity();
        if(damaged == null || !(damaged instanceof Player) || event.getCause() == null) {
            return;
        }

        Player player = (Player) damaged;
        String cause = event.getCause().toString();
        String damagerType = "";
        String damagerName = "";
        if(event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent event2 = (EntityDamageByEntityEvent) event;
            Entity damager = event2.getDamager();
            damagerType = damager.getType().name();
            if(damager.getCustomName() != null) {
                damagerName = damager.getCustomName();
            }
        }



        new ConditionEvent(plugin, player, event, EventType.PLAYER_DAMAGE, null)
                .addVariables(
                        new StoredVariable("%damager_type%",damagerType),
                        new StoredVariable("%damager_name%",damagerName),
                        new StoredVariable("%damage%", MathUtils.truncate(event.getFinalDamage())+""),
                        new StoredVariable("%cause%",cause)
                ).checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerKill(EntityDeathEvent event){
        LivingEntity entity = event.getEntity();
        if(entity == null || entity.getKiller() == null) {
            return;
        }

        Player player = entity.getKiller();
        Player target = null;
        if(entity instanceof Player){
            target = (Player) entity;
        }

        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.PLAYER_KILL, target);
        if(!conditionEvent.containsValidEvents()) return;
        conditionEvent.setCommonVictimVariables(entity).
                setCommonItemVariables(player.getItemInHand())
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event){
        Player player = event.getPlayer();
        Block block = event.getBlock();

        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.BLOCK_PLACE, null);
        if(!conditionEvent.containsValidEvents()) return;
        conditionEvent.setCommonItemVariables(player.getItemInHand())
                .setCommonBlockVariables(block)
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        Block block = event.getBlock();

        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.BLOCK_BREAK, null);
        if(!conditionEvent.containsValidEvents()) return;
        conditionEvent.setCommonItemVariables(player.getItemInHand())
                .setCommonBlockVariables(block)
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandUse(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage();
        String[] args = command.split(" ");

        ArrayList<StoredVariable> eventVariables = new ArrayList<StoredVariable>();
        for(int i=1;i<args.length;i++) {
            eventVariables.add(new StoredVariable("%arg_"+(i)+"%",args[i]));
        }

        new ConditionEvent(plugin, player, event, EventType.PLAYER_COMMAND, null)
                .addVariables(
                        new StoredVariable("%command%",command),
                        new StoredVariable("%main_command%",args[0]),
                        new StoredVariable("%args_length%",(args.length-1)+"")
                ).addVariables(eventVariables)
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        new ConditionEvent(plugin, player, event, EventType.PLAYER_CHAT, null)
                .addVariables(
                        new StoredVariable("%message%",event.getMessage())
                ).setAsync(true).checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLevelChange(PlayerLevelChangeEvent event) {
        Player player = event.getPlayer();

        new ConditionEvent(plugin, player, event, EventType.PLAYER_LEVELUP, null)
                .addVariables(
                        new StoredVariable("%old_level%",event.getOldLevel()+""),
                        new StoredVariable("%new_level%",event.getNewLevel()+"")
                ).checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArmorEquip(ArmorEquipEvent event) {
        Player player = event.getPlayer();
        String equipType = "EQUIP";
        ItemStack newItem = event.getNewArmorPiece();
        ItemStack previousItem = event.getOldArmorPiece();
        ItemStack selectedItem = null;
        if(newItem == null || newItem.getType().name().equals("AIR")) {
            equipType = "UNEQUIP";
            selectedItem = previousItem;
        }else {
            selectedItem = newItem;
        }
        String type = "";
        if(event.getType() != null) {
            type = event.getType().name();
        }

        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.PLAYER_ARMOR, null);
        if(!conditionEvent.containsValidEvents()) return;
        conditionEvent.addVariables(
                new StoredVariable("%armor_type%",type),
                new StoredVariable("%equip_type%",equipType)
        ).setCommonItemVariables(selectedItem)
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        Location locationFrom = event.getFrom();
        Location locationTo = event.getTo();

        new ConditionEvent(plugin, player, event, EventType.PLAYER_TELEPORT, null)
                .addVariables(
                        new StoredVariable("%cause%",event.getCause()+""),
                        new StoredVariable("%from_x%",locationFrom.getX()+""),
                        new StoredVariable("%from_y%",locationFrom.getY()+""),
                        new StoredVariable("%from_z%",locationFrom.getZ()+""),
                        new StoredVariable("%from_world%",locationFrom.getWorld().getName()+""),
                        new StoredVariable("%to_x%",locationTo.getX()+""),
                        new StoredVariable("%to_y%",locationTo.getY()+""),
                        new StoredVariable("%to_z%",locationTo.getZ()+""),
                        new StoredVariable("%to_world%",locationTo.getWorld().getName()+"")
                ).checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBedEnter(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();

        if(OtherUtils.isLegacy()){
            return;
        }

        new ConditionEvent(plugin, player, event, EventType.PLAYER_BED_ENTER, null)
                .addVariables(
                        new StoredVariable("%result%",event.getBedEnterResult()+"")
                ).checkEvent();
    }
}

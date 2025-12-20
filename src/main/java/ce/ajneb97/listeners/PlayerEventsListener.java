package ce.ajneb97.listeners;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.libs.armorequipevent.ArmorEquipEvent;
import ce.ajneb97.libs.offhandevent.OffHandEvent;
import ce.ajneb97.manager.MessagesManager;
import ce.ajneb97.model.EventType;
import ce.ajneb97.model.StoredVariable;
import ce.ajneb97.model.internal.ConditionEvent;
import ce.ajneb97.utils.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

@SuppressWarnings("deprecation")
public class PlayerEventsListener implements Listener {

    private final ConditionalEvents plugin;
    private final String prefix = ConditionalEvents.prefix;

    public PlayerEventsListener(ConditionalEvents plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreJoin(AsyncPlayerPreLoginEvent event) {
        new ConditionEvent(plugin, null, event, EventType.PLAYER_PRE_JOIN, null)
                .addVariables(
                        new StoredVariable("%name%", event.getName()),
                        new StoredVariable("%ip%", event.getAddress().getHostAddress()),
                        new StoredVariable("%uuid%", event.getUniqueId().toString())
                )
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        new ConditionEvent(plugin, player, event, EventType.PLAYER_JOIN, null)
                .checkEvent();

        plugin.getPlayerManager().manageJoin(player);

        //Update notification
        String latestVersion = plugin.getUpdateCheckerManager().getLatestVersion();
        if (player.isOp() && !(plugin.version.equals(latestVersion)) &&
                plugin.getConfigsManager().getMainConfigManager().isUpdateNotifications()) {
            player.sendMessage(MessagesManager.getLegacyColoredMessage(prefix + " &cThere is a new version available. &e(&7" + latestVersion + "&e)"));
            player.sendMessage(MessagesManager.getLegacyColoredMessage("&cYou can download it at: &ahttps://modrinth.com/plugin/conditionalevents"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        plugin.getPlayerManager().manageLeave(player);

        new ConditionEvent(plugin, player, event, EventType.PLAYER_LEAVE, null)
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        new ConditionEvent(plugin, player, event, EventType.PLAYER_RESPAWN, null)
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        String cause = "";
        String killerType = "";
        String killerName = "";
        String killerNameColorFormat = "";

        EntityDamageEvent entityDamageEvent = player.getLastDamageCause();
        if (entityDamageEvent != null) {
            EntityDamageEvent.DamageCause damageCause = entityDamageEvent.getCause();
            cause = damageCause.name();

            if (entityDamageEvent instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent event2 = (EntityDamageByEntityEvent) entityDamageEvent;
                Entity killer = event2.getDamager();
                killerType = killer.getType().name();
                if (killer.getCustomName() != null) {
                    if (plugin.getConfigsManager().getMainConfigManager().isUseMiniMessage()) {
                        killerName = MiniMessageUtils.getEntityCustomNamePlain(killer);
                        killerNameColorFormat = MiniMessageUtils.getEntityCustomNameMiniMessage(killer);
                    } else {
                        killerName = ChatColor.stripColor(killer.getCustomName());
                        killerNameColorFormat = killer.getCustomName().replace("§", "&");
                    }
                }
            }
        }

        new ConditionEvent(plugin, player, event, EventType.PLAYER_DEATH, null)
                .addVariables(
                        new StoredVariable("%cause%", cause),
                        new StoredVariable("%killer_type%", killerType),
                        new StoredVariable("%killer_name%", killerName),
                        new StoredVariable("%killer_color_format_name%", killerNameColorFormat)
                )
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        ServerVersion serverVersion = ConditionalEvents.serverVersion;
        if (serverVersion.serverVersionGreaterEqualThan(serverVersion, ServerVersion.v1_10_R1)) {
            if (!event.getAction().equals(Action.PHYSICAL) && (event.getHand() == null || !event.getHand().equals(EquipmentSlot.HAND))) {
                return;
            }
        }
        if (block == null) {
            return;
        }

        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.BLOCK_INTERACT, null);
        if (!conditionEvent.containsValidEvents()) return;
        conditionEvent.setCommonBlockVariables(block)
                .setCommonActionVariables(event.getAction(), player)
                .setCommonItemVariables(player.getItemInHand(), null)
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        ServerVersion serverVersion = ConditionalEvents.serverVersion;
        if (serverVersion.serverVersionGreaterEqualThan(serverVersion, ServerVersion.v1_9_R1) && !event.getHand().equals(EquipmentSlot.HAND)) {
            return;
        }

        LivingEntity target = null;
        if (entity instanceof LivingEntity) {
            target = (LivingEntity) entity;
        }

        new ConditionEvent(plugin, player, event, EventType.ENTITY_INTERACT, target)
                .setCommonEntityVariables(entity)
                .setCommonItemVariables(player.getItemInHand(), null)
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        Entity attacker = event.getDamager();
        Entity damaged = event.getEntity();
        Player player;

        //For projectiles must be the used item to shoot.
        ItemStack item = null;

        String attackType = "PLAYER";
        if (attacker instanceof Projectile) {
            Projectile projectile = (Projectile) attacker;
            if (projectile.getShooter() instanceof Player) {
                attackType = projectile.getType().name();
                player = (Player) projectile.getShooter();

                if (projectile.hasMetadata("conditionaleventes_projectile_item")) {
                    item = (ItemStack) projectile.getMetadata("conditionaleventes_projectile_item").get(0).value();
                }

            } else {
                return;
            }
        } else if (attacker instanceof Player) {
            player = (Player) attacker;
            item = player.getItemInHand();
        } else {
            return;
        }

        LivingEntity target = null;
        if (damaged instanceof LivingEntity) {
            target = (LivingEntity) damaged;
        }


        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.PLAYER_ATTACK, target);
        if (!conditionEvent.containsValidEvents()) return;
        conditionEvent.addVariables(
                        new StoredVariable("%damage%", MathUtils.truncate(event.getFinalDamage()) + ""),
                        new StoredVariable("%original_damage%", MathUtils.truncate(event.getDamage()) + ""),
                        new StoredVariable("%attack_type%", attackType)
                ).setCommonItemVariables(item, null)
                .setCommonVictimVariables(damaged)
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        new ConditionEvent(plugin, player, event, EventType.PLAYER_WORLD_CHANGE, null)
                .addVariables(
                        new StoredVariable("%world_from%", event.getFrom().getName()),
                        new StoredVariable("%world_to%", player.getWorld().getName()),
                        new StoredVariable("%online_players_from%", event.getFrom().getPlayers().size() + ""),
                        new StoredVariable("%online_players_to%", player.getWorld().getPlayers().size() + "")
                ).checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamaged(EntityDamageEvent event) {
        Entity damaged = event.getEntity();
        if (!(damaged instanceof Player)) {
            return;
        }

        Player player = (Player) damaged;
        String cause = event.getCause().toString();
        String damagerType = "";
        String damagerName = "";
        String damagerNameColorFormat = "";
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent event2 = (EntityDamageByEntityEvent) event;
            Entity damager = event2.getDamager();
            damagerType = damager.getType().name();
            if (damager.getCustomName() != null) {
                if (plugin.getConfigsManager().getMainConfigManager().isUseMiniMessage()) {
                    damagerName = MiniMessageUtils.getEntityCustomNamePlain(damager);
                    damagerNameColorFormat = MiniMessageUtils.getEntityCustomNameMiniMessage(damager);
                } else {
                    damagerName = ChatColor.stripColor(damager.getCustomName());
                    damagerNameColorFormat = damager.getCustomName().replace("§", "&");
                }
            }
        }

        new ConditionEvent(plugin, player, event, EventType.PLAYER_DAMAGE, null)
                .addVariables(
                        new StoredVariable("%damager_type%", damagerType),
                        new StoredVariable("%damager_name%", damagerName),
                        new StoredVariable("%damager_color_format_name%", damagerNameColorFormat),
                        new StoredVariable("%damage%", MathUtils.truncate(event.getFinalDamage()) + ""),
                        new StoredVariable("%original_damage%", MathUtils.truncate(event.getDamage()) + ""),
                        new StoredVariable("%cause%", cause)
                ).checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerKill(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getKiller() == null) {
            return;
        }

        Player player = entity.getKiller();

        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.PLAYER_KILL, entity);
        if (!conditionEvent.containsValidEvents()) return;
        conditionEvent.setCommonVictimVariables(entity).
                setCommonItemVariables(player.getItemInHand(), null)
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.BLOCK_PLACE, null);
        if (!conditionEvent.containsValidEvents()) return;
        conditionEvent.setCommonItemVariables(player.getItemInHand(), null)
                .setCommonBlockVariables(block)
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.BLOCK_BREAK, null);
        if (!conditionEvent.containsValidEvents()) return;
        conditionEvent.setCommonItemVariables(player.getItemInHand(), null)
                .setCommonBlockVariables(block)
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandUse(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage();
        String[] args = command.split(" ");

        ArrayList<StoredVariable> eventVariables = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            eventVariables.add(new StoredVariable("%arg_" + (i) + "%", args[i]));
        }

        new ConditionEvent(plugin, player, event, EventType.PLAYER_COMMAND, null)
                .addVariables(
                        new StoredVariable("%command%", command),
                        new StoredVariable("%main_command%", args[0]),
                        new StoredVariable("%args_length%", (args.length - 1) + "")
                ).addVariables(eventVariables)
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        new ConditionEvent(plugin, player, event, EventType.PLAYER_CHAT, null)
                .addVariables(
                        new StoredVariable("%message%", event.getMessage())
                ).setAsync(true).checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLevelChange(PlayerLevelChangeEvent event) {
        Player player = event.getPlayer();

        new ConditionEvent(plugin, player, event, EventType.PLAYER_LEVELUP, null)
                .addVariables(
                        new StoredVariable("%old_level%", event.getOldLevel() + ""),
                        new StoredVariable("%new_level%", event.getNewLevel() + "")
                ).checkEvent();
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArmorEquip(ArmorEquipEvent event) {
        Player player = event.getPlayer();
        String equipType = "EQUIP";
        ItemStack newItem = event.getNewArmorPiece();
        ItemStack previousItem = event.getOldArmorPiece();
        ItemStack selectedItem;
        if (newItem == null || newItem.getType().name().equals("AIR")) {
            equipType = "UNEQUIP";
            selectedItem = previousItem;
        } else {
            selectedItem = newItem;
        }
        String type = "";
        if (event.getType() != null) {
            type = event.getType().name();
        }

        if (newItem != null && !newItem.getType().name().equals("AIR")
                && previousItem != null && !previousItem.getType().name().equals("AIR")) {
            ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.PLAYER_ARMOR, null);
            if (!conditionEvent.containsValidEvents()) return;
            conditionEvent.addVariables(
                            new StoredVariable("%armor_type%", type),
                            new StoredVariable("%equip_type%", "UNEQUIP")
                    ).setCommonItemVariables(previousItem, null)
                    .checkEvent();
        }
        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.PLAYER_ARMOR, null);
        if (!conditionEvent.containsValidEvents()) return;
        conditionEvent.addVariables(
                        new StoredVariable("%armor_type%", type),
                        new StoredVariable("%equip_type%", equipType)
                ).setCommonItemVariables(selectedItem, null)
                .checkEvent();


    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        Location locationFrom = event.getFrom();
        Location locationTo = event.getTo();

        new ConditionEvent(plugin, player, event, EventType.PLAYER_TELEPORT, null)
                .addVariables(
                        new StoredVariable("%cause%", event.getCause() + ""),
                        new StoredVariable("%from_x%", locationFrom.getX() + ""),
                        new StoredVariable("%from_y%", locationFrom.getY() + ""),
                        new StoredVariable("%from_z%", locationFrom.getZ() + ""),
                        new StoredVariable("%from_world%", locationFrom.getWorld().getName()),
                        new StoredVariable("%from_yaw%", locationFrom.getYaw() + ""),
                        new StoredVariable("%from_pitch%", locationFrom.getPitch() + ""),
                        new StoredVariable("%to_x%", locationTo.getX() + ""),
                        new StoredVariable("%to_y%", locationTo.getY() + ""),
                        new StoredVariable("%to_z%", locationTo.getZ() + ""),
                        new StoredVariable("%to_world%", locationTo.getWorld().getName()),
                        new StoredVariable("%to_yaw%", locationTo.getYaw() + ""),
                        new StoredVariable("%to_pitch%", locationTo.getPitch() + "")
                ).checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBedEnter(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();

        if (OtherUtils.isLegacy()) {
            return;
        }

        new ConditionEvent(plugin, player, event, EventType.PLAYER_BED_ENTER, null)
                .addVariables(
                        new StoredVariable("%result%", event.getBedEnterResult() + "")
                ).checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFishEvent(PlayerFishEvent event) {
        Player player = event.getPlayer();
        PlayerFishEvent.State state = event.getState();
        Entity caught = event.getCaught();

        String caughtType = "";
        double x = 0;
        double y = 0;
        double z = 0;

        ItemStack caughtItem = null;
        LivingEntity target = null;
        if (caught != null) {
            caughtType = caught.getType().name();
            Location l = caught.getLocation();
            x = l.getX();
            y = l.getY();
            z = l.getZ();

            if (caught instanceof Item) {
                caughtItem = ((Item) caught).getItemStack();
            }

            if (caught instanceof LivingEntity) {
                target = (LivingEntity) caught;
            }
        }

        new ConditionEvent(plugin, player, event, EventType.PLAYER_FISH, target)
                .addVariables(
                        new StoredVariable("%state%", state.name()),
                        new StoredVariable("%caught_type%", caughtType),
                        new StoredVariable("%caught_x%", x + ""),
                        new StoredVariable("%caught_y%", y + ""),
                        new StoredVariable("%caught_z%", z + "")
                ).setCommonItemVariables(caughtItem, null).checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onOpenInventory(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();

        String inventoryTitle;
        if (plugin.getConfigsManager().getMainConfigManager().isUseMiniMessage()) {
            inventoryTitle = MiniMessageUtils.getInventoryTitlePlain(event);
        } else {
            inventoryTitle = ChatColor.stripColor(InventoryUtils.getViewTitle(event));
        }

        new ConditionEvent(plugin, player, event, EventType.PLAYER_OPEN_INVENTORY, null)
                .addVariables(
                        new StoredVariable("%inventory_type%", event.getInventory().getType().name()),
                        new StoredVariable("%inventory_title%", inventoryTitle)
                ).checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCloseInventory(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        String inventoryTitle;
        if (plugin.getConfigsManager().getMainConfigManager().isUseMiniMessage()) {
            inventoryTitle = MiniMessageUtils.getInventoryTitlePlain(event);
        } else {
            inventoryTitle = ChatColor.stripColor(InventoryUtils.getViewTitle(event));
        }

        new ConditionEvent(plugin, player, event, EventType.PLAYER_CLOSE_INVENTORY, null)
                .addVariables(
                        new StoredVariable("%inventory_type%", event.getInventory().getType().name()),
                        new StoredVariable("%inventory_title%", inventoryTitle)
                ).checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClickInventory(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.PLAYER_CLICK_INVENTORY, null);
        if (!conditionEvent.containsValidEvents()) return;

        String clickType = event.getClick().name();
        String action = event.getAction().name();
        String slotType;
        slotType = event.getSlotType().name();
        String title;
        String titleColorFormat;
        if (plugin.getConfigsManager().getMainConfigManager().isUseMiniMessage()) {
            title = MiniMessageUtils.getInventoryTitlePlain(event);
            titleColorFormat = MiniMessageUtils.getInventoryTitleMiniMessage(event);
        } else {
            title = InventoryUtils.getViewTitle(event);
            titleColorFormat = title.replace("§", "&");
            title = ChatColor.stripColor(title);
        }
        String inventoryType = "";
        int slot = event.getSlot();
        if (event.getClickedInventory() != null) {
            inventoryType = event.getClickedInventory().getType().name();
        }

        conditionEvent.addVariables(
                        new StoredVariable("%inventory_type%", inventoryType),
                        new StoredVariable("%inventory_title%", title),
                        new StoredVariable("%inventory_title_color_format%", titleColorFormat),
                        new StoredVariable("%click_type%", clickType),
                        new StoredVariable("%action_type%", action),
                        new StoredVariable("%slot_type%", slotType),
                        new StoredVariable("%slot%", slot + "")
                ).setCommonItemVariables(event.getCurrentItem(), null)
                .setCommonItemVariables(event.getCursor(), "cursor")
                .checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onStatisticIncrement(PlayerStatisticIncrementEvent event) {
        Player player = event.getPlayer();

        String material = "";
        if (event.getMaterial() != null) {
            material = event.getMaterial().name();
        }
        String entityType = "";
        if (event.getEntityType() != null) {
            entityType = event.getEntityType().name();
        }

        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.PLAYER_STATISTIC, null);
        if (!conditionEvent.containsValidEvents()) return;
        conditionEvent.addVariables(
                new StoredVariable("%statistic_name%", event.getStatistic().name()),
                new StoredVariable("%previous_value%", event.getPreviousValue() + ""),
                new StoredVariable("%new_value%", event.getNewValue() + ""),
                new StoredVariable("%entity%", entityType),
                new StoredVariable("%block%", material)
        ).checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.PLAYER_SNEAK, null);
        if (!conditionEvent.containsValidEvents()) return;
        conditionEvent.addVariables(
                new StoredVariable("%is_sneaking%", event.isSneaking() + "")
        ).checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRun(PlayerToggleSprintEvent event) {
        Player player = event.getPlayer();

        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.PLAYER_RUN, null);
        if (!conditionEvent.containsValidEvents()) return;
        conditionEvent.addVariables(
                new StoredVariable("%is_running%", event.isSprinting() + "")
        ).checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHealthRegain(EntityRegainHealthEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player) entity;

        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.PLAYER_REGAIN_HEALTH, null);
        if (!conditionEvent.containsValidEvents()) return;
        conditionEvent.addVariables(
                new StoredVariable("%reason%", event.getRegainReason().name()),
                new StoredVariable("%amount%", event.getAmount() + "")
        ).checkEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerOffHand(OffHandEvent event) {
        Player player = event.getPlayer();

        ItemStack placedItem = event.getPlacedInOffhand() != null ? event.getPlacedInOffhand() : new ItemStack(Material.AIR);
        ItemStack recoveredItem = event.getRecoveredFromOffhand() != null ? event.getRecoveredFromOffhand() : new ItemStack(Material.AIR);

        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.PLAYER_OFFHAND, null);
        if (!conditionEvent.containsValidEvents()) return;
        conditionEvent.addVariables(
                        new StoredVariable("%movement_type%", event.getMovementType().name())
                ).setCommonItemVariables(placedItem, null).setCommonItemVariables(recoveredItem, "offhand")
                .checkEvent();
    }
}

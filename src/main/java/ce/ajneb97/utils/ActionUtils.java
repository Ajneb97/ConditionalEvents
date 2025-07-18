package ce.ajneb97.utils;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.api.ConditionalEventsAPI;
import ce.ajneb97.api.ConditionalEventsCallEvent;
import ce.ajneb97.libs.actionbar.ActionBarAPI;
import ce.ajneb97.libs.titles.TitleAPI;
import ce.ajneb97.managers.InterruptEventManager;
import ce.ajneb97.managers.MessagesManager;
import ce.ajneb97.managers.dependencies.DiscordSRVManager;
import ce.ajneb97.model.StoredVariable;
import ce.ajneb97.model.internal.ExecutedEvent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class ActionUtils {

    public static void message(Player player,String actionLine){
        player.sendMessage(MessagesManager.getColoredMessage(actionLine));
    }

    public static void centeredMessage(Player player,String actionLine){
        actionLine = MessagesManager.getColoredMessage(actionLine);
        player.sendMessage(MessagesManager.getCenteredMessage(actionLine));
    }

    public static void consoleMessage(String actionLine){
        Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage(actionLine));
    }

    public static void jsonMessage(Player player,String actionLine){
        BaseComponent[] base = ComponentSerializer.parse(actionLine);
        player.spigot().sendMessage(base);
    }

    public static void miniMessage(Player player,String actionLine,ConditionalEvents plugin){
        ServerVersion serverVersion = ConditionalEvents.serverVersion;
        if(plugin.getDependencyManager().isPaper() && serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_19_R3)) {
            player.sendRichMessage(actionLine);
        }
    }

    public static void consoleCommand(String actionLine){
        ConsoleCommandSender sender = Bukkit.getConsoleSender();
        Bukkit.dispatchCommand(sender, actionLine);
    }

    public static void playerCommand(Player player,String actionLine){
        player.performCommand(actionLine);
    }

    public static void playerCommandAsOp(Player player,String actionLine){
        boolean isOp = player.isOp();
        player.setOp(true);
        player.performCommand(actionLine);
        if(!isOp) {
            player.setOp(false);
        }
    }

    public static void playerSendChat(Player player,String actionLine){
        player.chat(MessagesManager.getColoredMessage(actionLine));
    }

    public static void sendToServer(Player player,String actionLine,ConditionalEvents plugin){
        plugin.getBungeeMessagingManager().sendToServer(player,actionLine);
    }

    public static void teleport(Player player, String actionLine, Event minecraftEvent){
        String[] sep = actionLine.split(";");
        World world = Bukkit.getWorld(sep[0]);
        double x = Double.valueOf(sep[1]);
        double y = Double.valueOf(sep[2]);
        double z = Double.valueOf(sep[3]);
        float yaw = Float.valueOf(sep[4]);
        float pitch = Float.valueOf(sep[5]);
        Location l = new Location(world,x,y,z,yaw,pitch);

        if(minecraftEvent instanceof PlayerRespawnEvent) {
            PlayerRespawnEvent respawnEvent = (PlayerRespawnEvent) minecraftEvent;
            respawnEvent.setRespawnLocation(l);
        }else {
            player.teleport(l);
        }
    }

    public static void removeItemSlot(Player player,String actionLine){
        // remove_item_slot: <slot>;<amount>
        // slot: HAND,OFF_HAND,CHEST,FEET,HEAD,LEGS
        String[] sep = actionLine.split(";");
        String slot = sep[0];
        int amount = Integer.parseInt(sep[1]);

        ItemStack item = PlayerUtils.getItemBySlot(player,slot);
        if(item != null){
            int newAmount = item.getAmount()-amount;
            if(newAmount <= 0){
                PlayerUtils.setItemBySlot(player, slot, null);
            }else{
                item.setAmount(newAmount);
            }
            PlayerUtils.updatePlayerInventory(player);
        }
    }
    public static void removeItem(Player player,String actionLine){
        // remove_item: <id>;<amount>;datavalue: <datavalue>;name: <name>;lorecontains: <lore_line>
        // remove_item: %checkitem_remove...%
        if(actionLine.equals("no") || actionLine.equals("yes")) {
            //Variable already replaced and executed.
            return;
        }

        String[] sep = actionLine.split(";");
        String material = sep[0];
        int amount = Integer.valueOf(sep[1]);
        short datavalue = 0;
        String name = null;
        String loreContainsLoreLine = null;

        for(String sepLine : sep) {
            if(sepLine.startsWith("datavalue: ")) {
                datavalue = Short.valueOf(sepLine.replace("datavalue: ", ""));
            }else if(sepLine.startsWith("name: ")) {
                name = sepLine.replace("name: ", "");
            }else if(sepLine.startsWith("lorecontains: ")) {
                loreContainsLoreLine = sepLine.replace("lorecontains: ", "");
            }
        }

        ItemStack[] contents = player.getInventory().getContents();
        for(int i=0;i<contents.length;i++) {
            if(contents[i] != null && !contents[i].getType().equals(Material.AIR)) {
                if(!contents[i].getType().name().equals(material)) {
                    continue;
                }
                if(contents[i].getDurability() != datavalue) {
                    continue;
                }
                if(contents[i].hasItemMeta()) {
                    ItemMeta meta = contents[i].getItemMeta();
                    if(meta.hasDisplayName() && name != null
                        && !ChatColor.stripColor(meta.getDisplayName()).equals(ChatColor.stripColor(name))) {
                        continue;
                    }

                    if(meta.hasLore()) {
                        if(loreContainsLoreLine != null) {
                            List<String> lore = meta.getLore();
                            boolean contains = false;
                            for(String linea : lore) {
                                if(ChatColor.stripColor(linea).contains(loreContainsLoreLine)) {
                                    contains = true;
                                    break;
                                }
                            }
                            if(!contains) {
                                continue;
                            }
                        }
                    }
                }else {
                    if(name != null || loreContainsLoreLine != null) {
                        continue;
                    }
                }

                int currentAmount = contents[i].getAmount();
                if(currentAmount > amount) {
                    contents[i].setAmount(currentAmount-amount);
                    break;
                }else {
                    amount = amount-currentAmount;
                    if(!OtherUtils.isLegacy()) {
                        contents[i].setAmount(0);
                    }else {
                        player.getInventory().setItem(i, null);
                    }
                }
            }
        }
    }

    public static void givePotionEffect(Player player,String actionLine){
        String[] sep = actionLine.split(";");
        PotionEffectType potionEffectType = PotionEffectType.getByName(sep[0]);
        int duration = Integer.valueOf(sep[1]);
        int level = Integer.valueOf(sep[2])-1;
        boolean showParticles = true;
        if(sep.length >= 4) {
            showParticles = Boolean.valueOf(sep[3]);
        }
        PotionEffect effect = new PotionEffect(potionEffectType,duration,level,false,showParticles);
        player.addPotionEffect(effect);
    }

    public static void removePotionEffect(Player player,String actionLine){
        if(actionLine.equals("all")){
            for(PotionEffect effect : player.getActivePotionEffects()){
                player.removePotionEffect(effect.getType());
            }
        }else{
            PotionEffectType potionEffectType = PotionEffectType.getByName(actionLine);
            player.removePotionEffect(potionEffectType);
        }
    }

    public static void cancelEvent(String actionLine,Event minecraftEvent){
        boolean cancel = Boolean.valueOf(actionLine);
        if(minecraftEvent != null && minecraftEvent instanceof Cancellable) {
            Cancellable cancellableEvent = (Cancellable) minecraftEvent;
            cancellableEvent.setCancelled(cancel);
        }
    }

    public static void kick(Player player,String actionLine){
        player.kickPlayer(MessagesManager.getColoredMessage(actionLine));
    }

    public static void playSound(Player player,String actionLine){
        // playsound: sound;volume;pitch;(optional)<x>,<y>,<z>,<world>
        String[] sep = actionLine.split(";");
        Sound sound = null;
        float volume = 0;
        float pitch = 0;
        try {
            sound = getSoundByName(sep[0]);
            volume = Float.parseFloat(sep[1]);
            pitch = Float.parseFloat(sep[2]);
        }catch(Exception e ) {
            Bukkit.getConsoleSender().sendMessage(ConditionalEvents.prefix+
                    MessagesManager.getColoredMessage(" &7Sound Name: &c"+sep[0]+" &7is not valid. Change it in the config!"));
            return;
        }

        Location location = null;
        if(sep.length >= 4){
            String[] locParameters = sep[3].split(",");
            location = new Location(
                    Bukkit.getWorld(locParameters[3]),
                    Double.parseDouble(locParameters[0]),
                    Double.parseDouble(locParameters[1]),
                    Double.parseDouble(locParameters[2])
            );
        }

        if(location != null){
            location.getWorld().playSound(location,sound,volume,pitch);
        }else{
            player.playSound(player.getLocation(), sound, volume, pitch);
        }

    }

    public static void playSoundResourcePack(Player player,String actionLine){
        // playsound_resource_pack: sound;volume;pitch;(optional)<x>,<y>,<z>,<world>
        String[] sep = actionLine.split(";");
        String sound = sep[0];
        float volume = Float.parseFloat(sep[1]);
        float pitch = Float.parseFloat(sep[2]);

        Location location = null;
        if(sep.length >= 4){
            String[] locParameters = sep[3].split(",");
            location = new Location(
                    Bukkit.getWorld(locParameters[3]),
                    Double.parseDouble(locParameters[0]),
                    Double.parseDouble(locParameters[1]),
                    Double.parseDouble(locParameters[2])
            );
        }

        if(location != null){
            location.getWorld().playSound(location,sound,volume,pitch);
        }else{
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    public static void stopSound(Player player,String actionLine){
        // stopsound: sound/all
        ServerVersion serverVersion = ConditionalEvents.serverVersion;
        if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_10_R1)) {
            Sound sound = null;
            if(actionLine.equals("all")){
                if(!serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_17_R1)){
                    return;
                }
                player.stopAllSounds();
            }else{
                try {
                    sound = getSoundByName(actionLine);
                }catch(Exception e ) {
                    Bukkit.getConsoleSender().sendMessage(ConditionalEvents.prefix+
                            MessagesManager.getColoredMessage(" &7Sound Name: &c"+actionLine+" &7is not valid. Change it in the config!"));
                    return;
                }
                player.stopSound(sound);
            }

        }
    }

    private static Sound getSoundByName(String name){
        try {
            Class<?> soundTypeClass = Class.forName("org.bukkit.Sound");
            Method valueOf = soundTypeClass.getMethod("valueOf", String.class);
            return (Sound) valueOf.invoke(null,name);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void giveItem(Player player,String actionLine){
        // give_item: id:<id>;amount:<amount>;durability:<durability>;custom_model_data:<data>;name:<name>;
        // lore:<lore_line1>|<lore_lineN>;enchants:<name1>-<level1>|<nameN>-<levelN>;
        // flags:<flag1>|<flag2>
        // slot:<slot>

        // give_item: saved_item:<name>

        String[] sep = actionLine.replace("give_item: ","").split(";");
        ItemStack item = ItemUtils.getItemFromProperties(sep,player);

        String slot = null;
        boolean replace = true;
        for(String property : sep) {
            if(property.startsWith("slot:")){
                slot = property.replace("slot:", "");
            }else if(property.startsWith("slot_replace:")){
                replace = Boolean.parseBoolean(property.replace("slot_replace:",""));
            }
        }

        if(slot != null){
            ItemStack itemSlot = PlayerUtils.getItemBySlot(player,slot);
            if(replace){
                PlayerUtils.setItemBySlot(player,slot,item);
            }else{
                if(itemSlot == null || itemSlot.getType().equals(Material.AIR)){
                    // If empty, set
                    PlayerUtils.setItemBySlot(player,slot,item);
                }else if(itemSlot != null && itemSlot.isSimilar(item)){
                    // If same item, increase
                    int newAmount = itemSlot.getAmount()+1;
                    if(newAmount <= itemSlot.getType().getMaxStackSize()){
                        itemSlot.setAmount(newAmount);
                    }
                }
            }
            PlayerUtils.updatePlayerInventory(player);
        }else{
            player.getInventory().addItem(item);
        }
    }

    public static void dropItem(String actionLine) {
        // drop_item: location:<x>,<y>,<z>,<world>;id:<id>;amount:<amount>;...

        String[] sep = actionLine.replace("drop_item: ","").split(";");
        ItemStack item = ItemUtils.getItemFromProperties(sep,null);
        Location location = null;

        // Find location
        for(String property : sep){
            if(property.startsWith("location:")){
                String[] locationSplit = property.replace("location:", "").split(",");
                location = new Location(
                        Bukkit.getWorld(locationSplit[3]),
                        Double.parseDouble(locationSplit[0]),
                        Double.parseDouble(locationSplit[1]),
                        Double.parseDouble(locationSplit[2])
                );
                break;
            }
        }

        //Drop Item
        if(location != null){
            location.getWorld().dropItemNaturally(location,item);
        }
    }

    public static void setItem(String actionLine,Event minecraftEvent){
        String[] sep = actionLine.replace("set_item: ","").split(";");
        if(minecraftEvent instanceof PlayerFishEvent){
            PlayerFishEvent event = (PlayerFishEvent) minecraftEvent;
            Entity caught = event.getCaught();
            if(caught != null && caught instanceof Item){
                Item item = (Item) caught;
                item.setItemStack(ItemUtils.getItemFromProperties(sep,null));
            }
        }
    }

    public static void tabComplete(String actionLine,Event minecraftEvent){
        // tab_complete: <mode>;<element1>,<element2>,<elementN>
        String[] sep = actionLine.replace("tab_complete: ","").split(";");

        String mode = sep[0];
        List<String> completions = new ArrayList<>();
        if(sep.length > 1){
            completions = Arrays.asList(sep[1].split(","));
        }

        if(minecraftEvent instanceof TabCompleteEvent) {
            TabCompleteEvent event = (TabCompleteEvent) minecraftEvent;
            if (mode.equalsIgnoreCase("remove")) {
                event.getCompletions().removeAll(completions);
            } else if (mode.equalsIgnoreCase("clear")) {
                event.getCompletions().clear();
            } else if (mode.equalsIgnoreCase("set")) {
                String[] buffer_words = event.getBuffer().split(" ", -1); // Don't trim empties
                String partial_word = buffer_words[buffer_words.length - 1];
                event.getCompletions().addAll(completions.stream().filter(w -> w.startsWith(partial_word)).collect(Collectors.toList()));
                event.getCompletions().sort(Comparator.naturalOrder());
            }
        }
    }

    public static void setBlock(String actionLine){
        // set_block: location:<x>,<y>,<z>,<world>;id:<id>
        String[] sep = actionLine.replace("set_block: ","").split(";");
        Location location = null;
        Material material = Material.AIR;
        String blockData = null;
        String skullTexture = null;
        String skullOwner = null;

        for(String property : sep){
            if(property.startsWith("location:")){
                String[] locationSplit = property.replace("location:", "").split(",");
                location = new Location(
                        Bukkit.getWorld(locationSplit[3]),
                        Double.parseDouble(locationSplit[0]),
                        Double.parseDouble(locationSplit[1]),
                        Double.parseDouble(locationSplit[2])
                );
            }else if(property.startsWith("id:")){
                material = Material.valueOf(property.replace("id:",""));
            }else if(property.startsWith("block_data:")){
                blockData = property.replace("block_data:","");
            }else if(property.startsWith("skull_texture:")) {
                skullTexture = property.replace("skull_texture:", "");
            }else if(property.startsWith("skull_owner:")) {
                skullOwner = property.replace("skull_owner:", "");
            }
        }

        if(location != null){
            Block block = location.getWorld().getBlockAt(location);
            block.setType(material);
            if(blockData != null){
                block.setBlockData(BlockUtils.getBlockDataFromString(blockData,material));
            }
            if(skullTexture != null || skullOwner != null){
                BlockUtils.setHeadTextureData(block,skullTexture,skullOwner);
            }
        }
    }

    public static void lightningStrike(String actionLine) {
        // lightning_strike: <world>;<x>;<y>;<z>
        String[] sep = actionLine.split(";");
        World world = Bukkit.getWorld(sep[0]);
        double x = Double.parseDouble(sep[1]);
        double y = Double.parseDouble(sep[2]);
        double z = Double.parseDouble(sep[3]);
        Location l = new Location(world,x,y,z);

        l.getWorld().strikeLightningEffect(l);
    }

    public static void summon(String actionLine) {
        // summon: location:<x>,<y>,<z>,<world>;entity:<id>,<property>:<value>
        // custom_name:<value>
        // health:<value>
        // equipment:<helmet>,<chestplate>,<leggings>,<boots> (material or 'none')
        // amount:<amount>
        // hand_equipment:<mainhand>,<offhand>

        String[] sep = actionLine.replace("summon: ","").split(";");
        Location location = null;
        EntityType type = null;

        String customName = null;
        double health = 0;
        String equipmentString = null;
        String handEquipmentString = null;
        int amount = 1;

        for(String property : sep){
            if(property.startsWith("location:")){
                String[] locationSplit = property.replace("location:", "").split(",");
                location = new Location(
                        Bukkit.getWorld(locationSplit[3]),
                        Double.parseDouble(locationSplit[0]),
                        Double.parseDouble(locationSplit[1]),
                        Double.parseDouble(locationSplit[2])
                );
            }else if(property.startsWith("entity:")){
                type = EntityType.valueOf(property.replace("entity:",""));
            }else if(property.startsWith("custom_name:")){
                customName = property.replace("custom_name:","");
            }else if(property.startsWith("health:")){
                health = Double.parseDouble(property.replace("health:",""));
            }else if(property.startsWith("equipment:")){
                equipmentString = property.replace("equipment:","");
            }else if(property.startsWith("hand_equipment:")){
                handEquipmentString = property.replace("hand_equipment:","");
            }else if(property.startsWith("amount:")){
                amount = Integer.parseInt(property.replace("amount:",""));
            }
        }

        if(location != null){
            for(int i=0;i<amount;i++){
                Entity entity = location.getWorld().spawnEntity(location,type);
                if(customName != null){
                    entity.setCustomNameVisible(true);
                    entity.setCustomName(MessagesManager.getColoredMessage(customName));
                }

                if(entity instanceof LivingEntity){
                    LivingEntity livingEntity = (LivingEntity) entity;
                    if(health != 0){
                        livingEntity.setMaxHealth(health);
                        livingEntity.setHealth(health);
                    }

                    EntityEquipment equipment = livingEntity.getEquipment();
                    if(equipmentString != null){
                        String[] equipmentSplit = equipmentString.split(",");

                        //Helmet
                        equipment.setHelmet(!equipmentSplit[0].equals("none") ? ItemUtils.createItemFromString(equipmentSplit[0],null) : null);
                        equipment.setHelmetDropChance(0);
                        //Chestplate
                        equipment.setChestplate(!equipmentSplit[1].equals("none") ? ItemUtils.createItemFromString(equipmentSplit[1],null) : null);
                        equipment.setChestplateDropChance(0);
                        //Leggings
                        equipment.setLeggings(!equipmentSplit[2].equals("none") ? ItemUtils.createItemFromString(equipmentSplit[2],null) : null);
                        equipment.setLeggingsDropChance(0);
                        //Boots
                        equipment.setBoots(!equipmentSplit[3].equals("none") ? ItemUtils.createItemFromString(equipmentSplit[3],null) : null);
                        equipment.setBootsDropChance(0);
                    }

                    if(handEquipmentString != null){
                        String[] handEquipmentSplit = handEquipmentString.split(",");

                        // Hand
                        equipment.setItemInMainHand(!handEquipmentSplit[0].equals("none") ? ItemUtils.createItemFromString(handEquipmentSplit[0],null) : null);
                        equipment.setItemInMainHandDropChance(0);
                        // Offhand
                        equipment.setItemInOffHand(!handEquipmentSplit[1].equals("none") ? ItemUtils.createItemFromString(handEquipmentSplit[1],null) : null);
                        equipment.setItemInOffHandDropChance(0);
                    }
                }

            }
        }
    }

    public static void actionbar(Player player,String actionLine,ConditionalEvents plugin){
        String[] sep = actionLine.split(";");
        String text = sep[0];
        int duration = Integer.valueOf(sep[1]);
        ActionBarAPI.sendActionBar(player,text,duration,plugin);
    }

    public static void title(Player player,String actionLine){
        String[] sep = actionLine.split(";");
        int fadeIn = Integer.valueOf(sep[0]);
        int stay = Integer.valueOf(sep[1]);
        int fadeOut = Integer.valueOf(sep[2]);

        String title = sep[3];
        String subtitle = sep[4];
        if(title.equals("none")) {
            title = "";
        }
        if(subtitle.equals("none")) {
            subtitle = "";
        }
        TitleAPI.sendTitle(player,fadeIn,stay,fadeOut,title,subtitle);
    }

    /*
    public static void vector(Player player,String actionLine){
        // vector: <front>;<height>
        String[] sep = actionLine.split(";");
        double front = Double.parseDouble(sep[0]);
        double height = Double.parseDouble(sep[1]);

        Vector direction = player.getLocation().getDirection().normalize();
        Vector newDirection = new Vector(direction.getX()*front,height,direction.getZ()*front);
        player.setVelocity(player.getVelocity().add(newDirection));
    }*/
    public static void firework(Player player,String actionLine,ConditionalEvents plugin){
        // firework: colors:<color1>,<color2> type:<type> fade:<color1>,<color2> power:<power> location(optional):<x>;<y>;<z>;<world>
        // flicker:<boolean> trail:<boolean>
        // shot_direction:<off_x_min>,<off_x_max>;<off_y_min>,<off_y_max>;<off_z_min>,<off_z_max>;<velocity_min>-<velocity_max>
        ArrayList<Color> colors = new ArrayList<>();
        FireworkEffect.Type type = null;
        ArrayList<Color> fadeColors = new ArrayList<>();
        Location location = null;
        int power = 0;
        boolean hasTrail = false;
        boolean hasFlicker = false;
        String shotDirection = null;

        String[] sep = actionLine.split(" ");
        for(String s : sep) {
            if(s.startsWith("colors:")) {
                s = s.replace("colors:", "");
                String[] colorsSep = s.split(",");
                for(String colorSep : colorsSep) {
                    colors.add(OtherUtils.getFireworkColorFromName(colorSep));
                }
            }else if(s.startsWith("type:")) {
                s = s.replace("type:", "");
                type = FireworkEffect.Type.valueOf(s);
            }else if(s.startsWith("fade:")) {
                s = s.replace("fade:", "");
                String[] colorsSep = s.split(",");
                for(String colorSep : colorsSep) {
                    fadeColors.add(OtherUtils.getFireworkColorFromName(colorSep));
                }
            }else if(s.startsWith("power:")) {
                s = s.replace("power:", "");
                power = Integer.parseInt(s);
            }else if(s.startsWith("trail:")){
                s = s.replace("trail:", "");
                hasTrail = Boolean.parseBoolean(s);
            }else if(s.startsWith("flicker:")){
                s = s.replace("flicker:", "");
                hasFlicker = Boolean.parseBoolean(s);
            }else if(s.startsWith("shot_direction:")){
                s = s.replace("shot_direction:", "");
                shotDirection = s;
            }else if(s.startsWith("location:")) {
                String[] sep2 = s.replace("location:", "").split(";");
                location = new Location(
                   Bukkit.getWorld(sep2[3]), Double.parseDouble(sep2[0]), Double.parseDouble(sep2[1]), Double.parseDouble(sep2[2])
                );
            }
        }

        if(location == null){
            location = player.getLocation();
        }

        ServerVersion serverVersion = ConditionalEvents.serverVersion;
        EntityType entityType = null;
        if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_20_R4)){
            entityType = EntityType.FIREWORK_ROCKET;
        }else{
            entityType = EntityType.valueOf("FIREWORK");
        }
        Firework firework = (Firework) location.getWorld().spawnEntity(location, entityType);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();
        FireworkEffect effect = FireworkEffect.builder().flicker(false)
                .withColor(colors)
                .with(type)
                .withFade(fadeColors)
                .trail(hasTrail)
                .flicker(hasFlicker)
                .build();

        fireworkMeta.addEffect(effect);
        fireworkMeta.setPower(power);
        firework.setFireworkMeta(fireworkMeta);

        // 1.15+
        if(shotDirection != null && serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_15_R1)){
            firework.setShotAtAngle(true);
            String[] sepDirection = shotDirection.split(";");
            String[] offsetX = sepDirection[0].split(",");
            String[] offsetY = sepDirection[1].split(",");
            String[] offsetZ = sepDirection[2].split(",");
            String[] offsetVelocity = sepDirection[3].split("-");

            Vector direction = new Vector(
                    MathUtils.getRandomNumberFloat(Float.parseFloat(offsetX[0]),Float.parseFloat(offsetX[1])),
                    MathUtils.getRandomNumberFloat(Float.parseFloat(offsetY[0]),Float.parseFloat(offsetY[1])),
                    MathUtils.getRandomNumberFloat(Float.parseFloat(offsetZ[0]),Float.parseFloat(offsetZ[1]))
            );

            firework.setVelocity(direction.multiply(MathUtils.getRandomNumberFloat(Float.parseFloat(offsetVelocity[0]),Float.parseFloat(offsetVelocity[1]))));
        }

        firework.setMetadata("conditionalevents", new FixedMetadataValue(plugin, "no_damage"));
    }

    public static void particle(Player player,String actionLine){
        // particle: effect:<effect_name> offset:<x>;<y>;<z> speed:<speed> amount:<amount> location(optional):<x>;<y>;<z>;<world>
        //              force:<true/false>
        String effectName = null;
        double offsetX = 0;double offsetY = 0;double offsetZ = 0;
        double speed = 0;
        int amount = 1;
        Location location = null;
        boolean force = false;

        String[] sep = actionLine.split(" ");
        for(String s : sep) {
            if(s.startsWith("effect:")) {
                effectName = s.replace("effect:", "");
            }else if(s.startsWith("speed:")) {
                speed = Double.parseDouble(s.replace("speed:", ""));
            }else if(s.startsWith("amount:")) {
                amount = Integer.parseInt(s.replace("amount:", ""));
            }else if(s.startsWith("offset:")) {
                String[] sep2 = s.replace("offset:", "").split(";");
                offsetX = Double.parseDouble(sep2[0]);
                offsetY = Double.parseDouble(sep2[1]);
                offsetZ = Double.parseDouble(sep2[2]);
            }else if(s.startsWith("location:")) {
                String[] sep2 = s.replace("location:", "").split(";");
                location = new Location(
                        Bukkit.getWorld(sep2[3]), Double.parseDouble(sep2[0]), Double.parseDouble(sep2[1]), Double.parseDouble(sep2[2])
                );
            }else if(s.startsWith("force:")) {
                force = Boolean.parseBoolean(s.replace("force:",""));
            }
        }

        if(location == null){
            location = player.getLocation();
        }

        ServerVersion serverVersion = ConditionalEvents.serverVersion;
        if(!serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_9_R1)) {
            // 1.8
            return;
        }

        try {
            if(effectName.startsWith("REDSTONE;") || effectName.startsWith("DUST;")) {
                String[] effectSeparated = effectName.split(";");
                int red = Integer.parseInt(effectSeparated[1]);
                int green = Integer.parseInt(effectSeparated[2]);
                int blue = Integer.parseInt(effectSeparated[3]);
                Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(red,green,blue), 1);

                location.getWorld().spawnParticle(
                        Particle.valueOf(effectSeparated[0]),location,amount,offsetX,offsetY,offsetZ,speed,dustOptions,force);
            }else {
                location.getWorld().spawnParticle(
                        Particle.valueOf(effectName),location,amount,offsetX,offsetY,offsetZ,speed,null,force);
            }
        }catch(Exception e) {
            Bukkit.getConsoleSender().sendMessage(ConditionalEvents.prefix+
                    MessagesManager.getColoredMessage(" &7Particle Name: &c"+effectName+" &7is not valid. Change it in the config!"));
        }
    }

    public static void damage(Player player,String actionLine){
        player.damage(Double.parseDouble(actionLine));
    }

    public static void gamemode(Player player,String actionLine){
        player.setGameMode(GameMode.valueOf(actionLine));
    }

    public static void closeInventory(Player player){
        player.closeInventory();
    }

    public static void clearInventory(Player player){
        player.getInventory().clear();
    }

    public static void setOnFire(Player player,String actionLine){
        // set_on_fire: <duration_on_ticks>
        player.setFireTicks(Integer.parseInt(actionLine));
        
    }

    public static void freeze(Player player,String actionLine){
        // freeze: <duration_on_ticks>
        ServerVersion serverVersion = ConditionalEvents.serverVersion;
        if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_17_R1)) {
            player.setFreezeTicks(Integer.parseInt(actionLine));
        }
    }

    public static void heal(Player player,String actionLine){
        // heal: <amount>
        //double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double maxHealth = player.getMaxHealth();
        double currentHealth = player.getHealth();
        double newHealth = currentHealth+Double.parseDouble(actionLine);
        if(newHealth >= maxHealth){
            player.setHealth(maxHealth);
        }else{
            player.setHealth(newHealth);
        }
    }

    public static void setFoodLevel(Player player,String actionLine){
        player.setFoodLevel(Integer.parseInt(actionLine));
    }

    public static void wait(String actionLine, ExecutedEvent executedEvent){
        executedEvent.setOnWait(true);
        int timeSeconds = Integer.parseInt(actionLine);

        InterruptEventManager interruptEventManager = ConditionalEventsAPI.getPlugin().getInterruptEventManager();
        BukkitTask task = new BukkitRunnable(){
            @Override
            public void run() {
                interruptEventManager.removeTaskById(this.getTaskId());
                executedEvent.continueWithActions();
            }
        }.runTaskLater(executedEvent.getPlugin(), timeSeconds* 20L);

        interruptEventManager.addTask(
                executedEvent.getPlayer() != null ? executedEvent.getPlayer().getName() : null,
                executedEvent.getEvent().getName(),
                task
        );
    }

    public static void waitTicks(String actionLine, ExecutedEvent executedEvent){
        executedEvent.setOnWait(true);
        long timeTicks = Long.parseLong(actionLine);

        InterruptEventManager interruptEventManager = ConditionalEventsAPI.getPlugin().getInterruptEventManager();
        BukkitTask task = new BukkitRunnable(){
            @Override
            public void run() {
                interruptEventManager.removeTaskById(this.getTaskId());
                executedEvent.continueWithActions();
            }
        }.runTaskLater(executedEvent.getPlugin(), timeTicks);

        interruptEventManager.addTask(
                executedEvent.getPlayer() != null ? executedEvent.getPlayer().getName() : null,
                executedEvent.getEvent().getName(),
                task
        );
    }

    public static void keepItems(String actionLine,Event minecraftEvent){
        if(minecraftEvent instanceof PlayerDeathEvent) {
            PlayerDeathEvent deathEvent = (PlayerDeathEvent) minecraftEvent;
            if(actionLine.equals("items") || actionLine.equals("all")) {
                deathEvent.setKeepInventory(true);
                deathEvent.getDrops().clear();
            }
            if(actionLine.equals("xp") || actionLine.equals("all")) {
                deathEvent.setKeepLevel(true);
                deathEvent.setDroppedExp(0);
            }
        }
    }

    public static void cancelDrop(String actionLine,Event minecraftEvent){
        if(minecraftEvent instanceof BlockBreakEvent) {
            if(OtherUtils.isLegacy()){
                return;
            }
            BlockBreakEvent blockBreakEvent = (BlockBreakEvent) minecraftEvent;
            boolean cancel = Boolean.parseBoolean(actionLine);
            if(cancel){
                blockBreakEvent.setDropItems(false);
            }
        }else if(minecraftEvent instanceof EntityDeathEvent){
            EntityDeathEvent entityDeathEvent = (EntityDeathEvent) minecraftEvent;
            boolean cancel = Boolean.parseBoolean(actionLine);
            if(cancel){
                entityDeathEvent.getDrops().clear();
            }
        }
    }

    public static void setDamage(String actionLine,Event minecraftEvent){
        if(minecraftEvent instanceof EntityDamageEvent) {
            EntityDamageEvent damageEvent = (EntityDamageEvent) minecraftEvent;
            double damage = damageEvent.getDamage();
            if(actionLine.contains("%")){
                double modifier = Double.parseDouble(actionLine.substring(0,actionLine.length()-1));
                double finalModifier = modifier/100;
                damage = damage*finalModifier;
            }else{
                damage = Double.parseDouble(actionLine);
            }

            damageEvent.setDamage(damage);
        }
    }

    public static void hideJoinMessage(String actionLine,Event minecraftEvent){
        if(minecraftEvent instanceof PlayerJoinEvent) {
            PlayerJoinEvent joinEvent = (PlayerJoinEvent) minecraftEvent;
            boolean hideMessage = Boolean.parseBoolean(actionLine);
            if(hideMessage){
                joinEvent.setJoinMessage(null);
            }
        }
    }

    public static void hideLeaveMessage(String actionLine,Event minecraftEvent){
        if(minecraftEvent instanceof PlayerQuitEvent) {
            PlayerQuitEvent quitEvent = (PlayerQuitEvent) minecraftEvent;
            boolean hideMessage = Boolean.parseBoolean(actionLine);
            if(hideMessage){
                quitEvent.setQuitMessage(null);
            }
        }
    }

    public static void setDeathMessage(String actionLine,Event minecraftEvent){
        if(minecraftEvent instanceof PlayerDeathEvent) {
            PlayerDeathEvent deathEvent = (PlayerDeathEvent) minecraftEvent;
            if(actionLine.equals("no")){
                deathEvent.setDeathMessage(null);
            }else{
                deathEvent.setDeathMessage(MessagesManager.getColoredMessage(actionLine));
            }
        }
    }

    public static void preventJoin(String actionLine,Event minecraftEvent){
        // prevent_join: <message>
        if(minecraftEvent instanceof AsyncPlayerPreLoginEvent) {
            AsyncPlayerPreLoginEvent preJoinEvent = (AsyncPlayerPreLoginEvent) minecraftEvent;
            preJoinEvent.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            preJoinEvent.setKickMessage(MessagesManager.getColoredMessage(actionLine));
        }
    }

    public static void discordSRVEmbed(String actionLine,ConditionalEvents plugin){
        DiscordSRVManager discordSRVManager = plugin.getDependencyManager().getDiscordSRVManager();
        if(discordSRVManager != null){
            discordSRVManager.sendEmbedMessage(actionLine);
        }
    }

    public static boolean callEvent(String actionLine,Player player,ConditionalEvents plugin,ArrayList<StoredVariable> storedVariables){
        // call_event: <event>;%variable1%=<value1>;%variable2%=<value2>
        // call_event: <event>;%variable1%=<value1>;%variable2%=<value2>;already_stored
        ArrayList<StoredVariable> variables = new ArrayList<>();
        String eventName = null;
        try{
            String[] sep = actionLine.split(";");
            eventName = sep[0];
            if(sep.length > 1){
                for(int i=1;i<sep.length;i++){
                    if(sep[i].equals("already_stored")){
                        if(storedVariables != null){
                            variables.addAll(storedVariables);
                        }
                        continue;
                    }

                    String[] variableLineSep = sep[i].split("=");
                    variables.add(new StoredVariable(variableLineSep[0],variableLineSep[1]));
                }
            }
        }catch(Exception e){
            return false;
        }

        ConditionalEventsCallEvent event = new ConditionalEventsCallEvent(player,variables,eventName);
        plugin.getServer().getPluginManager().callEvent(event);
        return true;
    }

    public static void executeActionGroup(String actionLine,ExecutedEvent executedEvent,ConditionalEvents plugin){
        // execute_action_group: <group1>:<prob1>;<group2>:<prob2>
        ArrayList<String> actionGroups = new ArrayList<String>();
        ArrayList<Integer> probs = new ArrayList<Integer>();

        String[] sep = actionLine.split(";");
        for(String line : sep){
            String[] sep2 = line.split(":");
            actionGroups.add(sep2[0]);
            probs.add(Integer.parseInt(sep2[1]));
        }

        for(int i=0;i<probs.size();i++){
            if(i != 0){
                probs.set(i, probs.get(i-1)+probs.get(i));
            }
        }
        String actionGroup = null;

        Random random = new Random();
        int max = probs.get(probs.size()-1);
        int randomNumber = random.nextInt(max);
        for(int i=0;i<probs.size();i++){
            if(i==0){
                if(randomNumber < probs.get(i)){
                    actionGroup = actionGroups.get(i);
                }
            }else{
                if(randomNumber > probs.get(i-1) && randomNumber <= probs.get(i)){
                    actionGroup = actionGroups.get(i);
                }
            }
        }
        if(actionGroup == null){
            actionGroup = actionGroups.get(0);
        }

        //Clone ExecutedEvent
        new ExecutedEvent(
                executedEvent.getPlayer(),
                executedEvent.getEventVariables(),
                executedEvent.getEvent(),
                actionGroup,
                executedEvent.getMinecraftEvent(),
                executedEvent.getTarget(),
                plugin
        ).executeActions();
    }

    public static void setEventXp(String actionLine,Event minecraftEvent){
        int xp = Integer.parseInt(actionLine.replace("set_event_xp: ",""));
        if(minecraftEvent instanceof PlayerFishEvent){
            PlayerFishEvent event = (PlayerFishEvent) minecraftEvent;
            event.setExpToDrop(xp);
        }else if(minecraftEvent instanceof BlockBreakEvent){
            BlockBreakEvent event = (BlockBreakEvent) minecraftEvent;
            event.setExpToDrop(xp);
        }
    }
}

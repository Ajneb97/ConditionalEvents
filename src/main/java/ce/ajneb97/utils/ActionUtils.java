package ce.ajneb97.utils;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.libs.actionbar.ActionBarAPI;
import ce.ajneb97.libs.titles.TitleAPI;
import ce.ajneb97.managers.MessagesManager;
import ce.ajneb97.model.internal.ExecutedEvent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.*;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class ActionUtils {
    private ActionUtils() {}

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
        double x = Double.parseDouble(sep[1]);
        double y = Double.parseDouble(sep[2]);
        double z = Double.parseDouble(sep[3]);
        float yaw = Float.parseFloat(sep[4]);
        float pitch = Float.parseFloat(sep[5]);
        Location l = new Location(world,x,y,z,yaw,pitch);

        if(minecraftEvent instanceof PlayerRespawnEvent) {
            PlayerRespawnEvent respawnEvent = (PlayerRespawnEvent) minecraftEvent;
            respawnEvent.setRespawnLocation(l);
        }else {
            player.teleport(l);
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
        int amount = Integer.parseInt(sep[1]);
        short datavalue = 0;
        String name = null;
        String loreContainsLoreLine = null;

        for(String sepLine : sep) {
            if(sepLine.startsWith("datavalue: ")) {
                datavalue = Short.parseShort(sepLine.replace("datavalue: ", ""));
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
        int duration = Integer.parseInt(sep[1]);
        int level = Integer.parseInt(sep[2])-1;
        boolean showParticles = true;
        if(sep.length >= 4) {
            showParticles = Boolean.parseBoolean(sep[3]);
        }
        PotionEffect effect = new PotionEffect(potionEffectType,duration,level,false,showParticles);
        player.addPotionEffect(effect);
    }

    public static void removePotionEffect(Player player,String actionLine){
        PotionEffectType potionEffectType = PotionEffectType.getByName(actionLine);
        player.removePotionEffect(potionEffectType);
    }

    public static void cancelEvent(String actionLine,Event minecraftEvent){
        boolean cancel = Boolean.parseBoolean(actionLine);
        if(minecraftEvent instanceof Cancellable) {
            Cancellable cancellableEvent = (Cancellable) minecraftEvent;
            cancellableEvent.setCancelled(cancel);
        }
    }

    public static void kick(Player player,String actionLine){
        player.kickPlayer(MessagesManager.getColoredMessage(actionLine));
    }

    public static void playSound(Player player,String actionLine){
        String[] sep = actionLine.split(";");
        Sound sound = null;
        int volume = 0;
        float pitch = 0;
        try {
            sound = Sound.valueOf(sep[0]);
            volume = Integer.parseInt(sep[1]);
            pitch = Float.parseFloat(sep[2]);
        }catch(Exception e ) {
            Bukkit.getConsoleSender().sendMessage(ConditionalEvents.prefix+
                    MessagesManager.getColoredMessage(" &7Sound Name: &c"+sep[0]+" &7is not valid. Change it in the config!"));
            return;
        }

        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    public static void playSoundResourcePack(Player player,String actionLine){
        String[] sep = actionLine.split(";");
        String sound = sep[0];
        int volume = Integer.parseInt(sep[1]);
        float pitch = Float.parseFloat(sep[2]);
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    public static void actionbar(Player player,String actionLine,ConditionalEvents plugin){
        String[] sep = actionLine.split(";");
        String text = sep[0];
        int duration = Integer.parseInt(sep[1]);
        ActionBarAPI.sendActionBar(player,text,duration,plugin);
    }

    public static void title(Player player,String actionLine){
        String[] sep = actionLine.split(";");
        int fadeIn = Integer.parseInt(sep[0]);
        int stay = Integer.parseInt(sep[1]);
        int fadeOut = Integer.parseInt(sep[2]);

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

    public static void firework(Player player,String actionLine){
        ArrayList<Color> colors = new ArrayList<>();
        FireworkEffect.Type type = null;
        ArrayList<Color> fadeColors = new ArrayList<>();
        int power = 0;

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
            }
        }

        Location location = player.getLocation();
        Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();
        FireworkEffect effect = FireworkEffect.builder().flicker(false)
                .withColor(colors)
                .with(type)
                .withFade(fadeColors)
                .build();
        fireworkMeta.addEffect(effect);
        fireworkMeta.setPower(power);
        firework.setFireworkMeta(fireworkMeta);
    }

    public static void particle(Player player,String actionLine){

    }

    public static void gamemode(Player player,String actionLine){
        player.setGameMode(GameMode.valueOf(actionLine));
    }

    public static void wait(String actionLine, ExecutedEvent executedEvent){
        executedEvent.setOnWait(true);
        int timeSeconds = Integer.parseInt(actionLine);

        new BukkitRunnable(){
            @Override
            public void run() {
                executedEvent.continueWithActions();
            }
        }.runTaskLater(executedEvent.getPlugin(), timeSeconds*20);
    }

    public static void waitTicks(String actionLine, ExecutedEvent executedEvent){
        executedEvent.setOnWait(true);
        long timeTicks = Long.parseLong(actionLine);

        new BukkitRunnable(){
            @Override
            public void run() {
                executedEvent.continueWithActions();
            }
        }.runTaskLater(executedEvent.getPlugin(), timeTicks);
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
}

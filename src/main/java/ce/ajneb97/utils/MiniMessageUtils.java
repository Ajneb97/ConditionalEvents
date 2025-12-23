package ce.ajneb97.utils;

import ce.ajneb97.manager.MessagesManager;
import ce.ajneb97.model.internal.minimessage.GetVariablesItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("DataFlowIssue")
public class MiniMessageUtils {

    public static void messagePrefix(CommandSender sender, String message, boolean isPrefix, String prefix) {
        if (isPrefix) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(prefix + message));
        } else {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(message));
        }
    }

    public static void title(Player player, String title, String subtitle) {
        player.showTitle(Title.title(
                MiniMessage.miniMessage().deserialize(title), MiniMessage.miniMessage().deserialize(subtitle)
        ));
    }

    public static void actionbar(Player player, String message) {
        player.sendActionBar(MiniMessage.miniMessage().deserialize(message));
    }

    public static void message(Player player, String message) {
        player.sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

    public static void centeredMessage(Player player, String message) {
        MiniMessage mm = MiniMessage.miniMessage();
        Component component = mm.deserialize(message);
        String centeredTextLegacy = MessagesManager.getCenteredMessage(LegacyComponentSerializer.legacySection().serialize(component)); // to legacy
        Component centeredTextMiniMessage = LegacyComponentSerializer.legacySection().deserialize(centeredTextLegacy); // to minimessage
        player.sendMessage(centeredTextMiniMessage);
    }

    public static void consoleMessage(String message) {
        Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

    public static void playerSendChat(Player player, String message) {
        Component component = MiniMessage.miniMessage().deserialize(message);
        player.chat(MessagesManager.getLegacyColoredMessage(LegacyComponentSerializer.legacySection().serialize(component)));
    }

    public static void kick(Player player, String message) {
        player.kick(MiniMessage.miniMessage().deserialize(message));
    }

    public static Inventory createInventory(int slots, String title) {
        return Bukkit.createInventory(null, slots, MiniMessage.miniMessage().deserialize(title));
    }

    public static void setItemName(ItemMeta meta, String name) {
        meta.displayName(MiniMessage.miniMessage().deserialize(name).decoration(TextDecoration.ITALIC, false));
    }

    public static void setItemLore(ItemMeta meta, List<String> lore) {
        List<Component> loreComponent = new ArrayList<>();
        for (String s : lore) {
            loreComponent.add(MiniMessage.miniMessage().deserialize(s).decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(loreComponent);
    }

    public static String getItemNamePlain(ItemMeta meta) {
        return PlainTextComponentSerializer.plainText().serialize(meta.displayName());
    }

    public static String getItemNameMiniMessage(ItemMeta meta) {
        return MiniMessage.miniMessage().serialize(meta.displayName());
    }

    public static GetVariablesItemLore getVariablesItemLore(ItemMeta meta, List<String> loreList, List<String> colorFormatLoreList, String loreString, String colorFormatLoreString) {
        List<Component> lore = meta.lore();
        PlainTextComponentSerializer plainTextComponentSerializer = PlainTextComponentSerializer.plainText();
        MiniMessage miniMessage = MiniMessage.miniMessage();
        StringBuilder loreStringBuilder = new StringBuilder(loreString);
        StringBuilder colorFormatLoreStringBuilder = new StringBuilder(colorFormatLoreString);
        for (int i = 0; i < lore.size(); i++) {
            String plainFormat = plainTextComponentSerializer.serialize(lore.get(i));
            String tagFormat = miniMessage.serialize(lore.get(i));
            loreList.add(plainFormat);
            colorFormatLoreList.add(tagFormat);
            if (i == lore.size() - 1) {
                loreStringBuilder.append(plainFormat);
                colorFormatLoreStringBuilder.append(tagFormat);
            } else {
                loreStringBuilder.append(plainFormat).append(" ");
                colorFormatLoreStringBuilder.append(tagFormat).append(" ");
            }
        }
        colorFormatLoreString = colorFormatLoreStringBuilder.toString();
        loreString = loreStringBuilder.toString();

        return new GetVariablesItemLore(loreList, colorFormatLoreList, loreString, colorFormatLoreString);
    }

    public static void setEntityCustomName(Entity entity, String customName) {
        entity.customName(MiniMessage.miniMessage().deserialize(customName));
    }

    public static String getEntityCustomNamePlain(Entity entity) {
        return PlainTextComponentSerializer.plainText().serialize(entity.customName());
    }

    public static String getEntityCustomNameMiniMessage(Entity entity) {
        return MiniMessage.miniMessage().serialize(entity.customName());
    }

    public static void deathMessage(PlayerDeathEvent deathEvent, String message) {
        deathEvent.deathMessage(MiniMessage.miniMessage().deserialize(message));
    }

    public static void preLoginKickMessage(AsyncPlayerPreLoginEvent preJoinEvent, String message) {
        preJoinEvent.kickMessage(MiniMessage.miniMessage().deserialize(message));
    }

    public static String getInventoryTitlePlain(InventoryEvent event) {
        return PlainTextComponentSerializer.plainText().serialize(getViewTitleComponent(event));
    }

    public static String getInventoryTitleMiniMessage(InventoryEvent event) {
        return MiniMessage.miniMessage().serialize(getViewTitleComponent(event));
    }

    public static String getOpenInventoryViewTitlePlain(Player player) {
        return PlainTextComponentSerializer.plainText().serialize(getOpenInventoryViewTitleComponent(player));
    }

    private static Component getOpenInventoryViewTitleComponent(Player player) {
        try {
            Object view = player.getOpenInventory();
            Method getTitle = view.getClass().getMethod("title");
            getTitle.setAccessible(true);
            return (Component) getTitle.invoke(view);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static Component getViewTitleComponent(InventoryEvent event) {
        try {
            Object view = event.getView();
            Method getTitle = view.getClass().getMethod("title");
            getTitle.setAccessible(true);
            return (Component) getTitle.invoke(view);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}

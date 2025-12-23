package ce.ajneb97.utils;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

@SuppressWarnings("deprecation")
public class JSONMessage {

    private final Player player;
    private final String text;
    private BaseComponent[] hover;
    private String suggestCommand;
    private String executeCommand;

    public JSONMessage(Player player, String text) {
        this.player = player;
        this.hover = null;
        this.text = text;
    }

    public JSONMessage hover(List<String> list) {
        hover = new BaseComponent[list.size()];
        for (int i = 0; i < list.size(); i++) {
            TextComponent line = new TextComponent();
            if (i == list.size() - 1) {
                line.setText(ChatColor.translateAlternateColorCodes('&', list.get(i)));
            } else {
                line.setText(ChatColor.translateAlternateColorCodes('&', list.get(i)) + "\n");
            }
            hover[i] = line;
        }
        return this;
    }

    public JSONMessage setSuggestCommand(String command) {
        this.suggestCommand = command;
        return this;
    }

    public JSONMessage setExecuteCommand(String command) {
        this.executeCommand = command;
        return this;
    }

    public void send() {
        TextComponent message = new TextComponent();
        message.setText(ChatColor.translateAlternateColorCodes('&', text));
        if (hover != null) {
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
        }
        if (suggestCommand != null) {
            message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestCommand));
        }
        if (executeCommand != null) {
            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, executeCommand));
        }
        player.spigot().sendMessage(message);
    }
}

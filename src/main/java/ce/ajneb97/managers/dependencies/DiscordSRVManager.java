package ce.ajneb97.managers.dependencies;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.utils.OtherUtils;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageChannel;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import org.bukkit.Bukkit;
import java.awt.Color;
import org.bukkit.entity.Player;

public class DiscordSRVManager {

    private ConditionalEvents plugin;
    public DiscordSRVManager(ConditionalEvents plugin){
        this.plugin = plugin;
    }

    public void sendEmbedMessage(String actionLine){
        // discordsrv_embed: channel:<channel>;author_name:<name>;title:<title>;player_skin_name:<name>;
        //color:<r>,<g>,<b>
        EmbedBuilder embed = new EmbedBuilder();

        String channel = null;
        String authorName = null;
        String authorAvatarURL = DiscordSRV.getPlugin().getJda().getSelfUser().getAvatarUrl();
        String title = null;
        String footer = null;
        String description = null;
        int colorR = 0;
        int colorG = 0;
        int colorB = 0;

        String[] sep = actionLine.split(";");
        for(String s : sep) {
            String key = s.split(":")[0];
            String value = s.replace(key+":","");

            switch(key){
                case "channel":
                    channel = value;
                    break;
                case "author_name":
                    authorName = value;
                    break;
                case "title":
                    title = value;
                    break;
                case "footer":
                    footer = value;
                    break;
                case "player_skin_name":
                    Player player = Bukkit.getPlayer(value);
                    if(player != null){
                        authorAvatarURL = DiscordSRV.getAvatarUrl(player);
                    }
                    break;
                case "color":
                    try{
                        String[] color = value.split(",");
                        colorR = Integer.parseInt(color[0]);
                        colorG = Integer.parseInt(color[1]);
                        colorB = Integer.parseInt(color[2]);
                    }catch(Exception e){
                        colorR = 0;
                        colorG = 0;
                        colorB = 0;
                    }
                    break;
                case "description":
                    description = value;
                    break;
            }
        }

        embed.setAuthor(authorName,null,authorAvatarURL);
        embed.setTitle(title);
        embed.setFooter(footer);
        embed.setColor(new Color(colorR, colorG, colorB));
        embed.setDescription(description);

        MessageChannel messageChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(channel);
        messageChannel.sendMessage(embed.build()).queue();
    }
}

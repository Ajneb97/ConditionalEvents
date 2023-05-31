package ce.ajneb97.managers.dependencies;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.model.EventType;
import ce.ajneb97.model.StoredVariable;
import ce.ajneb97.model.internal.ConditionEvent;
import ce.ajneb97.utils.OtherUtils;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.*;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ProtocolLibManager {

    private ConditionalEvents plugin;
    public ProtocolLibManager(ConditionalEvents plugin){
        this.plugin = plugin;
        configure();
    }

    public void configure(){
        ProtocolLibrary.getProtocolManager().addPacketListener(getChatAdapter(PacketType.Play.Server.CHAT));
        if(Bukkit.getVersion().contains("1.19")) {
            ProtocolLibrary.getProtocolManager().addPacketListener(getChatAdapter(PacketType.Play.Server.SYSTEM_CHAT));
            ProtocolLibrary.getProtocolManager().addPacketListener(getChatAdapter(PacketType.Play.Server.DISGUISED_CHAT));
        }
    }

    public PacketAdapter getChatAdapter(PacketType type) {
        return new PacketAdapter(plugin, ListenerPriority.HIGHEST, type) {
            @Override
            public void onPacketSending(PacketEvent event) {
                ConditionalEvents pluginInstance = (ConditionalEvents) plugin;
                boolean isPaper = pluginInstance.getDependencyManager().isPaper();

                PacketContainer packet = event.getPacket();
                Player player = event.getPlayer();
                for(EnumWrappers.ChatType type : packet.getChatTypes().getValues()) {
                    if(type.equals(EnumWrappers.ChatType.GAME_INFO)) {
                        return;
                    }
                }

                for(Object object : packet.getModifier().getValues()) {
                    if(object == null) {
                        continue;
                    }

                    String jsonMessage = null;
                    String normalMessage = null;

                    if(object instanceof String) {
                        jsonMessage = (String) object;
                        normalMessage = OtherUtils.fromJsonMessageToNormalMessage(jsonMessage);
                    }else if(object instanceof BaseComponent[]) {
                        BaseComponent[] baseComponents = (BaseComponent[]) object;
                        normalMessage = BaseComponent.toLegacyText(baseComponents);
                        jsonMessage = ComponentSerializer.toString(baseComponents);
                    }

                    if(isPaper){
                        if(object instanceof Component){
                            WrappedChatComponent wrappedChatComponent = AdventureComponentConverter
                                    .fromComponent((Component)object);
                            jsonMessage = wrappedChatComponent.getJson();
                            normalMessage = OtherUtils.fromJsonMessageToNormalMessage(jsonMessage);
                        }
                    }

                    if(jsonMessage != null) {
                        executeEvent(player,jsonMessage,normalMessage,event);
                        return;
                    }
                }

                for(WrappedChatComponent wrappedChatComponent : packet.getChatComponents().getValues()) {
                    if(wrappedChatComponent != null) {
                        String jsonMessage = wrappedChatComponent.getJson();
                        String normalMessage = OtherUtils.fromJsonMessageToNormalMessage(jsonMessage);
                        executeEvent(player,jsonMessage,normalMessage,event);
                        return;
                    }
                }
            }
        };
    }

    public void executeEvent(Player player,String jsonMessage,String normalMessage,PacketEvent event){
        ProtocolLibReceiveMessageEvent messageEvent = new ProtocolLibReceiveMessageEvent(player,jsonMessage,normalMessage);
        ConditionEvent conditionEvent = new ConditionEvent(plugin, player, messageEvent, EventType.PROTOCOLLIB_RECEIVE_MESSAGE, null);
        if(!conditionEvent.containsValidEvents()) return;
        conditionEvent.addVariables(
                new StoredVariable("%json_message%",jsonMessage),
                new StoredVariable("%normal_message%",normalMessage.replace("§", "&"))
        ).checkEvent();

        if(messageEvent.isCancelled()){
            event.setCancelled(true);
        }
    }

}

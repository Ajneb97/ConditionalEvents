package ce.ajneb97.listeners.dependencies;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.model.EventType;
import ce.ajneb97.model.StoredVariable;
import ce.ajneb97.model.internal.ConditionEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CitizensListener implements Listener {

    public ConditionalEvents plugin;

    public CitizensListener(ConditionalEvents plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRightClick(NPCRightClickEvent event) {
        Player player = event.getClicker();
        NPC npc = event.getNPC();
        new ConditionEvent(plugin, player, event, EventType.CITIZENS_RIGHT_CLICK_NPC, null)
                .addVariables(
                        new StoredVariable("%npc_id%", npc.getId() + ""),
                        new StoredVariable("%npc_name%", npc.getName()))
                .checkEvent();
    }
}

package ce.ajneb97.model.internal;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.model.CEEvent;
import ce.ajneb97.model.EventType;
import ce.ajneb97.model.StoredVariable;
import ce.ajneb97.utils.BlockUtils;
import ce.ajneb97.utils.ColorUtils;
import ce.ajneb97.utils.OtherUtils;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Represent an event that is being executed and conditions need
// to be checked.
@Getter
public class ConditionEvent {

    private final ConditionalEvents plugin;

    private final Player player;
    private final Player target;
    private final ArrayList<StoredVariable> eventVariables;
    private final Event minecraftEvent;
    private final EventType eventType;
    private final boolean async;

    @Setter
    private CEEvent currentEvent; //The current event that is being checked

    public ConditionEvent(@Nonnull final ConditionalEvents plugin,
                          @Nullable final Player player,
                          @Nullable final Event minecraftEvent,
                          @Nonnull final EventType eventType,
                          @Nullable final Player target) {
        this(plugin, player, minecraftEvent, eventType, target, false);
    }

    public ConditionEvent(@Nonnull final ConditionalEvents plugin,
                          @Nullable final Player player,
                          @Nullable final Event minecraftEvent,
                          @Nonnull final EventType eventType,
                          @Nullable final Player target,
                          final boolean async) {
        this.plugin = Objects.requireNonNull(plugin);
        this.player = player;
        this.eventVariables = new ArrayList<>();
        this.minecraftEvent = minecraftEvent;
        this.eventType = Objects.requireNonNull(eventType);
        this.target = target;
        this.async = async;
    }

    public void checkEvent() {
        plugin.getEventsManager().checkEvent(this);
    }

    public boolean containsValidEvents() {
        ArrayList<CEEvent> validEvents = plugin.getEventsManager().getValidEvents(eventType);
        return !validEvents.isEmpty();
    }

    public ConditionEvent addVariables(@Nonnull final StoredVariable... variables) {
        eventVariables.addAll(ImmutableList.copyOf(Objects.requireNonNull(variables)));
        return this;
    }

    public ConditionEvent addVariables(@Nonnull final List<StoredVariable> variables) {
        eventVariables.addAll(Objects.requireNonNull(variables));
        return this;
    }

    public ConditionEvent setCommonBlockVariables(@Nonnull final Block block) {
        final Location location = block.getLocation();
        addStoredVariable("block_x", String.valueOf(location.getBlockX()));
        addStoredVariable("block_y", String.valueOf(location.getBlockY()));
        addStoredVariable("block_z", String.valueOf(location.getBlockZ()));
        addStoredVariable("block_world", location.getWorld().getName());
        addStoredVariable("block", block.getType().name());
        addStoredVariable("block_head_texture", BlockUtils.getHeadTextureData(block));
        if (OtherUtils.isLegacy()) {
            //noinspection deprecation
            addStoredVariable("block_data", String.valueOf(block.getData()));
        } else {
            addStoredVariable("block_data", BlockUtils.getBlockDataStringFromObject(block.getBlockData()));
        }
        return this;
    }

    private static final String SHIFT_RIGHT_CLICK = "SHIFT_RIGHT_CLICK";
    private static final String RIGHT_CLICK = "RIGHT_CLICK";
    private static final String SHIFT_LEFT_CLICK = "SHIFT_LEFT_CLICK";
    private static final String LEFT_CLICK = "LEFT_CLICK";
    private static final String PHYSICAL = "PHYSICAL";

    public ConditionEvent setCommonActionVariables(@Nonnull final Action action,
                                                   @Nonnull final Player player) {
        final String actionVariable;

        switch (action) {
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                actionVariable = player.isSneaking() ? SHIFT_RIGHT_CLICK : RIGHT_CLICK;
                break;
            case LEFT_CLICK_AIR:
            case LEFT_CLICK_BLOCK:
                actionVariable = player.isSneaking() ? SHIFT_LEFT_CLICK : LEFT_CLICK;
                break;
            case PHYSICAL:
                actionVariable = PHYSICAL;
                break;
            default:
                return this;
        }

        addStoredVariable("action_type", actionVariable);
        return this;
    }

    public ConditionEvent setCommonItemVariables(@Nullable final ItemStack item,
                                                 @Nullable String otherItemTag) {

        String material = null;
        short durability = 0;
        int amount = 0;

        String name = null;
        String colorFormatName = null;

        String metaString = null;
        String loreWithoutColorsFullString = null;
        String loreWithColorsFullString = null;
        List<String> loreWithoutColors = null;
        List<String> loreWithColors = null;

        int customModelData = 0;

        if (item != null) {
            material = item.getType().name();
            //noinspection deprecation
            durability = item.getDurability();
            amount = item.getAmount();
            if (item.hasItemMeta()) {

                val meta = item.getItemMeta();

                if (plugin.getConfigsManager().getMainConfigManager().isExperimentalSerializeItemMeta()) {
                    metaString = meta.toString();
                }

                if (meta.hasDisplayName()) {
                    //noinspection deprecation
                    final String displayName = meta.getDisplayName();
                    name = ColorUtils.stripColor(displayName);
                    colorFormatName = displayName.replace("§", "&");
                }

                if (meta.hasLore()) {
                    //noinspection deprecation
                    final List<String> lore = meta.getLore();
                    if (lore != null && !lore.isEmpty()) {
                        final ImmutableList.Builder<String> loreWithoutColorsBuilder = ImmutableList.builder();
                        final ImmutableList.Builder<String> loreWithColorsBuilder = ImmutableList.builder();

                        for (final String line : lore) {
                            loreWithoutColorsBuilder.add(ColorUtils.stripColor(line));
                            loreWithColorsBuilder.add(line.replace("§", "&"));
                        }

                        loreWithoutColors = loreWithoutColorsBuilder.build();
                        loreWithColors = loreWithColorsBuilder.build();

                        loreWithoutColorsFullString = String.join(" ", loreWithoutColors);
                        loreWithColorsFullString = String.join(" ", loreWithColors);
                    }
                }

                if (OtherUtils.isNew() && meta.hasCustomModelData()) {
                    customModelData = meta.getCustomModelData();
                }
            }
        }

        //Example: %offhand:<variable>%
        if (otherItemTag == null) {
            otherItemTag = "%";
        } else {
            otherItemTag = "%" + otherItemTag + ":";
        }

        addStoredVariable(String.format("%sitem", otherItemTag), nullToEmptyString(material));
        addStoredVariable(String.format("%sitem_name", otherItemTag), nullToEmptyString(name));
        addStoredVariable(String.format("%sitem_color_format_name", otherItemTag), nullToEmptyString(colorFormatName));
        addStoredVariable(String.format("%sitem_durability", otherItemTag), String.valueOf(durability));
        addStoredVariable(String.format("%sitem_amount", otherItemTag), String.valueOf(amount));
        addStoredVariable(String.format("%sitem_lore", otherItemTag), nullToEmptyString(loreWithoutColorsFullString));
        addStoredVariable(String.format("%sitem_color_format_lore", otherItemTag), nullToEmptyString(loreWithColorsFullString));
        addStoredVariable(String.format("%sitem_custom_model_data", otherItemTag), String.valueOf(customModelData));

        if (plugin.getConfigsManager().getMainConfigManager().isExperimentalSerializeItemMeta()) {
            addStoredVariable(String.format("%sitem_meta", otherItemTag), nullToEmptyString(metaString));
        }

        if (loreWithoutColors != null) {
            for (int i = 0; i < loreWithoutColors.size(); i++) {
                addStoredVariable(String.format("%sitem_lore_line_%d", otherItemTag, (i + 1)), loreWithoutColors.get(i));
            }
        }

        if (loreWithColors != null) {
            for (int i = 0; i < loreWithColors.size(); i++) {
                addStoredVariable(String.format("%sitem_color_format_lore_line_%d", otherItemTag, (i + 1)), loreWithColors.get(i));
            }
        }

        return this;
    }

    public ConditionEvent setCommonEntityVariables(@Nonnull final Entity entity) {
        return fillEntityVariables(entity, "entity");
    }

    public ConditionEvent setCommonVictimVariables(@Nonnull final Entity entity) {
        return fillEntityVariables(entity, "victim");
    }

    private ConditionEvent fillEntityVariables(@Nonnull final Entity entity,
                                               @Nonnull final String variablePrefix) {

        final String entityType = entity.getType().name();
        final String entityUUID = entity.getUniqueId().toString();
        final Location location = entity.getLocation();

        final String entityNameWithoutColors;
        final String entityNameWithColors;

        //noinspection deprecation
        final String customName = entity.getCustomName();
        if (customName != null) {
            entityNameWithoutColors = ColorUtils.stripColor(customName);
            entityNameWithColors = customName.replace("§", "&");
        } else {
            entityNameWithoutColors = "";
            entityNameWithColors = "";
        }

        addStoredVariable(variablePrefix, entityType);
        addStoredVariable(String.format("%s_name", variablePrefix), entityNameWithoutColors);
        addStoredVariable(String.format("%s_color_format_name", variablePrefix), entityNameWithColors);
        addStoredVariable(String.format("%s_block_x", variablePrefix), String.valueOf(location.getBlockX()));
        addStoredVariable(String.format("%s_block_y", variablePrefix), String.valueOf(location.getBlockY()));
        addStoredVariable(String.format("%s_block_z", variablePrefix), String.valueOf(location.getBlockZ()));
        addStoredVariable(String.format("%s_block_world", variablePrefix), location.getWorld().getName());
        addStoredVariable(String.format("%s_uuid", variablePrefix), entityUUID);
        return this;
    }

    public void addStoredVariable(@Nonnull final String name, @Nonnull final String value) {
        // result -> %name%
        this.eventVariables.add(new StoredVariable(String.format("%%%s%%",
                Objects.requireNonNull(name)),
                Objects.requireNonNull(value)
        ));
    }

    @Nonnull
    private String nullToEmptyString(@Nullable final Object object) {
        if (object == null) return "";
        return object.toString();
    }

}

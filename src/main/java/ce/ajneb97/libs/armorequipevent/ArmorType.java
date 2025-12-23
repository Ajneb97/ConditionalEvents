package ce.ajneb97.libs.armorequipevent;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.utils.ServerVersion;
import org.bukkit.inventory.ItemStack;

/**
 * @author Arnah
 * @since Jul 30, 2015
 */
public enum ArmorType {

    HELMET(5),

    CHESTPLATE(6),

    LEGGINGS(7),

    BOOTS(8);

    private final int slot;

    ArmorType(int slot) {
        this.slot = slot;
    }

    /**
     * Attempts to match the ArmorType for the specified ItemStack.
     *
     * @param itemStack The ItemStack to parse the type of.
     * @return The parsed ArmorType, or null if not found.
     */
    public static ArmorType matchType(final ItemStack itemStack) {
        if (ArmorListener.isAirOrNull(itemStack)) return null;
        String type = itemStack.getType().name();

        ServerVersion serverVersion = ConditionalEvents.serverVersion;
        if (serverVersion.serverVersionGreaterEqualThan(serverVersion, ServerVersion.v1_13_R1)) {
            if (type.equals("CARVED_PUMPKIN")) {
                return HELMET;
            }
        } else {
            if (type.equals("PUMPKIN")) {
                return HELMET;
            }
        }

        if (type.endsWith("_HELMET") || type.startsWith("SKULL_") || type.endsWith("_HEAD") || type.endsWith("_SKULL"))
            return HELMET;
        else if (type.endsWith("_CHESTPLATE") || type.equals("ELYTRA")) return CHESTPLATE;
        else if (type.endsWith("_LEGGINGS")) return LEGGINGS;
        else if (type.endsWith("_BOOTS")) return BOOTS;
        else return null;
    }

    public int getSlot() {
        return slot;
    }
}

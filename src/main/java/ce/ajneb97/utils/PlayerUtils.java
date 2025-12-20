package ce.ajneb97.utils;

import ce.ajneb97.ConditionalEvents;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

@SuppressWarnings("deprecation")
public class PlayerUtils {

    public static ItemStack getItemBySlot(Player player, String slot) {
        PlayerInventory inventory = player.getInventory();
        switch (slot) {
            case "HAND":
                return inventory.getItemInHand();
            case "OFF_HAND":
                return inventory.getItemInOffHand();
            case "HELMET":
                return inventory.getHelmet();
            case "CHESTPLATE":
                return inventory.getChestplate();
            case "LEGGINGS":
                return inventory.getLeggings();
            case "BOOTS":
                return inventory.getBoots();
            default:
                return inventory.getItem(Integer.parseInt(slot));
        }
    }

    public static void setItemBySlot(Player player, String slot, ItemStack item) {
        PlayerInventory inventory = player.getInventory();
        switch (slot) {
            case "HAND":
                inventory.setItemInHand(item);
                break;
            case "OFF_HAND":
                inventory.setItemInOffHand(item);
                break;
            case "HELMET":
                inventory.setHelmet(item);
                break;
            case "CHESTPLATE":
                inventory.setChestplate(item);
                break;
            case "LEGGINGS":
                inventory.setLeggings(item);
                break;
            case "BOOTS":
                inventory.setBoots(item);
                break;
            default:
                inventory.setItem(Integer.parseInt(slot), item);
                break;
        }
    }

    public static void updatePlayerInventory(Player player) {
        ServerVersion serverVersion = ConditionalEvents.serverVersion;
        if (!serverVersion.serverVersionGreaterEqualThan(serverVersion, ServerVersion.v1_13_R1)) {
            //1.12-
            player.updateInventory();
        }
    }
}

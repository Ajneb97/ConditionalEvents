package ce.ajneb97.libs.offhandevent;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class OffHandEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancel = false;
    private final MovementType movementType;
    private final ItemStack placedInOffhand;
    private final ItemStack recoveredFromOffhand;

    public OffHandEvent(Player player, ItemStack placedInOffhand, ItemStack recoveredFromOffhand) {
        super(player);
        this.placedInOffhand = placedInOffhand;
        this.recoveredFromOffhand = recoveredFromOffhand;

        boolean wasPlaced = placedInOffhand != null && !placedInOffhand.getType().equals(Material.AIR);
        boolean wasRecovered = recoveredFromOffhand != null && !recoveredFromOffhand.getType().equals(Material.AIR);

        if (wasPlaced && wasRecovered) {
            movementType = MovementType.SWAP;
        } else if (wasPlaced) {
            movementType = MovementType.PLACE;
        } else if (wasRecovered) {
            movementType = MovementType.RECOVER;
        } else {
            movementType = MovementType.NOTHING;
        }
    }

    public MovementType getMovementType() {
        return movementType;
    }

    public ItemStack getPlacedInOffhand() {
        return placedInOffhand;
    }

    public ItemStack getRecoveredFromOffhand() {
        return recoveredFromOffhand;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return this.cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public enum MovementType {
        PLACE,
        RECOVER,
        SWAP,
        NOTHING
    }
}

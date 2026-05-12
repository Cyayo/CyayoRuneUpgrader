package id.seria.cyayorune.api.event;

import id.seria.cyayorune.model.EnhanceRecipe;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class RuneEnhanceEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    protected final Player player;
    protected final EnhanceRecipe recipe;
    protected final ItemStack mainItem;
    protected final ItemStack materialItem;

    public RuneEnhanceEvent(@NotNull Player player, @NotNull EnhanceRecipe recipe, @NotNull ItemStack mainItem, @NotNull ItemStack materialItem) {
        this.player = player;
        this.recipe = recipe;
        this.mainItem = mainItem;
        this.materialItem = materialItem;
    }

    @NotNull
    public Player getPlayer() { return player; }
    @NotNull
    public EnhanceRecipe getRecipe() { return recipe; }
    @NotNull
    public ItemStack getMainItem() { return mainItem; }
    @NotNull
    public ItemStack getMaterialItem() { return materialItem; }

    @NotNull
    @Override
    public HandlerList getHandlers() { return HANDLERS; }
    @NotNull
    public static HandlerList getHandlerList() { return HANDLERS; }

    /**
     * Dipicu saat proses peningkatan dimulai. Dapat dibatalkan.
     */
    public static class Start extends RuneEnhanceEvent implements Cancellable {
        private boolean cancelled = false;
        private final boolean usingWhiteScroll;

        public Start(@NotNull Player player, @NotNull EnhanceRecipe recipe, @NotNull ItemStack mainItem, @NotNull ItemStack materialItem, boolean usingWhiteScroll) {
            super(player, recipe, mainItem, materialItem);
            this.usingWhiteScroll = usingWhiteScroll;
        }

        public boolean isUsingWhiteScroll() { return usingWhiteScroll; }

        @Override public boolean isCancelled() { return cancelled; }
        @Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }
    }

    /**
     * Dipicu saat peningkatan berhasil.
     */
    public static class Success extends RuneEnhanceEvent {
        private final ItemStack resultItem;
        private final double increase;

        public Success(@NotNull Player player, @NotNull EnhanceRecipe recipe, @NotNull ItemStack mainItem, @NotNull ItemStack materialItem, @NotNull ItemStack resultItem, double increase) {
            super(player, recipe, mainItem, materialItem);
            this.resultItem = resultItem;
            this.increase = increase;
        }

        @NotNull
        public ItemStack getResultItem() { return resultItem; }
        public double getIncrease() { return increase; }
    }

    /**
     * Dipicu saat peningkatan gagal.
     */
    public static class Failure extends RuneEnhanceEvent {
        public Failure(@NotNull Player player, @NotNull EnhanceRecipe recipe, @NotNull ItemStack mainItem, @NotNull ItemStack materialItem) {
            super(player, recipe, mainItem, materialItem);
        }
    }
}

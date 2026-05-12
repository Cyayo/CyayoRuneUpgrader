package id.seria.cyayorune.api.event;

import id.seria.cyayorune.model.CombineRecipe;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class RuneCombineEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    protected final Player player;
    protected final CombineRecipe recipe;
    protected final ItemStack[] inputItems;

    public RuneCombineEvent(@NotNull Player player, @NotNull CombineRecipe recipe, @NotNull ItemStack[] inputItems) {
        this.player = player;
        this.recipe = recipe;
        this.inputItems = inputItems;
    }

    @NotNull
    public Player getPlayer() { return player; }
    @NotNull
    public CombineRecipe getRecipe() { return recipe; }
    @NotNull
    public ItemStack[] getInputItems() { return inputItems; }

    @NotNull
    @Override
    public HandlerList getHandlers() { return HANDLERS; }
    @NotNull
    public static HandlerList getHandlerList() { return HANDLERS; }

    /**
     * Dipicu saat proses penggabungan dimulai. Dapat dibatalkan.
     */
    public static class Start extends RuneCombineEvent implements Cancellable {
        private boolean cancelled = false;
        private final boolean usingWhiteScroll;

        public Start(@NotNull Player player, @NotNull CombineRecipe recipe, @NotNull ItemStack[] inputItems, boolean usingWhiteScroll) {
            super(player, recipe, inputItems);
            this.usingWhiteScroll = usingWhiteScroll;
        }

        public boolean isUsingWhiteScroll() { return usingWhiteScroll; }

        @Override public boolean isCancelled() { return cancelled; }
        @Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }
    }

    /**
     * Dipicu saat penggabungan berhasil.
     */
    public static class Success extends RuneCombineEvent {
        private final ItemStack resultItem;

        public Success(@NotNull Player player, @NotNull CombineRecipe recipe, @NotNull ItemStack[] inputItems, @NotNull ItemStack resultItem) {
            super(player, recipe, inputItems);
            this.resultItem = resultItem;
        }

        @NotNull
        public ItemStack getResultItem() { return resultItem; }
    }

    /**
     * Dipicu saat penggabungan gagal.
     */
    public static class Failure extends RuneCombineEvent {
        public Failure(@NotNull Player player, @NotNull CombineRecipe recipe, @NotNull ItemStack[] inputItems) {
            super(player, recipe, inputItems);
        }
    }
}

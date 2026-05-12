package id.seria.cyayorune.menu;

import id.seria.cyayorune.CyayoRune;
import id.seria.cyayorune.util.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class BaseMenu implements InventoryHolder {

    protected final CyayoRune plugin;
    protected final Player player;
    protected Inventory inventory;

    public BaseMenu(CyayoRune plugin, Player player, String title, int size) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, size, ItemUtils.legacy.serialize(ItemUtils.parseText(title)));
    }

    public abstract void update();

    public void open() {
        update();
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    protected void fillFiller(ItemStack filler) {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }
}

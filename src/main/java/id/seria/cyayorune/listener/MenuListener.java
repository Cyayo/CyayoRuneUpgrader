package id.seria.cyayorune.listener;

import id.seria.cyayorune.CyayoRune;
import id.seria.cyayorune.menu.CombineMenu;
import id.seria.cyayorune.menu.EnhanceMenu;
import id.seria.cyayorune.util.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class MenuListener implements Listener {

    private final CyayoRune plugin;

    public MenuListener(CyayoRune plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        if (event.getInventory().getHolder() instanceof EnhanceMenu menu) {
            handleEnhanceClick(event, player, menu);
        } else if (event.getInventory().getHolder() instanceof CombineMenu cMenu) {
            handleCombineClick(event, player, cMenu);
        }
    }

    private void handleEnhanceClick(InventoryClickEvent event, Player player, EnhanceMenu menu) {
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        int slot = event.getRawSlot();

        // Prevent any interaction with items in the GUI except taking them back
        if (event.getClickedInventory() == player.getInventory()) {
            if (clicked == null || clicked.getType() == Material.AIR) return;
            
            if (!plugin.getRecipeManager().isWhitelisted(clicked)) {
                player.sendMessage(ItemUtils.legacy.serialize(ItemUtils.parseText(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("enhance.invalid-items"))));
                return;
            }

            if (isWhiteScroll(clicked)) {
                if (menu.getWhiteScrollItem() == null) {
                    ItemStack toPlace = clicked.clone();
                    toPlace.setAmount(1);
                    menu.setWhiteScrollItem(toPlace);
                    consumeOne(event, clicked);
                    plugin.getConfigManager().playSound(player, "put-item");
                }
            } else if (isBaseItem(clicked)) {
                if (menu.getMainItem() == null) {
                    ItemStack toPlace = clicked.clone();
                    toPlace.setAmount(1);
                    menu.setMainItem(toPlace);
                    consumeOne(event, clicked);
                    plugin.getConfigManager().playSound(player, "put-item");
                } else if (menu.getMaterialItem() == null) {
                     ItemStack toPlace = clicked.clone();
                     toPlace.setAmount(1);
                     menu.setMaterialItem(toPlace);
                     consumeOne(event, clicked);
                     plugin.getConfigManager().playSound(player, "put-item");
                }
            } else if (isMaterialItem(clicked)) {
                if (menu.getMaterialItem() == null) {
                    ItemStack toPlace = clicked.clone();
                    toPlace.setAmount(1);
                    menu.setMaterialItem(toPlace);
                    consumeOne(event, clicked);
                    plugin.getConfigManager().playSound(player, "put-item");
                }
            }
        } else {
            // Clicked in GUI
            if (slot == menu.getMainSlot()) {
                if (menu.getMainItem() != null) {
                    giveBack(player, menu.getMainItem());
                    menu.setMainItem(null);
                    plugin.getConfigManager().playSound(player, "take-item");
                }
            } else if (slot == menu.getMaterialSlot()) {
                if (menu.getMaterialItem() != null) {
                    giveBack(player, menu.getMaterialItem());
                    menu.setMaterialItem(null);
                    plugin.getConfigManager().playSound(player, "take-item");
                }
            } else if (slot == menu.getWhiteScrollSlot()) {
                if (menu.getWhiteScrollItem() != null) {
                    giveBack(player, menu.getWhiteScrollItem());
                    menu.setWhiteScrollItem(null);
                    plugin.getConfigManager().playSound(player, "take-item");
                }
            } else if (slot == menu.getResultSlot()) {
                menu.processEnhance();
            }
        }
        
        // Force sync
        Bukkit.getScheduler().runTask(plugin, player::updateInventory);
    }

    private void handleCombineClick(InventoryClickEvent event, Player player, CombineMenu menu) {
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        int slot = event.getRawSlot();

        if (event.getClickedInventory() == player.getInventory()) {
            if (clicked == null || clicked.getType() == Material.AIR) return;

            if (!plugin.getRecipeManager().isWhitelisted(clicked)) {
                player.sendMessage(ItemUtils.legacy.serialize(ItemUtils.parseText(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("enhance.invalid-items"))));
                return;
            }

            if (isWhiteScroll(clicked)) {
                if (menu.getWhiteScrollItem() == null) {
                    ItemStack toPlace = clicked.clone();
                    toPlace.setAmount(1);
                    menu.setWhiteScrollItem(toPlace);
                    consumeOne(event, clicked);
                    plugin.getConfigManager().playSound(player, "put-item");
                }
            } else {
                for (int i = 0; i < 4; i++) {
                    if (menu.getInputItems()[i] == null) {
                        ItemStack toPlace = clicked.clone();
                        toPlace.setAmount(1);
                        menu.setInputItem(i, toPlace);
                        consumeOne(event, clicked);
                        plugin.getConfigManager().playSound(player, "put-item");
                        break;
                    }
                }
            }
        } else {
            int inputIdx = menu.getInputSlots().indexOf(slot);
            if (inputIdx != -1) {
                if (menu.getInputItems()[inputIdx] != null) {
                    giveBack(player, menu.getInputItems()[inputIdx]);
                    menu.setInputItem(inputIdx, null);
                    plugin.getConfigManager().playSound(player, "take-item");
                }
            } else if (slot == menu.getWhiteScrollSlot()) {
                if (menu.getWhiteScrollItem() != null) {
                    giveBack(player, menu.getWhiteScrollItem());
                    menu.setWhiteScrollItem(null);
                    plugin.getConfigManager().playSound(player, "take-item");
                }
            } else if (slot == menu.getResultSlot()) {
                menu.processCombine();
            }
        }
        
        Bukkit.getScheduler().runTask(plugin, player::updateInventory);
    }

    private void consumeOne(InventoryClickEvent event, ItemStack item) {
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
            event.setCurrentItem(item);
        } else {
            event.setCurrentItem(null);
        }
    }

    private boolean isWhiteScroll(ItemStack item) {
        String type = plugin.getIntegrationManager().getMMOItemType(item);
        String id = plugin.getIntegrationManager().getMMOItemID(item);
        String wsType = plugin.getConfig().getString("white-scroll.type");
        String wsID = plugin.getConfig().getString("white-scroll.id");
        return type != null && type.equalsIgnoreCase(wsType) && id != null && id.equalsIgnoreCase(wsID);
    }
    
    private boolean isBaseItem(ItemStack item) {
        String type = plugin.getIntegrationManager().getMMOItemType(item);
        String id = plugin.getIntegrationManager().getMMOItemID(item);
        return plugin.getRecipeManager().getEnhanceRecipeFor(type, id) != null;
    }
    
    private boolean isMaterialItem(ItemStack item) {
        String type = plugin.getIntegrationManager().getMMOItemType(item);
        String id = plugin.getIntegrationManager().getMMOItemID(item);
        for (var recipe : plugin.getRecipeManager().getEnhanceRecipes().values()) {
            for (var mat : recipe.getMaterials()) {
                if (mat.getMmoitem().matches(type, id)) return true;
            }
        }
        return false;
    }

    private void giveBack(Player player, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;
        player.getInventory().addItem(item).values().forEach(remaining -> player.getWorld().dropItem(player.getLocation(), remaining));
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        
        if (event.getInventory().getHolder() instanceof EnhanceMenu menu) {
            if (menu.getMainItem() != null) giveBack(player, menu.getMainItem());
            if (menu.getMaterialItem() != null) giveBack(player, menu.getMaterialItem());
            if (menu.getWhiteScrollItem() != null) giveBack(player, menu.getWhiteScrollItem());
            plugin.getMenuManager().removeMenu(player);
        } else if (event.getInventory().getHolder() instanceof CombineMenu menu) {
            for (ItemStack item : menu.getInputItems()) {
                if (item != null) giveBack(player, item);
            }
            if (menu.getWhiteScrollItem() != null) giveBack(player, menu.getWhiteScrollItem());
            plugin.getMenuManager().removeMenu(player);
        }
    }
}

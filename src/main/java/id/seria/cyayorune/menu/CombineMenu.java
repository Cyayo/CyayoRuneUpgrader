package id.seria.cyayorune.menu;

import id.seria.cyayorune.CyayoRune;
import id.seria.cyayorune.model.CombineRecipe;
import id.seria.cyayorune.util.ItemUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CombineMenu extends BaseMenu {

    private final List<Integer> inputSlots;
    private final int resultSlot;
    private final int whiteScrollSlot = 16;
    
    private final ItemStack[] inputItems = new ItemStack[4];
    private ItemStack whiteScrollItem;

    public CombineMenu(CyayoRune plugin, Player player) {
        super(plugin, player,
            plugin.getConfigManager().getMenuCombine().getString("menu.title", "Combine Rune"),
            plugin.getConfigManager().getMenuCombine().getInt("menu.size", 45));
        
        this.inputSlots = plugin.getConfigManager().getMenuCombine().getIntegerList("menu.slots.inputs");
        this.resultSlot = plugin.getConfigManager().getMenuCombine().getInt("menu.slots.result", 25);
    }

    @Override
    public void update() {
        render();
    }

    public void render() {
        inventory.clear();
        ConfigurationSection menuSec = plugin.getConfigManager().getMenuCombine().getConfigurationSection("menu");
        ItemStack filler = ItemUtils.createItem(menuSec.getConfigurationSection("filler"));
        fillFiller(filler);

        ConfigurationSection placeholders = menuSec.getConfigurationSection("slots.placeholders");

        // Input Slots
        for (int i = 0; i < inputSlots.size(); i++) {
            int slot = inputSlots.get(i);
            if (inputItems[i] == null || inputItems[i].getType() == Material.AIR) {
                inventory.setItem(slot, ItemUtils.createItem(placeholders.getConfigurationSection("input")));
            } else {
                inventory.setItem(slot, inputItems[i]);
            }
        }

        // White Scroll Slot
        if (whiteScrollItem == null || whiteScrollItem.getType() == Material.AIR) {
            inventory.setItem(whiteScrollSlot, ItemUtils.createItem(placeholders.getConfigurationSection("white-scroll")));
        } else {
            inventory.setItem(whiteScrollSlot, whiteScrollItem);
        }

        updatePreview();
        player.updateInventory();
    }

    private void updatePreview() {
        ConfigurationSection menuSec = plugin.getConfigManager().getMenuCombine().getConfigurationSection("menu");
        ConfigurationSection placeholders = menuSec.getConfigurationSection("slots.placeholders");

        boolean allFilled = true;
        for (ItemStack item : inputItems) {
            if (item == null || item.getType() == Material.AIR) {
                allFilled = false;
                break;
            }
        }

        if (!allFilled) {
            boolean anyFilled = false;
            for (ItemStack item : inputItems) {
                if (item != null && item.getType() != Material.AIR) {
                    anyFilled = true;
                    break;
                }
            }
            
            if (anyFilled) {
                inventory.setItem(resultSlot, ItemUtils.createItem(menuSec.getConfigurationSection("preview-partial")));
            } else {
                inventory.setItem(resultSlot, ItemUtils.createItem(placeholders.getConfigurationSection("result-empty")));
            }
            return;
        }

        String type = plugin.getIntegrationManager().getMMOItemType(inputItems[0]);
        String id = plugin.getIntegrationManager().getMMOItemID(inputItems[0]);
        
        for (ItemStack item : inputItems) {
            if (item == null || !type.equals(plugin.getIntegrationManager().getMMOItemType(item)) || 
                !id.equals(plugin.getIntegrationManager().getMMOItemID(item))) {
                inventory.setItem(resultSlot, ItemUtils.createItem(menuSec.getConfigurationSection("preview-partial")));
                return;
            }
        }

        CombineRecipe recipe = plugin.getRecipeManager().getCombineRecipeFor(type, id);
        if (recipe == null) {
            inventory.setItem(resultSlot, ItemUtils.createItem(menuSec.getConfigurationSection("preview-partial")));
            return;
        }

        ItemStack resultItem = ItemUtils.getMMOItem(recipe.getResult().getType(), recipe.getResult().getId());
        if (resultItem == null) {
            inventory.setItem(resultSlot, ItemUtils.createItem(menuSec.getConfigurationSection("preview-partial")));
            return;
        }

        double baseSuccess = recipe.getSuccessRate();
        double bonus = (whiteScrollItem != null) ? (100.0 - baseSuccess) : 0.0;
        double finalSuccess = baseSuccess + bonus;
        double price = recipe.getPrice();
        String currency = recipe.getCurrency();

        ConfigurationSection previewSec = menuSec.getConfigurationSection("preview");
        List<String> customLore = previewSec.getStringList("lore").stream()
                .map(line -> ItemUtils.legacy.serialize(ItemUtils.parseText(line
                                 .replace("{success_rate}", ItemUtils.formatNumber(finalSuccess))
                                 .replace("{bonus_rate}", ItemUtils.formatNumber(bonus))
                                 .replace("{price}", ItemUtils.formatNumber(price))
                                 .replace("{currency}", currency))))
                .collect(Collectors.toList());

        ItemMeta meta = resultItem.getItemMeta();
        if (meta != null) {
            meta.setLore(customLore);
            String previewName = previewSec.getString("name");
            if (previewName != null && !previewName.isEmpty()) {
                meta.setDisplayName(ItemUtils.legacy.serialize(ItemUtils.parseText(previewName)));
            }
            resultItem.setItemMeta(meta);
        }

        inventory.setItem(resultSlot, resultItem);
    }

    public void processCombine() {
        boolean allFilled = true;
        for (ItemStack item : inputItems) {
            if (item == null || item.getType() == Material.AIR) {
                allFilled = false;
                break;
            }
        }
        if (!allFilled) return;

        String type = plugin.getIntegrationManager().getMMOItemType(inputItems[0]);
        String id = plugin.getIntegrationManager().getMMOItemID(inputItems[0]);
        CombineRecipe recipe = plugin.getRecipeManager().getCombineRecipeFor(type, id);
        if (recipe == null) return;

        double price = recipe.getPrice();
        String currency = recipe.getCurrency();

        if (!plugin.getIntegrationManager().takeCurrency(player, currency, price)) {
            player.sendMessage(ItemUtils.legacy.serialize(ItemUtils.parseText(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("enhance.insufficient-funds")
                .replace("{price}", ItemUtils.formatNumber(price))
                .replace("{currency}", currency))));
            plugin.getConfigManager().playSound(player, "insufficient-funds");
            return;
        }

        double chance = recipe.getSuccessRate();
        boolean usingWhiteScroll = whiteScrollItem != null;
        
        // Trigger Start Event
        id.seria.cyayorune.api.event.RuneCombineEvent.Start startEvent = new id.seria.cyayorune.api.event.RuneCombineEvent.Start(player, recipe, inputItems, usingWhiteScroll);
        org.bukkit.Bukkit.getPluginManager().callEvent(startEvent);
        if (startEvent.isCancelled()) return;

        if (usingWhiteScroll) {
            chance = 100.0;
            whiteScrollItem = null;
        }

        if (Math.random() * 100 <= chance) {
            ItemStack result = ItemUtils.getMMOItem(recipe.getResult().getType(), recipe.getResult().getId());
            if (result != null) {
                // Trigger Success Event
                id.seria.cyayorune.api.event.RuneCombineEvent.Success successEvent = new id.seria.cyayorune.api.event.RuneCombineEvent.Success(player, recipe, inputItems, result);
                org.bukkit.Bukkit.getPluginManager().callEvent(successEvent);
                
                ItemStack finalResult = successEvent.getResultItem();
                player.getInventory().addItem(finalResult).values().forEach(remaining -> player.getWorld().dropItem(player.getLocation(), remaining));
                player.sendMessage(ItemUtils.legacy.serialize(ItemUtils.parseText(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("combine.success")
                    .replace("{item_name}", ItemUtils.toMiniMessage(finalResult.getItemMeta().getDisplayName())))));
                plugin.getConfigManager().playSound(player, "success");
            }
        } else {
            // Trigger Failure Event
            org.bukkit.Bukkit.getPluginManager().callEvent(new id.seria.cyayorune.api.event.RuneCombineEvent.Failure(player, recipe, inputItems));
            
            player.sendMessage(ItemUtils.legacy.serialize(ItemUtils.parseText(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("combine.failed"))));
            plugin.getConfigManager().playSound(player, "failure");
        }

        for (int i = 0; i < 4; i++) {
            inputItems[i] = null;
        }

        render();
    }

    public void setInputItem(int index, ItemStack item) {
        if (index >= 0 && index < 4) {
            inputItems[index] = item;
            render();
        }
    }

    public ItemStack[] getInputItems() { return inputItems; }
    
    public void setWhiteScrollItem(ItemStack item) {
        this.whiteScrollItem = item;
        render();
    }
    
    public ItemStack getWhiteScrollItem() { return whiteScrollItem; }

    public List<Integer> getInputSlots() { return inputSlots; }
    public int getResultSlot() { return resultSlot; }
    public int getWhiteScrollSlot() { return whiteScrollSlot; }
}

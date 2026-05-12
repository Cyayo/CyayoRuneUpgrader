package id.seria.cyayorune.menu;

import id.seria.cyayorune.CyayoRune;
import id.seria.cyayorune.model.EnhanceRecipe;
import id.seria.cyayorune.util.ItemUtils;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EnhanceMenu extends BaseMenu {

    private final int mainSlot;
    private final int materialSlot;
    private final int resultSlot;
    private final int whiteScrollSlot = 16;
    
    private ItemStack mainItem;
    private ItemStack materialItem;
    private ItemStack whiteScrollItem;

    public EnhanceMenu(CyayoRune plugin, Player player) {
        super(plugin, player, 
            plugin.getConfigManager().getMenuEnhance().getString("menu.title", "Enhance Rune"),
            plugin.getConfigManager().getMenuEnhance().getInt("menu.size", 27));
        
        ConfigurationSection slots = plugin.getConfigManager().getMenuEnhance().getConfigurationSection("menu.slots");
        this.mainSlot = slots.getInt("main", 11);
        this.materialSlot = slots.getInt("material", 13);
        this.resultSlot = slots.getInt("result", 15);
    }

    @Override
    public void update() {
        render();
    }

    @Override
    public void open() {
        render();
        player.openInventory(inventory);
    }

    public void render() {
        inventory.clear();
        ConfigurationSection menuSec = plugin.getConfigManager().getMenuEnhance().getConfigurationSection("menu");
        ItemStack filler = ItemUtils.createItem(menuSec.getConfigurationSection("filler"));
        fillFiller(filler);

        ConfigurationSection placeholders = menuSec.getConfigurationSection("slots.placeholders");

        // Main Slot
        if (mainItem == null || mainItem.getType() == Material.AIR) {
            inventory.setItem(mainSlot, ItemUtils.createItem(placeholders.getConfigurationSection("main")));
        } else {
            inventory.setItem(mainSlot, mainItem);
        }

        // Material Slot
        if (materialItem == null || materialItem.getType() == Material.AIR) {
            inventory.setItem(materialSlot, ItemUtils.createItem(placeholders.getConfigurationSection("material")));
        } else {
            inventory.setItem(materialSlot, materialItem);
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
        ConfigurationSection menuSec = plugin.getConfigManager().getMenuEnhance().getConfigurationSection("menu");
        ConfigurationSection placeholders = menuSec.getConfigurationSection("slots.placeholders");
        
        if (mainItem == null || mainItem.getType() == Material.AIR) {
            inventory.setItem(resultSlot, ItemUtils.createItem(placeholders.getConfigurationSection("result-empty")));
            return;
        }

        String type = plugin.getIntegrationManager().getMMOItemType(mainItem);
        String id = plugin.getIntegrationManager().getMMOItemID(mainItem);
        EnhanceRecipe recipe = plugin.getRecipeManager().getEnhanceRecipeFor(type, id);

        if (recipe == null) {
            inventory.setItem(resultSlot, ItemUtils.createItem(placeholders.getConfigurationSection("result-empty")));
            return;
        }

        if (materialItem == null || materialItem.getType() == Material.AIR) {
            inventory.setItem(resultSlot, ItemUtils.createItem(menuSec.getConfigurationSection("preview-partial")));
            return;
        }

        String matType = plugin.getIntegrationManager().getMMOItemType(materialItem);
        String matId = plugin.getIntegrationManager().getMMOItemID(materialItem);
        
        EnhanceRecipe.MaterialData materialData = null;
        for (EnhanceRecipe.MaterialData mat : recipe.getMaterials()) {
            if (mat.getMmoitem().matches(matType, matId)) {
                materialData = mat;
                break;
            }
        }

        if (materialData == null) {
            inventory.setItem(resultSlot, ItemUtils.createItem(menuSec.getConfigurationSection("preview-partial")));
            return;
        }

        ItemStack resultPreview = mainItem.clone();
        
        double baseChance = materialData.getSuccessRate();
        double bonus = (whiteScrollItem != null) ? (100.0 - baseChance) : 0.0;
        double finalChance = baseChance + bonus;
        double increase = materialData.getSuccessIncrease();
        double price = materialData.getPrice();
        String currency = materialData.getCurrency();

        double currentStat = 0;
        try {
            LiveMMOItem mmoItem = new LiveMMOItem(mainItem);
            if (mmoItem.hasData(ItemStats.SUCCESS_RATE)) {
                currentStat = ((DoubleData) mmoItem.getData(ItemStats.SUCCESS_RATE)).getValue();
            }
        } catch (Exception ignored) {}
        double resultStat = Math.min(recipe.getMaxSuccess(), currentStat + increase);

        ConfigurationSection previewSec = menuSec.getConfigurationSection("preview");
        List<String> customLore = previewSec.getStringList("lore").stream()
                .map(line -> ItemUtils.legacy.serialize(ItemUtils.parseText(line
                                 .replace("{chance}", ItemUtils.formatNumber(finalChance))
                                 .replace("{bonus_rate}", ItemUtils.formatNumber(bonus))
                                 .replace("{increase}", ItemUtils.formatNumber(increase))
                                 .replace("{result_stat}", ItemUtils.formatNumber(resultStat))
                                 .replace("{price}", ItemUtils.formatNumber(price))
                                 .replace("{currency}", currency))))
                .collect(Collectors.toList());

        ItemMeta meta = resultPreview.getItemMeta();
        if (meta != null) {
            meta.setLore(customLore);
            String previewName = previewSec.getString("name");
            if (previewName != null && !previewName.isEmpty()) {
                meta.setDisplayName(ItemUtils.legacy.serialize(ItemUtils.parseText(previewName)));
            }
            resultPreview.setItemMeta(meta);
        }
        
        inventory.setItem(resultSlot, resultPreview);
    }

    public void processEnhance() {
        if (mainItem == null || materialItem == null) return;

        String type = plugin.getIntegrationManager().getMMOItemType(mainItem);
        String id = plugin.getIntegrationManager().getMMOItemID(mainItem);
        EnhanceRecipe recipe = plugin.getRecipeManager().getEnhanceRecipeFor(type, id);
        if (recipe == null) return;

        String matType = plugin.getIntegrationManager().getMMOItemType(materialItem);
        String matId = plugin.getIntegrationManager().getMMOItemID(materialItem);
        
        EnhanceRecipe.MaterialData materialData = null;
        for (EnhanceRecipe.MaterialData mat : recipe.getMaterials()) {
            if (mat.getMmoitem().matches(matType, matId)) {
                materialData = mat;
                break;
            }
        }

        if (materialData == null) return;

        double price = materialData.getPrice();
        String currency = materialData.getCurrency();

        if (!plugin.getIntegrationManager().takeCurrency(player, currency, price)) {
            player.sendMessage(ItemUtils.legacy.serialize(ItemUtils.parseText(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("enhance.insufficient-funds")
                .replace("{price}", ItemUtils.formatNumber(price))
                .replace("{currency}", currency))));
            plugin.getConfigManager().playSound(player, "insufficient-funds");
            return;
        }

        double chance = materialData.getSuccessRate();
        boolean usingWhiteScroll = whiteScrollItem != null;

        // Trigger Start Event
        id.seria.cyayorune.api.event.RuneEnhanceEvent.Start startEvent = new id.seria.cyayorune.api.event.RuneEnhanceEvent.Start(player, recipe, mainItem, materialItem, usingWhiteScroll);
        org.bukkit.Bukkit.getPluginManager().callEvent(startEvent);
        if (startEvent.isCancelled()) return;

        if (usingWhiteScroll) {
            chance = 100.0;
            whiteScrollItem = null;
        }

        if (Math.random() * 100 <= chance) {
            ItemStack upgraded = injectSuccessRate(mainItem, materialData.getSuccessIncrease(), recipe.getMaxSuccess());
            
            // Trigger Success Event
            id.seria.cyayorune.api.event.RuneEnhanceEvent.Success successEvent = new id.seria.cyayorune.api.event.RuneEnhanceEvent.Success(player, recipe, mainItem, materialItem, upgraded, materialData.getSuccessIncrease());
            org.bukkit.Bukkit.getPluginManager().callEvent(successEvent);
            
            ItemStack finalUpgraded = successEvent.getResultItem();
            player.sendMessage(ItemUtils.legacy.serialize(ItemUtils.parseText(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("enhance.success")
                .replace("{item_name}", ItemUtils.toMiniMessage(finalUpgraded.getItemMeta().getDisplayName())))));
            
            // Give upgraded item directly to inventory/drop
            player.getInventory().addItem(finalUpgraded).values().forEach(remaining -> player.getWorld().dropItem(player.getLocation(), remaining));
            mainItem = null; // Consume from GUI
            plugin.getConfigManager().playSound(player, "success");
        } else {
            // Trigger Failure Event
            org.bukkit.Bukkit.getPluginManager().callEvent(new id.seria.cyayorune.api.event.RuneEnhanceEvent.Failure(player, recipe, mainItem, materialItem));
            
            player.sendMessage(ItemUtils.legacy.serialize(ItemUtils.parseText(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("enhance.failed"))));
            // Material lost, but main item stays in GUI (common logic)
            plugin.getConfigManager().playSound(player, "failure");
        }
        
        materialItem = null;
        render();
    }

    private ItemStack injectSuccessRate(ItemStack item, double increase, double max) {
        try {
            LiveMMOItem mmoItem = new LiveMMOItem(item);
            double current = 0;
            if (mmoItem.hasData(ItemStats.SUCCESS_RATE)) {
                current = ((DoubleData) mmoItem.getData(ItemStats.SUCCESS_RATE)).getValue();
            }
            
            double newValue = Math.min(max, current + increase);
            mmoItem.setData(ItemStats.SUCCESS_RATE, new DoubleData(newValue));
            
            ItemStack result = mmoItem.newBuilder().build();
            plugin.getLogger().info("Success Rate Injected: " + current + " -> " + newValue + " for " + player.getName());
            return result;
        } catch (Exception e) {
            plugin.getLogger().severe("Error injecting Success Rate: " + e.getMessage());
            e.printStackTrace();
            return item;
        }
    }

    public ItemStack getMainItem() { return mainItem; }
    public void setMainItem(ItemStack mainItem) { this.mainItem = mainItem; render(); }

    public ItemStack getMaterialItem() { return materialItem; }
    public void setMaterialItem(ItemStack materialItem) { this.materialItem = materialItem; render(); }

    public ItemStack getWhiteScrollItem() { return whiteScrollItem; }
    public void setWhiteScrollItem(ItemStack whiteScrollItem) { this.whiteScrollItem = whiteScrollItem; render(); }

    public int getMainSlot() { return mainSlot; }
    public int getMaterialSlot() { return materialSlot; }
    public int getResultSlot() { return resultSlot; }
    public int getWhiteScrollSlot() { return whiteScrollSlot; }
}

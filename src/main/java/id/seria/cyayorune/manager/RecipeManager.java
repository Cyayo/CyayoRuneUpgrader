package id.seria.cyayorune.manager;

import id.seria.cyayorune.CyayoRune;
import id.seria.cyayorune.model.CombineRecipe;
import id.seria.cyayorune.model.EnhanceRecipe;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeManager {

    private final CyayoRune plugin;
    private final Map<String, EnhanceRecipe> enhanceRecipes = new HashMap<>();
    private final Map<String, CombineRecipe> combineRecipes = new HashMap<>();

    public RecipeManager(CyayoRune plugin) {
        this.plugin = plugin;
    }

    public void load() {
        enhanceRecipes.clear();
        combineRecipes.clear();

        loadEnhanceRecipes();
        loadCombineRecipes();
    }

    private void loadEnhanceRecipes() {
        File file = new File(plugin.getDataFolder(), "items/enhance.yml");
        if (!file.exists()) return;

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(reader);
            ConfigurationSection section = config.getConfigurationSection("recipes");
            if (section == null) return;

            for (String key : section.getKeys(false)) {
                ConfigurationSection recipeSection = section.getConfigurationSection(key);
                if (recipeSection == null) continue;

                EnhanceRecipe.MMOItemData baseItem = loadMMOItemData(recipeSection.getConfigurationSection("base-item"));
                double maxSuccess = recipeSection.getDouble("max-success", 100.0);
                
                List<EnhanceRecipe.MaterialData> materials = new ArrayList<>();
                List<Map<?, ?>> materialList = recipeSection.getMapList("materials");
                for (Map<?, ?> map : materialList) {
                    EnhanceRecipe.MMOItemData mmoitem = null;
                    if (map.containsKey("mmoitem")) {
                        Map<?, ?> mmoitemMap = (Map<?, ?>) map.get("mmoitem");
                        mmoitem = new EnhanceRecipe.MMOItemData(
                                (String) mmoitemMap.get("type"),
                                (String) mmoitemMap.get("id")
                        );
                    }
                    Object amountObj = map.get("amount");
                    int amount = (amountObj instanceof Number n) ? n.intValue() : 1;
                    
                    Object srObj = map.get("success-rate");
                    double successRate = (srObj instanceof Number n) ? n.doubleValue() : 100.0;
                    
                    Object siObj = map.get("success-increase");
                    double successIncrease = (siObj instanceof Number n) ? n.doubleValue() : 0.0;
                    
                    Object priceObj = map.get("price");
                    double price = (priceObj instanceof Number n) ? n.doubleValue() : 0.0;
                    
                    Object currObj = map.get("currency");
                    String currency = (currObj instanceof String s) ? s : "gins";

                    materials.add(new EnhanceRecipe.MaterialData(mmoitem, amount, successRate, successIncrease, price, currency));
                }

                enhanceRecipes.put(key, new EnhanceRecipe(key, baseItem, maxSuccess, materials));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCombineRecipes() {
        File file = new File(plugin.getDataFolder(), "items/combine.yml");
        if (!file.exists()) return;

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(reader);
            ConfigurationSection section = config.getConfigurationSection("recipes");
            if (section == null) return;

            for (String key : section.getKeys(false)) {
                ConfigurationSection recipeSection = section.getConfigurationSection(key);
                if (recipeSection == null) continue;

                EnhanceRecipe.MMOItemData input = loadMMOItemData(recipeSection.getConfigurationSection("input"));
                EnhanceRecipe.MMOItemData result = loadMMOItemData(recipeSection.getConfigurationSection("result"));
                double successRate = recipeSection.getDouble("success-rate", 100.0);
                double price = recipeSection.getDouble("price", 0.0);
                String currency = recipeSection.getString("currency", "gins");

                combineRecipes.put(key, new CombineRecipe(key, input, result, successRate, price, currency));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private EnhanceRecipe.MMOItemData loadMMOItemData(ConfigurationSection section) {
        if (section == null) return null;
        ConfigurationSection mmoSection = section.getConfigurationSection("mmoitem");
        if (mmoSection == null) return null;
        return new EnhanceRecipe.MMOItemData(mmoSection.getString("type"), mmoSection.getString("id"));
    }

    public Map<String, EnhanceRecipe> getEnhanceRecipes() {
        return enhanceRecipes;
    }

    public Map<String, CombineRecipe> getCombineRecipes() {
        return combineRecipes;
    }

    public EnhanceRecipe getEnhanceRecipeFor(String type, String id) {
        for (EnhanceRecipe recipe : enhanceRecipes.values()) {
            if (recipe.getBaseItem().matches(type, id)) return recipe;
        }
        return null;
    }

    public CombineRecipe getCombineRecipeFor(String type, String id) {
        for (CombineRecipe recipe : combineRecipes.values()) {
            if (recipe.getInput().matches(type, id)) return recipe;
        }
        return null;
    }
    
    public boolean isWhitelisted(org.bukkit.inventory.ItemStack item) {
        String type = plugin.getIntegrationManager().getMMOItemType(item);
        String id = plugin.getIntegrationManager().getMMOItemID(item);
        if (type == null || id == null) return false;
        
        // Check if it's a base item for enhance
        if (getEnhanceRecipeFor(type, id) != null) return true;
        
        // Check if it's a material for enhance
        for (EnhanceRecipe recipe : enhanceRecipes.values()) {
            for (EnhanceRecipe.MaterialData material : recipe.getMaterials()) {
                if (material.getMmoitem().matches(type, id)) return true;
            }
        }
        
        // Check if it's an input for combine
        if (getCombineRecipeFor(type, id) != null) return true;
        
        // Check if it's a White Scroll (from config)
        String wsType = plugin.getConfig().getString("white-scroll.type");
        String wsID = plugin.getConfig().getString("white-scroll.id");
        if (type.equalsIgnoreCase(wsType) && id.equalsIgnoreCase(wsID)) return true;

        return false;
    }
}

package id.seria.cyayorune.api;

import id.seria.cyayorune.manager.ConfigManager;
import id.seria.cyayorune.manager.RecipeManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface CyayoRuneAPI {

    /**
     * Membuka menu penggabungan rune (Combine Menu) untuk player.
     * 
     * @param player Player yang akan dibuka menunya
     */
    void openCombineMenu(@NotNull Player player);

    /**
     * Membuka menu peningkatan rune (Enhance Menu) untuk player.
     * 
     * @param player Player yang akan dibuka menunya
     */
    void openEnhanceMenu(@NotNull Player player);

    /**
     * Mendapatkan RecipeManager untuk mengelola resep kombinasi dan peningkatan.
     * 
     * @return RecipeManager instance
     */
    @NotNull
    RecipeManager getRecipeManager();

    /**
     * Mendapatkan ConfigManager untuk mengakses pesan dan suara.
     * 
     * @return ConfigManager instance
     */
    @NotNull
    ConfigManager getConfigManager();
}

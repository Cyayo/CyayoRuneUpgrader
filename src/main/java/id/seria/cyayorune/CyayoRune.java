package id.seria.cyayorune;

import id.seria.cyayorune.api.CyayoRuneAPI;
import id.seria.cyayorune.api.CyayoRuneProvider;
import id.seria.cyayorune.manager.ConfigManager;
import id.seria.cyayorune.manager.IntegrationManager;
import id.seria.cyayorune.manager.MenuManager;
import id.seria.cyayorune.manager.RecipeManager;
import id.seria.cyayorune.listener.MenuListener;
import id.seria.cyayorune.menu.CombineMenu;
import id.seria.cyayorune.menu.EnhanceMenu;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class CyayoRune extends JavaPlugin implements CyayoRuneAPI {

    private static CyayoRune instance;
    private ConfigManager configManager;
    private IntegrationManager integrationManager;
    private RecipeManager recipeManager;
    private MenuManager menuManager;

    @Override
    public void onEnable() {
        instance = this;
        CyayoRuneProvider.register(this);

        // Initialize Managers
        this.configManager = new ConfigManager(this);
        this.integrationManager = new IntegrationManager(this);
        this.recipeManager = new RecipeManager(this);
        this.menuManager = new MenuManager(this);

        // Load data
        reloadPlugin();

        // Register Listeners
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);

        // Register Commands
        getCommand("cyayoitemcombine").setExecutor(new id.seria.cyayorune.command.MainCommand(this));

        getLogger().info("CyayoRune has been enabled!");
    }

    @Override
    public void onDisable() {
        CyayoRuneProvider.unregister();
        getLogger().info("CyayoRune has been disabled!");
    }

    // --- API Implementation ---

    @Override
    public void openCombineMenu(@NotNull Player player) {
        new CombineMenu(this, player).open();
    }

    @Override
    public void openEnhanceMenu(@NotNull Player player) {
        new EnhanceMenu(this, player).open();
    }

    @Override
    @NotNull
    public RecipeManager getRecipeManager() {
        return recipeManager;
    }

    @Override
    @NotNull
    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void reloadPlugin() {
        configManager.load();
        recipeManager.load();
        getLogger().info("CyayoRune configurations reloaded.");
    }

    public static CyayoRune getInstance() {
        return instance;
    }

    public IntegrationManager getIntegrationManager() {
        return integrationManager;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }
}

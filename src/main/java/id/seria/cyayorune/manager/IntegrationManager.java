package id.seria.cyayorune.manager;

import id.seria.cyayorune.CyayoRune;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.excellenteconomy.api.ExcellentEconomyAPI;
import su.nightexpress.excellenteconomy.api.currency.ExcellentCurrency;
import su.nightexpress.excellenteconomy.api.currency.operation.NotificationTarget;
import su.nightexpress.excellenteconomy.api.currency.operation.OperationContext;
import org.bukkit.plugin.RegisteredServiceProvider;

public class IntegrationManager {

    private final CyayoRune plugin;
    private boolean mmoItemsEnabled;
    private boolean excellentEconomyEnabled;
    private ExcellentEconomyAPI excellentEconomyApi;
    private OperationContext operationContext;
    private boolean placeholderApiEnabled;

    public IntegrationManager(CyayoRune plugin) {
        this.plugin = plugin;
        this.mmoItemsEnabled = Bukkit.getPluginManager().isPluginEnabled("MMOItems");
        this.excellentEconomyEnabled = Bukkit.getPluginManager().isPluginEnabled("ExcellentEconomy");
        if (this.excellentEconomyEnabled) {
            RegisteredServiceProvider<ExcellentEconomyAPI> provider = Bukkit.getServer().getServicesManager().getRegistration(ExcellentEconomyAPI.class);
            if (provider != null) {
                this.excellentEconomyApi = provider.getProvider();
                this.operationContext = OperationContext.custom("CyayoRune")
                    .silentFor(NotificationTarget.USER, NotificationTarget.EXECUTOR, NotificationTarget.CONSOLE_LOGGER);
            }
        }
        this.placeholderApiEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    public boolean isMMOItemsEnabled() {
        return mmoItemsEnabled;
    }

    public boolean isExcellentEconomyEnabled() {
        return excellentEconomyEnabled;
    }

    public boolean isPlaceholderApiEnabled() {
        return placeholderApiEnabled;
    }

    public MMOItem getMMOItem(ItemStack item) {
        if (!mmoItemsEnabled || item == null) return null;
        return MMOItems.getTypeName(item) != null ? MMOItems.getID(item) != null ? MMOItems.plugin.getMMOItem(Type.get(MMOItems.getTypeName(item)), MMOItems.getID(item)) : null : null;
    }

    // Simplified MMOItems check
    public String getMMOItemType(ItemStack item) {
        if (!mmoItemsEnabled || item == null) return null;
        return MMOItems.getTypeName(item);
    }

    public String getMMOItemID(ItemStack item) {
        if (!mmoItemsEnabled || item == null) return null;
        return MMOItems.getID(item);
    }

    public boolean takeCurrency(org.bukkit.entity.Player player, String currencyId, double amount) {
        if (!excellentEconomyEnabled || excellentEconomyApi == null) return true;
        ExcellentCurrency currency = excellentEconomyApi.getCurrency(currencyId);
        if (currency == null) return false;
        
        if (excellentEconomyApi.getBalance(player, currency) < amount) return false;
        
        return excellentEconomyApi.withdraw(player, currency, amount, operationContext);
    }

    public double getBalance(org.bukkit.entity.Player player, String currencyId) {
        if (!excellentEconomyEnabled || excellentEconomyApi == null) return 0;
        ExcellentCurrency currency = excellentEconomyApi.getCurrency(currencyId);
        if (currency == null) return 0;
        return excellentEconomyApi.getBalance(player, currency);
    }
}

package id.seria.cyayorune.manager;

import id.seria.cyayorune.CyayoRune;
import id.seria.cyayorune.menu.CombineMenu;
import id.seria.cyayorune.menu.EnhanceMenu;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MenuManager {

    private final CyayoRune plugin;
    private final Map<UUID, EnhanceMenu> enhanceMenus = new HashMap<>();
    private final Map<UUID, CombineMenu> combineMenus = new HashMap<>();

    public MenuManager(CyayoRune plugin) {
        this.plugin = plugin;
    }

    public void openEnhanceMenu(Player player) {
        EnhanceMenu menu = new EnhanceMenu(plugin, player);
        enhanceMenus.put(player.getUniqueId(), menu);
        menu.open();
    }

    public void openCombineMenu(Player player) {
        CombineMenu menu = new CombineMenu(plugin, player);
        combineMenus.put(player.getUniqueId(), menu);
        menu.open();
    }

    public void removeMenu(Player player) {
        enhanceMenus.remove(player.getUniqueId());
        combineMenus.remove(player.getUniqueId());
    }

    public EnhanceMenu getEnhanceMenu(Player player) {
        return enhanceMenus.get(player.getUniqueId());
    }

    public CombineMenu getCombineMenu(Player player) {
        return combineMenus.get(player.getUniqueId());
    }
}

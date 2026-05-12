package id.seria.cyayorune.util;

import id.seria.cyayorune.CyayoRune;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemUtils {

    public static final MiniMessage mm = MiniMessage.miniMessage();
    public static final LegacyComponentSerializer legacy = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();
    public static final LegacyComponentSerializer ampersand = LegacyComponentSerializer.legacyAmpersand();

    public static net.kyori.adventure.text.Component parseText(String text) {
        if (text == null || text.isEmpty()) return net.kyori.adventure.text.Component.empty();
        
        String input = text;
        // 1. Handle Legacy Hex Format (§x§r§r§g§g§b§b or &x&r&r&g&g&b&b)
        input = input.replaceAll("[§&]x[§&]([0-9a-fA-F])[§&]([0-9a-fA-F])[§&]([0-9a-fA-F])[§&]([0-9a-fA-F])[§&]([0-9a-fA-F])[§&]([0-9a-fA-F])", "<#$1$2$3$4$5$6>");

        // 2. Handle Simple Hex Format (&#RRGGBB)
        input = input.replaceAll("&#([a-fA-F0-9]{6})", "<#$1>");

        // 3. Pre-process standard legacy codes (§ and &) to MiniMessage tags
        String processed = input
                .replace("§", "&")
                .replace("&0", "<reset><black>").replace("&1", "<reset><dark_blue>").replace("&2", "<reset><dark_green>")
                .replace("&3", "<reset><dark_aqua>").replace("&4", "<reset><dark_red>").replace("&5", "<reset><dark_purple>")
                .replace("&6", "<reset><gold>").replace("&7", "<reset><gray>").replace("&8", "<reset><dark_gray>")
                .replace("&9", "<reset><blue>").replace("&a", "<reset><green>").replace("&b", "<reset><aqua>")
                .replace("&c", "<reset><red>").replace("&d", "<reset><light_purple>").replace("&e", "<reset><yellow>")
                .replace("&f", "<reset><white>")
                .replace("&l", "<bold>").replace("&m", "<strikethrough>")
                .replace("&n", "<underline>").replace("&o", "<italic>").replace("&r", "<reset>")
                .replace("&k", "<obfuscated>");

        try {
            return mm.deserialize(processed);
        } catch (Exception e) {
            return ampersand.deserialize(input);
        }
    }

    public static String toMiniMessage(String text) {
        if (text == null || text.isEmpty()) return "";
        return mm.serialize(parseText(text));
    }

    public static String color(String text) {
        if (text == null || text.isEmpty()) return "";
        if (text.contains("\n")) {
            String[] lines = text.split("\n", -1);
            for (int i = 0; i < lines.length; i++) {
                lines[i] = color(lines[i]);
            }
            return String.join("\n", lines);
        }
        return legacy.serialize(parseText(text));
    }

    public static ItemStack createItem(ConfigurationSection section) {
        if (section == null) return new ItemStack(Material.AIR);
        
        String materialName = section.getString("material", "STONE");
        Material material = Material.matchMaterial(materialName);
        if (material == null) material = Material.STONE;
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String name = section.getString("name");
            if (name != null) {
                meta.setDisplayName(legacy.serialize(parseText(name)));
            }
            
            List<String> lore = section.getStringList("lore");
            if (!lore.isEmpty()) {
                List<String> formattedLore = new ArrayList<>();
                for (String line : lore) {
                    formattedLore.add(legacy.serialize(parseText(line)));
                }
                meta.setLore(formattedLore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
    
    public static ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (name != null) {
                meta.setDisplayName(legacy.serialize(parseText(name)));
            }
            if (lore != null) {
                List<String> formattedLore = new ArrayList<>();
                for (String line : lore) {
                    formattedLore.add(legacy.serialize(parseText(line)));
                }
                meta.setLore(formattedLore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public static String formatNumber(double value) {
        return String.format("%.1f", value).replace(",", ".");
    }

    public static ItemStack getMMOItem(String type, String id) {
        Type mmoType = Type.get(type);
        if (mmoType == null) return null;
        return MMOItems.plugin.getItem(mmoType, id);
    }
}

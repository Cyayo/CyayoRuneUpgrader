package id.seria.cyayorune.command;

import id.seria.cyayorune.CyayoRune;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import id.seria.cyayorune.util.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainCommand implements CommandExecutor, TabCompleter {

    private final CyayoRune plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();

    public MainCommand(CyayoRune plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(legacy.serialize(ItemUtils.parseText(plugin.getConfigManager().getMessage("general.only-player"))));
                return true;
            }
            if (!player.hasPermission("cyayoitemcombine.use")) {
                player.sendMessage(legacy.serialize(ItemUtils.parseText(plugin.getConfigManager().getMessage("general.no-permission"))));
                return true;
            }
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "enhance":
            case "combine":
                if (args.length >= 2) {
                    // Admin mode: /cic <menu> <player>
                    if (!sender.hasPermission("cyayoitemcombine.admin")) {
                        sender.sendMessage(legacy.serialize(ItemUtils.parseText(plugin.getConfigManager().getMessage("general.no-permission"))));
                        return true;
                    }
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage(legacy.serialize(ItemUtils.parseText(plugin.getConfigManager().getMessage("general.player-not-found").replace("{player}", args[1]))));
                        return true;
                    }
                    if (sub.equals("enhance")) {
                        plugin.getMenuManager().openEnhanceMenu(target);
                    } else {
                        plugin.getMenuManager().openCombineMenu(target);
                    }
                    plugin.getConfigManager().playSound(target, "open-menu");
                    sender.sendMessage(legacy.serialize(ItemUtils.parseText(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage(sub + ".gui-opened").replace("{player}", target.getName()))));
                } else {
                    // Self mode: /cic <menu>
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage(legacy.serialize(ItemUtils.parseText(plugin.getConfigManager().getMessage("general.only-player"))));
                        return true;
                    }
                    if (!player.hasPermission("cyayoitemcombine.use")) {
                        sender.sendMessage(legacy.serialize(ItemUtils.parseText(plugin.getConfigManager().getMessage("general.no-permission"))));
                        return true;
                    }
                    if (sub.equals("enhance")) {
                        plugin.getMenuManager().openEnhanceMenu(player);
                    } else {
                        plugin.getMenuManager().openCombineMenu(player);
                    }
                    plugin.getConfigManager().playSound(player, "open-menu");
                }
                break;

            case "reload":
                if (!sender.hasPermission("cyayoitemcombine.admin")) {
                    sender.sendMessage(legacy.serialize(ItemUtils.parseText(plugin.getConfigManager().getMessage("general.no-permission"))));
                    return true;
                }
                plugin.reloadPlugin();
                sender.sendMessage(legacy.serialize(ItemUtils.parseText(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("general.reload"))));
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(legacy.serialize(ItemUtils.parseText("<gradient:#00B4DB:#0083B0><b>CyayoRune Help</b></gradient>")));
        sender.sendMessage(legacy.serialize(ItemUtils.parseText("<gray>/cic enhance <white>- Buka menu enhance</white>")));
        sender.sendMessage(legacy.serialize(ItemUtils.parseText("<gray>/cic combine <white>- Buka menu combine</white>")));
        if (sender.hasPermission("cyayoitemcombine.admin")) {
            sender.sendMessage(legacy.serialize(ItemUtils.parseText("<gray>/cic enhance <player> <white>- Buka menu enhance untuk pemain</white>")));
            sender.sendMessage(legacy.serialize(ItemUtils.parseText("<gray>/cic combine <player> <white>- Buka menu combine untuk pemain</white>")));
            sender.sendMessage(legacy.serialize(ItemUtils.parseText("<gray>/cic reload <white>- Muat ulang plugin</white>")));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("enhance");
            completions.add("combine");
            if (sender.hasPermission("cyayoitemcombine.admin")) {
                completions.add("reload");
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("enhance") || args[0].equalsIgnoreCase("combine"))) {
            if (sender.hasPermission("cyayoitemcombine.admin")) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            }
        }
        
        String input = args[args.length - 1].toLowerCase();
        return completions.stream().filter(s -> s.startsWith(input)).collect(Collectors.toList());
    }
}

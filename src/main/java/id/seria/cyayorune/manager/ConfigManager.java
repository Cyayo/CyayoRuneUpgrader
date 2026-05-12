package id.seria.cyayorune.manager;

import id.seria.cyayorune.CyayoRune;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private final CyayoRune plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private FileConfiguration sounds;
    private FileConfiguration menuEnhance;
    private FileConfiguration menuCombine;

    public ConfigManager(CyayoRune plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();

        this.messages = loadResource("messages.yml");
        this.sounds = loadResource("sounds.yml");
        this.menuEnhance = loadResource("menus/enhance.yml");
        this.menuCombine = loadResource("menus/combine.yml");
        
        // Ensure items folder exists and save defaults
        saveResourceIfNotExists("items/enhance.yml");
        saveResourceIfNotExists("items/combine.yml");
    }

    private void saveResourceIfNotExists(String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            plugin.saveResource(name, false);
        }
    }

    private FileConfiguration loadResource(String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            plugin.saveResource(name, false);
        }
        try (java.io.InputStreamReader reader = new java.io.InputStreamReader(new java.io.FileInputStream(file), java.nio.charset.StandardCharsets.UTF_8)) {
            return YamlConfiguration.loadConfiguration(reader);
        } catch (IOException e) {
            e.printStackTrace();
            return YamlConfiguration.loadConfiguration(file);
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public FileConfiguration getSounds() {
        return sounds;
    }

    public FileConfiguration getMenuEnhance() {
        return menuEnhance;
    }

    public FileConfiguration getMenuCombine() {
        return menuCombine;
    }

    public String getMessage(String path) {
        return messages.getString(path, "Missing message: " + path);
    }

    public String getPrefix() {
        return messages.getString("prefix", "");
    }
    public void playSound(org.bukkit.entity.Player player, String path) {
        if (sounds == null) return;
        org.bukkit.configuration.ConfigurationSection section = sounds.getConfigurationSection("sounds." + path);
        if (section == null || !section.getBoolean("enabled", true)) return;
        
        String soundName = section.getString("sound");
        if (soundName == null) return;
        
        try {
            org.bukkit.Sound sound = org.bukkit.Sound.valueOf(soundName);
            float volume = (float) section.getDouble("volume", 1.0);
            float pitch = (float) section.getDouble("pitch", 1.0);
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound name in sounds.yml: " + soundName);
        }
    }
}

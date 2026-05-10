package com.quickcrates.storage;
import com.quickcrates.QuickCrates;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.util.*;

public class StorageManager {
    private final QuickCrates plugin;
    private final File file;
    public StorageManager(QuickCrates plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "virtual-keys.yml");
        load();
    }
    public void load() {
        if (!file.exists()) return;
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        for (String uuidStr : cfg.getKeys(false)) {
            try {
                UUID u = UUID.fromString(uuidStr);
                Map<String,Integer> m = new HashMap<>();
                for (String k : cfg.getConfigurationSection(uuidStr).getKeys(false))
                    m.put(k, cfg.getInt(uuidStr+"."+k));
                plugin.getKeyManager().getVirtualMap().put(u,m);
            } catch (Exception ignored) {}
        }
    }
    public void saveAll() {
        YamlConfiguration cfg = new YamlConfiguration();
        for (var e : plugin.getKeyManager().getVirtualMap().entrySet())
            for (var k : e.getValue().entrySet())
                cfg.set(e.getKey().toString()+"."+k.getKey(), k.getValue());
        try { cfg.save(file); } catch (Exception ex) {
            plugin.getLogger().warning("Failed saving virtual keys: " + ex.getMessage());
        }
    }
}

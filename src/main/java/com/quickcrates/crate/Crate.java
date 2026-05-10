package com.quickcrates.crate;

import com.quickcrates.QuickCrates;
import com.quickcrates.animation.AnimationType;
import com.quickcrates.reward.Reward;
import com.quickcrates.reward.WeightedTable;
import com.quickcrates.util.Items;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Crate {
    private final String id;
    private final String displayName;
    private final ItemStack keyItem;
    private final AnimationType animation;
    private final List<Location> locations;
    private final WeightedTable rewards;
    private final int previewRows;
    private final String previewTitle;

    public Crate(String id, String displayName, ItemStack keyItem, AnimationType animation,
                 List<Location> locations, WeightedTable rewards,
                 int previewRows, String previewTitle) {
        this.id = id;
        this.displayName = displayName;
        this.keyItem = keyItem;
        this.animation = animation;
        this.locations = locations;
        this.rewards = rewards;
        this.previewRows = previewRows;
        this.previewTitle = previewTitle;
    }

    public static Crate load(QuickCrates plugin, File file) {
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        String id = cfg.getString("id", file.getName().replace(".yml", ""));
        String name = cfg.getString("display-name", id);

        ItemStack key = Items.fromConfig(cfg.getConfigurationSection("key"));
        Items.tag(plugin, key, "qc_key", id);

        AnimationType anim = AnimationType.fromString(
                cfg.getString("animation", plugin.getConfig().getString(
                        "settings.default-animation", "SPIN")));

        List<Location> locs = new ArrayList<>();
        if (cfg.isList("locations")) {
            for (Object o : cfg.getList("locations")) {
                if (o instanceof java.util.Map<?,?> m) {
                    try {
                        org.bukkit.World w = org.bukkit.Bukkit.getWorld(String.valueOf(m.get("world")));
                        if (w == null) continue;
                        double x = ((Number) m.get("x")).doubleValue();
                        double y = ((Number) m.get("y")).doubleValue();
                        double z = ((Number) m.get("z")).doubleValue();
                        locs.add(new Location(w, x, y, z));
                    } catch (Exception ignored) {}
                }
            }
        }

        List<Reward> rewards = new ArrayList<>();
        ConfigurationSection rs = cfg.getConfigurationSection("rewards");
        if (rs == null && cfg.isList("rewards")) {
            for (Object o : cfg.getMapList("rewards")) {
                if (o instanceof java.util.Map<?,?> m) {
                    YamlConfiguration tmp = new YamlConfiguration();
                    flatten(m, tmp, "");
                    rewards.add(Reward.fromConfig(tmp));
                }
            }
        } else if (rs != null) {
            for (String k : rs.getKeys(false))
                rewards.add(Reward.fromConfig(rs.getConfigurationSection(k)));
        }

        int rows = cfg.getInt("preview-gui.rows", 3);
        String title = cfg.getString("preview-gui.title", "&8" + name);

        return new Crate(id, name, key, anim, locs, new WeightedTable(rewards), rows, title);
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public ItemStack getKeyItem() { return keyItem.clone(); }
    public AnimationType getAnimation() { return animation; }
    public List<Location> getLocations() { return locations; }
    public WeightedTable getRewards() { return rewards; }
    public int getPreviewRows() { return previewRows; }
    public String getPreviewTitle() { return previewTitle; }

    public boolean isAt(Location loc) {
        for (Location l : locations) {
            if (l.getWorld() != null && l.getWorld().equals(loc.getWorld())
                    && l.getBlockX() == loc.getBlockX()
                    && l.getBlockY() == loc.getBlockY()
                    && l.getBlockZ() == loc.getBlockZ()) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static void flatten(java.util.Map<?,?> map, org.bukkit.configuration.file.YamlConfiguration cfg, String prefix) {
        for (var e : map.entrySet()) {
            String key = prefix.isEmpty() ? e.getKey().toString() : prefix + "." + e.getKey().toString();
            Object val = e.getValue();
            if (val instanceof java.util.Map) {
                flatten((java.util.Map<?,?>) val, cfg, key);
            } else {
                cfg.set(key, val);
            }
        }
    }
}

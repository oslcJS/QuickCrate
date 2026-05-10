package com.quickcrates.crate;

import com.quickcrates.QuickCrates;
import org.bukkit.Location;

import java.io.File;
import java.util.*;

public class CrateManager {
    private final QuickCrates plugin;
    private final Map<String, Crate> crates = new HashMap<>();
    private final Set<UUID> activeOpens = new HashSet<>();

    public CrateManager(QuickCrates plugin) { this.plugin = plugin; }

    public void loadAll() {
        crates.clear();
        File dir = new File(plugin.getDataFolder(), "crates");
        if (!dir.exists()) dir.mkdirs();
        File[] files = dir.listFiles((d,n) -> n.toLowerCase().endsWith(".yml"));
        if (files == null) return;
        for (File f : files) {
            try {
                Crate c = Crate.load(plugin, f);
                crates.put(c.getId().toLowerCase(), c);
            } catch (Exception ex) {
                plugin.getLogger().warning("Failed to load crate " + f.getName() + ": " + ex.getMessage());
            }
        }
    }

    public void shutdown() {
        activeOpens.clear();
    }

    public Collection<Crate> getCrates() { return crates.values(); }
    public Crate get(String id) { return id == null ? null : crates.get(id.toLowerCase()); }

    public Crate getAt(Location loc) {
        for (Crate c : crates.values()) if (c.isAt(loc)) return c;
        return null;
    }

    public boolean lock(UUID uuid) { return activeOpens.add(uuid); }
    public void unlock(UUID uuid) { activeOpens.remove(uuid); }
    public boolean isOpening(UUID uuid) { return activeOpens.contains(uuid); }
}

package com.quickcrates.quicklink;

import com.quickcrates.QuickCrates;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public final class QuickLink {

    private static final File LINK_DIR = new File("plugins/QuickLink");
    private static QuickCrates plugin;

    private QuickLink() {}

    public static void register(QuickCrates p) {
        plugin = p;
        LINK_DIR.mkdirs();
        write();
        p.getLogger().info("QuickLink registered.");
    }

    public static void unregister() {
        File f = linkFile();
        if (f.exists()) f.delete();
    }

    public static boolean isLinked(String pluginName) {
        return new File(LINK_DIR, pluginName + ".link").exists();
    }

    public static YamlConfiguration readLink(String pluginName) {
        File f = new File(LINK_DIR, pluginName + ".link");
        if (!f.exists()) return null;
        return YamlConfiguration.loadConfiguration(f);
    }

    private static void write() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("name", "QuickCrates");
        cfg.set("version", plugin.getDescription().getVersion());
        cfg.set("server", plugin.getServer().getBukkitVersion());
        try { cfg.save(linkFile()); } catch (Exception e) {
            plugin.getLogger().warning("Failed to write QuickLink file: " + e.getMessage());
        }
    }

    private static File linkFile() {
        return new File(LINK_DIR, "QuickCrates.link");
    }
}

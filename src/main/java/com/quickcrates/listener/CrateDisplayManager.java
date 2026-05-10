package com.quickcrates.listener;

import com.quickcrates.QuickCrates;
import com.quickcrates.crate.Crate;
import com.quickcrates.util.Msg;
import org.bukkit.*;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CrateDisplayManager implements Listener {

    private static final Map<String, UUID> labels = new HashMap<>();
    private static QuickCrates plugin;

    public CrateDisplayManager(QuickCrates plugin) {
        CrateDisplayManager.plugin = plugin;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        World world = event.getWorld();
        for (Crate crate : plugin.getCrateManager().getCrates()) {
            for (Location loc : crate.getLocations()) {
                if (loc.getWorld().equals(world)) {
                    spawnLabel(loc, crate);
                }
            }
        }
    }

    public static void spawnLabel(Location blockLoc, Crate crate) {
        World world = blockLoc.getWorld();
        if (world == null) return;

        String key = locKey(blockLoc);
        removeLabel(blockLoc);

        double height = plugin.getConfig().getDouble("display.height", 1.8);
        Location spawnLoc = blockLoc.clone().add(0.5, height, 0.5);
        spawnLoc.setYaw(0);
        spawnLoc.setPitch(0);

        try {
            TextDisplay display = (TextDisplay) world.spawnEntity(spawnLoc, EntityType.TEXT_DISPLAY);

            display.setText(Msg.color(crate.getDisplayName()));

            String billboard = plugin.getConfig().getString("display.billboard", "CENTER");
            try {
                display.setBillboard(Display.Billboard.valueOf(billboard.toUpperCase()));
            } catch (Exception e) {
                display.setBillboard(Display.Billboard.CENTER);
            }

            display.setSeeThrough(plugin.getConfig().getBoolean("display.see-through", false));
            display.setShadowed(plugin.getConfig().getBoolean("display.shadow", true));
            display.setDefaultBackground(false);

            int bgAlpha = plugin.getConfig().getInt("display.background-alpha", 0);
            display.setBackgroundColor(Color.fromARGB(bgAlpha, 0, 0, 0));

            display.setAlignment(TextDisplay.TextAlignment.CENTER);

            float scale = (float) plugin.getConfig().getDouble("display.scale", 1.4);
            Transformation t = new Transformation(
                    new Vector3f(0, 0, 0),
                    new AxisAngle4f(0, 0, 0, 1),
                    new Vector3f(scale, scale, scale),
                    new AxisAngle4f(0, 0, 0, 1)
            );
            display.setTransformation(t);
            display.setVisibleByDefault(true);
            display.setPersistent(false);

            labels.put(key, display.getUniqueId());
        } catch (Throwable e) {
            plugin.getLogger().warning("Could not spawn label at " + key + ": " + e.getMessage());
        }
    }

    public static void removeLabel(Location blockLoc) {
        String key = locKey(blockLoc);
        UUID uid = labels.remove(key);
        if (uid == null) return;
        Entity entity = Bukkit.getEntity(uid);
        if (entity != null) entity.remove();
    }

    public static void respawnAll(QuickCrates p) {
        plugin = p;
        Set<String> missingWorlds = new HashSet<>();
        for (Crate crate : p.getCrateManager().getCrates()) {
            for (Location loc : crate.getLocations()) {
                if (loc.getWorld() == null) {
                    missingWorlds.add(loc.getWorld() != null ? loc.getWorld().getName() : "null");
                    continue;
                }
                spawnLabel(loc, crate);
            }
        }
        if (!missingWorlds.isEmpty()) {
            p.getLogger().info("Waiting for worlds to load: " + String.join(", ", missingWorlds));
        }
    }

    public static void removeAll() {
        for (UUID uid : labels.values()) {
            Entity e = Bukkit.getEntity(uid);
            if (e != null) e.remove();
        }
        labels.clear();
    }

    private static String locKey(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX()
                + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }
}

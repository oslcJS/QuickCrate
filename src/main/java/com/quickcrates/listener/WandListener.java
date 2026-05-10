package com.quickcrates.listener;

import com.quickcrates.QuickCrates;
import com.quickcrates.animation.AnimationRunner;
import com.quickcrates.crate.Crate;
import com.quickcrates.crate.CrateManager;
import com.quickcrates.util.Msg;
import com.quickcrates.util.Items;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.EnumSet;
import java.util.Set;

public class WandListener implements Listener {

    private static final Set<Material> CONTAINER_TYPES = EnumSet.of(
            Material.CHEST, Material.TRAPPED_CHEST,
            Material.BARREL,
            Material.SHULKER_BOX,
            Material.WHITE_SHULKER_BOX, Material.ORANGE_SHULKER_BOX, Material.MAGENTA_SHULKER_BOX,
            Material.LIGHT_BLUE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX, Material.LIME_SHULKER_BOX,
            Material.PINK_SHULKER_BOX, Material.GRAY_SHULKER_BOX, Material.LIGHT_GRAY_SHULKER_BOX,
            Material.CYAN_SHULKER_BOX, Material.PURPLE_SHULKER_BOX, Material.BLUE_SHULKER_BOX,
            Material.BROWN_SHULKER_BOX, Material.GREEN_SHULKER_BOX, Material.RED_SHULKER_BOX,
            Material.BLACK_SHULKER_BOX
    );

    private final QuickCrates plugin;
    private final AnimationRunner runner;

    public WandListener(QuickCrates plugin) {
        this.plugin = plugin;
        this.runner = new AnimationRunner(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;

        Player p = e.getPlayer();
        ItemStack hand = p.getInventory().getItemInMainHand();
        if (!isWand(hand)) return;

        Block block = e.getClickedBlock();
        if (block == null) return;

        e.setCancelled(true);

        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            handleRemove(p, block);
            return;
        }
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        handleRightClick(p, block);
    }

    

    private void handleRightClick(Player p, Block block) {
        CrateManager cm = plugin.getCrateManager();
        Crate existing = cm.getAt(block.getLocation());

        if (existing != null) {
            
            if (cm.isOpening(p.getUniqueId())) return;

            ItemStack inHand = p.getInventory().getItemInMainHand(); 
            boolean usedPhysical = false;

            if (plugin.getKeyManager().isKeyFor(inHand, existing)) {
                
                plugin.getKeyManager().takePhysical(p, existing, 1);
                usedPhysical = true;
            } else if (plugin.getKeyManager().hasPhysical(p, existing, 1)) {
                plugin.getKeyManager().takePhysical(p, existing, 1);
                usedPhysical = true;
            } else if (plugin.getKeyManager().getVirtual(p, existing.getId()) > 0) {
                plugin.getKeyManager().takeVirtual(p, existing.getId(), 1);
            } else {
                Msg.send(p, "no-key");
                p.sendMessage(Msg.color("&7This is the &e" + existing.getDisplayName()
                        + " &7crate. You need a key to open it."));
                return;
            }

            if (p.getInventory().firstEmpty() == -1) {
                Msg.send(p, "inventory-full");
                if (usedPhysical) p.getInventory().addItem(existing.getKeyItem());
                else plugin.getKeyManager().addVirtual(p, existing.getId(), 1);
                return;
            }

            cm.lock(p.getUniqueId());
            runner.play(p, existing);
        } else {
            
            if (!CONTAINER_TYPES.contains(block.getType())) {
                p.sendMessage(Msg.color(plugin.getConfig()
                        .getString("messages.prefix", "&6[QuickCrates] &r")
                        + "&cThat block can't be a crate. Use a chest, barrel, or shulker."));
                return;
            }
            WandPickerGui.open(plugin, p, block);
        }
    }

    

    private void handleRemove(Player p, Block block) {
        Crate crate = plugin.getCrateManager().getAt(block.getLocation());
        if (crate == null) {
            p.sendMessage(Msg.color(plugin.getConfig()
                    .getString("messages.prefix", "&6[QuickCrates] &r")
                    + "&cThis block is not linked to any crate."));
            return;
        }
        
        removeLocationFromYaml(crate, block);
        
        CrateDisplayManager.removeLabel(block.getLocation());
        plugin.getCrateManager().loadAll();
        p.sendMessage(Msg.color(plugin.getConfig()
                .getString("messages.prefix", "&6[QuickCrates] &r")
                + "&aRemoved &e" + crate.getDisplayName() + " &afrom this block."));
    }

    private void removeLocationFromYaml(Crate crate, Block block) {
        java.io.File file = new java.io.File(plugin.getDataFolder(), "crates/" + crate.getId() + ".yml");
        org.bukkit.configuration.file.YamlConfiguration cfg =
                org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
        if (!cfg.isList("locations")) return;
        java.util.List<java.util.Map<String, Object>> kept = new java.util.ArrayList<>();
        for (Object o : cfg.getList("locations")) {
            if (!(o instanceof java.util.Map<?, ?> m)) continue;
            try {
                int x = ((Number) m.get("x")).intValue();
                int y = ((Number) m.get("y")).intValue();
                int z = ((Number) m.get("z")).intValue();
                String world = String.valueOf(m.get("world"));
                if (x == block.getX() && y == block.getY() && z == block.getZ()
                        && world.equals(block.getWorld().getName())) continue; 
            } catch (Exception ignored) {}
            java.util.Map<String, Object> nm = new java.util.HashMap<>();
            for (var en : m.entrySet()) nm.put(String.valueOf(en.getKey()), en.getValue());
            kept.add(nm);
        }
        cfg.set("locations", kept);
        try { cfg.save(file); } catch (Exception ignored) {}
    }

    

    public static boolean isWand(ItemStack item) {
        if (item == null || item.getType() != Material.STICK) return false;
        String tag = Items.readTag(QuickCrates.get(), item, "qc_wand");
        return "true".equals(tag);
    }

    public static ItemStack createWand() {
        ItemStack wand = new ItemStack(Material.STICK);
        org.bukkit.inventory.meta.ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Msg.color("&6&lCrate Wand"));
            meta.setLore(java.util.List.of(
                    Msg.color("&7&oRight-click a chest/barrel/shulker to link it"),
                    Msg.color("&7&oto a crate, or spin it open."),
                    Msg.color("&7&oLeft-click a linked block to unlink it.")
            ));
            wand.setItemMeta(meta);
        }
        Items.tag(QuickCrates.get(), wand, "qc_wand", "true");
        return wand;
    }
}

package com.quickcrates.listener;

import com.quickcrates.QuickCrates;
import com.quickcrates.crate.Crate;
import com.quickcrates.gui.DonutSmpGui;
import com.quickcrates.reward.Reward;
import com.quickcrates.util.Msg;
import com.quickcrates.util.Items;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class GuiClickListener implements Listener {
    private final QuickCrates plugin;
    private final Set<UUID> donutSmpPlayers = new HashSet<>();

    public GuiClickListener(QuickCrates plugin) { this.plugin = plugin; }

    public void trackDonutSmp(UUID uuid) {
        donutSmpPlayers.add(uuid);
    }

    private boolean isAnimation(String title) {
        return title != null && title.contains("Opening...");
    }

    private boolean isWandPicker(String title) {
        return title != null && title.startsWith(WandPickerGui.TITLE_PREFIX);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView() == null) return;
        String title = e.getView().getTitle();
        Player player = (Player) e.getWhoClicked();

        
        if (isAnimation(title)) {
            e.setCancelled(true);
            return;
        }

        
        if (DonutSmpGui.isDonutSmpGui(title)) {
            e.setCancelled(true);
            if (e.getClickedInventory() == null
                    || !e.getClickedInventory().equals(e.getView().getTopInventory())) return;
            ItemStack current = e.getCurrentItem();
            if (current == null || current.getType().isAir()) return;
            String rewardId = Items.readTag(plugin, current, "qc_donut_reward");
            String crateId = Items.readTag(plugin, current, "qc_donut_crate");
            if (rewardId == null || crateId == null) return;

            Crate crate = plugin.getCrateManager().get(crateId);
            if (crate == null) return;

            Reward selected = null;
            for (Reward r : crate.getRewards().all()) {
                if (r.getId().equals(rewardId)) { selected = r; break; }
            }
            if (selected == null) return;

            donutSmpPlayers.remove(player.getUniqueId());
            player.closeInventory();
            plugin.getCrateManager().unlock(player.getUniqueId());

            if (player.getInventory().firstEmpty() == -1) {
                Msg.send(player, "inventory-full");
                return;
            }

            selected.give(player);
            Msg.send(player, "win", "reward", selected.displayName(), "crate", crate.getDisplayName());
            return;
        }

        
        
        if (isPreview(title)) {
            
            if (e.getClickedInventory() != null
                    && e.getClickedInventory().equals(e.getView().getTopInventory())) {
                
                if (e.isShiftClick()) {
                    
                    return;
                }
                
                e.setCancelled(true);
            }
            
            return;
        }

        
        if (isWandPicker(title)) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null || e.getCurrentItem().getType().isAir()) return;
            if (!WandPickerGui.hasPending(player.getUniqueId())) return;

            
            List<Crate> crates = new ArrayList<>(plugin.getCrateManager().getCrates());
            int slot = e.getSlot();
            if (slot < 0 || slot >= crates.size()) return;
            Crate chosen = crates.get(slot);

            Block block = WandPickerGui.consumePending(player.getUniqueId());
            player.closeInventory();

            if (block == null) return;

            
            addLocationToYaml(chosen, block);
            plugin.getCrateManager().loadAll();

            
            CrateDisplayManager.spawnLabel(block.getLocation(), chosen);

            player.sendMessage(Msg.color(plugin.getConfig()
                    .getString("messages.prefix", "&6[QuickCrates] &r")
                    + "&aLinked block to &e" + chosen.getDisplayName() + "&a!"));
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        if (donutSmpPlayers.remove(player.getUniqueId())) {
            plugin.getCrateManager().unlock(player.getUniqueId());
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getView() == null) return;
        String title = e.getView().getTitle();
        if (isAnimation(title) || DonutSmpGui.isDonutSmpGui(title) || isWandPicker(title)) {
            e.setCancelled(true);
        }
        
    }

    

    private boolean isPreview(String title) {
        
        
        if (title == null) return false;
        String low = title.toLowerCase();
        return low.contains("reward") || low.contains("preview");
    }

    private void addLocationToYaml(Crate crate, Block block) {
        File file = new File(plugin.getDataFolder(), "crates/" + crate.getId() + ".yml");
        org.bukkit.configuration.file.YamlConfiguration cfg =
                org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);

        List<Map<String, Object>> locs = new ArrayList<>();
        if (cfg.isList("locations")) {
            for (Object o : cfg.getList("locations")) {
                if (o instanceof Map<?, ?> m) {
                    Map<String, Object> nm = new LinkedHashMap<>();
                    for (var en : m.entrySet()) nm.put(String.valueOf(en.getKey()), en.getValue());
                    locs.add(nm);
                }
            }
        }
        Map<String, Object> nl = new LinkedHashMap<>();
        nl.put("world", block.getWorld().getName());
        nl.put("x", block.getX());
        nl.put("y", block.getY());
        nl.put("z", block.getZ());
        locs.add(nl);
        cfg.set("locations", locs);
        try { cfg.save(file); } catch (Exception ignored) {}
    }
}

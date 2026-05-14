package com.quickcrates.listener;

import com.quickcrates.QuickCrates;
import com.quickcrates.animation.AnimationRunner;
import com.quickcrates.crate.Crate;
import com.quickcrates.crate.CrateMode;
import com.quickcrates.gui.DonutSmpGui;
import com.quickcrates.gui.PreviewGui;
import com.quickcrates.util.Msg;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class CrateInteractListener implements Listener {
    private final QuickCrates plugin;
    private final AnimationRunner runner;
    private final GuiClickListener guiClickListener;

    public CrateInteractListener(QuickCrates plugin, GuiClickListener guiClickListener) {
        this.plugin = plugin;
        this.runner = new AnimationRunner(plugin);
        this.guiClickListener = guiClickListener;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        
        if (e.getHand() == EquipmentSlot.HAND) {
            ItemStack main = e.getPlayer().getInventory().getItemInMainHand();
            if (WandListener.isWand(main)) return;
        }

        Block block = e.getClickedBlock();
        if (block == null) return;
        Crate crate = plugin.getCrateManager().getAt(block.getLocation());
        if (crate == null) return;

        
        e.setCancelled(true);

        boolean offhand = e.getHand() == EquipmentSlot.OFF_HAND;
        if (offhand && !plugin.getConfig().getBoolean("settings.offhand-open", true)) return;

        Player p = e.getPlayer();

        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            PreviewGui.open(p, crate);
            return;
        }
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (plugin.getCrateManager().isOpening(p.getUniqueId())) return;

        CrateMode mode = CrateMode.fromString(plugin.getConfig().getString("settings.mode", "DEFAULT"));

        if (mode == CrateMode.DONUT_SMP) {
            plugin.getCrateManager().lock(p.getUniqueId());
            guiClickListener.trackDonutSmp(p.getUniqueId());
            Msg.playSound(p, "settings.open-sound", "BLOCK_ENDER_CHEST_OPEN", 0.9f, 0.8f);
            DonutSmpGui.open(plugin, p, crate);
            return;
        }

        ItemStack inHand = offhand
                ? p.getInventory().getItemInOffHand()
                : p.getInventory().getItemInMainHand();
        boolean usedPhysical = false;

        if (plugin.getKeyManager().isKeyFor(inHand, crate)) {
            if (!plugin.getKeyManager().takePhysical(p, crate, 1)) {
                Msg.send(p, "no-key"); return;
            }
            usedPhysical = true;
        } else if (plugin.getKeyManager().getVirtual(p, crate.getId()) > 0) {
            plugin.getKeyManager().takeVirtual(p, crate.getId(), 1);
        } else {
            Msg.send(p, "no-key");
            return;
        }

        if (p.getInventory().firstEmpty() == -1) {
            Msg.send(p, "inventory-full");
            if (usedPhysical) p.getInventory().addItem(crate.getKeyItem());
            else plugin.getKeyManager().addVirtual(p, crate.getId(), 1);
            return;
        }

        plugin.getCrateManager().lock(p.getUniqueId());
        runner.play(p, crate);
    }
}

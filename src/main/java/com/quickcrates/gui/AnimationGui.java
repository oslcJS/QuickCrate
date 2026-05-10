package com.quickcrates.gui;

import com.quickcrates.QuickCrates;
import com.quickcrates.crate.Crate;
import com.quickcrates.reward.Reward;
import com.quickcrates.util.Items;
import com.quickcrates.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AnimationGui {
    public static final String GUI_TAG = "qc_anim";

    private final QuickCrates plugin;
    private final Player player;
    private final Crate crate;
    private final Reward winner;
    private final Inventory inv;

    public AnimationGui(QuickCrates plugin, Player player, Crate crate, Reward winner) {
        this.plugin = plugin;
        this.player = player;
        this.crate = crate;
        this.winner = winner;
        this.inv = Bukkit.createInventory(null, 27,
                Msg.color("&8" + crate.getDisplayName() + " &8- Opening..."));
        
        ItemStack glass = pane(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < 9; i++) inv.setItem(i, glass);
        for (int i = 18; i < 27; i++) inv.setItem(i, glass);
        
        ItemStack pointer = pane(Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta pm = pointer.getItemMeta();
        if (pm != null) { pm.setDisplayName(Msg.color("&e&l▼")); pointer.setItemMeta(pm); }
        inv.setItem(4, pointer);
        inv.setItem(22, pointer);
    }

    public void open() {
        Items.tag(plugin, pane(Material.AIR), "qc_gui", GUI_TAG); 
        player.openInventory(inv);
    }

    private ItemStack pane(Material m) { return new ItemStack(m); }

    private ItemStack randomDisplay() {
        List<Reward> all = crate.getRewards().all();
        return all.get(ThreadLocalRandom.current().nextInt(all.size())).getDisplay();
    }

    public void tickRandom() {
        for (int i = 9; i < 18; i++) inv.setItem(i, randomDisplay());
    }

    public void tickSpin() {
        
        for (int i = 9; i < 17; i++) inv.setItem(i, inv.getItem(i + 1));
        inv.setItem(17, randomDisplay());
    }

    public void tickCsgoScroll() {
        
        tickSpin();
    }

    public void tickRoulette() {
        
        ItemStack tmp = inv.getItem(9);
        for (int i = 9; i < 17; i++) inv.setItem(i, inv.getItem(i + 1));
        inv.setItem(17, tmp == null ? randomDisplay() : tmp);
    }

    public void placeWinner(Reward winner) {
        for (int i = 9; i < 18; i++) inv.setItem(i, randomDisplay());
        inv.setItem(13, winner.getDisplay());
    }

    public Inventory getInventory() { return inv; }
}

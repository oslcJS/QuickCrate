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

import java.util.ArrayList;
import java.util.List;

public class DonutSmpGui {
    public static final String GUI_TAG = "qc_donut";

    public static void open(QuickCrates plugin, Player player, Crate crate) {
        List<Reward> rewards = crate.getRewards().all();
        int rows = Math.max(3, Math.min(6, (int) Math.ceil(rewards.size() / 7.0) + 2));
        Inventory inv = Bukkit.createInventory(null, rows * 9,
                Msg.color("&8" + crate.getDisplayName() + " &8- Select Reward"));

        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta bm = border.getItemMeta();
        if (bm != null) bm.setDisplayName(" "); border.setItemMeta(bm);

        for (int i = 0; i < rows * 9; i++) {
            inv.setItem(i, border);
        }

        int slot = 9 + 1;
        for (Reward reward : rewards) {
            while (slot < (rows - 1) * 9 - 1 && (slot % 9 == 0 || slot % 9 == 8)) slot++;
            if (slot >= (rows - 1) * 9 - 1) break;

            ItemStack display = reward.getDisplay().clone();
            ItemMeta meta = display.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.add("");
                lore.add(Msg.color("&e&lClick to claim!"));
                meta.setLore(lore);
                display.setItemMeta(meta);
            }
            Items.tag(plugin, display, "qc_donut_reward", reward.getId());
            Items.tag(plugin, display, "qc_donut_crate", crate.getId());
            inv.setItem(slot++, display);
        }

        player.openInventory(inv);
    }

    public static boolean isDonutSmpGui(String title) {
        return title != null && title.contains("Select Reward");
    }
}

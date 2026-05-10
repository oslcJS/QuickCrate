package com.quickcrates.gui;

import com.quickcrates.compat.QuickEcoBridge;
import com.quickcrates.crate.Crate;
import com.quickcrates.reward.Reward;
import com.quickcrates.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PreviewGui {
    public static final String GUI_TAG = "qc_preview";

    public static void open(Player player, Crate crate) {
        int rows = Math.max(1, Math.min(6, crate.getPreviewRows()));
        Inventory inv = Bukkit.createInventory(null, rows * 9,
                Msg.color(crate.getPreviewTitle().replace("{crate}", crate.getDisplayName())));

        int slot = 0;
        for (Reward r : crate.getRewards().all()) {
            if (slot >= inv.getSize()) break;
            ItemStack disp = r.getDisplay();
            ItemMeta meta = disp.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.add("");
                lore.add(Msg.color("&7Chance: &e" + chancePercent(r, crate) + "%"));
                if (r.getMoney() > 0) {
                    String formatted = QuickEcoBridge.isLinked()
                            ? QuickEcoBridge.format(r.getMoney())
                            : String.format("%.2f", r.getMoney());
                    lore.add(Msg.color("&6Money: &e" + formatted));
                }
                meta.setLore(lore);
                disp.setItemMeta(meta);
            }
            inv.setItem(slot++, disp);
        }
        player.openInventory(inv);
    }

    private static String chancePercent(Reward r, Crate c) {
        int total = 0;
        for (Reward x : c.getRewards().all()) total += x.getWeight();
        double pct = (r.getWeight() * 100.0) / Math.max(1, total);
        return String.format("%.2f", pct);
    }
}

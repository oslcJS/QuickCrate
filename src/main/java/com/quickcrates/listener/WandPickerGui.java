package com.quickcrates.listener;

import com.quickcrates.QuickCrates;
import com.quickcrates.crate.Crate;
import com.quickcrates.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class WandPickerGui {
    public static final String TITLE_PREFIX = "§6Link block to crate";

    
    private static final Map<UUID, Block> pending = new HashMap<>();

    public static void open(QuickCrates plugin, Player p, Block block) {
        Collection<Crate> crates = plugin.getCrateManager().getCrates();
        int rows = Math.max(1, (int) Math.ceil(crates.size() / 9.0));
        rows = Math.min(rows, 6);
        Inventory inv = Bukkit.createInventory(null, rows * 9, TITLE_PREFIX);

        int slot = 0;
        for (Crate c : crates) {
            if (slot >= inv.getSize()) break;
            ItemStack icon = c.getKeyItem();
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(Msg.color(c.getDisplayName()));
                List<String> lore = new ArrayList<>();
                lore.add(Msg.color("&7Click to link this block to &e" + c.getDisplayName()));
                lore.add(Msg.color("&8ID: " + c.getId()));
                meta.setLore(lore);
                icon.setItemMeta(meta);
            }
            inv.setItem(slot++, icon);
        }

        pending.put(p.getUniqueId(), block);
        p.openInventory(inv);
    }

    public static Block consumePending(UUID uuid) {
        return pending.remove(uuid);
    }

    public static boolean hasPending(UUID uuid) {
        return pending.containsKey(uuid);
    }
}

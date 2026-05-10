package com.quickcrates.util;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class Items {
    private Items() {}

    public static ItemStack fromConfig(ConfigurationSection sec) {
        if (sec == null) return new ItemStack(Material.STONE);
        Material mat;
        try {
            mat = Material.matchMaterial(sec.getString("material", "STONE"));
            if (mat == null) mat = Material.STONE;
        } catch (Exception e) { mat = Material.STONE; }

        ItemStack item = new ItemStack(mat, sec.getInt("amount", 1));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (sec.isString("name")) meta.setDisplayName(Msg.color(sec.getString("name")));
            if (sec.isList("lore")) {
                List<String> lore = new ArrayList<>();
                for (String l : sec.getStringList("lore")) lore.add(Msg.color(l));
                meta.setLore(lore);
            }
            if (sec.getBoolean("glow", false)) {
                
                try {
                    Enchantment glow = Enchantment.getByName("UNBREAKING");
                    if (glow == null) glow = Enchantment.getByName("DURABILITY");
                    if (glow == null && Enchantment.values().length > 0) glow = Enchantment.values()[0];
                    if (glow != null) meta.addEnchant(glow, 1, true);
                } catch (Throwable ignored) {}
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack tag(JavaPlugin plugin, ItemStack item, String key, String value) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, key),
                PersistentDataType.STRING, value);
        item.setItemMeta(meta);
        return item;
    }

    public static String readTag(JavaPlugin plugin, ItemStack item, String key) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().get(new NamespacedKey(plugin, key),
                PersistentDataType.STRING);
    }
}

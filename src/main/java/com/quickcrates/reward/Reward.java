package com.quickcrates.reward;

import com.quickcrates.QuickCrates;
import com.quickcrates.compat.QuickEcoBridge;
import com.quickcrates.util.Items;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Reward {
    private final String id;
    private final ItemStack display;
    private final int weight;
    private final List<ItemStack> items;
    private final List<String> commands;
    private final boolean broadcast;
    private final double money;

    public Reward(String id, ItemStack display, int weight, List<ItemStack> items, List<String> commands, boolean broadcast, double money) {
        this.id = id;
        this.display = display;
        this.weight = Math.max(1, weight);
        this.items = items;
        this.commands = commands;
        this.broadcast = broadcast;
        this.money = Math.max(0, money);
    }

    public static Reward fromConfig(ConfigurationSection sec) {
        String id = sec.getName();
        if (sec.isString("id")) id = sec.getString("id");
        ItemStack disp = Items.fromConfig(sec.getConfigurationSection("display"));
        int weight = sec.getInt("weight", 1);
        List<ItemStack> items = readItems(sec);
        List<String> cmds = new ArrayList<>(sec.getStringList("commands"));
        boolean bc = sec.getBoolean("broadcast", false);
        double money = sec.getDouble("money", 0.0);
        return new Reward(id, disp, weight, items, cmds, bc, money);
    }

    public String getId() { return id; }
    public ItemStack getDisplay() { return display.clone(); }
    public int getWeight() { return weight; }
    public boolean isBroadcast() { return broadcast; }
    public double getMoney() { return money; }

    public void give(Player player) {
        if (money > 0) {
            QuickEcoBridge.deposit(player, money);
        }
        for (ItemStack item : items) {
            giveItem(player, item.clone());
        }

        ConsoleCommandSender console = Bukkit.getConsoleSender();
        for (String cmd : commands) {
            String parsed = cmd.replace("{player}", player.getName());
            if (QuickCrates.get().getItemRegistry().isGiveCommand(parsed)) {
                QuickCrates.get().getItemRegistry().giveFromCommand(new RegistryPlayer(player), parsed);
                continue;
            }
            Bukkit.dispatchCommand(console, parsed);
        }
    }

    public String displayName() {
        if (display.hasItemMeta() && display.getItemMeta().hasDisplayName())
            return display.getItemMeta().getDisplayName();
        return id;
    }

    private static List<ItemStack> readItems(ConfigurationSection sec) {
        List<ItemStack> result = new ArrayList<>();
        if (!sec.isList("items")) {
            return result;
        }

        for (Object raw : sec.getList("items", List.of())) {
            if (raw instanceof String value) {
                ItemStack item = itemFromString(value);
                if (item != null) result.add(item);
            } else if (raw instanceof Map<?,?> map) {
                YamlConfiguration itemCfg = new YamlConfiguration();
                flatten(map, itemCfg, "");
                String material = itemCfg.getString("material", itemCfg.getString("type"));
                if (QuickCrates.get().getItemRegistry().isKnownItem(material)) {
                    result.add(Items.fromConfig(itemCfg));
                } else {
                    QuickCrates.get().getLogger().warning("Unknown item reward material '" + material + "' in reward '" + sec.getName() + "'.");
                }
            }
        }
        return result;
    }

    private static ItemStack itemFromString(String value) {
        String[] parts = value.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isBlank()) {
            return null;
        }
        int amount = 1;
        if (parts.length >= 2) {
            try {
                amount = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {}
        }
        ItemStack item = QuickCrates.get().getItemRegistry().create(parts[0], amount);
        if (item == null) {
            QuickCrates.get().getLogger().warning("Unknown item reward '" + value + "'.");
        }
        return item;
    }

    private void giveItem(Player player, ItemStack item) {
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
        for (ItemStack drop : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), drop);
        }
    }

    private record RegistryPlayer(Player player) implements com.quickcrates.util.ItemRegistry.PlayerLike {
        @Override public String name() { return player.getName(); }
        @Override public void add(ItemStack item) {
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
            for (ItemStack drop : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void flatten(Map<?,?> map, YamlConfiguration cfg, String prefix) {
        for (var entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey().toString() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                flatten((Map<?,?>) value, cfg, key);
            } else {
                cfg.set(key, value);
            }
        }
    }
}

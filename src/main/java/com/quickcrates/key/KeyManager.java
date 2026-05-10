package com.quickcrates.key;

import com.quickcrates.QuickCrates;
import com.quickcrates.crate.Crate;
import com.quickcrates.util.Items;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class KeyManager {
    private final QuickCrates plugin;
    
    private final Map<UUID, Map<String, Integer>> virtual = new ConcurrentHashMap<>();

    public KeyManager(QuickCrates plugin) { this.plugin = plugin; }

    public boolean isKeyFor(ItemStack stack, Crate crate) {
        if (stack == null) return false;
        String tag = Items.readTag(plugin, stack, "qc_key");
        return tag != null && tag.equalsIgnoreCase(crate.getId());
    }

    public int getVirtual(Player p, String crateId) {
        return virtual.getOrDefault(p.getUniqueId(), new HashMap<>())
                .getOrDefault(crateId.toLowerCase(), 0);
    }

    public void addVirtual(Player p, String crateId, int amount) {
        virtual.computeIfAbsent(p.getUniqueId(), k -> new HashMap<>())
                .merge(crateId.toLowerCase(), amount, Integer::sum);
    }

    public boolean takeVirtual(Player p, String crateId, int amount) {
        Map<String, Integer> m = virtual.get(p.getUniqueId());
        if (m == null) return false;
        int have = m.getOrDefault(crateId.toLowerCase(), 0);
        if (have < amount) return false;
        m.put(crateId.toLowerCase(), have - amount);
        return true;
    }

    public boolean takePhysical(Player p, Crate crate, int amount) {
        int taken = 0;
        ItemStack[] contents = p.getInventory().getContents();
        for (int i = 0; i < contents.length && taken < amount; i++) {
            ItemStack it = contents[i];
            if (isKeyFor(it, crate)) {
                int need = amount - taken;
                if (it.getAmount() <= need) {
                    taken += it.getAmount();
                    contents[i] = null;
                } else {
                    it.setAmount(it.getAmount() - need);
                    taken += need;
                }
            }
        }
        if (taken < amount) return false;
        p.getInventory().setContents(contents);
        return true;
    }

    public boolean hasPhysical(Player p, Crate crate, int amount) {
        int count = 0;
        for (ItemStack it : p.getInventory().getContents()) {
            if (isKeyFor(it, crate)) count += it.getAmount();
            if (count >= amount) return true;
        }
        return count >= amount;
    }

    public Map<UUID, Map<String, Integer>> getVirtualMap() { return virtual; }
}

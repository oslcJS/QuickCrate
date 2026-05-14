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
    public static final String UNIVERSAL_ID = "__universal__";

    private final QuickCrates plugin;
    
    private final Map<UUID, Map<String, Integer>> virtual = new ConcurrentHashMap<>();

    public KeyManager(QuickCrates plugin) { this.plugin = plugin; }

    public boolean isKeyFor(ItemStack stack, Crate crate) {
        if (stack == null) return false;
        String tag = Items.readTag(plugin, stack, "qc_key");
        if (tag == null) return false;
        return tag.equalsIgnoreCase(crate.getId()) || (isUniversalEnabled() && tag.equals(UNIVERSAL_ID));
    }

    public boolean isUniversalKey(ItemStack stack) {
        if (stack == null) return false;
        return UNIVERSAL_ID.equals(Items.readTag(plugin, stack, "qc_key"));
    }

    public boolean isUniversalEnabled() {
        return plugin.getConfig().getBoolean("settings.universal-keys", false);
    }

    public boolean isVirtualEnabled() {
        return plugin.getConfig().getBoolean("settings.virtual-keys", true);
    }

    public int getVirtual(Player p, String crateId) {
        int total = virtual.getOrDefault(p.getUniqueId(), new HashMap<>())
                .getOrDefault(crateId.toLowerCase(), 0);
        if (isUniversalEnabled()) {
            total += virtual.getOrDefault(p.getUniqueId(), new HashMap<>())
                    .getOrDefault(UNIVERSAL_ID, 0);
        }
        return total;
    }

    public int countPhysical(Player p, Crate crate) {
        int count = 0;
        for (ItemStack it : p.getInventory().getContents()) {
            if (isKeyFor(it, crate)) count += it.getAmount();
        }
        return count;
    }

    public int countAll(Player p, Crate crate) {
        int total = countPhysical(p, crate);
        if (isVirtualEnabled()) total += getVirtual(p, crate.getId());
        return total;
    }

    public void addVirtual(Player p, String crateId, int amount) {
        virtual.computeIfAbsent(p.getUniqueId(), k -> new HashMap<>())
                .merge(crateId.toLowerCase(), amount, Integer::sum);
    }

    public boolean takeVirtual(Player p, String crateId, int amount) {
        Map<String, Integer> m = virtual.get(p.getUniqueId());
        if (m == null) return false;
        int have = m.getOrDefault(crateId.toLowerCase(), 0);
        if (have >= amount) {
            m.put(crateId.toLowerCase(), have - amount);
            return true;
        }
        if (isUniversalEnabled()) {
            int universal = m.getOrDefault(UNIVERSAL_ID, 0);
            int needed = amount - have;
            if (universal >= needed) {
                m.put(crateId.toLowerCase(), 0);
                m.put(UNIVERSAL_ID, universal - needed);
                return true;
            }
        }
        return false;
    }

    public boolean takeKeys(Player p, Crate crate, int amount) {
        int remaining = amount;

        ItemStack[] contents = p.getInventory().getContents();
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack it = contents[i];
            if (isKeyFor(it, crate)) {
                int take = Math.min(it.getAmount(), remaining);
                remaining -= take;
                if (it.getAmount() <= take) {
                    contents[i] = null;
                } else {
                    it.setAmount(it.getAmount() - take);
                }
            }
        }
        p.getInventory().setContents(contents);

        if (remaining > 0 && isVirtualEnabled()) {
            Map<String, Integer> m = virtual.get(p.getUniqueId());
            if (m != null) {
                int crateV = m.getOrDefault(crate.getId().toLowerCase(), 0);
                int fromCrate = Math.min(crateV, remaining);
                remaining -= fromCrate;
                m.put(crate.getId().toLowerCase(), crateV - fromCrate);

                if (remaining > 0 && isUniversalEnabled()) {
                    int uniV = m.getOrDefault(UNIVERSAL_ID, 0);
                    int fromUni = Math.min(uniV, remaining);
                    remaining -= fromUni;
                    m.put(UNIVERSAL_ID, uniV - fromUni);
                }
            }
        }

        return remaining <= 0;
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

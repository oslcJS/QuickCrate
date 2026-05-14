package com.quickcrates.command;
import com.quickcrates.QuickCrates;
import com.quickcrates.crate.Crate;
import com.quickcrates.key.KeyManager;
import com.quickcrates.util.Msg;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class KeyCommand implements CommandExecutor, TabCompleter {
    private final QuickCrates plugin;
    public KeyCommand(QuickCrates p){ this.plugin = p; }

    public boolean onCommand(@NotNull CommandSender s,@NotNull Command c,@NotNull String l,@NotNull String[] a){
        if (a.length >= 1 && a[0].equalsIgnoreCase("check")) {
            if (!(s instanceof Player p)) { s.sendMessage("Players only."); return true; }
            p.sendMessage(Msg.color("&6&l--- Your Keys ---"));
            boolean any = false;
            for (Crate crate : plugin.getCrateManager().getCrates()) {
                int physical = plugin.getKeyManager().countPhysical(p, crate);
                int virtual = plugin.getKeyManager().isVirtualEnabled()
                        ? plugin.getKeyManager().getVirtual(p, crate.getId()) : 0;
                int total = physical + virtual;
                if (total > 0) {
                    any = true;
                    p.sendMessage(Msg.color("&e" + crate.getDisplayName()
                            + " &7- &e" + total + " &7(&aphysical: &e" + physical
                            + " &7| &avirtual: &e" + virtual + "&7)"));
                }
            }
            if (plugin.getKeyManager().isUniversalEnabled()) {
                int uniPhysical = 0;
                for (ItemStack it : p.getInventory().getContents()) {
                    if (plugin.getKeyManager().isUniversalKey(it)) uniPhysical += it.getAmount();
                }
                int uniVirtual = plugin.getKeyManager().isVirtualEnabled()
                        ? plugin.getKeyManager().getVirtual(p, KeyManager.UNIVERSAL_ID) : 0;
                if (uniPhysical > 0 || uniVirtual > 0) {
                    any = true;
                    p.sendMessage(Msg.color("&6Universal Keys &7- &e" + (uniPhysical + uniVirtual)
                            + " &7(&aphysical: &e" + uniPhysical + " &7| &avirtual: &e" + uniVirtual + "&7)"));
                }
            }
            if (!any) p.sendMessage(Msg.color("&7You don't have any keys."));
            return true;
        }

        if (!s.hasPermission("quickcrates.admin")) { Msg.send(s,"no-permission"); return true; }
        if (a.length < 2) { s.sendMessage(Msg.color("&cUsage: /key <give|givevirtual|check> <player> <crate> [amount]")); return true; }
        String sub = a[0].toLowerCase();
        if (a.length < 3) { s.sendMessage(Msg.color("&cUsage: /key " + sub + " <player> <crate> [amount]")); return true; }
        Player t = plugin.getServer().getPlayerExact(a[1]);
        if (t == null) { s.sendMessage(Msg.color("&cPlayer not found.")); return true; }
        Crate crate = plugin.getCrateManager().get(a[2]);
        if (crate == null) { s.sendMessage(Msg.color("&cUnknown crate.")); return true; }
        int amt = 1; if (a.length>=4) try{amt=Integer.parseInt(a[3]);}catch(Exception e){}
        if (sub.equals("givevirtual")) {
            plugin.getKeyManager().addVirtual(t, crate.getId(), amt);
            plugin.getStorageManager().saveAll();
            s.sendMessage(Msg.color("&aGave " + amt + " virtual keys."));
        } else if (sub.equals("give")) {
            ItemStack key = crate.getKeyItem(); key.setAmount(amt);
            t.getInventory().addItem(key);
            s.sendMessage(Msg.color("&aGave " + amt + " physical keys."));
        } else {
            s.sendMessage(Msg.color("&cUsage: /key <give|givevirtual|check> <player> <crate> [amount]"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
        if (a.length == 1) {
            return List.of("give", "givevirtual", "check");
        }
        if (a.length == 2 && (a[0].equalsIgnoreCase("give") || a[0].equalsIgnoreCase("givevirtual"))) {
            List<String> out = new ArrayList<>();
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(a[1].toLowerCase())) {
                    out.add(p.getName());
                }
            }
            return out;
        }
        if (a.length == 3 && (a[0].equalsIgnoreCase("give") || a[0].equalsIgnoreCase("givevirtual"))) {
            List<String> out = new ArrayList<>();
            for (Crate crate : plugin.getCrateManager().getCrates()) {
                if (crate.getId().toLowerCase().startsWith(a[2].toLowerCase())) {
                    out.add(crate.getId());
                }
            }
            return out;
        }
        return List.of();
    }
}

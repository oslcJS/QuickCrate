package com.quickcrates.command;

import com.quickcrates.QuickCrates;
import com.quickcrates.crate.Crate;
import com.quickcrates.key.KeyAllManager;
import com.quickcrates.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class KeyAllCommand implements CommandExecutor, TabCompleter {
    private final QuickCrates plugin;

    public KeyAllCommand(QuickCrates plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
        if (a.length == 0) {
            KeyAllManager mgr = plugin.getKeyAllManager();
            long secs = mgr.getSecondsUntilNext();
            if (secs <= 0) {
                s.sendMessage(Msg.color("&6KeyAll &7- Ready to distribute!"));
            } else {
                long mins = secs / 60;
                secs = secs % 60;
                s.sendMessage(Msg.color("&6KeyAll &7- Next distribution in &e" + mins + "m " + secs + "s"));
            }
            s.sendMessage(Msg.color("&7Usage: &e/keyall <crate> [amount]"));
            return true;
        }

        if (!s.hasPermission("quickcrates.admin")) { Msg.send(s, "no-permission"); return true; }
        Crate crate = plugin.getCrateManager().get(a[0]);
        if (crate == null) { s.sendMessage(Msg.color("&cUnknown crate.")); return true; }
        int amt = 1;
        if (a.length >= 2) try { amt = Integer.parseInt(a[1]); } catch (Exception ignored) {}

        for (Player p : Bukkit.getOnlinePlayers()) {
            plugin.getKeyManager().addVirtual(p, crate.getId(), amt);
        }
        plugin.getStorageManager().saveAll();
        plugin.getKeyAllManager().resetTimer();

        String msg = "&aGave &e" + amt + " &a" + crate.getDisplayName() + " &akey(s) to &e" + Bukkit.getOnlinePlayers().size() + " &aplayers!";
        s.sendMessage(Msg.color(msg));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
        if (a.length == 1) {
            List<String> out = new ArrayList<>();
            for (Crate cc : plugin.getCrateManager().getCrates()) out.add(cc.getId());
            return out;
        }
        return List.of();
    }
}

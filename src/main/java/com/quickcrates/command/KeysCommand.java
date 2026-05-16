package com.quickcrates.command;

import com.quickcrates.QuickCrates;
import com.quickcrates.crate.Crate;
import com.quickcrates.key.KeyManager;
import com.quickcrates.util.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class KeysCommand implements CommandExecutor, TabCompleter {

    private final QuickCrates plugin;

    public KeysCommand(QuickCrates plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
        if (!(s instanceof Player p)) { Msg.send(s, "player-only"); return true; }

        Msg.sendRaw(p, "keys-header");
        boolean any = false;
        KeyManager km = plugin.getKeyManager();

        for (Crate crate : plugin.getCrateManager().getCrates()) {
            int physical = km.countPhysical(p, crate);
            int virtual = km.isVirtualEnabled() ? km.getVirtual(p, crate.getId()) : 0;
            int total = physical + virtual;
            if (total > 0) {
                any = true;
                Msg.sendRaw(p, "keys-entry",
                        "crate", crate.getDisplayName(),
                        "total", String.valueOf(total),
                        "physical", String.valueOf(physical),
                        "virtual", String.valueOf(virtual));
            }
        }

        if (km.isUniversalEnabled()) {
            int uniPhysical = 0;
            for (ItemStack it : p.getInventory().getContents()) {
                if (km.isUniversalKey(it)) uniPhysical += it.getAmount();
            }
            int uniVirtual = km.isVirtualEnabled() ? km.getVirtual(p, KeyManager.UNIVERSAL_ID) : 0;
            if (uniPhysical > 0 || uniVirtual > 0) {
                any = true;
                Msg.sendRaw(p, "keys-entry-universal",
                        "total", String.valueOf(uniPhysical + uniVirtual),
                        "physical", String.valueOf(uniPhysical),
                        "virtual", String.valueOf(uniVirtual));
            }
        }

        if (!any) Msg.sendRaw(p, "keys-none");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
        return List.of();
    }
}

package com.quickcrates.command;
import com.quickcrates.QuickCrates;
import com.quickcrates.crate.Crate;
import com.quickcrates.util.Msg;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class KeyCommand implements CommandExecutor {
    private final QuickCrates plugin;
    public KeyCommand(QuickCrates p){ this.plugin = p; }
    public boolean onCommand(@NotNull CommandSender s,@NotNull Command c,@NotNull String l,@NotNull String[] a){
        if (!s.hasPermission("quickcrates.admin")) { Msg.send(s,"no-permission"); return true; }
        if (a.length < 2) { s.sendMessage(Msg.color("&cUsage: /key <give|givevirtual> <player> <crate> [amount]")); return true; }
        String sub = a[0].toLowerCase();
        if (a.length < 3) { s.sendMessage(Msg.color("&cUsage: /key " + sub + " <player> <crate> [amount]")); return true; }
        Player t = plugin.getServer().getPlayerExact(a[1]);
        if (t == null) { s.sendMessage(Msg.color("&cPlayer not found.")); return true; }
        Crate crate = plugin.getCrateManager().get(a[2]);
        if (crate == null) { s.sendMessage(Msg.color("&cUnknown crate.")); return true; }
        int amt = 1; if (a.length>=4) try{amt=Integer.parseInt(a[3]);}catch(Exception e){}
        if (sub.equals("givevirtual")) {
            plugin.getKeyManager().addVirtual(t, crate.getId(), amt);
            s.sendMessage(Msg.color("&aGave " + amt + " virtual keys."));
        } else {
            ItemStack key = crate.getKeyItem(); key.setAmount(amt);
            t.getInventory().addItem(key);
            s.sendMessage(Msg.color("&aGave " + amt + " physical keys."));
        }
        return true;
    }
}

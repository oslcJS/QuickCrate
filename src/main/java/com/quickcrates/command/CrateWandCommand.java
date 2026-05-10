package com.quickcrates.command;

import com.quickcrates.listener.WandListener;
import com.quickcrates.util.Msg;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CrateWandCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Player only.");
            return true;
        }
        if (!p.hasPermission("quickcrates.admin")) {
            Msg.send(p, "no-permission");
            return true;
        }
        ItemStack wand = WandListener.createWand();
        p.getInventory().addItem(wand);
        p.sendMessage(Msg.color(Msg.prefix() + "&aYou received the &6&lCrate Wand&a! "
                + "&7Right-click a chest/barrel/shulker to link it to a crate."));
        return true;
    }
}

package com.quickcrates.util;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class Msg {
    private static JavaPlugin plugin;
    public static void init(JavaPlugin p) { plugin = p; }

    public static String color(String s) {
        if (s == null) return "";
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static String prefix() {
        return color(plugin.getConfig().getString("messages.prefix", "&6[QuickCrates] &r"));
    }

    public static void send(CommandSender to, String key, String... repl) {
        String msg = plugin.getConfig().getString("messages." + key, key);
        for (int i = 0; i + 1 < repl.length; i += 2) {
            msg = msg.replace("{" + repl[i] + "}", repl[i+1]);
        }
        to.sendMessage(prefix() + color(msg));
    }
}

package com.quickcrates.command;

import com.quickcrates.QuickCrates;
import com.quickcrates.key.KeyManager;
import com.quickcrates.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.*;

public class CustomCommandManager {

    private final QuickCrates plugin;
    private final List<String> registered = new ArrayList<>();

    public CustomCommandManager(QuickCrates plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        unregisterAll();

        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("custom-commands");
        if (sec == null) return;

        CommandMap map = getCommandMap();
        if (map == null) return;

        for (String name : sec.getKeys(false)) {
            ConfigurationSection cmdSec = sec.getConfigurationSection(name);
            if (cmdSec == null) continue;

            String label = name.toLowerCase();
            List<String> aliases = cmdSec.getStringList("aliases");
            String permission = cmdSec.getString("permission", null);
            String description = cmdSec.getString("description", "");
            String action = cmdSec.getString("action", "");

            CustomCmd cmd = new CustomCmd(label, description, action, permission, aliases);
            map.register(plugin.getDescription().getName().toLowerCase(), cmd);
            registered.add(label);
        }
    }

    public void unregisterAll() {
        CommandMap map = getCommandMap();
        if (map == null || registered.isEmpty()) return;

        try {
            Field knownField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Command> known = (Map<String, Command>) knownField.get(map);

            for (String label : registered) {
                Command cmd = map.getCommand(label);
                if (cmd != null) {
                    cmd.unregister(map);
                    known.remove(label);
                    if (cmd.getAliases() != null) {
                        for (String alias : cmd.getAliases()) {
                            known.remove(alias.toLowerCase());
                        }
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to unregister custom commands: " + e.getMessage());
        }
        registered.clear();
    }

    private CommandMap getCommandMap() {
        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            return (CommandMap) field.get(Bukkit.getServer());
        } catch (Exception e) {
            plugin.getLogger().warning("Cannot access CommandMap: " + e.getMessage());
            return null;
        }
    }

    private class CustomCmd extends Command {
        private final String action;

        protected CustomCmd(String name, String description, String action,
                            String permission, List<String> aliases) {
            super(name, description, "/" + name, aliases);
            this.action = action;
            if (permission != null && !permission.isEmpty()) {
                setPermission(permission);
            }
        }

        @Override
        public boolean execute(CommandSender sender, String label, String[] args) {
            if (getPermission() != null && !sender.hasPermission(getPermission())) {
                Msg.send(sender, "no-permission");
                return true;
            }

            if (action == null || action.isBlank()) return true;

            String lower = action.toLowerCase().trim();

            if (lower.equals("keys")) {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage("Players only.");
                    return true;
                }
                p.sendMessage(Msg.color("&6&l--- Your Keys ---"));
                boolean any = false;
                for (com.quickcrates.crate.Crate crate : plugin.getCrateManager().getCrates()) {
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

            if (lower.startsWith("message:")) {
                String msg = action.substring("message:".length()).trim();
                sender.sendMessage(Msg.color(msg));
                return true;
            }

            if (lower.startsWith("broadcast:")) {
                String msg = action.substring("broadcast:".length()).trim()
                        .replace("{player}", sender.getName());
                Bukkit.broadcastMessage(Msg.color(msg));
                return true;
            }

            if (lower.startsWith("console:")) {
                String cmd = action.substring("console:".length()).trim()
                        .replace("{player}", sender.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                return true;
            }

            if (lower.startsWith("player:")) {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage("Players only.");
                    return true;
                }
                String cmd = action.substring("player:".length()).trim()
                        .replace("{player}", sender.getName());
                p.performCommand(cmd);
                return true;
            }

            return true;
        }
    }
}

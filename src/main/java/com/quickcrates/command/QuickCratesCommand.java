package com.quickcrates.command;

import com.quickcrates.QuickCrates;
import com.quickcrates.crate.Crate;
import com.quickcrates.util.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class QuickCratesCommand implements CommandExecutor, TabCompleter {
    private final QuickCrates plugin;
    public QuickCratesCommand(QuickCrates plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
        if (a.length == 0) { help(s); return true; }
        switch (a[0].toLowerCase()) {

            case "reload" -> {
                if (!s.hasPermission("quickcrates.admin")) { Msg.send(s, "no-permission"); return true; }
                plugin.reloadConfig();
                plugin.getItemRegistry().load();
                plugin.getCrateManager().loadAll();
                Msg.send(s, "reload");
            }

            case "list" -> {
                s.sendMessage(Msg.prefix() + Msg.color("&fLoaded crates: &e" + plugin.getCrateManager().getCrates().size()));
                for (Crate cc : plugin.getCrateManager().getCrates())
                    s.sendMessage(Msg.color(" &7- &e" + cc.getId() + " &8(" + cc.getDisplayName() + "&8)"));
            }

            case "removecrate" -> {
                if (!s.hasPermission("quickcrates.admin")) { Msg.send(s, "no-permission"); return true; }
                if (a.length < 2) { s.sendMessage(Msg.color("&cUsage: /qc removecrate <name>")); return true; }
                String name = a[1].toLowerCase().replaceAll("[^a-z0-9_-]", "_");
                Crate crate = plugin.getCrateManager().get(name);
                if (crate == null) {
                    s.sendMessage(Msg.color("&cUnknown crate &e" + name + "&c."));
                    return true;
                }
                File cratesDir = new File(plugin.getDataFolder(), "crates");
                File file = new File(cratesDir, name + ".yml");
                if (file.exists()) {
                    file.delete();
                    s.sendMessage(Msg.color("&cDeleted file &7" + file.getName()));
                }
                plugin.getCrateManager().loadAll();
                s.sendMessage(Msg.color("&cCrate &e" + name + " &cremoved."));
            }

            case "addcrate" -> {
                if (!s.hasPermission("quickcrates.admin")) { Msg.send(s, "no-permission"); return true; }
                if (a.length < 2) { s.sendMessage(Msg.color("&cUsage: /qc addcrate <name>")); return true; }
                String name = a[1].toLowerCase().replaceAll("[^a-z0-9_-]", "_");
                File cratesDir = new File(plugin.getDataFolder(), "crates");
                cratesDir.mkdirs();
                File file = new File(cratesDir, name + ".yml");
                if (file.exists()) {
                    s.sendMessage(Msg.color("&cA crate named &e" + name + " &calready exists."));
                    return true;
                }
                
                org.bukkit.configuration.ConfigurationSection def =
                        plugin.getConfig().getConfigurationSection("crate-defaults");
                String displayName = (def != null ? def.getString("display-name", "&6&l{name} Crate") : "&6&l{name} Crate")
                        .replace("{name}", capitalize(name));
                String keyMat    = def != null ? def.getString("key.material",  "TRIPWIRE_HOOK") : "TRIPWIRE_HOOK";
                String keyName   = (def != null ? def.getString("key.name", "&6&l{name} Key") : "&6&l{name} Key")
                        .replace("{name}", capitalize(name));
                boolean keyGlow  = def == null || def.getBoolean("key.glow", true);
                String animation = def != null ? def.getString("animation", "CSGO") : "CSGO";
                int previewRows  = def != null ? def.getInt("preview-gui.rows", 3) : 3;

                org.bukkit.configuration.file.YamlConfiguration cfg =
                        new org.bukkit.configuration.file.YamlConfiguration();
                cfg.set("id", name);
                cfg.set("display-name", displayName);
                cfg.set("key.material", keyMat);
                cfg.set("key.name", keyName);
                cfg.set("key.lore", List.of("&7Right-click a " + capitalize(name) + " Crate to use."));
                cfg.set("key.glow", keyGlow);
                cfg.set("animation", animation);
                cfg.set("locations", List.of()); 
                cfg.set("preview-gui.rows", previewRows);
                cfg.set("preview-gui.title", "&8" + capitalize(name) + " Crate Rewards");
                
                cfg.set("rewards.example.display.material", "DIAMOND");
                cfg.set("rewards.example.display.name", "&bExample Reward");
                cfg.set("rewards.example.display.lore", List.of("&7Edit me in crates/" + name + ".yml"));
                cfg.set("rewards.example.weight", 100);
                cfg.set("rewards.example.items", List.of(Map.of("material", "DIAMOND", "amount", 1)));
                cfg.set("rewards.example.money", 0.0);
                cfg.set("rewards.example.broadcast", false);
                try {
                    cfg.save(file);
                } catch (Exception ex) {
                    s.sendMessage(Msg.color("&cFailed to create crate file: " + ex.getMessage()));
                    return true;
                }
                plugin.getCrateManager().loadAll();
                s.sendMessage(Msg.color(Msg.prefix() + "&aCrate &e" + name + " &acreated! "
                        + "&7Edit &bcrates/" + name + ".yml &7to configure rewards, "
                        + "then use the &6Crate Wand &7to link blocks."));
                s.sendMessage(Msg.color("&7Give yourself a key: &e/qc givekey <player> " + name));
            }

            case "givekey" -> {
                if (!s.hasPermission("quickcrates.admin")) { Msg.send(s, "no-permission"); return true; }
                if (a.length < 3) { s.sendMessage(Msg.color("&cUsage: /qc givekey <player> <crate> [amount] [virtual]")); return true; }
                Player target = plugin.getServer().getPlayerExact(a[1]);
                if (target == null) { s.sendMessage(Msg.color("&cPlayer not found.")); return true; }
                Crate crate = plugin.getCrateManager().get(a[2]);
                if (crate == null) { s.sendMessage(Msg.color("&cUnknown crate.")); return true; }
                int amt = 1;
                if (a.length >= 4) try { amt = Integer.parseInt(a[3]); } catch (Exception ignored) {}
                boolean virtual = a.length >= 5 && a[4].equalsIgnoreCase("virtual");
                if (virtual) {
                    plugin.getKeyManager().addVirtual(target, crate.getId(), amt);
                    s.sendMessage(Msg.color("&aGave " + amt + " virtual keys."));
                } else {
                    ItemStack key = crate.getKeyItem();
                    key.setAmount(amt);
                    target.getInventory().addItem(key);
                    s.sendMessage(Msg.color("&aGave " + amt + " physical keys."));
                }
            }

            case "preview" -> {
                if (!(s instanceof Player p)) { s.sendMessage("Player only."); return true; }
                if (a.length < 2) { s.sendMessage(Msg.color("&cUsage: /qc preview <crate>")); return true; }
                Crate crate = plugin.getCrateManager().get(a[1]);
                if (crate == null) { s.sendMessage(Msg.color("&cUnknown crate.")); return true; }
                com.quickcrates.gui.PreviewGui.open(p, crate);
            }

            default -> help(s);
        }
        return true;
    }

    private void help(CommandSender s) {
        s.sendMessage(Msg.color("&6&lQuickCrates &7v" + plugin.getDescription().getVersion()));
        s.sendMessage(Msg.color("&e/qc list &7- list loaded crates"));
        s.sendMessage(Msg.color("&e/qc reload &7- reload config & crates"));
        s.sendMessage(Msg.color("&e/qc addcrate <name> &7- create a new crate"));
        s.sendMessage(Msg.color("&e/qc removecrate <name> &7- delete a crate"));
        s.sendMessage(Msg.color("&e/qc preview <crate> &7- open preview GUI"));
        s.sendMessage(Msg.color("&e/qc givekey <player> <crate> [amount] [virtual] &7- give keys"));
        s.sendMessage(Msg.color("&7Use &6/cratewand &7to link blocks to crates in-world."));
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
        if (a.length == 1) return List.of("list", "reload", "addcrate", "removecrate", "preview", "givekey");
        if (a.length == 2 && (a[0].equalsIgnoreCase("preview") || a[0].equalsIgnoreCase("removecrate"))) {
            List<String> out = new ArrayList<>();
            for (Crate cc : plugin.getCrateManager().getCrates()) out.add(cc.getId());
            return out;
        }
        if (a.length == 3 && a[0].equalsIgnoreCase("givekey")) {
            List<String> out = new ArrayList<>();
            for (Crate cc : plugin.getCrateManager().getCrates()) out.add(cc.getId());
            return out;
        }
        return Collections.emptyList();
    }
}

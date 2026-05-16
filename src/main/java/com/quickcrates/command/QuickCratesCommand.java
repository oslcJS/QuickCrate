package com.quickcrates.command;

import com.quickcrates.QuickCrates;
import com.quickcrates.crate.Crate;
import com.quickcrates.gui.PreviewGui;
import com.quickcrates.key.KeyAllManager;
import com.quickcrates.listener.WandListener;
import com.quickcrates.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QuickCratesCommand implements CommandExecutor, TabCompleter {

    private final QuickCrates plugin;
    private final Map<String, SubCommand> subs = new LinkedHashMap<>();

    public QuickCratesCommand(QuickCrates plugin) {
        this.plugin = plugin;
        subs.put("help",    new Sub("",                            this::help));
        subs.put("reload",  new Sub("quickcrates.reload",          this::reload));
        subs.put("list",    new Sub("quickcrates.crate.edit",      this::list));
        subs.put("create",  new Sub("quickcrates.crate.edit",      this::create));
        subs.put("remove",  new Sub("quickcrates.crate.edit",      this::remove));
        subs.put("preview", new Sub("quickcrates.preview",         this::preview));
        subs.put("wand",    new Sub("quickcrates.wand",            this::wand));
        subs.put("key",     new Sub("quickcrates.key.give",        this::key));
        subs.put("keyall",  new Sub("",                            this::keyall));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
        if (a.length == 0) { help(s, a); return true; }
        SubCommand sub = subs.get(a[0].toLowerCase());
        if (sub == null) { Msg.send(s, "unknown-subcommand", "sub", a[0]); return true; }
        if (!hasPerm(s, sub.permission())) { Msg.send(s, "no-permission"); return true; }
        sub.run(s, a);
        return true;
    }

    // -------- subcommands --------

    private void help(CommandSender s, String[] a) {
        Msg.sendRaw(s, "help", "version", plugin.getDescription().getVersion());
    }

    private void reload(CommandSender s, String[] a) {
        plugin.reloadConfig();
        plugin.getItemRegistry().load();
        plugin.getCrateManager().loadAll();
        plugin.getCustomCommandManager().loadAll();
        plugin.getKeyAllManager().stop();
        plugin.getKeyAllManager().start();
        Msg.send(s, "reload");
    }

    private void list(CommandSender s, String[] a) {
        var crates = plugin.getCrateManager().getCrates();
        Msg.send(s, "list-header", "count", String.valueOf(crates.size()));
        for (Crate cc : crates) {
            Msg.sendRaw(s, "list-entry", "id", cc.getId(), "name", cc.getDisplayName());
        }
    }

    private void create(CommandSender s, String[] a) {
        if (a.length < 2) { Msg.send(s, "usage-create"); return; }
        String name = a[1].toLowerCase().replaceAll("[^a-z0-9_-]", "_");

        File cratesDir = new File(plugin.getDataFolder(), "crates");
        cratesDir.mkdirs();
        File file = new File(cratesDir, name + ".yml");
        if (file.exists()) { Msg.send(s, "crate-exists", "name", name); return; }

        ConfigurationSection def = plugin.getConfig().getConfigurationSection("crate-defaults");
        String pretty       = capitalize(name);
        String displayName  = (def != null ? def.getString("display-name", "&6&l{name} Crate") : "&6&l{name} Crate").replace("{name}", pretty);
        String keyMat       = def != null ? def.getString("key.material", "TRIPWIRE_HOOK") : "TRIPWIRE_HOOK";
        String keyName      = (def != null ? def.getString("key.name", "&6&l{name} Key") : "&6&l{name} Key").replace("{name}", pretty);
        boolean keyGlow     = def == null || def.getBoolean("key.glow", true);
        String animation    = def != null ? def.getString("animation", "CSGO") : "CSGO";
        int previewRows     = def != null ? def.getInt("preview-gui.rows", 3) : 3;

        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("id", name);
        cfg.set("display-name", displayName);
        cfg.set("key.material", keyMat);
        cfg.set("key.name", keyName);
        cfg.set("key.lore", List.of("&7Right-click a " + pretty + " Crate to use."));
        cfg.set("key.glow", keyGlow);
        cfg.set("animation", animation);
        cfg.set("locations", List.of());
        cfg.set("preview-gui.rows", previewRows);
        cfg.set("preview-gui.title", "&8" + pretty + " Crate Rewards");
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
            Msg.send(s, "crate-create-failed", "error", ex.getMessage());
            return;
        }
        plugin.getCrateManager().loadAll();
        Msg.send(s, "crate-created", "name", name);
    }

    private void remove(CommandSender s, String[] a) {
        if (a.length < 2) { Msg.send(s, "usage-remove"); return; }
        String name = a[1].toLowerCase().replaceAll("[^a-z0-9_-]", "_");
        Crate crate = plugin.getCrateManager().get(name);
        if (crate == null) { Msg.send(s, "crate-unknown", "name", name); return; }

        File file = new File(new File(plugin.getDataFolder(), "crates"), name + ".yml");
        if (file.exists() && !file.delete()) {
            Msg.send(s, "crate-remove-failed", "name", name);
            return;
        }
        plugin.getCrateManager().loadAll();
        Msg.send(s, "crate-removed", "name", name);
    }

    private void preview(CommandSender s, String[] a) {
        if (!(s instanceof Player p)) { Msg.send(s, "player-only"); return; }
        if (a.length < 2) { Msg.send(s, "usage-preview"); return; }
        Crate crate = plugin.getCrateManager().get(a[1]);
        if (crate == null) { Msg.send(s, "crate-unknown", "name", a[1]); return; }
        PreviewGui.open(p, crate);
    }

    private void wand(CommandSender s, String[] a) {
        if (!(s instanceof Player p)) { Msg.send(s, "player-only"); return; }
        p.getInventory().addItem(WandListener.createWand());
        Msg.send(p, "wand-given");
    }

    private void key(CommandSender s, String[] a) {
        if (a.length < 2 || !(a[1].equalsIgnoreCase("give") || a[1].equalsIgnoreCase("givevirtual"))) {
            Msg.send(s, "usage-key");
            return;
        }
        boolean virtual = a[1].equalsIgnoreCase("givevirtual");
        if (a.length < 4) { Msg.send(s, "usage-key"); return; }

        Player target = plugin.getServer().getPlayerExact(a[2]);
        if (target == null) { Msg.send(s, "player-not-found", "name", a[2]); return; }
        Crate crate = plugin.getCrateManager().get(a[3]);
        if (crate == null) { Msg.send(s, "crate-unknown", "name", a[3]); return; }

        int amt = 1;
        if (a.length >= 5) try { amt = Math.max(1, Integer.parseInt(a[4])); } catch (NumberFormatException ignored) {}

        if (virtual) {
            plugin.getKeyManager().addVirtual(target, crate.getId(), amt);
            plugin.getStorageManager().saveAll();
            Msg.send(s, "key-given-virtual", "amount", String.valueOf(amt), "player", target.getName(), "crate", crate.getDisplayName());
        } else {
            ItemStack item = crate.getKeyItem();
            item.setAmount(amt);
            target.getInventory().addItem(item);
            Msg.send(s, "key-given-physical", "amount", String.valueOf(amt), "player", target.getName(), "crate", crate.getDisplayName());
        }
    }

    private void keyall(CommandSender s, String[] a) {
        KeyAllManager mgr = plugin.getKeyAllManager();

        if (a.length < 2) {
            long secs = mgr.getSecondsUntilNext();
            if (secs <= 0) {
                Msg.send(s, "keyall-ready");
            } else {
                Msg.send(s, "keyall-countdown",
                        "minutes", String.valueOf(secs / 60),
                        "seconds", String.valueOf(secs % 60));
            }
            return;
        }

        if (!hasPerm(s, "quickcrates.keyall")) { Msg.send(s, "no-permission"); return; }

        Crate crate = plugin.getCrateManager().get(a[1]);
        if (crate == null) { Msg.send(s, "crate-unknown", "name", a[1]); return; }
        int amt = 1;
        if (a.length >= 3) try { amt = Math.max(1, Integer.parseInt(a[2])); } catch (NumberFormatException ignored) {}

        for (Player p : Bukkit.getOnlinePlayers()) {
            plugin.getKeyManager().addVirtual(p, crate.getId(), amt);
        }
        plugin.getStorageManager().saveAll();
        mgr.resetTimer();

        Msg.send(s, "keyall-distributed",
                "amount", String.valueOf(amt),
                "crate", crate.getDisplayName(),
                "count", String.valueOf(Bukkit.getOnlinePlayers().size()));
    }

    // -------- tab completion --------

    @Override
    public List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
        if (a.length == 1) {
            return filter(subs.entrySet().stream()
                    .filter(e -> hasPerm(s, e.getValue().permission()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList()), a[0]);
        }
        String sub = a[0].toLowerCase();
        switch (sub) {
            case "preview", "remove" -> {
                if (a.length == 2) return filter(crateIds(), a[1]);
            }
            case "key" -> {
                if (a.length == 2) return filter(List.of("give", "givevirtual"), a[1]);
                if (a.length == 3) return filter(onlinePlayers(), a[2]);
                if (a.length == 4) return filter(crateIds(), a[3]);
            }
            case "keyall" -> {
                if (a.length == 2) return filter(crateIds(), a[1]);
            }
        }
        return Collections.emptyList();
    }

    // -------- helpers --------

    private boolean hasPerm(CommandSender s, String node) {
        if (node == null || node.isEmpty()) return true;
        return s.hasPermission(node) || s.hasPermission("quickcrates.admin");
    }

    private List<String> crateIds() {
        List<String> out = new ArrayList<>();
        for (Crate cc : plugin.getCrateManager().getCrates()) out.add(cc.getId());
        return out;
    }

    private List<String> onlinePlayers() {
        List<String> out = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) out.add(p.getName());
        return out;
    }

    private static List<String> filter(List<String> options, String prefix) {
        String p = prefix.toLowerCase();
        return options.stream()
                .filter(o -> o.toLowerCase().startsWith(p))
                .limit(50)
                .collect(Collectors.toList());
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // -------- router types --------

    @FunctionalInterface
    private interface SubHandler { void run(CommandSender s, String[] a); }

    private interface SubCommand {
        String permission();
        void run(CommandSender s, String[] a);
    }

    private record Sub(String permission, SubHandler handler) implements SubCommand {
        @Override public void run(CommandSender s, String[] a) { handler.run(s, a); }
    }
}

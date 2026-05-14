package com.quickcrates.compat;
import com.quickcrates.QuickCrates;
import com.quickcrates.crate.Crate;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PapiHook extends PlaceholderExpansion {
    private final QuickCrates plugin;
    public PapiHook(QuickCrates p){ this.plugin = p; }
    @Override public @NotNull String getIdentifier(){ return "quickcrates"; }
    @Override public @NotNull String getAuthor(){ return "QuickCrates"; }
    @Override public @NotNull String getVersion(){ return plugin.getDescription().getVersion(); }
    @Override public boolean persist(){ return true; }
    @Override public String onRequest(OfflinePlayer p, @NotNull String params) {
        if (p == null) return "";

        if (params.startsWith("keys_physical_")) {
            String id = params.substring("keys_physical_".length());
            Crate c = plugin.getCrateManager().get(id);
            if (c == null || !(p instanceof Player pl)) return "0";
            return String.valueOf(plugin.getKeyManager().countPhysical(pl, c));
        }

        if (params.startsWith("keys_virtual_")) {
            String id = params.substring("keys_virtual_".length());
            Crate c = plugin.getCrateManager().get(id);
            if (c == null) return "0";
            if (!plugin.getKeyManager().isVirtualEnabled()) return "0";
            return String.valueOf(plugin.getKeyManager().getVirtual(p.getPlayer(), id));
        }

        if (params.startsWith("keys_")) {
            String id = params.substring("keys_".length());
            Crate c = plugin.getCrateManager().get(id);
            if (c == null) return "0";
            if (!(p instanceof Player pl)) return "0";
            return String.valueOf(plugin.getKeyManager().countAll(pl, c));
        }

        if (params.equalsIgnoreCase("keyall_time")) {
            long secs = plugin.getKeyAllManager().getSecondsUntilNext();
            if (secs <= 0) return "Ready!";
            return (secs / 60) + "m " + (secs % 60) + "s";
        }

        if (params.equalsIgnoreCase("keyall_seconds")) {
            return String.valueOf(plugin.getKeyAllManager().getSecondsUntilNext());
        }

        if (params.equalsIgnoreCase("crate_count"))
            return String.valueOf(plugin.getCrateManager().getCrates().size());

        return null;
    }
}

package com.quickcrates.compat;
import com.quickcrates.QuickCrates;
import com.quickcrates.crate.Crate;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
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
        if (params.startsWith("keys_")) {
            String id = params.substring("keys_".length());
            Crate c = plugin.getCrateManager().get(id);
            if (c == null) return "0";
            int v = plugin.getKeyManager().getVirtual(p.getPlayer(), id);
            return String.valueOf(v);
        }
        if (params.equalsIgnoreCase("crate_count"))
            return String.valueOf(plugin.getCrateManager().getCrates().size());
        return null;
    }
}

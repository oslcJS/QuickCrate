package com.quickcrates.compat;

import com.quickcrates.QuickCrates;
import com.quickcrates.crate.Crate;
import com.quickcrates.key.KeyManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicesManager;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

public class QuickApiBridge {

    private final QuickCrates plugin;
    private Object providerInstance;
    private boolean hooked;

    public QuickApiBridge(QuickCrates plugin) {
        this.plugin = plugin;
    }

    public boolean hook() {
        if (!Bukkit.getPluginManager().isPluginEnabled("QuickApi")) return false;

        try {
            ServicesManager sm = Bukkit.getServicesManager();
            Class<?> apiClass = Class.forName("com.quickapi.QuickApi");
            Object api = sm.load(apiClass);
            if (api == null) return false;

            Class<?> providerClass = Class.forName("com.quickapi.DataProvider");
            Method register = apiClass.getMethod("registerProvider", String.class, providerClass);

            providerInstance = java.lang.reflect.Proxy.newProxyInstance(
                    providerClass.getClassLoader(),
                    new Class[]{providerClass},
                    (proxy, method, args) -> {
                        String name = method.getName();
                        if (name.equals("getString")) {
                            String key = (String) args[0];
                            Player p = (Player) args[1];
                            return getValue(key, p);
                        }
                        if (name.equals("getInt")) {
                            String key = (String) args[0];
                            Player p = (Player) args[1];
                            String val = getValue(key, p);
                            try { return Integer.parseInt(val); } catch (Exception e) { return 0; }
                        }
                        if (name.equals("getLong")) {
                            String key = (String) args[0];
                            Player p = (Player) args[1];
                            String val = getValue(key, p);
                            try { return Long.parseLong(val); } catch (Exception e) { return 0L; }
                        }
                        if (name.equals("keys")) {
                            return List.of(
                                "keys_total_vote", "keys_physical_vote", "keys_virtual_vote",
                                "keys_total_rare", "keys_physical_rare", "keys_virtual_rare",
                                "keyall_time", "keyall_seconds", "keyall_enabled",
                                "crate_count"
                            );
                        }
                        return null;
                    });

            register.invoke(api, "quickcrates", providerInstance);
            this.hooked = true;
            plugin.getLogger().info("Hooked into QuickApi.");
            return true;

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to hook QuickApi: " + e.getMessage());
            return false;
        }
    }

    public void unhook() {
        if (!hooked) return;
        try {
            ServicesManager sm = Bukkit.getServicesManager();
            Class<?> apiClass = Class.forName("com.quickapi.QuickApi");
            Object api = sm.load(apiClass);
            if (api != null) {
                Method unregister = apiClass.getMethod("unregisterProvider", String.class);
                unregister.invoke(api, "quickcrates");
            }
        } catch (Exception ignored) {}
    }

    private String getValue(String key, Player player) {
        if (key == null) return "";

        if (key.startsWith("keys_total_")) {
            String id = key.substring("keys_total_".length());
            Crate c = plugin.getCrateManager().get(id);
            if (c == null || player == null) return "0";
            return String.valueOf(plugin.getKeyManager().countAll(player, c));
        }

        if (key.startsWith("keys_physical_")) {
            String id = key.substring("keys_physical_".length());
            Crate c = plugin.getCrateManager().get(id);
            if (c == null || player == null) return "0";
            return String.valueOf(plugin.getKeyManager().countPhysical(player, c));
        }

        if (key.startsWith("keys_virtual_")) {
            String id = key.substring("keys_virtual_".length());
            Crate c = plugin.getCrateManager().get(id);
            if (c == null) return "0";
            if (!plugin.getKeyManager().isVirtualEnabled()) return "0";
            return String.valueOf(plugin.getKeyManager().getVirtual(player, id));
        }

        if (key.equals("keyall_time")) {
            long secs = plugin.getKeyAllManager().getSecondsUntilNext();
            if (secs <= 0) return "Ready!";
            return (secs / 60) + "m " + (secs % 60) + "s";
        }

        if (key.equals("keyall_seconds")) {
            return String.valueOf(plugin.getKeyAllManager().getSecondsUntilNext());
        }

        if (key.equals("keyall_enabled")) {
            return String.valueOf(plugin.getKeyAllManager().isEnabled());
        }

        if (key.equals("crate_count")) {
            return String.valueOf(plugin.getCrateManager().getCrates().size());
        }

        return "";
    }
}

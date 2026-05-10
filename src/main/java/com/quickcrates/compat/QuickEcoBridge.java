package com.quickcrates.compat;

import com.quickcrates.QuickCrates;
import com.quickcrates.quicklink.QuickLink;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public final class QuickEcoBridge {

    private static Boolean linked = null;
    private static Object economyProvider;
    private static Method depositMethod;
    private static Method formatMethod;

    private QuickEcoBridge() {}

    public static boolean isLinked() {
        if (linked != null) return linked;
        linked = QuickLink.isLinked("QuickEco") && resolve();
        return linked;
    }

    public static boolean deposit(Player player, double amount) {
        if (!isLinked()) {
            QuickCrates.get().getLogger().warning("QuickEco not linked — cannot deposit " + amount);
            return false;
        }
        try {
            depositMethod.invoke(economyProvider, (OfflinePlayer) player, amount);
            return true;
        } catch (Exception e) {
            QuickCrates.get().getLogger().warning("QuickEco deposit failed: " + e.getMessage());
            return false;
        }
    }

    public static String format(double amount) {
        if (!isLinked()) return String.format("%.2f", amount);
        try {
            Object result = formatMethod.invoke(economyProvider, amount);
            return result != null ? (String) result : String.format("%.2f", amount);
        } catch (Exception e) {
            return String.format("%.2f", amount);
        }
    }

    private static boolean resolve() {
        try {
            Class<?> apiClass = Class.forName("com.quickeco.api.QuickEcoAPI");
            Object api = Bukkit.getServicesManager().load(apiClass);
            if (api == null) {
                QuickCrates.get().getLogger().warning("QuickEco API service not registered.");
                return false;
            }

            Object provider = apiClass.getMethod("getEconomyProvider").invoke(api);
            if (provider == null) return false;

            depositMethod = provider.getClass().getMethod("depositPlayer", OfflinePlayer.class, double.class);
            formatMethod = provider.getClass().getMethod("format", double.class);

            economyProvider = provider;
            QuickCrates.get().getLogger().info("QuickEco bridge established via ServicesManager.");
            return true;
        } catch (Exception e) {
            QuickCrates.get().getLogger().warning("QuickEco bridge failed: " + e.getMessage());
            return false;
        }
    }
}

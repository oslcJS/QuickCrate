package com.quickcrates.key;

import com.quickcrates.QuickCrates;
import com.quickcrates.crate.Crate;
import com.quickcrates.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class KeyAllManager {

    private final QuickCrates plugin;
    private BukkitTask task;
    private long nextRunMillis;

    public KeyAllManager(QuickCrates plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("keyall");
        if (sec == null || !sec.getBoolean("enabled", false)) return;

        int interval = sec.getInt("interval-minutes", 60);
        long intervalMillis = interval * 60L * 1000L;
        this.nextRunMillis = System.currentTimeMillis() + intervalMillis;

        this.task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            ConfigurationSection s = plugin.getConfig().getConfigurationSection("keyall");
            if (s == null) return;
            String crateId = s.getString("crate", "");
            int amount = s.getInt("amount", 1);
            Crate crate = plugin.getCrateManager().get(crateId);
            if (crate == null) return;

            for (Player p : Bukkit.getOnlinePlayers()) {
                plugin.getKeyManager().addVirtual(p, crate.getId(), amount);
            }
            plugin.getStorageManager().saveAll();

            String msg = s.getString("broadcast", "&6KeyAll &7- Everyone received &e{amount} &7{keys}!");
            msg = msg.replace("{amount}", String.valueOf(amount))
                     .replace("{keys}", crate.getDisplayName() + " Key(s)");
            Bukkit.broadcastMessage(Msg.color(msg));

            this.nextRunMillis = System.currentTimeMillis() + intervalMillis;
        }, intervalMillis / 50, intervalMillis / 50);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void resetTimer() {
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("keyall");
        int interval = sec != null ? sec.getInt("interval-minutes", 60) : 60;
        this.nextRunMillis = System.currentTimeMillis() + interval * 60L * 1000L;
    }

    public long getMillisUntilNext() {
        return Math.max(0, nextRunMillis - System.currentTimeMillis());
    }

    public long getSecondsUntilNext() {
        return getMillisUntilNext() / 1000;
    }

    public boolean isEnabled() {
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("keyall");
        return sec != null && sec.getBoolean("enabled", false);
    }
}

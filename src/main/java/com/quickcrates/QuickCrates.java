package com.quickcrates;

import com.quickcrates.api.QuickCratesAPI;
import com.quickcrates.command.CrateCommand;
import com.quickcrates.command.CrateWandCommand;
import com.quickcrates.command.CustomCommandManager;
import com.quickcrates.command.KeyCommand;
import com.quickcrates.command.QuickCratesCommand;
import com.quickcrates.compat.PapiHook;
import com.quickcrates.crate.CrateManager;
import com.quickcrates.key.KeyManager;
import com.quickcrates.listener.CrateDisplayManager;
import com.quickcrates.listener.CrateInteractListener;
import com.quickcrates.listener.GuiClickListener;
import com.quickcrates.listener.WandListener;
import com.quickcrates.quicklink.QuickLink;
import com.quickcrates.storage.StorageManager;
import com.quickcrates.util.ItemRegistry;
import com.quickcrates.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class QuickCrates extends JavaPlugin implements QuickCratesAPI {

    private static QuickCrates instance;

    private CrateManager crateManager;
    private KeyManager keyManager;
    private StorageManager storageManager;
    private ItemRegistry itemRegistry;
    private GuiClickListener guiClickListener;
    private CustomCommandManager customCommandManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        saveResource("crates/vote.yml", false);

        Msg.init(this);
        this.itemRegistry = new ItemRegistry(this);
        itemRegistry.load();
        this.keyManager = new KeyManager(this);
        this.storageManager = new StorageManager(this);
        this.crateManager = new CrateManager(this);

        crateManager.loadAll();

        this.guiClickListener = new GuiClickListener(this);
        Bukkit.getPluginManager().registerEvents(new CrateInteractListener(this, guiClickListener), this);
        Bukkit.getPluginManager().registerEvents(guiClickListener, this);
        Bukkit.getPluginManager().registerEvents(new WandListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CrateDisplayManager(this), this);

        registerCommand("quickcrates", new QuickCratesCommand(this));
        registerCommand("crate", new CrateCommand(this));
        registerCommand("key", new KeyCommand(this));
        registerCommand("cratewand", new CrateWandCommand());

        this.customCommandManager = new CustomCommandManager(this);
        customCommandManager.loadAll();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PapiHook(this).register();
            getLogger().info("Hooked into PlaceholderAPI.");
        }

        Bukkit.getServicesManager().register(QuickCratesAPI.class, this, this, org.bukkit.plugin.ServicePriority.Normal);
        QuickLink.register(this);

        if (QuickLink.isLinked("QuickEco")) {
            getLogger().info("QuickEco linked via QuickLink — crate commands can reward money.");
        }

        
        Bukkit.getScheduler().runTaskLater(this, () -> CrateDisplayManager.respawnAll(this), 1L);

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (storageManager != null) storageManager.saveAll();
        }, 600L, 600L);

        getLogger().info("QuickCrates enabled. Loaded " + crateManager.getCrates().size() + " crates. " +
                "Server: " + Bukkit.getBukkitVersion());
    }

    @Override
    public void onDisable() {
        if (storageManager != null) storageManager.saveAll();
        if (crateManager != null) crateManager.shutdown();
        CrateDisplayManager.removeAll();
        QuickLink.unregister();
    }

    private void registerCommand(String name, org.bukkit.command.CommandExecutor executor) {
        PluginCommand cmd = getCommand(name);
        if (cmd != null) {
            cmd.setExecutor(executor);
            if (executor instanceof org.bukkit.command.TabCompleter tc) cmd.setTabCompleter(tc);
        }
    }

    public static QuickCrates get() { return instance; }
    @Override public CrateManager getCrateManager() { return crateManager; }
    @Override public KeyManager getKeyManager() { return keyManager; }
    public StorageManager getStorageManager() { return storageManager; }
    public ItemRegistry getItemRegistry() { return itemRegistry; }
    public CustomCommandManager getCustomCommandManager() { return customCommandManager; }
}

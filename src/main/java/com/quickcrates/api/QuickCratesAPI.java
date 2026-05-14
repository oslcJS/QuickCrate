package com.quickcrates.api;

import com.quickcrates.crate.CrateManager;
import com.quickcrates.key.KeyManager;
import com.quickcrates.key.KeyAllManager;
import org.bukkit.entity.Player;

public interface QuickCratesAPI {
    CrateManager getCrateManager();
    KeyManager getKeyManager();
    KeyAllManager getKeyAllManager();

    int getPlayerKeyCount(Player player, String crateId);
    int getTotalCrateCount();
    boolean isKeyAllActive();
    String getPluginVersion();
}

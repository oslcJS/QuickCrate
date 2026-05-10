package com.quickcrates.api;

import com.quickcrates.crate.CrateManager;
import com.quickcrates.key.KeyManager;

public interface QuickCratesAPI {
    CrateManager getCrateManager();
    KeyManager getKeyManager();
}

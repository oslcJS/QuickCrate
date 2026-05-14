# QuickApi — Central Bridge Plugin

QuickApi is a standalone plugin that acts as the central data bridge between all QuickPlugins (QuickCrates, QuickSidebar, QuickEco, etc.).

Rather than each plugin talking to every other plugin directly, all data flows through QuickApi. This keeps dependencies clean and allows each plugin to be optional.

---

## Architecture

```
QuickCrates ──→ registers data (keys, crates, timers)
QuickEco   ──→ registers data (balances, transactions)
                ↓
          QuickApi ←── Bukkit ServicesManager
                ↓
QuickSidebar ──→ reads data (displays in sidebar)
CustomPlugin ──→ reads data (uses in features)
```

- Both QuickCrates and QuickSidebar **soft-depend** on QuickApi
- QuickApi provides a single Bukkit Service interface
- Data is pushed from source plugins, polled by consumer plugins
- QuickApi itself has **no config**, it's purely a bridge

---

## QuickApi Interface (what QuickApi must expose)

```java
package com.quickapi;

import org.bukkit.entity.Player;
import java.util.Collection;
import java.util.Map;

public interface QuickApi {

    // ── Registration ──
    void registerProvider(String namespace, DataProvider provider);
    void unregisterProvider(String namespace);

    // ── Data Querying ──
    String getString(String namespace, String key, Player player);
    int getInt(String namespace, String key, Player player);
    long getLong(String namespace, String key, Player player);
    boolean has(String namespace, String key, Player player);

    // ── Metadata ──
    Collection<String> getNamespaces();
    Collection<String> getKeys(String namespace);
}
```

### DataProvider (implemented by source plugins)

```java
package com.quickapi;

import org.bukkit.entity.Player;
import java.util.Collection;

public interface DataProvider {
    String getString(String key, Player player);
    int getInt(String key, Player player);
    long getLong(String key, Player player);
    Collection<String> keys();
}
```

---

## What QuickCrates Registers

Namespace: `quickcrates`

| Key | Type | Returns | Example |
|---|---|---|---|
| `keys_total_<crate>` | int | Physical + virtual keys | `keys_total_vote` → `3` |
| `keys_physical_<crate>` | int | Physical keys in inventory | `keys_physical_vote` → `1` |
| `keys_virtual_<crate>` | int | Virtual keys | `keys_virtual_vote` → `2` |
| `keyall_time` | string | Formatted time | `15m 30s` |
| `keyall_seconds` | long | Raw seconds | `930` |
| `keyall_enabled` | boolean | Is auto KeyAll on | `true` |
| `crate_count` | int | Number of crates | `2` |

---

## QuickCrates → QuickApi Bridge Class

QuickCrates would implement this in its own codebase (no changes needed in QuickApi):

```java
// In QuickCrates (uses QuickApi if present)
public class QuickCratesDataProvider implements DataProvider {
    private final QuickCrates plugin;

    @Override
    public String getString(String key, Player player) { /* map key to data */ }
    @Override
    public int getInt(String key, Player player) { /* ... */ }
    @Override
    public long getLong(String key, Player player) { /* ... */ }
    @Override
    public Collection<String> keys() { return List.of("keys_total_vote", "keys_physical_vote", ...); }
}
```

Registration in QuickCrates' `onEnable()`:

```java
if (Bukkit.getPluginManager().isPluginEnabled("QuickApi")) {
    QuickApi api = Bukkit.getServicesManager().load(QuickApi.class);
    if (api != null) {
        api.registerProvider("quickcrates", new QuickCratesDataProvider(this));
    }
}
```

---

## How QuickSidebar Uses QuickApi

QuickSidebar never touches QuickCrates directly. It only talks to QuickApi:

```java
QuickApi api = Bukkit.getServicesManager().load(QuickApi.class);
if (api != null) {
    int voteKeys = api.getInt("quickcrates", "keys_total_vote", player);
    String timeLeft = api.getString("quickcrates", "keyall_time", player);
    int crateCount = api.getInt("quickcrates", "crate_count", player);
}
```

QuickSidebar's config would reference QuickApi namespaced keys:

```yaml
sidebar:
  title: "&6&lQuickCrates"
  lines:
    - "&7&m-----------------"
    - "&fYour Keys:"
    - " &e{{ quickcrates.keys_total_vote }} &7Vote Keys"
    - " &e{{ quickcrates.keys_total_rare }} &7Rare Keys"
    - ""
    - "&fNext KeyAll:"
    - " &e{{ quickcrates.keyall_time }}"
    - "&7&m-----------------"
  update-interval: 20
```

---

## Plugin Dependencies

| Plugin | Depends on | Type |
|---|---|---|
| QuickApi | nothing | standalone |
| QuickCrates | QuickApi | softdepend |
| QuickSidebar | QuickApi | softdepend |

All three are loaded at server startup. If QuickApi is missing, QuickCrates and QuickSidebar still work but without cross-plugin data sharing.

---

## Discovery via QuickLink

QuickApi uses QuickLink for plugin discovery. Each plugin writes a `.link` file:

**QuickCrates.link:**
```yaml
name: QuickCrates
version: 1.0.0
api-namespace: quickcrates
```

**QuickSidebar.link:**
```yaml
name: QuickSidebar
version: 1.0.0
```

QuickApi reads these to know which plugins are present and what namespaces they provide.

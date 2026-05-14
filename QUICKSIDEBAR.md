# QuickSidebar Integration

QuickSidebar displays a sidebar (scoreboard) on the right side of the screen.
The sidebar shows real-time data from QuickCrates via **QuickApi**.

**No PlaceholderAPI dependency.** All data flows through QuickApi.

---

## Data Flow

```
QuickCrates ──registers──→ QuickApi ←──reads── QuickSidebar
```

- QuickCrates registers a `DataProvider` with namespace `quickcrates`
- QuickSidebar queries QuickApi for keys/timers
- QuickApi handles all cross-plugin communication
- Both plugins soft-depend on QuickApi

---

## Available Data (namespace: `quickcrates`)

| Key | Type | Description |
|---|---|---|
| `keys_total_<crate>` | int | Physical + virtual keys for a crate |
| `keys_physical_<crate>` | int | Physical keys only |
| `keys_virtual_<crate>` | int | Virtual keys only |
| `keyall_time` | string | Time until next auto KeyAll (`15m 30s`) |
| `keyall_seconds` | long | Seconds until next KeyAll |
| `keyall_enabled` | boolean | Is auto KeyAll enabled |
| `crate_count` | int | Number of loaded crates |

---

## QuickSidebar Config (YAML)

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

Syntax: `{{ namespace.key }}` → resolved by QuickSidebar via QuickApi API.

---

## Java API (for QuickSidebar's internal use)

```java
// Get QuickApi instance
QuickApi api = Bukkit.getServicesManager().load(QuickApi.class);
if (api == null) return; // QuickApi not installed

// Read data for a player
int voteKeys = api.getInt("quickcrates", "keys_total_vote", player);
String timeLeft = api.getString("quickcrates", "keyall_time", player);
int crateCount = api.getInt("quickcrates", "crate_count", player);
```

---

## QuickApi Interface Reference

```java
public interface QuickApi {
    void registerProvider(String namespace, DataProvider provider);
    void unregisterProvider(String namespace);
    String getString(String namespace, String key, Player player);
    int getInt(String namespace, String key, Player player);
    long getLong(String namespace, String key, Player player);
    boolean has(String namespace, String key, Player player);
    Collection<String> getNamespaces();
    Collection<String> getKeys(String namespace);
}
```

---

## KeyAll Timer Details

- `keyall_seconds` returns the raw seconds remaining (0 if ready)
- Timer resets on manual `/keyall` or after each auto distribution
- Configured in QuickCrates' `config.yml` under `keyall:`

---

## Plugin Dependencies

```
QuickApi         → standalone (required for bridge)
QuickCrates      → softdepends on QuickApi
QuickSidebar     → softdepends on QuickApi
```

If QuickApi is missing, QuickCrates and QuickSidebar still operate independently but without data sharing.

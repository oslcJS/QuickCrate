# QuickSidebar Integration

QuickSidebar displays a sidebar (scoreboard) on the right side of the screen.
The sidebar should show real-time data from QuickCrates: **key counts**, **KeyAll time remaining**, and **crate info**.

---

## PlaceholderAPI Placeholders (already registered by QuickCrates)

| Placeholder | Description | Example |
|---|---|---|
| `%quickcrates_keys_<crate>%` | Total keys (physical + virtual) for a crate | `%quickcrates_keys_vote%` → `3` |
| `%quickcrates_keys_physical_<crate>%` | Physical keys only | `%quickcrates_keys_physical_vote%` → `1` |
| `%quickcrates_keys_virtual_<crate>%` | Virtual keys only | `%quickcrates_keys_virtual_vote%` → `2` |
| `%quickcrates_keyall_time%` | Time until next auto KeyAll distribution | `15m 30s` |
| `%quickcrates_keyall_seconds%` | Seconds until next KeyAll | `930` |

These are provided by the existing `PapiHook.java` class. QuickSidebar just needs to use them in its sidebar config.

---

## Internal API (for direct integration without PlaceholderAPI)

QuickSidebar can access `QuickCratesAPI` via Bukkit Services:

```java
QuickCratesAPI api = Bukkit.getServicesManager().load(QuickCratesAPI.class);
if (api != null) {
    // Get key counts
    int total = api.getKeyManager().countAll(player, crate);
    int physical = api.getKeyManager().countPhysical(player, crate);
    int virtual = api.getKeyManager().getVirtual(player, crateId);

    // Get KeyAll info
    KeyAllManager kam = ((QuickCrates) api).getKeyAllManager();
    long secs = kam.getSecondsUntilNext();
    boolean enabled = kam.isEnabled();

    // Get crate list
    Collection<Crate> crates = api.getCrateManager().getCrates();
}
```

### Key Classes

| Class | Location | Purpose |
|---|---|---|
| `QuickCratesAPI` | `com.quickcrates.api.QuickCratesAPI` | Public API interface |
| `KeyManager` | `com.quickcrates.key.KeyManager` | Key operations |
| `KeyAllManager` | `com.quickcrates.key.KeyAllManager` | Auto key distribution |
| `CrateManager` | `com.quickcrates.crate.CrateManager` | Crate registry |
| `Crate` | `com.quickcrates.crate.Crate` | Individual crate data |

### QuickCratesAPI Interface

```java
public interface QuickCratesAPI {
    CrateManager getCrateManager();
    KeyManager getKeyManager();
}
```

---

## Suggested QuickSidebar Config

```yaml
sidebar:
  title: "&6&lQuickCrates"
  lines:
    - "&7&m-----------------"
    - "&fYour Keys:"
    - " &e%quickcrates_keys_vote% &7Vote Keys"
    - " &e%quickcrates_keys_rare% &7Rare Keys"
    - ""
    - "&fNext KeyAll:"
    - " &e%quickcrates_keyall_time%"
    - "&7&m-----------------"
  update-interval: 20  # ticks (1 second)
```

---

## Data Flow

```
QuickCrates (manages keys/timers)
    ↓
PapiHook (registers %quickcrates_*% placeholders)
    ↓
PlaceholderAPI (resolves placeholders)
    ↓
QuickSidebar (displays in sidebar)
```

Or for direct integration:

```
QuickCrates (manages keys/timers)
    ↓
QuickSidebar (calls QuickCratesAPI directly)
    ↓
Sidebar (displays)
```

---

## KeyAll Timer Details

- `KeyAllManager.getSecondsUntilNext()` returns seconds remaining (0 if ready)
- Timer resets when `/keyall` is manually triggered
- Timer resets after each automatic distribution
- Configured via `config.yml` under `keyall:` section

---

## Maven Dependency (if QuickSidebar wants to compile against QuickCrates)

```xml
<dependency>
    <groupId>com.quickcrates</groupId>
    <artifactId>QuickCrates</artifactId>
    <version>1.0</version>
    <scope>provided</scope>
</dependency>
```

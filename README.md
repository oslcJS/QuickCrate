# QuickCrates

Free, open-source Minecraft crate plugin — weighted rewards, animated chest GUI, virtual & physical keys, preview GUI, PlaceholderAPI support.

## Supported versions
Paper / Spigot **1.20.4 → 26.1.2** (1.20.4, 1.20.5, 1.20.6, 1.21, 1.21.1, 1.21.3, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 26.1, 26.1.2).

The plugin uses only stable Bukkit/Paper API surfaces (Inventory, ItemMeta, PersistentDataContainer, BukkitRunnable) so a single jar runs across the whole range. Per-version compile profiles are provided to verify source compatibility.

## Build

Single universal jar:
```
mvn clean package
```
Produces `target/QuickCrates-1.0.0.jar`.

Build against every supported version:
```
./build-all.sh
```
Or use the GitHub Actions matrix (`.github/workflows/build.yml`).

Build against one specific version:
```
mvn -P paper-1.21.8 clean package
mvn -P paper-26.1.2 clean package
```

## Commands
- `/qc list` — list crates
- `/qc reload` — reload config
- `/qc preview <crate>` — open preview GUI
- `/qc givekey <player> <crate> [amount] [virtual]` — give keys
- `/qc setlocation <crate>` — bind looked-at block as crate
- `/key give|givevirtual <player> <crate> [amount]`

## Permissions
- `quickcrates.admin` — admin commands
- `quickcrates.use` — open crates (default)
- `quickcrates.preview` — preview crates (default)

## Crate file (`plugins/QuickCrates/crates/<id>.yml`)
See bundled `vote.yml`. Each reward has `weight` (relative), `display`, optional `items`, optional `money`, optional non-item `commands` (`{player}` placeholder), and optional `broadcast: true` for rare wins.

Item rewards are created directly from `plugins/QuickCrates/items.json`; QuickCrates does not dispatch `/give` for crate rewards. Legacy `give {player} <item> [amount]` reward commands are intercepted and converted into direct inventory items.

## Animations
`SPIN`, `CSGO`, `ROULETTE`, `QUICK`, `FIREWORK`. Set per crate via `animation:`.

## API
```java
QuickCratesAPI api = Bukkit.getServicesManager().load(QuickCratesAPI.class);
api.getCrateManager().get("vote");
api.getKeyManager().addVirtual(player, "vote", 5);
```

## PlaceholderAPI
- `%quickcrates_keys_<crateId>%` — virtual key count
- `%quickcrates_crate_count%` — number of loaded crates

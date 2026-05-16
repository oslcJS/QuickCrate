<p align="center">
  <img src="https://raw.githubusercontent.com/oslcJS/.github/main/assets/VAULT.gif" width="160">
</p>

<h1 align="center">QuickCrates</h1>

<p align="center">
Free and open source crate system for Minecraft servers.
</p>

---

<p align="center">
  <span style="font-family: 'IBM Plex Mono', monospace; font-style: italic; border: 1px solid #1c1c1c; padding: 6px 10px; color: #555; background: #000;">
    status <span style="color:#222;">/</span>
    <span style="color:#fff; font-weight:600;">stable</span>
  </span>
</p>

<div align="center">
  <table>
    <tr>
      <td><img src="https://raw.githubusercontent.com/oslcJS/.github/main/assets/logo_03.png" width="72"></td>
      <td><strong>QuickPlugins</strong><br>Small, fast Minecraft plugins built for modern Paper, Spigot, and Purpur servers.</td>
    </tr>
  </table>
</div>

---

## overview

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/14.gif)

QuickCrates is a lightweight crate plugin featuring weighted rewards, animations, preview GUIs, physical and virtual keys, and PlaceholderAPI support.

---

## features

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/2.gif)

- weighted rewards  
- animated opening GUIs  
- virtual & physical keys  
- preview inventories  
- PlaceholderAPI support  
- direct item rewards  
- configurable broadcasts  

---

## compatibility

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/3.gif)

- Paper  
- Spigot  
- 1.20.4 → 1.21.8  
- 26.1 → 26.1.2  

Single jar support across all versions.

---

## installs

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/4.gif)

Drop the jar into `/plugins` and restart the server.

---

## configs

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/5.gif)

Crates are configured in:

```txt
plugins/QuickCrates/crates/
```

Supports:
- rewards  
- weights  
- commands  
- money rewards  
- broadcasts  
- animations  

---

## plugins

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/7.gif)

- PlaceholderAPI  
- QuickEco integration  
- multi-plugin compatible  

---

## command

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/11.gif)

```txt
/qc list
/qc create <name>
/qc remove <name>
/qc preview <crate>
/qc wand
/qc key give <player> <crate> [amount]
/qc key givevirtual <player> <crate> [amount]
/qc keyall <crate> [amount]
/qc reload
/keys
```

---

## backend

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/9.gif)

Uses stable Bukkit/Paper APIs only:
- Inventory API  
- ItemMeta  
- PersistentDataContainer  
- BukkitRunnable  

---

## support

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/8.gif)

Built for modern Minecraft servers with lightweight runtime performance.

---

## license

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/6.gif)

MIT

package com.quickcrates.util;

import com.quickcrates.QuickCrates;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ItemRegistry {
    private static final Pattern QUOTED_VALUE = Pattern.compile("\"([A-Za-z0-9_:\\-.]+)\"");

    private final QuickCrates plugin;
    private final Set<String> items = new HashSet<>();

    public ItemRegistry(QuickCrates plugin) {
        this.plugin = plugin;
    }

    public void load() {
        items.clear();
        File file = new File(plugin.getDataFolder(), "items.json");
        if (!file.exists()) {
            plugin.saveResource("items.json", false);
        }

        try {
            String json = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            Matcher matcher = QUOTED_VALUE.matcher(json);
            while (matcher.find()) {
                register(matcher.group(1));
            }
        } catch (Exception ex) {
            plugin.getLogger().warning("Failed to load items.json, using Bukkit material registry: " + ex.getMessage());
        }

        if (items.isEmpty()) {
            try (InputStream in = plugin.getResource("items.json")) {
                if (in != null) {
                    String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                    Matcher matcher = QUOTED_VALUE.matcher(json);
                    while (matcher.find()) {
                        register(matcher.group(1));
                    }
                }
            } catch (Exception ex) {
                plugin.getLogger().warning("Failed to read bundled items.json: " + ex.getMessage());
            }
        }

        for (Material material : Material.values()) {
            if (material.isItem() && !material.isLegacy() && !material.isAir()) {
                items.add(material.name());
            }
        }
    }

    public boolean isKnownItem(String materialName) {
        Material material = match(materialName);
        return material != null && items.contains(material.name());
    }

    public ItemStack create(String materialName, int amount) {
        Material material = match(materialName);
        if (material == null || !items.contains(material.name())) {
            return null;
        }
        return new ItemStack(material, Math.max(1, amount));
    }

    public boolean isGiveCommand(String command) {
        String[] parts = split(command);
        return parts.length >= 3 && (parts[0].equalsIgnoreCase("give")
                || parts[0].equalsIgnoreCase("minecraft:give"));
    }

    public boolean giveFromCommand(PlayerLike player, String command) {
        String[] parts = split(command);
        if (parts.length < 3) {
            return false;
        }

        String target = parts[1];
        if (!target.equalsIgnoreCase(player.name())) {
            return true;
        }

        int amount = 1;
        if (parts.length >= 4) {
            try {
                amount = Integer.parseInt(parts[3]);
            } catch (NumberFormatException ignored) {}
        }

        ItemStack item = create(parts[2], amount);
        if (item == null) {
            plugin.getLogger().warning("Blocked /give reward for unknown item '" + parts[2] + "'. Add it to items.json if it is valid.");
            return true;
        }
        player.add(item);
        return true;
    }

    private void register(String value) {
        Material material = match(value);
        if (material != null && material.isItem() && !material.isLegacy() && !material.isAir()) {
            items.add(material.name());
        }
    }

    private Material match(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("MINECRAFT:")) {
            normalized = normalized.substring("MINECRAFT:".length());
        }
        return Material.matchMaterial(normalized);
    }

    private String[] split(String command) {
        if (command == null) {
            return new String[0];
        }
        String cleaned = command.trim();
        if (cleaned.startsWith("/")) {
            cleaned = cleaned.substring(1);
        }
        return cleaned.split("\\s+");
    }

    public interface PlayerLike {
        String name();
        void add(ItemStack item);
    }
}

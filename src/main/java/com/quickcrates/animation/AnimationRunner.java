package com.quickcrates.animation;

import com.quickcrates.QuickCrates;
import com.quickcrates.crate.Crate;
import com.quickcrates.gui.AnimationGui;
import com.quickcrates.reward.Reward;
import com.quickcrates.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AnimationRunner {
    private final QuickCrates plugin;

    public AnimationRunner(QuickCrates plugin) { this.plugin = plugin; }

    public void play(Player player, Crate crate) {
        Reward winner = crate.getRewards().pick();
        AnimationGui gui = new AnimationGui(plugin, player, crate, winner);
        gui.open();
        
        playSafe(player, "BLOCK_ENDER_CHEST_OPEN", 0.9f, 0.8f);

        switch (crate.getAnimation()) {
            case QUICK    -> runQuick(player, gui, winner, crate);
            case ROULETTE -> runRoulette(player, gui, winner, crate);
            case FIREWORK -> runFirework(player, gui, winner, crate);
            case CSGO     -> runCsgo(player, gui, winner, crate);
            default       -> runSpin(player, gui, winner, crate);
        }
    }

    

    private void finish(Player p, Crate c, Reward winner) {
        plugin.getCrateManager().unlock(p.getUniqueId());
        winner.give(p);

        if (winner.getMoney() > 0) {
            String formatted = com.quickcrates.compat.QuickEcoBridge.isLinked()
                    ? com.quickcrates.compat.QuickEcoBridge.format(winner.getMoney())
                    : String.format("%.2f", winner.getMoney());
            Msg.send(p, "win-money", "money", formatted, "crate", c.getDisplayName());
        } else {
            Msg.send(p, "win", "reward", winner.displayName(), "crate", c.getDisplayName());
        }

        
        playSafe(p, "ENTITY_PLAYER_LEVELUP",  1f, 1f);
        playSafe(p, "BLOCK_BELL_USE",          1f, 1.2f);
        playSafe(p, "ENTITY_EXPERIENCE_ORB_PICKUP", 0.6f, 1.4f);

        if (winner.isBroadcast() && plugin.getConfig().getBoolean("settings.broadcast-rare", true)) {
            String key = winner.getMoney() > 0 ? "messages.broadcast-money" : "messages.broadcast";
            String bc = plugin.getConfig().getString(key,
                    "&e{player} &7won &e{reward} &7from &6{crate}&7!");
            bc = bc.replace("{player}", p.getName())
                   .replace("{reward}", winner.displayName())
                   .replace("{money}", winner.getMoney() > 0
                           ? (com.quickcrates.compat.QuickEcoBridge.isLinked()
                               ? com.quickcrates.compat.QuickEcoBridge.format(winner.getMoney())
                               : String.format("%.2f", winner.getMoney()))
                           : "")
                   .replace("{crate}", c.getDisplayName());
            Bukkit.broadcastMessage(Msg.prefix() + Msg.color(bc));
        }
    }

    

    
    private void runQuick(Player p, AnimationGui gui, Reward winner, Crate crate) {
        new BukkitRunnable() {
            int t = 0;
            public void run() {
                gui.tickRandom();
                playSafe(p, "UI_BUTTON_CLICK", 0.5f, 1.6f - (t * 0.05f));
                if (++t >= 10) {
                    gui.placeWinner(winner);
                    cancel();
                    finish(p, crate, winner);
                }
            }
        }.runTaskTimer(plugin, 1L, 2L);
    }

    
    private void runSpin(Player p, AnimationGui gui, Reward winner, Crate crate) {
        new BukkitRunnable() {
            int t = 0;
            public void run() {
                gui.tickSpin();
                
                float pitch = Math.max(0.5f, 1.8f - (t * 0.04f));
                playSafe(p, "UI_BUTTON_CLICK", 0.4f, pitch);
                if (++t >= 30) {
                    gui.placeWinner(winner);
                    cancel();
                    playSafe(p, "BLOCK_NOTE_BLOCK_PLING", 1f, 1.5f);
                    finish(p, crate, winner);
                }
            }
        }.runTaskTimer(plugin, 1L, 2L);
    }

    
    private void runCsgo(Player p, AnimationGui gui, Reward winner, Crate crate) {
        new BukkitRunnable() {
            int t = 0;
            public void run() {
                gui.tickCsgoScroll();

                
                float pitch;
                if (t < 20)       pitch = 1.8f;
                else if (t < 35)  pitch = 1.4f;
                else               pitch = 0.9f;
                playSafe(p, "UI_BUTTON_CLICK", 0.35f, pitch);

                
                if (t > 25 && t % 8 == 0)
                    playSafe(p, "BLOCK_WOODEN_BUTTON_CLICK_ON", 0.6f, 0.8f);

                if (++t >= 45) {
                    gui.placeWinner(winner);
                    cancel();
                    
                    playSafe(p, "BLOCK_IRON_DOOR_CLOSE",  0.7f, 1.4f);
                    playSafe(p, "BLOCK_NOTE_BLOCK_PLING", 1f,   1.8f);
                    finish(p, crate, winner);
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    
    private void runRoulette(Player p, AnimationGui gui, Reward winner, Crate crate) {
        new BukkitRunnable() {
            int t = 0;
            public void run() {
                gui.tickRoulette();
                
                float pitch = (t % 2 == 0) ? 1.4f : 1.0f;
                playSafe(p, "BLOCK_WOODEN_BUTTON_CLICK_ON", 0.45f, pitch);
                if (++t >= 40) {
                    gui.placeWinner(winner);
                    cancel();
                    playSafe(p, "BLOCK_NOTE_BLOCK_PLING", 1f, 1.6f);
                    finish(p, crate, winner);
                }
            }
        }.runTaskTimer(plugin, 1L, 2L);
    }

    
    private void runFirework(Player p, AnimationGui gui, Reward winner, Crate crate) {
        new BukkitRunnable() {
            int t = 0;
            public void run() {
                gui.tickRandom();
                playSafe(p, "UI_BUTTON_CLICK", 0.4f, 1.5f - (t * 0.03f));
                if (++t >= 14) {
                    gui.placeWinner(winner);
                    cancel();
                    spawnFirework(p);
                    playSafe(p, "ENTITY_FIREWORK_ROCKET_LAUNCH", 1f, 1f);
                    finish(p, crate, winner);
                }
            }
        }.runTaskTimer(plugin, 1L, 2L);
    }

    

    private void spawnFirework(Player p) {
        try {
            org.bukkit.entity.Firework fw = (org.bukkit.entity.Firework)
                    p.getWorld().spawnEntity(p.getLocation(),
                            org.bukkit.entity.EntityType.valueOf("FIREWORK_ROCKET"));
            org.bukkit.inventory.meta.FireworkMeta meta = fw.getFireworkMeta();
            meta.addEffect(org.bukkit.FireworkEffect.builder()
                    .with(org.bukkit.FireworkEffect.Type.BURST)
                    .withColor(org.bukkit.Color.AQUA, org.bukkit.Color.YELLOW)
                    .withFlicker().withTrail().build());
            meta.setPower(0);
            fw.setFireworkMeta(meta);
        } catch (Throwable t) {
            try {
                p.getWorld().spawnEntity(p.getLocation(),
                        org.bukkit.entity.EntityType.valueOf("FIREWORK"));
            } catch (Throwable ignored) {}
        }
    }

    private void playSafe(Player p, String soundName, float volume, float pitch) {
        try {
            Sound s = Sound.valueOf(soundName);
            p.playSound(p.getLocation(), s, volume, Math.max(0.5f, Math.min(2f, pitch)));
        } catch (Throwable ignored) {}
    }
}

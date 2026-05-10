package com.quickcrates.animation;

public enum AnimationType {
    SPIN, CSGO, ROULETTE, QUICK, FIREWORK;

    public static AnimationType fromString(String s) {
        if (s == null) return SPIN;
        try { return AnimationType.valueOf(s.toUpperCase()); }
        catch (Exception ex) { return SPIN; }
    }
}

package com.quickcrates.crate;

public enum CrateMode {
    DEFAULT, DONUT_SMP;

    public static CrateMode fromString(String s) {
        if (s == null) return DEFAULT;
        try { return CrateMode.valueOf(s.toUpperCase().replace("-", "_")); }
        catch (Exception ex) { return DEFAULT; }
    }
}

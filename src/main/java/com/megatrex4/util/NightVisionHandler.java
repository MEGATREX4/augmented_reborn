package com.megatrex4.util;

public class NightVisionHandler {
    private static boolean nightVisionEnabled = false;

    public static void toggleNightVision() {
        nightVisionEnabled = !nightVisionEnabled;
    }

    public static boolean isNightVisionEnabled() {
        return nightVisionEnabled;
    }
}

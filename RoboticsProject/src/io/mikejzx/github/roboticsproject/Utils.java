package io.mikejzx.github.roboticsproject;

public final class Utils {
    public static float clamp(float x, float min, float max) {
        return (x < min) ? min : (x > max ? max : x);
    }

    public static float clamp01(float x) {
        return (x < 0.0f) ? 0.0f : (x > 1.0f ? 1.0f : x);
    }


    public static int lerp(int a, int b, float t) {
        return Math.round(a * (1.0f - t) + b * t);
    }
    
    public static float lerp(float a, float b, float t) {
        return a * (1.0f - t) + b * t;
    }
}

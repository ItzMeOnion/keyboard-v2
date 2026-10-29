package dev.keyboardoverlay.render;

import dev.keyboardoverlay.config.OverlayConfig;

/** Computes per-key RGB colours for the chosen RGB mode. */
public final class RgbEngine {

    private RgbEngine() {}

    /**
     * @param nx       horizontal position of the key on the board, 0..1
     * @param ny       vertical position of the key on the board, 0..1
     * @param index    key index, used for per-key effects
     * @param t        time in seconds since the mod started
     * @param fallback colour to use when RGB is off or this mode leaves the key alone. Its alpha is kept.
     */
    public static int color(OverlayConfig c, float nx, float ny, int index, double t, int fallback) {
        if (c.rgbMode == OverlayConfig.RgbMode.OFF) {
            return fallback;
        }
        int alpha = fallback & 0xFF000000;
        float speed = c.rgbSpeed;
        int rgb;
        switch (c.rgbMode) {
            case RAINBOW -> rgb = hsv(frac((float) (t * speed * 0.1) + nx * 0.5f + ny * 0.25f), c.rgbSaturation, c.rgbBrightness);
            case WAVE -> {
                float p = frac((float) (t * speed * 0.1));
                float d = Math.abs(nx - p);
                d = Math.min(d, 1f - d);
                float inten = Math.max(0f, 1f - d / Math.max(0.05f, c.rgbWaveWidth));
                rgb = scale(blend(fallback & 0xFFFFFF, c.rgbColorA & 0xFFFFFF, inten), c.rgbBrightness);
            }
            case PER_KEY_RANDOM -> rgb = hsv(frac(index * 0.618034f + (float) (t * speed * 0.05)), c.rgbSaturation, c.rgbBrightness);
            case BREATHE -> {
                float k = (float) ((Math.sin(t * speed * Math.PI) + 1.0) / 2.0);
                rgb = scale(blend(c.rgbColorA & 0xFFFFFF, c.rgbColorB & 0xFFFFFF, k), c.rgbBrightness);
            }
            case STATIC_A -> rgb = scale(c.rgbColorA & 0xFFFFFF, c.rgbBrightness);
            case GRADIENT_AB -> rgb = scale(blend(c.rgbColorA & 0xFFFFFF, c.rgbColorB & 0xFFFFFF, nx), c.rgbBrightness);
            default -> rgb = fallback & 0xFFFFFF;
        }
        return alpha | (rgb & 0xFFFFFF);
    }

    public static float frac(float v) {
        return v - (float) Math.floor(v);
    }

    /** Standard HSV to RGB, returned as 0xRRGGBB. */
    public static int hsv(float h, float s, float v) {
        float hh = frac(h) * 6f;
        int i = (int) Math.floor(hh);
        float f = hh - i;
        float p = v * (1f - s);
        float q = v * (1f - s * f);
        float tt = v * (1f - s * (1f - f));
        float r;
        float g;
        float b;
        switch (i % 6) {
            case 0 -> { r = v; g = tt; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = tt; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = tt; g = p; b = v; }
            default -> { r = v; g = p; b = q; }
        }
        return (clamp8(r) << 16) | (clamp8(g) << 8) | clamp8(b);
    }

    /** Blends two 0xRRGGBB colours. */
    public static int blend(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = (int) (((a >> 16) & 0xFF) * (1 - t) + ((b >> 16) & 0xFF) * t);
        int g = (int) (((a >> 8) & 0xFF) * (1 - t) + ((b >> 8) & 0xFF) * t);
        int bl = (int) ((a & 0xFF) * (1 - t) + (b & 0xFF) * t);
        return (r << 16) | (g << 8) | bl;
    }

    private static int scale(int rgb, float f) {
        int r = (int) (((rgb >> 16) & 0xFF) * f);
        int g = (int) (((rgb >> 8) & 0xFF) * f);
        int b = (int) ((rgb & 0xFF) * f);
        return (r << 16) | (g << 8) | b;
    }

    private static int clamp8(float v) {
        return Math.max(0, Math.min(255, Math.round(v * 255f)));
    }
}

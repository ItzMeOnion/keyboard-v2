package dev.keyboardoverlay.render;

import dev.keyboardoverlay.config.OverlayConfig;

/**
 * Resolved colours for the current look. Either comes from the chosen design,
 * or from the manual colour settings when "override design colours" is on.
 */
public record Palette(int fill, int border, int text, int glow, int radius, int borderThickness, boolean shadow) {

    public static Palette of(OverlayConfig c) {
        if (c.overrideDesignColors) {
            return new Palette(c.baseColor, c.borderColor, c.baseTextColor, c.glowColor,
                    Math.round(c.cornerRadius), c.borderThickness, c.labelShadow);
        }
        return switch (c.design) {
            case MODERN -> new Palette(0xB01A1A1A, 0xFF3A3A3A, 0xFFFFFFFF, 0xFF00E5FF, 3, 1, c.labelShadow);
            case MINIMAL -> new Palette(0x55000000, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0, 1, c.labelShadow);
            case NEON -> new Palette(0x88000000, 0xFFFF00FF, 0xFFFFFFFF, 0xFF00FFFF, 4, 2, c.labelShadow);
            case PIXEL -> new Palette(0xFF2B2B2B, 0xFF000000, 0xFFFFFFFF, 0xFF00FF88, 0, 3, c.labelShadow);
            case RETRO -> new Palette(0xFFE8DCC0, 0xFF6B5B3E, 0xFF3A2E1A, 0xFFFFAA33, 2, 2, true);
            case GLASS -> new Palette(0x44FFFFFF, 0x88FFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 6, 1, c.labelShadow);
            case OUTLINE -> new Palette(0x00000000, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 2, 1, c.labelShadow);
            case CYBER -> new Palette(0xCC0A0F1A, 0xFF00FFD5, 0xFF00FFD5, 0xFFFF2BD6, 0, 1, c.labelShadow);
            case CANDY -> new Palette(0xFFFFC8DD, 0xFFFFAFCC, 0xFF5E2B4F, 0xFFBDE0FE, 8, 0, c.labelShadow);
            case MONO -> new Palette(0xCC222222, 0xFF888888, 0xFFEEEEEE, 0xFFFFFFFF, 2, 1, c.labelShadow);
        };
    }

    /** Blends two ARGB colours, including alpha. */
    public static int blendArgb(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int aa = (int) (((a >>> 24) & 0xFF) * (1 - t) + ((b >>> 24) & 0xFF) * t);
        int rgb = RgbEngine.blend(a & 0xFFFFFF, b & 0xFFFFFF, t);
        return (aa << 24) | rgb;
    }
}

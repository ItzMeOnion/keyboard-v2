package dev.keyboardoverlay.input;

import dev.keyboardoverlay.config.OverlayConfig;
import dev.keyboardoverlay.config.OverlayConfig.Preset;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Layouts are written as text so presets and custom boards use the same parser.
 * Rows are separated by "|". Keys are separated by spaces. "_" is an empty gap.
 * A width can follow a key after a colon, e.g. "SPACE:3" or "LSHIFT:1.5".
 */
public final class Layouts {

    public record Board(List<KeyDef> keys, float width, float height) {}

    private static String cacheKey = null;
    private static Board cache = null;

    private Layouts() {}

    public static Board current(OverlayConfig c) {
        String layout = c.preset == Preset.CUSTOM ? c.customLayout : presetString(c.preset);
        String key = c.preset.name() + "|" + layout;
        if (cache == null || !key.equals(cacheKey)) {
            cache = parse(layout);
            cacheKey = key;
        }
        return cache;
    }

    public static String presetString(Preset preset) {
        return switch (preset) {
            case WASD -> "_ W _|A S D";
            case WASD_SPACE -> "_ W _|A S D|_ SPACE:3";
            case WASD_MOUSE -> "_ W _ _ _ LMB:1.5 RMB:1.5|A S D";
            case WASD_FULL_MODIFIERS -> "Q W E|A S D|LSHIFT:1.5 SPACE:3 LCTRL:1.5";
            case ARROWS -> "_ UP _|LEFT DOWN RIGHT";
            case FULL_KEYBOARD -> FULL;
            case FULL_KEYBOARD_MOUSE -> FULL + "|_ _ _ _ _ _ _ _ _ _ _ _ _ _ LMB:1.5 MMB RMB:1.5";
            case NUMPAD -> "N7 N8 N9 NADD|N4 N5 N6 NSUB|N1 N2 N3 NENTER|N0:2 NDOT";
            case MOUSE_ONLY -> "M4 LMB:1.5 MMB RMB:1.5 M5";
            case CUSTOM -> "";
        };
    }

    private static final String FULL =
            "ESC:1 F1 F2 F3 F4 F5 F6 F7 F8 F9 F10 F11 F12|"
            + "` 1 2 3 4 5 6 7 8 9 0 - = BKSP:2|"
            + "TAB:1.5 Q W E R T Y U I O P [ ] \\:1.5|"
            + "CAPS:1.75 A S D F G H J K L ; ' ENTER:2.25|"
            + "LSHIFT:2.25 Z X C V B N M , . / RSHIFT:2.75|"
            + "LCTRL:1.25 LALT:1.25 SPACE:6.25 RALT:1.25 RCTRL:1.25";

    /** Parses a layout string into positioned keys. Unknown tokens become gaps. */
    public static Board parse(String layout) {
        List<KeyDef> keys = new ArrayList<>();
        float maxW = 0f;
        float maxH = 0f;
        String[] rows = layout.split("\\|");
        for (int r = 0; r < rows.length; r++) {
            float x = 0f;
            String trimmed = rows[r].trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            for (String token : trimmed.split("\\s+")) {
                String name = token;
                float w = 1f;
                int colon = token.indexOf(':');
                if (colon > 0) {
                    name = token.substring(0, colon);
                    try {
                        w = Math.max(0.25f, Float.parseFloat(token.substring(colon + 1)));
                    } catch (NumberFormatException ignored) {
                        w = 1f;
                    }
                }
                if (name.equals("_")) {
                    x += w;
                    continue;
                }
                Keys.Key k = Keys.resolve(name);
                if (k != null) {
                    keys.add(new KeyDef(name.toUpperCase(Locale.ROOT), k.code(), k.mouse(), x, r, w, 1f));
                }
                x += w;
                maxW = Math.max(maxW, x);
            }
            maxH = Math.max(maxH, r + 1f);
        }
        return new Board(keys, maxW, maxH);
    }
}

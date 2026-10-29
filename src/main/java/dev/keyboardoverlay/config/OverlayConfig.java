package dev.keyboardoverlay.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Every user-facing setting lives here. Serialised to config/keyboard-overlay.json.
 * Public fields keep the settings screen and renderer straightforward.
 */
public final class OverlayConfig {

    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("keyboard-overlay.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static OverlayConfig instance = new OverlayConfig();

    // ---- general ----
    public boolean enabled = true;
    public Preset preset = Preset.WASD;
    public Design design = Design.MODERN;
    public boolean showMouse = false;
    public boolean hideInF3 = true;
    public boolean hideWhenChatOpen = false;

    // ---- layout ----
    public float scale = 1.0f;          // 0.25 .. 3.0
    public Anchor anchor = Anchor.BOTTOM_LEFT;
    public int offsetX = 8;             // px from the anchored edge
    public int offsetY = 8;
    public float keySize = 18f;         // base key size in px before scale
    public float keyGap = 2f;           // px between keys
    public float cornerRadius = 3f;     // 0 = square
    public boolean showLabels = true;
    public boolean labelShadow = true;
    public float labelScale = 1.0f;     // relative to key size
    public boolean uppercaseLabels = true;

    // ---- colours (ARGB) ----
    public int baseColor = 0xAA1A1A1A;       // idle fill
    public int baseTextColor = 0xFFFFFFFF;
    public int borderColor = 0xFF3A3A3A;
    public int borderThickness = 1;
    public int glowColor = 0xFF00E5FF;       // pulse colour
    public int pressedTextColor = 0xFF000000;
    public float fillOpacity = 1.0f;         // multiplier on base alpha
    public boolean useGradient = false;
    public int gradientEndColor = 0xAA0A0A0A;

    // ---- pulse / press animation ----
    public boolean pulseEnabled = true;
    public PulseStyle pulseStyle = PulseStyle.FADE;
    public int pulseDurationMs = 250;        // how long the pulse lasts
    public float pulseIntensity = 1.0f;      // 0 .. 2, how strong the flash is
    public float pulseGrow = 0.0f;           // px the key grows on press
    public boolean pulseSpread = false;      // glow bleeds outside the key
    public int pulseSpreadPx = 4;
    public Easing easing = Easing.EASE_OUT;
    public boolean holdGlow = true;          // keep lit while held

    // ---- rgb ----
    public RgbMode rgbMode = RgbMode.OFF;
    public float rgbSpeed = 1.0f;            // cycles per second
    public float rgbSaturation = 1.0f;
    public float rgbBrightness = 1.0f;
    public int rgbColorA = 0xFFFF0055;
    public int rgbColorB = 0xFF00AAFF;
    public float rgbWaveWidth = 0.35f;       // for wave mode, fraction of keyboard
    public boolean rgbAffectsBorder = true;
    public boolean rgbAffectsText = false;
    public boolean rgbAffectsGlow = false;

    // ---- design / custom layout ----
    public boolean overrideDesignColors = false; // true = use the colour fields above instead of the design palette
    public String customLayout = "Q W E|A S D|LSHIFT:1.5 SPACE:3 LCTRL:1.5";

    // ---- static look ----
    public float idleOpacity = 0.85f;
    public boolean hideIdleKeys = false;     // only show keys while pressed

    public static OverlayConfig get() {
        return instance;
    }

    public static void load() {
        if (!Files.exists(PATH)) {
            instance = new OverlayConfig();
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(PATH)) {
            OverlayConfig loaded = GSON.fromJson(reader, OverlayConfig.class);
            instance = loaded != null ? loaded : new OverlayConfig();
        } catch (IOException | RuntimeException e) {
            instance = new OverlayConfig();
        }
        instance.clamp();
    }

    public static void save() {
        try (Writer writer = Files.newBufferedWriter(PATH)) {
            GSON.toJson(instance, writer);
        } catch (IOException ignored) {
        }
    }

    public static void reset() {
        instance = new OverlayConfig();
        save();
    }

    /** Keeps values within sane ranges so a hand-edited file cannot break rendering. */
    public void clamp() {
        scale = clampF(scale, 0.25f, 3.0f);
        keySize = clampF(keySize, 6f, 64f);
        keyGap = clampF(keyGap, 0f, 16f);
        cornerRadius = clampF(cornerRadius, 0f, 20f);
        labelScale = clampF(labelScale, 0.3f, 2.0f);
        fillOpacity = clampF(fillOpacity, 0f, 1f);
        pulseDurationMs = Math.max(20, Math.min(5000, pulseDurationMs));
        pulseIntensity = clampF(pulseIntensity, 0f, 2f);
        pulseGrow = clampF(pulseGrow, 0f, 10f);
        pulseSpreadPx = Math.max(0, Math.min(24, pulseSpreadPx));
        rgbSpeed = clampF(rgbSpeed, 0f, 10f);
        rgbSaturation = clampF(rgbSaturation, 0f, 1f);
        rgbBrightness = clampF(rgbBrightness, 0f, 1f);
        rgbWaveWidth = clampF(rgbWaveWidth, 0.05f, 1f);
        idleOpacity = clampF(idleOpacity, 0f, 1f);
        borderThickness = Math.max(0, Math.min(6, borderThickness));
        if (preset == null) preset = Preset.WASD;
        if (design == null) design = Design.MODERN;
        if (anchor == null) anchor = Anchor.BOTTOM_LEFT;
        if (pulseStyle == null) pulseStyle = PulseStyle.FADE;
        if (easing == null) easing = Easing.EASE_OUT;
        if (rgbMode == null) rgbMode = RgbMode.OFF;
        if (customLayout == null) customLayout = "Q W E|A S D";
    }

    private static float clampF(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    // ---- enums ----

    public enum Anchor {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT,
        MIDDLE_LEFT, CENTER, MIDDLE_RIGHT,
        BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
    }

    public enum PulseStyle {
        FADE,        // colour fades out
        FLASH,       // instant on, linear off
        RING,        // expanding ring outline
        SHRINK,      // key compresses on press
        BLOOM        // soft glow halo
    }

    public enum Easing {
        LINEAR, EASE_IN, EASE_OUT, EASE_IN_OUT, BOUNCE
    }

    public enum RgbMode {
        OFF,
        RAINBOW,        // hue cycles across the whole board over time
        WAVE,           // a moving band across the keyboard
        PER_KEY_RANDOM, // each key gets a fixed hue
        BREATHE,        // colour A <-> colour B fade
        STATIC_A,       // solid colour A
        GRADIENT_AB     // gradient from A to B across the board
    }

    public enum Preset {
        WASD,
        WASD_SPACE,
        WASD_MOUSE,
        WASD_FULL_MODIFIERS,
        ARROWS,
        FULL_KEYBOARD,
        FULL_KEYBOARD_MOUSE,
        NUMPAD,
        MOUSE_ONLY,
        CUSTOM
    }

    public enum Design {
        MODERN,      // rounded, dark glass
        MINIMAL,     // flat, outline only
        NEON,        // bright outlines, glowing text
        PIXEL,       // hard edges, chunky border (matches the logo)
        RETRO,       // beige keycaps, drop shade
        GLASS,       // translucent white
        OUTLINE,     // transparent fill, border only
        CYBER,       // dark with angled corners
        CANDY,       // pastel, very round
        MONO         // greyscale, no colour
    }
}

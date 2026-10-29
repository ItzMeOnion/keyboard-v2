package dev.keyboardoverlay.input;

import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Maps layout tokens such as "W", "LSHIFT", "LMB" to GLFW key or mouse-button codes. */
public final class Keys {

    public record Key(int code, boolean mouse) {}

    private static final Map<String, Key> MAP = new HashMap<>();

    static {
        for (char c = 'A'; c <= 'Z'; c++) {
            put(String.valueOf(c), GLFW.GLFW_KEY_A + (c - 'A'), false);
        }
        for (int d = 0; d <= 9; d++) {
            put(String.valueOf(d), GLFW.GLFW_KEY_0 + d, false);
            put("N" + d, GLFW.GLFW_KEY_KP_0 + d, false);
        }
        for (int f = 1; f <= 12; f++) {
            put("F" + f, GLFW.GLFW_KEY_F1 + f - 1, false);
        }
        put("ESC", GLFW.GLFW_KEY_ESCAPE, false);
        put("TAB", GLFW.GLFW_KEY_TAB, false);
        put("CAPS", GLFW.GLFW_KEY_CAPS_LOCK, false);
        put("LSHIFT", GLFW.GLFW_KEY_LEFT_SHIFT, false);
        put("RSHIFT", GLFW.GLFW_KEY_RIGHT_SHIFT, false);
        put("LCTRL", GLFW.GLFW_KEY_LEFT_CONTROL, false);
        put("RCTRL", GLFW.GLFW_KEY_RIGHT_CONTROL, false);
        put("LALT", GLFW.GLFW_KEY_LEFT_ALT, false);
        put("RALT", GLFW.GLFW_KEY_RIGHT_ALT, false);
        put("SPACE", GLFW.GLFW_KEY_SPACE, false);
        put("ENTER", GLFW.GLFW_KEY_ENTER, false);
        put("BKSP", GLFW.GLFW_KEY_BACKSPACE, false);
        put("UP", GLFW.GLFW_KEY_UP, false);
        put("DOWN", GLFW.GLFW_KEY_DOWN, false);
        put("LEFT", GLFW.GLFW_KEY_LEFT, false);
        put("RIGHT", GLFW.GLFW_KEY_RIGHT, false);
        put("INS", GLFW.GLFW_KEY_INSERT, false);
        put("DEL", GLFW.GLFW_KEY_DELETE, false);
        put("HOME", GLFW.GLFW_KEY_HOME, false);
        put("END", GLFW.GLFW_KEY_END, false);
        put("PGUP", GLFW.GLFW_KEY_PAGE_UP, false);
        put("PGDN", GLFW.GLFW_KEY_PAGE_DOWN, false);
        put("`", GLFW.GLFW_KEY_GRAVE_ACCENT, false);
        put("-", GLFW.GLFW_KEY_MINUS, false);
        put("=", GLFW.GLFW_KEY_EQUAL, false);
        put("[", GLFW.GLFW_KEY_LEFT_BRACKET, false);
        put("]", GLFW.GLFW_KEY_RIGHT_BRACKET, false);
        put("\\", GLFW.GLFW_KEY_BACKSLASH, false);
        put(";", GLFW.GLFW_KEY_SEMICOLON, false);
        put("'", GLFW.GLFW_KEY_APOSTROPHE, false);
        put(",", GLFW.GLFW_KEY_COMMA, false);
        put(".", GLFW.GLFW_KEY_PERIOD, false);
        put("/", GLFW.GLFW_KEY_SLASH, false);
        put("NDOT", GLFW.GLFW_KEY_KP_DECIMAL, false);
        put("NENTER", GLFW.GLFW_KEY_KP_ENTER, false);
        put("NADD", GLFW.GLFW_KEY_KP_ADD, false);
        put("NSUB", GLFW.GLFW_KEY_KP_SUBTRACT, false);
        put("NMUL", GLFW.GLFW_KEY_KP_MULTIPLY, false);
        put("NDIV", GLFW.GLFW_KEY_KP_DIVIDE, false);
        put("LMB", GLFW.GLFW_MOUSE_BUTTON_LEFT, true);
        put("RMB", GLFW.GLFW_MOUSE_BUTTON_RIGHT, true);
        put("MMB", GLFW.GLFW_MOUSE_BUTTON_MIDDLE, true);
        put("M4", GLFW.GLFW_MOUSE_BUTTON_4, true);
        put("M5", GLFW.GLFW_MOUSE_BUTTON_5, true);
    }

    private Keys() {}

    private static void put(String name, int code, boolean mouse) {
        MAP.put(name, new Key(code, mouse));
    }

    /** Returns the key for a token, or null if the token is unknown. */
    public static Key resolve(String token) {
        return MAP.get(token.toUpperCase(Locale.ROOT));
    }
}

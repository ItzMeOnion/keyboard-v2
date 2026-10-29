package dev.keyboardoverlay.input;

/** One key on the board. Positions and sizes are in key units (1.0 = one standard key). */
public record KeyDef(String label, int code, boolean mouse, float x, float y, float w, float h) {}

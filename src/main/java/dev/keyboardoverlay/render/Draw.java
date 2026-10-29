package dev.keyboardoverlay.render;

import net.minecraft.client.gui.DrawContext;

/** Rounded rectangles and outlines built from one-pixel row fills. */
public final class Draw {

    private Draw() {}

    /** Horizontal inset for a row so that corners of radius r look round. */
    private static int inset(int row, int h, int r) {
        if (r <= 0) {
            return 0;
        }
        float dy;
        if (row < r) {
            dy = r - row - 0.5f;
        } else if (row >= h - r) {
            dy = row + 0.5f - (h - r);
        } else {
            return 0;
        }
        float dx = r - (float) Math.sqrt(Math.max(0f, (float) r * r - dy * dy));
        return Math.round(dx);
    }

    public static void fillRounded(DrawContext ctx, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0 || (color >>> 24) == 0) {
            return;
        }
        r = Math.min(r, Math.min(w, h) / 2);
        for (int row = 0; row < h; row++) {
            int in = inset(row, h, r);
            int x0 = x + in;
            int x1 = x + w - in;
            if (x1 > x0) {
                ctx.fill(x0, y + row, x1, y + row + 1, color);
            }
        }
    }

    /** Draws only the edge of a rounded rectangle, t pixels thick. */
    public static void ring(DrawContext ctx, int x, int y, int w, int h, int r, int t, int color) {
        if (w <= 0 || h <= 0 || t <= 0 || (color >>> 24) == 0) {
            return;
        }
        r = Math.min(r, Math.min(w, h) / 2);
        t = Math.min(t, Math.min(w, h) / 2);
        if (t <= 0) {
            return;
        }
        for (int row = 0; row < h; row++) {
            int in = inset(row, h, r);
            int x0 = x + in;
            int x1 = x + w - in;
            if (x1 <= x0) {
                continue;
            }
            if (row < t || row >= h - t) {
                ctx.fill(x0, y + row, x1, y + row + 1, color);
            } else {
                ctx.fill(x0, y + row, Math.min(x1, x0 + t), y + row + 1, color);
                ctx.fill(Math.max(x0, x1 - t), y + row, x1, y + row + 1, color);
            }
        }
    }
}

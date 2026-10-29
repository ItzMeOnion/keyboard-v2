package dev.keyboardoverlay.render;

import dev.keyboardoverlay.config.OverlayConfig;
import dev.keyboardoverlay.config.OverlayConfig.PulseStyle;
import dev.keyboardoverlay.gui.SettingsScreen;
import dev.keyboardoverlay.input.KeyDef;
import dev.keyboardoverlay.input.KeyTracker;
import dev.keyboardoverlay.input.Layouts;
import dev.keyboardoverlay.input.Layouts.Board;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;

import java.util.List;

public final class OverlayRenderer {

    private static final KeyTracker TRACKER = new KeyTracker();
    private static final long START_NS = System.nanoTime();

    private OverlayRenderer() {}

    public static void register() {
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.currentScreen instanceof SettingsScreen) {
                return; // the settings menu draws its own preview
            }
            OverlayConfig c = OverlayConfig.get();
            if (!c.enabled) {
                return;
            }
            if (c.hideInF3 && mc.options.hudHidden) {
                return;
            }
            if (c.hideWhenChatOpen && mc.currentScreen instanceof ChatScreen) {
                return;
            }
            Board board = Layouts.current(c);
            long now = System.nanoTime();
            TRACKER.update(mc.getWindow().getHandle(), board.keys(), now);

            float unit = c.keySize * c.scale;
            float gap = c.keyGap * c.scale;
            float[] size = boardSize(board, unit, gap);
            int sw = mc.getWindow().getScaledWidth();
            int sh = mc.getWindow().getScaledHeight();
            int ox = originX(c, sw, size[0]);
            int oy = originY(c, sh, size[1]);
            drawBoard(ctx, c, board, ox, oy, unit, gap, now);
        });
    }

    /** Polls keys for a board. The HUD does this each frame; the settings preview calls it too. */
    public static void pollInput(Board board) {
        MinecraftClient mc = MinecraftClient.getInstance();
        TRACKER.update(mc.getWindow().getHandle(), board.keys(), System.nanoTime());
    }

    /** Pixel size of the whole board. */
    public static float[] boardSize(Board board, float unit, float gap) {
        float stride = unit + gap;
        float w = Math.max(0f, board.width() * stride - gap);
        float h = Math.max(0f, board.height() * stride - gap);
        return new float[] {w, h};
    }

    private static int originX(OverlayConfig c, int screenW, float boardW) {
        int column = c.anchor.ordinal() % 3; // 0 left, 1 centre, 2 right
        return switch (column) {
            case 0 -> c.offsetX;
            case 1 -> (int) ((screenW - boardW) / 2f) + c.offsetX;
            default -> (int) (screenW - boardW) - c.offsetX;
        };
    }

    private static int originY(OverlayConfig c, int screenH, float boardH) {
        int row = c.anchor.ordinal() / 3; // 0 top, 1 middle, 2 bottom
        return switch (row) {
            case 0 -> c.offsetY;
            case 1 -> (int) ((screenH - boardH) / 2f) + c.offsetY;
            default -> (int) (screenH - boardH) - c.offsetY;
        };
    }

    /**
     * How strongly a key is pulsing, 0..1. While held (with hold glow on) it stays at 1.
     * Otherwise it decays from the moment of the press over the pulse duration.
     */
    static float pulseAmount(OverlayConfig c, KeyTracker.State s, long nowNs) {
        if (s.down && c.holdGlow) {
            return 1f;
        }
        if (!c.pulseEnabled || s.pressNs < 0) {
            return 0f;
        }
        float t = (float) ((nowNs - s.pressNs) / 1_000_000.0) / c.pulseDurationMs;
        t = Math.max(0f, Math.min(1f, t));
        if (c.pulseStyle == PulseStyle.FLASH) {
            return t < 1f ? 1f : 0f;
        }
        return Easing.apply(c.easing, 1f - t);
    }

    /** Draws the whole board at (ox, oy). Used by the HUD and by the settings preview. */
    public static void drawBoard(DrawContext ctx, OverlayConfig c, Board board, float ox, float oy,
                                 float unit, float gap, long nowNs) {
        Palette p = Palette.of(c);
        float stride = unit + gap;
        float bw = Math.max(1f, board.width());
        float bh = Math.max(1f, board.height());
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        double t = (nowNs - START_NS) / 1e9;
        List<KeyDef> keys = board.keys();

        for (int i = 0; i < keys.size(); i++) {
            KeyDef k = keys.get(i);
            KeyTracker.State s = TRACKER.state(i);
            float amt = pulseAmount(c, s, nowNs);
            if (c.hideIdleKeys && !s.down && amt <= 0f) {
                continue;
            }

            float kx = ox + k.x() * stride;
            float ky = oy + k.y() * stride;
            float kw = k.w() * stride - gap;
            float kh = k.h() * stride - gap;

            boolean pulsing = c.pulseEnabled && amt > 0f;
            if (pulsing && c.pulseGrow > 0f) {
                float g = amt * c.pulseGrow * c.scale;
                kx -= g;
                ky -= g;
                kw += 2 * g;
                kh += 2 * g;
            }
            if (pulsing && c.pulseStyle == PulseStyle.SHRINK) {
                float sh = amt * 2f * c.scale;
                kx += sh;
                ky += sh;
                kw -= 2 * sh;
                kh -= 2 * sh;
            }

            int x0 = Math.round(kx);
            int y0 = Math.round(ky);
            int w = Math.max(1, Math.round(kx + kw) - x0);
            int h = Math.max(1, Math.round(ky + kh) - y0);
            int radius = Math.round(c.cornerRadius * c.scale);

            float nx = (k.x() + k.w() / 2f) / bw;
            float ny = (k.y() + 0.5f) / bh;
            boolean rgbOn = c.rgbMode != OverlayConfig.RgbMode.OFF;

            // base fill
            int fillCol = scaleAlpha(p.fill(), c.idleOpacity * c.fillOpacity);
            if (c.useGradient && !rgbOn) {
                fillCol = scaleAlpha(Palette.blendArgb(p.fill(), c.gradientEndColor, ny), c.idleOpacity * c.fillOpacity);
            }
            if (rgbOn) {
                fillCol = scaleAlpha(RgbEngine.color(c, nx, ny, i, t, p.fill()), c.idleOpacity * c.fillOpacity);
            }

            // glow behind the key (bloom and spread)
            int glowCol = c.glowColor;
            if (rgbOn && c.rgbAffectsGlow) {
                glowCol = RgbEngine.color(c, nx, ny, i, t, c.glowColor);
            }
            if (pulsing && (c.pulseStyle == PulseStyle.BLOOM || c.pulseSpread)) {
                int layers = c.pulseStyle == PulseStyle.BLOOM ? 4 : Math.max(1, c.pulseSpreadPx);
                float strength = amt * c.pulseIntensity * 0.12f;
                for (int l = 1; l <= layers; l++) {
                    int col = scaleAlpha(glowCol, strength);
                    Draw.fillRounded(ctx, x0 - l, y0 - l, w + 2 * l, h + 2 * l, radius + l, col);
                }
            }

            Draw.fillRounded(ctx, x0, y0, w, h, radius, fillCol);

            // pulse overlay
            if (pulsing) {
                float flash = Math.min(1f, amt * c.pulseIntensity);
                switch (c.pulseStyle) {
                    case FADE, FLASH, BLOOM, SHRINK ->
                            Draw.fillRounded(ctx, x0, y0, w, h, radius, scaleAlpha(glowCol, flash * 0.85f));
                    case RING -> {
                        int grow = Math.round((1f - amt) * 8f * c.scale);
                        Draw.ring(ctx, x0 - grow, y0 - grow, w + 2 * grow, h + 2 * grow,
                                radius + grow, Math.max(1, Math.round(2 * c.scale)),
                                scaleAlpha(glowCol, flash));
                    }
                }
            }

            // border
            int borderCol = c.borderColor;
            if (p.border() != 0) {
                borderCol = p.border();
            }
            if (rgbOn && c.rgbAffectsBorder) {
                borderCol = RgbEngine.color(c, nx, ny, i, t, p.border());
            }
            int bt = Math.max(0, Math.round(p.borderThickness() * c.scale));
            Draw.ring(ctx, x0, y0, w, h, radius, bt, scaleAlpha(borderCol, c.idleOpacity));

            // label
            if (c.showLabels) {
                int textCol = pulsing && amt > 0.5f ? c.pressedTextColor : p.text();
                if (rgbOn && c.rgbAffectsText) {
                    textCol = RgbEngine.color(c, nx, ny, i, t, textCol);
                }
                String label = c.uppercaseLabels ? k.label() : k.label().toLowerCase();
                int tw = tr.getWidth(label);
                int tx = x0 + (w - tw) / 2;
                int ty = y0 + (h - tr.fontHeight) / 2 + 1;
                ctx.drawText(tr, label, tx, ty, textCol, p.shadow() && c.labelShadow);
            }
        }
    }

    private static int scaleAlpha(int argb, float factor) {
        int a = Math.max(0, Math.min(255, Math.round(((argb >>> 24) & 0xFF) * factor)));
        return (a << 24) | (argb & 0xFFFFFF);
    }
}

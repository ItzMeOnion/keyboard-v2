package dev.keyboardoverlay.gui;

import dev.keyboardoverlay.config.OverlayConfig;
import dev.keyboardoverlay.input.Layouts;
import dev.keyboardoverlay.render.OverlayRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

/** The full settings menu. Changes apply live and are saved when the menu closes. */
public final class SettingsScreen extends Screen {

    private static final String[] TABS = {"General", "Layout", "Colours", "Pulse", "RGB", "Presets"};
    private static final String[] COLOUR_NAMES = {
            "Fill", "Border", "Text", "Glow", "Pressed text", "Gradient end", "RGB A", "RGB B"
    };

    private final Screen parent;
    private int tab = 0;
    private int colourIndex = 0;   // which colour the Colours tab is editing
    private int rgbColourIndex = 6; // which colour the RGB tab is editing (6 or 7)
    private int slot = 0;
    private int leftX;
    private int rightX;

    public SettingsScreen(Screen parent) {
        super(Text.literal("Keyboard Overlay"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        rebuild();
    }

    private void rebuild() {
        clearChildren();
        slot = 0;
        int tabW = 72;
        int startX = width / 2 - (TABS.length * tabW) / 2;
        for (int i = 0; i < TABS.length; i++) {
            final int idx = i;
            addDrawableChild(ButtonWidget.builder(Text.literal(TABS[i]), b -> {
                tab = idx;
                rebuild();
            }).dimensions(startX + i * tabW, 26, tabW - 4, 18).build());
        }

        leftX = width / 2 - 205;
        rightX = width / 2 + 5;

        switch (tab) {
            case 0 -> buildGeneral();
            case 1 -> buildLayout();
            case 2 -> buildColours();
            case 3 -> buildPulse();
            case 4 -> buildRgb();
            case 5 -> buildPresets();
            default -> {
            }
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Reset all"), b -> {
            OverlayConfig.reset();
            rebuild();
        }).dimensions(width / 2 - 205, height - 26, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(width / 2 + 105, height - 26, 100, 20).build());
    }

    // ---- tab builders ----

    private void buildGeneral() {
        OverlayConfig c = OverlayConfig.get();
        toggle("Enabled", () -> c.enabled, v -> c.enabled = v);
        toggle("Hide when F3 open", () -> c.hideInF3, v -> c.hideInF3 = v);
        toggle("Hide in chat", () -> c.hideWhenChatOpen, v -> c.hideWhenChatOpen = v);
        toggle("Show labels", () -> c.showLabels, v -> c.showLabels = v);
        toggle("Label shadow", () -> c.labelShadow, v -> c.labelShadow = v);
        toggle("Uppercase labels", () -> c.uppercaseLabels, v -> c.uppercaseLabels = v);
        toggle("Hide idle keys", () -> c.hideIdleKeys, v -> c.hideIdleKeys = v);
        cycle("Design", () -> c.design.name(), () -> c.design = next(c.design));
        cycle("Anchor", () -> c.anchor.name(), () -> c.anchor = next(c.anchor));
    }

    private void buildLayout() {
        OverlayConfig c = OverlayConfig.get();
        slider("Scale", 0.25, 3.0, () -> c.scale, v -> c.scale = (float) v, false);
        slider("Key size", 6, 64, () -> c.keySize, v -> c.keySize = (float) v, true);
        slider("Key gap", 0, 16, () -> c.keyGap, v -> c.keyGap = (float) v, true);
        slider("Corner radius", 0, 20, () -> c.cornerRadius, v -> c.cornerRadius = (float) v, true);
        slider("Label scale", 0.3, 2.0, () -> c.labelScale, v -> c.labelScale = (float) v, false);
        slider("Offset X", 0, 400, () -> c.offsetX, v -> c.offsetX = (int) Math.round(v), true);
        slider("Offset Y", 0, 400, () -> c.offsetY, v -> c.offsetY = (int) Math.round(v), true);
        slider("Idle opacity", 0, 1, () -> c.idleOpacity, v -> c.idleOpacity = (float) v, false);
        slider("Fill opacity", 0, 1, () -> c.fillOpacity, v -> c.fillOpacity = (float) v, false);
        slider("Border thickness", 0, 6, () -> c.borderThickness, v -> c.borderThickness = (int) Math.round(v), true);
    }

    private void buildColours() {
        OverlayConfig c = OverlayConfig.get();
        toggle("Override design colours", () -> c.overrideDesignColors, v -> c.overrideDesignColors = v);
        toggle("Gradient fill", () -> c.useGradient, v -> c.useGradient = v);
        colourEditor(0, 5, () -> colourIndex, v -> colourIndex = v);
    }

    private void buildPulse() {
        OverlayConfig c = OverlayConfig.get();
        toggle("Pulse on press", () -> c.pulseEnabled, v -> c.pulseEnabled = v);
        toggle("Hold glow while down", () -> c.holdGlow, v -> c.holdGlow = v);
        toggle("Glow spread", () -> c.pulseSpread, v -> c.pulseSpread = v);
        cycle("Pulse style", () -> c.pulseStyle.name(), () -> c.pulseStyle = next(c.pulseStyle));
        cycle("Easing", () -> c.easing.name(), () -> c.easing = next(c.easing));
        slider("Duration (ms)", 20, 2000, () -> c.pulseDurationMs, v -> c.pulseDurationMs = (int) Math.round(v), true);
        slider("Intensity", 0, 2, () -> c.pulseIntensity, v -> c.pulseIntensity = (float) v, false);
        slider("Grow (px)", 0, 10, () -> c.pulseGrow, v -> c.pulseGrow = (float) v, false);
        slider("Spread (px)", 0, 24, () -> c.pulseSpreadPx, v -> c.pulseSpreadPx = (int) Math.round(v), true);
    }

    private void buildRgb() {
        OverlayConfig c = OverlayConfig.get();
        cycle("RGB mode", () -> c.rgbMode.name(), () -> c.rgbMode = next(c.rgbMode));
        slider("Speed", 0, 10, () -> c.rgbSpeed, v -> c.rgbSpeed = (float) v, false);
        slider("Saturation", 0, 1, () -> c.rgbSaturation, v -> c.rgbSaturation = (float) v, false);
        slider("Brightness", 0, 1, () -> c.rgbBrightness, v -> c.rgbBrightness = (float) v, false);
        slider("Wave width", 0.05, 1, () -> c.rgbWaveWidth, v -> c.rgbWaveWidth = (float) v, false);
        toggle("Affects border", () -> c.rgbAffectsBorder, v -> c.rgbAffectsBorder = v);
        toggle("Affects text", () -> c.rgbAffectsText, v -> c.rgbAffectsText = v);
        toggle("Affects glow", () -> c.rgbAffectsGlow, v -> c.rgbAffectsGlow = v);
        colourEditor(6, 7, () -> rgbColourIndex, v -> rgbColourIndex = v);
    }

    private void buildPresets() {
        OverlayConfig c = OverlayConfig.get();
        cycle("Preset", () -> c.preset.name(), () -> c.preset = next(c.preset));
        int[] pos = nextSlot();
        TextFieldWidget field = new TextFieldWidget(textRenderer, leftX, 44 + 2 * 24 + 6, 410, 20,
                Text.literal("Custom layout"));
        field.setMaxLength(300);
        field.setText(c.customLayout);
        field.setChangedListener(s -> c.customLayout = s);
        addDrawableChild(field);
    }

    // ---- widget helpers ----

    private int[] nextSlot() {
        int col = slot % 2;
        int row = slot / 2;
        slot++;
        return new int[] {col == 0 ? leftX : rightX, 44 + row * 24};
    }

    private void toggle(String label, Supplier<Boolean> get, java.util.function.Consumer<Boolean> set) {
        int[] pos = nextSlot();
        addDrawableChild(ButtonWidget.builder(Text.literal(label + ": " + (get.get() ? "On" : "Off")), b -> {
            set.accept(!get.get());
            OverlayConfig.get().clamp();
            rebuild();
        }).dimensions(pos[0], pos[1], 200, 20).build());
    }

    private void cycle(String label, Supplier<String> value, Runnable next) {
        int[] pos = nextSlot();
        addDrawableChild(ButtonWidget.builder(Text.literal(label + ": " + value.get()), b -> {
            next.run();
            OverlayConfig.get().clamp();
            rebuild();
        }).dimensions(pos[0], pos[1], 200, 20).build());
    }

    private void slider(String label, double min, double max, DoubleSupplier get, DoubleConsumer set, boolean integer) {
        int[] pos = nextSlot();
        addDrawableChild(new Slider(pos[0], pos[1], 200, 20, label, min, max, get, v -> {
            set.accept(v);
            OverlayConfig.get().clamp();
        }, integer));
    }

    /** Lets the user pick one of a set of colours and edit its A, R, G, B channels. */
    private void colourEditor(int firstIndex, int lastIndex, Supplier<Integer> getIdx, java.util.function.IntConsumer setIdx) {
        int[] pos = nextSlot();
        int idx = getIdx.get();
        addDrawableChild(ButtonWidget.builder(Text.literal("Editing: " + COLOUR_NAMES[idx]), b -> {
            int n = idx + 1;
            if (n > lastIndex) {
                n = firstIndex;
            }
            setIdx.accept(n);
            rebuild();
        }).dimensions(pos[0], pos[1], 200, 20).build());

        String[] channels = {"Alpha", "Red", "Green", "Blue"};
        int shift = 24;
        for (int ch = 0; ch < 4; ch++) {
            final int sh = shift - ch * 8;
            int[] p = nextSlot();
            addDrawableChild(new Slider(p[0], p[1], 200, 20, channels[ch], 0, 255,
                    () -> (getColour(idx) >>> sh) & 0xFF,
                    v -> setColour(idx, (getColour(idx) & ~(0xFF << sh)) | ((((int) Math.round(v)) & 0xFF) << sh)),
                    true));
        }
    }

    private static int getColour(int i) {
        OverlayConfig c = OverlayConfig.get();
        return switch (i) {
            case 0 -> c.baseColor;
            case 1 -> c.borderColor;
            case 2 -> c.baseTextColor;
            case 3 -> c.glowColor;
            case 4 -> c.pressedTextColor;
            case 5 -> c.gradientEndColor;
            case 6 -> c.rgbColorA;
            default -> c.rgbColorB;
        };
    }

    private static void setColour(int i, int v) {
        OverlayConfig c = OverlayConfig.get();
        switch (i) {
            case 0 -> c.baseColor = v;
            case 1 -> c.borderColor = v;
            case 2 -> c.baseTextColor = v;
            case 3 -> c.glowColor = v;
            case 4 -> c.pressedTextColor = v;
            case 5 -> c.gradientEndColor = v;
            case 6 -> c.rgbColorA = v;
            default -> c.rgbColorB = v;
        }
    }

    private static <E extends Enum<E>> E next(E current) {
        E[] values = current.getDeclaringClass().getEnumConstants();
        return values[(current.ordinal() + 1) % values.length];
    }

    // ---- rendering ----

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 8, 0xFFFFFFFF);

        OverlayConfig c = OverlayConfig.get();
        Layouts.Board board = Layouts.current(c);
        OverlayRenderer.pollInput(board);
        float unit = c.keySize * c.scale;
        float gap = c.keyGap * c.scale;
        float[] size = OverlayRenderer.boardSize(board, unit, gap);
        float ox = width / 2f - size[0] / 2f;
        float oy = height - 60 - size[1];
        OverlayRenderer.drawBoard(context, c, board, ox, oy, unit, gap, System.nanoTime());
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Preview"), width / 2, (int) (oy - 12), 0xFFAAAAAA);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }

    @Override
    public void removed() {
        OverlayConfig.get().clamp();
        OverlayConfig.save();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    /** A slider bound to a numeric setting. */
    private static final class Slider extends SliderWidget {
        private final String label;
        private final double min;
        private final double max;
        private final DoubleSupplier get;
        private final DoubleConsumer set;
        private final boolean integer;

        Slider(int x, int y, int w, int h, String label, double min, double max,
               DoubleSupplier get, DoubleConsumer set, boolean integer) {
            super(x, y, w, h, Text.empty(), normalise(get.getAsDouble(), min, max));
            this.label = label;
            this.min = min;
            this.max = max;
            this.get = get;
            this.set = set;
            this.integer = integer;
            updateMessage();
        }

        private static double normalise(double v, double min, double max) {
            return max == min ? 0 : Math.max(0, Math.min(1, (v - min) / (max - min)));
        }

        @Override
        protected void updateMessage() {
            if (label == null) {
                return;
            }
            double v = get == null ? min : get.getAsDouble();
            String shown = integer ? String.valueOf(Math.round(v)) : String.format("%.2f", v);
            setMessage(Text.literal(label + ": " + shown));
        }

        @Override
        protected void applyValue() {
            double v = min + value * (max - min);
            set.accept(integer ? Math.round(v) : v);
            updateMessage();
        }
    }
}

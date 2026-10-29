package dev.keyboardoverlay;

import dev.keyboardoverlay.config.OverlayConfig;
import dev.keyboardoverlay.gui.SettingsScreen;
import dev.keyboardoverlay.render.OverlayRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class KeyboardOverlayMod implements ClientModInitializer {

    public static final String MOD_ID = "keyboard-overlay";
    private static final String CATEGORY = "category.keyboard-overlay";

    private static KeyBinding menuKey;

    @Override
    public void onInitializeClient() {
        OverlayConfig.load();
        OverlayRenderer.register();

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.keyboard-overlay.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) {
                client.setScreen(new SettingsScreen(client.currentScreen));
            }
        });
    }
}

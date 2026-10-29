package dev.keyboardoverlay.input;

import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/** Polls the real key and mouse state and remembers when each key was last pressed. */
public final class KeyTracker {

    public static final class State {
        public boolean down;
        public long pressNs = -1L;
        public long releaseNs = -1L;
    }

    private State[] states = new State[0];

    public State state(int index) {
        return index >= 0 && index < states.length ? states[index] : new State();
    }

    public void update(long windowHandle, List<KeyDef> keys, long nowNs) {
        if (states.length != keys.size()) {
            states = new State[keys.size()];
            for (int i = 0; i < states.length; i++) {
                states[i] = new State();
            }
        }
        for (int i = 0; i < keys.size(); i++) {
            KeyDef k = keys.get(i);
            boolean now = k.mouse()
                    ? GLFW.glfwGetMouseButton(windowHandle, k.code()) == GLFW.GLFW_PRESS
                    : InputUtil.isKeyPressed(windowHandle, k.code());
            State s = states[i];
            if (now && !s.down) {
                s.pressNs = nowNs;
            } else if (!now && s.down) {
                s.releaseNs = nowNs;
            }
            s.down = now;
        }
    }
}

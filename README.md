# Keyboard Overlay

A fully customisable on-screen keyboard overlay for Minecraft Java 1.21.8 (Fabric).

## Features
- **Presets:** WASD, WASD + Space, WASD + mouse, WASD with modifiers, arrows, full keyboard, full keyboard + mouse, numpad, mouse only, and a custom layout
- **Custom layout:** type your own board. Rows are separated by `|`, keys by spaces, and `KEY:1.5` sets a width. `_` is an empty gap. Example: `Q W E|A S D|LSHIFT:1.5 SPACE:3`
- **10 designs:** Modern, Minimal, Neon, Pixel, Retro, Glass, Outline, Cyber, Candy, Mono
- **Pulse on press:** style (fade, flash, bloom, ring, shrink), duration, intensity, easing, grow, glow spread, hold glow
- **RGB:** off, rainbow, wave, per-key random, breathe, static, gradient, with speed, saturation, brightness and which parts it affects
- **Layout:** scale, key size, gap, corner radius, label scale, anchor (9 points) and X/Y offsets
- **Colours:** per-colour alpha, red, green and blue sliders, gradient fill, opacity controls
- Settings are saved to `config/keyboard-overlay.json`

## Controls
- Press **K** to open the settings menu (rebind under Options > Controls > Keyboard Overlay)
- The menu has a live preview at the bottom of the screen

## Build
Requires Java 21. Gradle 8.14 is used.

    gradle build

The jar appears in `build/libs/`. A GitHub Actions workflow builds it on every push and uploads it as an artifact.

## Requirements
- Minecraft 1.21.8
- Fabric Loader 0.17.2+
- Fabric API (version in `gradle.properties`)

## Notes
- Labels are drawn at a fixed font size. Scale changes the keys, not the text.
- Built from a code review only so far. Run the build once and report any compile errors.

# PulseAutoColor

> Work in Progress ⚠️

PulseAutoColor is an LSPosed module (LibXposed API 102) for Evolution X that automatically changes the Pulse visualizer color based on the dominant color of the currently playing album artwork.

Compatibility with other ROMs has not been tested.

## Requirements

- Evolution X
- LSPosed with LibXposed API 102
- Root access
- Pulse visualizer enabled

## How It Works

1. Hooks `NotificationMediaManager` inside `SystemUI`.
2. Detects media metadata updates.
3. Retrieves the current album artwork.
4. Extracts the dominant color using the Android Palette API.
5. Writes the color to `Settings.Secure.pulse_color_user`.
6. Evolution X updates the Pulse visualizer with the new color.

<a id="readme-top"></a>

[![GitHub Release][release-shield]][release-url]
<img src="https://img.shields.io/badge/Android-10%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android 10+">
<img src="https://img.shields.io/badge/libxposed-API_102-ff69b4?style=for-the-badge" alt="libxposed API 102">
[![Issues][issues-shield]][issues-url]
[![License][license-shield]][license-url]
  
<div align="center">

# PulseAutoColor

An LSPosed module that automatically changes Evolution X Pulse and Battery Bar colors based on the currently playing album artwork.

<img src="screenshots/pulse-red.png" width="300">
<img src="screenshots/pulse-blue.png" width="300">

</div>

<details>
<summary>Table of Contents</summary>

- [About](#about)
- [Requirements](#requirements)
- [How It Works](#how-it-works)
- [Downloads](#downloads)
- [Installation](#installation)
- [License](#license)

</details>

## About

PulseAutoColor is an LSPosed module designed for Evolution X users who want a more dynamic and personalized system UI. The module hooks into the system's notification media manager to detect when music changes and automatically extracts the dominant color from the album artwork.

The extracted color is then applied to both the Pulse visualizer and the Battery Bar, creating a consistent color theme throughout the system UI that changes automatically with your music.

This creates a seamless, visually immersive experience where your music's color palette is reflected in the system UI. Supports **LibXposed API 102** and requires root access via a compatible Xposed framework.

## Requirements

- **Evolution X** ROM
- **Android 10 (API 29)** or higher
- **LSPosed** with LibXposed API 102 or higher
- **Root access**
- **Pulse** with Color set to Custom enabled in settings

## How It Works

1. Hooks `NotificationMediaManager` inside `com.android.systemui`
2. Detects media metadata updates and album artwork changes
3. Retrieves the current album artwork from media notifications
4. Extracts the dominant color using the Android Palette API
5. Writes the color to `Settings.Secure.pulse_color_user` and `Settings.System.statusbar_battery_bar_color`
6. Evolution X updates the Pulse visualizer and Battery Bar with the new color in real time

## Downloads
Download the latest version from the GitHub Releases page:

**Latest Release:** [v1.1.0](../../releases/tag/v1.1.0) — Includes automatic Battery Bar color support

**Previous Release:** [v1.0.0](../../releases/tag/v1.0.0) — Pulse color support only

## Installation

1. Download the latest PulseAutoColor module from the releases page.
2. Enable the module in LSPosed Manager and grant `com.android.systemui` permissions.
3. Make sure you have enabled Pulse in Evolver and set the Color option to Custom.
4. Reboot your device for the changes to take effect.

> [!NOTE]
> This module is designed for Evolution X. Compatibility with other ROMs has not been tested and is not guaranteed. Ensure LSPosed or Vector with API 102 is properly installed and Pulse visualizer is enabled in your system settings.
> 
> **After rebooting your device, the Pulse visualizer may only start working after changing the system volume.** This is normal behavior and is not related to the module. To verify that the module is working, play music with a different album cover and check whether the Pulse and Battery Bar colors change automatically.

## License

This project is licensed under the **MIT License**.

See the [LICENSE](LICENSE) file for the full license text.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

[release-shield]: https://img.shields.io/github/v/release/v3ndable/PulseAutoColor.svg?style=for-the-badge
[release-url]: https://github.com/v3ndable/PulseAutoColor/releases

[issues-shield]: https://img.shields.io/github/issues/v3ndable/PulseAutoColor.svg?style=for-the-badge
[issues-url]: https://github.com/v3ndable/PulseAutoColor/issues

[license-shield]: https://img.shields.io/github/license/v3ndable/PulseAutoColor.svg?style=for-the-badge
[license-url]: https://github.com/v3ndable/PulseAutoColor/blob/main/LICENSE

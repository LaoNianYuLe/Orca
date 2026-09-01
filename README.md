# Orca

Orca is a customized, GPLv3-licensed derivative of [OpenMinis](https://github.com/OpenMinis/OpenMinis).
It keeps the original on-device AI agent foundation while adding a customized Android
experience, Orca branding, a mobile left sidebar, project and copywriting entry points,
and theme/startup behavior adapted for this build.

> This is an independent community build. It is not an official OpenMinis release.

## Features

- Bring your own model through supported providers and API keys
- On-device Alpine Linux sandbox with shell, files, and scripts
- Browser automation, skills, memory, and device integrations
- Android settings for providers, model groups, appearance, skills, memory, storage, and MCP
- ChatGPT-style mobile left sidebar
- Orca app name and custom Orca icon
- Light, dark, and system appearance modes
- First-install welcome flow when no chat history exists

## Build Android

Requirements: Android SDK, JDK 17, and Android NDK r28 or newer.

```bash
cd src/android
./gradlew :app:assembleDebug
```

The debug APK is generated at:

```text
src/android/app/build/outputs/apk/debug/app-debug.apk
```

## License and attribution

Orca is distributed under the [GNU General Public License v3.0](LICENSE).
The original OpenMinis copyright and license notices remain applicable to the
code derived from that project. This fork was modified by Kelvin on 2026-09-01.

The project includes GPL, LGPL, Apache-2.0, MIT, BSD, and other third-party
components. Their license notices are listed in
[THIRD_PARTY_LICENSES.md](THIRD_PARTY_LICENSES.md) and must be retained when
redistributing the project.

For the upstream project, source history, and original documentation, see
[OpenMinis/OpenMinis](https://github.com/OpenMinis/OpenMinis).

## Development status

The Android debug build has been built and installed on an emulator. The focused
regression tests for the customized startup and theme behavior pass. Some legacy
provider, terminal-sanitizer, and speech-correction tests remain to be reconciled
with the current upstream code and test fixtures.

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

## Current test release

The current public Android package is a **test release**, not a stable release.
It is intended for emulator and device testing of the Orca branding, mobile
sidebar, settings navigation, first-install welcome flow, and light/dark theme
behavior.

- App version: `1.12-test.3` (version code `26`)
- Package type: Android debug APK
- Architecture: `arm64-v8a`
- Test scope: Android; iOS packaging is not included in this release
- Known limitation: some legacy provider, terminal-sanitizer, and
  speech-correction tests still need reconciliation with the current fixtures

Install the APK only on a device or emulator you control. Remove an older
debug build first if Android reports a signature or downgrade conflict. Do not
use this package as a production release or enter credentials on a device you
do not trust.

The APK checksum is published in the GitHub release notes so the downloaded
file can be verified before installation.

Download: [Orca-v1.12-test.3-arm64-debug.apk](https://github.com/LaoNianYuLe/Orca/releases/download/v1.12-test.3/Orca-v1.12-test.3-arm64-debug.apk)

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

## First-run setup

1. Install the test APK or build it locally.
2. Open Orca and complete the welcome flow.
3. Open Settings → Providers and add your own provider endpoint and API key.
4. Select a model group, then start a new conversation.

Orca does not provide a shared API account. Provider fees, rate limits, and
terms are controlled by the provider account you configure. Never put a real
API key into source files or commit it to Git.

## Source and distribution

This repository contains the source used to build Orca, including the Android
and iOS code, build scripts, submodule references, tests, and required license
notices. Generated build directories, APK/AAB packages, local SDK settings,
signing keys, API keys, OAuth tokens, and other personal configuration are not
part of the source release.

Anyone building Orca must provide their own provider credentials in the app or
in the ignored local customization files. The repository does not contain a
shared API account or a bundled API key. The `models-dev-api.json` file is
public model metadata, not an authentication credential.

If an APK is distributed, the corresponding modified source must remain
available from this repository under GPLv3. The source repository is the
required part of the release; the APK is provided as a convenience for testing.

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

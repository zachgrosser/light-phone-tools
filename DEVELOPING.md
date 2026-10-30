# Developing

Setup, the emulator, tests, and getting a build onto a phone. Written for an Apple silicon Mac; the emulator script hasn't been tried anywhere else.

## Setup

1. Install [Android Studio](https://developer.android.com/studio) and run its setup wizard with the Standard options.
2. On the welcome screen, choose **Clone Repository** and pick `light-phone-tools`. Let the Gradle sync finish.
3. Light's build needs Java 17 exactly, which Android Studio doesn't come with. Go to **Settings → Build, Execution, Deployment → Build Tools → Gradle**, open the **Gradle JDK** menu, choose **Download JDK**, pick **17** and download it.

If Android Studio offers the AGP Upgrade Assistant, close it. Light picks those versions.

## Emulator

Light's emulator runs inside an Android virtual phone and behaves like a Light Phone III.

Create the virtual phone once, in **Device Manager → + → Create Virtual Device**:

1. **Medium Phone** (the script reshapes the screen).
2. **API 34**, **Android Open Source** (never Google Play), **ARM 64 v8a**.
3. Name it `LightPhone`. Don't press its play button.

Then, in Android Studio's Terminal:

```bash
scripts/emulator.sh setup LightPhone
```

This takes 15 to 30 minutes. When it says **All set**, go to **Settings → Allowed Tools → All Tools** in the emulator so Wiki shows up.

After that:

```bash
scripts/emulator.sh start
```

```bash
scripts/emulator.sh install
```

To try the Light Flip's 480×640 screen, use `scripts/emulator.sh screen flip`, and `screen lp3` to go back. With the emulator window in front, the Mac's keyboard types like a Bluetooth keyboard would.

For layout work, `tool/src/main/kotlin/com/zachgrosser/wiki/WikiPreviews.kt` with **Split** view shows every screen at both sizes without the emulator.

## Tests

```bash
./gradlew :tool:testDebugUnitTest -DlightSdk.toolOnly=true
```

`-DlightSdk.toolOnly=true` skips Light's example tools, which saves a few minutes.

## Light's rules

Light's build server only takes `tool/build.gradle.kts`, `tool/lighttool.toml` and `tool/src/main`, and builds them against its own copy of the SDK. That copy can be older than the one in this repo: in October 2026 it was v0.2.0, and the first Wiki build failed because it used an API from v0.2.1. To check against an older SDK, build `tool/` in a `git worktree` of that tag:

```bash
./gradlew :tool:assembleRelease -DlightSdk.unsigned=true -DlightSdk.toolOnly=true -DlightSdk.abiFilters=arm64-v8a
```

Light's plugin also blocks some things in tool code at build time: `android.app`, `Context`, `Intent`, `LocalContext`, `LocalView`, `androidx.activity`, reflection and a few more (see `BLOCKED_IMPORTS` in `plugin/.../LightSdkPlugin.kt`). Dependencies have to be on Light's allow-list. Don't add an `AndroidManifest.xml` or set the app id or version in Gradle; the plugin takes those from `lighttool.toml`.

## GitHub checks

| Workflow | When | What |
|---|---|---|
| Tool (ours) | Pushes that change `tool/` | Tests, a release build like Light's, and downloadable APKs |
| PR Check (Light's) | Every pull request | Builds and checks the whole SDK. About 12 minutes |

## On a real phone

Each Tool run on GitHub (**Actions → Tool**) has an **Artifacts** zip with a debug and a release APK. Test the release one before submitting; it's built the way Light builds it. Both are signed with the SDK's public dev key, so the phone needs **Settings → Developer → Allowed tools → All tools**.

LightOS doesn't have an official way to install your own APK yet. People use the File Manager's Tool Inbox ([unofficial installer](https://github.com/yarosz/lightphone-wifi-install)) or `adb install` with USB debugging. Only install APKs you trust.

## Updating Light's SDK

```bash
git checkout main && git pull
```

```bash
git checkout -b sdk-update
```

```bash
git fetch upstream && git merge upstream/main
```

```bash
git push -u origin sdk-update
```

If you don't have the `upstream` remote yet: `git remote add upstream https://github.com/lightphone/light-sdk.git`.

Merge the pull request with **Create a merge commit**, not squash. Git needs Light's commits as they are, or the next update clashes with everything. We only touch `tool/`, `scripts/` and our docs, so these merges are usually clean.

# CLAUDE.md

Notes for Claude Code (and anyone new) working in this repo. Read this first; the guides it links to have the details.

## What this repo is

A copy of [Light's SDK](https://github.com/lightphone/light-sdk) with one tool of ours added: **Wiki**, a Wikipedia reader for the Light Phone III (and later the Light Flip), in `tool/`.

- **Ours, edit freely:** `tool/`, `scripts/`, `.github/README.md`, `.github/workflows/tool.yml`, `.github/pull_request_template.md`, `.github/ISSUE_TEMPLATE/`, `DEVELOPING.md`, `COLLABORATING.md`, `SUBMITTING.md`, this file.
- **Light's, leave alone:** everything else (`sdk/`, `plugin/`, `lint-rules/`, `builder/`, `examples/`, `docs/`, `README.md`, Light's workflows…). It's updated by merging Light's repo, and edits there cause clashes. Suggestions for the SDK go to Light, not here.
- How Wiki works, file by file, and how its credits meet Wikipedia's rules: [`tool/README.md`](tool/README.md).

Explain changes in plain words. When the person you're working with needs to do something themselves, give the exact commands or clicks.

## Commands

**Java 17 is required.** Light's plugin and lint rules use `jvmToolchain(17)`; newer JDKs fail ("Cannot find a Java installation matching languageVersion=17", or Gradle 9 refusing Java 25). Don't accept Android Studio's AGP Upgrade Assistant: Light picks those versions.

```bash
./gradlew :tool:testDebugUnitTest -DlightSdk.toolOnly=true      # unit tests
./gradlew :tool:assembleRelease -DlightSdk.toolOnly=true -DlightSdk.abiFilters=arm64-v8a   # as Light's server builds it (R8 on)
./gradlew :tool:assembleDebug -DlightSdk.toolOnly=true          # debug APK
```

`-DlightSdk.toolOnly=true` skips Light's example tools and saves minutes.

**Emulator** (macOS; see [DEVELOPING.md §2](DEVELOPING.md#2-lights-emulator)):

```bash
scripts/emulator.sh setup LightPhone   # once: API 34 "Android Open Source" ARM64 image, installs Light's emulator as a system app
scripts/emulator.sh start              # start the virtual phone in the mode Light's emulator needs (-writable-system)
scripts/emulator.sh install            # rebuild Wiki and install it
scripts/emulator.sh screen flip|lp3    # Light Flip (480×640) or Light Phone III (1080×1240) screen size
```

`install` temporarily switches `serverPackage` to the emulator's (`com.thelightphone.sdk.emulator`) and restores `com.lightos` afterwards. Wiki only shows up in the emulator's tool list with **Settings → Allowed Tools → All Tools**. With the emulator running, `adb` works as usual (`adb logcat`, `adb exec-out screencap -p > shot.png`, `adb shell input text …`).

Quickest layout check: the previews in `tool/src/main/kotlin/com/zachgrosser/wiki/WikiPreviews.kt` show every screen at both device sizes.

## Light's rules (the build fails, or Light rejects the tool, otherwise)

- **Light's build server only takes** `tool/build.gradle.kts`, `tool/lighttool.toml` and `tool/src/main/{kotlin,java,res,assets}`, then runs `:tool:assembleRelease -DlightSdk.unsigned=true -DlightSdk.abiFilters=arm64-v8a`. Nothing else in the repo ships. See `builder/README.md`.
- **The build server compiles against its own pinned SDK, not ours.** In October 2026 that was **v0.2.0**, older than the SDK merged here, and the first Wiki build failed on an API added later (`sealedActivity.vibrator`, from v0.2.1). Only use SDK APIs that exist in the server's version. To check, build `tool/` inside a `git worktree` of that tag (e.g. `v0.2.0`) with the command above.
- **Never write an `AndroidManifest.xml`**, and don't set `applicationId`, `versionCode`, `versionName` or `namespace` in `build.gradle.kts`: the plugin generates them from `lighttool.toml`. No `buildscript {}`, `apply(...)`, `resolutionStrategy` or custom source dirs.
- **Forbidden in tool code** (checked at build time; full list in `plugin/src/main/kotlin/com/thelightphone/plugin/LightSdkPlugin.kt`, `BLOCKED_IMPORTS` and `BLOCKED_CODE_PATTERNS`): `android.app.*`, `android.content.Context`/`Intent`, `LocalContext`, `LocalView`, `LocalLifecycleOwner`, `androidx.activity.*`, reflection (including any `.java.` chain), `getSystemService`, `startActivity`, casts to Activity/Context. Use the SDK instead: `navigateTo`/`goBack`, `lightContext.filesDir`, `LocalHapticsEnabled`. For vibration, use Compose's `LocalHapticFeedback` (it works on every SDK version); `sealedActivity.vibrator` isn't on Light's build server yet.
- **Dependencies** must be on Light's allow-list (`plugin/src/main/resources/com/thelightphone/plugin/allowed-dependencies.txt`). Only what `:sdk:client` exposes with `api` is on the compile classpath; for example `lifecycle-viewmodel-compose` isn't, so `HomeScreen` keeps the keyboard's view model with `ViewModelProvider(this, …)`.
- **`tool/lighttool.toml`:** `id = "com.zachgrosser.wiki"` can never change once submitted. `serverPackage` must be `com.lightos` when committed. Raise `versionCode` by one for every build sent to Light.
- Light's [TOOL_GUIDELINES.md](TOOL_GUIDELINES.md): intentional, private, no ads or tracking, open source, Light's design components.

## Careful areas in Wiki

- **`Attribution.kt` and the About screen:** every credit, license, trademark and privacy line. The wording follows CC BY-SA 4.0 and Wikimedia's Terms of Use, Trademark Policy, User-Agent Policy and Privacy Policy; the table in [`tool/README.md`](tool/README.md#attribution-and-wikipedias-rules) maps each line to its rule. Check the sources before changing any of it, and keep `AttributionTest` passing.
- **`WikiApi.kt`:** the User-Agent is `LightPhoneWiki/<version> (<repo URL>)`. Never put a personal email in it. On HTTP 429, tell the reader and don't retry.
- **`ArticleParser.kt`:** drops reference sections but keeps "This article incorporates text from…" notices (required). Run `ArticleParserTest` after any change.
- **Typing (`SearchInput.kt`, `MultiTap.kt`):** Light's on-screen keyboard types on key *release*; `HardwareTyping` intercepts hardware typing keys in `onPreviewKeyEvent` and must claim both key down and key up, or characters arrive twice. Non-typing keys (volume, the Light Phone III's wheel and shutter) must return `false` so LightOS gets them. `lightOsRemap` undoes LightOS's remapping of external keyboards.
- **Screen sizes:** no orientation lock (Android letterboxes portrait-locked apps on the near-square Light Phone III screen). Size things with the SDK's `gridUnitsAsDp()` and `designVerticalPxToSp()`, and check both sizes in `WikiPreviews.kt`.
- **Languages (`Language.kt`, `LanguageSetting.kt`):** one setting changes Wiki's text and the Wikipedia it uses. Every article, saved entry, search and link carries its language; saved English articles keep their old file names (`<pageId>.json`; others are `<lang>-<pageId>.json`). Credits and About's legal text stay in English on purpose. Only Latin-alphabet languages, because Light's keyboard has no other layouts. Add text to `UiStrings` for every language at once (`LanguageTest` checks none is blank). On non-English Wikipedias `ArticleParser` keeps reference sections, because it only recognises imported-text notices in English. Each Wikipedia names its navigation boxes differently (`OTHER_WIKIS_SKIPPED_CLASSES`).
- **Unit tests run on the JVM without Android.** Keep logic that needs testing free of `android.*` classes (as `MultiTap` and `ArticleParser` are).

## Open questions

- **Light Flip** (ships April 2027): Light hasn't published how its keypad reaches tools or whether it has a touchscreen. Wiki assumes a 12-key keypad that sends digits (multi-tap in `MultiTap.kt`). Without a touchscreen, results and links would need keypad navigation. Light's on-screen keyboard (`LightEmbeddedLp3Keyboard`) is built for the Light Phone III's width: at 480×640 its right-hand keys are cut off. Revisit when Light publishes Flip support.
- English is the default; Spanish, French, German, Italian, Portuguese, Dutch and Swedish are offered (since 1.1). Their translations haven't had a native speaker's review. Images and tables are left out on purpose.
- **Android's Back button skips `onBackPressed`** in Light's SDK (`LightActivity` calls its own `goBack()`, not the screen's), so system Back on About or the language list leaves Wiki instead of going back a step. The top bar's back arrow works. Tools can't use `BackHandler` (`androidx.activity` is blocked), so this needs fixing in the SDK.

## Git and GitHub

- Never commit to `main`. Work on a branch, open a pull request, and let both checks pass: **Tool** (ours: tests, release and debug builds, downloadable APKs) and **PR Check** (Light's, about 12 minutes).
- Normal pull requests: **Squash and merge**. Pull requests that bring in Light's SDK: **Create a merge commit**, never squash, or every later SDK update clashes. How to update: [DEVELOPING.md §6](DEVELOPING.md#6-getting-lights-latest-sdk).
- Releasing to Light's Tool Library: follow [SUBMITTING.md](SUBMITTING.md). Making the repo public is the owner's decision.
- Feedback and bugs: GitHub issues on this repo.

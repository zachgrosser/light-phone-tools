# light-phone-tools

Our tools (apps) for the **Light Phone III**, built on Light's official SDK.

This repo started as a copy of [lightphone/light-sdk](https://github.com/lightphone/light-sdk). **Our code lives in `tool/`.** Everything else comes from Light and gets updated from their repo. Light's own README is at [`/README.md`](../README.md). Read it once, because it explains how the SDK works.

> GitHub shows this file (`.github/README.md`) as the repo's front page instead of Light's README. Keeping ours here means updates from Light never clash with it.

- **New collaborator?** Read [COLLABORATING.md](../COLLABORATING.md) first.
- **Light's rules for tools:** [TOOL_GUIDELINES.md](../TOOL_GUIDELINES.md)

## Our tools

### Wiki
Search and read Wikipedia on the Light Phone. Articles are shown as plain text with tappable links, one section at a time, and can be starred to read offline. A start-up screen credits Wikipedia and its CC BY-SA 4.0 license, and every article ends with a link back to its source.

The code is in [`tool/src/main/kotlin/com/zachgrosser/wiki/`](../tool/src/main/kotlin/com/zachgrosser/wiki/):

| File | What it does |
|---|---|
| `HomeScreen.kt` | Start-up credits screen, search box, saved articles, About |
| `SearchScreen.kt` | Keyboard and search results |
| `ArticleScreen.kt` / `SectionScreen.kt` | Reading an article, and saving it |
| `WikiApi.kt` | Talks to Wikipedia |
| `ArticleParser.kt` | Turns Wikipedia's HTML into plain text, links and sections |
| `SavedArticles.kt` | Stores saved articles on the phone |

## Who's working on this

- [@zachgrosser](https://github.com/zachgrosser)
- *(add your name here in your first pull request)*

## Getting set up

1. Install [Android Studio](https://developer.android.com/studio), [git](https://git-scm.com/downloads) and, optionally, the [GitHub CLI](https://cli.github.com/) (`gh`).
2. Download a copy of this repo to your computer:
   ```bash
   gh repo clone zachgrosser/light-phone-tools
   # or, without gh:
   git clone https://github.com/zachgrosser/light-phone-tools.git
   ```
3. Open the `light-phone-tools` folder in Android Studio and let Gradle finish syncing.
4. Create a phone emulator shaped like the Light Phone III:
   - 1080 × 1240, 3.92" screen
   - Android API 34
   - **No** Google Play Services
5. For the most realistic testing, run Light's emulator app as a system app. The steps are in [docs/system_app](../docs/system_app).
6. Start editing `tool/src/main/kotlin/com/thelightphone/sample/HomeScreen.kt`.

## What's where

| Folder / file | What it is | Do we edit it? |
|---|---|---|
| `tool/` | **Our tool's code** | ✅ Yes |
| `tool/lighttool.toml` | Our tool's name, id, version and permissions | ✅ Yes |
| `examples/` | Light's sample tools (weather, authenticator, audio, UI demo) | 👀 Read for ideas |
| `docs/` | Light's documentation | 👀 Read |
| `sdk/`, `plugin/`, `lint-rules/`, `builder/`, `signer/` | The SDK itself | ❌ No, it comes from Light |

## Trying a tool on the LightOS emulator (Mac)

Light's emulator shows a tool the way a Light Phone III would, on your computer. Setup takes about an hour the first time, mostly downloads; after that it starts in a minute.

**1. Install Android Studio.** Download it from [developer.android.com/studio](https://developer.android.com/studio) (on an M-series Mac, pick the "Mac with Apple chip" version). Open it and run the setup wizard with the **Standard** options.

**2. Get Java 17.** Light's build tools need exactly Java 17, which Android Studio doesn't include. In Android Studio, open **Settings → Build, Execution, Deployment → Build Tools → Gradle**, click the **Gradle JDK** menu, choose **Download JDK**, pick version **17** (any vendor) and click **Download**. Keep it selected as the Gradle JDK.

**2b. Open this repo in Android Studio.** On the welcome screen, choose **Clone Repository**, sign in to GitHub, pick `light-phone-tools` and clone it. When it opens, wait for the progress bar at the bottom ("Gradle sync") to finish. To try a tool that's still in a pull request, switch to its branch: click the branch name at the top of the window, then choose the branch under **Remote**, then **Checkout**.

**3. Create a virtual phone.** Open **Device Manager** (the phone icon on the right edge) and click **+**, then **Create Virtual Device**:
- Pick any phone, for example **Medium Phone**. The script resizes the screen to the Light Phone III's later.
- For the system image, set **API** to **API 34** ("UpsideDownCake") and **Services** to **Android Open Source** (some versions call it **No Google APIs**; not Google Play). Pick **ARM 64 v8a System Image** and click its download arrow.
- Name it `LightPhone` and click **Finish**. **Don't press its play button**; the script starts it in the mode LightOS needs.

**4. Run the setup script.** Open the **Terminal** tab at the bottom of Android Studio and run:

```bash
scripts/emulator.sh setup LightPhone
```

It starts the virtual phone, installs Light's LightOS emulator into it as a system app, then builds and installs our tool. It stops with a plain explanation if something's wrong.

**5. Allow our tool.** In the LightOS emulator on the virtual phone, open **Settings → Allowed Tools** and choose **All Tools**. Our tools then appear in the tool list. They're signed with the SDK's test key, not by Light.

**Next time:** start the phone with `scripts/emulator.sh start`, and after changing code run `scripts/emulator.sh install` to rebuild and reinstall.

**Other screen sizes.** Light's devices don't all share one screen: the Light Phone III is 1080 × 1240 and the Light Flip (due April 2027) is 480 × 640. Two ways to check a layout on both:
- **Previews:** open `tool/src/main/kotlin/com/zachgrosser/wiki/WikiPreviews.kt` and click **Split** at the top right. Every screen appears at both sizes, with no emulator needed.
- **On the virtual phone:** `scripts/emulator.sh screen flip` switches its screen to the Flip's size; `scripts/emulator.sh screen lp3` switches back.

## Getting a tool onto a phone

Every push that changes `tool/` triggers the **Build APK** workflow on GitHub. Open the repo's **Actions** tab, pick the latest **Build APK** run, and download the file under **Artifacts**. It's a zip containing the installable `.apk`. You can also start a build by hand with **Run workflow**.

That APK is a test build signed with the SDK's public development key, so a phone only shows it when its **Allowed tools** setting is **All tools**. For a build signed by Light, submit the tool on the [dashboard](https://dashboard.thelightphone.com), which needs a public repo. Light's README covers the steps under "Submitting Your Tool".

## Getting Light's latest SDK changes

Light says the SDK is changing fast, so pull their updates often:

```bash
git remote add upstream https://github.com/lightphone/light-sdk.git   # first time only, per computer
git checkout main
git pull                      # get the latest changes from our repo
git fetch upstream            # download Light's latest
git merge upstream/main       # mix it into ours
git push                      # share the result with the team
```

Since we only edit `tool/`, these merges should almost never clash.

## Before we share a tool

- [ ] Double-check `id` in `tool/lighttool.toml` (Wiki uses `com.zachgrosser.wiki`). It can't change once the tool is published.
- [ ] Keep `serverPackage` in `tool/lighttool.toml` set to `com.lightos`. Light's build rejects anything else; only switch it while testing on the LightOS emulator.
- [ ] Check it against [TOOL_GUIDELINES.md](../TOOL_GUIDELINES.md): it should be intentional to use, with no tracking, no ads and no infinite feeds.
- [ ] **Make the repo public.** Light only signs tools it can build from a public git commit, so we keep this repo private while we build and open it up when we're ready.

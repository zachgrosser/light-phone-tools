# Submitting Wiki to Light

Light builds and signs community tools from a **public** GitHub repo, then reviews them for their Tool Library. Their steps are in [README.md, "Submitting Your Tool"](README.md#submitting-your-tool); this is our checklist around them.

## Before submitting

- [ ] **Merged into `main`.** Light reads the tool's package name from the default branch.
- [ ] **Latest Light SDK merged in.** Light requires it (see [DEVELOPING.md §6](DEVELOPING.md#6-getting-lights-latest-sdk)).
- [ ] **The Tool workflow is green on `main`**, especially the release build, which matches what Light's server runs.
- [ ] **Tried the release APK** on the emulator or a phone (DEVELOPING.md §5): search, open an article, follow a link, read sections, star it, read it offline, About.
- [ ] **Checked both screen sizes** in `WikiPreviews.kt` or with `scripts/emulator.sh screen flip`.
- [ ] **Version bumped** in `tool/lighttool.toml`: `versionCode` must be higher than the last build sent to Light; `versionName` is what people see (e.g. `1.0.0`).
- [ ] **`id` is `com.zachgrosser.wiki`** and `serverPackage` is `com.lightos`. The id can never change after the first submission.
- [ ] **About and credits reviewed** against [tool/README.md, "Attribution and Wikipedia's rules"](tool/README.md#attribution-and-wikipedias-rules).
- [ ] **Against Light's [Tool Guidelines](TOOL_GUIDELINES.md):** intentional (search-led; no feeds or notifications), private (no tracking; disclosed), no ads, open source (MIT), built with the SDK, Light's design components, works offline for saved articles, Wikipedia's license and trademark rules followed.
- [ ] **We can explain every change in our own words.** Light's team reviews each submitted build before approving it.

## Make the repo public

**Settings → General → Danger Zone → Change visibility → Public.** Anyone will be able to see the code and its history. It contains no keys or passwords: the signing key in `sdk/keys/` is Light's public development key, and `local.properties` is never committed. Once public, turn on branch protection for `main` (**Settings → Branches**), and the feedback link on Wiki's About screen starts working.

## On Light's dashboard

1. At [dashboard.thelightphone.com](https://dashboard.thelightphone.com): **Settings → Account → Developer Account**, turn developer mode on.
2. **Manage Custom Tools → Submit New Tool**, and give the repo: `https://github.com/zachgrosser/light-phone-tools`.
3. **Add at least one image.** Screenshots from the emulator work: the camera button on the emulator's side toolbar saves one. Good choices: the start-up screen, search results, an article, About.
4. On the tool's page, **Submit build** with:
   - **Git ref:** a tag such as `wiki-v1.0.0`, made on `main` (`git tag wiki-v1.0.0 && git push origin wiki-v1.0.0`), or a commit SHA.
   - **Changelog:** a sentence or two.
5. Wait for the build. If it fails, **View details** under Builds shows why; **Retry build** reruns the same ref. When it works, **Download APK** appears and the version waits for Light's approval.
6. Light emails with feedback. Questions: tools@thelightphone.com.

## Suggested listing text

> **Wiki** — Search and read Wikipedia, one section at a time. Follow links between articles and star them to read offline. Text from Wikipedia™, by its volunteer contributors, under CC BY-SA 4.0. No ads or tracking. Not affiliated with or endorsed by the Wikimedia Foundation.

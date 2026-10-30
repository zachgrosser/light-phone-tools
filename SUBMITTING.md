# Submitting to Light

Light builds and signs tools from a public GitHub repo and reviews each version before it goes in their Tool Library. Their steps are in [README.md](README.md#submitting-your-tool); this is our checklist.

## Before submitting

- [ ] Merged into `main`.
- [ ] Light's latest SDK merged in ([DEVELOPING.md](DEVELOPING.md#updating-lights-sdk)).
- [ ] Builds against the SDK version Light's server uses ([DEVELOPING.md](DEVELOPING.md#lights-rules)).
- [ ] The Tool check is green on `main`.
- [ ] Tried the release APK on the emulator or a phone: search, open an article, follow a link, read a section, star it, read it offline, change the language, About.
- [ ] Checked both screen sizes.
- [ ] `versionCode` in `tool/lighttool.toml` is higher than the last one on Light's dashboard, and `versionName` is what people should see.
- [ ] About and the credits still match [tool/README.md](tool/README.md#attribution-and-wikipedias-rules).
- [ ] Still fits Light's [Tool Guidelines](TOOL_GUIDELINES.md).

## On Light's dashboard

At [dashboard.thelightphone.com](https://dashboard.thelightphone.com):

1. The first time only: turn on **Settings → Account → Developer Account**, then **Manage Custom Tools → Submit New Tool** with `https://github.com/zachgrosser/light-phone-tools`, and add at least one screenshot.
2. Tag the release on `main` and push the tag, for example `git tag wiki-v1.1.0 && git push origin wiki-v1.1.0`.
3. On the tool's page, **Submit build** with the tag as the git ref and a line or two of changelog.
4. If the build fails, **View details** says why. Fix it and tag again; don't move a tag Light has already built.

Light emails with any feedback. Questions go to tools@thelightphone.com.

## Listing text

Light's description field takes up to 255 characters:

> Search and read Wikipedia a section at a time. Follow links and star articles to read offline. Text from Wikipedia™, by its volunteer contributors, under CC BY-SA 4.0. No ads or tracking. Not affiliated with or endorsed by the Wikimedia Foundation.

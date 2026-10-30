# light-phone-tools

Tools for the Light Phone III, built on [Light's SDK](https://github.com/lightphone/light-sdk).

## Wiki

Search and read Wikipedia on the Light Phone, one section at a time, with working links between articles. Star an article to keep it for offline reading. Available in eight languages.

No ads, analytics or tracking, and saved articles stay on the phone. Every article ends with its source and license. [`tool/README.md`](../tool/README.md) has the details, including how it follows Wikipedia's reuse rules.

## Docs

- [DEVELOPING.md](../DEVELOPING.md): setting up, the emulator, tests
- [COLLABORATING.md](../COLLABORATING.md): branches, pull requests, reviews
- [SUBMITTING.md](../SUBMITTING.md): sending a version to Light
- Light's [TOOL_GUIDELINES.md](../TOOL_GUIDELINES.md), [README](../README.md) and [docs](../docs)

## Layout

This is a copy of Light's SDK with Wiki added. Everything outside these is Light's and gets updated from their repo, so don't edit it here:

- `tool/`: Wiki
- `scripts/emulator.sh`: sets up and runs Light's emulator on a Mac
- `.github/`: this page, the Tool workflow, PR and issue templates
- `DEVELOPING.md`, `COLLABORATING.md`, `SUBMITTING.md`

Light's `examples/` folder is worth a look for how their own tools are put together.

This file sits in `.github/` so GitHub shows it instead of Light's README, and their updates never clash with it.

## Licenses

Wiki (`tool/`) is [MIT](../tool/LICENSE). Light's SDK is [MIT](../LICENSE), © The Light Phone. Wikipedia text shown in Wiki is [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/), by Wikipedia's contributors.

Wikipedia is a trademark of the Wikimedia Foundation. This project isn't endorsed by or affiliated with the Wikimedia Foundation or Light.

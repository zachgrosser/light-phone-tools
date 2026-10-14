# light-phone-tools

Tools for the **Light Phone III**, built on [Light's official SDK](https://github.com/lightphone/light-sdk).

## Wiki

Search and read Wikipedia on the Light Phone. Articles appear as clean text, one section at a time, with tappable links between them. Star an article to read it offline.

- A start-up screen credits Wikipedia and its CC BY-SA 4.0 license, and every article ends with its source, license and what was changed.
- No ads, analytics or tracking. Saved articles stay on the phone.
- Works on the Light Phone III's screen and adapts to smaller ones like the Light Flip's.

How it works, and why its attribution reads the way it does: [`tool/README.md`](../tool/README.md).

## Where to start

| I want to… | Read |
|---|---|
| Set up my computer, run Wiki on the emulator, run the tests | [DEVELOPING.md](../DEVELOPING.md) |
| Work on this with others (branches, pull requests, reviews) | [COLLABORATING.md](../COLLABORATING.md) |
| Send Wiki to Light for their Tool Library | [SUBMITTING.md](../SUBMITTING.md) |
| Know what Light accepts | Light's [TOOL_GUIDELINES.md](../TOOL_GUIDELINES.md) |
| Understand the SDK itself | Light's [README](../README.md) and [docs](../docs) |

## What's in this repo

This repo is a copy of Light's SDK with our tool added. **Our code lives in `tool/`**; everything else comes from Light and is updated from their repo.

| Folder / file | What it is | Do we edit it? |
|---|---|---|
| `tool/` | **Wiki**: its code, tests, settings (`lighttool.toml`) and license | ✅ Yes |
| `scripts/emulator.sh` | Sets up and runs Light's emulator on a Mac | ✅ Yes |
| `.github/` | This page, our build workflow, pull request and issue templates | ✅ Yes |
| `DEVELOPING.md`, `COLLABORATING.md`, `SUBMITTING.md` | Our guides | ✅ Yes |
| `examples/` | Light's sample tools (weather, authenticator, …) | 👀 Read for ideas |
| `docs/`, `README.md`, `TOOL_GUIDELINES.md`, `CONTRIBUTING.md` | Light's documentation | 👀 Read |
| `sdk/`, `plugin/`, `lint-rules/`, `builder/`, `signer/`, … | The SDK itself | ❌ No, it comes from Light |

> GitHub shows this file (`.github/README.md`) as the front page instead of Light's README, so updates from Light never clash with it.

## Licenses

- Wiki's code (`tool/`): [MIT](../tool/LICENSE).
- Light's SDK (everything else): [MIT](../LICENSE), © The Light Phone.
- Wikipedia article text shown by Wiki: [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/), by Wikipedia's contributors.

Wikipedia is a trademark of the Wikimedia Foundation. This project is not endorsed by or affiliated with the Wikimedia Foundation or with Light.

## Who's working on this

- [@zachgrosser](https://github.com/zachgrosser)
- *(add your name here in your first pull request)*

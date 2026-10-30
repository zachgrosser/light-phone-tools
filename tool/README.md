# Wiki

Search and read Wikipedia on the Light Phone. Settings are in [`lighttool.toml`](lighttool.toml) and the code is in [`src/main/kotlin/com/zachgrosser/wiki/`](src/main/kotlin/com/zachgrosser/wiki/).

## What it does

- Opens on a search box with Light's keyboard up and your saved articles underneath. Results come from Wikipedia's own search.
- Articles show their intro and a contents list. Each section opens on its own screen, with a link to the next one. Links to other articles work; images, tables, infoboxes and citations are left out.
- The star saves an article for reading offline.
- Settings (the gear) has the language and an About page covering sources, license, privacy and trademark.
- The first time Wiki opens, it shows a short credit for Wikipedia and its license.

Wiki only asks for the `INTERNET` permission and only talks to Wikipedia. No ads, analytics or tracking.

## Code

| File | |
|---|---|
| `HomeScreen.kt` | Search box, saved articles, first-launch credits |
| `SearchInput.kt` | The search box and the three ways of typing into it |
| `MultiTap.kt` | Keypad typing (2 = abc, 3 = def…) |
| `SearchScreen.kt` | Results |
| `ArticleScreen.kt`, `SectionScreen.kt` | Reading |
| `SettingsScreen.kt` | Settings, language list, About |
| `WikiApi.kt` | Search (`/w/rest.php/v1/search/page`) and articles (`/w/api.php?action=parse`) |
| `ArticleParser.kt` | Wikipedia HTML to plain-text blocks |
| `SavedArticles.kt` | Saved articles, as JSON in the tool's private storage |
| `Attribution.kt` | Credit, license and trademark text |
| `Language.kt`, `LanguageSetting.kt` | Languages and the translated text |
| `Models.kt` | Data classes |
| `WikiUi.kt` | Theme, text styles, article blocks, credits |
| `WikiPreviews.kt` | Android Studio previews at Light Phone III and Light Flip sizes |

`ArticleParser` is the fiddly part. It drops anything that doesn't suit a small text-only screen, mostly by HTML class. On English Wikipedia it also drops the reference sections (References, External links and so on), but keeps any "This article incorporates text from…" notice it finds there, since Wikimedia asks reusers to keep those. The tests use a real section of the "Light Phone" article and a made-up page full of edge cases.

## Typing

- **Light Phone III:** Light's on-screen keyboard is up from the start.
- **Bluetooth or USB keyboard:** typing goes straight into the search box and Enter searches. The on-screen keyboard gets out of the way after the first key; tap the search box to bring it back.
- **Phone keypad:** multi-tap, like older phones. Press 2 once for "a", twice for "b", hold for "2". 0 is a space.

`HardwareTyping` in `SearchInput.kt` grabs typing keys before Light's keyboard sees them, so nothing gets typed twice, and leaves every other key (volume, the Light Phone III's buttons) to LightOS.

The Light Flip isn't out yet and Light hasn't said how its keypad reaches tools. Wiki assumes it reports itself as a 12-key keypad and sends digits, like Android keypad phones do. Worth checking once Light publishes Flip support, along with whether it has a touchscreen.

## Languages

English, Spanish, French, German, Italian, Portuguese, Dutch and Swedish. One setting changes both Wiki's text and which Wikipedia it uses. Only Latin-alphabet languages, because that's all Light's keyboard can type. Wikipedia's search ignores most accents, so "Koln" finds Köln.

- The translations live in `Language.kt` rather than Android string resources, since switching an app's language needs a `Context` and Light doesn't give tools one.
- Credits, license, trademark and privacy text stay in English so the wording is exactly what Wikimedia's rules ask for. Each article's credit links to the Wikipedia it came from.
- Articles keep their language. Links open on the same Wikipedia, and saved articles from another language are marked, as in "Berlin (de)". Anything saved before 1.1 is English.
- On other Wikipedias the reference sections stay in, because the parser only recognises imported-text notices in English and dropping one isn't allowed.
- The translations still need a native speaker's review.

## Attribution and Wikipedia's rules

Wikipedia's text can be reused under CC BY-SA 4.0, with conditions. This is how Wiki meets each one. I'm not a lawyer; it's my reading of the rules. Check the sources before changing anything in `Attribution.kt`.

| Requirement | Source | How |
|---|---|---|
| Credit the authors with a link to the article (its history lists them) | [Reusing Wikipedia content](https://en.wikipedia.org/wiki/Wikipedia:Reusing_Wikipedia_content), [Terms of Use §7](https://foundation.wikimedia.org/wiki/Policy:Terms_of_Use) | Every article and section ends with the article's name and address, written out since there's no browser |
| Name the license and link it | [CC BY-SA 4.0 §3(a)](https://creativecommons.org/licenses/by-sa/4.0/legalcode.en) | Every article's credit, the first-launch screen and About |
| Say the text was changed | CC BY-SA 4.0 §3(a)(1)(B), Terms of Use §7 | "Changed for this screen: images, tables, citations and some formatting are left out." |
| Keep notices for text imported from elsewhere | Terms of Use §7 | Kept by the parser and shown at the end of the article |
| Point to the disclaimer of warranties | CC BY-SA 4.0 §3(a)(1)(A)(iv) | About links [Wikipedia's general disclaimer](https://en.wikipedia.org/wiki/Wikipedia:General_disclaimer) |
| Use "Wikipedia" only to describe the content; ™ at first use on small screens, full notice elsewhere | [Trademark Policy §2.2, §3.6](https://foundation.wikimedia.org/wiki/Policy:Trademark_policy) | The tool is called "Wiki" and has no logos. The first-launch screen says "Wikipedia™" and "Not affiliated with or endorsed by the Wikimedia Foundation"; About has the full notice |
| Identify the app, with contact details | [User-Agent Policy](https://foundation.wikimedia.org/wiki/Policy:Wikimedia_Foundation_User-Agent_Policy) | `LightPhoneWiki/<version> (https://github.com/zachgrosser/light-phone-tools)` |
| Back off when asked | [API Usage Guidelines](https://foundation.wikimedia.org/wiki/Policy:Wikimedia_Foundation_API_Usage_Guidelines) | One request per tap. On HTTP 429 Wiki says so and doesn't retry |
| Disclose data that leaves the phone | Light's [Tool Guidelines](../TOOL_GUIDELINES.md), [Wikimedia Privacy Policy](https://foundation.wikimedia.org/wiki/Policy:Privacy_policy) | About says what Wikimedia receives (searches, articles opened, IP address) and links its privacy policy |

The same rules and license cover every language's Wikipedia.

## Tests

```bash
./gradlew :tool:testDebugUnitTest -DlightSdk.toolOnly=true
```

[DEVELOPING.md](../DEVELOPING.md) covers the emulator, previews and getting a build onto a phone.

## License

MIT, see [LICENSE](LICENSE). Wikipedia's text belongs to its authors and is CC BY-SA 4.0.

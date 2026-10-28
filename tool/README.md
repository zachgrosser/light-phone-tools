# Wiki

A Light Phone tool for searching and reading Wikipedia. Settings are in [`lighttool.toml`](lighttool.toml); the code is in [`src/main/kotlin/com/zachgrosser/wiki/`](src/main/kotlin/com/zachgrosser/wiki/).

## What it does

- **Start-up screen** credits Wikipedia and states the CC BY-SA 4.0 license; tap it or start typing to skip.
- **Search** straight away: Wiki opens on the search box with Light's on-screen keyboard up and saved articles below. Results come from Wikipedia's own search, each with its one-line description.
- **Read**: the intro, then a contents list; each section opens on its own screen with a "Next" link. Links to other articles are tappable; images, tables, infoboxes and citations are left out.
- **Save** with the star to read offline. Saved articles open from the phone first, with or without a connection.
- **About** explains the source, license, accuracy, privacy, trademark and how to give feedback.
- **Language**, under About: English (the default), Español, Français, Deutsch, Italiano, Português, Nederlands or Svenska. See [Languages](#languages).

Wiki only needs the `INTERNET` permission. It has no ads, analytics or tracking, and talks only to Wikipedia: `en.wikipedia.org`, or the chosen language's (`de.wikipedia.org` and so on).

## How it works

| File | What it does |
|---|---|
| `HomeScreen.kt` | Start-up screen; the home screen (search box, saved articles, keyboard); About |
| `SearchInput.kt` | Typing a search: the search box, Light's on-screen keyboard, hardware keyboards and keypads |
| `MultiTap.kt` | Letters from a phone keypad: 2 = abc, 3 = def, … |
| `SearchScreen.kt` | Search results |
| `ArticleScreen.kt` | An article's intro and contents; saving it |
| `SectionScreen.kt` | Reading one section |
| `WikiApi.kt` | Requests to Wikipedia: search (`/w/rest.php/v1/search/page`) and articles (`/w/api.php?action=parse`) |
| `ArticleParser.kt` | Turns Wikipedia's HTML into plain-text blocks with links, bold and italics |
| `SavedArticles.kt` | Saves articles as JSON files in the tool's private storage |
| `Attribution.kt` | Every credit, license, trademark and privacy line the tool shows |
| `Language.kt` | The languages, their Wikipedias, and Wiki's own text in each |
| `LanguageSetting.kt` | Remembers the chosen language (`language.txt` in private storage) and provides it to every screen |
| `Models.kt` | The data: articles, sections, blocks, search results |
| `WikiUi.kt` | Shared look: theme, text styles, article blocks, credits |
| `WikiPreviews.kt` | Android Studio previews of every screen at Light Phone III and Light Flip sizes |

`ArticleParser` is the part most likely to need care. It keeps paragraphs, headings, lists, quotes and "Main article:" notes. It skips anything that doesn't suit a small text-only screen: images, tables, infoboxes, navigation boxes and citation markers. On English Wikipedia it also leaves out the reference sections (References, Notes, Sources, External links and so on), but keeps any credit notice found there for imported text (see below). Other languages keep those sections (see [Languages](#languages)); their citation lists and navigation boxes still go, by HTML class. Formulas become readable text, and superscript and subscript numbers become ² and ₂. Its tests use a real section of the "Light Phone" article plus a made-up page full of edge cases.

## Keyboards

The home screen is ready to type into as soon as it appears, whichever way the phone types:

- **Light Phone III:** Light's own on-screen keyboard (from the SDK) is up from the start. Return or the search button searches.
- **A Bluetooth or USB keyboard:** typing goes straight into the search box; Enter searches, Backspace deletes. After the first key, the on-screen keyboard steps aside so more saved articles fit; tapping the search box brings it back.
- **A phone keypad (Light Flip):** the digit keys type letters by multi-tap, as on older phones: press 2 once for "a", twice for "b"; pause (or press another key) to move on; hold a key for its digit; 0 is a space and 1 is punctuation.

`HardwareTyping` (in `SearchInput.kt`) handles all hardware keys. It takes typing keys before Light's on-screen keyboard sees them, so nothing is typed twice, and leaves every other key (volume, the Light Phone III's buttons) to LightOS.

**The Light Flip isn't out yet,** and Light hasn't said how its keypad reaches tools. Wiki assumes what Android phones with keypads do: the keypad reports itself as a 12-key keypad and sends digits. If LightOS turns presses into letters itself instead, Wiki types those like any other keyboard. Its multi-tap code only runs for 12-key keypads, so nothing else is affected. When Light publishes Flip support (or a Flip emulator), check this, and whether the Flip has a touchscreen: without one, results and links would need the keypad's arrow keys too.

## Languages

One setting, at the top of About, changes both Wiki's own text and the Wikipedia it searches. English is the default. Each language's text is in `Language.kt`; Wiki can't use Android's string resources for this, because switching an app's language needs an Android `Context`, which Light doesn't allow tools.

- **Only Latin-alphabet languages,** because Light's on-screen keyboard only has Latin layouts (QWERTY, Colemak, AZERTY). Wiki keeps the English QWERTY layout for all of them. Wikipedia's search mostly ignores accents, so typing "Koln" finds Köln.
- **Credits, license, trademark, privacy and the rest of About stay in English,** so their wording stays exactly as Wikimedia's rules require. Only Wiki's buttons, messages, the About intro and the language setting are translated. Each article's credit links to the Wikipedia it came from.
- **Articles remember their language.** Searches use the language chosen at the time; links in an article open articles on the same Wikipedia; saved articles keep working after switching, and the saved list marks ones from another language, as in "Berlin (de)". Articles saved before 1.1 are English.
- **Reference sections aren't left out in other languages.** On English Wikipedia, `ArticleParser` drops References, External links and so on, keeping any "This article incorporates text from…" notice it finds there. It only recognises those notices in English, so on other Wikipedias it keeps the sections rather than risk dropping one. A section like "Weblinks" (German: external links) therefore still appears in the contents.
- **The translations haven't been checked by native speakers yet.** They were drafted for 1.1; please have someone who speaks each language read through its section of `Language.kt` (and look at the screens) before relying on it.

## Attribution and Wikipedia's rules

Wikipedia's text is free to reuse under **CC BY-SA 4.0**, with conditions. Here is how Wiki meets each one, and where the rule comes from:

| Requirement | Where it comes from | How Wiki meets it |
|---|---|---|
| Credit the authors: a link or URL to the article (its history lists everyone who wrote it) | [Reusing Wikipedia content](https://en.wikipedia.org/wiki/Wikipedia:Reusing_Wikipedia_content); [Terms of Use §7](https://foundation.wikimedia.org/wiki/Policy:Terms_of_Use) | The end of every article and section names the article and gives its address (written out, since the phone has no browser) |
| Name the license and give its URL | [CC BY-SA 4.0 §3(a)](https://creativecommons.org/licenses/by-sa/4.0/legalcode.en); reuse policy | Every article's credit; the start-up screen; About |
| Say if the text was changed | CC BY-SA 4.0 §3(a)(1)(B); Terms of Use §7 | "Changed for this screen: images, tables, citations and some formatting are left out." |
| Keep notices for text imported from other sources | Terms of Use §7 ("reusers should preserve them") | `ArticleParser` keeps "This article incorporates text from…" notices, which Wikipedia shows under Sources or References, and the article screen shows them |
| Refer to the disclaimer of warranties | CC BY-SA 4.0 §3(a)(1)(A)(iv) | About links to [Wikipedia's general disclaimer](https://en.wikipedia.org/wiki/Wikipedia:General_disclaimer) |
| Use "Wikipedia" only to describe the content, without implying endorsement; notice near first use, or ™ where space is short with the full notice on an About screen | [Trademark Policy §2.2 and §3.6](https://foundation.wikimedia.org/wiki/Policy:Trademark_policy) | The name is "Wiki", with no logos. The start-up screen says "Wikipedia™" and "Not affiliated with or endorsed by the Wikimedia Foundation"; About has the policy's notice wording |
| Identify the app to Wikipedia's servers, with contact details | [User-Agent Policy](https://foundation.wikimedia.org/wiki/Policy:Wikimedia_Foundation_User-Agent_Policy) | `LightPhoneWiki/<version> (https://github.com/zachgrosser/light-phone-tools)` |
| Slow down when asked; don't hide that Wikimedia provides the service | [API Usage Guidelines](https://foundation.wikimedia.org/wiki/Policy:Wikimedia_Foundation_API_Usage_Guidelines) | One request per tap; on "429 Too Many Requests" Wiki tells the reader and doesn't retry; About says the Wikimedia Foundation runs Wikipedia |
| Disclose data that leaves the phone | Light's [Tool Guidelines](../TOOL_GUIDELINES.md); Wikimedia's [Privacy Policy](https://foundation.wikimedia.org/wiki/Policy:Privacy_policy) | About says what the Wikimedia Foundation receives (searches, articles opened, IP address) and links its privacy policy |

The wording lives in `Attribution.kt`, with the same sources noted at the top. **Check these sources before changing it.** We're not lawyers, and this is our reading of the rules, not legal advice.

Not covered: images (never shown). For other languages, Wiki applies the same rules: these policies cover every Wikipedia, and all of them use CC BY-SA 4.0. Imported-text notices are kept by keeping their sections (see [Languages](#languages)).

## Testing

```bash
./gradlew :tool:testDebugUnitTest -DlightSdk.toolOnly=true
```

See [DEVELOPING.md](../DEVELOPING.md) for the emulator, previews and installing on a phone.

## License

The code in this folder is under the [MIT License](LICENSE). Wikipedia text displayed by the tool belongs to its authors under CC BY-SA 4.0.

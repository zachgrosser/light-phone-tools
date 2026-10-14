# Wiki

A Light Phone tool for searching and reading Wikipedia. Settings are in [`lighttool.toml`](lighttool.toml); the code is in [`src/main/kotlin/com/zachgrosser/wiki/`](src/main/kotlin/com/zachgrosser/wiki/).

## What it does

- **Start-up screen** credits Wikipedia and states the CC BY-SA 4.0 license; tap to skip.
- **Search**, using Wikipedia's own search, with each result's one-line description.
- **Read**: the intro, then a contents list; each section opens on its own screen with a "Next" link. Links to other articles are tappable; images, tables, infoboxes and citations are left out.
- **Save** with the star to read offline. Saved articles open from the phone first, with or without a connection.
- **About** explains the source, license, accuracy, privacy, trademark and how to give feedback.

Wiki only needs the `INTERNET` permission. It has no ads, analytics or tracking, and talks only to `en.wikipedia.org`.

## How it works

| File | What it does |
|---|---|
| `HomeScreen.kt` | Start-up screen, search box, saved articles, About |
| `SearchScreen.kt` | The keyboard and search results |
| `ArticleScreen.kt` | An article's intro and contents; saving it |
| `SectionScreen.kt` | Reading one section |
| `WikiApi.kt` | Requests to Wikipedia: search (`/w/rest.php/v1/search/page`) and articles (`/w/api.php?action=parse`) |
| `ArticleParser.kt` | Turns Wikipedia's HTML into plain-text blocks with links, bold and italics |
| `SavedArticles.kt` | Saves articles as JSON files in the tool's private storage |
| `Attribution.kt` | Every credit, license, trademark and privacy line the tool shows |
| `Models.kt` | The data: articles, sections, blocks, search results |
| `WikiUi.kt` | Shared look: theme, text styles, article blocks, credits |
| `WikiPreviews.kt` | Android Studio previews of every screen at Light Phone III and Light Flip sizes |

`ArticleParser` is the part most likely to need care. It keeps paragraphs, headings, lists, quotes and "Main article:" notes. It skips anything that doesn't suit a small text-only screen: images, tables, infoboxes, navigation boxes and citation markers. It also leaves out the reference sections (References, Notes, Sources, External links and so on), but keeps any credit notice found there for imported text (see below). Formulas become readable text, and superscript and subscript numbers become ² and ₂. Its tests use a real section of the "Light Phone" article plus a made-up page full of edge cases.

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

Not covered: images (never shown), and non-English Wikipedias (Wiki uses English Wikipedia only).

## Testing

```bash
./gradlew :tool:testDebugUnitTest -DlightSdk.toolOnly=true
```

See [DEVELOPING.md](../DEVELOPING.md) for the emulator, previews and installing on a phone.

## License

The code in this folder is under the [MIT License](LICENSE). Wikipedia text displayed by the tool belongs to its authors under CC BY-SA 4.0.

package com.zachgrosser.wiki

/** Credit and license wording, kept in one place so every screen says the same thing. */
object Attribution {
    const val SOURCE = "Articles from Wikipedia, the free encyclopedia."
    const val LICENSE =
        "Text is available under the Creative Commons Attribution-ShareAlike 4.0 License (CC BY-SA 4.0)."
    const val LICENSE_URL = "creativecommons.org/licenses/by-sa/4.0"
    const val NOT_AFFILIATED = "Not affiliated with or endorsed by the Wikimedia Foundation."

    fun articleUrl(title: String) = "$WIKI_HOST/wiki/${title.replace(' ', '_')}"

    /** Shown under every article and section: where the text came from and what was left out. */
    fun forArticle(title: String) =
        "From the Wikipedia article “$title” by its contributors, ${articleUrl(title)}. " +
            "Text available under CC BY-SA 4.0. Shown without images, tables and citations."
}

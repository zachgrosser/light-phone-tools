package com.zachgrosser.wiki

// Credit, license and trademark wording. It follows CC BY-SA 4.0 §3(a), Wikipedia's reuse
// policy, and Wikimedia's Terms of Use (§7) and Trademark Policy (§2.2, §3.6). See tool/README.md
// before changing any of it. Always in English.
object Attribution {
    const val LICENSE_NAME = "Creative Commons Attribution-ShareAlike 4.0 License"
    const val LICENSE_SHORT = "CC BY-SA 4.0"
    const val LICENSE_URL = "creativecommons.org/licenses/by-sa/4.0"
    const val DISCLAIMER_URL = "en.wikipedia.org/wiki/Wikipedia:General_disclaimer"
    const val PRIVACY_POLICY_URL = "foundation.wikimedia.org/wiki/Policy:Privacy_policy"
    const val SOURCE_CODE_URL = "github.com/zachgrosser/light-phone-tools"
    const val ISSUES_URL = "github.com/zachgrosser/light-phone-tools/issues"

    // The trademark policy allows "™" at first use on small screens, with the full notice in About.
    const val SPLASH_SOURCE = "Articles from Wikipedia™, the free encyclopedia, written by its volunteer contributors."
    const val SPLASH_LICENSE = "Text available under the $LICENSE_NAME ($LICENSE_SHORT)."
    const val NOT_AFFILIATED = "Not affiliated with or endorsed by the Wikimedia Foundation."

    // Trademark Policy §2.2
    const val TRADEMARK_NOTICE =
        "Wikipedia is a trademark of the Wikimedia Foundation and is used with the permission of " +
            "the Wikimedia Foundation. Wiki is not endorsed by or affiliated with the Wikimedia Foundation."

    // Written out, since there's no browser to link to.
    fun articleUrl(title: String, language: Language = Language.English): String {
        val path = title.trim()
            .replace("%", "%25")
            .replace("?", "%3F")
            .replace(' ', '_')
        return "${language.host}/wiki/$path"
    }

    // CC BY-SA wants the authors (the article's page lists them), the license and a note of changes.
    fun forArticle(title: String, language: Language = Language.English) =
        "From the Wikipedia article “$title” by its contributors: ${articleUrl(title, language)}\n" +
            "Available under $LICENSE_SHORT: $LICENSE_URL\n" +
            "Changed for this screen: images, tables, citations and some formatting are left out."
}

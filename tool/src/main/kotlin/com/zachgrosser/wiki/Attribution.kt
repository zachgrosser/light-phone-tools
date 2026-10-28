package com.zachgrosser.wiki

/**
 * Every credit, license, trademark and privacy line Wiki shows, in one place.
 *
 * The wording follows:
 * - Wikipedia's reuse policy: en.wikipedia.org/wiki/Wikipedia:Reusing_Wikipedia_content
 * - CC BY-SA 4.0, section 3(a): creativecommons.org/licenses/by-sa/4.0/legalcode
 * - Wikimedia's Terms of Use, section 7: foundation.wikimedia.org/wiki/Policy:Terms_of_Use
 * - Wikimedia's Trademark Policy, sections 2.2 and 3.6:
 *   foundation.wikimedia.org/wiki/Policy:Trademark_policy
 * Check those before changing anything here.
 */
object Attribution {
    const val LICENSE_NAME = "Creative Commons Attribution-ShareAlike 4.0 License"
    const val LICENSE_SHORT = "CC BY-SA 4.0"
    const val LICENSE_URL = "creativecommons.org/licenses/by-sa/4.0"
    const val DISCLAIMER_URL = "en.wikipedia.org/wiki/Wikipedia:General_disclaimer"
    const val PRIVACY_POLICY_URL = "foundation.wikimedia.org/wiki/Policy:Privacy_policy"
    const val SOURCE_CODE_URL = "github.com/zachgrosser/light-phone-tools"
    const val ISSUES_URL = "github.com/zachgrosser/light-phone-tools/issues"

    // On small screens the trademark policy allows "™" at first use, with the full notice
    // (TRADEMARK_NOTICE) on the About screen.
    const val SPLASH_SOURCE = "Articles from Wikipedia™, the free encyclopedia, written by its volunteer contributors."
    const val SPLASH_LICENSE = "Text available under the $LICENSE_NAME ($LICENSE_SHORT)."
    const val NOT_AFFILIATED = "Not affiliated with or endorsed by the Wikimedia Foundation."

    /** The notice wording from section 2.2 of Wikimedia's Trademark Policy. */
    const val TRADEMARK_NOTICE =
        "Wikipedia is a trademark of the Wikimedia Foundation and is used with the permission of " +
            "the Wikimedia Foundation. Wiki is not endorsed by or affiliated with the Wikimedia Foundation."

    /** The article's address on Wikipedia, written out because the phone has no browser to link to. */
    fun articleUrl(title: String, language: Language = Language.English): String {
        val path = title.trim()
            .replace("%", "%25")
            .replace("?", "%3F")
            .replace(' ', '_')
        return "${language.host}/wiki/$path"
    }

    /**
     * The credit shown at the end of every article and section. CC BY-SA 4.0 needs the authors
     * (a link to the article, whose history lists them), the license with a link to it, and a
     * note that the text was changed. Always in English, whatever the article's language.
     */
    fun forArticle(title: String, language: Language = Language.English) =
        "From the Wikipedia article “$title” by its contributors: ${articleUrl(title, language)}\n" +
            "Available under $LICENSE_SHORT: $LICENSE_URL\n" +
            "Changed for this screen: images, tables, citations and some formatting are left out."
}

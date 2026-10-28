package com.zachgrosser.wiki

import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class LanguageTest {

    private val dir: File = createTempDirectory("language-test").toFile()

    @AfterTest
    fun cleanUp() {
        runBlocking { LanguageSetting.choose(dir, Language.English) }
        dir.deleteRecursively()
    }

    @Test
    fun `each language has its own Wikipedia`() {
        assertEquals("en.wikipedia.org", Language.English.host)
        assertEquals("de.wikipedia.org", Language.German.host)
        assertEquals(Language.entries.size, Language.entries.map { it.host }.toSet().size)
    }

    @Test
    fun `unknown or missing codes mean English`() {
        assertEquals(Language.Swedish, Language.fromCode("sv"))
        assertEquals(Language.English, Language.fromCode("xx"))
        assertEquals(Language.English, Language.fromCode(null))
    }

    @Test
    fun `every language has all of Wiki's text`() {
        for (language in Language.entries) {
            val s = language.strings
            val fixed = listOf(
                s.aboutButton, s.about, s.aboutIntro, s.language, s.saved, s.savedHint,
                s.searchPlaceholder, s.search, s.back, s.loading, s.searching, s.contents,
                s.savedForOffline, s.removedFromSaved, s.saveAction, s.removeAction,
                s.busy, s.offline, s.savedStillReadable,
            )
            assertTrue(fixed.all { it.isNotBlank() }, "${language.englishName} is missing some text")
            assertTrue("Zebra" in s.next("Zebra"), language.englishName)
            assertTrue("Zebra" in s.noArticle("Zebra"), language.englishName)
            assertTrue("Zebra" in s.noResults("Zebra"), language.englishName)
        }
    }

    @Test
    fun `the language setting is remembered`() = runBlocking {
        LanguageSetting.load(dir)
        assertEquals(Language.English, LanguageSetting.current.value) // nothing chosen yet

        LanguageSetting.choose(dir, Language.Dutch)
        LanguageSetting.load(dir)
        assertEquals(Language.Dutch, LanguageSetting.current.value)
        assertEquals("nl", File(dir, "language.txt").readText())
    }
}

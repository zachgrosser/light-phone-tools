package com.zachgrosser.wiki

import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class SavedArticlesTest {

    private val dir: File = createTempDirectory("saved-articles-test").toFile()
    private val store = SavedArticles(dir)

    @AfterTest
    fun cleanUp() {
        dir.deleteRecursively()
    }

    private fun article(title: String, pageId: Long) = Article(
        title = title,
        pageId = pageId,
        revisionId = 1,
        description = "About $title",
        lead = listOf(Block(BlockKind.Paragraph, "Text about $title.")),
        sections = emptyList(),
    )

    @Test
    fun `finds a saved article by its title or by the title that redirected to it`() = runBlocking {
        store.save(article("United Kingdom", 1), aliases = listOf("UK"))

        assertEquals("United Kingdom", store.find("United Kingdom")?.title)
        assertEquals("United Kingdom", store.find("united_Kingdom")?.title) // first letter and _ don't matter
        assertEquals("United Kingdom", store.find("UK")?.title)
        assertNull(store.find("France"))
        assertTrue(store.isSaved(1))
    }

    @Test
    fun `removing an article forgets it everywhere`() = runBlocking {
        store.save(article("United Kingdom", 1), aliases = listOf("UK"))
        store.remove(1)

        assertNull(store.find("UK"))
        assertFalse(store.isSaved(1))
        assertTrue(store.list().isEmpty())
        assertFalse(File(dir, "saved-articles/1.json").exists())
    }

    @Test
    fun `lists the most recently saved first`() = runBlocking {
        store.save(article("Cat", 1), aliases = emptyList())
        Thread.sleep(5)
        store.save(article("Dog", 2), aliases = emptyList())
        Thread.sleep(5)
        store.save(article("Cat", 1), aliases = emptyList()) // saving again moves it to the top

        assertEquals(listOf("Cat", "Dog"), store.list().map { it.title })
    }

    @Test
    fun `a damaged index counts as empty and can be saved over`() = runBlocking {
        File(dir, "saved-articles").mkdirs()
        File(dir, "saved-articles/index.json").writeText("not json")

        assertTrue(store.list().isEmpty())
        store.save(article("Cat", 1), aliases = emptyList())
        assertEquals(listOf("Cat"), store.list().map { it.title })
    }

    @Test
    fun `articles saved before credit notices existed still open`() = runBlocking {
        File(dir, "saved-articles").mkdirs()
        File(dir, "saved-articles/index.json").writeText("""{"articles":[{"title":"Cat","pageId":5}]}""")
        File(dir, "saved-articles/5.json")
            .writeText("""{"title":"Cat","pageId":5,"revisionId":1,"lead":[],"sections":[]}""")

        val cat = store.find("Cat")
        assertEquals("Cat", cat?.title)
        assertEquals(emptyList(), cat?.notices)
    }
}

package com.zachgrosser.wiki

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Articles saved for offline reading. Each one is a JSON file in the tool's private storage,
 * plus a small index so the list can be shown without reading every article.
 */
class SavedArticles(filesDir: File) {

    private val directory = File(filesDir, "saved-articles")
    private val indexFile = File(directory, "index.json")

    /** Most recently saved first. */
    suspend fun list(): List<SavedArticle> = locked {
        readIndex().sortedByDescending { it.savedAtMillis }
    }

    /** Finds a saved article by its title or by a title that redirected to it. */
    suspend fun find(title: String): Article? = locked {
        val key = normalize(title)
        val entry = readIndex().firstOrNull { saved ->
            normalize(saved.title) == key || saved.aliases.any { normalize(it) == key }
        } ?: return@locked null
        runCatching { json.decodeFromString(Article.serializer(), articleFile(entry.pageId).readText()) }.getOrNull()
    }

    suspend fun isSaved(pageId: Long): Boolean = locked {
        readIndex().any { it.pageId == pageId }
    }

    suspend fun save(article: Article, aliases: Collection<String>) = locked {
        directory.mkdirs()
        writeAtomically(articleFile(article.pageId), json.encodeToString(Article.serializer(), article))
        val entry = SavedArticle(
            title = article.title,
            pageId = article.pageId,
            description = article.description,
            aliases = aliases.filter { normalize(it) != normalize(article.title) }.distinct(),
            savedAtMillis = System.currentTimeMillis(),
        )
        writeIndex(readIndex().filterNot { it.pageId == article.pageId } + entry)
    }

    suspend fun remove(pageId: Long) = locked {
        articleFile(pageId).delete()
        writeIndex(readIndex().filterNot { it.pageId == pageId })
    }

    private fun articleFile(pageId: Long) = File(directory, "$pageId.json")

    private fun readIndex(): List<SavedArticle> {
        if (!indexFile.exists()) return emptyList()
        return runCatching { json.decodeFromString(SavedIndex.serializer(), indexFile.readText()).articles }
            .getOrDefault(emptyList())
    }

    private fun writeIndex(articles: List<SavedArticle>) {
        directory.mkdirs()
        writeAtomically(indexFile, json.encodeToString(SavedIndex.serializer(), SavedIndex(articles)))
    }

    /** Writes to a temporary file first so a crash can't leave a half-written file behind. */
    private fun writeAtomically(target: File, content: String) {
        val temp = File(target.parentFile, "${target.name}.tmp")
        temp.writeText(content)
        if (!temp.renameTo(target)) {
            target.delete()
            temp.renameTo(target)
        }
    }

    private suspend fun <T> locked(block: () -> T): T =
        withContext(Dispatchers.IO) { lock.withLock { block() } }

    companion object {
        private val lock = Mutex()
        private val json = Json { ignoreUnknownKeys = true }

        /** Wikipedia titles ignore the case of the first letter and treat "_" as a space. */
        fun normalize(title: String): String =
            title.replace('_', ' ').trim().replaceFirstChar { it.uppercaseChar() }
    }
}

@Serializable
private data class SavedIndex(val articles: List<SavedArticle> = emptyList())

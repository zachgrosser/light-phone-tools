package com.zachgrosser.wiki

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

// One JSON file per article, plus an index for the list. Page ids are only unique within one
// Wikipedia, so entries are keyed by language too.
class SavedArticles(filesDir: File) {

    private val directory = File(filesDir, "saved-articles")
    private val indexFile = File(directory, "index.json")

    suspend fun list(): List<SavedArticle> = locked {
        readIndex().sortedByDescending { it.savedAtMillis }
    }

    // Matches the title or any title that redirected to it.
    suspend fun find(title: String, language: String = Language.English.code): Article? = locked {
        val key = normalize(title)
        val entry = readIndex().firstOrNull { saved ->
            saved.language == language &&
                (normalize(saved.title) == key || saved.aliases.any { normalize(it) == key })
        } ?: return@locked null
        runCatching {
            json.decodeFromString(Article.serializer(), articleFile(entry.language, entry.pageId).readText())
        }.getOrNull()
    }

    suspend fun isSaved(pageId: Long, language: String = Language.English.code): Boolean = locked {
        readIndex().any { it.pageId == pageId && it.language == language }
    }

    suspend fun save(article: Article, aliases: Collection<String>) = locked {
        directory.mkdirs()
        writeAtomically(articleFile(article.language, article.pageId), json.encodeToString(Article.serializer(), article))
        val entry = SavedArticle(
            title = article.title,
            pageId = article.pageId,
            language = article.language,
            description = article.description,
            aliases = aliases.filter { normalize(it) != normalize(article.title) }.distinct(),
            savedAtMillis = System.currentTimeMillis(),
        )
        writeIndex(readIndex().filterNot { it.pageId == article.pageId && it.language == article.language } + entry)
    }

    suspend fun remove(pageId: Long, language: String = Language.English.code) = locked {
        articleFile(language, pageId).delete()
        writeIndex(readIndex().filterNot { it.pageId == pageId && it.language == language })
    }

    // English files keep their pre-1.1 names.
    private fun articleFile(language: String, pageId: Long) =
        File(directory, if (language == Language.English.code) "$pageId.json" else "$language-$pageId.json")

    private fun readIndex(): List<SavedArticle> {
        if (!indexFile.exists()) return emptyList()
        return runCatching { json.decodeFromString(SavedIndex.serializer(), indexFile.readText()).articles }
            .getOrDefault(emptyList())
    }

    private fun writeIndex(articles: List<SavedArticle>) {
        directory.mkdirs()
        writeAtomically(indexFile, json.encodeToString(SavedIndex.serializer(), SavedIndex(articles)))
    }

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

        // Wikipedia ignores the case of the first letter and treats "_" as a space.
        fun normalize(title: String): String =
            title.replace('_', ' ').trim().replaceFirstChar { it.uppercaseChar() }
    }
}

@Serializable
private data class SavedIndex(val articles: List<SavedArticle> = emptyList())

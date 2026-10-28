package com.zachgrosser.wiki

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * The language chosen on the About screen, kept in a one-line file in the tool's private storage.
 * Shared by every screen; English until the file has been read, and when there isn't one.
 */
object LanguageSetting {

    private const val FILE_NAME = "language.txt"

    private val _current = MutableStateFlow(Language.English)
    val current: StateFlow<Language> = _current

    suspend fun load(filesDir: File) {
        _current.value = withContext(Dispatchers.IO) {
            Language.fromCode(runCatching { File(filesDir, FILE_NAME).readText().trim() }.getOrNull())
        }
    }

    suspend fun choose(filesDir: File, language: Language) {
        _current.value = language
        withContext(Dispatchers.IO) {
            filesDir.mkdirs()
            File(filesDir, FILE_NAME).writeText(language.code)
        }
    }
}

/** The chosen language, provided to every screen by WikiTheme. */
val LocalLanguage = staticCompositionLocalOf { Language.English }

/** Wiki's text in the chosen language. */
@Composable
fun strings(): UiStrings = LocalLanguage.current.strings

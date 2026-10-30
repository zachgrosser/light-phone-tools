package com.zachgrosser.wiki

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

object LanguageSetting {

    private const val FILE_NAME = "language.txt"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _current = MutableStateFlow(Language.English)
    val current: StateFlow<Language> = _current

    suspend fun load(filesDir: File) {
        _current.value = withContext(Dispatchers.IO) {
            Language.fromCode(runCatching { File(filesDir, FILE_NAME).readText().trim() }.getOrNull())
        }
    }

    fun choose(filesDir: File, language: Language): Job {
        _current.value = language
        return scope.launch {
            filesDir.mkdirs()
            File(filesDir, FILE_NAME).writeText(language.code)
        }
    }
}

val LocalLanguage = staticCompositionLocalOf { Language.English }

@Composable
fun strings(): UiStrings = LocalLanguage.current.strings

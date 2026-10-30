package com.zachgrosser.wiki

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Wikimedia's User-Agent policy asks for a name and a way to get in touch.
private val USER_AGENT = "LightPhoneWiki/${BuildConfig.VERSION_NAME} (https://${Attribution.SOURCE_CODE_URL})"

class ArticleNotFoundException(val title: String) : Exception("No article called $title")

// HTTP 429. Wikimedia asks apps to back off, so this is never retried automatically.
class WikipediaBusyException : Exception("Wikipedia asked for fewer requests")

fun errorMessage(error: Throwable, strings: UiStrings): String = when (error) {
    is WikipediaBusyException -> strings.busy
    else -> strings.offline
}

object WikiApi {

    private val json = Json { ignoreUnknownKeys = true }

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) { json(json) }
        install(HttpTimeout) {
            connectTimeoutMillis = 15_000
            requestTimeoutMillis = 30_000
        }
        defaultRequest { header(HttpHeaders.UserAgent, USER_AGENT) }
    }

    suspend fun search(language: Language, query: String): Result<List<SearchResult>> = apiCall {
        val response = client.get("https://${language.host}/w/rest.php/v1/search/page") {
            parameter("q", query)
            parameter("limit", 20)
        }
        checkStatus(response)
        response.body<SearchResponse>().pages.map { page ->
            SearchResult(title = page.title, description = page.description?.trim()?.ifEmpty { null })
        }
    }

    suspend fun fetchArticle(language: Language, title: String): Result<Article> = apiCall {
        val response = client.get("https://${language.host}/w/api.php") {
            parameter("action", "parse")
            parameter("page", title)
            parameter("prop", "text|revid")
            parameter("redirects", 1)
            parameter("disableeditsection", 1)
            parameter("disabletoc", 1)
            parameter("disablelimitreport", 1)
            parameter("format", "json")
            parameter("formatversion", 2)
        }
        checkStatus(response)
        val body = response.body<ParseResponse>()
        body.error?.let { apiError ->
            if (apiError.code == "missingtitle" || apiError.code == "invalidtitle") throw ArticleNotFoundException(title)
            error(apiError.info.ifEmpty { apiError.code })
        }
        val parsed = checkNotNull(body.parse) { "Wikipedia sent an empty response" }
        withContext(Dispatchers.Default) {
            ArticleParser.parse(parsed.title, parsed.pageid, parsed.revid, parsed.text, language)
        }
    }

    private fun checkStatus(response: HttpResponse) {
        if (response.status == HttpStatusCode.TooManyRequests) throw WikipediaBusyException()
        check(response.status.isSuccess()) { "Wikipedia answered HTTP ${response.status.value}" }
    }

    // runCatching, minus swallowing cancellation.
    private suspend fun <T> apiCall(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}

@Serializable
private data class SearchResponse(val pages: List<SearchPage> = emptyList())

@Serializable
private data class SearchPage(val title: String, val description: String? = null)

@Serializable
private data class ParseResponse(val parse: ParseBody? = null, val error: ApiError? = null)

@Serializable
private data class ParseBody(
    val title: String,
    val pageid: Long = 0,
    val revid: Long = 0,
    val text: String = "",
)

@Serializable
private data class ApiError(val code: String = "", val info: String = "")

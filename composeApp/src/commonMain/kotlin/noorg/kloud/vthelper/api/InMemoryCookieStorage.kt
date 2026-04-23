package noorg.kloud.vthelper.api

import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.fillDefaults
import io.ktor.client.plugins.cookies.matches
import io.ktor.http.*
import io.ktor.util.date.*
import kotlinx.atomicfu.*
import kotlinx.coroutines.sync.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.*

/** copied from [io.ktor.client.plugins.cookies.AcceptAllCookiesStorage] but with an ability to get all cookies */
class InMemoryCookieStorage(private val clock: () -> Long = { getTimeMillis() }) :
    CookiesStorage {

    @Serializable
    data class CookieWithTimestamp(val cookie: Cookie, val createdAt: Long)

    private val container: MutableList<CookieWithTimestamp> = mutableListOf()
    private val oldestCookieTime: AtomicLong = atomic(0L)
    private val mutex = Mutex()

    override suspend fun get(requestUrl: Url): List<Cookie> = mutex.withLock {
        val now = clock()
        if (now >= oldestCookieTime.value) cleanup(now)

        val cookies = container.filter { it.cookie.matches(requestUrl) }.map { it.cookie }
        return@withLock cookies
    }

    fun loadAllFromJson(jsonInput: String?) {
        if (jsonInput.isNullOrEmpty()) {
            return
        }
        try {
            // SerializationException - in case of any decoding-specific error
            // IllegalArgumentException - if the decoded input is not a valid instance of T
            val cookies = Json.decodeFromString<List<CookieWithTimestamp>>(jsonInput)
            loadAll(cookies)
        } catch (e: Exception) {
            println(e)
        }
    }

    fun getAllAsJson(): String {
        return Json.encodeToString(getAll())
    }

    fun loadAll(cookies: List<CookieWithTimestamp>) {
        clear()
        container.addAll(cookies)
    }

    fun getAll(): List<CookieWithTimestamp> {
        return container
    }

    fun clear() {
        container.clear()
    }

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        if (cookie.name.isBlank()) return

        val fullCookie = cookie.fillDefaults(requestUrl)

        mutex.withLock {
            container.removeAll { (existingCookie, _) ->
                // NOTE: Original used existingCookie.matches(requestUrl),
                // but this matched and removed cookies if the replacement was more specific
                // e.g existing on /path, new one on /path/subpath. Existing one was removed which caused errors
                val shouldBeRemoved =
                    existingCookie.name == fullCookie.name
                            && existingCookie.domain == fullCookie.domain
                            && existingCookie.path == fullCookie.path
                if (shouldBeRemoved) {
                    println(
                        "Removing existing cookie ${existingCookie.name} on ${existingCookie.domain}${existingCookie.path}"
                    )
                }
                return@removeAll shouldBeRemoved
            }
            val createdAt = clock()
            println("Adding cookie ${fullCookie.name} = '${fullCookie.value.take(25)}'... on ${fullCookie.domain}${fullCookie.path}")
            container.add(CookieWithTimestamp(fullCookie, createdAt))

            fullCookie.maxAgeOrExpires(createdAt)?.let {
                if (oldestCookieTime.value > it) {
                    oldestCookieTime.value = it
                }
            }
        }
    }

    override fun close() = Unit

    private fun updateOldestCookieTime() {
        oldestCookieTime.value = container.fold(Long.MAX_VALUE) { acc, (cookie, createdAt) ->
            cookie.maxAgeOrExpires(createdAt)
                ?.let {
                    min(acc, it)
                } ?: acc
        }
    }

    private fun cleanup(timestamp: Long) {
        container.removeAll { (cookie, createdAt) ->
            val expiresTs = cookie.maxAgeOrExpires(createdAt) ?: return@removeAll false
            val isExpired = expiresTs < timestamp
            if (isExpired) {
                println(
                    "Removing expired cookie ${cookie.name} on ${cookie.domain}${cookie.path}"
                )
            }
            return@removeAll isExpired
        }
        updateOldestCookieTime()
    }

    fun removeCookiesByName(name: String) {
        container.removeAll { (cookie, _) ->
            val matches = cookie.name == name
            if (matches) {
                println(
                    "Removing cookie by name ${cookie.name} on ${cookie.domain}${cookie.path}"
                )
            }
            return@removeAll matches
        }
        updateOldestCookieTime()
    }

    private fun Cookie.maxAgeOrExpires(createdAt: Long): Long? =
        maxAge?.let { createdAt + it * 1000L } ?: expires?.timestamp
}
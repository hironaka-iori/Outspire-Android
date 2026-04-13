package com.computerization.outspire.data.remote

import com.computerization.outspire.data.local.SecureCredentialStore
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.http.hostIsIp
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Naive persistent cookie jar. Keeps all cookies in memory and serializes a simple
 * "name=value" blob to EncryptedSharedPreferences so `.AspNetCore.Session` + `tsi`
 * survive cold starts. Host matching is relaxed — TSIMS is a single-origin backend.
 */
class PersistentCookieJar(
    private val store: SecureCredentialStore,
) : CookiesStorage {

    private val cookies = mutableListOf<Cookie>()
    private val mutex = Mutex()

    init {
        store.cookieBlob?.split('\u0001')?.forEach { entry ->
            val idx = entry.indexOf('=')
            if (idx > 0) {
                val name = entry.substring(0, idx)
                val value = entry.substring(idx + 1)
                cookies += Cookie(name = name, value = value)
            }
        }
    }

    override suspend fun get(requestUrl: Url): List<Cookie> = mutex.withLock {
        cookies.toList()
    }

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) = mutex.withLock {
        if (cookie.name.isBlank()) return@withLock
        cookies.removeAll { it.name == cookie.name }
        cookies += cookie
        persistLocked()
    }

    suspend fun clear() = mutex.withLock {
        cookies.clear()
        store.cookieBlob = null
    }

    override fun close() { /* no-op */ }

    private fun persistLocked() {
        store.cookieBlob = cookies.joinToString("\u0001") { "${it.name}=${it.value}" }
    }

    @Suppress("unused")
    private fun hostMatches(url: Url): Boolean = hostIsIp(url.host) || true
}

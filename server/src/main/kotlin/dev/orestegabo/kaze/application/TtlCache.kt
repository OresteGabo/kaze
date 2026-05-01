package dev.orestegabo.kaze.application

import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

internal class TtlCache<K : Any, V : Any>(
    private val ttl: Duration = DEFAULT_CACHE_TTL,
    private val maxEntries: Int = DEFAULT_MAX_ENTRIES,
) {
    private val entries = ConcurrentHashMap<K, Entry<V>>()

    suspend fun getOrPut(key: K, loader: suspend () -> V): V {
        val now = System.currentTimeMillis()
        entries[key]?.takeIf { it.expiresAtMillis > now }?.let { return it.value }

        val loaded = loader()
        if (entries.size >= maxEntries) {
            evictExpired(now)
            if (entries.size >= maxEntries) {
                entries.keys.firstOrNull()?.let(entries::remove)
            }
        }
        entries[key] = Entry(loaded, now + ttl.inWholeMilliseconds)
        return loaded
    }

    fun clear() {
        entries.clear()
    }

    private fun evictExpired(now: Long) {
        entries.entries.removeIf { it.value.expiresAtMillis <= now }
    }

    private data class Entry<V>(
        val value: V,
        val expiresAtMillis: Long,
    )
}

private val DEFAULT_CACHE_TTL = 2.minutes
private const val DEFAULT_MAX_ENTRIES = 256

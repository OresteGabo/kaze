package dev.orestegabo.kaze.application

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.LongAdder
import kotlinx.coroutines.CompletableDeferred
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

internal class TtlCache<K : Any, V : Any>(
    private val ttl: Duration = DEFAULT_CACHE_TTL,
    private val maxEntries: Int = DEFAULT_MAX_ENTRIES,
) {
    private val entries = ConcurrentHashMap<K, Entry<V>>()
    private val inFlightLoads = ConcurrentHashMap<K, CompletableDeferred<V>>()
    private val hitCount = LongAdder()
    private val missCount = LongAdder()
    private val loadCount = LongAdder()
    private val loadFailureCount = LongAdder()
    private val coalescedWaitCount = LongAdder()
    private val expiredEvictionCount = LongAdder()
    private val capacityEvictionCount = LongAdder()

    suspend fun getOrPut(key: K, loader: suspend () -> V): V {
        readValidEntry(key)?.let { return it }

        missCount.increment()
        val inFlight = CompletableDeferred<V>()
        val existingLoad = inFlightLoads.putIfAbsent(key, inFlight)
        if (existingLoad != null) {
            coalescedWaitCount.increment()
            return existingLoad.await()
        }

        try {
            readValidEntry(key)?.let {
                inFlight.complete(it)
                return it
            }

            val loaded = loader()
            val now = System.currentTimeMillis()
            if (entries.size >= maxEntries) {
                evictExpired(now)
                if (entries.size >= maxEntries) {
                    entries.keys.firstOrNull()?.let { evictedKey ->
                        entries.remove(evictedKey)
                        capacityEvictionCount.increment()
                    }
                }
            }
            entries[key] = Entry(loaded, now + ttl.inWholeMilliseconds)
            loadCount.increment()
            inFlight.complete(loaded)
            return loaded
        } catch (cause: Throwable) {
            loadFailureCount.increment()
            inFlight.completeExceptionally(cause)
            throw cause
        } finally {
            inFlightLoads.remove(key, inFlight)
        }
    }

    fun clear() {
        entries.clear()
    }

    fun invalidate(key: K) {
        entries.remove(key)
    }

    fun snapshot(name: String? = null): TtlCacheSnapshot =
        TtlCacheSnapshot(
            name = name,
            ttlMillis = ttl.inWholeMilliseconds,
            maxEntries = maxEntries,
            currentEntries = entries.size,
            inFlightLoads = inFlightLoads.size,
            hits = hitCount.sum(),
            misses = missCount.sum(),
            loads = loadCount.sum(),
            loadFailures = loadFailureCount.sum(),
            coalescedWaits = coalescedWaitCount.sum(),
            expiredEvictions = expiredEvictionCount.sum(),
            capacityEvictions = capacityEvictionCount.sum(),
        )

    private fun readValidEntry(key: K): V? {
        val now = System.currentTimeMillis()
        val entry = entries[key] ?: return null
        return if (entry.expiresAtMillis > now) {
            hitCount.increment()
            entry.value
        } else {
            if (entries.remove(key, entry)) {
                expiredEvictionCount.increment()
            }
            null
        }
    }

    private fun evictExpired(now: Long) {
        entries.entries.removeIf { candidate ->
            val shouldRemove = candidate.value.expiresAtMillis <= now
            if (shouldRemove) {
                expiredEvictionCount.increment()
            }
            shouldRemove
        }
    }

    private data class Entry<V>(
        val value: V,
        val expiresAtMillis: Long,
    )
}

internal data class TtlCacheSnapshot(
    val name: String? = null,
    val ttlMillis: Long,
    val maxEntries: Int,
    val currentEntries: Int,
    val inFlightLoads: Int,
    val hits: Long,
    val misses: Long,
    val loads: Long,
    val loadFailures: Long,
    val coalescedWaits: Long,
    val expiredEvictions: Long,
    val capacityEvictions: Long,
) {
    val requests: Long
        get() = hits + misses

    val hitRate: Double
        get() = if (requests == 0L) 0.0 else hits.toDouble() / requests.toDouble()
}

private val DEFAULT_CACHE_TTL = 2.minutes
private const val DEFAULT_MAX_ENTRIES = 256

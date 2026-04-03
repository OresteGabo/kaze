package dev.orestegabo.kaze.platform

interface SecureStore {
    suspend fun put(key: String, value: String)
    suspend fun get(key: String): String?
    suspend fun remove(key: String)
}

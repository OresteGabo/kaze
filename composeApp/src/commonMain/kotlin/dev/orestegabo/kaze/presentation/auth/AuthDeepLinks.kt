package dev.orestegabo.kaze.presentation.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal object AuthDeepLinks {
    const val CALLBACK_URI = "kaze://auth/callback"

    private val _callbacks = MutableSharedFlow<AuthCallback>(extraBufferCapacity = 1)
    val callbacks: SharedFlow<AuthCallback> = _callbacks.asSharedFlow()

    fun handle(url: String): Boolean {
        val callback = parseAuthCallback(url) ?: return false
        _callbacks.tryEmit(callback)
        return true
    }
}

internal data class AuthCallback(
    val loginToken: String,
    val state: String?,
)

fun handleKazeAuthDeepLink(url: String): Boolean = AuthDeepLinks.handle(url)

private fun parseAuthCallback(url: String): AuthCallback? {
    if (!url.startsWith(AuthDeepLinks.CALLBACK_URI)) return null
    val query = url.substringAfter("?", missingDelimiterValue = "")
    if (query.isBlank()) return null
    val params = query.split("&")
        .mapNotNull { part ->
            val key = part.substringBefore("=", missingDelimiterValue = "").urlDecode()
            val value = part.substringAfter("=", missingDelimiterValue = "").urlDecode()
            if (key.isBlank()) null else key to value
        }
        .toMap()
    val loginToken = params["login_token"]?.takeIf { it.isNotBlank() } ?: return null
    return AuthCallback(loginToken = loginToken, state = params["state"])
}

private fun String.urlDecode(): String =
    replace("+", " ")
        .replace(Regex("%[0-9a-fA-F]{2}")) { match ->
            match.value.drop(1).toInt(16).toChar().toString()
        }

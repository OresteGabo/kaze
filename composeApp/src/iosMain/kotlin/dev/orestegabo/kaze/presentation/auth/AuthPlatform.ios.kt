package dev.orestegabo.kaze.presentation.auth

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import platform.Foundation.NSBundle
import platform.Foundation.NSURL
import platform.UIKit.UIDevice
import platform.UIKit.UIApplication

internal actual fun createPlatformAuthHttpClient(json: Json): HttpClient =
    HttpClient(Darwin) {
        install(HttpTimeout) {
            requestTimeoutMillis = AUTH_REQUEST_TIMEOUT_MS
            connectTimeoutMillis = AUTH_CONNECT_TIMEOUT_MS
            socketTimeoutMillis = AUTH_REQUEST_TIMEOUT_MS
        }
        install(ContentNegotiation) {
            json(json)
        }
    }

internal actual fun defaultAuthApiBaseUrl(): String =
    (NSBundle.mainBundle.objectForInfoDictionaryKey("KAZE_API_BASE_URL") as? String)
        ?.takeIf { it.isNotBlank() }
        ?: "http://127.0.0.1:8080/api/v1"

internal actual fun defaultDeviceLabel(): String =
    UIDevice.currentDevice.name

internal actual fun createExternalUrlLauncher(): ExternalUrlLauncher =
    IosExternalUrlLauncher()

private class IosExternalUrlLauncher : ExternalUrlLauncher {
    override fun open(url: String): Boolean {
        val nsUrl = NSURL.URLWithString(url) ?: return false
        UIApplication.sharedApplication.openURL(
            url = nsUrl,
            options = emptyMap<Any?, Any>(),
            completionHandler = null,
        )
        return true
    }
}

private const val AUTH_CONNECT_TIMEOUT_MS = 5_000L
private const val AUTH_REQUEST_TIMEOUT_MS = 8_000L

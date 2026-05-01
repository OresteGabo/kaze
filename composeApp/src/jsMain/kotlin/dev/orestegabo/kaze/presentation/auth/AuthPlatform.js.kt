package dev.orestegabo.kaze.presentation.auth

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.browser.window
import kotlinx.serialization.json.Json

internal actual fun createPlatformAuthHttpClient(json: Json): HttpClient =
    HttpClient(Js) {
        install(HttpTimeout) {
            requestTimeoutMillis = AUTH_REQUEST_TIMEOUT_MS
            connectTimeoutMillis = AUTH_CONNECT_TIMEOUT_MS
            socketTimeoutMillis = AUTH_REQUEST_TIMEOUT_MS
        }
        install(ContentNegotiation) {
            json(json)
        }
    }

internal actual fun defaultAuthApiBaseUrl(): String = "${window.location.origin}/api/v1"

internal actual fun defaultDeviceLabel(): String = "Web"

internal actual fun createExternalUrlLauncher(): ExternalUrlLauncher =
    object : ExternalUrlLauncher {
        override fun open(url: String): Boolean {
            window.location.href = url
            return true
        }
    }

internal actual fun createNativeSocialAuthLauncher(): NativeSocialAuthLauncher =
    NoopNativeSocialAuthLauncher

private const val AUTH_CONNECT_TIMEOUT_MS = 5_000L
private const val AUTH_REQUEST_TIMEOUT_MS = 8_000L
